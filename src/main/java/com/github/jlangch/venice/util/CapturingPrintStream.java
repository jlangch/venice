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
package com.github.jlangch.venice.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;


/**
 * Captures the output written to this <tt>PrintStream</tt>.
 */
public class CapturingPrintStream extends PrintStream {

	private CapturingPrintStream(
			final String encoding,
			final LimitedByteArrayOutputStream boas
	) throws UnsupportedEncodingException {
		super(boas, true, encoding);
		this.encoding = encoding;
		this.boas = boas;
	}

	/**
	 * Creates a new <tt>CapturingPrintStream</tt> with the system's default
	 * charset and Venice's default capturing limit of 10MB.
	 * 
	 * <p>The <tt>CapturingPrintStream</tt> throws a <tt>SecurityException</tt>
	 * if the bytes written to the stream exceed the specified limit. 
	 * 
	 * @return a <tt>CapturingPrintStream</tt>
	 */
	public static CapturingPrintStream create() {
		return create(Charset.defaultCharset().name(), DEFAULT_LIMIT);
	}
	
	/**
	 * Creates a new <tt>CapturingPrintStream</tt> with the system's default
	 * charset and the given capturing limit.
	 * 
	 * <p>The <tt>CapturingPrintStream</tt> throws a <tt>SecurityException</tt>
	 * if the bytes written to the stream exceed the specified limit. 
	 * 
	 * @param limit A capturing limit
	 * @return a <tt>CapturingPrintStream</tt>
	 */
	public static CapturingPrintStream create(final int limit) {
		return create(Charset.defaultCharset().name(), limit);
	}
	
	/**
	 * Creates a new <tt>CapturingPrintStream</tt> with the given encoding
	 * and Venice's default capturing limit of 10MB.
	 * 
	 * <p>The <tt>CapturingPrintStream</tt> throws a <tt>SecurityException</tt>
	 * if the bytes written to the stream exceed the specified limit. 
	 * 
	 * @param encoding A charset encoding
	 * @return a <tt>CapturingPrintStream</tt>
	 */
	public static CapturingPrintStream create(final String encoding) {
		return create(encoding, DEFAULT_LIMIT);		
	}
	
	/**
	 * Creates a new <tt>CapturingPrintStream</tt> with the given encoding
	 * and capturing limit.
	 * 
	 * <p>The <tt>CapturingPrintStream</tt> throws a <tt>SecurityException</tt>
	 * if the bytes written to the stream exceed the specified limit. 
	 * 
	 * @param encoding A charset encoding
	 * @param limit A capturing limit
	 * @return a <tt>CapturingPrintStream</tt>
	 */
	public static CapturingPrintStream create(final String encoding, final int limit) {
		try {
			return new CapturingPrintStream(encoding, new LimitedByteArrayOutputStream(limit));
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding: " + encoding, ex);
		}		
	}

	public void reset() {
		boas.reset();
	}

	public boolean isEmpty() {
		return boas.size() == 0;
	}

	public String getOutput() {
		try {
			return boas.toString(encoding);
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding: " + encoding, ex);
		}
	}
	
	public byte[] getOutputAsBytes() {
		return boas.toByteArray();
	}
	
	
	private static class LimitedByteArrayOutputStream extends ByteArrayOutputStream {

		public LimitedByteArrayOutputStream(final int limit) {
			this.limit = limit;
		}


		public synchronized void write(int b) {
			validateGrowth(1);
			super.write(b);
			count += 1;
		}

		public synchronized void write(byte b[], int off, int len) {
			validateGrowth(len);
			super.write(b, off, len);
			count += len;
		}

		public synchronized void writeTo(OutputStream out) throws IOException {
			super.writeTo(out);
		}

		public synchronized void reset() {
			super.reset();
			count = 0;
		}

		public synchronized byte toByteArray()[] {
			return super.toByteArray();
		}

		public synchronized int size() {
			return count;
		}

		public synchronized String toString() {
			return super.toString();
		}

		public synchronized String toString(String charsetName) throws UnsupportedEncodingException {
			return super.toString(charsetName);
		}
		
		private void validateGrowth(final int len) {
			if (count + len > limit) {
				throw new SecurityException(String.format(
						"CapturingPrintStream exceeded the limit of %d bytes",
						limit));
			}
		}

		private final int limit;
		private int count = 0;
	}
	
	
	public static final int DEFAULT_LIMIT = 1024 * 1024 * 10;
	
	private final String encoding;
	private final LimitedByteArrayOutputStream boas;
}
