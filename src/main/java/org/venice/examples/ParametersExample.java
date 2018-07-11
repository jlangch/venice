package org.venice.examples;

import org.venice.Parameters;
import org.venice.Venice;


public class ParametersExample {
	
	public static void main(final String[] args) {
		final Venice venice = new Venice();
		
		System.out.println(
				(Long)venice.eval("(+ x y 3)", Parameters.of("x", 6, "y", 3L)));
	}
	
}