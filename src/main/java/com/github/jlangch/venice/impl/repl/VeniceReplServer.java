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
package com.github.jlangch.venice.impl.repl;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.CapturingPrintStream;
import com.github.jlangch.venice.util.NullInputStream;
import com.github.jlangch.venice.util.ipc.Authenticator;
import com.github.jlangch.venice.util.ipc.IMessage;
import com.github.jlangch.venice.util.ipc.Server;
import com.github.jlangch.venice.util.ipc.ServerConfig;


public class VeniceReplServer implements AutoCloseable  {

    public VeniceReplServer(
            final IVeniceInterpreter interpreter,
            final int port,
            final String principal,
            final String password
    ) {
        this.interpreter = interpreter;

        this.ipcServer = createIpcServer(port, principal, password);
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
                    "Failed to start Venice REPL server. The port (" + port + ") "
                    + "must be in the range [0..65536]!");
        }
        if (StringUtil.isEmpty(principal)) {
            throw new VncException(
                    "Failed to start Venice REPL server. The principal must not be empty!");
        }
        if (StringUtil.isEmpty(password)) {
            throw new VncException(
                    "Failed to start Venice REPL server. The password must not be empty!");
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
            server.createFunction("func/repl", this::handler);

            server.start();

            stop.set(false);

            return server;
        }
        catch(Exception ex) {
            throw new VncException("Failed to start Venice REPL server", ex);
        }
    }

    private IMessage handler(final IMessage request) {
        final long count = requestCounter.incrementAndGet();

        try(CapturingPrintStream out = new CapturingPrintStream();
            CapturingPrintStream err = new CapturingPrintStream()
        ) {
            final VncVal r = request.getVeniceData();

            final VncVal namespaceVal = ((VncMap)r).get(new VncString("ns"));
            final VncVal formVal = ((VncMap)r).get(new VncString("form"));

            try {
                final Env env = interpreter
                                    .createEnv(false, false, RunMode.REPL)
                                    .setStdoutPrintStream(out)
                                    .setStderrPrintStream(err)
                                    .setStdinReader(new InputStreamReader(new NullInputStream()));

                final VncVal result = interpreter.RE("xxxxxx", "repl", env);

                out.flush();
                err.flush();

                final String sOut = out.getOutput();
                final String sErr = err.getOutput();

                VncMap map = createDataMap(count, formVal, result, null, sOut, sErr);

                return request;
            }
            catch(Exception ex) {
                out.flush();
                err.flush();

                final String sOut = out.getOutput();
                final String sErr = err.getOutput();

                VncMap map = createDataMap(count, formVal, Nil, ex, sOut, sErr);

                return request;
            }
        }
        catch(Exception ex) {
            VncMap map = createDataMap(count, Nil, Nil, ex, "", "");

            // Build a response message from the exception
            return request;
        }
    }

    private VncHashMap createDataMap(
            final long count,
            final VncVal form,
            final VncVal ret,
            final Exception ex,
            final String out,
            final String err
    ) {
        return VncHashMap.of(
                new VncString("id"),     new VncLong(count),
                new VncString("form"),   form,
                new VncString("return"), ret,
                new VncString("ex"),     new VncString(formatEx(ex)),
                new VncString("out"),    new VncString(out),
                new VncString("err"),    new VncString(err));
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


    private final IVeniceInterpreter interpreter;
    private final Server ipcServer;
    private final AtomicLong requestCounter = new AtomicLong(0L);
    private final AtomicBoolean stop = new AtomicBoolean(false);
}
