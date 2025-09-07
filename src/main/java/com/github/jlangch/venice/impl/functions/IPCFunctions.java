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
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.json.VncJsonReader;
import com.github.jlangch.venice.nanojson.JsonReader;
import com.github.jlangch.venice.util.ipc.Message;
import com.github.jlangch.venice.util.ipc.Status;
import com.github.jlangch.venice.util.ipc.TcpClient;
import com.github.jlangch.venice.util.ipc.TcpServer;


public class IPCFunctions {

    public static VncFunction ipc_server =
        new VncFunction(
                "ipc/server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server port handler & options)")
                    .doc(
                        "Create a new TcpServer on the specified port.       \n\n" +
                        "The server must be closed after use!                \n\n" +
                        "*Arguments:* \n\n" +
                        "| port p    | The TCP/IP port |\n" +
                        "| handler h | A single argument handler function. E.g.: a simple echo handler: `(fn [m] (. m :asEchoResponse))`. The handler receives the request messsage and returns a response message. In case of a one-way request message the handler returns `nil`.|\n\n" +
                        "*Options:* \n\n" +
                        "| :max-connections n | The number of the max connections the server can handle in parallel. Defaults to 20 |\n")
                    .examples(
                        "(do                                                                \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message :REQUEST \"test\" \"hello\")     \n" +
                        "          ((fn [m] (println (ipc/message->map m)) m))              \n" +
                        "          (ipc/send client)                                        \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                final int port = Coerce.toVncLong(args.first()).getIntValue();
                final VncFunction handler = Coerce.toVncFunction(args.second());

                final VncHashMap options = VncHashMap.ofAll(args.slice(2));
                final VncVal maxConnections = options.get(new VncKeyword("max-connections"));

                final int maxConn = maxConnections == Nil ? 0
                                                          : Coerce.toVncLong(args.first()).getIntValue();

                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(handler) };

                // Create a wrapper that inherits the Venice thread context!
                final ThreadBridge threadBridge = ThreadBridge.create("tcp-server-handler", cf);

                final Function<Message,Message> handlerWrapper = threadBridge.bridgeFunction((Message m) -> {
                          final VncVal request = new VncJavaObject(m);
                          final VncVal response = handler.applyOf(request);
                          return Coerce.toVncJavaObject(response, Message.class);
                });

                final TcpServer server = new TcpServer(port);

