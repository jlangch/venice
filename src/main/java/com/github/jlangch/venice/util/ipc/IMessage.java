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

import java.time.LocalDateTime;

/**
 * Defines the messages that can be exchanged between a TcpClient and a TcpServer.
 */
public interface IMessage {

    /**
     * @return the message status
     */
    Status getStatus();

    /**
     * @return the message timestamp (milliseconds since epoch)
     */
    long getTimestamp();

    /**
     * @return the message timestamp as LocalDateTime
     */
    LocalDateTime getTimestampAsLocalDateTime();

    /**
     * @return the message topic
     */
    String getTopic();

    /**
     * @return the message mimetype
     */
    String getMimetype();

    /**
     * @return the message's payload data charset if the payload data is of
     *         type string else <code>null</code>
     */
    String getCharset();

    /**
     * @return the message binary payload data
     */
    byte[] getData();

    /**
     * @return the message textual payload data. Throws a VncException if the payload is binary data.
     */
    String getText() ;

    /**
     * @return <code>true</code> if this message is a text message, else <code>false</code>
     */
    boolean isTextMessage();

    /**
     * @return <code>true</code> if this message is a binary message, else <code>false</code>
     */
    boolean isBinaryMessage();

    /**
     * Returns an echo response for this message with the status RESPONSE_OK
     *
     * @return the echo response
     */
    IMessage asEchoResponse();


}
