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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.NullOutputStream;


public class IOStreamUtil {

    public static byte[] copyIStoByteArray(
            final InputStream is
    ) throws IOException {
        if (is == null) {
            return null;
        }

        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[16 * 1024];
            int n;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }

            return output.toByteArray();
        }
    }

    public static byte[] copyIStoByteArray(
            final InputStream input,
            final int numBytesToCopy
    ) throws IOException {
        if (numBytesToCopy < 0) {
            throw new IllegalArgumentException(
                    "numBytesToCopy must not be negative: " + numBytesToCopy);
        }

        if (numBytesToCopy == 0) {
            return new byte[0];
        }

        final byte[] data = new byte[numBytesToCopy];
        int offset = 0;
        int read;

        while (offset < numBytesToCopy
                && (read = input.read(data, offset, numBytesToCopy - offset)) != -1
        ) {
            offset += read;
        }

        if (offset != numBytesToCopy) {
            throw new IOException(
                    "Unexpected read size. current: " + offset
                        + ", expected: " + numBytesToCopy);
        }

        return data;
    }

    public static String copyIStoString(
            final InputStream is,
            final String charsetName
    ) throws IOException{
    	return is == null
                ? null
                : new String(copyIStoByteArray(is), CharsetUtil.charset(charsetName));
    }

    public static String copyIStoString(
            final InputStream is,
            final Charset charset
    ) throws IOException{
        return is == null
                ? null
                : new String(copyIStoByteArray(is), CharsetUtil.charset(charset));
    }

    public static void copyByteArrayToOS(
            final byte[] data,
            final OutputStream os
    ) throws IOException{
        if (os == null || data == null) {
            return;
        }

        os.write(data);
        os.flush();
    }

    public static void copyFileToOS(
            final File file,
            final OutputStream os
    ) throws IOException{
        if (os == null || file == null) {
            return;
        }

        try (FileInputStream is = new FileInputStream(file)) {
            copy(is, os);
        }
    }

    public static void copyStringToOS(
            final String data,
            final OutputStream os,
            final String charsetName
    ) throws IOException{
        if (os == null || data == null) {
            return;
        }

        os.write(data.getBytes(CharsetUtil.charset(charsetName)));
        os.flush();
    }


    public static void copy(final InputStream is, final OutputStream os)
    throws IOException {
        int len;
        byte[] buf=new byte[4096];

        while ((len=is.read(buf))!=-1) {
            os.write(buf,0,len);
        }

        os.flush();
    }


    public static PrintStream nullPrintStream() {
        return new PrintStream(new NullOutputStream(), true);
    }

    public static BufferedReader nullBufferedReader() {
        return new BufferedReader(new InputStreamReader(new NullInputStream()));
    }
}
