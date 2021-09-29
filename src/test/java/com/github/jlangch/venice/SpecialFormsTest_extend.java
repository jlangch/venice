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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_extend {
	
	@Test
	public void test_extend_basic_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                     \n" +
				"  (ns test)                             \n" +
				"  (defprotocol P (foo [x]))             \n" +
				"  (extend :core/long P (foo [x] x)))      ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                     \n" +
				"  (ns test)                             \n" +
				"  (defprotocol P (foo [x] [x y] nil))   \n" +
				"  (extend :core/long P (foo [x] x))     \n" +
				"  (foo 1))                                ";

		assertEquals(1L, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                     \n" +
				"  (ns test)                             \n" +
				"  (defprotocol P (foo [x] [x y] nil))  \n" +
				"  (extend :core/long P (foo [x] x))     \n" +
				"  (foo 1 2))                              ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_4a() {
		final Venice venice = new Venice();

		final String script =
				"(do                                            \n" +
				"  (ns test)                                    \n" +
				"  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
				"  (extend :core/long P (foo [x y] x))          \n" +
				"  (foo 1))                                       ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_4b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                            \n" +
				"  (ns test)                                    \n" +
				"  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
				"  (extend :core/long P (foo [x y] x))          \n" +
				"  (foo 2 100))                                   ";

		assertEquals(2L, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_4c() {
		final Venice venice = new Venice();

		final String script =
				"(do                                            \n" +
				"  (ns test)                                    \n" +
				"  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
				"  (extend :core/long P (foo [x y] x))          \n" +
				"  (foo 2 3 4))                                   ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_extend_basic_4d() {
		final Venice venice = new Venice();

		final String script =
				"(do                                            \n" +
				"  (ns test)                                    \n" +
				"  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
				"  (extend :core/long P (foo [x y] x))          \n" +
				"  (foo 2 3 4 5))                                   ";

		assertThrows(
				ArityException.class,
				() -> venice.eval(script));					
	}

	@Test
	public void test_extend_deftype_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                    \n" +
				"  (defprotocol P (foo [x]))            \n" +
				"  (deftype :person [name :string]      \n" +
				"           P (foo [x] (:name x)))      \n" +
				"  (foo (person. \"joe\")))               ";

		assertEquals("joe", venice.eval(script));					
	}

	@Test
	public void test_extend_deftype_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (defprotocol P (foo [x]) (bar [x]))               \n" +
				"  (deftype :person [name :string last :string]      \n" +
				"           P (foo [x] (:name x))                    \n" +
				"             (bar [x] (:last x)))                   \n" +
				"  (def p (person. \"joe\" \"smith\"))               \n" +
				"  (pr-str [(foo p) (bar p)]))";

		assertEquals("[\"joe\" \"smith\"]", venice.eval(script));					
	}

}
