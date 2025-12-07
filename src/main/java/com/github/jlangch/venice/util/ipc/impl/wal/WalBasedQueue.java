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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.QueueType;


public class WalBasedQueue implements IpcQueue<Message>, Closeable {

    public WalBasedQueue(
            final IpcQueue<Message> queue,
            final WalQueueManager queueManager
    ) throws IOException {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(queueManager);

        this.queue = queue;
        this.logger = queueManager.getLogger();

        logInfo("WAL based queue '" + queue.name() + "' created");

        final String filename = WalQueueManager.toFileName(queue.name());
        this.log = new WriteAheadLog(
                            new File(queueManager.getWalDir(), filename),
                            queueManager.isCompressed(),
                            queueManager.getLogger());

        if (this.log.getLastLsn() == 0) {
            final ConfigWalEntry ce = new ConfigWalEntry(queue.capacity(), queue.type());
            this.log.append(ce.toWalEntry());
        }
    }


    @Override
    public String name() {
        return queue.name();
    }

    @Override
    public QueueType type() {
        return queue.type();
    }

    @Override
    public int capacity() {
        return queue.capacity();
    }

    @Override
    public boolean isTemporary() {
        return queue.isTemporary();
    }

    @Override
    public boolean isDurable() {
        return queue.isDurable();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public Message poll() throws InterruptedException {
        if (closed) {
            throw new VncException("The queue " + queue.name() + " is closed!");
        }

        final Message m = queue.poll();

        if (m != null && m.isDurable()) {
            try {
                log.append(new AckWalEntry(m.getId()).toWalEntry());
            }
            catch(Exception ex) {
                throw new VncException("Failed to poll message from queue " + queue.name(), ex);
            }
        }

        return m;
    }

    @Override
    public Message poll(
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        Objects.requireNonNull(unit);

        if (closed) {
            throw new VncException("The queue " + queue.name() + " is closed!");
        }

        final Message m = queue.poll(timeout, unit);

        if (m != null && m.isDurable()) {
            try {
                log.append(new AckWalEntry(m.getId()).toWalEntry());
            }
            catch(Exception ex) {
                throw new VncException("Failed to poll message from queue " + queue.name(), ex);
            }
        }

        return m;
    }

    @Override
    public boolean offer(final Message item) throws InterruptedException {
        Objects.requireNonNull(item);

        if (closed) {
            throw new VncException("The queue " + queue.name() + " is closed!");
        }

        if (item.isDurable()) {
            try {
                log.append(new MessageWalEntry(item).toWalEntry());
            }
            catch(Exception ex) {
                throw new VncException("Failed to offer message to queue " + queue.name(), ex);
            }
        }

        return queue.offer(item, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean offer(
            final Message item,
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        Objects.requireNonNull(item);

        if (closed) {
            throw new VncException("The queue " + queue.name() + " is closed!");
        }

        if (item.isDurable()) {
            try {
                log.append(new MessageWalEntry(item).toWalEntry());
            }
            catch(Exception ex) {
                throw new VncException("Failed to offer message to queue " + queue.name(), ex);
            }
        }

        return queue.offer(item, timeout, unit);
    }

    @Override
    public void onRemove() {
        if (closed) {
            throw new VncException("The queue " + queue.name() + " is closed!");
        }

        final File logFile = log.getFile();

        try { log.close(); } catch(Exception ignore) { }

        if (logFile.exists()) {
            logFile.delete();
        }

        closed = true;

        // delegate queue
        try { queue.onRemove(); } catch(Exception ignore) { }

        logInfo("WAL based queue '" + queue.name() + "' removed");
    }

    @Override
    public void close() throws IOException {
        if (closed) return;

        try {
            closed = true;
            log.close();
        }
        finally {
            logInfo("WAL based queue '" + queue.name() + "' closed");
        }
    }


    private void logInfo(final String message) {
       logger.info(
           new File(WalQueueManager.toFileName(queue.name())),
           message);
    }



    private final IpcQueue<Message> queue;
    private final WriteAheadLog log;
    private final WalLogger logger;

    private volatile boolean closed = false;
}
