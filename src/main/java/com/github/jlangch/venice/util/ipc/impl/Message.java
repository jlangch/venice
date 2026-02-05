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
package com.github.jlangch.venice.util.ipc.impl;

import static com.github.jlangch.venice.impl.util.StringUtil.padRight;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


/**
 * Represents a message exchanged between the
 * <code>TcpClient</code> and the <code>TcpServer</code>.
 */
public class Message implements IMessage {

    public Message(
            final String requestId,
            final MessageType type,
            final ResponseStatus responseStatus,
            final boolean oneway,
            final boolean durable,
            final boolean subscriptionReply,
            final long expiresAt,
            final String subject,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        validateSubject(subject);
        validateMimetype(mimetype);
        validateCharset(charset);

        this.id = UUID.randomUUID();
        this.requestId = requestId;
        this.type = type;
        this.responseStatus = responseStatus;
        this.oneway = oneway;
        this.durable = durable;
        this.subscriptionReply = subscriptionReply;
        this.destinationName = null;
        this.replyToQueueName = null;
        this.timestamp = Instant.now().toEpochMilli();
        this.expiresAt = expiresAt < 0 ? Messages.EXPIRES_NEVER : expiresAt;
        this.subject = subject;
        this.timeout = Messages.DEFAULT_TIMEOUT;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    public Message(
            final UUID id,
            final String requestId,
            final MessageType type,
            final ResponseStatus responseStatus,
            final boolean oneway,
            final boolean durable,
            final boolean subscriptionReply,
            final long expiresAt,
            final String subject,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        validateSubject(subject);
        validateMimetype(mimetype);
        validateCharset(charset);

        this.id = id == null ? UUID.randomUUID() : id;
        this.requestId = requestId;
        this.type = type;
        this.responseStatus = responseStatus;
        this.oneway = oneway;
        this.durable = durable;
        this.subscriptionReply = subscriptionReply;
        this.destinationName = null;
        this.replyToQueueName = null;
        this.timestamp = Instant.now().toEpochMilli();
        this.expiresAt = expiresAt < 0 ? Messages.EXPIRES_NEVER : expiresAt;
        this.subject = subject;
        this.timeout = Messages.DEFAULT_TIMEOUT;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    public Message(
            final UUID id,
            final String requestId,
            final MessageType type,
            final ResponseStatus responseStatus,
            final boolean oneway,
            final boolean durable,
            final boolean subscriptionReply,
            final String destinationName,
            final String replyToQueueName,
            final long timestamp,
            final long expiresAt,
            final long timeout,
            final String subject,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        validateSubject(subject);
        validateMimetype(mimetype);
        validateCharset(charset);

        this.id = id == null ? UUID.randomUUID() : id;
        this.requestId = requestId;
        this.type = type;
        this.responseStatus = responseStatus;
        this.oneway = oneway;
        this.durable = durable;
        this.subscriptionReply = subscriptionReply;
        this.destinationName = StringUtil.trimToNull(destinationName);
        this.replyToQueueName = replyToQueueName;
        this.timestamp = timestamp <= 0 ? Instant.now().toEpochMilli() : timestamp;
        this.expiresAt = expiresAt < 0 ? Messages.EXPIRES_NEVER : expiresAt;
        this.timeout = timeout < 0 ? Messages.NO_TIMEOUT : timeout;
        this.subject = subject;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }


    /**
     * Change the type of a message and oneway mode.
     *
     * <p>Removes the queue name on the message.
     *
     * @param type a type
     * @param oneway oneway mode
     * @return a new message with the topic
     */
    public Message withType(
            final MessageType type,
            final boolean oneway
    ) {
        Objects.requireNonNull(type);
        return new Message(
                id, requestId,
                type, responseStatus, oneway, durable, subscriptionReply,
                destinationName, replyToQueueName, timestamp, expiresAt,
                timeout, subject, mimetype, charset, data);
  }

    /**
     * Change the type and response status of a message
     *
     * @param type a type
     * @param oneway oneway mode
     * @param id a a message id
     * @param responseStatus a response status
     * @return a new message with the topic
     */
    public Message withTypeAndResponseStatus(
            final MessageType type,
            final boolean oneway,
            final UUID id,
            final ResponseStatus responseStatus
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(id);
        Objects.requireNonNull(responseStatus);
        return new Message(
                id, requestId,
                type, responseStatus, oneway, durable, subscriptionReply,
                destinationName, replyToQueueName, timestamp, expiresAt,
                timeout, subject, mimetype, charset, data);
    }


    /**
     * Mark the message as subscription reply.
     *
     * @param subscriptionReply subscription reply
     * @return a new message with the subscription
     */
    public Message withSubscriptionReply(
            final boolean subscriptionReply
    ) {
        return new Message(
                id, requestId,
                type, responseStatus, oneway, durable, subscriptionReply,
                destinationName, replyToQueueName, timestamp, expiresAt,
                timeout, subject, mimetype, charset, data);
  }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public boolean isDurable() {
        return durable;
    }

    public boolean isSubscriptionReply() {
        return subscriptionReply;
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
    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean hasExpired() {
        return expiresAt >= 0 && expiresAt < System.currentTimeMillis();
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public String getReplyToQueueName() {
        return replyToQueueName;
    }

    @Override
    public LocalDateTime getTimestampAsLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getExpiresAtAsLocalDateTime() {
        return expiresAt < 0
                ? null
                : LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(expiresAt),
                    ZoneId.systemDefault());
    }

    @Override
    public long getMessageAge() {
        return System.currentTimeMillis() - timestamp;
    }

    @Override
    public String getSubject() {
        return subject;
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
        return isTextMessage()
                ? new String(data, Charset.forName(charset))
                : null;
    }

    @Override
    public VncVal getVeniceData() {
        if (isTextMessage()) {
            if ("application/json".equals(getMimetype())) {
                return Json.readJson(new String(data, Charset.forName(charset)), true);
            }
            else {
                throw new IpcException(
                        "A message with mimetype \"" + getMimetype()
                        + "\" can not be converted to Venice data!");
            }
        }
        else {
            throw new IpcException(
                    "A binary message can not be converted to Venice data!");
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
    public boolean hasSameId(final IMessage other) {
    	return other != null && Objects.equals(id, other.getId());
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
                   padRight("Request Id:", 12),
                   requestId == null ? "-" : requestId));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Type:", 12),
                   type.name()));

       sb.append(String.format(
                   "%s %b\n",
                   padRight("Durable:", 12),
                   durable));

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
                   padRight("Destination:", 12),
                   destinationName == null ? "-" : destinationName));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Reply Queue:", 12),
                   replyToQueueName == null ? "-" : replyToQueueName));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Timestamp:", 12),
                   getTimestampAsLocalDateTime()));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("ExpiresAt:", 12),
                   expiresAt < 0 ? "never" : getExpiresAtAsLocalDateTime()));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Timeout:", 12),
                   timeout < 0 ? "-" : String.valueOf(timeout) + "ms"));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Subject:", 12),
                   subject));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Mimetype:", 12),
                   mimetype));

       sb.append(String.format(
                   "%s %s\n",
                   padRight("Charset:", 12),
                   charset == null ? "-" : charset));

       sb.append(String.format(
                   "%s %s",
                   padRight("Data:", 12),
                   formatData()));

       return sb.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + (durable ? 1231 : 1237);
        result = prime * result + (int) (expiresAt ^ (expiresAt >>> 32));
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        result = prime * result + (oneway ? 1231 : 1237);
        result = prime * result + ((destinationName == null) ? 0 : destinationName.hashCode());
        result = prime * result + ((replyToQueueName == null) ? 0 : replyToQueueName.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((responseStatus == null) ? 0 : responseStatus.hashCode());
        result = prime * result + (subscriptionReply ? 1231 : 1237);
        result = prime * result + (int) (timeout ^ (timeout >>> 32));
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (charset == null) {
            if (other.charset != null)
                return false;
        } else if (!charset.equals(other.charset))
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        if (durable != other.durable)
            return false;
        if (expiresAt != other.expiresAt)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (mimetype == null) {
            if (other.mimetype != null)
                return false;
        } else if (!mimetype.equals(other.mimetype))
            return false;
        if (oneway != other.oneway)
            return false;
        if (destinationName == null) {
            if (other.destinationName != null)
                return false;
        } else if (!destinationName.equals(other.destinationName))
            return false;
        if (replyToQueueName == null) {
            if (other.replyToQueueName != null)
                return false;
        } else if (!replyToQueueName.equals(other.replyToQueueName))
            return false;
        if (requestId == null) {
            if (other.requestId != null)
                return false;
        } else if (!requestId.equals(other.requestId))
            return false;
        if (responseStatus != other.responseStatus)
            return false;
        if (subscriptionReply != other.subscriptionReply)
            return false;
        if (timeout != other.timeout)
            return false;
        if (timestamp != other.timestamp)
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        if (type != other.type)
            return false;
        return true;
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

    public static void validateSubject(final String subject) {
        if (subject.length() > Messages.SUBJECT_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A subject is limited to " + Messages.SUBJECT_MAX_LEN + "characters!");
        }
    }

    public static void validateMimetype(final String mimetype) {
        if (mimetype.length() > Messages.MIMETYPE_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A mimetype is limited to " + Messages.MIMETYPE_MAX_LEN + "characters!");
        }
    }

    public static void validateCharset(final String charset) {
        if (charset == null) {
            return;  // ok
        }

        if (StringUtil.isBlank(charset)) {
            throw new IllegalArgumentException(
                    "A charset can be null for binary messages but must "
                    + "not be empty or blank!");
        }

        if (charset.length() > Messages.CHARSET_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A charset is limited to " + Messages.CHARSET_MAX_LEN + "characters!");
        }
    }

    public static void validateQueueName(final String name) {
        if (name == null) {
            return; // ok
        }

        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("A queue name can be empty or blank!");
        }

        if (name.length() > Messages.QUEUENAME_MAX_LEN) {
            throw new IllegalArgumentException(
                    "A queue name is limited to " + Messages.QUEUENAME_MAX_LEN + "characters!");
        }
    }



    private final UUID id;
    private final String requestId;
    private final MessageType type;
    private final boolean durable;
    private final ResponseStatus responseStatus;
    private final boolean oneway;
    private final boolean subscriptionReply;
    private final String destinationName;  // used for offer/poll and publish/subscribe messages
    private final String replyToQueueName;  // used for offer/poll messages
    private final long timestamp;
    private final long expiresAt;
    private final long timeout;
    private final String subject;
    private final String mimetype;
    private final String charset;
    private final byte[] data;
}
