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
package com.github.jlangch.venice.util;

import java.io.PrintStream;


/**
 * Captures the output written to this {@link java.io.PrintStream}.
 */
public class CapturingPrintStream extends PrintStream {

	/**
	 * Creates a new {@link CapturingPrintStream} and a default capturing limit 
	 * of 10MB.
	 * 
	 * <p>The {@link CapturingPrintStream} throws a {@link SecurityException}
	 * if the bytes written to the stream exceed the specified limit. 
	 */
	public CapturingPrintStream() {
		this(DEFAULT_LIMIT);
	}

	/**
	 * Creates a new {@link CapturingPrintStream} with the given capturing limit.
	 * 
	 * <p>The {@link CapturingPrintStream} throws a {@link SecurityException}
	 * if the bytes written to the stream exceed the specified limit. 
	 * 
	 * @param limit A capturing limit
	 */
	public CapturingPrintStream(final int limit) {
		super(new NullOutputStream());
		
		this.limit = limit;
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
		appendToBuffer(s);
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
		appendToBuffer(s);
		appendToBuffer("\n");
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
	}

	public void reset() {
		synchronized (this) {
			sb.setLength(0);
		}
	}

	public boolean isEmpty() {
		synchronized (this) {
			return sb.length() == 0;
		}
	}

	public String getOutput() {
		synchronized (this) {
			return sb.toString();
		}
	}
	
	private void appendToBuffer(final String s) {
		synchronized (this) {
			final int left = limit - sb.length();
			
			if (left <= 0) {
				throw new SecurityException(String.format(
						"CapturingPrintStream exceeded the limit of %d chars",
						limit));
			}
			else if (s.length() <= left) {
				sb.append(s);		
			}
			else {
				sb.append(s.substring(0, left));
				
				throw new SecurityException(String.format(
						"CapturingPrintStream exceeded the limit of %d chars",
						limit));
			}
		}
		
	}
	
	
	public static final int DEFAULT_LIMIT = 1024 * 1024 * 10;
	
	private final int limit;
	private final StringBuilder sb = new StringBuilder();
}
