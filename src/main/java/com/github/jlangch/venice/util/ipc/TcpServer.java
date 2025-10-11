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
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.Subscriptions;
import com.github.jlangch.venice.util.ipc.impl.TcpServerConnection;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;

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
    public TcpServer setMaximumParallelConnections(final int count) {
        mngdExecutor.setMaximumThreadPoolSize(Math.max(1, count));
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
    public TcpServer setMaximumMessageSize(final long maxSize) {
        maxMessageSize.set(Math.max(MESSAGE_LIMIT_MIN, Math.min(MESSAGE_LIMIT_MAX, maxSize)));
        return this;
    }

    /**
     * @return return the server's max message size
     */
    public long getMaximumMessageSize() {
        return maxMessageSize.get();
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
    public ServerStatistics getStatistics() {
        return statistics;
    }

    /**
     * clear the server statistics
     */
    public void clearStatistics() {
        statistics.clear();
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

                // run in an executor thread to not block the caller
                executor.execute(() -> {
                    while (started.get()) {
                        try {
                            final SocketChannel channel = ch.accept();
                            channel.configureBlocking(true);
                            final TcpServerConnection conn = new TcpServerConnection(
                                                                   this, channel, handler,
                                                                   maxMessageSize, subscriptions,
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
                safeClose(ch);
                started.set(false);
                server.set(null);
                throw new VncException(
                        "Closed TcpServer @ 127.0.0.1 on port " + port + "!",
                        ex);
            }
        }
        else {
            throw new VncException(
                    "The TcpServer @ 127.0.0.1 on port " + port
                        + " has already been started!");
        }
    }

    /**
     * Close this TcpServer
     */
    @Override
    public void close() throws IOException {
        if (started.compareAndSet(true, false)) {
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
     * Create a new queue.
     *
     * @param queueName a queue name
     * @param capacity the queue capacity
     */
    public void createQueue(final String queueName, final int capacity) {
        Objects.requireNonNull(queueName);
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("A queue capacity must not be lower than 1");
        }

        // do not overwrite the queue if it already exists
        if (!p2pQueues.containsKey(queueName)) {
            // create the queue
            p2pQueues.put(
                queueName,
                new LinkedBlockingQueue<Message>(capacity));
        }
    }

    /**
     * Remove a queue.
     *
     * @param queueName a queue name
     */
    public void removeQueue(final String queueName) {
        Objects.requireNonNull(queueName);

        p2pQueues.remove(queueName);
    }

    /**
     * Exists queue.
     *
     * @param queueName a queue name
     * @return <code>true</code> if the queue exists else <code>false</code>
     */
    public boolean existsQueue(final String queueName) {
        Objects.requireNonNull(queueName);

        return p2pQueues.containsKey(queueName);
    }


    private void safeClose(final ServerSocketChannel ch) {
        if (ch != null) {
            try {
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
            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new VncException(
                    "Failed to start TcpServer @ 127.0.0.1 on port " + port + "! " + ex.getMessage(),
                    ex);
        }
        catch(Exception ex) {
            safeClose(srv);
            started.set(false);
            server.set(null);
            throw new VncException(
                    "Failed to start TcpServer @ 127.0.0.1 on port " + port + "!",
                    ex);
        }
    }


    public static final long MESSAGE_LIMIT_MIN = 2 * 1024;
    public static final long MESSAGE_LIMIT_MAX = 200 * 1024 * 1024;

    private final int port;
    private final int timeout;
    private final String endpointId;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
    private final AtomicLong maxMessageSize = new AtomicLong(MESSAGE_LIMIT_MAX);
    private final int publishQueueCapacity = 50;
    private final ServerStatistics statistics = new ServerStatistics();
    private final Subscriptions subscriptions = new Subscriptions();
    private final Map<String, LinkedBlockingQueue<Message>> p2pQueues = new HashMap<>();

    // compression
    private final AtomicReference<Compressor> compressor = new AtomicReference<>(new Compressor(-1));

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpserver-pool", 20);
}
