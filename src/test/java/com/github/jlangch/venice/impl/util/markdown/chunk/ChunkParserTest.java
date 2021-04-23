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
package com.github.jlangch.venice.impl.util.markdown.chunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class ChunkParserTest {
	
	@Test
	public void test_empty_chunk() {
		final Chunks chunks = new ChunkParser(new Chunks(new EmptyChunk())).parse();
		
		assertEquals(0, chunks.size());
	}
	
	@Test
	public void test_text_chunk() {
		Chunks chunks = new ChunkParser(new Chunks(new TextChunk(""))).parse();
		
		assertEquals(0, chunks.size());


		chunks = new ChunkParser(new Chunks(new TextChunk("*abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_empty() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk(""))).parse();
		
		assertEquals(0, chunks.size());
	}

	@Test
	public void test_single_text_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("a"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("a", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_2() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_inline_code_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("`abc`"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof InlineCodeChunk);
		assertEquals("abc", ((InlineCodeChunk)chunks.getChunks().get(0)).getText());
	}

	@Test
	public void test_single_text_inline_code_open() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("`abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("`abc", ((TextChunk)chunks.getChunks().get(0)).getText());
	}

	@Test
	public void test_single_text_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("*abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_italic_2_open() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("*abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("**abc**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("**abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("**abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("***abc***"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("***abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("***abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks(new RawChunk("***abc**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc**", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

}
