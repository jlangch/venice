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
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.util.ipc.Message;
import com.github.jlangch.venice.util.ipc.Status;
import com.github.jlangch.venice.util.ipc.TcpClient;
import com.github.jlangch.venice.util.ipc.TcpServer;


public class IPCFunctions {

    public static VncFunction ipc_start_server =
        new VncFunction(
                "ipc/start-server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/start-server port handler & options)")
                    .doc(
                        "....")
                    .examples(
                        "(do                                                              \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                      \n" +
                        "   (try-with [server (ipc/start-server 33333 handler)            \n" +
                        "              client (ipc/start-client \"localhost\" 33333)]     \n" +
                        "     (let [m (ipc/text-message :REQUEST \"test\"                 \n" +
                        "                               \"text/plain\" :UTF-8 \"hello\")] \n" +
                        "       (-<> (ipc/client-send client m)                           \n" +
                        "            (. <> :getText)                                      \n" +
                        "            (println <>)))))                                     ")
                    .seeAlso(
                        "ipc/xx")
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


    public static VncFunction ipc_start_client =
            new VncFunction(
                    "ipc/start-client",
                    VncFunction
                        .meta()
                        .arglists(
                            "(ipc/start-client host port)")
                        .doc(
                            "....")
                        .examples(
                            "(io/file \"/tmp/test.txt\")")
                        .seeAlso(
                            "ipc/xx")
                        .build()
            ) {
                @Override
                public VncVal apply(final VncList args) {
                    ArityExceptions.assertMinArity(this, args, 2);

                    final String host = Coerce.toVncString(args.first()).getValue();
                    final int port = Coerce.toVncLong(args.second()).getIntValue();

                    final TcpClient client = new TcpClient(host, port);

                    client.open();

                    return new VncJavaObject(client);
                }

                private static final long serialVersionUID = -1848883965231344442L;
            };


    public static VncFunction ipc_server_runnningQ =
        new VncFunction(
                "ipc/server-running?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/close-server server)")
                    .doc(
                        "....")
                    .examples(
                        "(io/file \"/tmp/test.txt\")")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpServer server = Coerce.toVncJavaObject(args.first(), TcpServer.class);

                return VncBoolean.of(server.isRunning());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_close_server =
        new VncFunction(
                "ipc/close-server",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/close-server server)")
                    .doc(
                        "....")
                    .examples(
                        "(io/file \"/tmp/test.txt\")")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpServer server = Coerce.toVncJavaObject(args.first(), TcpServer.class);

                try {
                    server.close();
                }
                catch(Exception ex) {
                    throw new VncException("Failed to close IPC server", ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_client_runnningQ =
        new VncFunction(
                "ipc/client-running?",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/close-server server)")
                    .doc(
                        "....")
                    .examples(
                        "(io/file \"/tmp/test.txt\")")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);

                return VncBoolean.of(client.isRunning());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction ipc_client_send =
        new VncFunction(
                "ipc/client-send",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/client-send client message)",
                        "(ipc/client-send client message timeout)")
                    .doc(
                        "....")
                    .examples(
                        "(do                                                              \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                      \n" +
                        "   (try-with [server (ipc/start-server 33333 handler)            \n" +
                        "              client (ipc/start-client \"localhost\" 33333)]     \n" +
                        "     (let [m (ipc/text-message :REQUEST \"test\"                 \n" +
                        "                               \"text/plain\" :UTF-8 \"hello\")] \n" +
                        "       (-<> (ipc/client-send client m)                           \n" +
                        "            (. <> :getText)                                      \n" +
                        "            (println <>)))))                                     ",
                        "(do                                                              \n" +
                        "   (defn handler [m] (. m :asEchoResponse))                      \n" +
                        "   (try-with [server (ipc/start-server 33333 handler)            \n" +
                        "              client (ipc/start-client \"localhost\" 33333)]     \n" +
                        "     (let [m (ipc/text-message :REQUEST \"test\"                 \n" +
                        "                               \"text/plain\" :UTF-8 \"hello\")] \n" +
                        "       (-<> (ipc/client-send client m 2000)                      \n" +
                        "            (. <> :getText)                                      \n" +
                        "            (println <>)))))                                     ")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);
                final Message request = Coerce.toVncJavaObject(args.second(), Message.class);
                final long timeout = args.size() > 2 ? Coerce.toVncLong(args.third()).toJavaLong() : 0;

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

        public static VncFunction ipc_client_send_async =
            new VncFunction(
                    "ipc/client-send-async",
                    VncFunction
                        .meta()
                        .arglists(
                            "(ipc/client-send-async client message)")
                        .doc(
                            "....")
                        .examples(
                            "(do                                                              \n" +
                            "   (defn handler [m] (. m :asEchoResponse))                      \n" +
                            "   (try-with [server (ipc/start-server 33333 handler)            \n" +
                            "              client (ipc/start-client \"localhost\" 33333)]     \n" +
                            "     (let [m (ipc/text-message :REQUEST \"test\"                 \n" +
                            "                               \"text/plain\" :UTF-8 \"hello\")] \n" +
                            "       (-<> (ipc/client-send-async client m)                     \n" +
                            "            (deref <>)                                           \n" +
                            "            (. <> :getText)                                      \n" +
                            "            (println <>)))))                                     ")
                        .seeAlso(
                            "ipc/xx")
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

    public static VncFunction ipc_close_client =
        new VncFunction(
                "ipc/close-client",
                VncFunction
                    .meta()
                    .arglists(
                        "(ipc/close-client client)")
                    .doc(
                        "....")
                    .examples(
                        "(io/file \"/tmp/test.txt\")")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final TcpClient client = Coerce.toVncJavaObject(args.first(), TcpClient.class);

                try {
                    client.close();
                }
                catch(Exception ex) {
                    throw new VncException("Failed to close IPC client", ex);
                }

                return Nil;
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
	                    "(ipc/text-message :REQUEST \"test\"                 \n" +
	                    "                  \"text/plain\" :UTF-8 \"hello\")  ")
                    .examples(
                        "(io/file \"/tmp/test.txt\")")
                    .seeAlso(
                        "ipc/xx")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 5);

                final VncKeyword status = Coerce.toVncKeyword(args.nth(0));
                final VncString topic = Coerce.toVncString(args.nth(1));
                final VncString mimetype = Coerce.toVncString(args.nth(2));
                final VncKeyword charset = Coerce.toVncKeyword(args.nth(3));
                final VncString text = Coerce.toVncString(args.nth(4));

                final Message msg = Message.text(
                                        convertToStatus(status),
                                        topic.getValue(),
                                        mimetype.getValue(),
                                        charset.getSimpleName(),
                                        text.getValue());

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
                            "....")
                        .examples(
                            "(io/file \"/tmp/test.txt\")")
                        .seeAlso(
                            "ipc/xx")
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
                    .add(ipc_start_server)
                    .add(ipc_close_server)
                    .add(ipc_server_runnningQ)

                    .add(ipc_start_client)
                    .add(ipc_client_runnningQ)
                    .add(ipc_close_client)

                    .add(ipc_client_send)
                    .add(ipc_client_send_async)

                    .add(ipc_text_message)
                    .add(ipc_binary_message)

                    .toMap();
}
