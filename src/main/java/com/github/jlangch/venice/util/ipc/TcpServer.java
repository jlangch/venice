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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;

// https://medium.com/coderscorner/tale-of-client-server-and-socket-a6ef54a74763

public class TcpServer implements Closeable {

    /**
     * Create a new TcpServer on the specified port
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
                            final Connection conn = new Connection(channel, handler);
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
            mngdExecutor.shutdownNow();
            safeClose(server.get());
            server.set(null);
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

    private static class Connection implements Runnable {
        public Connection(
                final SocketChannel ch,
                final Function<Message,Message> handler
        ) {
            this.ch = ch;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                while(ch.isOpen()) {
                    final Message request = Protocol.receiveMessage(ch);
                    if (request == null) {
                        // client closed connection
                        break;
                    }
                    final Message response = handler.apply(request);
                    if (response != null) {
                        Protocol.sendMessage(ch, response);
                    }
                }
            }
            catch(Exception ex) {
                // when client closed the connection -> java.io.IOException: Broken pipe
            }
        }

        private final SocketChannel ch;
        private final Function<Message,Message> handler;
    }



    private final int port;
    private final int timeout;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpserver-pool", 20);
}
