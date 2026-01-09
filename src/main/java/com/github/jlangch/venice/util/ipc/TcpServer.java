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
package com.github.jlangch.venice.util.ipc;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.QueueFactory;
import com.github.jlangch.venice.util.ipc.impl.QueueValidator;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.conn.ServerConnection;
import com.github.jlangch.venice.util.ipc.impl.conn.Subscriptions;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;
import com.github.jlangch.venice.util.ipc.impl.wal.WalQueueManager;

// https://medium.com/coderscorner/tale-of-client-server-and-socket-a6ef54a74763
// https://github.com/baswerc/niossl
// https://github.com/marianobarrios/tls-channel


/**
 * IPC Server
 */
public class TcpServer implements AutoCloseable {

    /**
     * Create a new server on the specified port.
     *
     * <p>The server must be closed after use!
     *
     * @param port a port
     */
    public TcpServer(final int port) {
        this(port, 0);
    }

    /**
     * Create a new server on the specified port and connection accept timeout
     *
     * @param port a port
     * @param timeout a connection accept timeout
     */
    public TcpServer(final int port, final int timeout) {
        this.port = port;
        this.timeout = Math.max(0, timeout);
        this.endpointId = UUID.randomUUID().toString();
    }


    /**
     * Set the executors maximum of parallel connections.
     *
     * <p>Defaults to 20
     *
     * @param count the max parallel connection count
     * @return this server
     */
    public TcpServer setMaxParallelConnections(final int count) {
        mngdExecutor.setMaximumThreadPoolSize(Math.max(1, count));
        return this;
    }

    /**
     * Set the encryption mode
     *
     * @param encrypt if <code>true</code> encrypt the payload data at transport
     *                level communication between this client and the server.
     * @return this server
     */
    public TcpServer setEncryption(final boolean encrypt) {
        if (started.get()) {
            throw new IllegalStateException(
                   "The encryption mode cannot be changed anymore "
                   + "once the server has been started!");
        }
        this.encrypt.set(encrypt);
        return this;
    }

    /**
     * @return <code>true</code> if this server has transport level encryption
     *         enabled else <code>false</code>
     */
    public boolean isEncrypted() {
        return encrypt.get();
    }

    /**
     * Set an authenticator
     *
     * <p>Note: For security reasons enforce encryption to securely send the
     * user credentials from a client to the server!
     *
     * @param authenticator a client authenticator.
     * @return this server
     */
    public TcpServer setAuthenticator(final Authenticator authenticator) {
        Objects.requireNonNull(authenticator);

        if (started.get()) {
            throw new IllegalStateException(
                   "The authenticator cannot be changed anymore "
                   + "once the server has been started!");
        }
        this.authenticator.set(authenticator);
        return this;
    }

    /**
     * Set the compression cutoff size for payload messages.
     *
     * <p>With a negative cutoff size payload messages will not be compressed.
     * If the payload message size is greater than the cutoff size it will be
     * compressed.
     *
     * <p>Defaults to -1 (no compression)
     *
     * @param cutoffSize the compress cutoff size in bytes
     * @return this server
     */
    public TcpServer setCompressCutoffSize(final long cutoffSize) {
        if (started.get()) {
            throw new IllegalStateException(
                   "The compression cutoff size cannot be changed anymore "
                   + "once the server has been started!");
        }
        compressor.set(new Compressor(cutoffSize));
        return this;
    }

    /**
     * @return return the server's payload message compression cutoff size
     */
    public long getCompressCutoffSize() {
        return compressor.get().cutoffSize();
    }

    /**
     * Set the maximum message size.
     *
     * <p>Defaults to 200 MB
     *
     * @param maxSize the max message size in bytes
     * @return this server
     */
    public TcpServer setMaxMessageSize(final long maxSize) {
        if (started.get()) {
            throw new IllegalStateException(
                   "The maximum message size cannot be changed anymore "
                   + "once the server has been started!");
        }
        maxMessageSize.set(Math.max(
                            Messages.MESSAGE_LIMIT_MIN,
                            Math.min(Messages.MESSAGE_LIMIT_MAX, maxSize)));
        return this;
    }

    /**
     * @return return the server's max message size
     */
    public long getMaxMessageSize() {
        return maxMessageSize.get();
    }


    /**
     * Set the max number of queues.
     *
     * <p>Defaults to 20
     *
     * @param maxQueues the max number of queues.
     * @return this server
     */
    public TcpServer setMaxQueues(final long maxQueues) {
        if (started.get()) {
            throw new IllegalStateException(
                   "The maximum queue count cannot be changed anymore "
                   + "once the server has been started!");
        }

        this.maxQueues.set(Math.max(QUEUES_MIN, Math.min(QUEUES_MAX, maxQueues)));
        return this;
    }

