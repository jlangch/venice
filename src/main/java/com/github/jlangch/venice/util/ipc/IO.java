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
package com.github.jlangch.venice.util.ipc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.github.jlangch.venice.VncException;


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

            final ByteBuffer data = ByteBuffer.allocate(n);
            readFully(ch, data);
            data.flip();

            return data;
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
            final ByteBuffer len = ByteBuffer.allocate(4).putInt(data.remaining());
            len.flip();
            writeFully(ch, len);
            writeFully(ch, data);
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
                    throw new IOException("EOF");
                }
            }
        }
        catch(Exception ex) {
            if ((ex instanceof IOException) && ("EOF".equals(ex.getMessage()))) {
                throw new VncException("Failed to read data from channel, channel EOF reached!", ex);
            }
            else if (ExceptionUtil.isBrokenPipeException(ex)) {
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
    
    
    public static void safeClose(final SocketChannel ch) {
        if (ch != null) {
            try {
                ch.close();
            }
            catch(Exception ignore) { }
        }
    }
}
