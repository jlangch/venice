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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
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
        else if (entries.contains(file.getPath())) {
            try {
                return ZipFileSystemUtil
                            .loadBinaryFileFromZip(zip, file);
            }
            catch(Exception ex) {
                return null;
            }
        }
        else {
           return null;
        }
    }

    @Override
    public InputStream getInputStream(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            return null;
        }
        else if (entries.contains(file.getPath())) {
            try {
                return ZipFileSystemUtil.getInputStreamFromZip(zip, file);
            }
            catch(Exception ex) {
                return null;
            }
        }
        else {
           return null;
        }
    }

    @Override
    public BufferedReader getBufferedReader(final File file, final Charset charset) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        if (file.isAbsolute()) {
            return null;
        }
        else if (entries.contains(file.getPath())) {
            try {
                return ZipFileSystemUtil.getBufferedReaderFromZip(zip, file, charset);
            }
            catch(Exception ex) {
                return null;
            }
        }
        else {
           return null;
        }
    }

    @Override
    public OutputStream getOutputStream(final File file, final OpenOption... options) {
        return null;   // not supported
    }

    @Override
    public boolean isRegularFileOnLoadPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        return isOnPath(file);
    }

    @Override
    public boolean isDirectoryOnLoadPath(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A file must not be null");
        }

        return false;  // not supported
    }


    public static boolean isZipFile(final File file) {
        if (file.getName().endsWith(".zip")) {
            try {
                return Zipper.isZipFile(readFirstNBytes(file, 4));
            }
            catch(Exception ex) {
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

    private static byte[] readFirstNBytes(final File file, final int n) throws Exception {
        try (FileInputStream is = new FileInputStream(file)) {
            final byte[] buffer = new byte[n];
            if (is.read(buffer) == buffer.length) {
                return buffer;
            }
            else {
                return null;
            }
        }
    }


    private final File zip;  // absolute & canonical -> see constructor!
    private final Set<String> entries = new HashSet<>();
}
