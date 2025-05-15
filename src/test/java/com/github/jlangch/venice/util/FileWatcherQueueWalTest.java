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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;


public class FileWatcherQueueWalTest {

    @Test
    public void test_empty() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            assertTrue(q.isEmpty());
            assertEquals(0, q.size());

            assertNotNull(q.getWalFile());
        }
    }

    @Test
    public void test_push() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f = new File("a").getAbsoluteFile();
            q.push(f);

            q.save();
            q.load();

            assertFalse(q.isEmpty());
            assertEquals(1, q.size());
        }
    }

    @Test
    public void test_push2() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f3);

            q.save();
            q.load();

            assertFalse(q.isEmpty());
            assertEquals(3, q.size());
        }
    }

    @Test
    public void test_push3() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f3);

            q.push(f2);
            q.push(f2);
            q.push(f2);

            q.save();
            q.load();

            assertFalse(q.isEmpty());
            assertEquals(3, q.size());
        }
    }

    @Test
    public void test_pop() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f = new File("a").getAbsoluteFile();
            q.push(f);

            q.save();
            q.load();

            assertEquals(f, q.pop());
            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }
    }

    @Test
    public void test_pop2() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f3);

            q.save();
            q.load();

            assertEquals(f1, q.pop());
            assertEquals(f2, q.pop());
            assertEquals(f3, q.pop());
            assertEquals(null, q.pop());
            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }
    }

    @Test
    public void test_pop3() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f3);

            List<File> files1 = q.pop(2);

            q.save();
            q.load();

            List<File> files2 = q.pop(2);

            q.save();
            q.load();

            List<File> files3 = q.pop(2);

            assertEquals(2, files1.size());
            assertEquals(1, files2.size());
            assertEquals(0, files3.size());

            assertEquals(f1, files1.get(0));
            assertEquals(f2, files1.get(1));
            assertEquals(f3, files2.get(0));

            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }
    }

    @Test
    public void test_open_with_wal() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f3);
        }

        // WAL: 3 entries
        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            assertFalse(q.isEmpty());
            assertEquals(3, q.size());

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();

            assertEquals(f1, q.pop());
            assertEquals(f2, q.pop());
            assertEquals(f3, q.pop());

            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }

        // WAL: 0 entries
        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }
    }

    @Test
    public void test_open_with_wal_compacted() throws IOException {
        final File walFile = File.createTempFile("file-watcher-", ".wal");
        walFile.deleteOnExit();

        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            q.clear();

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();
            q.push(f1);
            q.push(f2);
            q.push(f2);
            q.push(f3);
            q.push(f3);
            q.push(f3);
        }

        // WAL: 3 entries (after read compaction)
        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            assertFalse(q.isEmpty());
            assertEquals(3, q.size());

            File f1 = new File("a").getAbsoluteFile();
            File f2 = new File("b").getAbsoluteFile();
            File f3 = new File("c").getAbsoluteFile();

            assertEquals(f1, q.pop());
            assertEquals(f2, q.pop());
            assertEquals(f3, q.pop());

            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }

        // WAL: 0 entries
        try (FileWatcherQueue q = FileWatcherQueue.create(walFile)) {
            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
        }
    }
}
