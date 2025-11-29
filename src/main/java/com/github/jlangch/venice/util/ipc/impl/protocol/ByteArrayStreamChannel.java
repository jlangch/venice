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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.Objects;


/**
 * A ByteChannel backed by ByteArrayInputStream and/or ByteArrayOutputStream.
 *
 * <ul>
 *  <li>If constructed with only a ByteArrayInputStream or byte[], the channel is read-only.</li>
 *  <li>If constructed with only a ByteArrayOutputStream, the channel is write-only.</li>
 *  <li>If constructed with both, it is readable and writable.</li>
 </ul> *
 * Not thread-safe.
 */
public class ByteArrayStreamChannel implements ByteChannel {

    /**
     * Creates a read-only channel over a copy of the given byte array.
     *
     * @param data a byte array as input data
     */
    public ByteArrayStreamChannel(final byte[] data) {
        this(new ByteArrayInputStream(Objects.requireNonNull(data, "data")), null);
    }

    /**
     * Creates a read-only channel over the given ByteArrayInputStream.
     *
     * @param in a ByteArrayInputStream as input data
     */
    public ByteArrayStreamChannel(final ByteArrayInputStream in) {
        this(Objects.requireNonNull(in, "in"), null);
    }

    /**
     * Creates a write-only channel backed by the given ByteArrayOutputStream.
     *
     * @param out a ByteArrayOutputStream as input data
     */
    public ByteArrayStreamChannel(final ByteArrayOutputStream out) {
        this(null, Objects.requireNonNull(out, "out"));
    }

    /**
     * Creates a channel that can be read from and/or written to depending on which
     * streams are non-null.
     *
     * @param in a ByteArrayInputStream as input data
     * @param out a ByteArrayOutputStream as input data
     */
    public ByteArrayStreamChannel(
        final ByteArrayInputStream in,
        final ByteArrayOutputStream out
    ) {
        if (in == null && out == null) {
            throw new IllegalArgumentException("At least one of in or out must be non-null");
        }

        this.in = in;
        this.out = out;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        if (!open) {
            throw new ClosedChannelException();
        }
        if (in == null) {
            throw new NonReadableChannelException();
        }
        if (!dst.hasRemaining()) {
            return 0;
        }

        final int available = in.available();
        if (available == 0) {
            return -1; // EOF
        }

        final int toRead = Math.min(dst.remaining(), available);
        final byte[] buf = new byte[toRead];
        final int read = in.read(buf, 0, toRead);
        if (read == -1) {
            return -1;
        }

        dst.put(buf, 0, read);
        return read;
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        if (!open) {
            throw new ClosedChannelException();
        }
        if (out == null) {
            throw new NonWritableChannelException();
        }
        if (!src.hasRemaining()) {
            return 0;
        }

        final int len = src.remaining();
        final byte[] buf = new byte[len];
        src.get(buf);
        out.write(buf);
        return len;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        if (!open) {
            return;
        }
        open = false;
        if (in != null) {
            in.close(); // no-op but keeps semantics consistent
        }
        if (out != null) {
            out.close(); // also no-op for ByteArrayOutputStream
        }
    }

    /**
     * Convenience accessor for the underlying ByteArrayOutputStream's content.
     * Returns null if this channel is not backed by an output stream.
     *
     * @return the out data as byte array
     */
    public byte[] toByteArray() {
        return out != null ? out.toByteArray() : null;
    }


    private final ByteArrayInputStream in;
    private final ByteArrayOutputStream out;
    private volatile boolean open = true;
}
