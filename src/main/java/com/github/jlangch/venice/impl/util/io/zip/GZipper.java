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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


/**
 * A helper to compress/uncompress binary data blocks using the gzip
 * inflater/deflater.
 *
 */
public class GZipper {

    public static byte[] gzip(final byte[] binary) {
        if (binary == null) {
            throw new IllegalArgumentException("A 'binary' must not be null");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(binary, 0, binary.length);
                gzos.flush();
            }

            return baos.toByteArray();
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] gzip(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A 'file' must not be null");
        }

        try (InputStream is = new FileInputStream(file)) {
            return gzip(is);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] gzip(final InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("An 'is' must not be null");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                IOStreamUtil.copy(is, gzos);
                gzos.flush();
            }

            return baos.toByteArray();
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void gzip(final byte[] binary, final OutputStream os) {
        if (binary == null) {
            throw new IllegalArgumentException("A 'binary' must not be null");
        }
        if (os == null) {
            throw new IllegalArgumentException("An 'os' must not be null");
        }

        try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
            gzos.write(binary, 0, binary.length);
            gzos.flush();
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void gzip(final InputStream is, final OutputStream os) {
        if (is == null) {
            throw new IllegalArgumentException("An 'is' must not be null");
        }
        if (os == null) {
            throw new IllegalArgumentException("An 'os' must not be null");
        }

        try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
            IOStreamUtil.copy(is, gzos);
            gzos.flush();
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] ungzip(final byte[] binary) {
        if (binary == null) {
            throw new IllegalArgumentException("A 'binary' must not be null");
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(binary);

        try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
            return slurpBytes(gzis);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] ungzip(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A 'file' must not be null");
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return ungzip(is);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static byte[] ungzip(final InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("A 'inputStream' must not be null");
        }

        try (GZIPInputStream gzis = new GZIPInputStream(inputStream)) {
            return slurpBytes(gzis);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static InputStream ungzipToStream(final byte[] binary) {
        if (binary == null) {
            throw new IllegalArgumentException("A 'binary' must not be null");
        }

        return ungzipToStream(new ByteArrayInputStream(binary));
    }

    public static InputStream ungzipToStream(final InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("An 'inputStream' must not be null");
        }

        try {
            return new GZIPInputStream(inputStream);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static boolean isGZipFile(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("A 'file' must not be null");
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return isGZipFile(is);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static boolean isGZipFile(final InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("An 'is' must not be null");
        }

        try {
            is.mark(2);
            final byte[] bytes = IOStreamUtil.copyIStoByteArray(is, 2);
            is.reset();

            return isGZipFile(bytes);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static boolean isGZipFile(final byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }

        return ByteBuffer.wrap(bytes).getShort() == GZIP_HEADER;
    }

    private static byte[] slurpBytes(final Object source) throws IOException {
        if (source == null) {
            return new byte[0];
        }
        else {
            if (source instanceof byte[]) {
                return (byte[])source;
            }
            else if (source instanceof InputStream) {
                return IOStreamUtil.copyIStoByteArray((InputStream)source);
            }
            else if (source instanceof File) {
                try (FileInputStream fis = new FileInputStream((File)source)) {
                    return IOStreamUtil.copyIStoByteArray(fis);
                }
            }
            else {
                throw new IllegalArgumentException(
                        "Only entry values of type byte[], File or InputStream are supported!");
            }
        }
    }


    public static final short GZIP_HEADER = 0x1f8b;
}
