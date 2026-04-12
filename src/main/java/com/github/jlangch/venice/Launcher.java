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
package com.github.jlangch.venice;

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.AppRunner;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.SystemFunctions;
import com.github.jlangch.venice.impl.repl.CustomREPL;
import com.github.jlangch.venice.impl.repl.REPL;
import com.github.jlangch.venice.impl.repl.install.ReplInstaller;
import com.github.jlangch.venice.impl.repl.remote.RemoteReplServer;
import com.github.jlangch.venice.impl.repl.remote.ReplRemotingConfig;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.loadpath.ILoadPaths;
import com.github.jlangch.venice.impl.util.loadpath.LoadPathsFactory;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;


/**
 * The {@code Launcher} runs Venice scripts or apps and starts the REPL.
 *
 * <p>The launcher is configured as the Venice JAR's main-class.
 *
 * <p>Running scripts:
 * <pre>java -jar venice-1.12.89.jar -script "(+ 1 1)"</pre>
 *
 * <p>Running a REPL:
 * <pre>
 *    java \
 *       -server \
 *       -Xmx2G \
 *       -XX:-OmitStackTraceInFastThrow \
 *       -cp "libs/*" \
 *       com.github.jlangch.venice.Launcher \
 *       -repl \
 *       -colors
 *  </pre>
 */
public class Launcher {

    public static void main(final String[] args) {
        final int exitCode = run(args);
        System.exit(exitCode);
    }

    public static int run(final String[] args) {
        final CommandLineArgs cli = new CommandLineArgs(args);

        final ILoadPaths loadPaths = LoadPathsFactory.parseDelimitedLoadPath(
                                            cli.switchValue("-loadpath"),
                                            true);

        try {
            if (cli.switchPresent("-help")) {
                printHelp();
            }
            else if (cli.switchPresent("-setup")) {
                if (!ReplInstaller.install(args)) {
                    return 99;  // setup was not successful
                }
            }
            else if (cli.switchPresent("-file")) {
                runFileCmd(loadPaths, cli);
            }
            else if (cli.switchPresent("-cp-file")) {
                runClasspathFileCmd(loadPaths, cli);
            }
            else if (cli.switchPresent("-script")) {
                runScriptCmd(loadPaths, cli);
            }
            else if (cli.switchPresent("-app")) {
                runAppCmd(loadPaths, cli);
            }
            else if (cli.switchPresent("-app-repl")) {
                runCustomReplCmd(loadPaths, cli);
            }
            else {
                // run the Venice REPL
                runReplCmd(loadPaths, cli);
            }

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

    private static void runFileCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        // run the file from the filesystem
        final String file = suffixWithVeniceFileExt(cli.switchValue("-file"));
        final String script = new String(FileUtil.load(new File(file)));
        final String scriptName = new File(file).getName();

        final String scriptWrapped = "(do " + script + ")";

        final String result = runScript(cli, interceptor, scriptWrapped, scriptName, "-file");

        if (!"nil".equals(result)) {
            System.out.println(result);
        }
    }

    private static void runClasspathFileCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        // run the file from the classpath
        final String file = suffixWithVeniceFileExt(cli.switchValue("-cp-file"));
        final String script = new ClassPathResource(file).getResourceAsString();
        final String scriptName = new File(file).getName();

        final String result = runScript(cli, interceptor, script, scriptName, "-cp-file");

        if (!"nil".equals(result)) {
            System.out.println(result);
        }
    }

    private static void runScriptCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        // run the script passed as command line argument
        final String script = cli.switchValue("-script");

        final String result = runScript(cli, interceptor, script, "script", "-script");

