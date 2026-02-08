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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.ServerFunctionManager;
import com.github.jlangch.venice.util.ipc.impl.ServerQueueManager;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.ServerTopicManager;
import com.github.jlangch.venice.util.ipc.impl.conn.ServerConnection;
import com.github.jlangch.venice.util.ipc.impl.conn.ServerContext;
import com.github.jlangch.venice.util.ipc.impl.conn.SocketChannelFactory;
import com.github.jlangch.venice.util.ipc.impl.conn.Subscriptions;
import com.github.jlangch.venice.util.ipc.impl.dest.queue.IpcQueue;
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

        this.authenticator = config.getAuthenticator();
        this.compressor = new Compressor(config.getCompressCutoffSize());
        this.logger.enable(config.getLogDir());

        this.queueManager = new ServerQueueManager(
                                    config,
                                    new WalQueueManager(),
                                    this.logger);

        this.topicManager = new ServerTopicManager(
                                    config,
                                    this.logger);

        this.functionManager = new ServerFunctionManager(
                                    config,
                                    this.logger);
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
     * Start the server
     */
    public void start() {
        // configuration
        mngdExecutor.setMaximumThreadPoolSize(config.getMaxConnections() + 1);
        if (config.getWalDir() != null) {
            final WalQueueManager wal = queueManager.getWalQueueManager();
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

                logServerStart();

                // Preload the WAL based queues at server startup
                queueManager.preloadWalQueues();

                // run in an executor thread to handle incoming connections to not block the caller
                executor.execute(() -> {
                   // loop as long the server is not stopped
                    while (started.get()) {
                        try {
                            // wait for an incoming client connection
                            final SocketChannel channel = ch.accept();

                            startNewConnection(channel);
                        }
                        catch (IOException ex) {
                            logger.warn(
                                "server", "connection",
                                "Connection accept/start terminated with an exception.",
                                ex);
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
        return req -> {
                        if (sleepMillis > 0L) {
                            IO.sleep(sleepMillis);
                        }
                        return req;
                      };
    }

    /**
     * Create a new queue.
     *
     * <p>A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '.', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param queueName a queue name
     * @param capacity the queue capacity (must be greater than 1)
     * @param type the queue type, bounded or circular
     * @param persistence the persistence, durable or transient
     * @throws IpcException if the queue name does not follow the convention
     *                      for queue names or if the
     */
    public void createQueue(
            final String queueName,
            final int capacity,
            final QueueType type,
            final QueuePersistence persistence
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(persistence);

        queueManager.createQueue(queueName, capacity, type, persistence);
    }

    /**
     * Get a queue.
     *
     * @param queueName a queue name
     * @return the queue or <code>null</code> if the queue does not exist
     */
    public IpcQueue<Message> getQueue(final String queueName) {
        return queueManager.getQueue(queueName);
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
        return queueManager.removeQueue(queueName);
    }

    /**
     * Exists queue.
     *
     * @param queueName a queue name
     * @return <code>true</code> if the queue exists else <code>false</code>
     */
    public boolean existsQueue(final String queueName) {
        return queueManager.existsQueue(queueName);
    }

    /**
     * Get a queue status.
     *
     * @param queueName a queue name
     * @return the queue status
     */
    public Map<String,Object> getQueueStatus(final String queueName) {
        return queueManager.getQueueStatus(queueName);
    }

    /**
     * Create a new topic.
     *
     * <p>A topic name must only contain the characters 'a-z', 'A-Z', '0-9', '.', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param topicName a topic name
     * @throws IpcException if the topic name does not follow the convention
     *                      for topic names or if the
     */
    public void createTopic(final String topicName) {
        topicManager.createTopic(topicName);
    }

    /**
     * Remove a topic.
     *
     * @param topicName a topic name
     */
    public void removeTopic(final String topicName) {
        topicManager.removeTopic(topicName);
    }

    /**
     * Exists topic.
     *
     * @param topicName a topic name
     * @return <code>true</code> if the topic exists else <code>false</code>
     */
    public boolean existsTopic(final String topicName) {
        return topicManager.existsTopic(topicName);
    }


    /**
     * Create a new function.
     *
     * <p>A function name must only contain the characters 'a-z', 'A-Z', '0-9', '.', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param functionName a function name
     * @param func a function
     * @throws IpcException if the function name does not follow the convention
     *                      for function names or if the
     */
    public void createFunction(
            final String functionName,
            final Function<IMessage,IMessage> func
    ) {
        functionManager.createFunction(functionName, func);
    }

    /**
     * Remove a function.
     *
     * @param functionName a function name
     */
    public void removeFunction(final String functionName) {
        functionManager.removeFunction(functionName);
    }

    /**
     * Exists function.
     *
     * @param functionName a function name
     * @return <code>true</code> if the topic exists else <code>false</code>
     */
    public boolean existsFunction(final String functionName) {
        return functionManager.existsFunction(functionName);
    }

    private void startNewConnection(
        final SocketChannel channel
    ) throws IOException {
        final int maxThreadPoolSize = mngdExecutor.getMaximumThreadPoolSize();
        final int threadPoolSize = mngdExecutor.getThreadPoolSize();

        if (threadPoolSize >= maxThreadPoolSize) {
            logTooManyConnectionsError();
            try { channel.close(); } catch(Exception ignore) {}
            return;  // wait for next connection
        }

        if (config.getSndBufSize() > 0) channel.socket().setSendBufferSize(config.getSndBufSize());
        if (config.getRcvBufSize() > 0) channel.socket().setReceiveBufferSize(config.getRcvBufSize());
        channel.configureBlocking(true);

        // TCP_NODELAY is absolutely mandatory on Linux to get high throughput
        // with small messages
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);

        final long connId = connectionId.incrementAndGet();

        final ServerConnection conn = new ServerConnection(
                                               this,
                                               config,
                                               new ServerContext(
                                                       authenticator,
                                                       logger,
                                                       compressor,
                                                       subscriptions,
                                                       publishQueueCapacity,
                                                       statistics,
                                                       () -> mngdExecutor.info()),
                                               queueManager,
                                               topicManager,
                                               functionManager,
                                               channel,
                                               connId);

        try {
            mngdExecutor.getExecutor().execute(conn);
        }
        catch(RejectedExecutionException ex) {
            logger.error("server", "connection", "New connection rejected by thread pool!", ex);
            try { channel.close(); } catch(Exception ignore) {}
        }

        logger.info(
                "server", "connection",
                String.format(
                    "Server accepted new connection (%s) from %s. Thread pool: %d / %d",
                    connId,
                    IO.getRemoteAddress(channel),
                    threadPoolSize,
                    maxThreadPoolSize));
    }

    private void safeClose(final ServerSocketChannel ch) {
        if (ch != null) {
            try {
                try {
                    // close the durable queues
                    queueManager.close();
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

    private void logTooManyConnectionsError() {
        logger.error(
                "server", "connection",
                "Max connection limit (" + config.getMaxConnections() + ") "
                  + "exceeded! Connection rejected! "
                  + "You can increase the server's max connection config value.");
    }

    private void logServerStart() {
        final WalQueueManager wal = queueManager.getWalQueueManager();

        logger.info("server", "start", "Server started on " + config.getConnURI());
        logger.info("server", "start", "Endpoint ID: " + endpointId);
        logger.info("server", "start", "Encryption: " + config.isEncrypting());
        logger.info("server", "start", "Max Connections: " + (mngdExecutor.getMaximumThreadPoolSize() - 1));
        logger.info("server", "start", "Max Queues: " + config.getMaxQueues());
        logger.info("server", "start", "Max Msg Size: " + config.getMaxMessageSize());
        logger.info("server", "start", "Compress Cutoff Size: " + config.getCompressCutoffSize());
        logger.info("server", "start", "Log-File: " + logger.getLogFile());
        logger.info("server", "start", "Heartbeat: " + config.getHeartbeatIntervalSeconds() + "s");
        logger.info("server", "start", "Write-Ahead-Log: " + wal.isEnabled());
        logger.info("server", "start", "Write-Ahead-Log-Dir: " + wal.getWalDir());
    }


    public static final int QUEUES_MIN = 1;
    public static final int QUEUES_MAX_DEFAULT = 20;
    public static final int QUEUES_MAX = 50;

    public static final int TOPICS_MIN = 1;
    public static final int TOPICS_MAX_DEFAULT = 20;
    public static final int TOPICS_MAX = 50;

    public static final int FUNCTIONS_MIN = 0;
    public static final int FUNCTIONS_MAX_DEFAULT = 20;
    public static final int FUNCTIONS_MAX = 50;

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
    private final ServerQueueManager queueManager;
    private final ServerTopicManager topicManager;
    private final ServerFunctionManager functionManager;
    private final ServerStatistics statistics = new ServerStatistics();
    private final Subscriptions subscriptions = new Subscriptions();
    private final ServerLogger logger = new ServerLogger();

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor(
                    "venice-ipcserver-pool",
                    MAX_POOL_THREADS_DEFAULT);
}
