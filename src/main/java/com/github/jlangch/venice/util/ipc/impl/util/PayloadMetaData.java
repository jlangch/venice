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
package com.github.jlangch.venice.util.ipc.impl.util;

import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Topics;


public class PayloadMetaData {

    public PayloadMetaData(final Message msg) {
        this(
            msg.isOneway(),
            msg.isDurable(),
            msg.isSubscriptionReply(),
            msg.getRequestId(),
            msg.getType(),
            msg.getResponseStatus(),
            msg.getQueueName(),
            msg.getReplyToQueueName(),
            msg.getTopics(),
            msg.getMimetype(),
            msg.getCharset(),
            msg.getId());
    }

    public PayloadMetaData(
            final boolean oneway,
            final boolean durable,
            final boolean subscriptionReply,
            final String requestId,
            final MessageType type,
            final ResponseStatus responseStatus,
            final String queueName,
            final String replyToQueueName,
            final Topics topics,
            final String mimetype,
            final String charset,
            final UUID id
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(topics);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(id);

        this.oneway = oneway;
        this.durable = durable;
        this.subscriptionReply = subscriptionReply;
        this.requestId = requestId;
        this.type = type;
        this.responseStatus = responseStatus;
        this.queueName = queueName;
        this.replyToQueueName = replyToQueueName;
        this.topics = topics;
        this.mimetype = mimetype;
        this.charset = charset;
        this.id = id;
    }

    public PayloadMetaData(
            final String requestId,
            final String queueName,
            final String replyToQueueName,
            final Topics topics,
            final String mimetype,
            final String charset,
            final UUID id
    ) {
        Objects.requireNonNull(topics);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(id);

        this.oneway = false;
        this.durable = false;
        this.subscriptionReply = false;
        this.requestId = requestId;
        this.type = MessageType.NULL;
        this.responseStatus = ResponseStatus.NULL;
        this.queueName = queueName;
        this.replyToQueueName = replyToQueueName;
        this.topics = topics;
        this.mimetype = mimetype;
        this.charset = charset;
        this.id = id;
    }


    public boolean isOneway() {
        return oneway;
    }

    public boolean isDurable() {
        return durable;
    }

    public boolean isSubscriptionReply() {
        return subscriptionReply;
    }

    public String getRequestId() {
        return requestId;
    }

