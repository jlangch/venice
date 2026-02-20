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

import java.util.function.Consumer;

import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.repl.IScriptExecuter;
import com.github.jlangch.venice.impl.repl.ReplResultHistory;
import com.github.jlangch.venice.impl.repl.TerminalPrinter;
import com.github.jlangch.venice.impl.util.StringUtil;


public class RemoteScriptExecuter implements IScriptExecuter{

    public RemoteScriptExecuter(
            final String host,
            final int port,
            final String password
    ) {
        client = new RemoteReplClient(host, port, password);
    }

    @Override
    public void runSync(
            final String script,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix,
            final ReplResultHistory resultHistory,
            final Consumer<Exception> errorHandler
    ) {
        try {
            final FormResult result = client.eval(script);
            if (result.getEx() != null) {
                printer.println("error", result.getEx());
            }
            else {
                if (StringUtil.isNotBlank(result.getOut())) {
                    printer.println("stdout", result.getOut());
                }
                if (StringUtil.isNotBlank(result.getErr())) {
                    printer.println("stderr", result.getErr());
                }

                printer.println();
                printer.println("result", resultPrefix + result.getResult());
            }
        }
        catch (Exception ex) {
            errorHandler.accept(ex);
        }
    }

    @Override
    public void runAsync(
            final String script,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix,
            final ReplResultHistory resultHistory,
            final Consumer<Exception> errorHandler
    ) {
        printer.println(
            "error",
            "A remote REPL does not support running forms asynchronously!");
     }

    @Override
    public void runDebuggerExpressionAsync(
            final String expr,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final Consumer<Exception> errorHandler
    ) {
        printer.println(
            "error",
            "A remote REPL does not support debugging!");
    }

    @Override
    public boolean runInitialLoadFile(
            final String loadFile,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix
    ) {
        printer.println(
                "error",
                "A remote REPL does not support intial load files!");
        return false;
    }

    @Override
    public void cancelAsyncScripts() {
    }

    @Override
    public void close() {
        if (client != null) {
            try {
                client.close();
            }
            catch(Exception ignore) {}
        }
    }


    private final RemoteReplClient client;
}
