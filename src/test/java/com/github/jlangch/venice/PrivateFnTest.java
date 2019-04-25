/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class PrivateFnTest {

	@Test
	public void test_private_fn_1() {
		final String s = 
				"(do                               \n" +
				"   (load-module :test)            \n" +
				"   (test/test-fn-private 100))      ";

		assertThrows(VncException.class, () -> new Venice().eval(s));
	}

	@Test
	public void test_private_fn_2() {
		final String s = 
				"(do                                             \n" +
				"   (load-module :test)                          \n" +
				"   (let [x (fn [m] (test/test-fn-private m))]   \n" +
				"       (x 100)))                                  ";

		assertThrows(VncException.class, () -> new Venice().eval(s));
	}

	@Test
	public void test_private_fn_3() {
		final String s = 
				"(do                                          \n" +
				"   (load-module :test)                       \n" +
				"   (let [f \"test/test-fn-private\"]         \n" +
				"         ((resolve (symbol f)) 100)))          ";

		assertThrows(VncException.class, () -> new Venice().eval(s));
	}

	@Test
	public void test_private_fn_4() {
		final String s = 
				"(do                                             \n" +
				"   (load-module :test)                          \n" +
				"   (let [f \"test/test-fn-private\"             \n" +
				"         x (fn [m] ((resolve (symbol f)) m))]   \n" +
				"       (x 100)))                                 ";

		assertThrows(VncException.class, () -> new Venice().eval(s));
	}
	
	@Test
	public void test_private_fn_5() {
		final String s = 
				"(do                                             \n" +
				"   (load-module :test)                          \n" +
				"   (test/unexpected-error \"sdgh\"))              ";

		// no exception private fn call is ok
		assertDoesNotThrow(() -> new Venice().eval(s));
	}

	@Test
	public void test_public_fn() {
		final String s = 
				"(do                               \n" +
				"   (load-module :test)            \n" +
				"   (test/test-fn 100))              ";

		assertDoesNotThrow(() -> new Venice().eval(s));
	}
}
