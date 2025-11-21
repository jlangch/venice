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
package com.github.jlangch.venice.impl;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.SystemFunctions;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.autorun.AutoRunScript;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


/**
 * Auto run script laucher
 */
public class AutoRunScriptLauncher {

    public static void main(final String[] args) {
        final int exitCode = run(args);
        System.exit(exitCode);
    }

    public static int run(final String[] args) {
        final CommandLineArgs cli = new CommandLineArgs(args);

        try {
            final IVeniceInterpreter venice = new VeniceInterpreter(
                                                    new AcceptAllInterceptor());

            final String script = AutoRunScript.loadAutoRunScript();
            final String scriptName = AutoRunScript.loadAutoRunScriptName();

            final Env env = createEnv(
                                venice,
                                true,  // macroexpand
                                RunMode.SCRIPT,
                                Arrays.asList(
                                    convertCliArgsToVar(cli)));

            venice.PRINT(venice.RE(script, scriptName, env));

            return SystemFunctions.SYSTEM_EXIT_CODE.get();
        }
        catch (VncException ex) {
            System.err.println("Failed to run script!");
            ex.printVeniceStackTrace();
            return 99;
        }
        catch (Exception ex) {
            System.err.println("Failed to run script!");
            System.err.println(ex.getMessage());
            return 99;
        }
    }

    private static Env createEnv(
            final IVeniceInterpreter venice,
            final boolean macroexpand,
            final RunMode runMode,
            final List<Var> vars
    ) {
        return venice.createEnv(macroexpand, false, runMode)
                     .addGlobalVars(vars)
                     .setStdoutPrintStream(new PrintStream(System.out, true))
                     .setStderrPrintStream(new PrintStream(System.err, true))
                     .setStdinReader(new InputStreamReader(System.in));
    }

    private static Var convertCliArgsToVar(final CommandLineArgs cli) {
        return new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false, Var.Scope.Global);
    }
}
