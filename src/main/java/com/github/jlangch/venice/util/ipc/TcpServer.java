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
package com.github.jlangch.venice.util.ipc;

import java.io.Closeable;
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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.QueueFactory;
import com.github.jlangch.venice.util.ipc.impl.QueueValidator;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.Subscriptions;
import com.github.jlangch.venice.util.ipc.impl.TcpServerConnection;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;
import com.github.jlangch.venice.util.ipc.impl.wal.WalQueueManager;

// https://medium.com/coderscorner/tale-of-client-server-and-socket-a6ef54a74763
// https://github.com/baswerc/niossl
// https://github.com/marianobarrios/tls-channel

public class TcpServer implements Closeable {

    /**
     * Create a new TcpServer on the specified port.
     *
     * <p>The server must be closed after use!
     *
     * @param port a port
     */
    public TcpServer(final int port) {
        this(port, 0);
    }

    /**
     * Create a new TcpServer on the specified port and connection accept timeout
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
     */
    public void setEncryption(final boolean encrypt) {
        if (started.get()) {
            throw new VncException(
                   "The encryption mode cannot be set anymore "
                   + "once the server has been started!");
        }
        this.encrypt.set(encrypt);
    }

    /**
     * @return <code>true</code> if this server has transport level encryption
     *         enabled else <code>false</code>
     */
    public boolean isEncrypted() {
        return encrypt.get();
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
            throw new VncException(
                   "The compression cutoff size cannot be set anymore "
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
        maxMessageSize.set(Math.max(MESSAGE_LIMIT_MIN, Math.min(MESSAGE_LIMIT_MAX, maxSize)));
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
     * Enable  Write-Ahead-Logs
     *
     * @param walDir the Write-Ahead-Logs directory
     * @param compress enable/disable Write-Ahead-Log entry compression
     * @param compactAtStart if true compact the Write-Ahead-Log at startup
     */
    public void enableWriteAheadLog(
            final File walDir,
            final boolean compress,
            final boolean compactAtStart
     ) {
        Objects.requireNonNull(walDir);

        if (!walDir.isDirectory()) {
            throw new VncException(
                    "The WAL directory '" + walDir.getAbsolutePath() + "' does not exist!");
        }

        if (started.get()) {
            throw new VncException(
                    "Cannot enable the Write-Ahead-Log if the server has already been started!");
        }

        this.wal.activate(walDir, compress, compactAtStart);
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
     */
    public void enableLogger(final File logDir) {
        Objects.requireNonNull(logDir);

        if (!logDir.isDirectory()) {
            throw new VncException(
                    "The server log directory '" + logDir.getAbsolutePath() + "' does not exist!");
        }

        logger.enable(logDir);

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
           throw new VncException(
               "Failed to load messages for WAL queue " + queueName,
               ex);
       }
    }

    /**
     * Start the TcpServer without handler for incoming messages.
     *
     * <p>A handler is required for send/receive message passing only.
     */
    public void start() {
        start(missingHandler());
    }

    /**
     * Start the TcpServer
     *
     * @param handler to handle the incoming messages. The handler may return a
     *        <code>null</code> message
     */
    public void start(final Function<IMessage,IMessage> handler) {
        Objects.requireNonNull(handler);

        if (started.compareAndSet(false, true)) {
            final ServerSocketChannel ch = startServer();

            try {
                final ExecutorService executor = mngdExecutor.getExecutor();

                ch.configureBlocking(true);

                logger.info("server", "Server started on port " + port);
                logger.info("server", "Socket Timeout: " + timeout);
                logger.info("server", "Encryption: " + isEncrypted());
                logger.info("server", "Max Queues: " + getMaxQueues());
                logger.info("server", "Max Msg Size: " + getMaxMessageSize());
                logger.info("server", "Compress Cutoff Size: " + getCompressCutoffSize());
                logger.info("server", "Log-File: " + logger.getLogFile());
                logger.info("server", "Write-Ahead-Log: " + isWriteAheadLog());
                logger.info("server", "Write-Ahead-Log-Dir: " + wal.getWalDir());

                if (wal.isEnabled()) {
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
                                "server",
                                "Server accepted new connection (" + connId + ") from "
                                + IO.getRemoteAddress(channel));

                            final TcpServerConnection conn = new TcpServerConnection(
                                                                   this,
                                                                   channel,
                                                                   connId,
                                                                   logger,
                                                                   handler,
                                                                   maxMessageSize,
                                                                   maxQueues,
                                                                   wal,
                                                                   subscriptions,
                                                                   publishQueueCapacity,
                                                                   p2pQueues,
                                                                   compressor.get(),
                                                                   statistics,
                                                                   () -> mngdExecutor.info());

                            executor.execute(conn);
                        }
                        catch (IOException ignored) {
                            return; // finish listener
                        }
                    }
                });
            }
            catch(Exception ex) {
                final String msg = "Closed server on port " + port + "!";
                logger.error("server", msg, ex);

                safeClose(ch);
                started.set(false);
                server.set(null);
                throw new VncException(msg, ex);
            }
        }
        else {
            final String msg = "The server on port " + port + " has already been started!";
            logger.error("server", msg);

            throw new VncException(msg);
        }
    }

    /**
     * Close this TcpServer
     */
    @Override
    public void close() throws IOException {
        if (started.compareAndSet(true, false)) {
            // do not shutdown the thread-pools too early
            try { Thread.sleep(300); } catch(Exception ignore ) {}

            safeClose(server.get());
            server.set(null);
            mngdExecutor.shutdownNow();
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
     * @return an echo handler
     */
    public static Function<IMessage,IMessage> missingHandler() {
        return req -> new Message(
                            req.getRequestId(),
                            MessageType.RESPONSE,
                            ResponseStatus.HANDLER_ERROR,
                            true,   // oneway
                            false,  // transient
                            Message.EXPIRES_NEVER,
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
            throw new VncException(
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        // do not overwrite the queue if it already exists
        p2pQueues.computeIfAbsent(
            queueName,
            k -> QueueFactory.createQueue(
                    wal,
                    queueName,
                    capacity,
                    bounded,
                    durable));
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
                // close the durable queues
                if (wal.isEnabled()) {
                    wal.close(p2pQueues.values());
                }

                ch.close();
            }
            catch(Exception ignore) {}
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
            logger.error("server", msg, ex);

            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new VncException(msg, ex);
        }
        catch(Exception ex) {
            final String msg = "Failed to start server on port " + port + "!";
            logger.error("server", msg, ex);

            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new VncException(msg, ex);
        }
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }



    public static final long MESSAGE_LIMIT_MIN = 2 * 1024;
    public static final long MESSAGE_LIMIT_MAX = 200 * 1024 * 1024;
    public static final long QUEUES_MIN = 201;
    public static final long QUEUES_MAX = 20;

    private final int port;
    private final int timeout;
    private final String endpointId;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
    private final AtomicLong maxMessageSize = new AtomicLong(MESSAGE_LIMIT_MAX);
    private final AtomicLong maxQueues = new AtomicLong(QUEUES_MAX);
    private final AtomicBoolean encrypt = new AtomicBoolean(false);
    private final AtomicLong connectionId = new AtomicLong(0);
    private final WalQueueManager wal = new WalQueueManager();
    private final int publishQueueCapacity = 50;
    private final ServerStatistics statistics = new ServerStatistics();
    private final Subscriptions subscriptions = new Subscriptions();
    private final Map<String, IpcQueue<Message>> p2pQueues = new ConcurrentHashMap<>();

    // logger
    private final ServerLogger logger = new ServerLogger();

    // compression
    private final AtomicReference<Compressor> compressor = new AtomicReference<>(Compressor.off());

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpserver-pool", 20);
}
