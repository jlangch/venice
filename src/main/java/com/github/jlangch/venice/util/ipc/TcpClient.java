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
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Protocol;
import com.github.jlangch.venice.util.ipc.impl.TcpSubscriptionListener;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IEncryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
import com.github.jlangch.venice.util.ipc.impl.util.NullEncryptor;


public class TcpClient implements Cloneable, Closeable {

    /**
     * Create a new TcpClient connecting to a TcpServer on the local host
     * and port
     *
     * <p>The client must be closed after use!
     *
     * @param port a port
     */
    public TcpClient(final int port) {
        this("127.0.0.1", port, false);
    }

    /**
     * Create a new TcpClient connecting to a TcpServer on the specified host
     * and port
     *
     * @param host a host
     * @param port a port
     */
    public TcpClient(final String host, final int port) {
        this(host, port, false);
    }

    /**
     * Create a new TcpClient connecting to a TcpServer on the specified host
     * and port
     *
     * @param host a host
     * @param port a port
     * @param encrypt if <code>true</code> encrypt the payload data at transport
     *                level communication between this client and the server.
     */
    public TcpClient(final String host, final int port, final boolean encrypt) {
        this.host = StringUtil.isBlank(host) ? "127.0.0.1" : host;
        this.port = port;
        this.encrypt = encrypt;
        this.endpointId = UUID.randomUUID().toString();
        this.dhKeys = DiffieHellmanKeys.create();
    }


    @Override
    public Object clone() {
        final TcpClient client = new TcpClient(host, port, isEncrypted());
        client.setCompressCutoffSize(getCompressCutoffSize());
        client.setMaximumMessageSize(getMaximumMessageSize());
        return client;
    }

    /**
     * @return <code>true</code> if this client has transport level encryption
     *         enabled else <code>false</code>
     */
    public boolean isEncrypted() {
        return encryptor.get().isActive();
    }

