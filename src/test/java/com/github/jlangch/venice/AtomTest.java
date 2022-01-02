/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class AtomTest {

	@Test
	public void testLongCreate() {
		final Venice venice = new Venice();

		final String s = 
				"(do                            \n" +
				"   (def counter (atom 0))      \n" +
				"   (deref counter)             \n" +
				")                              ";
	
		assertEquals("0", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testLongReset() {
		final Venice venice = new Venice();

		final String s = 
				"(do                            " +
				"   (def counter (atom 0))      " +
				"	(reset! counter 99)         " +
				"   (deref counter)             " +
				")                              ";
	
		assertEquals("99", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testLongSwap() {
		final Venice venice = new Venice();

		final String s1 = 
				"(do                                        " +
				"   (def counter (atom 2))                  " +
				"	(swap! counter inc)                     " +
				"   (deref counter)                         " +
				")                                          ";
	
		assertEquals("3", venice.eval("(str " + s1 + ")"));

		final String s2 = 
				"(do                                        " +
				"   (def counter (atom 2))                  " +
				"	(swap! counter (fn [n] (+ n 1)))        " +
				"   (deref counter)                         " +
				")                                          ";
	
		assertEquals("3", venice.eval("(str " + s2 + ")"));
	}

	@Test
	public void testLongCompareAndSet() {
		final Venice venice = new Venice();

		final String s1 = 
				"(do                                        " +
				"   (def counter (atom 2))                  " +
				"	(compare-and-set! counter 2 4)          " +
				")                                          ";
	
		assertTrue((Boolean)venice.eval(s1));

		final String s2 = 
				"(do                                        " +
				"   (def counter (atom 2))                  " +
				"	(compare-and-set! counter 0 4)          " +
				")                                          ";
	
		assertFalse((Boolean)venice.eval(s2));
	}


	@Test
	public void testHashMapCreate() {
		final Venice venice = new Venice();

		final String s = 
				"(do                            " +
				"   (def counter (atom {}))     " +
				"   (deref counter)             " +
				")                              ";
	
		assertEquals("{}", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testHashMapReset() {
		final Venice venice = new Venice();

		final String s = 
				"(do                            " +
				"   (def counter (atom {}))     " +
				"	(reset! counter {:a 1})     " +
				"   (deref counter)             " +
				")                              ";
	
		assertEquals("{:a 1}", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testHashMapSwap() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                        " +
				"   (def counter (atom {}))                 " +
				"	(swap! counter assoc :b 2)              " +
				"   (deref counter)                         " +
				")                                          ";
	
		assertEquals("{:b 2}", venice.eval("(str " + s + ")"));
	}

	@Test
	public void testDeref() {
		final Venice venice = new Venice();

		final String s = 
				"(do                            \n" +
				"   (def counter (atom 0))      \n" +
				"   @counter                    \n" +
				")                              ";
	
		assertEquals("0", venice.eval("(str " + s + ")"));
	}

	@Test
	public void test_watch() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                     \n" +
				"   (def counter (atom 2))                                               \n" +
				"   (defn watcher [key ref old new]                                      \n" +
				"         (println \"watcher: \" key \", old:\" old \", new:\" new ))    \n" +
				"   (add-watch counter :test watcher)                                    \n" +
				"	(swap! counter (fn [n] (+ n 1)))                                     \n" +
				"   (deref counter)                                                      \n" +
				")                                                                         ";

		final Object result = venice.eval(script);
		
		assertEquals(Long.valueOf(3), result);
	}
}
