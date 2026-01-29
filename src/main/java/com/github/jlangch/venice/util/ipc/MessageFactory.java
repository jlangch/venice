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
package com.github.jlangch.venice.util.ipc;

import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.TopicValidator;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public abstract class MessageFactory {

    // ------------------------------------------------------------------------
    // Text messages
    // ------------------------------------------------------------------------

	/**
     * Create a text message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String requestId,
            final String subject,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                Messages.EXPIRES_NEVER,
                subject,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }

    /**
     * Create a text message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String requestId,
            final long expiresAt,
            final String subject,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                expiresAt,
                subject,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }

    /**
     * Create a text message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param durable a durable message
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String requestId,
            final long expiresAt,
            final boolean durable,
            final String subject,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                durable,
                false,  // no subscription reply
                expiresAt,
                subject,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }

    // ------------------------------------------------------------------------
    // JSON messages
    // ------------------------------------------------------------------------

    /**
     * Create a json message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param subject a subject
     * @param charset the charset of the message's payload data
     * @param json the json payload data
     * @return the message
     */
    public static IMessage json(
            final String requestId,
            final String subject,
            final String charset,
            final String json
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(json);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                Messages.EXPIRES_NEVER,
                subject,
                "application/json",
                charset,
                json.getBytes(Charset.forName(charset)));
    }

    /**
     * Create a json message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param subject a subject
     * @param charset the charset of the message's payload data
     * @param json the json payload data
     * @return the message
     */
    public static IMessage json(
            final String requestId,
            final long expiresAt,
            final String subject,
            final String charset,
            final String json
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(json);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                expiresAt,
                subject,
                "application/json",
                charset,
                json.getBytes(Charset.forName(charset)));
    }

    /**
     * Create a json message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param durable a durable message
     * @param subject a subject
     * @param charset the charset of the message's payload data
     * @param json the json payload data
     * @return the message
     */
    public static IMessage json(
            final String requestId,
            final long expiresAt,
            final boolean durable,
            final String subject,
            final String charset,
            final String json
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(json);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                durable,
                false,  // no subscription reply
                expiresAt,
                subject,
                "application/json",
                charset,
                json.getBytes(Charset.forName(charset)));
    }


    // ------------------------------------------------------------------------
    // Binary messages
    // ------------------------------------------------------------------------

    /**
     * Create a binary message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String requestId,
            final String subject,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                Messages.EXPIRES_NEVER,
                subject,
                mimetype,
                null,
                data);
    }

    /**
     * Create a binary message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String requestId,
            final long expiresAt,
            final String subject,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                false,  // not durable
                false,  // no subscription reply
                expiresAt,
                subject,
                mimetype,
                null,
                data);
    }

    /**
     * Create a binary message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param durable a durable message
     * @param subject a subject
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String requestId,
            final long expiresAt,
            final boolean durable,
            final String subject,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                requestId,
                MessageType.NULL,
                ResponseStatus.NULL,
                false,  // not oneway
                durable,
                false,  // no subscription reply
                expiresAt,
                subject,
                mimetype,
                null,
                data);
    }


    // ------------------------------------------------------------------------
    // Venice data messages
    // ------------------------------------------------------------------------

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

        TopicValidator.validate(topic);

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

        TopicValidator.validate(topic);

        return json(requestId, expiresAt, topic, "UTF-8", Json.writeJson(data, false));
    }

    /**
     * Create a Venice message.
     *
     * <p>The Venice data is serialized to JSON to transport it within
     * a message
     *
     * @param requestId an optional request ID (may be used for idempotency checks by the receiver)
     * @param expiresAt message expiration timestamp (millis since epoch, -1 means never expires)
     * @param durable a durable message
     * @param topic a topic
     * @param data venice data
     * @return the message
     */
    public static IMessage venice(
            final String requestId,
            final long expiresAt,
            final boolean durable,
            final String topic,
            final VncVal data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(data);

        TopicValidator.validate(topic);

        return json(requestId, expiresAt, durable, topic, "UTF-8", Json.writeJson(data, false));
    }


    // ------------------------------------------------------------------------
    // Text test messages
    // ------------------------------------------------------------------------

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
