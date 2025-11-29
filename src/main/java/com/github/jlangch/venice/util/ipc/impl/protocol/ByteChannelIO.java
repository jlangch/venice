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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;


public class ByteChannelIO {

    public static ByteBuffer readFrame(
            final ByteChannel ch
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
            final ByteChannel ch,
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
            final ByteChannel ch,
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
            final ByteChannel ch,
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

}
