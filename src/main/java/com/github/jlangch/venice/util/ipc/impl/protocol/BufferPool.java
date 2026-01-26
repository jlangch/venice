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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Lock-free byte buffer pool.
 */
public class BufferPool implements IBufferPool {

    public BufferPool(final int bufferSize) {
        this(bufferSize, true, false);
    }

    public BufferPool(
            final int bufferSize,
            final boolean clearAtCheckin,
            final boolean preset
    ) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("A buffer size must not be lower than 1");
        }

        this.bufferSize = bufferSize;
        this.clearAtCheckin = clearAtCheckin;
        if (preset) {
            buffer.set(new byte[bufferSize]);
        }
    }


    @Override
    public byte[] checkout() {
        final byte[] buf = buffer.get();
        return buf != null && buffer.compareAndSet(buf, null)
                 ? buf
                 : new byte[bufferSize]; // fallback
    }

    @Override
    public void checkin(byte[] b) {
        if (b != null && b.length == bufferSize) {
            if (clearAtCheckin) Arrays.fill(b, (byte)0x00);
            buffer.set(b);
        }
    }


    private final AtomicReference<byte[]> buffer = new AtomicReference<>();
    private final int bufferSize;
    private final boolean clearAtCheckin;
}
