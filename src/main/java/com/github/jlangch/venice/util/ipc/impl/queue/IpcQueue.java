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
package com.github.jlangch.venice.util.ipc.impl.queue;

import java.util.concurrent.TimeUnit;


public interface IpcQueue<T> {

    String name();

    QueueType type();

    boolean isTemporary();

    boolean isDurable();

    boolean isEmpty();

    int size();

    int capacity();

    T poll() throws InterruptedException;

    T poll(long timeout, TimeUnit unit) throws InterruptedException;

    boolean offer(T item) throws InterruptedException;

    boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException;

    void onRemove();

}
