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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Message;


public class PayloadMetaData {

    public PayloadMetaData(final Message msg) {
        this(
            msg.isOneway(),
            msg.isDurable(),
            msg.isSubscriptionReply(),
            msg.getType(),
            msg.getTimestamp(),
            msg.getExpiresAt(),
            msg.getTimeout(),
            msg.getResponseStatus(),
            msg.getRequestId(),
            msg.getDestinationName(),
            msg.getReplyToQueueName(),
            msg.getSubject(),
            msg.getMimetype(),
            msg.getCharset(),
            msg.getId());
    }

    public PayloadMetaData(
            final boolean oneway,
            final boolean durable,
            final boolean subscriptionReply,
            final MessageType type,
            final long timestamp,
            final long expiresAt,
            final long timeout,
            final ResponseStatus responseStatus,
            final String requestId,
            final String destinationName,
            final String replyToQueueName,
            final String subject,
            final String mimetype,
            final String charset,
            final UUID id
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(id);

        this.oneway = oneway;
        this.durable = durable;
        this.subscriptionReply = subscriptionReply;
        this.type = type;
        this.timestamp = timestamp;
        this.expiresAt = expiresAt;
        this.timeout = timeout;
        this.responseStatus = responseStatus;
        this.requestId = requestId;
        this.destinationName = destinationName;
        this.replyToQueueName = replyToQueueName;
        this.subject = subject;
        this.mimetype = mimetype;
        this.charset = charset;
        this.id = id;
    }

    public Message toMessage(final byte[] payloadData) {
        return new Message(
                id,
                requestId,
                type,
                responseStatus,
                oneway,
                durable,
                subscriptionReply,
                destinationName,
                replyToQueueName,
                timestamp,
                expiresAt,
                timeout,
                subject,
                mimetype,
                charset,
                payloadData);
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

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public long getTimeout() {
        return timeout;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getReplyToQueueName() {
        return replyToQueueName;
    }

    public String getSubject() {
        return subject;
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
        PayloadMetaData other = (PayloadMetaData) obj;
        if (charset == null) {
            if (other.charset != null)
                return false;
        } else if (!charset.equals(other.charset))
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

    public static byte[] encode(final PayloadMetaData data) {
        Objects.requireNonNull(data);

        final byte   _oneway                 = encodeBoolean(data.oneway);
        final byte   _durable                = encodeBoolean(data.durable);
        final byte   _subscriptionReply      = encodeBoolean(data.subscriptionReply);
        final short  _type                   = encodeMessageType(data.type);
        final short  _responseStatus         = encodeResponseStatus(data.responseStatus);
        final byte[] _requestId              = encodeString(data.requestId);
        final byte[] _destinationName        = encodeString(data.destinationName);
        final byte[] _replyToQueueName       = encodeString(data.replyToQueueName);
        final byte[] _subject                = encodeString(data.subject);
        final byte[] _mimetype               = encodeString(data.mimetype);
        final byte[] _charset                = encodeString(data.charset);
        final long   _idLeastSignificantBits = data.id.getLeastSignificantBits();
        final long   _idMostSignificantBits  = data.id.getMostSignificantBits();

        final int bytes = 1 + 1 + 1 +
                          2 +
                          8 + 8 + 8 +
                          2 +
                          2 + _requestId.length +
                          2 + _destinationName.length +
                          2 + _replyToQueueName.length +
                          2 + _subject.length +
                          2 + _mimetype.length +
                          2 + _charset.length +
                          8 + 8;

        final ByteBuffer buf = ByteBuffer.allocate(bytes);

        buf.put(_oneway);
        buf.put(_durable);
        buf.put(_subscriptionReply);
        buf.putShort(_type);
        buf.putLong(data.timestamp);
        buf.putLong(data.expiresAt);
        buf.putLong(data.timeout);
        buf.putShort(_responseStatus);
        putString(buf, _requestId);
        putString(buf, _destinationName);
        putString(buf, _replyToQueueName);
        putString(buf, _subject);
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
        final short  _type                   = buf.getShort();
        final long   _timestamp              = buf.getLong();
        final long   _expiresAt              = buf.getLong();
        final long   _timeout                = buf.getLong();
        final short  _responseStatus         = buf.getShort();
        final byte[] _requestId              = getString(buf);
        final byte[] _queueOrTopicName       = getString(buf);
        final byte[] _replyToQueueName       = getString(buf);
        final byte[] _subject                = getString(buf);
        final byte[] _mimetype               = getString(buf);
        final byte[] _charset                = getString(buf);
        final long   _idMostSignificantBits  = buf.getLong();
        final long   _idLeastSignificantBits = buf.getLong();

        return new PayloadMetaData(
                decodeBoolean(_oneway),
                decodeBoolean(_durable),
                decodeBoolean(_subscriptionReply),
                decodeMessageType(_type),
                _timestamp,
                _expiresAt,
                _timeout,
                decodeResponseStatus(_responseStatus),
                decodeStringTrimToNull(_requestId),
                decodeStringTrimToNull(_queueOrTopicName),
                decodeStringTrimToNull(_replyToQueueName),
                decodeString(_subject),
                decodeStringTrimToNull(_mimetype),
                decodeStringTrimToNull(_charset),
                new UUID(_idMostSignificantBits, _idLeastSignificantBits));
    }

    private static byte encodeBoolean(final boolean b) {
        return b ? (byte)1 : (byte)0;
    }

    private static byte[] encodeString(final String s) {
        return s == null || s.isEmpty() ? new byte[0] : s.getBytes(UTF_8);
    }

    private static short encodeMessageType(final MessageType e) {
        return (short)e.getValue();
    }

    private static short encodeResponseStatus(final ResponseStatus e) {
        return (short)e.getValue();
    }


    private static boolean decodeBoolean(final byte b) {
        return b == (byte)1;
    }

    private static String decodeString(final byte[] b) {
        return new String(b, UTF_8);
    }

    private static String decodeStringTrimToNull(final byte[] b) {
        return trimToNull(new String(b, UTF_8));
    }

    private static MessageType decodeMessageType(final short e) {
        if (e >= 0 && e < types.length) {
            return types[e];
        }
        else {
            throw new IpcException("Illegal IPC message MessageType value: " + e);
        }
    }

    private static ResponseStatus decodeResponseStatus(final short e) {
        if (e >= 0 && e < status.length) {
            return status[e];
        }
        else {
            throw new IpcException("Illegal IPC message ResponseStatus value: " + e);
        }
    }

    private static void putString(final ByteBuffer b, final byte[] sBuf) {
        b.putShort((short)sBuf.length);
        b.put(sBuf);
    }

    private static byte[] getString(final ByteBuffer b) {
        final int len = b.getShort();
        final byte[] buf = new byte[len];
        b.get(buf, 0, len);
        return buf;
    }



    private final static MessageType[] types = new MessageType[100];
    private final static ResponseStatus[] status = new ResponseStatus[100];

    static {
        for(MessageType m : MessageType.values()) {
            if (m.getValue() < 100) types[m.getValue()] = m;
        }
        for(ResponseStatus s : ResponseStatus.values()) {
           if (s.getValue() < 100) status[s.getValue()] = s;
        }
    }


    private final boolean oneway;
    private final boolean durable;
    private final boolean subscriptionReply;
    private final MessageType type;
    private final long timestamp;
    private final long expiresAt;
    private final long timeout;
    private final ResponseStatus responseStatus;
    private final String requestId;
    private final String destinationName;
    private final String replyToQueueName;
    private final String subject;
    private final String mimetype;
    private final String charset;
    private final UUID id;
}
