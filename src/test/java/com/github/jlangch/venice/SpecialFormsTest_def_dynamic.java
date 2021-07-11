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

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_def_dynamic {
	
	@Test
	public void test_def_dynamic_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   x)                           ";

		assertEquals(100L, venice.eval(script));
	}
	
	@Test
	public void test_def_dynamic_1a() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x nil)         \n" +
				"   nil)                          ";

		assertEquals(null, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (binding [x 200]            \n" +
				"     x))                         ";

		assertEquals(200L, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_2a() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (binding [x nil]            \n" +
				"     x))                         ";

		assertEquals(null, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_2b() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x nil)         \n" +
				"   (binding [x 200]            \n" +
				"     x))                         ";

		assertEquals(200L, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_3() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (binding [x 200]            \n" +
				"     (binding [x 300]          \n" +
				"        x)))                     ";

		assertEquals(300L, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_4() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (binding [x 200]            \n" +
				"     (binding [x 300]          \n" +
				"        x)                     \n" +
				"     x))                         ";

		assertEquals(200L, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_5() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (binding [x 200]            \n" +
				"     (binding [x 300]          \n" +
				"       x)                      \n" +
				"     x)                        \n" +
				"   x)                            ";

		assertEquals(100L, venice.eval(script));
	}

	@Test
	public void test_def_dynamic_6() {
		final Venice venice = new Venice();

		final String script = 
				"(do                            \n" +
				"   (def-dynamic x 100)         \n" +
				"   (let [x 200]                \n" +
				"     x))                         ";

		assertEquals(200L, venice.eval(script));
	}

	@Test
	public void test_binding_mutability() {
		final Venice venice = new Venice();

		// binding-introduced bindings are thread-locally mutable
		
		final String script = 
				"(do                            \n" +
				"   (binding [x 1]              \n" +
				"     (set! x 2)                \n" +
				"     x))                         ";

		assertEquals(2L, venice.eval(script));
	}

	@Test
	public void test_binding_qualified() {
		final Venice venice = new Venice();

		// binding-introduced bindings are thread-locally mutable
		
		final String script = 
				"(do                            \n" +
				"   (binding [user/x 1]         \n" +
				"     user/x))                    ";

		assertEquals(1L, venice.eval(script));
	}

}
