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
package com.github.jlangch.venice.util.ipc.impl.queue;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;



/**
 * A simple, thread-safe implementation of a circular buffer.
 */
public class CircularBuffer<T> implements IpcQueue<T> {

    public CircularBuffer(final String name, final int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        synchronized(this) {
            return buffer.size();
        }
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public void clear() {
        synchronized(this) {
            buffer.clear();
        }
    }

    @Override
    public T poll() {
        synchronized(this) {
            return buffer.isEmpty() ? null : buffer.pollFirst();
        }
    }

    @Override
    public T poll(long timeout, TimeUnit unit) {
        return poll();
    }

    @Override
    public boolean offer(final T item) {
        Objects.requireNonNull(item);

        synchronized(this) {
            while (buffer.size() >= capacity) {
                buffer.removeFirst();
                discardCount.incrementAndGet();
            }

            buffer.offerLast(item);

            return true;
        }
    }

    @Override
    public boolean offer(final T item, long timeout, TimeUnit unit) {
        return offer(item);
    }

    public long discardCount() {
        return discardCount.get();
    }


    private final String name;
    private final int capacity;
    private final LinkedList<T> buffer;
    private final AtomicLong discardCount = new AtomicLong(0);
}
