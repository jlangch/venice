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


import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.WriteAheadLogException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.QueueType;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.AckWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.ConfigWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.MessageWalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntry;
import com.github.jlangch.venice.util.ipc.impl.wal.entry.WalEntryType;


public class DurableBoundedQueue implements IpcQueue<Message>, AutoCloseable {

    public DurableBoundedQueue(
            final String queueName,
            final int capacity,
            final WriteAheadLog wal,
            final WalLogger logger
    ) throws WriteAheadLogException {
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(wal);
        Objects.requireNonNull(logger);
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }

        this.queueName = queueName;
        this.elements = new Message[capacity];
        this.wal = wal;
        this.logger = logger;

        // if the queue wal is new, append the config entry to the wal
        if (wal.getLastLsn() == 0) {
            try {
                final ConfigWalEntry ce = new ConfigWalEntry(
                                                capacity,
                                                QueueType.BOUNDED,
                                                wal.isCompressing());

                wal.append(ce.toWalEntry());
            }
            catch(Exception ex) {
                throw new WriteAheadLogException(
                        String.format(
                            "Failed to append the configuration entry to new "
                            + "Write-Ahead-Log of the durable queue %s!",
                            queueName),
                        ex);
            }
        }
    }

    @Override
    public String name() {
        return queueName;
    }

    @Override
    public QueueType type() {
        return QueueType.BOUNDED;
    }

    @Override
    public int capacity() {
        return elements.length;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isDurable() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return size;
        }
        finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            Arrays.fill(elements, null);
            head = 0;
            tail = 0;
            size = 0;
            notFull.signal();
        }
        finally {
            lock.unlock();
        }
    }


    // ------------------------------------------------------------
    // Recovery hook: rebuild state from WAL
    // ------------------------------------------------------------

    /**
     * Factory to create a queue that replays the WAL on startup.
     * Assumes wal.replay() returns the "live" elements in queue order.
     *
     * @param logFile the queue's Write-Ahead-Log file
     * @param logger the logger
     * @return the durable queue with the replayed entries from the Write-Ahead-Log
     * @throws WriteAheadLogException if the queue could not be created from Write-Ahead-Log
     */
    public static DurableBoundedQueue createFromWal(
            final File logFile,
            final WalLogger logger
    ) throws WriteAheadLogException {
        Objects.requireNonNull(logFile);
        Objects.requireNonNull(logger);

        final File walDir = logFile.getParentFile();

        final String queueName = WalQueueManager.toQueueName(logFile);

        try {
            // load all Write-Ahead-Log entries and compact the entries
            final List<WalEntry> entries = WriteAheadLog.compact(
                                                WriteAheadLog.loadAll(logFile, false),
                                                true); // discard expired entries

            // read the configuration WAL entry to get the queue type
            // and its capacity
            final WalEntry firstEntry = CollectionUtil.first(entries);
            if (firstEntry == null || firstEntry.getType() != WalEntryType.CONFIG) {
                final String errMsg = "Failed create queue " + queueName + " from WAL. "
                                       + "The WAL does not have a ConfigWalEntry";
                logger.error(new File(WalQueueManager.toFileName(queueName)), errMsg);
                throw new IpcException(errMsg);
            }

            final ConfigWalEntry config = ConfigWalEntry.fromWalEntry(firstEntry);
            entries.remove(0);  // remove the config entry from the entry list

            final int capacity = config.getQueueCapacity();
            final boolean compress = config.isWalCompressed();

            final WriteAheadLog wal = new WriteAheadLog(
                                              new File(
                                                      walDir,
                                                      WalQueueManager.toFileName(queueName)),
                                              compress,
                                              logger);

            final DurableBoundedQueue q = new DurableBoundedQueue(
                                                queueName,
                                                capacity,
                                                wal,
                                                logger);
            q.replayFromWal(entries);
            return q;
        }
        catch(CorruptedRecordException ex) {
            throw new WriteAheadLogException(
                    String.format(
                        "Failed to load the durable queue %s from its Write-Ahead-Log! "
                        + "The Write-Ahead-Log is corrupted!",
                        queueName),
                    ex);
        }
        catch(Exception ex) {
            throw new WriteAheadLogException(
                    String.format(
                        "Failed to load the durable queue %s from its Write-Ahead-Log!",
                        queueName),
                    ex);
        }
    }

    private void replayFromWal(final List<WalEntry> replayEntries) {
        Objects.requireNonNull(replayEntries);

        lock.lock();
        try {
            // load the write-ahead-log entries into the queue, take care for
            // the queue capacity
            final List<WalEntry> entries = replayEntries.size() <= capacity()
                                                ? replayEntries
                                                : replayEntries.subList(
                                                    replayEntries.size() - capacity(),
                                                    replayEntries.size());

            for(WalEntry e : entries) {
                if (WalEntryType.DATA == e.getType()) {
                    final Message m = MessageWalEntry.fromWalEntry(e).getMessage();
                    enqueueWithoutLogging(m);
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------
    // Non-blocking API
    // ------------------------------------------------------------

    /**
     * Non-blocking offer.
     * @return true if element was added, false if queue is full.
     */
    @Override
    public boolean offer(Message m) {
        Objects.requireNonNull(m);

        handleClosedQueue();

        lock.lock();
        try {
            if (size == elements.length) {
                return false; // full
            }
            enqueueWithLogging(m);
            return true;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Non-blocking poll.
     * @return element or null if queue is empty.
     */
    @Override
    public Message poll() {
        handleClosedQueue();

        lock.lock();
        try {
            if (size == 0) {
                return null;
            }
            return dequeueWithLogging();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Non-blocking peek.
     * @return front element or null if queue is empty.
     */
    public Message peek() {
        handleClosedQueue();

        lock.lock();
        try {
            if (size == 0) {
                return null;
            }
            return elements[head];
        }
        finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------
    // Timed API
    // ------------------------------------------------------------

    /**
     * Timed offer.
     * Waits up to the given time for space to become available.
     *
     * @return true if the element was enqueued, false if timeout elapsed before space was available.
     */
    @Override
    public boolean offer(
            final Message m,
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        Objects.requireNonNull(m);
        Objects.requireNonNull(unit);

        handleClosedQueue();

        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (size == elements.length) {
                if (nanos <= 0L) {
                    return false; // timed out
                }
                nanos = notFull.awaitNanos(nanos);

                handleClosedQueue();
            }
            enqueueWithLogging(m);
            return true;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Timed poll.
     * Waits up to the given time for an element to become available.
     *
     * @return element if available within timeout, null otherwise.
     */
    @Override
    public Message poll(
            final long timeout,
            final TimeUnit unit
    ) throws InterruptedException {
        Objects.requireNonNull(unit);

        handleClosedQueue();

        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (size == 0) {
                if (nanos <= 0L) {
                    return null; // timed out
                }
                nanos = notEmpty.awaitNanos(nanos);

                handleClosedQueue();
            }
            return dequeueWithLogging();
        }
        finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------
    // Blocking API
    // ------------------------------------------------------------

    public void put(final Message m) throws InterruptedException {
        Objects.requireNonNull(m);

        handleClosedQueue();

        lock.lockInterruptibly();
        try {
            while (size == elements.length) {
                notFull.await();
            }
            enqueueWithLogging(m);
        }
        finally {
            lock.unlock();
        }
    }

    public Message take() throws InterruptedException {
        handleClosedQueue();

        lock.lockInterruptibly();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            return dequeueWithLogging();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void onRemove() {
        handleClosedQueue();

        final File walFile = wal.getFile();

        try { wal.close(); } catch(Exception ignore) { }

        if (walFile.exists()) {
            walFile.delete();
        }

        closed = true;

        logInfo("Durable queue '" + queueName + "' removed.");
    }

    @Override
    public void close() {
        if (closed) return;

        try {
            closed = true;
            wal.close();
        }
        finally {
            logInfo("Durable queue '" + queueName + "' closed.");
        }
    }


    private void handleClosedQueue() {
        if (closed) {
            throw new IpcException("The queue " + queueName + " is closed!");
        }
    }


    // ------------------------------------------------------------
    // Internal helpers (must be called with lock held)
    // ------------------------------------------------------------

    private void enqueueWithoutLogging(Message m) {
        Objects.requireNonNull(m);

        elements[tail] = m;
        tail = (tail + 1) % elements.length;
        size++;
        notEmpty.signal();
    }

    private void enqueueWithLogging(Message m) {
        Objects.requireNonNull(m);

        handleClosedQueue();

        if (m.isDurable()) {
            // 1. WAL first (durable intent)
            try {
                wal.append(new MessageWalEntry(m).toWalEntry());
            }
            catch(Exception ex) {
                throw new IpcException("Failed to enqueue message on queue " + queueName, ex);
            }
        }

        // 2. Then in-memory mutation
        enqueueWithoutLogging(m);
    }

    private Message dequeueWithLogging() {
        handleClosedQueue();

        final Message m = elements[head];

        // 1. WAL: mark as consumed
        try {
            wal.append(new AckWalEntry(m.getId()).toWalEntry());
        }
        catch(Exception ex) {
            throw new IpcException("Failed to dequeue message from queue " + queueName, ex);
        }

        // 2. In-memory mutation
        elements[head] = null; // help GC
        head = (head + 1) % elements.length;
        size--;
        notFull.signal();

        return m;
    }

    // ------------------------------------------------------------
    // Internal logger helpers
    // ------------------------------------------------------------

    private void logInfo(final String message) {
        logger.info(
            new File(WalQueueManager.toFileName(queueName)),
            message);
     }



    private final String queueName;
    private final Message[] elements;
    private int head = 0; // index of next element to poll
    private int tail = 0; // index of next slot to offer
    private int size = 0; // number of elements in the queue

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private volatile boolean closed = false;

    private final WriteAheadLog wal;

    private final WalLogger logger;
}