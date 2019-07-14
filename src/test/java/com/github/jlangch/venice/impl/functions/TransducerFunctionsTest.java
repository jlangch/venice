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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class TransducerFunctionsTest {
	
	@Test
	public void test_transduce() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (map #(+ % 1)))              \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (str (transduce xf + coll)))           ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (map #(+ % 1)))              \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (str (transduce xf conj coll)))        ";
		
		final String script3 =
				"(do                                              \n" +
				"  (def xf (comp (map #(+ % 10)) (filter odd?)))  \n" +
				"  (def coll [1 2 3 4 5 6])                       \n" +
				"  (str (transduce xf conj coll)))                 ";

		assertEquals("27", venice.eval(script1));	
		assertEquals("[2 3 4 5 6 7]", venice.eval(script2));	
		assertEquals("[11 13 15]", venice.eval(script3));	
	}
	
	@Test
	public void test_map() {
		final Venice venice = new Venice();

		assertEquals("(2 3 4 5 6)", venice.eval("(str (map inc '(1 2 3 4 5)))"));

		assertEquals("(2 3 4 5 6)", venice.eval("(str (map inc [1 2 3 4 5]))"));

		assertEquals("(5 7 9)", venice.eval("(str (map + [1 2 3] [4 5 6]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3] [4 5 6] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3 9 9] [4 5 6 9] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(str (map + [1 2 3] [4 5 6 9] [7 8 9]))"));

		assertEquals("(1 3)", venice.eval("(str (map (fn [x] (get x :a)) [{:a 1 :b 2} {:a 3 :b 4}]))"));
		
		assertEquals("(true false true)", venice.eval("(str (map not [false, true, false]))"));
		
		assertEquals("((1 1) (2 2) (3 3))", venice.eval("(str (map list [1 2 3] [1 2 3]))"));	
	}	

	public void test_drop() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (drop 0 '()))"));
		assertEquals("()", venice.eval("(str (drop 1 '()))"));
		
		assertEquals("(1)", venice.eval("(str (drop 0 '(1)))"));
		assertEquals("()", venice.eval("(str (drop 1 '(1)))"));
		assertEquals("()", venice.eval("(str (drop 2 '(1)))"));
		
		assertEquals("(1 2)", venice.eval("(str (drop 0 '(1 2)))"));
		assertEquals("(2)", venice.eval("(str (drop 1 '(1 2)))"));
		assertEquals("()", venice.eval("(str (drop 2 '(1 2)))"));
		assertEquals("()", venice.eval("(str (drop 3 '(1 2)))"));
		
		assertEquals("(1 2 3)", venice.eval("(str (drop 0 '(1 2 3)))"));
		assertEquals("(2 3)", venice.eval("(str (drop 1 '(1 2 3)))"));
		assertEquals("(3)", venice.eval("(str (drop 2 '(1 2 3)))"));
		assertEquals("()", venice.eval("(str (drop 3 '(1 2 3)))"));
		assertEquals("()", venice.eval("(str (drop 4 '(1 2 3)))"));

		
		assertEquals("[]", venice.eval("(str (drop 0 []))"));
		assertEquals("[]", venice.eval("(str (drop 1 []))"));
		
		assertEquals("[1]", venice.eval("(str (drop 0 [1]))"));
		assertEquals("[]", venice.eval("(str (drop 1 [1]))"));
		assertEquals("[1]", venice.eval("(str (drop 2 [1]))"));
		
		assertEquals("[1 2]", venice.eval("(str (drop 0 [1 2]))"));
		assertEquals("[2]", venice.eval("(str (drop 1 [1 2]))"));
		assertEquals("[]", venice.eval("(str (drop 2 [1 2]))"));
		assertEquals("[]", venice.eval("(str (drop 3 [1 2]))"));
		
		assertEquals("[1 2 3]", venice.eval("(str (drop 0 [1 2 3]))"));
		assertEquals("[2 3]", venice.eval("(str (drop 1 [1 2 3]))"));
		assertEquals("[3]", venice.eval("(str (drop 2 [1 2 3]))"));
		assertEquals("[]", venice.eval("(str (drop 3 [1 2 3]))"));
		assertEquals("[]", venice.eval("(str (drop 4 [1 2 3]))"));
	}

	@Test
	public void test_drop_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '()))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("(4 5)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("()", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(3 4)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(3 2 1 0)", venice.eval("(str (drop-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));

		
		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) []))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[4 5]", venice.eval("(str (drop-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[3 4]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[3 2 1 0]", venice.eval("(str (drop-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}

	@Test
	public void test_take() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (take 0 '()))"));
		assertEquals("()", venice.eval("(str (take 1 '()))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1)))"));
		assertEquals("(1)", venice.eval("(str (take 2 '(1)))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1 2)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take 2 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take 3 '(1 2)))"));
		
		assertEquals("()", venice.eval("(str (take 0 '(1 2 3)))"));
		assertEquals("(1)", venice.eval("(str (take 1 '(1 2 3)))"));
		assertEquals("(1 2)", venice.eval("(str (take 2 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(str (take 3 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(str (take 4 '(1 2 3)))"));

		
		assertEquals("[]", venice.eval("(str (take 0 []))"));
		assertEquals("[]", venice.eval("(str (take 1 []))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1]))"));
		assertEquals("[1]", venice.eval("(str (take 2 [1]))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1 2]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take 2 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take 3 [1 2]))"));
		
		assertEquals("[]", venice.eval("(str (take 0 [1 2 3]))"));
		assertEquals("[1]", venice.eval("(str (take 1 [1 2 3]))"));
		assertEquals("[1 2]", venice.eval("(str (take 2 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(str (take 3 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(str (take 4 [1 2 3]))"));
	}

	@Test
	public void test_take_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '()))"));
		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("()", venice.eval("(str (take-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("(1)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(1)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(1 2)", venice.eval("(str (take-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));


		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) []))"));
		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[]", venice.eval("(str (take-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[1]", venice.eval("(str (take-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[1]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[1 2]", venice.eval("(str (take-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}
	
	@Test
	public void test_keep() {
		final Venice venice = new Venice();

		assertEquals("(false true false)", venice.eval("(str (keep even? (range 1 4)))"));
	}
	
	@Test
	public void test_dedupe() {
		final Venice venice = new Venice();

		assertEquals("(0)", venice.eval("(str (dedupe '(0 0 0)))"));
		assertEquals("(0 1 2 1 3)", venice.eval("(str (dedupe '(0 1 2 2 2 1 3)))"));
		assertEquals("[0]", venice.eval("(str (dedupe [0 0 0]))"));
		assertEquals("[0 1 2 1 3]", venice.eval("(str (dedupe [0 1 2 2 2 1 3]))"));
	}
	
	@Test
	public void test_filter() {
		final Venice venice = new Venice();

		assertEquals("(2 4 6 8)", venice.eval("(str (filter even? (range 1 10 1)))"));
		assertEquals("(2 4 6 8)", venice.eval("(str (filter (fn [x] (even? x)) (range 1 10 1)))"));
		assertEquals("(2 4 6 8)", venice.eval("(str (filter #(even? %) (range 1 10 1)))"));
	}
	
	@Test
	public void test_remove() {
		final Venice venice = new Venice();

		assertEquals("(1 3 5 7 9)", venice.eval("(str (remove even? (range 1 10 1)))"));
	}
	
}
