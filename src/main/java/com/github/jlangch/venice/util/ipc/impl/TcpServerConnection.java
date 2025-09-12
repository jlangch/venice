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
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.json.VncJsonWriter;
import com.github.jlangch.venice.nanojson.JsonAppendableWriter;
import com.github.jlangch.venice.nanojson.JsonWriter;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.TcpServer;


public class TcpServerConnection implements IPublisher, Runnable {

    public TcpServerConnection(
            final TcpServer server,
            final SocketChannel ch,
            final Function<IMessage,IMessage> handler,
            final AtomicLong maxMessageSize,
            final Subscriptions subscriptions,
            final int publishQueueCapacity,
            final AtomicLong serverMessageCount,
            final AtomicLong serverPublishCount,
            final AtomicLong serverDiscardedPublishCount,
            final Supplier<VncMap> serverThreadPoolStatistics
    ) {
        this.server = server;
        this.ch = ch;
        this.handler = handler;
        this.maxMessageSize = maxMessageSize;
        this.subscriptions = subscriptions;
        this.publishQueueCapacity = publishQueueCapacity;
        this.publishQueue = new LinkedBlockingQueue<Message>(publishQueueCapacity);
        this.serverMessageCount = serverMessageCount;
        this.serverPublishCount = serverPublishCount;
        this.serverDiscardedPublishCount = serverDiscardedPublishCount;
        this.serverThreadPoolStatistics = serverThreadPoolStatistics;
    }

