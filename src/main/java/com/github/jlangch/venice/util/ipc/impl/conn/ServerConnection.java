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

import static com.github.jlangch.venice.util.ipc.MessageType.AUTHENTICATION;
import static com.github.jlangch.venice.util.ipc.MessageType.CLIENT_CONFIG;
import static com.github.jlangch.venice.util.ipc.MessageType.CREATE_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.CREATE_TEMP_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.CREATE_TOPIC;
import static com.github.jlangch.venice.util.ipc.MessageType.DIFFIE_HELLMAN_KEY_REQUEST;
import static com.github.jlangch.venice.util.ipc.MessageType.HEARTBEAT;
import static com.github.jlangch.venice.util.ipc.MessageType.OFFER;
import static com.github.jlangch.venice.util.ipc.MessageType.POLL;
import static com.github.jlangch.venice.util.ipc.MessageType.PUBLISH;
import static com.github.jlangch.venice.util.ipc.MessageType.REMOVE_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.REMOVE_TOPIC;
import static com.github.jlangch.venice.util.ipc.MessageType.REQUEST;
import static com.github.jlangch.venice.util.ipc.MessageType.RESPONSE;
import static com.github.jlangch.venice.util.ipc.MessageType.SERVER_STATUS;
import static com.github.jlangch.venice.util.ipc.MessageType.SERVER_THREAD_POOL_STAT;
import static com.github.jlangch.venice.util.ipc.MessageType.STATUS_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.STATUS_TOPIC;
import static com.github.jlangch.venice.util.ipc.MessageType.SUBSCRIBE;
import static com.github.jlangch.venice.util.ipc.MessageType.TEST;
import static com.github.jlangch.venice.util.ipc.MessageType.UNSUBSCRIBE;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.BAD_REQUEST;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.DIFFIE_HELLMAN_ACK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.DIFFIE_HELLMAN_NAK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.FUNCTION_NOT_FOUND;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.HANDLER_ERROR;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.NO_PERMISSION;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.OK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_ACCESS_INTERRUPTED;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_EMPTY;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_FULL;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_NOT_FOUND;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.TOPIC_NOT_FOUND;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.AcknowledgeMode;
import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.QueuePersistence;
import com.github.jlangch.venice.util.ipc.QueueType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.QueueValidator;
import com.github.jlangch.venice.util.ipc.impl.ServerFunctionManager;
import com.github.jlangch.venice.util.ipc.impl.ServerQueueManager;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.ServerTopicManager;
import com.github.jlangch.venice.util.ipc.impl.TopicValidator;
import com.github.jlangch.venice.util.ipc.impl.dest.function.IpcFunction;
import com.github.jlangch.venice.util.ipc.impl.dest.queue.BoundedQueue;
import com.github.jlangch.venice.util.ipc.impl.dest.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.dest.topic.IpcTopic;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;
import com.github.jlangch.venice.util.ipc.impl.util.ServerLogger;
import com.github.jlangch.venice.util.ipc.impl.wal.WalQueueManager;


public class ServerConnection implements IPublisher, Runnable {

    public ServerConnection(
            final Server server,
            final ServerConfig config,
            final ServerContext context,
            final ServerQueueManager queueManager,
            final ServerTopicManager topicManager,
            final ServerFunctionManager functionManager,
            final SocketChannel ch,
            final long connectionId
    ) {
        this.server = server;
        this.ch = ch;
        this.connectionId = connectionId;
        this.context = context;
        this.queueManager = queueManager;
        this.topicManager = topicManager;
        this.functionManager = functionManager;

        this.maxMessageSize = config.getMaxMessageSize();
        this.maxQueues = config.getMaxQueues();
        this.maxTempQueuesPerConnection = config.getMaxTempQueuesPerConnection();
        this.heartbeatIntervalSeconds = config.getHeartbeatIntervalSeconds();
        this.enforceEncryption = config.isEncrypting();

        this.publishQueue = new BoundedQueue<Message>("publish", context.publishQueueCapacity, false);
        this.dhKeys = DiffieHellmanKeys.create();

        this.authenticator = context.authenticator;
        this.logger = context.logger;
        this.compressor = context.compressor;
        this.subscriptions = context.subscriptions;
        this.publishQueueCapacity = context.publishQueueCapacity;
        this.statistics = context.statistics;
        this.serverThreadPoolStatistics = context.serverThreadPoolStatistics;

        this.publisherThread = new Thread(
                                    () -> worker(),
                                    "venice-ipc-server-worker-" + connectionId);

        setupHandlers();
    }


