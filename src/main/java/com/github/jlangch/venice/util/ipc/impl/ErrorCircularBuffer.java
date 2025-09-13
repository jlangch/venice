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
package com.github.jlangch.venice.util.ipc.impl;

import java.util.LinkedList;


/**
 * A very simple, thread-safe implementation for an error circular buffer.
 *
 * <p>It's not designed for heavy traffic, but works fine for buffering
 * low traffic errors.
 */
public class ErrorCircularBuffer {

    public ErrorCircularBuffer(final int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
    }

    public int size() {
        synchronized(this) {
            return buffer.size();
        }
    }

    public void clear() {
        synchronized(this) {
            buffer.clear();
        }
    }

    public Message pop() {
        synchronized(this) {
            return buffer.isEmpty() ? null : buffer.getFirst();
        }
    }

    public void push(final Message message) {
        if (message == null) return;

        synchronized(this) {
            while (buffer.size() > capacity) {
                buffer.removeFirst();
            }

            buffer.addLast(message);
        }
    }


    private final int capacity;
    private final LinkedList<Message> buffer;
}
