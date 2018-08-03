package com.github.jlangch.venice.util;

import java.io.OutputStream;


/**
 * The <tt>NullOutputStream</tt> discards all bytes written to this
 * output stream.
 * 
 * @author juerg
 */
public class NullOutputStream extends OutputStream {

	@Override
	public void write(int b) {
	}

}
