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
package com.github.jlangch.venice.impl.util.ipc;

import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.VncException;


public class Message {

    Message(
            final Status status,
            final String mimetype,
            final String charset,
            final byte[] data
    ) {
        this.status = status;
        this.mimetype = mimetype;
        this.charset = charset;
        this.data = data;
    }

    public static Message text(
            final Status status,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                status,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }

    public static Message binary(
            final Status status,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                status,
                mimetype,
                null,
                data);
    }

    public static Message hello() {
        return Message.text(Status.REQUEST, "text/plain", "UTF-8", "Hello!");
    }

    public Message echo() {
        return new Message(Status.RESPONSE_OK, mimetype, charset, data);
    }


    public Status getStatus() {
        return status;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getCharset() {
        return charset;
    }

    public byte[] getData() {
        return data;
    }

    public String getText() {
        if (charset != null) {
            return new String(data, Charset.forName(charset));
        }
        else {
            throw new VncException("A binary message can be converted to text data!");
        }
    }


    private final Status status;
    private final String mimetype;
    private final String charset;
    private final byte[] data;
}
