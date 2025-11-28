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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;


public class WriteAheadLogTest {

    @Test
    public void test() {
        // with default encoding
        try {
            final File walFile = Files.createTempFile("wal", ".txt").normalize().toFile();
            walFile.deleteOnExit();

            // 1. Append some entries
            try (WriteAheadLog wal = new WriteAheadLog(walFile)) {
                long lsn1 = wal.append("first record".getBytes());
                long lsn2 = wal.append("second record".getBytes());
                long lsn3 = wal.append("third record".getBytes());

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

                assertEquals("first record",  new String(entries.get(0).getPayload()));
                assertEquals("second record", new String(entries.get(1).getPayload()));
                assertEquals("third record",  new String(entries.get(2).getPayload()));
            }
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
