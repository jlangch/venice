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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.zip.ZipFileSystemUtil;
import com.github.jlangch.venice.impl.util.io.zip.Zipper;


public class ZipLoadPath extends LoadPath {

    public ZipLoadPath(final File zip) {
        this.zip = canonical(zip);
        this.entries.addAll(list(zip));
    }

    @Override
    public File path() {
        return zip;
    }

    @Override
    public boolean isOnPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        return file.isAbsolute()
                ? false
                : entries.contains(file.getPath());
    }

    @Override
    public ByteBuffer load(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            return null;
        }
        else {
            try {
                return ZipFileSystemUtil
                            .loadBinaryFileFromZip(zip, file)
                            .getValue();
            }
            catch(Exception ex) {
                return null;
            }
        }
    }

    public static boolean isZipFile(final File file) {
        if (file.getName().endsWith(".zip")) {
            try (FileInputStream is = new FileInputStream(file)) {
                final byte[] buffer = new byte[4];
                if (is.read(buffer) == buffer.length) {
                    return Zipper.isZipFile(buffer);
                }

                return false;
            }
            catch(IOException ex) {
                throw new VncException(
                        String.format(
                                "The file '%s' is not a valid load path. It is not a " +
                                "zip file even though the file extension is '.zip'!",
                                file.getPath()));
            }
        }
        else {
            return false;
        }
    }

    private static List<String> list(final File zip) {
        try (ZipFile zf = new ZipFile(zip, ZipFile.OPEN_READ)) {
            return zf.stream()
                     .map(ZipEntry::getName)
                     .collect(Collectors.toList());
        }
        catch(IOException ex) {
            throw new VncException(
                    String.format( "Failed list the zip file's '%s' entries!",zip.getPath()));
        }
    }


    private final File zip;  // absolute & canonical -> see constructor!
    private final Set<String> entries = new HashSet<>();
}
