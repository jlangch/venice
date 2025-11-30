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

import static com.github.jlangch.venice.util.ipc.impl.wal.WalEntryType.ACK;
import static com.github.jlangch.venice.util.ipc.impl.wal.WalEntryType.DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class WriteAheadLogTest {

    @Test
    public void test() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, "first record").toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, "second record").toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, "third record").toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                // Recovered
                assertEquals(3, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll();

                assertEquals(3, entries.size());

                assertEquals(1, entries.get(0).getLsn());
                assertEquals(2, entries.get(1).getLsn());
                assertEquals(3, entries.get(2).getLsn());

                assertEquals(uuid1, entries.get(0).getUUID());
                assertEquals(uuid2, entries.get(1).getUUID());
                assertEquals(uuid3, entries.get(2).getUUID());

                assertEquals("first record",  new String(entries.get(0).getPayload()));
                assertEquals("second record", new String(entries.get(1).getPayload()));
                assertEquals("third record",  new String(entries.get(2).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testCompact() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, "first record").toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, "second record").toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, "third record").toWalEntry());
                long lsn4 = wal.append(new AckWalEntry(uuid2).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
                assertEquals(4, lsn4);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                // Recovered
                assertEquals(4, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll();

                assertEquals(4, entries.size());

                assertEquals(1, entries.get(0).getLsn());
                assertEquals(2, entries.get(1).getLsn());
                assertEquals(3, entries.get(2).getLsn());
                assertEquals(4, entries.get(3).getLsn());

                assertEquals(DATA, entries.get(0).getType());
                assertEquals(DATA, entries.get(1).getType());
                assertEquals(DATA, entries.get(2).getType());
                assertEquals(ACK,  entries.get(3).getType());

                assertEquals(uuid1, entries.get(0).getUUID());
                assertEquals(uuid2, entries.get(1).getUUID());
                assertEquals(uuid3, entries.get(2).getUUID());

                assertEquals("first record",  new String(entries.get(0).getPayload()));
                assertEquals("second record", new String(entries.get(1).getPayload()));
                assertEquals("third record",  new String(entries.get(2).getPayload()));
            }

            // 3. Compact
            WriteAheadLog.compact(walFile, true);


            // 4. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                // Recovered
                assertEquals(2, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll();

                assertEquals(2, entries.size());

                assertEquals(1, entries.get(0).getLsn());
                assertEquals(2, entries.get(1).getLsn());

                assertEquals(DATA, entries.get(0).getType());
                assertEquals(DATA, entries.get(1).getType());

                assertEquals(uuid1, entries.get(0).getUUID());
                assertEquals(uuid3, entries.get(1).getUUID());

                assertEquals("first record",  new String(entries.get(0).getPayload()));
                assertEquals("third record",  new String(entries.get(1).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
