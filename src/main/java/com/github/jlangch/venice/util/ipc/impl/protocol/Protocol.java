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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;


// +===================================+
// |             Header                |
// +-----------------------------------+
// |                                   |
// |   32 bytes                        |
// |   ✗ encrypted                     |
// |   ✗ compressed                    |
// |   Fields:                  bytes  |
// |     • magic chars              2  |
// |     • protocol version         4  |
// |     • compressed               1  |
// |     • encrypted                1  |
// |     • timestamp                8  |
// |     • expiresAt                8  |
// |     • timeout                  8  |
// +===================================+
// |         Payload Meta Data         |
// +-----------------------------------+
// |                                   |
// |   40-200 bytes                    |
// |   ✓ encrypted                     |
// |   ✗ compressed                    |
// |   Fields:                  bytes  |
// |     • oneway                   1  |
// |     • durable                  1  |
// |     • subscription reply       1  |
// |     • request id           2 + n  |
// |     • message type             2  |
// |     • response status          2  |
// |     • queue name           2 + n  |
// |     • replyTo queue name   2 + n  |
// |     • topics               2 + n  |
// |     • mimetype             2 + n  |
// |     • charset              2 + n  |
// |     • id                      16  |
// +===================================+
// |           Payload Data            |
// +-----------------------------------+
// |                                   |
// |   n bytes (binary)                |
// |   ✓ encrypted                     |
// |   ✓ compressed                    |
// +===================================+

public class Protocol {

    public Protocol(final boolean cacheBuffers) {
        this.cacheBuffers = cacheBuffers;
        this.cache = cacheBuffers
                        ? new ConcurrentHashMap<Integer,ByteBuffer>()
                        : null;
    }


    // ------------------------------------------------------------------------
    // Message send/receive
    // ------------------------------------------------------------------------

    public void sendMessage(
            final ByteChannel ch,
            final Message message,
            final Compressor compressor,
            final Encryptor encryptor,
            final long messageSizeLimit
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(message);
        Objects.requireNonNull(compressor);
        Objects.requireNonNull(encryptor);

        // Check message size limit
        final byte[] payloadMetaData = PayloadMetaData.encode(new PayloadMetaData(message));
        final byte[] payloadMsgData = message.getData();
        final int totalMsgSize = HEADER_SIZE + payloadMetaData.length + payloadMsgData.length;
        if (messageSizeLimit > 0 && totalMsgSize > messageSizeLimit) {
            throw new IpcException(String.format(
                    "The message size exceeds the configured limit!"
                    + "\nLimit:                %d"
                    + "\nMessage total:        %d"
                    + "\nMessage header:       %d"
                    + "\nMessage payload meta: %d"
                    + "\nMessage payload data: %d",
                    messageSizeLimit, totalMsgSize, HEADER_SIZE, payloadMetaData.length, payloadMsgData.length));

        }

        final boolean isCompressData = compressor.needsCompression(message.getData());

        // [1] header
        //     if encryption is active the header is processed as AAD
        //     (added authenticated data) with the encrypted payload meta
        //      data, so any tampering if the header data is detected!
        final ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
        // 2 bytes magic chars
        header.put((byte)'v');
        header.put((byte)'n');
        // 4 bytes (integer) protocol version
        header.putInt(PROTOCOL_VERSION);
        // 1 byte compressed data flag
        header.put(toByte(isCompressData));
        // 1 byte encrypted data flag
        header.put(toByte(encryptor.isActive()));
        // 8 bytes (long) timestamp
        header.putLong(message.getTimestamp());
        // 8 bytes (long) expiresAt
        header.putLong(message.getExpiresAt());
        // 8 bytes (long) timeout
        header.putLong(message.getTimeout());
        header.flip();
        ByteChannelIO.writeFully(ch, header);

        // [2] payload meta data (optionally encrypt)
        if (encryptor.isActive()) {
            final byte[] headerAAD = header.array(); ; // GCM AAD: added authenticated data
            final byte[] metaData = encryptor.encrypt(payloadMetaData, headerAAD);
            ByteChannelIO.writeFrame(ch, ByteBuffer.wrap(metaData));
        }
        else {
            ByteChannelIO.writeFrame(ch, ByteBuffer.wrap(payloadMetaData));
        }

        // [3] payload data (optionally compress and encrypt)
        byte[] payloadData = encryptor.encrypt(
                                compressor.compress(payloadMsgData, isCompressData));
        ByteChannelIO.writeFrame(ch, ByteBuffer.wrap(payloadData));
    }

