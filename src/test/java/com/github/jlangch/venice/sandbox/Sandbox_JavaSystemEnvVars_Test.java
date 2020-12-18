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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

public class Sandbox_JavaSystemEnvVars_Test {

	
	// ------------------------------------------------------------------------
	// Sandbox FAIL
	// ------------------------------------------------------------------------
		
	@Test
	public void test_RejectAccessToAllSystemEnvs_RejectAllInterceptor() {
		assertThrows(SecurityException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval("(system-env \"test\")");
		});
	}
	
	@Test
	public void test_RejectAccessToAllSystemEnvs_EmptySandbox() {
		final Interceptor interceptor = new SandboxInterceptor(new SandboxRules());				
		
		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(system-env \"test\")");
		});
	}
	
	@Test
	public void test_RejectAccessToNonStandardSystemEnvs() {
		final Interceptor interceptor = new SandboxInterceptor(
												new SandboxRules().withStandardSystemEnvs());				
		
		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(system-env \"test\")");
		});
	}
	
	@Test
	public void test_RejectAccessToNonWhitelistedSystemEnvs() {
		final Interceptor interceptor = new SandboxInterceptor(
												new SandboxRules().withSystemEnvs("user"));				
		
		assertThrows(SecurityException.class, () -> {
			new Venice(interceptor).eval("(system-env \"test\")");
		});
	}

	
	
	// ------------------------------------------------------------------------
	// Sandbox PASS
	// ------------------------------------------------------------------------

	@Test
	public void test_NoSandbox() {
		new Venice().eval("(system-env \"test\")");
	}

	@Test
	public void test_AccessToAllSystemEnvs() {
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withAllSystemEnvs());				

		new Venice(interceptor).eval("(system-env \"test\")");
	}
	
	@Test
	public void test_AccessToWhitelistedSystemEnvs() {
		final Interceptor interceptor = 
				new SandboxInterceptor(new SandboxRules().withSystemEnvs("test"));				
		
		new Venice(interceptor).eval("(system-env \"test\")");
	}

}
