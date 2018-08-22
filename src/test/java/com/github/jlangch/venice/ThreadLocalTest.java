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
package com.github.jlangch.venice;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ThreadLocalTest {

	@Test
	public void testAssoc() {
		final Venice venice = new Venice();

		final String s = 
				"(do                           \n" +
				"   (def ctx (thread-local))   \n" +
				"   (assoc ctx :a 1 :b 2)      \n" +
				"   (get ctx :a)               \n" +
				")                              ";
	
		assertEquals("1", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testDissoc() {
		final Venice venice = new Venice();

		final String s = 
				"(do                           \n" +
				"   (def ctx (thread-local))   \n" +
				"   (assoc ctx :a 1 :b 2)      \n" +
				"   (dissoc ctx :a)            \n" +
				"   (get ctx :a 100)           \n" +
				")                              ";
	
		assertEquals("100", venice.eval("(str " + s + ")"));
	}
}
