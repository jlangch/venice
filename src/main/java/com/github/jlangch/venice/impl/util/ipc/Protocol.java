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
package com.github.jlangch.venice.impl.util.ipc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.impl.util.StringUtil;


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
        final ByteBuffer header = ByteBuffer.allocate(10);
        // 2 bytes magic chars
        header.putChar('v');
        header.putChar('c');
        // 4 bytes (integer) protocol version
        header.putInt(PROTOCOL_VERSION);
        // 4 bytes (integer) request/response status
        header.putInt(message.getStatus().getValue());
        header.flip();
        IO.writeFrame(ch, header);

        // [2] charset frame
        if (StringUtil.isBlank(message.getCharset())) {
            final ByteBuffer charset = ByteBuffer.allocate(4);
            header.putInt(0);
            charset.flip();
            IO.writeFrame(ch, charset);
        }
        else {
            final byte[] charsetData = message.getCharset().getBytes(Charset.forName("UTF8"));
            final ByteBuffer charset = ByteBuffer.allocate(4 + charsetData.length);
            charset.putInt(charsetData.length);
            charset.put(charsetData);
            charset.flip();
            IO.writeFrame(ch, charset);
        }

        // [3] mimetype frame
        final byte[] mimetypeData = message.getMimetype().getBytes(Charset.forName("UTF8"));
        final ByteBuffer mimetype = ByteBuffer.allocate(4 + mimetypeData.length);
        mimetype.putInt(mimetypeData.length);
        mimetype.put(mimetypeData);
        mimetype.flip();
        IO.writeFrame(ch, mimetype);

        // [4] payload data
        final byte[] payloadData = message.getData();
        final ByteBuffer payload = ByteBuffer.allocate(4 + payloadData.length);
        payload.putInt(payloadData.length);
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
            final ByteBuffer header = ByteBuffer.allocate(10);
            ch.read(header);

            final byte magic1 = header.get();
            final byte magic2 = header.get();
            final int version = header.getInt();
            final int statusCode = header.getInt();
            final Status status = Status.fromCode(statusCode);

            if (magic1 != 'v' || magic2 != 'n') {
                throw new RuntimeException(
                        "Received unknow message (bad magic bytes)!");
            }

            if (version != PROTOCOL_VERSION) {
                throw new RuntimeException(
                        "Received message with unsupported protocol version" + version + "!");
            }

            // [2] charset frame
            final ByteBuffer charsetFrame = IO.readFrame(ch);
            final String charset = charsetFrame.hasRemaining()
                                    ? new String(charsetFrame.array(), Charset.forName("UTF8"))
                                    : null;

            // [3] mimetype frame
            final ByteBuffer mimetypeFrame = IO.readFrame(ch);
            final String mimetype = charsetFrame.hasRemaining()
                                        ? new String(mimetypeFrame.array(), Charset.forName("UTF8"))
                                        : "application/octet-stream";

            // [4] payload data
            final ByteBuffer payloadFrame = IO.readFrame(ch);
            final byte[] data = payloadFrame.array();

            if (status == null) {
                throw new RuntimeException(
                        "Received illegal status code " + statusCode + "!");
            }

            return new Message(status, charset, mimetype, data);
        }
        catch(IOException ex) {
            throw new RuntimeException(
                    "Failed to receive message!", ex);
        }
    }


    private final static int PROTOCOL_VERSION = 1;
}