    /**
     * Set the executors maximum of parallel tasks.
     *
     * <p>Defaults to 10
     *
     * @param count the max parallel task count when sending async messages
     * @return this client
     */
    public TcpClient setMaximumParallelTasks(final int count) {
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
    public TcpClient setCompressCutoffSize(final long cutoffSize) {
        compressCutoffSize.set(cutoffSize);
        return this;
    }

    /**
     * @return return the client's payload message compression cutoff size
     */
    public long getCompressCutoffSize() {
        return compressCutoffSize.get();
    }

    /**
     * Set the maximum message size.
     *
     * <p>Defaults to 200 MB
     *
     * @param maxSize the max message size in bytes
     * @return this client
     */
    public TcpClient setMaximumMessageSize(final long maxSize) {
        maxMessageSize.set(Math.max(MESSAGE_LIMIT_MIN, Math.min(MESSAGE_LIMIT_MAX, maxSize)));
        return this;
    }

    /**
     * @return return the client's max message size
     */
    public long getMaximumMessageSize() {
        return maxMessageSize.get();
    }

    /**
     * @return return the client's message send count
     */
    public long getMessageSendCount() {
       return messageSentCount.get();
    }

    /**
     * @return return the client's message receive count
     */
    public long getMessageReceiveCount() {
       return messageReceiveCount.get();
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

            if (encrypt) {
                try {
                    diffieHellmanKeyExchange();
                }
                catch(Exception ex) {
                    IO.safeClose(ch);
                    opened.set(false);
                    channel.set(null);
                    throw new VncException(
                            "Failed to open TcpClient for server " + host + "/" + port +
                            "! Diffie-Hellman key exchange error!",
                            ex);
                }
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
     * Sends a message to the server and returns the server's response.
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
    public IMessage sendMessage(final IMessage msg) {
        Objects.requireNonNull(msg);

        final Message m = ((Message)msg).withType(MessageType.REQUEST, false);

        return send(m);
    }

    /**
     * Sends a on-way message to the server.
     *
     * <p>The server does not send a response message.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg a message
     */
    public void sendMessageOneway(final IMessage msg) {
        Objects.requireNonNull(msg);

        final Message m = ((Message)msg).withType(MessageType.REQUEST, true);

        send(m);
    }

    /**
     * Sends a message to the server. Throws a TimeoutException if the response
     * is not received within the given timeout.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>TimeoutException</code> if the message send timed out
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg     a message
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the server's response
     */
    public IMessage sendMessage(final IMessage msg, final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(unit);

        final Message m = ((Message)msg).withType(MessageType.REQUEST, false);
        return send(m, timeout, unit);
    }

    /**
     * Sends a message asynchronously to the server and returns a Future
     * for the server's response message.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg  a message
     * @return the future for the server's response
     */
    public Future<IMessage> sendMessageAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        final Message m = ((Message)msg).withType(MessageType.REQUEST, false);
        return sendAsync(m);
   }

    /**
     * Subscribe for a topic.
     *
     * <p>Puts this client in subscription mode and listens for subscriptions
     * on the specified topic.
     *
     * <p>throws an exception if the client could not put into subscription mode
     *
     * @param topic  a topic
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public IMessage subscribe(final String topic, final Consumer<IMessage> handler) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(handler);

        return subscribe(CollectionUtil.toSet(topic), handler);
    }

    /**
     * Subscribe for a set of topics.
     *
     * <p>Puts this client in subscription mode and listens for subscriptions
     * on the specified topic.
     *
     * <p>throws an exception if the client could not put into subscription mode
     *
     * @param topics  a set of topics
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public IMessage subscribe(final Set<String> topics, final Consumer<IMessage> handler) {
        Objects.requireNonNull(topics);
        Objects.requireNonNull(handler);

        if (topics.isEmpty()) {
            throw new VncException("A subscription topic set must not be empty!");
        }

        final SocketChannel ch = channel.get();

        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        final Message subscribeMsg = createSubscribeRequestMessage(topics, endpointId);

        if (subscription.compareAndSet(false, true)) {
            try {
                final Callable<IMessage> task = () -> {
                    Protocol.sendMessage(ch, subscribeMsg, compressCutoffSize.get(), encryptor.get());
                    messageSentCount.incrementAndGet();

                    final Message response = Protocol.receiveMessage(ch, encryptor.get());
                    messageReceiveCount.incrementAndGet();

                    return response;
                };

                final IMessage response = deref(
                                            mngdExecutor
                                                .getExecutor()
                                                .submit(task),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    // start the subscription listener in this client
                    mngdExecutor
                        .getExecutor()
                        .submit(new TcpSubscriptionListener(ch, handler, encryptor));

                    return response;
                }
                else {
                   throw new VncException("Failed to start subscription mode");
                }
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
     * Publish a message to any other client that have subscribed to the
     * message's topic.
     *
     * <p>The server sends a response message to confirm the receiving
     * of the message.
     *
     * @param msg the message to publish
     * @return the response confirmation for the publish message
     */
    public IMessage publish(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        final Message m = ((Message)msg).withType(MessageType.PUBLISH, false);

        if (subscription.get()) {
            // if this client is in subscription mode publish this message
            // through another client!
            return sendThroughTemporaryClient(m, 5, TimeUnit.SECONDS);
        }
        else {
            return send(m, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * Offer a message to a queue. Throws a TimeoutException if the response
     * is not received within the given timeout.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>TimeoutException</code> if the message send timed out
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg       a message
     * @param queueName a queue name
     * @param timeout   the maximum time to wait
     * @param unit      the time unit of the timeout argument
     * @return the server's response
     */
    public IMessage offer(
            final IMessage msg,
            final String queueName,
            final long timeout,
            final TimeUnit unit
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(unit);

        validateMessageSize(msg);

        final Message m = createQueueOfferRequestMessage((Message)msg, queueName);

        return send(m, timeout, unit);
    }

    /**
     * Poll a message from a queue. Throws a TimeoutException if the response
     * is not received within the given timeout.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>TimeoutException</code> if the message send timed out
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param queueName a queue name
     * @param timeout   the maximum time to wait
     * @param unit      the time unit of the timeout argument
     * @return the server's response
     */
    public IMessage poll(
            final String queueName,
            final long timeout,
            final TimeUnit unit
    ) {
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(unit);

        final Message m = createQueuePollRequestMessage(queueName);

        return send(m, timeout, unit);
    }

    private IMessage sendThroughTemporaryClient(
            final IMessage msg,
            final long timeout,
            final TimeUnit unit
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(unit);

        // use the same configuration as the parent client
        try (final TcpClient client = (TcpClient)this.clone()) {
            client.open();
            return client.send(msg, timeout, unit);
        }
        catch(IOException ex) {
            // ignore client close exception
            return null;
        }
    }

    private IMessage send(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        if (isClientLocalMessage(msg)) {
            return handleClientLocalMessage(msg);
        }

        Protocol.sendMessage(ch, (Message)msg, compressCutoffSize.get(), encryptor.get());
        messageSentCount.incrementAndGet();

        if (msg.isOneway()) {
            return null;
        }
        else {
            final Message response = Protocol.receiveMessage(ch, encryptor.get());
            messageReceiveCount.incrementAndGet();
            return response;
        }
    }

    private IMessage send(final IMessage msg, final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(unit);

        validateMessageSize(msg);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        if (isClientLocalMessage(msg)) {
            return handleClientLocalMessage(msg);
        }

        return deref(sendAsync(msg), timeout, unit);
    }

    private Future<IMessage> sendAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        if (isClientLocalMessage(msg)) {
            return mngdExecutor
                    .getExecutor()
                    .submit(() -> handleClientLocalMessage(msg));
        }

        return sendAsyncRaw(msg, ch, compressCutoffSize.get(), encryptor.get());
    }

    private Future<IMessage> sendAsyncRaw(
            final IMessage msg,
            final SocketChannel ch,
            final long compressCutoffSize,
            final IEncryptor encryptor
    ) {
        final Callable<IMessage> task = () -> {
            Protocol.sendMessage(ch, (Message)msg, compressCutoffSize, encryptor);
            messageSentCount.incrementAndGet();

            if (msg.isOneway()) {
                return null;
            }
            else {
                final Message response = Protocol.receiveMessage(ch, encryptor);
                messageReceiveCount.incrementAndGet();
                return response;
            }
        };

        return mngdExecutor
                .getExecutor()
                .submit(task);
    }

    private void diffieHellmanKeyExchange() {
        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        final IMessage m = createDiffieHellmanRequestMessage(dhKeys.getPublicKeyBase64());

        // exchange the client's and the server's public key
        final Message response = (Message)deref(
                                    sendAsyncRaw(m, ch, 1, new NullEncryptor()),
                                    2,
                                    TimeUnit.SECONDS);

        if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_ACK) {
            // successfully exchanged keys
            final String serverPublicKey = response.getText();
            encryptor.set(new Encryptor(dhKeys.generateSharedSecret(serverPublicKey)));
        }
        else if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_NAK) {
            // server rejects key exchange
            final String errText = response.getText();
            throw new VncException("Error: The server rejected the Diffie-Hellman key exchange! " + errText);
        }
        else {
            throw new VncException("Failed to process Diffie-Hellman key exchange!");
        }
    }

    private Message handleClientLocalMessage(final IMessage request) {
        if ("client/thread-pool-statistics".equals(request.getTopic())) {
            // answer locally
            return getClientThreadPoolStatistics();
        }

        return null;  // no local messsage
    }

    private boolean isClientLocalMessage(final IMessage request) {
        return "client/thread-pool-statistics".equals(request.getTopic());
    }

    private Message getClientThreadPoolStatistics() {
        final VncMap statistics = mngdExecutor.info();

        return new Message(
                MessageType.RESPONSE,
                ResponseStatus.OK,
                false,
                Topics.of("client/thread-pool-statistics"),
                "application/json",
                "UTF-8",
                toBytes(Json.writeJson(statistics, false), "UTF-8"));
    }

    private void validateMessageSize(final IMessage msg) {
        Objects.requireNonNull(msg);

        if (msg.getData().length > maxMessageSize.get()) {
            throw new VncException(
                    String.format(
                            "The message (%dB) is too large! The limit is at %dB",
                            msg.getData().length,
                            maxMessageSize.get()));
        }
    }

    private IMessage deref(Future<IMessage> future, final long timeout, final TimeUnit unit) {
        try {
            return future.get(timeout, unit);
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

    private static Message createDiffieHellmanRequestMessage(final String clientPublicKey) {
        return new Message(
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.NULL,
                true,
                Topics.of("dh"),
                "text/plain",
                "UTF-8",
                toBytes(clientPublicKey, "UTF-8"));
    }

    private static Message createSubscribeRequestMessage(
            final Set<String> topics,
            final String endpointId
    ) {
        return new Message(
                MessageType.SUBSCRIBE,
                ResponseStatus.NULL,
                false,
                Topics.of(topics),
                "text/plain",
                "UTF-8",
                toBytes(endpointId, "UTF-8"));
    }

    private static Message createQueueOfferRequestMessage(
            final Message msg,
            final String queueName
    ) {
        return new Message(
                null,
                MessageType.OFFER,
                ResponseStatus.NULL,
                false,
                queueName,
                -1,
                msg.getTopics(),
                msg.getMimetype(),
                msg.getCharset(),
                msg.getData());
    }

    private static Message createQueuePollRequestMessage(final String queueName) {
        return new Message(
                null,
                MessageType.POLL,
                ResponseStatus.NULL,
                false,
                queueName,
                -1,
                Topics.of("queue/poll"),
                "application/octet-stream",
                null,
                new byte[0]);
    }


    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    public static final long MESSAGE_LIMIT_MIN = 2 * 1024;
    public static final long MESSAGE_LIMIT_MAX = 200 * 1024 * 1024;

    private final String host;
    private final int port;
    private final String endpointId;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicReference<SocketChannel> channel = new AtomicReference<>();
    private final AtomicBoolean subscription = new AtomicBoolean(false);
    private final AtomicLong compressCutoffSize = new AtomicLong(-1);
    private final AtomicLong maxMessageSize = new AtomicLong(MESSAGE_LIMIT_MAX);
    private final AtomicLong messageSentCount = new AtomicLong(0L);
    private final AtomicLong messageReceiveCount = new AtomicLong(0L);

    // encryption
    private final boolean encrypt;
    private final DiffieHellmanKeys dhKeys;
    private final AtomicReference<IEncryptor> encryptor = new AtomicReference<>(new NullEncryptor());

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);
}
