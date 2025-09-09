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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.ipc.impl.Message;


public abstract class MessageFactory {

    /**
     * Create a text message
     *
     * <p>Acceptable status:
     * <ul>
     *   <li>REQUEST - to create a request text message</li>
     *   <li>REQUEST_ONE_WAY - to create a request text message with one-way send</li>
     *   <li>RESPONSE_OK - to create an ok response text message from a TcpServer handler</li>
     *   <li>RESPONSE_BAD_REQUEST - to create an error response text message from a TcpServer handler</li>
     *   <li>RESPONSE_HANDLER_ERROR - to create an error response text message from a TcpServer handler</li>
     * </ul>
     *
     * @param status the message's status
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param charset the charset of the message's payload data
     * @param data the textual payload data
     * @return the message
     */
    public static IMessage text(
            final Status status,
            final String topic,
            final String mimetype,
            final String charset,
            final String data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(data);

        validateMessageStatus(status);

        return new Message(
                status,
                topic,
                mimetype,
                charset,
                data.getBytes(Charset.forName(charset)));
    }


    /**
     * Create a binary message
     *
     * <p>Acceptable status:
     * <ul>
     *   <li>REQUEST - to create a request binary message</li>
     *   <li>REQUEST_ONE_WAY - to create a request binary message with one-way send</li>
     *   <li>RESPONSE_OK - to create an ok response binary message from a TcpServer handler</li>
     *   <li>RESPONSE_BAD_REQUEST - to create an error response binary message from a TcpServer handler</li>
     *   <li>RESPONSE_HANDLER_ERROR - to create an error response binary message from a TcpServer handler</li>
     * </ul>
     *
     * @param status the message's status
     * @param topic a topic
     * @param mimetype the mimetype of the message's payload data
     * @param data the binary payload data
     * @return the message
     */
    public static IMessage binary(
            final Status status,
            final String topic,
            final String mimetype,
            final byte[] data
    ) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(mimetype);
        Objects.requireNonNull(data);

        validateMessageStatus(status);

        return new Message(
                status,
                topic,
                mimetype,
                null,
                data);
    }


    /**
     * Create a simple hello request message (with status 'REQUEST').
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
        return text(Status.REQUEST, "hello", "text/plain", "UTF-8", "Hello!");
    }


    /**
     * Create an echo response for a request message.
     *
     * @param request the request message
     * @return the response message
     */
    public static IMessage asEchoMessage(final IMessage request) {
        return ((Message)request).asEchoResponse();
    }


    private static void validateMessageStatus(final Status status) {
        if (!(status == Status.REQUEST
              || status == Status.REQUEST_ONE_WAY
              || status == Status.RESPONSE_OK
              || status == Status.RESPONSE_BAD_REQUEST
              || status == Status.RESPONSE_HANDLER_ERROR)
        ) {
            throw new VncException(
                    String.format(
                        "Unacceptable message status '%s'! " +
                        "Use 'REQUEST', 'REQUEST_ONE_WAY', " +
                        "'RESPONSE_OK', 'RESPONSE_BAD_REQUEST', " +
                        "or 'RESPONSE_HANDLER_ERROR'.",
                        status));
        }
    }
}
