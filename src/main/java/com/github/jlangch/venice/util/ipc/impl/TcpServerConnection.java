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

import static com.github.jlangch.venice.util.ipc.Status.REQUEST;
import static com.github.jlangch.venice.util.ipc.Status.REQUEST_ONE_WAY;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_BAD_REQUEST;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_HANDLER_ERROR;
import static com.github.jlangch.venice.util.ipc.Status.RESPONSE_OK;

import java.nio.channels.SocketChannel;
import java.util.function.Function;

import com.github.jlangch.venice.util.ipc.Message;
import com.github.jlangch.venice.util.ipc.TcpServer;


public class TcpServerConnection implements Runnable {

    public TcpServerConnection(
            final TcpServer server,
            final SocketChannel ch,
            final Function<Message,Message> handler
    ) {
        this.server = server;
        this.ch = ch;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            while(server.isRunning() && ch.isOpen()) {
                final Message request = Protocol.receiveMessage(ch);
                if (request == null) {
                    // client closed connection
                    break;
                }

                if (!server.isRunning()) break;

                // send an error back if the request message is not a request
                if (!(isRequestMsg(request) || isRequestOneWayMsg(request))) {
                    Protocol.sendMessage(
                        ch,
                        Message.text(
                           RESPONSE_BAD_REQUEST,
                           request.getTopic(),
                           "text/plain",
                           "UTF-8",
                           "Bad request status: " + request.getStatus().name()));
                    continue;
                }

                // Process request

                Message response = null;
                try {
                    response = handler.apply(request);

                    if (response == null && isRequestMsg(request)) {
                        // send an empty ok response back
                        response = Message.text(
                                     RESPONSE_OK,
                                     request.getTopic(),
                                     "text/plain",
                                     "UTF-8",
                                     "");
                    }
                }
                catch(Exception ex) {
                    // do not send an error back for a request of type REQUEST_ONE_WAY
                    response = isRequestMsg(request)
                                ? Message.text(
                                    RESPONSE_HANDLER_ERROR,
                                    request.getTopic(),
                                    "text/plain",
                                    "UTF-8",
                                    ExceptionUtil.printStackTraceToString(ex))
                                : null;
                }

                if (!server.isRunning()) break;

                if (response != null) {
                    Protocol.sendMessage(ch, response);
                }
            }
        }
        catch(Exception ex) {
            // when client closed the connection -> java.io.IOException: Broken pipe
            // -> quit
        }
        finally {
            IO.safeClose(ch);
        }
    }

    private static boolean isRequestMsg(final Message msg) {
        return msg.getStatus() == REQUEST;
    }

    private static boolean isRequestOneWayMsg(final Message msg) {
        return msg.getStatus() == REQUEST_ONE_WAY;
    }


    private final TcpServer server;
    private final SocketChannel ch;
    private final Function<Message,Message> handler;
}
