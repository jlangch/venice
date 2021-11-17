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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class TransducerFunctionsTest {
	
	@Test
	public void test_transduce() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                              \n" +
				"  (def xf (comp (map #(+ % 10)) (filter odd?)))  \n" +
				"  (def coll [1 2 3 4 5 6])                       \n" +
				"  (pr-str (transduce xf conj coll)))               ";
		
		final String script2 =
				"(do                                              \n" +
				"  (def xf (comp (take 3) (drop 2)))              \n" +
				"  (def coll [1 2 3 4 5 6])                       \n" +
				"  (pr-str (transduce xf conj coll)))               ";
		
		final String script3 =
				"(do                                              \n" +
				"  (def xf (comp (drop 2) (take 3)))              \n" +
				"  (def coll [1 2 3 4 5 6])                       \n" +
				"  (pr-str (transduce xf conj coll)))               ";

		assertEquals("[11 13 15]", venice.eval(script1));	
		assertEquals("[3]", venice.eval(script2));	
		assertEquals("[3 4 5]", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_2() {
		final Venice venice = new Venice();
				
		final String script1 =
				"(do                                              \n" +
				"  (def xf (comp (drop 2) (take 3)))              \n" +
				"  (def coll [1 2 3 4 5 6])                       \n" +
				"  (pr-str (transduce xf conj coll)))               ";
		
		final String script2 =
				"(do                                                   \n" +
				"  (def xf (comp (drop 2) (take 3) (drop 1) (take 1))) \n" +
				"  (def coll [1 2 3 4 5 6])                            \n" +
				"  (pr-str (transduce xf conj coll)))                     ";

		assertEquals("[3 4 5]", venice.eval(script1));	
		assertEquals("[4]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_3() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (map #(* % 10))                      \n" +
				"            (map #(- % 5))                       \n" +
				"            (sorted compare)                     \n" +
				"            (drop 3)                             \n" +
				"            (take 2)                             \n" +
				"            (reverse)))                          \n" +
				"  (def coll [5 2 1 6 4 3])                       \n" +
				"  (pr-str (transduce xf conj coll)))               ";

		assertEquals("[45 35]", venice.eval(script));	
	}
	
	@Test
	public void test_transduce_4() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (flatten)                            \n" +
				"            (map #(* % 10))                      \n" +
				"            (flatten)                            \n" +
				"            (map #(- % 5))                       \n" +
				"            (sorted compare)                     \n" +
				"            (flatten)                            \n" +
				"            (drop 3)                             \n" +
				"            (take 2)                             \n" +
				"            (reverse)))                          \n" +
				"  (def coll [5 [2 1 6] '(4) 3])                  \n" +
				"  (pr-str (transduce xf conj coll)))               ";

		assertEquals("[45 35]", venice.eval(script));	
	}
	
	@Test
	public void test_transduce_5() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (sorted compare)                     \n" +
				"            (reverse)                            \n" +
				"            (reverse)))                          \n" +
				"  (def coll [3 2 5 4 1])                         \n" +
				"  (pr-str (transduce xf conj coll)))               ";

		assertEquals("[1 2 3 4 5]", venice.eval(script));	
	}

	@Test
	public void test_transduce_map() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (map #(+ % 1)))              \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (map #(+ % 1)))              \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf conj coll)))     ";
		
		final String script3 =
				"(pr-str (transduce (comp (map :ip)) conj [{:ip 6}]))";
		
		final String script4 =
				"(pr-str (transduce (map :ip) conj [{:ip 6}]))";
		

		assertEquals("27", venice.eval(script1));	
		assertEquals("[2 3 4 5 6 7]", venice.eval(script2));	
		assertEquals("[6]", venice.eval(script3));	
		assertEquals("[6]", venice.eval(script4));	
	}

	@Test
	public void test_transduce_map_indexed() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (map-indexed vector))        \n" +
				"  (def coll [:a :b :c])                \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("[[0 :a] [1 :b] [2 :c]]", venice.eval(script1));	
	}
	
	@Test
	public void test_transduce_filter() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (filter odd?))               \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (filter odd?))               \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf conj coll)))     ";
		
		final String script3 =
				"(do                                    \n" +
				"  (def xf (filter #{3 4 5}))           \n" +
				"  (def coll [1 3 5 7 9])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("9", venice.eval(script1));	
		assertEquals("[1 3 5]", venice.eval(script2));	
		assertEquals("[3 5]", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_keep() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (keep identity))             \n" +
				"  (def coll [1 nil 3 nil 5 6])         \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (keep identity))             \n" +
				"  (def coll [1 nil 3 nil 5 6])         \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("15", venice.eval(script1));	
		assertEquals("[1 3 5 6]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_dedupe() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (dedupe))                    \n" +
				"  (def coll [1 2 2 2 3])               \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (dedupe))                    \n" +
				"  (def coll [1 2 2 2 3])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("6", venice.eval(script1));	
		assertEquals("[1 2 3]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_drop() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (drop 2))                    \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (transduce xf + coll))                 ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (drop 2))                    \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals(12L, venice.eval(script1));	
		assertEquals("[3 4 5]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_drop_while() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (drop-while neg?))           \n" +
				"  (def coll [-2 -1 0 1 2 3])           \n" +
				"  (transduce xf + coll))                 ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (drop-while neg?))           \n" +
				"  (def coll [-2 -1 0 1 2 3])           \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals(6L, venice.eval(script1));	
		assertEquals("[0 1 2 3]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_drop_last() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (drop-last 2))               \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (transduce xf + coll))                 ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (drop-last 2))               \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals(6L, venice.eval(script1));	
		assertEquals("[1 2 3]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_take() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (take 3))                    \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (take 3))                    \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("6", venice.eval(script1));	
		assertEquals("[1 2 3]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_take_while() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (take-while neg?))           \n" +
				"  (def coll [-2 -1 0 1 2 3])           \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (take-while neg?))           \n" +
				"  (def coll [-2 -1 0 1 2 3])           \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("-3", venice.eval(script1));	
		assertEquals("[-2 -1]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_take_last() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (take-last 2))               \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (transduce xf + coll))                 ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (comp (take-last 2)          \n" +
				"                (map #(* % 2))))       \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (transduce xf + coll))                  ";
		
		final String script3 =
				"(do                                    \n" +
				"  (def xf (take-last 2))               \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals(9L, venice.eval(script1));	
		assertEquals(18L, venice.eval(script2));	
		assertEquals("[4 5]", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_remove() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (remove odd?))               \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (remove odd?))               \n" +
				"  (def coll [1 2 3 4 5 6])             \n" +
				"  (pr-str (transduce xf conj coll)))     ";
		
		final String script3 =
				"(do                                    \n" +
				"  (def xf (remove #{3 5}))             \n" +
				"  (def coll [1 3 5 7 9])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";
		
		final String script4 =
				"(pr-str  (transduce (remove #{3 5}) conj [1 3 5 7 9]))";

		assertEquals("12", venice.eval(script1));	
		assertEquals("[2 4 6]", venice.eval(script2));	
		assertEquals("[1 7 9]", venice.eval(script3));	
		assertEquals("[1 7 9]", venice.eval(script4));	
	}
	
	@Test
	public void test_transduce_distinct() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                    \n" +
				"  (def xf (distinct))                  \n" +
				"  (def coll [1 2 2 2 3 4 4 2 5])       \n" +
				"  (pr-str (transduce xf + coll)))        ";
		
		final String script2 =
				"(do                                    \n" +
				"  (def xf (distinct))                  \n" +
				"  (def coll [1 2 2 2 3 4 4 2 5])       \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("15", venice.eval(script1));	
		assertEquals("[1 2 3 4 5]", venice.eval(script2));	
	}
	
	@Test
	public void test_transduce_sorted() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                    \n" +
				"  (def xf (sorted compare))            \n" +
				"  (def coll [5 3 2 4 1])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("[1 2 3 4 5]", venice.eval(script));	
	}
	
	@Test
	public void test_transduce_reverse() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                    \n" +
				"  (def xf (reverse))                   \n" +
				"  (def coll [1 2 3 4 5])               \n" +
				"  (pr-str (transduce xf conj coll)))     ";

		assertEquals("[5 4 3 2 1]", venice.eval(script));	
	}
	
	@Test
	public void test_transduce_flatten() {
		final Venice venice = new Venice();
				
		final String script =
				"(do                                     \n" +
				"  (def xf (flatten))                    \n" +
				"  (def coll [1 [2 3] '(4) 5])           \n" +
				"  (pr-str (transduce xf conj coll)))      ";

		assertEquals("[1 2 3 4 5]", venice.eval(script));	
	}
	
	@Test
	public void test_transduce_halt_when() {
		final Venice venice = new Venice();
		
		final String script1 =
				"(do                                  \n" +
				"  (def xf (comp                      \n" +
				"            (halt-when #(== % 10))   \n" +
				"            (filter odd?)))          \n" +
				"  (def coll [1 2 3 4 5 6 7 8 9])     \n" +
				"  (pr-str (transduce xf conj coll)))   ";
		
		final String script2a =
				"(do                                  \n" +
				"  (def xf (comp                      \n" +
				"            (halt-when #(> % 5))     \n" +
				"            (filter odd?)))          \n" +
				"  (def coll [1 2 3 4 5 6 7 8 9])     \n" +
				"  (pr-str (transduce xf conj coll)))   ";
		
		final String script2b =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (halt-when #(> % 5)                  \n" +
				"                       (fn [res in] in))         \n" +
				"            (filter odd?)))                      \n" +
				"  (def coll [1 2 3 4 5 6 7 8 9])                 \n" +
				"  (pr-str (transduce xf conj coll)))               ";
		
		// find first even (found)
		final String script3a =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter long?)                       \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (fn [res in] in)          \n" +
				"                       (constantly nil))))       \n" +
				"  (pr-str (transduce xf conj [1 3 6 7 9])))        ";
		
		// find first even (not found)
		final String script3b =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter long?)                       \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (fn [res in] in)          \n" +
				"                       (constantly nil))))       \n" +
				"  (pr-str (transduce xf conj [1 3 5 7 9])))        ";
		
		// find first
		final String script3c =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter long?)                       \n" +
				"            (halt-when (constantly true)         \n" +
				"                       (fn [res in] in)          \n" +
				"                       (constantly nil))))       \n" +
				"  (pr-str (transduce xf conj [1 3 6 7 9])))        ";
		
		// find first (not found)
		final String script3d =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter string?)                     \n" +
				"            (halt-when (constantly true)         \n" +
				"                       (fn [res in] in)          \n" +
				"                       (constantly nil))))       \n" +
				"  (pr-str (transduce xf conj [1 3 6 7 9])))        ";
		
		// find first (not found)
		final String script3e =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (halt-when (constantly true)         \n" +
				"                       (fn [res in] in)          \n" +
				"                       (constantly nil))))       \n" +
				"  (pr-str (transduce xf conj [])))                 ";

		// all match odd (true)
		final String script5 =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter long?)                       \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (constantly false)        \n" +
				"                       (constantly true))))      \n" +
				"  (pr-str (transduce xf conj [1 3 5 7 9])))        ";

		// all match odd (false)
		final String script6 =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (filter long?)                       \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (constantly false)        \n" +
				"                       (constantly true))))      \n" +
				"  (pr-str (transduce xf conj [1 3 6 7 9])))        ";

		// any match even (true)
		final String script7 =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (constantly true)         \n" +
				"                       (constantly false))))     \n" +
				"  (pr-str (transduce xf conj [1 3 5 7 9])))        ";

		// any match even (false)
		final String script8 =
				"(do                                              \n" +
				"  (def xf (comp                                  \n" +
				"            (halt-when #(even? %)                \n" +
				"                       (constantly true)         \n" +
				"                       (constantly false))))     \n" +
				"  (pr-str (transduce xf conj [1 3 6 7 9])))        ";

		assertEquals("[1 3 5 7 9]", venice.eval(script1));	
		assertEquals("6", venice.eval(script2a));	
		assertEquals("6", venice.eval(script2b));	
		
		assertEquals("6", venice.eval(script3a));	
		assertEquals("nil", venice.eval(script3b));	
		assertEquals("1", venice.eval(script3c));	
		assertEquals("nil", venice.eval(script3d));	
		assertEquals("nil", venice.eval(script3e));	
		
		assertEquals("true", venice.eval(script5));	
		assertEquals("false", venice.eval(script6));	
		
		assertEquals("false", venice.eval(script7));	
		assertEquals("true", venice.eval(script8));	
	}
	
	@Test
	public void test_transduce_reduction_max() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                       \n" +
		    "  (def xf (comp (filter number?)))        \n" +
		    "  (pr-str (transduce xf max [1 2 3])))      ";
		
		final String script2 =
		    "(do                                       \n" +
		    "  (def xf (comp (filter number?)))        \n" +
		    "  (pr-str (transduce xf max [1])))          ";

		final String script3 =
		    "(do                                       \n" +
		    "  (def xf (comp (filter number?)))        \n" +
		    "  (pr-str (transduce xf max [])))           ";

		assertEquals("3", venice.eval(script1));	
		assertEquals("1", venice.eval(script2));	
		assertEquals("nil", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_reduction_min() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                        \n" +
		    "  (def xf (comp (filter number?)))         \n" +
		    "  (pr-str (transduce xf min [1 2 3])))       ";
		
		final String script2 =
		    "(do                                        \n" +
		    "  (def xf (comp (filter number?)))         \n" +
		    "  (pr-str (transduce xf min [1])))           ";

		final String script3 =
		    "(do                                        \n" +
		    "  (def xf (comp (filter number?)))         \n" +
		    "  (pr-str (transduce xf min [])))            ";

		assertEquals("1", venice.eval(script1));	
		assertEquals("1", venice.eval(script2));	
		assertEquals("nil", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_reduction_rf_first() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-first [1 2 3])))       ";
		
		final String script2 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-first [1])))           ";
		
		final String script3 =
		    "(pr-str (transduce identity rf-first [nil 1 2]))";
		
		final String script4 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-first [nil])))         ";

		final String script5 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-first [])))            ";

		assertEquals("1", venice.eval(script1));	
		assertEquals("1", venice.eval(script2));	
		assertEquals("nil", venice.eval(script3));	
		assertEquals("nil", venice.eval(script4));	
		assertEquals("nil", venice.eval(script5));	
	}
	
	@Test
	public void test_transduce_reduction_rf_last() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-last [1 2 3])))       ";
		
		final String script2 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-last [1])))           ";
		
		final String script3 =
		    "(pr-str (transduce identity rf-last [1 2 nil])))";
		
		final String script4 =
		    "(pr-str (transduce identity rf-last [nil])) ";

		final String script5 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-last [nil])))          ";

		final String script6 =
		    "(do                                             \n" +
		    "  (def xf (comp (filter number?)))              \n" +
		    "  (pr-str (transduce xf rf-last [])))             ";

		assertEquals("3", venice.eval(script1));	
		assertEquals("1", venice.eval(script2));	
		assertEquals("nil", venice.eval(script3));	
		assertEquals("nil", venice.eval(script4));	
		assertEquals("nil", venice.eval(script5));	
		assertEquals("nil", venice.eval(script6));	
	}
	
	@Test
	public void test_transduce_reduction_rf_any() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                                          \n" +
		    "  (def xf (comp (filter number?)))                           \n" +
		    "  (pr-str (transduce xf (rf-any? pos?) [true -1 1 2 false])))  ";
		
		final String script2 =
		    "(do                                                          \n" +
		    "  (def xf (comp (filter number?)))                           \n" +
		    "  (pr-str (transduce xf (rf-any? pos?) [true -1 -2 false])))  ";

		final String script3 =
		    "(do                                                          \n" +
		    "  (def xf (comp (filter number?)))                           \n" +
		    "  (pr-str (transduce xf (rf-any? pos?) [])))                   ";
		
		assertEquals("true",  venice.eval(script1));	
		assertEquals("false", venice.eval(script2));	
		assertEquals("false", venice.eval(script3));	
	}
	
	@Test
	public void test_transduce_reduction_rf_every() {
		final Venice venice = new Venice();
				
		final String script1 =
		    "(do                                                             \n" +
		    "  (def xf (comp (filter number?)))                              \n" +
		    "  (pr-str (transduce xf (rf-every? pos?) [true 1 1 2 false])))    ";
		
		final String script2 =
		    "(do                                                             \n" +
		    "  (def xf (comp (filter number?)))                              \n" +
		    "  (pr-str (transduce xf (rf-every? pos?) [true 1 2 -3 false])))  ";

		final String script3 =
		    "(do                                                             \n" +
		    "  (def xf (comp (filter number?)))                              \n" +
		    "  (pr-str (transduce xf (rf-every? pos?) [])))                    ";
		
		assertEquals("true",  venice.eval(script1));	
		assertEquals("false", venice.eval(script2));	
		assertEquals("false", venice.eval(script3));	
	}
	
	@Test
	public void test_map() {
		final Venice venice = new Venice();

		assertEquals("(2 3 4 5 6)", venice.eval("(pr-str (map inc '(1 2 3 4 5)))"));

		assertEquals("(2 3 4 5 6)", venice.eval("(pr-str (map inc [1 2 3 4 5]))"));

		assertEquals("(1 3)", venice.eval("(pr-str (map (fn [x] (get x :a)) [{:a 1 :b 2} {:a 3 :b 4}]))"));
		
		assertEquals("(true false true)", venice.eval("(pr-str (map not [false, true, false]))"));

		assertEquals("(1 2 3)", venice.eval("(pr-str (map :p [{:p 1} {:p 2} {:p 3}]))"));

		// strings
		assertEquals("(true false true false true)", venice.eval("(pr-str (map str/digit? \"1-3-5\"))"));
	}	
	
	@Test
	public void test_map_multi() {
		final Venice venice = new Venice();

		assertEquals("(5 7 9)", venice.eval("(pr-str (map + [1 2 3] [4 5 6]))"));

		assertEquals("(12 15 18)", venice.eval("(pr-str (map + [1 2 3] [4 5 6] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(pr-str (map + [1 2 3 9 9] [4 5 6 9] [7 8 9]))"));

		assertEquals("(12 15 18)", venice.eval("(pr-str (map + [1 2 3] [4 5 6 9] [7 8 9]))"));

		assertEquals("((1 1) (2 2) (3 3))", venice.eval("(pr-str (map list [1 2 3] [1 2 3]))"));
	}	
	
	@Test
	public void test_map_multi_lazy_seq() {
		final Venice venice = new Venice();

		assertEquals("((1 10) (2 10) (3 10))", venice.eval("(pr-str (map list [1 2 3] (lazy-seq (fn [] 10))))"));
	}	
	
	@Test
	public void test_map_indexed() {
		final Venice venice = new Venice();

		assertEquals("([0 :a] [1 :b] [2 :c])", venice.eval("(pr-str (map-indexed (fn [idx val] [idx val]) [:a :b :c]))"));
		assertEquals("([0 :a] [1 :b] [2 :c])", venice.eval("(pr-str (map-indexed vector [:a :b :c]))"));
		assertEquals("({0 :a} {1 :b} {2 :c})", venice.eval("(pr-str (map-indexed hash-map [:a :b :c]))"));
		assertEquals("([0 \"a\"] [1 \"b\"] [2 \"c\"])", venice.eval("(pr-str (map-indexed vector \"abc\"))"));
	}	

	@Test
	public void test_drop() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (drop 0 '()))"));
		assertEquals("()", venice.eval("(pr-str (drop 1 '()))"));
		
		assertEquals("(1)", venice.eval("(pr-str (drop 0 '(1)))"));
		assertEquals("()", venice.eval("(pr-str (drop 1 '(1)))"));
		assertEquals("()", venice.eval("(pr-str (drop 2 '(1)))"));
		
		assertEquals("(1 2)", venice.eval("(pr-str (drop 0 '(1 2)))"));
		assertEquals("(2)", venice.eval("(pr-str (drop 1 '(1 2)))"));
		assertEquals("()", venice.eval("(pr-str (drop 2 '(1 2)))"));
		assertEquals("()", venice.eval("(pr-str (drop 3 '(1 2)))"));
		
		assertEquals("(1 2 3)", venice.eval("(pr-str (drop 0 '(1 2 3)))"));
		assertEquals("(2 3)", venice.eval("(pr-str (drop 1 '(1 2 3)))"));
		assertEquals("(3)", venice.eval("(pr-str (drop 2 '(1 2 3)))"));
		assertEquals("()", venice.eval("(pr-str (drop 3 '(1 2 3)))"));
		assertEquals("()", venice.eval("(pr-str (drop 4 '(1 2 3)))"));

		assertEquals("(4 5)", venice.eval("(pr-str (drop 3 '(1 2 3 4 5)))"));
		assertEquals("()", venice.eval("(pr-str (drop 6 '(1 2 3 4 5)))"));

		
		assertEquals("[]", venice.eval("(pr-str (drop 0 []))"));
		assertEquals("[]", venice.eval("(pr-str (drop 1 []))"));
		
		assertEquals("[1]", venice.eval("(pr-str (drop 0 [1]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 1 [1]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 2 [1]))"));
		
		assertEquals("[1 2]", venice.eval("(pr-str (drop 0 [1 2]))"));
		assertEquals("[2]", venice.eval("(pr-str (drop 1 [1 2]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 2 [1 2]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 3 [1 2]))"));
		
		assertEquals("[1 2 3]", venice.eval("(pr-str (drop 0 [1 2 3]))"));
		assertEquals("[2 3]", venice.eval("(pr-str (drop 1 [1 2 3]))"));
		assertEquals("[3]", venice.eval("(pr-str (drop 2 [1 2 3]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 3 [1 2 3]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 4 [1 2 3]))"));

		assertEquals("[4 5]", venice.eval("(pr-str (drop 3 [1 2 3 4 5]))"));
		assertEquals("[]", venice.eval("(pr-str (drop 6 [1 2 3 4 5]))"));
	}

	@Test
	public void test_drop_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '()))"));
		assertEquals("(4)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("(4 5)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("()", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(4)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("()", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(4)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(3 4)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(3 2 1 0)", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));

		
		assertEquals("[]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) []))"));
		assertEquals("[4]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[4 5]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[4]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[4]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[3 4]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[3 2 1 0]", venice.eval("(pr-str (drop-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}

	@Test
	public void test_drop_last() {
		final Venice venice = new Venice();

		// list (tiny)
		
		assertEquals("()", venice.eval("(pr-str (drop-last 0 '()))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 1 '()))"));

		assertEquals("(0)", venice.eval("(pr-str (drop-last 0 '(0)))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 1 '(0)))"));

		assertEquals("(0 1)", venice.eval("(pr-str (drop-last 0 '(0 1)))"));
		assertEquals("(0)", venice.eval("(pr-str (drop-last 1 '(0 1)))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 2 '(0 1)))"));

		assertEquals("(0 1 2)", venice.eval("(pr-str (drop-last 0 '(0 1 2)))"));
		assertEquals("(0 1)", venice.eval("(pr-str (drop-last 1 '(0 1 2)))"));
		assertEquals("(0)", venice.eval("(pr-str (drop-last 2 '(0 1 2)))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 3 '(0 1 2)))"));

		// list (large)
		assertEquals("(0 1 2 3 4 5 6 7 8 9)", venice.eval("(pr-str (drop-last 0 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0 1 2 3 4 5 6 7 8)", venice.eval("(pr-str (drop-last 1 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0 1 2 3 4)", venice.eval("(pr-str (drop-last 5 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0 1)", venice.eval("(pr-str (drop-last 8 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0)", venice.eval("(pr-str (drop-last 9 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 10 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("()", venice.eval("(pr-str (drop-last 11 '(0 1 2 3 4 5 6 7 8 9)))"));
	}
	
	@Test
	public void test_take() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (take 0 '()))"));
		assertEquals("()", venice.eval("(pr-str (take 1 '()))"));
		
		assertEquals("()", venice.eval("(pr-str (take 0 '(1)))"));
		assertEquals("(1)", venice.eval("(pr-str (take 1 '(1)))"));
		assertEquals("(1)", venice.eval("(pr-str (take 2 '(1)))"));
		
		assertEquals("()", venice.eval("(pr-str (take 0 '(1 2)))"));
		assertEquals("(1)", venice.eval("(pr-str (take 1 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take 2 '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take 3 '(1 2)))"));
		
		assertEquals("()", venice.eval("(pr-str (take 0 '(1 2 3)))"));
		assertEquals("(1)", venice.eval("(pr-str (take 1 '(1 2 3)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take 2 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(pr-str (take 3 '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(pr-str (take 4 '(1 2 3)))"));
		
		assertEquals("(1 2 3 4)", venice.eval("(pr-str (take 4 '(1 2 3 4 5 6)))"));
		assertEquals("(1 2 3 4 5 6)", venice.eval("(pr-str (take 10 '(1 2 3 4 5 6)))"));

		
		assertEquals("[]", venice.eval("(pr-str (take 0 []))"));
		assertEquals("[]", venice.eval("(pr-str (take 1 []))"));
		
		assertEquals("[]", venice.eval("(pr-str (take 0 [1]))"));
		assertEquals("[1]", venice.eval("(pr-str (take 1 [1]))"));
		assertEquals("[1]", venice.eval("(pr-str (take 2 [1]))"));
		
		assertEquals("[]", venice.eval("(pr-str (take 0 [1 2]))"));
		assertEquals("[1]", venice.eval("(pr-str (take 1 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (take 2 [1 2]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (take 3 [1 2]))"));
		
		assertEquals("[]", venice.eval("(pr-str (take 0 [1 2 3]))"));
		assertEquals("[1]", venice.eval("(pr-str (take 1 [1 2 3]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (take 2 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(pr-str (take 3 [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(pr-str (take 4 [1 2 3]))"));
		
		assertEquals("[1 2 3 4]", venice.eval("(pr-str (take 4 [1 2 3 4 5 6]))"));
		assertEquals("[1 2 3 4 5 6]", venice.eval("(pr-str (take 10 [1 2 3 4 5 6]))"));
		
		assertEquals("(3 3 3 3)", venice.eval("(pr-str (doall (take 4 (repeat 3))))"));
		assertEquals("(0 1 2 0 1 2 0 1 2 0)", venice.eval("(pr-str (doall (take 10 (cycle (range 0 3)))))"));
	}

	@Test
	public void test_take_while() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '()))"));
		assertEquals("()", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(4)))"));
		assertEquals("()", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(4 5)))"));

		assertEquals("(1)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1)))"));
		assertEquals("(1)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1 4)))"));

		assertEquals("(1 2)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1 2 4)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1 2 3 4)))"));

		assertEquals("(1 2)", venice.eval("(pr-str (take-while (fn [x] (< x 3)) '(1 2 3 2 1 0)))"));


		assertEquals("[]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) []))"));
		assertEquals("[]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [4]))"));
		assertEquals("[]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [4 5]))"));

		assertEquals("[1]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1]))"));
		assertEquals("[1]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1 4]))"));

		assertEquals("[1 2]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1 2]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1 2 4]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1 2 3 4]))"));

		assertEquals("[1 2]", venice.eval("(pr-str (take-while (fn [x] (< x 3)) [1 2 3 2 1 0]))"));
	}

	@Test
	public void test_take_last() {
		final Venice venice = new Venice();

		// list (tiny)
		
		assertEquals("()", venice.eval("(pr-str (take-last 0 '()))"));
		assertEquals("()", venice.eval("(pr-str (take-last 1 '()))"));

		assertEquals("()", venice.eval("(pr-str (take-last 0 '(0)))"));
		assertEquals("(0)", venice.eval("(pr-str (take-last 1 '(0)))"));

		assertEquals("()", venice.eval("(pr-str (take-last 0 '(0 1)))"));
		assertEquals("(1)", venice.eval("(pr-str (take-last 1 '(0 1)))"));
		assertEquals("(0 1)", venice.eval("(pr-str (take-last 2 '(0 1)))"));

		assertEquals("()", venice.eval("(pr-str (take-last 0 '(0 1 2)))"));
		assertEquals("(2)", venice.eval("(pr-str (take-last 1 '(0 1 2)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (take-last 2 '(0 1 2)))"));
		assertEquals("(0 1 2)", venice.eval("(pr-str (take-last 3 '(0 1 2)))"));

		// list (large)
		assertEquals("()", venice.eval("(pr-str (take-last 0 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(9)", venice.eval("(pr-str (take-last 1 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(5 6 7 8 9)", venice.eval("(pr-str (take-last 5 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(2 3 4 5 6 7 8 9)", venice.eval("(pr-str (take-last 8 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(1 2 3 4 5 6 7 8 9)", venice.eval("(pr-str (take-last 9 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0 1 2 3 4 5 6 7 8 9)", venice.eval("(pr-str (take-last 10 '(0 1 2 3 4 5 6 7 8 9)))"));
		assertEquals("(0 1 2 3 4 5 6 7 8 9)", venice.eval("(pr-str (take-last 11 '(0 1 2 3 4 5 6 7 8 9)))"));
	}
	
	@Test
	public void test_keep() {
		final Venice venice = new Venice();

		assertEquals("(false true false)", venice.eval("(pr-str (keep even? (range 1 4)))"));
		
		assertEquals("(3 5 7)", venice.eval("(pr-str (keep #{3 5 7} '(1 3 5 7 9)))"));
	}
	
	@Test
	public void test_dedupe() {
		final Venice venice = new Venice();

		assertEquals("(0)", venice.eval("(pr-str (dedupe '(0 0 0)))"));
		assertEquals("(0 1 2 1 3)", venice.eval("(pr-str (dedupe '(0 1 2 2 2 1 3)))"));
		assertEquals("[0]", venice.eval("(pr-str (dedupe [0 0 0]))"));
		assertEquals("[0 1 2 1 3]", venice.eval("(pr-str (dedupe [0 1 2 2 2 1 3]))"));
	}
	
	@Test
	public void test_filter() {
		final Venice venice = new Venice();

		assertEquals("(2 4 6 8)", venice.eval("(pr-str (filter even? (range 1 10 1)))"));
		assertEquals("(2 4 6 8)", venice.eval("(pr-str (filter (fn [x] (even? x)) (range 1 10 1)))"));
		assertEquals("(2 4 6 8)", venice.eval("(pr-str (filter #(even? %) (range 1 10 1)))"));

		assertEquals("(true -1 0 1 \"\" \"A\" [1])", venice.eval("(pr-str (filter identity [nil false true -1 0 1 \"\" \"A\" [1]]))"));
}
	
	@Test
	public void test_remove() {
		final Venice venice = new Venice();

		assertEquals("(1 3 5 7 9)", venice.eval("(pr-str (remove even? (range 1 10 1)))"));
		
		assertEquals("(1 7 9)", venice.eval("(pr-str (remove #{3 5} '(1 3 5 7 9)))"));
	}

	@Test
	public void test_distinct() {
		final Venice venice = new Venice();

		assertEquals("(0 1 2 3)", venice.eval("(pr-str (distinct '(0 1 2 1 3 3)))"));
		assertEquals("[0 1 2 3]", venice.eval("(pr-str (distinct [0 1 2 1 3 3]))"));
	}

	@Test
	public void test_sorted() {
		final Venice venice = new Venice();

		// list
		assertEquals("()", venice.eval("(pr-str (sorted compare '()))"));
		assertEquals("(1)", venice.eval("(pr-str (sorted compare '(1)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (sorted compare '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(pr-str (sorted compare '(3 2 1)))"));

		assertEquals("()", venice.eval("(pr-str (sorted compare '()))"));
		assertEquals("(1.0)", venice.eval("(pr-str (sorted compare '(1.0)))"));
		assertEquals("(1.0 2.0)", venice.eval("(pr-str (sorted compare '(2.0 1.0)))"));
		assertEquals("(1.0 2.0 3.0)", venice.eval("(pr-str (sorted compare '(3.0 2.0 1.0)))"));

		assertEquals("()", venice.eval("(pr-str (sorted compare '()))"));
		assertEquals("(\"a\")", venice.eval("(pr-str (sorted compare '(\"a\")))"));
		assertEquals("(\"a\" \"b\")", venice.eval("(pr-str (sorted compare '(\"b\" \"a\")))"));
		assertEquals("(\"a\" \"b\" \"c\")", venice.eval("(pr-str (sorted compare '(\"c\" \"b\" \"a\")))"));

		assertEquals("(1)", venice.eval("(pr-str (sorted compare '(1)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (sorted compare '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(pr-str (sorted compare '(3 2 1)))"));

		assertEquals("((1 1) (1 2) (1 3) (2 1) (2 2))", venice.eval("(pr-str (sorted compare '((1 2) (1 1) (2 1) (1 3) (2 2))))"));

		assertEquals("(3 2 1)", venice.eval("(pr-str (sorted (comp (partial * -1) compare) '(2 3 1)))"));

		// vector
		assertEquals("[]", venice.eval("(pr-str (sorted compare []))"));
		assertEquals("[1]", venice.eval("(pr-str (sorted compare [1]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (sorted compare [2 1]))"));
		assertEquals("[1 2 3]", venice.eval("(pr-str (sorted compare [3 2 1]))"));

		assertEquals("[]", venice.eval("(pr-str (sorted compare []))"));
		assertEquals("[1.0]", venice.eval("(pr-str (sorted compare [1.0]))"));
		assertEquals("[1.0 2.0]", venice.eval("(pr-str (sorted compare [2.0 1.0]))"));
		assertEquals("[1.0 2.0 3.0]", venice.eval("(pr-str (sorted compare [3.0 2.0 1.0]))"));

		assertEquals("[]", venice.eval("(pr-str (sorted compare []))"));
		assertEquals("[\"a\"]", venice.eval("(pr-str (sorted compare [\"a\"]))"));
		assertEquals("[\"a\" \"b\"]", venice.eval("(pr-str (sorted compare [\"b\" \"a\"]))"));
		assertEquals("[\"a\" \"b\" \"c\"]", venice.eval("(pr-str (sorted compare [\"c\" \"b\" \"a\"]))"));
	
		
		// set
		assertEquals("()", venice.eval("(pr-str (sorted compare (set )))"));
		assertEquals("(1)", venice.eval("(pr-str (sorted compare (set 1)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (sorted compare (set 2 1)))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(pr-str (sorted compare (set 5 4 3 2 1)))"));
	
		
		// map
		assertEquals("()", venice.eval("(pr-str (sorted compare {}))"));
		assertEquals("([:a 1])", venice.eval("(pr-str (sorted compare {:a 1}))"));
		assertEquals("([:a 1] [:b 2])", venice.eval("(pr-str (sorted compare {:b 2 :a 1}))"));
		assertEquals("([:a 1] [:b 2] [:c 3])", venice.eval("(pr-str (sorted compare {:c 3 :b 2 :a 1}))"));
	}

	@Test
	public void test_reverse() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (reverse '()))"));
		assertEquals("(1)", venice.eval("(pr-str (reverse '(1)))"));
		assertEquals("(2 1)", venice.eval("(pr-str (reverse '(1 2)))"));
		assertEquals("(3 2 1)", venice.eval("(pr-str (reverse '(1 2 3)))"));

		assertEquals("[]", venice.eval("(pr-str (reverse []))"));
		assertEquals("[1]", venice.eval("(pr-str (reverse [1]))"));
		assertEquals("[2 1]", venice.eval("(pr-str (reverse [1 2]))"));
		assertEquals("[3 2 1]", venice.eval("(pr-str (reverse [1 2 3]))"));
		
		assertEquals("[[9 8 7] [6 5 4] [3 2 1 0]]", venice.eval("(pr-str (reverse [[3 2 1 0] [6 5 4] [9 8 7]]))"));
	}

	@Test
	public void test_flatten() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(pr-str (flatten '()))"));
		assertEquals("(1)", venice.eval("(pr-str (flatten '(1)))"));
		assertEquals("(1 2)", venice.eval("(pr-str (flatten '(1 2)))"));

		assertEquals("()", venice.eval("(pr-str (flatten '(())))"));
		assertEquals("(1)", venice.eval("(pr-str (flatten '(1 ())))"));
		assertEquals("(1 2)", venice.eval("(pr-str (flatten '(1 2 ())))"));

		assertEquals("(1 2)", venice.eval("(pr-str (flatten '((1 2))))"));
		assertEquals("(1 2 3)", venice.eval("(pr-str (flatten '(1 (2 3))))"));
		assertEquals("(1 2 3 4)", venice.eval("(pr-str (flatten '(1 2 (3 4))))"));

		assertEquals("(1 2 3 4 5 6)", venice.eval("(pr-str (flatten '(1 2 (3 4 (5 6)))))"));

		assertEquals("({:a 1 :b 2})", venice.eval("(pr-str (flatten '({:a 1 :b 2})))"));
		assertEquals("({:a 1 :b 2})", venice.eval("(pr-str (flatten '(({:a 1 :b 2}))))"));
		assertEquals("(:a 1 :b 2)", venice.eval("(pr-str (flatten (seq {:a 1 :b 2})))"));
		assertEquals("(1 :a 2 :b 3)", venice.eval("(pr-str (flatten '(1 (:a 2 :b 3))))"));
		assertEquals("(1 2 :a 3 :b 4)", venice.eval("(pr-str (flatten '(1 2 (:a 3 :b 4))))"));
		assertEquals("(1 2 :a 3 :b 4 5 6)", venice.eval("(pr-str (flatten '(1 2 (:a 3 :b (4 5 6)))))"));

		
		assertEquals("[]", venice.eval("(pr-str (flatten []))"));
		assertEquals("[1]", venice.eval("(pr-str (flatten [1]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (flatten [1 2]))"));
		
		assertEquals("[]", venice.eval("(pr-str (flatten [[]]))"));
		assertEquals("[1]", venice.eval("(pr-str (flatten [1 []]))"));
		assertEquals("[1 2]", venice.eval("(pr-str (flatten [1 2 []]))"));
		
		assertEquals("[1 2]", venice.eval("(pr-str (flatten [[1 2]]))"));
		assertEquals("[1 2 3]", venice.eval("(pr-str (flatten [1 [2 3]]))"));
		assertEquals("[1 2 3 4]", venice.eval("(pr-str (flatten [1 2 [3 4]]))"));

		assertEquals("[1 2 3 4 5 6]", venice.eval("(pr-str (flatten [1 2 [3 4 [5 6]]]))"));

		assertEquals("[{:a 1 :b 2}]", venice.eval("(pr-str (flatten [{:a 1 :b 2}]))"));
		assertEquals("[{:a 1 :b 2}]", venice.eval("(pr-str (flatten [[{:a 1 :b 2}]]))"));
		assertEquals("[1 {:a 2 :b 3}]", venice.eval("(pr-str (flatten [1 {:a 2 :b 3}]))"));
		assertEquals("[1 2 {:a 3 :b [4 5 6]}]", venice.eval("(pr-str (flatten [1 2 {:a 3 :b [4 5 6]}]))"));
	}
}
