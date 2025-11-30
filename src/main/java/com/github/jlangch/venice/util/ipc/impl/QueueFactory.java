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

import java.io.File;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.ipc.impl.queue.BoundedQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.CircularBuffer;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.wal.WalBasedQueue;


public class QueueFactory {

    public static IpcQueue<Message> createQueue(
            final File walDir,
            final String queueName,
            final int capacity,
            final boolean bounded,
            final boolean durable
    ) {
        if (durable && walDir == null) {
            throw new VncException(
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        if (durable && walDir != null) {
            // durable: create WAL based queue
            try {
                final IpcQueue<Message> queue = createRawQueue(queueName, capacity, bounded, true);
                return new WalBasedQueue(queue, walDir);
            }
            catch(Exception ex) {
                throw new VncException("Failed to ceate WAL based queue: " + queueName, ex);
            }
        }
        else {
           return createRawQueue(queueName, capacity, bounded, false);
        }
    }

    private static IpcQueue<Message> createRawQueue(
            final String queueName,
            final int capacity,
            final boolean bounded,
            final boolean durable
    ) {
        if (bounded) {
           return new BoundedQueue<Message>(queueName, capacity, false, durable);
        }
        else {
           return new CircularBuffer<Message>(queueName, capacity, false, durable);
        }
    }
}
