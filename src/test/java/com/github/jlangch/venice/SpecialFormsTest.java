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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.CapturingPrintStream;


public class SpecialFormsTest {
	
	@Test
	public void test_eval() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(17), venice.eval("(eval '(let [a 10] (+ 3 4 a)))"));
		assertEquals(Long.valueOf(6), venice.eval("(eval (list + 1 2 3))"));
		assertEquals(Long.valueOf(5), venice.eval("(do (def x '(+ 2 3)) (eval x))"));
	}
	
	@Test
	public void test_bound_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(do (let [x 1] (bound? 'x)))"));
		assertTrue((Boolean)venice.eval("(do (def x 1) (bound? 'x))"));
		assertFalse((Boolean)venice.eval("(do (def x 1) (bound? 'y))"));
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
	public void test_dorun() {
		final Venice venice = new Venice();
		
		assertEquals(3L, venice.eval("(dorun 4 3)"));
		
		assertEquals(3L, venice.eval("(dorun 4 (+ 1 2))"));
		
		assertEquals(3L, venice.eval("(dorun 4 (do (+ 1 20) (+ 1 2)))"));
	}

	@Test
	public void test_dobench() {
		final Venice venice = new Venice();
		
		assertEquals(4L, ((List<?>)venice.eval("(dobench 4 3)")).size());
		
		assertEquals(4L, ((List<?>)venice.eval("(dobench 4 (+ 1 2))")).size());
		
		assertEquals(4L, ((List<?>)venice.eval("(dobench 4 (do (+ 1 20) (+ 1 2)))")).size());
	}

	@Test
	public void test_locking() {
		final String script = 
				"(do                           \n" +
				"   (def mutex 1)              \n" +
				"                              \n" +
				"   (locking mutex (+ 1 2))    \n" +
				") ";

		final Venice venice = new Venice();
		
		assertEquals(3L, venice.eval(script));
	}

	@Test
	public void test_def() {
		final Venice venice = new Venice();
		
		assertEquals(4L, venice.eval("(do (def x 1) (def y 3) (+ x y))"));
		assertEquals(30L, venice.eval("(do (def x 1) (def y 3) (let [x 10 y 20] (+ x y)))"));
		assertEquals(4L, venice.eval("(do (def x 1) (def y 3) (let [x 10 y 20] (+ x y)) (+ x y))"));
	}

	@Test
	public void test_defonce() {
		final Venice venice = new Venice();
		
		assertEquals(4L, venice.eval("(do (defonce x 1) (defonce y 3) (+ x y))"));
		assertEquals(30L, venice.eval("(do (defonce x 1) (defonce y 3) (let [x 10 y 20] (+ x y)))"));
		assertEquals(4L, venice.eval("(do (defonce x 1) (defonce y 3) (let [x 10 y 20] (+ x y)) (+ x y))"));

		assertThrows(VncException.class, () -> {
			venice.eval("(do (defonce x 1) (def x 3) x)");
		});
		
		assertThrows(VncException.class, () -> {
			venice.eval("(do (defonce x 1) (def-dynamic x 3) x)");
		});

		assertThrows(VncException.class, () -> {
			venice.eval("(do (defonce x 1) (defonce x 3) x)");
		});

		venice.eval("(do (def x 1) (defonce x 3) x)");

		assertThrows(VncException.class, () -> {
			venice.eval("(do (def x 1) (defonce x 3) (defonce x 5) x)");
		});
	}

	@Test
	public void test_def_dynamic() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (with-out-str                  \n" +
				"      (print x)                   \n" +
				"      (binding [x 200]            \n" +
				"        (print \"-\")             \n" +
				"        (print x))                \n" +
				"      (print \"-\")               \n" +
				"      (print x)))                   ";
				
		assertEquals("100-200-100", venice.eval(script));					
	}

	@Test
	public void test_def_dynamic_namespace() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                                   \n" +
				"   (ns alpha)                         \n" +
				"                                      \n" +
				"   (def-dynamic x 100)                \n" +
				"                                      \n" +
				"   (with-out-str                      \n" +
				"      (print alpha/x)                 \n" +
				"      (binding [alpha/x 200]          \n" +
				"        (print \"-\")                 \n" +
				"        (print alpha/x))              \n" +
				"      (print \"-\")                   \n" +
				"      (print alpha/x)))                 ";
				
		assertEquals("100-200-100", venice.eval(script));					
	}
	
	@Test
	public void test_def_dynamic_future() {
		final Venice venice = new Venice();

		final String script =
				"(do                                          \n" +
				"   (def-dynamic x 100)                       \n" +
				"                                             \n" +
				"   (with-out-str                             \n" +
				"      (print x)                              \n" +
				"      (print \"-a-\")                        \n" +
				"      (let [f (future (fn []                 \n" +
				"                        (print x)            \n" +
				"                        (print \"-b-\")      \n" +
				"                        (binding [x 200]     \n" +
				"                           (print x)         \n" +
				"                           (print \"-c-\"))  \n" +
				"                        x))]                 \n" +
				"        (print @f))))                          ";

		assertEquals("100-a-100-b-200-c-100", venice.eval(script));					
	}

	@Test
	public void test_binding() {
		final Venice venice = new Venice();
			
		final String script =
				"(with-out-str                \n" +
				"   (binding [x 200]          \n" +
				"      (print x)))              ";
				
		assertEquals("200", venice.eval(script));					
	}

	@Test
	public void test_binding_nested() {
		final Venice venice = new Venice();
			
		final String script =
				"(with-out-str                \n" +
				"   (binding [x 200]          \n" +
				"      (print x)              \n" +
				"      (print \"-\")          \n" +
				"      (binding [x 400]       \n" +
				"         (print x))))          ";
				
		assertEquals("200-400", venice.eval(script));					
	}

	@Test
	public void test_doc() {
		final CapturingPrintStream ps = new CapturingPrintStream();
		
		final Venice venice = new Venice();
		
		venice.eval("(doc +)", Parameters.of("*out*", ps));
		assertNotNull(ps.getOutput());
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

		assertEquals("yes",  venice.eval("(if true \"yes\")"));
		assertEquals(null,  venice.eval("(if false \"yes\")"));
		
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
	public void test_fn_anonymous_named() {
		final Venice venice = new Venice();
		
		assertEquals("(1 4 9 16 25 36 49 64 81)", venice.eval("(str (map (fn square [x] (* x x)) (range 1 10)))"));
		
		assertEquals("(2 4 6 8)", venice.eval("(str (map (fn double [x] (* 2 x)) (range 1 5)))"));
	}

	@Test
	public void test_fn_anonymous_short() {
		final Venice venice = new Venice();
		
		assertEquals("(1 4 9 16 25 36 49 64 81)", venice.eval("(str (map #(* %1 %1) (range 1 10)))"));
		assertEquals("(1 4 9 16 25 36 49 64 81)", venice.eval("(str (map #(* % %) (range 1 10)))"));
		
		assertEquals("(2 4 6 8)", venice.eval("(str (map #(* 2 %1) (range 1 5)))"));	
		assertEquals("(2 4 6 8)", venice.eval("(str (map #(* 2 %) (range 1 5)))"));
	}

	@Test
	public void test_fn_def() {
		final Venice venice = new Venice();
		
		assertEquals(Long.valueOf(7), venice.eval("test", "(do (defn add5 [x] (+ x 5)) (add5 2))"));
		assertEquals(Long.valueOf(7), venice.eval("test", "(do (defn add5 [x] (+ x 5)) (add5 (+ 1 1)))"));
		assertEquals(Long.valueOf(7), venice.eval("test", "(do (defn add5 [x] (+ x 5)) (add5 (+ (- 2 1) (+ 0 1))))"));
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
	public void test_fn_precondition() {
		final Venice venice = new Venice();

		final String script1 = 
				"(do                                    \n" +
				"   (def sum                            \n" +
				"        (fn [x y]                      \n" +
				"            { :pre [(> x 0)] }         \n" +
				"            (+ x y)))                  \n" +
				"                                       \n" +
				"   (sum 1 3)                           \n" +
				") ";

		assertEquals(Long.valueOf(4), venice.eval(script1));

		// this is legal (not a pre-condition)
		final String script2 = 
				"(do                                        \n" +
				"   (def datagen                            \n" +
				"        (fn [] { :a 100 :b 200 :c 300 } )) \n" +
				"                                           \n" +
				"   (datagen )                              \n" +
				") ";

		venice.eval(script2);
	}

	@Test
	public void test_fn_precondition_fail() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                    \n" +
				"   (def sum                            \n" +
				"        (fn [x y]                      \n" +
				"            { :pre [(> x 0)] }         \n" +
				"            (+ x y)))                  \n" +
				"                                       \n" +
				"   (sum 0 3)                           \n" +
				") ";

		assertThrows(AssertionException.class, () -> venice.eval(script));
	}

	@Test
	public void test_fn_precondition_named() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                    \n" +
				"   (def sum                            \n" +
				"        (fn sum [x y]                  \n" +
				"            { :pre [(> x 0)] }         \n" +
				"            (+ x y)))                  \n" +
				"                                       \n" +
				"   (sum 1 3)                           \n" +
				") ";

		assertEquals(Long.valueOf(4), venice.eval(script));

		// this is legal (not a pre-condition)
		final String script1 = 
				"(do                                             \n" +
				"   (def datagen                                 \n" +
				"        (fn test [] { :a 100 :b 200 :c 300 } )) \n" +
				"                                                \n" +
				"   (datagen )                                   \n" +
				") ";

		venice.eval(script1);
	}

	@Test
	public void test_fn_local() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                    \n" +
				"   (let [sum (fn [x y] (+ x y))]       \n" +
				"      (sum 4 5)))                      ";

		assertEquals(Long.valueOf(9), venice.eval(script));
	}

	@Test
	public void test_fn_local_error() {
		final String script = 
				"(do                                    \n" +
				"   (let [sum (fn [x y] (+ x y))]       \n" +
				"      (sum 4 5))                       \n" +
				"   (sum 1 2))                            ";

		assertThrows(VncException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_fn_multi_arity() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                                      \n" +
				"   (def arity (fn ([] 0)                                 \n" +
				"                  ([a] 1)                                \n" +
				"                  ([a b] 2)                              \n" +
				"                  ([a b c] 3)))                          \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3)))     ";
		
		assertEquals("0123", venice.eval(s));
	}

	@Test
	public void test_fn_multi_arity_remaining() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                                                      \n" +
				"   (def arity (fn ([] 0)                                                 \n" +
				"                  ([a & z] 1)                                            \n" +
				"                  ([a b & z] 2)                                          \n" +
				"                  ([a b c & z] 3)))                                      \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3) (arity 1 2 3 4)))     ";
		
		assertEquals("01233", venice.eval(s));
	}

	@Test
	public void test_defn_multi_arity() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                                      \n" +
				"   (defn arity    ([] 0)                                 \n" +
				"                  ([a] 1)                                \n" +
				"                  ([a b] 2)                              \n" +
				"                  ([a b c] 3))                           \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3)))     ";
		
		assertEquals("0123", venice.eval(s));
	}

	@Test
	public void test_defn_multi_arity_remaining() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                                                      \n" +
				"   (defn arity    ([] 0)                                                 \n" +
				"                  ([a & z] 1)                                            \n" +
				"                  ([a b & z] 2)                                          \n" +
				"                  ([a b c & z] 3))                                       \n" +
				"   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3) (arity 1 2 3 4)))     ";
		
		assertEquals("01233", venice.eval(s));
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
	public void test_let_4() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                " +
				"   (let [a 1, b 2]                 " +
				"      (defn get-a [] a)            " +
				"      (let [a 3]                   " +
				"         (str [a b (get-a )]))))   ";
		
		assertEquals("[3 2 1]", venice.eval(script));
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
	public void test_let_with_fn() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                           " +
				"   (let [x 2]                                 " +
				"        (defn sum [y] (+ y x)))               " +
				"   (sum 4)                                    " +
				")                                             ";
		
		assertEquals(6L, venice.eval(script));
	}

	@Test
	public void test_loop_1_arg() {
		final Venice venice = new Venice();
		
		final String lisp = 
				"(do                                           " +
				"  (defn cnt [n]                               " +
				"     (loop [i 0]                              " +
				"         (if (< i n)                          " +
				"            (recur (inc i))                   " +
				"            i)))                              " +
				"                                              " +
				"   (cnt 6)                                    " +
				")                                             ";

		assertEquals(Long.valueOf(6L), venice.eval(lisp));
	}

	@Test
	public void test_loop_2_args() {
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
				"      (loop [cnt n acc 0]                       " +
				"          (if (zero? cnt)                       " +
				"              acc                               " +
				"              (recur (dec cnt) (+ acc cnt)))))) " +
				"                                                " +
				"   (sum 100000)                                 " +
				")                                               ";

		assertEquals(Long.valueOf(5000050000L), venice.eval(lisp));
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
				"      (loop [cnt n, acc 0]                                     " +
				"          (if (zero? cnt)                                      " +
				"              acc                                              " +
				"              (recur (dec cnt) (+ acc (last (fib cnt))))))))   " +
				"                                                               " +
				"   (sum-fib 10)                                                " +
				")                                                              ";

		assertEquals(Long.valueOf(89L), venice.eval(lisp));
	}
	
	@Test
	public void test_keyword_as_function() {
		final Venice venice = new Venice();
	
		assertEquals("2",   venice.eval("(str (:b {:a 1 :b 2}))"));
		assertEquals("175I", venice.eval("(str (:blue (. :java.awt.Color :PINK)))"));
	}	
	
	@Test
	public void test_keyword_as_function_default() {
		final Venice venice = new Venice();
	
		assertEquals(null,   venice.eval("(:c {:a 1 :b 2})"));
		assertEquals("none", venice.eval("(:c {:a 1 :b 2} :none)"));
	}	
	
	@Test
	public void test_map_as_function() {
		final Venice venice = new Venice();
	
		assertEquals("2",   venice.eval("(str ({:a 1 :b 2} :b))"));
		assertEquals("175I", venice.eval("(str ((. :java.awt.Color :PINK) :blue)))"));
		
		assertEquals("2",   venice.eval("(str ({\"a\" 1 \"b\" 2} \"b\"))"));
	}	

	@Test
	public void test_set_BANG_1() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def x 100)                    \n" +
				"   (set! x 200))                    ";
				
		assertEquals(200L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_2() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def x 100)                    \n" +
				"   (set! x 200)                   \n" +
				"   x)                               ";
				
		assertEquals(200L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_3() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def x 100)                    \n" +
				"   (set! x (inc x)))                ";
				
		assertEquals(101L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_4() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def x 100)                    \n" +
				"   (set! x (inc x))               \n" +
				"   x)                               ";
				
		assertEquals(101L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_dynamic_1() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (set! x 200))                    ";
				
		assertEquals(200L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_dynamic_2() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (set! x 200)                   \n" +
				"   x)                               ";
				
		assertEquals(200L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_dynamic_3() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (set! x (inc x)))                ";
				
		assertEquals(101L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_dynamic_4() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (set! x (inc x))               \n" +
				"   x)                               ";
				
		assertEquals(101L, venice.eval(script));					
	}

	@Test
	public void test_set_BANG_dynamic_5() {
		final Venice venice = new Venice();
			
		final String script =
				"(do                               \n" +
				"   (def-dynamic x 100)            \n" +
				"   (with-out-str                  \n" +
				"      (print x)                   \n" +
				"      (binding [x 200]            \n" +
				"        (print (str \"-\" x))     \n" +
				"        (set! x (inc x))          \n" +
				"        (print (str \"-\" x)))    \n" +
				"      (print (str \"-\" x))))       ";
				
		assertEquals("100-200-201-100", venice.eval(script));					
	}

}
