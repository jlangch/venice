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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
        // [1] Open the connection to the server
        this.host = StringUtil.isBlank(host) ? "127.0.0.1" : host;
        this.port = port;

        final String serverAddress = this.host + "/" + this.port;

        try {
            channel = SocketChannel.open(
                        new InetSocketAddress(this.host, this.port));
        }
        catch(Exception ex) {
            throw new VncException(
                    "Failed to open TcpClient connection to server " + serverAddress + "!",
                    ex);
        }

        dhKeys = DiffieHellmanKeys.create();

        // [2] request the config from the server
        try {
            final IMessage response = deref(
                                        sendAsync(
                                            createConfigRequestMessage(),
                                            channel,
                                            Compressor.off(),
                                            Encryptor.off()),
                                        2_000);
            if (response.getResponseStatus() != ResponseStatus.OK) {
                throw new VncException(
                        "Failed to get client config from server. Server answered with "
                        + response.getResponseStatus() + "!");
            }

            // handle config values
            final VncMap config = (VncMap)((Message)response).getVeniceData();
            maxMessageSize = getLong(config, "max-msg-size", Message.MESSAGE_LIMIT_MAX);
            compressor = new Compressor(getLong(config, "compress-cutoff-size", -1));
            encrypt = useEncryption                              // client side encrypt
                      || getBoolean(config, "encrypt", false);   // server side encrypt
        }
        catch(Exception ex) {
            IO.safeClose(channel);
            throw new VncException("Failed to get client config from server!", ex);
        }

        // [3] Establish encryption through Diffie-Hellman key exchange
        if (encrypt) {
            try {
                encryptor = diffieHellmanKeyExchange(channel);
            }
            catch(Exception ex) {
                IO.safeClose(channel);
                throw new VncException(
                        "Failed to open TcpClient for server " + serverAddress +
                        "! Diffie-Hellman key exchange error!",
                        ex);
            }
        }
        else {
            encryptor = Encryptor.off();
        }

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


    public Future<IMessage> sendAsync(final IMessage msg) {
        Objects.requireNonNull(msg);

        if (!isOpen()) {
            throw new VncException(
                    "This TcpClient conection is not open! Cannot send the message!");
        }

        return sendAsync(msg, channel, compressor, encryptor);
    }

    public IMessage sendAtomically(final IMessage msg) {
        Objects.requireNonNull(msg);

        if (!isOpen()) {
            throw new VncException(
                    "This TcpClient conection is not open! Cannot send the message!");
        }

        return sendAtomically(msg, channel, compressor, encryptor);
    }

    /**
     * Closes the connection
     */
    @Override
    public void close() throws IOException {
        if (opened.compareAndSet(true, false)) {
            try {
                mngdExecutor.shutdownNow();
            }
            catch(Exception ignore) { }

            IO.safeClose(channel);
        }
    }


    private Future<IMessage> sendAsync(
            final IMessage msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        final Callable<IMessage> task = () -> sendAtomically(msg, ch, compressor, encryptor);

        return mngdExecutor
                .getExecutor()
                .submit(task);
    }

    private IMessage sendAtomically(
            final IMessage msg,
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        try {
            final boolean auditCount = msg.getType() != MessageType.DIFFIE_HELLMAN_KEY_REQUEST
                                       && msg.getType() != MessageType.CLIENT_CONFIG;

            // sending the request message and receiving the response
            // must be atomic otherwise request and response can be mixed
            // in multi-threaded environments
            if (sendSemaphore.tryAcquire(120L, TimeUnit.SECONDS)) {
                try {
                    Protocol.sendMessage(ch, (Message)msg, compressor, encryptor);
                    if (auditCount) {
                        messageSentCount.incrementAndGet();
                    }

                    if (msg.isOneway()) {
                        return null;
                    }
                    else {
                        final Message response = Protocol.receiveMessage(ch, compressor, encryptor);
                        if (auditCount) {
                            messageReceiveCount.incrementAndGet();
                        }
                        return response;
                    }
                }
                finally {
                    sendSemaphore.release();
                }
            }
            else {
               throw new com.github.jlangch.venice.TimeoutException(
                    "Timeout while trying to send an IPC message.");
            }
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while trying to send an IPC message");
        }
    }

    private Encryptor diffieHellmanKeyExchange(final SocketChannel ch) {
        final IMessage m = createDiffieHellmanRequestMessage(dhKeys.getPublicKeyBase64());

        // exchange the client's and the server's public key
        final Message response = (Message)deref(
                                    sendAsync(m, ch, Compressor.off(), Encryptor.off()),
                                    2_000);

        if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_ACK) {
            // successfully exchanged keys
            final String serverPublicKey = response.getText();
            return Encryptor.aes(dhKeys.generateSharedSecret(serverPublicKey));
        }
        else if (response.getResponseStatus() == ResponseStatus.DIFFIE_HELLMAN_NAK) {
            // server rejects key exchange
            final String errText = response.getText();
            throw new VncException("Error: The server rejected the Diffie-Hellman key exchange! " + errText);
        }
        else {
            throw new VncException("Failed to process Diffie-Hellman key exchange!");
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

    private static IMessage deref(Future<IMessage> future, final long timeout) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch(VncException ex) {
            throw ex;
        }
        catch(TimeoutException ex) {
            throw new com.github.jlangch.venice.TimeoutException(
                    "Timeout while waiting for IPC response.");
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

    // statistics
    private final AtomicLong messageSentCount = new AtomicLong(0L);
    private final AtomicLong messageReceiveCount = new AtomicLong(0L);

    // thread pool
    private final ManagedCachedThreadPoolExecutor mngdExecutor =
            new ManagedCachedThreadPoolExecutor("venice-tcpclient-pool", 10);
}
