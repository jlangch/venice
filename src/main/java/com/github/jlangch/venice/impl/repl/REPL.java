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

import static com.github.jlangch.venice.impl.util.CollectionUtil.drop;
import static com.github.jlangch.venice.impl.util.CollectionUtil.first;
import static com.github.jlangch.venice.impl.util.CollectionUtil.second;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.OSUtils;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.IRepl;
import com.github.jlangch.venice.InterruptedException;
import com.github.jlangch.venice.LicenseMgr;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.SourceCodeRenderer;
import com.github.jlangch.venice.SymbolNotFoundException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.EnvUtils;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.functions.JsonFunctions;
import com.github.jlangch.venice.impl.functions.SystemFunctions;
import com.github.jlangch.venice.impl.javainterop.DynamicClassLoader2;
import com.github.jlangch.venice.impl.namespaces.Namespaces;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.sandbox.SandboxFunctionGroups;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.zip.ZipFileSystemUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class REPL implements IRepl {

    public REPL(final IInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void run(final String[] args) {
        ThreadContext.setInterceptor(interceptor);

        if (terminal != null) {
            throw new VncException("The REPL is already running!");
        }

        final CommandLineArgs cli = new CommandLineArgs(args);
        final ILoadPaths loadpaths = interceptor.getLoadPaths();

        try {
            boolean macroexpand = false;

            config = ReplConfig.load(cli);

            initJLineLogger(config);

            final boolean setupMode = isSetupMode(cli);

            restartable = isRestartable(cli);

            ansiTerminal = isAnsiTerminal(cli, config);

            macroexpand = isMacroexpand(cli);

            if (ReplRestart.exists()) {
                try {
                    final ReplRestart restart = ReplRestart.read();
                    if (!restart.oudated()) {
                        macroexpand |= restart.hasMacroExpand();

                        if (restart.getColorMode() != ColorMode.None) {
                            config.switchColorMode(restart.getColorMode());
                        }
                    }
                }
                finally {
                    ReplRestart.remove();
                }
            }

            if (OSUtils.IS_WINDOWS) {
                final String jansiVersion = config.getJansiVersion();
                if (jansiVersion != null) {
                    System.out.println("Using Jansi V" + jansiVersion);
                }
                else if (!setupMode) {
                    System.out.print(
                            "--------------------------------------------------------------------\n" +
                            "The Venice REPL requires the jansi library on Windows.              \n" +
                            "Please download the jar artifact 'org.fusesource.jansi:jansi:2.4.1' \n" +
                            "from a Maven repo and put it on the classpath.                      \n" +
                            "--------------------------------------------------------------------\n\n");
                }
            }

            System.out.println("Venice REPL: V" + Venice.getVersion() + (setupMode ? " (setup mode)": ""));
            System.out.println("Java: " + System.getProperty("java.version"));
            System.out.println("Loading configuration from " + config.getConfigSource());
            if (loadpaths.active()) {
                System.out.print("Load paths: ");
                System.out.println(loadpaths.isUnlimitedAccess() ? "unrestricted > " : "retricted > ");
                loadpaths.getPaths().forEach(p ->  System.out.println("   " + p));
            }
            System.out.println(getTerminalInfo());
            if (macroexpand) {
                System.out.println("Macro expansion enabled");
            }
            if (!setupMode) {
                System.out.println("Type '!' for help.");
            }

            replDirs = ReplDirs.create();

            repl(cli, macroexpand);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setHandler(final Consumer<String> handler) {
        // not supported
    }

    @Override
    public void setPrompt(final String prompt) {
        // not supported
    }

    @Override
    public void setPrompt(final String prompt, final String secondaryPrompt) {
        // not supported
    }

    @Override
    public int getTerminalWidth() {
        return terminal.getWidth();
    }

    @Override
    public int getTerminalHeight() {
        return terminal.getHeight();
    }


    private void repl(
            final CommandLineArgs cli,
            final boolean macroexpand
    ) throws Exception {
        promptVenice = config.getPrompt();
        promptDebug = "debug> ";
        resultPrefix = config.getResultPrefix();

        changePrompt(promptVenice);

        final Thread mainThread = Thread.currentThread();

        final TerminalBuilder builder = TerminalBuilder
                                            .builder()
                                            .streams(System.in, System.out)
                                            .system(true)
                                            .dumb(!ansiTerminal)
                                            .jna(false);

        if (OSUtils.IS_WINDOWS) {
            builder.jansi(ansiTerminal);
        }
        else if (isRunningOnLinuxGitPod()) {
            // The terminal detection on Linux GitPod instances is wrong "xterm-color"
            // so set it explicitly to "xterm-256color"!
            builder.encoding("UTF-8");
            builder.type("xterm-256color");
        }
        else {
            builder.encoding("UTF-8");
        }

        terminal = builder.build();

        terminal.handle(Signal.INT, signal -> mainThread.interrupt());

        final PrintStream out = createPrintStream("stdout", terminal);
        final PrintStream err = createPrintStream("stderr", terminal);
        final BufferedReader in = createBufferedReader("stdin", terminal);

        printer = new TerminalPrinter(config, terminal, ansiTerminal, false);

        venice = new VeniceInterpreter(interceptor);
        // load the core functions without macro expansion! ==> check this!
        env = loadEnv(venice, cli, terminal, out, err, in, false);
        venice.setMacroExpandOnLoad(macroexpand);

        if (isSetupMode(cli)) {
            setupRepl(cli, venice, env, printer);
            return; // we stop here
        }

        if (!scriptExec.runInitialLoadFile(
                config.getLoadFile(), venice, env, printer, resultPrefix)
        ) {
            printer.println("error", "Stopped REPL");
            return; // we stop here, if the initial load file run failed
        }

        highlighter = config.isSyntaxHighlighting()
                        ? new ReplHighlighter(config)
                        : null;

        final ReplParser parser = new ReplParser(venice);
        parser.setEscapeChars(new char[0]);  // leave the char escape handling to Venice

        final History history = new DefaultHistory();
        final ReplResultHistory resultHistory = new ReplResultHistory(3);
        final ReplCompleter completer = new ReplCompleter(
                                                venice,
                                                env,
                                                interceptor.getLoadPaths().getPaths());
        final LineReader reader = createLineReader(
                                    terminal,
                                    history,
                                    completer,
                                    parser,
                                    secondaryPrompt);

        highlight = highlighter != null;

        replLoop(cli, resultPrefix, terminal, reader, history, resultHistory, out, err, in);
    }

    private void replLoop(
            final CommandLineArgs cli,
            final String resultPrefix,
            final Terminal terminal,
            final LineReader reader,
            final History history,
            final ReplResultHistory resultHistory,
            final PrintStream out,
            final PrintStream err,
            final BufferedReader in
    ) {
        while (true) {
            ThreadContext.clearCallStack(); // clear the Venice callstack
            Thread.interrupted(); // reset the thread's interrupt status

            resultHistory.mergeToEnv(env);

            String line;
            try {
                try {
                    line = reader.readLine(prompt, null, (MaskingCallback)null, null);
                    if (line == null) {
                        continue;
                    }
                }
                catch (ParseError ex) {
                    printer.printex("error", ex);
                    history.add(reader.getBuffer().toString());
                    continue;
                }
                catch (UserInterruptException ex) {
                    // gracefully handle ctrl-c when reading a line
                    Thread.interrupted(); // reset the thread's interrupt status
                    continue;
                }

                if (ReplParser.isExitCommand(line)) {
                    quitREPL(history);
                    return; // quit the REPL
                }

                if (DebugAgent.isAttached()) {
                    final DebugAgent agent = DebugAgent.current();

                    if (ReplParser.isCommand(line)) {
                        final String cmd = trimToEmpty(line.trim().substring(1));
                        switch(cmd) {
                            case "attach":
                                printer.println("debug", "The debugger is already attached!");
                                break;

                            case "detach":
                                switchToRegularREPL();
                                break;

                            case "terminate":
                                scriptExec.cancelAsyncScripts();
                                agent.clearBreaks();
                                break;

                            default:
                                debugClient.handleCommand(cmd);
                                break;
                        }
                    }
                    else if (ReplParser.isDroppedVeniceScriptFile(line)) {
                        agent.clearBreaks();
                        handleDroppedFileName(line, env, history, resultHistory, resultPrefix);
                    }
                    else if (agent.hasActiveBreak()) {
                        // run the expression in the context of the break
                        runDebuggerExprAsync(line, debugClient.getEnv());
                    }
                    else {
                        // run the s-expr read from the line reader
                        runScriptAsync(line, resultPrefix, resultHistory);
                    }
                }
                else {
                    if (ReplParser.isCommand(line)) {
                        final String cmd = trimToEmpty(line.trim().substring(1));
                        switch(cmd) {
                            case "reload":
                                env = loadEnv(venice, cli, terminal, out, err, in, venice.isMacroExpandOnLoad());
                                printer.println("system", "reloaded");
                                break;

                            case "restart":
                                if (restartable) {
                                    printer.println("system", "Restarting REPL...");
                                    ReplRestart.restart(
                                            venice.isMacroExpandOnLoad(),
                                            config.getColorMode());
                                    return;
                                }
                                else {
                                    printer.println("error", "This REPL is not restartable!");
                                }
                                break;

                            case "attach":
                                switchToDebugREPL();
                                break;

                            case "detach":
                                printer.println("error", "There is no debugger attached!");
                                break;

                            default:
                                handleReplCommand(cmd, env, terminal, history);
                                break;
                        }
                    }
                    else if (ReplParser.isDroppedVeniceScriptFile(line)) {
                        handleDroppedFileName(line, env, history, resultHistory, resultPrefix);
                    }
                    else {
                        // run the s-expr read from the line reader
                        runScriptSync(line, resultPrefix, resultHistory);
                    }
                }
            }
            catch (ContinueException ex) {
                // ok, just continue
            }
            catch (Exception ex) {
                handleException(ex);
            }
            catch (NoClassDefFoundError ex) {
                printer.printex("error", ex);
            }
        }
    }

    private void switchToRegularREPL() {
        debugClient = null;

        final DebugAgent agent = DebugAgent.current();
        if (agent != null) {
            agent.storeBreakpoints();
            agent.detach();
            DebugAgent.unregister();
            printer.println("debug", "Debugger: detached");
        }
        else {
            printer.println("error", "Debugger: not attached");
            printer.println("debug", "Attach a debuger first using: $attach");
        }
        changePrompt(promptVenice);
    }

    private void switchToDebugREPL() {
        if (DebugAgent.isAttached()) {
            printer.println("debug", "Debugger: already attached");
        }
        else {
            final DebugAgent agent = new DebugAgent();
            DebugAgent.register(agent);
            agent.restoreBreakpoints();
            printer.println("debug", "Debugger: attached");
            debugClient = new ReplDebugClient(
                                agent,
                                printer,
                                Thread.currentThread());
        }
        changePrompt(promptDebug);
    }

    private void quitREPL(final History history) {
        clearCommandHistoryIfRequired(history);
        printer.println("interrupt", " good bye ");
        try { Thread.sleep(1000); } catch(Exception e) {};
    }

    private void runScript(
            final String line,
            final String resultPrefix,
            final ReplResultHistory resultHistory
    ) throws Exception {
        if (hasActiveDebugSession()) {
            printer.println("error", "Debugging session is active! Can only run debug commands.");
        }
        else {
            if (DebugAgent.isAttached()) {
                runScriptAsync(line, resultPrefix, resultHistory);
            }
            else {
                runScriptSync(line, resultPrefix, resultHistory);
            }
        }
    }

    private void runScriptSync(
            final String script,
            final String resultPrefix,
            final ReplResultHistory resultHistory
    ) throws Exception {
        scriptExec.runSync(
                script,
                venice,
                env,
                printer,
                resultPrefix,
                resultHistory,
                this::handleException);
    }

    private void runScriptAsync(
            final String script,
            final String resultPrefix,
            final ReplResultHistory resultHistory
    ) throws Exception {
        scriptExec.runAsync(
                script,
                venice,
                env,
                printer,
                resultPrefix,
                resultHistory,
                this::handleException);
    }

    private void runDebuggerExprAsync(
            final String expr,
            final Env env
    ) {
        scriptExec.runDebuggerExpressionAsync(
                expr,
                venice,
                env,
                printer,
                this::handleException);
    }

    private LineReader createLineReader(
            final Terminal terminal,
            final History history,
            final ReplCompleter completer,
            final ReplParser parser,
            final String secondaryPrompt
    ) {
        return LineReaderBuilder
                .builder()
                .appName("Venice")
                .terminal(terminal)
                .history(history)
                .expander(new NullExpander())
                .completer(completer)
                .highlighter(highlighter)
                .parser(parser)
                .option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
                .variable(LineReader.INDENTATION, 2)
                .variable(LineReader.LIST_MAX, 100)
//              .variable(LineReader.HISTORY_SIZE, 20)
                .variable(LineReader.HISTORY_FILE, HISTORY_FILE)
                // allow syntax highlighting in line buffers upto 5000 characters
                // defaults to 1000
                .variable(LineReader.FEATURES_MAX_BUFFER_SIZE, 5000)
//              .variable(LineReader.HISTORY_FILE_SIZE, 25)
                .build();
    }

    private void handleDroppedFileName(
            final String droppedFileName,
            final Env env,
            final History history,
            final ReplResultHistory resultHistory,
            final String resultPrefix
    ) throws Exception {
        final String file = unescapeDroppedFileName(droppedFileName.trim());

        if (!new File(file).exists()) {
            printer.println("error", String.format("The file \"%s\" does not exist!", file));
            return;
        }

        final List<String> lines = Files.readAllLines(new File(file).toPath());

        if (lines.size() < 20) {
            // if file scripts has less than 20 lines display it on the
            // REPL and and add it to the history for easy modification
            final String script = String.join("\n", lines);
            history.add(script);
            printer.println("stdout", DocForm.highlight(new VncString(script), env).getValue());
        }

        ThreadContext.clearCallStack();

        final String script = String.format("(load-file \"%s\")", file);
        history.add(script);
        runScript(script, resultPrefix, resultHistory);
    }

    private void handleReplCommand(
            final String cmdLine,
            final Env env,
            final Terminal terminal,
            final History history
    ) {
        try {
            final List<String> items = Arrays.asList(cmdLine.split(" +"));
            final String cmd = items.get(0);
            final List<String> args = drop(items, 1);

            if (hasActiveDebugSession()) {
                printer.println("error", "Debugging session is active! Can only run debug commands.");
            }
            else {
                switch(cmd) {
                    case "macroexpand":   handleMacroExpandCommand(env); break;
                    case "me":            handleMacroExpandCommand(env); break;
                    case "":              handleHelpCommand(); break;
                    case "?":             handleHelpCommand(); break;
                    case "help":          handleHelpCommand(); break;
                    case "config":        handleConfigCommand(); break;
                    case "dark":          handleColorModeCommand(ColorMode.Dark); break;
                    case "darkmode":      handleColorModeCommand(ColorMode.Dark); break;
                    case "light":         handleColorModeCommand(ColorMode.Light); break;
                    case "lightmode":     handleColorModeCommand(ColorMode.Light); break;
                    case "restartable":   handleRestartableCommand(); break;
                    case "setup":         handleSetupCommand(venice, env, printer); break;
                    case "classpath":     handleReplClasspathCommand(); break;
                    case "cp":            handleReplClasspathCommand(); break;
                    case "loadpath":      handleLoadPathsCommand(interceptor.getLoadPaths()); break;
                    case "launcher":      handleLauncherCommand(); break;
                    case "app":           handleAppCommand(args, terminal, env); break;
                    case "manifest":      handleAppManifestCommand(args, terminal, env); break;
                    case "env":           handleEnvCommand(args, env); break;
                    case "hist":          handleHistoryCommand(args, terminal, history); break;
                    case "sandbox":       handleSandboxCommand(args, terminal, env); break;
                    case "colors":        handleConfiguredColorsCommand(); break;
                    case "info":          handleInfoCommand(terminal); break;
                    case "highlight":     handleHighlightCommand(args); break;
                    case "java-ex":       handleJavaExCommand(args); break;
                    case "debug":         handleDebugHelpCommand(); break;
                    case "source-pdf":    handleSourcePdfCommand(args); break;
                    case "license":       handleLicenseCommand(args); break;
                    default:              handleInvalidCommand(cmd); break;
                }
            }
        }
        catch(RuntimeException ex) {
            handleFailedCommand(ex);
        }
    }

    private void handleConfigCommand() {
        printer.println("stdout", "Sample REPL configuration. Save it as 'repl.json'");
        printer.println("stdout", "to the REPL's working directory:");
        printer.println();
        printer.println("stdout", ReplConfig.getDefaultClasspathConfig());
    }

    private void handleColorModeCommand(final ColorMode mode) {
        config.switchColorMode(mode);

        if (highlighter != null) {
            highlighter.reloadColors();
        }
    }

    private void handleMacroExpandCommand(final Env env) {
        venice.setMacroExpandOnLoad(true);
        printer.println("system", "Macro expansion enabled");
    }

    private void handleHelpCommand() {
        printer.println("stdout", ReplHelp.COMMANDS);
    }

    private void handleDebugHelpCommand() {
        ReplDebugClient.pringHelp(printer);
    }

    private void handleSetupCommand(
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer
    ) {
        try {
            // on Windows enforce dark mode
            final ColorMode colorMode = config.isColorModeLight() && OSUtils.IS_WINDOWS
                                            ? ColorMode.Dark
                                            : config.getColorMode();

            final String sColorMode = ":" + colorMode.name().toLowerCase();

            final String script =
                String.format(
                    "(do                                     \n" +
                    "  (load-module :repl-setup)             \n" +
                    "  (repl-setup/setup :color-mode %s      \n" +
                    "                    :ansi-terminal %s))   ",
                    sColorMode,
                    ansiTerminal ? "true" : "false");

            venice.RE(script, "user", env);
        }
        catch(Exception ex) {
            printer.printex("error", ex);
            printer.println("error", "REPL setup failed!");
        }
    }

    private void handleLauncherCommand() {
        final String name = ReplConfig.getLauncherScriptName();

        printer.println("stdout", "Sample REPL launcher script. Save it as '" + name + "'");
        printer.println("stdout", "to the REPL's working directory:");
        printer.println();
        printer.println("stdout", ReplConfig.getDefaultClasspathLauncherScript());
    }

    private void handleAppCommand(
            final List<String> params,
            final Terminal terminal,
            final Env env
    ) {
        if (params.size() != 1) {
            printer.println("stdout", ReplHelp.APP);
            return;
        }

        // run the Venice application archive
        final File appArchive = new File(addZipFileExt(params.get(0)));

        if (!appArchive.exists()) {
            printer.println("error", (String.format("App archive '%s' not found!", appArchive)));
            return;
        }

        final IInterceptor oldInterceptor = interceptor;
        final VncKeyword oldRunMode = (VncKeyword)env.getGlobalOrNull(new VncSymbol("*run-mode*"));

        try {
            final VncMap manifest = getAppManifest(appArchive);

            final String appName = Coerce.toVncString(manifest.get(new VncString("app-name"))).getValue();
            final String mainFile = Coerce.toVncString(manifest.get(new VncString("main-file"))).getValue();
            final String mainFileBasename = StringUtil.removeEnd(mainFile, ".venice");

            // Merge the load paths from the command line with the application archive
            final List<File> mergedLoadPaths = new ArrayList<>();
            mergedLoadPaths.add(appArchive.getAbsoluteFile());
            mergedLoadPaths.addAll(interceptor.getLoadPaths().getPaths());
            final ILoadPaths appLoadPaths = LoadPathsFactory.of(
                                                mergedLoadPaths,
                                                interceptor.getLoadPaths().isUnlimitedAccess());

            printer.println("stdout", (String.format("Launching Venice application '%s' ...", appName)));

            reconfigureVenice(
                new AcceptAllInterceptor(appLoadPaths),
                venice.isMacroExpandOnLoad());

            env.removeGlobalSymbol(new VncSymbol("*run-mode*"));
            env.setGlobal(new Var(new VncSymbol("*run-mode*"), RunMode.APP.mode, Var.Scope.Global));

            env.setGlobal(new Var(new VncSymbol("*app-name*"), new VncString(appName), false, Var.Scope.Global));
            env.setGlobal(new Var(new VncSymbol("*app-archive*"), new VncJavaObject(appArchive), false, Var.Scope.Global));

            final String expr = String.format("(do (load-file \"%s\") nil)", mainFileBasename);

            runScriptSync(expr, resultPrefix, null);
        }
        catch (Exception ex) {
            handleException(ex);
        }
        finally {
            reconfigureVenice(
                    oldInterceptor,
                    venice.isMacroExpandOnLoad());

            env.removeGlobalSymbol(new VncSymbol("*run-mode*"));
            env.setGlobal(new Var(new VncSymbol("*run-mode*"), oldRunMode, Var.Scope.Global));

            env.removeGlobalSymbol(new VncSymbol("*app-name*"));
            env.removeGlobalSymbol(new VncSymbol("*app-archive*"));
        }
    }

    private void handleAppManifestCommand(
            final List<String> params,
            final Terminal terminal,
            final Env env
    ) {
        if (params.size() != 1) {
            printer.println("stdout", ReplHelp.APP);
            return;
        }

        // run the Venice application archive
        final File appArchive = new File(addZipFileExt(params.get(0)));

        if (!appArchive.exists()) {
            printer.println("error", (String.format("App archive '%s' not found!", appArchive)));
            return;
        }


        try {
            final VncMap manifest = getAppManifest(appArchive);

            printer.println("stdout", manifest.toString(true));
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    private void handleEnvCommand(
            final List<String> params,
            final Env env
    ) {
        if (params.isEmpty()) {
            printer.println("stdout", ReplHelp.ENV);
            return;
        }
        else if (first(params).equals("print")) {
            if (params.size() == 2) {
                final VncVal val = env.get(new VncSymbol(second(params)));
                printer.println("stdout", venice.PRINT(val));
                return;
            }
        }
        else if (first(params).equals("global")) {
            if (params.size() == 1) {
                printer.println("stdout", EnvUtils.envGlobalsToString(env, null));
                return;
            }
            else if (params.size() == 2) {
                String filter = trimToNull(second(params));
                filter = filter == null ? null : filter.replaceAll("[*]", ".*");
                printer.println("stdout", EnvUtils.envGlobalsToString(env, filter));
                return;
            }
        }

        printer.println("error", "Invalid env command");
    }

    private void handleSandboxCommand(
            final List<String> params,
            final Terminal terminal,
            final Env env
    ) {
        if (params.isEmpty()) {
            terminal.writer().println(ReplHelp.SANDBOX);
            return;
        }

        final String interceptorName = interceptor.getClass().getSimpleName();

        if (params.size() == 1) {
            if (first(params).equals("status")) {
                if (interceptor instanceof AcceptAllInterceptor) {
                    printer.println("stdout", "No sandbox active (" + interceptorName +")");
                    return;
                }
                else if (interceptor instanceof RejectAllInterceptor) {
                    printer.println(
                        "stdout",
                        "Sandbox active (" + interceptorName + "). "
                            + "Rejects all Java calls and default "
                            + "blacklisted Venice functions");
                    return;
                }
                else if (interceptor instanceof SandboxInterceptor) {
                    printer.println("stdout", "Customized sandbox active (" + interceptorName + ")");
                    return;
                }
                else {
                    printer.println("stdout", "Sandbox: " + interceptorName);
                    return;
                }
            }
            else if (first(params).equals("accept-all")) {
                reconfigureVenice(
                    new AcceptAllInterceptor(
                        LoadPathsFactory.of(
                                interceptor.getLoadPaths().getPaths(),
                                true)),
                    venice.isMacroExpandOnLoad());
                return;
            }
            else if (first(params).equals("reject-all")) {
                reconfigureVenice(
                        new RejectAllInterceptor(),
                        venice.isMacroExpandOnLoad());
                return;
            }
            else if (first(params).equals("customized")) {
                reconfigureVenice(
                    new SandboxInterceptor(
                        new SandboxRules(),
                        LoadPathsFactory.of(
                                interceptor.getLoadPaths().getPaths(),
                                true)),
                    venice.isMacroExpandOnLoad());
                return;
            }
            else if (first(params).equals("config")) {
                if (interceptor instanceof AcceptAllInterceptor) {
                    printer.println("stdout", "[accept-all] NO sandbox active");
                    printer.println("stdout", "Java calls:                     No restriction");
                    printer.println("stdout", "Venice functions:               No restriction");
                    printer.println("stdout", "System properties:              No restriction");
                    printer.println("stdout", "System environment variables:   No restriction");
                    return;
                }
                else if (interceptor instanceof RejectAllInterceptor) {
                    printer.println("stdout", "[reject-all] SAFE restricted sandbox");
                    printer.println("stdout", "Java calls:\n   All rejected!");
                    printer.println(
                        "stdout",
                        "Whitelisted Venice modules:\n"
                            + ((RejectAllInterceptor)interceptor)
                                    .getWhitelistedVeniceModules()
                                    .stream()
                                    .map(s -> "   " + s)
                                    .collect(Collectors.joining("\n")));
                    printer.println(
                        "stdout",
                        "Blacklisted Venice functions:\n"
                            + ((RejectAllInterceptor)interceptor)
                                    .getBlacklistedVeniceFunctions()
                                    .stream()
                                    .map(s -> "   " + s)
                                    .collect(Collectors.joining("\n")));
                    printer.println("stdout", "System properties:\n   All rejected!");
                    printer.println("stdout", "System environment variables:\n   All rejected!");
                    return;
                }
                else if (interceptor instanceof SandboxInterceptor) {
                    printer.println("stdout", "[customized] Customized sandbox");
                    printer.println(
                        "stdout",
                        "Sandbox rules:\n" + ((SandboxInterceptor)interceptor).getRules().toString());
                        return;
                }
                else {
                    printer.println("stdout", "[" + interceptorName + "]");
                    printer.println("stdout", "no info");
                    return;
                }
            }
            else if (first(params).equals("fn-group")) {
                printer.println("stdout", "Groups: " +  String.join(", ", SandboxFunctionGroups.getGroups()));
                return;
            }
        }
        else if (params.size() == 2) {
            if (first(params).equals("fn-group")) {
                final String group = second(params);

                if (SandboxFunctionGroups.isValidGroup(group)) {
                    SandboxFunctionGroups
                        .groupFunctionsSorted(group)
                        .forEach(f -> printer.println("stdout", "   " + f));
                }
                else {
                    printer.println(
                            "error",
                            "invalid sandbox function group: " + group +
                            ". Use one of " + String.join(", ", SandboxFunctionGroups.getGroups()));
                }
                return;
            }
            else if (first(params).equals("add-rule")) {
                final String rule = second(params);
                if (!(interceptor instanceof SandboxInterceptor)) {
                    printer.println("system", "rules can only be added to a customized sandbox");
                    return;
                }

                final SandboxRules rules = ((SandboxInterceptor)interceptor).getRules();
                if (rule.startsWith("class:")) {
                    rules.withClasses(rule);
                }
                else if (rule.startsWith("system.property:")) {
                    rules.withSystemProperties(rule);
                }
                else if (rule.startsWith("system.env:")) {
                    rules.withSystemEnvs(rule);
                }
                else if (rule.startsWith("venice:module:")) {
                    rules.withVeniceModules(rule);
                }
                else if (rule.startsWith("blacklist:venice:func:")) {
                    rules.rejectVeniceFunctions(rule);
                }
                else if (rule.startsWith("whitelist:venice:func:")) {
                    rules.whitelistVeniceFunctions(rule);
                }
                else {
                    terminal.writer().println(ReplHelp.SANDBOX);
                    return;
                }

                // activate the change
                reconfigureVenice(
                    new SandboxInterceptor(
                        rules,
                        LoadPathsFactory.of(
                            interceptor.getLoadPaths().getPaths(),
                            true)),
                    venice.isMacroExpandOnLoad());
                return;
            }
        }

        printer.println("error", "invalid sandbox command: " + Arrays.asList(params));
    }

    private void handleHighlightCommand(final List<String> params) {
        if (params.isEmpty()) {
            printer.println("stdout", "Highlighting: " + (highlight ? "on" : "off"));
        }
        else {
            switch(trimToEmpty(first(params))) {
                case "on":
                    highlight = true;
                    if (highlighter != null) highlighter.enable(true);
                    break;

                case "off":
                    highlight = false;
                    if (highlighter != null) highlighter.enable(false);
                    break;

                default:
                    printer.println("error", "Invalid parameter. Use !highlight {on|off}.");
                    break;
            }
        }
    }

    private void handleJavaExCommand(final List<String> params) {
        if (params.isEmpty()) {
            printer.println("stdout", "Java Exceptions: " + (javaExceptions ? "on" : "off"));
        }
        else {
            switch(trimToEmpty(first(params))) {
                case "on":
                    javaExceptions = true;
                    printer.setPrintJavaEx(javaExceptions);
                    printer.println("stdout", "Printing Java exceptions");
                    break;

                case "off":
                    javaExceptions = false;
                    printer.setPrintJavaEx(javaExceptions);
                    printer.println("stdout", "Printing Venice exceptions");
                    break;

                default:
                    printer.println("error", "Invalid parameter. Use !java-ex {on|off}.");
                    break;
            }
        }
    }

    private void handleSourcePdfCommand(final List<String> params) {
        if (params.size() == 1) {
            final String sourceFile = trimToEmpty(first(params));
            final String destDir = ".";
            final String fontDir = replDirs.getFontsDir().getAbsolutePath();

            SourceCodeRenderer.render(sourceFile, destDir, fontDir, true, true);
        }
        else if (params.size() == 2) {
            final String sourceFile = trimToEmpty(first(params));
            final String destDir = trimToEmpty(second(params));
            final String fontDir = replDirs.getFontsDir().getAbsolutePath();

            SourceCodeRenderer.render(sourceFile, destDir, fontDir, true, true);
        }
        else {
            printer.println(
                "error", "Invalid parameter. Use !source-pdf {source-file} {dest-dir}");
        }
    }

    private void handleLicenseCommand(final List<String> params) {
        try {
            if (params.size() == 0) {
                printer.println("stdout", LicenseMgr.loadVeniceLicenseText());
            }
            else if ("all".equals(trimToEmpty(first(params)))) {
                printer.println("stdout", LicenseMgr.loadAll());
            }
            else {
                printer.println(
                        "error", "Invalid parameter. Use !license or !license all");
            }
        }
        catch(Exception ex) {
             printer.println("error", "Failed to display Venice license info");
        }
    }

    private void handleConfiguredColorsCommand() {
        printer.println("default",   "default");
        printer.println("command",   "command");
        printer.println("result",    "result");
        printer.println("stdout",    "stdout");
        printer.println("stderr",    "stderr");
        printer.println("debug",     "debug");
        printer.println("error",     "error");
        printer.println("system",    "system");
        printer.println("interrupt", "interrupt");
    }

    private void handleRestartableCommand() {
        printer.println("stdout", "restartable: " + (restartable ? "yes" : "no"));
    }

    private void handleLoadPathsCommand(final ILoadPaths loadPaths) {
        printer.println("stdout", "Restricted to load paths: " + (loadPaths.isUnlimitedAccess() ? "no" : "yes"));
        printer.println("stdout", "Paths: ");
        loadPaths.getPaths().forEach(p -> printer.println("stdout", "   " + p.getPath()));
    }

    private void handleInfoCommand(final Terminal terminal) {
        final Integer maxColors = terminal.getNumericCapability(Capability.max_colors);
        final Size size = terminal.getSize();
        final String jansiVersion = SystemFunctions.getJansiVersion();

        printer.println("stdout", "Terminal Name:   " + terminal.getName());
        printer.println("stdout", "Terminal Type:   " + terminal.getType());
        printer.println("stdout", "Terminal Size:   " + size.getRows() + "x" + size.getColumns());
        printer.println("stdout", "Terminal Colors: " + maxColors);
        printer.println("stdout", "Terminal Class:  " + terminal.getClass().getSimpleName());
        printer.println("stdout", "Jansi Library:   " + (jansiVersion == null ? "n/a" : jansiVersion));
        printer.println("stdout", "");
        printer.println("stdout", "Color Mode:      " + config.getColorMode().toString().toLowerCase());
        printer.println("stdout", "Highlighting:    " + (config.isSyntaxHighlighting() ? "on" : "off"));
        printer.println("stdout", "Java Exceptions: " + (javaExceptions ? "on" : "off"));
        printer.println("stdout", "Macro Expansion: " + (venice.isMacroExpandOnLoad() ? "on" : "off"));
        printer.println("stdout", "Restartable:     " + (restartable ? "yes" : "no"));
        printer.println("stdout", "Debugger:        " + getDebuggerStatus());
        printer.println("stdout", "");
        printer.println("stdout", "Home dir:        " + replDirs.getHomeDir());
        printer.println("stdout", "Libs dir:        " + replDirs.getLibsDir());
        printer.println("stdout", "Fonts dir:       " + replDirs.getFontsDir());
        printer.println("stdout", "Scripts dir:     " + replDirs.getScriptsDir());
        printer.println("stdout", "");
        printer.println("stdout", "Env TERM:        " + System.getenv("TERM"));
        printer.println("stdout", "Env GITPOD:      " + isRunningOnLinuxGitPod());
        printer.println("stdout", "");
        printer.println("stdout", "OS Arch:         " + System.getProperty("os.arch"));
        printer.println("stdout", "OS Name:         " + System.getProperty("os.name"));
        printer.println("stdout", "OS Version:      " + System.getProperty("os.version"));
        printer.println("stdout", "");
        printer.println("stdout", "Java Version:    " + System.getProperty("java.version"));
        printer.println("stdout", "Java Vendor:     " + System.getProperty("java.vendor"));
        printer.println("stdout", "Java VM Version: " + System.getProperty("java.vm.version"));
        printer.println("stdout", "Java VM Name:    " + System.getProperty("java.vm.name"));
        printer.println("stdout", "Java VM Vendor:  " + System.getProperty("java.vm.vendor"));
    }

    private void handleInvalidCommand(final String cmd) {
        if (ReplDebugClient.isDebugCommand(cmd)) {
            printer.println(
                "error",
                "This debugging command requires an attached debugger! "
                    + "Use the !attach command first.");
        }
        else {
            printer.println("error", "Invalid command");
        }
    }

    private void handleFailedCommand(final Exception ex) {
        printer.println("error", "Failed to handle REPL command");
        printer.println("error", ex.getMessage());
    }


    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            printer.println("stdout", "\nRunning interrupt hooks");
            Thread.interrupted();
            SystemFunctions.runInterruptHooks();
            printer.printex("error", ex);
        }
        else if (ex instanceof SymbolNotFoundException) {
            handleSymbolNotFoundException((SymbolNotFoundException)ex);
        }
        else {
            printer.printex("error", ex);
        }
    }

    private void handleSymbolNotFoundException(final SymbolNotFoundException ex) {
        final VncSymbol sym = new VncSymbol(ex.getSymbol());
        if (sym.hasNamespace()) {
            final String ns = sym.getNamespace();
            if (!Namespaces.isCoreNS(ns)) {
                final boolean nsLoaded = env.getAllGlobalFunctionSymbols()
                                            .stream()
                                            .anyMatch(s -> ns.equals(s.getNamespace()));

                if (!nsLoaded) {
                    printer.println("error", String.format(
                                                "Symbol '%s' not found!\n"
                                                    + "*** Have you loaded the module or file that "
                                                    + "defines the namespace '%s'? ***\n\n",
                                                sym,
                                                sym.getNamespace()));
                }
            }
        }
        printer.printex("error", ex);
    }

    private Env loadEnv(
            final IVeniceInterpreter venice,
            final CommandLineArgs cli,
            final Terminal terminal,
            final PrintStream out,
            final PrintStream err,
            final BufferedReader in,
            final boolean macroexpand
    ) {
        final Env env =
                venice
                    .createEnv(macroexpand, ansiTerminal, RunMode.REPL)
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
                    venice.isMacroExpandOnLoad(), replDirs);
    }

    private void reconfigureVenice(
            final IInterceptor interceptor,
            final boolean macroExpandOnLoad
    ) {
        final DebugAgent agent = DebugAgent.current();

        this.interceptor = interceptor;
        this.venice = new VeniceInterpreter(interceptor);
        this.venice.setMacroExpandOnLoad(macroExpandOnLoad);

        DebugAgent.register(agent);
    }

    private PrintStream createPrintStream(
            final String context,
            final Terminal terminal
    ) {
        return new ReplPrintStream(
                    terminal,
                    ansiTerminal ? config.getColor(context) : null);
    }

    private BufferedReader createBufferedReader(
            final String context,
            final Terminal terminal
    ) {
        return new BufferedReader(terminal.reader());
    }


    private String getTerminalInfo() {
        if (ansiTerminal) {
            switch(config.getColorMode()) {
                case Light:
                    return "Using Ansi terminal (light color mode turned on)\n"
                            + "Use the commands !lightmode or !darkmode to adapt "
                            + "to the terminal's colors";
                case Dark:
                    return "Using Ansi terminal (dark color mode turned on)\n"
                            + "Use the commands !lightmode or !darkmode to adapt "
                            + "to the terminal's colors";
                case None:
                default:
                    return "Using Ansi terminal (colors turned off, turn on with option '-colors')";
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

    private void setupRepl(
            final CommandLineArgs cli,
            final IVeniceInterpreter venice,
            final Env env,
            final TerminalPrinter printer
    ) {
        if (cli.switchPresent("-setup")) {
            handleSetupCommand(venice, env, printer);
            return; // we stop here
        }
    }

    private void handleReplClasspathCommand() {
        printer.println("stdout", "REPL classpath:");
        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
              .sorted()
              .forEach(f -> printer.println("stdout", "  " + f));

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl instanceof DynamicClassLoader2) {
            printer.println("stdout", "REPL dynamic classpath:");
            Arrays.stream(((URLClassLoader)cl).getURLs())
                  .map(u -> u.toString())
                  .sorted()
                  .forEach(u -> printer.println("stdout", "  " + u));
        }
    }

    private boolean isSetupMode(final CommandLineArgs cli) {
        return cli.switchPresent("-setup");
    }

    private boolean isRestartable(final CommandLineArgs cli) {
        return cli.switchPresent("-restartable");
    }

    private boolean isMacroexpand(final CommandLineArgs cli) {
        return cli.switchPresent("-macroexpand");
    }

    private boolean isRunningOnLinuxGitPod() {
        return  "Linux".equals(System.getProperty("os.name"))
                    && System.getenv("GITPOD_REPO_ROOT") != null;
    }

    private void clearCommandHistoryIfRequired(final History history) {
        if (config.isClearCommandHistoryOnExit()) {
            clearCommandHistory(history);
        }
    }

    private void clearCommandHistory(final History history) {
        try {
            printer.println("stdout", "Cleared REPL command history");
            history.purge();
        }
        catch(IOException ex) {
            printer.println("stderr", "Failed to clear REPL command history!");
        }
    }

    private void handleHistoryCommand(
            final List<String> params,
            final Terminal terminal,
            final History history
    ) {
        if (params.isEmpty()) {
            printer.println("stdout", String.format(
                                        "History: size: %d, first: %d, last: %d, index: %d",
                                        history.size(), history.first(),
                                        history.last(), history.index()));
        }
        else if (first(params).equals("clear")) {
            clearCommandHistory(history);
        }
        else if (first(params).equals("load")) {
            try {
                history.load();
            }
            catch(IOException ex) {
                printer.println("stderr", "Failed to reload REPL command history!");
            }
        }
        else if (first(params).equals("log")) {
            final Logger logger = Logger.getLogger("org.jline");
            logger.setLevel(Level.INFO);
            for(Handler h : logger.getHandlers()) logger.removeHandler(h);
            logger.addHandler(new ReplJLineLogHandler(printer));
            printer.println("stdout", "Enabled REPL JLine logging");
        }
        else {
            printer.println("error", "Invalid hist command");
        }
    }

    private String unescapeDroppedFileName(final String fileName) {
        // When dropping a file to the REPL that has special characters (space,
        // asteriks,  ...) in the filename the underlying OS shell is
        // escaping these characters.
        // E.g.:  "test\ 1.venice", "test\?1.venice"

        final String osType = osType();

        if ("windows".equals(osType)) {
            return fileName;
        }
        else {
            return fileName.replace("\\", "");
        }
    }

    private static String osType() {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return "windows";
        }
        else if (osName.startsWith("Mac OS X")) {
            return "mac-osx";
        }
        else if (osName.startsWith("Linux")) {
            return "linux";
        }
        else {
            return "unknown";
        }
    }

    private boolean hasActiveDebugSession() {
        final DebugAgent agent = DebugAgent.current();
        return agent != null && agent.hasActiveBreak();
    }

    private String getDebuggerStatus() {
        return DebugAgent.isAttached() ? "attached" : "not attached";
    }

    private void changePrompt(final String prompt) {
        this.prompt = prompt;
        this.secondaryPrompt = ansiTerminal
                                    ? StringUtil.repeat(' ', prompt.length())
                                    : "";

    }

    private String addZipFileExt(final String s) {
        return s.endsWith(".zip") ? s : s + ".zip";
    }

    private VncMap getAppManifest(final File app) {
        if (app.exists()) {
            try {
                final String manifest = ZipFileSystemUtil.loadTextFileFromZip(
                                            app,
                                            new File("MANIFEST.MF"),
                                            CharsetUtil.charset("UTF-8"));
                return Coerce.toVncMap(JsonFunctions.read_str.apply(VncList.of(new VncString(manifest))));
            }
            catch (Exception ex) {
                throw new VncException(String.format(
                            "Failed to load manifest from Venice application archive '%s'.",
                            app.getPath()));
            }
        }
        else {
            throw new VncException(String.format(
                        "The Venice application archive '%s' does not exist",
                        app.getPath()));
        }
    }



    private final static String HISTORY_FILE = ".repl.history";

    private Terminal terminal;

    private ReplConfig config;
    private IInterceptor interceptor;
    private volatile IVeniceInterpreter venice;
    private Env env;
    private TerminalPrinter printer;
    private ReplHighlighter highlighter;
    private boolean ansiTerminal = false;
    private boolean highlight = true;
    private boolean javaExceptions = false;
    private boolean restartable = false;
    private String promptVenice;
    private String promptDebug;
    private String prompt;
    private String secondaryPrompt;
    private String resultPrefix = "=> ";
    private ReplDebugClient debugClient = null;
    private ReplDirs replDirs;

    private final ScriptExecuter scriptExec = new ScriptExecuter();
}
