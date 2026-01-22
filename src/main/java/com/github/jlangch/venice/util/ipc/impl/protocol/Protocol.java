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

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;


/**
 * Message wire format:
 *
 * <pre>
 * +===================================+
 * | Header                            |
 * +-----------------------------------+
 * |                                   |
 * |   8 bytes                         |
 * |   ✗ encrypted                     |
 * |   ✗ compressed                    |
 * |   Fields:                  bytes  |
 * |     • magic chars              2  |
 * |     • protocol version         4  |
 * |     • compressed               1  |
 * |     • encrypted                1  |
 * +===================================+
 * | Payload Meta Data                 |
 * +-----------------------------------+
 * |                                   |
 * |   40-200 bytes                    |
 * |   ✓ encrypted                     |
 * |   ✗ compressed                    |
 * |   Fields:                  bytes  |
 * |     • oneway                   1  |
 * |     • durable                  1  |
 * |     • subscription reply       1  |
 * |     • message type             2  |
 * |     • timestamp                8  |
 * |     • expiresAt                8  |
 * |     • timeout                  8  |
 * |     • response status          2  |
 * |     • request id           2 + n  |
 * |     • queue name           2 + n  |
 * |     • replyTo queue name   2 + n  |
 * |     • topics               2 + n  |
 * |     • mimetype             2 + n  |
 * |     • charset              2 + n  |
 * |     • id                      16  |
 * +===================================+
 * | Payload Data                      |
 * +-----------------------------------+
 * |                                   |
 * |   n bytes (binary)                |
 * |   ✓ encrypted                     |
 * |   ✓ compressed                    |
 * +===================================+
 * </pre>
 *
 * Benchmarks with ByteArrayStreamChannel (MacBook Air M2, 24GB, MacOS 26):
 *
 * <pre>
 * IPC Protocol: Sent 500'000 messages:     0.41 us / msg
 * IPC Protocol: Received 500'000 messages: 0.51 us / msg
 * </pre>
 *
 */
public class Protocol {

    public Protocol() {
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

        final byte[] headerData = new byte[Header.SIZE];
        final byte[] payloadMetaData = PayloadMetaData.encode(new PayloadMetaData(message));
        final byte[] payloadMsgData = message.getData();

        final int totalMsgSize = Header.SIZE + payloadMetaData.length + payloadMsgData.length;

        // Check message size limit
        if (messageSizeLimit > 0 && totalMsgSize > messageSizeLimit) {
            throw new IpcException(String.format(
                    "The message size exceeds the configured limit!"
                    + "\nLimit:                %d"
                    + "\nMessage total:        %d"
                    + "\nMessage header:       %d"
                    + "\nMessage payload meta: %d"
                    + "\nMessage payload data: %d",
                    messageSizeLimit, totalMsgSize, Header.SIZE, payloadMetaData.length, payloadMsgData.length));

        }

        final boolean isCompressData = compressor.needsCompression(message.getData());

        // [1] header
        //     if encryption is active the header is processed as AAD
        //     (added authenticated data) with the encrypted payload meta
        //      data, so any tampering if the header data is detected!
        final ByteBuffer headerBuf = ByteBuffer.wrap(headerData);
        Header.write(new Header(PROTOCOL_VERSION, isCompressData, encryptor.isActive()), headerBuf);
        headerBuf.flip();
        ByteChannelIO.writeFully(ch, headerBuf);

        // [2] payload meta data (optionally encrypt)
        if (encryptor.isActive()) {
            final byte[] headerAAD = headerBuf.array();  // GCM AAD: added authenticated data
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
            final ByteBuffer headerBuf = ByteBuffer.allocate(Header.SIZE);
            final int bytesRead = ch.read(headerBuf);
            if (bytesRead < 0) {
                throw new EofException("Failed to read data from channel, channel EOF reached!");
            }

            headerBuf.flip();
            final Header header = Header.read(headerBuf);

            if (header.getVersion() != PROTOCOL_VERSION) {
                throw new IpcException(
                        "Received message with unsupported protocol version " + header.getVersion() + "!");
            }

            if (!header.isEncrypted() && encryptor.isActive()) {
                // prevent malicious clients
                throw new IpcException(
                        "Received an unencrypted message but encryption is mandatory!");
            }

            // [2] payload meta data (maybe encrypted)
            final ByteBuffer payloadMetaFrame = ByteChannelIO.readFrame(ch);
            final byte[] headerAAD = headerBuf.array(); // GCM AAD: added authenticated data
            final byte[] payloadMetaRaw = payloadMetaFrame.array();
            final PayloadMetaData payloadMeta = PayloadMetaData.decode(
                                                    encryptor.decrypt(
                                                        payloadMetaRaw,
                                                        headerAAD,
                                                        header.isEncrypted()));

            // [3] payload data (maybe compressed and encrypted)
            final ByteBuffer payloadFrame = ByteChannelIO.readFrame(ch);
            final byte[] payloadDataRaw = payloadFrame.array();
            final byte[] payloadData = compressor.decompress(
                                            encryptor.decrypt(
                                                payloadDataRaw,
                                                header.isEncrypted()),
                                            header.isCompressed());

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
                    payloadMeta.getTimestamp(),
                    payloadMeta.getExpiresAt(),
                    payloadMeta.getTimeout(),
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
        info.put("header",       Header.SIZE);
        info.put("payload-meta", payloadMetaData);
        info.put("payload-data", payloadMsgData);
        info.put("payload-data-compressed", payloadMsgDataCompressed);
        info.put("total",        Header.SIZE + payloadMetaData + payloadMsgData);
        return info;
    }


    private final static int PROTOCOL_VERSION = 1;
}
