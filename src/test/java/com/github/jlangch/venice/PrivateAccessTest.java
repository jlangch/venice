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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class PrivateAccessTest {

	@Test
	public void test_access_var() {
		final Venice venice = new Venice();

		final String s = 
				"(do                       \n" +
				"   (ns alpha)             \n" +
				"   (def ^:private y 100)  \n" +
				"                          \n" +
				"   (ns beta)              \n" +
				"   alpha/y                \n" +
				")                           ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}
	
	@Test
	public void test_access_function() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                  \n" +
				"   (ns alpha)                        \n" +
				"   (defn ^:private add [x] (+ x 10)) \n" +
				"                                     \n" +
				"   (ns beta)                         \n" +
				"   (alpha/add 5)                     \n" +
				")                                      ";
	
		assertThrows(VncException.class, () -> venice.eval(s));
	}
}
