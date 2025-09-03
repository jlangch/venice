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
package com.github.jlangch.venice.impl.util.ipc;

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

    public TcpServer(final int port) {
        this.port = port;
    }


    public void start(final Function<Message,Message> handler) {
        Objects.requireNonNull(handler);

        if (started.compareAndSet(false, true)) {
            final ServerSocketChannel ch = startServer();

            try {
                final ExecutorService executor = mngdExecutor.getExecutor();

                // run in a thread to not block the caller
                executor.execute(() -> {
                    while (started.get()) {
                        try {
                            final SocketChannel socket = ch.accept();
                            final Connection conn = new Connection(socket, handler);
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

    @Override
    public void close() throws IOException {
        if (started.compareAndSet(true, false)) {
            mngdExecutor.shutdown();

            safeClose(server.get());
            server.set(null);
        }
    }

    public boolean isRunning() {
        return started.get();
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
            // srv.socket().setSoTimeout(4000);
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
                final Message request = Protocol.receiveMessage(ch);

                final Message response = handler.apply(request);
                Protocol.sendMessage(ch, response);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        private final SocketChannel ch;
        private final Function<Message,Message> handler;
    }



    private final int port;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpserver-pool", 10);
}
