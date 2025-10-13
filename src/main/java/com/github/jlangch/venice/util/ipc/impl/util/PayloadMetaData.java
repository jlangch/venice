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
package com.github.jlangch.venice.util.ipc.impl.util;

import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.Topics;


public class PayloadMetaData {

    public PayloadMetaData(final Message msg) {
        this(
            msg.isOneway(),
            msg.getType(),
            msg.getResponseStatus(),
            msg.getQueueName(),
            msg.getTopics(),
            msg.getMimetype(),
            msg.getCharset(),
            msg.getId());
    }

    public PayloadMetaData(
            final boolean oneway,
            final MessageType type,
            final ResponseStatus responseStatus,
            final String queueName,
            final Topics topics,
            final String mimetype,
            final String charset,
            final String id
    ) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(responseStatus);
        Objects.requireNonNull(topics);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(id);

        this.oneway = oneway;
        this.type = type;
        this.responseStatus = responseStatus;
        this.queueName = queueName;
        this.topics = topics;
        this.mimetype = mimetype;
        this.charset = charset;
        this.id = id;
    }

    public PayloadMetaData(
            final String queueName,
            final Topics topics,
            final String mimetype,
            final String charset,
            final String id
    ) {
        Objects.requireNonNull(topics);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(id);

        this.oneway = false;
        this.type = MessageType.NULL;
        this.responseStatus = ResponseStatus.NULL;
        this.queueName = queueName;
        this.topics = topics;
        this.mimetype = mimetype;
        this.charset = charset;
        this.id = id;
    }


    public boolean isOneway() {
        return oneway;
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

    public Topics getTopics() {
        return topics;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getCharset() {
        return charset;
    }

    public String getId() {
        return id;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        result = prime * result + (oneway ? 1231 : 1237);
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + ((responseStatus == null) ? 0 : responseStatus.hashCode());
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
        if (responseStatus != other.responseStatus)
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

        final String s = (data.oneway ? "1" : "0")    + '\n' +
                         toCode(data.type)            + '\n' +
                         toCode(data.responseStatus)  + '\n' +
                         trimToEmpty(data.queueName)  + '\n' +
                         Topics.encode(data.topics)   + '\n' +
                         trimToEmpty(data.mimetype)   + '\n' +
                         trimToEmpty(data.charset)    + '\n' +
                         data.id.toString();

        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static PayloadMetaData decode(final byte[] data) {
        Objects.requireNonNull(data);

        final String s = new String(data, StandardCharsets.UTF_8);

        final List<String> lines = StringUtil.splitIntoLines(s);

        if (lines.size() == 8) {
            return new PayloadMetaData(
                    toBool(lines.get(0)),             // oneway
                    toMessageType(lines.get(1)),      // message type
                    toResponseStatus(lines.get(2)),   // respone status
                    trimToNull(lines.get(3)),         // queueName
                    Topics.decode(lines.get(4)),      // topics
                    lines.get(5),                     // mimetype
                    trimToNull(lines.get(6)),         // charset
                    lines.get(7));                    // id
        }
        else {
            throw new VncException(String.format(
                    "Failed to decode the payload meta data. Got only %d properties instead of 8!",
                    lines.size()));
        }
    }


    private static boolean toBool(final String s) {
        return s.equals("1");
    }

    private static String toCode(final MessageType e) {
        return String.valueOf(e.getValue());
    }

    private static String toCode(final ResponseStatus e) {
        return String.valueOf(e.getValue());
    }

    private static MessageType toMessageType(final String code) {
        MessageType t = MessageType.fromCode(Integer.parseInt(code));
        if (t == null) {
            throw new VncException("Illegal IPC message MessageType value");
        }
        else {
            return t;
        }
    }

    private static ResponseStatus toResponseStatus(final String code) {
        ResponseStatus s = ResponseStatus.fromCode(Integer.parseInt(code));
        if (s == null) {
            throw new VncException("Illegal IPC message ResponseStatus value");
        }
        else {
            return s;
        }
   }


    private final boolean oneway;
    private final MessageType type;
    private final ResponseStatus responseStatus;
    private final String queueName;
    private final Topics topics;
    private final String mimetype;
    private final String charset;
    private final String id;
}
