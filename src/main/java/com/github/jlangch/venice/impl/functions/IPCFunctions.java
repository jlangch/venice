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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncByteBuffer;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageFactory;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.TcpClient;
import com.github.jlangch.venice.util.ipc.TcpServer;
import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public class IPCFunctions {

    public static VncFunction ipc_server =
        new VncFunction(
                "ipc/server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server port handler & options)")
                    .doc(
                        "Create a new server on the specified port.\n\n" +
                        "*Arguments:* \n\n" +
                        "| port p    | The TCP/IP port |\n" +
                        "| handler h | A single argument handler function.¶" +
                                     " E.g.: a simple echo handler: `(fn [m] m)`.¶" +
                                     " The handler receives the request messsage and returns a response" +
                                     " message. In case of a one-way request message the server discards" +
                                     " the handler's response if it is not `nil`.|\n\n" +
                        "*Options:* \n\n" +
                        "| :max-connections n      | The number of the max connections the server can handle" +
                                                   " in parallel. Defaults to 20.|\n" +
                        "| :max-message-size n     | The max size of the message payload." +
                                                   " Defaults to `200MB`.¶" +
                                                   " The max size can be specified as a number like `20000`" +
                                                   " or a number with a unit like `:20KB` or `:20MB`|\n" +
                        "| :compress-cutoff-size n | The compression cutoff size for payload messages.¶" +
                                                   " With a negative cutoff size payload messages will not be" +
                                                   " compressed. If the payload message size is greater than the cutoff" +
                                                   " size it will be compressed.¶" +
                                                   " Defaults to -1 (no compression)¶" +
                                                   " The cutoff size can be specified as a number like `1000`" +
                                                   " or a number with a unit like `:1KB` or `:2MB`|\n\n" +
                        "**The server must be closed after use!**")
                    .examples(
                        "(do                                                     \n" +
                        "  (defn echo-handler [m]                                \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))       \n" +
                        "    m)                                                  \n" +
                        "                                                        \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)     \n" +
                        "             client (ipc/client \"localhost\" 33333)]   \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")    \n" +
                        "         (ipc/send client)                              \n" +
                        "         (ipc/message->map)                             \n" +
                        "         (println \"RESPONSE: \"))))                    ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-oneway",
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json",
                        "ipc/create-queue",
                        "ipc/remove-queue",
                        "ipc/server-status",
                        "ipc/server-thread-pool-statistics")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final int port = Coerce.toVncLong(args.first()).getIntValue();
                final VncFunction handler = Coerce.toVncFunction(args.second());

                final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                final VncVal maxConnVal = options.get(new VncKeyword("max-connections"));
                final VncVal maxMsgSizeVal = options.get(new VncKeyword("max-message-size"));
                final VncVal compressCutoffSizeVal = options.get(new VncKeyword("compress-cutoff-size"));

                final int maxConn = maxConnVal == Nil
                                        ? 0
                                        : Coerce.toVncLong(maxConnVal).getIntValue();

                final long maxMsgSize = convertMaxMessageSizeToLong(maxMsgSizeVal);
                final long compressCutoffSize = convertMaxMessageSizeToLong(compressCutoffSizeVal);

                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(handler) };

                // Create a wrapper that inherits the Venice thread context!
                final ThreadBridge threadBridge = ThreadBridge.create("tcp-server-handler", cf);

                final Function<IMessage,IMessage> handlerWrapper = threadBridge.bridgeFunction((IMessage m) -> {
                          final VncVal request = new VncJavaObject(m);
                          final VncVal response = handler.applyOf(request);
                          return Coerce.toVncJavaObject(response, IMessage.class);
                });

                final TcpServer server = new TcpServer(port);

                if (maxConn > 0) {
                    server.setMaximumParallelConnections(maxConn);
                }

                if (maxMsgSize > 0) {
                    server.setMaximumMessageSize(maxMsgSize);
                }

                if (compressCutoffSize >= 0) {
                    server.setCompressCutoffSize(compressCutoffSize);
                }

                server.start(handlerWrapper);

                return new VncJavaObject(server);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_client =
        new VncFunction(
                "ipc/client",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/client port)",
                        "(ipc/client host port & options)")
                    .doc(
                        "Create a new client connecting to a server on the specified " +
                        "host and port.\n\n" +
                        "*Arguments:* \n\n" +
                        "| port p | The server's TCP/IP port |\n" +
                        "| host h | The server's TCP/IP host |\n\n" +
                        "*Options:* \n\n" +
                        "| :max-message-size n     | The max size of the message payload." +
                                                   " Defaults to `200MB`.¶" +
                                                   " The max size can be specified as a number like `20000`" +
                                                   " or a number with a unit like `:20KB` or `:20MB`|\n" +
                        "| :compress-cutoff-size n | The compression cutoff size for payload messages.¶" +
                                                   " With a negative cutoff size payload messages will not be" +
                                                   " compressed. If the payload message size is greater than the" +
                                                   " cutoff size it will be compressed.¶" +
                                                   " Defaults to -1 (no compression)¶" +
                                                   " The cutoff size can be specified as a number like `1000`" +
                                                   " or a number with a unit like `:1KB` or `:2MB`|\n" +
                        "| :encrypt b              | If `true` encrypt the payload data of all messages exchanged" +
                                                   " between this client and its associated server.¶" +
                                                   " The data is AES-256-GCM encrypted using a secret that is" +
                                                   " created and exchanged using the Diffie-Hellman key exchange " +
                                                   " algorithm.|\n\n" +
                        "**The client is NOT thread safe!**" +
                        "**The client must be closed after use!**")
                    .examples(
                        "(do                                                                             \n" +
                        "  (defn echo-handler [m]                                                        \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))                               \n" +
                        "    m)                                                                          \n" +
                        "                                                                                \n" +
                        "  (defn send [client msg]                                                       \n" +
                        "    (->> (ipc/send client msg)                                                  \n" +
                        "         (ipc/message->map)                                                     \n" +
                        "         (println \"RESPONSE: \")))                                             \n" +
                        "                                                                                \n" +
                        "  (try-with [server   (ipc/server 33333 echo-handler)                           \n" +
                        "             client-1 (ipc/client 33333)                                        \n" +
                        "             client-2 (ipc/client \"localhost\" 33333 :compress-cutoff-size 0)  \n" +
                        "             client-3 (ipc/client :localhost 33333 :encrypt true)]              \n" +
                        "    (send client-1 (ipc/plain-text-message \"test\" \"hello\"))                 \n" +
                        "    (send client-2 (ipc/plain-text-message \"test\" \"hello\"))                 \n" +
                        "    (send client-3 (ipc/plain-text-message \"test\" \"hello\"))))               ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-oneway",
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/offer",
                        "ipc/poll",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                if ( args.size() == 1) {
                    final int port = Coerce.toVncLong(args.first()).getIntValue();

                    final TcpClient client = new TcpClient(port);
                    client.open();
                    return new VncJavaObject(client);
                }
                else {
                    final String host = Types.isVncKeyword(args.first())
                                          ? Coerce.toVncKeyword(args.first()).getSimpleName()
                                          : Coerce.toVncString(args.first()).getValue();
                    final int port = Coerce.toVncLong(args.second()).getIntValue();

                    final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                    final VncVal maxMsgSizeVal = options.get(new VncKeyword("max-message-size"));
                    final VncVal compressCutoffSizeVal = options.get(new VncKeyword("compress-cutoff-size"));
                    final VncVal encryptVal = options.get(new VncKeyword("encrypt"), VncBoolean.False);

                    final long maxMsgSize = convertMaxMessageSizeToLong(maxMsgSizeVal);
                    final long compressCutoffSize = convertMaxMessageSizeToLong(compressCutoffSizeVal);
                    final boolean encrypt = Coerce.toVncBoolean(encryptVal).getValue();

                    final TcpClient client = new TcpClient(host, port, encrypt);

                    if (maxMsgSize > 0) {
                        client.setMaximumMessageSize(maxMsgSize);
                    }

                    if (compressCutoffSize >= 0) {
                        client.setCompressCutoffSize(compressCutoffSize);
                    }

                    client.open();

                    return new VncJavaObject(client);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_clone =
        new VncFunction(
                "ipc/clone",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/clone client)")
                    .doc(
                        "Clone a client with all its configuration")
                    .examples(
                        "(do                                                                             \n" +
                        "  (defn echo-handler [m]                                                        \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))                               \n" +
                        "    m)                                                                          \n" +
                        "                                                                                \n" +
                        "  (defn send [client msg]                                                       \n" +
                        "    (->> (ipc/send client msg)                                                  \n" +
                        "         (ipc/message->map)                                                     \n" +
                        "         (println \"RESPONSE: \")))                                             \n" +
                        "                                                                                \n" +
                        "  (try-with [server   (ipc/server 33333 echo-handler)                           \n" +
                        "             client-1 (ipc/client \"localhost\" 33333 :encrypted true)          \n" +
                        "             client-2 (ipc/clone client-1)                                      \n" +
                        "             client-3 (ipc/clone client-1)]                                     \n" +
                        "    (send client-1 (ipc/plain-text-message \"test\" \"hello 1\"))               \n" +
                        "    (send client-2 (ipc/plain-text-message \"test\" \"hello 2\"))               \n" +
                        "    (send client-3 (ipc/plain-text-message \"test\" \"hello 3\"))))             ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);

                final TcpClient cloned = (TcpClient)client.clone();
                cloned.open();

                return new VncJavaObject(cloned);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_runnningQ =
        new VncFunction(
                "ipc/running?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/running? server)",
                        "(ipc/running? client)")
                    .doc(
                        "Return `true` if the server or client is running else `false`")
                    .examples(
                        "(do                                                         \n" +
                        "  (defn echo-handler [m] m)                                 \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)         \n" +
                        "             client (ipc/client \"localhost\" 33333)]       \n" +
                        "    (println \"Server running:\" (ipc/running? server))     \n" +
                        "    (println \"Client running:\" (ipc/running? client))))   ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/server",
                        "ipc/close")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate =  Coerce.toVncJavaObject(args.first()).getDelegate();
                if (delegate instanceof TcpServer) {
                    return VncBoolean.of(((TcpServer)delegate).isRunning());
                }
                else if (delegate instanceof TcpClient) {
                    return VncBoolean.of(((TcpClient)delegate).isRunning());
                }
                else {
                    throw new VncException(
                           "Function 'ipc/running?' expects either a TcpServer or a TcpClient!");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_close =
        new VncFunction(
                "ipc/close",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/close server)",
                        "(ipc/close client)")
                    .doc(
                        "Closes a server or client")
                    .examples(
                        ";; prefer try-with-resources to safely close server and client    \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (println \"Server running:\" (ipc/running? server))           \n" +
                        "    (println \"Client running:\" (ipc/running? client))))         ",

                        ";; explicitly closing server and client                           \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (let [server (ipc/server 33333 echo-handler)                    \n" +
                        "        client (ipc/client \"localhost\" 33333)]                  \n" +
                        "    (println \"Server running:\" (ipc/running? server))           \n" +
                        "    (println \"Client running:\" (ipc/running? client))           \n" +
                        "    (ipc/close client)                                            \n" +
                        "    (ipc/close server)))                                           ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/server",
                        "ipc/running?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object delegate =  Coerce.toVncJavaObject(args.first()).getDelegate();
                if (delegate instanceof TcpServer) {
                    try {
                        ((TcpServer)delegate).close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close IPC server", ex);
                    }
                    return Nil;
                }
                else if (delegate instanceof TcpClient) {
                    try {
                        ((TcpClient)delegate).close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close IPC client", ex);
                    }
                    return Nil;
                }
                else {
                    throw new VncException(
                           "Function 'ipc/close' expects either a TcpServer or a TcpClient!");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Send / Receive
    // ------------------------------------------------------------------------

    public static VncFunction ipc_send =
        new VncFunction(
                "ipc/send",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/send client message)",
                        "(ipc/send client timeout message)")
                    .doc(
                        "Sends a message to the server the client is associated with. \n\n" +
                        "The optional timeout is given in milliseconds.\n\n" +
                        "Returns the server's response message or `nil` if the message is " +
                        "declared as one-way message. Throws a timeout exception if the " +
                        "response is not received within the timeout time.\n\n" +
                        "The response message has one of these status:\n\n" +
                        "  * `:OK`            - request handled successfully and response holds the data\n" +
                        "  * `:SERVER_ERROR`  - indicates a server side error while processing the request \n" +
                        "  * `:BAD_REQUEST`   - invalid request, details in the payload\n" +
                        "  * `:HANDLER_ERROR` - an error in the server's request processing handler\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c  | A client to send the message from|\n" +
                        "| timeout t | A timeout in milliseconds for receiving the response|\n" +
                        "| message m | The message to send|")
                    .examples(
                        ";; echo handler                                                   \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"              \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler processing JSON message data                           \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [data   (json/read-str (. m :getText))                   \n" +
                        "          result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "      (ipc/text-message (. m :getTopic)                           \n" +
                        "                        \"application/json\" :UTF-8               \n" +
                        "                        result)))                                 \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/text-message \"test\"                               \n" +
                        "                           \"application/json\" :UTF-8            \n" +
                        "                           (json/write-str {\"x\" 100 \"y\" 200}))\n" +
                        "         (ipc/send client 2000)                                   \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler with remote code execution                             \n" +
                        ";; request: \"(+ 1 2)\" => exec => response: \"3\"                \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [cmd    (. m :getText)                                   \n" +
                        "          result (str (eval (read-string cmd)))]                  \n" +
                        "      (ipc/plain-text-message (. m :getTopic)                     \n" +
                        "                              result)))                           \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"exec\" \"(+ 1 2)\")            \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ")
                    .seeAlso(
                        "ipc/send-async",
                        "ipc/send-oneway",
                        "ipc/client",
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final boolean hasTimeout =  args.size() > 2;

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final long timeout = hasTimeout ? Coerce.toVncLong(args.nth(1)).toJavaLong() : 0;
                final IMessage request = Coerce.toVncJavaObject(args.nth(hasTimeout ? 2 : 1), IMessage.class);

                if (timeout <= 0) {
                    final IMessage response = client.sendMessage(request);
                    return response == null ? Nil : new VncJavaObject(response);
                }
                else {
                    final IMessage response = client.sendMessage(request, timeout, TimeUnit.MILLISECONDS);
                    return response == null ? Nil : new VncJavaObject(response);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_send_async =
        new VncFunction(
                "ipc/send-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/send-async client message)")
                    .doc(
                        "Sends a message to the server the client is associated with. \n\n" +
                        "Returns a future with the server's response message. The response " +
                        "message is `nil` if the message is declared as one-way message.\n\n" +
                        "The response message has one of these status:\n\n" +
                        "  * `:OK`            - request handled successfully and response holds the data\n" +
                        "  * `:SERVER_ERROR`  - indicates a server side error while processing the request \n" +
                        "  * `:BAD_REQUEST`   - invalid request, details in the payload\n" +
                        "  * `:HANDLER_ERROR` - an error in the server's request processing handler\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c  | A client to send the message from|\n" +
                        "| message m | The message to send|")
                    .examples(
                        ";; echo handler                                                   \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"              \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send-async client)                                  \n" +
                        "         (deref)                                                  \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler processing JSON message data                           \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [data   (json/read-str (. m :getText))                   \n" +
                        "          result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "      (ipc/text-message (. m :getTopic)                           \n" +
                        "                        \"application/json\" :UTF-8               \n" +
                        "                        result)))                                 \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/text-message \"test\"                               \n" +
                        "                           \"application/json\" :UTF-8            \n" +
                        "                           (json/write-str {\"x\" 100 \"y\" 200}))\n" +
                        "         (ipc/send-async client)                                  \n" +
                        "         (deref)                                                  \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler with remote code execution                             \n" +
                        ";; request: \"(+ 1 2)\" => exec => response: \"3\"                \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [cmd    (. m :getText)                                   \n" +
                        "          result (str (eval (read-string cmd)))]                  \n" +
                        "      (ipc/plain-text-message (. m :getTopic)                     \n" +
                        "                              result)))                           \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"exec\" \"(+ 1 2)\")            \n" +
                        "         (ipc/send-async client)                                  \n" +
                        "         (deref)                                                  \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ")
                    .seeAlso(
                        "ipc/send",
                        "ipc/send-oneway",
                        "ipc/client",
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final IMessage request = Coerce.toVncJavaObject(args.second(), IMessage.class);

                return new VncJavaObject(
                        new FutureWrapper(
                            client.sendMessageAsync(request)));
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_send_oneway =
        new VncFunction(
                "ipc/send-oneway",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/send-oneway client message)")
                    .doc(
                        "Sends a one-way message to the server the client is associated with. \n\n" +
                        "Does not wait for response and returns always `nil`.")
                    .examples(
                        "(do                                                                               \n" +
                        "  ;; thread-safe printing                                                         \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))               \n" +
                        "                                                                                  \n" +
                        "  (defn handler [m]                                                               \n" +
                        "    (println \"REQUEST:\" (ipc/message->json true m))                             \n" +
                        "    nil)                                                                          \n" +
                        "                                                                                  \n" +
                        "  (try-with [server (ipc/server 33333 handler)                                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]                             \n" +
                        "    ;; send a plain text messages:                                                \n" +
                        "    ;;    requestId=\"1\" and \"2\", topic=\"test\", payload=\"hello\"            \n" +
                        "    (ipc/send-oneway client (ipc/plain-text-message \"1\" \"test\" \"hello\"))    \n" +
                        "    (ipc/send-oneway client (ipc/plain-text-message \"2\" \"test\" \"hello\"))))  ")
                    .seeAlso(
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/client",
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final IMessage request = Coerce.toVncJavaObject(args.second(), IMessage.class);

                client.sendMessageOneway(request);

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    // ------------------------------------------------------------------------
    // Publish / Subscribe
    // ------------------------------------------------------------------------

    public static VncFunction ipc_subscribe =
        new VncFunction(
                "ipc/subscribe",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/subscribe client topic msg-handler)")
                    .doc(
                        "Subscribe to a topic.\n\n" +
                        "Puts this client into subscription mode and listens for messages of the " +
                        "specified topic.\n\n" +
                        "To unsubscribe from the topics just close the client.\n\n" +
                        "Returns the server's response message.\n\n" +
                        "The response message has one of these status:\n\n" +
                        "  * `:OK`            - subscription added. Subscribed messages will be delivered through the 'msg-handler'\n" +
                        "  * `:SERVER_ERROR`  - indicates a server side error while processing the request\n" +
                        "  * `:BAD_REQUEST`   - invalid request, details in the payload")
                    .examples(
                        "(do                                                                  \n" +
                        "  ;; thread-safe printing                                            \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))  \n" +
                        "                                                                     \n" +
                        "  ;; the server handler is not involved with publish/subscribe!      \n" +
                        "  (defn echo-handler [m] m)                                          \n" +
                        "                                                                     \n" +
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                  \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic 'test'              \n" +
                        "    (ipc/subscribe client1 \"test\" client-subscribe-handler)        \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", topic=\"test\", payload=\"hello\"          \n" +
                        "    (let [m (ipc/plain-text-message \"1\" \"test\" \"hello\")]       \n" +
                        "      (println \"PUBLISHED:\" (ipc/message->json true m))            \n" +
                        "      (ipc/publish client2 m))                                       \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/publish",
                        "ipc/publish-async",
                        "ipc/client",
                        "ipc/server",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final VncVal topicVal = args.nth(1);
                final VncFunction handler = Coerce.toVncFunction(args.nth(2));

                final HashSet<String> topics = new HashSet<>();
                if (Types.isVncString(topicVal)) {
                    topics.add(Coerce.toVncString(topicVal).getValue());
                }
                else if (Types.isVncSequence(topicVal)) {
                    Coerce.toVncSequence(topicVal).forEach(t -> {
                        if (Types.isVncString(t)) {
                            topics.add(Coerce.toVncString(t).getValue());
                        }
                        else {
                            throw new VncException(
                                    "Function 'ipc/subscribe' expects either a single string "
                                    + "topic or a sequence of topic strings!");
                            }
                    });
                }
                else {
                    throw new VncException(
                            "Function 'ipc/subscribe' expects either a single string "
                            + "topic or a sequence of topic strings!");
                }

                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(handler) };

                // Create a wrapper that inherits the Venice thread context!
                final ThreadBridge threadBridge = ThreadBridge.create("tcp-subscribe-handler", cf);

                final Consumer<IMessage> handlerWrapper = threadBridge.bridgeConsumer(m -> {
                    try {
                        handler.applyOf(new VncJavaObject(m));
                    }
                    catch(Exception ignore) { }
                });

                final IMessage response = client.subscribe(topics, handlerWrapper);

                return new VncJavaObject(response);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_publish =
        new VncFunction(
                "ipc/publish",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/publish client message)")
                    .doc(
                        "Publishes a messages to all clients that have subscribed to the " +
                        "message's topic.\n\n" +
                        "Returns the server's response message.\n\n" +
                        "The response message has one of these status:\n\n" +
                        "  * `:OK`            - message successfully published\n" +
                        "  * `:SERVER_ERROR`  - indicates a server side error while processing the request \n" +
                        "  * `:BAD_REQUEST`   - invalid request, details in the payload\n\n" +
                        "Note: a client in subscription mode can not send or publish messages!")
                    .examples(
                        "(do                                                                  \n" +
                        "  ;; thread-safe printing                                            \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))  \n" +
                        "                                                                     \n" +
                        "  ;; the server handler is not involved with publish/subscribe!      \n" +
                        "  (defn echo-handler [m] m)                                          \n" +
                        "                                                                     \n" +
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                  \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic 'test'              \n" +
                        "    (ipc/subscribe client1 \"test\" client-subscribe-handler)        \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", topic=\"test\", payload=\"hello\"          \n" +
                        "    (let [m (ipc/plain-text-message \"1\" \"test\" \"hello\")]       \n" +
                        "      (println \"PUBLISHING:\" (ipc/message->json true m))           \n" +
                        "      (->> (ipc/publish client2 m)                                   \n" +
                        "           (ipc/message->json true)                                  \n" +
                        "           (println \"PUBLISHED:\")))                                \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/subscribe",
                        "ipc/publish-async",
                        "ipc/client",
                        "ipc/server",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final IMessage request =  Coerce.toVncJavaObject(args.nth(1), IMessage.class);

                final IMessage response = client.publish(request);

                return new VncJavaObject(response);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_publish_async =
        new VncFunction(
                "ipc/publish-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/publish-async client message)")
                    .doc(
                        "Publishes a messages to all clients that have subscribed to the " +
                        "message's topic.\n\n" +
                        "Returns a future with the server's response message.\n\n" +
                        "The response message has one of these status:\n\n" +
                        "  * `:OK`            - message successfully published\n" +
                        "  * `:SERVER_ERROR`  - indicates a server side error while processing the request \n" +
                        "  * `:BAD_REQUEST`   - invalid request, details in the payload\n\n" +
                        "Note: a client in subscription mode can not send or publish messages!")
                    .examples(
                        "(do                                                                  \n" +
                        "  ;; thread-safe printing                                            \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))  \n" +
                        "                                                                     \n" +
                        "  ;; the server handler is not involved with publish/subscribe!      \n" +
                        "  (defn echo-handler [m] m)                                          \n" +
                        "                                                                     \n" +
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                  \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic 'test'              \n" +
                        "    (ipc/subscribe client1 \"test\" client-subscribe-handler)        \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", topic=\"test\", payload=\"hello\"          \n" +
                        "    (let [m (ipc/plain-text-message \"1\" \"test\" \"hello\")]       \n" +
                        "      (println \"PUBLISHING:\" (ipc/message->json true m))           \n" +
                        "      (-<> (ipc/publish-async client2 m)                             \n" +
                        "           (deref <> 300 :timeout)                                   \n" +
                        "           (ipc/message->json true <>)                               \n" +
                        "           (println \"PUBLISHED:\" <>)))                             \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/client",
                        "ipc/server",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final IMessage request =  Coerce.toVncJavaObject(args.nth(1), IMessage.class);

                return new VncJavaObject(
                        new FutureWrapper(
                           client.publishAsync(request)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    // ------------------------------------------------------------------------
    // Offer / Poll
    // ------------------------------------------------------------------------

    public static VncFunction ipc_offer =
        new VncFunction(
                "ipc/offer",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/offer client queue-name timeout message)")
                    .doc(
                        "Offers a message to the named queue.\n\n" +
                        "Returns the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c     | A client to send the offer message from |\n" +
                        "| queue-name q | A queue name to offer the message to|\n" +
                        "| timeout t    | A timeout in milliseconds for receiving the response|\n" +
                        "| message m    | The offer request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message added to the queue\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while offering the message to the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_FULL`      - the queue is full, offer rejected")
                    .examples(
                        "(do                                                                                           \n" +
                        "  ;; thread-safe printing                                                                     \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                           \n" +
                        "                                                                                              \n" +
                        "  ;; the server handler is not involved with offer/poll!                                      \n" +
                        "  (defn echo-handler [m] m)                                                                   \n" +
                        "                                                                                              \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                                           \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                                         \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]                                        \n" +
                        "    (let [order-queue \"orders\"                                                              \n" +
                        "          capacity    1_000                                                                   \n" +
                        "          timeout     300]                                                                    \n" +
                        "      ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "      (ipc/create-queue server order-queue capacity)                                          \n" +
                        "                                                                                              \n" +
                        "      ;; client1 offers order Venice data message to the queue                                \n" +
                        "      ;;   requestId=\"1\" and \"2\", topic=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "      (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "        (locking mutex (println \"ORDER:\" (ipc/message->json true order)))                   \n" +
                        "                                                                                              \n" +
                        "        ;; publish the order                                                                  \n" +
                        "        (->> (ipc/offer client1 order-queue timeout order)                                    \n" +
                        "             (ipc/message->json true)                                                         \n" +
                        "             (println \"OFFERED:\")))                                                         \n" +
                        "                                                                                              \n" +
                        "      ;; client2 pulls next order from the queue                                              \n" +
                        "      (->> (ipc/poll client2 order-queue timeout)                                             \n" +
                        "           (ipc/message->json true)                                                           \n" +
                        "           (println \"POLLED:\")))))                                                          ")
                    .seeAlso(
                        "ipc/offer-async",
                        "ipc/poll",
                        "ipc/poll-async",
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/create-queue",
                        "ipc/remove-queue")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final long timeout = Coerce.toVncLong(args.third()).toJavaLong();
                final IMessage request = Coerce.toVncJavaObject(args.fourth(), IMessage.class);

                return new VncJavaObject(client.offer(request, name, timeout, TimeUnit.MILLISECONDS));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_offer_async =
        new VncFunction(
                "ipc/offer-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/offer-async client queue-name message)")
                    .doc(
                        "Offers a message to the named queue.\n\n" +
                        "Returns a future with the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c     | A client to send the offer message from |\n" +
                        "| queue-name q | A queue name to offer the message to|\n" +
                        "| timeout t    | A timeout in milliseconds for receiving the response|\n" +
                        "| message m    | The offer request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message added to the queue\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while offering the message to the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_FULL`      - the queue is full, offer rejected")
                    .examples(
                        "(do                                                                                           \n" +
                        "  ;; thread-safe printing                                                                     \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                           \n" +
                        "                                                                                              \n" +
                        "  ;; the server handler is not involved with offer/poll!                                      \n" +
                        "  (defn echo-handler [m] m)                                                                   \n" +
                        "                                                                                              \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                                           \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                                         \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]                                        \n" +
                        "    (let [order-queue \"orders\"                                                              \n" +
                        "          capacity    1_000                                                                   \n" +
                        "          timeout     300]                                                                    \n" +
                        "      ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "      (ipc/create-queue server order-queue capacity)                                          \n" +
                        "                                                                                              \n" +
                        "      ;; client1 offers order Venice data message to the queue                                \n" +
                        "      ;;   requestId=\"1\" and \"2\", topic=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "      (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "        (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                              \n" +
                        "        ;; publish the order                                                                  \n" +
                        "        (-<> (ipc/offer-async client1 order-queue order)                                      \n" +
                        "             (deref <> 300 :timeout)                                                          \n" +
                        "             (ipc/message->json true <>)                                                      \n" +
                        "             (println \"OFFERED:\" <>)))                                                      \n" +
                        "                                                                                              \n" +
                        "      ;; client2 pulls next order from the queue                                              \n" +
                        "      (-<> (ipc/poll-async client2 order-queue)                                               \n" +
                        "           (deref <> 300 :timeout)                                                            \n" +
                        "           (ipc/message->json true <>)                                                        \n" +
                        "           (println \"POLLED:\" <>)))))                                                       ")
                    .seeAlso(
                        "ipc/offer",
                        "ipc/poll",
                        "ipc/poll-async",
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/create-queue",
                        "ipc/remove-queue")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final IMessage request = Coerce.toVncJavaObject(args.third(), IMessage.class);

                return new VncJavaObject(
                        new FutureWrapper(
                            client.offerAsync(request, name)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_poll =
        new VncFunction(
                "ipc/poll",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/poll client queue-name timeout)")
                    .doc(
                        "Polls a message from the named queue.\n\n" +
                        "Returns the server's response message.\n\n" +
                       "*Arguments:* \n\n" +
                        "| client c     | A client to send the poll message from |\n" +
                        "| queue-name q | A queue name to poll the message to|\n" +
                        "| timeout t    | A timeout in milliseconds for receiving the response|\n" +
                        "| message m    | The poll request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message successfully polled from the queue, response holds the data\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while polling a message from the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_EMPTY`     - the queue is empty")
                    .examples(
                        "(do                                                                                           \n" +
                        "  ;; thread-safe printing                                                                     \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                           \n" +
                        "                                                                                              \n" +
                        "  ;; the server handler is not involved with offer/poll!                                      \n" +
                        "  (defn echo-handler [m] m)                                                                   \n" +
                        "                                                                                              \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                                           \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                                         \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]                                        \n" +
                        "    (let [order-queue \"orders\"                                                              \n" +
                        "          capacity    1_000                                                                   \n" +
                        "          timeout     300]                                                                    \n" +
                        "      ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "      (ipc/create-queue server order-queue capacity)                                          \n" +
                        "                                                                                              \n" +
                        "      ;; client1 offers order Venice data message to the queue                                \n" +
                        "      ;;   requestId=\"1\" and \"2\", topic=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "      (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "        (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                              \n" +
                        "        ;; publish the order                                                                  \n" +
                        "        (->> (ipc/offer client1 order-queue timeout order)                                    \n" +
                        "             (ipc/message->json true)                                                         \n" +
                        "             (println \"OFFERED:\")))                                                         \n" +
                        "                                                                                              \n" +
                        "      ;; client2 pulls next order from the queue                                              \n" +
                        "      (->> (ipc/poll client2 order-queue timeout)                                             \n" +
                        "           (ipc/message->json true)                                                           \n" +
                        "           (println \"POLLED:\")))))                                                          ")
                    .seeAlso(
                        "ipc/poll-async",
                        "ipc/offer",
                        "ipc/offer-async",
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/create-queue",
                        "ipc/remove-queue")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final long timeout = Coerce.toVncLong(args.third()).toJavaLong();

                return new VncJavaObject(client.poll(name, timeout, TimeUnit.MILLISECONDS));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_poll_async =
        new VncFunction(
                "ipc/poll-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/poll-async client queue-name)")
                    .doc(
                        "Polls a message from the named queue.\n\n" +
                        "Returns a future with the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c     | A client to send the poll message from |\n" +
                        "| queue-name q | A queue name to poll the message to|\n" +
                        "| timeout t    | A timeout in milliseconds for receiving the response|\n" +
                        "| message m    | The poll request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message successfully polled from the queue, response holds the data\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while polling a message from the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_EMPTY`     - the queue is empty")
                    .examples(
                        "(do                                                                                           \n" +
                        "  ;; thread-safe printing                                                                     \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                           \n" +
                        "                                                                                              \n" +
                        "  ;; the server handler is not involved with offer/poll!                                      \n" +
                        "  (defn echo-handler [m] m)                                                                   \n" +
                        "                                                                                              \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                                           \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                                         \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]                                        \n" +
                        "    (let [order-queue \"orders\"                                                              \n" +
                        "          capacity    1_000                                                                   \n" +
                        "          timeout     300]                                                                    \n" +
                        "      ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "      (ipc/create-queue server order-queue capacity)                                          \n" +
                        "                                                                                              \n" +
                        "      ;; client1 offers order Venice data message to the queue                                \n" +
                        "      ;;   requestId=\"1\" and \"2\", topic=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "      (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "        (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                              \n" +
                        "        ;; publish the order                                                                  \n" +
                        "        (-<> (ipc/offer-async client1 order-queue order)                                      \n" +
                        "             (deref <> 300 :timeout)                                                          \n" +
                        "             (ipc/message->json true <>)                                                      \n" +
                        "             (println \"OFFERED:\" <>)))                                                      \n" +
                        "                                                                                              \n" +
                        "      ;; client2 pulls next order from the queue                                              \n" +
                        "      (-<> (ipc/poll-async client2 order-queue)                                               \n" +
                        "           (deref <> 300 :timeout)                                                            \n" +
                        "           (ipc/message->json true <>)                                                        \n" +
                        "           (println \"POLLED:\" <>)))))                                                       ")
                    .seeAlso(
                        "ipc/poll",
                        "ipc/offer-async",
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/create-queue",
                        "ipc/remove-queue")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final String name = Coerce.toVncString(args.second()).getValue();

                return new VncJavaObject(
                        new FutureWrapper(
                            client.pollAsync(name)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Statistics
    // ------------------------------------------------------------------------

    public static VncFunction ipc_server_status =
        new VncFunction(
                "ipc/server-status",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server-status client)")
                    .doc(
                        "Returns the status and statistics of the server the client is " +
                        "connected to.")
                    .examples(
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send client))                                       \n" +
                        "    (println \"STATUS:\" (ipc/server-status client))))            ")
                     .seeAlso(
                         "ipc/server-thread-pool-statistics",
                         "ipc/server",
                         "ipc/close",
                         "ipc/running?")
                     .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                final IMessage response = client.sendMessage(
                                            MessageFactory.text(
                                                null,
                                                "tcp-server/status",
                                                "appliaction/json",
                                                "UTF-8",
                                                ""),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    try {
                        return Json.readJson(response.getText(), true);
                    }
                    catch(Exception ex) {
                        throw new VncException ("Failed to get server status", ex);
                    }
                }
                else {
                    throw new VncException ("Failed to get server status");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_server_thread_pool_statistics =
        new VncFunction(
                "ipc/server-thread-pool-statistics",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server-thread-pool-statistics client)")
                    .doc(
                        "Returns the server's thread pool statistics the client is " +
                        "connected to.")
                    .examples(
                        "(do                                                                   \n" +
                        "  (defn echo-handler [m] m)                                           \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                   \n" +
                        "             client (ipc/client \"localhost\" 33333)]                 \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")                  \n" +
                        "         (ipc/send client))                                           \n" +
                        "    (println \"STATS:\" (ipc/server-thread-pool-statistics client)))) ")
                     .seeAlso(
                         "ipc/server-status",
                         "ipc/server",
                         "ipc/client",
                         "ipc/close",
                         "ipc/running?")
                     .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                final IMessage response = client.sendMessage(
                                            MessageFactory.text(
                                                null,
                                                "tcp-server/thread-pool-statistics",
                                                "appliaction/json",
                                                "UTF-8",
                                                ""),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    try {
                        return Json.readJson(response.getText(), true);
                    }
                    catch(Exception ex) {
                        throw new VncException ("Failed to get server thread pool statistics", ex);
                    }
                }
                else {
                    throw new VncException ("Failed to get server thread pool statistics");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Messages
    // ------------------------------------------------------------------------

    public static VncFunction ipc_text_message =
        new VncFunction(
                "ipc/text-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/text-message topic mimetype charset text)",
                        "(ipc/text-message request-id topic mimetype charset text)")
                    .doc(
                        "Creates a text message\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t      | A topic (string) |\n" +
                        "| mimetype m   | The mimetype of the payload text. A string like 'text/plain' |\n" +
                        "| charset c    | The charset of the payload text. A keyword like `:UTF-8`|\n" +
                        "| text t       | The message payload text (a string)|")
                    .examples(
                        "(->> (ipc/text-message \"test\"                          \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json",
                        "ipc/oneway?",
                        "ipc/response-ok?",
                        "ipc/response-err?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4, 5, 6);

                final VncVal requestId;
                final VncString topic;
                final VncString mimetype ;
                final VncKeyword charset;
                final VncVal textVal;
                final VncVal expiresAt;

                if (args.size() == 4) {
                    requestId = null;
                    topic = Coerce.toVncString(args.nth(0));
                    mimetype = Coerce.toVncString(args.nth(1));
                    charset = Coerce.toVncKeyword(args.nth(2));
                    textVal = args.nth(3);
                    expiresAt = null;
                }
                else if (args.size() == 5) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    expiresAt = null;
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    expiresAt = args.nth(5);
                }

                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final IMessage msg = MessageFactory.text(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Message.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        topic.getValue(),
                                        mimetype.getValue(),
                                        charset.getSimpleName(),
                                        text);

                return new VncJavaObject(msg);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_plain_text_message =
        new VncFunction(
                "ipc/plain-text-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/plain-text-message topic text)",
                        "(ipc/plain-text-message request-id topic text)")
                    .doc(
                        "Creates a plain text message with mimetype `text/plain` and charset `:UTF-8`.\n\n"  +
                        "*Arguments:* \n\n" +
                        "| request-id r | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t      | A topic (string) |\n" +
                        "| text t       | The message payload text (a string)|")
                    .examples(
                        "(->> (ipc/plain-text-message \"test\" \"hello\")  \n" +
                        "     (ipc/message->map)                           \n" +
                        "     (println))                                   ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json",
                        "ipc/oneway?",
                        "ipc/response-ok?",
                        "ipc/response-err?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3, 4);

                final VncVal requestId;
                final VncString topic;
                final VncVal textVal;
                final VncVal expiresAt;

                if (args.size() == 2) {
                    requestId = null;
                    topic = Coerce.toVncString(args.nth(0));
                    textVal = args.nth(1);
                    expiresAt = null;
                }
                else if (args.size() == 3) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    expiresAt = null;
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    expiresAt = args.nth(3);
                }

                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final IMessage msg = MessageFactory.text(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Message.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        topic.getValue(),
                                        "text/plain",
                                        "UTF-8",
                                        text);

                return new VncJavaObject(msg);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_binary_message =
        new VncFunction(
                "ipc/binary-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/binary-message topic mimetype data)",
                        "(ipc/binary-message request-id topic mimetype data)")
                    .doc(
                        "Creates a binary message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t      | A topic (string) |\n" +
                        "| mimetype m   | The mimetype of the payload data. A string like 'application/octet-stream', 'image/png'|\n" +
                        "| data d       | The message payload binary data (a bytebuf)|")
            .examples(
                        "(->> (ipc/binary-message \"test\"                        \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7]))    \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/message->map",
                        "ipc/message->json",
                        "ipc/oneway?",
                        "ipc/response-ok?",
                        "ipc/response-err?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4, 5);

                final VncVal requestId;
                final VncString topic;
                final VncString mimetype;
                final VncByteBuffer data;
                final VncVal expiresAt;

                if (args.size() == 3) {
                    requestId = null;
                    topic = Coerce.toVncString(args.nth(0));
                    mimetype = Coerce.toVncString(args.nth(1));
                    data = Coerce.toVncByteBuffer(args.nth(2));
                    expiresAt = null;
                }
                else if (args.size() == 4) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    expiresAt = null;
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    expiresAt = args.nth(4);
                }

                final IMessage msg = MessageFactory.binary(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Message.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        topic.getValue(),
                                        mimetype.getValue(),
                                        data.getBytes());

                return new VncJavaObject(msg);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_venice_message =
        new VncFunction(
                "ipc/venice-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/venice-message topic data)",
                        "(ipc/venice-message request-id topic data)")
                    .doc(
                        "Creates a venice message.\n\n" +
                        "The Venice data is serialized as JSON (mimetype: 'application/json') " +
                        "for transport within the message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t      | A topic (string) |\n" +
                        "| data d       | The message payload Venice data (e.g.: a map, list, ...)|")
            .examples(
                        "(->> (ipc/venice-message \"test\"                        \n" +
                        "                         {:a 100, :b 200})               \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/message->map",
                        "ipc/message->json",
                        "ipc/oneway?",
                        "ipc/response-ok?",
                        "ipc/response-err?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3, 4);

                final VncVal requestId;
                final VncString topic;
                final VncVal data;
                final VncVal expiresAt;

                if (args.size() == 2) {
                    requestId = null;
                    topic = Coerce.toVncString(args.nth(0));
                    data = args.nth(1);
                    expiresAt = null;
                }
                else if (args.size() == 3) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(0));
                    data = args.nth(2);
                    expiresAt = null;
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(0));
                    data = args.nth(2);
                    expiresAt = args.nth(3);
                }

                final IMessage msg = MessageFactory.venice(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Message.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        topic.getValue(),
                                        data);

                return new VncJavaObject(msg);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_message_field =
        new VncFunction(
                "ipc/message-field",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/message-field message field)")
                    .doc(
                        "Returns a specific field from the message. \n\n" +
                        "```\n" +
                        "           Message                         set by \n" +
                        " ┌───────────────────────────────┐   \n" +
                        " │ ID                            │   by send, publish/subscribe method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Message Type                  │   by send, publish/subscribe method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Oneway                        │   by client or framework method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Response Status               │   by server response processor\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Timestamp                     │   by message creator\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ ExpiresAt                     │   by client (may be null)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Request ID                    │   by client (may be used for idempotency checks by the receiver)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Topic                         │   by client\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload Mimetype              │   by client\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload Charset               │   by client if payload data is a string else null\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload data                  │   by client\n" +
                        " └───────────────────────────────┘   \n" +
                        "```\n\n" +
                        "**Supported field names:** \n\n" +
                        "  * `:id`               - the message's technical ID\n" +
                        "  * `:type`             - the message type (request, response, ..) \n" +
                        "  * `:oneway?`          - `true` if one-way message else `false`\n" +
                        "  * `:response-status`  - the response status (ok, bad request, ...) \n" +
                        "  * `:timestamp`        - the message's creation timestamp in milliseconds since epoch\n" +
                        "  * `:expires-at`       - the message's expiry timestamp in milliseconds since epoch (may be nil)\n" +
                        "  * `:request-id`       - the request ID (may be nil)\n" +
                        "  * `:topic`            - the topic\n" +
                        "  * `:payload-mimetype` - the payload data mimetype\n" +
                        "  * `:payload-charset`  - the payload data charset (if payload is a text form)\n" +
                        "  * `:payload-text`     - the payload converted to text data if payload is textual data else error\n" +
                        "  * `:payload-binary`   - the payload binary data (the raw message binary data)\n" +
                        "  * `:payload-venice`   - the payload converted venice data if mimetype is 'application/json' else error\n\n" +
                        "**Message type:** \n\n" +
                        "  * `:REQUEST`     - a request message\n" +
                        "  * `:PUBLISH`     - a publish message\n" +
                        "  * `:SUBSCRIBE`   - a subscribe message\n" +
                        "  * `:UNSUBSCRIBE` - an unsubscribe message\n" +
                        "  * `:OFFER`       - an offer message for a queue\n" +
                        "  * `:POLL`        - a poll message from a queue\n" +
                        "  * `:RESPONSE`    - a response to a request message\n" +
                        "  * `:NULL`        - a message with yet undefined type\n\n" +
                        "**Response status:** \n\n" +
                        "  * `:OK`              - a response message for a successfully processed request\n" +
                        "  * `:SERVER_ERROR`    - a response indicating a server side error while processing the request \n" +
                        "  * `:BAD_REQUEST`     - invalid request\n" +
                        "  * `:HANDLER_ERROR`   - a server handler error in the server's request processing\n" +
                        "  * `:QUEUE_NOT_FOUND` - the required queue does not exist\n" +
                        "  * `:QUEUE_EMPTY`     - the adressed queue in a poll request is empty\n" +
                        "  * `:QUEUE_FULL`      - the adressed queue in offer request is full\n" +
                        "  * `:NULL`            - a message with yet undefined status")
            .examples(
                        "(let [m (ipc/text-message \"test\"                         \n" +
                        "                          \"text/plain\"                   \n" +
                        "                          :UTF-8                           \n" +
                        "                          \"Hello!\")]                     \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :expires-at))              \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
                        "  (println (ipc/message-field m :request-id))              \n" +
                        "  (println (ipc/message-field m :topic))                   \n" +
                        "  (println (ipc/message-field m :payload-mimetype))        \n" +
                        "  (println (ipc/message-field m :payload-charset))         \n" +
                        "  (println (ipc/message-field m :payload-text))            \n" +
                        "  (println (ipc/message-field m :payload-binary)))         ",

                        "(let [m (ipc/binary-message \"test\"                       \n" +
                        "                            \"application/octet-stream\"   \n" +
                        "                            (bytebuf [0 1 2 3 4 5 6 7]))]  \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :expires-at))              \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
                        "  (println (ipc/message-field m :request-id))              \n" +
                        "  (println (ipc/message-field m :topic))                   \n" +
                        "  (println (ipc/message-field m :payload-mimetype))        \n" +
                        "  (println (ipc/message-field m :payload-charset))         \n" +
                        "  (println (ipc/message-field m :payload-binary)))         ",

                        "(let [m (ipc/venice-message \"test\"                  \n" +
                        "                            {:a 100, :b 200})]        \n" +
                        "  (println (ipc/message-field m :id))                 \n" +
                        "  (println (ipc/message-field m :type))               \n" +
                        "  (println (ipc/message-field m :oneway?))            \n" +
                        "  (println (ipc/message-field m :timestamp))          \n" +
                        "  (println (ipc/message-field m :expires-at))         \n" +
                        "  (println (ipc/message-field m :response-status))    \n" +
                        "  (println (ipc/message-field m :request-id))         \n" +
                        "  (println (ipc/message-field m :topic))              \n" +
                        "  (println (ipc/message-field m :payload-mimetype))   \n" +
                        "  (println (ipc/message-field m :payload-charset))    \n" +
                        "  (println (ipc/message-field m :payload-venice)))    ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/message->expired?",
                        "ipc/message->map",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final IMessage message = Coerce.toVncJavaObject(args.first(), IMessage.class);
                final VncKeyword field = Coerce.toVncKeyword(args.second());

                switch(field.getSimpleName()) {
                    case "id":               return new VncString(message.getId().toString());
                    case "type":             return new VncKeyword(message.getType().name());
                    case "timestamp":        return new VncLong(message.getTimestamp());
                    case "expires-at":       return message.getExpiresAt() < 0
                                                        ? Nil
                                                        : new VncLong(message.getExpiresAt());
                    case "oneway?":          return VncBoolean.of(message.isOneway());
                    case "response-status":  return new VncKeyword(message.getResponseStatus().name());
                    case "topic":            return new VncString(message.getTopic());
                    case "request-id":       return message.getRequestId() == null
                                                        ? Nil
                                                        : new VncString(message.getRequestId());
                    case "payload-mimetype": return new VncString(message.getMimetype());
                    case "payload-charset":  return message.getCharset() == null
                                                        ? Nil
                                                        : new VncKeyword(message.getCharset());
                    case "payload-text":     return new VncString(message.getText());
                    case "payload-binary":   return new VncByteBuffer(message.getData());
                    case "payload-venice":   return message.getVeniceData();
                    default:
                        throw new VncException ("Invalid message field name :" + field.getSimpleName());
                }
             }

            private static final long serialVersionUID = -1848883965231344442L;
        };


        public static VncFunction ipc_message_expiredQ =
            new VncFunction(
                    "ipc/message-expired?",
                    VncFunction
                        .meta()
                        .arglists(
                            "(ipc/message-expired? message)")
                        .doc(
                            "Returns `true` the message has expired else `false`.")
                .examples(
                            "(let [m (ipc/text-message \"test\"                         \n" +
                            "                          \"text/plain\"                   \n" +
                            "                          :UTF-8                           \n" +
                            "                          \"Hello!\")]                     \n" +
                            "  (println (ipc/message-expired? m)))                      ")
                        .seeAlso(
                            "ipc/server",
                            "ipc/client",
                            "ipc/text-message",
                            "ipc/plain-text-message",
                            "ipc/venice-message",
                            "ipc/message-field",
                            "ipc/message->map",
                            "ipc/message->json")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1);

                    final IMessage message = Coerce.toVncJavaObject(args.first(), IMessage.class);

                    return VncBoolean.of(message.hasExpired());
                 }

                private static final long serialVersionUID = -1848883965231344442L;
            };



    public static VncFunction ipc_message_to_map =
        new VncFunction(
                "ipc/message->map",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/message->map message)")
                    .doc(
                        "Converts a message to a Venice map.\n\n" +
                        "Returns a Venice map with the keys:\n\n" +
                        "  * `:type`\n" +
                        "  * `:status`\n" +
                        "  * `:timestamp`\n" +
                        "  * `:expires-at`\n" +
                        "  * `:request-id`\n" +
                        "  * `:topic`\n" +
                        "  * `:mimetype`\n" +
                        "  * `:charset`\n" +
                        "  * `:text` (only set if there is a messsage charset defined)\n" +
                        "  * `:data`\n")
                    .examples(
                        "(->> (ipc/text-message \"test\"                          \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map))                                 ",
                        "(->> (ipc/venice-message \"test\"                        \n" +
                        "                         {:a 100, :b 200})               \n" +
                        "     (ipc/message->map))                                 ",
                        "(->> (ipc/binary-message \"test\"                        \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4]))          \n" +
                        "     (ipc/message->map))                                 ")
                   .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message-field",
                        "ipc/message-expired?",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage m = Coerce.toVncJavaObject(args.first(), IMessage.class);

                if (m.getCharset() == null) {
                    // binary
                    return VncOrderedMap.of(
                            new VncKeyword("type"),       new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),     new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"),  new VncLong(m.getTimestamp()),
                            new VncKeyword("expires-at"), m.getExpiresAt() < 0 ? Nil : new VncLong(m.getExpiresAt()),
                            new VncKeyword("request-id"), m.getRequestId() == null ? Nil : new VncString(m.getRequestId()),
                            new VncKeyword("topic"),      new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),   new VncString(m.getMimetype()),
                            new VncKeyword("data"),       new VncByteBuffer(m.getData()));
                }
                else if ("application/json".equals(m.getMimetype())) {
                    // json
                    return VncOrderedMap.of(
                            new VncKeyword("type"),       new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),     new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"),  new VncLong(m.getTimestamp()),
                            new VncKeyword("expires-at"), m.getExpiresAt() < 0 ? Nil : new VncLong(m.getExpiresAt()),
                            new VncKeyword("request-id"), m.getRequestId() == null ? Nil : new VncString(m.getRequestId()),
                            new VncKeyword("topic"),      new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),   new VncString(m.getMimetype()),
                            new VncKeyword("data"),       Json.readJson(m.getText(), true));
                }
                else {
                    // text
                    return VncOrderedMap.of(
                            new VncKeyword("type"),       new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),     new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"),  new VncLong(m.getTimestamp()),
                            new VncKeyword("expires-at"), m.getExpiresAt() < 0 ? Nil : new VncLong(m.getExpiresAt()),
                            new VncKeyword("request-id"), m.getRequestId() == null ? Nil : new VncString(m.getRequestId()),
                            new VncKeyword("topic"),      new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),   new VncString(m.getMimetype()),
                            new VncKeyword("charset"),    new VncKeyword(m.getCharset()),
                            new VncKeyword("text"),       new VncString(m.getText()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


        public static VncFunction ipc_message_to_json =
            new VncFunction(
                    "ipc/message->json",
                    VncFunction
                        .meta()
                        .arglists(
                            "(ipc/message->json message)",
                            "(ipc/message->json pretty message)")
                        .doc(
                            "Converts message to a Json string with optional pretty " +
                            "printing.\n\n" +
                            "Returns a Json string.")
                        .examples(
                            "(->> (ipc/text-message \"test\"                          \n" +
                            "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                            "     (ipc/message->json true))                           ",
                            "(->> (ipc/venice-message \"test\"                        \n" +
                            "                         {:a 100, :b 200})               \n" +
                            "     (ipc/message->json true))                           ",
                            "(->> (ipc/binary-message \"test\"                        \n" +
                            "                         \"application/octet-stream\"    \n" +
                            "                         (bytebuf [0 1 2 3 4]))          \n" +
                            "     (ipc/message->json true))                           ")
                        .seeAlso(
                            "ipc/server",
                            "ipc/client",
                            "ipc/text-message",
                            "ipc/plain-text-message",
                            "ipc/venice-message",
                            "ipc/binary-message",
                            "ipc/message-field",
                            "ipc/message-expired?",
                            "ipc/message->map")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertArity(this, args, 1, 2);

                    final boolean pretty = args.size() == 1 ? false : Coerce.toVncBoolean(args.nth(0)).getValue();
                    final IMessage m = Coerce.toVncJavaObject(args.nth(args.size() == 1 ? 0 : 1), IMessage.class);

                    final VncMap data = (VncMap)ipc_message_to_map.applyOf(new VncJavaObject(m));

                    return new VncString(Json.writeJson(data, pretty));
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };


    public static VncFunction ipc_onewayQ =
        new VncFunction(
                "ipc/oneway?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/oneway? message)")
                    .doc(
                        "Returns `true` if the message is one-way else `false`.\n\n" +
                        "Note: the oneway flag on the message is delayed until a message is " +
                        "sent from the client to the server or vice versa." )
                    .examples(
                        "(do                                                               \n" +
                        "  (defn echo-handler [m]                                          \n" +
                        "     (if (ipc/oneway? m) nil m))                                  \n" +
                        "                                                                  \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ")
                    .seeAlso(
                        "ipc/response-ok?",
                        "ipc/response-err?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage m = Coerce.toVncJavaObject(args.first(), IMessage.class);

                return VncBoolean.of(m.isOneway());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_response_okQ =
        new VncFunction(
                "ipc/response-ok?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/response-ok? message)")
                    .doc(
                        "Returns `true` if the message response status is `:OK` else `false`.")
                    .examples(
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/response-ok?))))                                    ")
                    .seeAlso(
                        "ipc/oneway?",
                        "ipc/response-err?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage m = Coerce.toVncJavaObject(args.first(), IMessage.class);

                return VncBoolean.of(m.getResponseStatus() == ResponseStatus.OK);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_response_errQ =
        new VncFunction(
                "ipc/response-err?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/response-err? message)")
                    .doc(
                        "Returns `true` if the message has a response error status else `false`.")
                    .examples(
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/response-err?))))                                   ")
                    .seeAlso(
                        "ipc/oneway?",
                        "ipc/response-ok?",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/message->json")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage m = Coerce.toVncJavaObject(args.first(), IMessage.class);

                return VncBoolean.of(m.getResponseStatus() != ResponseStatus.OK
                                     && m.getResponseStatus() != ResponseStatus.NULL);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Queues
    // ------------------------------------------------------------------------

    public static VncFunction ipc_create_queue =
        new VncFunction(
                "ipc/create-queue",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/create-queue server name capacity)")
                    .doc(
                        "Creates a named queue on server. Messages can be exchanged asynchronously " +
                        "between two clients using a queue. Each message is delivered to exactly " +
                        "one client. 1 to N clients can *offer* / *poll* messages *from* / *to* the " +
                        "queue. \n\n" +
                        "Returns always `nil` or throws an exception.\n\n" +
                        "*Arguments:* \n\n" +
                        "| server s   | A server |\n" +
                        "| name n     | A queue name (string)|\n" +
                        "| capacity t | The queue's capacity (max number of messages)|")
                    .examples(
                        "(do                                                                       \n" +
                        "  (defn echo-handler [m] m)                                               \n" +
                        "                                                                          \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)                       \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                     \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]                    \n" +
                        "    (let [order-queue \"orders\"                                          \n" +
                        "          capacity    100_000                                             \n" +
                        "          order       (ipc/venice-message                                 \n" +
                        "                            \"order\"                                     \n" +
                        "                            {:item \"espresso\", :count 2})]              \n" +
                        "      (ipc/create-queue server order-queue capacity)                      \n" +
                        "      (->> (ipc/message->json true order)                                 \n" +
                        "           (println \"ORDER:\"))                                          \n" +
                        "      (->> (ipc/offer client1 order-queue 300 order)                      \n" +
                        "           (ipc/message->json true)                                       \n" +
                        "           (println \"OFFERED:\"))                                        \n" +
                        "      (->> (ipc/poll client2 order-queue 300)                             \n" +
                        "           (ipc/message->json true)                                       \n" +
                        "           (println \"POLLED:\")))))                                      ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/remove-queue",
                        "ipc/exists-queue?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final TcpServer server = Coerce.toVncJavaObject(args.first(), TcpServer.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final int capacity = (int)Coerce.toVncLong(args.third()).toJavaLong();

                server.createQueue(name, capacity);
                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_remove_queue =
        new VncFunction(
                "ipc/remove-queue",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/remove-queue server name)")
                    .doc(
                        "Removes a named queue from the server.\n\n" +
                        "Returns always `nil` or throws an exception.\n\n" +
                        "*Arguments:* \n\n" +
                        "| server s | A server |\n" +
                        "| name n   | A queue name (string)|")
                    .examples(
                        "(do                                                    \n" +
                        "  (defn echo-handler [m] m)                            \n" +
                        "                                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)]   \n" +
                        "    (let [order-queue \"orders\"                       \n" +
                        "          capacity    100_000]                         \n" +
                        "      (ipc/create-queue server order-queue capacity)   \n" +
                        "      ;; ...                                           \n" +
                        "      (ipc/remove-queue server order-queue))))         ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/create-queue",
                        "ipc/exists-queue?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpServer server = Coerce.toVncJavaObject(args.first(), TcpServer.class);
                final String name = Coerce.toVncString(args.second()).getValue();

                server.removeQueue(name);
                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_exists_queueQ =
        new VncFunction(
                "ipc/exists-queue?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/exists-queue? server name)")
                    .doc(
                        "Returns `true` if the named queue exists else `false`.\n\n" +
                        "*Arguments:* \n\n" +
                        "| server s | A server |\n" +
                        "| name n   | A queue name (string)|")
                    .examples(
                        "(do                                                    \n" +
                        "  (defn echo-handler [m] m)                            \n" +
                        "                                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)]   \n" +
                        "    (let [order-queue \"orders\"                       \n" +
                        "          capacity    100_000]                         \n" +
                        "      (ipc/create-queue server order-queue capacity)   \n" +
                        "      ;; ...                                           \n" +
                        "      (ipc/exists-queue? server order-queue))))        ")
                    .seeAlso(
                            "ipc/server",
                            "ipc/create-queue",
                            "ipc/remove-queue")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpServer server = Coerce.toVncJavaObject(args.first(), TcpServer.class);
                final String name = Coerce.toVncString(args.second()).getValue();

                return VncBoolean.of(server.existsQueue(name));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static long convertMaxMessageSizeToLong(final VncVal val) {
        if (val == Nil) {
            return 0L;
        }
        else if (Types.isVncLong(val)) {
            return Coerce.toVncLong(val).toJavaLong();
        }
        else if (Types.isVncKeyword(val)) {
            final String sVal = ((VncKeyword)val).getSimpleName();
            if (sVal.matches("^[1-9][0-9]*B$")) {
               return Long.parseLong(StringUtil.removeEnd(sVal, "B"));
            }
            else if (sVal.matches("^[1-9][0-9]*KB$")) {
                return Long.parseLong(StringUtil.removeEnd(sVal, "KB")) * 1024;
            }
            else if (sVal.matches("^[1-9][0-9]*MB$")) {
                return Long.parseLong(StringUtil.removeEnd(sVal, "MB")) * 1024 * 1024;
            }
            else {
                throw new VncException("Invalid max-message-size value! Use 20000, 500KB, 10MB, ...");
            }
        }
        else {
           throw new VncException("Invalid max-message-size value! Use 20000, 500KB, 10MB, ...");
        }
    }


    private static class FutureWrapper implements Future<VncVal> {

        public FutureWrapper(final Future<IMessage> d) {
            this.delegate = d;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public VncVal get() throws InterruptedException, ExecutionException {
            return new VncJavaObject(delegate.get());
        }

        @Override
        public VncVal get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return new VncJavaObject(delegate.get(timeout, unit));
        }


        private final Future<IMessage> delegate;
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(ipc_server)
                    .add(ipc_client)
                    .add(ipc_clone)
                    .add(ipc_close)
                    .add(ipc_runnningQ)

                    .add(ipc_send)
                    .add(ipc_send_async)
                    .add(ipc_send_oneway)

                    .add(ipc_publish)
                    .add(ipc_publish_async)
                    .add(ipc_subscribe)

                    .add(ipc_offer)
                    .add(ipc_offer_async)
                    .add(ipc_poll)
                    .add(ipc_poll_async)

                    .add(ipc_text_message)
                    .add(ipc_plain_text_message)
                    .add(ipc_binary_message)
                    .add(ipc_venice_message)
                    .add(ipc_message_expiredQ)
                    .add(ipc_message_field)
                    .add(ipc_message_to_map)
                    .add(ipc_message_to_json)
                    .add(ipc_onewayQ)
                    .add(ipc_response_okQ)
                    .add(ipc_response_errQ)

                    .add(ipc_create_queue)
                    .add(ipc_remove_queue)
                    .add(ipc_exists_queueQ)

                    .add(ipc_server_status)
                    .add(ipc_server_thread_pool_statistics)

                    .toMap();
}
