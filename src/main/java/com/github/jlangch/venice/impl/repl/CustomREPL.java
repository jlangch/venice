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

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;

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

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.Var;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class CustomREPL {
	
	public CustomREPL(
			final IInterceptor interceptor, 
			final List<String> loadPaths,
			final File app
	) {
		this.interceptor = interceptor;
		this.loadPaths = loadPaths;
		this.app = app;
	}
	
	public void run(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);

		try {
			System.out.println("Venice custom REPL: V" + Venice.getVersion());
			config = ReplConfig.load(cli);
			
			repl(cli);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	public void setHandler(final Consumer<String> handler) {
		this.cmdHandler = handler;
	}
	
	public void setPrompt(final String prompt) {
		this.prompt = prompt;
		this.secondaryPrompt = "";
	}
	
	public void setPrompt(final String prompt, final String secondaryPrompt) {
		this.prompt = prompt;
		this.secondaryPrompt = secondaryPrompt;
	}

	private void repl(final CommandLineArgs cli) throws Exception {
		setPrompt(config.getPrompt(), config.getSecondaryPrompt());

		final TerminalBuilder builder = TerminalBuilder.builder();
		
		final Thread mainThread = Thread.currentThread();
		
		final Terminal terminal = builder
									.encoding("UTF-8")
									.type("xterm-256color")
									.system(true)
									.nativeSignals(true)
									.signalHandler(createSignalHandler(mainThread))
									.build();
 
		final PrintStream ps_out = createPrintStream("stdout", terminal, System.out);

		final PrintStream ps_err = createPrintStream("stderr", terminal, System.out);

		final TerminalPrinter printer = new TerminalPrinter(config, terminal, false);
		
		final VeniceInterpreter venice = new VeniceInterpreter(interceptor, loadPaths);
		
		final Env env = loadEnv(venice, cli, ps_out, ps_err);
		

		try {
			printer.println("stdout", "loading file \"" + app.getPath() + "\"");
			venice.RE("(load-file \"" + app.getPath() + "\")" , "user", env);
		}
		catch(Exception ex) {
			printer.printex("error", ex);
		}

		
		final History history = new DefaultHistory();
		
		final LineReader reader = LineReaderBuilder
									.builder()
									.appName("Venice")
									.terminal(terminal)
									.history(history)
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
				if (line == null) { 
					continue; 
				}
				
				cmdHandler.accept(line);
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
				continue;
			}
		}
	}

	private Env loadEnv(
			final VeniceInterpreter venice,
			final CommandLineArgs cli,
			final PrintStream ps_out,
			final PrintStream ps_err
	) {
		return venice.createEnv(macroexpand, new VncKeyword("repl"))
					 .setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList(), false))
					 .setGlobal(new Var(new VncSymbol("*REPL*"), new VncJavaObject(this), false))
					 .setStdoutPrintStream(ps_out)
					 .setStderrPrintStream(ps_err);
	}
	
	private PrintStream createPrintStream(
			final String context,
			final Terminal terminal,
			final PrintStream defaultPS
	) {
		final String color = config.getColor(context);
		return color != null ? new ReplPrintStream(terminal,color) : defaultPS;	
	}
	
	private Terminal.SignalHandler createSignalHandler(final Thread mainThread) {
		return new Terminal.SignalHandler() {
			public void handle(final Signal signal) {
				if (signal == Signal.INT) {
					// ctrl-C stops infinite Venice loops
					mainThread.interrupt();
				}
			}
		 };
	}
	
	
	
	private static final String DEFAULT_PROMPT_PRIMARY   = "venice> ";
	private static final String DEFAULT_PROMPT_SECONDARY = "      | ";
	

	private final List<String> loadPaths;
	private final File app;

	private String prompt = DEFAULT_PROMPT_PRIMARY;
	private String secondaryPrompt  = DEFAULT_PROMPT_SECONDARY;
	private Consumer<String> cmdHandler;
	private ReplConfig config;
	private IInterceptor interceptor;
	private boolean macroexpand = false;
}