    public Message receiveMessage(
            final ByteChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(compressor);
        Objects.requireNonNull(encryptor);

        try {
            // [1] header
            final ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
            final int bytesRead = ch.read(header);
            if (bytesRead < 0) {
                throw new EofException("Failed to read data from channel, channel EOF reached!");
            }

            header.flip();

            // [1a] parse header (magic bytes)
            final byte magic1 = header.get();
            final byte magic2 = header.get();

            if (magic1 != 'v' || magic2 != 'n') {
                throw new IpcException(
                        "Received unknown message (bad magic bytes)!");
            }

            // [1b] parse header (version field)
            final int version = header.getInt();

            if (version != PROTOCOL_VERSION) {
                throw new IpcException(
                        "Received message with unsupported protocol version " + version + "!");
            }

            // [1c] parse header (data fields)
            final boolean isCompressedData = toBool(header.get());
            final boolean isEncryptedData = toBool(header.get());
            final long timestamp = header.getLong();
            final long expiresAt = header.getLong();
            final long timeout = header.getLong();

            if (!isEncryptedData && encryptor.isActive()) {
                // prevent malicious clients
                throw new IpcException(
                        "Received an unencrypted message but encryption is mandatory!");
            }

            // [2] payload meta data (maybe encrypted)
            final ByteBuffer payloadMetaFrame = ByteChannelIO.readFrame(ch);
            final byte[] headerAAD = header.array(); // GCM AAD: added authenticated data
            final PayloadMetaData payloadMeta = PayloadMetaData.decode(
                                                    encryptor.decrypt(
                                                        payloadMetaFrame.array(),
                                                        headerAAD,
                                                        isEncryptedData));

            // [3] payload data (maybe compressed and encrypted)
            final ByteBuffer payloadFrame = ByteChannelIO.readFrame(ch);
            final byte[] payloadData = compressor.decompress(
                                            encryptor.decrypt(
                                                payloadFrame.array(),
                                                isEncryptedData),
                                            isCompressedData);

            return new Message(
                    payloadMeta.getId(),
                    payloadMeta.getRequestId(),
                    payloadMeta.getType(),
                    payloadMeta.getResponseStatus(),
                    payloadMeta.isOneway(),
                    payloadMeta.isDurable(),
                    payloadMeta.isSubscriptionReply(),
                    payloadMeta.getQueueName(),
                    payloadMeta.getReplyToQueueName(),
                    timestamp,
                    expiresAt,
                    timeout,
                    payloadMeta.getTopics(),
                    payloadMeta.getMimetype(),
                    payloadMeta.getCharset(),
                    payloadData);
        }
        catch(IOException ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new EofException("Failed to read data from channel, channel was closed!", ex);
            }
            else {
                throw new IpcException("Failed to read data from channel!", ex);
            }
        }
    }

    public Map<String,Integer> messageSize(final Message message) {
        Objects.requireNonNull(message);

        final Compressor compressor = new Compressor(0);

        final int payloadMetaData = PayloadMetaData.encode(new PayloadMetaData(message)).length;
        final int payloadMsgData = message.getData().length;
        final int payloadMsgDataCompressed = compressor.compress(message.getData(), true).length;

        final Map<String,Integer> info = new HashMap<>();
        info.put("header",       HEADER_SIZE);
        info.put("payload-meta", payloadMetaData);
        info.put("payload-data", payloadMsgData);
        info.put("payload-data-compressed", payloadMsgDataCompressed);
        info.put("total",        HEADER_SIZE + payloadMetaData + payloadMsgData);
        return info;
    }



    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static boolean toBool(final byte n) {
        if (n == 0) return false;
        else if (n == 1) return true;
        else throw new IpcException("Illegal IPC message boolean value");
    }

    private static byte toByte(final boolean b) {
        return b ? (byte)1 : (byte)0;
    }


    private final static int PROTOCOL_VERSION = 1;

    private final static int HEADER_SIZE = 32;


    private final boolean cacheBuffers;
    private final ConcurrentHashMap<Integer,ByteBuffer> cache;
}
