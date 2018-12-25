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

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Reader;
import com.github.jlangch.venice.impl.Token;


public class ReaderTest {

	@Test
	public void testAtomLong() {
		assertEquals(Long.valueOf(100), new Venice().eval("(do 100)"));
	}

	@Test
	public void testAtomDouble() {
		assertEquals(Double.valueOf(100.2), new Venice().eval("(do 100.2)"));
	}

	@Test
	public void testAtomDecimal() {
		assertEquals(new BigDecimal("100.123"), new Venice().eval("(do 100.123M)"));
	}

	@Test
	public void testAtomNil() {
		assertEquals(null, new Venice().eval("(do nil)"));
	}

	@Test
	public void testAtomTrue() {
		assertEquals(Boolean.TRUE, new Venice().eval("(do true)"));
	}

	@Test
	public void testAtomFalse() {
		assertEquals(Boolean.FALSE, new Venice().eval("(do false)"));
	}

	@Test
	public void testAtomString() {
		assertEquals("abc", new Venice().eval("(do \"abc\")"));
		assertEquals("a\nb\nc", new Venice().eval("(do \"a\nb\nc\")"));
	}

	@Test
	public void testAtomString_TripleQuotes() {
		assertEquals("abc", new Venice().eval("(do \"\"\"abc\"\"\")"));
		assertEquals("a\"b\"c", new Venice().eval("(do \"\"\"a\"b\"c\"\"\")"));
		assertEquals("a\nb\nc", new Venice().eval("(do \"\"\"a\nb\nc\"\"\")"));
	}

	@Test
	public void testAtomKeyword() {
		assertEquals(":abc", new Venice().eval("(do (str :abc))"));
	}

	@Test
	public void testAtomSymbol() {
		assertEquals(Long.valueOf(100), new Venice().eval("(do (let [abc 100] abc))"));
	}

	@Test
	public void testTokenize() {	
		final String s = 
				"(do                                  \n" +
				"   100                               \n" +
				"   ;comment                          \n" +
				"   \"abcdef\"                        \n" +
				"   \"abc\\\"def\"                    \n" +
				"   \"abc\ndef\"                      \n" +
				"   \"\"\"uvwxyz\"\"\"                \n" +
				"   \"\"\"uvw\"xyz\"\"\"              \n" +
				"   \"\"\"uvw\nxyz\"\"\"              \n" +
				"   \"\"\"uvw\"\"\" \"\"\"xyz\"\"\"   \n" +
				"   (+ 2 3)                           \n" +
				")                                      ";
		
		int pos = 0;
		
		final ArrayList<Token> tokens = Reader.tokenize(s,"test");
		assertEquals("(", tokens.get(pos++).getToken());
		assertEquals("do", tokens.get(pos++).getToken());
		assertEquals("100", tokens.get(pos++).getToken());
		assertEquals("\"abcdef\"", tokens.get(pos++).getToken());
		assertEquals("\"abc\\\"def\"", tokens.get(pos++).getToken());
		assertEquals("\"abc\ndef\"", tokens.get(pos++).getToken());
		assertEquals("\"\"\"uvwxyz\"\"\"", tokens.get(pos++).getToken());
		assertEquals("\"\"\"uvw\"xyz\"\"\"", tokens.get(pos++).getToken());
		assertEquals("\"\"\"uvw\nxyz\"\"\"", tokens.get(pos++).getToken());
		assertEquals("\"\"\"uvw\"\"\"", tokens.get(pos++).getToken());
		assertEquals("\"\"\"xyz\"\"\"", tokens.get(pos++).getToken());
		assertEquals("(", tokens.get(pos++).getToken());
		assertEquals("+", tokens.get(pos++).getToken());
		assertEquals("2", tokens.get(pos++).getToken());
		assertEquals("3", tokens.get(pos++).getToken());
		assertEquals(")", tokens.get(pos++).getToken());
		assertEquals(")", tokens.get(pos++).getToken());
	}

}