    @Override
    public void run() {
        try {
            while(mode != State.Terminated && server.isRunning() && ch.isOpen()) {
                if (mode == State.Request_Response) {
                    // process a request/response message
                    mode = processRequestResponse();
                }
                else if (mode == State.Publish) {
                    // process a publish message if available for this connection
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
            publishQueue.offer(msg, 1, TimeUnit.SECONDS);
        }
        catch(Exception ignore) {
            // there is no dead letter queue yet, just count the
            // discarded messages
            serverDiscardedPublishCount.incrementAndGet();
        }
    }

    private State processRequestResponse() {
        // [1] receive message
        final Message request = Protocol.receiveMessage(ch);
        if (request == null) {
            return State.Terminated; // client closed connection
        }

        serverMessageCount.incrementAndGet();

        if (!server.isRunning()) {
            return State.Terminated;  // this server was closed
        }

        // send an error back if the received message is not a request
        if (!(isRequestMsg(request)
              || isRequestPublish(request)
              || isRequestSubscribe(request))
        ) {
            if (mode == State.Request_Response) {
                Protocol.sendMessage(
                    ch,
                    createTextResponseMessage(
                       ResponseStatus.BAD_REQUEST,
                       request.getTopic(),
                       "text/plain",
                       "Bad request type: " + request.getType().name()));

                return State.Request_Response;
            }
            else {
                this.serverDiscardedPublishCount.incrementAndGet();
                return mode;
            }
        }

        if (request.getData().length > maxMessageSize.get()) {
            if (mode == State.Request_Response && !request.isOneway()) {
                // return error: message to large
                sendTooLargeMessageResponse(request);
                return State.Request_Response;
            }
            else {
                this.serverDiscardedPublishCount.incrementAndGet();
                return mode;
            }
        }

        // [2] Handle the request
        if ("server/status".equals(request.getTopic())) {
            // process a server status request
            Protocol.sendMessage(ch, getServerStatus());
            return State.Request_Response;
        }
        else if ("server/thread-pool-statistics".equals(request.getTopic())) {
            // process a server status request
            Protocol.sendMessage(ch, getServerThreadPoolStatistics());
            return State.Request_Response;
        }
        else if (isRequestPublish(request)) {
            // the client sent a message to be published to all subscribers
            // of the message's topic
            handlePublish(request);
            return State.Request_Response;
        }
        else if (isRequestSubscribe(request)) {
            // the client wants to to subscribe a topic
            handleSubscribe(request);

            // switch to publish mode for this connections
            return State.Publish;
        }
        else if (isRequestMsg(request)) {
            // client sent a normal message request, send the response
            // back
            // call the server handler always (also for oneway requests)
            final Message response = handleRequest(request);

            if (!server.isRunning()) {
                return State.Terminated;  // this server was closed
            }

            // [3] Send response
            if (response != null && !request.isOneway()) {
                Protocol.sendMessage(ch, response);
            }

            return State.Request_Response;
        }
        else {
            // should not get here
            return State.Request_Response;
        }
    }

    private State processPublication() throws InterruptedException {
        final Message msg = publishQueue.poll(5, TimeUnit.SECONDS);

        if (msg != null) {
            serverPublishCount.incrementAndGet();
            Protocol.sendMessage(ch, msg.withType(MessageType.REQUEST));
        }

        return State.Publish;
    }

    private Message handleRequest(final Message request) {
        try {
            final IMessage response = handler.apply(request);

            if (request.isOneway()) {
                return null; // do not reply on one-way messages
            }
            else {
                if (response == null) {
                    // create a standard response
                    return createTextResponseMessage(
                              ResponseStatus.OK,
                              request.getTopic(),
                              "text/plain",
                              "");
                }
                else {
                    return ((Message)response)
                                .withType(MessageType.RESPONSE)
                                .withResponseStatus(ResponseStatus.OK);
                }
            }
        }
        catch(Exception ex) {
            if (request.isOneway()) {
                return null; // do not reply on one-way messages
            }
            else {
                // send error response
                return createTextResponseMessage(
                         ResponseStatus.HANDLER_ERROR,
                         request.getTopic(),
                         "text/plain",
                         ExceptionUtil.printStackTraceToString(ex));
            }
        }
    }


    private void handleSubscribe(final Message request) {
        final String[] topicsArr = request.getTopic().split(",");

        final HashSet<String> topics = new HashSet<>(Arrays.asList(topicsArr));

        // register subscription
        subscriptions.addSubscription(topics, this);

        // acknowledge the subscription
        Protocol.sendMessage(
            ch,
            createTextResponseMessage(
                ResponseStatus.OK,
                request.getTopic(),
                "text/plain",
                "subscribed to the topic"));
    }

    private void handlePublish(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        Protocol.sendMessage(
            ch,
            createTextResponseMessage(
                ResponseStatus.OK,
                request.getTopic(),
                "text/plain",
                "Message has been enqued to publish"));
    }

    private void sendTooLargeMessageResponse(final Message request) {
        Protocol.sendMessage(
            ch,
            createTooLargeMessageResponse(request));
    }

    private Message createTooLargeMessageResponse(final Message request) {
        return createTextResponseMessage(
                ResponseStatus.BAD_REQUEST,
                request.getTopic(),
                "text/plain",
                String.format(
                        "The message (%dB) is too large! The limit is at %dB",
                        request.getData().length,
                        maxMessageSize));
    }

    private Message getServerStatus() {
        return createTextResponseMessage(
                   ResponseStatus.OK,
                   "server/status",
                   "application/json",
                   "{\"running\": " + server.isRunning() + ", " +
                    "\"mode\": \"" + mode.name() + "\", " +
                    "\"message_count\": " + serverMessageCount.get() + ", " +
                    "\"publish_count\": " + serverPublishCount.get() + ", " +
                    "\"publish_discarded_count\": " + serverDiscardedPublishCount.get() + ", " +
                    "\"subscription_client_count\": " + subscriptions.getClientSubscriptionCount() + ", " +
                    "\"subscription_topic_count\": " + subscriptions.getTopicSubscriptionCount() + ", " +
                    "\"publish_queue_capacity\": " + publishQueueCapacity + ", " +
                    "\"publish_queue_size\": " + publishQueue.size() + ", " +
                    "\"message_size_min\": " + TcpServer.MESSAGE_LIMIT_MIN + ", " +
                    "\"message_size_max\": " + maxMessageSize.get() +
                   "}");
    }

    private Message getServerThreadPoolStatistics() {
        final VncMap statistics = serverThreadPoolStatistics.get();

        final StringBuilder sb = new StringBuilder();
        final JsonAppendableWriter writer = JsonWriter.indent("  ").on(sb);
        new VncJsonWriter(writer, false).write(statistics).done();

        return createTextResponseMessage(
                ResponseStatus.OK,
                "server/thread-pool-statistics",
                "application/json",
                sb.toString());
    }


    private static Message createTextResponseMessage(
            final ResponseStatus status,
            final String topic,
            final String mimetype,
            final String text
    ) {
        return new Message(
                MessageType.RESPONSE,
                status,
                false,
                topic,
                mimetype,
                "UTF-8",
                text.getBytes(Charset.forName("UTF-8")));
    }


    private static boolean isRequestMsg(final Message msg) {
        return msg.getType() == MessageType.REQUEST;
    }

    private static boolean isRequestSubscribe(final Message msg) {
        return msg.getType() == MessageType.SUBSCRIBE;
    }

    private static boolean isRequestPublish(final Message msg) {
        return msg.getType() == MessageType.PUBLISH;
    }


    private static enum State { Request_Response, Publish, Terminated };


    private State mode = State.Request_Response;

    private final TcpServer server;
    private final SocketChannel ch;
    private final Function<IMessage,IMessage> handler;
    private final AtomicLong maxMessageSize;
    private final Subscriptions subscriptions;
    private final int publishQueueCapacity;
    private final AtomicLong serverMessageCount;
    private final AtomicLong serverPublishCount;
    private final AtomicLong serverDiscardedPublishCount;
    private final Supplier<VncMap> serverThreadPoolStatistics;

    private final LinkedBlockingQueue<Message> publishQueue;
}
