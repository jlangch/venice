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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.github.jlangch.venice.util.ipc.IpcException;


/**
 * Socket channel factory.
 *
 * <p>The factory supports:
 * <ul>
 *    <li>AF_INET sockets (TCP/IP sockets)</li>
 *    <li>AF_UNIX sockets (Unix sockets, requires junixsocket libraries)</li>
 * </ul>
 *
 * <p>AF_INET
 * af-inet://localhost:3333
 *
 * <p>AF_UNIX
 * af-unix:///path/to/your/socket.sock
 *
 * @see <a href="https://kohlschutter.github.io/junixsocket/unixsockets.html">Unix Sockets</a>
 */
public class SocketChannelFactory {

    // ------------------------------------------------------------------------
    // Client SocketChannel
    // ------------------------------------------------------------------------

	public static SocketChannel createSocketChannel(
            final URI conn
    ) throws IOException {
        final String scheme = conn.getScheme();

        if ("af-inet".equals(scheme)) {
            final String host = conn.getHost();
            final int port = conn.getPort();
            return createSocketChannel(host, port);
        }
        else if ("af-unix".equals(scheme)) {
            if (!isJUnixSocketLibAvailable()) {
                throw new IpcException("JUnixSocket lib is not on the classpath!");
            }

            final File socketFile = new File(conn.getPath());

            try {
                // final SocketChannel ch = org.newsclub.net.unix.AFSocketChannel.open();
                // ch.connect(org.newsclub.net.unix.AFUNIXSocketAddress.of(socketFile));

                final Class<?> clazz1 = Class.forName("org.newsclub.net.unix.AFSocketChannel");
                final Method openMethod = clazz1.getMethod("open");
                final Method connectMethod = clazz1.getMethod("connect");

                final Class<?> clazz2 = Class.forName("org.newsclub.net.unix.AFUNIXSocketAddress");
                final Method ofMethod = clazz2.getMethod("of");

                final Object ch = openMethod.invoke(null);
                final Object socketAddr = ofMethod.invoke(socketFile);
                connectMethod.invoke(socketAddr);

                return (SocketChannel)ch;
            }
            catch(Exception ex) {
                throw new IpcException("Failed to create SocketChannel for connection URI " + conn);
            }
        }
        else {
            throw new IpcException(
                    "Invalid connection URI scheme '" + scheme + "'.\n Use: " +
                    "\"af-inet://localhost:3333\" or \"af-unix:///path/to/your/socket.sock\"");
        }
    }

    public static SocketChannel createSocketChannel(
            final String host,
            final int port
    ) throws IOException {
        final SocketChannel ch = SocketChannel.open();
        ch.connect(new InetSocketAddress(host, port));
        return ch;
    }



    // ------------------------------------------------------------------------
    // Server SocketChannel
    // ------------------------------------------------------------------------

    public static ServerSocketChannel createServerSocketChannel(
            final URI conn
    ) throws IOException {
        final String scheme = conn.getScheme();

        if ("af-inet".equals(scheme)) {
            final String host = conn.getHost();
            final int port = conn.getPort();
            return createServerSocketChannel(host, port);
        }
        else if ("af-unix".equals(scheme)) {
            if (!isJUnixSocketLibAvailable()) {
                throw new IpcException("JUnixSocket lib is not on the classpath!");
            }

            final File socketFile = new File(conn.getPath());

            try {
                // final ServerSocketChannel ch = org.newsclub.net.unix.AFServerSocketChannel.open();
                // ch.bind(org.newsclub.net.unix.AFUNIXSocketAddress.of(socketFile));

                final Class<?> clazz1 = Class.forName("org.newsclub.net.unix.AFServerSocketChannel");
                final Method openMethod = clazz1.getMethod("open");
                final Method bindMethod = clazz1.getMethod("bind");

                final Class<?> clazz2 = Class.forName("org.newsclub.net.unix.AFUNIXSocketAddress");
                final Method ofMethod = clazz2.getMethod("of");

                final Object ch = openMethod.invoke(null);
                final Object socketAddr = ofMethod.invoke(socketFile);
                bindMethod.invoke(socketAddr);

                return (ServerSocketChannel)ch;
            }
            catch(Exception ex) {
                throw new IpcException("Failed to create ServerSocketChannel for connection URI " + conn);
            }
        }
        else {
            throw new IpcException(
                    "Invalid connection URI scheme '" + scheme + "'.\n Use: " +
                    "\"af-inet://localhost:3333\" or \"af-unix:///path/to/your/socket.sock\"");
        }
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



    private static boolean isJUnixSocketLibAvailable() {
        try {
            return Class.forName("org.newsclub.net.unix.AFUNIXSocket") != null;
        }
        catch(Exception ex) {
            return false;
        }
    }


}
