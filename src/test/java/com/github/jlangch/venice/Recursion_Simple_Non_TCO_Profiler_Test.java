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


public class Recursion_Simple_Non_TCO_Profiler_Test {
	
	@Test
	public void test_recursive_profiler() {
		final Venice venice = new Venice();
		
		// (fib 4)
		//
		// (+ (fib 3)                         (fib 2))
		// (+ (+ (fib 2)             (fib 1)) (+ (fib 1) (fib 0)))
		// (+ (+ (+ (fib 1) (fib 0)) 1)       (+ 1       0))
		// (+ (+ (+  1      0)       1)       (+ 1       0))
		//
		// => 3

		final String lisp = 
				"(do                                                 \n" +
				"  (defn fib [n]                                     \n" +
				"    (if (< n 2)                                     \n" +
				"      n                                             \n" +
				"      (+ (fib (- n 1)) (fib (- n 2)))))             \n" +
				"                                                    \n" +
				"  (perf (fib 4) 0 1)                                \n" +
				"  (->> (prof :data)                                 \n" +
				"       (filter (fn [x] (= (:name x) \"user/fib\"))) \n" +
				"       (map #(:count %))                            \n" +
				"       (first)))                                      ";

		assertEquals(Long.valueOf(9L), venice.eval(lisp));
	}

}
