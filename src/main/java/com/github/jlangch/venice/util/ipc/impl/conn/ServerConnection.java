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
import static com.github.jlangch.venice.util.ipc.MessageType.DIFFIE_HELLMAN_KEY_REQUEST;
import static com.github.jlangch.venice.util.ipc.MessageType.HEARTBEAT;
import static com.github.jlangch.venice.util.ipc.MessageType.OFFER;
import static com.github.jlangch.venice.util.ipc.MessageType.POLL;
import static com.github.jlangch.venice.util.ipc.MessageType.PUBLISH;
import static com.github.jlangch.venice.util.ipc.MessageType.REMOVE_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.REQUEST;
import static com.github.jlangch.venice.util.ipc.MessageType.RESPONSE;
import static com.github.jlangch.venice.util.ipc.MessageType.STATUS_QUEUE;
import static com.github.jlangch.venice.util.ipc.MessageType.SUBSCRIBE;
import static com.github.jlangch.venice.util.ipc.MessageType.TEST;
import static com.github.jlangch.venice.util.ipc.MessageType.UNSUBSCRIBE;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.BAD_REQUEST;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.DIFFIE_HELLMAN_ACK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.DIFFIE_HELLMAN_NAK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.HANDLER_ERROR;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.NO_PERMISSION;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.OK;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_ACCESS_INTERRUPTED;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_EMPTY;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_FULL;
import static com.github.jlangch.venice.util.ipc.ResponseStatus.QUEUE_NOT_FOUND;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.QueueValidator;
import com.github.jlangch.venice.util.ipc.impl.ServerQueueManager;
import com.github.jlangch.venice.util.ipc.impl.ServerStatistics;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.queue.BoundedQueue;
import com.github.jlangch.venice.util.ipc.impl.queue.CircularBuffer;
import com.github.jlangch.venice.util.ipc.impl.queue.IpcQueue;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.Error;
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
            final SocketChannel ch,
            final long connectionId
    ) {
        this.server = server;
        this.ch = ch;
        this.connectionId = connectionId;
        this.context = context;
        this.queueManager = queueManager;

        this.maxMessageSize = config.getMaxMessageSize();
        this.maxQueues = config.getMaxQueues();
        this.maxTempQueuesPerConnection = config.getMaxTempQueuesPerConnection();
        this.permitClientQueueMgmt = config.isPermitClientQueueMgmt();
        this.heartbeatInterval = config.getHeartbeatIntervalSeconds();
        this.enforceEncryption = config.isEncrypting();

        this.publishQueue = new BoundedQueue<Message>("publish", context.publishQueueCapacity, false);
        this.errorBuffer = new CircularBuffer<>("error", ERROR_QUEUE_CAPACITY, false);
        this.dhKeys = DiffieHellmanKeys.create();


        this.authenticator = context.authenticator;
        this.logger = context.logger;
        this.handler = context.handler;
        this.compressor = context.compressor;
        this.subscriptions = context.subscriptions;
        this.publishQueueCapacity = context.publishQueueCapacity;
        this.statistics = context.statistics;
        this.serverThreadPoolStatistics = context.serverThreadPoolStatistics;

        setupHandlers();
    }


    public void close() {
        stop.set(true);
        publisherThread.interrupt();

        IO.safeClose(ch);  // will trigger closeChannel()
    }

    public long millisSinceLastHeartbeat() {
        return lastHeartbeat == 0L ? 0L : System.currentTimeMillis() - lastHeartbeat;
    }


    @Override
    public void run() {
        try {
            logInfo("Listening on connection from " + IO.getRemoteAddress(ch));

            statistics.incrementConnectionCount();

            // start publisher thread
            publisherThread = new Thread(() -> publisher(), "venice-ipc-server-publisher");
            publisherThread.setDaemon(true);
            publisherThread.start();

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
            logError("Error on connection from "
                        +  IO.getRemoteAddress(ch) + "!",
                    ex);
        }
        finally {
            closeChannel();
        }
    }

    @Override
    public void publish(final Message msg) {
        try {
            // Enqueue the message to publish it as soon as possible
            // to this channels's client.
            // The publish queue is blocking to not get overrun. to prevent
            // a backlash if the queue is full, the message will be discarded!
            final long timeout = msg.getTimeout();
            final boolean ok = timeout < 0L
                                ? publishQueue.offer(msg)
                                : publishQueue.offer(msg, timeout, TimeUnit.SECONDS);
            if (!ok) {
                auditPublishError(msg, "Failed to enque message for publishing. Publish queue is full!");
            }
        }
        catch(Exception ex) {
            // there is no dead letter queue yet, just count the
            // discarded messages
            auditPublishError(msg, "Failed to enque message for publishing!", ex);
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
        handlers.put(STATUS_QUEUE,               this::handleStatusQueueRequest);
        handlers.put(CLIENT_CONFIG,              this::handleClientConfigRequest);
        handlers.put(DIFFIE_HELLMAN_KEY_REQUEST, this::handleDiffieHellmanKeyExchange);
        handlers.put(AUTHENTICATION,             this::handleAuthentication);
        handlers.put(HEARTBEAT,                  this::handleHeartbeat);
        handlers.put(TEST,                       this::handleTest);
    }

    private boolean isStop() {
        // update stop status
        stop.set(stop.get()
                 || mode == State.Terminated
                 || !server.isRunning()
                 || !ch.isOpen()
                 || Thread.interrupted());

        return stop.get();
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

    private void publisher() {
        logInfo("Asychronous publisher started");

        while(!isStop()) {
            try {
                final Message msg = publishQueue.poll(1, TimeUnit.SECONDS);

                if (isStop()) break;

                if (msg != null) {
                    statistics.incrementPublishCount();
                    final Message pubMsg = msg.withType(REQUEST, true);

                   sendResponse(pubMsg);
               }
            }
            catch(InterruptedException ex) {
               break;
            }
        }

        logInfo("Asychronous publisher stopped");
    }


    // ------------------------------------------------------------------------
    // Process requests
    // ------------------------------------------------------------------------

    private void processRequestResponse() throws InterruptedException {
        // [1] receive message
        final Message request = protocol.receiveMessage(ch, compressor, encryptor.get());
        if (request == null) {
            mode = State.Terminated; // client closed connection
            return;
        }

        if (!server.isRunning()) {
            mode = State.Terminated;  // this server was closed
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
                auditResponseError(
                    request,
                    "Request too large! Cannot send error response back for oneway request!");
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
            mode = State.Terminated;  // this server was closed
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
                return createTextResponse(
                        request,
                        NO_PERMISSION,
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
            // send an error response
            auditResponseError(request, errMsg, ex);

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
        if (request.getTopic().startsWith(Messages.TOPIC_SERVER_PREFIX)) {
            if (Messages.TOPIC_SERVER_STATUS.equals(request.getTopic())) {
                return getTcpServerStatus(request);
            }
            else if (Messages.TOPIC_SERVER_THREAD_POOL_STATS.equals(request.getTopic())) {
                return getTcpServerThreadPoolStatistics(request);
            }
            else if (Messages.TOPIC_SERVER_ERROR.equals(request.getTopic())) {
                return getTcpServerNextError(request);
            }
            else {
                return createBadRequestResponse(
                        request,
                        "Unknown server request topic. \n"
                          + "Valid topics are:\n"
                          + "  • " + Messages.TOPIC_SERVER_STATUS + "\n"
                          + "  • " + Messages.TOPIC_SERVER_THREAD_POOL_STATS + "\n"
                          + "  • " + Messages.TOPIC_SERVER_ERROR);
            }
        }
        else {
            // note: exceptions are handled upstream
            final IMessage response = handler.apply(request);

            if (response == null) {
                // create an empty text response
                return createTextResponse(request, OK, "");
            }
            else {
                return ((Message)response)
                            .withType(RESPONSE, true)
                            .withResponseStatus(request.getId(), OK);
            }
        }
    }

    private Message handleSubscribeToTopic(final Message request) {
        // register subscription
        subscriptions.addSubscriptions(request.getTopicsSet(), this);

        logInfo(String.format("Subscribed to topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the subscription
        return createOkTextResponse(request, "Subscribed to the topics.");
    }

    private Message handleUnsubscribeFromTopic(final Message request) {
        // unregister subscription
        subscriptions.removeSubscriptions(request.getTopicsSet(), this);

        logInfo(String.format("Unsubscribed from topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the unsubscription
        return createOkTextResponse(request, "Unsubscribed from the topics.");
    }

    private Message handlePublishToTopic(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        return createOkTextResponse(request, "Message has been enqued to publish.");
    }

    private Message handleOfferToQueue(final Message request) {
        try {
            final String queueName = request.getQueueName();
            final IpcQueue<Message> queue = queueManager.getQueue(queueName);
            if (queue != null) {
                // convert message type from OFFER to REQUEST
                final Message msg = request.withType(REQUEST, request.isOneway());
                final long timeout = msg.getTimeout();

                final boolean ok = timeout < 0
                                    ? queue.offer(msg)
                                    : queue.offer(msg, timeout, TimeUnit.MILLISECONDS);
                if (ok) {
                    final boolean durable = msg.isDurable()                  // message is durable
                                            && queue.isDurable()             // queue is durable
                                            && queueManager.isWalEnabled();  // server supports write-ahead-log

                    return createTextResponse(
                            request,
                            OK,
                            String.format(
                                "Offered the message to the queue %s (durable: %b).",
                                queue.name(),
                                durable));
                }
                else {
                    return createTextResponse(
                            request,
                            QUEUE_FULL,
                            "Offer rejected! The queue is full.");
                }
            }
            else {
                return createTextResponse(
                        request,
                        QUEUE_NOT_FOUND,
                        "Offer rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    QUEUE_ACCESS_INTERRUPTED,
                    "Offer rejected! Queue access interrupted.");
        }
    }

    private Message handlePollFromQueue(final Message request) {
        try {
            final long timeout = request.getTimeout();
            final String queueName = request.getQueueName();
            final IpcQueue<Message> queue = queueManager.getQueue(queueName);
            if (queue != null) {
                while(true) {
                    final Message msg = timeout < 0
                                            ? queue.poll()
                                            : queue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (msg == null) {
                        return createTextResponse(
                                request,
                                QUEUE_EMPTY,
                                "Poll rejected! The queue is empty.");
                    }
                    else if (msg.hasExpired()) {
                        // discard expired message -> try next message from the queue
                        auditResponseError(
                            request,
                            String.format(
                                "Discarded expired message (request-id: %s)" +
                                "polling from queue '%s'!",
                                msg.getRequestId(),
                                queueName));
                       continue;
                    }
                    else {
                        return msg.withType(RESPONSE, true)
                                  .withResponseStatus(request.getId(), OK);
                    }
                }
            }
            else {
                return createTextResponse(
                        request,
                        QUEUE_NOT_FOUND,
                        "Poll rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    QUEUE_ACCESS_INTERRUPTED,
                    "Poll rejected! Queue access interrupted.");
        }
    }

    private Message handleCreateQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createNonJsonRequestResponse(request);
        }

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to create queues!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();
        final int capacity = Coerce.toVncLong(payload.get(new VncString("capacity"))).toJavaInteger();
        final boolean bounded = Coerce.toVncBoolean(payload.get(new VncString("bounded"))).getValue();
        final boolean durable = Coerce.toVncBoolean(payload.get(new VncString("durable"))).getValue();

        queueManager.createQueue(queueName, capacity, bounded, durable);

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

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to remove queues!");
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

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to request queue status!");
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

        final IpcQueue<Message> q = queueManager.getQueue(queueName);

        final String response = new JsonBuilder()
                                        .add("name",      queueName)
                                        .add("exists",    q != null)
                                        .add("type",      q == null ? null : q.type().name())
                                        .add("temporary", q != null && q.isTemporary())
                                        .add("durable",   q != null && q.isDurable())
                                        .add("capacity",  q == null ? 0L : (long)q.capacity())
                                        .add("size",      q == null ? 0L : (long)q.size())
                                        .toJson(false);

        return createJsonResponse(request, OK, response);
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
                            .add("permit-client-queue-mgmt", permitClientQueueMgmt)
                            .add("encrypt", enforceEncryption)
                            .add("heartbeat-interval", heartbeatInterval)
                            .add("authentication", authenticator.isActive())
                            .toJson(false));
    }

    private Message handleDiffieHellmanKeyExchange(final Message request) {
        if (clientPublicKey.get() != null) {
            logWarn("Diffie-Hellman key already exchanged!");
            return createPlainTextResponse(
                       request.getId(),
                       DIFFIE_HELLMAN_NAK,
                       null,
                       Topics.of(Messages.TOPIC_DIFFIE_HELLMANN),
                       "Error: Diffie-Hellman key already exchanged!");
        }
        else {
            try {
                logInfo("Diffie-Hellman key exchange initiated!");

                final String publicKey = request.getText();
                clientPublicKey.set(publicKey);

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
                           null,
                           Topics.of(Messages.TOPIC_DIFFIE_HELLMANN),
                           dhKeys.getPublicKeyBase64());
            }
            catch(Exception ex) {
                logError("Diffie-Hellman key exchange error!", ex);

                return createPlainTextResponse(
                           request.getId(),
                           DIFFIE_HELLMAN_NAK,
                           null,
                           Topics.of(Messages.TOPIC_DIFFIE_HELLMANN),
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
                logInfo("Authenticated user '" + payload.get(0) + "'");
                return createTextResponse(request, OK, "");
            }
        }

        logError("Authentication failure '" + payload.get(0) + "'");
        return createTextResponse(request, NO_PERMISSION, "");
    }

    private Message handleHeartbeat(final Message request) {
        lastHeartbeat = System.currentTimeMillis();
        logInfo("Heartbeat");
        return createTextResponse(request, OK, "");
    }

    private Message handleTest(final Message request) {
        return request.isOneway()
                ? null
                : new Message(
                        request.getId(),
                        request.getRequestId(),
                        RESPONSE,
                        OK,
                        true,   // oneway
                        false,  // transient
                        false,  // not a subscription msg
                        Messages.EXPIRES_NEVER,
                        request.getTopics(),
                        request.getMimetype(), // always "application/octet-tream"
                        null,
                        new byte[0]);
    }


    // ------------------------------------------------------------------------
    // Create response messages
    // ------------------------------------------------------------------------

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
                    request.getTopics(),
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
                true,   // oneway
                false,  // transient
                false,  // not a subscription msg
                Messages.EXPIRES_NEVER,
                request.getTopics(),
                "application/json",
                "UTF-8",
                toBytes(json, "UTF-8"));
    }

    private static Message createPlainTextResponse(
            final UUID id,
            final ResponseStatus status,
            final String requestID,
            final Topics topics,
            final String text
    ) {
        return new Message(
                id,
                requestID,
                RESPONSE,
                status,
                true,   // oneway
                false,  // transient
                false,  // not a subscription msg
                Messages.EXPIRES_NEVER,
                topics,
                "text/plain",
                "UTF-8",
                text == null || text.isEmpty()
                    ? new byte[0]
                    : toBytes(text, "UTF-8"));
    }

    // ------------------------------------------------------------------------
    // Server utilities
    // ------------------------------------------------------------------------

    private Message getTcpServerStatus(final Message request) {
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
                           .add("permit-client-queue-mgmt", permitClientQueueMgmt)
                           .add("compression-cutoff-size", compressor.cutoffSize())
                           .add("write-ahead-log-dir", wal.isEnabled()
                                                            ? wal.getWalDir().getAbsolutePath()
                                                            : "-" )
                           .add("write-ahead-log-count", wal.isEnabled()
                                                            ? wal.countLogFiles()
                                                            : 0 )
                           .add("hearbeat-interval", heartbeatInterval)
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

    private Message getTcpServerThreadPoolStatistics(final Message request) {
        final VncMap statistics = serverThreadPoolStatistics.get();

        return createJsonResponse(request, OK, Json.writeJson(statistics, true));
    }

    private Message getTcpServerNextError(final Message request) {
        try {
            final Error err = errorBuffer.poll();
            if (err == null) {
                return createJsonResponse(
                        request,
                        QUEUE_EMPTY,
                        new JsonBuilder()
                                .add("status", "no_errors_available")
                                .toJson(false));
            }
            else {
                final String description = err.getDescription();
                final Exception ex = err.getException();
                final String exMsg = ex == null ? null :  ex.getMessage();

                return createJsonResponse(
                        request,
                        OK,
                        new JsonBuilder()
                                .add("status", "error")
                                .add("description", description)
                                .add("exception", exMsg)
                                .add("errors-left", errorBuffer.size())
                                .toJson(false));
            }
        }
        catch(Exception ex) {
            return createJsonResponse(
                    request,
                    HANDLER_ERROR,
                    new JsonBuilder()
                            .add("status", "temporarily_unavailable")
                            .toJson(false));
        }
    }

    private void auditResponseError(final Message request, final String errorMsg) {
        auditResponseError(request, errorMsg, null);
    }

    private void auditResponseError(
            final Message request,
            final String errorMsg,
            final Exception ex
    ) {
        try {
            errorBuffer.offer(new Error(errorMsg, request, ex));
            statistics.incrementDiscardedResponseCount();
        }
        catch(InterruptedException ignore) {}
    }


    private void auditPublishError(final Message msg, final String errorMsg) {
        auditPublishError(msg, errorMsg, null);
    }

    private void auditPublishError(
            final Message msg,
            final String errorMsg,
            final Exception ex
    ) {
        try {
            errorBuffer.offer(new Error(errorMsg, msg, ex));
            statistics.incrementDiscardedPublishCount();
        }
        catch(InterruptedException ignore) { }
    }

    private void removeAllChannelTemporaryQueues() {
        try {
            queueManager.withQueues(queues -> {
                final Set<String> names = tmpQueues.keySet();
                names.forEach(n -> queues.remove(n));
                tmpQueues.clear();
            });

            logInfo("Removed all temporary queues of the connnection");
        }
        catch(Exception ignore) { }
    }

    private void closeChannel() {
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


    private static enum State { Active, Terminated };

    public static final int ERROR_QUEUE_CAPACITY = 50;

    private volatile State mode = State.Active;
    private volatile String principal = "anon";
    private volatile boolean authenticated = false;
    private volatile Thread publisherThread;
    private volatile AcknowledgeMode msgAcknowledgeMode = AcknowledgeMode.NO_ACKNOWLEDGE;
    private volatile long lastHeartbeat = 0L;

    private final Server server;
    private final SocketChannel ch;
    private final long connectionId;
    private final ServerContext context;
    private final ServerLogger logger;

    private final Function<IMessage,IMessage> handler;
    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final ServerStatistics statistics;
    private final Supplier<VncMap> serverThreadPoolStatistics;
    private final Authenticator authenticator;

    private final AtomicBoolean stop = new AtomicBoolean(false);

    private final Semaphore sendSemaphore = new Semaphore(1);

    private final Protocol protocol = new Protocol();

    // configuration
    private final long maxMessageSize;
    private final long maxQueues;
    private final long maxTempQueuesPerConnection;
    private final boolean permitClientQueueMgmt;
    private final long heartbeatInterval;

    final ServerQueueManager queueManager;

    // compression
    private final Compressor compressor;

    // encryption
    private final boolean enforceEncryption;
    private final DiffieHellmanKeys dhKeys;
    private final AtomicReference<Encryptor> encryptor = new AtomicReference<>(Encryptor.off());
    private final AtomicReference<String> clientPublicKey = new AtomicReference<>(null);

    // queues
    private final IpcQueue<Error> errorBuffer;
    private final IpcQueue<Message> publishQueue;
    private final Map<String, Integer> tmpQueues = new ConcurrentHashMap<>();

    private final Map<MessageType, Function<Message,Message>> handlers = new HashMap<>();
}