                if (maxConn > 0) {
                    server.setMaximumParallelConnections(maxConn);
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
                            "(ipc/client host port)")
                        .doc(
                            "Create a new TcpClient on the specified host and port")
                        .examples(
                            "(do                                                                \n" +
                            "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                            "   (try-with [server (ipc/server 33333 handler)                    \n" +
                            "              client (ipc/client \"localhost\" 33333)]             \n" +
                            "     (->> (ipc/plain-text-message :REQUEST \"test\" \"hello\")     \n" +
                            "          ((fn [m] (println (ipc/message->map m)) m))              \n" +
                            "          (ipc/send client)                                        \n" +
                            "          (ipc/message->map)                                       \n" +
                            "          (println))))                                             ")
                        .seeAlso(
                            "ipc/server",
                            "ipc/client",
                            "ipc/close",
                            "ipc/running?",
                            "ipc/send",
                            "ipc/send-async",
                            "ipc/text-message",
                            "ipc/plain-text-message",
                            "ipc/binary-message",
                            "ipc/message->map")
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

                        final TcpClient client = new TcpClient(host, port);
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
                        "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))))         ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
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
                        "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))))         ",
                        ";; explicitly closing server and client                            \n" +
                        "(do                                                                \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                        "   (let [server (ipc/server 33333 handler)                         \n" +
                        "         client (ipc/client \"localhost\" 33333)]                  \n" +
                        "     (println \"Server running:\" (ipc/running? server))           \n" +
                        "     (println \"Client running:\" (ipc/running? client))           \n" +
                        "     (ipc/close client)                                            \n" +
                        "     (ipc/close server)))                                           ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/running?",
                        "ipc/send",
                        "ipc/send-async",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/binary-message",
                        "ipc/message->map")
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
                        "Sends a message to the TCP/IP port the client is associated with. \n\n" +
                        "Returns the servers response message or `nil` if the message is a " +
                        "declared as one-way message.")
                    .examples(
                        ";; echo handler                                                    \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"               \n" +
                        "(do                                                                \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message :REQUEST \"test\" \"hello\")     \n" +
                        "          (ipc/send client)                                        \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ",
                        ";; handler processing JSON message data                            \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                                \n" +
                        "   (defn handler [m]                                               \n" +
                        "     (let [data   (json/read-str (. m :getText))                   \n" +
                        "           result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "       (ipc/text-message :RESPONSE_OK (. m :getTopic)              \n" +
                        "                         \"application/json\" :UTF-8               \n" +
                        "                         result)))                                 \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/text-message :REQUEST \"test\"                      \n" +
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
                        "       (ipc/plain-text-message :RESPONSE_OK                        \n" +
                        "                               (. m :getTopic)                     \n" +
                        "                               result)))                           \n" +
                        "   (try-with [server (ipc/server 33333 handler)                    \n" +
                        "              client (ipc/client \"localhost\" 33333)]             \n" +
                        "     (->> (ipc/plain-text-message :REQUEST \"exec\" \"(+ 1 2)\")   \n" +
                        "          (ipc/send client)                                        \n" +
                        "          (ipc/message->map)                                       \n" +
                        "          (println))))                                             ")
                 .seeAlso(
                     "ipc/server",
                     "ipc/client",
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
                final Message request = Coerce.toVncJavaObject(args.nth(hasTimeout ? 2 : 1), Message.class);

                if (timeout <= 0) {
                    final Message response = client.sendMessage(request);
                    return response == null ? Nil : new VncJavaObject(response);
                }
                else {
                    final Message response = client.sendMessage(request, timeout, TimeUnit.MILLISECONDS);
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
                            "Sends a message asynchronously to the TCP/IP port the client is " +
                            "associated with. \n\n" +
                            "Retruns a future to get the server's response message.")
                        .examples(
                            "(do                                                                \n" +
                            "   (defn handler [m] (. m :asEchoResponse))                        \n" +
                            "   (try-with [server (ipc/server 33333 handler)                    \n" +
                            "              client (ipc/client \"localhost\" 33333)]             \n" +
                            "     (->> (ipc/plain-text-message :REQUEST \"test\" \"hello\")     \n" +
                            "          (ipc/send-async client)                                  \n" +
                            "          (deref)                                                  \n" +
                            "          (ipc/message->map)                                       \n" +
                            "          (println))))                                             ")
                        .seeAlso(
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
                    ArityExceptions.assertArity(this, args, 2);

                    final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                    final Message request = Coerce.toVncJavaObject(args.second(), Message.class);

                    final Future<Message> response = client.sendMessageAsync(request);

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
                        "Puts this client in subscription mode and listens for subscriptions " +
                        "on the specified topic.")
                    .examples(
                        "")
                 .seeAlso(
                     "ipc/server",
                     "ipc/client",
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
                ArityExceptions.assertArity(this, args, 3);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final String topic = Coerce.toVncString(args.nth(1)).getValue();
                final VncFunction handler = Coerce.toVncFunction(args.nth(2));


                final CallFrame[] cf = new CallFrame[] {
                                            new CallFrame(this, args),
                                            new CallFrame(handler) };

                // Create a wrapper that inherits the Venice thread context!
                final ThreadBridge threadBridge = ThreadBridge.create("tcp-subscribe-handler", cf);

                final Consumer<Message> handlerWrapper = threadBridge.bridgeConsumer(m -> {
                    try {
                        handler.applyOf(new VncJavaObject(m));
                    }
                    catch(Exception ignore) { }
                });

                final Message response = client.subscribe(topic, handlerWrapper);

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
                        "(ipc/publish client message")
                    .doc(
                        "Publishes a messages to all clients that have subscribed to. \n\n" +
                        "the message's topic.")
                    .examples(
                        "")
                 .seeAlso(
                     "ipc/server",
                     "ipc/client",
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
                ArityExceptions.assertArity(this, args, 2);

                final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);
                final Message request =  Coerce.toVncJavaObject(args.nth(1), Message.class);

                final Message response = client.publish(request);

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
                            "(ipc/server-status client")
                        .doc(
                            "Returns the status of server the client is connected to.")
                        .examples(
                            "")
                     .seeAlso(
                         "ipc/server",
                         "ipc/client",
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
                    ArityExceptions.assertArity(this, args, 1);

                    final TcpClient client = Coerce.toVncJavaObject(args.nth(0), TcpClient.class);

                    final Message response = client.sendMessage(
                                                Message.text(
                                                    Status.REQUEST,
                                                    "server/status",
                                                    "appliaction/json",
                                                    "UTF-8",
                                                    ""),
                                                5,
                                                TimeUnit.SECONDS);

                    if (response.getStatus() == Status.RESPONSE_OK) {
                        final Function<VncVal,VncVal> keyFn = t -> CoreFunctions.keyword.applyOf(t);
                        try {
                            return new VncJsonReader(
                                        JsonReader.from(response.getText()),
                                        keyFn,
                                        null,
                                        false).read();
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

    public static VncFunction ipc_text_message =
        new VncFunction(
                "ipc/text-message",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/text-message status topic mimetype charset text)")
                    .doc(
                        "Creates a text message                      \n\n" +
                        "Clients use these status to send requests to ther server:\n\n" +
                        "| :REQUEST         | send a request and wait for a server response |\n" +
                        "| :REQUEST_ONE_WAY | send a one-way request, the server does not send a response |\n\n" +
                        "Servers use these status to send reponses back to the client: \n\n" +
                        "| :RESPONSE_OK            | processing successful |\n" +
                        "| :RESPONSE_SERVER_ERROR  | internal server error, the request can not be processed |\n" +
                        "| :RESPONSE_HANDLER_ERROR | error while processing the request by the handler |\n" +
                        "| :RESPONSE_BAD_REQUEST   | bad request data |\n")
                    .examples(
                        "(->> (ipc/text-message :REQUEST \"test\"                 \n" +
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
                ArityExceptions.assertArity(this, args, 5);

                final VncKeyword status = Coerce.toVncKeyword(args.nth(0));
                final VncString topic = Coerce.toVncString(args.nth(1));
                final VncString mimetype = Coerce.toVncString(args.nth(2));
                final VncKeyword charset = Coerce.toVncKeyword(args.nth(3));
                final VncVal textVal = args.nth(4);
                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final Message msg = Message.text(
                                        convertToStatus(status),
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
                        "Creates a plain text message with mimetype `text/plain` and charset `:UTF-8`.\n\n" +
                        "Clients use these status to send requests to ther server:\n\n" +
                        "| :REQUEST         | send a request and wait for a server response |\n" +
                        "| :REQUEST_ONE_WAY | send a one-way request, the server does not send a response |\n\n" +
                        "Servers use these status to send reponses back to the client: \n\n" +
                        "| :RESPONSE_OK            | processing successful |\n" +
                        "| :RESPONSE_SERVER_ERROR  | internal server error, the request can not be processed |\n" +
                        "| :RESPONSE_HANDLER_ERROR | error while processing the request by the handler |\n" +
                        "| :RESPONSE_BAD_REQUEST   | bad request data |\n")
                    .examples(
                        "(->> (ipc/plain-text-message :REQUEST \"test\" \"hello\")  \n" +
                        "     (ipc/message->map)                                    \n" +
                        "     (println))                                            ")
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
                ArityExceptions.assertArity(this, args, 3);

                final VncKeyword status = Coerce.toVncKeyword(args.nth(0));
                final VncString topic = Coerce.toVncString(args.nth(1));
                final VncVal textVal = args.nth(2);
                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final Message msg = Message.text(
                                        convertToStatus(status),
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
                            "Creates a binary message.\n\n" +
                            "Clients use these status to send requests to ther server:\n\n" +
                            "| :REQUEST         | send a request and wait for a server response |\n" +
                            "| :REQUEST_ONE_WAY | send a one-way request, the server does not send a response |\n\n" +
                            "Servers use these status to send reponses back to the client: \n\n" +
                            "| :RESPONSE_OK            | processing successful |\n" +
                            "| :RESPONSE_SERVER_ERROR  | internal server error, the request can not be processed |\n" +
                            "| :RESPONSE_HANDLER_ERROR | error while processing the request by the handler |\n" +
                            "| :RESPONSE_BAD_REQUEST   | bad request data |\n")
                .examples(
                            "(->> (ipc/binary-message :REQUEST \"test\"               \n" +
                            "                       \"application/octet-stream\"      \n" +
                            "                       (bytebuf [0 1 2 3 4 5 6 7]))      \n" +
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
                    ArityExceptions.assertArity(this, args, 4);

                    final VncKeyword status = Coerce.toVncKeyword(args.nth(0));
                    final VncString topic = Coerce.toVncString(args.nth(1));
                    final VncString mimetype = Coerce.toVncString(args.nth(2));
                    final VncByteBuffer data = Coerce.toVncByteBuffer(args.nth(3));

                    final Message msg = Message.binary(
                                            convertToStatus(status),
                                            topic.getValue(),
                                            mimetype.getValue(),
                                            data.getBytes());

                    return new VncJavaObject(msg);
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
                        "(->> (ipc/text-message :REQUEST \"test\"                 \n" +
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

                final Message m = Coerce.toVncJavaObject(args.first(), Message.class);

                if (m.getCharset() != null) {
                    return VncOrderedMap.of(
                            new VncKeyword("status"),    new VncKeyword(m.getStatus().name()),
                            new VncKeyword("timestamp"), new VncJavaObject(m.getTimestamp()),
                            new VncKeyword("topic"),     new VncString(m.getTopic()),
                            new VncKeyword("mimetype"),  new VncString(m.getMimetype()),
                            new VncKeyword("charset"),   new VncKeyword(m.getCharset()),
                            new VncKeyword("text"),      new VncString(m.getText()));
                }
                else {
                    return VncOrderedMap.of(
                            new VncKeyword("status"),    new VncKeyword(m.getStatus().name()),
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

    private static final Status convertToStatus(final VncKeyword status) {
        try {
            return Status.valueOf(status.getSimpleName());
        }
        catch(Exception ex) {
            throw new VncException("Invalid IPC message status " + status);
        }
    }

    private static class FutureWrapper implements Future<VncVal> {
        public FutureWrapper(final Future<Message> future) {
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
            final Message val = delegate.get();
            return val == null ? Nil : new VncJavaObject(val);
        }

        @Override
        public VncVal get(
                final long timeout,
                final TimeUnit unit
        ) throws InterruptedException, ExecutionException, TimeoutException {
            final Message val = delegate.get(timeout, unit);
            return val == null ? Nil : new VncJavaObject(val);
        }

        private final Future<Message> delegate;
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

                    .add(ipc_subscribe)
                    .add(ipc_publish)

                    .add(ipc_text_message)
                    .add(ipc_plain_text_message)
                    .add(ipc_binary_message)
                    .add(ipc_message_to_map)

                    .add(ipc_server_status)

                    .toMap();
}
