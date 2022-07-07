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
package com.github.jlangch.venice.impl.util.io;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;


public class ClassPathResource {

    public ClassPathResource(final String path) {
        this(path, null, null);
    }

    public ClassPathResource(final Package pkg, final String name) {
        this(toPath(pkg, name), null, null);
    }

    public ClassPathResource(final String path, final Class<?> clazz) {
        this(path, clazz, null);
    }

    public ClassPathResource(final String path, final ClassLoader classLoader) {
        this(path, null, classLoader);
    }

    public ClassPathResource(
            final String path,
            final Class<?> clazz,
            final ClassLoader classLoader
    ) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("A 'path' must not be blank");
        }

        this.path = path;
        this.clazz = clazz;
        this.classLoader = classLoader;
    }

    public InputStream getInputStream() {
        InputStream is = null;

        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        }
        else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        }

        if (is == null) {
            is = Thread.currentThread()
                       .getContextClassLoader()
                       .getResourceAsStream(this.path);
        }

        if (is == null) {
            is = ClassLoader.getSystemClassLoader()
                            .getResourceAsStream(this.path);
        }

        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }

        return is;
    }

    public URL getResource() {
        URL url = null;

        if (this.clazz != null) {
            url = this.clazz.getResource(this.path);
        }
        else if (this.classLoader != null) {
            url = this.classLoader.getResource(this.path);
        }

        if (url == null) {
            url = Thread.currentThread()
                        .getContextClassLoader()
                        .getResource(this.path);
        }

        if (url == null) {
            url = ClassLoader.getSystemClassLoader()
                             .getResource(this.path);
        }

        return url;
    }

    public byte[] getResourceAsBinary() {
        try(InputStream is = getInputStream()) {
            if (is == null) {
                throw new RuntimeException(
                        String.format(
                                "Classpath resource %s not found",
                                path));
            }
            else {
                return IOStreamUtil.copyIStoByteArray(is);
            }
        }
        catch(RuntimeException ex) {
            if (ex.getMessage().startsWith("Classpath resource ")) {
                throw ex;
            }
            else {
                throw new RuntimeException(
                        String.format(
                                "Failed to load classpath resource '%s'",
                                path),
                        ex);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(
                    String.format(
                        "Failed to load classpath resource '%s'",
                        path),
                    ex);
        }
     }

    public ByteBuffer getResourceAsByteBuffer() {
        final byte[] data = getResourceAsBinary();
        return data == null ? null : ByteBuffer.wrap(data);
    }

    public String getResourceAsString() {
        return getResourceAsString("UTF-8");
    }

    public String getResourceAsString(final String charsetName) {
        try {
            return new String(getResourceAsBinary(), charsetName);
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(
                    String.format(
                        "Failed to load classpath resource '%s' as string. "
                            + "The charset name '%' is not supported!",
                        path,
                        charsetName),
                    ex);
        }
    }

    public static String toPath(final Package pkg, final String name) {
        return pkg.getName().replace('.', '/') + "/" + name;
    }


    private final String path;
    private final ClassLoader classLoader;
    private final Class<?> clazz;
}
