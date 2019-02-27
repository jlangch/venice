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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Reader;
import com.github.jlangch.venice.impl.Token;


public class ReaderTest {

	@Test
	public void testMeta() {	
		assertEquals(100L, new Venice().eval("(do (def ^{:a 200} x 100) x)"));
		assertEquals(200L, new Venice().eval("(do (def ^{:a 200} x 100) (:a (meta x)))"));
		assertEquals("(xx yy)", new Venice().eval("(do (def ^{:a '(\"xx\" \"yy\")} x 100) (str (:a (meta x))))"));
		assertEquals(true, new Venice().eval("(do (def ^:private x 100) (:private (meta x))))"));
	}

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

	@Test
	public void testTokenizeTripleQuotedStringEof() {	
		assertThrows(EofException.class, () -> {
			Reader.tokenize("\"\"\"uvwxyz", "test");
		});
	}

	@Test
	public void testTokenizeSingleQuotedStringEol() {	
		assertThrows(ParseError.class, () -> {
			Reader.tokenize("\"uvwxyz", "test");
		});
	}

	@Test
	public void testStringInterpolation_single_simple_value() {	
		assertEquals("100", new Venice().eval("(do (def x 100) \"~{x}\")"));
		assertEquals(" 100", new Venice().eval("(do (def x 100) \" ~{x}\")"));
		assertEquals("100 ", new Venice().eval("(do (def x 100) \"~{x} \")"));
		assertEquals(" 100 ", new Venice().eval("(do (def x 100) \" ~{x} \")"));

		assertEquals("100 200", new Venice().eval("(do (def x 100) (def y 200) \"~{x} ~{y}\")"));
		assertEquals(" 100 200", new Venice().eval("(do (def x 100) (def y 200) \" ~{x} ~{y}\")"));
		assertEquals("100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"~{x} ~{y} \")"));
		assertEquals(" 100 200 ", new Venice().eval("(do (def x 100) (def y 200) \" ~{x} ~{y} \")"));

