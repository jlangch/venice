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

import static com.github.jlangch.venice.util.ipc.Status.REQUEST;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_ONE_WAY;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_PUBLISH;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_START_SUBSCRIPTION;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_BAD_REQUEST;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_HANDLER_ERROR;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_OK;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.github.jlangch.venice.util.ipc.Message;
import com.github.jlangch.venice.util.ipc.TcpServer;


public class TcpServerConnection implements IPublisher, Runnable {

    public TcpServerConnection(
            final TcpServer server,
            final SocketChannel ch,
            final Function<Message,Message> handler,
            final Subscriptions subscriptions,
            final AtomicLong serverMessageCount,
            final AtomicLong serverPublishCount,
            final AtomicLong serverDiscardedPublishCount
    ) {
        this.server = server;
        this.ch = ch;
        this.handler = handler;
        this.subscriptions = subscriptions;
        this.publishQueue = new LinkedBlockingQueue<Message>(50);
        this.serverMessageCount = serverMessageCount;
        this.serverPublishCount = serverPublishCount;
        this.serverDiscardedPublishCount = serverDiscardedPublishCount;
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
                    // process a publish message
                    mode = processPublications();
                }
                else {
                    break;
                }
            }
        }
        catch(Exception ex) {
            // when client closed the connection -> java.io.IOException: Broken pipe
            // -> quit
        }
        finally {
            subscriptions.removePublisher(this);
            IO.safeClose(ch);
        }
    }

    @Override
    public void publish(final Message msg) {
        try {
            // enqueue the message to publish it as soon as possible
            // to this channels's client
            publishQueue.offer(msg, 1, TimeUnit.SECONDS);
        }
        catch(Exception ignore) {
            serverDiscardedPublishCount.incrementAndGet();
            // there is no dead letter queue yet
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

        // send an error back if the request message is not a request
        if (!(isRequestMsg(request)
                || isRequestOneWayMsg(request)
                || isRequestPublish(request)
                || isRequestStartSubscription(request))
        ) {
            Protocol.sendMessage(
                ch,
                Message.text(
                   RESPONSE_BAD_REQUEST,
                   request.getTopic(),
                   "text/plain",
                   "UTF-8",
                   "Bad request status: " + request.getStatus().name()));

            return State.Request_Response;
        }

        // [2] Handle the request
        if ("server/status".equals(request.getTopic())) {
            Protocol.sendMessage(ch, getServerStatus());
            return State.Request_Response;
        }
        else if (isRequestPublish(request)) {
            // the client sent a message to be published to all subscribors
            // of the message's topic
            return handlePublish(request);
        }
        else if (isRequestStartSubscription(request)) {
            // the client wants to listen for subribed messages
            return handleSubscribe(request);
        }
        else {
            // client sent a normal message request, send the response
            // back
            final Message response = handleRequest(request);

            if (!server.isRunning()) {
                return State.Terminated;  // this server was closed
            }

            // [3] Send response
            if (response != null) {
                Protocol.sendMessage(ch, response);
            }

            return State.Request_Response;
        }
    }

    private State processPublications() throws InterruptedException {
        final Message msg = publishQueue.poll(5, TimeUnit.SECONDS);

        if (msg != null) {
            serverPublishCount.incrementAndGet();

            Protocol.sendMessage(ch, msg.withStatus(REQUEST));
        }

        return State.Publish;
    }

    private Message handleRequest(final Message request) {
        try {
            final Message response = handler.apply(request);

            if (isRequestMsg(request)) {
                return response == null
                        ?  Message.text(
                                RESPONSE_OK,
                                request.getTopic(),
                                "text/plain",
                                "UTF-8",
                                "")
                        : response;
            }
            else if (isRequestOneWayMsg(request)) {
                return null; // do not reply on one-way messages
            }
            else {
                return null; // already handled by caller, should not reach here
            }
        }
        catch(Exception ex) {
            if (isRequestMsg(request)) {
                // send error response
                return Message.text(
                         RESPONSE_HANDLER_ERROR,
                         request.getTopic(),
                         "text/plain",
                         "UTF-8",
                         ExceptionUtil.printStackTraceToString(ex));
            }
            else if (isRequestOneWayMsg(request)) {
               return null; // do not reply on one-way messages
            }
            else {
               return null; // already handled by caller, should not reach here
            }
        }
    }


    private State handleSubscribe(final Message request) {
        // register subscription
        subscriptions.addSubscription(request.getTopic(), this);

        // acknowledge the subscription
        Protocol.sendMessage(
            ch,
            Message.text(
                RESPONSE_OK,
                request.getTopic(),
                "text/plain",
                "UTF-8",
                ""));

        // switch in publish mode for this connections
        return State.Publish;
    }

    private State handlePublish(final Message request) {
        // asynchronously publish to all subscriptions
        subscriptions.publish(request);

        // acknowledge the publish
        Protocol.sendMessage(
            ch,
            Message.text(
                RESPONSE_OK,
                request.getTopic(),
                "text/plain",
                "UTF-8",
                "message has been enqued to publish"));

        // switch in publish mode for this connections
        return State.Request_Response;
    }

    private Message getServerStatus() {
        return Message.text(
                   RESPONSE_OK,
                   "server/status",
                   "application/json",
                   "UTF-8",
                   "{\"running\": " + server.isRunning() + ", " +
                    "\"mode\": \"" + mode.name()  + "\", " +
                    "\"message_count\": " + serverMessageCount.get()  + ", " +
                    "\"publish_count\": " + serverPublishCount.get()  + ", " +
                    "\"discarded_publish_count\": " + serverDiscardedPublishCount.get()  + ", " +
                    "\"subscription_client_count\": " + subscriptions.getClientSubscriptionCount()  + ", " +
                    "\"subscription_topic_count\": " + subscriptions.getTopicSubscriptionCount()  + ", " +
                    "\"publish_queue_size\": " + publishQueue.size() +
                   "}");
    }


    private static boolean isRequestMsg(final Message msg) {
        return msg.getStatus() == REQUEST;
    }

    private static boolean isRequestOneWayMsg(final Message msg) {
        return msg.getStatus() == REQUEST_ONE_WAY;
    }

    private static boolean isRequestStartSubscription(final Message msg) {
        return msg.getStatus() == REQUEST_START_SUBSCRIPTION;
    }

    private static boolean isRequestPublish(final Message msg) {
        return msg.getStatus() == REQUEST_PUBLISH;
    }


    private static enum State { Request_Response, Publish, Terminated };


    private State mode = State.Request_Response;

    private final TcpServer server;
    private final SocketChannel ch;
    private final Function<Message,Message> handler;
    private final Subscriptions subscriptions;
    private final AtomicLong serverMessageCount;
    private final AtomicLong serverPublishCount;
    private final AtomicLong serverDiscardedPublishCount;

    private final LinkedBlockingQueue<Message> publishQueue;
}
