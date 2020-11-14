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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class Recursion_Auto_TCO_Test {

	@Test
	public void test_recursion_multi_arity() {
		final String script = 
				"(do                                                 \n"
				+ "  (defn factorial                                 \n"
				+ "     ([n]     (factorial n 1))                    \n"
				+ "     ([n acc] (if (== n 1)                        \n"
				+ "                acc                               \n"
				+ "                (factorial (dec n) (* acc n)))))  \n"
				+ "                                                  \n"
				+ "  (factorial 5))";

		final Venice venice = new Venice();
		
		assertEquals(120L, venice.eval(script));
	}

	@Test
	public void test_recursion_local_function() {
		final String script = 
				"(do                                               \n"
				+ "  (defn factorial [n]                           \n"
				+ "    (let [fact (fn [n acc]                      \n"
				+ "                 (if (== n 1)                   \n"
				+ "                   acc                          \n"
				+ "                   (fact (dec n) (* acc n))))]  \n"
				+ "      (fact n 1)))                              \n"
				+ "  (factorial 5))";

		final Venice venice = new Venice();
		
		assertEquals(120L, venice.eval(script));
	}

	@Test
	public void test_recursion_2nd_function() {
		final String script = 
				"(do                                        \n"
				+ "  (defn factorial [n] (factorial* n 1))  \n"
				+ "                                         \n"
				+ "  (defn factorial* [n acc]               \n"
				+ "    (if (== n 1)                         \n"
				+ "      acc                                \n"
				+ "      (factorial* (dec n) (* acc n))))   \n"
				+ "                                         \n"
				+ "  (factorial 5))";

		final Venice venice = new Venice();
		
		assertEquals(120L, venice.eval(script));
	}

	@Test
	public void test_recursion_deep_multi_arity() {
		final Venice venice = new Venice();
		
		final String script = 
				"(do                                           \n"
				+ "  (defn sum                                 \n"
				+ "     ([n]     (sum n 1))                    \n"
				+ "     ([n acc] (if (== n 1)                  \n"
				+ "                acc                         \n"
				+ "                (sum (dec n) (+ acc n)))))  \n"
				+ "                                            \n"
				+ "  (sum 1_000_000))";

		assertEquals(500000500000L, venice.eval(script));
	}

	@Test
	public void test_recursion_no_tail_pos() {
		final String script = 
				"(do                                           \n"
				+ "  (defn factorial [n] (factorial* n 1))     \n"
				+ "                                            \n"
				+ "  (defn factorial* [n acc]                  \n"
				+ "    (if (== n 1)                            \n"
				+ "      acc                                   \n"
				+ "      (do                                   \n"
				+ "        (factorial* (dec n) (* acc n))      \n"
				+ "        1)))                                \n"
				+ "                                            \n"
				+ "  (factorial 2))";

		final Venice venice = new Venice();
		
		assertEquals(1L, venice.eval(script));
	}

}
