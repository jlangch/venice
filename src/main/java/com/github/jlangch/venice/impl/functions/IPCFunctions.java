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
import com.github.jlangch.venice.util.ipc.impl.IO;


public class IPCFunctions {

    public static VncFunction ipc_server =
        new VncFunction(
                "ipc/server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server port handler & options)")
                    .doc(
                        "Create a new TcpServer on the specified port.  \n\n" +
                        "The server must be closed after use!           \n\n" +
                        "*Arguments:* \n\n" +
                        "| port p    | The TCP/IP port |\n" +
                        "| handler h | A single argument handler function.¶" +
                                     " E.g.: a simple echo handler: `(fn [m] m)`.¶" +
                                     " The handler receives the request messsage and returns a response" +
                                     " message. In case of a one-way request message the handler" +
                                     " returns `nil`.|\n\n" +
                        "*Options:* \n\n" +
                        "| :max-connections n  | The number of the max connections the server can handle" +
                                               " in parallel. Defaults to 20.|\n" +
                        "| :max-message-size n | The max size of the message payload." +
                                               " Defaults to `200MB`.¶" +
                                               " The max size can be specified as a number like `20000`" +
                                               " or a number with a unit like `:20KB`, or `:20MB`|\n")
                    .examples(
                        "(do                                                      \n" +
                        "   (defn echo-handler [m]                                \n" +
                        "     (println \"request:  \" (ipc/message->map m))       \n" +
                        "     m)                                                  \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)     \n" +
                        "              client (ipc/client \"localhost\" 33333)]   \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")    \n" +
                        "          (ipc/send client)                              \n" +
                        "          (ipc/message->map)                             \n" +
                        "          (println \"response: \"))))                    ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map",
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

                final int maxConn = maxConnVal == Nil
                                        ? 0 : Coerce.toVncLong(maxConnVal).getIntValue();

