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

import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
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
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.ThreadLocalMap;


public class REPL {
	
	public REPL() {
	}
	
	public void run(final String[] args) {
		final CommandLineArgs cli = new CommandLineArgs(args);

		System.out.println("REPL Venice: V" + Venice.getVersion());

		try {
			config = ReplConfig.load(cli);
			repl_jline(cli);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	private void repl_jline(final CommandLineArgs cli) throws Exception {
		final TerminalBuilder builder = TerminalBuilder.builder();

		final Terminal terminal = builder
									.encoding("UTF-8")
									.type("xterm-256color")
									.build();
 		
		final DefaultParser parser = new DefaultParser();
		
		final LineReader reader = LineReaderBuilder
									.builder()
									.appName("Venice")
									.terminal(terminal)
									//.completer(completer)
									.parser(parser)
									.build();
 
		final VeniceInterpreter venice = new VeniceInterpreter();
		
		final ReplPrintStream ps = new ReplPrintStream(
											Charset.defaultCharset().name(), 
											System.out, 
											terminal, 
											config.getColor("colors.stdout"));
		
		final Env env = venice
							.createEnv()
							.setGlobal(new Var(new VncSymbol("*ARGV*"), cli.argsAsList()))
							.setGlobal(new DynamicVar(
											new VncSymbol("*out*"), 
											new VncJavaObject(config.useColors() ? ps : System.out)));

		final String prompt = config.getPrompt();
		final String resultPrefix = config.getResultPrefix();

		// REPL loop
		while (true) {
			String line;
			try {
				line = reader.readLine(prompt, null, (MaskingCallback) null, null);
				if (line == null) { 
					continue; 
				}
			} 
			catch (UserInterruptException ex) {
				terminal.flush();
				write(terminal, "interrupt", " ! interrupted ! ");
				terminal.flush();
				terminal.writer().println();
				terminal.flush();
				Thread.sleep(1000);
				break;
			} 
			catch (EofException | EndOfFileException ex) {
				break;
			} 
			catch (VncException ex) {
				write(terminal, "error", t -> ex.printVeniceStackTrace(t.writer()));
				continue;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				break;
			}
			
			try {
				ThreadLocalMap.clearCallStack();
				write(terminal, "result", resultPrefix + venice.PRINT(venice.RE(line, "repl", env)));
			} 
			catch (ContinueException ex) {
				continue;
			} 
			catch (ValueException ex) {
				write(terminal, "error", t -> ex.printVeniceStackTrace(t.writer()));
				write(terminal, "error", "Thrown value: " + Printer._pr_str(ex.getValue(), false));
				continue;
			} 
			catch (VncException ex) {
				write(terminal, "error", t -> ex.printVeniceStackTrace(t.writer()));
				continue;
			}
			catch (Exception ex) {
				write(terminal, "error", t -> ex.printStackTrace(t.writer()));
				continue;
			}
		}
	}

	private void write(
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

	private void write(
			final Terminal terminal,
			final String colorID,
			final String text
	) {
		write(terminal, colorID, t -> t.writer().println(text));
	}
	
	
	private ReplConfig config;
}