		assertEquals("100200", new Venice().eval("(do (def x 100) (def y 200) \"~{x}~{y}\")"));
		assertEquals(" 100200", new Venice().eval("(do (def x 100) (def y 200) \" ~{x}~{y}\")"));
		assertEquals("100200 ", new Venice().eval("(do (def x 100) (def y 200) \"~{x}~{y} \")"));
		assertEquals(" 100200 ", new Venice().eval("(do (def x 100) (def y 200) \" ~{x}~{y} \")"));
	}

	@Test
	public void testStringInterpolation_single_expression() {	
		assertEquals("101", new Venice().eval("(do (def x 100) \"~(inc x)\")"));
		assertEquals(" 101", new Venice().eval("(do (def x 100) \" ~(inc x)\")"));
		assertEquals("101 ", new Venice().eval("(do (def x 100) \"~(inc x) \")"));
		assertEquals(" 101 ", new Venice().eval("(do (def x 100) \" ~(inc x) \")"));

		assertEquals("101 99", new Venice().eval("(do (def x 100) \"~(inc x) ~(dec x)\")"));
		assertEquals(" 101 99", new Venice().eval("(do (def x 100) \" ~(inc x) ~(dec x)\")"));
		assertEquals("101 99 ", new Venice().eval("(do (def x 100) \"~(inc x) ~(dec x) \")"));
		assertEquals(" 101 99 ", new Venice().eval("(do (def x 100) \" ~(inc x) ~(dec x) \")"));

		assertEquals("10199", new Venice().eval("(do (def x 100) \"~(inc x)~(dec x)\")"));
		assertEquals(" 10199", new Venice().eval("(do (def x 100) \" ~(inc x)~(dec x)\")"));
		assertEquals("10199 ", new Venice().eval("(do (def x 100) \"~(inc x)~(dec x) \")"));
		assertEquals(" 10199 ", new Venice().eval("(do (def x 100) \" ~(inc x)~(dec x) \")"));
	}

	@Test
	public void testStringInterpolation_single_mixed() {	
		assertEquals("100 101", new Venice().eval("(do (def x 100) \"~{x} ~(inc x)\")"));
		assertEquals(" 100 101", new Venice().eval("(do (def x 100) \" ~{x} ~(inc x)\")"));
		assertEquals("100 101 ", new Venice().eval("(do (def x 100) \"~{x} ~(inc x) \")"));
		assertEquals(" 100 101 ", new Venice().eval("(do (def x 100) \" ~{x} ~(inc x) \")"));

		assertEquals("100101", new Venice().eval("(do (def x 100) \"~{x}~(inc x)\")"));
		assertEquals(" 100101", new Venice().eval("(do (def x 100) \" ~{x}~(inc x)\")"));
		assertEquals("100101 ", new Venice().eval("(do (def x 100) \"~{x}~(inc x) \")"));
		assertEquals(" 100101 ", new Venice().eval("(do (def x 100) \" ~{x}~(inc x) \")"));

		assertEquals("101 100", new Venice().eval("(do (def x 100) \"~(inc x) ~{x}\")"));
		assertEquals(" 101 100", new Venice().eval("(do (def x 100) \" ~(inc x) ~{x}\")"));
		assertEquals("101 100 ", new Venice().eval("(do (def x 100) \"~(inc x) ~{x} \")"));
		assertEquals(" 101 100 ", new Venice().eval("(do (def x 100) \" ~(inc x) ~{x} \")"));

		assertEquals("101100", new Venice().eval("(do (def x 100) \"~(inc x)~{x}\")"));
		assertEquals(" 101100", new Venice().eval("(do (def x 100) \" ~(inc x)~{x}\")"));
		assertEquals("101100 ", new Venice().eval("(do (def x 100) \"~(inc x)~{x} \")"));
		assertEquals(" 101100 ", new Venice().eval("(do (def x 100) \" ~(inc x)~{x} \")"));
	}

	@Test
	public void testStringInterpolation_triple_simple_value() {	
		assertEquals("100", new Venice().eval("(do (def x 100) \"\"\"~{x}\"\"\")"));
		assertEquals(" 100", new Venice().eval("(do (def x 100) \"\"\" ~{x}\"\"\")"));
		assertEquals("100 ", new Venice().eval("(do (def x 100) \"\"\"~{x} \"\"\")"));
		assertEquals(" 100 ", new Venice().eval("(do (def x 100) \"\"\" ~{x} \"\"\")"));

		assertEquals("100 200", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x} ~{y}\"\"\")"));
		assertEquals(" 100 200", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x} ~{y}\"\"\")"));
		assertEquals("100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x} ~{y} \"\"\")"));
		assertEquals(" 100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x} ~{y} \"\"\")"));

		assertEquals("100200", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x}~{y}\"\"\")"));
		assertEquals(" 100200", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x}~{y}\"\"\")"));
		assertEquals("100200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x}~{y} \"\"\")"));
		assertEquals(" 100200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x}~{y} \"\"\")"));
	}

	@Test
	public void testStringInterpolation_triple_expression() {	
		assertEquals("101", new Venice().eval("(do (def x 100) \"\"\"~(inc x)\"\"\")"));
		assertEquals(" 101", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)\"\"\")"));
		assertEquals("101 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) \"\"\")"));
		assertEquals(" 101 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) \"\"\")"));

		assertEquals("101 99", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~(dec x)\"\"\")"));
		assertEquals(" 101 99", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~(dec x)\"\"\")"));
		assertEquals("101 99 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~(dec x) \"\"\")"));
		assertEquals(" 101 99 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~(dec x) \"\"\")"));

		assertEquals("10199", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~(dec x)\"\"\")"));
		assertEquals(" 10199", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~(dec x)\"\"\")"));
		assertEquals("10199 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~(dec x) \"\"\")"));
		assertEquals(" 10199 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~(dec x) \"\"\")"));
	}

	@Test
	public void testStringInterpolation_triple_mixed() {	
		assertEquals("100 101", new Venice().eval("(do (def x 100) \"\"\"~{x} ~(inc x)\"\"\")"));
		assertEquals(" 100 101", new Venice().eval("(do (def x 100) \"\"\" ~{x} ~(inc x)\"\"\")"));
		assertEquals("100 101 ", new Venice().eval("(do (def x 100) \"\"\"~{x} ~(inc x) \"\"\")"));
		assertEquals(" 100 101 ", new Venice().eval("(do (def x 100) \"\"\" ~{x} ~(inc x) \"\"\")"));

		assertEquals("100101", new Venice().eval("(do (def x 100) \"\"\"~{x}~(inc x)\"\"\")"));
		assertEquals(" 100101", new Venice().eval("(do (def x 100) \"\"\" ~{x}~(inc x)\"\"\")"));
		assertEquals("100101 ", new Venice().eval("(do (def x 100) \"\"\"~{x}~(inc x) \"\"\")"));
		assertEquals(" 100101 ", new Venice().eval("(do (def x 100) \"\"\" ~{x}~(inc x) \"\"\")"));

		assertEquals("101 100", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~{x}\"\"\")"));
		assertEquals(" 101 100", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~{x}\"\"\")"));
		assertEquals("101 100 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~{x} \"\"\")"));
		assertEquals(" 101 100 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~{x} \"\"\")"));

		assertEquals("101100", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~{x}\"\"\")"));
		assertEquals(" 101100", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~{x}\"\"\")"));
		assertEquals("101100 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~{x} \"\"\")"));
		assertEquals(" 101100 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~{x} \"\"\")"));
	}

}