    public MessageType getType() {
        return type;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getReplyToQueueName() {
        return replyToQueueName;
    }

    public Topics getTopics() {
        return topics;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getCharset() {
        return charset;
    }

    public UUID getId() {
        return id;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + (durable ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        result = prime * result + (oneway ? 1231 : 1237);
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + ((replyToQueueName == null) ? 0 : replyToQueueName.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((responseStatus == null) ? 0 : responseStatus.hashCode());
        result = prime * result + (subscriptionReply ? 1231 : 1237);
        result = prime * result + ((topics == null) ? 0 : topics.hashCode());
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
        PayloadMetaData other = (PayloadMetaData) obj;
        if (charset == null) {
            if (other.charset != null)
                return false;
        } else if (!charset.equals(other.charset))
            return false;
        if (durable != other.durable)
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
        if (queueName == null) {
            if (other.queueName != null)
                return false;
        } else if (!queueName.equals(other.queueName))
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
        if (topics == null) {
            if (other.topics != null)
                return false;
        } else if (!topics.equals(other.topics))
            return false;
        if (type != other.type)
            return false;
        return true;
    }


    public static byte[] encode(final PayloadMetaData data) {
        Objects.requireNonNull(data);

        final byte   _oneway                 = encodeBoolean(data.oneway);
        final byte   _durable                = encodeBoolean(data.durable);
        final byte   _subscriptionReply      = encodeBoolean(data.subscriptionReply);
        final byte[] _requestId              = encodeString(data.requestId);
        final int    _type                   = encodeMessageType(data.type);
        final int    _responseStatus         = encodeResponseStatus(data.responseStatus);
        final byte[] _queueName              = encodeString(data.queueName);
        final byte[] _replyToQueueName       = encodeString(data.replyToQueueName);
        final byte[] _topics                 = encodeTopics(data.topics);
        final byte[] _mimetype               = encodeString(data.mimetype);
        final byte[] _charset                = encodeString(data.charset);
        final long   _idLeastSignificantBits = data.id.getLeastSignificantBits();
        final long   _idMostSignificantBits  = data.id.getMostSignificantBits();

        final int bytes = 1 + 1 + 1 +
                          4 + _requestId.length +
                          4 + 4 +
                          4 + _queueName.length +
                          4 + _replyToQueueName.length +
                          4 + _topics.length +
                          4 + _mimetype.length +
                          4 + _charset.length +
                          8 + 8;

        final ByteBuffer buf = ByteBuffer.allocate(bytes);

        buf.put(_oneway);
        buf.put(_durable);
        buf.put(_subscriptionReply);
        putString(buf, _requestId);
        buf.putInt(_type);
        buf.putInt(_responseStatus);
        putString(buf, _queueName);
        putString(buf, _replyToQueueName);
        putString(buf, _topics);
        putString(buf, _mimetype);
        putString(buf, _charset);
        buf.putLong(_idMostSignificantBits);
        buf.putLong(_idLeastSignificantBits);

        return buf.array();
    }

    public static PayloadMetaData decode(final byte[] data) {
        Objects.requireNonNull(data);

        final ByteBuffer buf = ByteBuffer.wrap(data);

        final byte   _oneway                 = buf.get();
        final byte   _durable                = buf.get();
        final byte   _subscriptionReply      = buf.get();
        final byte[] _requestId              = getString(buf);
        final int    _type                   = buf.getInt();
        final int    _responseStatus         = buf.getInt();
        final byte[] _queueName              = getString(buf);
        final byte[] _replyToQueueName       = getString(buf);
        final byte[] _topics                 = getString(buf);
        final byte[] _mimetype               = getString(buf);
        final byte[] _charset                = getString(buf);
        final long   _idMostSignificantBits  = buf.getLong();
        final long   _idLeastSignificantBits = buf.getLong();

        return new PayloadMetaData(
                decodeBoolean(_oneway),
                decodeBoolean(_durable),
                decodeBoolean(_subscriptionReply),
                decodeString(_requestId),
                decodeMessageType(_type),
                decodeResponseStatus(_responseStatus),
                decodeString(_queueName),
                decodeString(_replyToQueueName),
                decodeTopics(_topics),
                decodeString(_mimetype),
                decodeString(_charset),
                new UUID(_idMostSignificantBits, _idLeastSignificantBits));
    }

    private static byte encodeBoolean(final boolean b) {
        return b ? (byte)1 : (byte)0;
    }

    private static byte[] encodeString(final String s) {
        return s == null || s.isEmpty() ? new byte[0] : s.getBytes(UTF_8);
    }

    private static int encodeMessageType(final MessageType e) {
        return e.getValue();
    }

    private static int encodeResponseStatus(final ResponseStatus e) {
        return e.getValue();
    }

    private static byte[] encodeTopics(final Topics t) {
        final String s = Topics.encode(t);
        return s == null || s.isEmpty() ? new byte[0] : s.getBytes(UTF_8);
    }


    private static boolean decodeBoolean(final byte b) {
        return b == (byte)1;
    }

    private static String decodeString(final byte[] b) {
        return trimToNull(new String(b, UTF_8));
    }

    private static MessageType decodeMessageType(final int e) {
        return MessageType.fromCode(e);
    }

    private static ResponseStatus decodeResponseStatus(final int e) {
        return ResponseStatus.fromCode(e);
    }

    private static Topics decodeTopics(final byte[] b) {
        final String s = new String(b, UTF_8);
        return Topics.decode(s);
    }

    private static void putString(final ByteBuffer b, final byte[] sBuf) {
        b.putInt(sBuf.length);
        b.put(sBuf);
    }

    private static byte[] getString(final ByteBuffer b) {
        final int len = b.getInt();
        final byte[] buf = new byte[len];
        b.get(buf, 0, len);
        return buf;
    }


    private final boolean oneway;
    private final boolean durable;
    private final boolean subscriptionReply;
    private final String requestId;
    private final MessageType type;
    private final ResponseStatus responseStatus;
    private final String queueName;
    private final String replyToQueueName;
    private final Topics topics;
    private final String mimetype;
    private final String charset;
    private final UUID id;
}
