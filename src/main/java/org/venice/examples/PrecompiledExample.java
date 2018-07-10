package org.venice.examples;

import org.venice.Parameters;
import org.venice.PreCompiled;
import org.venice.Venice;

public class PrecompiledExample {
	
	public static void main(final String[] args) {
		Venice venice = new Venice();
		
		PreCompiled precompiled = venice.precompile("(+ 1 x)");
		
		for(int ii=0; ii<100; ii++) {
			System.out.println(venice.eval(precompiled, Parameters.of("x", ii)));
		}
	}
	
}