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
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 * Lock-free byte buffer pool.
 */
public class BufferArrayPool implements IBufferPool {

    public BufferArrayPool(final int poolSize, final int bufferSize) {
        this(poolSize, bufferSize, true, false);
    }

    public BufferArrayPool(
            final int poolSize,
            final int bufferSize,
            final boolean clearAtCheckin,
            final boolean preset
    ) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("A pool size must not be lower than 1");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("A buffer size must not be lower than 1");
        }

        this.pool = new AtomicReferenceArray<>(poolSize);
        this.bufferSize = bufferSize;
        this.clearAtCheckin = clearAtCheckin;

        if (preset) {
            for (int ii=0; ii<pool.length(); ii++) {
               pool.set(ii, new byte[bufferSize]);
            }
        }
    }


    @Override
    public byte[] checkout() {
        for (int i = 0; i < pool.length(); i++) {
            byte[] buf = pool.get(i);
            if (buf != null && pool.compareAndSet(i, buf, null)) {
                return buf;
            }
        }
        return new byte[bufferSize]; // fallback
    }

    @Override
    public void checkin(byte[] b) {
        if (b.length != bufferSize) {
            return; // reject foreign buffers
        }
        for (int ii=0; ii<pool.length(); ii++) {
            if (pool.get(ii) == null) {
                if (pool.compareAndSet(ii, null, b)) {
                    if (clearAtCheckin) Arrays.fill(b, (byte)0x00);
                    return;
                }
            }
        }
        // pool full → drop
    }


    private final AtomicReferenceArray<byte[]> pool;
    private final int bufferSize;
    private final boolean clearAtCheckin;
}
