/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.Licenses;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

public class REPL {
	
	public REPL(final IInterceptor interceptor, final List<String> loadPaths) {
		this.interceptor = interceptor;
		this.loadPaths = loadPaths;
	}
	
	public void run(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);

		try {
			config = ReplConfig.load(cli);
			
			final Level jlineLogLevel = config.getJLineLogLevel();
			if (jlineLogLevel != null) {
				Logger.getLogger("org.jline").setLevel(jlineLogLevel);
			}

			if (OSUtils.IS_WINDOWS) {
				final String jansiVersion = config.getJansiVersion();
				if (jansiVersion != null) {
					System.out.println("Using Jansi V" + jansiVersion);
				}
				else {
					System.out.print(
							"--------------------------------------------------------------------\n" +
							"The Venice REPL requires the jansi library on Windows.              \n" +
							"Please download the jar artifact 'org.fusesource.jansi:jansi:1.18'  \n" +
							"from a Maven repo and put it on the classpath.                      \n" +
							"--------------------------------------------------------------------\n");
				}
			}
			
			System.out.println("Venice REPL: V" + Venice.getVersion());			
			System.out.println("Type '!' for help.");
			
			repl(cli);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	private void repl(final CommandLineArgs cli) throws Exception {
		final boolean dumbTerminal = config.isJLineDumbTerminal();
		
		final String prompt = config.getPrompt();
		final String secondaryPrompt = dumbTerminal ? "" : config.getSecondaryPrompt();
		final String resultPrefix = config.getResultPrefix();

		final Thread mainThread = Thread.currentThread();
		
		final TerminalBuilder builder = TerminalBuilder
											.builder()
											.streams(System.in, System.out)
											.system(true)
											.dumb(dumbTerminal)
											.jna(false);

		final Terminal terminal = OSUtils.IS_WINDOWS
									? builder
										.jna(false)
										.jansi(!dumbTerminal)
										.build()
									: builder
										.encoding("UTF-8")
										.build();
	 
		terminal.handle(Signal.INT, signal -> mainThread.interrupt());
       
		final PrintStream ps_out = createPrintStream("stdout", terminal);

		final PrintStream ps_err = createPrintStream("stderr", terminal);

		printer = new TerminalPrinter(config, terminal, false);
		
		venice = new VeniceInterpreter(interceptor, loadPaths);
		
		Env env = loadEnv(cli, ps_out, ps_err);

		
		final ReplParser parser = new ReplParser(venice);
		
		final ReplCompleter completer = new ReplCompleter(venice, env, loadPaths);
		
		final History history = new DefaultHistory();
		
		final LineReader reader = LineReaderBuilder
									.builder()
									.appName("Venice")
									.terminal(terminal)
									.history(history)
									.completer(completer)
									.parser(parser)
									.variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
									.variable(LineReader.INDENTATION, 2)
				                    .variable(LineReader.LIST_MAX, 100)
									.build();

		final ReplResultHistory resultHistory = new ReplResultHistory(3);

		runLoadFile(config.getLoadFile(), env, resultPrefix);

		loadLibraries(env);
		loadFonts(env);

		// REPL loop
		while (true) {
			resultHistory.mergeToEnv(env);
			
			String line;
			try {
				Thread.interrupted(); // reset the thread's interrupt status
				
				line = reader.readLine(prompt, null, (MaskingCallback)null, null);
				if (line == null) { 
					continue; 
				}
				
				if (line.startsWith("!")) {
					final String cmd = StringUtil.trimToEmpty(line.substring(1));				
					if (cmd.equals("reload")) {
						env = loadEnv(cli, ps_out, ps_err);
						printer.println("system", "reloaded");					
						continue;
					}
					else if (cmd.equals("exit")) {
						printer.println("interrupt", " good bye ");
						Thread.sleep(1000);
						break;
					}
					else {			
						handleCommand(cmd, env, terminal);
						continue;
					}
				}
			} 
			catch (ContinueException ex) {
				continue;
			}
			catch (UserInterruptException ex) {
				Thread.interrupted(); // reset the thread's interrupt status

				// User typed ctrl-C
				if (parser.isEOF()) {
					// cancel multi-line edit
					printer.println("interrupt", " cancel ");					
					parser.reset();
					continue;
				}
				else {
					// quit the REPL
					printer.println("interrupt", " ! interrupted ! ");
					Thread.sleep(1000);
					break;
				}
			} 
			catch (EofException | EndOfFileException ex) {
				break;
			} 
			catch (ParseError ex) {
				// put the script to the history to allow to fix it
				history.add(reader.getBuffer().toString());
				printer.printex("error", ex);
				continue;
			}
			catch (Exception ex) {
				printer.printex("error", ex);
				continue;
			}
			
			final VncVal result = runCodeFragment(line, env);
			if (result != null) {
				printer.println("result", resultPrefix + venice.PRINT(result));
				resultHistory.add(result);
			}
		}
	}

	private VncVal runCodeFragment(final String snippet, final Env env) {
		try {				
			ThreadLocalMap.clearCallStack();			
			return venice.RE(snippet, "user", env, macroexpand);
		} 
		catch (ContinueException ex) {
			// just continue
			return null;
		} 
		catch (Exception ex) {
			printer.printex("error", ex);
			return null;
		}
		catch (Throwable ex) {
			printer.printex("error", ex);
			return null;
		}
	}
	
	private void handleCommand(
			final String cmd, 
			final Env env, 
			final Terminal terminal
	) {
		if (cmd.equals("macroexpand") || cmd.equals("me")) {
			macroexpand = true;
			setMacroexpandOnLoad(env, true);
			printer.println("system", "macroexpansion enabled");					
		}
		else if (cmd.isEmpty() || cmd.equals("?") || cmd.equals("help")) {
			printer.println("stdout", HELP);					
		}
		else if (cmd.equals("config")) {
			handleConfigCommand();
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
		else if (cmd.startsWith("java-ex")) {
			printer.setPrintJavaEx(true);
			printer.println("stdout", "Printing Java exceptions");
		}
		else if (cmd.equals("sandbox")) {
			handleSandboxCommand(new String[0], terminal, env);
		}
		else if (cmd.startsWith("sandbox ")) {
			final String[] params = StringUtil.trimToEmpty(cmd.substring(7)).split(" +");
			handleSandboxCommand(params, terminal, env);
		}
		else if (cmd.equals("lic")) {
			Licenses.lics().entrySet().forEach(e -> {
				printer.println("stdout", "");
				printer.println("stdout", DELIM);
				printer.println("stdout", e.getKey() + " License");
				printer.println("stdout", DELIM);
				printer.println("stdout", e.getValue());
			});
		}
		else if (cmd.equals("colors")) {
			printer.println("default",   "default");
			printer.println("result",    "result");
			printer.println("stdout",    "stdout");
			printer.println("stderr",    "stderr");
			printer.println("error",     "error");
			printer.println("system",    "system");
			printer.println("interrupt", "interrupt");
		}
		else {	
			printer.println("error", "invalid command");
		}
	}

	private void handleConfigCommand() {
		printer.println("stdout", "Sample REPL configuration. Save it as 'repl.json'");
		printer.println("stdout", "in the REPL's working directory:");
		printer.println();
		printer.println("stdout", ReplConfig.getRawClasspathConfig());
	}

	private void handleLauncherCommand() {
		final String name = ReplConfig.getRawClasspathLauncherName();
		
		printer.println("stdout", "Sample REPL launcher script. Save it as '" + name + "'");
		printer.println("stdout", "in the REPL's working directory:");
		printer.println();
		printer.println("stdout", ReplConfig.getRawClasspathLauncher());
	}

	private void handleEnvCommand(
			final String[] params,
			final Env env
	) {
		if (params.length == 0) {
			printer.println("stdout", HELP_ENV);
			return;
		}
		else if (params[0].equals("levels")) {
			if (params.length == 1) {
				printer.println("stdout", "Levels: " + (env.level() + 1));
				return;
			}
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
				printer.println("stdout", env.globalsToString());
				return;
			}
			else if (params.length == 2) {
				String filter = StringUtil.trimToNull(params[1]);
				filter = filter == null ? null : filter.replaceAll("[*]", ".*");
				printer.println("stdout", env.globalsToString(filter));
				return;
			}
		}
		else if (params[0].equals("local")) {
			if (params.length == 2) {
				final int level = Integer.valueOf(params[1]);
				printer.println("stdout", env.getLevelEnv(level).localsToString());
				return;
			}
		}
				
		printer.println("error", "invalid env command");					
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
				activate(new AcceptAllInterceptor());
				return;
			}
			else if (params[0].equals("reject-all")) {
				activate(new RejectAllInterceptor());
				return;			
			}
			else if (params[0].equals("customized")) {
				activate(new SandboxInterceptor(new SandboxRules()));
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
				activate(new SandboxInterceptor(rules));
				return;
			}
		}
		
