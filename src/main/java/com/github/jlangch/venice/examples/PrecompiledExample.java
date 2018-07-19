package com.github.jlangch.venice.examples;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Venice;


public class PrecompiledExample {
	
	public static void main(final String[] args) {
		final Venice venice = new Venice();
		
		final PreCompiled precompiled = venice.precompile("(+ 1 x)");
		
		for(int ii=0; ii<100; ii++) {
			System.out.println(
					venice.eval(precompiled, Parameters.of("x", ii)));
		}
	}
	
}