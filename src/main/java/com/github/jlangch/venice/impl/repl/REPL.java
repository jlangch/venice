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
import com.github.jlangch.venice.impl.Namespaces;
import com.github.jlangch.venice.impl.RunMode;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.javainterop.DynamicClassLoader2;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.repl.ReplConfig.ColorMode;
import com.github.jlangch.venice.impl.specialforms.DocForm;
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
		final CommandLineArgs cli = new CommandLineArgs(args);

		try {
			config = ReplConfig.load(cli);

			final boolean setupMode = isSetupMode(cli);

			final Level jlineLogLevel = config.getJLineLogLevel();
			if (jlineLogLevel != null) {
				Logger.getLogger("org.jline").setLevel(jlineLogLevel);
			}

			restartable = cli.switchPresent("-restartable");

			final String jansiVersion = config.getJansiVersion();

			final boolean dumbTerminal = (OSUtils.IS_WINDOWS && (jansiVersion == null))
											|| cli.switchPresent("-dumb") 
											|| config.isJLineDumbTerminal();
			
			ansiTerminal = !dumbTerminal;

			if (OSUtils.IS_WINDOWS) {
				if (jansiVersion != null) {
					System.out.println("Using Jansi V" + jansiVersion);
				}
				else if (!setupMode) {
					System.out.print(
							"--------------------------------------------------------------------\n" +
							"The Venice REPL requires the jansi library on Windows.              \n" +
							"Please download the jar artifact 'org.fusesource.jansi:jansi:1.18'  \n" +
							"from a Maven repo and put it on the classpath.                      \n" +
							"--------------------------------------------------------------------\n\n");
				}
			}
			
			System.out.println("Loading configuration from " + config.getConfigSource());
			System.out.println(getTerminalInfo());
			System.out.println("Venice REPL: V" + Venice.getVersion() + (setupMode ? " (setup mode)": ""));
			if (cli.switchPresent("-macroexpand")) {
				System.out.println("Macro expansion enabled");
			}
			if (!setupMode) {
				System.out.println("Type '!' for help.");
			}
			
			repl(cli);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	private void repl(final CommandLineArgs cli) throws Exception {
		final String prompt = config.getPrompt();
		final String secondaryPrompt = ansiTerminal ? config.getSecondaryPrompt() : "";
		final String resultPrefix = config.getResultPrefix();
		
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
		
		highlighter = config.isSyntaxHighlighting()
						? new ReplHighlighter(config)
						: null;
		
		Env env = loadEnv(cli, out, err, in, false);		
		venice.setMacroexpandOnLoad(cli.switchPresent("-macroexpand"), env);
		if (allowDynamicClassLoader) {
			mainThread.setContextClassLoader(new DynamicClassLoader2());
		}
		
		final ReplParser parser = new ReplParser(venice);
		parser.setEscapeChars(new char[0]);  // leave the char escape handling to Venice
		
		final ReplCompleter completer = new ReplCompleter(
												venice, 
												env, 
												interceptor.getLoadPaths().getPaths());
		
		final History history = new DefaultHistory();
		
		final LineReader reader = createLineReader(
									terminal,
									history,
									completer,
									parser,
									secondaryPrompt);
		
		final ReplResultHistory resultHistory = new ReplResultHistory(3);
		
		if (cli.switchPresent("-setup-ext") || cli.switchPresent("-setup-extended")) {
			handleSetupCommand(venice, env, SetupMode.Extended, printer);
			return; // we stop here
		}
		else if (cli.switchPresent("-setup")) {
			handleSetupCommand(venice, env, SetupMode.Minimal, printer);
			return; // we stop here
		}

		if (!runLoadFile(config.getLoadFile(), env, resultPrefix)) {
			return; // stop REPL
		}

		highlight = highlighter != null;
				
		// REPL loop
		while (true) {
			resultHistory.mergeToEnv(env);
			
			String line;
			try {
				Thread.interrupted(); // reset the thread's interrupt status
				
				try {
					line = reader.readLine(prompt, null, (MaskingCallback)null, null);
				}
				catch (ParseError ex) {
					printer.printex("error", ex);
					history.add(reader.getBuffer().toString());
					line = null;
				}
				catch (UserInterruptException ex) {
					// gracefully handle ctrl-c when reading a line
					Thread.interrupted(); // reset the thread's interrupt status
					line = null;
				}
				
				if (line != null) { 
					if (ReplParser.isCommand(line)) {
						final String cmd = StringUtil.trimToEmpty(line.trim().substring(1));
						if (cmd.equals("reload")) {
							env = loadEnv(cli, out, err, in, venice.isMacroexpandOnLoad());
							// Resetting the dynamic classloader is NOT working properly!
							// thus libraries cannot be reloaded!
							// if (allowDynamicClassLoader) {
							// 	// create a new context class loader
							// 	mainThread.setContextClassLoader(new DynamicClassLoader2());
							// }
							printer.println("system", "reloaded");
						}
						else if (cmd.equals("restart")) {
							if (restartable) {
								printer.println("system", "restarting...");
								System.exit(RESTART_EXIT_CODE);
							}
							else {
								printer.println("error", "The REPL is not restartable!");
							}
						}
						else if (cmd.equals("restartable")) {
							printer.println("stdout", "restartable: " + (restartable ? "yes" : "no"));
						}
						else if (cmd.equals("activate-class-loader")) {
							if (!allowDynamicClassLoader) {
								allowDynamicClassLoader = true;
								// create a new context class loader
								mainThread.setContextClassLoader(new DynamicClassLoader2());
								printer.println("system", "dynamic class loader activated");
							}
							else {
								printer.println("system", "dynamic class loader already activated");
							}
						}
						else if (isExitCommand(cmd)) {
							if (config.isClearCommandHistoryOnExit()) {
								clearCommandHistory(history);
							}
							printer.println("interrupt", " good bye ");
							Thread.sleep(1000);
							break; // quit the REPL
						}
						else {
							handleCommand(cmd, env, terminal, history);
						}
					}
					else if (ReplParser.isDroppedVeniceScriptFile(line)) {
						final String fileName = unescapeDroppedFileName(line.trim());
						final List<String> lines = Files
													.readAllLines(new File(fileName).toPath())
													.stream()
													.filter(l -> !l.startsWith(";;;;"))
													.collect(Collectors.toList());
						String script = null;
						if (lines.size() < 20) {
							// file scripts with less than 20 lines, treat them as if they have 
							// been typed to allow editing it
							script = String.join("\n", lines);
							printer.println("stdout", DocForm.highlight(new VncString(script), env).getValue());
						}
						else {
							script = String.format("(load-file \"%s\")", line.trim());
						}
						history.add(script);
						ThreadLocalMap.clearCallStack();
						final VncVal result = venice.RE(script, "user", env);
						if (result != null) {
							printer.println("result", resultPrefix + venice.PRINT(result));
							resultHistory.add(result);
						}
					}
					else {
						// run the s-expr read from the line reader
						ThreadLocalMap.clearCallStack();
						final VncVal result = venice.RE(line, "user", env);
						if (result != null) {
							printer.println("result", resultPrefix + venice.PRINT(result));
							resultHistory.add(result);
						}
					}
				}
			} 
			catch (ContinueException ex) {
				// ok, just continue
			}
			catch (SymbolNotFoundException ex) {
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
															+ "Have you loaded the module or file that "
															+ "defines the namespace '%s'?\n\n",
														sym,
														sym.getNamespace()));
						}
					}
				}
				printer.printex("error", ex);
			}
			catch (Exception ex) {
				printer.printex("error", ex);
			}
		}
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
	
	private void handleCommand(
			final String cmd, 
			final Env env, 
			final Terminal terminal,
			final History history
	) {
		try {
			if (cmd.equals("macroexpand") || cmd.equals("me")) {
				venice.setMacroexpandOnLoad(true, env);
				printer.println("system", "Macro expansion enabled");
			}
			else if (cmd.isEmpty() || cmd.equals("?") || cmd.equals("help")) {
				printer.println("stdout", HELP);
			}
			else if (cmd.equals("config")) {
				handleConfigCommand();
			}
			else if (cmd.equals("setup")) {
				handleSetupCommand(venice, env, SetupMode.Minimal, printer);
			}
			else if (cmd.equals("setup-ext")) {
				handleSetupCommand(venice, env, SetupMode.Extended, printer);
			}
			else if (cmd.equals("classpath")) {
				handleReplClasspathCommand();
			}
			else if (cmd.equals("loadpath")) {
				printLoadPaths(interceptor.getLoadPaths());
			}
			else if (cmd.equals("launcher")) {
				handleLauncherCommand();
			}
			else if (cmd.equals("env")) {
				handleEnvCommand(new String[0], env);
			}
			else if (cmd.startsWith("env ")) {
				final String[] params = StringUtil.trimToEmpty(cmd.substring(3)).split(" +");
				handleEnvCommand(params, env);
			}
			else if (cmd.equals("hist")) {
				handleHistoryCommand(new String[0], terminal, history);
			}
			else if (cmd.startsWith("hist ")) {
				final String[] params = StringUtil.trimToEmpty(cmd.substring(4)).split(" +");
				handleHistoryCommand(params, terminal, history);
			}
			else if (cmd.equals("sandbox")) {
				handleSandboxCommand(new String[0], terminal, env);
			}
			else if (cmd.startsWith("sandbox ")) {
				final String[] params = StringUtil.trimToEmpty(cmd.substring(7)).split(" +");
				handleSandboxCommand(params, terminal, env);
			}
			else if (cmd.equals("colors")) {
				printConfiguredColors();
			}
			else if (cmd.equals("info")) {
				printInfo(terminal);
			}
			else if (cmd.startsWith("highlight")) {
				if (cmd.equals("highlight")) {
					printer.println("stdout", "Highlighting: " + (highlight ? "on" : "off"));
				}
				else {
					final String param = StringUtil.trimToEmpty(cmd.substring("highlight".length()));
					if ("on".equals(param)) {
						highlight = true;
						if (highlighter != null) highlighter.enable(true);
					}
					else if ("off".equals(param)) {
						highlight = false;
						if (highlighter != null) highlighter.enable(false);
					}
					else {
						printer.println("error", "Invalid parameter. Use !highlight {on|off}.");
					}
				}
			}
			else if (cmd.startsWith("java-ex")) {
				if (cmd.equals("java-ex")) {
					printer.println("stdout", "Java Exceptions: " + (javaExceptions ? "on" : "off"));
				}
				else {
					final String param = StringUtil.trimToEmpty(cmd.substring("java-ex".length()));
					if ("on".equals(param)) {
						javaExceptions = true;
						printer.setPrintJavaEx(javaExceptions);
						printer.println("stdout", "Printing Java exceptions");
					}
					else if ("off".equals(param)) {
						javaExceptions = false;
						printer.setPrintJavaEx(javaExceptions);
						printer.println("stdout", "Printing Venice exceptions");
					}
					else {
						printer.println("error", "Invalid parameter. Use !java-ex {on|off}.");
					}
				}
			}
			else {	
				printer.println("error", "Invalid command");
			}
		}
		catch(RuntimeException ex) {
			printer.println("error", "Failed to handle command");
			printer.println("error", ex.getMessage());
		}
	}

	private void handleConfigCommand() {
		printer.println("stdout", "Sample REPL configuration. Save it as 'repl.json'");
		printer.println("stdout", "to the REPL's working directory:");
		printer.println();
		printer.println("stdout", ReplConfig.getDefaultClasspathConfig());
	}

	private void handleSetupCommand(
			final VeniceInterpreter venice, 
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
			final String[] params,
			final Env env
	) {
		if (params.length == 0) {
			printer.println("stdout", HELP_ENV);
			return;
		}
		else if (params[0].equals("print")) {
			if (params.length == 2) {
				final VncVal val = env.get(new VncSymbol(params[1]));
				printer.println("stdout", venice.PRINT(val));
				return;
			}
		}
		else if (params[0].equals("global")) {
			if (params.length == 1) {
				printer.println("stdout", envGlobalsToString(env));
				return;
			}
			else if (params.length == 2) {
				String filter = StringUtil.trimToNull(params[1]);
				filter = filter == null ? null : filter.replaceAll("[*]", ".*");
				printer.println("stdout", envGlobalsToString(env, filter));
				return;
			}
		}
				
		printer.println("error", "Invalid env command");					
	}

	private void handleSandboxCommand(
			final String[] params,
			final Terminal terminal,
			final Env env
	) {
		if (params.length == 0) {
			terminal.writer().println(HELP_SANDBOX);
			return;
		}

		final String interceptorName = interceptor.getClass().getSimpleName();
			
		if (params.length == 1) {
			if (params[0].equals("status")) {
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
			else if (params[0].equals("accept-all")) {
				activate(
					new AcceptAllInterceptor(
						LoadPathsFactory.of(
								interceptor.getLoadPaths().getPaths(), 
								true)));
				return;
			}
			else if (params[0].equals("reject-all")) {
				activate(new RejectAllInterceptor());
				return;			
			}
			else if (params[0].equals("customized")) {
				activate(
					new SandboxInterceptor(
						new SandboxRules(),
						LoadPathsFactory.of(
								interceptor.getLoadPaths().getPaths(), 
								true)));
				return;			
			}
			else if (params[0].equals("config")) {
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
		else if (params.length == 2) {
			if (params[0].equals("add-rule")) {
				final String rule = params[1];
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
				activate(
					new SandboxInterceptor(
						rules,
						LoadPathsFactory.of(
							interceptor.getLoadPaths().getPaths(),
							true)));
				return;
			}
		}
		
		printer.println("error", "invalid sandbox command: " + Arrays.asList(params));
	}

	private Env loadEnv(
			final CommandLineArgs cli,
			final PrintStream out,
			final PrintStream err,
			final BufferedReader in,
			final boolean macroexpand
	) {
		return venice.createEnv(macroexpand, ansiTerminal, RunMode.REPL)
					 .setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false))
					 .setGlobal(new Var(
								new VncSymbol("*repl-color-theme*"), 
								new VncKeyword(config.getColorMode().name().toLowerCase()),
								false))
					 .setStdoutPrintStream(out)
					 .setStderrPrintStream(err)
					 .setStdinReader(in);
	}
	
	private void activate(final IInterceptor interceptor) {
		this.interceptor = interceptor; 
		this.venice = new VeniceInterpreter(interceptor);
		JavaInterop.register(interceptor);			
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
				   .filter(v -> regexFilter == null ? true : v.getName().getName().matches(regexFilter))
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

	private PrintStream createPrintStream(final String context, final Terminal terminal) {
		return new ReplPrintStream(
					terminal, 
					ansiTerminal ? config.getColor(context) : null);
	}
	
	private BufferedReader createBufferedReader(final String context, final Terminal terminal) {
		return new BufferedReader(terminal.reader());
	}
	
	private boolean runLoadFile(final String loadFile, final Env env, final String resultPrefix) {
		try {
			if (loadFile != null) {
				printer.println("stdout", "loading file \"" + loadFile + "\"");
				final VncVal result = venice.RE("(load-file \"" + loadFile + "\")" , "user", env);
				printer.println("stdout", resultPrefix + venice.PRINT(result));
			}
			return true;
		}
		catch(Exception ex) {
			printer.printex("error", ex);
			printer.println("error", "Stopped REPL");
			return false; // stop the REPL
		}
	}
	
	private String getTerminalInfo() {
		if (ansiTerminal) {
			switch(config.getColorMode()) {
				case Light:
					return "Using Ansi terminal (light color mode turned on)";
				case Dark:
					return "Using Ansi terminal (dark color mode turned on)";
				case None:
				default:
					return "Using Ansi terminal (colors turned off, turn on with option '-colors')";
			}
		}
		else {
			return "Using dumb terminal (colors turned off)";
		}
	}

	private void printConfiguredColors() {
		printer.println("default",   "default");
		printer.println("result",    "result");
		printer.println("stdout",    "stdout");
		printer.println("stderr",    "stderr");
		printer.println("error",     "error");
		printer.println("system",    "system");
		printer.println("interrupt", "interrupt");
	}
	
	private void printLoadPaths(final ILoadPaths loadPaths) {
		printer.println("stdout", "Restricted to load paths: " + (loadPaths.isUnlimitedAccess() ? "no" : "yes"));
		printer.println("stdout", "Paths: ");
		loadPaths.getPaths().forEach(p -> printer.println("stdout", "   " + p.getPath()));
	}
	
	private void printInfo(final Terminal terminal) {
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
		printer.println("stdout", "Macro Expansion: " + (venice.isMacroexpandOnLoad() ? "on" : "off"));
		printer.println("stdout", "Restartable:     " + (restartable ? "yes" : "no"));
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
	
	private boolean isExitCommand(final String cmd) {
		return cmd.equals("quit") 
				|| cmd.equals("q") 
				|| cmd.equals("exit") 
				|| cmd.equals("e");
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
	
	private boolean isRunningOnLinuxGitPod() {
		return  "Linux".equals(System.getProperty("os.name"))
					&& System.getenv("GITPOD_REPO_ROOT") != null;
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
			final String[] params,
			final Terminal terminal,
			final History history
	) {
		if (params.length == 0) {
			printer.println("stdout", String.format(
										"History: size: %d, first: %d, last: %d, index: %d",
										history.size(), history.first(), 
										history.last(), history.index()));
		}
		else if (params[0].equals("clear")) {
			clearCommandHistory(history);	
		}
		else if (params[0].equals("load")) {
			try {
				history.load();
			}
			catch(IOException ex) {
				printer.println("stderr", "Failed to reload REPL command history!");
			}	
		}
		else if (params[0].equals("log")) {
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
		// dropping a file to the REPL that has special characters (space, 
		// asteriks,  ...) in the filename. The underlying OS shell is 
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
	
	public static String osType() {
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
	
	
	public static enum SetupMode { Minimal, Extended };
	
	private final static String HELP =
			"Venice REPL: V" + Venice.getVersion() + "\n\n" +
			"Commands: \n" +
			"  !reload      reload Venice environment\n" +
			"  !restart     restart the REPL.\n" +
			"               note: the REPL launcher script must support\n" +
			"                     REPL restarting.\n" +
			"  !?, !help    help\n" +
			"  !info        show REPL setup context data\n" +
			"  !config      show a sample REPL config\n" +
			"  !classpath   show the REPL classpath\n" +
			"  !loadpath    show the REPL loadpath\n" +
			"  !highlight   turn highlighting dynamically on or off\n" +
			"                 !highlight {on/off}\n" +
			"  !lic         prints the licenses for 3rd party\n" +
			"               libs included with Venice\n" +
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
	private VeniceInterpreter venice;
	private TerminalPrinter printer;
	private ReplHighlighter highlighter;
	private boolean ansiTerminal = false;
	private boolean highlight = true;
	private boolean javaExceptions = false;
	private boolean allowDynamicClassLoader = false;
	private boolean restartable = false;
}
