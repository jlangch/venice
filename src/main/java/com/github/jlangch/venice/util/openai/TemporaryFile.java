/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.openai;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class TemporaryFile implements AutoCloseable {

    public TemporaryFile(final File tmpDir, final File file) {
        this.tmpDir = tmpDir;
        this.file = file;
    }

    public static TemporaryFile of(final byte[] data, final String fileName) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(fileName);

        final String normalizedFileName = new File(fileName).getName();

        File tmpDir = null;
        File file = null;

        try {
            tmpDir = Files.createTempDirectory("openai-upload").toFile();
            file = new File(tmpDir, normalizedFileName);
            FileUtil.save(data, file, true);
            return new TemporaryFile(tmpDir, file);
        }
        catch (Exception ex) {
            clear(tmpDir, file);
            throw new VncException("Failed to create TemporaryFile '" + normalizedFileName + "'");
        }
    }

    public static TemporaryFile of(final InputStream is, final String fileName) {
        Objects.requireNonNull(is);
        Objects.requireNonNull(fileName);

        File tmpDir = null;
        File file = null;

        try {
            tmpDir = Files.createTempDirectory("openai-upload").toFile();
            file = new File(tmpDir, fileName);
            FileUtil.save(is, file, true);
            return new TemporaryFile(tmpDir, file);
        }
        catch (Exception ex) {
            clear(tmpDir, file);
            throw new VncException("Failed to create TemporaryFile '" + fileName + "'");
        }
    }


    public File getFile() {
        return file;
    }

    public Path getPath() {
        return file.toPath();
    }

    @Override
    public void close() throws Exception {
        clear(tmpDir, file);
    }


    private static void clear(final File tmpDir, final File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            if (tmpDir.exists()) {
                tmpDir.delete();
            }
        }
        catch (Exception ignore) {
        }
    }


    private final File tmpDir;
    private final File file;
}
