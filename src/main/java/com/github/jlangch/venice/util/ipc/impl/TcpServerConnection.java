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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.TcpServer;
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


public class TcpServerConnection implements IPublisher, Runnable {

    public TcpServerConnection(
            final TcpServer server,
            final SocketChannel ch,
            final Function<IMessage,IMessage> handler,
            final AtomicLong maxMessageSize,
            final Subscriptions subscriptions,
            final int publishQueueCapacity,
            final Map<String, IpcQueue<Message>> p2pQueues,
            final Compressor compressor,
            final ServerStatistics statistics,
            final Supplier<VncMap> serverThreadPoolStatistics
    ) {
        this.server = server;
        this.ch = ch;
        this.handler = handler;
        this.maxMessageSize = maxMessageSize;
        this.subscriptions = subscriptions;
        this.publishQueueCapacity = publishQueueCapacity;
        this.compressor = compressor;
        this.statistics = statistics;
        this.serverThreadPoolStatistics = serverThreadPoolStatistics;

        this.publishQueue = new BoundedQueue<Message>("publish", publishQueueCapacity);
        this.errorBuffer = new CircularBuffer<>("error", ERROR_QUEUE_CAPACITY);
        this.p2pQueues = p2pQueues;

        this.dhKeys = DiffieHellmanKeys.create();
    }

