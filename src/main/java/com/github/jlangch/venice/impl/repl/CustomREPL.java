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
package com.github.jlangch.venice.impl.repl;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jline.reader.EndOfFileException;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.OSUtils;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.IRepl;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;


public class CustomREPL implements IRepl {

    public CustomREPL(
            final IInterceptor interceptor,
            final File app
    ) {
        this.interceptor = interceptor == null ? new AcceptAllInterceptor() : interceptor;
        this.app = app;
    }

    public void run(final String[] args) {
        if (!semaphore.tryAcquire()) {
            throw new VncException("The custom REPL is already running!");
        }

        try {
            ThreadContext.setInterceptor(interceptor);

            final CommandLineArgs cli = new CommandLineArgs(args);
            final ILoadPaths loadpaths = interceptor.getLoadPaths();

            config = ReplConfig.load(cli);

            initJLineLogger(config);

            ansiTerminal = isAnsiTerminal(cli, config);

            final String jansiVersion = config.getJansiVersion();

            if (OSUtils.IS_WINDOWS && jansiVersion == null) {
                System.out.print(
                    "--------------------------------------------------------------------\n" +
                    "The Venice REPL requires the Jansi library on Windows.              \n" +
                    "Please download the jar artifact 'org.fusesource.jansi:jansi:2.4.1' \n" +
                    "from a Maven repo and put it on the REPL classpath.                 \n" +
                    "                                                                    \n" +
                    "> curl https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/2.4.1/jansi-2.4.1.jar --output jansi-2.4.1.jar \n" +
                    "--------------------------------------------------------------------\n\n");
            }

            System.out.println("Venice custom REPL: " + Venice.getVersion());
            System.out.println("Home: " + new File(".").getCanonicalPath());
            System.out.println("Java: " + System.getProperty("java.version"));
            System.out.println("Jansi: " + (jansiVersion == null ? "not detected" : jansiVersion));
            System.out.println("Loading configuration from " + config.getConfigSource());
            if (loadpaths.active()) {
                System.out.print("Load paths: ");
                System.out.println(loadpaths.isUnlimitedAccess() ? "unrestricted > " : "retricted > ");
                loadpaths.getPaths().forEach(p -> System.out.println("   " + p));
            }
            System.out.println(getTerminalInfo());
            System.out.println("Type '!' for help.");

            repl(cli);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            semaphore.release();
            ThreadContext.remove();
        }
    }

    @Override
    public void setHandler(final Consumer<String> handler) {
        this.cmdHandler = handler;
    }

    @Override
    public void setPrompt(final String prompt) {
        this.prompt = prompt;
        this.secondaryPrompt = "";
    }

    @Override
    public void setPrompt(final String prompt, final String secondaryPrompt) {
        this.prompt = prompt;
        this.secondaryPrompt = secondaryPrompt;
    }

    @Override
    public int getTerminalWidth() {
        return terminal.getWidth();
    }

    @Override
    public int getTerminalHeight() {
        return terminal.getHeight();
    }


    private void repl(final CommandLineArgs cli) throws Exception {
        setPrompt(config.getPrompt(), ansiTerminal ? config.getSecondaryPrompt() : "");

        final Thread mainThread = Thread.currentThread();

        final TerminalBuilder builder = TerminalBuilder
                                            .builder()
                                            .streams(System.in, System.out)
                                            .system(true)
                                            .dumb(!ansiTerminal)
                                            .jna(false);

        terminal = OSUtils.IS_WINDOWS
                        ? builder
                            .jansi(ansiTerminal)
                            .build()
                        : builder
                            .encoding("UTF-8")
                            .build();

        terminal.handle(Signal.INT, signal -> mainThread.interrupt());


        final TerminalPrinter printer = new TerminalPrinter(
                                                config,
                                                terminal,
                                                ansiTerminal,
                                                false);

        final PrintStream out = createPrintStream("stdout", terminal);
        final PrintStream err = createPrintStream("stderr", terminal);
        final BufferedReader in = createBufferedReader("stdin", terminal);

        final IVeniceInterpreter venice = new VeniceInterpreter(interceptor);
        final Env env = loadEnv(venice, cli, out, err, in);

        if (!runApp(venice, env, printer)) {
            return; // stop REPL
        }

        final History history = new DefaultHistory();
        final LineReader reader = LineReaderBuilder
                                    .builder()
                                    .appName("Venice")
                                    .terminal(terminal)
                                    .history(history)
                                    .expander(new NullExpander())
                                    .option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
                                    .variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
                                    .build();
        final ReplResultHistory resultHistory = new ReplResultHistory(3);


        // REPL loop
        while (true) {
            resultHistory.mergeToEnv(env);

            String line;
            try {
                Thread.interrupted(); // reset the thread's interrupt status

                line = reader.readLine(prompt, null, (MaskingCallback)null, null);
                if (line != null) {
                    cmdHandler.accept(line);
                }
            }
            catch (UserInterruptException ex) {
                Thread.interrupted(); // reset the thread's interrupt status

                // quit the REPL
                printer.println("interrupt", " ! interrupted ! ");
                Thread.sleep(1000);
                break;
            }
            catch (EofException | EndOfFileException ex) {
                break;
            }
            catch (Exception ex) {
                printer.printex("error", ex);
            }
        }
    }

