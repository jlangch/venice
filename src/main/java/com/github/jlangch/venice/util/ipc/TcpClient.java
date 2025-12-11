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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.TcpSubscriptionListener;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;


public class TcpClient implements Cloneable, Closeable {

    /**
     * Create a new TcpClient connecting to a TcpServer on the local host
     * and port
     *
     * <p>The client is NOT thread safe!
     *
     * <p>The client must be closed after use!
     *
     * @param port a port
     */
    public TcpClient(final int port) {
        this(null, port);
    }

    /**
     * Create a new TcpClient connecting to a TcpServer on the specified host
     * and port
     *
     * <p>The client is NOT thread safe!
     *
     * <p>The client must be closed after use!
     *
     * @param host a host
     * @param port a port
     */
    public TcpClient(final String host, final int port) {
        this.host = StringUtil.isBlank(host) ? "127.0.0.1" : host;
        this.port = port;
        this.endpointId = UUID.randomUUID().toString();
        this.dhKeys = DiffieHellmanKeys.create();
    }


    @Override
    public Object clone() {
        final TcpClient client = new TcpClient(host, port);
        client.setEncryption(isEncrypted());
        return client;
    }

    /**
     * Set the encryption mode
     *
     * @param encrypt if <code>true</code> encrypt the payload data at transport
     *                level communication between this client and the server.
     */
    public void setEncryption(final boolean encrypt) {
        if (opened.get()) {
            throw new VncException(
                   "The encryption mode cannot be set anymore "
                   + "once the client has been opened!");
        }
        this.encrypt.set(encrypt);
    }

    /**
     * @return <code>true</code> if this client has transport level encryption
     *         enabled else <code>false</code>
     */
    public boolean isEncrypted() {
        return encrypt.get();
    }

    /**
     * @return return the client's payload message compression cutoff size
     */
    public long getCompressCutoffSize() {
        return compressor.get().cutoffSize();
    }

