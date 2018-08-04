package com.github.jlangch.venice.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


public class CaputringPrintStream extends PrintStream {

	private CaputringPrintStream(
			final String encoding,
			final ByteArrayOutputStream boas
	) throws UnsupportedEncodingException {
		super(boas, true, encoding);
		this.encoding = encoding;
		this.boas = boas;
	}
	
	public CaputringPrintStream create(final String encoding) {
		try {
			return new CaputringPrintStream(encoding, new ByteArrayOutputStream());
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding: " + encoding, ex);
		}
	}

	public CaputringPrintStream create() {
		return create("UTF-8");
	}

	public void reset() {
		boas.reset();
	}
	
	public String getOutput() {
		try {
			return boas.toString(encoding);
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding: " + encoding, ex);
		}
	}
	
	
	private final String encoding;
	private final ByteArrayOutputStream boas;
}
