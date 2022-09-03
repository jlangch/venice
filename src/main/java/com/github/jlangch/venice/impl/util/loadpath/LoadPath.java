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
package com.github.jlangch.venice.impl.util.loadpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;

import com.github.jlangch.venice.VncException;


public abstract class LoadPath {

    abstract File path();

    abstract boolean isOnPath(final File file, final Access mode);

    abstract boolean isRegularFileOnLoadPath(File file, final Access mode);

    abstract boolean isDirectoryOnLoadPath(File file, final Access mode);

    abstract ByteBuffer load(final File file) throws IOException;

    abstract InputStream getInputStream(File file) throws IOException;

    abstract BufferedReader getBufferedReader(File file, Charset charset) throws IOException;

    abstract OutputStream getOutputStream(File file, OpenOption... options) throws IOException;

    public File canonical(final File file) {
        try {
            return file.getAbsoluteFile().getCanonicalFile();
        }
        catch(IOException ex) {
            throw new VncException(
                    String.format(
                            "The file '%s' can not be converted to a canonical path!",
                            file.getPath()),
                    ex);
        }
    }


    public static LoadPath of(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isDirectory()) {
            // existing directory
            return new DirectoryLoadPath(file);
        }
        else if (file.isFile()) {
            // existing regular file
            if (ZipLoadPath.isZipFile(file)) {
                return new ZipLoadPath(file);
            }
            else {
                return new FileLoadPath(file);
            }
        }

        throw new VncException(
                String.format(
                        "The file '%s' is not a valid load path. It is neither " +
                        "an existing regular file or directory!",
                        file.getPath()));
    }

}
