/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


/**
 * Captures the output written to this <tt>PrintStream</tt>.
 */
public class CapturingPrintStream extends PrintStream {

	private CapturingPrintStream(
			final String encoding,
			final ByteArrayOutputStream boas
	) throws UnsupportedEncodingException {
		super(boas, true, encoding);
		this.encoding = encoding;
		this.boas = boas;
	}
	
	public static CapturingPrintStream create(final String encoding) {
		try {
			return new CapturingPrintStream(encoding, new ByteArrayOutputStream());
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding: " + encoding, ex);
		}
	}

	public static CapturingPrintStream create() {
		return create("UTF-8");
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
	
	
	private final String encoding;
	private final ByteArrayOutputStream boas;
}
