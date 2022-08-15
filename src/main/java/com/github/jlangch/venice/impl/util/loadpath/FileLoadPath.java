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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;


public class FileLoadPath extends LoadPath {

    public FileLoadPath(final File file) {
        this.file = canonical(file);
    }

    @Override
    public File path() {
        return file;
    }

    @Override
    public boolean isOnPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        final File f = canonical(file);
        return this.file.equals(f);
    }

    @Override
    public ByteBuffer load(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        final File f = canonical(file);
        if (this.file.equals(f)) {
            try {
                return ByteBuffer.wrap(Files.readAllBytes(f.toPath()));
            }
            catch(IOException ex) {
                return null; // just proceed and try with next load path
            }
        }

        return null;
    }


    private final File file;  // absolute & canonical -> see constructor!
}
