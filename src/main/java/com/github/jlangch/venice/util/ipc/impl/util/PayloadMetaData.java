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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.ipc.impl.Topics;


public class PayloadMetaData {

    public PayloadMetaData(
            final String queueName,
            final Topics topics,
            final String mimetype,
            final String charset
    ) {
        Objects.requireNonNull(topics);
        Objects.requireNonNull(mimetype);

        this.queueName = queueName;
        this.topics = topics;
        this.mimetype = mimetype;
        this.charset = charset;
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



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + ((topics == null) ? 0 : topics.hashCode());
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
        if (mimetype == null) {
            if (other.mimetype != null)
                return false;
        } else if (!mimetype.equals(other.mimetype))
            return false;
        if (queueName == null) {
            if (other.queueName != null)
                return false;
        } else if (!queueName.equals(other.queueName))
            return false;
        if (topics == null) {
            if (other.topics != null)
                return false;
        } else if (!topics.equals(other.topics))
            return false;
        return true;
    }


    public static byte[] encode(final PayloadMetaData data) {
        Objects.requireNonNull(data);

        final String s = StringUtil.trimToEmpty(data.queueName) + '\n' +
                         Topics.encode(data.topics)             + '\n' +
                         StringUtil.trimToEmpty(data.mimetype)  + '\n' +
                         StringUtil.trimToEmpty(data.charset);

        return s.getBytes(Charset.forName("UTF8"));
    }

    public static PayloadMetaData decode(final byte[] data) {
        Objects.requireNonNull(data);

        final String s = new String(data, Charset.forName("UTF8"));

        final List<String> lines = StringUtil.splitIntoLines(s);

        if (lines.size() == 3) {
            return new PayloadMetaData(
                    StringUtil.trimToNull(lines.get(0)),
                    Topics.decode(lines.get(1)),
                    lines.get(2),
                    null);
        }
        else if (lines.size() == 4) {
            return new PayloadMetaData(
                    StringUtil.trimToNull(lines.get(0)),
                    Topics.decode(lines.get(1)),
                    lines.get(2),
                    StringUtil.trimToNull(lines.get(3)));
        }
        else {
            throw new VncException(String.format(
                    "Failed to decode the payload meta data. Got only %d properties!",
                    lines.size()));
        }
    }


    private final String queueName;
    private final Topics topics;
    private final String mimetype;
    private final String charset;
}
