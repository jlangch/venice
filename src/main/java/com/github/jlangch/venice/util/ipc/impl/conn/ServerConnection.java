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

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
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
import com.github.jlangch.venice.util.ipc.TcpServer;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.QueueFactory;
import com.github.jlangch.venice.util.ipc.impl.QueueValidator;
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
            final TcpServer server,
            final SocketChannel ch,
            final long connectionId,
            final Authenticator authenticator,
            final ServerLogger logger,
            final Function<IMessage,IMessage> handler,
            final long maxMessageSize,
            final long maxQueues,
            final boolean permitClientQueueMgmt,
            final long heartbeatInterval,
            final WalQueueManager wal,
            final Subscriptions subscriptions,
            final int publishQueueCapacity,
            final Map<String, IpcQueue<Message>> p2pQueues,
            final boolean enforceEncryption,
            final Compressor compressor,
            final ServerStatistics statistics,
            final Supplier<VncMap> serverThreadPoolStatistics
    ) {
        this.server = server;
        this.ch = ch;
        this.connectionId = connectionId;
        this.authenticator = authenticator;
        this.logger = logger;
        this.handler = handler;
        this.maxMessageSize = maxMessageSize;
        this.maxQueues = maxQueues;
        this.permitClientQueueMgmt = permitClientQueueMgmt;
        this.heartbeatInterval = heartbeatInterval;
        this.wal = wal;
        this.subscriptions = subscriptions;
        this.publishQueueCapacity = publishQueueCapacity;
        this.enforceEncryption = enforceEncryption;
        this.compressor = compressor;
        this.statistics = statistics;
        this.serverThreadPoolStatistics = serverThreadPoolStatistics;

        this.publishQueue = new BoundedQueue<Message>("publish", publishQueueCapacity, false);
        this.errorBuffer = new CircularBuffer<>("error", ERROR_QUEUE_CAPACITY, false);
        this.p2pQueues = p2pQueues;

        this.dhKeys = DiffieHellmanKeys.create();
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
        // Note: no compression, no encryption!
        Protocol.sendMessage(ch, response, Compressor.off(), Encryptor.off(), -1);
    }

    private void sendResponse(final Message response) throws InterruptedException {
        if (sendSemaphore.tryAcquire(3, TimeUnit.SECONDS)) {
            try {
                Protocol.sendMessage(ch, response, compressor, encryptor.get(), maxMessageSize);
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
                    final Message pubMsg = msg.withType(MessageType.REQUEST, true);

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
        final Message request = Protocol.receiveMessage(ch, compressor, encryptor.get());
        if (request == null) {
            mode = State.Terminated; // client closed connection
            return;
        }

        if (!server.isRunning()) {
            mode = State.Terminated;  // this server was closed
            return;
        }

        if (request.getType() != MessageType.DIFFIE_HELLMAN_KEY_REQUEST
            && request.getType() != MessageType.CLIENT_CONFIG
        ) {
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

        // [4] Send response
        if (request.isOneway()) {
            // oneway request -> do not send any response message back
            switch(response.getResponseStatus()) {
                case SERVER_ERROR:
                case HANDLER_ERROR:
                case BAD_REQUEST:
                    auditResponseError(
                            request,
                            String.format(
                              "Cannot send error response %s for oneway requests! Request: %s",
                              response.getResponseStatus(),
                              request.getType()));
                    break;

                default:
                    break;
            }
        }
        else {
            // request -> response
            switch(response.getResponseStatus()) {
                case DIFFIE_HELLMAN_ACK:
                case DIFFIE_HELLMAN_NAK:
                    // Diffie Hellman responses without compressing and encrypting!
                    sendDiffieHellmanResponse(response);
                    break;

                default:
                    sendResponse(response);
                    break;
            }
        }
    }

    private Message handleRequestMessage(final Message request) {
        // Authentication
        if (authenticator.isActive()) {
            switch(request.getType()) {
                case CLIENT_CONFIG:
                case DIFFIE_HELLMAN_KEY_REQUEST:
                case AUTHENTICATION:
                    break;
                default:
                    if (!authenticated) {
                        return createTextResponse(
                                request,
                                ResponseStatus.NO_PERMISSION,
                                "Authentication is required!");
                    }
                    break;
            }
        }

        try {
            switch(request.getType()) {
                case REQUEST:
                    // client sent a normal message request, send the response back.
                    // call the server handler to process the request into a
                    // response and send the response only for non one-way
                    // requests back to the caller
                    return handleSend(request);

                case SUBSCRIBE:
                    // the client wants to subscribe to a topic
                    return handleSubscribe(request);

                case UNSUBSCRIBE:
                    // the client wants to unsubscribe from topic
                    return handleUnsubscribe(request);

                case PUBLISH:
                    // the client sent a message to be published to all subscribers
                    // of the message's topic
                    return handlePublish(request);

                case OFFER:
                    // the client offers a new message to a queue
                    return handleOffer(request);

                case POLL:
                    // the client polls a new message from a queue
                    return handlePoll(request);

                case CREATE_QUEUE:
                    return handleCreateQueueRequest(request);

                case CREATE_TEMP_QUEUE:
                    return handleCreateTemporaryQueueRequest(request);

                case REMOVE_QUEUE:
                    return handleRemoveQueueRequest(request);

                case STATUS_QUEUE:
                    return handleStatusQueueRequest(request);

                case CLIENT_CONFIG:
                    return handleClientConfigRequest(request);

                case DIFFIE_HELLMAN_KEY_REQUEST:
                    return handleDiffieHellmanKeyExchange(request);

                case AUTHENTICATION:
                    return handleAuthentication(request);

                case HEARTBEAT:
                    return handleHeartbeat(request);

                case TEST:
                    return handleTest(request);

                default:
                    // Invalid request type
                    return createBadRequestResponse(
                                    request,
                                    "Invalid request type: " + request.getType());
            }
        }
        catch(Exception ex) {
            // send an error response
            auditResponseError(request, "Failed to handle request!", ex);

            // TODO: how much information from the exception shall we pass back
            //       to the client
            return createTextResponse(
                        request,
                        ResponseStatus.HANDLER_ERROR,
                        "Failed to handle request of type " + request.getType() + "!\n"
                        + ExceptionUtil.printStackTraceToString(ex));
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
                return createTextResponse(
                            request,
                            ResponseStatus.OK,
                            "");
            }
            else {
                return ((Message)response)
                            .withType(MessageType.RESPONSE, true)
                            .withResponseStatus(request.getId(), ResponseStatus.OK);
            }
        }
    }

    private Message handleSubscribe(final Message request) {
        // register subscription
        subscriptions.addSubscriptions(request.getTopicsSet(), this);

        logInfo(String.format("Subscribed to topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the subscription
        return createOkTextResponse(request, "Subscribed to the topics.");
    }

    private Message handleUnsubscribe(final Message request) {
        // unregister subscription
        subscriptions.removeSubscriptions(request.getTopicsSet(), this);

        logInfo(String.format("Unsubscribed from topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the unsubscription
        return createOkTextResponse(request, "Unsubscribed from the topics.");
    }

    private Message handlePublish(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        return createOkTextResponse(request, "Message has been enqued to publish.");
    }

    private Message handleOffer(final Message request) {
        try {
            final String queueName = request.getQueueName();
            final IpcQueue<Message> queue = p2pQueues.get(queueName);
            if (queue != null) {
                // convert message type from OFFER to REQUEST
                final Message msg = request.withType(MessageType.REQUEST, request.isOneway());
                final long timeout = msg.getTimeout();

                final boolean ok = timeout < 0
                                    ? queue.offer(msg)
                                    : queue.offer(msg, timeout, TimeUnit.MILLISECONDS);
                if (ok) {
                    final boolean durable = msg.isDurable()       // message is durable
                                            && queue.isDurable()  // queue is durable
                                            && wal.isEnabled();   // server supports write-ahead-log

                    return createTextResponse(
                            request,
                            ResponseStatus.OK,
                            String.format(
                                "Offered the message to the queue %s (durable: %b).",
                                queue.name(),
                                durable));
                }
                else {
                    return createTextResponse(
                            request,
                            ResponseStatus.QUEUE_FULL,
                            "Offer rejected! The queue is full.");
                }
            }
            else {
                return createTextResponse(
                        request,
                        ResponseStatus.QUEUE_NOT_FOUND,
                        "Offer rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    ResponseStatus.QUEUE_ACCESS_INTERRUPTED,
                    "Offer rejected! Queue access interrupted.");
        }
    }

    private Message handlePoll(final Message request) {
        try {
            final long timeout = request.getTimeout();
            final String queueName = request.getQueueName();
            final IpcQueue<Message> queue = p2pQueues.get(queueName);
            if (queue != null) {
                while(true) {
                    final Message msg = timeout < 0
                                            ? queue.poll()
                                            : queue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (msg == null) {
                        return createTextResponse(
                                request,
                                ResponseStatus.QUEUE_EMPTY,
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
                        return msg.withType(MessageType.RESPONSE, true)
                                  .withResponseStatus(request.getId(), ResponseStatus.OK);
                    }
                }
            }
            else {
                return createTextResponse(
                        request,
                        ResponseStatus.QUEUE_NOT_FOUND,
                        "Poll rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextResponse(
                    request,
                    ResponseStatus.QUEUE_ACCESS_INTERRUPTED,
                    "Poll rejected! Queue access interrupted.");
        }
    }

    private Message handleCreateQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to create queues!");
        }

        final long maxQ =  maxQueues;
        if (countStandardQueues() >= maxQ) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Too many queues! Reached the limit of %d queues.",
                        request.getType(),
                        maxQ));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();
        final int capacity = Coerce.toVncLong(payload.get(new VncString("capacity"))).toJavaInteger();
        final boolean bounded = Coerce.toVncBoolean(payload.get(new VncString("bounded"))).getValue();
        final boolean durable = Coerce.toVncBoolean(payload.get(new VncString("durable"))).getValue();

        if (durable && !wal.isEnabled()) {
            return createBadRequestResponse(
                    request,
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                            "Request %s: Invalid queue name: %s",
                            request.getType(), ex.getMessage()));
        }

        if (StringUtil.isBlank(queueName) || capacity < 1) {
            return createBadRequestResponse(
                    request,
                    String.format(
                       "Request %s: A queue name must not be blank and the "
                       + "capacity must not be lower than 1",
                       request.getType()));
        }
        else {
            try {
                // do not overwrite the queue if it already exists
                p2pQueues.computeIfAbsent(
                        queueName,
                        k -> { final IpcQueue<Message> q = QueueFactory.createQueue(
                                                                wal,
                                                                queueName,
                                                                capacity,
                                                                bounded,
                                                                durable);
                                logInfo(String.format(
                                      "Created queue %s. Capacity=%d, bounded=%b, durable=%b",
                                      queueName,
                                      capacity,
                                      bounded,
                                      durable));

                               return q;
                        });
            }
            catch(Exception ex) {
                return createBadRequestResponse(
                        request,
                        String.format(
                            "Request %s: Failed to ceate queue: %s. Reason: %s",
                            request.getType(), queueName, ex.getMessage()));
            }

            return createOkTextResponse(
                    request,
                    String.format("Request %s: Queue %s created.", request.getType(), queueName));
        }
    }

    private Message handleCreateTemporaryQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final long maxQ = maxQueues;
        if (tmpQueues.size() >= maxQ) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Too many temporary queues! "
                        + "Reached the limit of %d temporary queues for this client.",
                        request.getType(),
                        maxQ));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final int capacity = Coerce.toVncLong(payload.get(new VncString("capacity"))).toJavaInteger();

        if (capacity < 1) {
            return createBadRequestResponse(
                    request,
                    String.format(
                       "Request %s: A queue capacity must not be lower than 1",
                       request.getType()));
        }
        else {
            final String queueName = "queue/" + UUID.randomUUID().toString();

            try {
                QueueValidator.validate(queueName);
            }
            catch(Exception ex) {
                return createBadRequestResponse(
                        request,
                        String.format(
                            "Request %s: Invalid queue name: %s",
                            request.getType(), ex.getMessage()));
            }


            // do not overwrite the queue if it already exists
           p2pQueues.computeIfAbsent(
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

            return createOkTextResponse(request, queueName);
        }
    }

    private Message handleRemoveQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Expected a JSON payload",
                        request.getType()));
        }

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to remove queues!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Invalid queue name: %s",
                        request.getType(), ex.getMessage()));
        }

        final IpcQueue<Message> queue = p2pQueues.get(queueName);
        if (queue != null) {
            queue.onRemove();
        }

        p2pQueues.remove(queueName);
        tmpQueues.remove(queueName);

        logInfo(String.format("Removed queue %s.", queueName));

        return createOkTextResponse(
                request,
                String.format(
                    "Request %s: Queue %s removed.",
                    request.getType(), queueName));
    }

    private Message handleStatusQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Expected a JSON payload",
                        request.getType()));
        }

        if (!permitClientQueueMgmt) {
            return createNoPermissionResponse(
                    request,
                    "Clients are not permitted to request queue status!");
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestResponse(
                    request,
                    String.format(
                        "Request %s: Invalid queue name: %s",
                        request.getType(), ex.getMessage()));
        }

        final IpcQueue<Message> q = p2pQueues.get(queueName);

        final String response = new JsonBuilder()
                                        .add("name",      queueName)
                                        .add("exists",    q != null)
                                        .add("type",      q == null ? null : q.type().name())
                                        .add("temporary", q != null && q.isTemporary())
                                        .add("durable",   q != null && q.isDurable())
                                        .add("capacity",  q == null ? 0L : (long)q.capacity())
                                        .add("size",      q == null ? 0L : (long)q.size())
                                        .toJson(false);

        return createJsonResponse(
                    request,
                    ResponseStatus.OK,
                    response);
    }

    private Message handleClientConfigRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String sAckMode = Coerce.toVncString(payload.get(new VncString("ackMode"))).getValue();
        msgAcknowledgeMode = AcknowledgeMode.valueOf(sAckMode);

        logInfo("Handling client config request! AcknowledgeMode: " + msgAcknowledgeMode);
        return createJsonResponse(
                    request,
                    ResponseStatus.OK,
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
                       ResponseStatus.DIFFIE_HELLMAN_NAK,
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
                           ResponseStatus.DIFFIE_HELLMAN_ACK,
                           null,
                           Topics.of(Messages.TOPIC_DIFFIE_HELLMANN),
                           dhKeys.getPublicKeyBase64());
            }
            catch(Exception ex) {
                logError("Diffie-Hellman key exchange error!", ex);

                return createPlainTextResponse(
                           request.getId(),
                           ResponseStatus.DIFFIE_HELLMAN_NAK,
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
                return createTextResponse(request, ResponseStatus.OK, "");
            }
        }

        logError("Authentication failure '" + payload.get(0) + "'");
        return createTextResponse(request, ResponseStatus.NO_PERMISSION, "");
    }

    private Message handleHeartbeat(final Message request) {
        lastHeartbeat = System.currentTimeMillis();
        logInfo("Heartbeat");
        return createTextResponse(request, ResponseStatus.OK, "");
    }

    private Message handleTest(final Message request) {
        return createTextResponse(request, ResponseStatus.OK, "");
    }


    // ------------------------------------------------------------------------
    // Create response messages
    // ------------------------------------------------------------------------

    private Message createBadRequestResponse(
            final Message request,
            final String errorMsg
    ) {
        return createTextResponse(request, ResponseStatus.BAD_REQUEST, errorMsg);
    }

    private Message createNoPermissionResponse(
            final Message request,
            final String errorMsg
    ) {
        return createTextResponse(request, ResponseStatus.NO_PERMISSION, errorMsg);
    }

    private Message createOkTextResponse(
            final Message request,
            final String message
    ) {
       return createTextResponse(request, ResponseStatus.OK, message);
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

    private static Message createJsonResponse(
            final Message request,
            final ResponseStatus status,
            final String json
    ) {
        return new Message(
                request.getId(),
                request.getRequestId(),
                MessageType.RESPONSE,
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
                MessageType.RESPONSE,
                status,
                true,   // oneway
                false,  // transient
                false,  // not a subscription msg
                Messages.EXPIRES_NEVER,
                topics,
                "text/plain",
                "UTF-8",
                toBytes(text, "UTF-8"));
    }

    // ------------------------------------------------------------------------
    // Server utilities
    // ------------------------------------------------------------------------

    private Message getTcpServerStatus(final Message request) {
        return createJsonResponse(
                   request,
                   ResponseStatus.OK,
                   new JsonBuilder()
                           .add("running", server.isRunning())
                            // config
                           .add("encryption", encryptor.get().isActive())
                           .add("max-queues", maxQueues)
                           .add("message-size-max", maxMessageSize)
                           .add("permit-client-queue-mgmt", permitClientQueueMgmt)
                           .add("compression-cutoff-size", server.getCompressCutoffSize())
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
                           .add("queue-count", p2pQueues.size())
                           .add("temp-queue-count", p2pQueues.values().stream().filter(q -> q.isTemporary()).count())
                           .add("temp-queue-this-client-count", tmpQueues.size())
                           .toJson(false));
    }

    private Message getTcpServerThreadPoolStatistics(final Message request) {
        final VncMap statistics = serverThreadPoolStatistics.get();

        return createJsonResponse(
                request,
                ResponseStatus.OK,
                Json.writeJson(statistics, true));
    }

    private Message getTcpServerNextError(final Message request) {
        try {
            final Error err = errorBuffer.poll();
            if (err == null) {
                return createJsonResponse(
                        request,
                        ResponseStatus.QUEUE_EMPTY,
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
                        ResponseStatus.OK,
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
                    ResponseStatus.HANDLER_ERROR,
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
            final Set<String> names = tmpQueues.keySet();
            names.forEach(n -> p2pQueues.remove(n));
            tmpQueues.clear();

            logInfo("Removed all temporary queues of the connnection");
        }
        catch(Exception ignore) { }
    }

    private long countStandardQueues() {
        return p2pQueues
                .values()
                .stream()
                .filter(q -> !q.isTemporary())
                .count();
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

    private final TcpServer server;
    private final SocketChannel ch;
    private final long connectionId;
    private final ServerLogger logger;

    private final Function<IMessage,IMessage> handler;
    private final WalQueueManager wal;
    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final ServerStatistics statistics;
    private final Supplier<VncMap> serverThreadPoolStatistics;
    private final Authenticator authenticator;

    private final AtomicBoolean stop = new AtomicBoolean(false);

    private final Semaphore sendSemaphore = new Semaphore(1);

    // configuration
    private final long maxMessageSize;
    private final long maxQueues;
    private final boolean permitClientQueueMgmt;
    private final long heartbeatInterval;

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
    private final Map<String, IpcQueue<Message>> p2pQueues;
    private final Map<String, Integer> tmpQueues = new ConcurrentHashMap<>();
}
