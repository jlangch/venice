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
package com.github.jlangch.venice.impl.util.markdown.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class TitleBlockParserTest {

	@Test
	public void test_title_block_H1() {
		final String md = "# H1";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof TitleBlock);
		assertEquals("H1", ((TitleBlock)blocks.get(0)).getText());
		assertEquals(1, ((TitleBlock)blocks.get(0)).getLevel());
	}

	@Test
	public void test_title_block_H2() {
		final String md = "## H2";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof TitleBlock);
		assertEquals("H2", ((TitleBlock)blocks.get(0)).getText());
		assertEquals(2, ((TitleBlock)blocks.get(0)).getLevel());
	}

	@Test
	public void test_title_block_H3() {
		final String md = "### H3";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof TitleBlock);
		assertEquals("H3", ((TitleBlock)blocks.get(0)).getText());
		assertEquals(3, ((TitleBlock)blocks.get(0)).getLevel());
	}

	@Test
	public void test_title_block_H4() {
		final String md = "#### H4";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof TitleBlock);
		assertEquals("H4", ((TitleBlock)blocks.get(0)).getText());
		assertEquals(4, ((TitleBlock)blocks.get(0)).getLevel());
	}

	@Test
	public void test_title_block_no() {
		final String md = "####";
		
		Blocks blocks = new BlockParser(md).parse();
		
		assertEquals(1, blocks.size());
		assertTrue(blocks.get(0) instanceof TextBlock);
		
		TextBlock text = (TextBlock)blocks.get(0); 

		assertEquals(1, text.getChunks().size());
		assertEquals("####", ((TextChunk)text.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text.getChunks().get(0)).getFormat());
	}

}
