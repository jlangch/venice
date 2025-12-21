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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.TimeoutException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.threadpool.ManagedCachedThreadPoolExecutor;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class ClientConnection implements Closeable {

    public ClientConnection(
            final String host,
            final int port,
            final boolean useEncryption
    ) {
        this.host = StringUtil.isBlank(host) ? "127.0.0.1" : host;
        this.port = port;

        final String serverAddress = this.host + "/" + this.port;

        // [1] Open the connection to the server
        try {
            channel = SocketChannel.open(
                        new InetSocketAddress(this.host, this.port));
        }
        catch(Exception ex) {
            throw new IpcException(
                    "Failed to open TcpClient connection to server " + serverAddress + "!",
                    ex);
        }

        dhKeys = DiffieHellmanKeys.create();

        // [2] Start the executor after the connection has been established
        mngdExecutor = new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);

        // [3] Request the client configuration from the server
        try {
            final VncMap config = getClientConfiguration(channel);

            maxMessageSize = getLong(config, "max-msg-size", Message.MESSAGE_LIMIT_MAX);
            compressor = new Compressor(getLong(config, "compress-cutoff-size", -1));
            encrypt = useEncryption                              // client side encrypt
                      || getBoolean(config, "encrypt", false);   // server side encrypt
        }
        catch(Exception ex) {
            mngdExecutor.shutdownNow();
            IO.safeClose(channel);
            throw new IpcException("Failed to get client config from server!", ex);
        }

        // [4] Establish encryption through Diffie-Hellman key exchange
        if (encrypt) {
            try {
                encryptor = diffieHellmanKeyExchange(channel);
            }
            catch(Exception ex) {
                mngdExecutor.shutdownNow();
                IO.safeClose(channel);
                throw new IpcException(
                        "Failed to open TcpClient for server " + serverAddress +
                        "! Diffie-Hellman key exchange error!",
                        ex);
            }
        }
        else {
            encryptor = Encryptor.off();
        }

        // [5] Start the channel message listener
        mngdExecutor
           .getExecutor()
           .submit(() -> backgroundChannelMessageListener());

        opened.set(true);
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isOpen() {
        return opened.get();
    }

    public boolean isEncrypted() {
        return encrypt;
    }

    public long getCompressCutoffSize() {
        return compressor.cutoffSize();
    }

    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    public long getMessageSendCount() {
       return messageSentCount.get();
    }

    public long getMessageReceiveCount() {
       return messageReceiveCount.get();
    }

    public VncMap getThreadPoolStatistics() {
        return mngdExecutor.info();
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
            topics.forEach(t -> subscriptionHandlers.remove(t));
        }
    }

    public void removeSubscriptionHandler(
            final Set<String> topics
    ) {
        Objects.requireNonNull(topics);

        topics.forEach(t -> subscriptionHandlers.remove(t));
    }

    public IMessage send(final IMessage msg, final long timeoutMillis) {
        Objects.requireNonNull(msg);

        if (!isOpen()) {
            throw new IpcException("Client connection is closed! Cannot send the message!");
        }

        if (!channel.isOpen()) {
            throw new IpcException("Server connection is closed! Cannot send the message!");
        }

        if (receiveQueueEOF.get()) {
            throw new IpcException(String.format(
                        "EOF on server connection (err: %b, irq: %b)! Cannot send the message!",
                        receiveQueueERR.get(),
                        receiveQueueIRQ.get()));
        }

        final long start = System.currentTimeMillis();
        final long limit = start + timeoutMillis;

        try {
            // sending the request message atomically preventing any other thread to
            // interfere with this message send
            if (sendSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    Protocol.sendMessage(channel, (Message)msg, compressor, encryptor);
                    messageSentCount.incrementAndGet();

                    if (msg.isOneway()) {
                        return null;
                    }

                    final long sendDone = System.currentTimeMillis();

                    // poll the response from the receive queue
                    while(isOpen()) {
                    	// if a response is ready consume immediately
                        Message response = (Message)receiveQueue.poll();
                        if (msg.hasSameId(response)) {
                            return response; // the response matches the request
                        }

                        // check server status
                        if (!channel.isOpen() || receiveQueueEOF.get()) {
                           break;
                        }

                        // check response in 80ms steps, to react faster if client or server has closed!!
                        final long timeout = Math.min(80, limit - System.currentTimeMillis());

                        if (timeout >= 0) {
                            response = (Message)receiveQueue.poll(timeout, TimeUnit.MILLISECONDS);
                            if (response == null) {
                                continue;
                            }
                            else if (msg.hasSameId(response)) {
                                return response; // the response matches the request
                            }
                            else {
                                continue;  // discard out-of-order response
                            }
                        }
                        else {
                            final String errMsg = String.format(
                                    "Timeout after %dms (send took %dms) on receiving IPC message response.",
                                    System.currentTimeMillis() - start,
                                    sendDone - start);
                            System.err.println(errMsg);
                            throw new TimeoutException(errMsg);
                        }
                    }

                    final String errMsg = "EOF while receiving IPC message response.";
                    System.err.println(errMsg);
                    throw new EofException(errMsg);
                }
                finally {
                    sendSemaphore.release();
                }
            }
            else {
                final String errMsg = String.format(
                            "Timeout after %dms on sending IPC message. "
                                + "Could not aquire send semaphore in time!",
                            System.currentTimeMillis() - start);
                System.err.println(errMsg);
                throw new TimeoutException(errMsg);
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while trying to send an IPC message");
        }
    }

    public Future<IMessage> sendAsync(final IMessage msg, final long timeoutMillis) {
        Objects.requireNonNull(msg);

        if (!isOpen()) {
            throw new IpcException(
                    "This TcpClient conection is not open! Cannot send the message!");
        }

        return mngdExecutor
                .getExecutor()
                .submit(() -> send(msg, timeoutMillis));
    }

    /**
     * Closes the connection
     */
    @Override
    public void close() throws IOException {
        if (opened.compareAndSet(true, false)) {

            // wait max 500ms for tasks to be completed
            mngdExecutor.shutdown();
            mngdExecutor.awaitTermination(500);

            IO.safeClose(channel);
        }
    }


    private void backgroundChannelMessageListener() {
        receiveQueueEOF.set(false);
        receiveQueueERR.set(false);
        receiveQueueIRQ.set(false);

        while(true) {
            if (Thread.interrupted()) {
                receiveQueueIRQ.set(true);
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
                        receiveQueue.offer(msg);
                        messageReceiveCount.incrementAndGet();
                    }
                }
            }
            catch(EofException ex) {
                // channel was closed
                receiveQueueEOF.set(true);
                break;
            }
            catch(Exception ex) {
                // channel error
                receiveQueueERR.set(true);
                break;
           }
        }

        receiveQueueEOF.set(true);
    }

    private Message sendDirect(
            final Message msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor,
            final long timeoutMillis
    ) {
        return deref(
                mngdExecutor
                        .getExecutor()
                        .submit(() -> sendDirect(msg, ch, compressor, encryptor)),
                    timeoutMillis);
    }

    private Message sendDirect(
            final Message msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        try {
            // sending the request message and receiving the response
            // must be atomic otherwise request and response can be mixed
            // in multi-threaded environments
            if (sendSemaphore.tryAcquire(10L, TimeUnit.SECONDS)) {
                try {
                    Protocol.sendMessage(ch, msg, compressor, encryptor);

                    final Message response = Protocol.receiveMessage(ch, compressor, encryptor);

                    return response;
                }
                finally {
                    sendSemaphore.release();
                }
            }
            else {
                throw new TimeoutException(
                        "Timeout while trying to send an IPC message. "
                        + "Could not aquire send semaphore!");
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while trying to send an IPC message");
        }
    }

    private Consumer<IMessage> getSubscriptionHandler(final IMessage msg) {
        final String topic = msg.getTopic();

        return subscriptionHandlers.get(topic);
    }

    private VncMap getClientConfiguration(final SocketChannel ch) {
        final IMessage response = sendDirect(
                                    createConfigRequestMessage(),
                                    ch,
                                    Compressor.off(),
                                    Encryptor.off(),
                                    2_000);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to get client config from server. Server answered with "
                    + response.getResponseStatus() + "!");
        }

        return (VncMap)((Message)response).getVeniceData();
    }

    private Encryptor diffieHellmanKeyExchange(final SocketChannel ch) {
        final Message m = createDiffieHellmanRequestMessage(dhKeys.getPublicKeyBase64());

        // exchange the client's and the server's public key
        final Message response = sendDirect(m, ch, Compressor.off(), Encryptor.off(), 2_000);

        if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_ACK) {
            // successfully exchanged keys
            final String serverPublicKey = response.getText();
            return Encryptor.aes(dhKeys.generateSharedSecret(serverPublicKey));
        }
        else if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_NAK) {
            // server rejects key exchange
            final String errText = response.getText();
            throw new IpcException("Error: The server rejected the Diffie-Hellman key exchange! " + errText);
        }
        else {
            throw new IpcException("Failed to process Diffie-Hellman key exchange!");
        }
    }


    private static Message createDiffieHellmanRequestMessage(final String clientPublicKey) {
        return new Message(
                null,
                MessageType.DIFFIE_HELLMAN_KEY_REQUEST,
                ResponseStatus.NULL,
                false,
                false,
                false,
                Message.EXPIRES_NEVER,
                Topics.of("dh"),
                "text/plain",
                "UTF-8",
                toBytes(clientPublicKey, "UTF-8"));
    }

    private static Message createConfigRequestMessage() {
        return new Message(
                null,
                null,
                MessageType.CLIENT_CONFIG,
                ResponseStatus.NULL,
                false,
                false,
                false,
                null,
                null,
                System.currentTimeMillis(),
                Message.EXPIRES_NEVER,
                Message.NO_TIMEOUT,
                Topics.of("client-config"),
                "text/plain",
                "UTF-8",
                new byte[0]);
    }

    private static Message deref(Future<Message> future, final long timeout) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(java.util.concurrent.TimeoutException ex) {
            throw new TimeoutException("Timeout while waiting for IPC response.");
        }
        catch(ExecutionException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof VncException) {
                throw (VncException)cause;
            }
            else {
                throw new VncException("Error in IPC call", cause);
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while waiting for IPC response.");
        }
    }

    private static byte[] toBytes(final String s, final String charset) {
        return s.getBytes(Charset.forName(charset));
    }

    private static long getLong(final VncMap map, final String entryName, final long defaulValue) {
        return Coerce.toVncLong(
                map.get(
                   new VncKeyword(entryName),
                   new VncLong(defaulValue))).toJavaLong();
    }

    private static boolean getBoolean(final VncMap map, final String entryName, final boolean defaulValue) {
        return VncBoolean.isTrue(
                map.get(
                     new VncKeyword(entryName),
                     VncBoolean.of(defaulValue)));
    }


    private final String host;
    private final int port;

    private final SocketChannel channel;

    private final AtomicBoolean opened = new AtomicBoolean(false);

    private final Semaphore sendSemaphore = new Semaphore(1);

    private final long maxMessageSize;

    // compression
    private final Compressor compressor;

    // encryption
    private final DiffieHellmanKeys dhKeys;
    private final boolean encrypt;
    private final Encryptor encryptor;

    // subscriptions
    private final Map<String,Consumer<IMessage>> subscriptionHandlers = new ConcurrentHashMap<>();

    // statistics
    private final AtomicLong messageSentCount = new AtomicLong(0L);
    private final AtomicLong messageReceiveCount = new AtomicLong(0L);
    private final AtomicLong discardedMessageSubscriptionCount = new AtomicLong(0L);

    private final LinkedBlockingQueue<IMessage> receiveQueue = new LinkedBlockingQueue<>(100);
    private final AtomicBoolean receiveQueueEOF = new AtomicBoolean(false);
    private final AtomicBoolean receiveQueueERR = new AtomicBoolean(false);
    private final AtomicBoolean receiveQueueIRQ = new AtomicBoolean(false);

    // thread pool
    private final ManagedCachedThreadPoolExecutor mngdExecutor;
}
