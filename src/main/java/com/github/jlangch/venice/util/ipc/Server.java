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

import java.io.IOException;
import java.net.BindException;
import java.net.StandardSocketOptions;
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
import com.github.jlangch.venice.util.ipc.impl.conn.SocketChannelFactory;
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
public class Server implements AutoCloseable {

    private Server(final ServerConfig config) {
        Objects.requireNonNull(config);

        this.config = config;
        this.endpointId = UUID.randomUUID().toString();

        authenticator = config.getAuthenticator();
        compressor = new Compressor(config.getCompressCutoffSize());
        logger.enable(config.getLogDir());
    }


    public static Server of(final ServerConfig config) {
        Objects.requireNonNull(config);
        return new Server(config);
    }

    public static Server of(final int port) {
        return new Server(ServerConfig.of(port));
    }


    /**
     * @return the this server's configuration
     */
    public ServerConfig getConfig() {
        return config;
    }

    /**
     * @return return true if Write-Ahead-Log is enabled.
     */
    public boolean isWriteAheadLog() {
        return wal.isEnabled();
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

        // configuration
        mngdExecutor.setMaximumThreadPoolSize(config.getMaxConnections() + 1);
        if (config.getWalDir() != null) {
            wal.activate(config.getWalDir(), config.isWalCompress(), config.isWalCompactAtStart());
        }
        if (authenticator.isActive() && !config.isEncrypting()) {
            throw new IpcException(
                    "Please enable encryption with an active authenticator to securely "
                    + "transfer the credentials freom a client to the server!");
        }

        if (started.compareAndSet(false, true)) {
            final ServerSocketChannel ch = startServer();

            try {
                ch.configureBlocking(true);

                final ExecutorService executor = mngdExecutor.getExecutor();

                logger.info("server", "start", "Server started on " + config.getConnURI());
                logger.info("server", "start", "Endpoint ID: " + endpointId);
                logger.info("server", "start", "Encryption: " + config.isEncrypting());
                logger.info("server", "start", "Max Parallel Connections: " + (mngdExecutor.getMaximumThreadPoolSize() - 1));
                logger.info("server", "start", "Max Queues: " + config.getMaxQueues());
                logger.info("server", "start", "Max Msg Size: " + config.getMaxMessageSize());
                logger.info("server", "start", "Compress Cutoff Size: " + config.getCompressCutoffSize());
                logger.info("server", "start", "Log-File: " + logger.getLogFile());
                logger.info("server", "start", "Heartbeat: " + config.getHeartbeatIntervalSeconds() + "s");
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
                            if (config.getSndBufSize() > 0) channel.socket().setSendBufferSize(config.getSndBufSize());
                            if (config.getRcvBufSize() > 0) channel.socket().setReceiveBufferSize(config.getRcvBufSize());
                            channel.configureBlocking(true);

                            // TCP_NODELAY is absolutely mandatory on Linux to get high throughput
                            // with small messages
                            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);

                            final long connId = connectionId.incrementAndGet();

                            final ServerConnection conn = new ServerConnection(
                                                                   this,
                                                                   channel,
                                                                   connId,
                                                                   authenticator,
                                                                   logger,
                                                                   handler,
                                                                   config.getMaxMessageSize(),
                                                                   config.getMaxQueues(),
                                                                   config.isPermitClientQueueMgmt(),
                                                                   config.getHeartbeatIntervalSeconds(),
                                                                   wal,
                                                                   subscriptions,
                                                                   publishQueueCapacity,
                                                                   p2pQueues,
                                                                   config.isEncrypting(),
                                                                   compressor,
                                                                   statistics,
                                                                   () -> mngdExecutor.info());

                            final int maxThreadPoolSize = mngdExecutor.getMaximumThreadPoolSize();
                            final int threadPoolSize = mngdExecutor.getThreadPoolSize();

                            if (threadPoolSize < maxThreadPoolSize) {
                                logger.info(
                                        "server", "start",
                                        String.format(
                                            "Server accepted new connection (%s) from %s. Thread pool: %d / %d",
                                            connId,
                                            IO.getRemoteAddress(channel),
                                            threadPoolSize,
                                            maxThreadPoolSize));

                                executor.execute(conn);
                            }
                            else {
                                logger.error("server", "start", "No free thread pool slot! Connection rejected!");
                                try { conn.close(); } catch(Exception ignore) {}
                            }
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
                final String msg = "Closed server on " + config.getConnURI() + "!";
                logger.error("server", "start", msg, ex);

                safeClose(ch);
                started.set(false);
                server.set(null);
                throw new IpcException(msg, ex);
            }
        }
        else {
            final String msg = "The server on " + config.getConnURI() + " has already been started!";
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
            srv = SocketChannelFactory.createServerSocketChannel(config.getConnURI());
            server.set(srv);
            return srv;
        }
        catch(BindException ex) {
            final String msg = "Already running! Failed to start server on " + config.getConnURI() + "!";
            logger.error("server", "start", msg, ex);

            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new IpcException(msg, ex);
        }
        catch(Exception ex) {
            final String msg = "Failed to start server on " + config.getConnURI() + "!";
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


    public static final int QUEUES_MIN =  1;
    public static final int QUEUES_MAX = 20;

    public static final int MAX_CONNECTIONS_DEFAULT = 20;

    // need one extra thread for the connection manager
    private static final int MAX_POOL_THREADS_DEFAULT = MAX_CONNECTIONS_DEFAULT + 1;


    private final String endpointId;
    private final ServerConfig config;
    private final Authenticator authenticator;
    private final Compressor compressor;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
    private final AtomicLong connectionId = new AtomicLong(0);

    private final int publishQueueCapacity = 50;
    private final WalQueueManager wal = new WalQueueManager();
    private final ServerStatistics statistics = new ServerStatistics();
    private final Subscriptions subscriptions = new Subscriptions();
    private final Map<String, IpcQueue<Message>> p2pQueues = new ConcurrentHashMap<>();
    private final ServerLogger logger = new ServerLogger();

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor(
                    "venice-ipcserver-pool",
                    MAX_POOL_THREADS_DEFAULT);
}
