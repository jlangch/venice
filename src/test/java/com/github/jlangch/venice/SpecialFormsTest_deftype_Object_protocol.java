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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_deftype_Object_protocol {
	
	@Test
	public void test_Object_protocol_toString() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                               \n" +
				"  (deftype :complex [real :long, imaginary :long]                 \n" +
				"     Object                                                       \n" + 
			    "       (toString [self] (let [re (:real self)                     \n" +
				"                              im (:imaginary self)]               \n" +
			    "                          (str/format \"(%s %s i%s)\"             \n" +
			    "                                      re                          \n" +
			    "                                      (if (neg? im) \"-\" \"+\")  \n" +
			    "                                      im))))                      \n" +
				"  (def x (complex. 1 2))                                          \n" +
				"  (pr-str x))                                                       ";

		assertEquals("(1 + i2)", venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_compareTo() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (compareTo [self other] (. (:x self) :compareTo (:x other))))        \n" +
				"  (pr-str (sort [(point. 2 100) (point. 3 101) (point. 1 102)])))           ";

		assertEquals(
				"[{:custom-type* :user/point :x 1 :y 102}" +
				" {:custom-type* :user/point :x 2 :y 100}" +
				" {:custom-type* :user/point :x 3 :y 101}]", 
				venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_toString_and_compareTo() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (toString [self] (str/format \"[%d,%d]\" (:x self) (:y self)))       \n" +
			    "       (compareTo [self other] (. (:x self) :compareTo (:x other))))        \n" +
				"  (pr-str (sort [(point. 2 100) (point. 3 101) (point. 1 102)])))           ";

		assertEquals("[[1,102] [2,100] [3,101]]", venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_sort_without_compareTo() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (toString [self] (str/format \"[%d,%d]\" (:x self) (:y self))))      \n" +
				"  (pr-str (sort [(point. 2 100) (point. 3 101) (point. 1 102)])))           ";

		assertEquals("[[1,102] [3,101] [2,100]]", venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_sort_equals_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (toString [self] (str/format \"[%d,%d]\" (:x self) (:y self))))      \n" +
				"  (= (point. 1 100) (point. 1 100)))                                          ";

		assertTrue((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_sort_equals_2a() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (toString [self] (str/format \"[%d,%d]\" (:x self) (:y self))))      \n" +
				"  (= (point. 2 100) (point. 1 100)))                                          ";

		assertFalse((Boolean)venice.eval(script));					
	}
	
	@Test
	public void test_Object_protocol_sort_equals_2b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                         \n" +
				"  (deftype :point [x :long, y :long]                                        \n" +
				"     Object                                                                 \n" + 
			    "       (toString [self] (str/format \"[%d,%d]\" (:x self) (:y self))))      \n" +
				"  (= (point. 1 101) (point. 1 100)))                                          ";

		assertFalse((Boolean)venice.eval(script));					
	}
}
