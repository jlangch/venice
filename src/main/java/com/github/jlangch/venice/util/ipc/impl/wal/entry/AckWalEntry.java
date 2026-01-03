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

import com.github.jlangch.venice.impl.util.UUIDHelper;


/**
 * WalEntry serializer/deserializer for acknowledge WAL entries
 */
public class AckWalEntry {

    public AckWalEntry(final UUID ackedEntryUUID) {
        if (ackedEntryUUID == null) {
            throw new IllegalArgumentException("ackedEntryUUID must not be null");
        }

        this.ackedEntryUUID = ackedEntryUUID;
    }

    public UUID getAckedEntryUUID() {
        return ackedEntryUUID;
    }

    public WalEntry toWalEntry() {
        return new WalEntry(
                    WalEntryType.ACK,
                    UUID.randomUUID(),
                    -1,
                    UUIDHelper.convertUUIDToBytes(ackedEntryUUID));
    }

    public static AckWalEntry fromWalEntry(final WalEntry entry) {
       return new AckWalEntry(UUIDHelper.convertBytesToUUID(entry.getPayload()));
    }


    private final UUID ackedEntryUUID;
}
