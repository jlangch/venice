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

import java.util.function.Consumer;

import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;


public interface IScriptExecuter {

    public void runSync(
            final String script,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix,
            final ReplResultHistory resultHistory,
            final Consumer<Exception> errorHandler);

    public void runAsync(
            final String script,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix,
            final ReplResultHistory resultHistory,
            final Consumer<Exception> errorHandler);

    public void runDebuggerExpressionAsync(
            final String expr,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final Consumer<Exception> errorHandler);

    public boolean runInitialLoadFile(
            final String loadFile,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer,
            final String resultPrefix);

    public void cancelAsyncScripts();

    public void close();
}
