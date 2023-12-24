/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.InterruptedException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.thread.ThreadBridge;
import com.github.jlangch.venice.impl.threadpool.ThreadPoolUtil;
import com.github.jlangch.venice.impl.types.VncVal;


public class ScriptExecuter {

    public ScriptExecuter() {

    }


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
            final VncVal result = venice.RE(script, "user", env);
            if (result != null) {
                printer.println("result", resultPrefix + venice.PRINT(result));

                // do not add the result for "*1", "*2", "*3", "**" to the result history
                if (resultHistory != null && !resultHistory.isResultHistorySymbol(script)) {
                    resultHistory.add(result);
                }
            }
        }
        catch (Exception ex) {
            errorHandler.accept(ex);
        }
    }

    public void runAsync(
            final String script,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix,
            final ReplResultHistory resultHistory,
            final Consumer<Exception> errorHandler
    ) {
        final long asyncID = asyncCounter.getAndIncrement();

        printer.println("debug", String.format("[%d] Async ...", asyncID));

        final Thread replThread = Thread.currentThread();

        // Create a wrapper that inherits the Venice thread context
        // from the parent thread to the executer thread!
        final ThreadBridge threadBridge = ThreadBridge.create("run-script-async");
        final Callable<Boolean> task = threadBridge.bridgeCallable(() ->  {
            try {
                final VncVal result = venice.RE(script, "user", env);

                printer.println("result", String.format(
                                            "[%d] %s%s",
                                            asyncID,
                                            resultPrefix,
                                            venice.PRINT(result)));

                printer.println("debug", String.format(
                                            "[%d] Async execution finished.",
                                            asyncID));

                // do not add the result for "*1", "*2", "*3", "**" to the
                // result history
                if (resultHistory != null && !resultHistory.isResultHistorySymbol(script)) {
                    resultHistory.add(result);
                }

                try { Thread.sleep(200); } catch(Exception ex) {}

                return true;
            }
            catch (InterruptedException ex) {
                printer.println(
                    "debug",
                    "\nScript under debugging interrupted and terminated!");
                return false;
            }
            catch (Exception ex) {
                errorHandler.accept(ex);
                return false;
            }
            finally {
                // Interrupt the LineReader of the REPL to display a new prompt
                replThread.interrupt();
            }
        });

        futures.add(executor.submit(task));
    }

    public void runDebuggerExpressionAsync(
            final String expr,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final Consumer<Exception> errorHandler
    ) {
        // Create a wrapper that inherits the Venice thread context
        // from the parent thread to the executer thread!
        final ThreadBridge threadBridge = ThreadBridge.create("run-script-async");
        final Callable<Boolean> task = threadBridge.bridgeCallable(() -> {
            try {
                final Env safeEnv = new Env(env);

                final VncVal result = venice.RE(expr, "debugger", safeEnv);
                printer.println("debug", venice.PRINT(result));
                return true;
            }
            catch (Exception ex) {
                errorHandler.accept(ex);
                return false;
            }});

        try {
            futures.add(executor.submit(task));
        }
        catch(Exception ex) {
            errorHandler.accept(ex);
        }
    }

    public boolean runInitialLoadFile(
            final String loadFile,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix
    ) {
        try {
            if (loadFile != null) {
                printer.println("stdout", "Loading file \"" + loadFile + "\"");
                final VncVal result = venice.RE(
                                        "(load-file \"" + loadFile + "\")",
                                        "user",
                                        env);
                printer.println("stdout", resultPrefix + venice.PRINT(result));
            }
            return true;
        }
        catch(Exception ex) {
            printer.printex("error", ex);
            return false;
        }
    }

    public void cancelAsyncScripts() {
        futures.forEach(f -> f.cancel(true));

        futures = futures
                    .stream()
                    .filter(f -> !f.isDone())
                    .collect(Collectors.toList());
    }



    private List<Future<Boolean>> futures = new ArrayList<>();

    private final AtomicLong asyncCounter = new AtomicLong(1L);

    private final ExecutorService executor =
            Executors.newCachedThreadPool(
                    ThreadPoolUtil.createCountedThreadFactory(
                            "repl-async-pool",
                            true /* daemon threads */));
}
