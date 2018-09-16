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
package com.github.jlangch.venice.sandbox;

import org.junit.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_BlacklistedVeniceFn_Test {
	
	@Test(expected = SecurityException.class)
	public void test_RejectAllInterceptor_slurp() {
		// RejectAllInterceptor -> all Venice IO functions blacklisted
		new Venice(new RejectAllInterceptor()).eval("(io/slurp \"/tmp/test\")");
	}
	
	@Test(expected = SecurityException.class)
	public void test_rejectAllVeniceIoFunctions_slurp() {
		// Sandbox::rejectAllVeniceIoFunctions() -> all Venice IO functions blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().rejectAllVeniceIoFunctions());				

		new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
	}
	
	@Test(expected = SecurityException.class)
	public void test_blacklistedIO_slurp() {
		// all Venice IO functions blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("*io*"));
	
		new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
	}
	
	@Test(expected = SecurityException.class)
	public void test_withBlacklistedVeniceFn_slurp() {
		// Venice 'slurp' function blacklisted
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withBlacklistedVeniceFn("io/slurp"));				
		
		new Venice(interceptor).eval("(io/slurp \"/tmp/test\")");
	}

}
