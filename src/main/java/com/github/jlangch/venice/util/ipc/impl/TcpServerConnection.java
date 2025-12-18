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
package com.github.jlangch.venice.util.ipc.impl;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.TcpServer;
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


public class TcpServerConnection implements IPublisher, Runnable {

    public TcpServerConnection(
            final TcpServer server,
            final SocketChannel ch,
            final long connectionId,
            final ServerLogger logger,
            final Function<IMessage,IMessage> handler,
            final AtomicLong maxMessageSize,
            final AtomicLong maxQueues,
            final WalQueueManager wal,
            final Subscriptions subscriptions,
            final int publishQueueCapacity,
            final Map<String, IpcQueue<Message>> p2pQueues,
            final Compressor compressor,
            final ServerStatistics statistics,
            final Supplier<VncMap> serverThreadPoolStatistics
    ) {
        this.server = server;
        this.ch = ch;
        this.connectionId = connectionId;
        this.logger = logger;
        this.handler = handler;
        this.maxMessageSize = maxMessageSize;
        this.maxQueues = maxQueues;
        this.wal = wal;
        this.subscriptions = subscriptions;
        this.publishQueueCapacity = publishQueueCapacity;
        this.compressor = compressor;
        this.statistics = statistics;
        this.serverThreadPoolStatistics = serverThreadPoolStatistics;

        this.publishQueue = new BoundedQueue<Message>("publish", publishQueueCapacity, false);
        this.errorBuffer = new CircularBuffer<>("error", ERROR_QUEUE_CAPACITY, false);
        this.p2pQueues = p2pQueues;

        this.dhKeys = DiffieHellmanKeys.create();
    }

