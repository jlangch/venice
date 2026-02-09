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
package com.github.jlangch.venice.util.ipc.impl.dest.queue;

import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.util.ipc.QueueType;
import com.github.jlangch.venice.util.ipc.impl.Destination;


public class NullQueue<T> extends Destination implements IpcQueue<T> {

    public NullQueue(final String name, final QueueType type) {
        super(name);
        this.type = type;
    }


    @Override
    public QueueType type() {
        return type;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public T poll() throws InterruptedException {
        return null;
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public boolean offer(T item) throws InterruptedException {
        return false;
    }

    @Override
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void onRemove() {
    }


    private final QueueType type;
}
