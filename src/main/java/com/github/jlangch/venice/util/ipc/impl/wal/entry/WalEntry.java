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
package com.github.jlangch.venice.util.ipc.impl.wal.entry;

import java.util.UUID;


public final class WalEntry {

    public WalEntry(
            final WalEntryType type,
            final UUID uuid,
            final long expiry,
            final byte[] payload
    ) {
        this(-1, type, uuid, expiry, payload);
    }

    public WalEntry(
            final long lsn,
            final WalEntryType type,
            final UUID uuid,
            final long expiry,
            final byte[] payload
    ) {
        this.lsn = lsn;
        this.type = type;
        this.uuid = uuid;
        this.expiry = -1;
        this.payload = payload;
    }

    public long getLsn() {
        return lsn;
    }

    public WalEntryType getType() {
        return type;
    }

    public UUID getUUID() {
        return uuid;
    }

    public long getExpiry() {
        return expiry;
    }

    public byte[] getPayload() {
        return payload;
    }

    public boolean hasExpired() {
        return expiry >= 0 && expiry < System.currentTimeMillis();
    }


    private final long lsn;
    private final WalEntryType type;
    private final UUID uuid;
    private final long expiry;  // expiry timestamp (millis since epoch or -1 if never expires)
    private final byte[] payload;
}