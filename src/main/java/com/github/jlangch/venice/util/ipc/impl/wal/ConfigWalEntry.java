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
package com.github.jlangch.venice.util.ipc.impl.wal;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.github.jlangch.venice.util.ipc.impl.queue.QueueType;


/**
 * WalEntry serializer/deserializer for queue config WAL entries
 */
public class ConfigWalEntry {

    public ConfigWalEntry(final int queueCapacity, final QueueType queueType) {
        this.queueCapacity = queueCapacity;
        this.queueType = queueType;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public boolean isBoundedQueue() {
        return queueType == QueueType.BOUNDED;
    }

    public WalEntry toWalEntry() {
        final ByteBuffer payload = ByteBuffer.allocate(8);
        payload.putInt(queueCapacity);
        payload.putInt(queueType.getValue());
        payload.flip();

        return new WalEntry(WalEntryType.CONFIG, UUID.randomUUID(), payload.array());
    }

    public static ConfigWalEntry fromWalEntry(final WalEntry entry) {
       final ByteBuffer payload = ByteBuffer.wrap(entry.getPayload());
       final int capacity = payload.getInt();
       final int type = payload.getInt();
       return new ConfigWalEntry(capacity, QueueType.fromCode(type));
    }


    private final int queueCapacity;
    private final QueueType queueType;
}