    @Override
    public void run() {
        try {
            logger.info("conn-" + connectionId, "Listening on connection from " + IO.getRemoteAddress(ch));

            statistics.incrementConnectionCount();
            while(mode != State.Terminated && server.isRunning() && ch.isOpen()) {
                if (mode == State.Request_Response) {
                    // process a request/response message
                    mode = processRequestResponse();
                }
                else if (mode == State.Publish) {
                    // process publish messages if there are any waiting
                    mode = processPublication();
                }
                else {
                    break; // should no get here
                }
            }
        }
        catch(Exception ex) {
            // when the client closed the connection
            //   - server gets a java.io.IOException: Broken pipe
            //   - quit this connection and close the channel
        }
        finally {
            removeAllChannelTemporaryQueues();
            statistics.decrementConnectionCount();
            subscriptions.removeSubscriptions(this);
            IO.safeClose(ch);

            logger.info("conn-" + connectionId, "Closed connection");
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


    // ------------------------------------------------------------------------
    // Process requests
    // ------------------------------------------------------------------------

    private State processRequestResponse() throws InterruptedException {
        // [1] receive message
        final Message request = Protocol.receiveMessage(ch, compressor, encryptor.get());
        if (request == null) {
            return State.Terminated; // client closed connection
        }

        if (!server.isRunning()) {
            return State.Terminated;  // this server was closed
        }

        if (request.getType() != MessageType.DIFFIE_HELLMAN_KEY_REQUEST
            && request.getType() != MessageType.CLIENT_CONFIG
        ) {
            statistics.incrementMessageCount();
        }

        // [2] Handle max message payload size
        if (request.getData().length > maxMessageSize.get()) {
            if (request.isOneway()) {
                // oneway request -> cannot send and error message back
                auditResponseError(
                    request,
                    "Request too large! Cannot send error response back for oneway request!");
            }
            else {
               final Message response = createBadRequestTextMessageResponse(
                                            request,
                                            String.format(
                                                "The message (%d bytes) is too large! The limit is at %d bytes.",
                                                request.getData().length,
                                                maxMessageSize));
               Protocol.sendMessage(ch, response, compressor, encryptor.get());
            }
            return mode;
        }

        // [3] Handle server info requests
        if (request.getTopic().startsWith("tcp-server/")) {
            // process a server status request
            final Message response = handleTcpServerRequest(request);
            if (!request.isOneway()) {
                Protocol.sendMessage(ch, response, compressor, encryptor.get());
            }
            return mode;
        }

        // [4] Handle all other requests
        final Tuple2<State, Message> result = handleRequestMessage(mode, request);
        final State newState = result._1;
        final Message response = result._2;

        if (!server.isRunning()) {
            return State.Terminated;  // this server was closed
        }

        // [5] Send response
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
            if (mode == State.Request_Response) {
                switch(response.getResponseStatus()) {
                    case DIFFIE_HELLMAN_ACK:
                    case DIFFIE_HELLMAN_NAK:
                        // Diffie Hellman responses with compressing and encrypting!
                        Protocol.sendMessage(ch, response, Compressor.off(), Encryptor.off());
                        break;

                    default:
                        Protocol.sendMessage(ch, response, compressor,  encryptor.get());
                        break;
                }
            }
            else if (mode == State.Publish) {
                auditResponseError(
                        request,
                        String.format(
                          "Cannot send message response back to clients in subscription mode! Request: %s",
                          request.getType()));
            }
            else {
               // do nothing
            }
        }

        return newState;
    }


    // ------------------------------------------------------------------------
    // Process publications
    // ------------------------------------------------------------------------

    private State processPublication() throws InterruptedException {
        // check the publish queue
        final Message msg = publishQueue.poll(5, TimeUnit.SECONDS);

        if (msg != null) {
            statistics.incrementPublishCount();
            Protocol.sendMessage(
                    ch,
                    msg.withType(MessageType.REQUEST, true),
                    compressor,
                    encryptor.get());
        }

        return State.Publish;
    }

    private Tuple2<State,Message> handleRequestMessage(
            final State currState,
            final Message request
    ) {
        try {
            switch(request.getType()) {
                case REQUEST:
                    // client sent a normal message request, send the response back.
                    // call the server handler to process the request into a
                    // response and send the response only for non one-way
                    // requests back to the caller
                    return new Tuple2<State,Message>(
                                State.Request_Response,
                                handleSend(request));

                case SUBSCRIBE:
                    // the client wants to subscribe to a topic
                    return new Tuple2<State,Message>(
                            State.Publish,
                            handleSubscribe(request));

                case UNSUBSCRIBE:
                    // the client wants to unsubscribe from topic
                    return new Tuple2<State,Message>(
                            State.Publish,
                            handleUnsubscribe(request));

                case PUBLISH:
                    // the client sent a message to be published to all subscribers
                    // of the message's topic
                    return new Tuple2<State,Message>(
                            State.Request_Response,
                            handlePublish(request));

                case OFFER:
                    // the client offers a new message to a queue
                    return new Tuple2<State,Message>(
                            State.Request_Response,
                            handleOffer(request));

                case POLL:
                    // the client polls a new message from a queue
                    return new Tuple2<State,Message>(
                            State.Request_Response,
                            handlePoll(request));

                case CREATE_QUEUE:
                    return new Tuple2<State,Message>(
                            currState,
                            handleCreateQueueRequest(request));

                case CREATE_TEMP_QUEUE:
                    return new Tuple2<State,Message>(
                            currState,
                            handleCreateTemporaryQueueRequest(request));

                case REMOVE_QUEUE:
                    return new Tuple2<State,Message>(
                            currState,
                            handleRemoveQueueRequest(request));

                case STATUS_QUEUE:
                    return new Tuple2<State,Message>(
                            currState,
                            handleStatusQueueRequest(request));

                case CLIENT_CONFIG:
                    return new Tuple2<State,Message>(
                            currState,
                            handleClientConfigRequest(request));

                case DIFFIE_HELLMAN_KEY_REQUEST:
                    return new Tuple2<State,Message>(
                            State.Request_Response,
                            handleDiffieHellmanKeyExchange(request));

                default:
                    // Invalid request type
                    return new Tuple2<State,Message>(
                            currState,
                            createBadRequestTextMessageResponse(
                                    request,
                                    "Invalid request type: " + request.getType()));
            }
        }
        catch(Exception ex) {
        	// send an error response
            auditResponseError(request, "Failed to handle request!", ex);
            return new Tuple2<State,Message>(
                    currState,
                    createTextMessageResponse(
                        request,
                        ResponseStatus.HANDLER_ERROR,
                        "Failed to handle request of type " + request.getType() + "!\n"
                        + ExceptionUtil.printStackTraceToString(ex)));
        }
    }



    // ------------------------------------------------------------------------
    // Request handler
    // ------------------------------------------------------------------------

    private Message handleSend(final Message request) {
        final IMessage response = handler.apply(request);

        if (response == null) {
            // create an empty text response
            return createPlainTextResponseMessage(
                      ResponseStatus.OK,
                      request.getRequestId(),
                      request.getTopics(),
                      "");
        }
        else {
            return ((Message)response)
                        .withType(MessageType.RESPONSE, true)
                        .withResponseStatus(ResponseStatus.OK);
        }
    }

    private Message handleSubscribe(final Message request) {
        // register subscription
        subscriptions.addSubscriptions(request.getTopicsSet(), this);

        logger.info(
                "conn-" + connectionId,
                String.format("Subscribed to topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the subscription
        return createOkTextMessageResponse(request, "Subscribed to the topics.");
    }

    private Message handleUnsubscribe(final Message request) {
        // unregister subscription
        subscriptions.removeSubscriptions(request.getTopicsSet(), this);

        logger.info(
                "conn-" + connectionId,
                String.format("Unsubscribed from topics: %s.", Topics.encode(request.getTopics())));

        // acknowledge the unsubscription
        return createOkTextMessageResponse(request, "Unsubscribed from the topics.");
    }

    private Message handlePublish(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        return createOkTextMessageResponse(request, "Message has been enqued to publish.");
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

                    return new Message(
                            msg.getRequestId(),
                            MessageType.RESPONSE,
                            ResponseStatus.OK,
                            true,   // oneway
                            false,  // nondurable response
                            Message.EXPIRES_NEVER,
                            msg.getTopics(),
                            "text/plain",
                            "UTF-8",
                            toBytes(String.format(
                                        "Offered the message to the queue %s (durable: %b).",
                                        queue.name(),
                                        durable),
                                    "UTF-8"));
                }
                else {
                    return createTextMessageResponse(
                            request,
                            ResponseStatus.QUEUE_FULL,
                            "Offer rejected! The queue is full.");
                }
            }
            else {
                return createTextMessageResponse(
                        request,
                        ResponseStatus.QUEUE_NOT_FOUND,
                        "Offer rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextMessageResponse(
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
                        return createTextMessageResponse(
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
                                  .withResponseStatus(ResponseStatus.OK);
                    }
                }
            }
            else {
                return createTextMessageResponse(
                        request,
                        ResponseStatus.QUEUE_NOT_FOUND,
                        "Poll rejected! The queue does not exist.");
            }
        }
        catch(InterruptedException ex) {
            // interrupted while waiting for queue
            return createTextMessageResponse(
                    request,
                    ResponseStatus.QUEUE_ACCESS_INTERRUPTED,
                    "Poll rejected! Queue access interrupted.");
        }
    }

