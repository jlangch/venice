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

import static com.github.jlangch.venice.impl.util.CollectionUtil.first;
import static com.github.jlangch.venice.impl.util.StringUtil.splitIntoLines;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

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
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.jar.AutoRunScriptJarRewriter;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


/**
 * Auto run script laucher
 *
 * <p>This launcher is configured as the Venice JAR's main-class by the
 * JAR rewriter to make a Venice script runnable.
 *
 * <p>Running scripts:
 * <pre>java -jar venice-1.12.56.jar"</pre>
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

            final String script = loadAutoRunScript();
            final String scriptName = loadAutoRunScriptName();

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
            ex.printVeniceStackTrace();
            return 99;
        }
        catch (Exception ex) {
            ex.printStackTrace();
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

    private static String loadAutoRunScript() {
        final String script = new ClassPathResource(AutoRunScriptJarRewriter.AUTORUN_SCRIPT_PATH)
                                       .getResourceAsString("UTF-8");
        if (script == null) {
            throw new VncException("Failed to load embedded auto run script!");
        }

        return script;
    }

    private static String loadAutoRunScriptName() {
        try {
            final String data = new ClassPathResource(AutoRunScriptJarRewriter.AUTORUN_META_PATH)
                                           .getResourceAsString("UTF-8");

            final String line = trimToEmpty(first(splitIntoLines(data)));

            final String name = trimToNull(line.split("=")[1]);

            return name == null ? "autorun" : name;
        }
        catch(Exception ex) {
            return "autorun";
        }
    }

    private static Var convertCliArgsToVar(final CommandLineArgs cli) {
        return new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false, Var.Scope.Global);
    }
}
