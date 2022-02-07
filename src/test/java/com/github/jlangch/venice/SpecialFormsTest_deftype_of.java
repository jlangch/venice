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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_deftype_of {
		
	@Test
	public void test_deftype_of() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                      \n" +
				"  (deftype-of :user/email-address :string)               \n" +
				"  (def x (.: :user/email-address \"foo@foo.org\"))       \n" +
				"  (pr-str x))                                              ";

		assertEquals("\"foo@foo.org\"", venice.eval(script));					
	}
	
	@Test
	public void test_deftype_of_long_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                             \n" +
				"  (deftype-of :my-long :long)   \n" +
				"  (+ 10 (my-long. 20)))           ";

		assertEquals(30L, venice.eval(script));					
	}
	
	@Test
	public void test_deftype_of_long_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                             \n" +
				"  (deftype-of :my-long :long)   \n" +
				"  (+ (my-long. 20) 10))           ";

		assertEquals(30L, venice.eval(script));					
	}
	
	@Test
	public void test_deftype_of_long_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                       \n" +
				"  (deftype-of :my-long-1 :long)           \n" +
				"  (deftype-of :my-long-2 :long)           \n" +
				"                                          \n" +
				"  (+ (my-long-1. 10) (my-long-2. 20)))      ";

		venice.eval(script);					
	}
	
	@Test
	public void test_deftype_of_invalid_type() {
		final String script =
				"(do                                          \n" +
				"  (deftype-of :user/email-address :string)   \n" +
				"  (user/email-address. 200))                   ";
	
		assertThrows(VncException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_deftype_of_invalid_name() {
		final String script =
				"(do                                      \n" +
				"  (deftype-of :email-address. :string))    ";

		assertThrows(VncException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_deftype_of_type_builder() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                 \n" +
				"  (deftype-of :email-address :string)               \n" +
				"  (def x (email-address. \"foo@foo.org\"))          \n" +
				"  (pr-str x))                                         ";

		assertEquals("\"foo@foo.org\"", venice.eval(script));					
	}

	@Test
	public void test_deftype_of_type_checker() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                     \n" +
				"  (deftype-of :email-address :string)                   \n" +
				"  (email-address? (.: :email-address \"foo@foo.org\")))   ";

		assertTrue((Boolean)venice.eval(script));					
	}

	@Test
	public void test_deftype_of_type() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                      \n" +
				"  (deftype-of :user/email-address :string)               \n" +
				"  (def x (.: :user/email-address \"foo@foo.org\"))       \n" +
				"  (pr-str (type x)))                                       ";

		assertEquals(":user/email-address", venice.eval(script));					
	}
	
	@Test
	public void test_deftype_of_supertype() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                      \n" +
				"  (deftype-of :user/email-address :string)               \n" +
				"  (def x (.: :user/email-address \"foo@foo.org\"))       \n" +
				"  (pr-str (supertype x)))                                  ";

		assertEquals(":core/string", venice.eval(script));					
	}

	@Test
	public void test_deftype_of_validation_OK_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                            \n" +
				"  (deftype-of :user/email-address                              \n" +
				"              :string                                          \n" +
				"              (fn [e] (assert (str/valid-email-addr? e)        \n" +
				"                              \"invalid email address\")))     \n" +
				"  (def x (.: :user/email-address \"foo@foo.org\"))             \n" +
				"  (pr-str x))                                                    ";

		assertEquals("\"foo@foo.org\"", venice.eval(script));					
	}

	@Test
	public void test_deftype_of_validation_OK_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                            \n" +
				"  (deftype-of :user/email-address                              \n" +
				"              :string                                          \n" +
				"              str/valid-email-addr?)                           \n" +
				"  (def x (.: :user/email-address \"foo@foo.org\"))             \n" +
				"  (pr-str x))                                                    ";

		assertEquals("\"foo@foo.org\"", venice.eval(script));					
	}

	@Test
	public void test_deftype_of_validation_FAILED_1() {
		final String script =
				"(do                                                            \n" +
				"  (deftype-of :user/email-address                              \n" +
				"              :string                                          \n" +
				"              (fn [e] (assert (str/valid-email-addr? e)        \n" +
				"                              \"invalid email address\")))     \n" +
				"  (def x (.: :user/email-address \"..foo@foo.org\"))           \n" +
				"  (pr-str x))                                                    ";

		assertThrows(AssertionException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_deftype_of_validation_FAILED_2() {
		final String script =
				"(do                                                            \n" +
				"  (deftype-of :user/email-address                              \n" +
				"              :string                                          \n" +
				"              str/valid-email-addr?)                           \n" +
				"  (def x (.: :user/email-address \"..foo@foo.org\"))           \n" +
				"  (pr-str x))                                                    ";

		assertThrows(AssertionException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_deftype_of_ns_hijack_1() {
		final String script =
				"(do                                         \n" +
				"  (ns foo)                                  \n" +
				"  (deftype-of :user/email-address :string))   ";

		assertThrows(VncException.class, () -> new Venice().eval(script));
	}
	
	@Test
	public void test_deftype_of_ns_hijack_2() {
		final String script =
				"(do                                         \n" +
				"  (ns foo)                                  \n" +
				"  (deftype-of :core/email-address :string))   ";

		assertThrows(VncException.class, () -> new Venice().eval(script));
	}
	
	@Test
	public void test_deftype_of_equality() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                                      \n" +
				"  (deftype-of :email-address :string str/valid-email-addr?)                              \n" +
				"  (assert (email-address. \"foo@foo.org\") (email-address. \"foo@foo.org\")))            \n" +
				"  (assert (not (= (email-address. \"foo@foo.org\") (email-address. \"boo@foo.org\"))))   \n" +
				"  (assert (not (= (email-address. \"foo@foo.org\") 100)))                                \n" +
				"  nil)";

		venice.eval(script);					
	}
	
	@Test
	public void test_deftype_of_equality_strict() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                                        \n" +
				"  (deftype-of :email-address :string str/valid-email-addr?)                                \n" +
				"  (assert (== (email-address. \"foo@foo.org\") (email-address. \"foo@foo.org\")))          \n" +
				"  (assert (not (== (email-address. \"foo@foo.org\") (email-address. \"boo@foo.org\"))))    \n" +
				"  (assert (not (== (email-address. \"foo@foo.org\") 100)))                                 \n" +
				"  nil)";

		venice.eval(script);					
	}
}
