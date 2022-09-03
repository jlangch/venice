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

import com.github.jlangch.venice.VncException;


public class DirectoryLoadPath extends LoadPath {

    public DirectoryLoadPath(final File dir) {
        this.dir = canonical(dir);
    }

    @Override
    public File path() {
        return dir;
    }

    @Override
    public boolean isOnPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        try {
            final File f = realFile(file);
            return isFileWithinDirectory(f);
        }
        catch (Exception ex) {
            throw new VncException(
                        String.format(
                                "Failed to check if the file '%s' is on the load path",
                                file.getPath()),
                        ex);
        }
    }

    @Override
    public ByteBuffer load(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        final File f = realFile(file);
        return f.isFile() && isFileWithinDirectory(f)
                ? ByteBuffer.wrap(Files.readAllBytes(f.toPath()))
                : null;
    }

    @Override
    public InputStream getInputStream(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        final File f = realFile(file);
        return f.isFile() && isFileWithinDirectory(f)
                ? Files.newInputStream(f.toPath())
                : null;
    }

    @Override
    public BufferedReader getBufferedReader(final File file, final Charset charset) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        final File f = realFile(file);
        return f.isFile() && isFileWithinDirectory(f)
                ? Files.newBufferedReader(f.toPath(), charset)
                : null;
    }

    @Override
    public OutputStream getOutputStream(final File file, final OpenOption... options) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }


        final File f = realFile(file);
        return isFileWithinDirectory(f)
                ? Files.newOutputStream(f.toPath(), options)
                : null;
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

        return isOnPath(file) && file.isDirectory();
    }

    @Override
    public String toString() {
    	return dir.getPath();
    }

    private File realFile(final File file) {
        return file.isAbsolute() ? file : new File(dir, file.getPath());
    }

    private boolean isFileWithinDirectory(final File file) {
        if (canonical(file).toPath().startsWith(dir.toPath())) {
            // Prevent accessing files outside the load-path
            //
            // Load path:  [/Users/pit/scripts]
            // E.g.: foo.venice                     =>  /Users/pit/scripts/foo.venice          (ok)
            //       /Users/pit/scripts/foo.venice  =>  /Users/pit/scripts/foo.venice          (ok)
            //       ../bar/foo.venice              =>  /Users/pit/scripts/../bar/foo.venice   (!!!)
            //       /Users/pit/foo.venice          =>  /Users/pit/foo.venice                  (!!!)
            return true;
        }

        return false;
    }


    private final File dir;  // absolute & canonical -> see constructor!
}
