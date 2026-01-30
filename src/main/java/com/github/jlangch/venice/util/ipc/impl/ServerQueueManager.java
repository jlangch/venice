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
package com.github.jlangch.venice.util.ipc.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;
import com.github.jlangch.venice.util.ipc.impl.wal.WalQueueManager;


public class ServerQueueManager {

    public ServerQueueManager(
            final ServerConfig config,
            final WalQueueManager wal,
            final ServerLogger logger
    ) {
        this.wal = wal;
        this.logger = logger;
        this.maxQueues = config.getMaxConnections();
    }

    /**
     * @return <code>true</code> if  Write-Ahead-Logs are enabled else <code>false</code>
     */
    public boolean isWalEnabled() {
        return wal.isEnabled();
    }

    /**
     * @return the WAL queue manager
     */
    public WalQueueManager getWalQueueManager() {
        return wal;
    }

    /**
     * Apply a function on behalf of the server queues
     *
     * @param fn a function
     */
    public void withQueues(final Consumer<Map<String, IpcQueue<Message>>> fn) {
        fn.accept(queues);
    }

   /**
     * @return the number of standard queues (non temporary queues)
     */
    public long countStandardQueues() {
        return queues
                .values()
                .stream()
                .filter(q -> !q.isTemporary())
                .count();
    }

    /**
     * @return the number of temporary queues
     */
    public long countTemporaryQueues() {
        return queues
                .values()
                .stream()
                .filter(q -> q.isTemporary())
                .count();
    }

    /**
     * Preload the WAL based queues at server startup
     */
    public void preloadWalQueues() {
        try {
            if (wal.isEnabled()) {
                final int queueCount = wal.countLogFiles();
                if (queueCount > 0) {
                    logger.info("server", "start", "Loading " + queueCount + " queue(s) from WAL...");
                }

                // Preload the queues from the Write-Ahead-Log
                //
                // Note: 1) must be run after starting the ServerSocketChannel
                //          as a locking mechanism to ensure this server is the
                //          server in charge!
                //       2) must be completed before the server accepts messages
                //          on a SocketChannel!!
                //       3) will replace any queue with the same name created on this
                //          server before starting the server
                queues.putAll(wal.preloadQueues());
            }
        }
        catch(Exception ex) {
            throw new IpcException("Failed to preload WAL based queues!", ex);
        }
    }

    /**
     * Get a queue.
     *
     * @param queueName a queue name
     * @return the queue or <code>null</code> if the queue does not exist
     */
    public IpcQueue<Message> getQueue(final String queueName) {
        return queues.get(queueName);
    }

    /**
     * Remove a queue.
     *
     * <p>Temporary queues cannot be removed. They are implicitly removed if
     * the connection they belong to is closed!
     *
     * @param queueName a queue name
     * @return <code>true</code> if the queue has been removed else <code>false</code>
     */
    public boolean removeQueue(final String queueName) {
        final IpcQueue<Message> queue = queues.get(queueName);
        if (queue != null) {
            if (queue.isTemporary()) {
                throw new IpcException("Cannot remove a temporary queue!");
            }

            queues.remove(queueName);
            queue.onRemove();

            logger.info("server", "queue", String.format("Removed queue %s.", queueName));

            return true;
        }

        return false;
    }

    /**
     * Exists queue.
     *
     * @param queueName a queue name
     * @return <code>true</code> if the queue exists else <code>false</code>
     */
    public boolean existsQueue(final String queueName) {
        final IpcQueue<Message> queue = queues.get(queueName);
        return queue != null && !queue.isTemporary();
    }

    /**
     * Create a new queue.
     *
     * <p>A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param queueName a queue name
     * @param capacity the queue capacity (must be greater than 1)
     * @param bounded if true create a bounded queue else create a circular queue
     * @param durable if true create a durable queue else a nondurable queue
     */
    public void createQueue(
            final String queueName,
            final int capacity,
            final boolean bounded,
            final boolean durable
    ) {
        QueueValidator.validateQueueName(queueName);
        QueueValidator.validateQueueCapacity(capacity);

        if (countStandardQueues() >= maxQueues) {
            throw new IpcException(String.format(
                    "Cannot create queue! Reached the limit of %d queues.",
                    maxQueues));
        }

        if (durable && !wal.isEnabled()) {
            throw new IpcException(
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        // do not overwrite the queue if it already exists
        queues.computeIfAbsent(
            queueName,
            k -> { final IpcQueue<Message> q = QueueFactory.createQueue(
                                                    wal,
                                                    queueName,
                                                    capacity,
                                                    bounded,
                                                    durable);
                   logger.info(
                      "server", "queue",
                      String.format(
                          "Created queue %s. Capacity=%d, bounded=%b, durable=%b",
                          queueName,
                          capacity,
                          bounded,
                          durable));

                   return q;
            });
    }

    /**
     * Get a queue's status.
     *
     * @param queueName a queue name
     * @return the queue or <code>null</code> if the queue does not exist
     */
    public Map<String,Object> getQueueStatus(final String queueName) {
        final IpcQueue<Message> q = queues.get(queueName);

        final Map<String,Object> status = new HashMap<>();

        status.put("name",      queueName);
        status.put("exists",    q != null);
        status.put("type",      q == null ? "unknown" : q.type().name());
        status.put("temporary", q != null && q.isTemporary());
        status.put("durable",   q != null && q.isDurable());
        status.put("capacity",  q == null ? 0L : (long)q.capacity());
        status.put("size",      q == null ? 0L : (long)q.size());

        return status;
    }

    /**
     * Close the queues
     *
     * <p>Closes all durable queue's Write-Ahead-Log
     */
    public void close() {
        try {
            // close the durable queues
            if (wal.isEnabled()) {
                wal.close(queues.values());
            }
        }
        catch(Exception ex) {
            logger.warn("server", "close", "Error closing queues.", ex);
        }
    }


    private final WalQueueManager wal;
    private final ServerLogger logger;
    private final int maxQueues;
    private final Map<String, IpcQueue<Message>> queues = new ConcurrentHashMap<>();
}
