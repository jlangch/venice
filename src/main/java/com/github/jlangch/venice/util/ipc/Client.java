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

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.conn.ClientConnection;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.ConstantFuture;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;


/**
 * IPC Client
 *
 * <p>This class is thread-safe!
 */
public class Client implements Cloneable, AutoCloseable {


    private Client(final ClientConfig config) {
        Objects.requireNonNull(config);
        this.config = config;
        this.endpointId = UUID.randomUUID().toString();
    }


    public static Client of(final ClientConfig config) {
        Objects.requireNonNull(config);
        return new Client(config);
    }

    public static Client of(final int port) {
        return new Client(ClientConfig.of(port));
    }


    public Client copy() {
        final Client client = new Client(config);
        return client;
    }

    public Client openClone() {
        final Client client = new Client(config);
        if (u != null && p != null) {
            client.open(String.valueOf(u), String.valueOf(p));
        }
        else {
            client.open();
        }
        return client;
    }


    /**
     * @return the this client's configuration
     */
    public ClientConfig getConfig() {
        return config;
    }


    public String getEndpointId() {
        return endpointId;
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

        return conn.isEncrypted();
    }

    /**
     * @return <code>true</code> if this client has transport level compression
     *         enabled else <code>false</code>
     */
    public boolean isCompressing() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the encryption mode!");
        }

        return conn.isCompressing();
    }

    /**
     * @return return the client's payload message compression cutoff size
     */
    public long getCompressCutoffSize() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the compression cutoff size!");
        }

        return conn.getCompressCutoffSize();
    }

    /**
     * @return return the client's max message size
     */
    public long getMaxMessageSize() {
        if (!opened.get()) {
            throw new IllegalStateException(
                   "Wait until the client has been opened to get the max message size!");
        }

        return conn.getMaxMessageSize();
    }

    /**
     * @return return the client's message send count
     */
    public long getMessageSendCount() {
       return opened.get() ? conn.getMessageSendCount() : 0L;
    }

    /**
     * @return return the client's message receive count
     */
    public long getMessageReceiveCount() {
       return opened.get() ? conn.getMessageReceiveCount() : 0L;
    }

    /**
     * Opens the client
     *
     * @return this client
     */
    public Client open() {
        return open(null, null);
    }

    /**
     * Opens the client with the specified user identity.
     *
     * @param userName authentication user name
     * @param password authentication password
     * @return this client
     */
    public Client open(final String userName, final String password) {
        if (userName != null && password == null) {
            throw new IpcException("Authentication requires both a user name and a password!");
        }
        if (userName == null && password != null) {
            throw new IpcException("Authentication requires both a user name and a password!");
        }

        if (opened.compareAndSet(false, true)) {
            ClientConnection c = null;
            try {
                conn = new ClientConnection(config, userName, password);

                this.u = userName == null ? null : userName.toCharArray();
                this.p = password == null ? null : password.toCharArray();

                return this;
            }
            catch(Exception ex) {
                IO.safeClose(c);
                opened.set(false);
                conn = null;
                throw new IpcException(
                        "Failed to open client for server " + config.getConnURI() + "!",
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
       return conn != null && conn.isOpen();
    }

    /**
     * Closes the client
     */
    @Override
    public void close() {
        if (opened.compareAndSet(true, false)) {
            IO.sleep(100);

            IO.safeClose(conn);
            conn = null;
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
     * @param functionName the of the server handler function function
     * @return the response
     */
    public IMessage sendMessage(
            final IMessage msg,
            final String functionName
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(functionName);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createSendRequestMessage(msg, functionName, false);
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
     * @param functionName the of the server handler function function
     */
    public void sendMessageOneway(
            final IMessage msg,
            final String functionName
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(functionName);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createSendRequestMessage(msg, functionName, true);
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
     * @param functionName the of the server handler function function
     * @return a future with the server's message response
     */
    public Future<IMessage> sendMessageAsync(
            final IMessage msg,
            final String functionName
    ) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(functionName);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createSendRequestMessage(msg, functionName, false);
        return sendAsync(m);
    }

    /**
     * Subscribe for a topics.
     *
     * <p>Multiple subscriptions with different handlers are supported.
     *
     * @param topicName  a topic name
     * @param handler the subscription message handler
     * @return the response for the subscribe
     */
    public IMessage subscribe(final String topicName, final Consumer<IMessage> handler) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(handler);

        if (topicName.isEmpty()) {
            throw new IpcException("A subscription topic set must not be empty!");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        if (conn == null || !conn.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        conn.addSubscriptionHandler(topicName, handler);

        final Message m = createSubscribeRequestMessage(topicName, getEndpointId());
        return send(m);
    }

    /**
     * Unsubscribe from a topic.
     *
     * @param topicName  a topic name
     * @return the response for the subscribe
     */
    public IMessage unsubscribe(final String topicName) {
        Objects.requireNonNull(topicName);

        if (topicName.isEmpty()) {
            throw new IpcException("A subscription topic set must not be empty!");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        if (conn == null || !conn.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        conn.removeSubscriptionHandler(topicName);

        final Message m = createUnsubscribeRequestMessage(topicName, getEndpointId());
        return send(m);
    }

    /**
     * Publish a message to any other client that have subscribed to the
     * message's topic.
     *
     * <p>The server sends a response message to confirm the receiving
     * of the message.
     *
     * @param topicName the topic to publish to
     * @param msg the message to publish
     * @return the response confirmation for the publish message
     */
    public IMessage publish(final String topicName, final IMessage msg) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(msg);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createTopicPublishRequestMessage((Message)msg, topicName);
        return send(m);
    }


    /**
     * Publish a message to any other client that have subscribed to the
     * message's topic.
     *
     * <p>The server sends a response message to confirm the receiving
     * of the message.
     *
     * @param topicName the topic to publish to
     * @param msg the message to publish
     * @return the response confirmation for the publish message
     */
    public Future<IMessage> publishAsync(final String topicName, final IMessage msg) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(msg);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final Message m = createTopicPublishRequestMessage((Message)msg, topicName);
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
     * @param oneway if true send oneway messages
     * @return the response message
     */
    public IMessage test(final byte[] payload, final boolean oneway) {
        Objects.requireNonNull(payload);

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        return send(Messages.testMessage(payload, oneway));
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
     * @param type the queue type, bounded or circular
     * @param persistence the persistence, durable or transient
     */
    public void createQueue(
            final String queueName,
            final int capacity,
            final QueueType type,
            final QueuePersistence persistence
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(persistence);

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
                                    .add("type", type.name())
                                    .add("persistence", persistence.name())
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.CREATE_QUEUE,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
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
                                "",
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
                                "",
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
                                "",
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
     * Create a new topic.
     *
     * <p>A topic name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'.
     * Up to 80 characters are allowed.
     *
     * @param topicName a topic name
     * @throws IpcException if the topic name does not follow the convention
     *                      for topic names or if the
     */
    public void createTopic(final String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new IllegalArgumentException("A topic name must not be blank");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final String payload = new JsonBuilder()
                                    .add("name", topicName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.CREATE_TOPIC,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to create topic '" + topicName + "'! Reason: " + response.getText());
        }
    }

    /**
     * Remove a topic.
     *
     * @param topicName a topic name
     */
    public void removeTopic(final String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new IllegalArgumentException("A topic name must not be blank");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final String payload = new JsonBuilder()
                                    .add("name", topicName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.REMOVE_TOPIC,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
                                "application/json",
                                "UTF-8",
                                toBytes(payload, "UTF-8"));

        final IMessage response = send(m);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to remove topic '" + topicName + "'! Reason: " + response.getText());
        }
    }

    /**
     * Exists topic.
     *
     * @param topicName a topic name
     * @return <code>true</code> if the topic exists else <code>false</code>
     */
    public boolean existsTopic(final String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new IllegalArgumentException("A topic name must not be blank");
        }

        if (!opened.get()) {
            throw new IllegalStateException("The client is not open!");
        }

        final String payload = new JsonBuilder()
                                    .add("name", topicName)
                                    .toJson(false);

        final Message m = new Message(
                                null,
                                MessageType.STATUS_TOPIC,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
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
                    "Failed to check if topic " + topicName + " exists! Reason: " + response.getText());
        }
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
     * Calculate the effective message size
     *
     * <p>Returns a map with the break down message size:
     *
     * <ul>
     *   <li>header: the header size</li>
     *   <li>payload-meta: the payload meta data size</li>
     *   <li>payload: the payload size</li>
     *   <li>total: the total size</li>
     * </ul>
     *
     * @param message a message
     * @return the message size info
     */
    public static Map<String,Integer> msgSize(final IMessage message) {
        Objects.requireNonNull(message);

        return new Protocol().messageSize((Message)message);
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
                                "",
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
                                MessageType.SERVER_STATUS,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
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
                                MessageType.SERVER_THREAD_POOL_STAT,
                                ResponseStatus.NULL,
                                false,
                                false,
                                false,
                                1_000L,
                                "",
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

    private IMessage send(final IMessage msg) {
        Objects.requireNonNull(msg);

        if (conn == null || !conn.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        validateMessageSize(msg, conn);

        if (isClientLocalMessage(msg)) {
            return getClientThreadPoolStatistics();
        }

        return conn.send(msg, 10_000);  // TODO: do the timeout right
    }

    private Future<IMessage> sendAsync(final IMessage msg) {
        Objects.requireNonNull(msg);


        if (conn == null || !conn.isOpen()) {
            throw new IpcException("This client is not open!");
        }

        validateMessageSize(msg, conn);

        // final long msgQueueTimeout = ((Message)msg).getTimeout();

        if (isClientLocalMessage(msg)) {
            return new ConstantFuture<IMessage>(getClientThreadPoolStatistics());
        }

        return conn.sendAsync(msg, 10_000);   // TODO: do the timeout right
    }

    private boolean isClientLocalMessage(final IMessage request) {
        return Messages.SUBJECT_CLIENT_THREAD_POOL_STATS.equals(request.getSubject());
    }

    private Message getClientThreadPoolStatistics() {
        final VncMap statistics = conn.getThreadPoolStatistics();

        return new Message(
                null,
                MessageType.RESPONSE,
                ResponseStatus.OK,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                Messages.SUBJECT_CLIENT_THREAD_POOL_STATS,
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
            final String topicName,
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
                topicName,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.DEFAULT_TIMEOUT,
                "",
                "text/plain",
                "UTF-8",
                toBytes(endpointId, "UTF-8"));
    }

    private static Message createUnsubscribeRequestMessage(
            final String topicName,
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
                topicName,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.DEFAULT_TIMEOUT,
                "",
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
                msg.getSubject(),
                msg.getMimetype(),
                msg.getCharset(),
                msg.getData());
    }

    private static Message createTopicPublishRequestMessage(
            final Message msg,
            final String topicName
    ) {
        return new Message(
                null,
                msg.getRequestId(),
                MessageType.PUBLISH,
                ResponseStatus.NULL,
                false,
                msg.isDurable(),
                false,
                topicName,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.NO_TIMEOUT,
                msg.getSubject(),
                msg.getMimetype(),
                msg.getCharset(),
                msg.getData());
    }

    private static Message createSendRequestMessage(
            final IMessage msg,
            final String functionName,
            final boolean oneway

    ) {
        return new Message(
                null,
                msg.getRequestId(),
                MessageType.REQUEST,
                ResponseStatus.NULL,
                oneway,
                msg.isDurable(),
                false,
                functionName,
                null,
                System.currentTimeMillis(),
                Messages.EXPIRES_NEVER,
                Messages.NO_TIMEOUT,
                msg.getSubject(),
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
                "",
                "application/octet-stream",
                null,
                new byte[0]);
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    private volatile char[] u = null;
    private volatile char[] p = null;

    private final String endpointId;

    private final ClientConfig config;

    private final AtomicBoolean opened = new AtomicBoolean(false);
    private volatile ClientConnection conn = null;
}
