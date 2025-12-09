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
 *
 * <p>The <code>ConfigWalEntry</code> is always the first entry written
 * to a queue WAL file and holds the queue's capacity and type.
 *
 * <p>While reconstructing a queue from a WAL file, the queue's capacity
 * and type is read from this entry!
 */
public class ConfigWalEntry {

    public ConfigWalEntry(
            final int queueCapacity,
            final QueueType queueType,
            final boolean walCompressed
    ) {
        this.queueCapacity = queueCapacity;
        this.queueType = queueType;
        this.walCompressed = walCompressed;
    }


    public int getQueueCapacity() {
        return queueCapacity;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public boolean isWalCompressed() {
        return walCompressed;
    }

    public boolean isBoundedQueue() {
        return queueType == QueueType.BOUNDED;
    }

    public WalEntry toWalEntry() {
        final ByteBuffer payload = ByteBuffer.allocate(12);
        payload.putInt(queueCapacity);
        payload.putInt(queueType.getValue());
        payload.putInt(walCompressed ? 1 : 0);
        payload.flip();

        return new WalEntry(WalEntryType.CONFIG, UUID.randomUUID(), 1, payload.array());
    }

    public static ConfigWalEntry fromWalEntry(final WalEntry entry) {
       final ByteBuffer payload = ByteBuffer.wrap(entry.getPayload());
       final int capacity = payload.getInt();
       final int type = payload.getInt();
       final boolean compressed = payload.getInt() == 0 ? false : true;
       return new ConfigWalEntry(capacity, QueueType.fromCode(type), compressed);
    }


    private final int queueCapacity;
    private final boolean walCompressed;
    private final QueueType queueType;
}
