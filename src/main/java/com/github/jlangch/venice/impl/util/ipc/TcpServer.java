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
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;


public class TcpServer implements Closeable {

    public TcpServer(final int port) {
        this.port = port;
    }


    public void start() {
        if (!started.compareAndSet(false, true)) {
            ServerSocketChannel srv = null;
            try {
                srv = ServerSocketChannel.open();
                srv.bind(new InetSocketAddress("127.0.0.1", port));
                server.set(srv);
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
        else {
            throw new VncException(
                    "The TcpServer @ 127.0.0.1 on port " + port
                        + " has already been started!");
        }
    }

    @Override
    public void close() throws IOException {
        if (started.compareAndSet(true, false)) {
            safeClose(server.get());
            server.set(null);
        }
    }


    private void safeClose(final ServerSocketChannel ch) {
        if (ch != null) {
            try {
                ch.close();
            }
            catch(Exception ignore) {}
        }
    }


    private final int port;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> server = new AtomicReference<>();
}
