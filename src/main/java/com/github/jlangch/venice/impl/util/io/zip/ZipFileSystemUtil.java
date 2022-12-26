/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util.io.zip;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.github.jlangch.venice.VncException;


public class ZipFileSystemUtil {

    public static FileSystem mountZip(final File zip) throws IOException {
        try {
            return FileSystems.newFileSystem(
                    zip.toPath(),
                    Zipper.class.getClassLoader());
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                        "Failed to mount ZIP filesystem from '%s'",
                        zip.getPath()));
        }
    }

    public static ByteBuffer loadBinaryFileFromZip(
            final File zip,
            final File file
    ) {
        if (!zip.exists()) {
            throw new VncException(String.format(
                    "The ZIP file '%s' does not exist",
                    zip.getPath()));
        }

        try {
            try (FileSystem zipFS = mountZip(zip)) {
                final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
                return ByteBuffer.wrap(data);
            }
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                        "Failed to load binary file '%s' from ZIP '%s'",
                        file.getPath(),
                        zip.getPath()));
        }
    }

    public static String loadTextFileFromZip(
            final File zip,
            final File file,
            final Charset charset
    ) {
        if (!zip.exists()) {
            throw new VncException(String.format(
                    "The ZIP file '%s' does not exist",
                    zip.getPath()));
        }

        try {
            try (FileSystem zipFS = mountZip(zip)) {
                final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
                return new String(data, charset);
            }
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                        "Failed to load file '%s' from ZIP '%s'",
                        file.getPath(),
                        zip.getPath()));
        }
    }

    public static InputStream getInputStreamFromZip(
            final File zip,
            final File file
    ) {
        if (!zip.exists()) {
            throw new VncException(String.format(
                    "The ZIP file '%s' does not exist",
                    zip.getPath()));
        }

        try {
            try (FileSystem zipFS = mountZip(zip)) {
                // The Inflater used with the zipFS is closed, create new stream
                final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
                return new ByteArrayInputStream(data);
            }
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                        "Failed to return an InputStream for file '%s' from ZIP '%s'",
                        file.getPath(),
                        zip.getPath()));
        }
    }

    public static BufferedReader getBufferedReaderFromZip(
            final File zip,
            final File file,
            final Charset charset
    ) {
        if (!zip.exists()) {
            throw new VncException(String.format(
                    "The ZIP file '%s' does not exist",
                    zip.getPath()));
        }

        try {
            try (FileSystem zipFS = mountZip(zip)) {
                // The Inflater used with the zipFS is closed, create new reader
                final byte[] data = Files.readAllBytes(zipFS.getPath(file.getPath()));
                return new BufferedReader(new StringReader(new String(data, charset)));
            }
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                        "Failed to return a BufferedReader for file '%s' from ZIP '%s'",
                        file.getPath(),
                        zip.getPath()));
        }
    }

}
