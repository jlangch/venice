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
package com.github.jlangch.venice.util.ipc;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.conn.ClientConnection;
import com.github.jlangch.venice.util.ipc.impl.util.ConstantFuture;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;


/**
 * IPC Client
 *
 * <p>This class is thread-safe!
 */
public class TcpClient implements Cloneable, AutoCloseable {


    private TcpClient(final URI connURI) {
        this.connURI = connURI;
        this.endpointId = UUID.randomUUID().toString();
    }

    /**
     * Create a new client connecting to a server on the local host
     * and port
     *
     * <p>The client must be closed after use!
     *
     * @param port a port
     * @return the TcpClient
     */
    public static TcpClient of(final int port) {
        return of(null, port);
    }

    /**
     * Create a new client connecting to a server on the specified host
     * and port
     *
     * <p>The client must be closed after use!
     *
     * @param host a host
     * @param port a port
     * @return the TcpClient
     */
    public static TcpClient of(final String host, final int port) {
        try {
            final URI uri = new URI(String.format(
                                        "af-inet://%s:%d",
                                        StringUtil.isBlank(host) ? "127.0.0.1" : host,
                                        port));
            return new TcpClient(uri);
        }
        catch(URISyntaxException ex) {
            throw new IpcException("Invalid TcpClient connection URI", ex);
        }
    }

    /**
     * Create a new client for the specified connection URI.
     *
     * <p>The client must be closed after use!
     *
     * <p>Supported socket types:
     * <ul>
     *    <li>AF_INET sockets (TCP/IP sockets)</li>
     *    <li>AF_UNIX domain sockets (Unix sockets, requires junixsocket libraries)</li>
     * </ul>
     *
     * <p>AF_INET
     * af-inet://localhost:33333
     *
     * <p>AF_UNIX
     * af-unix:///path/to/your/socket.sock
     *
     * @param connUri a connection URI
     * @return the TcpClient
     */
    public static TcpClient of(final URI connUri) {
        Objects.requireNonNull(connUri);
        return new TcpClient(connUri);
    }


    public TcpClient openClone() {
        final TcpClient client = new TcpClient(connURI);
        client.setEncryption(encrypt.get());
        if (u != null && p != null) {
            client.open(String.valueOf(u), String.valueOf(p));
        }
        else {
            client.open();
        }
        return client;
    }

   /**
     * Set the encryption mode for this client-server connection.
     *
     * <p>The encryption is basically controlled by the server the client is attached to.
     *
     * <p>If the server requests encryption it cannot be weakened by the client.
     *
     * <pre>
     * ┌────────────────┬────────────────┬──────────────────────────┐
     * │ server encrypt │ client encrypt │ client-server connection │
     * ├────────────────┼────────────────┼──────────────────────────┤
     * │      false     │     false      │      not encrypted       │
     * │      false     │     true       │        encrypted         │
     * │      true      │     false      │        encrypted         │
     * │      true      │     true       │        encrypted         │
     * └────────────────┴────────────────┴──────────────────────────┘
     * </pre>
     *
     * @param encrypt if <code>true</code> encrypt the payload data at transport
     *                level communication between this client and the server.
     */
    public void setEncryption(final boolean encrypt) {
        if (opened.get()) {
            throw new IllegalStateException(
                   "The encryption mode cannot be changed once the client has been opened!");
        }

        this.encrypt.set(encrypt);
    }

