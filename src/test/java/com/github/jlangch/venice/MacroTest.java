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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;


public class MacroTest {

	@Test
	public void testQuoting() {
		final Venice venice = new Venice();
		
		assertEquals("(1 2 3)",                    venice.eval("(str '(1 2 3))"));
		assertEquals("(1 2 (list 4 5))",           venice.eval("(str '(1 2 (list 4 5)))"));
		assertEquals("(1 2 (unquote (list 4 5)))", venice.eval("(str '(1 2 ~(list 4 5)))"));

		assertEquals("(1 2 3)",                    venice.eval("(str `(1 2 3))"));
		assertEquals("(1 2 (list 4 5))",           venice.eval("(str `(1 2 (list 4 5)))"));
		assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 5)))"));
		
		assertEquals("(1 2 (list 4 5))",           venice.eval("(str `(1 2 (list 4 5)))"));
		assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 5)))"));
		assertEquals("(1 2 4 5)",                  venice.eval("(str `(1 2 ~@(list 4 5)))"));
		
		assertEquals("(1 2 (list 4 (+ 3 2)))",     venice.eval("(str `(1 2 (list 4 (+ 3 2))))"));
		assertEquals("(1 2 (4 5))",                venice.eval("(str `(1 2 ~(list 4 (+ 3 2))))"));
		assertEquals("(1 2 4 5)",                  venice.eval("(str `(1 2 ~@(list 4 (+ 3 2))))"));
		
		assertEquals("(1 2 (4 (+ 3 2)))",          venice.eval("(str `(1 2 ~(list 4 `(+ 3 2))))"));
		assertEquals("(1 2 4 (+ 3 2))",            venice.eval("(str `(1 2 ~@(list 4 `(+ 3 2))))"));
	}

	@Test
	public void testGensym() {
		final Venice venice = new Venice();

		final Set<String> symbols = new HashSet<>();
		
		for(int ii=0; ii<1000; ii++) {
			symbols.add((String)venice.eval("(gensym)"));
		}
		assertEquals(1000, symbols.size());
		
		for(int ii=0; ii<1000; ii++) {
			symbols.add((String)venice.eval("(gensym 'hello)"));
		}
		assertEquals(2000, symbols.size());
	}

	@Test
	public void test_assert() {
		final Venice venice = new Venice();

		try {
			venice.eval("(assert false)");			
			fail("Expected AssertionException");
		}
		catch(AssertionException ex) {
			assertEquals("Assert failed: false", ex.getMessage());
		}

		try {
			venice.eval("(assert false \"error\")");			
			fail("Expected AssertionException");
		}
		catch(AssertionException ex) {
			assertEquals("Assert failed (error): false", ex.getMessage());
		}
	}

	@Test
	public void test_and() {
		final Venice venice = new Venice();

		assertEquals(true,  (Boolean)venice.eval("(and true))"));
		assertEquals(false, (Boolean)venice.eval("(and false))"));
		
		assertEquals(true,  (Boolean)venice.eval("(and true true))"));
		assertEquals(false, (Boolean)venice.eval("(and true false))"));
		assertEquals(false, (Boolean)venice.eval("(and false true))"));
		assertEquals(false, (Boolean)venice.eval("(and false false))"));
		
		assertEquals(true,  (Boolean)venice.eval("(and true true true))"));
		assertEquals(false, (Boolean)venice.eval("(and true true false))"));
		assertEquals(false, (Boolean)venice.eval("(and false false false))"));


		assertEquals(true,  (Boolean)venice.eval("(and (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 0)))"));
		
		assertEquals(true,  (Boolean)venice.eval("(and (== 1 1) (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 1) (== 1 0)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 0) (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 0) (== 1 0)))"));
		
		assertEquals(true,  (Boolean)venice.eval("(and (== 1 1) (== 1 1) (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 1) (== 1 1) (== 1 0)))"));
		assertEquals(false, (Boolean)venice.eval("(and (== 1 0) (== 1 0) (== 1 0)))"));
	}

	@Test
	public void test_or() {
		final Venice venice = new Venice();

		assertEquals(true,  (Boolean)venice.eval("(or true))"));
		assertEquals(false, (Boolean)venice.eval("(or false))"));
		
		assertEquals(true,  (Boolean)venice.eval("(or true true))"));
		assertEquals(true,  (Boolean)venice.eval("(or true false))"));
		assertEquals(true,  (Boolean)venice.eval("(or false true))"));
		assertEquals(false, (Boolean)venice.eval("(or false false))"));
		
		assertEquals(true,  (Boolean)venice.eval("(or true true true))"));
		assertEquals(true,  (Boolean)venice.eval("(or true true false))"));
		assertEquals(true,  (Boolean)venice.eval("(or false false true))"));
		assertEquals(true,  (Boolean)venice.eval("(or false true true))"));
		assertEquals(false, (Boolean)venice.eval("(or false false false))"));

		
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 1))"));
		assertEquals(false, (Boolean)venice.eval("(or (== 1 0))"));
		
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 1) (== 1 1)))"));
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 1) (== 1 0)))"));
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 0) (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(or (== 1 0) (== 1 0)))"));
		
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 1) (== 1 1) (== 1 1)))"));
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 1) (== 1 1) (== 1 0)))"));
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 0) (== 1 0) (== 1 1)))"));
		assertEquals(true,  (Boolean)venice.eval("(or (== 1 0) (== 1 1) (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(or (== 1 0) (== 1 0) (== 1 0)))"));
	}
	
	@Test
	public void test_or_SideEffects() {
		final Venice venice = new Venice();

		assertEquals(
			Long.valueOf(0L), 
			venice.eval(
				"(do                                                  " +
				"    (def counter (atom 0))                           " + 
				"    (or true (== 1 (do (swap! counter inc) 0)))      " + 
				"    (deref counter)                                  " + 
				") "));

		assertEquals(
			Long.valueOf(1L), 
			venice.eval(
				"(do                                                  " +
				"    (def counter (atom 0))                           " + 
				"    (or false (== 1 (do (swap! counter inc) 0)))     " + 
				"    (deref counter)                                  " + 
				") "));
	}
	
	@Test
	public void test_and_SideEffects() {
		final Venice venice = new Venice();

		assertEquals(
			Long.valueOf(1L), 
			venice.eval(
				"(do                                                  " +
				"    (def counter (atom 0))                           " + 
				"    (and true (== 1 (do (swap! counter inc) 0)))     " + 
				"    (deref counter)                                  " + 
				") "));

		assertEquals(
			Long.valueOf(0L), 
			venice.eval(
				"(do                                                  " +
				"    (def counter (atom 0))                           " + 
				"    (and false (== 1 (do (swap! counter inc) 0)))    " + 
				"    (deref counter)                                  " + 
				") "));
	}
	
	@Test
	public void test_not() {
		final Venice venice = new Venice();

		assertEquals(false, (Boolean)venice.eval("(not true))"));
		assertEquals(true,  (Boolean)venice.eval("(not false))"));

		assertEquals(true,  (Boolean)venice.eval("(not (not true)))"));
		assertEquals(false, (Boolean)venice.eval("(not (not (not true))))"));

		assertEquals(false, (Boolean)venice.eval("(not (== 1 1))"));
		assertEquals(true,  (Boolean)venice.eval("(not (== 1 2)))"));

		assertEquals(true,  (Boolean)venice.eval("(not (not (== 1 1)))"));
		assertEquals(false, (Boolean)venice.eval("(not (not (== 1 2))))"));
	}
	
	@Test
	public void test_cond() {
		final Venice venice = new Venice();
		
		final String pos = 
				"(do                                 " +
				"   (defn pos-neg-or-zero [n]        " +
				"         (cond                      " +
				"   		 (< n 0) \"negative\"    " +
				"            (> n 0) \"positive\"    " +
				"            :else \"zero\"))        " +
				"                                    " +
				"   (pos-neg-or-zero 5)              " +
				")                                   ";
		
		final String neg = 
				"(do                                 " +
				"   (defn pos-neg-or-zero [n]        " +
				"         (cond                      " +
				"   		 (< n 0) \"negative\"    " +
				"            (> n 0) \"positive\"    " +
				"            :else \"zero\"))        " +
				"                                    " +
				"   (pos-neg-or-zero -5)             " +
				")                                   ";
		
		final String zero = 
				"(do                                 " +
				"   (defn pos-neg-or-zero [n]        " +
				"         (cond                      " +
				"   		 (< n 0) \"negative\"    " +
				"            (> n 0) \"positive\"    " +
				"            :else \"zero\"))        " +
				"                                    " +
				"   (pos-neg-or-zero 0)              " +
				")                                   ";

		assertEquals("positive", venice.eval(pos));
		assertEquals("negative", venice.eval(neg));
		assertEquals("zero", venice.eval(zero));
	}
	
	@Test
	public void test_case() {
		final Venice venice = new Venice();
		
		final String script1 = 
				"(case (+ 1 9)    " +
				"   10  :ten      " +
				"   20  :twenty   " +
				"   30  :thirty   " +
				"   :dont-know)   ";

		assertEquals(":ten", venice.eval("(str " + script1 + ")"));
		//System.out.println(venice.eval("(str (macroexpand " + script1 + "))"));
		
		final String script2 = 
				"(case (+ 1 19)   " +
				"   10  :ten      " +
				"   20  :twenty   " +
				"   30  :thirty   " +
				"   :dont-know)   ";

		assertEquals(":twenty", venice.eval("(str " + script2 + ")"));
		
		final String script3 = 
				"(case 1          " +
				"   10  :ten      " +
				"   20  :twenty   " +
				"   30  :thirty   " +
				"   :dont-know)   ";

		assertEquals(":dont-know", venice.eval("(str " + script3 + ")"));
		
		final String script4 = 
				"(case 1          " +
				"   10  :ten      " +
				"   20  :twenty   " +
				"   30  :thirty)  ";

		assertNull(venice.eval(script4));
	}
		
	@Test
	public void test_when() {
		final Venice venice = new Venice();

		assertTrue((Boolean)venice.eval("(when (== 1 1) true)"));
		assertEquals(null, venice.eval("(when (!= 1 1) true)"));
		
		assertEquals(Long.valueOf(300), venice.eval("(when (== 1 1) (println 100) 300)", Parameters.of("*out*", null)));
		assertEquals(Long.valueOf(300), venice.eval("(when (== 1 1) 100 200 300)"));
		assertEquals(null,              venice.eval("(when (!= 1 1) 100 200 300)"));
	}
	
	@Test
	public void test_whenSideEffects() {
		final Venice venice = new Venice();

		final String sideeffect1 = 
				"(do                                        " +
				"   (def counter (atom 0))                  " +
				"                                           " +
				"   (when true                              " +
				"         (swap! counter inc)               " +
				"         (swap! counter inc))              " +
				"                                           " +
				"   (deref counter)                         " +
				")                                          ";
		assertEquals(2L, venice.eval(sideeffect1));

		final String sideeffect2 = 
				"(do                                        " +
				"   (def counter (atom 0))                  " +
				"                                           " +
				"   (when false                             " +
				"         (swap! counter inc)               " +
				"         (swap! counter inc))              " +
				"                                           " +
				"   (deref counter)                         " +
				")                                          ";
		assertEquals(0L, venice.eval(sideeffect2));
	}

	@Test
	public void test_defn() {
		final Venice venice = new Venice();

		final String script1 =
				"(do                            \n" +
				"   (defn sum [x y] (+ x y))    \n" + 
				"   (sum 2 5)                   \n" + 
				") ";

		assertEquals(Long.valueOf(7), venice.eval(script1));
		
		// this is legal (not a pre-condition)
		final String script2 = 
				"(do                           \n" +
				"   (def datagen               \n" +
				"        (fn [] { :a 100 } ))  \n" +
				"                              \n" +
				"   (str (datagen ))           \n" +
				") ";

		assertEquals("{:a 100}", venice.eval(script2));
	}

	@Test
	public void test_defn_multi_arity() {
		final Venice venice = new Venice();
		
		final String s = 
				"(do                                                   \n" +
				"   (defn arity ([] 0)                                 \n" +
				"               ([a] 1)                                \n" +
				"               ([a b] 2)                              \n" +
				"               ([a b c] 3))                           \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3)))  ";
		
		assertEquals("0123", venice.eval(s));
	}

	@Test
	public void test_defn_multi_arity_precondition() {
		final Venice venice = new Venice();
		
		final String s = 
				"(do                                                   \n" +
				"   (defn arity ([] 0)                                 \n" +
				"               ([a] { :pre [(> a 0)] } 1)             \n" +
				"               ([a b] { :pre [(> a 0)] } 2)           \n" +
				"               ([a b c] { :pre [(> a 0)] } 3))        \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3)))  ";
		
		assertEquals("0123", venice.eval(s));
	}

	@Test
	public void test_defn_precondition() {
		final Venice venice = new Venice();

		final String script1 = 
				"(do                           \n" +
				"   (defn sum [x y]            \n" +
				"         { :pre [(> x 0)] }   \n" +
				"         (+ x y))             \n" +
				"                              \n" +
				"   (sum 1 3)                  \n" +
				") ";

		assertEquals(Long.valueOf(4), venice.eval(script1));
	}

	@Test
	public void test_defn_precondition_failed() {
		final Venice venice = new Venice();

		final String script = 
				"(do                           \n" +
				"   (defn sum [x y]            \n" +
				"         { :pre [(> x 0)] }   \n" +
				"         (+ x y))             \n" +
				"                              \n" +
				"   (sum 0 3)                  \n" +
				") ";

		assertThrows(AssertionException.class, () -> venice.eval(script));
	}

	@Test
	public void test_dotimes() {
		final Venice venice = new Venice();

		final String lisp =
				"(do                                      " +
				"    (def counter (atom 0))               " + 
				"    (dotimes [n 10] (swap! counter inc)) " + 
				"    (deref counter)                      " + 
				") ";

		assertEquals(Long.valueOf(10L), venice.eval(lisp));
	}

	@Test
	public void test_dotimes2() {
		final Venice venice = new Venice();

		final String lisp =
				"(do                                                   " +
				"    (def counter (atom 0))                            " + 
				"    (dotimes [n 10] (swap! counter (fn [x] (+ x n)))) " + 
				"    (deref counter)                                   " + 
				") ";

		assertEquals(Long.valueOf(45L), venice.eval(lisp));
	}

	@Test
	public void test_while() {
		final Venice venice = new Venice();

		final String lisp =
				"(do                                                            " +
				"    (def a (atom 10))                                          " + 
				"    (def b (atom 0))                                           " + 
				"    (while (pos? (deref a)) (do (swap! b inc) (swap! a dec)))  " + 
				"    (deref b)                                                  " +
				") ";

		assertEquals(Long.valueOf(10L), venice.eval(lisp));
	}

	@Test
	public void test_threadfirst() {
		final Venice venice = new Venice();

		// (- (/ (+ 5 3) 2) 1)
		assertEquals(Long.valueOf(3L), venice.eval("(-> 5 (+ 3) (/ 2) (- 1))"));
	}

	@Test
	public void test_threadlast() {
		final Venice venice = new Venice();
	
		// (- 1 (/ 32 (+ 3 5)))
		assertEquals(Long.valueOf(-3L), venice.eval("(->> 5 (+ 3) (/ 32) (- 1))"));

		assertEquals(
				"(7 9)", 
				venice.eval(
						"(str                                                      " +
						"   (->> [{:a 1 :b 2} {:a 3 :b 4} {:a 5 :b 6} {:a 7 :b 8}] " +
						"        (map (fn [x] (get x :b)))                         " +
						"        (filter (fn [x] (> x 4)))                         " +
						"        (map inc))))                                      "));
}

	@Test
	public void test_macroexpand() {
		final Venice venice = new Venice();

		final String s1 = 
				"(do                                        " +
				"   (defmacro when1 [expr form]             " +
				"      (list 'if expr nil form))            " +
				"                                           " +
				"   (macroexpand (when1 true (+ 1 1)))      " +
				")                                          ";


		final String s2 = 
				"(do                                        " +
				"   (defmacro when1 [expr form]             " +
				"      (list 'if expr nil form))            " +
				"                                           " +
				"   (macroexpand (when1 false (+ 1 1)))     " +
				")                                          ";

		assertEquals("(if true nil (+ 1 1))", venice.eval("(str " + s1 + ")"));
		assertEquals("(if false nil (+ 1 1))", venice.eval("(str " + s2 + ")"));
	}

	@Test
	public void test_list_comp() {
		final Venice venice = new Venice();

		assertEquals(
				"(0 1 2 3 4 5 6 7 8 9)", 
				venice.eval("(str (list-comp [x (range 10)] x))"));

		assertEquals(
				"(0 2 4 6 8)", 
				venice.eval("(str (list-comp [x (range 5)] (* x 2)))"));

		assertEquals(
				"(1 3 5 7 9)", 
				venice.eval("(str (list-comp [x (range 10) :when (odd? x)] x))"));

		assertEquals(
				"(2 6 10 14 18)", 
				venice.eval("(str (list-comp [x (range 10) :when (odd? x)] (* x 2)))"));

		assertEquals(
				"([a 0] [a 1] [a 2] [b 0] [b 1] [b 2] [c 0] [c 1] [c 2])", 
				venice.eval("(str (list-comp [x (into '() \"abc\") y [0 1 2]] [x y]))"));
	}

	@Test
	public void test_time() {
		final Venice venice = new Venice();

		assertEquals("2", venice.eval("(str (time (+ 1 1)))"));
	}

	@Test
	public void test_if_let() {
		final Venice venice = new Venice();

		final String script =
				"(do                                     " +
				"   (defn demo [arg]                     " + 
				"      (if-let [x arg]                   " + 
				"         \"then\"                       " + 
				"         \"else\"))                     " +
				"                                        " +
				"   [ (demo 1) (demo nil) (demo false) ] " +
				") ";

		assertEquals("[then else else]", venice.eval("(str " + script + ")"));
	}

}
