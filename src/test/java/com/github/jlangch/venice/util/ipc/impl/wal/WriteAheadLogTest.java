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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;


public class WriteAheadLogTest {

    @Test
    public void test_clean_close() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();

            // Append an entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                wal.append(new DataWalEntry(uuid1, smallMsg(1)).toWalEntry());

                assertTrue(walFile.isFile());
            }

            assertTrue(walFile.isFile());

            // ensure the file can be deleted
            walFile.delete();
            assertFalse(walFile.isFile());
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_SMALL() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, smallMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, smallMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, smallMsg(3)).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
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

                assertEquals(smallMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(smallMsg(2), new String(entries.get(1).getPayload()));
                assertEquals(smallMsg(3), new String(entries.get(2).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testCompact_SMALL() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, smallMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, smallMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, smallMsg(3)).toWalEntry());
                long lsn4 = wal.append(new AckWalEntry(uuid2).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
                assertEquals(4, lsn4);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                // Recovered
                assertEquals(4, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll();

                // We've got 4 entries
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

                assertEquals(smallMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(smallMsg(2), new String(entries.get(1).getPayload()));
                assertEquals(smallMsg(3), new String(entries.get(2).getPayload()));
            }

            // 3. Compact
            WriteAheadLog.compact(
                walFile,
                false,  // discard expired entries,
                true);  // remove backup logfile

            // 4. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
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

                assertEquals(smallMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(smallMsg(3), new String(entries.get(1).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Test
    public void test_LARGE() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, largeMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, largeMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, largeMsg(3)).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
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

                assertEquals(largeMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(largeMsg(2), new String(entries.get(1).getPayload()));
                assertEquals(largeMsg(3), new String(entries.get(2).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testCompact_LARGE() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, largeMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, largeMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, largeMsg(3)).toWalEntry());
                long lsn4 = wal.append(new AckWalEntry(uuid2).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
                assertEquals(4, lsn4);
            }

            // 2. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
                // Recovered
                assertEquals(4, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll();

                // We've got 4 entries
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

                assertEquals(largeMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(largeMsg(2), new String(entries.get(1).getPayload()));
                assertEquals(largeMsg(3), new String(entries.get(2).getPayload()));
            }

            // 3. Compact
            WriteAheadLog.compact(
                walFile,
                false,  // discard expired entries,
                true);  // remove backup logfile

            // 4. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, WalLogger.asTemporary())) {
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

                assertEquals(largeMsg(1), new String(entries.get(0).getPayload()));
                assertEquals(largeMsg(3), new String(entries.get(1).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    private String smallMsg(final int id) {
        return id + "-" + StringUtil.repeat("a", 10);
    }

    private String largeMsg(final int id) {
        return id + "-" + StringUtil.repeat("a", 100_000);
    }
}
