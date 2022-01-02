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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_deftypeQ {
	
	@Test
	public void test_deftypeQ_deftype() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype :complex [real :long, imaginary :long])  \n" +
				"  (deftype? :complex))                                ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_deftypeQ_deftype_value_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype :complex [real :long, imaginary :long])  \n" +
				"  (def x (.: :complex 100 200))                     \n" +
				"  (deftype? (type x)))                                ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_deftypeQ_deftype_value_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype :complex [real :long, imaginary :long])  \n" +
				"  (def x (.: :complex 100 200))                     \n" +
				"  (deftype? x))                                       ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_deftypeQ_deftype_of() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype-of :email-address :string)               \n" +
				"  (deftype? :email-address))                          ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_deftypeQ_deftype_of_value_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype-of :email-address :string)               \n" +
				"  (def x (.: :email-address \"foo@foo.org\"))       \n" +
				"  (deftype? (type x)))                                ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_deftypeQ_deftype_of_value_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (ns foo)                                          \n" +
				"  (deftype-of :email-address :string)               \n" +
				"  (def x (.: :email-address \"foo@foo.org\"))       \n" +
				"  (deftype? x))                                       ";

		assertTrue((Boolean)venice.eval(script));					
	}
}
