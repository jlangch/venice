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
package org.venice.javainterop;

import org.junit.Test;
import org.venice.Venice;


public class BlackListedFunctionTest {
	
	@Test(expected = SecurityException.class)
	public void test_prn() {
		new Venice(new RejectAllInterceptor()).eval("(prn 100)");
	}
	
	@Test(expected = SecurityException.class)
	public void test_println() {
		new Venice(new RejectAllInterceptor()).eval("(println 100)");
	}
	
	@Test(expected = SecurityException.class)
	public void test_readline() {
		new Venice(new RejectAllInterceptor()).eval("(readline \"> \")");
	}
	
	@Test(expected = SecurityException.class)
	public void test_slurp() {
		new Venice(new RejectAllInterceptor()).eval("(slurp \"/tmp/test\")");
	}
	
	@Test(expected = SecurityException.class)
	public void test_prn_blacklisted() {
		// all venice 'prn' function blacklisted
		final JavaInterceptor interceptor = new JavaSandboxInterceptor(
													new SandboxRules().add("blacklist:venice:prn"));
		
		new Venice(interceptor).eval("(prn 100)");
	}
	
	@Test(expected = SecurityException.class)
	public void test_prn_blacklisted_io_1() {
		// all venice IO functions blacklisted
		final JavaInterceptor interceptor = new JavaSandboxInterceptor(
													new SandboxRules().add("blacklist:venice:*io*"));

		new Venice(interceptor).eval("(prn 100)");
	}
	
	@Test(expected = SecurityException.class)
	public void test_prn_blacklisted_io_2() {
		// all venice IO functions blacklisted
		final JavaInterceptor interceptor = new JavaSandboxInterceptor(
													new SandboxRules().rejectAllVeniceIoFunctions());

		new Venice(interceptor).eval("(prn 100)");
	}
	
	@Test(expected = SecurityException.class)
	public void test_system_exit() {
		new Venice(new RejectAllInterceptor()).eval("(. :java.lang.System :exit 0)");
	}

}
