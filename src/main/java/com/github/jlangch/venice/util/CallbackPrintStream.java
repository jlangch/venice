/*   __	__		 _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *	\ \/ / _ \ '_ \| |/ __/ _ \
 *	 \  /  __/ | | | | (_|  __/
 *	  \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.util;

import java.io.PrintStream;
import java.util.function.Consumer;


/**
 * Sends the objects printed to this {@link java.io.PrintStream} to the supplied
 * consumer. If {@code autoFlush} is enabled sends the text whenever a
 * line-feed is encountered or {@link #flush()} is called, else only sends
 * on {@link #flush()}.
 */
public class CallbackPrintStream extends PrintStream {

	public CallbackPrintStream(
			final boolean autoFlush,
			final Consumer<String> printer
	) {
		super(new NullOutputStream());
		
		this.autoFlush = autoFlush;
		this.printer = printer;
	}

	
	@Override
	public PrintStream append(final CharSequence csq) {
		print(csq == null ? "null" : csq.toString());
		return this;
	}
	
	@Override
	public PrintStream append(final CharSequence csq, final int start, final int end) {
		final CharSequence cs = (csq == null ? "null" : csq);
		print(cs.subSequence(start, end).toString());
		return this;
	}

	@Override
	public PrintStream append(final char c) {
		print(c);
		return this;
	}

	@Override
	public void print(final boolean x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final int x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final long x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final float x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final double x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final char x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final char[] x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final Object x) {
		print(String.valueOf(x));
	}

	@Override
	public void print(final String s) {
		synchronized (this) {
			sb.append(s);
			
			if (autoFlush && (s.indexOf('\n') >= 0)) {
				flush();
			}
		}
	}

	@Override
	public void println() {
		println("");
	}

	@Override
	public void println(final boolean x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final int x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final long x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final float x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final double x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final char x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final char[] x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final Object x) {
		println(String.valueOf(x));
	}

	@Override
	public void println(final String s) {
		synchronized (this) {
			sb.append(s).append("\n");		
			if (autoFlush) flush();
		}
	}

	@Override
	public void write(final byte buf[], final int off, final int len) {
		throw new RuntimeException(
				"Method write(byte[],int,int) is not supported");
	}
	
	@Override
	public void write(final int b) {
		throw new RuntimeException(
				"Method write(int) is not supported");
	}
	
	@Override
	public void close() {
	}

	@Override
	public void flush() {
		synchronized (this) {
			if (sb.length() > 0) {
				printer.accept(sb.toString());
				sb.setLength(0);
			}
		}
	}

	
	private final Consumer<String> printer;
	private final StringBuilder sb = new StringBuilder();
	private final boolean autoFlush;
}
