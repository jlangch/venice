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
import java.util.function.Consumer;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;

import com.github.jlangch.venice.ContinueException;
import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.DynamicVar;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.ValueException;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class REPL {
	
	public REPL() {
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
		
		final VeniceInterpreter venice = new VeniceInterpreter();

		final ReplParser parser = new ReplParser(venice);
		
		final LineReader reader = LineReaderBuilder
									.builder()
									.appName("Venice")
									.terminal(terminal)
									//.completer(completer)
									.parser(parser)
									.variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
									.build();

		final ReplResultHistory resultHistory = new ReplResultHistory(3);

		
		Env env = loadEnv(venice, cli, ps);

		// REPL loop
		while (true) {
			resultHistory.mergeToEnv(env);
			
			String line;
			try {
				Thread.interrupted(); // reset the thread's interrupt status
				
				line = reader.readLine(prompt, null, (MaskingCallback) null, null);
				if (line == null) { 
					continue; 
				}
				
				if (line.startsWith("!")) {
					final String cmd = StringUtil.trimToEmpty(line.substring(1));				
					if (cmd.equals("reload")) {
						env = loadEnv(venice, cli, ps);
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
					else if (cmd.equals("env") || cmd.startsWith("env ")) {
						final String[] params = StringUtil.trimToEmpty(cmd.substring(3)).split(" +");
						handleEnvCommand(params, terminal, venice, env);
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
			catch (Exception ex) {
				printex(terminal, "error", ex);
				continue;
			}
			
			try {				
				ThreadLocalMap.clearCallStack();
				final VncVal result = venice.RE(line, "repl", env);
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
			final VeniceInterpreter venice,
			final Env env
	) {
		if (params.length == 1) {
			if (params[0].equals("levels")) {
				terminal.writer().println("Levels: " + (env.level() + 1));
				return;
			}
			else if (params[0].equals("global")) {
				terminal.writer().println(env.globalsToString());
				return;
			}
		}
		else if (params.length == 2) {
			if (params[0].equals("print")) {
				final VncVal val = env.get(new VncSymbol(params[1]));
				terminal.writer().println(venice.PRINT(val));
				return;
			}
			else if (params[0].equals("local")) {
				final int level = Integer.valueOf(params[1]);
				terminal.writer().println(env.getLevelEnv(level).localsToString());
				return;
			}
		}
		
		println(terminal, "system", "invalid env command");					
	}

	private Env loadEnv(
			final VeniceInterpreter venice,
			final CommandLineArgs cli,
			final PrintStream ps
	) {
		return venice
					.createEnv()
					.setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList()))
					.setGlobal(new DynamicVar(new VncSymbol("*out*"), new VncJavaObject(ps)));
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
			println(terminal, colorID, "Thrown value: " + Printer._pr_str(((ValueException)ex).getValue(), false));			
		}
		else if (ex instanceof VncException) {
			print(terminal, colorID, t -> ((VncException)ex).printVeniceStackTrace(t.writer()));		
		}
		else {
			print(terminal, colorID, t -> ex.printStackTrace(t.writer()));			
		}
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
			"  !exit       quit REPL\n\n" +	
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

	private ReplConfig config;
}
