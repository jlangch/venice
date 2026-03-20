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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

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
 * <pre>java -jar venice-1.12.85.jar -script "(+ 1 1)"</pre>
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
 *
 *  <p>Launcher command line options:
 *  <pre>
 *  -loadpath path     defines a load path, colon or semi-colon delimited paths
 *                     On Windows the path separator <code>;</code> is supported
 *                     only. Linux and MacOS support the path separators <code>:</code>
 *                     and <code>;</code>
 *                     E.g.: -loadpath "/users/foo/scripts:/users/foo/res"
 *                           -loadpath "/users/foo/scripts;/users/foo/res"
 *
 *  -macroexpand b     turns up-front macro expansion on or off by setting the value
 *                     to true or false. Turning macro expansion on results in a
 *                     much better performance.
 *                     Enabled by default for launchers:  -file, -cp-file, -script, -app, or -app-rep
 *                     Disabled by default for launchers: -repl
 *
 *  -colors            use light mode colors (requires jansi library on the classpath)
 *
 *  -colors-light      synonym for -colors
 *
 *  -colors-dark       use dark mode colors (requires jansi library on the classpath)
 *
 *  -minimal           setup a minimal REPL. Only use together with -setup
 *
 *  -dir directory     REPL setup directory. Only use together with -setup
 *
 *  -file script       loads the script to run from a file
 *                     E.g.:  -file ./test.venice
 *
 *  -cp-file res       loads the script to run from the classpath
 *                     E.g.:  -cp-file com/github/jlangch/venice/test.venice
 *
 *  -script script     run a script
 *                     E.g.:  -script "(+ 1 10)"
 *
 *  -app app           run a Venice app
 *                     E.g.:  -app test-app.zip
 *
 *  -repl              start a REPL
 *                     E.g.:  -repl
 *
 *  -setup             setup a REPL
 *                     E.g.:  java -jar venice-1.12.85.jar -setup -colors -dir ./repl \n" +
 *                            java -jar venice-1.12.85.jar -setup -minimal -colors -dir ./repl \n" +
 *                            java -jar venice-1.12.85.jar -setup -colors-light -dir ./repl \n" +
 *                            java -jar venice-1.12.85.jar -setup -colors-dark -dir ./repl \n" +
 *
 *  -help              prints a help
 *  </pre>
 *
 *  <p>Note:
 *  The options '-file', '-cp-file', '-script', '-app', '-repl, '-app-repl', and '-setup'
 *  exclude each other
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
                new REPL(new AcceptAllInterceptor(loadPaths)).run(args);
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

        final boolean macroexpand = isMacroexpand(cli);
        final int replServerPort = getReplServerPort(cli);
        final String replServerPassword = getReplServerPassword(cli);
        final boolean replServerEncrypt = getReplEncrypt(cli);
        final boolean replServerCompress = getReplCompress(cli);
        final int replServerSessionTimeoutMinutes = getReplSessionTimeoutMinutes(cli);

        // run the file from the filesystem
        final String file = suffixWithVeniceFileExt(cli.switchValue("-file"));
        final String script = new String(FileUtil.load(new File(file)));

        final String scriptWrapped = "(do " + script + ")";

        final String result = runScript(
                                cli.removeSwitches("-file",
                                                   "-macroexpand",  "-loadpath",
                                                   "-minimal",      "dir",
                                                   "-colors",
                                                   "-colors-light", "-colors-dark",
                                                   "-repl-port", "-repl-pwd",
                                                   "-repl-encrypt", "-repl-compress",
                                                   "-repl-session-timeout"),
                                macroexpand,
                                replServerPort,
                                replServerPassword,
                                replServerEncrypt,
                                replServerCompress,
                                replServerSessionTimeoutMinutes,
                                interceptor,
                                scriptWrapped,
                                new File(file).getName());

        if (!"nil".equals(result)) {
            System.out.println(result);
        }
    }

    private static void runClasspathFileCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        final boolean macroexpand = isMacroexpand(cli);
        final int replServerPort = getReplServerPort(cli);
        final String replServerPassword = getReplServerPassword(cli);
        final boolean replServerEncrypt = getReplEncrypt(cli);
        final boolean replServerCompress = getReplCompress(cli);
        final int replServerSessionTimeoutMinutes = getReplSessionTimeoutMinutes(cli);

        // run the file from the classpath
        final String file = suffixWithVeniceFileExt(cli.switchValue("-cp-file"));
        final String script = new ClassPathResource(file).getResourceAsString();

        final String result = runScript(
                                cli.removeSwitches("-cp-file",
                                                   "-macroexpand",  "-loadpath",
                                                   "-minimal",      "dir",
                                                   "-colors",
                                                   "-colors-light", "-colors-dark",
                                                   "-repl-port", "-repl-pwd",
                                                   "-repl-encrypt", "-repl-compress",
                                                   "-repl-session-timeout"),
                                macroexpand,
                                replServerPort,
                                replServerPassword,
                                replServerEncrypt,
                                replServerCompress,
                                replServerSessionTimeoutMinutes,
                                interceptor,
                                script,
                                new File(file).getName());

        if (!"nil".equals(result)) {
            System.out.println(result);
        }
    }

    private static void runScriptCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        final IInterceptor interceptor = new AcceptAllInterceptor(loadPaths);

        final boolean macroexpand = isMacroexpand(cli);
        final int replServerPort = getReplServerPort(cli);
        final String replServerPassword = getReplServerPassword(cli);
        final boolean replServerEncrypt = getReplEncrypt(cli);
        final boolean replServerCompress = getReplCompress(cli);
        final int replServerSessionTimeoutMinutes = getReplSessionTimeoutMinutes(cli);

        // run the script passed as command line argument
        final String script = cli.switchValue("-script");

        final String result = runScript(
                                cli.removeSwitches("-script",
                                                   "-macroexpand",  "-loadpath",
                                                   "-minimal",      "dir",
                                                   "-colors",
                                                   "-colors-light", "-colors-dark",
                                                   "-repl-port",    "-repl-pwd",
                                                   "-repl-encrypt", "-repl-compress",
                                                   "-repl-session-timeout"),
                                macroexpand,
                                replServerPort,
                                replServerPassword,
                                replServerEncrypt,
                                replServerCompress,
                                replServerSessionTimeoutMinutes,
                                interceptor,
                                script,
                                "script");
        System.out.println(result);
    }

    private static void runAppCmd(
            final ILoadPaths loadPaths,
            final CommandLineArgs cli
    ) {
        System.out.println("Launching Venice application ...");

        // run the Venice application archive
        final File appFile = new File(suffixWithZipFileExt(cli.switchValue("-app")));

        // The app runner has 'macroexpand' implicitly enabled
        AppRunner.run(
            appFile,
            cli.removeSwitches("-app", "-macroexpand", "-loadpath")
               .argsAsList(),
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
        new CustomREPL(interceptor, new File(file))
               .run(cli.removeSwitches("-macroexpand").args());
    }

    private static void printHelp() {
        System.out.println(
             "The Launcher runs Venice scripts or apps and starts the REPL. \n" +
             "\n" +
             "The launcher is configured as the Venice JAR's main-class. \n" +
             "\n" +
             "Running scripts: \n" +
             "    java -jar venice-1.12.85.jar -script \"(+ 1 1)\" \n" +
             "\n" +
             "Running a REPL:  \n" +
             "    java \\ \n" +
             "       -server \\ \n" +
             "       -Xmx2G \\ \n" +
             "       -XX:-OmitStackTraceInFastThrow \\ \n" +
             "       -cp \"libs/*\" \\ \n" +
             "       com.github.jlangch.venice.Launcher \\ \n" +
             "       -repl \\ \n" +
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
             "  -colors-light     synonym for -colors\n" +
             "\n" +
             "  -colors-dark      use dark mode colors (requires jansi library on the classpath)\n" +
             "\n" +
             "  -minimal          setup a minimal REPL. Only use together with -setup\n" +
             "\n" +
             "  -dir directory    REPL setup directory. Only use together with -setup\n" +
             "\n" +
             "  -file script      run a script that is loaded from a file \n" +
             "                    e.g.:  -file ./test.venice \n" +
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
             "  -repl-port port   remote REPL communication port \n" +
             "                    e.g.:  -repl-port 33334 \n" +
             "\n" +
             "  -repl-pwd pwd     remote REPL password \n" +
             "                    e.g.:  -repl-pwd xcf6zu=UI \n" +
             "\n" +
             "  -repl-encrypt b   remote REPL transport encryption. Defaults to on\n" +
             "                    e.g.:  -repl-encrypt on \n" +
             "                           -repl-encrypt off \n" +
             "\n" +
             "  -repl-compress b  remote REPL transport compression. Defaults to off\n" +
             "                    e.g.:  -repl-compress on \n" +
             "                           -repl-compress off \n" +
             "\n" +
             "  -repl-session-timeout t  \n" +
             "                    remote REPL session timeout in minutes. Defaults to \n" +
             "                    20 minutes\n" +
             "                    e.g.: -repl-session-timeout 20 \n" +
             "\n" +
             "  -app-repl         start a custom REPL \n" +
             "                    e.g.:  -app-repl /Users/foo/tools/dbclient.venice\n" +
             "\n" +
             "  -setup            setup a REPL \n" +
             "                    e.g.:  java -jar venice-1.12.85.jar -setup -colors -dir ./repl \n" +
             "                           java -jar venice-1.12.85.jar -setup -minimal -colors -dir ./repl \n" +
             "                           java -jar venice-1.12.85.jar -setup -colors-light -dir ./repl \n" +
             "                           java -jar venice-1.12.85.jar -setup -colors-dark -dir ./repl \n" +
             "\n" +
             "  -help             prints a help \n" +
             "\n" +
             "Note: \n" +
             "  The options '-file', '-cp-file', '-script', '-app', '-repl', '-app-repl', \n" +
             "  and '-setup' exclude each other \n");
    }

    private static String runScript(
            final CommandLineArgs cli,
            final boolean macroexpand,
            final int replServerPort,
            final String replServerPassword,
            final boolean replServerEncrypt,
            final boolean replServerCompress,
            final int replServerSessionTimeoutMinutes,
            final IInterceptor interceptor,
            final String script,
            final String name
    ) {
        final IVeniceInterpreter venice = new VeniceInterpreter(interceptor);

        final Env env = createEnv(
                            venice,
                            macroexpand,
                            RunMode.SCRIPT,
                            Arrays.asList(
                                convertCliArgsToVar(cli)));

        if (replServerPort > 0) {
            try (RemoteReplServer server = new RemoteReplServer(
                                                venice,
                                                new Env(env),  // run in own context
                                                replServerPort,
                                                replServerPassword,
                                                replServerEncrypt,
                                                replServerCompress,
                                                replServerSessionTimeoutMinutes)
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

    private static boolean isMacroexpand(final CommandLineArgs cli) {
        if (cli.switchPresent("-macroexpand")) {
             final String value = cli.switchValue("-macroexpand");
             return "true".equals(value.toLowerCase());
        }
        else {
            return true;  // defaults to true (macroexpand on)
        }
    }

    private static int getReplServerPort(final CommandLineArgs cli) {
        if (cli.switchPresent("-repl-port")) {
             final long port = cli.switchLongValue("-repl-port", 0L);
             if (port < 0L) return 0;
             if (port > 65536) return 0;
             return (int)port;
        }
        else {
            return 0;
        }
    }

    private static String getReplServerPassword(final CommandLineArgs cli) {
        if (cli.switchPresent("-repl-pwd")) {
            final String pwd = cli.switchValue("-repl-pwd");
            if (pwd.startsWith("env:")) {
                final String envVar = pwd.substring(4);
                return System.getenv(envVar);
            }
            else {
                return pwd;
            }
       }
       else {
            return null;
       }
    }

    private static boolean getReplEncrypt(final CommandLineArgs cli) {
        return cli.switchPresent("-repl-encrypt")
                ? isTrue(cli.switchValue("-repl-encrypt", "on"), true)
                : true;
    }

    private static boolean getReplCompress(final CommandLineArgs cli) {
       return cli.switchPresent("-repl-compress")
                ? isTrue(cli.switchValue("-repl-compress", "off"), false)
                : false;
    }

    private static int getReplSessionTimeoutMinutes(final CommandLineArgs cli) {
        if (cli.switchPresent("-repl-session-timeout")) {
            final long timeoutMinutes = cli.switchLongValue(
                                                "-repl-session-timeout",
                                                DEFAULT_REPL_SESSION_TIMEOUT);
            return (int)Math.min(1440L, Math.max(1L, timeoutMinutes));
        }
        else {
            return (int)DEFAULT_REPL_SESSION_TIMEOUT;
        }
    }

    private static boolean isTrue(final String s, final boolean defaultVal) {
        if ("on".equalsIgnoreCase(s)) return true;
        if ("off".equalsIgnoreCase(s)) return false;

        if ("yes".equalsIgnoreCase(s)) return true;
        if ("no".equalsIgnoreCase(s)) return false;

        if ("true".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s)) return false;

        return defaultVal;
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


    private final static long DEFAULT_REPL_SESSION_TIMEOUT = 30L;
}
