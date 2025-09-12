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

import static com.github.jlangch.venice.impl.util.StringUtil.padRight;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;


/**
 * Represents a message exchanged between the
 * <code>TcpClient</code> and the <code>TcpServer</code>.
 */
public class Message implements IMessage {

    public Message(
            final MessageType type,
            final ResponseStatus responseStatus,
            final boolean oneway,
            final String topic,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        this.id = UUID.randomUUID();
        this.type = type;
        this.responseStatus = responseStatus;
        this.oneway = oneway;
        this.timestamp = Instant.now().toEpochMilli();
        this.topic = topic;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    public Message(
            final UUID id,
            final MessageType type,
            final ResponseStatus responseStatus,
            final boolean oneway,
            final long timestamp,
            final String topic,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        this.id = id;
        this.type = type;
        this.responseStatus = responseStatus;
        this.oneway = oneway;
        this.timestamp = timestamp;
        this.topic = topic;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }


    /**
     * Change the type of a message
     *
     * @param type a type
     * @return a new message with the topic
     */
    public Message withType(final MessageType type) {
        Objects.requireNonNull(type);
        return new Message(
                id,
                type, responseStatus, oneway,
                timestamp, topic, mimetype, charset, data);
    }

    /**
     * Change the type of a message and oneway mode
     *
     * @param type a type
     * @param oneway oneway mode
     * @return a new message with the topic
     */
    public Message withType(final MessageType type, final boolean oneway) {
        Objects.requireNonNull(type);
        return new Message(
                id,
                type, responseStatus, oneway,
                timestamp, topic, mimetype, charset, data);
    }

    /**
     * Change the response status of a message
     *
     * @param responseStatus a response status
     * @return a new message with the topic
     */
    public Message withResponseStatus(final ResponseStatus responseStatus) {
        Objects.requireNonNull(responseStatus);
        return new Message(
                id,
                type, responseStatus, oneway,
                timestamp, topic, mimetype, charset, data);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public boolean isOneway() {
        return oneway;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public LocalDateTime getTimestampAsLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    @Override
    public long getMessageAge() {
        final long now = Instant.now().toEpochMilli();
        return now - timestamp;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getMimetype() {
        return mimetype;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String getText() {
        if (isTextMessage()) {
            return new String(data, Charset.forName(charset));
        }
        else {
            throw new VncException("A binary message can be converted to text data!");
        }
    }

    @Override
    public boolean isTextMessage() {
        return charset != null;
    }

    @Override
    public boolean isBinaryMessage() {
        return charset == null;
    }


    @Override
    public String toString() {
       final StringBuilder sb = new StringBuilder();

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Id:", 12),
                   id.toString()));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Type:", 12),
                   type.name()));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Response:", 12),
                   responseStatus.name()));

       sb.append(String.format(
                   "%s %b\n",
                   padRight("Oneway:", 12),
                   oneway));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Timestamp:", 12),
                   getTimestampAsLocalDateTime()));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Topic:", 12),
                   topic));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Mimetype:", 12),
                   mimetype));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Charset:", 12),
                   charset == null ? "" : charset));

       sb.append(String.format(
                   "%s %s",
                   padRight("Data:", 12),
                   formatData()));

       return sb.toString();
    }


    private String formatData() {
       if (isTextMessage()) {
           final String text = getText();
           return StringUtil.truncate(text, 80, "... (" + text.length() + ")");
       }
       else {
           return formatDataLen(data.length);
       }
    }

    private static String formatDataLen(final int len) {
        if (len < 10 * 1024) {
            return String.valueOf(len) + "B";
        }
        else if (len < 10 * 1024 * 1024) {
            return String.valueOf(len / 1024) + "KB";
        }
        else {
            return String.valueOf(len / 1024 / 1024) + "MB";
        }
    }


    private final UUID id;
    private final MessageType type;
    private final ResponseStatus responseStatus;
    private final boolean oneway;
    private final long timestamp;
    private final String topic;
    private final String mimetype;
    private final String charset;
    private final byte[] data;
}
