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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Reader;
import com.github.jlangch.venice.impl.Token;


public class ReaderTest {

	@Test
	public void testTokenize() {	
		final String s = 
				"(do                       \n" +
				"   100                    \n" +
				"   ;comment               \n" +
				"   \"abcdef\"             \n" +
				"   \"\"\"uvwxyz\"\"\"     \n" +
				"   \"\"\"uvw\"xyz\"\"\"   \n" +
				"   (+ 2 3)                \n" +
				")                           ";
		
		final ArrayList<Token> tokens = Reader.tokenize(s,"test");
		assertEquals("(", tokens.get(0).getToken());
		assertEquals("do", tokens.get(1).getToken());
		assertEquals("100", tokens.get(2).getToken());
		assertEquals("\"abcdef\"", tokens.get(3).getToken());
		assertEquals("\"\"\"uvwxyz\"\"\"", tokens.get(4).getToken());
		assertEquals("\"\"\"uvw\"xyz\"\"\"", tokens.get(5).getToken());
		assertEquals("(", tokens.get(6).getToken());
		assertEquals("+", tokens.get(7).getToken());
		assertEquals("2", tokens.get(8).getToken());
		assertEquals("3", tokens.get(9).getToken());
		assertEquals(")", tokens.get(10).getToken());
		assertEquals(")", tokens.get(11).getToken());
	}

}
