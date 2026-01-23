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
 * |   8 bytes                         |
 * |   ✗ encrypted                     |
 * |   ✗ compressed                    |
 * |                                   |
 * |   Fields:                  bytes  |
 * |     • magic chars              2  |
 * |     • protocol version         4  |
 * |     • compressed               1  |
 * |     • encrypted                1  |
 * |                                   |
 * +===================================+
 * | Payload Meta Data                 |
 * +-----------------------------------+
 * |   40-200 bytes                    |
 * |   ✓ encrypted                     |
 * |   ✗ compressed                    |
 * |                                   |
 * |   Frame len:             4 bytes  |
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
 * |                                   |
 * +===================================+
 * | Payload Data                      |
 * +-----------------------------------+
 * |   ✓ encrypted                     |
 * |   ✓ compressed                    |
 * |                                   |
 * |   Frame len:             4 bytes  |
 * |   Binary Data:           n bytes  |
 * |                                   |
 * +===================================+
 * </pre>
 *
 * Benchmarks with ByteArrayStreamChannel (MacBook Air M2, 24GB, MacOS 26):
 *
 * <p>Without protocol optimizations for small messages
 * <pre>
 * IPC Protocol: Sent 500'000 1KB messages:     0.41 us / msg
 * IPC Protocol: Received 500'000 1KB messages: 0.51 us / msg
 * </pre>
 *
 * <p>With protocol optimizations for small messages
 * <pre>
 * IPC Protocol: Sent 500000 1KB messages: 0.88 us / msg
 * IPC Protocol: Received 500000 1KB messages: 0.37 us / msg
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

        final boolean compress = compressor.needsCompression(message.getData());
        final boolean encrypt  = encryptor.isActive();

        // Raw message data
        final byte[] headerData = new byte[Header.SIZE];
        final byte[] payloadMetaDataRaw = PayloadMetaData.encode(new PayloadMetaData(message));
        final byte[] payloadDataRaw = message.getData();

        // Header
        final ByteBuffer headerBuf = ByteBuffer.wrap(headerData);
        Header.write(new Header(PROTOCOL_VERSION, compress, encrypt), headerBuf);
        headerBuf.flip();

        // Compression (optional)
        final byte[] payloadDataZip = compressor.compress(payloadDataRaw, compress);

        // effective payload data optionally encrypted
        final byte[] payloadMetaDataEff;
        final byte[] payloadDataEff;

        validateRawMessageSizeLimit(
            payloadMetaDataRaw.length,
            payloadDataRaw.length,
            messageSizeLimit);


        // [1] Optionally encrypt payload meta data
        //     if encryption is active the header is processed as AAD
        //     (added authenticated data) with the encrypted payload meta
        //      data, so any tampering if the header data is detected!
        final byte[] headerAAD = headerBuf.array();  // GCM AAD: added authenticated data
        payloadMetaDataEff = encryptor.encrypt(payloadMetaDataRaw, headerAAD);

        // [2] Optionally encrypt payload data
       payloadDataEff = encryptor.encrypt(payloadDataZip);

        // Performance optimization:
        //
        //   Every channel write costs an expensive context switch, this is very costly
        //   with small messages!
        //   => for small messages (< 16KB) aggregate all parts into a single buffer
        //      and just do a single channel write!
        final long messageTotalSize = headerData.length
                                        + 4 + payloadMetaDataEff.length
                                        + 4 + payloadDataEff.length;
        if (messageTotalSize < 16 * KB) {
            final byte[] buf = new byte[16 * KB];

            // Aggregate to a single buffer
            final ByteBuffer b = ByteBuffer.wrap(buf, 0, (int)messageTotalSize);
            b.put(headerBuf.array());
            b.putInt(payloadMetaDataEff.length);
            b.put(payloadMetaDataEff);
            b.putInt(payloadDataEff.length);
            b.put(payloadDataEff);
            b.flip();

            // Write message to channel,
            ByteChannelIO.writeFully(ch, b);
        }
        else {
            // Write message to channel,
            ByteChannelIO.writeFully(ch, headerBuf);
            ByteChannelIO.writeFrame(ch, ByteBuffer.wrap(payloadMetaDataEff));
            ByteChannelIO.writeFrame(ch, ByteBuffer.wrap(payloadDataEff));
        }
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
                throw new EofException(
                        "Failed to read data from channel, channel EOF reached!");
            }

            headerBuf.flip();
            final Header header = Header.read(headerBuf);

            validateProtocolVersion(header);
            validateEncryptionMode(header, encryptor);

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

            return payloadMeta.toMessage(payloadData);
        }
        catch(IOException ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new EofException(
                        "Failed to read data from channel, channel was closed!",
                        ex);
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


    private void validateRawMessageSizeLimit(
            final int payloadMetaDataSize,
            final int payloadDataSize,
            final long messageSizeLimit
    ) {
        // Check raw message size limit
        final int totalMsgSize = Header.SIZE + payloadMetaDataSize + payloadDataSize;
        if (messageSizeLimit > 0 && totalMsgSize > messageSizeLimit) {
            throw new IpcException(String.format(
                    "The message size exceeds the configured limit!"
                    + "\nLimit:                %d"
                    + "\nMessage total:        %d"
                    + "\nMessage header:       %d"
                    + "\nMessage payload meta: %d"
                    + "\nMessage payload data: %d",
                    messageSizeLimit,
                    totalMsgSize,
                    Header.SIZE,
                    payloadMetaDataSize,
                    payloadDataSize));
        }
    }

    private void validateProtocolVersion(final Header header) {
        if (header.getVersion() != PROTOCOL_VERSION) {
            throw new IpcException(
                    "Received message with unsupported protocol version "
                    + header.getVersion() + "!");
        }
    }

    private void validateEncryptionMode(final Header header, final Encryptor encryptor) {
        if (!header.isEncrypted() && encryptor.isActive()) {
            // prevent malicious clients
            throw new IpcException(
                    "Received an unencrypted message but encryption is mandatory!");
        }
    }


    private final static int PROTOCOL_VERSION = 1;

    private final static int KB = 1024;
}
