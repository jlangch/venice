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
package com.github.jlangch.venice.util.ipc.impl.conn;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.TimeoutException;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;


public class ChannelMessageListener implements Runnable {

    public ChannelMessageListener(
            final SocketChannel channel,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        this.channel = channel;
        this.compressor = compressor;
        this.encryptor = encryptor;
    }


    public boolean isEOF() {
        return eof.get();
    }

    public boolean isERR() {
        return err.get();
    }

    public boolean isIRQ() {
        return irq.get();
    }

    public long getMessageReceiveCount() {
        return messageReceiveCount.get();
    }

    public long getDiscardedMessageSubscriptionCount() {
        return discardedMessageSubscriptionCount.get();
    }

    public Message readResponse(
            final Message request,
            final long timeoutMillis,
            final AtomicBoolean connectionOpen
    ) throws InterruptedException {
        final long start = System.currentTimeMillis();
        final long limit = start + timeoutMillis;

        // poll the response from the receive queue
        while(connectionOpen.get()) {
            // if a response is ready consume immediately
            Message response = poll();
            if (response != null && request.hasSameId(response)) {
                return response; // the response matches the request
            }

            if (timeoutMillis <= 0) {
                throw new TimeoutException("Timeout on receiving IPC message response.");
            }

            // check server status
            if (!channel.isOpen() || isEOF()) {
               break;
            }

            // check response in 80ms steps, to react faster if client or server has closed!!
            final long timeout = Math.min(80, limit - System.currentTimeMillis());

            if (timeout >= 0) {
                response = poll(timeout);
                if (response == null) {
                    continue;
                }
                else if (request.hasSameId(response)) {
                    return response; // the response matches the request
                }
                else {
                    continue;  // discard out-of-order response
                }
            }
            else {
                final String errMsg = String.format(
                        "Timeout after %dms on receiving IPC message response.",
                        System.currentTimeMillis() - start);
                System.err.println(errMsg);
                throw new TimeoutException(errMsg);
            }
        }

        final String errMsg = "EOF while receiving IPC message response.";
        System.err.println(errMsg);
        throw new EofException(errMsg);
    }


    public Message poll() {
        return queue.poll();
    }

    public Message poll(final long timeout) throws InterruptedException {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }


    public void addSubscriptionHandler(
            final Set<String> topics,
            final Consumer<IMessage> handler
    ) {
        Objects.requireNonNull(topics);

        if (handler != null) {
            topics.forEach(t -> subscriptionHandlers.put(t, handler));
        }
        else {
            removeSubscriptionHandler(topics);
        }
    }

    public void removeSubscriptionHandler(
            final Set<String> topics
    ) {
        Objects.requireNonNull(topics);

        topics.forEach(t -> subscriptionHandlers.remove(t));
    }


    @Override
    public void run() {
        eof.set(false);
        err.set(false);
        irq.set(false);

        while(true) {
            if (Thread.interrupted()) {
                irq.set(true);
                break;
            }

            try {
                final Message msg = Protocol.receiveMessage(channel, compressor, encryptor);

                if (msg != null) {
                    if (msg.isSubscriptionReply()) {
                        final Consumer<IMessage> handler = getSubscriptionHandler(msg);
                        if (handler != null) {
                            try {
                                // deliver subscription reply msg via subscription handler
                                handler.accept(msg);
                            }
                            catch(Exception ignore) {}
                        }
                        else {
                            // can not deliver without subscription handler
                            discardedMessageSubscriptionCount.incrementAndGet();
                        }
                    }
                    else {
                        // regular response message for a request
                        queue.offer(msg);
                        messageReceiveCount.incrementAndGet();
                    }
                }
            }
            catch(EofException ex) {
                // channel was closed
                eof.set(true);
                break;
            }
            catch(Exception ex) {
                // channel error
                err.set(true);
                break;
           }
        }

        eof.set(true);
    }


    private Consumer<IMessage> getSubscriptionHandler(final Message msg) {
        final String topic = msg.getTopic();

        return subscriptionHandlers.get(topic);
    }


    private final LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>(100);
    private final AtomicBoolean eof = new AtomicBoolean(false);
    private final AtomicBoolean err = new AtomicBoolean(false);
    private final AtomicBoolean irq = new AtomicBoolean(false);

    private final AtomicLong messageReceiveCount = new AtomicLong(0L);
    private final AtomicLong discardedMessageSubscriptionCount = new AtomicLong(0L);

    private final SocketChannel channel;
    private final Compressor compressor;
    private final Encryptor encryptor;

    private final Map<String,Consumer<IMessage>> subscriptionHandlers = new ConcurrentHashMap<>();
}
