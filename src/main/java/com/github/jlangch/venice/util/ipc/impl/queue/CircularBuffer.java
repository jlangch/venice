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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.resilience4j.circularbuffer.ConcurrentEvictingQueue;



/**
 * Circular buffer.
 */
public class CircularBuffer<T> implements IpcQueue<T> {

    public CircularBuffer(final String name, final int capacity, final boolean temporary) {
        this.name = name;
        this.capacity = capacity;
        this.temporary = temporary;
        this.buffer = new ConcurrentEvictingQueue<>(capacity);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }


    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    @Override
    public T poll() {
        return buffer.poll();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) {
        return buffer.poll();
    }

    @Override
    public boolean offer(final T item) {
        Objects.requireNonNull(item);
        return buffer.offer(item);
    }

    @Override
    public boolean offer(final T item, long timeout, TimeUnit unit) {
        Objects.requireNonNull(item);
        return buffer.offer(item);
    }


    private final String name;
    private final boolean temporary;
    private final int capacity;
    private final ConcurrentEvictingQueue<T> buffer;
}
