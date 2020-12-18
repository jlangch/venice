/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_VeniceAgent_Test {
	
	@Test
	public void test_agent_sandbox_ok() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                             \n" +
				"   (defn add [a b] (io/file \"zz\") (+ a b 10)) \n" +
				"   (def x (agent 100))                          \n" +
				"   (send x add 5)                               \n" +
				"   (sleep 200)                                  \n" +
				"   (deref x))                                     ";

		final Object result = venice.eval(script);
		
		assertEquals(Long.valueOf(115), result);
	}
	
	@Test
	public void test_agent_sandbox_violation() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().rejectVeniceFunctions("io/file"));

		final Venice venice = new Venice(interceptor);

		final String script = 
				"(do                                             \n" +
				"   (defn add [a b] (io/file \"zz\") (+ a b 10)) \n" +
				"   (def x (agent 100 :error-mode :fail))        \n" +
				"   (send x add 5)                               \n" +
				"   (sleep 200)                                  \n" +
				"   (agent-error x))                               ";

		final SecurityException ex = (SecurityException)venice.eval(script);
		assertEquals("Venice Sandbox: Access denied to function io/file", ex.getMessage());
	}

}
