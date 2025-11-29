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

import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * WalEntry serializer/deserializer for data WAL entries
 */
public class DataWalEntry {

    public DataWalEntry(final UUID uuid, final byte[] payload) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid must not be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }

        this.uuid = uuid;
        this.payload = payload;
    }

    public DataWalEntry(final UUID uuid, final String payload) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid must not be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }

        this.uuid = uuid;
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
    }

    public UUID getUUID() {
        return uuid;
    }

    public byte[] getPayload() {
        return payload;
    }

    public WalEntry toWalEntry() {
        return new WalEntry(-1, WalEntryType.DATA,  uuid, payload);
    }

    public static DataWalEntry fromWalEntry(final WalEntry entry) {
       return new DataWalEntry(entry.getUUID(), entry.getPayload());
    }


    private final UUID uuid;
    private final byte[] payload;
}
