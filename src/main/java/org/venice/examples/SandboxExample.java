package org.venice.examples;

import org.venice.Venice;
import org.venice.javainterop.JavaInterceptor;
import org.venice.javainterop.JavaSandboxInterceptor;
import org.venice.javainterop.SandboxRules;


public class SandboxExample {
	
	public static void main(final String[] args) {
		// --------------------------------------------------------------
		// Reject all Java calls and all Venice IO functions
		// --------------------------------------------------------------
		try {
			sandboxing_strict();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		// --------------------------------------------------------------
		// Allow dedicated Java calls and reject all Venice IO functions 
		// --------------------------------------------------------------
		try {
			sandboxing_java_calls_with_safe_venice_func();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void sandboxing_strict() {
		// disable all Java calls and all Venice IO functions
		// like 'println', 'slurp', ...
		//
		// Note: Using the RejectAllInterceptor has the same effect
		final JavaInterceptor interceptor = new JavaSandboxInterceptor(
													new SandboxRules().rejectAllVeniceIoFunctions());
		
		final Venice venice = new Venice(interceptor);

		// => FAIL (Venice IO function) with Sandbox SecurityException
		venice.eval("(println 100)"); 
	}
	
	private static void sandboxing_java_calls_with_safe_venice_func() {
		final JavaInterceptor interceptor =
				new JavaSandboxInterceptor(
						new SandboxRules()
								.rejectAllVeniceIoFunctions()
								.add(
									"java.lang.Long",  // Math::min, Math::max arguments/return type
									"java.lang.Boolean",  // ArrayList::add return type
									"java.lang.Math:min", 
									"java.lang.Math:max", 
									"java.time.ZonedDateTime:*", 
									"java.util.ArrayList:new",
									"java.util.ArrayList:add"));

		final Venice venice = new Venice(interceptor);

		// => OK (static method)
		venice.eval("(. :java.lang.Math :min 20 30)"); 
		
		// => OK (static method)
		venice.eval("(. :java.lang.Math :max 20 30)"); 
		
		// => OK (constructor & instance method)
		venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))"); 
		
		// => OK (constructor)
		venice.eval("(. :java.util.ArrayList :new)");
	
		// => OK (constructor & instance method)
		venice.eval(
				"(doto (. :java.util.ArrayList :new)  " +
				"	   (. :add 1)                     " +
				"	   (. :add 2))                    ");

		// => FAIL (static method) with Sandbox SecurityException
		venice.eval("(. :java.lang.System :exit 0)"); 
	}
	
}