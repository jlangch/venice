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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.types.VncString;


public class JavaFunctionsTest {

	@Test
	public void test_class() {
		final Venice venice = new Venice();

		assertEquals(
				java.util.ArrayList.class, 
				venice.eval("(class :java.util.ArrayList)"));
	}

	@Test
	public void test_class_of() {
		final Venice venice = new Venice();

		assertEquals(
				com.github.jlangch.venice.impl.types.VncLong.class, 
				venice.eval("(class-of 100)"));

		assertEquals(
				com.github.jlangch.venice.impl.types.collections.VncJavaList.class, 
				venice.eval("(class-of (. :java.util.ArrayList :new))"));

		assertEquals(
				java.awt.Point.class, 
				venice.eval("(class-of (. :java.awt.Point :new 10 10))"));

		assertEquals(
				java.lang.Class.class, 
				venice.eval("(class-of (class :java.awt.Point))"));

		assertEquals(
				java.lang.Class.class, 
				venice.eval("(class-of (class-of (class :java.awt.Point)))"));
	}

	@Test
	public void test_class_name() {
		final Venice venice = new Venice();

		assertEquals(
				"com.github.jlangch.venice.impl.types.VncLong", 
				venice.eval("(class-name (class-of 100))"));

		assertEquals(
				"com.github.jlangch.venice.impl.types.collections.VncJavaList", 
				venice.eval("(class-name (class-of (. :java.util.ArrayList :new)))"));

		assertEquals(
				"java.awt.Point", 
				venice.eval("(class-name (class-of (. :java.awt.Point :new 10 10)))"));
	}

	@Test
	public void test_classloader() {
		final Venice venice = new Venice();

		// the classloader result depends on the Java VM
		
		assertNotNull(
				venice.eval("(classloader)"));

		assertNotNull(
				venice.eval("(classloader :system)"));

		assertNotNull(
				venice.eval("(classloader :application)"));

		assertNotNull(
				venice.eval("(classloader :thread-context)"));
	}

	@Test
	public void test_classloader_of() {
		final Venice venice = new Venice();

		assertEquals(
				java.awt.Point.class.getClassLoader(), 
				venice.eval("(classloader-of (class :java.awt.Point))"));

		assertEquals(
				java.awt.Point.class.getClassLoader(), 
				venice.eval("(classloader-of (class-of (. :java.awt.Point :new 10 10)))"));

		assertEquals(
				VncString.class.getClassLoader(), 
				venice.eval("(classloader-of (class-of \"abcdef\"))"));

		assertEquals(
				VncString.class.getClassLoader(), 
				venice.eval("(classloader-of \"abcdef\")"));
	}

	@Test
	public void test_java_unwrap_optional() {
		final Venice venice = new Venice();

		assertEquals(
				"123", 
				venice.eval("(java-unwrap-optional s)", Parameters.of("s", Optional.of("123"))));

		assertEquals(
				null, 
				venice.eval("(java-unwrap-optional s)", Parameters.of("s", Optional.empty())));
	}
}

