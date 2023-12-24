/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.modules;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ModuleLoader {

    public static String loadModule(final String module) {
        if (!Modules.isValidModule(module)) {
            throw new VncException(String.format(
                    "The Venice module '%s' does not exist",
                    module));
        }

        final String name = module + ".venice";

        try {
            return modules.computeIfAbsent(
                    name,
                    k -> new ClassPathResource(Venice.class.getPackage(), k)
                                .getResourceAsString("UTF-8"));
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                    "Failed to load Venice module '%s'", name),
                    ex);
        }
    }

    public static String loadClasspathVeniceFile(final String file) {
        // Just allow to load venice scripts!
        //
        // This function is exclusively called by the Venice function
        // `core/load-classpath-file` that will fail anyway if someone points
        // to a file with no valid Venice source content!
        if (!file.endsWith(".venice")) {
            throw new VncException(String.format(
                    "Must not load other than Venice (*.venice) resources from "
                        + "classpath. Resource: '%s'",
                    file));
        }

        try {
            final IInterceptor interceptor = ThreadContext.getInterceptor();

            return classpathFiles.computeIfAbsent(
                    file,
                    k -> loadClasspathVeniceFile(file, interceptor));
        }
        catch (SecurityException ex) {
            throw ex;
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                    "Failed to load Venice classpath file '%s'", file),
                    ex);
        }
    }

    public static String loadExternalVeniceFile(final String file) {
        // Just allow to load venice scripts!
        //
        // This function is exclusively called by the Venice function
        // `core/load-file` that will fail anyway if someone points
        // this file to a file with no valid Venice source content!
        if (!file.endsWith(".venice")) {
            throw new VncException(String.format(
                    "Must not load other than Venice (*.venice) files. "
                        + "File: '%s'",
                    file));
        }

        final File f = new File(file);

        final IInterceptor interceptor = ThreadContext.getInterceptor();

        final String data = interceptor.getLoadPaths().loadVeniceFile(f);

        if (data == null) {
            throw new VncException("The file '" + f + "' does not exist!");
        }
        else {
            externalFiles.put(f.getName(), data);
            return data;
        }
    }

    public static boolean isLoadedModule(final String module) {
        return modules.containsKey(module);
    }

    public static boolean isLoadedClasspathFile(final String file) {
        return classpathFiles.containsKey(file);
    }

    public static boolean isLoadedExternalFile(final String file) {
        return externalFiles.containsKey(file);
    }

    public static String getCachedLoadedModule(final String module) {
        return modules.get(module);
    }

    public static String getCachedClasspathFile(final String file) {
        return classpathFiles.get(file);
    }

    public static String getCachedExternalFile(final String file) {
        return externalFiles.get(file);
    }

    public static void clear() {
        modules.clear();
        classpathFiles.clear();
        externalFiles.clear();
    }

    private static String loadClasspathVeniceFile(
            final String file,
            final IInterceptor interceptor
    ) {
        try {
            return new String(interceptor.onLoadClassPathResource(file), "UTF-8");
        }
        catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("UnsupportedEncoding", ex);
        }
    }


    private static final Map<String,String> modules = new ConcurrentHashMap<>();
    private static final Map<String,String> classpathFiles = new ConcurrentHashMap<>();
    private static final Map<String,String> externalFiles = new ConcurrentHashMap<>();
}