    public void close() {
        closeChannel();
    }


    @Override
    public void run() {
        try {
            logInfo("Listening on connection from " + IO.getRemoteAddress(ch));

            statistics.incrementConnectionCount();

            // start publisher thread
            publisherThread.setDaemon(true);
            publisherThread.start();

            logInfo(heartbeatIntervalSeconds <= 0L
                      ? "Heartbeat is not active"
                      : "Heartbeat (" + heartbeatIntervalSeconds + "s) is active");

            // enter message request processing loop
            while(!isStop()) {
                processRequestResponse();
            }
        }
        catch(Exception ex) {
            // fail fast -> close channel

            // when the client closed the connection
            //   - server gets a java.io.IOException: Broken pipe
            //   - quit this connection and close the channel
            logError("Error on connection -> closing connection!", ex);
        }
        finally {
            closeChannel();
        }
    }

    @Override
    public void publish(final Message msg) {
        // Note: publish can be called from another ServerConnection thread!!
        try {
            // mark the message as a subscription reply
            final Message pubMsg = msg.withSubscriptionReply(true);

            // Enqueue the message to publish it as soon as possible
            // to this channels's client.
            // The publish queue is blocking to not get overrun. To prevent
            // a backlash if the queue is full, the message will be discarded!
            final long timeout = pubMsg.getTimeout();
            final boolean ok = timeout < 0L
                                ? publishQueue.offer(pubMsg)
                                : publishQueue.offer(pubMsg, timeout, TimeUnit.SECONDS);
            if (!ok) {
                throw new RuntimeException("Publish failure!");
            }
        }
        catch(Exception ex) {
            try { addMessageToDeadLetterQueue(msg); } catch(Exception ignore) {}
            statistics.incrementDiscardedPublishCount();
        }
    }

    private void setupHandlers() {
        handlers.put(REQUEST,                    this::handleSend);
        handlers.put(SUBSCRIBE,                  this::handleSubscribeToTopic);
        handlers.put(UNSUBSCRIBE,                this::handleUnsubscribeFromTopic);
        handlers.put(PUBLISH,                    this::handlePublishToTopic);
        handlers.put(OFFER,                      this::handleOfferToQueue);
        handlers.put(POLL,                       this::handlePollFromQueue);
        handlers.put(CREATE_QUEUE,               this::handleCreateQueueRequest);
        handlers.put(CREATE_TEMP_QUEUE,          this::handleCreateTemporaryQueueRequest);
        handlers.put(REMOVE_QUEUE,               this::handleRemoveQueueRequest);
        handlers.put(CREATE_TOPIC,               this::handleCreateTopicRequest);
        handlers.put(REMOVE_TOPIC,               this::handleRemoveTopicRequest);
        handlers.put(STATUS_QUEUE,               this::handleStatusQueueRequest);
        handlers.put(STATUS_TOPIC,               this::handleStatusTopicRequest);
        handlers.put(CLIENT_CONFIG,              this::handleClientConfigRequest);
        handlers.put(SERVER_STATUS,              this::handleServerStatusRequest);
        handlers.put(SERVER_THREAD_POOL_STAT,    this::handleServerThreadPoolStatisticsRequest);
        handlers.put(DIFFIE_HELLMAN_KEY_REQUEST, this::handleDiffieHellmanKeyExchange);
        handlers.put(AUTHENTICATION,             this::handleAuthentication);
        handlers.put(HEARTBEAT,                  this::handleHeartbeat);
        handlers.put(TEST,                       this::handleTest);
    }


    private boolean isStop() {
        if (stop.get()) {
            return true;
        }
        else {
            // update stop status
            stop.set(!server.isRunning()
                     || !ch.isOpen()
                     || Thread.interrupted());

           return stop.get();
        }
    }

    // ------------------------------------------------------------------------
    // Sending replies back
    // ------------------------------------------------------------------------

    private void sendDiffieHellmanResponse(final Message response) throws InterruptedException {
        if (sendSemaphore.tryAcquire(3, TimeUnit.SECONDS)) {
            try {
                // Note: no compression, no encryption!
                protocol.sendMessage(ch, response, Compressor.off(), Encryptor.off(), -1);
            }
            finally {
                sendSemaphore.release();
            }
        }
    }

