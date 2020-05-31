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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.ParseError;


public class RegexTokenizerTest {

	@Test
	public void testTokenize_LF() {	
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
		
		final List<Token> tokens = RegexTokenizer.tokenize(s,"test");
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

	@Test
	public void testTokenize_CR_LF() {	
		final String s = 
				"(do                                  \r\n" +
				"   100                               \r\n" +
				"   ;comment                          \r\n" +
				"   \"abcdef\"                        \r\n" +
				"   \"abc\\\"def\"                    \r\n" +
				"   \"abc\ndef\"                      \r\n" +
				"   \"\"\"uvwxyz\"\"\"                \r\n" +
				"   \"\"\"uvw\"xyz\"\"\"              \r\n" +
				"   \"\"\"uvw\nxyz\"\"\"              \r\n" +
				"   \"\"\"uvw\"\"\" \"\"\"xyz\"\"\"   \r\n" +
				"   (+ 2 3)                           \r\n" +
				")                                      ";
		
		int pos = 0;
		
		final List<Token> tokens = RegexTokenizer.tokenize(s,"test");
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

	@Test
	public void testTokenizeTripleQuotedStringEof() {	
		assertThrows(EofException.class, () -> {
			RegexTokenizer.tokenize("\"\"\"uvwxyz", "test");
		});
	}

	@Test
	public void testTokenizeSingleQuotedStringEol() {	
		assertThrows(ParseError.class, () -> {
			RegexTokenizer.tokenize("\"uvwxyz", "test");
		});
	}

	@Test
	public void testEscapedString() {	
		List<Token> tokens = RegexTokenizer.tokenize("\"aaa\"", "test");
		assertEquals(1, tokens.size());
		assertEquals("\"aaa\"", tokens.get(0).getToken());
		
		tokens = RegexTokenizer.tokenize("\\S", "test");
		assertEquals(1, tokens.size());
		assertEquals("\\S", tokens.get(0).getToken());
	}

}
