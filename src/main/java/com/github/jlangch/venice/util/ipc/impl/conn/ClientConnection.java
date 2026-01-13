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

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
import com.github.jlangch.venice.util.ipc.AcknowledgeMode;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.protocol.Protocol;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.JsonBuilder;


public class ClientConnection implements AutoCloseable {

    public ClientConnection(
            final String host,
            final int port,
            final boolean useEncryption,
            final AcknowledgeMode ackMode,
            final String userName,
            final String password
    ) {
        this.host = StringUtil.isBlank(host) ? "127.0.0.1" : host;
        this.port = port;
        this.ackMode = ackMode;

        final String serverAddress = this.host + "/" + this.port;

        // [1] Open the connection to the server
        try {
            channel = SocketChannel.open(
                        new InetSocketAddress(this.host, this.port));
        }
        catch(Exception ex) {
            throw new IpcException(
                    "Failed to open connection to the server " + serverAddress + "!",
                    ex);
        }

        // [2] Start the executor after the connection has been established
        try {
            mngdExecutor = new ManagedCachedThreadPoolExecutor("venice-ipc-client-pool", 10);
        }
        catch(Exception ex) {
            IO.safeClose(channel);
            throw new IpcException("Failed to start client connection!", ex);
        }

        try {
            dhKeys = DiffieHellmanKeys.create();

            // [3] Request the client configuration from the server
            try {
                // config
                final VncMap config = getClientConfiguration(channel, ackMode);
                final long srv_cutoffSize     = getLong(config, "compress-cutoff-size", -1);
                final long srv_maxMessageSize = getLong(config, "max-msg-size", Messages.MESSAGE_LIMIT_MAX);
                final boolean srv_encryption  = getBoolean(config, "encrypt", false);
                final boolean srv_permitQMgmt = getBoolean(config, "permit-client-queue-mgmt", false);
                final long srv_heartbeatInterval = getLong(config, "heartbeat-interval", 0);
                final boolean srv_authentication = getBoolean(config, "authentication", false);

                maxMessageSize = srv_maxMessageSize;
                permitClientQueueMgmt = srv_permitQMgmt;
                heartbeatInterval = srv_heartbeatInterval;
                compressor = new Compressor(srv_cutoffSize);
                encrypt = useEncryption || srv_encryption;
                authentication = srv_authentication;

                // Note: The server is enforcing the encryption if activated.
                //       The client cannot disregard it. Trying to do so results
                //       in an aborted connection!
            }
            catch(Exception ex) {
                throw new IpcException("Failed to get client config from server!", ex);
            }

            // [4] Establish encryption through Diffie-Hellman key exchange
            if (encrypt) {
                try {
                    final String serverPublicKey = diffieHellmanKeyExchange(channel, dhKeys);
                    encryptor = Encryptor.aes(dhKeys.generateSharedSecret(serverPublicKey));
                }
                catch(Exception ex) {
                    throw new IpcException("Failed on Diffie-Hellman key exchange!", ex);
                }
            }
            else {
                encryptor = Encryptor.off();
            }

            // [5] Authentication
            if (authentication) {
                if (userName == null || password == null) {
                   throw new IpcException(
                           "The IPC server requires authentication! Please pass "
                           + "a user name and a password for opening an IPC client!");
                }

                // authenticate
                final boolean ok = authenticate(channel, userName, password);
                if (!ok) {
                    throw new IpcException("Authentication failure! Bad user credentials!");
                }
            }
            else if (userName != null || password != null){
                throw new IpcException("Authentication is not enabled! User credentials are not required!");
            }

            // [6] Start the channel message listener
            listener = new ChannelMessageListener(channel, compressor, encryptor);
            mngdExecutor
               .getExecutor()
               .submit(listener);

            // [7] Ready for the music
            opened.set(true);

            // [8] Start heartbeat timer
            if (heartbeatInterval > 0) {
                heartbeatTimer.set(new Timer("venice-ipc-heartbeat"));
                heartbeatTimer.get().scheduleAtFixedRate(
                    wrapTask(() -> sendHeartbeat()),
                    HEARTBEAT_START_DELAY,
                    heartbeatInterval * 1000L);
            }
        }
        catch(Exception ex) {
            opened.set(false);

            stopHeartbeatTimer();
            mngdExecutor.shutdownNow();
            IO.safeClose(channel);

            throw ex;
        }
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public AcknowledgeMode getAcknowledgeMode() {
        return ackMode;
    }

    public boolean isOpen() {
        return opened.get();
    }

    public boolean isEncrypted() {
        return encrypt;
    }

    public boolean isPermitClientQueueMgmt() {
        return permitClientQueueMgmt;
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
       return listener.getMessageReceiveCount();
    }

    public VncMap getThreadPoolStatistics() {
        return mngdExecutor.info();
    }

    public void addSubscriptionHandler(
            final Set<String> topics,
            final Consumer<IMessage> handler
    ) {
        listener.addSubscriptionHandler(topics, handler);
    }

    public void removeSubscriptionHandler(
            final Set<String> topics
    ) {
       listener.removeSubscriptionHandler(topics);
    }

    public IMessage send(final IMessage msg, final long timeoutMillis) {
        Objects.requireNonNull(msg);

        if (!isOpen()) {
            throw new IpcException("Client connection is closed! Cannot send the message!");
        }

        if (!channel.isOpen()) {
            throw new IpcException("Server connection is closed! Cannot send the message!");
        }

        if (listener.isEOF()) {
            throw new IpcException(String.format(
                        "EOF on server connection (err: %b, irq: %b)! Cannot send the message!",
                        listener.isERR(),
                        listener.isIRQ()));
        }

        final long start = System.currentTimeMillis();
        final long limit = start + timeoutMillis;

        try {
            // sending the request message atomically preventing any other thread to
            // interfere with this message send
            if (sendSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    Protocol.sendMessage(channel, (Message)msg, compressor, encryptor, maxMessageSize);
                    messageSentCount.incrementAndGet();

                    if (msg.isOneway()) {
                        return null;
                    }

                    final long remainingTimeout = limit - System.currentTimeMillis();

                    return listener.readResponse(
                            (Message)msg,
                            Math.max(0, remainingTimeout),
                            opened);
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
                    "This client conection is not open! Cannot send the message!");
        }

        return mngdExecutor
                .getExecutor()
                .submit(() -> send(msg, timeoutMillis));
    }

    /**
     * Closes the connection
     */
    @Override
    public void close() {
        if (opened.compareAndSet(true, false)) {
            stopHeartbeatTimer();

            // wait max 500ms for tasks to be completed
            mngdExecutor.shutdown();
            mngdExecutor.awaitTermination(500);

            IO.safeClose(channel);
        }
    }


    private Message sendDirect(
            final Message msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor,
            final long timeoutMillis
    ) {
        if (opened.get()) {
            throw new IpcException(
                    "ClientConnection::sendDirect must only be called during initialization!");
        }

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
        if (opened.get()) {
            throw new IpcException(
                    "ClientConnection::sendDirect must only be called during initialization!");
        }

        try {
            // sending the request message and receiving the response
            // must be atomic otherwise request and response can be mixed
            // in multi-threaded environments
            if (sendSemaphore.tryAcquire(10L, TimeUnit.SECONDS)) {
                try {
                    Protocol.sendMessage(ch, msg, compressor, encryptor, -1);

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

    private VncMap getClientConfiguration(
            final SocketChannel ch,
            final AcknowledgeMode ackMode
    ) {
        final IMessage response = sendDirect(
                                    createConfigRequestMessage(ackMode),
                                    ch,
                                    Compressor.off(),
                                    Encryptor.off(),
                                    CLIENT_CONFIG_TIMEOUT);
        if (response.getResponseStatus() != ResponseStatus.OK) {
            throw new IpcException(
                    "Failed to get client config from server. Server answered with "
                    + response.getResponseStatus() + "!");
        }

        return (VncMap)((Message)response).getVeniceData();
    }

    private String diffieHellmanKeyExchange(
            final SocketChannel ch,
            final DiffieHellmanKeys dhKeys
    ) {
        final Message m = createDiffieHellmanRequestMessage(dhKeys.getPublicKeyBase64());

        // exchange the client's and the server's public key
        final Message response = sendDirect(m, ch, Compressor.off(), Encryptor.off(), DIFFIE_HELLMAN_TIMEOUT);

        if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_ACK) {
            // successfully exchanged keys, return the server's public key
             return response.getText();
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

    private boolean authenticate(
            final SocketChannel ch,
            final String userName,
            final String password
    ) {
        final IMessage response = sendDirect(
                                    createAuthenticationMessage(userName, password),
                                    ch,
                                    Compressor.off(),
                                    encryptor,
                                    AUTHENTICATE_TIMEOUT);
        return response.getResponseStatus() == ResponseStatus.OK;
    }

    private void sendHeartbeat() {
        if (isOpen()) {
            try {
                final IMessage response = send(createHeartbeatMessage(), HEARTBEAT_TIMEOUT);
                if (response.getResponseStatus() != ResponseStatus.OK) {
                    throw new RuntimeException("Failed Hearbeat");
                }
            }
            catch(Exception ex) {
                close();
            }
        }
    }

    private void stopHeartbeatTimer() {
        final Timer t = heartbeatTimer.get();
        if (t != null) {
            t.cancel();
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
                Messages.EXPIRES_NEVER,
                Topics.of(Messages.TOPIC_DIFFIE_HELLMANN),
                "text/plain",
                "UTF-8",
                toBytes(clientPublicKey, "UTF-8"));
    }

    private static Message createConfigRequestMessage(final AcknowledgeMode ackMode) {
        final String payload = new JsonBuilder()
                                    .add("ackMode", ackMode.name())
                                    .toJson(false);

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
                Messages.EXPIRES_NEVER,
                Messages.NO_TIMEOUT,
                Topics.of("client-config"),
                "application/json",
                "UTF-8",
                toBytes(payload, "UTF-8"));
    }

    private static Message createAuthenticationMessage(
            final String userName,
            final String password
    ) {
        return new Message(
                null,
                MessageType.AUTHENTICATION,
                ResponseStatus.NULL,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                Topics.of(Messages.TOPIC_AUTHENTICATION),
                "text/plain",
                "UTF-8",
                toBytes(userName + "\n" + password, "UTF-8"));
    }

    private static Message createHeartbeatMessage() {
        return new Message(
                null,
                MessageType.HEARTBEAT,
                ResponseStatus.NULL,
                false,
                false,
                false,
                Messages.EXPIRES_NEVER,
                Topics.of(Messages.TOPIC_HEARTBEAT),
                "text/plain",
                "UTF-8",
                toBytes("", "UTF-8"));
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

    private static TimerTask wrapTask(final Runnable r) {
        return new TimerTask() {
            @Override
            public void run() {
               r.run();
            }
         };
    }


    private static final long HEARTBEAT_START_DELAY  = 3_000;
    private static final long HEARTBEAT_TIMEOUT      = 5_000;
    private static final long CLIENT_CONFIG_TIMEOUT  = 2_000;
    private static final long AUTHENTICATE_TIMEOUT   = 2_000;
    private static final long DIFFIE_HELLMAN_TIMEOUT = 2_000;

    private final String host;
    private final int port;
    private final AcknowledgeMode ackMode;

    private final SocketChannel channel;

    private final AtomicBoolean opened = new AtomicBoolean(false);

    private final AtomicReference<Timer> heartbeatTimer = new  AtomicReference<>();

    private final Semaphore sendSemaphore = new Semaphore(1);

    private final long maxMessageSize;
    private final boolean permitClientQueueMgmt;
    private final long heartbeatInterval;
    private final boolean authentication;

    // compression
    private final Compressor compressor;

    // encryption
    private final DiffieHellmanKeys dhKeys;
    private final boolean encrypt;
    private final Encryptor encryptor;

    // statistics
    private final AtomicLong messageSentCount = new AtomicLong(0L);

    private final ChannelMessageListener listener;

    // thread pool
    private final ManagedCachedThreadPoolExecutor mngdExecutor;
}