    /**
     * @return return the client's max message size
     */
    public long getMaxMessageSize() {
        if (!opened.get()) {
            throw new VncException(
                   "The max message size cannot be queried if the client has "
                   + "been opened and has requested the size from the server!");
        }
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

            // request the config from server
            try {
                final IMessage response = send(createConfigRequestMessage());
                if (response.getResponseStatus() != ResponseStatus.OK) {
                    throw new VncException(
                            "Failed to get client config from server. Server answered with "
                            + response.getResponseStatus() + " !");
                }

                // handle config
                final VncMap config = (VncMap)((Message)response).getVeniceData();
                maxMessageSize.set(
                    Coerce.toVncLong(
                        config.get(
                            new VncKeyword("max-msg-size"),
                            new VncLong(Message.MESSAGE_LIMIT_MAX))).toJavaLong());
                compressor.set(
                    new Compressor(
                        Coerce.toVncLong(
                            config.get(
                                new VncKeyword("compress-cutoff-size"),
                                new VncLong(-1))).toJavaLong()));
                encrypt.set(
                    encrypt.get()             // client side encrypt request
                    || VncBoolean.isTrue(     // server side encrypt request
                        config.get(
                            new VncKeyword(":encrypt"),
                            VncBoolean.False)));
            }
            catch(Exception ex) {
                IO.safeClose(ch);
                opened.set(false);
                channel.set(null);
                throw new VncException("Failed to get client config from server!", ex);
            }

            if (encrypt.get()) {
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
     * <p>Blocks, potentially forever, while waiting for the server's response.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * <p><b>Note:</b>
     *
     * <p>If the server's handler function takes more than a couple of 10 milliseconds to process
     * the request, consider to use <i>offer/poll</i> with a reply queue to improve the system throughput!
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
     * <p>The server does not send a response message. Returns immediately after sending
     * the message. The method just guarantees that the message has been completely sent
     * over the channel, but returns no information whether the server received the message
     * and processed it.
     *
     * @param msg a message
     */
    public void sendMessageOneway(final IMessage msg) {
        Objects.requireNonNull(msg);

        final Message m = ((Message)msg).withType(MessageType.REQUEST, true);

        send(m);
    }

    /**
     * Sends a message asynchronously to the server.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * <p>If the server's handler function takes more than a couple of 10 milliseconds to process
     * the request, consider to use <i>offer/poll</i> with a reply queue to improve the system throughput!
     *
     * @param msg a message
     * @return a future with the server's message response
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
                    Protocol.sendMessage(ch, subscribeMsg, compressor.get(), encryptor.get());
                    messageSentCount.incrementAndGet();

                    final Message response = Protocol.receiveMessage(ch, compressor.get(), encryptor.get());
                    messageReceiveCount.incrementAndGet();

                    return response;
                };

                final IMessage response = deref(
                                            mngdExecutor
                                                .getExecutor()
                                                .submit(task),
                                            5_000);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    // start the subscription listener in this client
                    mngdExecutor
                        .getExecutor()
                        .submit(new TcpSubscriptionListener(ch, handler, compressor.get(), encryptor.get()));

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
            return sendThroughTemporaryClient(m);
        }
        else {
            return send(m);
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
    public Future<IMessage> publishAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        final Message m = ((Message)msg).withType(MessageType.PUBLISH, false);

        if (subscription.get()) {
            // if this client is in subscription mode publish this message
            // through another client!
            return sendThroughTemporaryClientAsync(m);
        }
        else {
            return sendAsync(m);
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
     * @param replyToQueueName an optional reply-to queue name
     * @param queueOfferTimeout the maximum time in milliseconds the server waits offering
     *                          the message to the queue. A timeout of -1 means wait as
     *                          long as it takes.
     * @return the server's response
     */
    public IMessage offer(
            final IMessage msg,
            final String queueName,
            final String replyToQueueName,
            final long queueOfferTimeout
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(queueName);

        validateMessageSize(msg);

        final Message m = createQueueOfferRequestMessage(
                                (Message)msg,
                                queueName,
                                replyToQueueName,
                                queueOfferTimeout);

        return send(m);
    }


    /**
     * Offer a message asynchronously to a queue.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param msg       a message
     * @param queueName a queue name
     * @param replyToQueueName an optional reply-to queue name
     * @param queueOfferTimeout the maximum time in milliseconds the server waits offering
     *                          the message to the queue. A timeout of -1 means wait as
     *                          long as it takes.
     * @return a future with the server's response message
     */
    public Future<IMessage> offerAsync(
            final IMessage msg,
            final String queueName,
            final String replyToQueueName,
            final long queueOfferTimeout
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(queueName);

        validateMessageSize(msg);

        final Message m = createQueueOfferRequestMessage(
                            (Message)msg,
                            queueName,
                            replyToQueueName,
                            queueOfferTimeout);

        return sendAsync(m);
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
     * @param queuePollTimeout the maximum time in milliseconds the server waits polling
     *                         a message from the queue. A timeout of -1 means wait as
     *                         long as it takes.
     * @return the server's response
     */
    public IMessage poll(
            final String queueName,
            final long queuePollTimeout
    ) {
        Objects.requireNonNull(queueName);

        final Message m = createQueuePollRequestMessage(queueName, queuePollTimeout);

        return send(m);
    }

    /**
     * Poll a message asynchronously from a queue.
     *
     * <p>The server sends always a response message back.
     *
     * <p>throws <code>EofException</code> if the channel has reached end-of-stream while reading the response
     *
     * @param queueName a queue name
     * @param queuePollTimeout the maximum time in milliseconds the server waits polling
     *                         a message from the queue. A timeout of -1 means wait as
     *                         long as it takes.
     * @return a future with the server's response message
     */
    public Future<IMessage> pollAsync(
            final String queueName,
            final long queuePollTimeout
    ) {
        Objects.requireNonNull(queueName);

        final Message m = createQueuePollRequestMessage(queueName, queuePollTimeout);

        return sendAsync(m);
    }

    /**
     * Create a new queue.
     *
     * <p>A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param queueName a queue name
     * @param capacity the queue capacity
     * @param bounded if true create a bounded queue else create a circular queue
     * @param durable if true create a durable queue else a nondurable queue
     */
    public void createQueue(
            final String queueName,
            final int capacity,
            final boolean bounded,
            final boolean durable
    ) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("A queue capacity must not be lower than 1");
        }

        final String payload = new JsonBuilder()
                                    .add("name", queueName)
                                    .add("capacity", capacity)
                                    .add("bounded", bounded)
                                    .add("durable", durable)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.CREATE_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/create"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new VncException(
                    "Failed to create queue " + queueName + "! Reason: " + response.getText());
        }
    }

    /**
     * Create a new temporary queue.
     *
     * <p>The temporary queue is automatically removed when the client terminates.
     *
     * @param capacity the queue capacity
     * @return the name of the temporary queue
     */
    public String createTemporaryQueue(final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("A queue capacity must not be lower than 1");
        }

        final String payload = new JsonBuilder()
                                    .add("capacity", capacity)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.CREATE_TEMP_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/create-temporary"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
            return response.getText();
        }
        else {
            throw new VncException(
                    "Failed to create temporary queue! Reason: " + response.getText());
        }
    }

    /**
     * Remove a queue.
     *
     * @param queueName a queue name
     */
    public void removeQueue(final String queueName) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }

        final String payload = new JsonBuilder()
                                    .add("name", queueName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.CREATE_TEMP_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/create-temporary"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new VncException(
                    "Failed to remove queue " + queueName + "! Reason: " + response.getText());
        }
    }

    /**
     * Check if a queue exists.
     *
     * @param queueName a queue name
     * @return true if the queue exists els false
     */
    public boolean existsQueue(final String queueName) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }

        final String payload = new JsonBuilder()
                                    .add("name", queueName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.STATUS_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/status"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
           final VncMap data = (VncMap)response.getVeniceData();
           final VncVal exists = data.get(new VncKeyword("exists"));
           return VncBoolean.isTrue(exists);
        }
        else {
            throw new VncException(
                    "Failed to check if queue " + queueName + " exists! Reason: " + response.getText());
        }
    }

    /**
     * Return a queue's status.
     *
     * @param queueName a queue name
     * @return a map with the status fields
     */
    public Map<String,Object> getQueueStatus(final String queueName) {
        final VncMap data = getQueueStatusRaw(queueName);

       @SuppressWarnings("unchecked")
       final Map<Object,Object> tmp = (Map<Object,Object>)data.convertToJavaObject();

       final Map<String,Object> map = new LinkedHashMap<>();
       map.put("name",      tmp.get("name"));
       map.put("exists",    tmp.get("exists"));
       map.put("type",      tmp.get("type"));
       map.put("temporary", tmp.get("temporary"));
       map.put("durable",   tmp.get("durable"));
       map.put("capacity",  tmp.get("capacity"));
       map.put("size",      tmp.get("size"));

       return map;
    }

    /**
     * Return a queue's status.
     *
     * @param queueName a queue name
     * @return a map with the status fields
     */
    public VncMap getQueueStatusAsVncMap(final String queueName) {
       final VncMap data = getQueueStatusRaw(queueName);

       return VncOrderedMap.of(
               new VncKeyword("name")      , data.get(new VncKeyword("name")),
               new VncKeyword("exists")    , data.get(new VncKeyword("exists")),
               new VncKeyword("type")      , data.get(new VncKeyword("type")),
               new VncKeyword("temporary") , data.get(new VncKeyword("temporary")),
               new VncKeyword("durable")   , data.get(new VncKeyword("durable")),
               new VncKeyword("capacity")  , data.get(new VncKeyword("capacity")),
               new VncKeyword("size")      , data.get(new VncKeyword("size")));
    }

    private VncMap getQueueStatusRaw(final String queueName) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }

        final String payload = new JsonBuilder()
                                    .add("name", queueName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.STATUS_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/status"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
           return (VncMap)response.getVeniceData();
        }
        else {
            throw new VncException(
                    "Failed to check if queue " + queueName + " exists! Reason: " + response.getText());
        }
    }

    private IMessage sendThroughTemporaryClient(
            final IMessage msg
    ) {
        Objects.requireNonNull(msg);

        // use the same configuration as the parent client
        try (final TcpClient client = (TcpClient)this.clone()) {
            client.open();
            return client.send(msg);
        }
        catch(IOException ex) {
            // ignore client close exception
            return null;
        }
    }

    private Future<IMessage> sendThroughTemporaryClientAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        // use the same configuration as the parent client
        try (final TcpClient client = (TcpClient)this.clone()) {
            client.open();
            return client.sendAsync(msg);
        }
        catch(IOException ex) {
            // ignore client close exception
            return new FutureNull();
        }
    }


    private IMessage send(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        if (isClientLocalMessage(msg)) {
            return handleClientLocalMessage(msg);
        }

        return sendAtomically(msg, ch, compressor.get(), encryptor.get());
    }

    private Future<IMessage> sendAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        validateMessageSize(msg);

        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        if (subscription.get()) {
            throw new VncException("A client in subscription mode cannot send request messages!");
        }

        if (isClientLocalMessage(msg)) {
            return mngdExecutor
                    .getExecutor()
                    .submit(() -> handleClientLocalMessage(msg));
        }

        return sendAsyncRaw(msg, ch, compressor.get(), encryptor.get());
    }

    private Future<IMessage> sendAsyncRaw(
            final IMessage msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        final Callable<IMessage> task = () -> sendAtomically(msg, ch, compressor, encryptor);

        return mngdExecutor
                .getExecutor()
                .submit(task);
    }

    private IMessage sendAtomically(
            final IMessage msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        try {
            final boolean auditCount = msg.getType() != MessageType.DIFFIE_HELLMAN_KEY_REQUEST
                                       && msg.getType() != MessageType.CLIENT_CONFIG;

            // sending the request message and receiving the response
            // must be atomic otherwise request and response can be mixed
            // in multi-threaded environments
            if (sendSemaphore.tryAcquire(120L, TimeUnit.SECONDS)) {
                try {
                    Protocol.sendMessage(ch, (Message)msg, compressor, encryptor);
                    if (auditCount) {
                        messageSentCount.incrementAndGet();
                    }

                    if (msg.isOneway()) {
                        return null;
                    }
                    else {
                        final Message response = Protocol.receiveMessage(ch, compressor, encryptor);
                        if (auditCount) {
                            messageReceiveCount.incrementAndGet();
                        }
                        return response;
                    }
                }
                finally {
                    sendSemaphore.release();
                }
            }
            else {
               throw new com.github.jlangch.venice.TimeoutException(
                    "Timeout while trying to send an IPC message.");
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while trying to send an IPC message");
        }
    }

    private void diffieHellmanKeyExchange() {
        final SocketChannel ch = channel.get();
        if (ch == null || !ch.isOpen()) {
            throw new VncException("This TcpClient is not open!");
        }

        final IMessage m = createDiffieHellmanRequestMessage(dhKeys.getPublicKeyBase64());

        // exchange the client's and the server's public key
        final Message response = (Message)deref(
                                    sendAsyncRaw(m, ch, Compressor.off(), Encryptor.off()),
                                    2_000);

        if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_ACK) {
            // successfully exchanged keys
            final String serverPublicKey = response.getText();
            encryptor.set(Encryptor.aes(dhKeys.generateSharedSecret(serverPublicKey)));
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
                null,
                MessageType.RESPONSE,
                ResponseStatus.OK,
                false,
                false,
                Message.EXPIRES_NEVER,
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

    private IMessage deref(Future<IMessage> future, final long timeout) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
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
                null,
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.NULL,
                false,
                false,
                Message.EXPIRES_NEVER,
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
                null,
                null,
                MessageType.SUBSCRIBE,
                ResponseStatus.NULL,
                false,
                false,
                null,
                null,
                System.currentTimeMillis(),
                Message.EXPIRES_NEVER,
                Message.DEFAULT_TIMEOUT,
                Topics.of(topics),
                "text/plain",
                "UTF-8",
                toBytes(endpointId, "UTF-8"));
    }

    private static Message createQueueOfferRequestMessage(
            final Message msg,
            final String queueName,
            final String replyToQueueName,
            final long queueOfferTimeout
    ) {
        return new Message(
                null,
                msg.getRequestId(),
                MessageType.OFFER,
                ResponseStatus.NULL,
                false,
                msg.isDurable(),
                queueName,
                replyToQueueName,
                System.currentTimeMillis(),
                Message.EXPIRES_NEVER,
                queueOfferTimeout < 0 ? Message.NO_TIMEOUT : queueOfferTimeout,
                msg.getTopics(),
                msg.getMimetype(),
                msg.getCharset(),
                msg.getData());
    }

    private static Message createQueuePollRequestMessage(
            final String queueName,
            final long queuePollTimeout
    ) {
        return new Message(
                null,
                null,
                MessageType.POLL,
                ResponseStatus.NULL,
                false,
                false,
                queueName,
                null,
                System.currentTimeMillis(),
                Message.EXPIRES_NEVER,
                queuePollTimeout < 0 ? Message.NO_TIMEOUT : queuePollTimeout,
                Topics.of("queue/poll"),
                "application/octet-stream",
                null,
                new byte[0]);
    }

    private static Message createConfigRequestMessage() {
        return new Message(
                null,
                null,
                MessageType.CLIENT_CONFIG,
                ResponseStatus.NULL,
                false,
                false,
                null,
                null,
                System.currentTimeMillis(),
                Message.EXPIRES_NEVER,
                Message.NO_TIMEOUT,
                Topics.of("client-config"),
                "text/plain",
                "UTF-8",
                new byte[0]);
    }


    private static class FutureNull implements Future<IMessage> {

        public FutureNull() {
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public IMessage get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public IMessage get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }


        private boolean cancelled = false;
    }


    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    private final Semaphore sendSemaphore = new Semaphore(1);

    private final String host;
    private final int port;
    private final String endpointId;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicReference<SocketChannel> channel = new AtomicReference<>();
    private final AtomicBoolean subscription = new AtomicBoolean(false);
    private final AtomicLong maxMessageSize = new AtomicLong(Message.MESSAGE_LIMIT_MAX);
    private final AtomicLong messageSentCount = new AtomicLong(0L);
    private final AtomicLong messageReceiveCount = new AtomicLong(0L);

    // compression
    private final AtomicReference<Compressor> compressor = new AtomicReference<>(Compressor.off());

    // encryption
    private final DiffieHellmanKeys dhKeys;
    private final AtomicBoolean encrypt = new AtomicBoolean(false);
    private final AtomicReference<Encryptor> encryptor = new AtomicReference<>(Encryptor.off());

    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);
}
