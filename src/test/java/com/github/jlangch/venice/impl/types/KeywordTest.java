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
package com.github.jlangch.venice.impl.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class KeywordTest {

	@Test
	public void test_toString() {
		assertEquals(":alpha", new VncKeyword("alpha").toString());
		assertEquals(":xx/alpha", new VncKeyword("xx/alpha").toString());

		assertEquals(":alpha", new VncKeyword(":alpha").toString());
		assertEquals(":xx/alpha", new VncKeyword(":xx/alpha").toString());

		assertEquals(":alpha", new VncKeyword("alpha").toString(true));
		assertEquals(":xx/alpha", new VncKeyword("xx/alpha").toString(true));

		assertEquals(":alpha", new VncKeyword(":alpha").toString(true));
		assertEquals(":xx/alpha", new VncKeyword(":xx/alpha").toString(true));
	}

	@Test
	public void test_convertToJavaObject() {
		assertEquals("alpha", new VncKeyword("alpha").convertToJavaObject());
		assertEquals("xx/alpha", new VncKeyword("xx/alpha").convertToJavaObject());
		
		assertEquals("alpha", new VncKeyword(":alpha").convertToJavaObject());
		assertEquals("xx/alpha", new VncKeyword(":xx/alpha").convertToJavaObject());
	}

	@Test
	public void test_WithoutNamespace() {
		assertEquals("alpha", new VncKeyword("alpha").getValue());
		assertEquals("alpha", new VncKeyword("alpha").getQualifiedName());
		assertEquals("alpha", new VncKeyword("alpha").getSimpleName());
		assertEquals(null, new VncKeyword("alpha").getNamespace());
		assertFalse(new VncKeyword("alpha").hasNamespace());

		assertEquals("alpha", new VncKeyword(":alpha").getValue());
		assertEquals("alpha", new VncKeyword(":alpha").getQualifiedName());
		assertEquals("alpha", new VncKeyword(":alpha").getSimpleName());
		assertEquals(null, new VncKeyword(":alpha").getNamespace());
		assertFalse(new VncKeyword(":alpha").hasNamespace());
	}

	@Test
	public void test_WithNamespace() {
		assertEquals("xx/alpha", new VncKeyword("xx/alpha").getValue());
		assertEquals("xx/alpha", new VncKeyword("xx/alpha").getQualifiedName());
		assertEquals("alpha", new VncKeyword("xx/alpha").getSimpleName());
		assertEquals("xx", new VncKeyword("xx/alpha").getNamespace());
		assertTrue(new VncKeyword("xx/alpha").hasNamespace());

		assertEquals("xx/alpha", new VncKeyword(":xx/alpha").getValue());
		assertEquals("xx/alpha", new VncKeyword(":xx/alpha").getQualifiedName());
		assertEquals("alpha", new VncKeyword(":xx/alpha").getSimpleName());
		assertEquals("xx", new VncKeyword(":xx/alpha").getNamespace());
		assertTrue(new VncKeyword(":xx/alpha").hasNamespace());
	}

	@Test
	public void test_KeywordAsFunction() {
		final Venice venice = new Venice();

		// maps
		assertEquals("2", venice.eval("(pr-str (:a {:a 2 :b 3}))"));		
		assertEquals("nil", venice.eval("(pr-str (:c {:a 2 :b 3}))"));
		
		// maps with default
		assertEquals("2", venice.eval("(pr-str (:a {:a 2 :b 3} 5))"));
		assertEquals("5", venice.eval("(pr-str (:c {:a 2 :b 3} 5))"));

		// sets
		assertEquals(":a", venice.eval("(pr-str (:a #{:a :b}))"));		
		assertEquals("nil", venice.eval("(pr-str (:c #{:a :b}))"));
		
		// sets with default
		assertEquals(":a", venice.eval("(pr-str (:a #{:a :b } :e))"));
		assertEquals(":e", venice.eval("(pr-str (:c #{:a :b} :e))"));
	}
}
