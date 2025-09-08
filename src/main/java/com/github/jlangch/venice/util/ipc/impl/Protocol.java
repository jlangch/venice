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
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.Message;
import com.github.jlangch.venice.util.ipc.Status;


public class Protocol {

    public Protocol() {
    }


    public static void sendMessage(
            final SocketChannel ch,
            final Message message
    ) {
        Objects.requireNonNull(ch);
        Objects.requireNonNull(message);

        // [1] header
        final ByteBuffer header = ByteBuffer.allocate(18);
        // 2 bytes magic chars
        header.put((byte)'v');
        header.put((byte)'n');
        // 4 bytes (integer) protocol version
        header.putInt(PROTOCOL_VERSION);
        // 4 bytes (integer) request/response status
        header.putInt(message.getStatus().getValue());
        // 8 bytes (long) timestamp
        header.putLong(message.getTimestamp());
        header.flip();

        IO.writeFully(ch, header);

        // [2] charset frame
        if (StringUtil.isBlank(message.getCharset())) {
            IO.writeFrame(ch, null);  // frame with 0 length
        }
        else {
            final byte[] charsetData = message.getCharset().getBytes(Charset.forName("UTF8"));
            final ByteBuffer charset = ByteBuffer.allocate(charsetData.length);
            charset.put(charsetData);
            charset.flip();
            IO.writeFrame(ch, charset);
        }

        // [3] topic frame
        final byte[] topicData = message.getTopic().getBytes(Charset.forName("UTF8"));
        final ByteBuffer topic = ByteBuffer.allocate(topicData.length);
        topic.put(topicData);
        topic.flip();
        IO.writeFrame(ch, topic);

        // [4] mimetype frame
        final byte[] mimetypeData = message.getMimetype().getBytes(Charset.forName("UTF8"));
        final ByteBuffer mimetype = ByteBuffer.allocate(mimetypeData.length);
        mimetype.put(mimetypeData);
        mimetype.flip();
        IO.writeFrame(ch, mimetype);

        // [5] payload data
        final byte[] payloadData = message.getData();
        final ByteBuffer payload = ByteBuffer.allocate(payloadData.length);
        payload.put(payloadData);
        payload.flip();
        IO.writeFrame(ch, payload);
    }

    public static Message receiveMessage(
            final SocketChannel ch
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

            final byte magic1 = header.get();
            final byte magic2 = header.get();
            final int version = header.getInt();
            final int statusCode = header.getInt();
            final long timestamp = header.getLong();

            final Status status = Status.fromCode(statusCode);

            if (magic1 != 'v' || magic2 != 'n') {
                throw new VncException(
                        "Received unknown message (bad magic bytes)!");
            }

            if (version != PROTOCOL_VERSION) {
                throw new VncException(
                        "Received message with unsupported protocol version " + version + "!");
            }

            // [2] charset frame (has frame length 0 for binary messages)
            final ByteBuffer charsetFrame = IO.readFrame(ch);
            final String charset = charsetFrame.hasRemaining()
                                    ? new String(charsetFrame.array(), Charset.forName("UTF8"))
                                    : null;

            // [3] topic frame
            final ByteBuffer topicFrame = IO.readFrame(ch);
            final String topic = topicFrame.hasRemaining()
                                        ? new String(topicFrame.array(), Charset.forName("UTF8"))
                                        : "*";

            // [4] mimetype frame
            final ByteBuffer mimetypeFrame = IO.readFrame(ch);
            final String mimetype = mimetypeFrame.hasRemaining()
                                        ? new String(mimetypeFrame.array(), Charset.forName("UTF8"))
                                        : "application/octet-stream";

            // [5] payload data
            final ByteBuffer payloadFrame = IO.readFrame(ch);
            final byte[] data = payloadFrame.array();

            if (status == null) {
                throw new VncException(
                        "Received illegal status code " + statusCode + "!");
            }

            return new Message(status, timestamp, topic, mimetype, charset, data);
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


    private final static int PROTOCOL_VERSION = 1;
}
