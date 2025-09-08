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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.util.ipc.impl.Subscriptions;
import com.github.jlangch.venice.util.ipc.impl.TcpServerConnection;

// https://medium.com/coderscorner/tale-of-client-server-and-socket-a6ef54a74763

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
     */
    public void setMaximumParallelConnections(final int count) {
        mngdExecutor.setMaximumThreadPoolSize(Math.max(1, count));
    }

    /**
     * @return the endpoint ID of this server
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * @return the server's message count
     */
    public long getMessageCount() {
        return messageCount.get();
    }

    /**
     * @return the server's publish count
     */
    public long getPublishCount() {
        return publishCount.get();
    }

    /**
     * @return the server's publish discard count
     */
    public long getPublishDiscardCount() {
        return discardedPublishCount.get();
    }

    /**
     * Start the TcpServer
     *
     * @param handler to handle the incoming messages. The handler may return a
     *        <code>null</code> message
     */
    public void start(final Function<Message,Message> handler) {
        Objects.requireNonNull(handler);

        if (started.compareAndSet(false, true)) {
            final ServerSocketChannel ch = startServer();

            try {
                final ExecutorService executor = mngdExecutor.getExecutor();

                // run in an executor thread to not block the caller
                executor.execute(() -> {
                    while (started.get()) {
                        try {
                            final SocketChannel channel = ch.accept();
                            channel.configureBlocking(true);
                            final TcpServerConnection conn = new TcpServerConnection(
                                                                   this, channel, handler, subscriptions,
                                                                   messageCount, publishCount,
                                                                   discardedPublishCount);
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



    private final int port;
    private final int timeout;
    private final String endpointId;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
    private final AtomicLong messageCount = new AtomicLong(0L);
    private final AtomicLong publishCount = new AtomicLong(0L);
    private final AtomicLong discardedPublishCount = new AtomicLong(0L);
    private final Subscriptions subscriptions = new Subscriptions();

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpserver-pool", 20);
}
