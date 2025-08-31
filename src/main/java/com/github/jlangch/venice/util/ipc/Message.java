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
package com.github.jlangch.venice.util.ipc;

import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.VncException;


/**
 * Represents a message exchanged between the <code>TcpClient</code>
 * and <code>TcpServer</code>
 */
public class Message {

    Message(
            final Status status,
            final String topic,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        this.status = status;
        this.topic = topic;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    /**
     * Create a text message
     *
     * @param status the message's status
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param charset the chartset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static Message text(
            final Status status,
            final String topic,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                status,
                topic,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }

    /**
     * Create a binary message
     *
     * @param status the message's status
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static Message binary(
            final Status status,
            final String topic,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                status,
                topic,
                mimetype,
                null,
                data);
    }

    /**
     * @return a simple hello message (topic: "hello", data; "Hello!")
     */
    public static Message hello() {
        return Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", "Hello!");
    }

    /**
     * @return a simple echo message (topic: "echo", data; "Hello!")
     */
    public static Message echo() {
        return Message.text(Status.REQUEST, "echo", "text/plain", "UTF-8", "Hello!");
    }

    /**
     * @return this message as echoed message
     */
    public Message asEcho() {
        return new Message(Status.RESPONSE_OK, topic, mimetype, charset, data);
    }

    /**
     * @return the message status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the message topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the message mimetype
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @return the message's payload data charset if the payload data is of
     *         type string else <code>null</code>
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @return the message binary payload data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the message textual payload data. Throws a VncException if the payload is binary data.
     */
    public String getText() {
        if (charset != null) {
            return new String(data, Charset.forName(charset));
        }
        else {
            throw new VncException("A binary message can be converted to text data!");
        }
    }


    private final Status status;
    private final String topic;
    private final String mimetype;
    private final String charset;
    private final byte[] data;
}
