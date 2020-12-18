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


public class Recursion_Loop_Recur_Test {
	
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
		
		// single loop expression
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
	public void test_loop_deep2() {
		final Venice venice = new Venice();
		
		// multiple loop expression
		final String lisp = 
				"(do                                             " +
				"  (def sum                                      " +
				"    (fn [n]                                     " +
				"      (loop [cnt n acc 0]                       " +
				"          10                                    " +
				"          20                                    " +
				"          (if (zero? cnt)                       " +
				"              acc                               " +
				"              (recur (dec cnt) (+ acc cnt)))))) " +
				"                                                " +
				"   (sum 100000)                                 " +
				")                                               ";

		assertEquals(Long.valueOf(5000050000L), venice.eval(lisp));
	}

	@Test
	public void test_loop_deep3() {
		final Venice venice = new Venice();
		
		// multiple loop expression, if expressions reversed
		final String lisp = 
				"(do                                             " +
				"  (def sum                                      " +
				"    (fn [n]                                     " +
				"      (loop [cnt n acc 0]                       " +
				"          10                                    " +
				"          20                                    " +
				"          (if (not (zero? cnt))                 " +
				"              (recur (dec cnt) (+ acc cnt))     " +
				"              acc))))                           " +
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
	public void test_loop_case_1() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]                   \n" +
				"   (case (> x 1)               \n" +
				"      false  0                 \n" +
				"      true   (recur (- x 2)))) \n";

		assertEquals(Long.valueOf(0L), venice.eval(s));
	}

	@Test
	public void test_loop_case_2() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]                   \n" +
				"   (case (> x 1)               \n" +
				"      true   (recur (- x 2))   \n" +
				"      false  0 ))                ";

		assertEquals(Long.valueOf(0L), venice.eval(s));
	}

	@Test
	public void test_loop_not_tail_loop() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]           \n" +
				"   (when (> x 1)       \n" +
				"      (recur (- x 2))) \n" +
				"   67)                   ";

		assertThrows(NotInTailPositionException.class, () -> venice.eval(s));
	}

	@Test
	public void test_loop_not_tail_when() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]           \n" +
				"   (when (> x 1)       \n" +
				"      (recur (- x 2))  \n" +
				"      67))               ";

		assertThrows(NotInTailPositionException.class, () -> venice.eval(s));
	}

	@Test
	public void test_loop_not_tail_let() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]              \n" +
				"   (when (> x 1)          \n" +
				"      (let [a 1]          \n" +
				"         (recur (- x 2))  \n" +
				"         67)))              ";

		assertThrows(NotInTailPositionException.class, () -> venice.eval(s));
	}

	@Test
	public void test_loop_not_tail_do() {
		final Venice venice = new Venice();
		
		final String s = 
				"(loop [x 10]              \n" +
				"   (when (> x 1)          \n" +
				"      (do                 \n" +
				"         (recur (- x 2))  \n" +
				"         67)))              ";

		assertThrows(NotInTailPositionException.class, () -> venice.eval(s));
	}

}