    private Env loadEnv(
            final IVeniceInterpreter venice,
            final CommandLineArgs cli,
            final PrintStream out,
            final PrintStream err,
            final BufferedReader in
    ) {
        final Env env =
               venice.createEnv(true, ansiTerminal, RunMode.REPL)
                     .setGlobal(new Var(
                                     new VncSymbol("*ARGV*"),
                                     cli.argsAsList(),
                                     false,
                                     Var.Scope.Global))
                     .setStdoutPrintStream(out)
                     .setStderrPrintStream(err)
                     .setStdinReader(in);

        return ReplFunctions.register(
                    env,
                    this, terminal, config,
                    true, ReplDirs.create());
    }

    private PrintStream createPrintStream(final String context, final Terminal terminal) {
        return new ReplPrintStream(
                    terminal,
                    ansiTerminal ? config.getColor(context) : null);
    }

    private BufferedReader createBufferedReader(final String context, final Terminal terminal) {
        return new BufferedReader(terminal.reader());
    }

    private String getTerminalInfo() {
        if (ansiTerminal) {
            if (config.getColorMode() == ColorMode.None) {
                return "Using ansi terminal (colors turned off, turn on with option '-colors')";
            }
            else {
                return "Using ansi terminal (colors turned on)";
            }
        }
        else {
            return "Using dumb terminal (colors turned off)";
        }
    }

    private boolean isAnsiTerminal(
            final CommandLineArgs cli,
            final ReplConfig config
    ) {
        final String jansiVersion = config.getJansiVersion();

        final boolean dumbTerminal = (OSUtils.IS_WINDOWS && (jansiVersion == null))
                                        || cli.switchPresent("-dumb")
                                        || config.isJLineDumbTerminal();

        return !dumbTerminal;
    }

    private void initJLineLogger(final ReplConfig config) {
        final Level jlineLogLevel = config.getJLineLogLevel();
        if (jlineLogLevel != null) {
            Logger.getLogger("org.jline").setLevel(jlineLogLevel);
        }
    }

    private boolean runApp(
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer
    ) {
        try {
            if (app == null) {
                printer.println("stdout", "No custom REPL app file supplied!");
            }
            else {
                printer.println("stdout", "Loading custom REPL file: '" + app.getPath() + "'");
                venice.RE("(load-file \"" + app.getPath() + "\")" , "user", env);
            }
            return true;
        }
        catch(Exception ex) {
            printer.printex("error", ex);
            printer.println("error", "Stopped REPL");
            return false; // stop the REPL
        }
    }


    private static final String DEFAULT_PROMPT_PRIMARY   = "venice> ";
    private static final String DEFAULT_PROMPT_SECONDARY = "      | ";

    private final Semaphore semaphore = new Semaphore(1);

    private final File app;

    private Terminal terminal;

    private String prompt = DEFAULT_PROMPT_PRIMARY;
    private String secondaryPrompt  = DEFAULT_PROMPT_SECONDARY;
    private Consumer<String> cmdHandler;
    private ReplConfig config;
    private IInterceptor interceptor;
    private boolean ansiTerminal = false;
}
