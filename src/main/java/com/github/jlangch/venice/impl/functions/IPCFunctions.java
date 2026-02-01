/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
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
import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.Benchmark;
import com.github.jlangch.venice.util.ipc.Client;
import com.github.jlangch.venice.util.ipc.ClientConfig;
import com.github.jlangch.venice.util.ipc.ClientConfig.Builder;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.IpcException;
import com.github.jlangch.venice.util.ipc.MessageFactory;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.Json;


public class IPCFunctions {

    public static VncFunction ipc_server =
        new VncFunction(
                "ipc/server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/server port & options)",
                        "(ipc/server port handler & options)",
                        "(ipc/server conn-uri & options)",
                        "(ipc/server conn-uri handler & options)")
                    .doc(
                        "Create a new server on the specified port or connection URI.\n\n" +
                        "*Arguments:* \n\n" +
                        "| [![text-align: left; width: 10%]] | [![text-align: left; width: 90%]] |\n" +
                        "| port p     | A TCP/IP port. E.g.: 33333 |\n" +
                        "| conn-uri u | A connection URI¶" +
                                      " \u00A0 • TCP/IP sockets¶" +
                                      " \u00A0\u00A0\u00A0 `af-inet://localhost:33333`¶" +
                                      " \u00A0 • Unix domain sockets (requires junixsocket libraries!)¶" +
                                      " \u00A0\u00A0\u00A0 `af-unix:///data/ipc/test.sock`|\n" +
                        "| handler h  | A single argument handler function.¶" +
                                      " E.g.: a simple echo handler: `(fn [m] m)`.¶" +
                                      " The handler receives the request messsage and returns a response" +
                                      " message. In case of a one-way request message the server discards" +
                                      " the handler's response if it is not `nil`.¶" +
                                      " A handler is only required for send/receive message passing style. It "+
                                      " is not required for offer/poll and publish/subscribe!|\n\n" +
                        "*Options:* \n\n" +
                        "| [![text-align: left; width: 25%]] | [![text-align: left; width: 75%]] |\n" +
                        "| :max-connections n           | The number of the max connections the server can handle" +
                                                        " in parallel.¶Defaults to 20.|\n" +
                        "| :max-message-size n          | The max size of the message payload.¶" +
                                                        " Defaults to `50MB`.¶" +
                                                        " The max size can be specified as a number like `20000`" +
                                                        " or a number with a unit like `:20KB` or `:20MB`. The value" +
                                                        " must be in the range 2KB ... 250MB|\n" +
                        "| :max-queues n                | The number of the max queues the server can handle.¶" +
                                                        " Defaults to 20.|\n" +
                        "| :max-topics n                | The number of the max topics the server can handle.¶" +
                                                        " Defaults to 20.|\n" +
                        "| :compress-cutoff-size n      | The compression cutoff size for payload messages.¶" +
                                                        " With a negative cutoff size payload messages will not be" +
                                                        " compressed. If the payload message size is greater than the cutoff" +
                                                        " size it will be compressed.¶"  +
                                                        " The cutoff size can be specified as a number like `1000`" +
                                                        " or a number with a unit like `:1KB` or `:2MB`.¶" +
                                                        " Defaults to -1 (no compression)|\n" +
                        "| :encrypt b                   | If `true` encrypt the payload data of all messages exchanged" +
                                                        " with this server.¶" +
                                                        " The data is AES-256-GCM encrypted using a secret that is" +
                                                        " created and exchanged using the Diffie-Hellman key exchange" +
                                                        " algorithm.|\n" +
                        "| :server-log-dir f            | If the server-log-dir is specified writes a server log to this" +
                                                        " directory.¶" +
                                                        " Defaults to `nil`.|\n" +
                        "| :write-ahead-log-dir f       | Provide a write-ahead-log directory to support durable queues.¶" +
                                                        " Defaults to `nil`.|\n" +
                        "| :write-ahead-log-compress b  | If `true` compresses the write-ahead-log records.¶" +
                                                        " Defaults to `false`.|\n" +
                        "| :write-ahead-log-compact b   | If `true` compacts the write-ahead-logs at server start.¶" +
                        "                                 Defaults to `false`.|\n" +
                        "| :authenticator a             | An authenticator. If an authenticator is used encryption must \n" +
                                                        " be enabled to safely transmit users credentials!¶" +
                                                        " Defaults to `nil`.|\n" +
                        "| :socket-snd-buf-size n       | The server socket's send buffer size.¶" +
                                                        " Defaults to `-1` (use the sockets default buf size).¶" +
                                                        " The size can be specified as a number like `64536`" +
                                                        " or a number with a unit like `:64KB` or `:1MB`.|\n" +
                        "| :socket-rcv-buf-size n       | The server socket's receive buffer size.¶" +
                                                        " Defaults to `-1` (use the sockets default buf size).¶" +
                                                        " The size can be specified as a number like `64536`" +
                                                        " or a number with a unit like `:64KB` or `:1MB`.|\n" +
                        "| :heartbeat-interval n        | Connection heartbeat interval in seconds. Must be greater" +
                                                        " than 0.¶" +
                                                        " Defaults to `nil`.|\n\n" +
                        "**The server must be closed after use!**\n\n" +
                        "[See Inter-Process-Communication](https://github.com/jlangch/venice/blob/master/doc/readme/ipc.md)")
                    .examples(
                        "(do                                                           \n" +
                        "  (defn echo-handler [m]                                      \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))             \n" +
                        "    m)                                                        \n" +
                        "                                                              \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)           \n" +
                        "             client (ipc/client \"localhost\" 33333)]         \n" +
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")    \n" +
                        "         (ipc/send client)                                    \n" +
                        "         (ipc/message->map)                                   \n" +
                        "         (println \"RESPONSE: \"))))                          ",
                        "(do                                                             \n" +
                        "  (defn echo-handler [m]                                        \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))               \n" +
                        "    m)                                                          \n" +
                        "                                                                \n" +
                        "  (try-with [server (ipc/server \"af-inet://localhost:33333\"   \n" +
                        "                                echo-handler)                   \n" +
                        "             client (ipc/client \"af-inet://localhost:33333\")] \n" +
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")      \n" +
                        "         (ipc/send client)                                      \n" +
                        "         (ipc/message->map)                                     \n" +
                        "         (println \"RESPONSE: \"))))                            ",
                        "(do                                                             \n" +
                        "  (defn echo-handler [m]                                        \n" +
                        "    (println \"REQUEST:  \" (ipc/message->map m))               \n" +
                        "    m)                                                          \n" +
                        "                                                                \n" +
                        "  (let [a (ipc/authenticator)]                                  \n" +
                        "    (ipc/add-credentials a \"tom\" \"3-kio\")                   \n" +
                        "    (try-with [server (ipc/server 33333 echo-handler            \n" +
                        "                                  :encrypt true                 \n" +
                        "                                  :authenticator a)             \n" +
                        "               client (ipc/client \"localhost\" 33333           \n" +
                        "                                  :user-name \"tom\"            \n" +
                        "                                  :password \"3-kio\")]         \n" +
                        "      (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")    \n" +
                        "           (ipc/send client)                                    \n" +
                        "           (ipc/message->map)                                   \n" +
                        "           (println \"RESPONSE: \")))))                         ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?",
                        "ipc/authenticator",
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
                        "ipc/benchmark",
                        "ipc/server-status",
                        "ipc/server-thread-pool-statistics")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                // -- Parse arguments -----------------------------------------

                final VncVal handlerVal = args.second();

                // note: keywords are functions of type IVncFunction but not of type VncFunction!
                final boolean hasHandler = Types.isVncFunction(handlerVal);

                // arguments: handler
                final VncFunction handler = hasHandler ? Coerce.toVncFunction(handlerVal) : null;

                // options
                final VncHashMap options = VncHashMap.ofAll(args.slice(hasHandler ? 2 : 1));

                final VncVal maxConnVal = options.get(new VncKeyword("max-connections"));
                final VncVal maxMsgSizeVal = options.get(new VncKeyword("max-message-size"));
                final VncVal maxMaxQueuesVal = options.get(new VncKeyword("max-queues"), new VncLong(-1));
                final VncVal maxMaxTopicsVal = options.get(new VncKeyword("max-topics"), new VncLong(-1));
                final VncVal compressCutoffSizeVal = options.get(new VncKeyword("compress-cutoff-size"));
                final VncVal encryptVal = options.get(new VncKeyword("encrypt"), VncBoolean.False);
                final VncVal serverLogDirVal = options.get(new VncKeyword("server-log-dir"));
                final VncVal walDirVal = options.get(new VncKeyword("write-ahead-log-dir"));
                final VncVal walCompressVal = options.get(new VncKeyword("write-ahead-log-compress"));
                final VncVal walCompactAtStartVal = options.get(new VncKeyword("write-ahead-log-compact"));
                final VncVal authenticatorVal = options.get(new VncKeyword("authenticator"));
                final VncVal heartbeatIntervalVal = options.get(new VncKeyword("heartbeat-interval"), new VncLong(0));
                final VncVal sndBufSizeVal = options.get(new VncKeyword("socket-snd-buf-size"), new VncLong(-1));
                final VncVal rcvBufSizeVal = options.get(new VncKeyword("socket-rcv-buf-size"), new VncLong(-1));

                final int maxConn = maxConnVal == Nil
                                        ? 0
                                        : Coerce.toVncLong(maxConnVal).getIntValue();

                final long maxMsgSize = convertUnitValueToLong(maxMsgSizeVal);
                final long maxQueues = Coerce.toVncLong(maxMaxQueuesVal).getValue();
                final long maxTopics = Coerce.toVncLong(maxMaxTopicsVal).getValue();
                final long compressCutoffSize = convertUnitValueToLong(compressCutoffSizeVal);
                final boolean encrypt = Coerce.toVncBoolean(encryptVal).getValue();
                final long heartbeatInterval = Coerce.toVncLong(heartbeatIntervalVal).getValue();
                final int sndBufSize = (int)convertUnitValueToLong(sndBufSizeVal);
                final int rcvBufSize = (int)convertUnitValueToLong(rcvBufSizeVal);

                final File serverLogDir = serverLogDirVal == Nil
                                            ? null
                                            : IOFunctions.convertToFile(
                                                serverLogDirVal,
                                                "Function 'ipc/server' arg ':server-log-dir' must be an `io/file`");

                if (serverLogDir != null && !serverLogDir.isDirectory() && !serverLogDir.canWrite()) {
                    throw new VncException(
                            "The 'server-log-dir' " + serverLogDir
                            + " does not exist or is not writable!");
                }

                final File walDir = walDirVal == Nil
                                    ? null
                                    : IOFunctions.convertToFile(
                                        walDirVal,
                                        "Function 'ipc/server' arg ':write-ahead-log-dir' must be an `io/file`");

                if (walDir != null && !walDir.isDirectory() && !walDir.canWrite()) {
                    throw new VncException(
                            "The 'write-ahead-log-dir' " + walDir
                            + " does not exist or is not writable!");
                }

                final boolean walCompress = walCompressVal != Nil && Coerce.toVncBoolean(walCompressVal).getValue();
                final boolean walCompactAtStart = walCompactAtStartVal != Nil && Coerce.toVncBoolean(walCompactAtStartVal).getValue();

                final Authenticator authenticator = Coerce.toVncJavaObjectOrNull(authenticatorVal, Authenticator.class);

                // -- Create and configure the server ------------------------------------

                // -- Configure the server ------------------------------------
                ServerConfig.Builder builder = ServerConfig.builder();
                setConfigConnection(args.first(), builder);
                if (maxMsgSize > 0) {
                    builder.maxMessageSize(maxMsgSize);
                }
                if (maxConn > 0) {
                    builder.maxParallelConnections(maxConn);
                }
                if (maxQueues > 0) {
                    builder.maxQueues((int)maxQueues);
                }
                if (maxTopics > 0) {
                    builder.maxTopics((int)maxTopics);
                }
                if (compressCutoffSize >= 0) {
                    builder.compressCutoffSize((int)compressCutoffSize);
                }
                builder.encrypt(encrypt);
                if (serverLogDir != null) {
                    builder.enableLogger(serverLogDir);
                }
                if (walDir != null) {
                    builder.enableWriteAheadLog(walDir, walCompress, walCompactAtStart);
                }
                builder.authenticator(authenticator);
                if (heartbeatInterval > 0) {
                    builder.hearbeatIntervalSeconds((int)heartbeatInterval);
                }
                builder.sendBufferSize(sndBufSize);
                builder.receiveBufferSize(rcvBufSize);

                Server server = Server.of(builder.build());

                // -- Start the server ----------------------------------------

                final Function<IMessage,IMessage> handlerWrapper = wrapFunction(this, args, handler);
                if (handlerWrapper == null) {
                    server.start();
                }
                else {
                    server.start(handlerWrapper);
                }

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
                        "(ipc/client port & options)",
                        "(ipc/client host port & options)",
                        "(ipc/client conn-uri)",
                        "(ipc/client conn-uri & options)")
                    .doc(
                        "Create a new client connecting to a server on the specified " +
                        "host and port.\n\n" +
                        "*Arguments:* \n\n" +
                        "| port p     | The server's TCP/IP port |\n" +
                        "| host h     | The server's TCP/IP host |\n" +
                        "| conn-uri u | A connection URI¶" +
                                      " \u00A0 • TCP/IP sockets¶" +
                                      " \u00A0\u00A0\u00A0 `af-inet://localhost:33333`¶" +
                                      " \u00A0 • Unix domain sockets (requires junixsocket libraries!)¶" +
                                      " \u00A0\u00A0\u00A0 `af-unix:///data/ipc/test.sock`|\n\n" +
                        "*Options:* \n\n" +
                        "| :encrypt b              | If `true` encrypt the payload data of all messages exchanged" +
                                                   " between this client and its associated server.¶" +
                                                   " The data is AES-256-GCM encrypted using a secret that is" +
                                                   " created and exchanged using the Diffie-Hellman key exchange " +
                                                   " algorithm.|\n" +
                        "| :user-name s            | A user-name if the server requires authentication|\n" +
                        "| :password s             | A password if the server requires authentication|\n" +
                        "| :socket-snd-buf-size n  | The client socket's send buffer size.¶" +
                                                   " Defaults to `-1` (use the sockets default buf size).¶" +
                                                   " The size can be specified as a number like `64536`" +
                                                   " or a number with a unit like `:64KB` or `:1MB`.|\n" +
                        "| :socket-rcv-buf-size n  | The client socket's receive buffer size.¶" +
                                                   " Defaults to `-1` (use the sockets default buf size).¶" +
                                                   " The size can be specified as a number like `64536`" +
                                                   " or a number with a unit like `:64KB` or `:1MB`.|\n\n" +
                        "**The client is thread-safe!** \n\n" +
                        "**The client must be closed after use!**\n\n" +
                        "[See Inter-Process-Communication](https://github.com/jlangch/venice/blob/master/doc/readme/ipc.md)")
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
                        "             client-2 (ipc/client \"localhost\" 33333)                          \n" +
                        "             client-3 (ipc/client :localhost 33333)                             \n" +
                        "             client-4 (ipc/client \"af-inet://localhost:33333\")]               \n" +
                        "    (send client-1 (ipc/plain-text-message \"1\" \"test\" \"hello\"))           \n" +
                        "    (send client-2 (ipc/plain-text-message \"2\" \"test\" \"hello\"))           \n" +
                        "    (send client-3 (ipc/plain-text-message \"3\" \"test\" \"hello\"))           \n" +
                        "    (send client-4 (ipc/plain-text-message \"4\" \"test\" \"hello\"))))         ")
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
                        "ipc/message->json",
                        "ipc/benchmark")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final Builder clientConfig = ClientConfig.builder();

                if (args.size() == 1) {
                    // [port] or [conn-uri]
                    setConfigConnection(args.first(), clientConfig);
                    return new VncJavaObject(
                                    Client
                                          .of(clientConfig.build())
                                          .open());

                }
                else if (args.size() == 2) {
                    // [host port]
                    final String host = Types.isVncKeyword(args.first())
                                          ? Coerce.toVncKeyword(args.first()).getSimpleName()
                                          : Coerce.toVncString(args.first()).getValue();
                    final int port = Coerce.toVncLong(args.second()).getIntValue();

                    clientConfig.conn(host, port);
                    return new VncJavaObject(
                                  Client
                                        .of(clientConfig.build())
                                        .open());
                }
                else {
                    // [host port & options] or [port & options] or [conn-uri & options]
                    final boolean host_and_port = Types.isVncString(args.first()) && Types.isVncLong(args.second())
                                                || Types.isVncKeyword(args.first()) && Types.isVncLong(args.second());

                    final boolean port_or_uri = Types.isVncLong(args.first()) || Types.isVncString(args.first());

                    final VncHashMap options;

                    if (host_and_port) {
                        options = VncHashMap.ofAll(args.slice(2));

                        final String host = Types.isVncKeyword(args.first())
                                                ? Coerce.toVncKeyword(args.first()).getSimpleName()
                                                : Coerce.toVncString(args.first()).getValue();
                        final int port = Coerce.toVncLong(args.second()).getIntValue();
                        clientConfig.conn(host, port);
                    }
                    else if (port_or_uri){
                        options = VncHashMap.ofAll(args.slice(1));
                        setConfigConnection(args.first(), clientConfig);
                    }
                    else {
                        throw new VncException(
                                "'ipc/client' called with invalid host/port or connection URI arguments!");
                    }

                    // Options
                    final VncVal encryptVal = options.get(new VncKeyword("encrypt"), VncBoolean.False);
                    final VncVal userVal = options.get(new VncKeyword("user-name"));
                    final VncVal pwdVal = options.get(new VncKeyword("password"));
                    final VncVal sndBufSizeVal = options.get(new VncKeyword("socket-snd-buf-size"), new VncLong(-1));
                    final VncVal rcvBufSizeVal = options.get(new VncKeyword("socket-rcv-buf-size"), new VncLong(-1));

                    final boolean encrypt = Coerce.toVncBoolean(encryptVal).getValue();
                    final String user = userVal == Nil ? null : Coerce.toVncString(userVal).getValue();
                    final String pwd = pwdVal == Nil ? null : Coerce.toVncString(pwdVal).getValue();
                    final int sndBufSize = (int)convertUnitValueToLong(sndBufSizeVal);
                    final int rcvBufSize = (int)convertUnitValueToLong(rcvBufSizeVal);

                    clientConfig.sendBufferSize(sndBufSize);
                    clientConfig.receiveBufferSize(rcvBufSize);
                    clientConfig.encrypt(encrypt);

                    return new VncJavaObject(
                                    Client
                                          .of(clientConfig.build())
                                          .open(user, pwd));
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
                        "    (send client-1 (ipc/plain-text-message \"1\" \"test\" \"hello 1\"))         \n" +
                        "    (send client-2 (ipc/plain-text-message \"2\" \"test\" \"hello 2\"))         \n" +
                        "    (send client-3 (ipc/plain-text-message \"3\" \"test\" \"hello 3\"))))       ")
                    .seeAlso(
                        "ipc/client",
                        "ipc/close",
                        "ipc/running?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);

                final Client cloned = client.openClone();

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
                if (delegate instanceof Server) {
                    return VncBoolean.of(((Server)delegate).isRunning());
                }
                else if (delegate instanceof Client) {
                    return VncBoolean.of(((Client)delegate).isRunning());
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
                if (delegate instanceof Server) {
                    try {
                        ((Server)delegate).close();
                    }
                    catch(Exception ex) {
                        throw new VncException("Failed to close IPC server", ex);
                    }
                    return Nil;
                }
                else if (delegate instanceof Client) {
                    try {
                        ((Client)delegate).close();
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
    // Benchmark
    // ------------------------------------------------------------------------

    public static VncFunction ipc_benchmark =
        new VncFunction(
                "ipc/benchmark",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/benchmark conn-uri msg-size duration & options)")
                    .doc(
                        "Runs a benchmark.\n\n" +
                        "*Arguments:* \n\n" +
                        "| [![text-align: left; width: 10%]] | [![text-align: left; width: 90%]] |\n" +
                        "| conn-uri u  | A connection URI¶" +
                                       " \u00A0 • TCP/IP sockets¶" +
                                       " \u00A0\u00A0\u00A0 `af-inet://localhost:33333`¶" +
                                       " \u00A0 • Unix domain sockets (requires junixsocket libraries!)¶" +
                                       " \u00A0\u00A0\u00A0 `af-unix:///data/ipc/test.sock`|\n" +
                        "| msg-size n  | The message payload size (payload is random data).¶" +
                                       " The size can be specified as a number like `16384`" +
                                       " or a number with a unit like `:16KB` or `:1MB`.|\n" +
                        "| duration n  | The duration in seconds.|\n\n" +
                        "*Options:* \n\n" +
                        "| [![text-align: left; width: 25%]] | [![text-align: left; width: 75%]] |\n" +
                        "| :mode m                      | Operation mode `:client`, `:server`, or `:client-server`. " +
                                                        "Defaults to `:client-server`|\n" +
                        "| :print b                     | If `true` print the result to stdout. Defaults to `false`|\n" +
                        "| :oneway b                    | If `true` send oneway messages. Defaults to `false`|\n" +
                        "| :encrypt b                   | If `true` encrypt the messages. Defaults to `false`|\n" +
                        "| :connections n               | The number of parallel connctions. Defaults to 1|\n" +
                        "| :socket-snd-buf-size n       | The server socket's send buffer size.¶" +
                                                        " Defaults to `-1` (use the sockets default buf size).¶" +
                                                        " The size can be specified as a number like `64536`" +
                                                        " or a number with a unit like `:64KB` or `:1MB`.|\n" +
                        "| :socket-rcv-buf-size n       | The server socket's receive buffer size.¶" +
                                                        " Defaults to `-1` (use the sockets default buf size).¶" +
                                                        " The size can be specified as a number like `64536`" +
                                                        " or a number with a unit like `:64KB` or `:1MB`.|\n" +
                        "| :ramp-up n                   | The ramp-up duration in seconds. Defaults to 0s.|\n\n" +
                        "Prints this statistics if the *print* option is enabled:\n\n" +
                        "```                           \n" +
                        "Messages:         79370       \n" +
                        "Payload size:     50 KB       \n" +
                        "Encryption:       off         \n" +
                        "------------------------------\n" +
                        "Duration:         5.0 s       \n" +
                        "Total bytes:      3875.5 MB   \n" +
                        "Throughput msgs:  15871 msg/s \n" +
                        "Throughput bytes: 775 MB/s    \n" +
                        "```                           \n\n" +
                        "With deactivated *print* option a map with detailed statistics values will be returned.")
                    .examples(
                        "(ipc/benchmark \"af-inet://localhost:33333\"  \n" +
                        "               :5KB                           \n" +
                        "               5                              \n" +
                        "               :print true)                   ",
                        "(ipc/benchmark \"af-inet://localhost:33333\"  \n" +
                        "               :5KB                           \n" +
                        "               5                              \n" +
                        "               :print true                    \n" +
                        "               :ramp-up 1)                    ",
                        "(ipc/benchmark \"af-unix:///Users/foo/Desktop/venice/tmp/test.sock\"   \n" +
                        "               :5KB                                                    \n" +
                        "               5                                                       \n" +
                        "               :socket-snd-buf-size :128KB                             \n" +
                        "               :socket-rcv-buf-size :128KB                             \n" +
                        "               :print true)                                            ")
                     .seeAlso(
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                try {
                    // -- Parse arguments -----------------------------------------
                    final String sConnURI = Coerce.toVncString(args.first()).getValue();
                    final long msgSize   = convertUnitValueToLong((args.second()));
                    final long duration  = Coerce.toVncLong(args.third()).getValue();

                    // options
                    final VncHashMap options = VncHashMap.ofAll(args.slice(3));

                    final VncVal modeVal        = options.get(new VncKeyword("mode"), new VncKeyword("client-server"));
                    final VncVal printVal       = options.get(new VncKeyword("print"), VncBoolean.False);
                    final VncVal encryptVal     = options.get(new VncKeyword("encrypt"), VncBoolean.False);
                    final VncVal onewayVal      = options.get(new VncKeyword("onway"), VncBoolean.False);
                    final VncVal connectionsVal = options.get(new VncKeyword("connections"), new VncLong(1));
                    final VncVal sndBufSizeVal  = options.get(new VncKeyword("socket-snd-buf-size"), new VncLong(-1));
                    final VncVal rcvBufSizeVal  = options.get(new VncKeyword("socket-rcv-buf-size"), new VncLong(-1));
                    final VncVal rampUpVal      = options.get(new VncKeyword("ramp-up"), new VncLong(0));

                    final String mode     = Coerce.toVncKeyword(modeVal).getValue();
                    final boolean print   = Coerce.toVncBoolean(printVal).getValue();
                    final boolean encrypt = Coerce.toVncBoolean(encryptVal).getValue();
                    final boolean oneway  = Coerce.toVncBoolean(onewayVal).getValue();
                    final int connections = Coerce.toVncLong(connectionsVal).toJavaInteger();
                    final int sndBufSize  = (int)convertUnitValueToLong(sndBufSizeVal);
                    final int rcvBufSize  = (int)convertUnitValueToLong(rcvBufSizeVal);
                    final int rampUp      = Coerce.toVncLong(rampUpVal).toJavaInteger();

                    final HashMap<String,Object> params = new HashMap<>();
                    params.put("connection-uri",      sConnURI);
                    params.put("msg-size",            msgSize);
                    params.put("duration",            duration);
                    params.put("connections",         connections);
                    params.put("socket-snd-buf-size", sndBufSize);
                    params.put("socket-rcv-buf-size", rcvBufSize);
                    params.put("ramp-up",             rampUp);
                    params.put("mode",                mode);

                    final Benchmark benchmark =   new Benchmark(
                                                        sConnURI,
                                                        msgSize,
                                                        duration,
                                                        connections,
                                                        print,
                                                        encrypt,
                                                        oneway,
                                                        sndBufSize,
                                                        rcvBufSize,
                                                        rampUp);

                    if (mode.equals("client-server")) {
                        final Map<String,Object> result = benchmark.run();
                        if (result == null) {
                            return Nil;
                        }
                        else {
                            result.put("params", params);
                            return JavaInteropUtil.convertToVncVal(result, true);
                        }
                    }
                    else if (mode.equals("client")) {
                        final Map<String,Object> result = benchmark.runClient();
                        if (result == null) {
                            return Nil;
                        }
                        else {
                            result.put("params", params);
                            return JavaInteropUtil.convertToVncVal(result, true);
                        }
                    }
                    else if (mode.equals("server")) {
                        benchmark.runServer();
                        return Nil;
                    }
                    else {
                        throw new IpcException(
                                "Invalide mode!. Please use one of :client, :server, or :client-server");
                    }
                }
                catch(Exception ex) {
                    throw new IpcException("Benchmark failed!", ex);
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
                        "(ipc/send client message)")
                    .doc(
                        "Sends a message to the server the client is associated with. \n\n" +
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
                        "| message m | The message to send|\n\n" +
                        "**Note**\n\n" +
                        "If the server's handler function takes more than a couple of 10 milliseconds " +
                        "to process the request, consider to use *offer/poll* with a reply queue " +
                        "to improve the system throughput!")
                    .examples(
                        ";; echo handler                                                   \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"              \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler processing JSON message data                           \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [data   (json/read-str (. m :getText))                   \n" +
                        "          result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "      (ipc/text-message (. m :getRequestId)                       \n" +
                        "                        (. m :getTopic)                           \n" +
                        "                        \"application/json\" :UTF-8               \n" +
                        "                        result)))                                 \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/text-message \"1\"                                  \n" +
                        "                           \"test\"                               \n" +
                        "                           \"application/json\" :UTF-8            \n" +
                        "                           (json/write-str {\"x\" 100 \"y\" 200}))\n" +
                        "         (ipc/send client)                                        \n" +
                        "         (ipc/message->map)                                       \n" +
                        "         (println))))                                             ",

                        ";; handler with remote code execution                             \n" +
                        ";; request: \"(+ 1 2)\" => exec => response: \"3\"                \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [cmd    (. m :getText)                                   \n" +
                        "          result (str (eval (read-string cmd)))]                  \n" +
                        "      (ipc/plain-text-message (. m :getRequestId)                 \n" +
                        "                              (. m :getTopic)                     \n" +
                        "                              result)))                           \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (->> (ipc/plain-text-message \"1\" \"exec\" \"(+ 1 2)\")      \n" +
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
                ArityExceptions.assertArity(this, args, 2);

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                final IMessage request = Coerce.toVncJavaObject(args.nth(1), IMessage.class);

                final IMessage response = client.sendMessage(request);
                return response == null ? Nil : new VncJavaObject(response);
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
                        "| message m | The message to send|\n\n" +
                        "**Note**\n\n" +
                        "If the server's handler function takes more than a couple of 10 milliseconds " +
                        "to process the request, consider to use *offer/poll* with a reply queue " +
                        "to improve the system throughput!")
                    .examples(
                        ";; echo handler                                                   \n" +
                        ";; request: \"hello\" => echo => response: \"hello\"              \n" +
                        "(do                                                               \n" +
                        "  (defn echo-handler [m] m)                                       \n" +
                        "  (try-with [server (ipc/server 33333 echo-handler)               \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (-<> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
                        "         (ipc/send-async client <>)                               \n" +
                        "         (deref <> 1_000 :timeout)                                \n" +
                        "         (ipc/message->map <>)                                    \n" +
                        "         (println <>))))                                          ",

                        ";; handler processing JSON message data                           \n" +
                        ";; request: {\"x\": 100, \"y\": 200} => add => response: {\"z\": 300}  \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [data   (json/read-str (. m :getText))                   \n" +
                        "          result (json/write-str { \"z\" (+ (get data \"x\") (get data \"y\"))})]  \n" +
                        "      (ipc/text-message (. m :getRequestId)                       \n" +
                        "                        (. m :getTopic)                           \n" +
                        "                        \"application/json\" :UTF-8               \n" +
                        "                        result)))                                 \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (-<> (ipc/text-message \"1\"                                  \n" +
                        "                           \"test\"                               \n" +
                        "                           \"application/json\" :UTF-8            \n" +
                        "                           (json/write-str {\"x\" 100 \"y\" 200}))\n" +
                        "         (ipc/send-async client <>)                               \n" +
                        "         (deref <> 1_000 :timeout)                                \n" +
                        "         (ipc/message->map <>)                                    \n" +
                        "         (println <>))))                                          ",

                        ";; handler with remote code execution                             \n" +
                        ";; request: \"(+ 1 2)\" => exec => response: \"3\"                \n" +
                        "(do                                                               \n" +
                        "  (defn handler [m]                                               \n" +
                        "    (let [cmd    (. m :getText)                                   \n" +
                        "          result (str (eval (read-string cmd)))]                  \n" +
                        "      (ipc/plain-text-message (. m :getRequestId)                 \n" +
                        "                              (. m :getTopic)                     \n" +
                        "                              result)))                           \n" +
                        "  (try-with [server (ipc/server 33333 handler)                    \n" +
                        "             client (ipc/client \"localhost\" 33333)]             \n" +
                        "    (-<> (ipc/plain-text-message \"1\" \"exec\" \"(+ 1 2)\")      \n" +
                        "         (ipc/send-async client <>)                               \n" +
                        "         (deref <> 1_000 :timeout)                                \n" +
                        "         (ipc/message->map <>)                                    \n" +
                        "         (println <>))))                                          ")
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

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
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
                        "Does not wait for a response and returns always `nil`.")
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
                        "    ;;    requestId=\"1\" and \"2\", subject=\"test\", payload=\"hello\"            \n" +
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

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
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
                        "Multiple subscriptions with different handlers are supported.\n\n" +
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
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server  (ipc/server 33333)                              \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; create topic :test                                            \n" +
                        "    (ipc/create-topic server :test)                                  \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic 'test'              \n" +
                        "    (ipc/subscribe client1 \"test\" client-subscribe-handler)        \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", subject=\"test\", payload=\"hello\"        \n" +
                        "    (let [m (ipc/plain-text-message \"1\" \"test\" \"hello\")]       \n" +
                        "      (println \"PUBLISHED:\" (ipc/message->json true m))            \n" +
                        "      (ipc/publish client2 m))                                       \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/unsubscribe",
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

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                final VncVal topicVal = args.nth(1);
                final VncFunction handler = Coerce.toVncFunction(args.nth(2));

                final String topicName = Coerce.toVncString(topicVal).getValue();

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

                final IMessage response = client.subscribe(topicName, handlerWrapper);

                return new VncJavaObject(response);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_unsubscribe =
        new VncFunction(
                "ipc/unsubscribe",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/unsubscribe client topic)")
                    .doc(
                        "Unsubscribe from a topic.\n\n" +
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
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server  (ipc/server 33333)                              \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; create topic :test                                            \n" +
                        "    (ipc/create-topic server :test)                                  \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic 'test'              \n" +
                        "    (ipc/subscribe client1 \"test\" client-subscribe-handler)        \n" +
                        "                                                                     \n" +
                        "    ;; client1 unsubscribes from messages with topic 'test'          \n" +
                        "    (ipc/unsubscribe client1 \"test\")                               \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", subject=\"test\", payload=\"hello\"        \n" +
                        "    (let [m (ipc/plain-text-message \"1\" \"test\" \"hello\")]       \n" +
                        "      (println \"PUBLISHED:\" (ipc/message->json true m))            \n" +
                        "      (ipc/publish client2 m))                                       \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/subscribe",
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
                ArityExceptions.assertArity(this, args, 2);

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                final VncVal topicVal = args.nth(1);

                final String topicName = Coerce.toVncString(topicVal).getValue();

                final IMessage response = client.unsubscribe(topicName);

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
                        "(ipc/publish client topic-name message)")
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
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server  (ipc/server 33333)                              \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; create topic :test                                            \n" +
                        "    (ipc/create-topic server :test)                                  \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic :test               \n" +
                        "    (ipc/subscribe client1 :test client-subscribe-handler)           \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", subject=:testing, payload=\"hello\"        \n" +
                        "    (let [m (ipc/plain-text-message \"1\" :testing \"hello\")]       \n" +
                        "      (println \"PUBLISHING:\" (ipc/message->json true m))           \n" +
                        "      (->> (ipc/publish client2 :test m)                             \n" +
                        "           (ipc/message->json true)                                  \n" +
                        "           (println \"PUBLISHED:\")))                                \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/publish-async",
                        "ipc/subscribe",
                        "ipc/unsubscribe",
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

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                final String topicName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                final IMessage request =  Coerce.toVncJavaObject(args.nth(2), IMessage.class);

                final IMessage response = client.publish(topicName, request);

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
                        "(ipc/publish-async topic-name client message)")
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
                        "  (defn client-subscribe-handler [m]                                 \n" +
                        "    (println \"SUBSCRIBED:\" (ipc/message->json true m)))            \n" +
                        "                                                                     \n" +
                        "  (try-with [server  (ipc/server 33333)                              \n" +
                        "             client1 (ipc/client \"localhost\" 33333)                \n" +
                        "             client2 (ipc/client \"localhost\" 33333)]               \n" +
                        "                                                                     \n" +
                        "    ;; create topic :test                                            \n" +
                        "    (ipc/create-topic server :test)                                  \n" +
                        "                                                                     \n" +
                        "    ;; client1 subscribes to messages with topic :test'              \n" +
                        "    (ipc/subscribe client1 :test client-subscribe-handler)           \n" +
                        "                                                                     \n" +
                        "    ;; client2 publishes a plain text message:                       \n" +
                        "    ;;   requestId=\"1\", subject=:testing, payload=\"hello\"        \n" +
                        "    (let [m (ipc/plain-text-message \"1\" :testing \"hello\")]       \n" +
                        "      (println \"PUBLISHING:\" (ipc/message->json true m))           \n" +
                        "      (-<> (ipc/publish-async client2 :test m)                       \n" +
                        "           (deref <> 1_000 :timeout)                                 \n" +
                        "           (ipc/message->json true <>)                               \n" +
                        "           (println \"PUBLISHED:\" <>)))                             \n" +
                        "                                                                     \n" +
                        "    (sleep 300)))                                                    ")
                    .seeAlso(
                        "ipc/publish",
                        "ipc/subscribe",
                        "ipc/unsubscribe",
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

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                final String topicName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                final IMessage request =  Coerce.toVncJavaObject(args.nth(2), IMessage.class);

                return new VncJavaObject(
                        new FutureWrapper(
                           client.publishAsync(topicName, request)));
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
                        "(ipc/offer client queue-name queue-offer-timeout message)",
                        "(ipc/offer client queue-name reply-to-queue-name queue-offer-timeout message)")
                    .doc(
                        "Offers a message to the named queue.\n\n" +
                        "Returns the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c              | A client to send the offer message from |\n" +
                        "| queue-name q          | A queue name (string or keyword) to offer the message to|\n" +
                        "| reply-to-queue-name q | An optional reply-to queue name (string or keyword) where replies are sent to |\n" +
                        "| queue-offer-timeout t | The maximum time in milliseconds the server waits offering the message to the queue.¶A timeout of -1 means wait as long as it takes.|\n" +
                        "| message m             | The offer request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message added to the queue\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while offering the message to the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_FULL`      - the queue is full, offer rejected")
                    .examples(
                        "(do                                                                                         \n" +
                        "  ;; thread-safe printing                                                                   \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                         \n" +
                        "                                                                                            \n" +
                        "  (try-with [server  (ipc/server 33333)                                                     \n" +
                        "             client1 (ipc/client 33333)                                                     \n" +
                        "             client2 (ipc/client 33333)]                                                    \n" +
                        "    ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "    (ipc/create-queue server :orders 100)                                                   \n" +
                        "                                                                                            \n" +
                        "    ;; client1 offers order Venice data message to the queue                                \n" +
                        "    ;;   requestId=\"1\" and \"2\", subject=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "    (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "      (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                            \n" +
                        "      ;; publish the order                                                                  \n" +
                        "      (->> (ipc/offer client1 :orders 300 order)                                            \n" +
                        "           (ipc/message->json true)                                                         \n" +
                        "           (println \"OFFERED:\")))                                                         \n" +
                        "                                                                                            \n" +
                        "    ;; client2 pulls next order from the queue                                              \n" +
                        "    (->> (ipc/poll client2 :orders 300)                                                     \n" +
                        "         (ipc/message->json true)                                                           \n" +
                        "         (println \"POLLED:\"))))                                                          ")
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
                ArityExceptions.assertArity(this, args, 4, 5);

                if (args.size() == 4) {
                    final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                    final String queueName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                    final String replyToQueueName = null;
                    final long timeout = Coerce.toVncLong(args.nth(2)).toJavaLong();
                    final IMessage request = Coerce.toVncJavaObject(args.nth(3), IMessage.class);

                    return new VncJavaObject(client.offer(request, queueName, replyToQueueName, timeout));
                }
                else {
                    final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                    final String queueName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                    final String replyToQueueName = args.nth(2) == Nil ? null : StringUtil.trimToNull(Coerce.toVncString(args.nth(2)).getValue());
                    final long timeout = Coerce.toVncLong(args.nth(3)).toJavaLong();
                    final IMessage request = Coerce.toVncJavaObject(args.nth(4), IMessage.class);

                    return new VncJavaObject(client.offer(request, queueName, replyToQueueName, timeout));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_offer_async =
        new VncFunction(
                "ipc/offer-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/offer client queue-name queue-offer-timeout message)",
                        "(ipc/offer client queue-name reply-to-queue-name queue-offer-timeout message)")
                    .doc(
                        "Offers a message to the named queue.\n\n" +
                        "Returns a future with the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c              | A client to send the offer message from |\n" +
                        "| queue-name q          | A queue name (string or keyword) to offer the message to|\n" +
                        "| reply-to-queue-name q | An optional reply-to queue name (string or keyword) where replies are sent to |\n" +
                        "| queue-offer-timeout t | The maximum time in milliseconds the server waits offering the message to the queue.¶A timeout of -1 means wait as long as it takes.|\n" +
                        "| message m             | The offer request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message added to the queue\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while offering the message to the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_FULL`      - the queue is full, offer rejected")
                    .examples(
                        "(do                                                                                         \n" +
                        "  ;; thread-safe printing                                                                   \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                         \n" +
                        "                                                                                            \n" +
                        "  (try-with [server  (ipc/server 33333)                                                     \n" +
                        "             client1 (ipc/client 33333)                                                     \n" +
                        "             client2 (ipc/client 33333)]                                                    \n" +
                        "    ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "    (ipc/create-queue server :orders 100)                                                   \n" +
                        "                                                                                            \n" +
                        "    ;; client1 offers order Venice data message to the queue                                \n" +
                        "    ;;   requestId=\"1\" and \"2\", subject=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "    (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "      (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                            \n" +
                        "      ;; publish the order                                                                  \n" +
                        "      (-<> (ipc/offer-async client1 :orders 300 order)                                      \n" +
                        "           (deref <> 1_000 :timeout)                                                        \n" +
                        "           (ipc/message->json true <>)                                                      \n" +
                        "           (println \"OFFERED:\" <>)))                                                      \n" +
                        "                                                                                            \n" +
                        "    ;; client2 pulls next order from the queue                                              \n" +
                        "    (-<> (ipc/poll-async client2 :orders 300)                                               \n" +
                        "         (deref <> 1_000 :timeout)                                                          \n" +
                        "         (ipc/message->json true <>)                                                        \n" +
                        "         (println \"POLLED:\" <>))))                                                       ")
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
                ArityExceptions.assertArity(this, args, 4, 5);

                if (args.size() == 4) {
                    final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                    final String queueName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                    final String replyToQueueName = null;
                    final long timeout = Coerce.toVncLong(args.nth(2)).toJavaLong();
                    final IMessage request = Coerce.toVncJavaObject(args.nth(3), IMessage.class);

                    return new VncJavaObject(
                            new FutureWrapper(
                                client.offerAsync(request, queueName, replyToQueueName, timeout)));
                }
                else {
                    final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);
                    final String queueName = StringUtil.trimToNull(Coerce.toVncString(args.nth(1)).getValue());
                    final String replyToQueueName = args.nth(2) == Nil ? null : StringUtil.trimToNull(Coerce.toVncString(args.nth(2)).getValue());
                    final long timeout = Coerce.toVncLong(args.nth(3)).toJavaLong();
                    final IMessage request = Coerce.toVncJavaObject(args.nth(4), IMessage.class);

                    return new VncJavaObject(
                            new FutureWrapper(
                                client.offerAsync(request, queueName, replyToQueueName, timeout)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_poll =
        new VncFunction(
                "ipc/poll",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/poll client queue-name queue-poll-timeout)")
                    .doc(
                        "Polls a message from the named queue.\n\n" +
                        "Returns the server's response message.\n\n" +
                       "*Arguments:* \n\n" +
                        "| client c             | A client to send the poll message from |\n" +
                        "| queue-name q         | A queue name (string or keyword) to poll the message to|\n" +
                        "| queue-poll-timeout t | The maximum time in milliseconds the server waits to poll a the message from the queue.¶A timeout of -1 means wait as long as it takes.|\n" +
                        "| message m            | The poll request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message successfully polled from the queue, response holds the data\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while polling a message from the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_EMPTY`     - the queue is empty")
                    .examples(
                        "(do                                                                                         \n" +
                        "  ;; thread-safe printing                                                                   \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                         \n" +
                        "                                                                                            \n" +
                        "  (try-with [server  (ipc/server 33333)                                                     \n" +
                        "             client1 (ipc/client 33333)                                                     \n" +
                        "             client2 (ipc/client 33333)]                                                    \n" +
                        "    ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "    (ipc/create-queue server :orders 100)                                                   \n" +
                        "                                                                                            \n" +
                        "    ;; client1 offers order Venice data message to the queue                                \n" +
                        "    ;;   requestId=\"1\" and \"2\", subject=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "    (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "      (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                            \n" +
                        "      ;; publish the order                                                                  \n" +
                        "      (->> (ipc/offer client1 :orders 300 order)                                            \n" +
                        "           (ipc/message->json true)                                                         \n" +
                        "           (println \"OFFERED:\")))                                                         \n" +
                        "                                                                                            \n" +
                        "    ;; client2 pulls next order from the queue                                              \n" +
                        "    (->> (ipc/poll client2 :orders 300)                                                     \n" +
                        "         (ipc/message->json true)                                                           \n" +
                        "         (println \"POLLED:\"))))                                                          ")
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

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final long timeout = Coerce.toVncLong(args.third()).toJavaLong();

                return new VncJavaObject(client.poll(name, timeout));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_poll_async =
        new VncFunction(
                "ipc/poll-async",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/poll-async client queue-name, queue-poll-timeout)")
                    .doc(
                        "Polls a message from the named queue.\n\n" +
                        "Returns a future with the server's response message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c             | A client to send the poll message from |\n" +
                        "| queue-name q         | A queue name (string or keyword) to poll the message to|\n" +
                        "| queue-poll-timeout t | The maximum time in milliseconds the server waits to poll a the message from the queue.¶A timeout of -1 means wait as long as it takes.|\n" +
                        "| message m            | The poll request message|\n\n" +
                        "The server returns a response message with one of these status:\n\n" +
                        "  * `:OK`              - message successfully polled from the queue, response holds the data\n" +
                        "  * `:SERVER_ERROR`    - indicates a server while polling a message from the queue\n" +
                        "  * `:BAD_REQUEST`     - invalid request, details in the payload\n" +
                        "  * `:QUEUE_NOT_FOUND` - the queue does not exist\n" +
                        "  * `:QUEUE_EMPTY`     - the queue is empty")
                    .examples(
                        "(do                                                                                         \n" +
                        "  ;; thread-safe printing                                                                   \n" +
                        "  (defn println [& msg] (locking println (apply core/println msg)))                         \n" +
                        "                                                                                            \n" +
                        "  (try-with [server  (ipc/server 33333)                                                     \n" +
                        "             client1 (ipc/client 33333)                                                     \n" +
                        "             client2 (ipc/client 33333)]                                                    \n" +
                        "    ;; create a queue to allow client1 and client2 to exchange messages                     \n" +
                        "    (ipc/create-queue server :orders 100)                                                   \n" +
                        "                                                                                            \n" +
                        "    ;; client1 offers order Venice data message to the queue                                \n" +
                        "    ;;   requestId=\"1\" and \"2\", subject=\"order\", payload={:item \"espresso\", :count 2} \n" +
                        "    (let [order (ipc/venice-message \"1\" \"order\" {:item \"espresso\", :count 2})]        \n" +
                        "      (println \"ORDER:\" (ipc/message->json true order))                                   \n" +
                        "                                                                                            \n" +
                        "      ;; publish the order                                                                  \n" +
                        "      (-<> (ipc/offer-async client1 :orders 300 order)                                      \n" +
                        "           (deref <> 1_000 :timeout)                                                        \n" +
                        "           (ipc/message->json true <>)                                                      \n" +
                        "           (println \"OFFERED:\" <>)))                                                      \n" +
                        "                                                                                            \n" +
                        "    ;; client2 pulls next order from the queue                                              \n" +
                        "    (-<> (ipc/poll-async client2 :orders 300)                                               \n" +
                        "         (deref <> 1_000 :timeout)                                                          \n" +
                        "         (ipc/message->json true <>)                                                        \n" +
                        "         (println \"POLLED:\" <>))))                                                       ")
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
                ArityExceptions.assertArity(this, args, 3);

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                final String name = Coerce.toVncString(args.second()).getValue();
                final long timeout = Coerce.toVncLong(args.third()).toJavaLong();

                return new VncJavaObject(
                        new FutureWrapper(
                            client.pollAsync(name, timeout)));
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
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
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

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);

                final Map<String,Object> status = client.getServerStatus();
                return JavaInteropUtil.convertToVncVal(status, true);
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
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")            \n" +
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

                final Client client = Coerce.toVncJavaObject(args.nth(0), Client.class);

                final Map<String,Object> statistics = client.getServerThreadPoolStatistics();
                return JavaInteropUtil.convertToVncVal(statistics, true);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    // ------------------------------------------------------------------------
    // Authenticator
    // ------------------------------------------------------------------------

    public static VncFunction ipc_authenticator =
        new VncFunction(
                "ipc/authenticator",
                VncFunction
                    .meta()
                    .arglists("(ipc/authenticator)")
                    .doc(
                        "Creates a new authenticator the manages the credential on behalf of " +
                        "the server.\n\n" +
                        "The authenticator stores passwords as salted PBKDF2 hashes! It does not keep " +
                        "the clear text passwords.")
                    .examples(
                        "(ipc/authenticator)",
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")         \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\" :admin)) ")
                    .seeAlso(
                        "ipc/load-authenticator",
                        "ipc/store-authenticator",
                        "ipc/add-credentials")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);
                final Authenticator authenticator = new Authenticator(true);
                return new VncJavaObject(authenticator);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_load_authenticator =
        new VncFunction(
                "ipc/load-authenticator",
                VncFunction
                    .meta()
                    .arglists("(ipc/load-authenticator source)")
                    .doc("Loads an authenticator from a file or an input stream.")
                    .examples(
                        "(let [auth (ipc/authenticator)]                               \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")                  \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\")                  \n" +
                        "  (ipc/store-authenticator auth (io/file \"./ipc.cred\"))     \n" +
                        "  (let [b (ipc/load-authenticator (io/file \"./ipc.cred\"))]  \n" +
                        "     ;; ...                                                   \n" +
                        "     ))                                                       ")
                    .seeAlso(
                        "ipc/authenticator",
                        "ipc/store-authenticator",
                        "ipc/add-credentials")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal source = args.first();

                final Authenticator authenticator = new Authenticator(true);
                if (Types.isVncJavaObject(source, InputStream.class)) {
                    authenticator.load(Coerce.toVncJavaObject(source, InputStream.class));
                }
                else if (Types.isVncJavaObject(source, File.class)) {
                    authenticator.load(Coerce.toVncJavaObject(source, File.class));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'ipc/load-authenticator' does not allow %s as source",
                            Types.getType(source)));
                }
                return new VncJavaObject(authenticator);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_store_authenticator =
        new VncFunction(
                "ipc/store-authenticator",
                VncFunction
                    .meta()
                    .arglists("(ipc/store-authenticator authenticator dest)")
                    .doc(
                        "Stores an authenticator to a file or an output stream.\n\n" +
                        "Passwords are stored as salted PBKDF2 hashes!")
                    .examples(
                        "(let [auth (ipc/authenticator)]                             \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")                \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\")                \n" +
                        "  (ipc/store-authenticator auth (io/file \"./ipc.cred\")))  ")
                    .seeAlso(
                        "ipc/authenticator",
                        "ipc/load-authenticator",
                        "ipc/add-credentials")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Authenticator authenticator = Coerce.toVncJavaObject(args.first(), Authenticator.class);
                final VncVal dest = args.second();

                if (Types.isVncJavaObject(dest, OutputStream.class)) {
                    authenticator.save(Coerce.toVncJavaObject(dest, OutputStream.class));
                }
                else if (Types.isVncJavaObject(dest, File.class)) {
                    authenticator.save(Coerce.toVncJavaObject(dest, File.class));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'ipc/store-authenticator' does not allow %s as dest",
                            Types.getType(dest)));
                }
                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_add_credentials =
        new VncFunction(
                "ipc/add-credentials",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/add-credentials authenticator user-name password)",
                        "(ipc/add-credentials authenticator user-name password role)")
                    .doc(
                        "Adds user credentials to an authenticator.\n\n" +
                        "The only role currently support is the `:admin` role. The `:admin` is " +
                        "mandatory for clients to manage queues!")
                    .examples(
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")         \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\" :admin)) ")
                    .seeAlso(
                        "ipc/remove-credentials",
                        "ipc/clear-credentials",
                        "ipc/authenticator",
                        "ipc/load-authenticator",
                        "ipc/store-authenticator")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                final Authenticator authenticator = Coerce.toVncJavaObject(args.first(), Authenticator.class);
                final String userName = Coerce.toVncString(args.second()).toString();
                final String password = Coerce.toVncString(args.third()).toString();
                final String role     = args.size() > 3 ? Coerce.toVncKeyword(args.fourth()).getSimpleName() : null;

                if (role == null) {
                    authenticator.addCredentials(userName, password);
                }
                else if (Authenticator.isAdminRole(role)) {
                    authenticator.addCredentials(userName, password, true);
                }
                else {
                    throw new IpcException("Invalid role '" + role + "' !");
                }

                return new VncJavaObject(authenticator);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_remove_credentials =
        new VncFunction(
                "ipc/remove-credentials",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/remove-credentials authenticator user-name)")
                    .doc(
                        "Remove user credentials from an authenticator.")
                    .examples(
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")         \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\" :admin)  \n" +
                        "  (ipc/remove-credentials auth \"tom\"))             ")
                    .seeAlso(
                        "ipc/add-credentials",
                        "ipc/clear-credentials",
                        "ipc/authenticator",
                        "ipc/load-authenticator",
                        "ipc/store-authenticator")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Authenticator authenticator = Coerce.toVncJavaObject(args.first(), Authenticator.class);
                final String userName = Coerce.toVncString(args.second()).toString();

                authenticator.removeCredentials(userName);

                return new VncJavaObject(authenticator);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_clear_credentials =
        new VncFunction(
                "ipc/clear-credentials",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/clear-credentials authenticator)")
                    .doc(
                        "Clears all user credentials from an authenticator.")
                    .examples(
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\")         \n" +
                        "  (ipc/add-credentials auth \"max\" \"456\" :admin)  \n" +
                        "  (ipc/clear-credentials auth))                      ")
                    .seeAlso(
                        "ipc/add-credentials",
                        "ipc/remove-credentials",
                        "ipc/authenticator",
                        "ipc/load-authenticator",
                        "ipc/store-authenticator")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Authenticator authenticator = Coerce.toVncJavaObject(args.first(), Authenticator.class);

                authenticator.clearCredentials();

                return new VncJavaObject(authenticator);
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
                        "(ipc/text-message request-id topic mimetype charset text)",
                        "(ipc/text-message request-id topic mimetype charset text durable)",
                        "(ipc/text-message request-id topic mimetype charset text durable expires-at)",
                        "(ipc/text-message request-id topic mimetype charset text durable expires-val expires-unit)")
                    .doc(
                        "Creates a text message\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r   | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t        | A topic (string or keyword) |\n" +
                        "| mimetype m     | The mimetype of the payload text. A string like 'text/plain' |\n" +
                        "| charset c      | The charset of the payload text. A keyword like `:UTF-8`|\n" +
                        "| text t         | The message payload text (a string)|\n" +
                        "| durable b      | If `true` create a durable message if the server supports durable queues.¶Defaults to `false`.|\n" +
                        "| expires-at t   | Message expiration time in millis since epoch (may be `nil`)|\n" +
                        "| expires-val v  | Message expiration duration. E.g.: 2 (may be `nil`)|\n" +
                        "| expires-unit u | Message expiration time unit. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}|")
                    .examples(
                        "(->> (ipc/text-message \"1\" :test                       \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/text-message \"1\" \"test\"                    \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/text-message \"1\" \"test\"                    \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\"   \n" +
                        "                       false                             \n" +
                        "                       2 :hours)                         \n" +
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
                ArityExceptions.assertArity(this, args, 5, 6, 7, 8);

                final VncVal requestId;
                final VncString topic;
                final VncString mimetype ;
                final VncKeyword charset;
                final VncVal textVal;
                final VncVal durableVal;
                final VncVal expiresAtVal;

                if (args.size() == 5) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    durableVal = null;
                    expiresAtVal = null;
                }
                else if (args.size() == 6) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    durableVal = args.nth(5);
                    expiresAtVal = null;
                }
                else if (args.size() == 7) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    durableVal = args.nth(5);
                    expiresAtVal = args.nth(6);
                }
                else  {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    charset = Coerce.toVncKeyword(args.nth(3));
                    textVal = args.nth(4);
                    durableVal = args.nth(5);
                    expiresAtVal = args.nth(6) == Nil
                                 ? null
                                 : TimeFunctions.to_millis.applyOf(
                                    TimeFunctions.plus.applyOf(
                                        new VncJavaObject(LocalDateTime.now()),
                                        args.nth(7),    // unit
                                        args.nth(6)));  // n
                }

                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final boolean durable = durableVal != null
                                            && durableVal != Nil
                                            && Coerce.toVncBoolean(durableVal).getValue();

                final IMessage msg = MessageFactory.text(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                            expiresAtVal == null || expiresAtVal == Nil
                                            ? Messages.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAtVal).getValue(),
                                        durable,
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
                        "(ipc/plain-text-message request-id topic text)",
                        "(ipc/plain-text-message request-id topic text durable)",
                        "(ipc/plain-text-message request-id topic text durable expires-at)",
                        "(ipc/plain-text-message request-id topic text durable expires-val expires-unit)")
                    .doc(
                        "Creates a plain text message with mimetype `text/plain` and charset `:UTF-8`.\n\n"  +
                        "*Arguments:* \n\n" +
                        "| request-id r   | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t        | A topic (string or keyword) |\n" +
                        "| text t         | The message payload text (a string)|\n" +
                        "| durable b      | If `true` create a durable message if the server supports durable queues.¶Defaults to `false`.|\n" +
                        "| expires-at t   | Message expiration time in millis since epoch (may be `nil`)|\n" +
                        "| expires-val v  | Message expiration duration. E.g.: 2 (may be `nil`)|\n" +
                        "| expires-unit u | Message expiration time unit. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}|")
                    .examples(
                        "(->> (ipc/plain-text-message \"1\" :test \"hello\")       \n" +
                        "     (ipc/message->map)                                   \n" +
                        "     (println))                                           ",
                        "(->> (ipc/plain-text-message \"1\" \"test\" \"hello\")    \n" +
                        "     (ipc/message->map)                                   \n" +
                        "     (println))                                           ",
                        "(->> (ipc/plain-text-message \"100\" \"test\" \"hello\"   \n" +
                        "                             false                        \n" +
                        "                             2 :hours)                    \n" +
                        "     (ipc/message->map)                                   \n" +
                        "     (println))                                           ")
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
                ArityExceptions.assertArity(this, args, 3, 4, 5, 6);

                final VncVal requestId;
                final VncString topic;
                final VncVal textVal;
                final VncVal durableVal;
                final VncVal expiresAt;

                if (args.size() == 3) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    durableVal = null;
                    expiresAt = null;
                }
                else if (args.size() == 4) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = null;
                }
                else if (args.size() == 5) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = args.nth(4);
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    textVal = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = args.nth(4) == Nil
                                 ? null
                                 : TimeFunctions.to_millis.applyOf(
                                    TimeFunctions.plus.applyOf(
                                        new VncJavaObject(LocalDateTime.now()),
                                        args.nth(5),    // unit
                                        args.nth(4)));  // n
                }

                final String text = Types.isVncString(textVal)
                                        ? ((VncString)textVal).getValue()
                                        : textVal.toString(true);  // aggressively convert to string

                final boolean durable = durableVal != null
                                            && durableVal != Nil
                                            && Coerce.toVncBoolean(durableVal).getValue();

                final IMessage msg = MessageFactory.text(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Messages.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                            durable,
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
                        "(ipc/binary-message request-id topic mimetype data)",
                        "(ipc/binary-message request-id topic mimetype data durable)",
                        "(ipc/binary-message request-id topic mimetype data durable expires-at)",
                        "(ipc/binary-message request-id topic mimetype data durable expires-val expires-unit)")
                    .doc(
                        "Creates a binary message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r   | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t        | A topic (string or keyword) |\n" +
                        "| mimetype m     | The mimetype of the payload data. A string like 'application/octet-stream', 'image/png'|\n" +
                        "| data d         | The message payload binary data (a bytebuf)|\n" +
                        "| durable b      | If `true` create a durable message if the server supports durable queues.¶Defaults to `false`.|\n" +
                        "| expires-at t   | Message expiration time in millis since epoch (may be `nil`)|\n" +
                        "| expires-val v  | Message expiration duration. E.g.: 2 (may be `nil`)|\n" +
                        "| expires-unit u | Message expiration time unit. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}|")
            .examples(
                        "(->> (ipc/binary-message \"100\" :test                   \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7]))    \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/binary-message \"100\" \"test\"                \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7]))    \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/binary-message \"100\" \"test\"                \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7])     \n" +
                        "                         false                           \n" +
                        "                         (-> (time/local-date-time)      \n" +
                        "                             (time/plus :hours 2)        \n" +
                        "                             (time/to-millis)))          \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/binary-message \"100\" \"test\"                \n" +
                        "                         \"application/octet-stream\"    \n" +
                        "                         (bytebuf [0 1 2 3 4 5 6 7])     \n" +
                        "                         false                           \n" +
                        "                         2 :hours)                       \n" +
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
                ArityExceptions.assertArity(this, args, 4, 5, 6, 7);

                final VncVal requestId;
                final VncString topic;
                final VncString mimetype;
                final VncByteBuffer data;
                final VncVal durableVal;
                final VncVal expiresAt;

                if (args.size() == 4) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    durableVal = null;
                    expiresAt = null;
                }
                else if (args.size() == 5) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    durableVal = args.nth(4);
                    expiresAt = null;
                }
                else if (args.size() == 6) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    durableVal = args.nth(4);
                    expiresAt = args.nth(5);
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    mimetype = Coerce.toVncString(args.nth(2));
                    data = Coerce.toVncByteBuffer(args.nth(3));
                    durableVal = args.nth(4);
                    expiresAt = args.nth(5) == Nil
                                 ? null
                                 : TimeFunctions.to_millis.applyOf(
                                    TimeFunctions.plus.applyOf(
                                        new VncJavaObject(LocalDateTime.now()),
                                        args.nth(6),    // unit
                                        args.nth(5)));  // n
                }

                final boolean durable = durableVal != null
                                            && durableVal != Nil
                                            && Coerce.toVncBoolean(durableVal).getValue();

                final IMessage msg = MessageFactory.binary(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Messages.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        durable,
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
                        "(ipc/venice-message request-id topic data)",
                        "(ipc/venice-message request-id topic data durable)",
                        "(ipc/venice-message request-id topic data durable expires-at)",
                        "(ipc/venice-message request-id topic data durable expires-val expires-unit)")
                    .doc(
                        "Creates a venice message.\n\n" +
                        "The Venice data is serialized as JSON (mimetype: 'application/json') " +
                        "for transport within the message.\n\n" +
                        "*Arguments:* \n\n" +
                        "| request-id r   | A request ID (string, may be `nil`). May be used for idempotency checks by the receiver |\n" +
                        "| topic t        | A topic (string or keyword) |\n" +
                        "| data d         | The message payload Venice data (e.g.: a map, list, ...)|\n" +
                        "| durable b      | If `true` create a durable message if the server supports durable queues.¶Defaults to `false`.|\n" +
                        "| expires-at t   | Message expiration time in millis since epoch (may be `nil`)|\n" +
                        "| expires-val v  | Message expiration duration. E.g.: 2 (may be `nil`)|\n" +
                        "| expires-unit u | Message expiration time unit. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}|")
            .examples(
                        "(->> (ipc/venice-message \"100\" :test {:a 100, :b 200}) \n" +
                        "     (ipc/message->map)                                  \n" +
                        "     (println))                                          ",
                        "(->> (ipc/venice-message \"100\" \"test\" {:a 100, :b 200}) \n" +
                        "     (ipc/message->map)                                     \n" +
                        "     (println))                                             ",
                        "(->> (ipc/venice-message \"100\" \"test\"                \n" +
                        "                         {:a 100, :b 200}                \n" +
                        "                         false                           \n" +
                        "                         2 :hours)                       \n" +
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
                ArityExceptions.assertArity(this, args, 3, 4, 5, 6);

                final VncVal requestId;
                final VncString topic;
                final VncVal data;
                final VncVal durableVal;
                final VncVal expiresAt;

                if (args.size() == 3) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    data = args.nth(2);
                    durableVal = null;
                    expiresAt = null;
                }
                else if (args.size() == 4) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    data = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = null;
                }
                else if (args.size() == 5) {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    data = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = args.nth(4);
                }
                else {
                    requestId = args.nth(0);
                    topic = Coerce.toVncString(args.nth(1));
                    data = args.nth(2);
                    durableVal = args.nth(3);
                    expiresAt = args.nth(4) == Nil
                                 ? null
                                 : TimeFunctions.to_millis.applyOf(
                                    TimeFunctions.plus.applyOf(
                                        new VncJavaObject(LocalDateTime.now()),
                                        args.nth(5),    // unit
                                        args.nth(4)));  // n
                }

                final boolean durable = durableVal != null
                                            && durableVal != Nil
                                            && Coerce.toVncBoolean(durableVal).getValue();

                final IMessage msg = MessageFactory.venice(
                                        requestId == null || requestId == Nil
                                            ? null
                                            : Coerce.toVncString(requestId).getValue(),
                                        expiresAt == null || expiresAt == Nil
                                            ? Messages.EXPIRES_NEVER
                                            : Coerce.toVncLong(expiresAt).getValue(),
                                        durable,
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
                        "   Message                           Originator\n" +
                        " ┌───────────────────────────────┐   \n" +
                        " │ ID                            │   send, publish/subscribe method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Message Type                  │   send, publish/subscribe method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Oneway                        │   client or framework method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Durable                       │   client or framework method\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Response Status               │   server response processor\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Timestamp                     │   message creator\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ ExpiresAt                     │   client (may be null)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Request ID                    │   client (may be used for idempotency checks the receiver)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Subject                       │   client\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Queue Name                    │   client  (offer/poll, else null)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ ReplyTo Queue Name            │   client  (offer/poll, may be null)\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload Mimetype              │   client\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload Charset               │   client if payload data is a string else null\n" +
                        " ├───────────────────────────────┤   \n" +
                        " │ Payload data                  │   client\n" +
                        " └───────────────────────────────┘   \n" +
                        "```\n\n" +
                        "**Supported field names:** \n\n" +
                        "  * `:id`                  - the message's technical ID\n" +
                        "  * `:type`                - the message type (request, response, ..) \n" +
                        "  * `:oneway?`             - `true` if one-way message else `false`\n" +
                        "  * `:durable?`            - `true` if durable message else `false`\n" +
                        "  * `:response-status`     - the response status (ok, bad request, ...) \n" +
                        "  * `:timestamp`           - the message's creation timestamp in milliseconds since epoch\n" +
                        "  * `:expires-at`          - the message's expiry timestamp in milliseconds since epoch (may be nil)\n" +
                        "  * `:request-id`          - the request ID (may be nil)\n" +
                        "  * `:subject`             - the subject\n" +
                        "  * `:queue-name`          - the queue name\n" +
                        "  * `:reply-to-queue-name` - the reply to queue name\n" +
                        "  * `:payload-mimetype`    - the payload data mimetype\n" +
                        "  * `:payload-charset`     - the payload data charset (if payload is a text form)\n" +
                        "  * `:payload-text`        - the payload converted to text data if payload is textual data else error\n" +
                        "  * `:payload-binary`      - the payload binary data (the raw message binary data)\n" +
                        "  * `:payload-venice`      - the payload converted venice data if mimetype is 'application/json' else error\n\n" +
                        "**Message type:** \n\n" +
                        "  * `:REQUEST`            - a request message\n" +
                        "  * `:PUBLISH`            - a publish message\n" +
                        "  * `:SUBSCRIBE`          - a subscribe message\n" +
                        "  * `:UNSUBSCRIBE`        - an unsubscribe message\n" +
                        "  * `:OFFER`              - an offer message for a queue\n" +
                        "  * `:POLL`               - a poll message from a queue\n" +
                        "  * `:CREATE_QUEUE`       - a queue create request message\n" +
                        "  * `:CREATE_TEMP_QUEUE`  - a temporary queue create request message\n" +
                        "  * `:REMOVE_QUEUE`       - a queue remove request message\n" +
                        "  * `:STATUS_QUEUE`       - a queue status request message\n" +
                        "  * `:RESPONSE`           - a response to a request message\n" +
                        "  * `:NULL`               - a message with yet undefined type\n\n" +
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
                        "(let [m (ipc/text-message \"100\"                          \n" +
                        "                          \"test\"                         \n" +
                        "                          \"text/plain\"                   \n" +
                        "                          :UTF-8                           \n" +
                        "                          \"Hello!\")]                     \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :durable?))                \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :expires-at))              \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
                        "  (println (ipc/message-field m :request-id))              \n" +
                        "  (println (ipc/message-field m :subject))                 \n" +
                        "  (println (ipc/message-field m :payload-mimetype))        \n" +
                        "  (println (ipc/message-field m :payload-charset))         \n" +
                        "  (println (ipc/message-field m :payload-text))            \n" +
                        "  (println (ipc/message-field m :payload-binary)))         ",

                        "(let [m (ipc/binary-message \"100\"                        \n" +
                        "                            \"test\"                       \n" +
                        "                            \"application/octet-stream\"   \n" +
                        "                            (bytebuf [0 1 2 3 4 5 6 7]))]  \n" +
                        "  (println (ipc/message-field m :id))                      \n" +
                        "  (println (ipc/message-field m :type))                    \n" +
                        "  (println (ipc/message-field m :oneway?))                 \n" +
                        "  (println (ipc/message-field m :durable?))                \n" +
                        "  (println (ipc/message-field m :timestamp))               \n" +
                        "  (println (ipc/message-field m :expires-at))              \n" +
                        "  (println (ipc/message-field m :response-status))         \n" +
                        "  (println (ipc/message-field m :request-id))              \n" +
                        "  (println (ipc/message-field m :subject))                 \n" +
                        "  (println (ipc/message-field m :payload-mimetype))        \n" +
                        "  (println (ipc/message-field m :payload-charset))         \n" +
                        "  (println (ipc/message-field m :payload-binary)))         ",

                        "(let [m (ipc/venice-message \"100\" \"test\"          \n" +
                        "                            {:a 100, :b 200})]        \n" +
                        "  (println (ipc/message-field m :id))                 \n" +
                        "  (println (ipc/message-field m :type))               \n" +
                        "  (println (ipc/message-field m :oneway?))            \n" +
                        "  (println (ipc/message-field m :durable?))           \n" +
                        "  (println (ipc/message-field m :timestamp))          \n" +
                        "  (println (ipc/message-field m :expires-at))         \n" +
                        "  (println (ipc/message-field m :response-status))    \n" +
                        "  (println (ipc/message-field m :request-id))         \n" +
                        "  (println (ipc/message-field m :subject))            \n" +
                        "  (println (ipc/message-field m :payload-mimetype))   \n" +
                        "  (println (ipc/message-field m :payload-charset))    \n" +
                        "  (println (ipc/message-field m :payload-venice)))    ")
                    .seeAlso(
                        "ipc/server",
                        "ipc/client",
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/message-expired?",
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
                    case "id":                  return new VncString(message.getId().toString());
                    case "type":                return new VncKeyword(message.getType().name());
                    case "timestamp":           return new VncLong(message.getTimestamp());
                    case "expires-at":          return message.getExpiresAt() < 0
                                                        ? Nil
                                                        : new VncLong(message.getExpiresAt());
                    case "oneway?":             return VncBoolean.of(message.isOneway());
                    case "durable?":            return VncBoolean.of(message.isDurable());
                    case "response-status":     return new VncKeyword(message.getResponseStatus().name());
                    case "subject":             return new VncString(message.getSubject());
                    case "destination-name":    return new VncString(message.getDestinationName());
                    case "reply-to-queue-name": return new VncString(message.getReplyToQueueName());
                    case "request-id":          return message.getRequestId() == null
                                                        ? Nil
                                                        : new VncString(message.getRequestId());
                    case "payload-mimetype":    return new VncString(message.getMimetype());
                    case "payload-charset":     return message.getCharset() == null
                                                        ? Nil
                                                        : new VncKeyword(message.getCharset());
                    case "payload-text":        return new VncString(message.getText());
                    case "payload-binary":      return new VncByteBuffer(message.getData());
                    case "payload-venice":      return message.getVeniceData();
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
                            "(let [m (ipc/text-message \"100\"                          \n" +
                            "                          \"test\"                         \n" +
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
                        "  * `:oneway?`\n" +
                        "  * `:durable?`\n" +
                        "  * `:request-id`\n" +
                        "  * `:topic`\n" +
                        "  * `:mimetype`\n" +
                        "  * `:charset`\n" +
                        "  * `:text` (only set if there is a messsage charset defined)\n" +
                        "  * `:data`\n")
                    .examples(
                        "(->> (ipc/text-message \"1\" \"test\"                    \n" +
                        "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                        "     (ipc/message->map))                                 ",
                        "(->> (ipc/venice-message \"1\" \"test\"                  \n" +
                        "                         {:a 100, :b 200})               \n" +
                        "     (ipc/message->map))                                 ",
                        "(->> (ipc/binary-message \"1\" \"test\"                  \n" +
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
                            new VncKeyword("oneway?"),    VncBoolean.of(m.isOneway()),
                            new VncKeyword("durable?"),   VncBoolean.of(m.isDurable()),
                            new VncKeyword("subject"),    new VncString(m.getSubject()),
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
                            new VncKeyword("oneway?"),    VncBoolean.of(m.isOneway()),
                            new VncKeyword("durable?"),   VncBoolean.of(m.isDurable()),
                            new VncKeyword("subject"),    new VncString(m.getSubject()),
                            new VncKeyword("mimetype"),   new VncString(m.getMimetype()),
                            new VncKeyword("data"),       m.getVeniceData());
                }
                else {
                    // text
                    return VncOrderedMap.of(
                            new VncKeyword("type"),       new VncKeyword(m.getType().name()),
                            new VncKeyword("status"),     new VncKeyword(m.getResponseStatus().name()),
                            new VncKeyword("timestamp"),  new VncLong(m.getTimestamp()),
                            new VncKeyword("expires-at"), m.getExpiresAt() < 0 ? Nil : new VncLong(m.getExpiresAt()),
                            new VncKeyword("request-id"), m.getRequestId() == null ? Nil : new VncString(m.getRequestId()),
                            new VncKeyword("oneway?"),    VncBoolean.of(m.isOneway()),
                            new VncKeyword("durable?"),   VncBoolean.of(m.isDurable()),
                            new VncKeyword("subject"),    new VncString(m.getSubject()),
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
                            "(->> (ipc/text-message \"1\" \"test\"                    \n" +
                            "                       \"text/plain\" :UTF-8 \"hello\")  \n" +
                            "     (ipc/message->json true))                           ",
                            "(->> (ipc/venice-message \"1\" \"test\"                  \n" +
                            "                         {:a 100, :b 200})               \n" +
                            "     (ipc/message->json true))                           ",
                            "(->> (ipc/binary-message \"1\" \"test\"                  \n" +
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

    public static VncFunction ipc_message_size =
        new VncFunction(
                "ipc/message-size",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/message-size message)")
                    .doc(
                        "Calculates the effective message size (not compressed, not encrypted).\n\n" +
                        "Returns a map with the keys:¶" +
                        " • `header` - header size in bytes¶" +
                        " • `payload-meta` - payload meta data size in bytes¶" +
                        " • `payload-data` - payload data size in bytes¶" +
                        " • `total` - total size in bytes")
                    .examples(
                        "(->> (ipc/plain-text-message \"1\" \"test\" \"hello\") \n" +
                        "     (ipc/message-size))                               ")
                    .seeAlso(
                        "ipc/text-message",
                        "ipc/plain-text-message",
                        "ipc/venice-message",
                        "ipc/binary-message",
                        "ipc/message->map")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final IMessage request = Coerce.toVncJavaObject(args.first(), IMessage.class);

                final Map<String, Integer> info = Client.msgSize(request);

                return VncOrderedMap.of(
                        new VncKeyword("header"),       new VncLong(info.get("header")),
                        new VncKeyword("payload-meta"), new VncLong(info.get("payload-meta")),
                        new VncKeyword("payload-data"), new VncLong(info.get("payload-data")),
                        new VncKeyword("total"),        new VncLong(info.get("total")));
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
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
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
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
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
                        "    (->> (ipc/plain-text-message \"1\" \"test\" \"hello\")        \n" +
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
                        "(ipc/create-queue node name capacity)",
                        "(ipc/create-queue node name capacity type)",
                        "(ipc/create-queue node name capacity type durable)")
                    .doc(
                        "Creates a named queue on the server. Messages can be exchanged asynchronously " +
                        "between two clients using a queue. Each message is delivered to exactly " +
                        "one client. 1 to N clients can *offer* / *poll* messages *from* / *to* the " +
                        "queue. \n\n" +
                        "Queues live as long as server lives if they are not durable.\n\n" +
                        "A queue can be bounded or circular. Bounded queues block when offering new " +
                        "messages and the queue is full.¶" +
                        "Circular queues never block but just keep the last 'capacity' messages. The " +
                        "oldest messages get discarded if the buffer is full and new messages are " +
                        "offered to the queue.\n\n" +
                        "A queue name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'. " +
                        "Up to 80 characters are allowed.\n\n" +
                        "Use `ipc/offer` to offer a new message to a queue.¶" +
                        "Use `ipc/poll` to poll a message from a queue.\n\n" +
                        "Returns always `nil` or throws an exception if the named queue already exists.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node s     | A server or a client|\n" +
                        "| name s     | A queue name (string or keyword)|\n" +
                        "| capacity n | The queue's capacity (max number of messages)|\n" +
                        "| type t     | Optional queue type `:bounded` or `:circular`. Defaults to `:bounded`.|\n" +
                        "| durable b  | If `true` create a durable queue (if the server supports it), else create a nondurable queue. Defaults to `false`.|")
                    .examples(
                        "(try-with [server  (ipc/server 33333)                   \n" +
                        "           client1 (ipc/client 33333)                   \n" +
                        "           client2 (ipc/client 33333)]                  \n" +
                        "  (let [order  (ipc/venice-message                      \n" +
                        "                   \"1\"                                \n" +
                        "                   :order                               \n" +
                        "                   {:item \"espresso\", :count 2})]     \n" +
                        "    (ipc/create-queue server :orders 100)               \n" +
                        "    (->> (ipc/message->json true order)                 \n" +
                        "         (println \"ORDER:\"))                          \n" +
                        "    (->> (ipc/offer client1 :orders 300 order)          \n" +
                        "         (ipc/message->json true)                       \n" +
                        "         (println \"OFFERED:\"))                        \n" +
                        "    (->> (ipc/poll client2 :orders 300)                 \n" +
                        "         (ipc/message->json true)                       \n" +
                        "         (println \"POLLED:\"))))                       ",
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\" :admin)  \n" +
                        "  (try-with [server (ipc/server 33333                \n" +
                        "                                :encrypt true        \n" +
                        "                                :authenticator auth) \n" +
                        "             client (ipc/client 33333                \n" +
                        "                                :user-name \"tom\"   \n" +
                        "                                :password \"123\")]  \n" +
                        "    (ipc/create-queue client :orders 100)            \n" +
                        "    ;; ...                                           \n" +
                        "    ))                                               ")

                    .seeAlso(
                        "ipc/create-temporary-queue",
                        "ipc/remove-queue",
                        "ipc/exists-queue?",
                        "ipc/queue-status",
                        "ipc/offer",
                        "ipc/poll",
                        "ipc/offer-async",
                        "ipc/poll-async",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4, 5);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    final int capacity = (int)Coerce.toVncLong(args.third()).toJavaLong();

                    final VncVal typeVal = args.size() < 4 || args.nth(3) == Nil ? Nil : Coerce.toVncKeyword(args.nth(3));
                    final VncVal durableVal = args.size() < 5 || args.nth(4) == Nil ? Nil : Coerce.toVncBoolean(args.nth(4));

                    final boolean durable = durableVal == Nil ? false : ((VncBoolean)durableVal).getValue();

                    if (typeVal == Nil) {
                        server.createQueue(name, capacity, true, durable);
                    }
                    else {
                        final String sType = ((VncKeyword)typeVal).getSimpleName();
                        switch(sType) {
                            case "bounded":
                                server.createQueue(name, capacity, true, durable);
                                break;
                            case "circular":
                                if (durable) {
                                    throw new VncException ("Circular queues can not be made durable.");
                                }
                                server.createQueue(name, capacity, false, durable);
                                break;
                            default:
                                throw new VncException (
                                    "Invalid queue type :" + sType + "! Use one of {:bounded, :circular}.");
                        }
                    }
                    return Nil;
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    final int capacity = (int)Coerce.toVncLong(args.third()).toJavaLong();

                    final VncVal typeVal = args.size() < 4 || args.nth(3) == Nil ? Nil : Coerce.toVncKeyword(args.nth(3));
                    final VncVal durableVal = args.size() < 5 || args.nth(4) == Nil ? Nil : Coerce.toVncBoolean(args.nth(4));

                    final boolean durable = durableVal == Nil ? false : ((VncBoolean)durableVal).getValue();

                    if (typeVal == Nil) {
                        client.createQueue(name, capacity, true, durable);
                    }
                    else {
                        final String sType = ((VncKeyword)typeVal).getSimpleName();
                        switch(sType) {
                            case "bounded":
                                client.createQueue(name, capacity, true, durable);
                                break;
                            case "circular":
                                if (durable) {
                                    throw new VncException ("Circular queues can not be made durable.");
                                }
                                client.createQueue(name, capacity, false, durable);
                                break;
                            default:
                                throw new VncException (
                                    "Invalid queue type :" + sType + "! Use one of {:bounded, :circular}.");
                        }
                    }
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/create-queue: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_create_temporary_queue =
        new VncFunction(
                "ipc/create-temporary-queue",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/create-temporary-queue client capacity)")
                    .doc(
                        "Creates a named temporary queue on the server. Temporary queues live as long as " +
                        "the client, that created it, lives.\n\n" +
                        "Venice can create a temporary queue dynamically for use as a dedicated reply " +
                        "queue for a client. You can use this to ensure that a reply message can be sent " +
                        "to the appropriate queue and reaches the desired client.\n\n" +
                        "Returns the name of the created temporary queue.\n\n" +
                        "Use `ipc/offer` to offer a new message to a temporary queue.¶" +
                        "Use `ipc/poll` to poll a message from a temporary queue.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c   | A client. Can only be called on behalf of a client! |\n" +
                        "| capacity n | The queue's capacity (max number of messages)|")
                    .examples(
                        "(do                                                                                   \n" +
                        "  (try-with [server  (ipc/server 33333)                                               \n" +
                        "             client1 (ipc/client 33333)                                               \n" +
                        "             client2 (ipc/client 33333)]                                              \n" +
                        "                                                                                      \n" +
                        "    (ipc/create-queue server :orders 100)                                             \n" +
                        "                                                                                      \n" +
                        "    (let [confirm-queue  (ipc/create-temporary-queue client1 100)]                    \n" +
                        "      ;; client1 sends an order to the order queue                                    \n" +
                        "      (ipc/offer client1 :orders confirm-queue 300                                    \n" +
                        "                 (ipc/venice-message                                                  \n" +
                        "                            \"1\"                                                     \n" +
                        "                            :order                                                    \n" +
                        "                            {:item \"espresso\", :count 2}))                          \n" +
                        "                                                                                      \n" +
                        "      ;; client2 receives order from order queue and replies to the reply-to queue    \n" +
                        "      (let [order          (ipc/poll client2 :orders 300)                             \n" +
                        "            request-id     (ipc/message-field order :request-id)                      \n" +
                        "            reply-to-queue (ipc/message-field order :reply-to-queue-name)             \n" +
                        "            order-data     (ipc/message-field order :payload-venice)                  \n" +
                        "            reply-message  (ipc/venice-message request-id \"confirmed\" order-data)]  \n" +
                        "        (ipc/offer client2 reply-to-queue 1_000 reply-message))                       \n" +
                        "                                                                                      \n" +
                        "      ;; client1 receives confirmation                                                \n" +
                        "      (println (ipc/poll client2 confirm-queue 300)))))                               ")
                    .seeAlso(
                        "ipc/create-queue",
                        "ipc/remove-queue",
                        "ipc/exists-queue?",
                        "ipc/queue-status",
                        "ipc/offer",
                        "ipc/poll",
                        "ipc/offer-async",
                        "ipc/poll-async",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                final int capacity = (int)Coerce.toVncLong(args.second()).toJavaLong();

                return new VncString(client.createTemporaryQueue(capacity));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_remove_queue =
        new VncFunction(
                "ipc/remove-queue",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/remove-queue node name)")
                    .doc(
                        "Removes a named queue.\n\n" +
                        "Returns always `nil` or throws an exception.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node s | A server or a client |\n" +
                        "| name n | A queue name (string or keyword)|")
                    .examples(
                        "(try-with [server (ipc/server 33333 echo-handler)]   \n" +
                        "  (ipc/create-queue server :orders 100)              \n" +
                        "  ;; ...                                             \n" +
                        "  (ipc/remove-queue server :orders))                 ",
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\" :admin)  \n" +
                        "  (try-with [server (ipc/server 33333                \n" +
                        "                                :encrypt true        \n" +
                        "                                :authenticator auth) \n" +
                        "             client (ipc/client 33333                \n" +
                        "                                :user-name \"tom\"   \n" +
                        "                                :password \"123\")]  \n" +
                        "    (ipc/create-queue client :orders 100)            \n" +
                        "    ;; ...                                           \n" +
                        "    (ipc/remove-queue client :orders)))             ")
                    .seeAlso(
                        "ipc/create-queue",
                        "ipc/create-temporary-queue",
                        "ipc/exists-queue?",
                        "ipc/queue-status",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    server.removeQueue(name);
                    return Nil;
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    client.removeQueue(name);
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/remove-queue: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_exists_queueQ =
        new VncFunction(
                "ipc/exists-queue?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/exists-queue? node name)")
                    .doc(
                        "Returns `true` if the named queue exists else `false`.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node n | A server or client |\n" +
                        "| name n | A queue name (string or keyword)|")
                    .examples(
                        "(try-with [server (ipc/server 33333)]      \n" +
                        "  (ipc/create-queue server :orders 100)    \n" +
                        "  ;; ...                                   \n" +
                        "  (ipc/exists-queue? server :orders))      ",
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\" :admin)  \n" +
                        "  (try-with [server (ipc/server 33333                \n" +
                        "                                :encrypt true        \n" +
                        "                                :authenticator auth) \n" +
                        "             client (ipc/client 33333                \n" +
                        "                                :user-name \"tom\"   \n" +
                        "                                :password \"123\")   \n" +
                        "    (ipc/create-queue client :orders 100)            \n" +
                        "    ;; ...                                           \n" +
                        "    (ipc/exists-queue? client :orders)))             ")
                    .seeAlso(
                        "ipc/create-queue",
                        "ipc/create-temporary-queue",
                        "ipc/remove-queue",
                        "ipc/queue-status",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(server.existsQueue(name));
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(client.existsQueue(name));
                }
                else {
                    throw new VncException (
                            "ipc/exists-queue?: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_queue_status =
        new VncFunction(
                "ipc/queue-status",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/queue-status client name)")
                    .doc(
                        "Returns a map with the queue status key - values.\n\n" +
                        "*Arguments:* \n\n" +
                        "| client c | A client |\n" +
                        "| name n   | A queue name (string or keyword)|\n\n" +
                        "*Queue status map keys:* \n\n" +
                        "| :name      | The queue name (string) |\n" +
                        "| :exists    | Queue exists: `true` or `false` |\n" +
                        "| :type      | Queue type \"bounded\" or \"circular\" |\n" +
                        "| :temporary | Queue is temporary: `true` or `false` |\n" +
                        "| :capycity  | The capacity (long) |\n" +
                        "| :size      | The current size (long) |")
                    .examples(
                        "(try-with [server (ipc/server 33333)          \n" +
                        "           client (ipc/client 33333)]         \n" +
                        "   (ipc/create-queue server :orders 100)      \n" +
                        "   ;; ...                                     \n" +
                        "   (ipc/queue-status server :orders))         ",
                        "(let [auth (ipc/authenticator)]                      \n" +
                        "  (ipc/add-credentials auth \"tom\" \"123\" :admin)  \n" +
                        "  (try-with [server (ipc/server 33333                \n" +
                        "                                :encrypt true        \n" +
                        "                                :authenticator auth) \n" +
                        "             client (ipc/client 33333                \n" +
                        "                                :user-name \"tom\"   \n" +
                        "                                :password \"123\")]  \n" +
                        "    (ipc/create-queue client :orders 100)            \n" +
                        "    ;; ...                                           \n" +
                        "    (ipc/exists-status? client :orders)))             ")
                    .seeAlso(
                        "ipc/create-queue",
                        "ipc/create-temporary-queue",
                        "ipc/remove-queue",
                        "ipc/exists-queue?",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    final Map<String,Object> status = server.getQueueStatus(name);

                    return VncOrderedMap.of(
                            new VncKeyword("name")      , new VncString((String)status.get("name")),
                            new VncKeyword("exists")    , VncBoolean.of((Boolean)status.get("exists")),
                            new VncKeyword("type")      , new VncKeyword((String)status.get("type")),
                            new VncKeyword("temporary") , VncBoolean.of((Boolean)status.get("temporary")),
                            new VncKeyword("durable")   , VncBoolean.of((Boolean)status.get("durable")),
                            new VncKeyword("capacity")  , new VncLong((Long)status.get("capacity")),
                            new VncKeyword("size")      , new VncLong((Long)status.get("size")));
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    final Map<String,Object> status = client.getQueueStatus(name);

                    return VncOrderedMap.of(
                            new VncKeyword("name")      , new VncString((String)status.get("name")),
                            new VncKeyword("exists")    , VncBoolean.of((Boolean)status.get("exists")),
                            new VncKeyword("type")      , new VncKeyword((String)status.get("type")),
                            new VncKeyword("temporary") , VncBoolean.of((Boolean)status.get("temporary")),
                            new VncKeyword("durable")   , VncBoolean.of((Boolean)status.get("durable")),
                            new VncKeyword("capacity")  , new VncLong((Long)status.get("capacity")),
                            new VncKeyword("size")      , new VncLong((Long)status.get("size")));
                }
                else {
                    throw new VncException (
                            "ipc/queue-status?: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    // ------------------------------------------------------------------------
    // Topics
    // ------------------------------------------------------------------------

    public static VncFunction ipc_create_topic =
        new VncFunction(
                "ipc/create-topic",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/create-topic node name)")
                    .doc(
                        "Creates a named topic on the server. \n\n" +
                        "A topic name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'. " +
                        "Up to 80 characters are allowed.\n\n" +
                        "Returns always `nil` or throws an exception if the named topic already exists.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node s     | A server or a client|\n" +
                        "| name s     | A topic name (string or keyword)|")
                    .examples(
                        "(try-with [server  (ipc/server 33333)            \n" +
                        "           client1 (ipc/client 33333)]           \n" +
                        "  (ipc/create-topic server :orders-closed)       \n" +
                        "    ;;                                           \n" +
                        "  (ipc/exists-topic? server :orders-closed))     ")
                    .seeAlso(
                        "ipc/remove-topic",
                        "ipc/exists-topic?",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    server.createTopic(name);
                    return Nil;
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    client.createTopic(name);
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/create-topic: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_remove_topic =
        new VncFunction(
                "ipc/remove-topic",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/remove-topic node name)")
                    .doc(
                        "Removes a named topic.\n\n" +
                        "Returns always `nil`.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node s | A server or a client |\n" +
                        "| name n | A topic name (string or keyword)|")
                    .examples(
                        "(try-with [server (ipc/server 33333 echo-handler)]   \n" +
                        "  (ipc/create-topic server :orders-closed 100)       \n" +
                        "  ;; ...                                             \n" +
                        "  (ipc/remove-topic server :orders-closed))          ")
                    .seeAlso(
                        "ipc/create-topic",
                        "ipc/exists-topic?",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    server.removeTopic(name);
                    return Nil;
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    client.removeTopic(name);
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/remove-topic: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_exists_topicQ =
        new VncFunction(
                "ipc/exists-topic?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/exists-topic? node name)")
                    .doc(
                        "Returns `true` if the named topic exists else `false`.\n\n" +
                        "*Arguments:* \n\n" +
                        "| node n | A server or client |\n" +
                        "| name n | A topic name (string or keyword)|")
                    .examples(
                        "(try-with [server (ipc/server 33333)]            \n" +
                        "  (ipc/create-topic server :orders-closed 100)   \n" +
                        "  ;; ...                                         \n" +
                        "  (ipc/exists-topic? server :orders-closed))     ")
                    .seeAlso(
                        "ipc/create-topic",
                        "ipc/remove-topic",
                        "ipc/server",
                        "ipc/client")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(server.existsQueue(name));
                }
                else if (Types.isVncJavaObject(args.first(), Client.class)) {
                    final Client client = Coerce.toVncJavaObject(args.first(), Client.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(client.existsTopic(name));
                }
                else {
                    throw new VncException (
                            "ipc/exists-topic?: the first arg must be either a server or client.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    // ------------------------------------------------------------------------
    // Functions
    // ------------------------------------------------------------------------

    public static VncFunction ipc_create_function =
        new VncFunction(
                "ipc/create-function",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/create-function server name func)")
                    .doc(
                        "Creates a named function on the server. \n\n" +
                        "A function name must only contain the characters 'a-z', 'A-Z', '0-9', '_', '-', or '/'. " +
                        "Up to 80 characters are allowed.\n\n" +
                        "Returns always `nil`.")
                    .examples(
                        "(try-with [server  (ipc/server 33333)            \n" +
                        "           client1 (ipc/client 33333)]           \n" +
                        "  (ipc/create-topic server :orders-closed)       \n" +
                        "    ;;                                           \n" +
                        "  (ipc/exists-topic? server :orders-closed))     ")
                    .seeAlso(
                        "ipc/remove-function",
                        "ipc/exists-function?",
                        "ipc/server")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    final VncFunction handler = Coerce.toVncFunction(args.third());

                    final Function<IMessage,IMessage> fnWrapper = wrapFunction(this, args, handler);
                    server.createFunction(name, fnWrapper);
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/create-function: the first arg must be a server.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_remove_function =
        new VncFunction(
                "ipc/remove-function",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/remove-function server name)")
                    .doc(
                        "Removes a named topic.\n\n" +
                        "Returns always `nil` or throws an exception.")
                    .examples(
                        "(try-with [server (ipc/server 33333 echo-handler)]   \n" +
                        "  (ipc/create-topic server :orders-closed 100)       \n" +
                        "  ;; ...                                             \n" +
                        "  (ipc/remove-topic server :orders-closed))          ")
                    .seeAlso(
                        "ipc/create-function",
                        "ipc/exists-function?",
                        "ipc/server")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    server.removeFunction(name);
                    return Nil;
                }
                else {
                    throw new VncException (
                            "ipc/remove-function: the first arg must be a server.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction ipc_exists_functionQ =
        new VncFunction(
                "ipc/exists-function?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/exists-function? node name)")
                    .doc(
                        "Returns `true` if the named topic exists else `false`.")
                    .examples(
                        "(try-with [server (ipc/server 33333)]            \n" +
                        "  (ipc/create-topic server :orders-closed 100)   \n" +
                        "  ;; ...                                         \n" +
                        "  (ipc/exists-topic? server :orders-closed))     ")
                    .seeAlso(
                        "ipc/create-function",
                        "ipc/remove-function",
                        "ipc/server")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                if (Types.isVncJavaObject(args.first(), Server.class)) {
                    final Server server = Coerce.toVncJavaObject(args.first(), Server.class);
                    final String name = Coerce.toVncString(args.second()).getValue();
                    return VncBoolean.of(server.existsFunction(name));
                }
                else {
                    throw new VncException (
                            "ipc/exists-function?: the first arg must be a server.");
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };




    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------


    private static void setConfigConnection(
            final VncVal portOrConnURI,
            final ServerConfig.Builder builder
    ) {
        if (Types.isVncLong(portOrConnURI)) {
            final int port = Coerce.toVncLong(portOrConnURI).getIntValue();
            builder.conn(port);
        }
        else if (Types.isVncString(portOrConnURI)) {
            final String connURI = Coerce.toVncString(portOrConnURI).getValue();
            builder.connURI(connURI);
        }
        else {
            throw new VncException(
                    "Function 'ipc/server' expects either a port or a connection URI!");
        }
    }

    private static void setConfigConnection(
                final VncVal portOrConnURI,
                final ClientConfig.Builder builder
    ) {
        if (Types.isVncLong(portOrConnURI)) {
            final int port = Coerce.toVncLong(portOrConnURI).getIntValue();
            builder.conn(port);
        }
        else if (Types.isVncString(portOrConnURI)) {
            final String connURI = Coerce.toVncString(portOrConnURI).getValue();
            builder.connURI(connURI);
        }
        else {
            throw new VncException(
                    "Function 'ipc/client' expects either a port a host/port or a connection URI!");
        }
    }

    private static long convertUnitValueToLong(final VncVal val) {
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
                throw new VncException("Invalid unit value! Use 20000, 500KB, 10MB, ...");
            }
        }
        else {
           throw new VncException("Invalid unit value! Use 20000, 500KB, 10MB, ...");
        }
    }

    private static Function<IMessage,IMessage> wrapFunction(
            final VncFunction callingFunction,
            final VncList callingFunctionArgs,
            final VncFunction handler
    ) {
        if (handler == null) {
            return null; // no handler passed
        }
        else {
            final CallFrame[] cf = new CallFrame[] {
                                        new CallFrame(callingFunction, callingFunctionArgs),
                                        new CallFrame(handler) };

            // Create a wrapper that inherits the Venice thread context!
            final ThreadBridge threadBridge = ThreadBridge.create("tcp-server-handler", cf);

            return threadBridge.bridgeFunction((IMessage m) -> {
                          final VncVal request = new VncJavaObject(m);
                          final VncVal response = handler.applyOf(request);
                          return response == null || response == Nil
                                  ? null
                                  : Coerce.toVncJavaObject(response, IMessage.class);
                     });
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

                    .add(ipc_benchmark)

                    .add(ipc_send)
                    .add(ipc_send_async)
                    .add(ipc_send_oneway)

                    .add(ipc_publish)
                    .add(ipc_publish_async)
                    .add(ipc_subscribe)
                    .add(ipc_unsubscribe)

                    .add(ipc_offer)
                    .add(ipc_offer_async)
                    .add(ipc_poll)
                    .add(ipc_poll_async)

                    .add(ipc_authenticator)
                    .add(ipc_load_authenticator)
                    .add(ipc_store_authenticator)
                    .add(ipc_add_credentials)
                    .add(ipc_remove_credentials)
                    .add(ipc_clear_credentials)

                    .add(ipc_text_message)
                    .add(ipc_plain_text_message)
                    .add(ipc_binary_message)
                    .add(ipc_venice_message)
                    .add(ipc_message_expiredQ)
                    .add(ipc_message_field)
                    .add(ipc_message_to_map)
                    .add(ipc_message_to_json)
                    .add(ipc_message_size)
                    .add(ipc_onewayQ)
                    .add(ipc_response_okQ)
                    .add(ipc_response_errQ)

                    .add(ipc_create_queue)
                    .add(ipc_create_temporary_queue)
                    .add(ipc_remove_queue)
                    .add(ipc_exists_queueQ)
                    .add(ipc_queue_status)

                    .add(ipc_create_topic)
                    .add(ipc_remove_topic)
                    .add(ipc_exists_topicQ)

                    .add(ipc_server_status)
                    .add(ipc_server_thread_pool_statistics)

                    .toMap();
}
