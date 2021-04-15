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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class CoreFunctionsTest {
	
	@Test
	public void test_any_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) nil)"));	
		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) [])"));	
		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) [:a])"));	
		assertFalse((Boolean)venice.eval("(any? (fn [x] (number? x)) (set :a))"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [:a 1 2])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [1])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) [1 2])"));	
		assertTrue((Boolean)venice.eval("(any? (fn [x] (number? x)) (set :a 1 2))"));	
	}
	
	@Test
	public void test_any_pred() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("((any-pred number?) 1)"));	
		assertTrue((Boolean)venice.eval("((any-pred number?) 1 \"a\")"));	
		assertTrue((Boolean)venice.eval("((any-pred number?) 1 \"a\" \"a\")"));	
		
		assertFalse((Boolean)venice.eval("((any-pred number?) true)"));	
		assertFalse((Boolean)venice.eval("((any-pred number?) true false)"));	
		assertFalse((Boolean)venice.eval("((any-pred number?) true true false)"));	

		assertTrue((Boolean)venice.eval("((any-pred number? string?) 1)"));	
		assertTrue((Boolean)venice.eval("((any-pred number? string?) \"a\")"));	
		assertTrue((Boolean)venice.eval("((any-pred number? string?) 1 \"a\")"));	
		assertTrue((Boolean)venice.eval("((any-pred number? string?) \"a\" 1)"));	
		
		assertFalse((Boolean)venice.eval("((any-pred number? string?) true)"));	
		assertFalse((Boolean)venice.eval("((any-pred number? string?) true false)"));	
		assertFalse((Boolean)venice.eval("((any-pred number? string?) true true false)"));	
	}
	
	@Test
	public void test_apply() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(12), venice.eval("(apply + [3 4 5])"));
		
		assertEquals(Long.valueOf(10), venice.eval("(apply + 1 2 [3 4]))"));
		assertEquals(Long.valueOf(3), venice.eval("(apply + 1 2 []))"));
		assertEquals(Long.valueOf(3), venice.eval("(apply + 1 2 nil))"));

		assertEquals(Long.valueOf(3), venice.eval("(apply + '(1 2))"));

		assertEquals(Long.valueOf(2), venice.eval("(apply inc [1])"));
	}
	
	@Test
	public void test_assoc() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (assoc {}))"));
		assertEquals("{}", venice.eval("(str (assoc (ordered-map )))"));
		assertEquals("{}", venice.eval("(str (assoc (sorted-map )))"));

		assertEquals("{:a 1}", venice.eval("(str (assoc {} :a 1))"));
		assertEquals("{:a 1}", venice.eval("(str (assoc (ordered-map ) :a 1))"));
		assertEquals("{:a 1}", venice.eval("(str (assoc (sorted-map ) :a 1))"));

		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc {} :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (ordered-map ) :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (sorted-map ) :a 1 :b 2))"));
		
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc {:a 4} :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (ordered-map :a 4) :a 1 :b 2))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc (sorted-map :a 4) :a 1 :b 2))"));
		
		assertEquals("[:a :b :z]", venice.eval("(str (assoc [:x :y :z] 0 :a 1 :b))"));
		assertEquals("[:x :y :z :a]", venice.eval("(str (assoc [:x :y :z] 99 :a))"));
		
		assertEquals("abz", venice.eval("(str (assoc \"xyz\" 0 \"a\" 1 \"b\"))"));
		assertEquals("xyza", venice.eval("(str (assoc \"xyz\" 99 \"a\"))"));
	}
	
	@Test
	public void test_assoc_BANG() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (assoc! (mutable-map)))"));

		assertEquals("{:a 1}", venice.eval("(str (assoc! (mutable-map) :a 1))"));

		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc! (mutable-map) :a 1 :b 2))"));
		
		assertEquals("{:a 1 :b 2}", venice.eval("(str (assoc! (mutable-map :a 4) :a 1 :b 2))"));
	}
	
	@Test
	public void test_assoc_in() {
		final Venice venice = new Venice();

		// map
		assertEquals("{:a 100}", venice.eval("(str (assoc-in {} [:a] 100))"));
		assertEquals("{:a 100 :b 200}", venice.eval("(str (assoc-in {:a 100} [:b] 200))"));
		assertEquals("{:a 200}", venice.eval("(str (assoc-in {:a 100} [:a] 200))"));
		assertEquals("{:a {:b {:c 100}}}", venice.eval("(str (assoc-in {} [:a :b :c] 100))"));
		assertEquals("{:a {:c 100}}", venice.eval("(str (assoc-in {:a {}} [:a :c] 100))"));

		// vector
		assertEquals("[100]", venice.eval("(str (assoc-in [] [0] 100))"));
		assertEquals("[0 100]", venice.eval("(str (assoc-in [0] [1] 100))"));
		assertEquals("[0 100]", venice.eval("(str (assoc-in [0 1] [1] 100))"));
		assertEquals("[0 1 100]", venice.eval("(str (assoc-in [0 1] [5] 100))"));
		assertEquals("[[0 1] [2 100]]", venice.eval("(str (assoc-in [[0 1] [2 3]] [1 1] 100))"));
		assertEquals("[[0 1] [2 3 100]]", venice.eval("(str (assoc-in [[0 1] [2 3]] [1 2] 100))"));
		
		// map / vector
		assertEquals("{:a {:b [0 9 2]}}", venice.eval("(str (assoc-in {:a {:b [0 1 2]}} [:a :b 1] 9))"));
		assertEquals("{:a {:b [0 1 2 9]}}", venice.eval("(str (assoc-in {:a {:b [0 1 2]}} [:a :b 3] 9))"));
		
		// vector / map
		assertEquals("[0 1 {:a 1 :b {:c 9}}]", venice.eval("(str (assoc-in [0 1 {:a 1 :b {:c 2}}] [2 :b :c] 9))"));
	}
	
	@Test
	public void test_bigint() {
		final Venice venice = new Venice();

		assertEquals(new BigInteger("0"), venice.eval("(bigint false)"));
		assertEquals(new BigInteger("1"), venice.eval("(bigint true)"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint 3)"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint 3I)"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint 3.067M)"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint 3N)"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint \"3\")"));
		assertEquals(new BigInteger("3"), venice.eval("(bigint 3.3)"));
	}
	
	@Test
	public void test_bigint_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(bigint? nil)"));	
		assertFalse((Boolean)venice.eval("(bigint? true)"));	
		assertFalse((Boolean)venice.eval("(bigint? 1)"));	
		assertFalse((Boolean)venice.eval("(bigint? -3.0)"));
		assertFalse((Boolean)venice.eval("(bigint? -3.0M)"));
		assertTrue((Boolean)venice.eval("(bigint? -3N)"));
		assertFalse((Boolean)venice.eval("(bigint? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(bigint? :a)"));
		assertFalse((Boolean)venice.eval("(bigint? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(bigint? '())"));
		assertFalse((Boolean)venice.eval("(bigint? [])"));
		assertFalse((Boolean)venice.eval("(bigint? {})"));
	}
	
	@Test
	public void test_boolean() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(boolean nil)"));	
		assertTrue((Boolean)venice.eval("(boolean true)"));	
		assertFalse((Boolean)venice.eval("(boolean false)"));	
		assertTrue((Boolean)venice.eval("(boolean 0)"));
		assertTrue((Boolean)venice.eval("(boolean 1)"));
		assertTrue((Boolean)venice.eval("(boolean \"true\")"));
		assertTrue((Boolean)venice.eval("(boolean \"false\")"));
	}
	
	@Test
	public void test_boolean_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(boolean? nil)"));	
		assertTrue((Boolean)venice.eval("(boolean? true)"));	
		assertTrue((Boolean)venice.eval("(boolean? false)"));	
		assertFalse((Boolean)venice.eval("(boolean? 1)"));	
		assertFalse((Boolean)venice.eval("(boolean? -3.0)"));
		assertFalse((Boolean)venice.eval("(boolean? -3.0M)"));
		assertFalse((Boolean)venice.eval("(boolean? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(boolean? :a)"));
		assertFalse((Boolean)venice.eval("(boolean? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(boolean? '())"));
		assertFalse((Boolean)venice.eval("(boolean? [])"));
		assertFalse((Boolean)venice.eval("(boolean? {})"));
	}
	
	@Test
	public void test_butlast() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str (butlast nil))"));
		assertEquals("()", venice.eval("(str (butlast '()))"));
		assertEquals("()", venice.eval("(str (butlast '(1)))"));
		assertEquals("(1)", venice.eval("(str (butlast '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (butlast '(1 2 3)))"));
		assertEquals("(1 2 3)", venice.eval("(str (butlast '(1 2 3 4)))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (butlast '(1 2 3 4 5)))"));

		assertEquals("[]", venice.eval("(str (butlast []))"));
		assertEquals("[]", venice.eval("(str (butlast [1]))"));
		assertEquals("[1]", venice.eval("(str (butlast [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (butlast [1 2 3]))"));
		assertEquals("[1 2 3]", venice.eval("(str (butlast [1 2 3 4]))"));
		assertEquals("[1 2 3 4]", venice.eval("(str (butlast [1 2 3 4 5]))"));
	}

	@Test
	public void test_new_char() {
		final Venice venice = new Venice();
		
		assertEquals('A', venice.eval("(char 65)"));
		assertEquals('A', venice.eval("(char \"A\")"));
	}

	@Test
	public void test_char_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(char? (char 65))"));
		assertTrue((Boolean)venice.eval("(char? (char \"A\"))"));
	}
	
	@Test
	public void test_coll_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(coll? nil)"));	
		assertFalse((Boolean)venice.eval("(coll? 1)"));	
		assertTrue((Boolean)venice.eval("(coll? '(1 2))"));	
		assertTrue((Boolean)venice.eval("(coll? [1 2])"));	
		assertTrue((Boolean)venice.eval("(coll? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(coll? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (sorted-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(coll? (set 1 2))"));	
	}
	
	@Test
	public void test_comp() {
		final Venice venice = new Venice();

		assertEquals("24", venice.eval("((comp str +) 8 8 8)"));
		
		assertEquals("(-5 -7 -9 -11)", venice.eval("(str (map (comp - (partial + 3) (partial * 2)) [1 2 3 4]))")); 

		assertEquals("-315", venice.eval("(str (reduce (comp - (partial + 3) (partial * 2)) [1 2 3 4]))")); 

		assertEquals("(1 2 3)", venice.eval("(str (map (comp inc) [0 1 2]))"));

		assertEquals("(false false)", venice.eval("(str (filter (comp not) [false true false]))"));

		assertEquals("(1 2 3)", venice.eval("(str (filter (comp not zero?) [0 1 0 2 0 3]))"));
		
		assertEquals("5", venice.eval(
								"(do " +
								"   (def fifth (comp first rest rest rest rest)) " +
								"   (str (fifth [1 2 3 4 5])))"));

		assertEquals("(1 2 3 4)", venice.eval("(str (map (comp) [1 2 3 4]))"));
	}
	
	@Test
	public void test_compare() {
		final Venice venice = new Venice();

		// nil
		assertEquals(-1L, venice.eval("(compare nil 1)"));
		assertEquals(0L,  venice.eval("(compare nil nil)"));
		assertEquals(1L,  venice.eval("(compare 1 nil)"));

		// boolean
		assertEquals(0L,  venice.eval("(compare false false)"));
		assertEquals(0L,  venice.eval("(compare true true)"));
		assertEquals(-1L, venice.eval("(compare false true)"));
		assertEquals(1L,  venice.eval("(compare true false)"));

		// symbol
		assertEquals(-1L, venice.eval("(compare 'a 'b)"));
		assertEquals(0L,  venice.eval("(compare 'a 'a)"));
		assertEquals(1L,  venice.eval("(compare 'b 'a)"));

		// keyword
		assertEquals(-1L, venice.eval("(compare :a :b)"));
		assertEquals(0L,  venice.eval("(compare :a :a)"));
		assertEquals(1L,  venice.eval("(compare :b :a)"));

		// long
		assertEquals(-1L, venice.eval("(compare 0 1)"));
		assertEquals(0L,  venice.eval("(compare 1 1)"));
		assertEquals(1L,  venice.eval("(compare 1 0)"));

		// double
		assertEquals(-1L, venice.eval("(compare 0.0 1.0)"));
		assertEquals(0L,  venice.eval("(compare 1.0 1.0)"));
		assertEquals(1L,  venice.eval("(compare 1.0 0.0)"));

		// decimal
		assertEquals(-1L, venice.eval("(compare 0.0M 1.0M)"));
		assertEquals(0L,  venice.eval("(compare 1.0M 1.0M)"));
		assertEquals(1L,  venice.eval("(compare 1.0M 0.0M)"));

		// string
		assertEquals(-1L, venice.eval("(compare \"a\" \"b\")"));
		assertEquals(-1L, venice.eval("(compare \"aa\" \"b\")"));
		assertEquals(-1L, venice.eval("(compare \"aaa\" \"bb\")"));
		assertEquals(-1L, venice.eval("(compare \"aaa\" \"bbb\")"));
		assertEquals(0L,  venice.eval("(compare \"aaa\" \"aaa\")"));
		assertEquals(1L,  venice.eval("(compare \"bbb\" \"aaa\")"));

		// char
		assertEquals(-1L, venice.eval("(compare (char \"a\") (char \"b\"))"));
		assertEquals(0L,  venice.eval("(compare (char \"a\") (char \"a\"))"));
		assertEquals(1L,  venice.eval("(compare (char \"b\") (char \"a\"))"));

		// char / string
		assertEquals(-1L, venice.eval("(compare (char \"a\") \"b\")"));
		assertEquals(0L,  venice.eval("(compare (char \"a\") \"a\")"));
		assertEquals(1L,  venice.eval("(compare (char \"b\") \"a\")"));

		// string / char
		assertEquals(-1L, venice.eval("(compare \"a\" (char \"b\"))"));
		assertEquals(0L,  venice.eval("(compare \"a\" (char \"a\"))"));
		assertEquals(1L,  venice.eval("(compare \"b\" (char \"a\"))"));
	}
	
	@Test
	public void test_conj() {
		final Venice venice = new Venice();

		assertEquals("[]", venice.eval("(str (conj))"));
		assertEquals("1", venice.eval("(str (conj 1))"));
		assertEquals("(1)", venice.eval("(str (conj nil 1))"));
		assertEquals("(1 2)", venice.eval("(str (conj nil 1 2))"));

		// List
		assertEquals("(1)", venice.eval("(str (conj '() 1))"));
		assertEquals("(1 2)", venice.eval("(str (conj '(1) 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (conj '(1 2) 3))"));
		assertEquals("(1 2)", venice.eval("(str (conj '() 1 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (conj '(1) 2 3))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (conj '(1 2) 3 4))"));
		assertEquals("((1 2))", venice.eval("(str (conj '() '(1 2)))"));
		assertEquals("((1 2) (3 4))", venice.eval("(str (conj '((1 2)) '(3 4)))"));
		assertEquals("(0 (1 2) (3 4) (5 6))", venice.eval("(str (conj '(0 (1 2)) '(3 4) '(5 6)))"));

		// Vector
		assertEquals("[1]", venice.eval("(str (conj [] 1))"));
		assertEquals("[1 2]", venice.eval("(str (conj [1] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (conj [1 2] 3))"));		
		assertEquals("[1 2]", venice.eval("(str (conj [] 1 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (conj [1] 2 3))"));
		assertEquals("[1 2 3 4]", venice.eval("(str (conj [1 2] 3 4))"));
		assertEquals("[[1 2]]", venice.eval("(str (conj [] [1 2]))"));
		assertEquals("[[1 2] [3 4]]", venice.eval("(str (conj [[1 2]] [3 4]))"));
		assertEquals("[[1 2] [3 4] [5 6] 7]", venice.eval("(str (conj [[1 2]] [3 4] [5 6] 7))"));
		
		// Set
		assertEquals("#{1}", venice.eval("(str (conj (set ) 1))"));
		assertEquals("#{1 2}", venice.eval("(str (conj (set ) 1 2))"));
		assertEquals("#{1 3 5}", venice.eval("(str (conj (sorted-set 1) 3 5))"));
		
		// Map
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1 :b 2) [:c 3]))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1) [:b 2] [:c 3]))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1 :b 2) {:c 3}))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1) {:b 2} {:c 3}))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1 :b 2) (map-entry :c 3)))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1) (map-entry :b 2) (map-entry :c 3)))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (conj (ordered-map :a 1) (ordered-map :b 2 :c 3)))"));
	}

	@Test
	public void test_concat() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (concat '()))"));
		assertEquals("()", venice.eval("(str (concat '() '()))"));
		assertEquals("()", venice.eval("(str (concat '() '() '()))"));
		assertEquals("(1)", venice.eval("(str (concat '(1)))"));
		assertEquals("(1)", venice.eval("(str (concat '(1) '() '()))"));
		assertEquals("(1 2)", venice.eval("(str (concat '(1 2) '() '()))"));
		assertEquals("(1 2 3)", venice.eval("(str (concat '(1 2) '(3) '()))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (concat '(1 2) '(3 4) '()))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (concat '(1 2) '(3 4) '(5)))"));
		assertEquals("(1 2 3 4 5 6)", venice.eval("(str (concat '(1 2) '(3 4) nil '(5 6)))"));

		assertEquals("()", venice.eval("(str (concat []))"));
		assertEquals("()", venice.eval("(str (concat [] []))"));
		assertEquals("()", venice.eval("(str (concat [] [] []))"));
		assertEquals("(1)", venice.eval("(str (concat [1]))"));
		assertEquals("(1)", venice.eval("(str (concat [1] [] []))"));
		assertEquals("(1 2)", venice.eval("(str (concat [1 2] [] []))"));
		assertEquals("(1 2 3)", venice.eval("(str (concat [1 2] [3] []))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (concat [1 2] [3 4] []))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (concat [1 2] [3 4] [5]))"));
		assertEquals("(1 2 3 4 5 6)", venice.eval("(str (concat [1 2] [3 4] nil [5 6]))"));

		assertEquals("([:a 1] [:b 2])", venice.eval("(str (concat (ordered-map :a 1 :b 2)))"));
		
		assertEquals("()", venice.eval("(str (concat \"\"))"));
		assertEquals("(a)", venice.eval("(str (concat \"a\"))"));
		assertEquals("(a b c d e f)", venice.eval("(str (concat \"abc\" \"def\"))"));
		
		
		// Java Interop
		
		assertEquals(
				"(1 2 3 4)", 
				venice.eval(
						"(str (concat                                 " +
						"        (doto (. :java.util.ArrayList :new)  " +
						"	           (. :add 1)                     " +
						"	           (. :add 2))                    " +
						"        '(3 4)))                             "));

		assertEquals(
				"([a 1] [b 2] 3 4)", 
				venice.eval(
						"(str (concat                                    " +
						"        (doto (. :java.util.LinkedHashMap :new) " +
						"	           (. :put :a 1)                     " +
						"	           (. :put :b 2))                    " +
						"        '(3 4)))                                "));

	}
	
	@Test
	public void test_cons() {
		final Venice venice = new Venice();

		// List
		assertEquals("(1)", venice.eval("(str (cons 1 '()))"));
		assertEquals("(1 2)", venice.eval("(str (cons 1 '(2)))"));
		assertEquals("(1 2 3)", venice.eval("(str (cons 1 '(2 3)))"));	
		assertEquals("((1 2))", venice.eval("(str (cons '(1 2) '()))"));
		assertEquals("((1 2) 3)", venice.eval("(str (cons '(1 2) '(3)))"));
		assertEquals("((1 2) 3 4)", venice.eval("(str (cons '(1 2) '(3 4)))"));

		// Vector
		assertEquals("[1]", venice.eval("(str (cons 1 []))"));
		assertEquals("[1 2]", venice.eval("(str (cons 1 [2]))"));
		assertEquals("[1 2 3]", venice.eval("(str (cons 1 [2 3]))"));
		assertEquals("[[1 2]]", venice.eval("(str (cons [1 2] []))"));
		assertEquals("[[1 2] 3]", venice.eval("(str (cons [1 2] [3]))"));
		assertEquals("[[1 2] 3 4]", venice.eval("(str (cons [1 2] [3 4]))"));
		
		// Set
		assertEquals("#{1 2 3}", venice.eval("(str (cons 3 (sorted-set 1 2)))"));
		
		// Map
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (cons {:c 3} (ordered-map :a 1 :b 2)))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (cons (ordered-map :c 3) (ordered-map :a 1 :b 2)))"));
	}
	
	@Test
	public void test_cons_BANG() {
		final Venice venice = new Venice();

		// Set
		assertEquals("#{1 2 3}", venice.eval("(str (cons! 3 (mutable-set 1 2)))"));

		// List
		assertEquals("(1 2 3)", venice.eval("(str (cons! 1 (mutable-list 2 3)))"));
	}
	
	@Test
	public void test_conj_BANG() {
		final Venice venice = new Venice();

		// Set
		assertEquals("#{1 2 3}", venice.eval("(str (conj! (mutable-set 1 2) 3))"));
		assertEquals("#{1 2 3 4}", venice.eval("(str (conj! (mutable-set 1 2) 3 4))"));

		// List
		assertEquals("(1 2 3)", venice.eval("(str (conj! (mutable-list 1 2) 3))"));
		assertEquals("(1 2 3 4)", venice.eval("(str (conj! (mutable-list 1 2) 3 4))"));
	}
	
	@Test
	public void test_contains() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(contains? {:a 1 :b 2} :b)"));
		assertFalse((Boolean)venice.eval("(contains? {:a 1 :b 2} :z)"));

		assertTrue((Boolean)venice.eval("(contains? (ordered-map :a 1 :b 2) :b)"));
		assertFalse((Boolean)venice.eval("(contains? (ordered-map :a 1 :b 2) :z)"));

		assertTrue((Boolean)venice.eval("(contains? (sorted-map :a 1 :b 2) :b)"));
		assertFalse((Boolean)venice.eval("(contains? (sorted-map :a 1 :b 2) :z)"));

		assertTrue((Boolean)venice.eval("(contains? #{:a :b} :b)"));
		assertFalse((Boolean)venice.eval("(contains? #{:a :b} :z)"));
	}
	
	@Test
	public void test_count() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0L), venice.eval("(count nil)"));

		assertEquals(Long.valueOf(1L), venice.eval("(count '(1))"));
		assertEquals(Long.valueOf(2L), venice.eval("(count '(1 2))"));
		assertEquals(Long.valueOf(3L), venice.eval("(count '(1 2 3))"));

		assertEquals(Long.valueOf(0L), venice.eval("(count [])"));
		assertEquals(Long.valueOf(1L), venice.eval("(count [1])"));
		assertEquals(Long.valueOf(2L), venice.eval("(count [1 2])"));
		assertEquals(Long.valueOf(3L), venice.eval("(count [1 2 3])"));

		assertEquals(Long.valueOf(0L), venice.eval("(count {})"));
		assertEquals(Long.valueOf(1L), venice.eval("(count {:a 1})"));
		assertEquals(Long.valueOf(2L), venice.eval("(count {:a 1 :b 2})"));
		assertEquals(Long.valueOf(3L), venice.eval("(count {:a 1 :b 2 :c 3})"));
		
		assertEquals(Long.valueOf(0), venice.eval("(count (ordered-map ))"));
		assertEquals(Long.valueOf(1), venice.eval("(count (ordered-map :a 1))"));
		assertEquals(Long.valueOf(2), venice.eval("(count (ordered-map :a 1 :b 2))"));

		assertEquals(Long.valueOf(0), venice.eval("(count (sorted-map ))"));
		assertEquals(Long.valueOf(1), venice.eval("(count (sorted-map :a 1))"));
		assertEquals(Long.valueOf(2), venice.eval("(count (sorted-map :a 1 :b 2))"));

		assertEquals(Long.valueOf(0), venice.eval("(count \"\")"));
		assertEquals(Long.valueOf(1), venice.eval("(count \"a\")"));
		assertEquals(Long.valueOf(2), venice.eval("(count \"ab\")"));
		
		assertEquals(Long.valueOf(0L),venice.eval("(count (bytebuf))"));		
		assertEquals(Long.valueOf(3L),venice.eval("(count (bytebuf [0 1 2]))"));		

		assertEquals(Long.valueOf(0), venice.eval("(count (stack))"));
		assertEquals(Long.valueOf(1), venice.eval("(count (push! (stack) 1))"));

		// Java Interop
		
		assertEquals(
				"(1 2)", 
				venice.eval(
						"(str                                     " +
						"    (doto (. :java.util.ArrayList :new)  " +
						"	       (. :add 1)                     " +
						"	       (. :add 2)))                   " ));
		
		assertEquals(
				Long.valueOf(2L), 
				venice.eval(
						"(count                                   " +
						"    (doto (. :java.util.ArrayList :new)  " +
						"	       (. :add 1)                     " +
						"	       (. :add 2)))                   " ));

		assertEquals(
				Long.valueOf(2L), 
				venice.eval(
						"(count                                       " +
						"    (doto (. :java.util.LinkedHashMap :new)  " +
						"	       (. :put :a 1)                      " +
						"	       (. :put :b 2)))                    " ));
	}
	
	@Test
	public void test_cycle() {
		final Venice venice = new Venice();

		assertEquals("(1 2 1 2 1)", venice.eval("(pr-str (doall (take 5 (cycle [1 2]))))"));
		assertEquals("(1 1 1 1 1)", venice.eval("(pr-str (doall (take 5 (cycle [1]))))"));

		assertThrows(VncException.class, () -> venice.eval("(cycle [])"));
	}

	@Test
	public void test_decimal() {
		final Venice venice = new Venice();

		assertEquals(new BigDecimal("0"), venice.eval("(decimal false)"));
		assertEquals(new BigDecimal("1"), venice.eval("(decimal true)"));
		assertEquals(new BigDecimal("3"), venice.eval("(decimal 3)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(decimal 3.0M)"));
		assertEquals(new BigDecimal("3"), venice.eval("(decimal 3N)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("3.0M"));
		assertEquals(new BigDecimal("3.00"), venice.eval("(decimal 3 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.00"), venice.eval("(decimal \"3.0\" 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.30"), venice.eval("(decimal 3.3 2 :HALF_UP)"));
		assertEquals(new BigDecimal("3.12"), venice.eval("(decimal 3.123456M 2 :HALF_UP)"));
	}
	
	@Test
	public void test_decimal_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(decimal? nil)"));	
		assertFalse((Boolean)venice.eval("(decimal? true)"));	
		assertFalse((Boolean)venice.eval("(decimal? 1)"));	
		assertFalse((Boolean)venice.eval("(decimal? -3.0)"));
		assertTrue((Boolean)venice.eval("(decimal? -3.0M)"));
		assertFalse((Boolean)venice.eval("(decimal? -3N)"));
		assertFalse((Boolean)venice.eval("(decimal? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(decimal? :a)"));
		assertFalse((Boolean)venice.eval("(decimal? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(decimal? '())"));
		assertFalse((Boolean)venice.eval("(decimal? [])"));
		assertFalse((Boolean)venice.eval("(decimal? {})"));
	}

	@Test
	public void test_disj() {
		final Venice venice = new Venice();
		
		// Set
		assertEquals("#{}", venice.eval("(str (disj (set 1) 1))"));
		assertEquals("#{1}", venice.eval("(str (disj (set 1) 2))"));
		assertEquals("#{1}", venice.eval("(str (disj (set 1 2) 2))"));
		assertEquals("#{1}", venice.eval("(str (disj (set 1 2 3) 2 3))"));
		
		// Set
		assertEquals("#{}", venice.eval("(str (disj (sorted-set 1) 1))"));
		assertEquals("#{1}", venice.eval("(str (disj (sorted-set 1) 2))"));
		assertEquals("#{1}", venice.eval("(str (disj (sorted-set 1 2) 2))"));
		assertEquals("#{1}", venice.eval("(str (disj (sorted-set 1 2 3) 2 3))"));
		assertEquals("#{1 4}", venice.eval("(str (disj (sorted-set 1 2 3 4) 2 3))"));
	}
	
	@Test
	public void test_docoll() {
		final Venice venice = new Venice();

		// docoll on list
		final String script1 = 
				"(do                                                      " +
				"   (def counter (atom 0))                                " +
				"                                                         " +
				"   (def sum (fn [x] (swap! counter (fn [n] (+ n x)))))   " +
				"                                                         " +
				"   (docoll sum '(1 2 3 4))                               " +
				"   (deref counter)                                       " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script1));

		// docoll on vector
		final String script2 = 
				"(do                                                      " +
				"   (def counter (atom 0))                                " +
				"                                                         " +
				"   (def sum (fn [x] (swap! counter (fn [n] (+ n x)))))   " +
				"                                                         " +
				"   (docoll sum [1 2 3 4])                                " +
				"   (deref counter)                                       " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script2));

		// docoll on map
		final String script3 = 
				"(do                                                          " +
				"   (def counter (atom 0))                                    " +
				"                                                             " +
				"   (def sum (fn [[k v]] (swap! counter (fn [n] (+ n v)))))   " +
				"                                                             " +
				"   (docoll sum {:a 1 :b 2 :c 3 :d 4})                        " +
				"   (deref counter)                                           " +
				") ";

		assertEquals(Long.valueOf(10), venice.eval(script3));
	}

	@Test
	public void test_difference() {
		final Venice venice = new Venice();

		assertEquals("#{1 2 3}", venice.eval("(str (difference (set 1 2 3)))"));
		assertEquals("#{1}", venice.eval("(str (difference (set 1 2) (set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (difference (set 1 2 3) (set 1) (set 1 4) (set 3)))"));

		assertEquals("#{1 2 3}", venice.eval("(str (difference (sorted-set 1 2 3)))"));
		assertEquals("#{1}", venice.eval("(str (difference (sorted-set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (difference (sorted-set 1 2 3) (sorted-set 1) (sorted-set 1 4) (sorted-set 3)))"));

		assertEquals("#{1}", venice.eval("(str (difference (set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (difference (set 1 2 3) (set 1) (set 1 4) (sorted-set 3)))"));
	}

	@Test
	public void test_dissoc() {
		final Venice venice = new Venice();

		assertEquals("{:a 1}", venice.eval("(str (dissoc {:a 1 :b 2} :b)))"));
		assertEquals("{:a 1 :c 3}", venice.eval("(str (dissoc (ordered-map :a 1 :b 2 :c 3) :b)))"));
		assertEquals("{:a 1 :c 3}", venice.eval("(str (dissoc (sorted-map :a 1 :b 2 :c 3) :b)))"));
		
		assertEquals("[:y :z]", venice.eval("(str (dissoc [:x :y :z] 0))"));
		assertEquals("[:x :z]", venice.eval("(str (dissoc [:x :y :z] 1))"));
		assertEquals("[:x :y]", venice.eval("(str (dissoc [:x :y :z] 2))"));
		assertEquals("[:z]", venice.eval("(str (dissoc [:x :y :z] 0 0))"));
		assertEquals("[]", venice.eval("(str (dissoc [:x :y :z] 0 0 0))"));
		
		assertEquals("yz", venice.eval("(str (dissoc \"xyz\" 0))"));
		assertEquals("xz", venice.eval("(str (dissoc \"xyz\" 1))"));
		assertEquals("xy", venice.eval("(str (dissoc \"xyz\" 2))"));
		assertEquals("z", venice.eval("(str (dissoc \"xyz\" 0 0))"));
		assertEquals("", venice.eval("(str (dissoc \"xyz\" 0 0 0))"));
	}

	@Test
	public void test_dissoc_BANG() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :c 3}", venice.eval("(str (dissoc! (mutable-map :a 1 :b 2 :c 3) :b)))"));
	}
	
	@Test
	public void test_dissoc_in() {
		final Venice venice = new Venice();
		
		// vectors
		assertEquals(
				"[1 2 3]", 
				venice.eval("(pr-str (dissoc-in [1 2 3] []))"));

		assertEquals(
				"[1 3]", 
				venice.eval("(pr-str (dissoc-in [1 2 3] [1]))"));

		assertEquals(
				"[1 2 3]", 
				venice.eval("(pr-str (dissoc-in [1 2 3] [4]))"));

		assertEquals(
				"[1 [4 5] 3]", 
				venice.eval("(pr-str (dissoc-in [1 [4 5 6] 3] [1 2]))"));

		assertEquals(
			"[{:name \"James\" :age 26}]", 
			venice.eval("(pr-str (dissoc-in [ (ordered-map :name \"James\" :age 26)   \n" +
					    "                     (ordered-map :name \"John\"  :age 43) ] \n" +
					    "                   [1]))                                        "));

		assertEquals(
				"[{:name \"James\" :age 26} {:name \"John\"}]", 
				venice.eval("(pr-str (dissoc-in [ (ordered-map :name \"James\" :age 26)   \n" +
						    "                     (ordered-map :name \"John\"  :age 43) ] \n" +
						    "                   [1 :age]))                                        "));

		// maps
		assertEquals("{:a 12 :b 5}", venice.eval("(pr-str (dissoc-in {:a 12 :b 5} []))"));
		
		assertEquals("{:b 5}", venice.eval("(pr-str (dissoc-in {:a 12 :b 5} [:a]))"));
		
		assertEquals("{:b 5}", venice.eval("(pr-str (dissoc-in {:a {:x 12} :b 5} [:a]))"));
		
		assertEquals("{:a {:y 14} :b 5}", venice.eval("(pr-str (dissoc-in {:a {:x 12 :y 14} :b 5} [:a :x]))"));
		
		assertEquals("{:a [1 3] :b 5}", venice.eval("(pr-str (dissoc-in {:a [1 2 3] :b 5} [:a 1]))"));
		
		assertEquals("{:a {} :b 5}", venice.eval("(pr-str (dissoc-in {:a {:x 12} :b 5} [:a :x]))"));
		
		assertEquals("{:a 12 :b 5}", venice.eval("(pr-str (dissoc-in {:a 12 :b 5} [:c]))"));
	}

	@Test
	public void test_double() {
		final Venice venice = new Venice();

		assertEquals(Double.valueOf(0.0), venice.eval("(double nil)"));
		assertEquals(Double.valueOf(0.0), venice.eval("(double false)"));
		assertEquals(Double.valueOf(1.0), venice.eval("(double true)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3.0)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3.0M)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double 3N)"));
		assertEquals(Double.valueOf(3.0), venice.eval("(double \"3.0\")"));
	}
	
	@Test
	public void test_double_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(double? nil)"));	
		assertFalse((Boolean)venice.eval("(double? true)"));	
		assertFalse((Boolean)venice.eval("(double? 1)"));	
		assertTrue((Boolean)venice.eval("(double? -3.0)"));
		assertFalse((Boolean)venice.eval("(double? -3.0M)"));
		assertFalse((Boolean)venice.eval("(double? -3N)"));
		assertFalse((Boolean)venice.eval("(double? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(double? :a)"));
		assertFalse((Boolean)venice.eval("(double? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(double? '())"));
		assertFalse((Boolean)venice.eval("(double? [])"));
		assertFalse((Boolean)venice.eval("(double? {})"));
	}
	
	@Test
	public void test_equals_Q() {
		final Venice venice = new Venice();
		
		// Nil
		assertTrue((Boolean)venice.eval("(== nil nil)"));
		
		// Boolean
		assertTrue((Boolean)venice.eval("(== true true)"));
		assertFalse((Boolean)venice.eval("(== true false)"));

		// Integer
		assertTrue((Boolean)venice.eval("(== 2I 2I)"));
		assertTrue((Boolean)venice.eval("(== 2I 2)"));
		assertTrue((Boolean)venice.eval("(== 2I 2.0)"));
		assertTrue((Boolean)venice.eval("(== 2I 2.0M)"));
		assertTrue((Boolean)venice.eval("(== 2I 2N)"));
		assertFalse((Boolean)venice.eval("(== 2I 3I)"));
		assertFalse((Boolean)venice.eval("(== 2I 3)"));
		assertFalse((Boolean)venice.eval("(== 2I 3.0)"));
		assertFalse((Boolean)venice.eval("(== 2I 3.0M)"));
		assertFalse((Boolean)venice.eval("(== 2I 3N)"));
		assertFalse((Boolean)venice.eval("(== 2I 2.1)"));

		// Long
		assertTrue((Boolean)venice.eval("(== 2 2)"));
		assertTrue((Boolean)venice.eval("(== 2 2I)"));
		assertTrue((Boolean)venice.eval("(== 2 2.0)"));
		assertTrue((Boolean)venice.eval("(== 2 2.0M)"));
		assertTrue((Boolean)venice.eval("(== 2 2N)"));
		assertFalse((Boolean)venice.eval("(== 2 3)"));
		assertFalse((Boolean)venice.eval("(== 2 3I)"));
		assertFalse((Boolean)venice.eval("(== 2 3.0)"));
		assertFalse((Boolean)venice.eval("(== 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(== 2 3N)"));
		assertFalse((Boolean)venice.eval("(== 2 2.1)"));

		// Double
		assertTrue((Boolean)venice.eval("(== 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(== 2.0 2)"));
		assertTrue((Boolean)venice.eval("(== 2.0 2I)"));
		assertTrue((Boolean)venice.eval("(== 2.0 2.0M)"));
		assertTrue((Boolean)venice.eval("(== 2.0 2N)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3I)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(== 2.0 3N)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(== 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(== 2.0M 2)"));
		assertTrue((Boolean)venice.eval("(== 2.0M 2I)"));
		assertTrue((Boolean)venice.eval("(== 2.0M 2.0)"));
		assertTrue((Boolean)venice.eval("(== 2.0M 2N)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3I)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3.0)"));
		assertFalse((Boolean)venice.eval("(== 2.0M 3N)"));

		// BigInteger
		assertTrue((Boolean)venice.eval("(== 2N 2N)"));
		assertTrue((Boolean)venice.eval("(== 2N 2)"));
		assertTrue((Boolean)venice.eval("(== 2N 2I)"));
		assertTrue((Boolean)venice.eval("(== 2N 2.0)"));
		assertTrue((Boolean)venice.eval("(== 2N 2.0M)"));
		assertFalse((Boolean)venice.eval("(== 2N 3N)"));
		assertFalse((Boolean)venice.eval("(== 2N 3)"));
		assertFalse((Boolean)venice.eval("(== 2N 3I)"));
		assertFalse((Boolean)venice.eval("(== 2N 3.0)"));
		assertFalse((Boolean)venice.eval("(== 2N 3.0M)"));

		// String
		assertTrue((Boolean)venice.eval("(== \"aa\" \"aa\")"));
		assertFalse((Boolean)venice.eval("(== \"aa\" \"zz\")"));

		// String/Keyword
		assertTrue((Boolean)venice.eval("(== \"aa\" :aa)"));
		assertTrue((Boolean)venice.eval("(== :aa \"aa\")"));

		// String/Char
		assertTrue((Boolean)venice.eval("(== \"a\" (char \"a\"))"));
		assertFalse((Boolean)venice.eval("(== \"a\" (char \"z\"))"));
		assertFalse((Boolean)venice.eval("(== \"abc\" (char \"z\"))"));

		// Char
		assertTrue((Boolean)venice.eval("(== (char \"a\") (char \"a\"))"));
		assertFalse((Boolean)venice.eval("(== (char \"a\") (char \"z\"))"));

		// Char/String
		assertTrue((Boolean)venice.eval("(== (char \"a\") \"a\")"));
		assertFalse((Boolean)venice.eval("(== (char \"a\") \"z\")"));
		assertFalse((Boolean)venice.eval("(== (char \"a\") \"xyz\")"));

		// Keyword
		assertTrue((Boolean)venice.eval("(== :a :a)"));
		assertFalse((Boolean)venice.eval("(== :a :b)"));

		// List
		assertTrue((Boolean)venice.eval("(== '(1 2) '(1 2))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '(1 4))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '(1))"));
		assertFalse((Boolean)venice.eval("(== '(1 2) '())"));
		assertFalse((Boolean)venice.eval("(== '(1 2) nil)"));

		// Vector
		assertTrue((Boolean)venice.eval("(== [1 2] [1 2])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [1 4])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [1])"));
		assertFalse((Boolean)venice.eval("(== [1 2] [])"));
		assertFalse((Boolean)venice.eval("(== [1 2] nil)"));

		// Map
		assertTrue((Boolean)venice.eval("(== {:a 1 :b 2} {:a 1 :b 2})"));
		assertFalse((Boolean)venice.eval("(== {:a 1 :b 2} {:a 1 :b 3})"));
		assertFalse((Boolean)venice.eval("(== {:a 1 :b 2} {:a 1 :c 2})"));
		assertFalse((Boolean)venice.eval("(== {:a 1 :b 2} {:a 1})"));
		assertFalse((Boolean)venice.eval("(== {:a 1 :b 2} {})"));
		assertFalse((Boolean)venice.eval("(== {:a 1 :b 2} nil)"));
	}
	
	@Test
	public void test_equals_strict_Q() {
		final Venice venice = new Venice();
		
		// Nil
		assertTrue((Boolean)venice.eval("(= nil nil)"));
		
		// Boolean
		assertTrue((Boolean)venice.eval("(= true true)"));
		assertFalse((Boolean)venice.eval("(= true false)"));

		// Integer
		assertTrue((Boolean)venice.eval("(= 2I 2I)"));
		assertFalse((Boolean)venice.eval("(= 2I 2)"));
		assertFalse((Boolean)venice.eval("(= 2I 2.0)"));
		assertFalse((Boolean)venice.eval("(= 2I 2.0M)"));
		assertFalse((Boolean)venice.eval("(= 2I 2N)"));
		assertFalse((Boolean)venice.eval("(= 2I 3I)"));
		assertFalse((Boolean)venice.eval("(= 2I 3)"));
		assertFalse((Boolean)venice.eval("(= 2I 3.0)"));
		assertFalse((Boolean)venice.eval("(= 2I 3.0M)"));
		assertFalse((Boolean)venice.eval("(= 2I 3N)"));

		// Long
		assertTrue((Boolean)venice.eval("(= 2 2)"));
		assertFalse((Boolean)venice.eval("(= 2 2I)"));
		assertFalse((Boolean)venice.eval("(= 2 2.0)"));
		assertFalse((Boolean)venice.eval("(= 2 2.0M)"));
		assertFalse((Boolean)venice.eval("(= 2 2N)"));
		assertFalse((Boolean)venice.eval("(= 2 2N)"));
		assertFalse((Boolean)venice.eval("(= 2 3)"));
		assertFalse((Boolean)venice.eval("(= 2 3I)"));
		assertFalse((Boolean)venice.eval("(= 2 3.0)"));
		assertFalse((Boolean)venice.eval("(= 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(= 2 3N)"));

		// Double
		assertTrue((Boolean)venice.eval("(= 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(= 2.0 2)"));
		assertFalse((Boolean)venice.eval("(= 2.0 2I)"));
		assertFalse((Boolean)venice.eval("(= 2.0 2.0M)"));
		assertFalse((Boolean)venice.eval("(= 2.0 2N)"));
		assertFalse((Boolean)venice.eval("(= 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(= 2.0 3)"));
		assertFalse((Boolean)venice.eval("(= 2.0 3I)"));
		assertFalse((Boolean)venice.eval("(= 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(= 2.0 3N)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(= 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 2)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 2I)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 2.0)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 2N)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 3)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 3I)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 3.0)"));
		assertFalse((Boolean)venice.eval("(= 2.0M 3N)"));

		// BigInteger
		assertTrue((Boolean)venice.eval("(= 2N 2N)"));
		assertFalse((Boolean)venice.eval("(= 2N 2)"));
		assertFalse((Boolean)venice.eval("(= 2N 2I)"));
		assertFalse((Boolean)venice.eval("(= 2N 2.0)"));
		assertFalse((Boolean)venice.eval("(= 2N 2.0M)"));
		assertFalse((Boolean)venice.eval("(= 2N 3N)"));
		assertFalse((Boolean)venice.eval("(= 2N 3)"));
		assertFalse((Boolean)venice.eval("(= 2N 3I)"));
		assertFalse((Boolean)venice.eval("(= 2N 3.0)"));
		assertFalse((Boolean)venice.eval("(= 2N 3.0M)"));

		// String
		assertTrue((Boolean)venice.eval("(= \"aa\" \"aa\")"));
		assertFalse((Boolean)venice.eval("(= \"aa\" \"zz\")"));

		// Char
		assertTrue((Boolean)venice.eval("(= (char \"a\") (char \"a\"))"));
		assertFalse((Boolean)venice.eval("(= (char \"a\") (char \"z\"))"));

		// Keyword
		assertTrue((Boolean)venice.eval("(= :a :a)"));
		assertFalse((Boolean)venice.eval("(= :a :b)"));

		// String/Keyword
		assertTrue((Boolean)venice.eval("(= \"aa\" :aa)"));
		assertTrue((Boolean)venice.eval("(= :aa \"aa\")"));

		// List
		assertTrue((Boolean)venice.eval("(= '(1 2) '(1 2))"));
		assertFalse((Boolean)venice.eval("(= '(1 2) '(1 4))"));
		assertFalse((Boolean)venice.eval("(= '(1 2) '(1))"));
		assertFalse((Boolean)venice.eval("(= '(1 2) '())"));
		assertFalse((Boolean)venice.eval("(= '(1 2) nil)"));

		// Vector
		assertTrue((Boolean)venice.eval("(= [1 2] [1 2])"));
		assertFalse((Boolean)venice.eval("(= [1 2] [1 4])"));
		assertFalse((Boolean)venice.eval("(= [1 2] [1])"));
		assertFalse((Boolean)venice.eval("(= [1 2] [])"));
		assertFalse((Boolean)venice.eval("(= [1 2] nil)"));

		// Map
		assertTrue((Boolean)venice.eval("(= {:a 1 :b 2} {:a 1 :b 2})"));
		assertFalse((Boolean)venice.eval("(= {:a 1 :b 2} {:a 1 :b 3})"));
		assertFalse((Boolean)venice.eval("(= {:a 1 :b 2} {:a 1 :c 2})"));
		assertFalse((Boolean)venice.eval("(= {:a 1 :b 2} {:a 1})"));
		assertFalse((Boolean)venice.eval("(= {:a 1 :b 2} {})"));
		assertFalse((Boolean)venice.eval("(= {:a 1 :b 2} nil)"));
	}
	
	@Test
	public void test_empty_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(empty? nil)"));	
		assertTrue((Boolean)venice.eval("(empty? \"\")"));	
		assertTrue((Boolean)venice.eval("(empty? '())"));	
		assertTrue((Boolean)venice.eval("(empty? [])"));	
		assertTrue((Boolean)venice.eval("(empty? {})"));	
		assertTrue((Boolean)venice.eval("(empty? (stack))"));	

		assertFalse((Boolean)venice.eval("(empty? \"a\")"));	
		assertFalse((Boolean)venice.eval("(empty? '(1))"));	
		assertFalse((Boolean)venice.eval("(empty? [1])"));	
		assertFalse((Boolean)venice.eval("(empty? (push! (stack) 1))"));	
	}
	
	@Test
	public void test_empty_to_nil() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(empty-to-nil \"\")"));	
		assertNull(venice.eval("(empty-to-nil '())"));	
		assertNull(venice.eval("(empty-to-nil [])"));	
		assertNull(venice.eval("(empty-to-nil {})"));	

		assertNotNull(venice.eval("(empty-to-nil \"a\")"));	
		assertNotNull(venice.eval("(empty-to-nil '(1))"));	
		assertNotNull(venice.eval("(empty-to-nil [1])"));	
		assertNotNull(venice.eval("(empty-to-nil {:a 1})"));	
	}
	
	@Test
	public void test_every_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) nil)"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [])"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [:a])"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) [:a 1 2])"));	
		assertFalse((Boolean)venice.eval("(every? (fn [x] (number? x)) (set :a 1 2))"));	
		assertTrue((Boolean)venice.eval("(every? (fn [x] (number? x)) [1])"));	
		assertTrue((Boolean)venice.eval("(every? (fn [x] (number? x)) [1 2])"));	
		assertTrue((Boolean)venice.eval("(every? (fn [x] (number? x)) (set 1 2))"));	
	}
	
	@Test
	public void test_every_pred() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("((every-pred number?) 1)"));	
		assertTrue((Boolean)venice.eval("((every-pred number?) 1 2)"));	
		assertTrue((Boolean)venice.eval("((every-pred number?) 1 2 3)"));	
		
		assertFalse((Boolean)venice.eval("((every-pred number?) 1 true)"));	
		assertFalse((Boolean)venice.eval("((every-pred number?) 1 2 true)"));	
		assertFalse((Boolean)venice.eval("((every-pred number?) 1 true 2 3)"));	

		assertTrue((Boolean)venice.eval("((every-pred number? pos?) 1)"));	
		assertTrue((Boolean)venice.eval("((every-pred number? pos?) 1 2)"));	
		assertTrue((Boolean)venice.eval("((every-pred number? pos?) 1 2 3)"));	
		
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 -1)"));	
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 2 -1)"));	
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 -1 2 3)"));	
		
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 -1 true)"));	
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 2 true -1)"));	
		assertFalse((Boolean)venice.eval("((every-pred number? pos?) 1 -1 true 2 3)"));	
	}
	
	@Test
	public void test_false_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(false? nil)"));	
		assertFalse((Boolean)venice.eval("(false? true)"));	
		assertTrue((Boolean)venice.eval("(false? false)"));	
		assertFalse((Boolean)venice.eval("(false? 1)"));	
	}
	
	@Test
	public void test_flush() {
		final Venice venice = new Venice();

		assertEquals("1\n", venice.eval("(with-out-str (do (println 1) (flush )))"));	
		assertEquals("1\n", venice.eval("(with-out-str (do (println 1) (flush (. :java.lang.System :out))))"));	
		assertEquals("1\n", venice.eval("(with-out-str (do (println 1) (flush *out*)))"));	
	}

	@Test
	public void test_filter_k() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (filter-k (fn [k] (= k :d)) {:a 1 :b 2 :c 3}))"));
		
		assertEquals("{:a 1}", venice.eval("(str (filter-k (fn [k] (= k :a)) {:a 1 :b 2 :c 3}))"));
	}

	@Test
	public void test_filter_kv() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (filter-kv (fn [k v] (= k :d)) {:a 1 :b 2 :c 3}))"));
		assertEquals("{:a 1}", venice.eval("(str (filter-kv (fn [k v] (= k :a)) {:a 1 :b 2 :c 3}))"));
		
		assertEquals("{}", venice.eval("(str (filter-kv (fn [k v] (= v 0)) {:a 1 :b 2 :c 3}))"));
		assertEquals("{:a 1}", venice.eval("(str (filter-kv (fn [k v] (= v 1)) {:a 1 :b 2 :c 3}))"));
	}

	@Test
	public void test_find() {
		final Venice venice = new Venice();

		assertEquals("[:b 2]", venice.eval("(str (find {:a 1 :b 2} :b))"));
		assertEquals(null, venice.eval("(find {:a 1 :b 2} :z)"));
	}
	
	@Test
	public void test_first() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(first \"\")"));
		assertEquals("1", venice.eval("(first \"1\")"));
		assertEquals("1", venice.eval("(first \"12\")"));
		assertEquals("1", venice.eval("(first \"123\")"));

		assertNull(venice.eval("(first '())"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1))"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1 2))"));
		assertEquals(Long.valueOf(1), venice.eval("(first '(1 2 3))"));

		assertNull(venice.eval("(first [])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1 2])"));
		assertEquals(Long.valueOf(1), venice.eval("(first [1 2 3])"));
	}
	
	@Test
	public void test_fnil() {
		final Venice venice = new Venice();

		assertEquals(100L, venice.eval("((fnil + 100) nil)"));	
		assertEquals(100L, venice.eval("((fnil + 100) nil)"));	
		assertEquals(101L, venice.eval("((fnil + 100) nil 1)"));	
		assertEquals(103L, venice.eval("((fnil + 100) nil 1 2)"));	
		assertEquals(145L, venice.eval("((fnil + 100) nil 1 2 3 4 5 6 7 8 9)"));	

		assertEquals(200L, venice.eval("((fnil + 100) 200)"));	
		assertEquals(200L, venice.eval("((fnil + 100) 200)"));	
		assertEquals(201L, venice.eval("((fnil + 100) 200 1)"));	
		assertEquals(203L, venice.eval("((fnil + 100) 200 1 2)"));	
		assertEquals(245L, venice.eval("((fnil + 100) 200 1 2 3 4 5 6 7 8 9)"));
		
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10))"));

		
		
		assertEquals(1100L, venice.eval("((fnil + 1000 100) nil nil)"));	
		assertEquals(1101L, venice.eval("((fnil + 1000 100) nil nil 1)"));	
		assertEquals(1103L, venice.eval("((fnil + 1000 100) nil nil 1 2)"));	
		assertEquals(1145L, venice.eval("((fnil + 1000 100) nil nil 1 2 3 4 5 6 7 8 9)"));	

		assertEquals(2100L, venice.eval("((fnil + 1000 100) 2000 nil)"));	
		assertEquals(2101L, venice.eval("((fnil + 1000 100) 2000 nil 1)"));	
		assertEquals(2103L, venice.eval("((fnil + 1000 100) 2000 nil 1 2)"));	
		assertEquals(2145L, venice.eval("((fnil + 1000 100) 2000 nil 1 2 3 4 5 6 7 8 9)"));	

		assertEquals(1200L, venice.eval("((fnil + 1000 100) nil 200)"));	
		assertEquals(1201L, venice.eval("((fnil + 1000 100) nil 200 1)"));	
		assertEquals(1203L, venice.eval("((fnil + 1000 100) nil 200 1 2)"));	
		assertEquals(1245L, venice.eval("((fnil + 1000 100) nil 200 1 2 3 4 5 6 7 8 9)"));	

		assertEquals(2200L, venice.eval("((fnil + 1000 100) 2000 200)"));	
		assertEquals(2201L, venice.eval("((fnil + 1000 100) 2000 200 1)"));	
		assertEquals(2203L, venice.eval("((fnil + 1000 100) 2000 200 1 2)"));	
		assertEquals(2245L, venice.eval("((fnil + 1000 100) 2000 200 1 2 3 4 5 6 7 8 9)"));	
		
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10 20))"));
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10 20) nil)"));

		
		
		assertEquals(11100L, venice.eval("((fnil + 10000 1000 100) nil nil nil)"));	
		assertEquals(11101L, venice.eval("((fnil + 10000 1000 100) nil nil nil 1)"));	
		assertEquals(11103L, venice.eval("((fnil + 10000 1000 100) nil nil nil 1 2)"));	
		assertEquals(11145L, venice.eval("((fnil + 10000 1000 100) nil nil nil 1 2 3 4 5 6 7 8 9)"));	
		
		assertEquals(21100L, venice.eval("((fnil + 10000 1000 100) 20000 nil nil)"));	
		assertEquals(21101L, venice.eval("((fnil + 10000 1000 100) 20000 nil nil 1)"));	
		assertEquals(21103L, venice.eval("((fnil + 10000 1000 100) 20000 nil nil 1 2)"));	
		assertEquals(21145L, venice.eval("((fnil + 10000 1000 100) 20000 nil nil 1 2 3 4 5 6 7 8 9)"));	
		
		assertEquals(12100L, venice.eval("((fnil + 10000 1000 100) nil 2000 nil)"));	
		assertEquals(12101L, venice.eval("((fnil + 10000 1000 100) nil 2000 nil 1)"));	
		assertEquals(12103L, venice.eval("((fnil + 10000 1000 100) nil 2000 nil 1 2)"));	
		assertEquals(12145L, venice.eval("((fnil + 10000 1000 100) nil 2000 nil 1 2 3 4 5 6 7 8 9)"));	
		
		assertEquals(11200L, venice.eval("((fnil + 10000 1000 100) nil nil 200)"));	
		assertEquals(11201L, venice.eval("((fnil + 10000 1000 100) nil nil 200 1)"));	
		assertEquals(11203L, venice.eval("((fnil + 10000 1000 100) nil nil 200 1 2)"));	
		assertEquals(11245L, venice.eval("((fnil + 10000 1000 100) nil nil 200 1 2 3 4 5 6 7 8 9)"));	
		
		assertEquals(22200L, venice.eval("((fnil + 10000 1000 100) 20000 2000 200)"));	
		assertEquals(22201L, venice.eval("((fnil + 10000 1000 100) 20000 2000 200 1)"));	
		assertEquals(22203L, venice.eval("((fnil + 10000 1000 100) 20000 2000 200 1 2)"));	
		assertEquals(22245L, venice.eval("((fnil + 10000 1000 100) 20000 2000 200 1 2 3 4 5 6 7 8 9)"));	
		
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10 20 30))"));
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10 20 30) nil)"));
		assertThrows(VncException.class, () -> new Venice().eval("((fnil + 10 20 30) nil nil)"));
	}

	@Test
	public void test_fn_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(do (def add5 (fn [x] (+ x 5))) (fn? add5))"));
	}

	@Test
	public void test_fn_body() {
		final Venice venice = new Venice();
		
		assertEquals("((if x false true))", venice.eval("(pr-str (fn-body not))"));
	}
	
	@Test
	public void test_frequencies() {
		final Venice venice = new Venice();
		
		assertEquals(3L, venice.eval("(:a (frequencies [:a :a :b :a]))"));
		assertEquals(1L, venice.eval("(:b (frequencies [:a :a :b :a]))"));
	}

	@Test
	public void test_gensym() {
		final Venice venice = new Venice();

		assertNotNull(venice.eval("(gensym)"));
		assertNotNull(venice.eval("(gensym :abc)"));
		assertNotNull(venice.eval("(gensym \"abc\")"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym))"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym :abc))"));
		assertTrue((Boolean)venice.eval("(symbol? (gensym \"abc\"))"));
		assertTrue(((String)venice.eval("(str (gensym :abc__))")).startsWith("abc__"));
	}
	
	@Test
	public void test_get() {
		final Venice venice = new Venice();

		// maps
		assertEquals(2L, venice.eval("(get {:a 1 :b 2} :b)"));
		assertEquals(null, venice.eval("(get {:a 1 :b 2} :z)"));
		assertEquals(null, venice.eval("(get {} :z)"));
		assertEquals(9L, venice.eval("(get {:a 1 :b 2} :z 9)"));
		assertEquals(9L, venice.eval("(get {} :z 9)"));

		assertEquals(2L, venice.eval("(get (ordered-map :a 1 :b 2) :b)"));
		assertEquals(null, venice.eval("(get (ordered-map :a 1 :b 2) :z)"));
		assertEquals(null, venice.eval("(get (ordered-map ) :z)"));
		assertEquals(9L, venice.eval("(get (ordered-map :a 1 :b 2) :z 9)"));
		assertEquals(9L, venice.eval("(get (ordered-map ) :z 9)"));

		assertEquals(2L, venice.eval("(get (sorted-map :a 1 :b 2) :b)"));
		assertEquals(null, venice.eval("(get (sorted-map :a 1 :b 2) :z)"));
		assertEquals(null, venice.eval("(get (sorted-map ) :z)"));
		assertEquals(9L, venice.eval("(get (sorted-map :a 1 :b 2) :z 9)"));
		assertEquals(9L, venice.eval("(get (sorted-map ) :z 9)"));

		// sets
		assertEquals(2L, venice.eval("(get #{1 2} 2)"));
		assertEquals(null, venice.eval("(get #{1 2} 3)"));
		assertEquals(null, venice.eval("(get #{} 2)"));
		assertEquals(9L, venice.eval("(get #{1 2} 3 9)"));
		assertEquals(9L, venice.eval("(get #{} 3 9)"));

		// vector
		assertEquals(2L, venice.eval("(get [1 2 3] 1)"));
		assertEquals(null, venice.eval("(get [1 2 3] 5)"));
		assertEquals(9L, venice.eval("(get [1 2 3] 5 9)"));
	}
	
	@Test
	public void test_get_in() {
		final Venice venice = new Venice();
		
		// map
		assertEquals("", venice.eval("(str (get-in {:a 1} [:b]))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} [:a]))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} [:b :c]))"));

		assertEquals("9", venice.eval("(str (get-in {:a 1} [:b] 9))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} [:a] 9))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} [:b :c] 9))"));

		assertEquals("", venice.eval("(str (get-in {:a 1} '(:b)))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} '(:a)))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} '(:b :c)))"));

		assertEquals("9", venice.eval("(str (get-in {:a 1} '(:b) 9))"));
		assertEquals("1", venice.eval("(str (get-in {:a 1} '(:a) 9))"));
		assertEquals("2", venice.eval("(str (get-in {:a 1 :b {:c 2}} '(:b :c) 9))"));
		
		// list
		assertEquals("", venice.eval("(str (get-in '() [0]))"));
		assertEquals("1", venice.eval("(str (get-in '(1) [0]))"));
		assertEquals("1", venice.eval("(str (get-in '(1 2) [0]))"));
		assertEquals("2", venice.eval("(str (get-in '(1 2) [1]))"));
		assertEquals("", venice.eval("(str (get-in '(1 2) [2]))"));
		assertEquals("", venice.eval("(str (get-in '(1 2) [0 0]))"));
		assertEquals("1", venice.eval("(str (get-in '((1) 2) [0 0]))"));
		assertEquals("2", venice.eval("(str (get-in '((1 2) 2) [0 1]))"));
		assertEquals("", venice.eval("(str (get-in '((1 2) 2) [0 2]))"));

		assertEquals("9", venice.eval("(str (get-in '() [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '(1) [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '(1 2) [0] 9))"));
		assertEquals("2", venice.eval("(str (get-in '(1 2) [1] 9))"));
		assertEquals("9", venice.eval("(str (get-in '(1 2) [2] 9))"));
		assertEquals("9", venice.eval("(str (get-in '(1 2) [0 0] 9))"));
		assertEquals("1", venice.eval("(str (get-in '((1) 2) [0 0] 9))"));
		assertEquals("2", venice.eval("(str (get-in '((1 2) 2) [0 1] 9))"));
		assertEquals("9", venice.eval("(str (get-in '((1 2) 2) [0 2] 9))"));
		
		// vector
		assertEquals("", venice.eval("(str (get-in [] [0]))"));
		assertEquals("1", venice.eval("(str (get-in [1] [0]))"));
		assertEquals("1", venice.eval("(str (get-in [1 2] [0]))"));
		assertEquals("2", venice.eval("(str (get-in [1 2] [1]))"));
		assertEquals("", venice.eval("(str (get-in [1 2] [2]))"));
		assertEquals("", venice.eval("(str (get-in [1 2] [0 0]))"));
		assertEquals("1", venice.eval("(str (get-in ['(1) 2] [0 0]))"));
		assertEquals("2", venice.eval("(str (get-in ['(1 2) 2] [0 1]))"));
		assertEquals("", venice.eval("(str (get-in ['(1 2) 2] [0 2]))"));

		assertEquals("9", venice.eval("(str (get-in [] [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in [1] [0] 9))"));
		assertEquals("1", venice.eval("(str (get-in [1 2] [0] 9))"));
		assertEquals("2", venice.eval("(str (get-in [1 2] [1] 9))"));
		assertEquals("9", venice.eval("(str (get-in [1 2] [2] 9))"));
		assertEquals("9", venice.eval("(str (get-in [1 2] [0 0] 9))"));
		assertEquals("1", venice.eval("(str (get-in ['(1) 2] [0 0] 9))"));
		assertEquals("2", venice.eval("(str (get-in ['(1 2) 2] [0 1] 9))"));
		assertEquals("9", venice.eval("(str (get-in ['(1 2) 2] [0 2] 9))"));
		
		// map / vector
		assertEquals(":y", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 1]))"));
		assertEquals("", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 5]))"));
		assertEquals("9", venice.eval("(str (get-in {:a 1 :b [:x :y :z]} [:b 5] 9))"));
		
		// vector / map
		assertEquals("1", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :c]))"));
		assertEquals("", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :x]))"));
		assertEquals("9", venice.eval("(str (get-in [:a :b {:c 1 :d 2} :e] [2 :x] 9))"));
	}

	@Test
	public void test_gt() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(> 2 3)"));
		assertFalse((Boolean)venice.eval("(> 2 2)"));
		assertTrue((Boolean)venice.eval("(> 2 1)"));
		assertFalse((Boolean)venice.eval("(> 2 3.0)"));
		assertFalse((Boolean)venice.eval("(> 2 2.0)"));
		assertTrue((Boolean)venice.eval("(> 2 1.0)"));
		assertFalse((Boolean)venice.eval("(> 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2 1.0M)"));
		assertFalse((Boolean)venice.eval("(> 2 3N)"));
		assertFalse((Boolean)venice.eval("(> 2 2N)"));
		assertTrue((Boolean)venice.eval("(> 2 1N)"));

		assertTrue((Boolean)venice.eval("(> 4 3 2)"));
		assertFalse((Boolean)venice.eval("(> 4 1 2)"));
		assertFalse((Boolean)venice.eval("(> 4 6 2)"));
		assertFalse((Boolean)venice.eval("(> 2 3 2)"));

		assertTrue((Boolean)venice.eval("(> 9 8 7 6 5 4 3 2)"));

		// Double
		assertFalse((Boolean)venice.eval("(> 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1.0)"));
		assertFalse((Boolean)venice.eval("(> 2.0 3)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1)"));
		assertFalse((Boolean)venice.eval("(> 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0 3N)"));
		assertFalse((Boolean)venice.eval("(> 2.0 2N)"));
		assertTrue((Boolean)venice.eval("(> 2.0 1N)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(> 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1.0M)"));
		assertFalse((Boolean)venice.eval("(> 2.0M 3))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2))"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 3.0))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2.0))"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1.0))"));
		assertFalse((Boolean)venice.eval("(> 2.0M 3N)"));
		assertFalse((Boolean)venice.eval("(> 2.0M 2N)"));
		assertTrue((Boolean)venice.eval("(> 2.0M 1N)"));

		// BigInteger
		assertFalse((Boolean)venice.eval("(> 2N 3N)"));
		assertFalse((Boolean)venice.eval("(> 2N 2N)"));
		assertTrue((Boolean)venice.eval("(> 2N 1N)"));
		assertFalse((Boolean)venice.eval("(> 2N 3))"));
		assertFalse((Boolean)venice.eval("(> 2N 2))"));
		assertTrue((Boolean)venice.eval("(> 2N 1))"));
		assertFalse((Boolean)venice.eval("(> 2N 3.0))"));
		assertFalse((Boolean)venice.eval("(> 2N 2.0))"));
		assertTrue((Boolean)venice.eval("(> 2N 1.0))"));
		assertFalse((Boolean)venice.eval("(> 2N 3.0M)"));
		assertFalse((Boolean)venice.eval("(> 2N 2.0M)"));
		assertTrue((Boolean)venice.eval("(> 2N 1.0M)"));
	}

	@Test
	public void test_gte() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(>= 2 3)"));
		assertTrue((Boolean)venice.eval("(>= 2 2)"));
		assertTrue((Boolean)venice.eval("(>= 2 1)"));
		assertFalse((Boolean)venice.eval("(>= 2 3.0)"));
		assertTrue((Boolean)venice.eval("(>= 2 2.0)"));
		assertTrue((Boolean)venice.eval("(>= 2 1.0)"));
		assertFalse((Boolean)venice.eval("(>= 2 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2 1.0M)"));
		assertFalse((Boolean)venice.eval("(>= 2 3N)"));
		assertTrue((Boolean)venice.eval("(>= 2 2N)"));
		assertTrue((Boolean)venice.eval("(>= 2 1N)"));

		assertTrue((Boolean)venice.eval("(>= 4 3 2)"));
		assertFalse((Boolean)venice.eval("(>= 4 1 2)"));
		assertFalse((Boolean)venice.eval("(>= 4 6 2)"));
		assertFalse((Boolean)venice.eval("(>= 2 3 2)"));

		assertTrue((Boolean)venice.eval("(>= 9 8 7 6 5 4 3 2)"));

		// Double
		assertFalse((Boolean)venice.eval("(>= 2.0 3.0)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2.0)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1.0)"));
		assertFalse((Boolean)venice.eval("(>= 2.0 3)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1)"));
		assertFalse((Boolean)venice.eval("(>= 2.0 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1.0M)"));
		assertFalse((Boolean)venice.eval("(>= 2.0 3N)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 2N)"));
		assertTrue((Boolean)venice.eval("(>= 2.0 1N)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(>= 2.0M 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1.0M)"));
		assertFalse((Boolean)venice.eval("(>= 2.0M 3))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1))"));
		assertFalse((Boolean)venice.eval("(>= 2.0M 3.0))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2.0))"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1.0))"));
		assertFalse((Boolean)venice.eval("(>= 2.0M 3N)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 2N)"));
		assertTrue((Boolean)venice.eval("(>= 2.0M 1N)"));

		// BigInteger
		assertFalse((Boolean)venice.eval("(>= 2N 3N)"));
		assertTrue((Boolean)venice.eval("(>= 2N 2N)"));
		assertTrue((Boolean)venice.eval("(>= 2N 1N)"));
		assertFalse((Boolean)venice.eval("(>= 2N 3))"));
		assertTrue((Boolean)venice.eval("(>= 2N 2))"));
		assertTrue((Boolean)venice.eval("(>= 2N 1))"));
		assertFalse((Boolean)venice.eval("(>= 2N 3.0))"));
		assertTrue((Boolean)venice.eval("(>= 2N 2.0))"));
		assertTrue((Boolean)venice.eval("(>= 2N 1.0))"));
		assertFalse((Boolean)venice.eval("(>= 2N 3.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2N 2.0M)"));
		assertTrue((Boolean)venice.eval("(>= 2N 1.0M)"));
	}

	@Test
	public void test_group_by() {
		final Venice venice = new Venice();
		
		assertEquals("{1 [a] 2 [as aa] 3 [asd] 4 [asdf qwer]}", venice.eval("(str (group-by count [\"a\" \"as\" \"asd\" \"aa\" \"asdf\" \"qwer\"]))"));

		assertEquals("{false [0 2 4 6 8] true [1 3 5 7 9]}", venice.eval("(str (group-by odd? (range 0 10)))"));
	}
	
	@Test
	public void test_hash_map_exception() {
		assertThrows(VncException.class, () -> {
			new Venice().eval("{:a 1, :b}");
		});
	}
	
	@Test
	public void test_hash_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str {})"));
		assertEquals("{:a 1}", venice.eval("(str {:a 1})"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str {:a 1 :b 2})"));

		assertEquals("{}", venice.eval("(str (hash-map))"));
		assertEquals("{:a 1}", venice.eval("(str (hash-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (hash-map :a 1 :b 2))"));
	}
	
	@Test
	public void test_hash_map_convertToJava() {
		final Venice venice = new Venice();

		assertEquals("{a=null}", venice.eval("(hash-map :a nil)").toString());
	}
	
	@Test
	public void test_hash_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(hash-map? nil)"));	
		assertFalse((Boolean)venice.eval("(hash-map? 1)"));	
		assertFalse((Boolean)venice.eval("(hash-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? [1 2])"));	
		assertTrue((Boolean)venice.eval("(hash-map? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(hash-map? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(hash-map? (set 1 2))"));	
	}

	@Test
	public void test_highlight() {
		final Venice venice = new Venice();

		@SuppressWarnings("unchecked")
		final List<Object> list = (List<Object>)venice.eval("(highlight \"(+ 10 20)\")");
		assertEquals(7, list.size());
	}	

	@Test
	public void test_identity() {
		final Venice venice = new Venice();

		assertEquals("4", venice.eval("(str (identity 4))"));
		assertEquals("(1 2 3 4 true 1234)", venice.eval("(str (filter identity [1 2 3 nil 4 false true 1234]))"));		
	}	

	@Test
	public void test_instance_of_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(instance-of? :core/nil nil)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/boolean true)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/boolean false)"));
		
		assertTrue((Boolean)venice.eval("(instance-of? :core/keyword :a)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/symbol 'a)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/atom (atom 0))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/thread-local (thread-local))"));
		
		assertTrue((Boolean)venice.eval("(instance-of? :core/long 1)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/double 1.0)"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/decimal 1.0M)"));

		assertTrue((Boolean)venice.eval("(instance-of? :core/just (just 1))"));

		assertTrue((Boolean)venice.eval("(instance-of? :core/list '())"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/list (list))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/vector '[])"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/vector (vector))"));

		assertTrue((Boolean)venice.eval("(instance-of? :core/set '#{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/set (sorted-set))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/set (mutable-set))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/hash-set '#{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/sorted-set (sorted-set))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/mutable-set (mutable-set))"));
		
		assertTrue((Boolean)venice.eval("(instance-of? :core/map '{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/map (hash-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/map (sorted-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/map (ordered-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/map (mutable-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/hash-map '{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/hash-map (hash-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/sorted-map (sorted-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/ordered-map (ordered-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/mutable-map (mutable-map))"));

		assertTrue((Boolean)venice.eval("(instance-of? :core/sequence '())"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/sequence '[])"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/sequence (list))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/sequence (vector))"));
	
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection '())"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection '[])"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (list))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (vector))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection '#{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (sorted-set))"));	
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection '{})"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (hash-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (sorted-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (ordered-map))"));
		assertTrue((Boolean)venice.eval("(instance-of? :core/collection (mutable-map))"));

		// Java Interop		
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.List      (. :java.util.ArrayList :new))"));
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.ArrayList (. :java.util.ArrayList :new))"));
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.Set       (. :java.util.HashSet :new))"));
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.HashSet   (. :java.util.HashSet :new))"));
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.Map       (. :java.util.HashMap :new))"));
		assertTrue((Boolean)venice.eval("(instance-of? :java.util.HashMap   (. :java.util.HashMap :new))"));

		// Java Interop		
		assertTrue((Boolean)venice.eval(
				"(do                                         \n" +
				"  (import :java.util.List)                  \n" +
				"  (import :java.util.ArrayList)             \n" +
				"  (instance-of? :List (. :ArrayList :new)))   "));
		assertTrue((Boolean)venice.eval(
				"(do                                              \n" +
				"  (import :java.util.ArrayList)                  \n" +
				"  (instance-of? :ArrayList (. :ArrayList :new)))   "));

		// Custom Types	
		assertTrue((Boolean)venice.eval(
						"(do                                                          \n" +
						"  (deftype :user/complex [real :long, imaginary :long])      \n" +
						"  (instance-of? :user/complex (.: :user/complex 100 200)))      "));
		assertTrue((Boolean)venice.eval(
						"(do                                                          \n" +
						"  (deftype :user/complex [real :long, imaginary :long])      \n" +
						"  (instance-of? :core/custom-type (.: :user/complex 100 200)))  "));
		assertTrue((Boolean)venice.eval(
						"(do                                                          \n" +
						"  (deftype :user/complex [real :long, imaginary :long])      \n" +
						"  (instance-of? :core/map (.: :user/complex 100 200)))          "));
	}
	
	@Test
	public void test_int() {
		final Venice venice = new Venice();

		assertEquals(Integer.valueOf(0), venice.eval("(int nil)"));
		assertEquals(Integer.valueOf(0), venice.eval("(int false)"));
		assertEquals(Integer.valueOf(1), venice.eval("(int true)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int 3)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int 3I)"));
		assertEquals(Integer.valueOf(255), venice.eval("(int 0x0FFI)"));
		assertEquals(Integer.valueOf(255), venice.eval("(int 0X0FFI)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int 3.1)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int 3.0M)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int 3N)"));
		assertEquals(Integer.valueOf(3), venice.eval("(int \"3\")"));
	}

	@Test
	public void test_int_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(int? nil)"));	
		assertFalse((Boolean)venice.eval("(int? true)"));	
		assertFalse((Boolean)venice.eval("(int? 1)"));	
		assertTrue((Boolean)venice.eval("(int? 3I)"));
		assertTrue((Boolean)venice.eval("(int? 0x0FFI)"));
		assertTrue((Boolean)venice.eval("(int? 0X0FFI)"));
		assertFalse((Boolean)venice.eval("(int? -3.0)"));
		assertFalse((Boolean)venice.eval("(int? -3.0M)"));
		assertFalse((Boolean)venice.eval("(int? -3N)"));
		assertFalse((Boolean)venice.eval("(int? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(int? :a)"));
		assertFalse((Boolean)venice.eval("(int? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(int? '())"));
		assertFalse((Boolean)venice.eval("(int? [])"));
		assertFalse((Boolean)venice.eval("(int? {})"));
	}

	@Test
	public void test_interleave() {
		final Venice venice = new Venice();
		
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b] [1 2]))"));
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b :c] [1 2]))"));
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b] [1 2 3]))"));

		assertEquals("(:a 1 10 :b 2 20)", venice.eval("(str (interleave [:a :b] [1 2] [10 20]))"));
		assertEquals("(:a 1 10 :b 2 20)", venice.eval("(str (interleave [:a :b :c] [1 2] [10 20]))"));
		assertEquals("(:a 1 10 :b 2 20)", venice.eval("(str (interleave [:a :b] [1 2 3] [10 20 30]))"));
		
		// with lazy sequences 
		assertEquals("(:a 10 :b 10)", venice.eval("(str (interleave [:a :b] (repeat 10)))"));
		assertEquals("(10 :a 10 :b)", venice.eval("(str (interleave (repeat 10) [:a :b]))"));

		assertEquals("(:a 1 10 :b 2 10)", venice.eval("(str (interleave [:a :b] [1 2] (repeat 10)))"));
		assertEquals("(10 :a 1 10 :b 2)", venice.eval("(str (interleave (repeat 10) [:a :b] [1 2]))"));

		assertEquals("(:a 1 10 :b 2 10)", venice.eval("(str (interleave [:a :b] [1 2 3] (repeat 10)))"));
		assertEquals("(10 :a 1 10 :b 2)", venice.eval("(str (interleave (repeat 10) [:a :b] [1 2 3]))"));
		
		assertEquals("(:a 1 :b 2)", venice.eval("(str (interleave [:a :b] (lazy-seq 1 #(+ % 1))))"));
	}
	
	@Test
	public void test_interpose() {
		final Venice venice = new Venice();
		
		assertEquals("(1 - 2 - 3)", venice.eval("(str (interpose \"-\" [1 2 3]))"));
		assertEquals("1-2-3", venice.eval("(apply str (interpose \"-\" [1 2 3]))"));
	}

	@Test
	public void test_intersection() {
		final Venice venice = new Venice();

		assertEquals("#{1}", venice.eval("(str (intersection (set 1)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (set 2 3) (set 2 4)))"));
		assertEquals("#{}", venice.eval("(str (intersection (set 1 2) (set 3 4)))"));

		assertEquals("#{1}", venice.eval("(str (intersection (sorted-set 1)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (sorted-set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (sorted-set 1 2) (sorted-set 2 3) (sorted-set 2 4)))"));
		assertEquals("#{}", venice.eval("(str (intersection (sorted-set 1 2) (sorted-set 3 4)))"));

		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{2}", venice.eval("(str (intersection (set 1 2) (set 2 3) (sorted-set 2 4)))"));
		assertEquals("#{}", venice.eval("(str (intersection (set 1 2) (sorted-set 3 4)))"));
	}
		
	@Test
	public void test_into() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (into '() '()))"));
		assertEquals("(1)", venice.eval("(str (into '() '(1)))"));
		assertEquals("(2 1)", venice.eval("(str (into '() '(1 2)))"));

		assertEquals("()", venice.eval("(str (into '() []))"));
		assertEquals("(1)", venice.eval("(str (into '() [1]))"));
		assertEquals("(2 1)", venice.eval("(str (into '() [1 2]))"));

		assertEquals("[]", venice.eval("(str (into [] '()))"));
		assertEquals("[1]", venice.eval("(str (into [] '(1)))"));
		assertEquals("[1 2]", venice.eval("(str (into [] '(1 2)))"));

		assertEquals("[]", venice.eval("(str (into [] []))"));
		assertEquals("[1]", venice.eval("(str (into [] [1]))"));
		assertEquals("[1 2]", venice.eval("(str (into [] [1 2]))"));

		
		assertEquals("(0)", venice.eval("(str (into '(0) '()))"));
		assertEquals("(1 0)", venice.eval("(str (into '(0) '(1)))"));
		assertEquals("(2 1 0)", venice.eval("(str (into '(0) '(1 2)))"));

		assertEquals("(0)", venice.eval("(str (into '(0) []))"));
		assertEquals("(1 0)", venice.eval("(str (into '(0) [1]))"));
		assertEquals("(2 1 0)", venice.eval("(str (into '(0) [1 2]))"));
		assertEquals("(3 2 1)", venice.eval("(str (into '() '(1 2 3)))"));
		assertEquals("([1 2] [3 4])", venice.eval("(str (sort (into '() {1 2 3 4})))"));
		assertEquals("(6 5 4 1 2 3)", venice.eval("(str (into '(1 2 3) '(4 5 6)))"));
		assertEquals("(6 5 4 1 2 3)", venice.eval("(str (into '(1 2 3) [4 5 6]))"));

		assertEquals("(a b c)", venice.eval("(str (into '() \"abc\"))"));
		
				
		assertEquals("[0]", venice.eval("(str (into [0] '()))"));
		assertEquals("[0 1]", venice.eval("(str (into [0] '(1)))"));
		assertEquals("[0 1 2]", venice.eval("(str (into [0] '(1 2)))"));

		assertEquals("[0]", venice.eval("(str (into [0] []))"));
		assertEquals("[0 1]", venice.eval("(str (into [0] [1]))"));
		assertEquals("[0 1 2]", venice.eval("(str (into [0] [1 2]))"));
		assertEquals("[[1 2] [3 4]]", venice.eval("(str (sort (into [] {1 2, 3 4})))"));
		assertEquals("[1 2 3 4 5 6]", venice.eval("(str (into [1 2 3] '(4 5 6)))"));
		assertEquals("[1 2 3 4 5 6]", venice.eval("(str (into [1 2 3] [4 5 6]))"));
		assertEquals("[a b c]", venice.eval("(str (into [] \"abc\"))"));
		
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) [[:a 1] [:b 2] [:c 3]] ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) [{:a 1} {:b 2} {:c 3}] ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) { :a 1 :b 2 :c 3} ))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (into (ordered-map) (ordered-map :a 1 :b 2 :c 3) ))"));
	}
	
	@Test
	public void test_juxt() {
		final Venice venice = new Venice();

		assertEquals("[1 4]", venice.eval("(str ((juxt first last) '(1 2 3 4)))))"));
	}
	
	@Test
	public void test_key() {
		final Venice venice = new Venice();

		assertEquals(":b", venice.eval("(str (key (find {:a 1 :b 2} :b)))"));
	}
	
	@Test
	public void test_keys() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (keys {})))"));
		assertEquals("(:a :b :c)", venice.eval("(str (sort (keys {:a 1 :b 2 :c 3})))"));

		assertEquals("()", venice.eval("(str (keys (ordered-map )))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (ordered-map :a 1 :b 2 :c 3)))"));

		assertEquals("()", venice.eval("(str (keys (sorted-map )))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (sorted-map :a 1 :b 2 :c 3)))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (sorted-map :b 2 :a 1 :c 3)))"));
		assertEquals("(:a :b :c)", venice.eval("(str (keys (sorted-map :c 3 :a 1 :b 2)))"));
	}
	
	@Test
	public void test_keyword() {
		final Venice venice = new Venice();

		assertEquals(":a", venice.eval("(str (keyword :a))"));
		assertEquals(":a", venice.eval("(str (keyword \"a\"))"));
		assertTrue((Boolean)venice.eval("(keyword? (keyword :a))"));	
		assertTrue((Boolean)venice.eval("(keyword? (keyword \"a\"))"));	
		
		// keywords act like functions on maps
		assertEquals(Long.valueOf(100), venice.eval("(:a {:a 100 :b 200})"));
	}
	
	@Test
	public void test_list() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (list))"));
		assertEquals("(1 2 3)", venice.eval("(str (list 1 2 3))"));
		assertEquals("(())", venice.eval("(str (list '()))"));
		assertEquals("([])", venice.eval("(str (list []))"));
		assertEquals("((1 2))", venice.eval("(str (list '(1 2)))"));
		assertEquals("((1 2) 3 4)", venice.eval("(str (list '(1 2) 3 4))"));

		assertEquals("[1, null]", venice.eval("'(1 nil)").toString());
	}
	
	@Test
	public void test_list_eval() {
		final Venice venice = new Venice();

		assertEquals("(1 (+ 1 2))", venice.eval("(pr-str '(1 (+ 1 2)))"));
		assertEquals("(1 3)", venice.eval("(pr-str (list 1 (+ 1 2)))"));
	}
	
	@Test
	public void test_list_ASTERISK() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(list* nil)"));
		assertEquals("(1 2)", venice.eval("(str (list* 1 2 nil))"));
		
		// list
		assertEquals("(1)", venice.eval("(str (list* '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (list* '(1 2)))"));
		assertEquals("(1 2 3)", venice.eval("(str (list* '(1 2 3)))"));
		
		assertEquals("(1 9)", venice.eval("(str (list* 1 '(9)))"));
		assertEquals("(1 2 3 8 9)", venice.eval("(str (list* 1 2 3 '(8 9)))"));
		assertEquals("(1 2 3 7 8 9)", venice.eval("(str (list* 1 2 3 '(7 8 9)))"));
		
		assertEquals("([1 2] 3 7 8 9)", venice.eval("(str (list* [1 2] 3 '(7 8 9)))"));
		
		assertEquals("(nil 1 2)", venice.eval("(str (list* nil '(1 2)))"));
		assertEquals("(nil 1 2 3)", venice.eval("(str (list* nil 1 '(2 3)))"));
		
		// vector
		assertEquals("(1)", venice.eval("(str (list* [1]))"));
		assertEquals("(1 2)", venice.eval("(str (list* [1 2]))"));
		assertEquals("(1 2 3)", venice.eval("(str (list* [1 2 3]))"));
		
		assertEquals("(1 9)", venice.eval("(str (list* 1 [9]))"));
		assertEquals("(1 2 3 8 9)", venice.eval("(str (list* 1 2 3 [8 9]))"));
		assertEquals("(1 2 3 7 8 9)", venice.eval("(str (list* 1 2 3 [7 8 9]))"));
		
		assertEquals("([1 2] 3 7 8 9)", venice.eval("(str (list* [1 2] 3 [7 8 9]))"));
		
		assertEquals("(nil 1 2)", venice.eval("(str (list* nil [1 2]))"));
		assertEquals("(nil 1 2 3)", venice.eval("(str (list* nil 1 [2 3]))"));
	}
	
	@Test
	public void test_stack() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(stack? (stack))"));

		assertEquals(null, venice.eval("(pop! (stack))"));
				
		assertEquals(1L, venice.eval(
							"(let [s (stack)]  \n" +
							"    (push! s 1)   \n" +
							"    (pop! s))     \n"));
		
		assertEquals(3L, venice.eval(
							"(let [s (stack)]  \n" +
							"    (push! s 1)   \n" +
							"    (push! s 2)   \n" +
							"    (push! s 3)   \n" +
							"    (pop! s))     \n"));
		
		assertEquals(1L, venice.eval(
							"(let [s (stack)]  \n" +
							"    (push! s 1)   \n" +
							"    (push! s 2)   \n" +
							"    (push! s 3)   \n" +
							"    (pop! s)      \n" +
							"    (pop! s)      \n" +
							"    (pop! s))     \n"));
		
		assertEquals(null, venice.eval(
							"(let [s (stack)]  \n" +
							"    (push! s 1)   \n" +
							"    (push! s 2)   \n" +
							"    (push! s 3)   \n" +
							"    (pop! s)      \n" +
							"    (pop! s)      \n" +
							"    (pop! s)      \n" +
							"    (pop! s))     \n"));
	}
	
	@Test
	public void test_queue() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(queue? (queue))"));

		assertEquals(null, venice.eval("(poll! (queue))"));

		assertEquals(1L, venice.eval(
							"(let [s (queue 10)]  \n" +
							"    (offer! s 1)     \n" +
							"    (poll! s))       \n"));
		
		assertEquals(1L, venice.eval(
							"(let [s (queue 10)]  \n" +
							"    (offer! s 1)     \n" +
							"    (offer! s 2)     \n" +
							"    (offer! s 3)     \n" +
							"    (poll! s))       \n"));
		
		assertEquals(3L, venice.eval(
							"(let [s (queue 10)]  \n" +
							"    (offer! s 1)     \n" +
							"    (offer! s 2)     \n" +
							"    (offer! s 3)     \n" +
							"    (poll! s)        \n" +
							"    (poll! s)        \n" +
							"    (poll! s))       \n"));
		
		assertEquals(null, venice.eval(
							"(let [s (queue 10)]  \n" +
							"    (offer! s 1)     \n" +
							"    (offer! s 2)     \n" +
							"    (offer! s 3)     \n" +
							"    (poll! s)        \n" +
							"    (poll! s)        \n" +
							"    (poll! s)        \n" +
							"    (poll! s))       \n"));
		
		assertEquals(null, venice.eval(
							"(let [s (queue 10)]      \n" +
							"    (offer! s 1 100)     \n" +
							"    (offer! s 2 100)     \n" +
							"    (offer! s 3 100)     \n" +
							"    (poll! s 100)        \n" +
							"    (poll! s 100)        \n" +
							"    (poll! s 100)        \n" +
							"    (poll! s 100))       \n"));
	}

	@Test
	public void test_queue_offer_return() {
		final Venice venice = new Venice();

		assertEquals(true, venice.eval(
							"(let [s (queue 2)]  \n" +
							"    (offer! s 1))   \n"));

		assertEquals(true, venice.eval(
							"(let [s (queue 2)]  \n" +
							"    (offer! s 1)    \n" +
							"    (offer! s 1))   \n"));

		assertEquals(false, venice.eval(
							"(let [s (queue 2)]  \n" +
							"    (offer! s 1)    \n" +
							"    (offer! s 1)    \n" +
							"    (offer! s 1))   \n"));


		assertEquals(true, venice.eval(
							"(let [s (queue 2)]      \n" +
							"    (offer! s 1 100))   \n"));

		assertEquals(true, venice.eval(
							"(let [s (queue 2)]      \n" +
							"    (offer! s 1 100)    \n" +
							"    (offer! s 1 100))   \n"));

		assertEquals(false, venice.eval(
							"(let [s (queue 2)]      \n" +
							"    (offer! s 1 100)    \n" +
							"    (offer! s 1 100)    \n" +
							"    (offer! s 1 100))   \n"));
	}

	@Test
	public void test_keyword_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(keyword? nil)"));	
		assertFalse((Boolean)venice.eval("(keyword? true)"));	
		assertFalse((Boolean)venice.eval("(keyword? 1)"));	
		assertFalse((Boolean)venice.eval("(keyword? -3.0)"));
		assertFalse((Boolean)venice.eval("(keyword? -3.0M)"));
		assertFalse((Boolean)venice.eval("(keyword? \"ABC\")"));
		assertTrue((Boolean)venice.eval("(keyword? :a)"));
		assertFalse((Boolean)venice.eval("(keyword? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(keyword? '())"));
		assertFalse((Boolean)venice.eval("(keyword? [])"));
		assertFalse((Boolean)venice.eval("(keyword? {})"));
	}
	
	@Test
	public void test_last() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(last \"\")"));
		assertEquals("1", venice.eval("(last \"1\")"));
		assertEquals("2", venice.eval("(last \"12\")"));
		assertEquals("3", venice.eval("(last \"123\")"));

		assertEquals(null, venice.eval("(last '())"));
		assertEquals(Long.valueOf(1), venice.eval("(last '(1))"));
		assertEquals(Long.valueOf(2), venice.eval("(last '(1 2))"));
		assertEquals(Long.valueOf(3), venice.eval("(last '(1 2 3))"));

		assertEquals(null, venice.eval("(last [])"));
		assertEquals(Long.valueOf(1), venice.eval("(last [1])"));
		assertEquals(Long.valueOf(2), venice.eval("(last [1 2])"));
		assertEquals(Long.valueOf(3), venice.eval("(last [1 2 3])"));
	}

	@Test
	public void test_lazy_seq_empty() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (take 5 (lazy-seq)))))";
				
		assertEquals("()",venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_seed() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (take 5 (lazy-seq 0 #(+ % 1))))))";
				
		assertEquals("(0 1 2 3 4)",venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_doall_1() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (take 5 (lazy-seq 0 #(+ % 1))))))";
				
		assertEquals("(0 1 2 3 4)",venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_doall_2() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall 5 (lazy-seq 0 #(+ % 1)))))";
				
		assertEquals("(0 1 2 3 4)",venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_cons() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (take 5 (cons -1 (lazy-seq 0 #(+ % 1))))))";
				
		assertEquals("(-1 0 1 2 3)",venice.eval(script));					
	}
	
	@Test
	public void test_lazy_seq_supplier_finite() {
		final Venice venice = new Venice();

		final String script = "(do                                            \n" +
							  "   (def counter (atom 5))                      \n" +
							  "   (defn generate []                           \n" +
							  "           (swap! counter dec)                 \n" +
							  "           (if (pos? @counter) @counter nil))  \n" +
							  "   (pr-str (doall (lazy-seq generate))))";
				
		assertEquals("(4 3 2 1)",venice.eval(script));					
	}
	
	@Test
	public void test_lazy_seq_list_finite() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (lazy-seq '(1 2 3 4 5))))";
				
		assertEquals("(1 2 3 4 5)",venice.eval(script));					
	}
	
	@Test
	public void test_lazy_seq_vector_finite() {
		final Venice venice = new Venice();

		final String script = "(pr-str (doall (lazy-seq [1 2 3 4 5])))";
				
		assertEquals("(1 2 3 4 5)",venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_recursion_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                               \n" +
				"  (defn positives                                 \n" +
				"    ([]  (positives 1))                           \n" +
				"    ([n] (cons n (fn [] (positives (inc n))))))   \n" +
				"                                                  \n" +
				"  (pr-str (doall (take 4 (positives)))))            ";
				
		assertEquals("(1 2 3 4)", venice.eval(script));					
	}

	@Test
	public void test_lazy_seq_recursion_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                              \n" +
				"  (defn fib                                      \n" +
				"    ([]    (fib 1 1))                            \n" +
				"    ([a b] (cons a (fn [] (fib b (+ a b))))))    \n" +
				"                                                 \n" +
				"  (pr-str (doall (take 6 (fib)))))                 ";
				
		assertEquals("(1 1 2 3 5 8)", venice.eval(script));					
	}
		
	@Test
	public void test_ordered_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(ordered-map? nil)"));	
		assertFalse((Boolean)venice.eval("(ordered-map? 1)"));	
		assertFalse((Boolean)venice.eval("(ordered-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? [1 2])"));	
		assertFalse((Boolean)venice.eval("(ordered-map? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(ordered-map? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(ordered-map? (set 1 2))"));	
	}
	
	@Test
	public void test_ordered_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (ordered-map))"));
		assertEquals("{:a 1}", venice.eval("(str (ordered-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (ordered-map :a 1 :b 2))"));
	}
	
	@Test
	public void test_list_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(list? nil)"));	
		assertTrue((Boolean)venice.eval("(list? '())"));	
		assertFalse((Boolean)venice.eval("(list? [])"));	
		assertFalse((Boolean)venice.eval("(list? 1)"));	
	}


	@Test
	public void test_load_string1() {
		final Venice venice = new Venice();

		final String script =
				"(load-string \"   " +
				"   (def x 1)      " + 
				"   (+ x 2) \")    ";

		assertEquals(Long.valueOf(3), venice.eval(script));
	}

	@Test
	public void test_load_string2() {
		final Venice venice = new Venice();

		final String script =
				"(do                               " +
				"   (load-string \" (def x 1) \")  " + 
				"   (+ x 2))                       ";

		assertEquals(Long.valueOf(3), venice.eval(script));
	}

	@Test
	public void test_long() {
		final Venice venice = new Venice();

		assertEquals(Long.valueOf(0), venice.eval("(long nil)"));
		assertEquals(Long.valueOf(0), venice.eval("(long false)"));
		assertEquals(Long.valueOf(1), venice.eval("(long true)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3)"));
		assertEquals(Long.valueOf(255), venice.eval("(long 0x0FF)"));
		assertEquals(Long.valueOf(255), venice.eval("(long 0X0FF)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3I)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3.1)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3.0M)"));
		assertEquals(Long.valueOf(3), venice.eval("(long 3N)"));
		assertEquals(Long.valueOf(3), venice.eval("(long \"3\")"));
	}

	@Test
	public void test_long_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(long? nil)"));	
		assertFalse((Boolean)venice.eval("(long? true)"));	
		assertTrue((Boolean)venice.eval("(long? 1)"));	
		assertTrue((Boolean)venice.eval("(long? 0x0FF)"));	
		assertTrue((Boolean)venice.eval("(long? 0X0FF)"));	
		assertFalse((Boolean)venice.eval("(long? 3I)"));
		assertFalse((Boolean)venice.eval("(long? -3.0)"));
		assertFalse((Boolean)venice.eval("(long? -3.0M)"));
		assertFalse((Boolean)venice.eval("(long? -3N)"));
		assertFalse((Boolean)venice.eval("(long? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(long? :a)"));
		assertFalse((Boolean)venice.eval("(long? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(long? '())"));
		assertFalse((Boolean)venice.eval("(long? [])"));
		assertFalse((Boolean)venice.eval("(long? {})"));
	}

	@Test
	public void test_lt() {
		final Venice venice = new Venice();

		// Long
		assertTrue((Boolean)venice.eval("(< 2 3)"));
		assertFalse((Boolean)venice.eval("(< 2 2)"));
		assertFalse((Boolean)venice.eval("(< 2 1)"));
		assertTrue((Boolean)venice.eval("(< 2 3.0)"));
		assertFalse((Boolean)venice.eval("(< 2 2.0)"));
		assertFalse((Boolean)venice.eval("(< 2 1.0)"));
		assertTrue((Boolean)venice.eval("(< 2 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2 1.0M)"));
		assertTrue((Boolean)venice.eval("(< 2 3N)"));
		assertFalse((Boolean)venice.eval("(< 2 2N)"));
		assertFalse((Boolean)venice.eval("(< 2 1N)"));

		assertTrue((Boolean)venice.eval("(< 2 3 4)"));
		assertFalse((Boolean)venice.eval("(< 2 1 4)"));
		assertFalse((Boolean)venice.eval("(< 2 6 4)"));
		assertFalse((Boolean)venice.eval("(< 2 3 2)"));

		assertTrue((Boolean)venice.eval("(< 2 3 4 5 6 7 8 9)"));

		// Double
		assertTrue((Boolean)venice.eval("(< 2.0 3.0)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1.0)"));
		assertTrue((Boolean)venice.eval("(< 2.0 3)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1)"));
		assertTrue((Boolean)venice.eval("(< 2.0 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1.0M)"));
		assertTrue((Boolean)venice.eval("(< 2.0 3N)"));
		assertFalse((Boolean)venice.eval("(< 2.0 2N)"));
		assertFalse((Boolean)venice.eval("(< 2.0 1N)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(< 2.0M 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1.0M)"));
		assertTrue((Boolean)venice.eval("(< 2.0M 3))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1))"));
		assertTrue((Boolean)venice.eval("(< 2.0M 3.0))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2.0))"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1.0))"));
		assertTrue((Boolean)venice.eval("(< 2.0M 3N)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 2N)"));
		assertFalse((Boolean)venice.eval("(< 2.0M 1N)"));

		// BigInteger
		assertTrue((Boolean)venice.eval("(< 2N 3N)"));
		assertFalse((Boolean)venice.eval("(< 2N 2N)"));
		assertFalse((Boolean)venice.eval("(< 2N 1N)"));
		assertTrue((Boolean)venice.eval("(< 2N 3))"));
		assertFalse((Boolean)venice.eval("(< 2N 2))"));
		assertFalse((Boolean)venice.eval("(< 2N 1))"));
		assertTrue((Boolean)venice.eval("(< 2N 3.0))"));
		assertFalse((Boolean)venice.eval("(< 2N 2.0))"));
		assertFalse((Boolean)venice.eval("(< 2N 1.0))"));
		assertTrue((Boolean)venice.eval("(< 2N 3.0M)"));
		assertFalse((Boolean)venice.eval("(< 2N 2.0M)"));
		assertFalse((Boolean)venice.eval("(< 2N 1.0M)"));
	}

	@Test
	public void test_lte() {
		final Venice venice = new Venice();

		// Long
		assertTrue((Boolean)venice.eval("(<= 2 3)"));
		assertTrue((Boolean)venice.eval("(<= 2 2)"));
		assertFalse((Boolean)venice.eval("(<= 2 1)"));
		assertTrue((Boolean)venice.eval("(<= 2 3.0)"));
		assertTrue((Boolean)venice.eval("(<= 2 2.0)"));
		assertFalse((Boolean)venice.eval("(<= 2 1.0)"));
		assertTrue((Boolean)venice.eval("(<= 2 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2 1.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2 3N)"));
		assertTrue((Boolean)venice.eval("(<= 2 2N)"));
		assertFalse((Boolean)venice.eval("(<= 2 1N)"));

		assertTrue((Boolean)venice.eval("(<= 2 3 4)"));
		assertFalse((Boolean)venice.eval("(<= 2 1 4)"));
		assertFalse((Boolean)venice.eval("(<= 2 6 4)"));
		assertFalse((Boolean)venice.eval("(<= 2 3 2)"));

		assertTrue((Boolean)venice.eval("(<= 2 3 4 5 6 7 8 9)"));

		// Double
		assertTrue((Boolean)venice.eval("(<= 2.0 3.0)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2.0)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1.0)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 3)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 3N)"));
		assertTrue((Boolean)venice.eval("(<= 2.0 2N)"));
		assertFalse((Boolean)venice.eval("(<= 2.0 1N)"));

		// Decimal
		assertTrue((Boolean)venice.eval("(<= 2.0M 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 3))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2))"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 3.0))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2.0))"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1.0))"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 3N)"));
		assertTrue((Boolean)venice.eval("(<= 2.0M 2N)"));
		assertFalse((Boolean)venice.eval("(<= 2.0M 1N)"));
		
		// BigInteger
		assertTrue((Boolean)venice.eval("(<= 2N 3N)"));
		assertTrue((Boolean)venice.eval("(<= 2N 2N)"));
		assertFalse((Boolean)venice.eval("(<= 2N 1N)"));
		assertTrue((Boolean)venice.eval("(<= 2N 3))"));
		assertTrue((Boolean)venice.eval("(<= 2N 2))"));
		assertFalse((Boolean)venice.eval("(<= 2N 1))"));
		assertTrue((Boolean)venice.eval("(<= 2N 3.0))"));
		assertTrue((Boolean)venice.eval("(<= 2N 2.0))"));
		assertFalse((Boolean)venice.eval("(<= 2N 1.0))"));
		assertTrue((Boolean)venice.eval("(<= 2N 3.0M)"));
		assertTrue((Boolean)venice.eval("(<= 2N 2.0M)"));
		assertFalse((Boolean)venice.eval("(<= 2N 1.0M)"));
	}

	@Test
	public void test_macro_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(do (defmacro _not [x] `(if ~x false true)) (macro? _not))"));
	}

	@Test
	public void test_mapcat() {
		final Venice venice = new Venice();

		// mapcat built with concat and map
		assertEquals(
				"(aa bb cc dd ee ff)", 
				venice.eval("(str (apply concat (map (fn [x] (str/split x \"[0-9]+\")) [\"aa1bb\" \"cc2dd\" \"ee3ff\"])))"));

		
		// mapcat
		assertEquals(
				"(aa bb cc dd ee ff)", 
				venice.eval("(str (mapcat (fn [x] (str/split x \"[0-9]+\")) [\"aa1bb\" \"cc2dd\" \"ee3ff\"]))"));

		assertEquals(
				"(0 1 2 3 4 5 6 7 8 9)",
				venice.eval("(str (mapcat reverse [[3 2 1 0] [6 5 4] [9 8 7]]))"));
	}

	@Test
	public void test_map_invert() {
		final Venice venice = new Venice();

		assertEquals(
				"{1 :a 2 :b 3 :c}", 
				venice.eval("(str (map-invert (sorted-map :a 1 :b 2 :c 3)))"));

		assertEquals(
				"{1 :a 2 :b 3 :d}", 
				venice.eval("(str (map-invert (sorted-map :a 1 :b 2 :c 3 :d 3)))"));
	}

	@Test
	public void test_map_keys() {
		final Venice venice = new Venice();

		assertEquals(
				"{\"a\" 1 \"b\" 2 \"c\" 3}", 
				venice.eval("(pr-str (map-keys name {:a 1 :b 2 :c 3}))"));

		assertEquals(
				"{\"a\" 1 \"b\" 2 \"c\" 3 \"d\" 4}", 
				venice.eval("(pr-str (map-keys name (sorted-map :a 1 :b 2 :c 3 :d 4)))"));
	}

	@Test
	public void test_map_vals() {
		final Venice venice = new Venice();

		assertEquals(
				"{:a 2 :b 3 :c 4}", 
				venice.eval("(pr-str (map-vals inc {:a 1 :b 2 :c 3}))"));
	}

	@Test
	public void test_mapv() {
		final Venice venice = new Venice();

		assertEquals("[2 3 4 5 6]", venice.eval("(str (mapv inc '(1 2 3 4 5)))"));

		assertEquals("[2 3 4 5 6]", venice.eval("(str (mapv inc [1 2 3 4 5]))"));

		assertEquals("[5 7 9]", venice.eval("(str (mapv + [1 2 3] [4 5 6]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3] [4 5 6] [7 8 9]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3 9 9] [4 5 6 9] [7 8 9]))"));

		assertEquals("[12 15 18]", venice.eval("(str (mapv + [1 2 3] [4 5 6 9] [7 8 9 10]))"));

		assertEquals("[1 3]", venice.eval("(str (mapv (fn [x] (get x :a)) [{:a 1 :b 2} {:a 3 :b 4}]))"));
		
		assertEquals("[true false true]", venice.eval("(str (mapv not [false, true, false]))"));
		
		assertEquals("[(1 1) (2 2) (3 3)]", venice.eval("(str (mapv list [1 2 3] [1 2 3]))"));	
		
		assertEquals("[4 5 6]", venice.eval("(str (mapv (fn [x] (get {14 4 15 5 16 6 } x 0.0)) [14 15 16]))"));	
	}	
		
	@Test
	public void test_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(map? nil)"));	
		assertFalse((Boolean)venice.eval("(map? 1)"));	
		assertFalse((Boolean)venice.eval("(map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(map? [1 2])"));	
		assertTrue((Boolean)venice.eval("(map? {:a 1 :b 2})"));	
		assertTrue((Boolean)venice.eval("(map? (hash-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(map? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(map? (set 1 2))"));	
	}

	@Test
	public void test_match_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(match? \"123\" \"[0-9]+\")"));
		assertFalse((Boolean)venice.eval("(match? \"123a\" \"[0-9]+\")"));
	}

	@Test
	public void test_not_match_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(not-match? \"123\" \"[0-9]+\")"));
		assertTrue((Boolean)venice.eval("(not-match? \"123a\" \"[0-9]+\")"));
	}

	@Test
	public void test_memoize() {
		final Venice venice = new Venice();


		final String script1 = 
				"(do                                  " +
				"   (def counter (atom 0))            " +
				"                                     " +
				"   (def test                         " +
				"        (fn [a]                      " +
				"            (do                      " +
				"               (swap! counter inc)   " +
				"               (+ a 100))))          " +
				"                                     " +
				"   (def test-memo (memoize test))    " +
				"                                     " +
				"   [ (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 2)                   " +
				"     (test-memo 2)                   " +
				"     (deref counter) ]               " +
				")                                    ";

		final String script2 = 
				"(do                                  " +
				"   (def counter (atom 0))            " +
				"                                     " +
				"   (def test-memo                    " +
				"        (memoize                     " +
				"           (fn [a]                   " +
				"              (do                    " +
				"                 (swap! counter inc) " +
				"                 (+ a 100)))))       " +
				"                                     " +
				"   [ (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 1)                   " +
				"     (test-memo 2)                   " +
				"     (test-memo 2)                   " +
				"     (deref counter) ]               " +
				")                                    ";

		assertEquals("[101 101 101 102 102 2]", venice.eval("(str " + script1 + ")"));
		assertEquals("[101 101 101 102 102 2]", venice.eval("(str " + script2 + ")"));
	}

	@Test
	public void test_merge() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 9 :c 3 :d 4}", venice.eval("(str (sorted-map (merge {:a 1 :b 2 :c 3} {:b 9 :d 4})))"));
		assertEquals("{:a 1}", venice.eval("(str (merge {:a 1} nil))"));
		assertEquals("{:a 1}", venice.eval("(str (merge nil {:a 1}))"));
		assertEquals(null, venice.eval("(merge nil nil)"));
	}

	@Test
	public void test_merge_with() {
		final Venice venice = new Venice();

		assertEquals(
				"{}", 
				venice.eval("(str (sorted-map (merge-with +)))"));

		assertEquals(
				"{:a 0 :b 10}", 
				venice.eval("(str (sorted-map (merge-with + {:a 0 :b 10})))"));

		assertEquals(
				"{:a 0 :b 10 :c 20 :d 30}", 
				venice.eval("(str (sorted-map (merge-with + {:a 0 :b 2 :c 13} {:b 8 :c 7 :d 30})))"));

		assertEquals(
				"{:a [1] :b [2 3] :c [4]}", 
				venice.eval("(str (merge-with into {:a [1] :b [2]} {:b [3] :c [4]}))"));
		
		assertEquals(
				"{:a #{1 2 3 7 8} :b #{4 5 6} :c #{1 2 3}}", 
				venice.eval("(str (sorted-map (merge-with union                \n" +
							"		            {:a #{1 2 3}    :b #{4 5 6}}   \n" +
							"		            {:a #{2 3 7 8}  :c #{1 2 3}})))  "));
	}

	@Test
	public void test_meta() {
		final Venice venice = new Venice();

		assertEquals(
				"{column=7, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta 3)")).toString());

		assertEquals(
				"{column=8, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta '(1))")).toString());

		assertEquals(
				"{column=8, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta '(1 2))")).toString());

		assertEquals(
				"{column=8, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta '(1 2 3))")).toString());
		
		assertEquals(
				"{column=7, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta [1])")).toString());
		
		assertEquals(
				"{column=7, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta [1 2])")).toString());
		
		assertEquals(
				"{column=7, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta [1 2 3])")).toString());
		
		assertEquals(
				"{column=7, file=unknown, line=1}", 
				new TreeMap<Object,Object>((Map<?,?>)venice.eval("(meta { :a 1 })")).toString());
	
		assertEquals("{:a 1}", venice.eval("(str (meta (with-meta [1 2 3] { :a 1 })))"));
	}

	@Test
	public void test_meta_constants() {
		final Venice venice = new Venice();

		assertEquals("nil", venice.eval("(pr-str (meta nil))"));
		assertEquals("nil", venice.eval("(pr-str (meta true))"));
		assertEquals("nil", venice.eval("(pr-str (meta false))"));
		assertEquals(
				"{:column 34 :file \"unknown\" :line 1}", 
				venice.eval("(pr-str (into (sorted-map) (meta 1)))) "));
	}

	@Test
	public void test_meta_def_sym() {
		final Venice venice = new Venice();

		assertEquals(
				"{}", 
				venice.eval(
					"(do                                           \n" +
					"  (def x)                                     \n" +
					"  (pr-str (into (sorted-map) (meta x)))))       "));

		assertEquals(
				"{:column 8 :file \"unknown\" :line 2 :ns \"user\"}", 
				venice.eval(
					"(do                                           \n" +
					"  (def x 100)                                 \n" +
					"  (pr-str (into (sorted-map) (meta x)))))       "));

		assertEquals(
				"{:column 8 :file \"unknown\" :line 3 :ns \"A\"}", 
				venice.eval(
					"(do                                           \n" +
					"  (ns A)                                      \n" +
					"  (def x 100)                                \n" +
					"  (pr-str (into (sorted-map) (meta x)))))       "));
	}

	@Test
	public void test_meta_def_fn() {
		final Venice venice = new Venice();

		assertEquals(
				"{:column 9 :file \"unknown\" :line 2 :ns \"user\"}", 
				venice.eval(
					"(do                                           \n" +
					"  (defn x [] 100)                             \n" +
					"  (pr-str (into (sorted-map) (meta x)))))       "));

		assertEquals(
				"{:column 9 :file \"unknown\" :line 3 :ns \"A\"}", 
				venice.eval(
					"(do                                           \n" +
					"  (ns A)                                      \n" +
					"  (defn x [] 100)                             \n" +
					"  (pr-str (into (sorted-map) (meta x)))))       "));
	}

	@Test
	public void test_name() {
		final Venice venice = new Venice();

		assertEquals("alpha", venice.eval("(name :alpha)"));	
		assertEquals("alpha", venice.eval("(name 'alpha)"));	
		assertEquals("alpha", venice.eval("(name \"alpha\")"));	
		assertEquals("user/alpha", venice.eval("(do (defn alpha [] 100) (name alpha)))"));	
		assertEquals("user/alpha", venice.eval("(do (let [x (fn alpha [] 100)] (name x)))"));	
	}
	
	@Test
	public void test_newline() {
		final Venice venice = new Venice();

		assertEquals("1\n", venice.eval("(with-out-str (do (print 1) (newline )))"));	
		assertEquals("1 2\n", venice.eval("(with-out-str (do (print 1 2) (newline )))"));	
	}
	
	@Test
	public void test_nfirst() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (nfirst nil 1))"));
		assertEquals("()", venice.eval("(str (nfirst '() 1))"));
		assertEquals("()", venice.eval("(str (nfirst '(1 2 3) 0))"));
		assertEquals("(1)", venice.eval("(str (nfirst '(1 2 3) 1))"));
		assertEquals("(1 2)", venice.eval("(str (nfirst '(1 2 3) 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (nfirst '(1 2 3) 3))"));
		assertEquals("(1 2 3)", venice.eval("(str (nfirst '(1 2 3) 4))"));

		assertEquals("[]", venice.eval("(str (nfirst [] 1))"));
		assertEquals("[]", venice.eval("(str (nfirst [1 2 3] 0))"));
		assertEquals("[1]", venice.eval("(str (nfirst [1 2 3] 1))"));
		assertEquals("[1 2]", venice.eval("(str (nfirst [1 2 3] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (nfirst [1 2 3] 3))"));
		assertEquals("[1 2 3]", venice.eval("(str (nfirst [1 2 3] 4))"));

		assertEquals("",    venice.eval("(nfirst \"abc\" 0)"));
		assertEquals("a",   venice.eval("(nfirst \"abc\" 1)"));
		assertEquals("ab",  venice.eval("(nfirst \"abc\" 2)"));
		assertEquals("abc", venice.eval("(nfirst \"abc\" 3)"));
		assertEquals("abc", venice.eval("(nfirst \"abc\" 4)"));

		// with lazy sequences
		assertEquals("(1 2 3 4)", venice.eval("(str (doall (nfirst (lazy-seq 1 #(+ % 1)) 4)))"));

		// with java lists
		assertEquals(
				"(1 2)", 
				venice.eval(
						"(str (nfirst                                 " +
						"        (doto (. :java.util.ArrayList :new)  " +
						"	           (. :add 1)                     " +
						"	           (. :add 2)                     " +
						"	           (. :add 3)                     " +
						"	           (. :add 4))                    " +
						"	     2))                                  "));
	}

	@Test
	public void test_nil_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(nil? nil)"));	
		assertFalse((Boolean)venice.eval("(nil? true)"));	
		assertFalse((Boolean)venice.eval("(nil? false)"));	
		assertFalse((Boolean)venice.eval("(nil? 1)"));	
	}
	
	@Test
	public void test_nlast() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (nlast nil 1))"));
		assertEquals("()", venice.eval("(str (nlast '() 1))"));
		assertEquals("()", venice.eval("(str (nlast '(1 2 3) 0))"));
		assertEquals("(3)", venice.eval("(str (nlast '(1 2 3) 1))"));
		assertEquals("(2 3)", venice.eval("(str (nlast '(1 2 3) 2))"));
		assertEquals("(1 2 3)", venice.eval("(str (nlast '(1 2 3) 3))"));
		assertEquals("(1 2 3)", venice.eval("(str (nlast '(1 2 3) 4))"));

		assertEquals("[]", venice.eval("(str (nlast [] 1))"));
		assertEquals("[]", venice.eval("(str (nlast [1 2 3] 0))"));
		assertEquals("[3]", venice.eval("(str (nlast [1 2 3] 1))"));
		assertEquals("[2 3]", venice.eval("(str (nlast [1 2 3] 2))"));
		assertEquals("[1 2 3]", venice.eval("(str (nlast [1 2 3] 3))"));
		assertEquals("[1 2 3]", venice.eval("(str (nlast [1 2 3] 4))"));

		assertEquals("",    venice.eval("(nlast \"abc\" 0)"));
		assertEquals("c",   venice.eval("(nlast \"abc\" 1)"));
		assertEquals("bc",  venice.eval("(nlast \"abc\" 2)"));
		assertEquals("abc", venice.eval("(nlast \"abc\" 3)"));
		assertEquals("abc", venice.eval("(nlast \"abc\" 4)"));
		
		// with java lists
		assertEquals(
				"(3 4)", 
				venice.eval(
						"(str (nlast                                  " +
						"        (doto (. :java.util.ArrayList :new)  " +
						"	           (. :add 1)                     " +
						"	           (. :add 2)                     " +
						"	           (. :add 3)                     " +
						"	           (. :add 4))                    " +
						"	     2))                                  "));
	}
	
	@Test
	public void test_not_any_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(not-any? (fn [x] (number? x)) nil)"));	
		assertTrue((Boolean)venice.eval("(not-any? (fn [x] (number? x)) [])"));	
		assertTrue((Boolean)venice.eval("(not-any? (fn [x] (number? x)) [:a])"));	
		assertTrue((Boolean)venice.eval("(not-any? (fn [x] (number? x)) (set :a))"));	
		assertFalse((Boolean)venice.eval("(not-any? (fn [x] (number? x)) [:a 1 2])"));	
		assertFalse((Boolean)venice.eval("(not-any? (fn [x] (number? x)) [1])"));	
		assertFalse((Boolean)venice.eval("(not-any? (fn [x] (number? x)) [1 2])"));	
		assertFalse((Boolean)venice.eval("(not-any? (fn [x] (number? x)) (set :a 1 2))"));	
	}
	
	@Test
	public void test_not_every_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(not-every? (fn [x] (number? x)) nil)"));	
		assertTrue((Boolean)venice.eval("(not-every? (fn [x] (number? x)) [])"));	
		assertTrue((Boolean)venice.eval("(not-every? (fn [x] (number? x)) [:a])"));	
		assertTrue((Boolean)venice.eval("(not-every? (fn [x] (number? x)) [:a 1 2])"));	
		assertTrue((Boolean)venice.eval("(not-every? (fn [x] (number? x)) (set :a 1 2))"));	
		assertFalse((Boolean)venice.eval("(not-every? (fn [x] (number? x)) [1])"));	
		assertFalse((Boolean)venice.eval("(not-every? (fn [x] (number? x)) [1 2])"));	
		assertFalse((Boolean)venice.eval("(not-every? (fn [x] (number? x)) (set 1 2))"));	
}
	
	@Test
	public void test_nth() {
		final Venice venice = new Venice();

		assertEquals("1", venice.eval("(nth \"1\" 0)"));
		assertEquals("2", venice.eval("(nth \"12\" 1 )"));
		assertEquals("3", venice.eval("(nth \"123\" 2)"));

		assertEquals(Long.valueOf(1), venice.eval("(nth '(1) 0)"));
		assertEquals(Long.valueOf(2), venice.eval("(nth '(1 2) 1)"));
		assertEquals(Long.valueOf(3), venice.eval("(nth '(1 2 3) 2)"));
		
		assertEquals(Long.valueOf(1), venice.eval("(nth [1] 0)"));
		assertEquals(Long.valueOf(2), venice.eval("(nth [1 2] 1)"));
		assertEquals(Long.valueOf(3), venice.eval("(nth [1 2 3] 2)"));
	}

	@Test
	public void test_number_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(number? nil)"));	
		assertFalse((Boolean)venice.eval("(number? true)"));	
		assertTrue((Boolean)venice.eval("(number? 1)"));	
		assertTrue((Boolean)venice.eval("(number? -3.0)"));
		assertTrue((Boolean)venice.eval("(number? -3.0M)"));
		assertFalse((Boolean)venice.eval("(number? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(number? :a)"));
		assertFalse((Boolean)venice.eval("(number? '())"));
		assertFalse((Boolean)venice.eval("(number? [])"));
		assertFalse((Boolean)venice.eval("(number? {})"));
	}
	
	@Test
	public void test_partial() {
		final Venice venice = new Venice();
		
		assertEquals(6L, venice.eval("((partial * 2) 3)"));
		
		assertEquals(3L, venice.eval("((partial +) 3)"));
		
		assertEquals("[2, 4, 6, 8]", venice.eval("(map (partial * 2) [1 2 3 4])").toString());
		
		assertEquals("500", venice.eval(
								"(do " +
								"   (def hundred-times (partial * 100)) " +
								"   (str (hundred-times 5)))"));
	}

	@Test
	public void test_partition_padseq_without() {
		final Venice venice = new Venice();
		
		assertEquals("()", venice.eval("(str (partition 1 1 nil))"));
		assertEquals("()", venice.eval("(str (partition 1 1 '()))"));
		assertEquals("((1))", venice.eval("(str (partition 1 1 '(1)))"));
		assertEquals("((1) (2))", venice.eval("(str (partition 1 1 '(1 2)))"));
		assertEquals("((1) (2) (3))", venice.eval("(str (partition 1 1 '(1 2 3)))"));

		assertEquals("()", venice.eval("(str (partition 3 nil))"));
		assertEquals("()", venice.eval("(str (partition 3 '()))"));
		assertEquals("()", venice.eval("(str (partition 3 '(1)))"));
		assertEquals("()", venice.eval("(str (partition 3 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 '(1 2 3 4)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 1 nil))"));
		assertEquals("()", venice.eval("(str (partition 3 1 '()))"));
		assertEquals("()", venice.eval("(str (partition 3 1 '(1)))"));
		assertEquals("()", venice.eval("(str (partition 3 1 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 1 '(1 2 3)))"));
		assertEquals("((1 2 3) (2 3 4))", venice.eval("(str (partition 3 1 '(1 2 3 4)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5))", venice.eval("(str (partition 3 1 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6))", venice.eval("(str (partition 3 1 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6) (5 6 7))", venice.eval("(str (partition 3 1 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 2 nil))"));
		assertEquals("()", venice.eval("(str (partition 3 2 '()))"));
		assertEquals("()", venice.eval("(str (partition 3 2 '(1)))"));
		assertEquals("()", venice.eval("(str (partition 3 2 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 2 '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 2 '(1 2 3 4)))"));
		assertEquals("((1 2 3) (3 4 5))", venice.eval("(str (partition 3 2 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (3 4 5))", venice.eval("(str (partition 3 2 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (3 4 5) (5 6 7))", venice.eval("(str (partition 3 2 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 3 nil))"));
		assertEquals("()", venice.eval("(str (partition 3 3 '()))"));
		assertEquals("()", venice.eval("(str (partition 3 3 '(1)))"));
		assertEquals("()", venice.eval("(str (partition 3 3 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 3 '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 3 '(1 2 3 4)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 3 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 3 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 3 '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 4 nil))"));
		assertEquals("()", venice.eval("(str (partition 3 4 '()))"));
		assertEquals("()", venice.eval("(str (partition 3 4 '(1)))"));
		assertEquals("()", venice.eval("(str (partition 3 4 '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 '(1 2 3 4)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 '(1 2 3 4 5)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6 7 8)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6 7 8 9)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6 7 8 9 10)))"));
		assertEquals("((1 2 3) (5 6 7) (9 10 11))", venice.eval("(str (partition 3 4 '(1 2 3 4 5 6 7 8 9 10 11)))"));
	}
	
	@Test
	public void test_partition_with_padseq_nil() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (partition 3 3 nil nil))"));
		assertEquals("()", venice.eval("(str (partition 3 3 nil '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 3 nil '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 3 nil '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 3 nil '(1 2 3)))"));
		assertEquals("((1 2 3) (4))", venice.eval("(str (partition 3 3 nil '(1 2 3 4)))"));
		assertEquals("((1 2 3) (4 5))", venice.eval("(str (partition 3 3 nil '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 3 nil '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6) (7))", venice.eval("(str (partition 3 3 nil '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 1 nil nil))"));
		assertEquals("()", venice.eval("(str (partition 3 1 nil '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 1 nil '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 1 nil '(1 2)))"));
		assertEquals("((1 2 3) (2 3))", venice.eval("(str (partition 3 1 nil '(1 2 3)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4))", venice.eval("(str (partition 3 1 nil '(1 2 3 4)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5))", venice.eval("(str (partition 3 1 nil '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6) (5 6))", venice.eval("(str (partition 3 1 nil '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6) (5 6 7) (6 7))", venice.eval("(str (partition 3 1 nil '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 2 nil nil))"));
		assertEquals("()", venice.eval("(str (partition 3 2 nil '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 2 nil '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 2 nil '(1 2)))"));
		assertEquals("((1 2 3) (3))", venice.eval("(str (partition 3 2 nil '(1 2 3)))"));
		assertEquals("((1 2 3) (3 4))", venice.eval("(str (partition 3 2 nil '(1 2 3 4)))"));
		assertEquals("((1 2 3) (3 4 5) (5))", venice.eval("(str (partition 3 2 nil '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (3 4 5) (5 6))", venice.eval("(str (partition 3 2 nil '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (3 4 5) (5 6 7) (7))", venice.eval("(str (partition 3 2 nil '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 4 nil nil))"));
		assertEquals("()", venice.eval("(str (partition 3 4 nil '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 4 nil '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 4 nil '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 nil '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 nil '(1 2 3 4)))"));
		assertEquals("((1 2 3) (5))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (5 6))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6 7 8)))"));
		assertEquals("((1 2 3) (5 6 7) (9))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6 7 8 9)))"));
		assertEquals("((1 2 3) (5 6 7) (9 10))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6 7 8 9 10)))"));
		assertEquals("((1 2 3) (5 6 7) (9 10 11))", venice.eval("(str (partition 3 4 nil '(1 2 3 4 5 6 7 8 9 10 11)))"));
	}
	
	@Test
	public void test_partition_with_padseq_empty() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (partition 3 3 [] nil))"));
		assertEquals("()", venice.eval("(str (partition 3 3 [] '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 3 [] '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 3 [] '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 3 [] '(1 2 3)))"));
		assertEquals("((1 2 3) (4))", venice.eval("(str (partition 3 3 [] '(1 2 3 4)))"));
		assertEquals("((1 2 3) (4 5))", venice.eval("(str (partition 3 3 [] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (4 5 6))", venice.eval("(str (partition 3 3 [] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (4 5 6) (7))", venice.eval("(str (partition 3 3 [] '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 1 [] nil))"));
		assertEquals("()", venice.eval("(str (partition 3 1 [] '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 1 [] '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 1 [] '(1 2)))"));
		assertEquals("((1 2 3) (2 3))", venice.eval("(str (partition 3 1 [] '(1 2 3)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4))", venice.eval("(str (partition 3 1 [] '(1 2 3 4)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5))", venice.eval("(str (partition 3 1 [] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6) (5 6))", venice.eval("(str (partition 3 1 [] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (2 3 4) (3 4 5) (4 5 6) (5 6 7) (6 7))", venice.eval("(str (partition 3 1 [] '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 2 [] nil))"));
		assertEquals("()", venice.eval("(str (partition 3 2 [] '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 2 [] '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 2 [] '(1 2)))"));
		assertEquals("((1 2 3) (3))", venice.eval("(str (partition 3 2 [] '(1 2 3)))"));
		assertEquals("((1 2 3) (3 4))", venice.eval("(str (partition 3 2 [] '(1 2 3 4)))"));
		assertEquals("((1 2 3) (3 4 5) (5))", venice.eval("(str (partition 3 2 [] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (3 4 5) (5 6))", venice.eval("(str (partition 3 2 [] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (3 4 5) (5 6 7) (7))", venice.eval("(str (partition 3 2 [] '(1 2 3 4 5 6 7)))"));

		assertEquals("()", venice.eval("(str (partition 3 4 [] nil))"));
		assertEquals("()", venice.eval("(str (partition 3 4 [] '()))"));
		assertEquals("((1))", venice.eval("(str (partition 3 4 [] '(1)))"));
		assertEquals("((1 2))", venice.eval("(str (partition 3 4 [] '(1 2)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 [] '(1 2 3)))"));
		assertEquals("((1 2 3))", venice.eval("(str (partition 3 4 [] '(1 2 3 4)))"));
		assertEquals("((1 2 3) (5))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5)))"));
		assertEquals("((1 2 3) (5 6))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3) (5 6 7))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6 7 8)))"));
		assertEquals("((1 2 3) (5 6 7) (9))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6 7 8 9)))"));
		assertEquals("((1 2 3) (5 6 7) (9 10))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6 7 8 9 10)))"));
		assertEquals("((1 2 3) (5 6 7) (9 10 11))", venice.eval("(str (partition 3 4 [] '(1 2 3 4 5 6 7 8 9 10 11)))"));
	}

	
	@Test
	public void test_partition_with_padseq() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (partition 5 3 '(9) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 '(9) '()))"));
		assertEquals("((1 9))", venice.eval("(str (partition 5 3 '(9) '(1)))"));
		assertEquals("((1 2 9))", venice.eval("(str (partition 5 3 '(9) '(1 2)))"));
		assertEquals("((1 2 3 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9))", venice.eval("(str (partition 5 3 '(9) '(1 2 3 4 5 6 7 8)))"));

		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9) '()))"));
		assertEquals("((1 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1)))"));
		assertEquals("((1 2 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2)))"));
		assertEquals("((1 2 3 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9 9))", venice.eval("(str (partition 5 3 '(9 9) '(1 2 3 4 5 6 7 8)))"));

		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9) '()))"));
		assertEquals("((1 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1)))"));
		assertEquals("((1 2 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2)))"));
		assertEquals("((1 2 3 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9) '(1 2 3 4 5 6 7 8)))"));

		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9 9) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9 9) '()))"));
		assertEquals("((1 9 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1)))"));
		assertEquals("((1 2 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2)))"));
		assertEquals("((1 2 3 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9) '(1 2 3 4 5 6 7 8)))"));

		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9 9 9) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '()))"));
		assertEquals("((1 9 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1)))"));
		assertEquals("((1 2 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2)))"));
		assertEquals("((1 2 3 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9 9 9))", venice.eval("(str (partition 5 3 '(9 9 9 9 9) '(1 2 3 4 5 6 7 8)))"));
	}

	@Test
	public void test_partition_with_padding_lazy_seq() {
		final Venice venice = new Venice();
		
		// padding with lazy seq
		assertEquals("()", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) nil))"));
		assertEquals("()", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '()))"));
		assertEquals("((1 9 9 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1)))"));
		assertEquals("((1 2 9 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2)))"));
		assertEquals("((1 2 3 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3)))"));
		assertEquals("((1 2 3 4 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3 4)))"));
		assertEquals("((1 2 3 4 5) (4 5 9 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3 4 5)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3 4 5 6)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3 4 5 6 7)))"));
		assertEquals("((1 2 3 4 5) (4 5 6 7 8) (7 8 9 9 9))", venice.eval("(str (partition 5 3 (lazy-seq (constantly 9)) '(1 2 3 4 5 6 7 8)))"));
	}

	@Test
	public void test_partition_finite_lazy_seq() {
		final Venice venice = new Venice();
				
		// partition a finite lazy sequence
		assertEquals("((1 2) (3 4))", venice.eval("(str (partition 2 (lazy-seq [1 2 3 4 5])))"));
		
		assertEquals("((1 2) (3 4) (5))", venice.eval("(str (partition 2 2 [] (lazy-seq [1 2 3 4 5])))"));
	}

	@Test
	public void test_partition_by() {
		final Venice venice = new Venice();
		
		assertEquals("((1) (2 4) (3 5) (6))", venice.eval("(str (partition-by even? [1 2 4 3 5 6]))"));
		assertEquals("((A) (B B) (A))", venice.eval("(str (partition-by identity (seq \"ABBA\")))"));
		assertEquals("((1 1 1 1) (2 2) (3))", venice.eval("(str (partition-by identity [1 1 1 1 2 2 3]))"));
	}
	
	@Test
	public void test_peek() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(peek nil)"));
		
		assertEquals(null, venice.eval("(peek '())"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1))"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1 2))"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek '(1 2 3))"));

		assertEquals(null, venice.eval("(peek [])"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek [1])"));
		assertEquals(Long.valueOf(2L), venice.eval("(peek [1 2])"));
		assertEquals(Long.valueOf(3L), venice.eval("(peek [1 2 3])"));

		assertEquals(null, venice.eval("(peek (stack))"));
		assertEquals(Long.valueOf(1L), venice.eval("(peek (push! (stack) 1))"));
	}
	
	@Test
	public void test_pop() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (pop '()))"));
		assertEquals("()", venice.eval("(str (pop '(1)))"));
		assertEquals("(2)", venice.eval("(str (pop '(1 2)))"));
		assertEquals("(2 3)", venice.eval("(str (pop '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (pop []))"));
		assertEquals("[]", venice.eval("(str (pop [1]))"));
		assertEquals("[1]", venice.eval("(str (pop [1 2]))"));
		assertEquals("[1 2]", venice.eval("(str (pop [1 2 3]))"));
	}
	
	@Test
	public void test_pop_BANG() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(pop! (stack))"));
		assertEquals(Long.valueOf(1L), venice.eval("(pop! (push! (stack) 1))"));
	}

	@Test
	public void test_pos() {
		final Venice venice = new Venice();

		// Long
		assertFalse((Boolean)venice.eval("(pos? -3)"));
		assertFalse((Boolean)venice.eval("(pos? 0)"));
		assertTrue((Boolean)venice.eval("(pos? 3)"));
	
		// Double
		assertFalse((Boolean)venice.eval("(pos? -3.0)"));
		assertFalse((Boolean)venice.eval("(pos? 0.0)"));
		assertTrue((Boolean)venice.eval("(pos? 3.0)"));

		// Decimal
		assertFalse((Boolean)venice.eval("(pos? -3.0M)"));
		assertFalse((Boolean)venice.eval("(pos? 0.0M)"));
		assertTrue((Boolean)venice.eval("(pos? 3.0M)"));
	}
	
	@Test
	public void test_pr_str() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(pr-str )"));	
		assertEquals("nil", venice.eval("(pr-str nil)"));	
		assertEquals("\"abc\"", venice.eval("(pr-str \"abc\")"));
		assertEquals("\"abc\" 1 2 \"def\" nil true", venice.eval("(pr-str \"abc\" 1 2 \"def\" nil true)"));
		assertEquals("true", venice.eval("(pr-str true)"));	
		assertEquals("1", venice.eval("(pr-str 1)"));	
		assertEquals("-3.0", venice.eval("(pr-str -3.0)"));
		assertEquals("-3.0123M", venice.eval("(pr-str -3.0123M)"));
		assertEquals(":a", venice.eval("(pr-str :a)"));
		assertEquals("a", venice.eval("(pr-str (symbol :a))"));
		assertEquals("(1 2)", venice.eval("(pr-str '(1 2))"));
		assertEquals("[1 2]", venice.eval("(pr-str [1 2])"));
		assertEquals("{:a 1 :b 2}", venice.eval("(pr-str (ordered-map :a 1 :b 2))"));
		assertEquals("{:a \"1\" :b \"2\"}", venice.eval("(pr-str (ordered-map :a \"1\" :b \"2\"))"));
	}
	
	@Test
	public void test_print() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(with-out-str (print ))"));	
		assertEquals("1", venice.eval("(with-out-str (print 1))"));	
		assertEquals("1 2", venice.eval("(with-out-str (print 1 2))"));	
		assertEquals("1 2 3", venice.eval("(with-out-str (print 1 2 3))"));	
	}
	
	@Test
	public void test_println() {
		final Venice venice = new Venice();

		assertEquals("\n", venice.eval("(with-out-str (println ))"));	
		assertEquals("1\n", venice.eval("(with-out-str (println 1))"));	
		assertEquals("1 2\n", venice.eval("(with-out-str (println 1 2))"));	
		assertEquals("1 2 3\n", venice.eval("(with-out-str (println 1 2 3))"));	
	}
	
	@Test
	public void test_printf() {
		final Venice venice = new Venice();

		assertEquals("abc: 100", venice.eval("(with-out-str (printf \"%s: %d\" \"abc\" 100))"));	
	}
	
	@Test
	public void test_push_BANG() {
		final Venice venice = new Venice();

		assertEquals(null, venice.eval("(peek (stack))"));
		assertEquals(2L, venice.eval("(count (push! (push! (stack) 1) 2))"));
	}
	
	@Test
	public void test_reduce() {
		final Venice venice = new Venice();

		// ((100 + 3) * 2) + 1 => 207
		assertEquals(Long.valueOf(207), venice.eval("((reduce comp [(partial + 1) (partial * 2) (partial + 3)]) 100)"));
		
		assertEquals(Long.valueOf(15), venice.eval("(reduce + [1 2 3 4 5])"));
		assertEquals(Long.valueOf(0),  venice.eval("(reduce + [])"));
		assertEquals(Long.valueOf(1),  venice.eval("(reduce + [1])"));
		assertEquals(Long.valueOf(3),  venice.eval("(reduce + [1 2])"));
		assertEquals(Long.valueOf(3),  venice.eval("(reduce + 3 [])"));
		assertEquals(Long.valueOf(9),  venice.eval("(reduce + 4 [2 3])"));
	}

	@Test
	public void test_reduce_kv() {
		final Venice venice = new Venice();

		assertEquals(
				"{:a 1 :b 2 :c 3}", 
				venice.eval(
						"(str (reduce-kv (fn [m k v] (assoc m k v)) \n" +
						"                (sorted-map)               \n" +
						"                {:a 1 :b 2 :c 3}))"));
		
		assertEquals(
				"{:a 2 :b 3 :c 4}",
				venice.eval(
						"(str (reduce-kv (fn [m k v] (assoc m k (inc v))) \n" +
						"                (sorted-map)                     \n" +
						"                {:a 1 :b 2 :c 3}))"));
		 
		assertEquals(
				6L, 
				venice.eval(
						"(reduce-kv (fn [x k v] (+ x v)) 0 {:a 1 :b 2 :c 3})"));
		
		assertEquals(
				"{:c 10 :e 20 :b 30 :d 40 :a 50}", 
				venice.eval(
						"(str (reduce-kv (fn [m k v] (assoc m k (:len v)))   \n" + 
						"           (ordered-map)                            \n" + 
						"           (ordered-map :c {:col :red   :len 10}    \n" + 
						"                        :e {:col :white :len 20}    \n" + 
						"                        :b {:col :green :len 30}    \n" + 
						"                        :d {:col :pink  :len 40}    \n" + 
						"                        :a {:col :blue  :len 50} )))  "));
		
		assertEquals(
				"{:a 50 :b 30 :c 10 :d 40 :e 20}", 
				venice.eval(
						"(str (reduce-kv (fn [m k v] (assoc m k (:len v)))   \n" + 
						"           (sorted-map)                             \n" + 
						"           (ordered-map :c {:col :red   :len 10}    \n" + 
						"                        :e {:col :white :len 20}    \n" + 
						"                        :b {:col :green :len 30}    \n" + 
						"                        :d {:col :pink  :len 40}    \n" + 
						"                        :a {:col :blue  :len 50} )))  "));
	}

	@Test
	public void test_replace() {
		final Venice venice = new Venice();

		// list
		assertEquals("()", venice.eval("(str (replace {} '()))"));
		assertEquals("(10 20)", venice.eval("(str (replace {} '(10 20)))"));
		assertEquals("(11 20)", venice.eval("(str (replace {10 11} '(10 20)))"));
		assertEquals("(11 21 11)", venice.eval("(str (replace {10 11 20 21} '(10 20 10)))"));

		// vector
		assertEquals("[]", venice.eval("(str (replace {} []))"));
		assertEquals("[10 20]", venice.eval("(str (replace {} [10 20]))"));
		assertEquals("[11 20]", venice.eval("(str (replace {10 11} [10 20]))"));
		assertEquals("[11 21 11]", venice.eval("(str (replace {10 11 20 21} [10 20 10]))"));
		assertEquals("[10 5 10]", venice.eval("(str (replace {:a 5} [10 :a 10]))"));
		assertEquals("[10 5 10]", venice.eval("(str (replace {'a 5} [10 'a 10]))"));

		// set
		assertEquals("#{}", venice.eval("(str (replace {} #{}))"));
		assertEquals("#{10}", venice.eval("(str (replace {} #{10}))"));
		assertEquals("#{11}", venice.eval("(str (replace {10 11} #{10}))"));

		// map
		assertEquals("{}", venice.eval("(str (replace {} {}))"));
		assertEquals("{:a 10}", venice.eval("(str (replace {} {:a 10}))"));
		assertEquals("{:A 11}", venice.eval("(str (replace {[:a 10] [:A 11]} {:a 10}))"));
	}

	@Test
	public void test_repeat() {
		final Venice venice = new Venice();

		// Long
		assertEquals("()", venice.eval("(str (repeat 0 1))"));
		assertEquals("(1 1 1 1)", venice.eval("(str (repeat 4 1))"));

		// Long (lazy seq)
		assertEquals("(1 1 1 1)", venice.eval("(str (doall (take 4 (repeat 1))))"));

		// Double
		assertEquals("()", venice.eval("(str (repeat 0 1.0))"));
		assertEquals("(1.0 1.0 1.0 1.0)", venice.eval("(str (repeat 4 1.0))"));
		
		// Decimal
		assertEquals("()", venice.eval("(str (repeat 0 1.0M))"));
		assertEquals("(1.0M 1.0M 1.0M 1.0M)", venice.eval("(str (repeat 4 1.0M))"));

		// Vector
		assertEquals("()", venice.eval("(str (repeat 0 [5]))"));
		assertEquals("([4 5] [4 5] [4 5] [4 5])", venice.eval("(str (repeat 4 [4 5]))"));

		// String
		assertEquals("(a a a a)", venice.eval("(str (repeat 4 \"a\"))"));
	}
	
	@Test
	public void test_rest() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str (rest nil))"));
		assertEquals("()", venice.eval("(str (rest '()))"));
		assertEquals("()", venice.eval("(str (rest '(1)))"));
		assertEquals("(2)", venice.eval("(str (rest '(1 2)))"));
		assertEquals("(2 3)", venice.eval("(str (rest '(1 2 3)))"));

		assertEquals("[]", venice.eval("(str (rest []))"));
		assertEquals("[]", venice.eval("(str (rest [1]))"));
		assertEquals("[2]", venice.eval("(str (rest [1 2]))"));
		assertEquals("[2 3]", venice.eval("(str (rest [1 2 3]))"));
	}
	
	@Test
	public void test_second() {
		final Venice venice = new Venice();

		assertNull(venice.eval("(second \"\")"));
		assertNull(venice.eval("(second \"1\")"));
		assertEquals("2", venice.eval("(second \"12\")"));
		assertEquals("2", venice.eval("(second \"123\")"));

		assertNull(venice.eval("(second '())"));
		assertNull(venice.eval("(second '(1))"));
		assertEquals(Long.valueOf(2), venice.eval("(second '(1 2))"));
		
		assertNull(venice.eval("(second [])"));
		assertNull(venice.eval("(second [1])"));
		assertEquals(Long.valueOf(2), venice.eval("(second [1 2])"));
	}

	@Test
	public void test_seq() {
		final Venice venice = new Venice();

		assertEquals("(1)", venice.eval("(str (seq '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (seq '(1 2)))"));
		assertEquals("(1 2)", venice.eval("(str (seq [1 2]))"));
		assertEquals("(a b c)", venice.eval("(str (seq \"abc\"))"));
		assertEquals("([:a 1] [:b 2])", venice.eval("(str (seq {:a 1 :b 2}))"));	

		// Corner cases
		assertEquals(null, venice.eval("(seq nil)"));
		assertEquals(null, venice.eval("(seq \"\")"));
		assertEquals(null, venice.eval("(seq '())"));
		assertEquals(null, venice.eval("(seq [])"));
		assertEquals(null, venice.eval("(seq {})"));
	}

	@Test
	public void test_seq_java_stream() {
		final Venice venice = new Venice();

		final List<String> strings = Arrays.asList("1", "2", "3");	
		assertEquals(
			"(1 2 3)", 
			venice.eval("(str (seq s))", Parameters.of("s", strings.stream())));
	}
	
	@Test
	public void test_sequential_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(sequential? nil)"));	
		assertFalse((Boolean)venice.eval("(sequential? 1)"));	
		assertTrue((Boolean)venice.eval("(sequential? '(1 2))"));	
		assertTrue((Boolean)venice.eval("(sequential? [1 2])"));	
		assertFalse((Boolean)venice.eval("(sequential? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(sequential? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sequential? (set 1 2))"));	
	}
	
	@Test
	public void test_set() {
		final Venice venice = new Venice();

		assertEquals("#{}", venice.eval("(str (set ))"));
		assertEquals("#{1 2 3}", venice.eval("(str (set 1 2 3))"));
		assertEquals("#{[1 2]}", venice.eval("(str (set [1 2]))"));
		assertEquals("#{0 1 2 [1 2] [2 3]}", venice.eval("(str (sorted-set 0 [2 3] [1 2] 1 2))"));
		assertEquals("#{0 [1]}", venice.eval("(str (set 0 [1]))"));
		assertEquals("2", venice.eval("(str (count (set 0 [1 2])))"));

		assertEquals("#{}", venice.eval("(str #{})"));
		assertEquals("#{1 2 3}", venice.eval("(str #{1 2 3})"));
		assertEquals("#{[1 2]}", venice.eval("(str #{[1 2]})"));
		assertEquals("#{0 [1]}", venice.eval("(str #{0 [1]})"));
		assertEquals("2", venice.eval("(str (count #{0 [1 2]}))"));
	}
	
	@Test
	public void test_set_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(set? nil)"));	
		assertFalse((Boolean)venice.eval("(set? 1)"));	
		assertFalse((Boolean)venice.eval("(set? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(set? [1 2])"));	
		assertFalse((Boolean)venice.eval("(set? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(set? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(set? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(set? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(set? (sorted-set 1 2))"));	
		assertTrue((Boolean)venice.eval("(set? (set 1 2))"));	
	}
	
	@Test
	public void test_sorted_set() {
		final Venice venice = new Venice();

		assertEquals("#{}", venice.eval("(str (sorted-set ))"));
		assertEquals("#{2 4 6}", venice.eval("(str (sorted-set 6 2 4))"));
		assertEquals("#{0 1 2 3 [1 2] [2 3]}", venice.eval("(str (sorted-set [2 3] 3 2 [1 2] 1 0))"));
		assertEquals("3", venice.eval("(str (count (sorted-set 0 1 2)))"));
	}
	
	@Test
	public void test_sorted_set_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(sorted-set? nil)"));	
		assertFalse((Boolean)venice.eval("(sorted-set? 1)"));	
		assertFalse((Boolean)venice.eval("(sorted-set? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-set? [1 2])"));	
		assertFalse((Boolean)venice.eval("(sorted-set? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(sorted-set? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-set? (ordered-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-set? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-set? (set 1 2))"));	
		assertTrue((Boolean)venice.eval("(sorted-set? (sorted-set 1 2))"));	
	}

	@Test
	public void test_some() {
		final Venice venice = new Venice();

		assertEquals(Boolean.TRUE, venice.eval("(some even? '(1 2 3 4))"));	
		assertEquals(null,         venice.eval("(some even? '(1 3 5 7))"));	
		assertEquals(Boolean.TRUE, venice.eval("(some true? [false true false])"));	
		assertEquals(Boolean.TRUE, venice.eval("(some #(== 5 %) [1 2 3 4 5])"));	
		assertEquals(null,         venice.eval("(some #(== 5 %) [1 2 3 4 6])"));	
		assertEquals(2L,           venice.eval("(some #(if (even? %) %) [1 2 3 4])"));			
	}

	@Test
	public void test_some_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(some? nil)"));	
		assertTrue((Boolean)venice.eval("(some? true)"));	
		assertTrue((Boolean)venice.eval("(some? false)"));	
		assertTrue((Boolean)venice.eval("(some? 1)"));	
	}
	
	@Test
	public void test_sorted_map_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(sorted-map? nil)"));	
		assertFalse((Boolean)venice.eval("(sorted-map? 1)"));	
		assertFalse((Boolean)venice.eval("(sorted-map? '(1 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? [1 2])"));	
		assertFalse((Boolean)venice.eval("(sorted-map? {:a 1 :b 2})"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (hash-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (ordered-map :a 1 :b 2))"));	
		assertTrue((Boolean)venice.eval("(sorted-map? (sorted-map :a 1 :b 2))"));	
		assertFalse((Boolean)venice.eval("(sorted-map? (set 1 2))"));	
	}
	
	@Test
	public void test_sort() {
		final Venice venice = new Venice();

		// list
		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(1)", venice.eval("(str (sort '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort '(3 2 1)))"));

		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(1.0)", venice.eval("(str (sort '(1.0)))"));
		assertEquals("(1.0 2.0)", venice.eval("(str (sort '(2.0 1.0)))"));
		assertEquals("(1.0 2.0 3.0)", venice.eval("(str (sort '(3.0 2.0 1.0)))"));

		assertEquals("()", venice.eval("(str (sort '()))"));
		assertEquals("(a)", venice.eval("(str (sort '(\"a\")))"));
		assertEquals("(a b)", venice.eval("(str (sort '(\"b\" \"a\")))"));
		assertEquals("(a b c)", venice.eval("(str (sort '(\"c\" \"b\" \"a\")))"));

		assertEquals("(1)", venice.eval("(str (sort '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort '(3 2 1)))"));

		assertEquals("((1 1) (1 2) (1 3) (2 1) (2 2))", venice.eval("(str (sort '((1 2) (1 1) (2 1) (1 3) (2 2))))"));

		assertEquals("(1 2 3)", venice.eval("(str (sort compare '(2 3 1)))"));
		assertEquals("(3 2 1)", venice.eval("(str (sort (comp (partial * -1) compare) '(2 3 1)))"));

		// vector
		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[1]", venice.eval("(str (sort [1]))"));
		assertEquals("[1 2]", venice.eval("(str (sort [2 1]))"));
		assertEquals("[1 2 3]", venice.eval("(str (sort [3 2 1]))"));

		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[1.0]", venice.eval("(str (sort [1.0]))"));
		assertEquals("[1.0 2.0]", venice.eval("(str (sort [2.0 1.0]))"));
		assertEquals("[1.0 2.0 3.0]", venice.eval("(str (sort [3.0 2.0 1.0]))"));

		assertEquals("[]", venice.eval("(str (sort []))"));
		assertEquals("[a]", venice.eval("(str (sort [\"a\"]))"));
		assertEquals("[a b]", venice.eval("(str (sort [\"b\" \"a\"]))"));
		assertEquals("[a b c]", venice.eval("(str (sort [\"c\" \"b\" \"a\"]))"));
	
		
		// set
		assertEquals("()", venice.eval("(str (sort (set )))"));
		assertEquals("(1)", venice.eval("(str (sort (set 1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort (set 2 1)))"));
		assertEquals("(1 2 3 4 5)", venice.eval("(str (sort (set 5 4 3 2 1)))"));
	
		
		// map
		assertEquals("()", venice.eval("(str (sort {}))"));
		assertEquals("([:a 1])", venice.eval("(str (sort {:a 1}))"));
		assertEquals("([:a 1] [:b 2])", venice.eval("(str (sort {:b 2 :a 1}))"));
		assertEquals("([:a 1] [:b 2] [:c 3])", venice.eval("(str (sort {:c 3 :b 2 :a 1}))"));
	}

	@Test
	public void test_sort_compare_fn() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("(1)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(1)))"));
		assertEquals("(1 2)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(2 1)))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(3 2 1)))"));

		// sort reverse
		assertEquals("()", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '()))"));
		assertEquals("(1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(1)))"));
		assertEquals("(2 1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(2 1)))"));
		assertEquals("(3 2 1)", venice.eval("(str (sort (fn [x y] (* (if (< x y) -1 1) -1)) '(2 3 1)))"));

		assertEquals("()", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("(1.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(1.0)))"));
		assertEquals("(1.0 2.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(2.0 1.0)))"));
		assertEquals("(1.0 2.0 3.0)", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) '(3.0 2.0 1.0)))"));

		
		assertEquals("[]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) []))"));
		assertEquals("[1]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [1]))"));
		assertEquals("[1 2]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [2 1]))"));
		assertEquals("[1 2 3]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [3 2 1]))"));

		assertEquals("[]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) []))"));
		assertEquals("[1.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [1.0]))"));
		assertEquals("[1.0 2.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [2.0 1.0]))"));
		assertEquals("[1.0 2.0 3.0]", venice.eval("(str (sort (fn [x y] (if (< x y) -1 1)) [3.0 2.0 1.0]))"));
	}

	@Test
	public void test_sort_by() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort-by last '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last '((:a 1))))"));
		assertEquals("((:b 1) (:a 2))", venice.eval("(str (sort-by last '((:a 2) (:b 1))))"));
		assertEquals("((:c 1) (:b 2) (:a 3))", venice.eval("(str (sort-by last '((:a 3) (:b 2) (:c 1))))"));

		assertEquals("()", venice.eval("(str (sort-by first '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by first '((:a 1))))"));
		assertEquals("((:a 2) (:b 1))", venice.eval("(str (sort-by first '((:a 2) (:b 1))))"));
		assertEquals("((:a 3) (:b 2) (:c 1))", venice.eval("(str (sort-by first '((:b 2) (:a 3) (:c 1))))"));
	}

	@Test
	public void test_sort_by_compare_fn() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 1))))"));
		assertEquals("((:b 1) (:a 2))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 2) (:b 1))))"));
		assertEquals("((:c 1) (:b 2) (:a 3))", venice.eval("(str (sort-by last (fn [x y] (if (< x y) -1 1)) '((:a 3) (:b 2) (:c 1))))"));

		// sort reverse
		assertEquals("()", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '()))"));
		assertEquals("((:a 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 1))))"));
		assertEquals("((:a 2) (:b 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 2) (:b 1))))"));
		assertEquals("((:a 3) (:b 2) (:c 1))", venice.eval("(str (sort-by last (fn [x y] (* (if (< x y) -1 1) -1)) '((:a 3) (:b 2) (:c 1))))"));
	}
	
	@Test
	public void test_sorted_map() {
		final Venice venice = new Venice();

		assertEquals("{}", venice.eval("(str (sorted-map))"));
		assertEquals("{:a 1}", venice.eval("(str (sorted-map :a 1))"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (sorted-map :b 2 :a 1))"));
		assertEquals("{:a 1 :b [2 3] :c #{3 4}}", venice.eval("(str (sorted-map :a 1 :c #{3 4}  :b [2 3]))"));
	}
	
	@Test
	public void test_string_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(string? nil)"));	
		assertFalse((Boolean)venice.eval("(string? true)"));	
		assertFalse((Boolean)venice.eval("(string? 1)"));	
		assertFalse((Boolean)venice.eval("(string? -3.0)"));
		assertFalse((Boolean)venice.eval("(string? -3.0M)"));
		assertFalse((Boolean)venice.eval("(string? -3N)"));
		assertTrue((Boolean)venice.eval("(string? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(string? :a)"));
		assertFalse((Boolean)venice.eval("(string? (symbol :a))"));
		assertFalse((Boolean)venice.eval("(string? '())"));
		assertFalse((Boolean)venice.eval("(string? [])"));
		assertFalse((Boolean)venice.eval("(string? {})"));
	}
	
	@Test
	public void test_split_at() {
		final Venice venice = new Venice();

		assertEquals("[() (1 2 3 4 5)]", venice.eval("(str (split-at 0 [1 2 3 4 5]))"));	
		assertEquals("[(1) (2 3 4 5)]", venice.eval("(str (split-at 1 [1 2 3 4 5]))"));	
		assertEquals("[(1 2) (3 4 5)]", venice.eval("(str (split-at 2 [1 2 3 4 5]))"));	
		assertEquals("[(1 2 3) (4 5)]", venice.eval("(str (split-at 3 [1 2 3 4 5]))"));	
		assertEquals("[(1 2 3 4) (5)]", venice.eval("(str (split-at 4 [1 2 3 4 5]))"));	
		assertEquals("[(1 2 3 4 5) ()]", venice.eval("(str (split-at 5 [1 2 3 4 5]))"));	
		assertEquals("[(1 2 3 4 5) ()]", venice.eval("(str (split-at 6 [1 2 3 4 5]))"));	
	}
	
	@Test
	public void test_split_with() {
		final Venice venice = new Venice();

		assertEquals("[(1 3 5) (6 7 9)]", venice.eval("(str (split-with odd? [1 3 5 6 7 9]))"));	
		assertEquals("[(1 3 5) ()]", venice.eval("(str (split-with odd? [1 3 5]))"));	
		assertEquals("[() (2 4 6)]", venice.eval("(str (split-with odd? [2 4 6]))"));	
	}
	
	@Test
	public void test_str() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str )"));	
		assertEquals("", venice.eval("(str nil)"));	
		assertEquals("abc", venice.eval("(str \"abc\")"));
		assertEquals("abcdef", venice.eval("(str \"abc\" \"def\")"));
		assertEquals("true", venice.eval("(str true)"));	
		assertEquals("1", venice.eval("(str 1)"));	
		assertEquals("-3.0", venice.eval("(str -3.0)"));
		assertEquals("-3.0123M", venice.eval("(str -3.0123M)"));
		assertEquals("-3N", venice.eval("(str -3N)"));
		assertEquals(":a", venice.eval("(str :a)"));
		assertEquals("a", venice.eval("(str (symbol :a))"));
		assertEquals("()", venice.eval("(str '())"));
		assertEquals("(1)", venice.eval("(str '(1))"));
		assertEquals("(1 2)", venice.eval("(str '(1 2))"));
		assertEquals("[]", venice.eval("(str [])"));
		assertEquals("[1]", venice.eval("(str [1])"));
		assertEquals("[1 2]", venice.eval("(str [1 2])"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (ordered-map :a 1 :b 2 ))"));
	}

	@Test
	public void test_subtract() {
		final Venice venice = new Venice();

		// Long
		assertEquals(Long.valueOf(-3), venice.eval("(- 3)"));
		assertEquals(Long.valueOf(3), venice.eval("(- 4 1)"));
		assertEquals(Long.valueOf(1), venice.eval("(- 4 1 1 1)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(- 4 1.0)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(- 4 1.0M)"));

		// Double
		assertEquals(Double.valueOf(-4.0D), venice.eval("(- 4.0)"));
		assertEquals(Double.valueOf(3.0D), venice.eval("(- 4.0 1.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(- 4.0 1.0 1.0)"));
		assertEquals(Double.valueOf(2.0D), venice.eval("(- 4.0 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0 2.0M)"));

		// Decimal
		assertEquals(new BigDecimal("-4.0"), venice.eval("(- 4.0M)"));
		assertEquals(new BigDecimal("3.0"), venice.eval("(- 4.0M 1.0M)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 1.0M 1.0M)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 2)"));
		assertEquals(new BigDecimal("2.0"), venice.eval("(- 4.0M 2.0)"));
	}

	@Test
	public void test_subvec() {
		final Venice venice = new Venice();

		assertEquals("[3 4 5]", venice.eval("(str (subvec [0 1 2 3 4 5] 3))"));		
		assertEquals("[0 1 2]", venice.eval("(str (subvec [0 1 2 3 4 5] 0 3))"));		
		assertEquals("[2 3 4]", venice.eval("(str (subvec [0 1 2 3 4 5] 2 5))"));		
	}

	@Test
	public void test_symbol() {
		final Venice venice = new Venice();
		
		assertEquals("a", venice.eval("(str (symbol \"a\"))"));
		assertEquals("a", venice.eval("(str (symbol :a))"));
		assertEquals("a", venice.eval("(str (symbol (symbol \"a\")))"));
		assertEquals("foo/a", venice.eval("(str (symbol \"foo\" \"a\"))"));
		assertEquals("user/a", venice.eval("(str (symbol *ns* \"a\"))"));
		assertTrue((Boolean)venice.eval("(symbol? (symbol \"a\"))"));	
		assertTrue((Boolean)venice.eval("(symbol? (symbol :a))"));	
	}

	@Test
	public void test_symbol_Q() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(symbol? (symbol :a))"));
		assertTrue((Boolean)venice.eval("(symbol? (symbol 'a))"));
		assertTrue((Boolean)venice.eval("(symbol? 'a)"));
		assertTrue((Boolean)venice.eval("(symbol? '<>)"));

		assertFalse((Boolean)venice.eval("(symbol? nil)"));	
		assertFalse((Boolean)venice.eval("(symbol? true)"));	
		assertFalse((Boolean)venice.eval("(symbol? 1)"));	
		assertFalse((Boolean)venice.eval("(symbol? -3.0)"));
		assertFalse((Boolean)venice.eval("(symbol? -3.0M)"));
		assertFalse((Boolean)venice.eval("(symbol? \"ABC\")"));
		assertFalse((Boolean)venice.eval("(symbol? :a)"));
		assertFalse((Boolean)venice.eval("(symbol? '())"));
		assertFalse((Boolean)venice.eval("(symbol? [])"));
		assertFalse((Boolean)venice.eval("(symbol? {})"));
	}

	@Test
	public void test_trampoline() {
		final Venice venice = new Venice();
		
		final String script = 
				"(defn incrementor2 [n]                  \n" +
				"  (let [_ (print (str n \", \"))        \n" +
				"        next-val (+ n (rand-long 100))] \n" +
				"    (if (< -10000 next-val)             \n" +
				"      #(decrementor2 next-val))))       \n" +
				"                                        \n" +
				"(defn decrementor2 [n]                  \n" +
				"  (let [_ (print (str n \", \"))        \n" +
				"        next-val (- n (rand-long 100))] \n" +
				"    (if (> 10000 next-val)              \n" +
				"      #(incrementor2 next-val))))       \n" +
				"                                        \n" +
				"(trampoline incrementor2 500)             ";

		venice.eval(script);	
	}

	@Test
	public void test_true_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(true? nil)"));	
		assertTrue((Boolean)venice.eval("(true? true)"));	
		assertFalse((Boolean)venice.eval("(true? false)"));	
		assertFalse((Boolean)venice.eval("(true? 1)"));	
	}

	@Test
	public void test_type() {
		final Venice venice = new Venice();

		assertEquals(":core/nil", venice.eval("(pr-str (type nil))"));
		assertEquals(":core/boolean", venice.eval("(pr-str (type false))"));
		assertEquals(":core/boolean", venice.eval("(pr-str (type true))"));
		
		assertEquals(":core/keyword", venice.eval("(pr-str (type :a))"));
		assertEquals(":core/symbol", venice.eval("(pr-str (type 'a))"));
		assertEquals(":core/atom", venice.eval("(pr-str (type (atom 0)))"));
		assertEquals(":core/thread-local", venice.eval("(pr-str (type (thread-local)))"));
		
		assertEquals(":core/long", venice.eval("(pr-str (type 1))"));
		assertEquals(":core/long", venice.eval("(pr-str (type (* 1 3)))"));
		assertEquals(":core/double", venice.eval("(pr-str (type 1.0))"));
		assertEquals(":core/double", venice.eval("(pr-str (type (* 1.0 2.0)))"));
		assertEquals(":core/decimal", venice.eval("(pr-str (type 1.06M))"));
		assertEquals(":core/decimal", venice.eval("(pr-str (type (* 1.06M 2.0M)))"));
		
		assertEquals(":core/list", venice.eval("(pr-str (type '()))"));
		assertEquals(":core/list", venice.eval("(pr-str (type (list)))"));
		assertEquals(":core/vector", venice.eval("(pr-str (type '[]))"));
		assertEquals(":core/vector", venice.eval("(pr-str (type (vector)))"));
		assertEquals(":core/hash-set", venice.eval("(pr-str (type '#{}))"));
		assertEquals(":core/sorted-set", venice.eval("(pr-str (type (sorted-set)))"));
		assertEquals(":core/hash-map", venice.eval("(pr-str (type '{}))"));
		assertEquals(":core/hash-map", venice.eval("(pr-str (type (hash-map)))"));
		assertEquals(":core/sorted-map", venice.eval("(pr-str (type (sorted-map)))"));
		assertEquals(":core/ordered-map", venice.eval("(pr-str (type (ordered-map)))"));
		assertEquals(":core/mutable-map", venice.eval("(pr-str (type (mutable-map)))"));

		// Java Interop		
		assertEquals(":java.util.ArrayList", venice.eval("(pr-str (type (. :java.util.ArrayList :new)))"));
		assertEquals(":java.util.HashSet", venice.eval("(pr-str (type (. :java.util.HashSet :new)))"));
		assertEquals(":java.util.HashMap", venice.eval("(pr-str (type (. :java.util.HashMap :new)))"));

		// Custom Types	
		assertEquals(
			":user/complex", 
			venice.eval(
				"(do                                                      \n" +
				"  (deftype :user/complex [real :long, imaginary :long])  \n" +
				"  (pr-str (type (.: :user/complex 100 200))))             "));
	}

	@Test
	public void test_supertype() {
		final Venice venice = new Venice();

		assertEquals(":core/val", venice.eval("(pr-str (supertype nil))"));
		assertEquals(":core/val", venice.eval("(pr-str (supertype false))"));
		assertEquals(":core/val", venice.eval("(pr-str (supertype true))"));
		
		assertEquals(":core/string", venice.eval("(pr-str (supertype :a))"));
		assertEquals(":core/val", venice.eval("(pr-str (supertype 'a))"));
		assertEquals(":core/val", venice.eval("(pr-str (supertype (atom 0)))"));
		assertEquals(":core/val", venice.eval("(pr-str (supertype (thread-local)))"));
		
		assertEquals(":core/number", venice.eval("(pr-str (supertype 1))"));
		assertEquals(":core/number", venice.eval("(pr-str (supertype (* 1 3)))"));
		assertEquals(":core/number", venice.eval("(pr-str (supertype 1.0))"));
		assertEquals(":core/number", venice.eval("(pr-str (supertype (* 1.0 2.0)))"));
		assertEquals(":core/number", venice.eval("(pr-str (supertype 1.06M))"));
		assertEquals(":core/number", venice.eval("(pr-str (supertype (* 1.06M 2.0M)))"));
		
		assertEquals(":core/sequence", venice.eval("(pr-str (supertype '()))"));
		assertEquals(":core/sequence", venice.eval("(pr-str (supertype (list)))"));
		assertEquals(":core/sequence", venice.eval("(pr-str (supertype '[]))"));
		assertEquals(":core/sequence", venice.eval("(pr-str (supertype (vector)))"));
		assertEquals(":core/set", venice.eval("(pr-str (supertype '#{}))"));
		assertEquals(":core/set", venice.eval("(pr-str (supertype (sorted-set)))"));
		assertEquals(":core/map", venice.eval("(pr-str (supertype '{}))"));
		assertEquals(":core/map", venice.eval("(pr-str (supertype (hash-map)))"));
		assertEquals(":core/map", venice.eval("(pr-str (supertype (sorted-map)))"));
		assertEquals(":core/map", venice.eval("(pr-str (supertype (ordered-map)))"));
		assertEquals(":core/map", venice.eval("(pr-str (supertype (mutable-map)))"));

		// Java Interop		
		assertEquals(":java.util.AbstractList", venice.eval("(pr-str (supertype (. :java.util.ArrayList :new)))"));
		assertEquals(":java.util.AbstractSet", venice.eval("(pr-str (supertype (. :java.util.HashSet :new)))"));
		assertEquals(":java.util.AbstractMap", venice.eval("(pr-str (supertype (. :java.util.HashMap :new)))"));

		// Custom Types	
		assertEquals(
			":core/custom-type", 
			venice.eval(
				"(do                                                      \n" +
				"  (deftype :user/complex [real :long, imaginary :long])  \n" +
				"  (pr-str (supertype (.: :user/complex 100 200))))         "));
	}

	@Test
	public void test_union() {
		final Venice venice = new Venice();

		assertEquals("#{1 2 3}", venice.eval("(str (union (set 1 2 3)))"));
		assertEquals("#{1 2 3}", venice.eval("(str (union (set 1 2) (set 2 3)))"));
		assertEquals("#{1 2 3 4}", venice.eval("(str (union (set 1 2 3) (set 1 2) (set 1 4) (set 3)))"));

		assertEquals("#{1 2 3}", venice.eval("(str (union (sorted-set 1 2 3)))"));
		assertEquals("#{1 2 3}", venice.eval("(str (union (sorted-set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{1 2 3 4}", venice.eval("(str (union (sorted-set 1 2 3) (sorted-set 1 2) (sorted-set 1 4) (sorted-set 3)))"));

		assertEquals("#{1 2 3}", venice.eval("(str (union (set 1 2) (sorted-set 2 3)))"));
		assertEquals("#{1 2 3 4}", venice.eval("(str (union (set 1 2 3) (set 1 2) (sorted-set 1 4) (sorted-set 3)))"));
	}
	
	@Test
	public void test_update() {
		final Venice venice = new Venice();
		
		// list
		assertEquals("(3)", venice.eval("(str (update '() 0 (fn [x] 3)))"));
		assertEquals("(0 1)", venice.eval("(str (update '(0) 1 (fn [x] 1)))"));
		assertEquals("(3 1)", venice.eval("(str (update '(0 1) 0 (fn [x] 3)))"));
		assertEquals("(4 1)", venice.eval("(str (update '(0 1) 0 (fn [x] (+ x 4))))"));
		
		// vector
		assertEquals("[3]", venice.eval("(str (update [] 0 (fn [x] 3)))"));
		assertEquals("[0 1]", venice.eval("(str (update [0] 1 (fn [x] 1)))"));
		assertEquals("[3 1]", venice.eval("(str (update [0 1] 0 (fn [x] 3)))"));
		assertEquals("[4 1]", venice.eval("(str (update [0 1] 0 (fn [x] (+ x 4))))"));
		
		// map
		assertEquals("{:a 0}", venice.eval("(str (update {} :a (fn [x] 0)))"));
		assertEquals("{:a 1 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] 1)))"));
		assertEquals("{:a 3 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] 3)))"));
		assertEquals("{:a 4 :b 1}", venice.eval("(str (update {:a 0 :b 1} :a (fn [x] (+ x 4))))"));		
	}
	
	@Test
	public void test_update_BANG() {
		final Venice venice = new Venice();
		
		// map
		assertEquals("{:a 0}", venice.eval("(str (update! (mutable-map) :a (fn [x] 0)))"));
		assertEquals("{:a 1 :b 1}", venice.eval("(str (update! (mutable-map :a 0 :b 1) :a (fn [x] 1)))"));
		assertEquals("{:a 3 :b 1}", venice.eval("(str (update! (mutable-map :a 0 :b 1) :a (fn [x] 3)))"));
		assertEquals("{:a 4 :b 1}", venice.eval("(str (update! (mutable-map :a 0 :b 1) :a (fn [x] (+ x 4))))"));		
	}
	
	@Test
	public void test_update_in() {
		final Venice venice = new Venice();
		
		// vectors
		assertEquals("[2]", venice.eval("(str (update-in [1] [0] inc))"));
		assertEquals("[1 2 [4 4] 5]", venice.eval("(str (update-in [1 2 [3 4] 5] [2 0] inc))"));
		assertEquals("[1 2 {:a 4 :b 4} 5]", venice.eval("(str (update-in [1 2 {:a 3 :b 4} 5] [2 :a] inc))"));

		assertEquals(
			"[{:name \"James\" :age 26} {:name \"John\" :age 44}]", 
			venice.eval("(pr-str (update-in [ (ordered-map :name \"James\" :age 26)   \n" +
					    "                     (ordered-map :name \"John\"  :age 43) ] \n" +
					    "                   [1 :age]                                  \n" +
					    "                   inc))                                       "));
		
		// maps
		assertEquals("{:a 3}", venice.eval("(pr-str (update-in {:a 12} [:a] / 4))"));
	}
	
	@Test
	public void test_val() {
		final Venice venice = new Venice();

		assertEquals("2", venice.eval("(str (val (find {:a 1 :b 2} :b)))"));
	}
	
	@Test
	public void test_vals() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (sort (vals {})))"));
		assertEquals("(1 2 3)", venice.eval("(str (sort (vals {:a 1 :b 2 :c 3})))"));

		assertEquals("()", venice.eval("(str (sort (vals (ordered-map ))))"));
		assertEquals("(1 2 3)", venice.eval("(str (vals (ordered-map :a 1 :b 2 :c 3)))"));

		assertEquals("()", venice.eval("(str (sort (vals (sorted-map ))))"));
		assertEquals("(1 2 3)", venice.eval("(str (vals (sorted-map :a 1 :b 2 :c 3)))"));
	}

	@Test
	public void test_vary_meta() {
		final Venice venice = new Venice();
								
		assertEquals("1", venice.eval("(str (get (meta (vary-meta [] assoc :a 1)) :a))"));
	}
	
	@Test
	public void test_vector() {
		final Venice venice = new Venice();

		assertEquals("[]", venice.eval("(str [])"));
		assertEquals("[1 2 3]", venice.eval("(str [1 2 3])"));
		
		final Object result = venice.eval("(do [1 2 3])");
		assertTrue(result instanceof List);

		assertEquals("[]", venice.eval("(str (vector))"));
		assertEquals("[1 2 3]", venice.eval("(str (vector 1 2 3))"));
		assertEquals("[[]]", venice.eval("(str (vector []))"));
		assertEquals("[()]", venice.eval("(str (vector '()))"));
		assertEquals("[[1 2]]", venice.eval("(str (vector [1 2]))"));
		assertEquals("[[1 2] 3 4]", venice.eval("(str (vector [1 2] 3 4))"));
	}
	
	@Test
	public void test_vector_eval() {
		final Venice venice = new Venice();

		assertEquals("[1 3]", venice.eval("(pr-str [1 (+ 1 2)])"));
		assertEquals("[1 3]", venice.eval("(pr-str (vector 1 (+ 1 2)))"));
	}
	
	@Test
	public void test_vector_Q() {
		final Venice venice = new Venice();

		assertFalse((Boolean)venice.eval("(vector? nil)"));	
		assertFalse((Boolean)venice.eval("(vector? '())"));	
		assertTrue((Boolean)venice.eval("(vector? [])"));	
		assertFalse((Boolean)venice.eval("(vector? 1)"));	
	}

	@Test
	public void test_with_meta() {
		final Venice venice = new Venice();
		
		assertEquals("[1 2 3]", venice.eval("(str (with-meta [1 2 3] { :a 1 }))"));
		assertEquals("{:a 1}", venice.eval("(str (meta (with-meta [1 2 3] { :a 1 })))"));
	}

	@Test
	public void test_zipmap() {
		final Venice venice = new Venice();

		assertEquals("{:a 1 :b 2 :c 3 :d 4 :e 5}", venice.eval("(str (zipmap [:a :b :c :d :e] [1 2 3 4 5]))"));
		assertEquals("{:a 1 :b 2 :c 3}", venice.eval("(str (zipmap [:a :b :c] [1 2 3 4 5]))"));		
	}	
	
}
