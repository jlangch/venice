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
package com.github.jlangch.venice.util.ipc;

import java.nio.charset.Charset;
import java.util.Objects;

import com.github.jlangch.venice.util.ipc.impl.Message;


public abstract class MessageFactory {

    /**
     * Create a text message
     *
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final String topic,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        return new Message(
                Status.REQUEST,  // just a placeholder, will be set accordingly by
                                 // the TcpClient/TcpServer based on the request/response
                                 // context
                topic,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a binary message
     *
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final String topic,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        return new Message(
                Status.REQUEST,  // just a placeholder, will be set accordingly by
                                 // the TcpClient/TcpServer based on the request/response
                                 // context
                topic,
                mimetype,
                null,
                data);
    }


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
        return text("hello", "text/plain", "UTF-8", "Hello!");
    }

}