    /**
     * @return return the max number of queues.
     */
    public long getMaxQueues() {
        return maxQueues.get();
    }


    /**
     * Give the clients permission to manage (add/remove) queues.
     *
     * <p>Defaults to <code>true</code>
     *
     * <p>Note: Temporary queues are not subject to this permission! They
     *          can be created any time by clients as needed.
     *
     * @param permit if <code>true</code> clients are permitted to add/remove
     *              queues
     * @return this server
     */
    public TcpServer setPermitClientQueueMgmt(final boolean permit) {
        if (started.get()) {
            throw new IllegalStateException(
                   "Cannot change the permission for clients to manage queues "
                   + "once the server has been started!");
        }

        this.permitClientQueueMgmt.set(permit);
        return this;
    }

    /**
     * @return return <code>true</code> if clients are permitted to add/remove
     *         queues else <code>false</code>
     */
    public boolean isPermitClientQueueMgmt() {
        return permitClientQueueMgmt.get();
    }


    /**
     * Set a heartbeat interval in seconds. A value equal or lower to zero  will
     * turnoff the hearbeat.
     *
     * <p>Defaults to <code>0</code> (hearbeat turned off)
     *
     * @param intervalSeconds the heartbeat interval in seconds
     * @return this server
     */
    public TcpServer setHearbeatInterval(final int intervalSeconds) {
        if (started.get()) {
            throw new IllegalStateException(
                   "Cannot change the heartbeat interval once the server has been started!");
        }

        this.heartbeatInterval.set(Math.max(0, intervalSeconds));
        return this;
    }

    /**
     * @return return <code>true</code> if clients are permitted to add/remove
     *         queues else <code>false</code>
     */
    public long setHearbeatInterval() {
        return heartbeatInterval.get();
    }


    /**
     * Enable  Write-Ahead-Logs
     *
     * @param walDir the Write-Ahead-Logs directory
     * @param compress enable/disable Write-Ahead-Log entry compression
     * @param compactAtStart if true compact the Write-Ahead-Log at startup
     * @return this server
     */
    public TcpServer enableWriteAheadLog(
            final File walDir,
            final boolean compress,
            final boolean compactAtStart
     ) {
        Objects.requireNonNull(walDir);

        if (!walDir.isDirectory()) {
            throw new IpcException(
                    "The WAL directory '" + walDir.getAbsolutePath() + "' does not exist!");
        }

        if (started.get()) {
            throw new IllegalStateException(
                    "Cannot enable the Write-Ahead-Log if the server has already been started!");
        }

        this.wal.activate(walDir, compress, compactAtStart);

        return this;
    }

    /**
     * @return return true if Write-Ahead-Log is enabled.
     */
    public boolean isWriteAheadLog() {
        return wal.isEnabled();
    }


    /**
     * Enable the server logger within the specified log directory
     *
     * @param logDir a log directory
     * @return this server
     */
    public TcpServer enableLogger(final File logDir) {
        Objects.requireNonNull(logDir);

        if (!logDir.isDirectory()) {
            throw new IpcException(
                    "The server log directory '" + logDir.getAbsolutePath() + "' does not exist!");
        }

        if (started.get()) {
            throw new IllegalStateException(
                    "Cannot enable the logger if the server has already been started!");
        }

        logger.enable(logDir);

        return this;
    }

    /**
     * @return the endpoint ID of this server
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * @return the server's statistics
     */
    public IServerStatistics getStatistics() {
        return statistics;
    }

    /**
     * clear the server statistics
     */
    public void clearStatistics() {
        statistics.clear();
    }

    /**
     * Loads the compacted messages from a WAL based queue.
     *
     * <p><b>Use for testing and debugging purposes only!</b>
     *
     * @param queueName a queue name
     * @return the compacted messages from the queue.
     */
    public List<IMessage> loadWalQueueMessages(final String queueName) {
        try {
           return wal.loadWalQueueMessages(queueName);
        }
        catch(Exception ex) {
            throw new IpcException(
                "Failed to load messages for WAL queue " + queueName,
                ex);
        }
    }

    /**
     * Start the server without handler for incoming messages.
     *
     * <p>A handler is required for send/receive message passing only.
     */
    public void start() {
        start(missingHandler());
    }