    /**
     * @return <code>true</code> if this client has transport level encryption
     *         enabled else <code>false</code>
     */
    public boolean isEncrypted() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the encryption mode!");
        }

        return conn.get().isEncrypted();
    }

    /**
     * @return return <code>true</code> if clients are permitted to add/remove
     *         queues else <code>false</code>
     */
    public boolean isPermitClientQueueMgmt() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the encryption mode!");
        }

        return conn.get().isPermitClientQueueMgmt();
    }

    /**
     * @return return the client's payload message compression cutoff size
     */
    public long getCompressCutoffSize() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the compression cutoff size!");
        }

        return conn.get().getCompressCutoffSize();
    }

    /**
     * @return return the client's max message size
     */
    public long getMaxMessageSize() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the max message size!");
        }

        return conn.get().getMaxMessageSize();
    }

    /**
     * @return return the client's message send count
     */
    public long getMessageSendCount() {
       return opened.get() ? conn.get().getMessageSendCount() : 0L;
    }

    /**
     * @return return the client's message receive count
     */
    public long getMessageReceiveCount() {
       return opened.get() ? conn.get().getMessageReceiveCount() : 0L;
    }

    /**
     * @return the endpoint ID of this client
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * @return the acknowledge mode of this client
     */
    public AcknowledgeMode getAcknowledgeMode() {
        return ackMode.get();
    }

    /**
     * Opens the client
     */
    public void open() {
        open(null, null);
    }

    /**
     * Opens the client with the specified user identity.
     *
     * @param userName authentication user name
     * @param password authentication password
     */
    public void open(final String userName, final String password) {
        if (userName != null && password == null) {
            throw new IpcException("Authentication requires both a user name and a password!");
        }
        if (userName == null && password != null) {
            throw new IpcException("Authentication requires both a user name and a password!");
        }

        if (opened.compareAndSet(false, true)) {
            ClientConnection c = null;
            try {
                c = new ClientConnection(connURI, encrypt.get(), ackMode.get(), userName, password);
                conn.set(c);

                this.u = userName == null ? null : userName.toCharArray();
                this.p = password == null ? null : password.toCharArray();
            }
            catch(Exception ex) {
                IO.safeClose(c);
                opened.set(false);
                conn.set(null);
                throw new IpcException(
                        "Failed to open client for server " + connURI + "!",
                        ex);
            }
        }
        else {
            throw new IpcException("This client is already open!");
        }
    }

    /**
     * @return <code>true</code> if the client is running else <code>false</code>
     */
    public boolean isRunning() {
       final ClientConnection c = conn.get();
       return c != null && c.isOpen();
    }

    /**
     * Closes the client
     */
    @Override
    public void close() {
        if (opened.compareAndSet(true, false)) {
            IO.sleep(100);

            IO.safeClose(conn.get());
            conn.set(null);
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = ((Message)msg).withType(MessageType.REQUEST, false);
        return sendAsync(m);
    }

    /**
     * Subscribe for a topic.
     *
     * <p>Multiple subscriptions with different handlers are supported.
     *
     * @param topic  a topic
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public IMessage subscribe(final String topic, final Consumer<IMessage> handler) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(handler);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        return subscribe(CollectionUtil.toSet(topic), handler);
    }

    /**
     * Subscribe for a set of topics.
     *
     * <p>Multiple subscriptions with different handlers are supported.
     *
     * @param topics  a set of topics
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public IMessage subscribe(final Set<String> topics, final Consumer<IMessage> handler) {
        Objects.requireNonNull(topics);
        Objects.requireNonNull(handler);

        if (topics.isEmpty()) {
            throw new IpcException("A subscription topic set must not be empty!");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final ClientConnection c = conn.get();
        if (c == null || !c.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        c.addSubscriptionHandler(topics, handler);

        final Message m = createSubscribeRequestMessage(topics, endpointId);
        return send(m);
    }

    /**
     * Unsubscribe from a topic.
     *
     * @param topic  a topic
     * @return the response for the subscribe
     */
    public IMessage unsubscribe(final String topic) {
        Objects.requireNonNull(topic);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        return unsubscribe(CollectionUtil.toSet(topic));
    }

    /**
     * Unsubscribe from a set of topics.
     *
     * @param topics  a set of topics
     * @return the response for the subscribe
     */
    public IMessage unsubscribe(final Set<String> topics) {
        Objects.requireNonNull(topics);

        if (topics.isEmpty()) {
            throw new IpcException("A subscription topic set must not be empty!");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final ClientConnection c = conn.get();
        if (c == null || !c.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        c.removeSubscriptionHandler(topics);

        final Message m = createUnsubscribeRequestMessage(topics, endpointId);
        return send(m);
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = ((Message)msg).withType(MessageType.PUBLISH, false);
        return send(m);
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = ((Message)msg).withType(MessageType.PUBLISH, false);
        return sendAsync(m);
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createQueuePollRequestMessage(queueName, queuePollTimeout);

        return sendAsync(m);
    }

    /**
     * Send a test message
     *
     * @param payload a test payload
     * @return the response message
     */
    public IMessage test(final byte[] payload) {
        Objects.requireNonNull(payload);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = new Message(
                                null,
                                MessageType.TEST,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                Messages.EXPIRES_NEVER,
                                Topics.of(Messages.TOPIC_TEST),
                                "application/octet-stream",
                                null,
                                payload);

        return send(m);
    }

    /**
     * Create a new queue.
     *
     * <p>A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * <p>Queue creation by clients is allowed only when explicitly configured on the server.
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
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
                                false,
                                1_000L,
                                Topics.of("queue/create"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to create queue '" + queueName + "'! Reason: " + response.getText());
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
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
            throw new IpcException(
                    "Failed to create temporary queue! Reason: " + response.getText());
        }
    }

    /**
     * Remove a queue.
     *
     * <p>Queue removal by clients is allowed only when explicitly configured on the server.
     *
     * @param queueName a queue name
     */
    public void removeQueue(final String queueName) {
        if (StringUtil.isBlank(queueName)) {
            throw new IllegalArgumentException("A queue name must not be blank");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final String payload = new JsonBuilder()
                                    .add("name", queueName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.REMOVE_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                Topics.of("queue/remove"),
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to remove queue '" + queueName + "'! Reason: " + response.getText());
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

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
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
            throw new IpcException(
                    "Failed to check if queue " + queueName + " exists! Reason: " + response.getText());
        }
    }

    /**
     * Return a queue's status.
     *
     * <p>Queue status checks by clients is allowed only when explicitly configured on the server.
     *
     * @param queueName a queue name
     * @return a map with the status fields
     */
    public Map<String,Object> getQueueStatus(final String queueName) {
        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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
        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

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

    /**
     * Return the server's status.
     *
     * @return a map with the status fields
     */
    public Map<String,Object> getServerStatus() {
        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final VncMap data = getServerStatusRaw();

        @SuppressWarnings("unchecked")
        final Map<String,Object> tmp = (Map<String,Object>)data.convertToJavaObject();

        return tmp;
    }

    /**
     * Return the server's thread pool statistics.
     *
     * @return a map with the statistics
     */
    public Map<String,Object> getServerThreadPoolStatistics() {
        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final VncMap data = getServerThreadPoolStatisticsRaw();

        @SuppressWarnings("unchecked")
        final Map<String,Object> tmp = (Map<String,Object>)data.convertToJavaObject();

        return tmp;
    }

    /**
     * Return the next server error related to this client
     *
     * @return a map with the error or <code>null</code> if no error is available
     */
    @SuppressWarnings("unchecked")
    public Map<String,Object> getNextServerError() {
        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final VncMap data = getNextServerErrorRaw();

        return data == null
                ? null
                : (Map<String,Object>)data.convertToJavaObject();
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
            throw new IpcException(
                    "Failed to check if queue " + queueName + " exists! Reason: " + response.getText());
        }
    }

    private VncMap getServerStatusRaw() {
        final Message m = new Message(
                                null,
                                MessageType.REQUEST,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                Topics.of(Messages.TOPIC_SERVER_STATUS),
                                "text/plain",
                                "UTF-8",
                                new byte[0]);

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
           return (VncMap)response.getVeniceData();
        }
        else {
            throw new IpcException(
                    "Failed get server status! Reason: " + response.getText());
        }
    }

    private VncMap getServerThreadPoolStatisticsRaw() {
        final Message m = new Message(
                                null,
                                MessageType.REQUEST,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                Topics.of(Messages.TOPIC_SERVER_THREAD_POOL_STATS),
                                "text/plain",
                                "UTF-8",
                                new byte[0]);

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
           return (VncMap)response.getVeniceData();
        }
        else {
            throw new IpcException(
                    "Failed get server thread pool statistics! Reason: " + response.getText());
        }
    }

    private VncMap getNextServerErrorRaw() {
        final Message m = new Message(
                                null,
                                MessageType.REQUEST,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                Topics.of(Messages.TOPIC_SERVER_ERROR),
                                "text/plain",
                                "UTF-8",
                                new byte[0]);

        final IMessage response = send(m);
        if (response.getResponseStatus() == ResponseStatus.OK) {
           return (VncMap)response.getVeniceData();
        }
        else if (response.getResponseStatus() == ResponseStatus.QUEUE_EMPTY) {
            return null;
         }
        else {
            throw new IpcException(
                    "Failed get server error! Reason: " + response.getText());
        }
    }


    private IMessage send(final IMessage msg) {
        Objects.requireNonNull(msg);

        final ClientConnection c = conn.get();
        if (c == null || !c.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        validateMessageSize(msg, c);

        if (isClientLocalMessage(msg)) {
            return handleClientLocalMessage(msg);
        }

        return c.send(msg, 10_000);  // TODO: do the timeout right
    }

    private Future<IMessage> sendAsync(final IMessage msg) {
        Objects.requireNonNull(msg);


        final ClientConnection c = conn.get();
        if (c == null || !c.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        validateMessageSize(msg, c);

        // final long msgQueueTimeout = ((Message)msg).getTimeout();

        if (isClientLocalMessage(msg)) {
            return new ConstantFuture<IMessage>(handleClientLocalMessage(msg));
        }

        return c.sendAsync(msg, 10_000);   // TODO: do the timeout right
    }

    private Message handleClientLocalMessage(final IMessage request) {
        if (Messages.TOPIC_CLIENT_THREAD_POOL_STATS.equals(request.getTopic())) {
            // answer locally
            return getClientThreadPoolStatistics();
        }

        return null;  // no local messsage
    }

    private boolean isClientLocalMessage(final IMessage request) {
        return Messages.TOPIC_CLIENT_THREAD_POOL_STATS.equals(request.getTopic());
    }

    private Message getClientThreadPoolStatistics() {
        final VncMap statistics = conn.get().getThreadPoolStatistics();

        return new Message(
                null,
                MessageType.RESPONSE,
                ResponseStatus.OK,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                Topics.of(Messages.TOPIC_CLIENT_THREAD_POOL_STATS),
                "application/json",
                "UTF-8",
                toBytes(Json.writeJson(statistics, false), "UTF-8"));
    }

    private void validateMessageSize(
            final IMessage msg,
            final ClientConnection conn
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(conn);

        if (msg.getData().length > conn.getMaxMessageSize()) {
            throw new IpcException(
                    String.format(
                            "The message (%dB) is too large! The limit is at %dB",
                            msg.getData().length,
                            conn.getMaxMessageSize()));
        }
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
                false,
                null,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.DEFAULT_TIMEOUT,
                Topics.of(topics),
                "text/plain",
                "UTF-8",
                toBytes(endpointId, "UTF-8"));
    }

    private static Message createUnsubscribeRequestMessage(
            final Set<String> topics,
            final String endpointId
    ) {
        return new Message(
                null,
                null,
                MessageType.UNSUBSCRIBE,
                ResponseStatus.NULL,
                false,
                false,
                false,
                null,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.DEFAULT_TIMEOUT,
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
                false,
                queueName,
                replyToQueueName,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                queueOfferTimeout < 0 ? Messages.NO_TIMEOUT : queueOfferTimeout,
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
                false,
                queueName,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                queuePollTimeout < 0 ? Messages.NO_TIMEOUT : queuePollTimeout,
                Topics.of("queue/poll"),
                "application/octet-stream",
                null,
                new byte[0]);
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }

    private volatile char[] u = null;
    private volatile char[] p = null;

    private final URI connURI;
    private final String endpointId;

    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicBoolean encrypt = new AtomicBoolean(false);
    private final AtomicReference<AcknowledgeMode> ackMode = new AtomicReference<>(AcknowledgeMode.NO_ACKNOWLEDGE);
    private final AtomicReference<ClientConnection> conn = new AtomicReference<>();
}
