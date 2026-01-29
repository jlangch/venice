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

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.jlangch.venice.impl.types.VncVal;


/**
 * Defines the messages that can be exchanged between a TcpClient and a TcpServer.
 *
 * <pre>
 *  Message                            Originator
 * ┌───────────────────────────────┐
 * │ ID                            │   send, publish/subscribe method
 * ├───────────────────────────────┤
 * │ Message Type                  │   send, publish/subscribe method
 * ├───────────────────────────────┤
 * │ Oneway                        │   client or framework method
 * ├───────────────────────────────┤
 * │ Durable                       │   client or framework method
 * ├───────────────────────────────┤
 * │ Response Status               │   server response processor
 * ├───────────────────────────────┤
 * │ Timestamp                     │   message creator
 * ├───────────────────────────────┤
 * │ ExpiresAt                     │   client (may be null)
 * ├───────────────────────────────┤
 * │ Timeout                       │   client (used as server-side queue offer/poll timeout)
 * ├───────────────────────────────┤
 * │ Request ID                    │   client (may be used for idempotency checks by the receiver)
 * ├───────────────────────────────┤
 * │ Topic                         │   client
 * ├───────────────────────────────┤
 * │ Queue Name                    │   client  (offer/poll, else null)
 * ├───────────────────────────────┤
 * │ ReplyTo Queue Name            │   client  (offer/poll, may be null)
 * ├───────────────────────────────┤
 * │ Payload Mimetype              │   client
 * ├───────────────────────────────┤
 * │ Payload Charset               │   client if payload data is a string else null
 * ├───────────────────────────────┤
 * │ Payload data                  │   client
 * └───────────────────────────────┘
 * </pre>
 */
public interface IMessage {

    /**
     * @return the message id
     */
    public UUID getId();


    /**
     * @return the request id
     */
    public String getRequestId();

    /**
     * @return the message type
     */
    MessageType getType();

    /**
     * @return <code>true</code> if this message is a one-way message
     *        that the receiver must not answer with a reply message,
     *        else <code>false</code>
     */
    boolean isOneway();

    /**
     * @return true if the message is durable
     */
    boolean isDurable();

    /**
     * @return the message response status
     */
    ResponseStatus getResponseStatus();

    /**
     * @return the message timestamp (milliseconds since epoch)
     */
    long getTimestamp();

    /**
     * @return the message timestamp as LocalDateTime
     */
    LocalDateTime getTimestampAsLocalDateTime();

    /**
     * @return the message's age in milliseconds
     */
    long getMessageAge();

    /**
     * @return the message expiry timestamp (milliseconds since epoch), -1 if it never expires
     */
    long getExpiresAt();

    /**
     * @return <code>true</code> if the message has expired else <code>false</code>
     */
    boolean hasExpired();

    /**
     * @return the message expiresAt as LocalDateTime or <code>null</code> if
     *         it never expires
     */
    LocalDateTime getExpiresAtAsLocalDateTime();

    /**
     * @return the message queue name
     */
    String getQueueName();

    /**
     * @return the message topic name
     */
    String getTopicName();

    /**
     * @return the message replyTo queue name
     */
    String getReplyToQueueName();

    /**
     * @return the message's subject
     */
    String getSubject();

    /**
     * @return the message mimetype
     */
    String getMimetype();

    /**
     * @return the message's payload data charset if the payload data is of
     *         type string else <code>null</code>
     */
    String getCharset();

    /**
     * @return the message binary payload data
     */
    byte[] getData();

    /**
     * @return the message textual payload data. Throws a VncException if the payload is binary data.
     */
    String getText() ;

    /**
     * @return the message payload as Venice data. Throws a VncException if the payload cannot be converted.
     */
    VncVal getVeniceData();

    /**
     * @return <code>true</code> if this message is a text message, else <code>false</code>
     */
    boolean isTextMessage();

    /**
     * @return <code>true</code> if this message is a binary message, else <code>false</code>
     */
    boolean isBinaryMessage();

    /**
     * @param other a message
     * @return <code>true</code> if this message has the same ID as another message else <code>false</code>
     */
    boolean hasSameId(IMessage other);
}
