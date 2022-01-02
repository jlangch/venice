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
package com.github.jlangch.venice.impl.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SymbolTest {

	@Test
	public void test_toString() {
		assertEquals("alpha", new VncSymbol("alpha").toString());
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").toString());

		assertEquals("alpha", new VncSymbol("alpha").toString(true));
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").toString(true));
	}

	@Test
	public void test_convertToJavaObject() {
		assertEquals("alpha", new VncSymbol("alpha").convertToJavaObject());
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").convertToJavaObject());
	}

	@Test
	public void test_WithoutNamespace() {
		assertEquals("alpha", new VncSymbol("alpha").getName());
		assertEquals("alpha", new VncSymbol("alpha").getValue());
		assertEquals("alpha", new VncSymbol("alpha").getQualifiedName());
		assertEquals("alpha", new VncSymbol("alpha").getSimpleName());
		assertEquals(null, new VncSymbol("alpha").getNamespace());
		assertFalse(new VncSymbol("alpha").hasNamespace());
	}

	@Test
	public void test_WithNamespace() {
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").getName());
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").getValue());
		assertEquals("xx/alpha", new VncSymbol("xx/alpha").getQualifiedName());
		assertEquals("alpha", new VncSymbol("xx/alpha").getSimpleName());
		assertEquals("xx", new VncSymbol("xx/alpha").getNamespace());
		assertTrue(new VncSymbol("xx/alpha").hasNamespace());
	}
}