    /**
     * Start the server
     *
     * @param handler to handle the incoming messages. The handler may return a
     *        <code>null</code> message
     */
    public void start(final Function<IMessage,IMessage> handler) {
        Objects.requireNonNull(handler);

        if (authenticator.get().isActive() && !encrypt.get()) {
            throw new IpcException(
                    "Please enable encryption with an active authenticator to securely "
                    + "transfer the credentials freom a client to the server!");
        }

        if (started.compareAndSet(false, true)) {
            final ServerSocketChannel ch = startServer();

            try {
                final ExecutorService executor = mngdExecutor.getExecutor();

                ch.configureBlocking(true);

                logger.info("server", "start", "Server started on port " + port);
                logger.info("server", "start", "Socket Timeout: " + timeout);
                logger.info("server", "start", "Endpoint ID: " + endpointId);
                logger.info("server", "start", "Encryption: " + isEncrypted());
                logger.info("server", "start", "Max Parallel Connections: " + mngdExecutor.getMaximumThreadPoolSize());
                logger.info("server", "start", "Max Queues: " + getMaxQueues());
                logger.info("server", "start", "Max Msg Size: " + getMaxMessageSize());
                logger.info("server", "start", "Compress Cutoff Size: " + getCompressCutoffSize());
                logger.info("server", "start", "Log-File: " + logger.getLogFile());
                logger.info("server", "start", "Write-Ahead-Log: " + isWriteAheadLog());
                logger.info("server", "start", "Write-Ahead-Log-Dir: " + wal.getWalDir());

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
                    p2pQueues.putAll(wal.preloadQueues());
                }

                // run in an executor thread to not block the caller
                executor.execute(() -> {
                   // loop as long the server is not stopped
                    while (started.get()) {
                        try {
                            // wait for an incoming client connection
                            final SocketChannel channel = ch.accept();
                            channel.configureBlocking(true);

                            final long connId = connectionId.incrementAndGet();
                            logger.info(
                                "server", "start",
                                "Server accepted new connection (" + connId + ") from "
                                + IO.getRemoteAddress(channel));

                            final ServerConnection conn = new ServerConnection(
                                                                   this,
                                                                   channel,
                                                                   connId,
                                                                   authenticator.get(),
                                                                   logger,
                                                                   handler,
                                                                   maxMessageSize.get(),
                                                                   maxQueues.get(),
                                                                   permitClientQueueMgmt.get(),
                                                                   heartbeatInterval.get(),
                                                                   wal,
                                                                   subscriptions,
                                                                   publishQueueCapacity,
                                                                   p2pQueues,
                                                                   isEncrypted(),
                                                                   compressor.get(),
                                                                   statistics,
                                                                   () -> mngdExecutor.info());

                            executor.execute(conn);
                        }
                        catch (IOException ex) {
                            logger.warn("server", "conn", "Connection accept/start terminated with an exception.", ex);

                            return; // finish listener
                        }
                    }
                });

                logger.info("server", "start", "Server is operational and ready to accept connections.");
            }
            catch(Exception ex) {
                final String msg = "Closed server on port " + port + "!";
                logger.error("server", "start", msg, ex);

                safeClose(ch);
                started.set(false);
                server.set(null);
                throw new IpcException(msg, ex);
            }
        }
        else {
            final String msg = "The server on port " + port + " has already been started!";
            logger.error("server", "start", msg);

            throw new IpcException(msg);
        }
    }

    /**
     * Close this server
     */
    @Override
    public void close() {
        if (started.compareAndSet(true, false)) {
            logger.info("server", "close", "Server closing...");

            // do not shutdown the thread-pools too early
            IO.sleep(300);

            safeClose(server.get());
            server.set(null);

            // wait max 1'000ms for tasks to be completed
            mngdExecutor.shutdown();
            final boolean terminated = mngdExecutor.awaitTermination(1_000);

            IO.sleep(100);

            logger.info(
                "server", "close",
                terminated
                    ? "Server closed."
                    : "Server closed. Some connections are delaying shutdown confirmation.");
        }
    }

    /**
     * @return <code>true</code> if the server is running else <code>false</code>
     */
    public boolean isRunning() {
       final ServerSocketChannel ch = server.get();
       return ch != null && ch.isOpen();
    }

    /**
     * @return an echo handler
     */
    public static Function<IMessage,IMessage> echoHandler() {
        return req -> req;
    }

    /**
     * @param sleepMillis the handler's sleep time
     * @return an echo handler that sleeps n milliseconds before echoing the request
     */
    public static Function<IMessage,IMessage> echoHandler(final long sleepMillis) {
        return req -> { IO.sleep(sleepMillis); return req; };
    }

    /**
     * @return an echo handler
     */
    public static Function<IMessage,IMessage> missingHandler() {
        return req -> new Message(
                            req.getRequestId(),
                            MessageType.RESPONSE,
                            ResponseStatus.HANDLER_ERROR,
                            true,   // oneway
                            false,  // transient
                            false,  // not subscribed
                            Messages.EXPIRES_NEVER,
                            ((Message)req).getTopics(),
                            "text/plain",
                            "UTF-8",
                            toBytes("Error: There is no handler defined for this server!", "UTF-8"));
    }

    /**
     * Create a new queue.
     *
     * <p>A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param queueName a queue name
     * @param capacity the queue capacity
     * @param bounded if true create a bounded queue else create a circular queue
     * @param durable if true create a durable queue else a nondurable queue
     */
    public void createQueue(
            final String queueName,
            final int capacity,
            final boolean bounded,
            final boolean durable
    ) {
        QueueValidator.validate(queueName);
        if (capacity < 1) {
            throw new IllegalArgumentException("A queue capacity must not be lower than 1");
        }

        if (durable && !wal.isEnabled()) {
            throw new IpcException(
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        // do not overwrite the queue if it already exists
        p2pQueues.computeIfAbsent(
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
     * Get a queue.
     *
     * @param queueName a queue name
     * @return the queue or <code>null</code> if the queue does not exist
     */
    public IpcQueue<Message> getQueue(final String queueName) {
        QueueValidator.validate(queueName);

        return p2pQueues.get(queueName);
    }

    /**
     * Remove a queue.
     *
     * @param queueName a queue name
     */
    public void removeQueue(final String queueName) {
        QueueValidator.validate(queueName);

        p2pQueues.remove(queueName);

        logger.info("server", "queue", String.format("Removed queue %s.", queueName));
    }

    /**
     * Exists queue.
     *
     * @param queueName a queue name
     * @return <code>true</code> if the queue exists else <code>false</code>
     */
    public boolean existsQueue(final String queueName) {
        QueueValidator.validate(queueName);

        return p2pQueues.containsKey(queueName);
    }


    private void safeClose(final ServerSocketChannel ch) {
        if (ch != null) {
            try {
                try {
                    // close the durable queues
                    if (wal.isEnabled()) {
                        wal.close(p2pQueues.values());
                    }
                }
                catch(Exception ex) {
                    logger.warn("server", "close", "Error while closing queue WALs.", ex);
                }

                ch.close();
            }
            catch(Exception ex) {
                logger.warn("server", "close", "Error while closing server.", ex);
            }
        }
    }

    private ServerSocketChannel startServer() {
        ServerSocketChannel srv = null;
        try {
            srv = ServerSocketChannel.open();
            srv.bind(new InetSocketAddress("127.0.0.1", port));
            if (timeout > 0) {
                srv.socket().setSoTimeout(timeout);
            }
            server.set(srv);
            return srv;
        }
        catch(BindException ex) {
            final String msg = "Already running! Failed to start server on port " + port + "!";
            logger.error("server", "start", msg, ex);

            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new IpcException(msg, ex);
        }
        catch(Exception ex) {
            final String msg = "Failed to start server on port " + port + "!";
            logger.error("server", "start", msg, ex);

            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new IpcException(msg, ex);
        }
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }



    public static final long QUEUES_MIN =  1;
    public static final long QUEUES_MAX = 20;

    private final int port;
    private final int timeout;
    private final String endpointId;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
    private final AtomicLong connectionId = new AtomicLong(0);
    private final AtomicReference<Authenticator> authenticator = new AtomicReference<>(new Authenticator(false));
    private final WalQueueManager wal = new WalQueueManager();
    private final int publishQueueCapacity = 50;
    private final ServerStatistics statistics = new ServerStatistics();
    private final Subscriptions subscriptions = new Subscriptions();
    private final Map<String, IpcQueue<Message>> p2pQueues = new ConcurrentHashMap<>();

    // configuration
    private final AtomicLong maxMessageSize = new AtomicLong(Messages.MESSAGE_LIMIT_MAX);
    private final AtomicLong maxQueues = new AtomicLong(QUEUES_MAX);
    private final AtomicBoolean encrypt = new AtomicBoolean(false);
    private final AtomicBoolean permitClientQueueMgmt = new AtomicBoolean(true);
    private final AtomicLong heartbeatInterval = new AtomicLong(0);


    // logger
    private final ServerLogger logger = new ServerLogger();

    // compression
    private final AtomicReference<Compressor> compressor = new AtomicReference<>(Compressor.off());

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-ipcserver-pool", 20);
}
