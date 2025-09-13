/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.ipc.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.json.VncJsonReader;
import com.github.jlangch.venice.impl.util.json.VncJsonWriter;
import com.github.jlangch.venice.nanojson.JsonAppendableWriter;
import com.github.jlangch.venice.nanojson.JsonReader;
import com.github.jlangch.venice.nanojson.JsonWriter;


public class IO {

    public static ByteBuffer readFrame(
            final SocketChannel ch
    ) {
        try {
            final ByteBuffer len = ByteBuffer.allocate(4);
            readFully(ch, len);
            len.flip();
            final int n = len.getInt();

            if (n < 0 || n > (1 << 24)) {
                throw new IOException("Bad length: " + n);
            }

            if (n == 0) {
                return ByteBuffer.allocate(0);
            }
            else {
                final ByteBuffer data = ByteBuffer.allocate(n);
                readFully(ch, data);
                data.flip();
                return data;
            }
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(IOException ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new VncException("Failed to read data from channel, channel was closed!", ex);
            }
            else {
                throw new VncException("Failed to read data from channel!", ex);
            }
        }
    }

    public static void writeFrame(
            final SocketChannel ch,
            final ByteBuffer data
    ) {
        try {
            if (data == null) {
                final ByteBuffer len = ByteBuffer.allocate(4).putInt(0);
                len.flip();
                writeFully(ch, len);
            }
            else {
                final ByteBuffer len = ByteBuffer.allocate(4).putInt(data.remaining());
                len.flip();
                writeFully(ch, len);
                writeFully(ch, data);
            }
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(Exception ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new VncException("Failed to write data to channel, channel was closed!", ex);
            }
            else {
                throw new VncException("Failed to write data to channel!", ex);
            }
        }
    }

    public static void readFully(
            final SocketChannel ch,
            final ByteBuffer buf
    ) {
        try {
            while (buf.hasRemaining()) {
                if (ch.read(buf) < 0) {
                    throw new EofException("Failed to read data from channel, channel EOF reached!");
                }
            }
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(Exception ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new VncException("Failed to read data from channel, channel was closed!", ex);
            }
            else {
                throw new VncException("Failed to read data from channel!", ex);
            }
        }
    }

    public static void writeFully(
            final SocketChannel ch,
            final ByteBuffer buf
    ) {
        try {
            while (buf.hasRemaining()) ch.write(buf);
        }
        catch(Exception ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new VncException("Failed to write data to channel, channel was closed!", ex);
            }
            else {
                throw new VncException("Failed to write data to channel!", ex);
            }
        }
    }

    public static VncVal readJson(
            final String json,
            final boolean mapKeysToKeywords
    ) {
        try {
            final Function<VncVal,VncVal> keyFn = t -> CoreFunctions.keyword.applyOf(t);
            return new VncJsonReader(
                        JsonReader.from(json),
                        mapKeysToKeywords ? keyFn : null,
                        null,
                        false).read();
        }
        catch(Exception ex) {
            throw new VncException("Failed to parse JSON data to Venice data!", ex);
        }
    }

    public static String writeJson(final VncVal val) {
        final StringBuilder sb = new StringBuilder();
        final JsonAppendableWriter writer = JsonWriter.indent("  ").on(sb);
        new VncJsonWriter(writer, false).write(val).done();
        return sb.toString();
    }

    public static void safeClose(final SocketChannel ch) {
        if (ch != null) {
            try {
                ch.close();
            }
            catch(Exception ignore) { }
        }
    }
}
