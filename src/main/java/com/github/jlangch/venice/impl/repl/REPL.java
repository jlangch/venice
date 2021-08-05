/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.SymbolNotFoundException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.debug.DebugAgent;
import com.github.jlangch.venice.impl.docgen.runtime.DocForm;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.javainterop.DynamicClassLoader2;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.ILoadPaths;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class REPL {
	
	public REPL(final IInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void run(final String[] args) {
		JavaInterop.register(interceptor);

		final CommandLineArgs cli = new CommandLineArgs(args);

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
							"Please download the jar artifact 'org.fusesource.jansi:jansi:2.3.4' \n" +
							"from a Maven repo and put it on the classpath.                      \n" +
							"--------------------------------------------------------------------\n\n");
				}
			}
			
			System.out.println("Venice REPL: V" + Venice.getVersion() + (setupMode ? " (setup mode)": ""));
			System.out.println("Loading configuration from " + config.getConfigSource());
			System.out.println(getTerminalInfo());
			if (macroexpand) {
				System.out.println("Macro expansion enabled");
			}
			if (!setupMode) {
				System.out.println("Type '!' for help.");
			}
						
			repl(cli, macroexpand);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
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
		
		final Terminal terminal = builder.build();
	 
		terminal.handle(Signal.INT, signal -> mainThread.interrupt());
       
		final PrintStream out = createPrintStream("stdout", terminal);
		final PrintStream err = createPrintStream("stderr", terminal);		
		final BufferedReader in = createBufferedReader("stdin", terminal);

		printer = new TerminalPrinter(config, terminal, ansiTerminal, false);
		
		venice = new VeniceInterpreter(interceptor);
		// load the core functions without macro expansion! ==> check this!
		env = loadEnv(venice, cli, terminal, out, err, in, false);	
		venice.setMacroExpandOnLoad(macroexpand, env);

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
			ThreadLocalMap.clearCallStack(); // clear the Venice callstack
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
				
				if (ReplParser.isCommand(line)) {
					final String cmd = trimToEmpty(line.trim().substring(1));
					if (cmd.equals("quit") || cmd.equals("q") || cmd.equals("exit") || cmd.equals("e")) {
						quitREPL( history);
						return; // quit the REPL
					}
				}
				
				if (DebugAgent.isAttached()) {
					final DebugAgent agent = DebugAgent.current();
					
					if (ReplParser.isCommand(line)) {
						final String cmd = trimToEmpty(line.trim().substring(1));
						switch(cmd) {
							case "detach":
								switchToRegularREPL();
								break;
								
							case "terminate":
								scriptExec.cancelAsyncScript();
								break;
								
							default:
								new ReplDebugClient(
										agent, 
										printer, 
										Thread.currentThread()
									).handleCommand(cmd);
								break;
						}
					}
					else if (ReplParser.isDroppedVeniceScriptFile(line)) {
						handleDroppedFileName(line, env, history, resultHistory, resultPrefix);
					}
					else if (DebugAgent.current().hasBreak()) {
						// run the expression in the context of the break
						runDebuggerExprAsync(line, agent.getBreak().getEnv());
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
									printer.println("system", "restarting...");
									ReplRestart.write(
										venice.isMacroExpandOnLoad(),
										config.getColorMode());
									System.exit(RESTART_EXIT_CODE);
									return;
								}
								else {
									printer.println("error", "This REPL is not restartable!");
								}
								break;

							case "attach":
								switchToDebugREPL();
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
		}
	}
	
	private void switchToRegularREPL() {
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
				.variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
				.variable(LineReader.INDENTATION, 2)
				.variable(LineReader.LIST_MAX, 100)
//				.variable(LineReader.HISTORY_SIZE, 20)
				.variable(LineReader.HISTORY_FILE, HISTORY_FILE)
//				.variable(LineReader.HISTORY_FILE_SIZE, 25)
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
		
		final List<String> lines = Files.readAllLines(new File(file).toPath());
		
		if (lines.size() < 20) {
			// if file scripts has less than 20 lines display it on the
			// REPL and and add it to the history for easy modification
			final String script = String.join("\n", lines);
			history.add(script);
			printer.println("stdout", DocForm.highlight(new VncString(script), env).getValue());
		}

		ThreadLocalMap.clearCallStack();

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
					case "macroexpand": handleMacroExpandCommand(env); break;
					case "me":          handleMacroExpandCommand(env); break;
					case "":            handleHelpCommand(); break;
					case "?":           handleHelpCommand(); break;
					case "help":        handleHelpCommand(); break;
					case "config":      handleConfigCommand(); break;
					case "dark":   		handleColorModeCommand(ColorMode.Dark); break;
					case "darkmode":    handleColorModeCommand(ColorMode.Dark); break;
					case "light":    	handleColorModeCommand(ColorMode.Light); break;
					case "lightmode":   handleColorModeCommand(ColorMode.Light); break;
					case "restartable": handleRestartableCommand(); break;
					case "setup":       handleSetupCommand(venice, env, Minimal, printer); break;
					case "setup-ext":   handleSetupCommand(venice, env, Extended, printer); break;
					case "classpath":   handleReplClasspathCommand(); break;
					case "cp":          handleReplClasspathCommand(); break;
					case "loadpath":    handleLoadPathsCommand(interceptor.getLoadPaths()); break;
					case "launcher":    handleLauncherCommand(); break;
					case "env":         handleEnvCommand(args, env); break;
					case "hist":        handleHistoryCommand(args, terminal, history); break;
					case "sandbox":     handleSandboxCommand(args, terminal, env); break;
					case "colors":      handleConfiguredColorsCommand(); break;
					case "info":        handleInfoCommand(terminal); break;
					case "highlight":   handleHighlightCommand(args); break;
					case "java-ex":     handleJavaExCommand(args); break;
					default:            handleInvalidCommand(cmd); break;
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
		
		env.setGlobal(new Var(
				new VncSymbol("*repl-color-theme*"), 
				new VncKeyword(mode.name().toLowerCase()),
				true));

	}
	
	private void handleMacroExpandCommand(final Env env) {
		venice.setMacroExpandOnLoad(true, env);
		printer.println("system", "Macro expansion enabled");
	}

	private void handleHelpCommand() {
		printer.println("stdout", HELP);
	}

	private void handleSetupCommand(
			final IVeniceInterpreter venice, 
			final Env env, 
			final SetupMode setupMode,
			final TerminalPrinter printer
	) {
		try {
			// on Windows enforce dark mode
			final ColorMode colorMode = config.isColorModeLight() && OSUtils.IS_WINDOWS
											? ColorMode.Dark
											: config.getColorMode();
			
			final String sSetupMode = ":" + setupMode.name().toLowerCase();
			final String sColorMode = ":" + colorMode.name().toLowerCase();
			
			final String script = 
				String.format(
					"(do                                     \n" +
					"  (load-module :repl-setup)             \n" +
					"  (repl-setup/setup :setup-mode %s      \n" +
					"                    :color-mode %s      \n" +
					"                    :ansi-terminal %s))   ",
					sSetupMode,
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

	private void handleEnvCommand(
			final List<String> params,
			final Env env
	) {
		if (params.isEmpty()) {
			printer.println("stdout", HELP_ENV);
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
				printer.println("stdout", envGlobalsToString(env));
				return;
			}
			else if (params.size() == 2) {
				String filter = trimToNull(second(params));
				filter = filter == null ? null : filter.replaceAll("[*]", ".*");
				printer.println("stdout", envGlobalsToString(env, filter));
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
			terminal.writer().println(HELP_SANDBOX);
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
					printer.println("stdout", "All Java calls accepted, no Venice calls rejected");
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
		}
		else if (params.size() == 2) {
			if (first(params).equals("add-rule")) {
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
				else {
					terminal.writer().println(HELP_SANDBOX);
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
	
	private void handleConfiguredColorsCommand() {
		printer.println("default",   "default");
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
		printer.println("stdout", "Terminal Name:   " + terminal.getName());
		printer.println("stdout", "Terminal Type:   " + terminal.getType());
		printer.println("stdout", "Terminal Size:   " + size.getRows() + "x" + size.getColumns());
		printer.println("stdout", "Terminal Colors: " + maxColors);
		printer.println("stdout", "Terminal Class:  " + terminal.getClass().getSimpleName());
		printer.println("stdout", "");
		printer.println("stdout", "Color Mode:      " + config.getColorMode().toString().toLowerCase());
		printer.println("stdout", "Highlighting:    " + (config.isSyntaxHighlighting() ? "on" : "off"));
		printer.println("stdout", "Java Exceptions: " + (javaExceptions ? "on" : "off"));
		printer.println("stdout", "Macro Expansion: " + (venice.isMacroExpandOnLoad() ? "on" : "off"));
		printer.println("stdout", "Restartable:     " + (restartable ? "yes" : "no"));
		printer.println("stdout", "Debugger:        " + getDebuggerStatus());
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
		printer.println("error", "Invalid command");
	}

	private void handleFailedCommand(final Exception ex) {
		printer.println("error", "Failed to handle command");
		printer.println("error", ex.getMessage());
	}

	
	private void handleException(final Exception ex) {
		if (ex instanceof SymbolNotFoundException) {
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
								 	false))
					.setGlobal(new Var(
									new VncSymbol("*repl-color-theme*"), 
									new VncKeyword(config.getColorMode().name().toLowerCase()),
									true))
					.setStdoutPrintStream(out)
					.setStderrPrintStream(err)
					.setStdinReader(in);
		
		return ReplFunctions.register(env, terminal, config);
	}
	
	private void reconfigureVenice(
			final IInterceptor interceptor,
			final boolean macroExpandOnLoad
	) {
		final DebugAgent agent = DebugAgent.current();
		
		this.interceptor = interceptor; 
		this.venice = new VeniceInterpreter(interceptor);
		this.venice.setMacroExpandOnLoad(macroExpandOnLoad, env);
		
		DebugAgent.register(agent);
	}

	private String envGlobalsToString(final Env env) {
		return new StringBuilder()
					.append(formatGlobalVars(
								env.getAllGlobalSymbols(), 
								null))
					.toString();
	}
	
	private String envGlobalsToString(final Env env, final String regexFilter) {
		return new StringBuilder()
					.append(formatGlobalVars(
								env.getAllGlobalSymbols(), 
								regexFilter))
					.toString();
	}

	private String formatGlobalVars(
			final Map<VncSymbol,Var> vars, 
			final String regexFilter
	) {
		return vars.values()
				   .stream()
				   .sorted((a,b) -> a.getName().getName().compareTo(b.getName().getName()))
				   .filter(v -> regexFilter == null 
				   					? true 
				   					: v.getName().getName().matches(regexFilter))
				   .map(v -> String.format(
								"%s (:%s)", 
								v.getName().getName(),
								formatGlobalVarType(v).getValue()))
				   .collect(Collectors.joining("\n"));
	}
	
	public VncKeyword formatGlobalVarType(final Var var_) {
		final VncVal val = var_.getVal();
		if (Types.isVncJavaObject(val)) {
			final VncJavaObject vJava = (VncJavaObject)val;
			if (vJava.getDelegateFormalType() != null) {
				return new VncKeyword(vJava.getDelegateFormalType().getName());
			}
			else {
				return new VncKeyword(vJava.getDelegate().getClass().getName());
			}
		}
		else {
			return Types.getType(val);
		}
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
		if (cli.switchPresent("-setup-ext") || cli.switchPresent("-setup-extended")) {
			handleSetupCommand(venice, env, Extended, printer);
			return; // we stop here
		}
		else if (cli.switchPresent("-setup")) {
			handleSetupCommand(venice, env, Minimal, printer);
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
		return cli.switchPresent("-setup") 
				|| cli.switchPresent("-setup-ext") 
				|| cli.switchPresent("-setup-extended");
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
		return agent != null && agent.hasBreak();
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
	
	
	public static enum SetupMode { Minimal, Extended };

	private static final SetupMode Minimal = SetupMode.Minimal;
	private static final SetupMode Extended = SetupMode.Extended;

	private final static String HELP =
			"Venice REPL: V" + Venice.getVersion() + "\n\n" +
			"Commands: \n" +
			"  !reload      reload Venice environment\n" +
			"  !restart     restart the REPL.\n" +
			"               note: the REPL launcher script must support\n" +
			"                     REPL restarting.\n" +
			"  !, !?, !help help\n" +
			"  !darkmode    switch to Venice's dark color theme\n" +
			"  !lightmode   switch to Venice's light color theme\n" +
			"  !info        show REPL setup context data\n" +
			"  !config      show a sample REPL config\n" +
			"  !classpath   show the REPL classpath\n" +
			"  !loadpath    show the REPL loadpath\n" +
			"  !highlight   turn highlighting dynamically on or off\n" +
			"                 !highlight {on/off}\n" +
			"  !macroexpand enable macro expansion while loading\n" +
			"               files and modules. \n" +
			"               This can speed-up script execution by\n" +
			"               a factor 3 or 5 and even more with\n" +
			"               complex code!\n" +
			"  !env         print env symbols:\n" +
			"                 !env print {symbol-name}\n" +
			"                 !env global\n" +
			"                 !env global io/*\n" +
			"                 !env global *file*\n" +
			"  !sandbox     sandbox\n" +	
			"                 !sandbox status\n" +
			"                 !sandbox config\n" +
			"                 !sandbox accept-all\n" +
			"                 !sandbox reject-all\n" +
			"                 !sandbox customized\n" +
			"                 !sandbox add-rule rule\n" +
			"  !java-ex     print Java exception\n" +
			"                 !java-ex\n" +
			"                 !java-ex {on/off}\n" +
			"  !hist clear  clear the history\n" +
			"  !quit, !q    quit the REPL\n\n" +
			"Drag&Drop: \n" +
			"  Scripts can be dragged to the REPL. Upon pressing [return]\n" +
			"  the  REPL loads the script through the dropped absolute or\n" +
			"  relative filename. If the script has less than 20 lines it's\n" +
			"  source is displayed.\n\n" +
			"History: \n" +
			"  A history of the last three result values is kept by\n" +
			"  the REPL, accessible through the symbols `*1`, `*2`, `*3`,\n" +
			"  and `**`. E.g. (printl *1)\n\n" +
			"Shortcuts:\n" +
			"  ctrl-A   move the cursor to the start\n" +
			"  ctrl-C   stop the running command, cancel a multi-line\n" +
			"           edit, or break out of the REPL\n" +
			"  ctrl-E   move the cursor to the end\n" +
			"  ctrl-K   remove the text after the cursor and store it\n" +
			"           in a cut-buffer\n" +
			"  ctrl-L   clear the screen\n" +
			"  ctrl-Y   yank the text from the cut-buffer\n" +
			"  ctrl-_   undo\n";

	private final static String HELP_ENV =
			"Please choose from:\n" +
			"   !env print {symbol-name}\n" +
			"   !env global\n" +
			"   !env global io/*\n" +
			"   !env global *file*\n";

	private final static String HELP_SANDBOX =
			"Please choose from:\n" +
			"   !sandbox status\n" +
			"   !sandbox config\n" +
			"   !sandbox accept-all\n" +
			"   !sandbox reject-all\n" +
			"   !sandbox customized\n" +
			"   !sandbox add-rule class:java.lang.Math:*\n" +
			"   !sandbox add-rule system.property:os.name\n" +
			"   !sandbox add-rule blacklist:venice:func:io/exists-dir?\n" +
			"   !sandbox add-rule blacklist:venice:func:*io*\n" +
			"   !sandbox add-rule venice:module:shell\n";

	private final static int RESTART_EXIT_CODE = 99;
	
	private final static String HISTORY_FILE = ".repl.history";
	

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

	private final ScriptExecuter scriptExec = new ScriptExecuter();
}
