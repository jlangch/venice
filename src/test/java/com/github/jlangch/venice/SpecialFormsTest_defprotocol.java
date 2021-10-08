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


public class SpecialFormsTest_defprotocol {
	
	@Test
	public void test_protocol_basic_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                          \n" +
				"  (ns test)                  \n" +
				"  (defprotocol P (foo [x]))  \n" +
				"  (pr-str P))                  ";

		assertEquals("test/P", venice.eval(script));					
	}
	
	@Test
	public void test_protocol_basic_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                          \n" +
				"  (ns test)                  \n" +
				"  (defprotocol P             \n" +
				"     (foo [x] [x y] nil))    \n" +
				"  (pr-str P))                  ";

		assertEquals("test/P", venice.eval(script));					
	}
	
	@Test
	public void test_protocol_basic_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                          \n" +
				"  (ns test)                  \n" +
				"  (defprotocol P             \n" +
				"     (foo [x] [x y] nil)     \n" +
				"     (bar [x] [x y] nil))    \n" +
				"  (pr-str P))                  ";

		assertEquals("test/P", venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                              \n" +
				"  (ns test)                      \n" +
				"  (defprotocol P                 \n" +
				"     (foo [x] [x y]))            \n" +
				"  (extend :core/long P)          \n" +
				"  (foo 10))                        ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                              \n" +
				"  (ns test)                      \n" +
				"  (defprotocol P                 \n" +
				"     (foo [x] [x y] nil))        \n" +
				"  (extend :core/long P)          \n" +
				"  (foo 10))                        ";

		assertEquals(null, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                          \n" +
				"  (ns test)                  \n" +
				"  (defprotocol P             \n" +
				"     (foo [x] [x y] 100))    \n" +
				"  (foo 10))                    ";

		assertEquals(100L, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                              \n" +
				"  (ns test)                      \n" +
				"  (defprotocol P                 \n" +
				"     (foo [x] [x y] x))          \n" +
				"  (extend :core/long P)          \n" +
				"  (foo 10))                        ";

		assertEquals(10L, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_5() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"  (ns test)                   \n" +
				"  (defprotocol P              \n" +
				"     (foo [x] [x y] (+ 1 2))) \n" +
				"  (extend :core/long P)       \n" +
				"  (foo 10))                     ";

		assertEquals(3L, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_6() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"  (ns test)                   \n" +
				"  (defprotocol P              \n" +
				"     (foo [x] [x y] (+ 1 x))) \n" +
				"  (extend :core/long P)       \n" +
				"  (foo 10))                     ";

		assertEquals(11L, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_7() {
		final Venice venice = new Venice();

		final String script =
				"(do                                \n" +
				"  (ns test)                        \n" +
				"  (defprotocol P                   \n" +
				"     (foo [x] [x y] (vector 1 2))) \n" +
				"  (pr-str (foo 10)))                 ";

		assertEquals("[1 2]", venice.eval(script));					
	}
	
	@Test
	public void test_protocol_default_8() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"  (ns test)                   \n" +
				"  (defprotocol P              \n" +
				"     (foo [x] [x y] nil x))   \n" +
				"  (extend :core/long P)       \n" +
				"  (foo 10))                     ";

		assertEquals(10L, venice.eval(script));					
	}
	
	@Test
	public void test_protocol_type() {
		final Venice venice = new Venice();

		final String script =
				"(do                          \n" +
				"  (ns test)                  \n" +
				"  (defprotocol P (foo [x]))  \n" +
				"  (pr-str (type P)))           ";

		assertEquals(":core/protocol", venice.eval(script));					
	}
	
	@Test
	public void test_protocol_doc() {
		final Venice venice = new Venice();

		final String script =
				"(do                               \n" +
				"  (ns test)                       \n" +
				"  (defprotocol                    \n" +
				"     ^{ :doc \"test protocol\" }  \n" +
				"     P (foo [x]))                 \n" +
				"  (with-out-str (doc P)))           ";

		assertEquals("test protocol\n", venice.eval(script));					
	}
	
		
	// ------------------------------------------------------------------------
	// Errors
	// ------------------------------------------------------------------------

	
	@Test
	public void test_protocol_error_1() {
		final Venice venice = new Venice();

		assertThrows(
				ArityException.class,
				() -> venice.eval("(defprotocol)"));					
	}
	
	@Test
	public void test_protocol_error_2() {
		final Venice venice = new Venice();

		assertThrows(
				ArityException.class,
				() -> venice.eval("(defprotocol P)"));					
	}

	@Test
	public void test_protocol_error_3() {
		final Venice venice = new Venice();

		assertThrows(
				VncException.class,
				() -> venice.eval("(defprotocol P 1)"));					
	}

	@Test
	public void test_protocol_error_4() {
		final Venice venice = new Venice();

		assertThrows(
				VncException.class,
				() -> venice.eval("(defprotocol P (foo))"));					
	}

	@Test
	public void test_protocol_error_5() {
		final Venice venice = new Venice();

		assertThrows(
				VncException.class,
				() -> venice.eval("(defprotocol P (foo [1]))"));					
	}

	@Test
	public void test_protocol_error_6() {
		final Venice venice = new Venice();

		assertThrows(
				VncException.class,
				() -> venice.eval("(defprotocol P (foo [x]) 1)"));					
	}

}
