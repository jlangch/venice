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

import java.util.function.Consumer;

import org.jline.terminal.Terminal;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.VncVal;


public class TerminalPrinter {
	
	public TerminalPrinter(
			final ReplConfig config,
			final Terminal terminal,
			final boolean ansiTerminal,
			final boolean printJavaEx
	) {
		this.config = config;
		this.terminal = terminal;
		this.ansiTerminal = ansiTerminal;
		this.printJavaEx = printJavaEx;
	}
	
	public void setPrintJavaEx(final boolean printJavaEx) {
		this.printJavaEx = printJavaEx;
	}
	
	public void print(
			final String colorID,
			final Consumer<Terminal> fn
	) {
		synchronized (lock) {
			final String color = getColor(colorID);
			if (color == null) {
				fn.accept(terminal);
				terminal.flush();
			}
			else {
				try {
					terminal.writer().print(color);
					fn.accept(terminal);
				}
				finally {
					terminal.writer().print(ReplConfig.ANSI_RESET);
					terminal.flush();
				}
			}
		}
	}
	
	public void println() {
		synchronized (lock) {
			terminal.writer().println();
			terminal.flush();
		}
	}
	
	public void println(
			final String colorID,
			final String text
	) {
		synchronized (lock) {
			print(colorID, t -> t.writer().print(text));
			terminal.writer().println();
			terminal.flush();
		}
	}
	
	public void printex(
			final String colorID,
			final Throwable ex
	) {
		synchronized (lock) {
			try {
				if (ex instanceof ValueException) {
					print(
						colorID, 
						t -> ((ValueException)ex).printVeniceStackTrace(t.writer()));
					println(
						colorID, 
						"\nThrown value: " + Printer.pr_str(
												(VncVal)((ValueException)ex).getValue(), 
												false));			
				}
				else if (ex instanceof VncException) {
					if (printJavaEx) {
						print(
							colorID, 
							t -> ex.printStackTrace(t.writer()));			
					}
					else {
						print(
							colorID, 
							t -> ((VncException)ex).printVeniceStackTrace(t.writer()));		
					}
				}
				else {
					print(colorID, t -> ex.printStackTrace(t.writer()));			
				}
			}
			catch(Throwable e) {
				System.out.println("Internal REPL error while printing exception.");
				e.printStackTrace();
			}
		}
	}
	
	private String getColor(final String colorID) {
		return ansiTerminal ? config.getColor(colorID) : null;
	}
	
	
	private final Object lock = new Object();
	private final Terminal terminal;
	private final boolean ansiTerminal;
	private final ReplConfig config;
	private boolean printJavaEx;
}
