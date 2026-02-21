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
package com.github.jlangch.venice.impl.repl.remote;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.util.CapturingPrintStream;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.MessageFactory;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;


public class RemoteReplServer implements AutoCloseable  {

    public RemoteReplServer(
            final IVeniceInterpreter interpreter,
            final Env env,
            final int port,
            final String password
    ) {
        this.interpreter = interpreter;
        this.env = env;

        this.ipcServer = createIpcServer(port, RemoteRepl.PRINCIPAL, password);
    }


    public boolean isRunning() {
        return ipcServer != null && ipcServer.isRunning() && !isStop();
    }

    @Override
    public void close() throws IOException {
        if (stop.compareAndSet(false, true)) {
            if (ipcServer != null) {
                ipcServer.close();
            }
        }
    }

    private boolean isStop() {
        return stop.get();
    }


    private Server createIpcServer(
            final int port,
            final String principal,
            final String password
    ) {
        if (port <= 0 || port > 65536) {
            throw new VncException(
                    "Failed to start Venice REPL server. "
                    + "The port (" + port + ") must be in the range [0..65536]! "
                    + "Please pass the option '-repl-port 33334'.");
        }
        if (StringUtil.isEmpty(principal)) {
            throw new VncException(
                    "Failed to start Venice REPL server. The principal must not be empty!");
        }
        if (StringUtil.isEmpty(password)) {
            throw new VncException(
                    "Failed to start Venice REPL server. "
                    + "No password supplied! Please pass the option "
                    + "'-repl-pwd 123' or '-repl-pwd env:REPL_PASSWORD'");
        }

        try {
            final Authenticator authenticator = new Authenticator(true);
            authenticator.addCredentials(principal, password);

            final ServerConfig config = ServerConfig
                                            .builder()
                                            .conn(port)
                                            .authenticator(authenticator)
                                            .encrypt(true)
                                            .build();

            final Server server = Server.of(config);

            final Function<IMessage,IMessage> fnWrapper = wrapFunction(this::handler);

            server.createFunction(RemoteRepl.FUNCTION, fnWrapper);

            server.start();

            stop.set(false);

            return server;
        }
        catch(Exception ex) {
            throw new VncException("Failed to start Venice REPL server", ex);
        }
    }

    private IMessage handler(final IMessage request) {
        final long start = System.currentTimeMillis();

        try(CapturingPrintStream out = new CapturingPrintStream();
            CapturingPrintStream err = new CapturingPrintStream()
        ) {
            final VncVal r = request.getVeniceData();
            final VncVal formVal = ((VncMap)r).get(new VncKeyword("form"));
            final String form = Coerce.toVncString(formVal).getValue();

            try {
                env.setStdoutPrintStream(out)
                   .setStderrPrintStream(err)
                   .setStdinReader(new InputStreamReader(new NullInputStream()));

                final VncVal result = interpreter.RE(form, "repl", env);

                final VncMap data = createDataMap(formVal, result, null, out, err, elapsed(start));
                return responseMessage(request, data);
            }
            catch(Exception ex) {
                final VncMap data = createDataMap(formVal, Nil, ex, out, err, elapsed(start));
                return responseMessage(request, data);
            }
        }
        catch(Exception ex) {
            return responseMessage(request, createDataMap(Nil, Nil, ex, null, null, 0L));
        }
    }

    private VncHashMap createDataMap(
            final VncVal form,
            final VncVal ret,
            final Exception ex,
            final CapturingPrintStream outPS,
            final CapturingPrintStream errPS,
            final long elapsedMillis
    ) {
        return VncHashMap.of(
                new VncKeyword("form"),   form,
                new VncKeyword("return"), ret,
                new VncKeyword("ex"),     ex == null ? Nil : new VncString(formatEx(ex)),
                new VncKeyword("out"),    new VncString(capturedText(outPS)),
                new VncKeyword("err"),    new VncString(capturedText(errPS)),
                new VncKeyword("ms"),     new VncLong(elapsedMillis));
    }

    private String formatEx(final Exception ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        if (ex instanceof ValueException) {
            ((ValueException)ex).printVeniceStackTrace(pw);
            pw.println();
            pw.print("Thrown value: ");
            pw.println(Printer.pr_str((VncVal)((ValueException)ex).getValue(), false));
        }
        else if (ex instanceof VncException) {
            ((VncException)ex).printVeniceStackTrace(pw);
        }
        else {
            ex.printStackTrace(pw);
        }

        pw.flush();
        return sw.toString();
    }

    private static Function<IMessage,IMessage> wrapFunction(
            final Function<IMessage,IMessage> handler
    ) {
        final CallFrame[] cf = new CallFrame[] { };

        // Create a wrapper that inherits the Venice thread context!
        final ThreadBridge threadBridge = ThreadBridge.create("tcp-repl-server-handler", cf);

        return threadBridge.bridgeFunction((IMessage m) -> handler.apply(m));
    }

    private IMessage responseMessage(final IMessage request, final VncMap responseData) {
        return MessageFactory.venice(
                request.getRequestId(),
                request.getSubject(),
                responseData);
    }

    private String capturedText(final CapturingPrintStream ps) {
        if (ps != null) {
            ps.flush();
           return ps.getOutput();
        }
        else {
            return "";
        }
    }

    private long elapsed(final long start) {
        return System.currentTimeMillis() - start;
    }


    private final IVeniceInterpreter interpreter;
    private final Env env;
    private final Server ipcServer;
    private final AtomicBoolean stop = new AtomicBoolean(false);
}
