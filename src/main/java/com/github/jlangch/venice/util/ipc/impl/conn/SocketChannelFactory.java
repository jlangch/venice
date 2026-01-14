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
package com.github.jlangch.venice.util.ipc.impl.conn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 * Socket channel factory.
 *
 * <p>The factory supports:
 * <ul>
 *    <li>AF_INET  (TCP/IP sockets)</li>
 *    <li>AF_UNIX  (Unix domain sockets, requires junixsocket librarries)</li>
 * </ul>
 *
 * <p>AF_INET
 * af-inet://localhost:3333
 *
 * <p>AF_UNIX
 * af-unix:///path/to/your/socket
 */
public class SocketChannelFactory {

    public static SocketChannel createSocketChannel(
            final String host,
            final int port
    ) throws IOException {
        return SocketChannel.open(new InetSocketAddress(host, port));
    }


    public static ServerSocketChannel createServerSocketChannel(
            final int port
    ) throws IOException {
        return createServerSocketChannel("127.0.0.1", port);
    }

    public static ServerSocketChannel createServerSocketChannel(
            final String host,
            final int port
    ) throws IOException {
        final ServerSocketChannel ch = ServerSocketChannel.open();
        ch.bind(new InetSocketAddress(host, port));
        return ch;
    }

}
