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
import static org.junit.Assert.fail;

import org.junit.Test;


public class SpecialFormsTest {

	@Test
	public void test_try_only() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                          " +
				"  (throw \"test message\")    " +
				")                             ";

		try {
			venice.eval(lisp);
		}
		catch(VncException ex) {
			assertEquals("test message", ex.getMessage());
			return;
		}
		
		fail("Expected VncException");
	}
	
	@Test
	public void test_try_only_2() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                          " +
				"  (throw )                    " +
				")                             ";

		try {
			venice.eval(lisp);
		}
		catch(VncException ex) {
			assertEquals("nil", ex.getMessage());
			return;
		}
		
		fail("Expected VncException");
	}
	
	@Test
	public void test_try_catch_1() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                   " +
				"  (throw 100)          " +
				"  (catch               " +
				"    (do                " +
				"      (+ 1 2)          " +
				"      (+ 3 4)          " +
				"      -1)))            " +
				")                      ";

		assertEquals(Long.valueOf(-1L), venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_2() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                                   " +
				"  (throw 100)                          " +
				"  (catch                               " +
				"    (do                                " +
				"      (+ 1 2)                          " +
				"      (+ 3 4)                          " +
				"      (str (ordered-map :a 1 :b 2))))   " +
				")                                      ";

		assertEquals("{:a 1 :b 2}", venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_3() {
		final Venice venice = new Venice();

		final String lisp = 
				"(do                                      " +
				"  (+ 10 (try                             " +
				"          (throw 100)                    " +
				"          (catch 30)))                   " +
				")                                        ";

		assertEquals(Long.valueOf(40L), venice.eval(lisp));
	}
	
	@Test
	public void test_try_finally() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                   " +
				"  (throw 100)          " +
				"  (finally             " +
				"    (do                " +
				"      (+ 1 2)          " +
				"      (+ 3 4)          " +
				"      -2))             " +
				")                      ";

		assertEquals(Long.valueOf(-2L), venice.eval(lisp));
	}
	
	@Test
	public void test_try_catch_finally() {
		final Venice venice = new Venice();

		final String lisp = 
				"(try                   " +
				"  (throw 100)          " +
				"  (catch               " +
				"    (do                " +
				"      (+ 1 2)          " +
				"      (+ 3 4)          " +
				"      -1))             " +
				"  (finally             " +
				"    (do                " +
				"      (+ 1 2)          " +
				"      (+ 3 4)          " +
				"      -2))             " +
				")                      ";

		assertEquals(Long.valueOf(-2L), venice.eval(lisp));
	}

	@Test
	public void test_eval() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(17), venice.eval("(eval '(let [a 10] (+ 3 4 a)))"));
		assertEquals(Long.valueOf(6), venice.eval("(eval (list + 1 2 3))"));
		assertEquals(Long.valueOf(5), venice.eval("(do (def x '(+ 2 3)) (eval x))"));
	}

	@Test
	public void test_do() {
		final Venice venice = new Venice();
		
		assertEquals(null, venice.eval("(do )"));
		assertEquals(Long.valueOf(4), venice.eval("(do 4)"));
		assertEquals(Long.valueOf(5), venice.eval("(do 4 5)"));
		assertEquals(Long.valueOf(3), venice.eval("(do (+ 1 2))"));
		assertEquals(Long.valueOf(4), venice.eval("(do (+ 1 2) (+ 1 3))"));
		assertEquals(Long.valueOf(5), venice.eval("(do (+ 1 2) (+ 1 3) 5)"));
		assertEquals("{:a 1 :b 2}", venice.eval("(str (do (+ 1 2) (+ 1 3) (ordered-map :a 1 :b 2)))"));
	}

	@Test
	public void test_if() {
		final Venice venice = new Venice();

		assertEquals("no",  venice.eval("(if (> 3 6) \"yes\" \"no\")"));
		
		assertEquals("yes", venice.eval("(if (< 3 6) \"yes\" \"no\")"));
		
		assertEquals(null,  venice.eval("(if (< 3 6) nil \"no\")"));
		assertEquals("no",  venice.eval("(if (> 3 6) nil \"no\")"));

		assertEquals(null,  venice.eval("(if true nil \"no\")"));
		assertEquals("no",  venice.eval("(if false nil \"no\")"));
		
		assertEquals("empty",  venice.eval("(if (== 0 (count '())) :empty :not-empty)"));
		assertEquals("not-empty",  venice.eval("(if (== 0 (count '(1))) :empty :not-empty)"));
	}

	@Test
	public void test_fn_anonymous() {
		final Venice venice = new Venice();
		
		assertEquals("(1 4 9 16 25 36 49 64 81)", venice.eval("(str (map (fn [x] (* x x)) (range 1 10)))"));
		
		assertEquals("(2 4 6 8)", venice.eval("(str (map (fn [x] (* 2 x)) (range 1 5)))"));
	}

	@Test
	public void test_fn_def() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(7), venice.eval("(do (def add5 (fn [x] (+ x 5))) (add5 2))"));
	}

	@Test
	public void test_fn_destructuring_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                    \n" +
				"   (def test                           \n" +
				"        (fn [x [u v w]]                \n" +
				"            (+ x u v w)))              \n" +
				"                                       \n" +
				"   (test 2 [3 4 5])                    \n" +
				") ";

		assertEquals(Long.valueOf(14), venice.eval(script));
	}

	@Test
	public void test_fn_destructuring_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                    \n" +
				"   (def test                           \n" +
				"        (fn [x {:keys [u v w]}]        \n" +
				"            (+ x u v w)))              \n" +
				"                                       \n" +
				"   (test 2 {:u 3 :v 4 :w 5})           \n" +
				") ";

		assertEquals(Long.valueOf(14), venice.eval(script));
	}

	@Test
	public void test_let() {
		final Venice venice = new Venice();
		
		assertEquals("1", venice.eval("(str (let [a 1]  a))"));
		
		assertEquals("1", venice.eval("(str (let [a 1 b 2] b a))"));

		assertEquals("2", venice.eval("(str (let [a 1 b 2] a b))"));
		
		assertEquals("3", venice.eval("(str (let [a 1 b 2] (+ a b)))"));
		
		assertEquals("8", venice.eval("(str (let [c (+ 1 2) d 5 e 6] (- (+ d e) c)))"));
	}
	
	@Test
	public void test_let_2() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(9L), venice.eval("(let [a 1 b 2] (+ a b) 6 9)"));
		assertEquals(Long.valueOf(3L), venice.eval("(let [a 1 b 2] 6 9 (+ a b))"));
		assertEquals(Long.valueOf(3L), venice.eval("(let [a 1 b 2] (do 6 9 (+ a b)))"));
	}
	
	@Test
	public void test_let_3() {
		final Venice venice = new Venice();

		final String sideeffect = 
				"(do                                        " +
				"   (def counter (atom 3))                  " +
				"                                           " +
				"   (let [a 1 b 2]                          " +
				"         (swap! counter inc)               " +
				"         (swap! counter inc)               " +
				"         (deref counter))                  " +
				")                                          ";
		
		assertEquals(5L, venice.eval(sideeffect));
	}
	
	@Test
	public void test_let_destructuring_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                           " +
				"   (let [x 2                                  " +
				"         [u v w] [3 4 5] ]                    " +
				"        (+ x u v w))                          " +
				")                                             ";
		
		assertEquals(14L, venice.eval(script));
	}
	
	@Test
	public void test_let_destructuring_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                           " +
				"   (let [x 2                                  " +
				"         {:keys [u v w]} {:u 3 :v 4 :w 5} ]   " +
				"        (+ x u v w))                          " +
				")                                             ";
		
		assertEquals(14L, venice.eval(script));
	}

	@Test
	public void test_loop() {
		final Venice venice = new Venice();
		
		final String lisp = 
				"(do                                             " +
				"  (def factorial                                " +
				"    (fn [n]                                     " +
				"      (loop [cnt n                              " +
				"             acc 1]                             " +
				"          (if (zero? cnt)                       " +
				"              acc                               " +
				"              (recur (dec cnt) (* acc cnt)))))) " +
				"                                                " +
				"   (factorial 6)                                " +
				")                                               ";

		assertEquals(Long.valueOf(720L), venice.eval(lisp));
	}

	@Test
	public void test_loop_deep() {
		final Venice venice = new Venice();
		
		final String lisp = 
				"(do                                             " +
				"  (def sum                                      " +
				"    (fn [n]                                     " +
				"      (loop [cnt n                              " +
				"             acc 0]                             " +
				"          (if (zero? cnt)                       " +
				"              acc                               " +
				"              (recur (dec cnt) (+ acc cnt)))))) " +
				"                                                " +
				"   (sum 10000)                                  " +
				")                                               ";

		assertEquals(Long.valueOf(50005000), venice.eval(lisp));
	}

	@Test
	public void test_loop_fib() {
		final Venice venice = new Venice();
		
		final String lisp = 
				"(do                                                            " +
				"  (def fib                                                     " +
				"    (fn [n]                                                    " +
				"      (loop [x [0 1]]                                          " +
				"          (if (>= (count x) n)                                 " +
				"              x                                                " +
				"              (recur (conj x (+ (last x)                       " +
				"                                (nth x (- (count x) 2))))))))) " +
				"                                                               " +
				"   (str (fib 10))                                              " +
				")                                                              ";

		assertEquals("[0 1 1 2 3 5 8 13 21 34]", venice.eval(lisp).toString());
	}

	@Test
	public void test_loop_nested() {
		final Venice venice = new Venice();
		
		final String lisp = 
				"(do                                                            " +
				"                                                               " +
				"  (def fib                                                     " +
				"    (fn [n]                                                    " +
				"      (loop [x [0 1]]                                          " +
				"          (if (>= (count x) n)                                 " +
				"              x                                                " +
				"              (recur (conj x (+ (last x)                       " +
				"                                (nth x (- (count x) 2))))))))) " +
				"                                                               " +
				"  (def sum-fib                                                 " +
				"    (fn [n]                                                    " +
				"      (loop [cnt n                                             " +
				"             acc 0]                                            " +
				"          (if (zero? cnt)                                      " +
				"              acc                                              " +
				"              (recur (dec cnt) (+ acc (last (fib cnt))))))))   " +
				"                                                               " +
				"   (sum-fib 10)                                                " +
				")                                                              ";

		assertEquals(Long.valueOf(89L), venice.eval(lisp));
	}

}
