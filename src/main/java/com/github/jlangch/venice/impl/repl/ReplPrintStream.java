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

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.jline.terminal.Terminal;


public class ReplPrintStream extends PrintStream {

	public ReplPrintStream(
			final String encoding,
			final PrintStream ps,
			final Terminal terminal,
			final String colorEscape
	) throws UnsupportedEncodingException {
		super(ps, true, encoding);
		this.terminal = terminal;
		this.colorEscape = colorEscape;
	}

	public void print(final String s) {
		write(s);
	}

	public void println(final String s) {
		write(s + "\n");
	}

	private void write(final String s) {
		if (colorEscape != null) terminal.writer().print(colorEscape);
		
		terminal.writer().print(s);
		
		if (colorEscape != null) terminal.writer().print(ReplConfig.ANSI_RESET);
		
		terminal.flush();
	}
    
    
    private final Terminal terminal;
	private final String colorEscape;
}