                final long maxMsgSize = convertMaxMessageSizeToLong(maxMsgSizeVal);

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
                        "Create a new TcpClient connecting to a TcpServer on the  specified " +
                        "host and port.\n\n" +
                        "The client must be closed after use!" +
                        "*Arguments:* \n\n" +
                        "| port p | The server's TCP/IP port |\n" +
                        "| host h | The server's TCP/IP host |\n\n" +
                        "*Options:* \n\n" +
                        "| :max-parallel-tasks n | The max number of parallel tasks (e.g. sending async messages)" +
                                                 " the client can handle. Defaults to 10. |\n" +
                        "| :max-message-size n   | The max size of the message payload." +
                                                 " Defaults to `200MB`.¶" +
                                                 " The max size can be specified as a number like `20000`" +
                                                 " or a number with a unit like `:20KB`, or `:20MB` |\n")
                    .examples(
                        "(do                                                      \n" +
                        "   (defn echo-handler [m]                                \n" +
                        "     (println \"request:  \" (ipc/message->map m))       \n" +
                        "     m)                                                  \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)     \n" +
                        "              client (ipc/client \"localhost\" 33333)]   \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")    \n" +
                        "          (ipc/send client)                              \n" +
                        "          (ipc/message->map)                             \n" +
                        "          (println \"response: \"))))                    ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map",
                        "ipc/client-thread-pool-statistics")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                if ( args.size() == 1) {
                    final int port = Coerce.toVncLong(args.first()).getIntValue();

                    final TcpClient client = new TcpClient(port);
                    client.open();
                    return new VncJavaObject(client);
                }
                else {
                    final String host = Coerce.toVncString(args.first()).getValue();
                    final int port = Coerce.toVncLong(args.second()).getIntValue();

                    final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                    final VncVal maxParallelTasksVal = options.get(new VncKeyword("max-parallel-tasks"));
                    final VncVal maxMsgSizeVal = options.get(new VncKeyword("max-message-size"));

                    final int maxParallelTasks = maxParallelTasksVal == Nil
                                                      ? 0
                                                      : Coerce.toVncLong(maxParallelTasksVal).getIntValue();

                    final long maxMsgSize = convertMaxMessageSizeToLong(maxMsgSizeVal);

                    final TcpClient client = new TcpClient(host, port);

                    if (maxParallelTasks > 0) {
                        client.setMaximumParallelTasks(maxParallelTasks);
                    }

                    if (maxMsgSize > 0) {
                        client.setMaximumMessageSize(maxMsgSize);
                    }

                    client.open();

                    return new VncJavaObject(client);
                }
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
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))))         ")
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
                        "Closes the server or client")
                    .examples(
                        ";; prefer try-with-resources to safely close server and client     \n" +
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))))         ",

                        ";; explicitly closing server and client                            \n" +
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (let [server (ipc/server 33333 echo-handler)                    \n" +
                        "         client (ipc/client \"localhost\" 33333)]                  \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))           \n" +
                        "     (ipc/close client)                                            \n" +
                        "     (ipc/close server)))                                           ")
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
                        "Returns the servers response message or `nil` if the message is " +
                        "declared as one-way message. Throws a timeout exception if the " +
                        "response is not received within the timeout time.")
                    .examples(
                        ";; echo handler                                                    \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"               \n" +
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "          (ipc/send client)                                        \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ",

                        ";; handler processing JSON message data                            \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                                \n" +
                        "   (defn handler [m]                                               \n" +
                        "     (let [data   (json/read-str (. m :getText))                   \n" +
                        "           result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "       (ipc/text-message (. m :getTopic)                           \n" +
                        "                         \"application/json\" :UTF-8               \n" +
                        "                         result)))                                 \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/text-message \"test\"                               \n" +
                        "                            \"application/json\" :UTF-8            \n" +
                        "                            (json/write-str {\"x\" 100 \"y\" 200}))\n" +
                        "          (ipc/send client 2000)                                   \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ",

                        ";; handler with remote code execution                              \n" +
                        ";; request: \"(+ 1 2)\" => exec => response: \"3\"                 \n" +
                        "(do                                                                \n" +
                        "   (defn handler [m]                                               \n" +
                        "     (let [cmd    (. m :getText)                                   \n" +
                        "           result (str (eval (read-string cmd)))]                  \n" +
                        "       (ipc/plain-text-message (. m :getTopic)                     \n" +
                        "                               result)))                           \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"exec\" \"(+ 1 2)\")            \n" +
                        "          (ipc/send client)                                        \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ")
                 .seeAlso(
                     "ipc/client",
                     "ipc/server",
                     "ipc/close",
                     "ipc/running?",
                     "ipc/send-async",
                     "ipc/text-message",
                     "ipc/plain-text-message",
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
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "          (ipc/send-oneway client))))                              ")
                 .seeAlso(
                     "ipc/client",
                     "ipc/server",
                     "ipc/close",
                     "ipc/running?",
                     "ipc/send-async",
                     "ipc/text-message",
                     "ipc/plain-text-message",
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
                        "Sends a message asynchronously to the server the client is associated " +
                        "with. \n\n" +
                        "Returns a future to get the server's response message.")
                    .examples(
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "          (ipc/send-async client)                                  \n" +
                        "          (deref)                                                  \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final IMessage request = Coerce.toVncJavaObject(args.second(), IMessage.class);

                final Future<IMessage> response = client.sendMessageAsync(request);

                return response == null ? Nil : new VncJavaObject(new FutureWrapper(response));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_subscribe =
        new VncFunction(
                "ipc/subscribe",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/subscribe client topic handler)")
                    .doc(
                        "Subscribe to a topic.\n\n" +
                        "Puts this client into subscription mode and listens for messages of the " +
                        "specified topic.\n\n" +
                        "To unsubscribe from the topics just close the client.")
                    .examples(
                        "(do                                                                             \n" +
                        "   (def mutex 0)                                                                \n" +
                        "                                                                                \n" +
                        "   ;; the server handler is not involved with publish/subscribe!                \n" +
                        "   (defn server-handler [m]                                                     \n" +
                        "     (locking mutex (println (ipc/message->map m)))                             \n" +
                        "     m)                                                                         \n" +
                        "                                                                                \n" +
                        "   (defn client-subscribe-handler [m]                                           \n" +
                        "     (locking mutex (println \"SUB:\" (ipc/message->map m))))                   \n" +
                        "                                                                                \n" +
                        "   (try-with [server   (ipc/server 33333 server-handler)                        \n" +
                        "              client-1 (ipc/client \"localhost\" 33333)                         \n" +
                        "              client-2 (ipc/client \"localhost\" 33333)                         \n" +
                        "              client-3 (ipc/client \"localhost\" 33333)]                        \n" +
                        "     ;; client 'client-1' subscribes to 'alpha' messages                        \n" +
                        "     (ipc/subscribe client-1 \"alpha\" client-subscribe-handler)                \n" +
                        "                                                                                \n" +
                        "     ;; client 'client-2' subscribes to 'alpha' and 'beta' messages             \n" +
                        "     (ipc/subscribe client-2 [\"alpha\" \"beta\"] client-subscribe-handler)     \n" +
                        "                                                                                \n" +
                        "     ;; client 'client-3' publishes message                                     \n" +
                        "     (->> (ipc/plain-text-message \"alpha\" \"hello\")                          \n" +
                        "          (ipc/publish client-3))                                               \n" +
                        "     (->> (ipc/plain-text-message \"beta\" \"hello\")                           \n" +
                        "          (ipc/publish client-3))                                               \n" +
                        "                                                                                \n" +
                        "     (sleep 300)                                                                \n" +
                        "                                                                                \n" +
                        "     ;; print server status and statistics                                      \n" +
                        "     (locking mutex (println \"STATUS:\" (ipc/server-status client-3)))))       ")
                 .seeAlso(
                     "ipc/publish",
                     "ipc/client",
                     "ipc/server",
                     "ipc/text-message",
                     "ipc/plain-text-message",
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
                        "Publishes a messages to all clients that have subscribed to the" +
                        "message's topic.\n\n" +
                        "Note: a client in subscription mode can not send or publish messages!")
                    .examples(
                        "(do                                                                             \n" +
                        "   (def mutex 0)                                                                \n" +
                        "                                                                                \n" +
                        "   ;; the server handler is not involved with publish/subscribe!                \n" +
                        "   (defn server-handler [m]                                                     \n" +
                        "     (locking mutex (println (ipc/message->map m)))                             \n" +
                        "     m)                                                                         \n" +
                        "                                                                                \n" +
                       "   (defn client-subscribe-handler [m]                                            \n" +
                        "     (locking mutex (println \"SUB:\" (ipc/message->map m))))                   \n" +
                        "                                                                                \n" +
                        "   (try-with [server   (ipc/server 33333 server-handler)                        \n" +
                        "              client-1 (ipc/client \"localhost\" 33333)                         \n" +
                        "              client-2 (ipc/client \"localhost\" 33333)]                        \n" +
                        "     ;; client 'client-1' subscribes to 'test' messages                         \n" +
                        "     (ipc/subscribe client-1 \"test\" client-subscribe-handler)                 \n" +
                        "                                                                                \n" +
                        "     ;; client 'client-2' publishes a 'test' message                            \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")                           \n" +
                        "          (ipc/publish client-2))                                               \n" +
                        "                                                                                \n" +
                        "     (sleep 300)                                                                \n" +
                        "                                                                                \n" +
                        "     ;; print server status and statistics                                      \n" +
                        "     (locking mutex (println \"STATUS:\"(ipc/server-status client-2)))))        ")
                 .seeAlso(
                     "ipc/subscribe",
                     "ipc/client",
                     "ipc/server",
                     "ipc/text-message",
                     "ipc/plain-text-message",
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
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "          (ipc/send client))                                       \n" +
                        "     (println \"STATUS:\" (ipc/server-status client))))            ")
                 .seeAlso(
                     "ipc/server-thread-pool-statistics",
                     "ipc/server",
                     "ipc/client",
                     "ipc/close",
                     "ipc/running?",
                     "ipc/send",
                     "ipc/text-message",
                     "ipc/plain-text-message",
                     "ipc/binary-message",
                     "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                final IMessage response = client.sendMessage(
                                            MessageFactory.text(
                                                "server/status",
                                                "appliaction/json",
                                                "UTF-8",
                                                ""),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    try {
                        return IO.readJson(response.getText(), true);
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
                        "(do                                                                    \n" +
                        "   (defn echo-handler [m] m)                                           \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)                   \n" +
                        "              client (ipc/client \"localhost\" 33333)]                 \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")                  \n" +
                        "          (ipc/send client))                                           \n" +
                        "     (println \"STATS:\" (ipc/server-thread-pool-statistics client)))) ")
                 .seeAlso(
                     "ipc/server-status",
                     "ipc/server",
                     "ipc/client",
                     "ipc/close",
                     "ipc/running?",
                     "ipc/send",
                     "ipc/text-message",
                     "ipc/plain-text-message",
                     "ipc/binary-message",
                     "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                final IMessage response = client.sendMessage(
                                            MessageFactory.text(
                                                "server/thread-pool-statistics",
                                                "appliaction/json",
                                                "UTF-8",
                                                ""),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    try {
                        return IO.readJson(response.getText(), true);
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


    public static VncFunction ipc_client_thread_pool_statistics =
        new VncFunction(
                "ipc/client-thread-pool-statistics",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/client-thread-pool-statistics client)")
                    .doc(
                        "Returns the client's thread pool statistics.")
                    .examples(
                        "(do                                                                \n" +
                        "   (defn echo-handler [m] m)                                       \n" +
                        "   (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message \"test\" \"hello\")              \n" +
                        "          (ipc/send client))                                       \n" +
                        "     (println (ipc/client-thread-pool-statistics client))))        ")
                 .seeAlso(
                     "ipc/client",
                     "ipc/server",
                     "ipc/close",
                     "ipc/running?",
                     "ipc/send",
                     "ipc/text-message",
                     "ipc/plain-text-message",
                     "ipc/binary-message",
                     "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                final IMessage response = client.sendMessage(
                                            MessageFactory.text(
                                                "client/thread-pool-statistics",
                                                "appliaction/json",
                                                "UTF-8",
                                                ""),
                                            5,
                                            TimeUnit.SECONDS);

                if (response.getResponseStatus() == ResponseStatus.OK) {
                    try {
                        return IO.readJson(response.getText(), true);
                    }
                    catch(Exception ex) {
                        throw new VncException ("Failed to get client thread pool statistics", ex);
                    }
                }
                else {
                    throw new VncException ("Failed to get client thread pool statistics");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_text_message =
        new VncFunction(
                "ipc/text-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/text-message topic mimetype charset text)")
                    .doc(
                        "Creates a text message")
                    .examples(
                        "(->> (ipc/text-message \"test\"                         \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4);

                final VncString topic = Coerce.toVncString(args.nth(0));
                final VncString mimetype = Coerce.toVncString(args.nth(1));
                final VncKeyword charset = Coerce.toVncKeyword(args.nth(2));
                final VncVal textVal = args.nth(3);
                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final IMessage msg = MessageFactory.text(
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
                        "(ipc/plain-text-message status topic text)")
                    .doc(
                        "Creates a plain text message with mimetype `text/plain` and charset `:UTF-8`.")
                    .examples(
                        "(->> (ipc/plain-text-message \"test\" \"hello\")  \n" +
                        "     (ipc/message->map)                           \n" +
                        "     (println))                                   ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncString topic = Coerce.toVncString(args.nth(0));
                final VncVal textVal = args.nth(1);
                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final IMessage msg = MessageFactory.text(
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
                        "(ipc/binary-message status topic mimetype data)")
                    .doc(
                        "Creates a binary message.")
            .examples(
                        "(->> (ipc/binary-message \"test\"                        \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7]))    \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final VncString topic = Coerce.toVncString(args.nth(0));
                final VncString mimetype = Coerce.toVncString(args.nth(1));
                final VncByteBuffer data = Coerce.toVncByteBuffer(args.nth(2));

                final IMessage msg = MessageFactory.binary(
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
                        "(ipc/venice-message status topic data)")
                    .doc(
                        "Creates a venice message. \n\n" +
                        "The Venice data is serialized as JSON for transport within a message")
            .examples(
                        "(->> (ipc/venice-message \"test\"                        \n" +
                        "                         {:a 100, :b 200})               \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncString topic = Coerce.toVncString(args.first());
                final VncVal data = args.second();

                final IMessage msg = MessageFactory.venice(topic.getValue(), data);

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
                        "Supported field names: \n\n" +
                        "  * `id`\n" +
                        "  * `type`\n" +
                        "  * `oneway?`\n" +
                        "  * `response-status`\n" +
                        "  * `timestamp`\n" +
                        "  * `topic`\n" +
                        "  * `payload-mimetype`\n" +
                        "  * `payload-charset`\n" +
                        "  * `payload-text`\n" +
                        "  * `payload-binary`\n" +
                        "  * `payload-venice`\n")
            .examples(
                        "(let [m (ipc/text-message \"test\"                         \n" +
                        "                          \"text/plain\"                   \n" +
                        "                          :UTF-8                           \n" +
                        "                          \"Hello!\")]                     \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
                        "  (println (ipc/message-field m :topic))                   \n" +
                        "  (println (ipc/message-field m :payload-mimetype))        \n" +
                        "  (println (ipc/message-field m :payload-charset))          \n" +
                        "  (println (ipc/message-field m :payload-text)))            ",

                        "(let [m (ipc/binary-message \"test\"                       \n" +
                        "                            \"application/octet-stream\"   \n" +
                        "                            (bytebuf [0 1 2 3 4 5 6 7]))]  \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
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
                        "  (println (ipc/message-field m :response-status))    \n" +
                        "  (println (ipc/message-field m :topic))              \n" +
                        "  (println (ipc/message-field m :payload-mimetype))   \n" +
                        "  (println (ipc/message-field m :payload-charset))    \n" +
                        "  (println (ipc/message-field m :payload-venice)))    ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/message->map")
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
                    case "oneway?":          return VncBoolean.of(message.isOneway());
                    case "response-status":  return new VncKeyword(message.getResponseStatus().name());
                    case "topic":            return new VncString(message.getTopic());
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


    public static VncFunction ipc_message_to_map =
        new VncFunction(
                "ipc/message->map",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/message->map message)")
                    .doc(
                        "Converts a Java IPC `Message` to a Venice map")
                    .examples(
                        "(->> (ipc/text-message \"test\"                          \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map))                                 ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage m = Coerce.toVncJavaObject(args.first(), IMessage.class);

                if (m.getCharset() != null) {
                    return VncOrderedMap.of(
                            new VncKeyword("type"),      new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),    new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"), new VncJavaObject(m.getTimestamp()),
                            new VncKeyword("topic"),     new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),  new VncString(m.getMimetype()),
                            new VncKeyword("charset"),   new VncKeyword(m.getCharset()),
                            new VncKeyword("text"),      new VncString(m.getText()));
                }
                else {
                    return VncOrderedMap.of(
                            new VncKeyword("type"),      new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),    new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"), new VncJavaObject(m.getTimestamp()),
                            new VncKeyword("topic"),     new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),  new VncString(m.getMimetype()),
                            new VncKeyword("data"),      new VncByteBuffer(m.getData()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

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
                return Long.parseLong(StringUtil.removeEnd(sVal, "KB"));
            }
            else if (sVal.matches("^[1-9][0-9]*MB$")) {
                return Long.parseLong(StringUtil.removeEnd(sVal, "MB"));
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
        public FutureWrapper(final Future<IMessage> future) {
            this.delegate = future;
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
            final IMessage val = delegate.get();
            return val == null ? Nil : new VncJavaObject(val);
        }

        @Override
        public VncVal get(
                final long timeout,
                final TimeUnit unit
        ) throws InterruptedException, ExecutionException, TimeoutException {
            final IMessage val = delegate.get(timeout, unit);
            return val == null ? Nil : new VncJavaObject(val);
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
                    .add(ipc_close)
                    .add(ipc_runnningQ)

                    .add(ipc_send)
                    .add(ipc_send_async)
                    .add(ipc_send_oneway)

                    .add(ipc_publish)
                    .add(ipc_subscribe)

                    .add(ipc_text_message)
                    .add(ipc_plain_text_message)
                    .add(ipc_binary_message)
                    .add(ipc_venice_message)
                    .add(ipc_message_field)
                    .add(ipc_message_to_map)

                    .add(ipc_server_status)
                    .add(ipc_server_thread_pool_statistics)
                    .add(ipc_client_thread_pool_statistics)

                    .toMap();
}
