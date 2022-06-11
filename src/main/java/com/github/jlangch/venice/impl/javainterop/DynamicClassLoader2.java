/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.javainterop;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.jlangch.venice.impl.VeniceInterpreter;


public class DynamicClassLoader2 extends URLClassLoader {

    public DynamicClassLoader2() {
        this(getParentClassLoader());
    }

    public DynamicClassLoader2(final ClassLoader parent) {
        super(EMPTY_URLS, parent);
        systemClassLoader = getSystemClassLoader();
    }


    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(
            final String name,
            final boolean resolve
    ) throws ClassNotFoundException {
        // has the class loaded already?
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                if (systemClassLoader != null) {
                    loadedClass = systemClassLoader.loadClass(name);
                }
            }
            catch (ClassNotFoundException ex) {
                // class not found in system class loader... silently skipping
            }

            try {
                // find the class from given jar urls as in first constructor parameter.
                if (loadedClass == null) {
                    loadedClass = findClass(name);
                }
            }
            catch (ClassNotFoundException e) {
                // class is not found in the given urls.
                // Let's try it in parent classloader.
                // If class is still not found, then this method will throw class not found ex.
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {	  // marked to resolve
            resolveClass(loadedClass);
        }

        return loadedClass;
    }



    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final List<URL> allRes = new LinkedList<>();

        // load resources from sys class loader
        final Enumeration<URL> sysResources = systemClassLoader.getResources(name);
        if (sysResources != null) {
            while (sysResources.hasMoreElements()) {
                allRes.add(sysResources.nextElement());
            }
        }

        // load resource from this classloader
        final Enumeration<URL> thisRes = findResources(name);
        if (thisRes != null) {
            while (thisRes.hasMoreElements()) {
                allRes.add(thisRes.nextElement());
            }
        }

        // then try finding resources from parent classloaders
        final Enumeration<URL> parentRes = super.findResources(name);
        if (parentRes != null) {
            while (parentRes.hasMoreElements()) {
                allRes.add(parentRes.nextElement());
            }
        }

        return new Enumeration<URL>() {
            final Iterator<URL> it = allRes.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public URL getResource(final String name) {
        URL res = null;
        if (systemClassLoader != null) {
            res = systemClassLoader.getResource(name);
        }
        if (res == null) {
            res = findResource(name);
        }
        if (res == null) {
            res = super.getResource(name);
        }
        return res;
    }


    private static ClassLoader getParentClassLoader() {
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        final ClassLoader vncClassLoader = VeniceInterpreter.class.getClassLoader();

        if (ctxClassLoader == null) {
            return vncClassLoader;
        }
        else if (ctxClassLoader == sysClassLoader) {
            return vncClassLoader;
        }
        else if (DynamicClassLoader2.class.getSimpleName().equals(
                    ctxClassLoader.getClass().getSimpleName())) {
            return vncClassLoader;
        }
        else {
            return ctxClassLoader;
        }
    }



    private static final URL[] EMPTY_URLS = new URL[]{};

    private final ClassLoader systemClassLoader;
}