    private void sendResponse(final Message response) throws InterruptedException {
        if (sendSemaphore.tryAcquire(3, TimeUnit.SECONDS)) {
            try {
                protocol.sendMessage(ch, response, compressor, encryptor.get(), maxMessageSize);
            }
            finally {
                sendSemaphore.release();
            }
        }
    }

    private void worker() {
        logInfo("Asychronous worker started");

        while(!isStop()) {
            try {
                // [1] Message publishing
                final Message msg = publishQueue.poll(1, TimeUnit.SECONDS);

                if (isStop()) break;

                if (msg != null) {
                    try {
                       sendResponse(msg.withType(REQUEST, true));
                       statistics.incrementPublishCount();
                    }
                    catch(InterruptedException ex ) {
                        addMessageToDeadLetterQueue(msg);
                        statistics.incrementDiscardedPublishCount();
                        throw ex;
                    }
                    catch(Exception ex ) {
                        addMessageToDeadLetterQueue(msg);
                        statistics.incrementDiscardedPublishCount();
                    }
                }

                // [2] Heartbeat
                //
                // check heartbeat timeout and close the channel if heartbeats did
                // not arrive within the timeout period
                checkHeartbeatTimeout();
            }
            catch(InterruptedException ex) {
               break;
            }
        }

        logInfo("Asychronous worker stopped");
    }


    // ------------------------------------------------------------------------
    // Process requests
    // ------------------------------------------------------------------------

    private void processRequestResponse() throws InterruptedException {
        // [1] receive message
        final Message request = protocol.receiveMessage(ch, compressor, encryptor.get());
        if (request == null) {
            stop.set(true); // client closed connection
            return;
        }

        if (!server.isRunning()) {
            stop.set(true);  // this server was closed
            return;
        }

        final MessageType type = request.getType();

        if (type != DIFFIE_HELLMAN_KEY_REQUEST && type != CLIENT_CONFIG) {
            statistics.incrementMessageCount();
        }

        // [2] Handle max message payload size
        if (request.getData().length > maxMessageSize) {
            if (request.isOneway()) {
                // oneway request -> cannot send and error message back
            }
            else {
               final Message response = createBadRequestResponse(
                                            request,
                                            String.format(
                                                "The message (%d bytes) is too large! The limit is at %d bytes.",
                                                request.getData().length,
                                                maxMessageSize));
               sendResponse(response);
            }

            return;
        }

        // [3] Handle request
        final Message response = handleRequestMessage(request);

        if (!server.isRunning()) {
            stop.set(true);  // this server was closed
            return;
        }

        final ResponseStatus respStatus = response.getResponseStatus();

        // [4] Send response
        if (!request.isOneway()) {
            if (respStatus == DIFFIE_HELLMAN_ACK || respStatus == DIFFIE_HELLMAN_NAK) {
                // Diffie Hellman responses without compressing and encrypting!
                sendDiffieHellmanResponse(response);
            }
            else {
                sendResponse(response);
            }
        }
    }

    private Message handleRequestMessage(final Message request) {
        final MessageType type = request.getType();

        // Authentication
        if (authenticator.isActive() && !authenticated) {
            if (!(type == CLIENT_CONFIG
                  || type == DIFFIE_HELLMAN_KEY_REQUEST
                  || type == AUTHENTICATION)
            ) {
                return createNoPermissionResponse(
                        request,
                        "Authentication is required!");
            }
        }

        try {
            final Function<Message,Message> handler = handlers.get(type);
            return handler != null
                    ? handler.apply(request)
                    : createBadRequestResponse(
                            request,
                            "Invalid request type: " + type);
        }
        catch(Exception ex) {
            final String errMsg = "Failed to handle '" + type + "' request!";

            // TODO: how much information from the exception shall we pass back
            //       to the client
            return createTextResponse(
                        request,
                        HANDLER_ERROR,
                        errMsg+ "\n" + ExceptionUtil.printStackTraceToString(ex));
        }
    }



    // ------------------------------------------------------------------------
    // Request handler
    // ------------------------------------------------------------------------

    private Message handleSend(final Message request) {
        final String functionName = request.getDestinationName();

        final IpcFunction fn = functionManager.getFunction(functionName);
        if (fn == null) {
            return createFunctionNotFoundResponse(request);
        }

        if (authenticated && !adminAuthorization && !fn.canExecute(principal)) {
            return createNoPermissionResponse(
                    request,
                    "Not authenticated for function calling!");
        }

        // note: exceptions are handled upstream
        final IMessage response = fn.apply(request);

        if (response == null) {
            // create an empty text response
            return createOkTextResponse(request, "");
        }
        else {
            return ((Message)response).withTypeAndResponseStatus(
                        RESPONSE, true, request.getId(), OK);
        }
    }

