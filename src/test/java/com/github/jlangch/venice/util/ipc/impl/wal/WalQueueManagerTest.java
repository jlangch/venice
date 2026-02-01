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
package com.github.jlangch.venice.util.ipc.impl.wal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.MessageFactory;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.QueueFactory;
import com.github.jlangch.venice.util.ipc.impl.dest.queue.IpcQueue;


public class WalQueueManagerTest {

    @Test
    public void test() throws IOException {
        final File walDir = Files.createTempDirectory("wal-").normalize().toFile();
        final File logger = new File(walDir, "wal.log");

        try {
            final WalQueueManager wqm = new WalQueueManager();
            wqm.activate(walDir, false, false);

            final IpcQueue<Message> queue = QueueFactory.createQueue(
                                                wqm,
                                                "queue/test",
                                                100,
                                                true,
                                                true);

            assertTrue(queue instanceof DurableBoundedQueue);

            queue.offer(smallMsg(1));
            queue.offer(smallMsg(2));

            final File walFile = new File(walDir, WalQueueManager.toFileName(queue.name()));
            assertTrue(walFile.isFile());
            assertTrue(logger.isFile());
            assertTrue(walFile.length() > 0);

            wqm.close(CollectionUtil.toList(queue));

            // check WAL dir can be deleted
            walFile.delete();
            logger.delete();
            walDir.delete();
            assertFalse(walFile.isFile());
            assertFalse(logger.isFile());
            assertFalse(walDir.isDirectory());
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void test_server_with_wal() throws Exception {
        final File walDir = Files.createTempDirectory("wal-").normalize().toFile();
        final File log = new File(walDir, "wal.log");

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, false, false)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.offer(smallMsg(1));
            queue.offer(smallMsg(2));
            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        final File walFile = new File(walDir, WalQueueManager.toFileName("queue/test"));
        assertTrue(walFile.isFile());
        assertTrue(log.isFile());
        assertTrue(walFile.length() > 0);

        // check WAL dir can be deleted
        walFile.delete();
        log.delete();
        walDir.delete();
        assertFalse(walFile.isFile());
        assertFalse(log.isFile());
        assertFalse(walDir.isDirectory());
    }

    @Test
    public void test_server_with_wal_compact() throws Exception {
        final File walDir = Files.createTempDirectory("wal-").normalize().toFile();
        final File logger = new File(walDir, "wal.log");

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, true, true)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.offer(smallMsg(1));
            queue.offer(smallMsg(2));
            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(100);

        final File walFile = new File(walDir, WalQueueManager.toFileName("queue/test"));
        assertTrue(walFile.isFile());
        assertTrue(logger.isFile());
        assertTrue(walFile.length() > 0);

        // check WAL dir can be deleted
        walFile.delete();
        logger.delete();
        walDir.delete();
        assertFalse(walFile.isFile());
        assertFalse(logger.isFile());
        assertFalse(walDir.isDirectory());
    }

    @Test
    public void test_server_reopen_with_wal() throws Exception {
        final File walDir = Files.createTempDirectory("wal-").normalize().toFile();
        final File logger = new File(walDir, "wal.log");

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, false, false)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.offer(smallMsg(1));
            queue.offer(smallMsg(2));
            queue.offer(smallMsg(3));
            queue.offer(smallMsg(4));
            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(100);

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, false, false)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(100);

        final File walFile = new File(walDir, WalQueueManager.toFileName("queue/test"));
        assertTrue(walFile.isFile());
        assertTrue(logger.isFile());
        assertTrue(walFile.length() > 0);

        // check WAL dir can be deleted
        walFile.delete();
        logger.delete();
        walDir.delete();
        assertFalse(walFile.isFile());
        assertFalse(logger.isFile());
        assertFalse(walDir.isDirectory());
    }

    @Test
    public void test_server_reopen_with_wal_compact() throws Exception {
        final File walDir = Files.createTempDirectory("wal-").normalize().toFile();
        final File logger = new File(walDir, "wal.log");

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, true, true)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.offer(smallMsg(1));
            queue.offer(smallMsg(2));
            queue.offer(smallMsg(3));
            queue.offer(smallMsg(4));
            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(100);

        try(Server server = Server.of(ServerConfig
                                        .builder()
                                        .conn(33333)
                                        .enableWriteAheadLog(walDir, true, true)
                                        .build())
        ) {
            server.start();

            server.createQueue("queue/test", 100, true, true);

            // uses WalQueueManager to manage the queue
            final IpcQueue<Message> queue = server.getQueue("queue/test");

            queue.poll(0, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(100);

        final File walFile = new File(walDir, WalQueueManager.toFileName("queue/test"));
        assertTrue(walFile.isFile());
        assertTrue(logger.isFile());
        assertTrue(walFile.length() > 0);

        // check WAL dir can be deleted
        walFile.delete();
        logger.delete();
        walDir.delete();
        assertFalse(walFile.isFile());
        assertFalse(logger.isFile());
        assertFalse(walDir.isDirectory());
    }


    private Message smallMsg(final int id) {
        return (Message)MessageFactory.text(
                    String.valueOf(id),
                    "hello",
                    "text/plain",
                    "UTF-8",
                    String.valueOf(id) + "-" + StringUtil.repeat("a", 10));
    }

}
