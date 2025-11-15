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
import com.github.jlangch.venice.util.ipc.impl.util.Compressor;
import com.github.jlangch.venice.util.ipc.impl.util.Encryptor;
import com.github.jlangch.venice.util.ipc.impl.util.ExceptionUtil;
import com.github.jlangch.venice.util.ipc.impl.util.IO;
import com.github.jlangch.venice.util.ipc.impl.util.PayloadMetaData;


public class Protocol {

    public Protocol() {
    }


    public static void sendMessage(
            final SocketChannel ch,
            final Message message,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(message);

        final boolean isCompressData = compressor.needsCompression(message.getData());

        // [1] header
        //     if encryption is active the header is processed as AAD
        //     (added authenticated data) with the encrypted payload meta
        //      data, so any tampering if the header data is detected!
        final ByteBuffer header = ByteBuffer.allocate(18);
        // 2 bytes magic chars
        header.put((byte)'v');
        header.put((byte)'n');
        // 4 bytes (integer) protocol version
        header.putInt(PROTOCOL_VERSION);
        // 2 bytes (short) compressed data flag
        header.putShort(toShort(isCompressData));
        // 2 bytes (short) encrypted data flag
        header.putShort(toShort(encryptor.isActive()));
        // 8 bytes (long) timestamp
        header.putLong(message.getTimestamp());
        header.flip();
        IO.writeFully(ch, header);

        // [2] payload meta data (optionally encrypt)
        final byte[] headerAAD = header.array(); ; // GCM AAD: added authenticated data
        final byte[] metaData = encryptor.encrypt(
                                    PayloadMetaData.encode(
                                        new PayloadMetaData(message)),
                                    headerAAD);
        final ByteBuffer meta = ByteBuffer.allocate(metaData.length);
        meta.put(metaData);
        meta.flip();
        IO.writeFrame(ch, meta);

        // [3] payload data (optionally compress and encrypt)
        byte[] payloadData = encryptor.encrypt(
                                compressor.compress(
                                    message.getData(),
                                    isCompressData));
        final ByteBuffer payload = ByteBuffer.allocate(payloadData.length);
        payload.put(payloadData);
        payload.flip();
        IO.writeFrame(ch, payload);
    }

    public static Message receiveMessage(
            final SocketChannel ch,
            final Compressor compressor,
            final Encryptor encryptor
    ) {
        Objects.requireNonNull(ch);

        try {
            // [1] header
            final ByteBuffer header = ByteBuffer.allocate(18);
            final int bytesRead = ch.read(header);
            if (bytesRead < 0) {
                throw new EofException("Failed to read data from channel, channel EOF reached!");
            }

            header.flip();

            // parse header
            final byte magic1 = header.get();
            final byte magic2 = header.get();
            final int version = header.getInt();
            final boolean isCompressedData = toBool(header.getShort());
            final boolean isEncryptedData = toBool(header.getShort());
            final long timestamp = header.getLong();

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
            final byte[] headerAAD = header.array(); // GCM AAD: added authenticated data
            final PayloadMetaData payloadMeta = PayloadMetaData.decode(
                                                    encryptor.decrypt(
                                                        payloadMetaFrame.array(),
                                                        headerAAD,
                                                        isEncryptedData));

            // [3] payload data (maybe compressed and encrypted)
            final ByteBuffer payloadFrame = IO.readFrame(ch);
            byte[] payloadData = compressor.decompress(
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
                    payloadMeta.getQueueName(),
                    timestamp,
                    payloadMeta.getTopics(),
                    payloadMeta.getMimetype(),
                    payloadMeta.getCharset(),
                    payloadData);
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



    private static boolean toBool(final int n) {
        if (n == 0) return false;
        else if (n == 1) return true;
        else throw new VncException("Illegal IPC message boolean value");
    }

    private static short toShort(final boolean b) {
        return b ? (short)1 : (short)0;
    }


    private final static int PROTOCOL_VERSION = 1;
}