    private Message handleSubscribeToTopic(final Message request) {
        final String topicName = request.getDestinationName();

        final IpcTopic topic = topicManager.getTopic(topicName);
        if (topic == null) {
            return createTopicNotFoundResponse(request);
        }

        if (authenticated && !adminAuthorization && !topic.canRead(principal)) {
            return createNoPermissionResponse(
                    request,
                    "Not authenticated for topic subscription!");
        }

        // register subscription
        subscriptions.addSubscription(topicName, this);

        logInfo(String.format("Subscribed to topic: %s.", request.getDestinationName()));

        // acknowledge the subscription
        return createOkTextResponse(request, "Subscribed to the topic.");
    }

    private Message handleUnsubscribeFromTopic(final Message request) {
        final String topicName = request.getDestinationName();

        final IpcTopic topic = topicManager.getTopic(topicName);
        if (topic == null) {
            return createTopicNotFoundResponse(request);
        }

        // unregister subscription
        subscriptions.removeSubscription(topicName, this);

        logInfo(String.format("Unsubscribed from topic: %s.", request.getDestinationName()));

        // acknowledge the unsubscription
        return createOkTextResponse(request, "Unsubscribed from the topic.");
    }

    private Message handlePublishToTopic(final Message request) {
        final String topicName = request.getDestinationName();

        final IpcTopic topic = topicManager.getTopic(topicName);
        if (topic == null) {
            return createTopicNotFoundResponse(request);
        }

        if (authenticated && !adminAuthorization && !topic.canWrite(principal)) {
            return createNoPermissionResponse(
                    request,
                    "Not authenticated for topic publication!");
        }

        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        return createOkTextResponse(request, "Message has been enqued to publish.");
    }

    private Message handleOfferToQueue(final Message request) {
        final String queueName = request.getDestinationName();

        try {
            final IpcQueue<Message> queue = queueManager.getQueue(queueName);
            if (queue == null) {
                return createQueueNotFoundResponse(request);
            }

            if (authenticated
                && !adminAuthorization
                && !queue.isTemporary()
                && !queue.canWrite(principal)
            ) {
                return createNoPermissionResponse(
                        request,
                        "Not authenticated for queue offer!");
            }

            // convert message type from OFFER to REQUEST
            final Message msg = request.withType(REQUEST, request.isOneway());
            final long timeout = msg.getTimeout();

            final boolean ok = timeout < 0
                                ? queue.offer(msg)
                                : queue.offer(msg, timeout, TimeUnit.MILLISECONDS);
            if (ok) {
                return createOkTextResponse(
                        request,
                        "Offered the message to the queue " + queueName);
            }
            else {
                if (request.isOneway()) {
                    addMessageToDeadLetterQueue(request);
                }

                return createTextResponse(
                        request,
                        QUEUE_FULL,
                        "Offer to " + queueName + " rejected! The queue is full.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    QUEUE_ACCESS_INTERRUPTED,
                    "Offer to " + queueName + " rejected! Queue access interrupted.");
        }
    }

    private Message handlePollFromQueue(final Message request) {
        if (request.isOneway()) {
            logError("Queue poll requests must nor be oneway!");
            return null;
        }

        final String queueName = request.getDestinationName();
        try {
            final long timeout = request.getTimeout();
            final IpcQueue<Message> queue = queueManager.getQueue(queueName);
            if (queue == null) {
                return createQueueNotFoundResponse(request);
            }

            if (authenticated
                && !adminAuthorization
                && !queue.isTemporary()
                && !queue.canRead(principal)
            ) {
                return createNoPermissionResponse(
                        request,
                        "Not authenticated for queue poll!");
            }

            while(true) {
                final Message msg = timeout < 0
                                        ? queue.poll()
                                        : queue.poll(timeout, TimeUnit.MILLISECONDS);
                if (msg == null) {
                    return createTextResponse(
                            request,
                            QUEUE_EMPTY,
                            "Poll from queue " + queueName + " rejected! The queue is empty.");
                }
                else if (msg.hasExpired()) {
                    // discard expired message -> try next message from the queue
                    continue;
                }
                else {
                    return msg.withTypeAndResponseStatus(RESPONSE, true, request.getId(), OK);
                }
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    QUEUE_ACCESS_INTERRUPTED,
                    "Poll from queue " + queueName + " rejected! Queue access interrupted.");
        }
    }

    private Message handleCreateQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to create queues! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();
        final int capacity = Coerce.toVncLong(payload.get(new VncString("capacity"))).toJavaInteger();
        final String sType = Coerce.toVncString(payload.get(new VncString("type"))).getValue();
        final String sPersistence = Coerce.toVncString(payload.get(new VncString("persistence"))).getValue();

        final QueueType type = QueueType.valueOf(sType);
        final QueuePersistence persistence = QueuePersistence.valueOf(sPersistence);
        queueManager.createQueue(queueName, capacity, type, persistence);

        return createOkTextResponse(
                request,
                String.format("Request %s: Queue %s created.", request.getType(), queueName));
    }

