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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class JavaInterop_import_Test {

	@Test
	public void testImport() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"   (import :java.lang.Long)   \n" +
				"   (. :Long :new 10))           ";
		
		assertEquals(10L, venice.eval(script));
	}
	
	@Test
	public void testImport_1a() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"   (import :java.lang.Long)   \n" +
				"   (. (class :Long) :new 10))   ";
		
		assertEquals(10L, venice.eval(script));
	}
	
	@Test
	public void testImport_1b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                     \n" +
				"   (import :java.lang.Long)             \n" +
				"   (. (class :java.lang.Long) :new 10))   ";
		
		assertEquals(10L, venice.eval(script));
	}
	
	@Test
	public void testImport_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"   (import :java.lang.Long)   \n" +
				"   (import :java.lang.Long)   \n" +
				"   (. :Long :new 10))           ";
		
		assertEquals(10L, venice.eval(script));
	}
	
	@Test
	public void testImport_failure() {
		final Venice venice = new Venice();

		final String script =
				"(do                           \n" +
				"   (import :java.lang.Long)   \n" +
				"   (import :foo.some.Long)    \n" +
				"   (. :Long :new 10))           ";

		assertThrows(VncException.class, () -> venice.eval(script));
	}

}