    private Message handleTcpServerRequest(final Message request) {
        if ("tcp-server/status".equals(request.getTopic())) {
            return getTcpServerStatus();
        }
        else if ("tcp-server/thread-pool-statistics".equals(request.getTopic())) {
            return getTcpServerThreadPoolStatistics();
        }
        else if ("tcp-server/error".equals(request.getTopic())) {
            return getTcpServerNextError();
        }
        else {
            return createBadRequestTextMessageResponse(
                    request,
                    "Unknown tcp server request topic. \n"
                          + "Valid topics are:\n"
                          + "  • tcp-server/status\n"
                          + "  • tcp-server/thread-pool-statistics\n"
                          + "  • tcp-server/error");
        }
    }

    private Message handleCreateQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final long maxQ =  maxQueues.get();
        if (countStandardQueues() >= maxQ) {
            return createBadRequestTextMessageResponse(
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
            return createBadRequestTextMessageResponse(
                    request,
                    "Cannot create a durable queue, if write-ahead-log is not activated on the server!");
        }

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format(
                            "Request %s: Invalid queue name: %s",
                            request.getType(), ex.getMessage()));
        }

        if (StringUtil.isBlank(queueName) || capacity < 1) {
            return createBadRequestTextMessageResponse(
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
                               logger.info(
                                  "conn-" + connectionId,
                                  String.format(
                                      "Created queue %s. Capacity=%d, bounded=%b, durable=%b",
                                      queueName,
                                      capacity,
                                      bounded,
                                      durable));

                               return q;
                        });
            }
            catch(Exception ex) {
                return createBadRequestTextMessageResponse(
                        request,
                        String.format(
                            "Request %s: Failed to ceate queue: %s. Reason: %s",
                            request.getType(), queueName, ex.getMessage()));
            }

            return createOkTextMessageResponse(
                    request,
                    String.format("Request %s: Queue %s created.", request.getType(), queueName));
        }
    }

    private Message handleCreateTemporaryQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final long maxQ = maxQueues.get();
        if (tmpQueues.size() >= maxQ) {
            return createBadRequestTextMessageResponse(
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
            return createBadRequestTextMessageResponse(
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
                return createBadRequestTextMessageResponse(
                        request,
                        String.format("Request %s: Invalid queue name: %s", request.getType(), ex.getMessage()));
            }


            // do not overwrite the queue if it already exists
           p2pQueues.computeIfAbsent(
               queueName,
               k -> {
                   final IpcQueue<Message> q = new BoundedQueue<Message>(queueName, capacity, true);
                   tmpQueues.put(queueName, 0);

                   logger.info(
                           "conn-" + connectionId,
                           String.format(
                               "Created temporary queue %s. Capacity=%d",
                               queueName,
                               capacity));

                   return q;
               });

            return createOkTextMessageResponse(request, queueName);
        }
    }

    private Message handleRemoveQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Invalid queue name: %s", request.getType(), ex.getMessage()));
        }

        final IpcQueue<Message> queue = p2pQueues.get(queueName);
        if (queue != null) {
            queue.onRemove();
        }

        p2pQueues.remove(queueName);
        tmpQueues.remove(queueName);

        logger.info("conn-" + connectionId, String.format("Removed queue %s.", queueName));

        return createOkTextMessageResponse(
                request,
                String.format("Request %s: Queue %s removed.", request.getType(), queueName));
    }

    private Message handleStatusQueueRequest(final Message request) {
        if (!"application/json".equals(request.getMimetype())) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Expected a JSON payload", request.getType()));
        }

        final VncMap payload = (VncMap)Json.readJson(request.getText(), false);
        final String queueName = Coerce.toVncString(payload.get(new VncString("name"))).getValue();

        try {
            QueueValidator.validate(queueName);
        }
        catch(Exception ex) {
            return createBadRequestTextMessageResponse(
                    request,
                    String.format("Request %s: Invalid queue name: %s", request.getType(), ex.getMessage()));
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

        return createJsonResponseMessage(
                    ResponseStatus.OK,
                    request.getRequestId(),
                    request.getTopics(),
                    response);
    }

    private Message handleClientConfigRequest(final Message request) {
        return createJsonResponseMessage(
                    ResponseStatus.OK,
                    request.getRequestId(),
                    request.getTopics(),
                    new JsonBuilder()
                            .add("max-msg-size", maxMessageSize.get())
                            .add("compress-cutoff-size", compressor.cutoffSize())
                            .add("encrypt",      enforceEncryption)
                            .toJson(false));
    }

    private Message handleDiffieHellmanKeyExchange(final Message request) {
        if (encryptor.get().isActive()) {
            logger.warn("conn-" + connectionId, "Diffie-Hellman key already exchanged!");
            return createPlainTextResponseMessage(
                       ResponseStatus.DIFFIE_HELLMAN_NAK,
                       null,
                       Topics.of("dh"),
                       "Error: Diffie-Hellman key already exchanged!");
        }
        else {
            try {
                logger.info("conn-" + connectionId, "Diffie-Hellman key exchange initiated!");

                final String clientPublicKey = request.getText();
                encryptor.set(Encryptor.aes(dhKeys.generateSharedSecret(clientPublicKey)));

                logger.info("conn-" + connectionId, "Diffie-Hellman key exchange completed!");
                logger.info("conn-" + connectionId, "Activated message encryption!");

                // send the server's public key back
                return createPlainTextResponseMessage(
                           ResponseStatus.DIFFIE_HELLMAN_ACK,
                           null,
                           Topics.of("dh"),
                           dhKeys.getPublicKeyBase64());
            }
            catch(Exception ex) {
                logger.warn("conn-" + connectionId, "Diffie-Hellman key exchange error!", ex);

                return createPlainTextResponseMessage(
                           ResponseStatus.DIFFIE_HELLMAN_NAK,
                           null,
                           Topics.of("dh"),
                           "Failed to exchange Diffie-Hellman key! Reason: " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Create response messages
    // ------------------------------------------------------------------------

    private Message createBadRequestTextMessageResponse(
            final Message request,
            final String errorMsg
    ) {
        return createTextMessageResponse(request, ResponseStatus.BAD_REQUEST, errorMsg);
    }

    private Message createOkTextMessageResponse(
            final Message request,
            final String message
    ) {
       return createTextMessageResponse(request, ResponseStatus.OK, message);
    }

    private Message createTextMessageResponse(
            final Message request,
            final ResponseStatus responseStatus,
            final String message
    ) {
        return createPlainTextResponseMessage(
                    responseStatus,
                    request.getRequestId(),
                    request.getTopics(),
                    message);
    }

    private static Message createJsonResponseMessage(
            final ResponseStatus status,
            final String requestID,
            final Topics topics,
            final String json
    ) {
        return new Message(
                requestID,
                MessageType.RESPONSE,
                status,
                true,   // oneway
                false,  // transient
                Message.EXPIRES_NEVER,
                topics,
                "application/json",
                "UTF-8",
                toBytes(json, "UTF-8"));
    }

    private static Message createPlainTextResponseMessage(
            final ResponseStatus status,
            final String requestID,
            final Topics topics,
            final String text
    ) {
        return new Message(
                requestID,
                MessageType.RESPONSE,
                status,
                true,   // oneway
                false,  // transient
                Message.EXPIRES_NEVER,
                topics,
                "text/plain",
                "UTF-8",
                toBytes(text, "UTF-8"));
    }

    // ------------------------------------------------------------------------
    // Server utilities
    // ------------------------------------------------------------------------

    private Message getTcpServerStatus() {
        return createJsonResponseMessage(
                   ResponseStatus.OK,
                   null,
                   Topics.of("tcp-server/status"),
                   new JsonBuilder()
                           .add("running", server.isRunning())
                           .add("mode", mode.name())
                           // config
                           .add("message-size-max", maxMessageSize.get())
                           .add("compression-cutoff-size", server.getCompressCutoffSize())
                           .add("encryption", encryptor.get().isActive())
                           .add("write-ahead-log-dir", wal.isEnabled()
                                                            ? wal.getWalDir().getAbsolutePath()
                                                            : "-" )
                           .add("write-ahead-log-count", wal.isEnabled()
                                                            ? wal.countLogFiles()
                                                            : 0 )
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

    private Message getTcpServerThreadPoolStatistics() {
        final VncMap statistics = serverThreadPoolStatistics.get();

        return createJsonResponseMessage(
                ResponseStatus.OK,
                null,
                Topics.of("tcp-server/thread-pool-statistics"),
                Json.writeJson(statistics, true));
    }

    private Message getTcpServerNextError() {
        try {
            final Error err = errorBuffer.poll();
            if (err == null) {
                return createJsonResponseMessage(
                        ResponseStatus.OK,
                        null,
                        Topics.of("tcp-server/error"),
                        new JsonBuilder()
                                .add("status", "no_errors_available")
                                .toJson(false));
            }
            else {
                final String description = err.getDescription();
                final Exception ex = err.getException();
                final String exMsg = ex == null ? null :  ex.getMessage();

                return createJsonResponseMessage(
                        ResponseStatus.OK,
                        null,
                        Topics.of("tcp-server/error"),
                        new JsonBuilder()
                                .add("status", "error")
                                .add("description", description)
                                .add("exception", exMsg)
                                .add("errors-left", errorBuffer.size())
                                .toJson(false));
            }
        }
        catch(Exception ex) {
            return createJsonResponseMessage(
                    ResponseStatus.OK,
                    null,
                    Topics.of("tcp-server/error"),
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

            logger.info("conn-" + connectionId, "Removed all temporary queues of the connnection");
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



    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    private static enum State { Request_Response, Publish, Terminated };

    public static final int ERROR_QUEUE_CAPACITY = 50;

    private State mode = State.Request_Response;

    private final TcpServer server;
    private final SocketChannel ch;
    private final long connectionId;
    private final ServerLogger logger;

    private final Function<IMessage,IMessage> handler;
    private final AtomicLong maxMessageSize;
    private final AtomicLong maxQueues;
    private final WalQueueManager wal;
    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final ServerStatistics statistics;
    private final Supplier<VncMap> serverThreadPoolStatistics;

    // compression
    private final Compressor compressor;

    // encryption
    private final boolean enforceEncryption = false;
    private final DiffieHellmanKeys dhKeys;
    private final AtomicReference<Encryptor> encryptor = new AtomicReference<>(Encryptor.off());

    // queues
    private final IpcQueue<Error> errorBuffer;
    private final IpcQueue<Message> publishQueue;
    private final Map<String, IpcQueue<Message>> p2pQueues;
    private final Map<String, Integer> tmpQueues = new ConcurrentHashMap<>();
}
