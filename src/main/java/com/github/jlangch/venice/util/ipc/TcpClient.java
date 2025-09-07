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

import static com.github.jlangch.venice.util.ipc.Status.REQUEST;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_ONE_WAY;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_PUBLISH;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_START_SUBSCRIPTION;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_OK;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.util.ipc.impl.IO;
import com.github.jlangch.venice.util.ipc.impl.Protocol;
import com.github.jlangch.venice.util.ipc.impl.TcpSubscriptionListener;


public class TcpClient implements Closeable {

    /**
     * Create a new TcpClient on the specified port on the local host
     *
     * <p>The client must be closed after use!
     *
     * @param port a port
     */
    public TcpClient(final int port) {
        this("127.0.0.1", port);
    }

    /**
     * Create a new TcpClient on the specified host and port
     *
     * @param host a host
     * @param port a port
     */
    public TcpClient(final String host, final int port) {
        this.host = host;
        this.port = port;
        this.endpointId = UUID.randomUUID().toString();
    }


    /**
     * Set the executors maximum of parallel tasks.
     *
     * <p>Defaults to 10
     *
     * @param count the max parallel task count
     */
    public void setMaximumParallelTasks(final int count) {
        mngdExecutor.setMaximumThreadPoolSize(Math.max(1, count));
    }


    /**
     * @return the endpoint ID of this client
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * Opens the client
     */
    public void open() {
        if (opened.compareAndSet(false, true)) {
            SocketChannel ch = null;
            try {
                ch = SocketChannel.open(new InetSocketAddress(host, port));
                channel.set(ch);
            }
            catch(Exception ex) {
                IO.safeClose(ch);
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

    /**
     * @return <code>true</code> if the client is running else <code>false</code>
     */
    public boolean isRunning() {
       final SocketChannel ch = channel.get();
       return ch != null && ch.isOpen();
    }

    /**
     * Closes the client
     */
    @Override
    public void close() throws IOException {
        if (opened.compareAndSet(true, false)) {
            IO.safeClose(channel.get());
            channel.set(null);
        }
    }

    /**
     * Sends a message to the server and returns the server's
     * response.
     *
     * <p>Blocks while waiting for the server's response.
     *
     * <p>Returns <code>null</code> if a one way message was sent
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg a message
     * @return the response
     */
    public Message sendMessage(final Message msg) {
        Objects.requireNonNull(msg);

        validateMessageSendStatus(msg, REQUEST, REQUEST_ONE_WAY, REQUEST_START_SUBSCRIPTION, REQUEST_PUBLISH);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        final SocketChannel ch = channel.get();

        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        final boolean oneway = msg.getStatus() == Status.REQUEST_ONE_WAY;

        Protocol.sendMessage(ch, msg);

        return oneway ? null : Protocol.receiveMessage(ch);
    }

    /**
     * Sends a message to the server. Throws a TimeoutException if the response
     * is not received within the given timeout.
     *
     * <p>Returns <code>null</code> if a one way message was sent.
     *
     * <p>throws <code>TimeoutException</code> if the message send timed out
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg     a message
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the server's response
     */
    public Message sendMessage(final Message msg, final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(unit);

        validateMessageSendStatus(msg, REQUEST, REQUEST_ONE_WAY, REQUEST_START_SUBSCRIPTION, REQUEST_PUBLISH);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

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

    /**
     * Sends a message asynchronously to the server and returns a Future
     * for the server's response message.
     *
     * <p>Returns <code>null</code> if a one way message was sent
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg  a message
     * @return the future for the server's response
     */
    public Future<Message> sendMessageAsync(final Message msg) {
        Objects.requireNonNull(msg);

        validateMessageSendStatus(msg, REQUEST, REQUEST_ONE_WAY, REQUEST_START_SUBSCRIPTION, REQUEST_PUBLISH);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        final SocketChannel ch = channel.get();

        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        final boolean oneway = msg.getStatus() == Status.REQUEST_ONE_WAY;

        final Callable<Message> task = () -> {
            Protocol.sendMessage(ch, msg);
            return oneway ? null : Protocol.receiveMessage(ch);
        };

        return mngdExecutor
                .getExecutor()
                .submit(task);
    }

    /**
     * Puts this client in subscription mode and listens for subscriptions
     * on the specified topic.
     *
     * <p>throws an exception if the client could not put into subscription mode
     *
     * @param topic  a topic
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public Message subscribe(final String topic, final Consumer<Message> handler) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(handler);

        final SocketChannel ch = channel.get();

        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        if (subscription.compareAndSet(false, true)) {
            try {
                final Message subscribeMsg = Message.text(
                                                REQUEST_START_SUBSCRIPTION,
                                                topic,
                                                "text/plain",
                                                "UTF-8",
                                                endpointId);

                final Callable<Message> task = () -> {
                    Protocol.sendMessage(ch, subscribeMsg);
                    return Protocol.receiveMessage(ch);
                };

                final Message response = mngdExecutor
                                           .getExecutor()
                                           .submit(task)
                                           .get(5, TimeUnit.SECONDS);


                if (response.getStatus() == RESPONSE_OK) {
                    // start the subscription listener in this client
                    mngdExecutor
                        .getExecutor()
                        .submit(new TcpSubscriptionListener(ch, handler));

                    return response;
                }
                else {
                   throw new VncException("Failed to start subscription mode");
                }
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
                subscription.set(false);
                throw new com.github.jlangch.venice.InterruptedException(
                        "Interrupted while waiting for IPC response.");
            }
            catch(Exception ex) {
                subscription.set(false);
                throw ex;
            }
        }
        else {
            throw new VncException("The client is already in subscription mode!");
        }
    }

    /**
     * Publish a message.
     *
     * <p>Any other client with a subscription on the message's topic will receive
     * this message.
     *
     * @param msg the message to publish
     * @return the response for the publish
     */
    public Message publish(final Message msg) {
        Objects.requireNonNull(msg);

        validateMessageSendStatus(msg, REQUEST, REQUEST_ONE_WAY);

        if (subscription.get()) {
            // if this client is in subscription mode publish this message
            // through another client!
            Message response = null;
            try (final TcpClient client = new TcpClient(host, port)) {
                client.open();
                response = client.sendMessage(
                                    msg.withStatus(REQUEST_PUBLISH),
                                    5,
                                    TimeUnit.SECONDS);
            }
            catch(IOException ex) {
                // ignore client close exception
            }
            return response;
        }
        else {
            return sendMessage(msg.withStatus(REQUEST_PUBLISH), 5, TimeUnit.SECONDS);
        }
    }


    private void validateMessageSendStatus(final Message msg, final Status... status) {
        if (!CollectionUtil.toSet(status).contains(msg.getStatus())) {
            throw new VncException(
                    String.format(
                        "Unacceptable message status '%s'",
                        msg.getStatus()));
        }
    }


    private final String host;
    private final int port;
    private final String endpointId;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicReference<SocketChannel> channel = new AtomicReference<>();
    private final AtomicBoolean subscription = new AtomicBoolean(false);

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);
}
