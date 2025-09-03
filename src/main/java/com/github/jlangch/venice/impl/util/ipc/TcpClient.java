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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;


public class TcpClient implements Closeable {

    public TcpClient(final int port) {
        this.host = "127.0.0.1";
        this.port = port;
    }

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

    public Message sendMessage(final Message msg, final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(msg);

        try {
            return sendMessageAsync(msg).get(timeout, unit);
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(TimeoutException ex) {
            throw new com.github.jlangch.venice.TimeoutException(
                    "Timeout while waiting for IPC response.");
        }
        catch(ExecutionException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof VncException) {
                throw (VncException)cause;
            }
            else {
                throw new VncException("Error in IPC call", cause);
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while waiting for IPC response.");
        }
    }

    public Future<Message> sendMessageAsync(final Message msg) {
        Objects.requireNonNull(msg);

        final SocketChannel ch = channel.get();

        if (ch == null) {
            throw new VncException("This TcpClient is not open!");
        }

        final Callable<Message> task = () -> {
            Protocol.sendMessage(ch, msg);
            return Protocol.receiveMessage(ch);
        };

        return mngdExecutor
                .getExecutor()
                .submit(task);
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

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);
}
