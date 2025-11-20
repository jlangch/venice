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

import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Topics;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public abstract class MessageFactory {

    /**
     * Create a text message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String requestId,
            final String topic,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                Message.EXPIRES_NEVER,
                Topics.of(topic),
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a text message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String requestId,
            final long expiresAt,
            final String topic,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                expiresAt,
                Topics.of(topic),
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a json message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param topic a topic
     * @param charset the charset of the message's payload data
     * @param json the json payload data
     * @return the message
     */
    public static IMessage json(
            final String requestId,
            final String topic,
            final String charset,
            final String json
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(json);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                Message.EXPIRES_NEVER,
                Topics.of(topic),
                "application/json",
                charset,
                json.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a json message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param topic a topic
     * @param charset the charset of the message's payload data
     * @param json the json payload data
     * @return the message
     */
    public static IMessage json(
            final String requestId,
            final long expiresAt,
            final String topic,
            final String charset,
            final String json
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(json);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                expiresAt,
                Topics.of(topic),
                "application/json",
                charset,
                json.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a binary message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String requestId,
            final String topic,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                Message.EXPIRES_NEVER,
                Topics.of(topic),
                mimetype,
                null,
                data);
    }


    /**
     * Create a binary message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String requestId,
            final long expiresAt,
            final String topic,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,
                expiresAt,
                Topics.of(topic),
                mimetype,
                null,
                data);
    }


    /**
     * Create a Venice message.
     *
     * <p>The Venice data is serialized to JSON to transport it within
     * a message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param topic a topic
     * @param data venice data
     * @return the message
     */
    public static IMessage venice(
            final String requestId,
            final String topic,
            final VncVal data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return json(requestId, topic, "UTF-8", Json.writeJson(data, false));
    }


    /**
     * Create a Venice message.
     *
     * <p>The Venice data is serialized to JSON to transport it within
     * a message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param topic a topic
     * @param data venice data
     * @return the message
     */
    public static IMessage venice(
            final String requestId,
            final long expiresAt,
            final String topic,
            final VncVal data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(data);

        Topics.validate(topic);

        return json(requestId, expiresAt, topic, "UTF-8", Json.writeJson(data, false));
    }


    /**
     * Create a simple hello message.
     *
     * <ul>
     *   <li>topic: "hello"</li>
     *   <li>mimetype: "text/plain"</li>
     *   <li>charset: "UTF-8"</li>
     *   <li>text: "Hello!"</li>
     * </ul>
     * @return the hello message
     */
    public static IMessage hello() {
        return text(null, "hello", "text/plain", "UTF-8", "Hello!");
    }

}
