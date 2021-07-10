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
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;


public class Sandbox_JavaCall_Test {
	
	@Test
	public void testRedefineJavaFn() {
		assertThrows(VncException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval(
					"(do                           \n" +
					"  (defn . [x] x)              \n" +
					"  (. 12))                       ");
		});
	}
	
	@Test
	public void testException() {
		assertThrows(VncException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval(
					"(throw (ex :VncException \"x\")))");
		});
	}
	
	@Test
	public void testRedefineJavaProxifyFn() {
		assertThrows(VncException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval(
					"(do                           \n" +
					"  (defn proxify [x] x)        \n" +
					"  (proxify 12))                 ");
		});
	}

	@Test
	public void test_system_exit_1() {
		assertThrows(SecurityException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval("(. :java.lang.System :exit 0)");
		});
	}

	@Test
	public void test_system_exit_2() {
		assertThrows(SecurityException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval("(docoll (fn [x] (. :java.lang.System :exit 0)) [1])");
		});
	}

	@Test
	public void test_system_exit_3() {
		assertThrows(SecurityException.class, () -> {
			new Venice(new RejectAllInterceptor()).eval("(map (fn [x] (do (. :java.lang.System :exit 0) x)) [1])");
		});
	}

}
