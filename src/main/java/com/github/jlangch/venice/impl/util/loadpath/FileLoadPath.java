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
import java.nio.file.Files;
import java.nio.file.OpenOption;


public class FileLoadPath extends LoadPath {

    public FileLoadPath(final File file) {
        lpFile = canonical(file);
    }

    @Override
    public File path() {
        return lpFile;
    }

    @Override
    public boolean isOnPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            return lpFile.equals(canonical(file));
        }
        else {
         	return file.getParent() == null && lpFile.getName().equals(file.getName());
        }
    }

    @Override
    public ByteBuffer load(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            final File f = canonical(file);
            if (lpFile.equals(f) && f.isFile()) {
                return ByteBuffer.wrap(Files.readAllBytes(f.toPath()));
            }
        }
        else {
            if ((file.getParent() == null) && lpFile.getName().equals(file.getName())) {
                return ByteBuffer.wrap(Files.readAllBytes(lpFile.toPath()));
            }
        }

        return null;
    }

    @Override
    public InputStream getInputStream(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            final File f = canonical(file);
            if (lpFile.equals(f) && f.isFile()) {
                return Files.newInputStream(f.toPath());
            }
        }
        else {
            if ((file.getParent() == null) && lpFile.getName().equals(file.getName())) {
                return Files.newInputStream(lpFile.toPath());
            }
        }

        return null;
    }

    @Override
    public BufferedReader getBufferedReader(final File file, final Charset charset) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            final File f = canonical(file);
            if (lpFile.equals(f) && f.isFile()) {
                return Files.newBufferedReader(f.toPath(), charset);
            }
        }
        else {
            if ((file.getParent() == null) && lpFile.getName().equals(file.getName())) {
                return Files.newBufferedReader(lpFile.toPath(), charset);
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(final File file, final OpenOption... options) throws IOException {
        return null;   // not supported
    }

    @Override
    public boolean isRegularFileOnLoadPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        return isOnPath(file) && file.isFile();
    }

    @Override
    public boolean isDirectoryOnLoadPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        return false;
    }

    @Override
    public String toString() {
    	return lpFile.getPath();
    }


    private final File lpFile;  // absolute & canonical -> see constructor!
}
