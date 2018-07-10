package org.venice.examples;

import org.venice.Venice;

public class EvalExample {
	
	public static void main(final String[] args) {
		Venice venice = new Venice();
		System.out.println((Long)venice.eval("(+ 1 2)"));
	}
	
}