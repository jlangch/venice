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
package com.github.jlangch.venice.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class NamespaceTest {

	@Test
	public void test_ns_1() {
		final Venice venice = new Venice();

		assertEquals("user", venice.eval("*ns*"));
	}

	@Test
	public void test_ns_2() {
		final Venice venice = new Venice();

		assertEquals("B", venice.eval("(do (ns A) (ns B) *ns*)"));
	}

	@Test
	public void test_namespace_symbol() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(namespace 'foo)"));
		assertEquals("xxx", venice.eval("(namespace 'xxx/foo)"));
	}

	@Test
	public void test_namespace_keyword() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(namespace :alpha)"));
		assertEquals("user", venice.eval("(namespace :user/alpha)"));
	}

	@Test
	public void test_namespace_function() {
		final Venice venice = new Venice();

		final String script =
				"(do                         \n" +
				"   (ns xxx)                 \n" +
				"   (defn f1 [x] (+ x 1))    \n" +
				"   (namespace f1))            ";

		assertEquals("xxx", venice.eval(script));
	}

	@Test
	public void test_def() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                   \n" +
				"   (ns A)                                             \n" +
				"                                                      \n" +
				"   (def s1 1)                                         \n" +
				"   (def s2 s1)                                        \n" +
				"   (defn f1 [x] (+ x s1 s2))                          \n" +
				"   (defn f2 [x] (+ x (f1 x)))                         \n" +
				"   (defn f3 [x] (+ x ((resolve (symbol \"f1\")) x)))  \n" +
				"                                                      \n" +
				"   (ns B)                                             \n" +
				"                                                      \n" +
				"   (str [(A/f1 100) (A/f2 100) (A/f3 100)])           \n" +
				")";

		assertEquals("[102 202 202]", venice.eval(script));
	}

	@Test
	public void test_defmulti() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                             \n" +
				"   (ns A)                                                       \n" +
				"                                                                \n" +
				"	(defmulti math-op (fn [s] (:op s)))                          \n" +
				"                                                                \n" +
				"	(defmethod math-op \"add\" [s] (+ (:op1 s) (:op2 s)))        \n" +
				"	(defmethod math-op \"subtract\" [s] (- (:op1 s) (:op2 s)))   \n" +
				"	(defmethod math-op :default [s] 0)                           \n" +
				"                                                                \n" +
				"   (ns B)                                                       \n" +
				"                                                                \n" +
				"   (str                                                         \n" +
				"	   [ (A/math-op {:op \"add\"      :op1 1 :op2 5})            \n" +
				"	     (A/math-op {:op \"subtract\" :op1 1 :op2 5})            \n" +
				"	     (A/math-op {:op \"bogus\"    :op1 1 :op2 5}) ] ))       \n" +
				")                                                                 ";

		assertEquals("[6 -4 0]", venice.eval(script));
	}

	@Test
	public void test_import() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                   \n" +
				"   (ns A)                                             \n" +
				"                                                      \n" +
				"   (import :java.lang.Long)                           \n" +
				"                                                      \n" +
				"   (defn f1 [x] (. :Long :new x))                     \n" +
				"   (defn f2 [x] (+ x (f1 x)))                         \n" +
				"                                                      \n" +
				"   (ns B)                                             \n" +
				"                                                      \n" +
				"   (str [(A/f1 100) (A/f2 100)])                      \n" +
				")";

		assertEquals("[100 200]", venice.eval(script));
	}
}