		printer.println("error", "invalid sandbox command: " + Arrays.asList(params));
	}

	private Env loadEnv(
			final CommandLineArgs cli,
			final PrintStream ps_out,
			final PrintStream ps_err
	) {
		return venice.createEnv(macroexpand, new VncKeyword("repl"))
					 .setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false))
					 .setStdoutPrintStream(ps_out)
					 .setStderrPrintStream(ps_err);
	}
	
	private void setMacroexpandOnLoad(final Env env, final boolean macroexpandOnLoad) {
		env.setGlobal(new Var(new VncSymbol("*macroexpand-on-load*"), 
				              macroexpandOnLoad ? True : False, 
				              true));
	}
	
	private void activate(final IInterceptor interceptor) {
		this.interceptor = interceptor; 
		this.venice = new VeniceInterpreter(interceptor, loadPaths);
		JavaInterop.register(interceptor);			
	}
	
	private PrintStream createPrintStream(final String context, final Terminal terminal) {
		return new ReplPrintStream(terminal, config.getColor(context));	
	}
	
	private void runLoadFile(final String loadFile, final Env env, final String resultPrefix) {
		try {
			if (loadFile != null) {
				printer.println("stdout", "loading file \"" + loadFile + "\"");
				final VncVal result = venice.RE("(load-file \"" + loadFile + "\")" , "user", env);
				printer.println("stdout", resultPrefix + venice.PRINT(result));
			}
		}
		catch(Exception ex) {
			printer.printex("error", ex);
			printer.println("error", "Stopped REPL");
			return; // stop the REPL
		}
	}

	private void loadLibraries(final Env env) {
		try {
			final String dir = config.getLibsDir();
			final List<String> libs = 
					config.getLibsMandatory()
						  .stream()
						  .filter(l -> !new File(dir, getMavenArtefactFilename(l, ".jar")).isFile())
						  .collect(Collectors.toList());
			
			if (!libs.isEmpty()) {
				printer.println("stdout", "Loading Java libraries...");
				
				if (!new File(dir).isDirectory()) {
					printer.println("error", String.format("The lib dir %s is not a directory", dir));
					return;
				}

				VncVal result = venice.RE("(internet-avail? \"http://www.google.com\")", "user", env);
				if (result.equals(Constants.False)) {
					printer.println("error", "Internet is not available!");
					return;
				}
				
				venice.RE("(do (load-module :maven) (load-module :ansi))", "user", env);
				
				libs.forEach(l -> venice.RE(
									String.format("(maven/download \"%s\" :dir \"%s\")", l, dir), 
									"user", env));
			}
		}
		catch(Exception ex) {
			printer.printex("error", ex);
			printer.println("error", "Stopped REPL");
			return; // stop the REPL
		}
	}

	private void loadFonts(final Env env) {
		try {
			final String dir = config.getFontsDir();
			final String baseUrl = config.getFontsBaseUrl();
			
			final List<Tuple2<String,File>> fonts = 
					config.getFontsMandatory()
						  .stream()
						  .map(l -> new Tuple2<String,File>(baseUrl + l, new File(dir, new File(l).getName())))
						  .filter(l -> !l._2.isFile())
						  .collect(Collectors.toList());
			
			if (!fonts.isEmpty()) {
				printer.println("stdout", "Loading fonts...");
				
				if (!new File(dir).isDirectory()) {
					printer.println("error", String.format("The font dir %s is not a directory", dir));
					return;
				}

				VncVal result = venice.RE("(internet-avail? \"http://www.google.com\")", "user", env);
				if (result.equals(Constants.False)) {
					printer.println("error", "Internet is not available!");
					return;
				}
				
				venice.RE("(load-module :ansi)", "user", env);
				
				final String script =
						"(let [uri         \"%s\"                                        \n" + 
						"      target      (io/file \"%s\")                              \n" + 
						"      font-name   \"%s\"                                        \n" + 
						"      progress-fn (ansi/progress-bar                            \n" +
						"                     :caption     (str font-name \" \")         \n" +
						"                     :start-msg   \"\"                          \n" +
						"                     :end-msg     (str font-name \" ok\")       \n" +
						"                     :failed-msg  (str font-name \" failed\"))] \n" +
						"  (io/spit target (io/download uri                              \n" +
						"                      :binary true                              \n" +
						"                      :user-agent \"Mozilla\"                   \n" +
						"                      :progress-fn progress-fn)))               \n";

				fonts.forEach(l -> venice.RE(
									String.format(script, l._1, l._2.getPath(), l._2.getName()), 
									"user", env));
			}
		}
		catch(Exception ex) {
			printer.printex("error", ex);
			printer.println("error", "Stopped REPL");
			return; // stop the REPL
		}
	}

	
	final static String getMavenArtefactFilename(final String artefact, final String suffix) {
		final String[] elements = artefact.split(":");
		return elements[1] + "-" + elements[2] + suffix;
	}
	
	
	private final static String HELP =
			"Venice REPL: V" + Venice.getVersion() + "\n\n" +
			"Commands: \n" +	
			"  !reload      reload Venice environment\n" +	
			"  !?, !help    help\n" +	
			"  !config      show a sample REPL config\n" +	
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
			"                 !env local {level}\n" +	
			"                 !env levels\n" +	
			"  !sandbox     sandbox\n" +	
			"                 !sandbox status\n" +	
			"                 !sandbox config\n" +	
			"                 !sandbox accept-all\n" +	
			"                 !sandbox reject-all\n" +	
			"                 !sandbox customized\n" +	
			"                 !sandbox add-rule rule\n" +
			"  !java-ex     print Java exception\n" +	
			"  !exit        quit the REPL\n\n" +	
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
			"   !env global *file*\n" +	
			"   !env local {level}\n" +	
			"   !env levels\n";

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

	private final static String DELIM = StringUtil.repeat('-', 80);

	private final List<String> loadPaths;

	private ReplConfig config;
	private IInterceptor interceptor;
	private VeniceInterpreter venice;
	private TerminalPrinter printer;
	private boolean macroexpand = false;
}
