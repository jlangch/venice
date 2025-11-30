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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;



/**
 * A bounded, thread-safe queue based on LinkedBlockingQueue
 */
public class BoundedQueue<T> implements IpcQueue<T> {

    public BoundedQueue(
            final String name,
            final int capacity,
            final boolean temporary,
            final boolean durable
    ) {
        this.name = name;
        this.capacity = capacity;
        this.temporary = temporary;
        this.durable = durable;
        this.queue = new LinkedBlockingQueue<>(capacity);
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
    public boolean isDurable() {
        return durable;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public T poll() throws InterruptedException {
        return queue.poll(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public T poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    @Override
    public boolean offer(final T item) throws InterruptedException {
        Objects.requireNonNull(item);

       return queue.offer(item, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean offer(final T item, final long timeout, final TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(item);

       return queue.offer(item, timeout, unit);
    }

    @Override
    public void onRemove() {
        clear();
    }


    private final String name;
    private final boolean temporary;
    private final boolean durable;
    private final int capacity;
    private final LinkedBlockingQueue<T> queue;
}
