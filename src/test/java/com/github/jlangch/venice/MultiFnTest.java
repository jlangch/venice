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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class MultiFnTest {

	@Test
	public void test_defmulti() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                  \n" +
				"   (defmulti math (fn [op _ _] op))  \n" +
				")                                      ";
	
		assertEquals("multi-fn math", venice.eval("(str " + s + ")"));
	}

	@Test
	public void test_defmethod() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                     \n" +
				"   (defmulti math (fn [op _ _] op))     \n" +
				"   (defmethod math :+ [_ x y] (+ x y))  \n" +
				"   (defmethod math :- [_ x y] (- x y))  \n" +
				"                                        \n" +
				"   (math :+ 3 5)                        \n" +
				")                                      ";
	
		assertEquals(8L, venice.eval(s));
	}

	@Test
	public void test_defmethod_default() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                             \n" +
				"   (defmulti math (fn [op _ _] op))             \n" +
				"   (defmethod math :+ [_ x y] (+ x y))          \n" +
				"   (defmethod math :- [_ x y] (- x y))          \n" +
				"   (defmethod math :default [_ x y] (str x y))  \n" +
				"                                                \n" +
				"   (math :* 3 5)                                \n" +
				")                                               ";
	
		assertEquals("35", venice.eval(s));
	}

	@Test
	public void test_defmethod_1() {
		final Venice venice = new Venice();

		final String s_en = 
				"(do                                                             \n" +
				"   (import :java.lang.IllegalArgumentException)                 \n" +
				"                                                                \n" +
				"   (defmulti greeting (fn[x] (x \"language\")))                 \n" +
				"                                                                \n" +
				"   (defmethod greeting \"English\" [params] \"Hello!\")         \n" +
				"   (defmethod greeting \"French\" [params] \"Bonjour!\")        \n" +
				"   (defmethod greeting :default [params]                        \n" +
				"                 (throw (. :IllegalArgumentException            \n" +
				"                           :new                                 \n" +
				"                           \"unknow language\")))               \n" +
				"                                                                \n" +
				"   (def english-map {\"id\" \"1\", \"language\" \"English\"})   \n" +
				"   (def  french-map {\"id\" \"2\", \"language\" \"French\"})    \n" +
				"   (def spanish-map {\"id\" \"3\", \"language\" \"Spanish\"})   \n" +
				"                                                                \n" +
				"   (greeting english-map)                                       \n" +
				")                                                                 ";

		final String s_fr = 
				"(do                                                             \n" +
				"   (import :java.lang.IllegalArgumentException)                 \n" +
				"                                                                \n" +
				"   (defmulti greeting (fn[x] (x \"language\")))                 \n" +
				"                                                                \n" +
				"   (defmethod greeting \"English\" [params] \"Hello!\")         \n" +
				"   (defmethod greeting \"French\" [params] \"Bonjour!\")        \n" +
				"   (defmethod greeting :default [params]                        \n" +
				"                 (throw (. :IllegalArgumentException            \n" +
				"                           :new                                 \n" +
				"                           \"unknow language\")))               \n" +
				"                                                                \n" +
				"   (def english-map {\"id\" \"1\", \"language\" \"English\"})   \n" +
				"   (def  french-map {\"id\" \"2\", \"language\" \"French\"})    \n" +
				"   (def spanish-map {\"id\" \"3\", \"language\" \"Spanish\"})   \n" +
				"                                                                \n" +
				"   (greeting french-map)                                        \n" +
				")                                                                 ";

		final String s_sp = 
				"(do                                                             \n" +
				"   (import :java.lang.IllegalArgumentException)                 \n" +
				"                                                                \n" +
				"   (defmulti greeting (fn[x] (x \"language\")))                 \n" +
				"                                                                \n" +
				"   (defmethod greeting \"English\" [params] \"Hello!\")         \n" +
				"   (defmethod greeting \"French\" [params] \"Bonjour!\")        \n" +
				"   (defmethod greeting :default [params]                        \n" +
				"                 (throw (. :IllegalArgumentException            \n" +
				"                           :new                                 \n" +
				"                           \"unknow language\")))               \n" +
				"                                                                \n" +
				"   (def english-map {\"id\" \"1\", \"language\" \"English\"})   \n" +
				"   (def  french-map {\"id\" \"2\", \"language\" \"French\"})    \n" +
				"   (def spanish-map {\"id\" \"3\", \"language\" \"Spanish\"})   \n" +
				"                                                                \n" +
				"   (greeting spanish-map)                                       \n" +
				")                                                                 ";

		assertEquals("Hello!", venice.eval(s_en));
		assertEquals("Bonjour!", venice.eval(s_fr));	
		assertThrows(IllegalArgumentException.class, () -> venice.eval(s_sp));
	}

	@Test
	public void test_defmethod_2() {
		final Venice venice = new Venice();

		final String s = 
				"(do                                                                       \n" +
				"   ;;defmulti with dispatch function                                      \n" +
				"   (defmulti salary (fn[amount] (amount :t)))                             \n" +
				"                                                                          \n" +
				"   ;;defmethod provides a function implementation for a particular value  \n" +
				"   (defmethod salary \"com\" [amount] (+ (:b amount) (/ (:b amount) 2)))  \n" +
				"   (defmethod salary \"bon\" [amount] (+ (:b amount) 99))                 \n" +
				"   (defmethod salary :default  [amount] (:b amount))                      \n" +
				"                                                                          \n" +
				"   [(salary {:t \"com\" :b 1000})                                         \n" +
				"    (salary {:t \"bon\" :b 1000})                                         \n" +
				"    (salary {:t \"xxx\" :b 1000})]                                        \n" +
				")                                                                           ";
	
		assertEquals("[1500 1099 1000]", venice.eval("(str " + s + ")"));
	}
}
