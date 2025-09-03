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
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;


public class TcpClient implements Closeable {

    public TcpClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void open() {
        if (opened.compareAndSet(false, true)) {
            SocketChannel ch = null;
            try {
                ch = SocketChannel.open(new InetSocketAddress(host, port));
                channel.set(ch);
            }
            catch(Exception ex) {
                safeClose(ch);
                opened.set(false);
                channel.set(null);
                throw new VncException(
                        "Failed to open TcpClient for server " + host + "/" + port + "!",
                        ex);
            }
        }
        else {
            throw new VncException("This TcpClient is already open!");
        }
    }

    @Override
    public void close() throws IOException {
        if (opened.compareAndSet(true, false)) {
            safeClose(channel.get());
            channel.set(null);
        }
    }

    public Message sendMessage(final Message msg) {
        Objects.requireNonNull(msg);

        final SocketChannel ch = channel.get();

        if (ch == null) {
            throw new VncException("This TcpClient is not open!");
        }

        Protocol.sendMessage(ch, msg);

        return Protocol.receiveMessage(ch);
    }


    private void safeClose(final SocketChannel ch) {
        if (ch != null) {
            try {
                ch.close();
            }
            catch(Exception ignore) { }
        }
    }


    private final String host;
    private final int port;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicReference<SocketChannel> channel = new AtomicReference<>();
}
