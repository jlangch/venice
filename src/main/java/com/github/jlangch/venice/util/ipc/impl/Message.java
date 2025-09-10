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
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.Status;


/**
 * Represents a message exchanged between the
 * <code>TcpClient</code> and the <code>TcpServer</code>.
 */
public class Message implements IMessage {

    public Message(
            final Status status,
            final String topic,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        this.id = UUID.randomUUID();
        this.status = status;
        this.timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        this.topic = topic;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    public Message(
            final UUID id,
            final Status status,
            final long timestamp,
            final String topic,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        this.id = id;
        this.status = status;
        this.timestamp = timestamp;
        this.topic = topic;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }


    /**
     * Change the status of a message
     *
     * @param status a status
     * @return a new message with the topic
     */
    public Message withStatus(final Status status) {
        Objects.requireNonNull(status);
        return new Message(id, status, timestamp, topic, mimetype, charset, data);
    }

    @Override
    public Message asEchoResponse() {
        return withStatus(Status.RESPONSE_OK);
    }


    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Status getStatus() {
        return status;
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
                   padRight("Status:", 12),
                   status.name()));

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
                   formatDataLen(data.length)));

       return sb.toString();
    }


    public void validateMessageStatus(final Status...status) {
        if (!CollectionUtil.toSet(status).contains(getStatus())) {
            final String goodList = CollectionUtil
                                        .toList(status)
                                        .stream()
                                        .map(s -> "'" + s +"'")
                                        .collect(Collectors.joining(", "));

            throw new VncException(
                    String.format(
                        "Unacceptable message status '%s'! Use one of {%s}.",
                        status,
                        goodList));
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
    private final Status status;
    private final long timestamp;
    private final String topic;
    private final String mimetype;
    private final String charset;
    private final byte[] data;
}
