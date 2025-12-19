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

import static com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntryType.DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.DataWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntry;


public class WriteAheadLogCorruptionTest {


    @Test
    public void testOpenCorrupted_WAL() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("test", ".wal").normalize().toFile();
            walFile.deleteOnExit();

            final WalLogger logger = WalLogger.asTemporary();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();
            final UUID uuid4 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, logger)) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, smallMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, smallMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, smallMsg(3)).toWalEntry());
                long lsn4 = wal.append(new DataWalEntry(uuid4, smallMsg(4)).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
                assertEquals(4, lsn4);
            }

            // 2. Damage the WAL at the last entry
            try(RandomAccessFile raf = new RandomAccessFile(walFile, "rw")) {
                final long size = raf.length();
                raf.seek(size - 10);
                raf.write(0x55);
                raf.write(0xAA);
            }

            // 3. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, logger)) {
                // Recovered, we lost the last entry that is corrupted
                // Getting 3 instead of 4 entries
                assertEquals(3, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll(false);

                // We've got 3 entries
                assertEquals(3, entries.size());

                assertEquals(1, entries.get(0).getLsn());
                assertEquals(2, entries.get(1).getLsn());
                assertEquals(3, entries.get(2).getLsn());

                assertEquals(DATA, entries.get(0).getType());
                assertEquals(DATA, entries.get(1).getType());
                assertEquals(DATA, entries.get(2).getType());

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
    public void testCompactCorrupted_WAL() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("test", ".wal").normalize().toFile();
            walFile.deleteOnExit();

            final WalLogger logger = WalLogger.asTemporary();

            final UUID uuid1 = UUID.randomUUID();
            final UUID uuid2 = UUID.randomUUID();
            final UUID uuid3 = UUID.randomUUID();
            final UUID uuid4 = UUID.randomUUID();


            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, logger)) {
                long lsn1 = wal.append(new DataWalEntry(uuid1, smallMsg(1)).toWalEntry());
                long lsn2 = wal.append(new DataWalEntry(uuid2, smallMsg(2)).toWalEntry());
                long lsn3 = wal.append(new DataWalEntry(uuid3, smallMsg(3)).toWalEntry());
                long lsn4 = wal.append(new DataWalEntry(uuid4, smallMsg(4)).toWalEntry());

                assertEquals(1, lsn1);
                assertEquals(2, lsn2);
                assertEquals(3, lsn3);
                assertEquals(4, lsn4);
            }

            assertEquals(4, WriteAheadLog.loadAll(walFile, false).size());


            // 2. Damage the WAL at the last entry
            try(RandomAccessFile raf = new RandomAccessFile(walFile, "rw")) {
                final long size = raf.length();
                raf.seek(size - 10);
                raf.write(0x55);
                raf.write(0xAA);
            }


            // 3. Compact
            WriteAheadLog.compact(
                walFile,
                logger,
                false,  // discard expired entries,
                true);  // remove backup logfile

            assertEquals(3, WriteAheadLog.loadAll(walFile, false).size());


            // 4. Simulate restart: open WAL again and recover entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile, logger)) {
                // Recovered, we lost the last entry that is corrupted
                // Getting 3 instead of 4 entries
                assertEquals(3, wal.getLastLsn());

                final List<WalEntry> entries = wal.readAll(false);

                // We've got 3 entries
                assertEquals(3, entries.size());

                assertEquals(1, entries.get(0).getLsn());
                assertEquals(2, entries.get(1).getLsn());
                assertEquals(3, entries.get(2).getLsn());

                assertEquals(DATA, entries.get(0).getType());
                assertEquals(DATA, entries.get(1).getType());
                assertEquals(DATA, entries.get(2).getType());

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



    private String smallMsg(final int id) {
        return id + "-" + StringUtil.repeat("a", 10);
    }
}
