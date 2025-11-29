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

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class WalBasedQueue<T> implements IpcQueue<T>, IWalQueue {

    public WalBasedQueue(
            final IpcQueue<T> queue,
            final File walDir,
            final boolean walEnabled
    ) {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(walDir);

        this.queue = queue;
        this.queueName = queue.name();
        this.walDir = walDir;
        this.walEnabled = walEnabled && !queue.isTemporary();
    }


    @Override
    public void reload() {
        if (walEnabled) {

        }
    }

    @Override
    public void compact() {
        if (walEnabled) {

        }
    }

    @Override
    public String name() {
        return queue.name();
    }

    @Override
    public int capacity() {
        return queue.capacity();
    }

    @Override
    public boolean isTemporary() {
        return queue.isTemporary();
    }


    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void clear() {
        if (walEnabled) {

        }

        queue.clear();
    }

    @Override
    public T poll() throws InterruptedException {
        if (walEnabled) {

        }

        return queue.poll();
    }

    @Override
    public T poll(
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        if (walEnabled) {

        }

        return queue.poll(timeout, unit);
    }

    @Override
    public boolean offer(final T item) throws InterruptedException {
        Objects.requireNonNull(item);

        if (walEnabled) {

        }

        return queue.offer(item, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean offer(
            final T item,
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        Objects.requireNonNull(item);

        if (walEnabled) {

        }

        return queue.offer(item, timeout, unit);
    }

    @Override
    public void onRemove() {
        if (walEnabled) {

        }

        queue.onRemove();
    }


    private final IpcQueue<T> queue;
    private final String queueName;
    private final File walDir;
    private final boolean walEnabled;
}