    private Message handleCreateTemporaryQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (tmpQueues.size() >= maxTempQueuesPerConnection) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Too many temporary queues! "
                        + "Reached the limit of %d temporary queues for this client.",
                        request.getType(),
                        maxTempQueuesPerConnection));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final int capacity = Coerce.toVncLong(payload.get(new VncString("capacity"))).toJavaInteger();

        final String queueName = "queue/" + UUID.randomUUID().toString();

        try {
            QueueValidator.validateQueueName(queueName);
            QueueValidator.validateQueueCapacity(capacity);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Invalid queue data: %s",
                        request.getType(), ex.getMessage()));
        }

        // do not overwrite the queue if it already exists
        queueManager.withQueues(queues -> {
            queues.computeIfAbsent(
               queueName,
               k -> {
                   final IpcQueue<Message> q = new BoundedQueue<Message>(queueName, capacity, true);
                   tmpQueues.put(queueName, 0);

                   logInfo(String.format(
                               "Created temporary queue %s. Capacity=%d",
                               queueName,
                               capacity));

                   return q;
               });
        });

        return createOkTextResponse(request, queueName);
    }

    private Message handleRemoveQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to remove queues! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        // Note: Temporary queues cannot be removed. They are implicitly
        //       removed if the connection they belong to is closed!
        queueManager.removeQueue(queueName);

        logInfo(String.format("Removed queue %s.", queueName));

        return createOkTextResponse(
                request,
                String.format(
                    "Request %s: Queue %s removed.",
                    request.getType(), queueName));
    }

    private Message handleStatusQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to request queue status! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            QueueValidator.validateQueueName(queueName);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Invalid queue name: %s",
                        request.getType(), ex.getMessage()));
        }

        final Map<String,Object> s = queueManager.getQueueStatus(queueName);

        final String response = new JsonBuilder()
                                        .add("name",      queueName)
                                        .add("exists",    (Boolean)s.get("exists"))
                                        .add("type",      (String)s.get("type"))
                                        .add("temporary", (Boolean)s.get("temporary"))
                                        .add("durable",   (Boolean)s.get("durable"))
                                        .add("capacity",  (Long)s.get("capacity"))
                                        .add("size",      (Long)s.get("size"))
                                        .toJson(false);

        return createJsonResponse(request, OK, response);
    }

    private Message handleStatusTopicRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to request topic status! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String topicName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            TopicValidator.validateTopicName(topicName);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Invalid topic name: %s",
                        request.getType(), ex.getMessage()));
        }

        final Map<String,Object> s = topicManager.getTopicStatus(topicName);

        final String response = new JsonBuilder()
                                        .add("name",    topicName)
                                        .add("exists",  (Boolean)s.get("exists"))
                                        .toJson(false);

        return createJsonResponse(request, OK, response);
    }

    private Message handleCreateTopicRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to create topics! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String topicName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        topicManager.createTopic(topicName);

        return createOkTextResponse(
                request,
                String.format("Request %s: Topic %s created.", request.getType(), topicName));
    }

    private Message handleRemoveTopicRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "Client is not permitted to remove topics! Authenticate as 'admin' user please!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String topicName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        topicManager.removeTopic(topicName);

        logInfo(String.format("Removed topic %s.", topicName));

        return createOkTextResponse(
                request,
                String.format(
                    "Request %s: Topic %s removed.",
                    request.getType(), topicName));
    }

    private Message handleClientConfigRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String sAckMode = Coerce.toVncString(payload.get(new VncString("ackMode"))).getValue();
        msgAcknowledgeMode = AcknowledgeMode.valueOf(sAckMode);

        logInfo("Handling client config request! AcknowledgeMode: " + msgAcknowledgeMode);
        return createJsonResponse(
                    request,
                    OK,
                    new JsonBuilder()
                            .add("max-msg-size", maxMessageSize)
                            .add("compress-cutoff-size", compressor.cutoffSize())
                            .add("encrypt", enforceEncryption)
                            .add("heartbeat-interval-seconds", heartbeatIntervalSeconds)
                            .add("authentication", authenticator.isActive())
                            .toJson(false));
    }

    private Message handleDiffieHellmanKeyExchange(final Message request) {
        if (clientPublicKey != null) {
            logWarn("Diffie-Hellman key already exchanged!");
            return createPlainTextResponse(
                       request.getId(),
                       DIFFIE_HELLMAN_NAK,
                       null,  // no request id
                       null,  // no destination name
                       "",
                       "Error: Diffie-Hellman key already exchanged!");
        }
        else {
            try {
                logInfo("Diffie-Hellman key exchange initiated!");

                final String publicKey = request.getText();
                clientPublicKey = publicKey;

                logInfo("Diffie-Hellman key exchange completed!");

                encryptor.set(Encryptor.aes(dhKeys.generateSharedSecret(publicKey)));

                if (enforceEncryption) {
                    logInfo("Setup message encryptor! Encryption is mandatory.");
                }
                else {
                    logInfo("Setup message encryptor! Encryption is optional.");
                }

                // send the server's public key back
                return createPlainTextResponse(
                           request.getId(),
                           DIFFIE_HELLMAN_ACK,
                           null,  // no request id
                           null,  // no destination name
                           "",
                           dhKeys.getPublicKeyBase64());
            }
            catch(Exception ex) {
                logError("Diffie-Hellman key exchange error!", ex);

                return createPlainTextResponse(
                           request.getId(),
                           DIFFIE_HELLMAN_NAK,
                           null,  // no request id
                           null,  // no destination name
                           "",
                           "Failed to exchange Diffie-Hellman key! Reason: " + ex.getMessage());
            }
        }
    }

    private Message handleAuthentication(final Message request) {
        if (!"text/plain".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format("Request %s: Expected a text payload", request.getType()));
        }

        final List<String> payload = StringUtil.splitIntoLines(request.getText());
        if (payload.size() == 2) {
            authenticated = authenticator.isAuthenticated(payload.get(0), payload.get(1));
            if (authenticated) {
                principal = payload.get(0);
                adminAuthorization = authenticator.isAdmin(payload.get(0));
                logInfo("Authenticated user '" + payload.get(0) + "'");
                return createOkTextResponse(request, "");
            }
        }

        logError("Authentication failure '" + payload.get(0) + "'");
        return createNoPermissionResponse(request, "");
    }

    private Message handleHeartbeat(final Message request) {
        lastHeartbeat = System.currentTimeMillis();
        logInfo("Heartbeat");
        return createOkTextResponse(request, "");
    }

    private Message handleTest(final Message request) {
        return request.isOneway()
                ? null
                : new Message(
                        request.getId(),
                        request.getRequestId(),
                        RESPONSE,
                        OK,
                        ONEWAY_MSG,
                        TRANSIENT_MSG,
                        false,  // not a subscription msg
                        Messages.EXPIRES_NEVER,
                        request.getSubject(),
                        request.getMimetype(), // always "application/octet-tream"
                        null,
                        new byte[0]);
    }

    private Message handleServerStatusRequest(final Message request) {
        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "The clients is not permitted to get the server status!");
        }

        final WalQueueManager wal = queueManager.getWalQueueManager();

        int sndBufSize = -1;
        int rcvBufSize = -1;

        try { sndBufSize = ch.socket().getSendBufferSize(); } catch (Exception ignore) { }
        try { rcvBufSize = ch.socket().getReceiveBufferSize(); } catch (Exception ignore) { }

        return createJsonResponse(
                   request,
                   OK,
                   new JsonBuilder()
                           .add("running", server.isRunning())
                            // config
                           .add("encryption", encryptor.get().isActive())
                           .add("max-queues", maxQueues)
                           .add("message-size-max", maxMessageSize)
                           .add("admin", adminAuthorization)
                           .add("compression-cutoff-size", compressor.cutoffSize())
                           .add("write-ahead-log-dir", wal.isEnabled()
                                                            ? wal.getWalDir().getAbsolutePath()
                                                            : "-" )
                           .add("write-ahead-log-count", wal.isEnabled()
                                                            ? wal.countLogFiles()
                                                            : 0 )
                           .add("hearbeat-interval-seconds", heartbeatIntervalSeconds)
                           .add("logger-enabled", logger.isEnabled())
                           .add("logger-file", logger.getLogFile() != null
                                                ? logger.getLogFile().getAbsolutePath()
                                                : "-")
                           .add("error-queue-capacity", ERROR_QUEUE_CAPACITY)
                           .add("publish-queue-capacity", publishQueueCapacity)
                            // statistics
                           .add("connection_count", statistics.getConnectionCount())
                           .add("message-count", statistics.getMessageCount())
                           .add("publish-count", statistics.getPublishCount())
                           .add("response-discarded-count", statistics.getDiscardedResponseCount())
                           .add("publish-discarded-count", statistics.getDiscardedPublishCount())
                           .add("subscription-client-count", subscriptions.getClientSubscriptionCount())
                           .add("subscription-topic-count", subscriptions.getTopicSubscriptionCount())
                           .add("queue-count", queueManager.countStandardQueues())
                           .add("temp-queue-total-count", queueManager.countTemporaryQueues())
                           .add("temp-queue-connection-count", (long)tmpQueues.size())
                           .add("socket-snd-buf-size", sndBufSize)
                           .add("socket-rcv-buf-size", rcvBufSize)
                           .toJson(false));
    }

    private Message handleServerThreadPoolStatisticsRequest(final Message request) {
        if (!adminAuthorization) {
            return createNoPermissionResponse(
                    request,
                    "The client is not permitted to get the server thread pool statistics!");
        }

        final VncMap statistics = serverThreadPoolStatistics.get();

        return createJsonResponse(request, OK, Json.writeJson(statistics, true));
    }


    // ------------------------------------------------------------------------
    // Create response messages
    // ------------------------------------------------------------------------

    private Message createQueueNotFoundResponse(final Message request) {
        return createTextResponse(
                request,
                QUEUE_NOT_FOUND,
                "The queue " + request.getDestinationName() + " does not exist!");
    }

    private Message createTopicNotFoundResponse(final Message request) {
        return createTextResponse(
                request,
                TOPIC_NOT_FOUND,
                "The topic " + request.getDestinationName() + " does not exist!");
    }

    private Message createFunctionNotFoundResponse(final Message request) {
        return createTextResponse(
                request,
                FUNCTION_NOT_FOUND,
                "The function " + request.getDestinationName() + " does not exist!");
    }

    private Message createBadRequestResponse(
            final Message request,
            final String errorMsg
    ) {
        return createTextResponse(request, BAD_REQUEST, errorMsg);
    }

    private Message createNoPermissionResponse(
            final Message request,
            final String errorMsg
    ) {
        return createTextResponse(request, NO_PERMISSION, errorMsg);
    }

    private Message createOkTextResponse(
            final Message request,
            final String message
    ) {
       return createTextResponse(request, OK, message);
    }

    private Message createTextResponse(
            final Message request,
            final ResponseStatus responseStatus,
            final String message
    ) {
        return createPlainTextResponse(
                    request.getId(),
                    responseStatus,
                    request.getRequestId(),
                    request.getDestinationName(),
                    request.getSubject(),
                    message);
    }

    private Message createNonJsonRequestResponse(final Message request) {
        return createBadRequestResponse(
                request,
                String.format("Request %s: Expected a JSON payload", request.getType()));
    }

    private static Message createJsonResponse(
            final Message request,
            final ResponseStatus status,
            final String json
    ) {
        return new Message(
                request.getId(),
                request.getRequestId(),
                RESPONSE,
                status,
                ONEWAY_MSG,
                TRANSIENT_MSG,
                false,  // not a subscription msg
                request.getDestinationName(),
                null,
                Instant.now().toEpochMilli(),
                Messages.EXPIRES_NEVER,
                Messages.NO_TIMEOUT,
                request.getSubject(),
                "application/json",
                "UTF-8",
                toBytes(json, "UTF-8"));
    }

    private static Message createPlainTextResponse(
            final UUID id,
            final ResponseStatus status,
            final String requestID,
            final String destinationName,
            final String subject,
            final String text
    ) {
        return new Message(
                id,
                requestID,
                RESPONSE,
                status,
                ONEWAY_MSG,
                TRANSIENT_MSG,
                false,  // not a subscription msg
                destinationName,
                null,
                Instant.now().toEpochMilli(),
                Messages.EXPIRES_NEVER,
                Messages.NO_TIMEOUT,
                subject,
                "text/plain",
                "UTF-8",
                text == null || text.isEmpty()
                    ? new byte[0]
                    : toBytes(text, "UTF-8"));
    }


    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private void addMessageToDeadLetterQueue(final Message m)
    throws InterruptedException {
        try {
            if (m != null) {
                queueManager.getDeadLetterQueue().offer(m);
            }
        }
        catch (InterruptedException ex) { throw ex; }
        catch (Exception ignore) { }
	}

    private void checkHeartbeatTimeout() {
        if (heartbeatIntervalSeconds > 0L) {
            // timeout: after 2 missed heartbeats
            //
            // --+-----+-----o-----o-----o-----
            //          \_____________^
            //
            final long timeout = (long)(2.5F * (heartbeatIntervalSeconds * 1000L));
            if (System.currentTimeMillis() - lastHeartbeat > timeout) {
                // Heartbeat timeout
                logError("Heartbeat timeout -> closing connection");
                closeChannel();
            }
        }
    }

    private void removeAllChannelTemporaryQueues() {
        try {
            queueManager.withQueues(queues -> {
                tmpQueues.keySet().forEach(n -> queues.remove(n));
                tmpQueues.clear();
            });

            logInfo("Removed all temporary queues of the connnection");
        }
        catch(Exception ignore) { }
    }

    private void closeChannel() {
        stop.set(true);
        publisherThread.interrupt();

        try { removeAllChannelTemporaryQueues(); } catch(Exception ignore) {}

        statistics.decrementConnectionCount();

        try { subscriptions.removeSubscriptions(this); } catch(Exception ignore) {}

        IO.safeClose(ch);

        logInfo("Closed connection");
    }

    private void logInfo(final String message) {
        logger.info(principal, "conn-" + connectionId, message);
    }

    private void logWarn(final String message) {
        logger.warn(principal, "conn-" + connectionId, message);
    }

    private void logError(final String message) {
        logger.error(principal, "conn-" + connectionId, message);
    }

    private void logError(final String message, final Exception ex) {
        logger.error(principal, "conn-" + connectionId, message, ex);
    }



    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    public static final int ERROR_QUEUE_CAPACITY = 50;

    private static final boolean ONEWAY_MSG = true;
    private static final boolean TRANSIENT_MSG = false;


    private volatile long lastHeartbeat = System.currentTimeMillis();  // Millis since epoch

    private AcknowledgeMode msgAcknowledgeMode = AcknowledgeMode.NO_ACKNOWLEDGE;
    private String clientPublicKey = null;

    // authentication
    private String principal = "anon";
    private boolean authenticated = false;
    private boolean adminAuthorization = false;

    private final Server server;
    private final SocketChannel ch;
    private final long connectionId;
    private final ServerContext context;
    private final ServerLogger logger;

    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final ServerStatistics statistics;
    private final Supplier<VncMap> serverThreadPoolStatistics;
    private final Authenticator authenticator;

    // lifecycle
    private final AtomicBoolean stop = new AtomicBoolean(false);

    private final Semaphore sendSemaphore = new Semaphore(1);

    private final Protocol protocol = new Protocol();

    // topic publisher
    private final Thread publisherThread;

    // configuration
    private final long maxMessageSize;
    private final long maxQueues;
    private final long maxTempQueuesPerConnection;
    private final long heartbeatIntervalSeconds;

    private final ServerQueueManager queueManager;
    private final ServerTopicManager topicManager;
    private final ServerFunctionManager functionManager;

    // compression
    private final Compressor compressor;

    // encryption
    private final boolean enforceEncryption;
    private final DiffieHellmanKeys dhKeys;
    private final AtomicReference<Encryptor> encryptor = new AtomicReference<>(Encryptor.off());

    // queues
    private final IpcQueue<Message> publishQueue;
    private final Map<String, Integer> tmpQueues = new ConcurrentHashMap<>();

    private final Map<MessageType, Function<Message,Message>> handlers = new HashMap<>(100, 0.5F);
}
