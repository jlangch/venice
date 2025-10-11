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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.UUIDHelper;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;
import com.github.jlangch.venice.util.ipc.impl.util.IEncryptor;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.PayloadMetaData;


public class Protocol {

    public Protocol() {
    }


    public static void sendMessage(
            final SocketChannel ch,
            final Message message,
            final Compressor compressor,
            final IEncryptor encryptor
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(message);

        final boolean compressData = compressor.needsCompression(message.getData());

        // [1] header
        final ByteBuffer header = ByteBuffer.allocate(44);
        // 2 bytes magic chars
        header.put((byte)'v');
        header.put((byte)'n');
        // 4 bytes (integer) protocol version
        header.putInt(PROTOCOL_VERSION);
        // 4 bytes (integer) message type
        header.putInt(message.getType().getValue());
        // 2 bytes (short) oneway flag
        header.putShort(toShort(message.isOneway()));
        // 2 bytes (short) compressed data flag
        header.putShort(toShort(compressData));
        // 2 bytes (short) encrypted data flag
        header.putShort(toShort(encryptor.isActive()));
        // 4 bytes (integer) response status
        header.putInt(message.getResponseStatus().getValue());
        // 8 bytes (long) timestamp
        header.putLong(message.getTimestamp());
        // 16 bytes UUID
        header.put(UUIDHelper.convertUUIDToBytes(message.getId()));
        header.flip();
        IO.writeFully(ch, header);

        // [2] payload meta data (optionally encrypt)
        final byte[] metaData = encryptor.encrypt(
                                    PayloadMetaData.encode(
                                        new PayloadMetaData(
                                            message.getQueueName(),
                                            message.getTopics(),
                                            message.getMimetype(),
                                            message.getCharset())));
        final ByteBuffer meta = ByteBuffer.allocate(metaData.length);
        meta.put(metaData);
        meta.flip();
        IO.writeFrame(ch, meta);

        // [3] payload data (optionally compress and encrypt)
        byte[] payloadData = encryptor.encrypt(
                                compressor.compress(
                                    message.getData(),
                                    compressData));
        final ByteBuffer payload = ByteBuffer.allocate(payloadData.length);
        payload.put(payloadData);
        payload.flip();
        IO.writeFrame(ch, payload);
    }

    public static Message receiveMessage(
            final SocketChannel ch,
            final Compressor compressor,
            final IEncryptor encryptor
    ) {
        Objects.requireNonNull(ch);

        try {
            // [1] header
            final ByteBuffer header = ByteBuffer.allocate(44);
            final int bytesRead = ch.read(header);
            if (bytesRead < 0) {
                throw new EofException("Failed to read data from channel, channel EOF reached!");
            }

            header.flip();

            // parse header
            final byte magic1 = header.get();
            final byte magic2 = header.get();
            final int version = header.getInt();
            final int typeCode = header.getInt();
            final boolean oneway = toBool(header.getShort());
            final boolean compressedData = toBool(header.getShort());
            final boolean encryptedData = toBool(header.getShort());
            final int statusCode = header.getInt();
            final long timestamp = header.getLong();
            final byte[] uuid = new byte[16];
            header.get(uuid);

            final MessageType type = MessageType.fromCode(typeCode);
            final ResponseStatus status = ResponseStatus.fromCode(statusCode);

            if (magic1 != 'v' || magic2 != 'n') {
                throw new VncException(
                        "Received unknown message (bad magic bytes)!");
            }

            if (version != PROTOCOL_VERSION) {
                throw new VncException(
                        "Received message with unsupported protocol version " + version + "!");
            }

            // [2] payload meta data (maybe encrypted)
            final ByteBuffer payloadMetaFrame = IO.readFrame(ch);
            final PayloadMetaData payloadMeta = PayloadMetaData.decode(
                                                    decryptPayloadData(
                                                        payloadMetaFrame.array(),
                                                        encryptedData,
                                                        encryptor));

            // [3] payload data (maybe compressed and encrypted)
            final ByteBuffer payloadFrame = IO.readFrame(ch);
            byte[] data = compressor.decompress(
                                decryptPayloadData(
                                    payloadFrame.array(),
                                    encryptedData,
                                    encryptor),
                                compressedData);

            if (status == null) {
                throw new VncException(
                        "Received illegal status code " + statusCode + "!");
            }

            return new Message(
                    UUIDHelper.convertBytesToUUID(uuid),
                    type,
                    status,
                    oneway,
                    payloadMeta.getQueueName(),
                    timestamp,
                    payloadMeta.getTopics(),
                    payloadMeta.getMimetype(),
                    payloadMeta.getCharset(),
                    data);
        }
        catch(IOException ex) {
            if (ExceptionUtil.isBrokenPipeException(ex)) {
                throw new VncException("Failed to read data from channel, channel was closed!", ex);
            }
            else {
                throw new VncException("Failed to read data from channel!", ex);
            }
        }
    }


    private static byte[] decryptPayloadData(
            final byte[] payloadData,
            final boolean encrypted,
            final IEncryptor encryptor
    ) {
        byte[] data = payloadData;

        // decrypt
        if (encrypted) {
            if (!encryptor.isActive()) {
                throw new VncException(
                        "Message error: the received message data is encrypted "
                        + "but encryption is disabled!");
            }
            data = encryptor.decrypt(data);
        }

        return data;
    }

    private static boolean toBool(final int n) {
        return n != 0;
    }

    private static short toShort(final boolean b) {
        return b ? (short)1 : (short)0;
    }


    private final static int PROTOCOL_VERSION = 1;
}
