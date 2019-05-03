/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;
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

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.javainterop.JavaInterop;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.concurrent.ThreadLocalMap;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class REPL {
	
	public REPL(final IInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void run(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);

		System.out.println("Venice REPL: V" + Venice.getVersion());
		System.out.println("Type '!' for help.");

		try {
			config = ReplConfig.load(cli);
			repl(cli);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	private void repl(final CommandLineArgs cli) throws Exception {
		final String prompt = config.getPrompt();
		final String secondaryPrompt = config.getSecondaryPrompt();
		final String resultPrefix = config.getResultPrefix();

		final TerminalBuilder builder = TerminalBuilder.builder();
		
		final Thread mainThread = Thread.currentThread();
		
		final Terminal terminal = builder
									.encoding("UTF-8")
									.type("xterm-256color")
									.system(true)
									.nativeSignals(true)
									.signalHandler(new Terminal.SignalHandler() {
										public void handle(final Signal signal) {
											if (signal == Signal.INT) {
												// ctrl-C stops infinite Venice loops
												mainThread.interrupt();
											}
										}
									 })
									.build();
 
		final PrintStream ps = config.useColors() 
									? new ReplPrintStream(
											Charset.defaultCharset().name(), 
											System.out, 
											terminal, 
											config.getColor("colors.stdout"))
									: System.out;
		
		venice = new VeniceInterpreter(interceptor);

		final ReplParser parser = new ReplParser(venice);
		
		final History history = new DefaultHistory();
		
		final LineReader reader = LineReaderBuilder
									.builder()
									.appName("Venice")
									.terminal(terminal)
									.history(history)
									//.completer(completer)
									.parser(parser)
									.variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
									.build();

		final ReplResultHistory resultHistory = new ReplResultHistory(3);

		
		Env env = loadEnv(cli, ps);

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
						env = loadEnv(cli, ps);
						println(terminal, "system", "reloaded");					
						continue;
					}
					else if (cmd.isEmpty() || cmd.equals("?") || cmd.equals("help")) {
						terminal.writer().println(HELP);
						continue;
					}
					else if (cmd.equals("config")) {
						handleConfigCommand(terminal);
						continue;
					}
					else if (cmd.equals("env")) {
						handleEnvCommand(new String[0], terminal, env);
						continue;
					}
					else if (cmd.startsWith("env ")) {
						final String[] params = StringUtil.trimToEmpty(cmd.substring(3)).split(" +");
						handleEnvCommand(params, terminal, env);
						continue;
					}
					else if (cmd.equals("sandbox")) {
						handleSandboxCommand(new String[0], terminal, env);
						continue;
					}
					else if (cmd.startsWith("sandbox ")) {
						final String[] params = StringUtil.trimToEmpty(cmd.substring(7)).split(" +");
						handleSandboxCommand(params, terminal, env);
						continue;
					}
					else if (cmd.equals("exit")) {
						println(terminal, "interrupt", " good bye ");					
						Thread.sleep(1000);
						break;
					}
					
					println(terminal, "system", "invalid command");					
					continue;
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
					println(terminal, "interrupt", " cancel ");					
					parser.reset();
					continue;
				}
				else {
					// quit the REPL
					println(terminal, "interrupt", " ! interrupted ! ");
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
				printex(terminal, "error", ex);
				continue;
			}
			catch (Exception ex) {
				printex(terminal, "error", ex);
				continue;
			}
			
			try {				
				ThreadLocalMap.clearCallStack();
				final VncVal result = venice.RE(line, "user", env);
				resultHistory.add(result);
				println(terminal, "result", resultPrefix + venice.PRINT(result));
			} 
			catch (ContinueException ex) {
				continue;
			} 
			catch (Exception ex) {
				printex(terminal, "error", ex);
				continue;
			}
		}
	}

	private void handleConfigCommand(final Terminal terminal) {
		terminal.writer().println("Sample REPL configuration. Save it as 'repl.json'");
		terminal.writer().println("in the REPL's working directory:");
		terminal.writer().println();
		terminal.writer().println(ReplConfig.getRawClasspathConfig());
	}

	private void handleEnvCommand(
			final String[] params,
			final Terminal terminal,
			final Env env
	) {
		if (params.length == 0) {
			terminal.writer().println(HELP_ENV);
			return;
		}
		else if (params.length == 1) {
			if (params[0].equals("levels")) {
				println(terminal, "stdout", "Levels: " + (env.level() + 1));
				return;
			}
			else if (params[0].equals("global")) {
				println(terminal, "stdout", env.globalsToString());
				return;
			}
		}
		else if (params.length == 2) {
			if (params[0].equals("print")) {
				final VncVal val = env.get(new VncSymbol(params[1]));
				println(terminal, "stdout", venice.PRINT(val));
				return;
			}
			else if (params[0].equals("local")) {
				final int level = Integer.valueOf(params[1]);
				println(terminal, "stdout", env.getLevelEnv(level).localsToString());
				return;
			}
		}
		
		println(terminal, "system", "invalid env command");					
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
					println(terminal, "stdout", "No sandbox active (" + interceptorName +")");
					return;
				}
				else if (interceptor instanceof RejectAllInterceptor) {
					println(
						terminal, 
						"stdout", 
						"Sandbox active (" + interceptorName + "). "
							+ "Rejects all Java calls and default "
							+ "blacklisted Venice functions");
					return;
				}
				else if (interceptor instanceof SandboxInterceptor) {
					println(terminal, "stdout", "Customized sandbox active (" + interceptorName + ")");
					return;
				}
				else {
					println(terminal, "stdout", "Sandbox: " + interceptorName);
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
					println(terminal, "stdout", "[accept-all] NO sandbox active");
					println(terminal, "stdout", "All Java calls accepted no Venice calls rejected");
					return;
				}
				else if (interceptor instanceof RejectAllInterceptor) {
					println(terminal, "stdout", "[reject-all] SAFE restricted sandbox");
					println(terminal, "stdout", "Java calls:\n   All rejected!");
					println(
						terminal,
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
					println(terminal, "stdout", "[customized] Customized sandbox");
					println(
						terminal, 
						"stdout", 
						"Sandbox rules:\n" + ((SandboxInterceptor)interceptor).getRules().toString());
						return;
				}
				else {
					println(terminal, "stdout", "[" + interceptorName + "]");
					println(terminal, "stdout", "no info");
					return;
				}
			}
		}
		else if (params.length == 2) {
			if (params[0].equals("add-rule")) {
				final String rule = params[1];
				if (!(interceptor instanceof SandboxInterceptor)) {
					println(terminal, "system", "rules can only be added to a customized sandbox");
					return;
				}
				
				final SandboxRules rules = ((SandboxInterceptor)interceptor).getRules();
				if (rule.startsWith("class:")) {
					rules.withClasses(rule);
				}
				else if (rule.startsWith("system.property:")) {
					rules.withSystemProperties(rule);
				}
				else if (rule.startsWith("blacklist:venice:")) {
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
		
		println(terminal, "system", "invalid sandbox command: " + Arrays.asList(params));
	}

	private Env loadEnv(
			final CommandLineArgs cli,
			final PrintStream ps
	) {
		return venice.createEnv()
					 .setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList()))
					 .setStdoutPrintStream(ps);
	}
	
	private void print(
			final Terminal terminal,
			final String colorID,
			final Consumer<Terminal> fn
	) {
		final String color = config.getColor("colors." + colorID);
		if (color != null) {
			terminal.writer().print(color);
		}
		
		fn.accept(terminal);
		
		if (color != null) {
			terminal.writer().print(ReplConfig.ANSI_RESET);
		}
		
		terminal.flush();
	}
	
	private void println(
			final Terminal terminal,
			final String colorID,
			final String text
	) {
		print(terminal, colorID, t -> t.writer().print(text));
		terminal.writer().println();
		terminal.flush();
	}
	
	private void printex(
			final Terminal terminal,
			final String colorID,
			final Exception ex
	) {
		if (ex instanceof ValueException) {
			print(terminal, colorID, t -> ((ValueException)ex).printVeniceStackTrace(t.writer()));		
			println(terminal, colorID, "Thrown value: " + Printer.pr_str(((ValueException)ex).getValue(), false));			
		}
		else if (ex instanceof VncException) {
			print(terminal, colorID, t -> ((VncException)ex).printVeniceStackTrace(t.writer()));		
		}
		else {
			print(terminal, colorID, t -> ex.printStackTrace(t.writer()));			
		}
	}
	
	private void activate(final IInterceptor interceptor) {
		this.interceptor = interceptor; 
		this.venice = new VeniceInterpreter(interceptor);
		JavaInterop.register(interceptor);			
	}

	
	private final static String HELP =
			"Venice REPL: V" + Venice.getVersion() + "\n\n" +
			"Commands: \n" +	
			"  !reload     reload Venice environment\n" +	
			"  !?, !help   help\n" +	
			"  !config     show a sample REPL config\n" +	
			"  !env        print env symbols:\n" +	
			"                !env print {symbol-name}\n" +	
			"                !env global\n" +	
			"                !env local {level}\n" +	
			"                !env levels\n" +	
			"  !sandbox    sandbox\n" +	
			"                !sandbox status\n" +	
			"                !sandbox config\n" +	
			"                !sandbox accept-all\n" +	
			"                !sandbox reject-all\n" +	
			"                !sandbox customized\n" +	
			"                !sandbox add-rule rule\n" +
			"  !exit       quit the REPL\n\n" +	
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
			"   !sandbox add-rule blacklist:venice:io/exists-dir?\n" +
			"   !sandbox add-rule blacklist:venice:*io*\n";	

	
	private ReplConfig config;
	private IInterceptor interceptor;
	private VeniceInterpreter venice;
}
