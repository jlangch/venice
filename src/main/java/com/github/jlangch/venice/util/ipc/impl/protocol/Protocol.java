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
 * |   16 bytes                        |
 * |   ✗ encrypted                     |
 * |   ✗ compressed                    |
 * |                                   |
 * |   Fields:                  bytes  |
 * |     • magic chars              2  |
 * |     • protocol version         4  |
 * |     • compressed¹⁾             1  |
 * |     • encrypted¹⁾              1  |
 * |     • payload meta len         4  |
 * |     • payload data len         4  |
 * |                                   |
 * +===================================+
 * | Payload Meta Data                 |
 * +-----------------------------------+
 * |   40-200 bytes                    |
 * |   ✓ encrypted                     |
 * |   ✗ compressed                    |
 * |                                   |
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
 * |     • destination name     2 + n  |
 * |     • replyTo queue name   2 + n  |
 * |     • subject              2 + n  |
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
 * |   Binary Data:           n bytes  |
 * |                                   |
 * +===================================+
 *
 * ¹⁾ Used for GCM AAD: added authenticated data
 *
 * </pre>
 *
 * Benchmarks with ByteArrayStreamChannel (MacBook Air M2, 24GB, MacOS 26):
 *
 * <p>With protocol optimizations for small messages
 * <pre>
 * IPC Protocol: Sent     500000 messages: 0.32 us / msg
 * IPC Protocol: Received 500000 messages: 0.45 us / msg
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
        final byte[] payloadMetaRaw = PayloadMetaData.encode(new PayloadMetaData(message));
        final byte[] payloadDataRaw = message.getData();

        validateRawMessageSizeLimit(
            payloadMetaRaw.length,
            payloadDataRaw.length,
            messageSizeLimit);

        // Compression (optional)
        final byte[] payloadDataZip = compressor.compress(payloadDataRaw, compress);


        // Note on encryption:
        //
        // AES-256 GCM detects tampered encrypted data and throws an exception
        // while decrypting it!
        //
        // The unencrypted header is protected by GCM AAD (added authenticated data)
        // Thus tampered header data will be detected and results in an exception.

        // [1] Optionally encrypt payload meta data
        //     if encryption is active the header is processed as AAD
        //     (added authenticated data) with the encrypted payload meta
        //     data, so any tampering if the header data is detected!
        final byte[] headerAAD = Header.aadData(compress, encrypt);  // GCM AAD: added authenticated data
        final byte[] payloadMetaEff = encryptor.encrypt(payloadMetaRaw, headerAAD);

        // [2] Optionally encrypt payload data
        final byte[] payloadDataEff = encryptor.encrypt(payloadDataZip);


        // Encode header
        final ByteBuffer headerBuf = ByteBuffer.wrap(headerData);
        Header.write(
                new Header(PROTOCOL_VERSION, compress, encrypt,
                           payloadMetaEff.length, payloadDataEff.length),
                headerBuf);
        headerBuf.flip();


        // Performance optimization:
        //
        // Every channel read/write costs an expensive context switch, this
        // is very costly with small messages!
        //
        // => for small messages (< 16KB) aggregate all parts into a
        //    single buffer and just do a single channel write!
        final long messageTotalSize = headerData.length
                                        + payloadMetaEff.length
                                        + payloadDataEff.length;

        if (OPT_SMALL_BUF_SND && (messageTotalSize < SMALL_BUF_16KB)) {
            final IBufferPool cachedBuffer = messageTotalSize < SMALL_BUF_8KB
                                                    ? cachedBuffer_8KB
                                                    : cachedBuffer_16KB;

            //final byte[] buf = new byte[SMALL_BUF_16KB];  // OS friendly buffer 16KB
            final byte[] buf = cachedBuffer.checkout();

            // Aggregate to a single buffer (gathering write)
            final ByteBuffer b = ByteBuffer.wrap(buf, 0, (int)messageTotalSize);
            b.put(headerBuf);
            b.put(payloadMetaEff);
            b.put(payloadDataEff);
            b.flip();

            // Write message to channel (1 write)
            ByteChannelIO.writeFully(ch, b);

            cachedBuffer.checkin(buf);
        }
        else {
            // Write message to channel (3 writes)
            ByteChannelIO.writeFully(ch, headerBuf);
            ByteChannelIO.writeFully(ch, ByteBuffer.wrap(payloadMetaEff));
            ByteChannelIO.writeFully(ch, ByteBuffer.wrap(payloadDataEff));
        }
    }

    @SuppressWarnings("unused")
    public Message receiveMessage(
            final ByteChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(compressor);
        Objects.requireNonNull(encryptor);

        try {
            // [1] Read header from channel
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

            final byte[] payloadMetaRaw;
            final byte[] payloadDataRaw;

            // optimization turned off
            if (OPT_SMALL_BUF_RCV && (header.getPayloadMetaSize() + header.getPayloadDataSize()) < SMALL_BUF_16KB) {
                final byte[] b = new byte[SMALL_BUF_16KB];  // OS friendly buffer

                final int effSize = header.getPayloadMetaSize() + header.getPayloadDataSize();
                final ByteBuffer buf = ByteBuffer.wrap(b, 0, effSize);
                ByteChannelIO.readFully(ch, buf);
                buf.flip();

                payloadMetaRaw = new byte[header.getPayloadMetaSize()];
                payloadDataRaw = new byte[header.getPayloadDataSize()];

                System.arraycopy(b, 0, payloadMetaRaw, 0, payloadMetaRaw.length);
                System.arraycopy(b, payloadMetaRaw.length, payloadDataRaw, 0, payloadDataRaw.length);
            }
            else {
                // [2] Read payload meta data from channel
                final ByteBuffer payloadMetaBuf = ByteBuffer.allocate(header.getPayloadMetaSize());
                ByteChannelIO.readFully(ch, payloadMetaBuf);
                payloadMetaRaw = payloadMetaBuf.array();

                // [3] Read payload data from channel
                final ByteBuffer payloadDataBuf = ByteBuffer.allocate(header.getPayloadDataSize());
                ByteChannelIO.readFully(ch, payloadDataBuf);
                payloadDataRaw = payloadDataBuf.array();
            }

            // [4] Process payload meta data (maybe encrypted)
            final PayloadMetaData payloadMeta = PayloadMetaData.decode(
                                                    header.isEncrypted()
                                                        ? encryptor.decrypt(
                                                                payloadMetaRaw,
                                                                Header.aadData(header),
                                                                true)
                                                        : payloadMetaRaw);

            // [5] Process payload data (maybe compressed and encrypted)
            final byte[] payloadData = compressor.decompress(
                                            encryptor.decrypt(
                                                payloadDataRaw,
                                                header.isEncrypted()),
                                            header.isCompressed());

            // [6] Build message
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

    private final static int SMALL_BUF_8KB  =  8 * KB;
    private final static int SMALL_BUF_16KB = 16 * KB;

    private final static boolean OPT_SMALL_BUF_SND = true;
    private final static boolean OPT_SMALL_BUF_RCV = false;


    private final IBufferPool cachedBuffer_8KB  = new BufferPool(SMALL_BUF_8KB);
    private final IBufferPool cachedBuffer_16KB = new BufferPool(SMALL_BUF_16KB);
}