    @Override
    public void run() {
        try {
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
                    break;
                }
            }
        }
        catch(Exception ex) {
            // when the client closed the connection
            //   - server gets a java.io.IOException: Broken pipe
            //   - quit this connection and close the channel
        }
        finally {
            statistics.decrementConnectionCount();
            subscriptions.removeSubscriptions(this);
            IO.safeClose(ch);
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
                errorBuffer.offer(new Error("Failed to enque message for publishing. Publish queue is full!", msg));
                statistics.incrementDiscardedPublishCount();
            }
        }
        catch(Exception ex) {
            // there is no dead letter queue yet, just count the
            // discarded messages
            try {
                errorBuffer.offer(new Error("Failed to enque message for publishing!", msg, ex));
                statistics.incrementDiscardedPublishCount();
            }
            catch(InterruptedException ignore) {
            }
        }
    }

    private State processRequestResponse() throws InterruptedException {
        // [1] receive message
        final Message request = Protocol.receiveMessage(ch, compressor, encryptor.get());
        if (request == null) {
            return State.Terminated; // client closed connection
        }

        if (!isRequestDiffieHellman(request)) {
            statistics.incrementMessageCount();
        }

        if (!server.isRunning()) {
            return State.Terminated;  // this server was closed
        }

        // send an error back if the received message is not a request
        if (!(isRequestMsg(request)
              || isRequestPublish(request)
              || isRequestSubscribe(request)
              || isRequestUnsubscribe(request)
              || isRequestOffer(request)
              || isRequestPoll(request)
              || isRequestDiffieHellman(request))
        ) {
            handleInvalidRequestType(request);
            return mode;
        }

        if (request.getData().length > maxMessageSize.get()) {
            handleRequestTooLarge(request);
            return mode;
        }

        // [2] Handle the request
        if (request.getTopic().startsWith("tcp-server/")) {
            // process a server status request
            handleTcpServerRequest(request);
            return State.Request_Response;
        }
        else if (isRequestPublish(request)) {
            // the client sent a message to be published to all subscribers
            // of the message's topic
            handlePublish(request);
            return State.Request_Response;
        }
        else if (isRequestMsg(request)) {
            // client sent a normal message request, send the response
            // back
            // call the server handler to process the request into a
            // response and send the response only for non one-way
            // requests back to the caller
            final Message response = handleRequest(request);

            if (!server.isRunning()) {
                return State.Terminated;  // this server was closed
            }

            // [3] Send response
            if (response != null && !request.isOneway()) {
                Protocol.sendMessage(
                        ch,
                        response,
                        compressor,
                        encryptor.get());
            }

            return State.Request_Response;
        }
        else if (isRequestOffer(request)) {
            // the client offers a new message to a queue
            handleOffer(request);

            return State.Request_Response;
        }
        else if (isRequestPoll(request)) {
            // the client polls a new message from a queue
            handlePoll(request);

            return State.Request_Response;
        }
        else if (isRequestSubscribe(request)) {
            // the client wants to subscribe a topic
            handleSubscribe(request);

            // switch to publish mode for this connections
            return State.Publish;
        }
        else if (isRequestUnsubscribe(request)) {
            // the client wants to subscribe a topic
            handleUnsubscribe(request);

            // switch to publish mode for this connections
            return State.Publish;
        }
        else if (isRequestDiffieHellman(request)) {
            handleDiffieHellmanKeyExchange(request);

            return State.Request_Response;
        }
        else {
            // should not get here
            handleInvalidRequestType(request);
            return State.Request_Response;
        }
    }

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

    private Message handleRequest(final Message request) {
        try {
            final IMessage response = handler.apply(request);

            if (request.isOneway()) {
                // do not reply on one-way messages
                // just discard the handler's response and return null!
                return null;
            }
            else {
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
        }
        catch(Exception ex) {
            if (request.isOneway()) {
                // do not reply on one-way messages
                // just discard the exception
                return null;
            }
            else {
                // send an error response
                return createPlainTextResponseMessage(
                         ResponseStatus.HANDLER_ERROR,
                         request.getRequestId(),
                         request.getTopics(),
                         ExceptionUtil.printStackTraceToString(ex));
            }
        }
    }

    private void handleSubscribe(final Message request) {
        // register subscription
        subscriptions.addSubscriptions(request.getTopicsSet(), this);

        // acknowledge the subscription
        Protocol.sendMessage(
            ch,
            createPlainTextResponseMessage(
                ResponseStatus.OK,
                request.getRequestId(),
                request.getTopics(),
                "Subscribed to the topics."),
            compressor,
            encryptor.get());
    }

    private void handleUnsubscribe(final Message request) {
        // unregister subscription
        subscriptions.removeSubscriptions(request.getTopicsSet(), this);

        // acknowledge the unsubscription
        Protocol.sendMessage(
            ch,
            createPlainTextResponseMessage(
                ResponseStatus.OK,
                request.getRequestId(),
                request.getTopics(),
                "Unsubscribed from the topics."),
            compressor,
            encryptor.get());
    }

    private void handlePublish(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        Protocol.sendMessage(
            ch,
            createPlainTextResponseMessage(
                ResponseStatus.OK,
                request.getRequestId(),
                request.getTopics(),
                "Message has been enqued to publish."),
            compressor,
            encryptor.get());
    }

    private void handleOffer(final Message request) throws InterruptedException {
        final String queueName = request.getQueueName();
        final IpcQueue<Message> queue = p2pQueues.get(queueName);
        if (queue != null) {
            final Message msg = request.withType(MessageType.REQUEST, false);
            final long timeout = msg.getTimeout();

            final boolean ok = timeout < 0
                                ? queue.offer(msg)
                                : queue.offer(msg, timeout, TimeUnit.MILLISECONDS);
            if (ok) {
                Protocol.sendMessage(
                    ch,
                    createPlainTextResponseMessage(
                        ResponseStatus.OK,
                        request.getRequestId(),
                        request.getTopics(),
                        "Offered the message to the queue."),
                    compressor,
                    encryptor.get());
            }
            else {
                Protocol.sendMessage(
                    ch,
                    createPlainTextResponseMessage(
                        ResponseStatus.QUEUE_FULL,
                        request.getRequestId(),
                        request.getTopics(),
                        "Offer rejected! The queue is full."),
                    compressor,
                    encryptor.get());
            }
        }
        else {
            Protocol.sendMessage(
                ch,
                createPlainTextResponseMessage(
                    ResponseStatus.QUEUE_NOT_FOUND,
                    request.getRequestId(),
                    request.getTopics(),
                    "Offer rejected! The queue does not exist."),
                compressor,
                encryptor.get());
        }
    }

    private void handlePoll(final Message request) throws InterruptedException {
        final long timeout = request.getTimeout();
        final String queueName = request.getQueueName();
        final IpcQueue<Message> queue = p2pQueues.get(queueName);
        if (queue != null) {
            while(true) {
                final Message msg = timeout < 0
                                        ? queue.poll()
                                        : queue.poll(timeout, TimeUnit.MILLISECONDS);
                if (msg == null) {
                    Protocol.sendMessage(
                        ch,
                        createPlainTextResponseMessage(
                            ResponseStatus.QUEUE_EMPTY,
                            request.getRequestId(),
                            request.getTopics(),
                            "Poll rejected! The queue is empty."),
                        compressor,
                        encryptor.get());
                    return;
                }
                else if (msg.hasExpired()) {
                    // discard expired message -> try next message from the queue
                    errorBuffer.offer(
                        new Error(
                            String.format(
                                "Discarded expired message (request-id: %s)" +
                                "polling from queue '%s'!",
                                msg.getRequestId(),
                                queueName),
                            request));
                   continue;
                }
                else {
                    Protocol.sendMessage(
                        ch,
                        msg.withType(MessageType.RESPONSE, true)
                           .withResponseStatus(ResponseStatus.OK),
                        compressor,
                        encryptor.get());
                    return;
                }
            }
        }
        else {
            Protocol.sendMessage(
                ch,
                createPlainTextResponseMessage(
                    ResponseStatus.QUEUE_NOT_FOUND,
                    request.getRequestId(),
                    request.getTopics(),
                    "Poll rejected! The queue does not exist."),
                compressor,
                encryptor.get());
        }
    }

    private void handleTcpServerRequest(final Message request) {
        if ("tcp-server/status".equals(request.getTopic())) {
            Protocol.sendMessage(
                    ch,
                    getTcpServerStatus(),
                    compressor,
                    encryptor.get());
        }
        else if ("tcp-server/thread-pool-statistics".equals(request.getTopic())) {
            Protocol.sendMessage(
                    ch,
                    getTcpServerThreadPoolStatistics(),
                    compressor,
                    encryptor.get());
        }
        else if ("tcp-server/error".equals(request.getTopic())) {
            Protocol.sendMessage(
                    ch,
                    getTcpServerNextError(),
                    compressor,
                    encryptor.get());
        }
        else {
            Protocol.sendMessage(
                ch,
                createPlainTextResponseMessage(
                    ResponseStatus.BAD_REQUEST,
                    request.getRequestId(),
                    request.getTopics(),
                    "Unknown tcp server request topic. \n"
                      + "Valid topics are:\n"
                      + "  • tcp-server/status\n"
                      + "  • tcp-server/thread-pool-statistics\n"
                      + "  • tcp-server/error\n"),
                compressor,
                encryptor.get());
        }
    }

    private void handleDiffieHellmanKeyExchange(final Message request) {
        if (encryptor.get().isActive()) {
            Protocol.sendMessage(
                    ch,
                    createPlainTextResponseMessage(
                       ResponseStatus.DIFFIE_HELLMAN_NAK,
                       null,
                       Topics.of("dh"),
                       "Error: Diffie-Hellman keys already exchanged!"),
                    Compressor.off(),
                    Encryptor.off());
        }
        else {
            try {
                final String clientPublicKey = request.getText();
                encryptor.set(Encryptor.aes(dhKeys.generateSharedSecret(clientPublicKey)));

                // send the server's public key back
                Protocol.sendMessage(
                        ch,
                        createPlainTextResponseMessage(
                           ResponseStatus.DIFFIE_HELLMAN_ACK,
                           null,
                           Topics.of("dh"),
                           dhKeys.getPublicKeyBase64()),
                        Compressor.off(),
                        Encryptor.off());
            }
            catch(Exception ex) {
                Protocol.sendMessage(
                        ch,
                        createPlainTextResponseMessage(
                           ResponseStatus.DIFFIE_HELLMAN_NAK,
                           null,
                           Topics.of("dh"),
                           "Failed to exchange Diffie-Hellman keys! Reason: " + ex.getMessage()),
                        Compressor.off(),
                        Encryptor.off());
            }
        }
    }

    private void handleInvalidRequestType(final Message request) throws InterruptedException {
        if (mode == State.Request_Response) {
            if (request.isOneway()) {
                // oneway request -> cannot send and error message back
                errorBuffer.offer(
                        new Error(
                                "Bad request type '" + request.getType() + "'! "
                                    + "Cannot send error response for oneway request!",
                                request));
                statistics.incrementDiscardedResponseCount();
            }
            else {
                Protocol.sendMessage(
                    ch,
                    createPlainTextResponseMessage(
                       ResponseStatus.BAD_REQUEST,
                       null,
                       request.getTopics(),
                       "Bad request type: " + request.getType().name()),
                    compressor,
                    encryptor.get());
            }
        }
        else {
            errorBuffer.offer(new Error(
                "Bad request type '" + request.getType() + "'! "
                    + "Cannot send error response for channel in publish mode!",
                request));
            statistics.incrementDiscardedPublishCount();
        }
    }

    private void handleRequestTooLarge(final Message request) throws InterruptedException {
        if (mode == State.Request_Response) {
            if (request.isOneway()) {
                // oneway request -> cannot send and error message back
                errorBuffer.offer(new Error(
                    "Request too large! Cannot send error response for oneway request!",
                    request));
                statistics.incrementDiscardedResponseCount();
            }
            else {
                // return error: message to large
                sendTooLargeMessageResponse(request);
            }
        }
        else {
            statistics.incrementDiscardedPublishCount();
        }
    }

    private void sendTooLargeMessageResponse(final Message request) {
        Protocol.sendMessage(
            ch,
            createTooLargeErrorMessageResponse(request),
            compressor,
            encryptor.get());
    }

    private Message getTcpServerStatus() {
        return createJsonResponseMessage(
                   ResponseStatus.OK,
                   null,
                   "tcp-server/status",
                   new JsonBuilder()
                           .add("running", server.isRunning())
                           .add("mode", mode.name())
                           .add("connection_count", statistics.getConnectionCount())
                           .add("message-count", statistics.getMessageCount())
                           .add("publish-count", statistics.getPublishCount())
                           .add("response-discarded-count", statistics.getDiscardedResponseCount())
                           .add("publish-discarded-count", statistics.getDiscardedPublishCount())
                           .add("subscription-client-count", subscriptions.getClientSubscriptionCount())
                           .add("subscription-topic-count", subscriptions.getTopicSubscriptionCount())
                           .add("publish-queue-capacity", publishQueueCapacity)
                           .add("p2p-queue-count", p2pQueues.size())
                           .add("error-queue-capacity", ERROR_QUEUE_CAPACITY)
                           .add("message-size-min", TcpServer.MESSAGE_LIMIT_MIN)
                           .add("message-size-max", maxMessageSize.get())
                           .add("compression-cutoff-size", server.getCompressCutoffSize())
                           .add("encryption", encryptor.get().isActive())
                           .toJson(false));
    }

    private Message getTcpServerNextError() {
        try {
            final Error err = errorBuffer.poll();
            if (err == null) {
                return createJsonResponseMessage(
                        ResponseStatus.OK,
                        null,
                        "tcp-server/error",
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
                        "tcp-server/error",
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
                    "tcp-server/error",
                    new JsonBuilder()
                            .add("status", "temporarily_unavailable")
                            .toJson(false));
        }
    }

    private Message getTcpServerThreadPoolStatistics() {
        final VncMap statistics = serverThreadPoolStatistics.get();

        return createJsonResponseMessage(
                ResponseStatus.OK,
                null,
                "tcp-server/thread-pool-statistics",
                Json.writeJson(statistics, false));
    }

    private static Message createJsonResponseMessage(
            final ResponseStatus status,
            final String requestID,
            final String topic,
            final String json
    ) {
        return new Message(
                requestID,
                MessageType.RESPONSE,
                status,
                true,
                Message.EXPIRES_NEVER,
                Topics.of(topic),
                "application/json",
                "UTF-8",
                toBytes(json, "UTF-8"));
    }

//    private static Message createPlainTextResponseMessage(
//            final ResponseStatus status,
//            final String requestID,
//            final String topic,
//            final String text
//    ) {
//        return new Message(
//                requestID,
//                MessageType.RESPONSE,
//                status,
//                true,
//                Topics.of(topic),
//                "text/plain",
//                "UTF-8",
//                toBytes(text, "UTF-8"));
//    }

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
                true,
                Message.EXPIRES_NEVER,
                topics,
                "text/plain",
                "UTF-8",
                toBytes(text, "UTF-8"));
    }

    private Message createTooLargeErrorMessageResponse(final Message request) {
        return createPlainTextResponseMessage(
                ResponseStatus.BAD_REQUEST,
                request.getRequestId(),
                request.getTopics(),
                String.format(
                        "The message (%d bytes) is too large! The limit is at %d bytes.",
                        request.getData().length,
                        maxMessageSize));
    }


    private static boolean isRequestMsg(final Message msg) {
        return msg.getType() == MessageType.REQUEST;
    }

    private static boolean isRequestSubscribe(final Message msg) {
        return msg.getType() == MessageType.SUBSCRIBE;
    }


    private static boolean isRequestUnsubscribe(final Message msg) {
        return msg.getType() == MessageType.UNSUBSCRIBE;
    }

    private static boolean isRequestPublish(final Message msg) {
        return msg.getType() == MessageType.PUBLISH;
    }

    private static boolean isRequestOffer(final Message msg) {
        return msg.getType() == MessageType.OFFER;
    }

    private static boolean isRequestPoll(final Message msg) {
        return msg.getType() == MessageType.POLL;
    }

    private static boolean isRequestDiffieHellman(final Message msg) {
        return msg.getType() == MessageType.DIFFIE_HELLMAN_KEY_REQUEST;
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }


    private static enum State { Request_Response, Publish, Terminated };

    public static final int ERROR_QUEUE_CAPACITY = 50;

    private State mode = State.Request_Response;

    private final TcpServer server;
    private final SocketChannel ch;
    private final Function<IMessage,IMessage> handler;
    private final AtomicLong maxMessageSize;
    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final ServerStatistics statistics;
    private final Supplier<VncMap> serverThreadPoolStatistics;

    // compression
    private final Compressor compressor;

    // encryption
    private final DiffieHellmanKeys dhKeys;
    private final AtomicReference<Encryptor> encryptor = new AtomicReference<>(Encryptor.off());

    // queues
    private final IpcQueue<Error> errorBuffer;
    private final IpcQueue<Message> publishQueue;
    private final Map<String, IpcQueue<Message>> p2pQueues;
}
