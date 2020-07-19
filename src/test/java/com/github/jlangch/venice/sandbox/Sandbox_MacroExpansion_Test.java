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
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;


public class Sandbox_MacroExpansion_Test {
	
	@Test
	public void test_execution_at_macro_expansion_ok() {
		final VeniceInterpreter venice = new VeniceInterpreter(new RejectAllInterceptor());	
		
		final Env env = venice.createEnv(true, false, new VncKeyword("script"));
		
		final String macro =
				"(do                                             \n" +
				"  (ns alpha)                                    \n" +
				"                                                \n" +
				"  (defmacro whenn [test form]                   \n" +
				"    (do                                         \n" +
				"      `(if ~test ~form nil))))                    ";
		
		// READ/EVAL macro definition is OK
		venice.RE(macro, "test", env);


		final String script = "(alpha/whenn true 100)";

		// READ macro usage is OK
		venice.READ(script, "test"); 
		
		// MACROEXPAND macro usage is OK
		venice.MACROEXPAND(venice.READ(script, "test"), env);

		
		// READ/EVAL with macro expansion is OK
		final VncVal result = venice.RE(script, "test", env);
		assertEquals("100", result.toString());
	}


	@Test
	public void test_execution_at_macro_expansion_sandbox_violation() {
		final VeniceInterpreter venice = new VeniceInterpreter(new RejectAllInterceptor());	
		
		final Env env = venice.createEnv(true, false, new VncKeyword("script"));
		
		final String macro =
				"(do                                             \n" +
				"  (ns alpha)                                    \n" +
				"                                                \n" +
				"  (defmacro whenn [test form]                   \n" +
				"    (do                                         \n" +
				"      (. :java.lang.System :exit 0)             \n" +
				"      `(if ~test ~form nil))))                    ";

		// READ/EVAL macro definition is OK
		venice.RE(macro, "test", env);


		final String script = "(alpha/whenn true 100)";

		// READ macro usage is OK
		venice.READ(script, "test"); 
		
		
		// MACROEXPAND macro usage must FAIL
		try {
			// (. :java.lang.System :exit 0) is executed while the macro expands
			// -> SecurityException from the sandbox
			
			venice.MACROEXPAND(venice.READ(script, "test"), env);
			fail();
		}
		catch(SecurityException ex) {
			assertEquals(
				"Venice Sandbox (RejectAllInterceptor): Access denied to target java.lang.System. File <unknown> (1,1)",
				ex.getMessage());
		}
	}

	
}
