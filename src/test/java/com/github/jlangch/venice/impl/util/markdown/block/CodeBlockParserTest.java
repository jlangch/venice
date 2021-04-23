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
package com.github.jlangch.venice.impl.util.markdown.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class CodeBlockParserTest {

	@Test
	public void test_code_block() {
		final String md = "```\n" +
						  "line1\n" +
						  "line2\n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof CodeBlock);
		assertEquals(2, ((CodeBlock)blocks.get(0)).size());
		assertEquals("line1", ((CodeBlock)blocks.get(0)).get(0));
		assertEquals("line2", ((CodeBlock)blocks.get(0)).get(1));
	}
	
	@Test
	public void test_code_block_empty() {
		final String md = "```\n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(0, blocks.size());
	}
	
	@Test
	public void test_code_block_empty_line() {
		final String md = "```\n" +
				  		  "   \n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof CodeBlock);
		assertEquals(1, ((CodeBlock)blocks.get(0)).size());
		assertEquals("", ((CodeBlock)blocks.get(0)).get(0));
	}
	
	@Test
	public void test_code_block_with_whitespaces() {
		final String md = "```\n" +
						  " \n" +
						  "line1\n" +
						  "  line2  \n" +
						  "    line3  \n" +
						  " \n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof CodeBlock);
		assertEquals(5, ((CodeBlock)blocks.get(0)).size());
		assertEquals("", ((CodeBlock)blocks.get(0)).get(0));
		assertEquals("line1", ((CodeBlock)blocks.get(0)).get(1));
		assertEquals("  line2", ((CodeBlock)blocks.get(0)).get(2));
		assertEquals("    line3", ((CodeBlock)blocks.get(0)).get(3));
		assertEquals("", ((CodeBlock)blocks.get(0)).get(4));
	}
	
	@Test
	public void test_code_block_language() {
		final String md = "```java\n" +
						  "line1\n" +
						  "line2\n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof CodeBlock);
		assertEquals("java", ((CodeBlock)blocks.get(0)).getLanguage());
		assertEquals(2, ((CodeBlock)blocks.get(0)).size());
		assertEquals("line1", ((CodeBlock)blocks.get(0)).get(0));
		assertEquals("line2", ((CodeBlock)blocks.get(0)).get(1));
	}
	
	@Test
	public void test_code_block_language_2() {
		final String md = "``` java \n" +
						  "line1\n" +
						  "line2\n" +
						  "```";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof CodeBlock);
		assertEquals("java", ((CodeBlock)blocks.get(0)).getLanguage());
		assertEquals(2, ((CodeBlock)blocks.get(0)).size());
		assertEquals("line1", ((CodeBlock)blocks.get(0)).get(0));
		assertEquals("line2", ((CodeBlock)blocks.get(0)).get(1));
	}
	
}
