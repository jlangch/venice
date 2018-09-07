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
package com.github.jlangch.venice.impl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class ConcurrencyFunctionsTest {

	@Test
	public void test_promise() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def p (promise))                       " +
				"   (def task (fn []                        " +
				"                 (do                       " +
				"                    (sleep 500)            " +
				"                    (deliver p 123))))     " +
				"                                           " +
				"   (future task)                           " +
				"   (deref p))                              " +
				") ";

		assertEquals(Long.valueOf(123), venice.eval(script));
	}

	@Test
	public void test_future_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                             " +
				"   (def wait (fn [] (do (sleep 500) {:a 100}))) " +
				"                                                " +
				"   (let [f (future wait)]                       " +
				"        (deref f))                              " +
				") ";

		assertEquals("{:a 100}", venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_future_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (do (sleep 500) 100))) " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f 700 :timeout))            " +
				") ";

		assertEquals(Long.valueOf(100), venice.eval(script));
	}

	@Test
	public void test_future_timeout() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (do (sleep 500) 100))) " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f 300 :timeout))            " +
				") ";

		assertEquals("timeout", venice.eval(script));
	}
	
	@Test
	public void test_future_not_sandboxed() {
			final Venice venice = new Venice();
	
			final String script = 
					"(do                                        " +
					"   (def wait (fn [] (sandboxed?)))         " +
					"                                           " +
					"   (let [f (future wait)]                  " +
					"        (deref f))                         " +
					") ";

			assertFalse((Boolean)venice.eval(script));
	}
	
	@Test
	public void test_future_sandboxed() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = new SandboxInterceptor(
													new SandboxRules().add("blacklist:venice:io/file"));

		final Venice venice = new Venice(interceptor);

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (sandboxed?)))         " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		assertTrue((Boolean)venice.eval(script));
	}
	
	@Test(expected = SecurityException.class)
	public void test_future_sandbox_violation() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = new SandboxInterceptor(
													new SandboxRules().add("blacklist:venice:io/file"));

		final Venice venice = new Venice(interceptor);

		// 'io/file' is black listed, thus a call to 'io/file' must 
		// throw a SecurityException!
		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (io/file \"a.txt\")))  " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_future_sandbox_ok() {
		// all venice 'file' function blacklisted
		final Interceptor interceptor = new SandboxInterceptor(
													new SandboxRules().add("blacklist:venice:io/slurp"));

		final Venice venice = new Venice(interceptor);

		final String script = 
				"(do                                        " +
				"   (def wait (fn [] (io/file \"a.txt\")))  " +
				"                                           " +
				"   (let [f (future wait)]                  " +
				"        (deref f))                         " +
				") ";

		final File file = (File)venice.eval(script);
		assertEquals("a.txt", file.getName());
	}
	
}