        System.out.println(result);
    }

    private static String runScript(
            final CommandLineArgs cli,
            final IInterceptor interceptor,
            final String script,
            final String name,
            final String blockCmdLineOption
    ) {
        final boolean macroexpand = isMacroexpand(cli);
        final String replServerConfigFile = getReplServerConfigFile(cli);

        final ReplRemotingConfig replRemoteConfig =
                replServerConfigFile == null
                    ? null
                    : ReplRemotingConfig.load(new File(replServerConfigFile));

        // adjust options
        final CommandLineArgs scriptCli = cli.removeAllSwitches(SCRIPT_BLOCK_OPTIONS)
                                             .removeSwitch(blockCmdLineOption);

        final IVeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final List<Var> vars = Arrays.asList(convertCliArgsToVar(scriptCli));

        final Env env = createEnv(venice, macroexpand, RunMode.SCRIPT, vars);

        if (replRemoteConfig != null) {
            try (RemoteReplServer server = new RemoteReplServer(
                                                venice,
                                                new Env(env),  // run in own context
                                                replRemoteConfig);
            ) {
                return venice.PRINT(venice.RE(script, name, env));
            }
            catch(IOException ex) {
                throw new VncException("Failed to close Remote REPL server!", ex);
            }
        }
        else {
            return venice.PRINT(venice.RE(script, name, env));
        }
    }

    private static void runAppCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        System.out.println("Launching Venice application ...");

        // run the Venice application archive
        final File appFile = new File(suffixWithZipFileExt(cli.switchValue("-app")));

        // The app runner has 'macroexpand' implicitly enabled

        // Luncher options
        final CommandLineArgs appCli = cli.removeAllSwitches(APP_BLOCK_OPTIONS)
                                          .removeSwitch("-app");

        AppRunner.run(
            appFile,
            appCli.argsAsList(),
            loadPaths,
            new PrintStream(System.out, true),
            new PrintStream(System.err, true),
            new InputStreamReader(System.in));
    }

    private static void runCustomReplCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        // run a custom application repl
        final String file = cli.switchValue("-app-repl");

        // The custom REPL has 'macroexpand' implicitly enabled

        // Launcher options
        final CommandLineArgs appCli = cli.removeAllSwitches(APP_REPL_BLOCK_OPTIONS)
                                          .removeSwitch("-app-repl");

        new CustomREPL(interceptor, new File(file)).run(appCli.args());
    }

    private static void runReplCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        // Launcher options
        final CommandLineArgs appCli = cli.removeAllSwitches(REPL_BLOCK_OPTIONS)
                                          .removeSwitch("-repl");

        new REPL(new AcceptAllInterceptor(loadPaths)).run(appCli);
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


    // OPTIONS
    //
    // ╭─────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
    // │                             -help   -setup   -file   -cp-file   -script   -app   -repl   -app-repl          │
    // │─────────────────────────────────────────────────────────────────────────────────────────────────────────────│
    // │                                                                                                             │
    // │ -macroexpand                                   +        +          +        +      -         +              │
    // │ -loadpath                              x       x        x          x        x      x         x              │
    // │ -restartable                           x                                           x                        │
    // │ -minimal                               x                                                                    │
    // │ -dir                                   x                                                                    │
    // │ -load-file                                                                         x                        │
    // │                                                                                                             │
    // │ -colors                                x                                           x         x              │
    // │ -colors-light                          x                                           x         x              │
    // │ -colors-dark                           x                                           x         x              │
    // │                                                                                                             │
    // │ -repl-server-config                             x        x         x                                        │
    // ╰─────────────────────────────────────────────────────────────────────────────────────────────────────────────╯
    //                                               x: optionally    +: implicitely active    -:implicitely inactive

    private static void printHelp() {
        System.out.println(
             "The Launcher runs Venice scripts and apps or starts the REPL. \n" +
             "\n" +
             "The launcher is configured as the Venice JAR's main-class. \n" +
             "\n" +
             "Running scripts: \n" +
             "    java -jar venice-1.12.89.jar -script \"(+ 1 1)\" \n" +
             "\n" +
             "Running a REPL:  \n" +
             "    java \\ \n" +
             "       -server \\\n" +
             "       -Xmx2G \\\n" +
             "       -XX:-OmitStackTraceInFastThrow \\\n" +
             "       -cp \"libs/*\" \\\n" +
             "       com.github.jlangch.venice.Launcher \\\n" +
             "       -repl \\\n" +
             "       -colors \n" +
             "\n\n" +
             "Launcher command line options: \n" +
             "  -loadpath path    defines a colon or semi-colon delimited load path.\n" +
             "                    Linux and MacOS support the path separators \":\" and \";\"\n" +
             "                    On Windows the path separator \";\" is supported only.\n" +
             "                    E.g.: -loadpath \"/users/foo/scripts:/users/foo/res\"\n" +
             "                          -loadpath \"/users/foo/scripts;/users/foo/res\"\n" +
             "\n" +
             "  -macroexpand b    turns up-front macro expansion on or off by setting the value \n" +
             "                    to true or false. Turning macro expansion on results in a \n" +
             "                    much better performance. \n" +
             "                    (The launcher also understands on/off values) \n"+
             "                    Enabled by default for launchers:  -file, -cp-file, -script, \n" +
             "                                                       -app, or -app-repl \n" +
             "                    Disabled by default for launchers: -repl \n" +
             "\n" +
             "  -colors           use light mode colors (requires jansi library on the classpath)\n" +
             "\n" +
             "  -colors-light     synonym for -colors (requires jansi library on the classpath)\n" +
             "\n" +
             "  -colors-dark      use dark mode colors (requires jansi library on the classpath)\n" +
             "\n" +
             "  -minimal          setup a minimal REPL. Only use together with -setup\n" +
             "\n" +
             "  -dir directory    REPL setup directory. Only use together with -setup\n" +
             "\n" +
             "  -restartable      Mark a REPL as restartable. \n" +
             "                    Only use this switch in the 'repl.sh' or 'repl.bat' launcher\n" +
             "                    scripts!\n" +
             "\n" +
             "  -file script      run a script that is loaded from a file \n" +
             "                    e.g.:  -file ./test.venice \n" +
             "\n" +
             "                    run a script with a REPL server enabled:\n" +
             "                    e.g.:  -file ./test.venice -repl-port 33334 -repl-pwd xcf6zu=UI\n" +
             "\n" +
             "  -cp-file res      run a script that is loaded from a classpath resource file \n" +
             "                    e.g.:  -cp-file com/github/jlangch/venice/test.venice \n" +
             "\n" +
             "  -script script    run a script \n" +
             "                    e.g.:  -script \"(+ 1 10)\" \n" +
             "\n" +
             "  -app app          run a Venice app  \n" +
             "                    e.g.:  -app test-app.zip \n" +
             "\n" +
             "  -repl             start a REPL \n" +
             "                    e.g.:  -repl \n" +
             "\n" +
             "  -app-repl         start a custom REPL \n" +
             "                    e.g.:  -app-repl /Users/foo/tools/dbclient.venice\n" +
             "\n" +
             "  -setup            setup a REPL \n" +
             "                    e.g.:  java -jar venice-1.12.89.jar -setup -colors -dir ./repl \n" +
             "                           java -jar venice-1.12.89.jar -setup -minimal -colors -dir ./repl \n" +
             "                           java -jar venice-1.12.89.jar -setup -colors-light -dir ./repl \n" +
             "                           java -jar venice-1.12.89.jar -setup -colors-dark -dir ./repl \n" +
             "\n" +
             "  -repl-server-config file\n" +
             "                    REPL server JSON configuration file\n" +
             "                    Starts a REPL server on the given socket port and acts as\n" +
             "                    a local REPL in the  application.\n" +
             "                    Only use together with -file or -cp-file option\n" +
             "                    e.g.:  { \"port\": 33334,                \n" +
             "                             \"password\": \"123\",          \n" +
             "                             \"encrypt\": true,              \n" +
             "                             \"compress\": false,            \n" +
             "                             \"sessionTimeoutMinutes\": 30,  \n" +
             "                             \"signKeyExchange\": false,     \n" +
             "                             \"serverPublicKeyFile\": null,  \n" +
             "                             \"serverPrivateKeyFile\": null, \n" +
             "                             \"clientPublicKeyFile\": null } \n" +

             "\n" +
             "  -help             prints a help \n" +
             "\n" +
             "Note: \n" +
             "  The options '-file', '-cp-file', '-script', '-app', '-repl', '-app-repl', \n" +
             "  and '-setup' exclude each other \n");
    }

    private static boolean isMacroexpand(final CommandLineArgs cli) {
        return CommandLineArgs.isTrue(cli.switchValue("-macroexpand", "on"), true);
    }

    private static String getReplServerConfigFile(final CommandLineArgs cli) {
        return cli.switchValue("-repl-server-config", null);
    }

    private static Var convertCliArgsToVar(final CommandLineArgs cli) {
        return new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false, Var.Scope.Global);
    }

    private static String suffixWithVeniceFileExt(final String s) {
        return s == null ? null : (s.endsWith(".venice") ? s : s + ".venice");
    }

    private static String suffixWithZipFileExt(final String s) {
        return s == null ? null : (s.endsWith(".zip") ? s : s + ".zip");
    }

    private final static Set<String> SCRIPT_BLOCK_OPTIONS = toSet(
            "-macroexpand",
            "-loadpath",
            "-repl-server-config");

    private final static Set<String> APP_BLOCK_OPTIONS = toSet(
            "-macroexpand",
            "-loadpath");

    private final static Set<String> APP_REPL_BLOCK_OPTIONS = toSet(
            "-macroexpand",
            "-loadpath");

    private final static Set<String> REPL_BLOCK_OPTIONS = toSet(
            "-loadpath");
}
