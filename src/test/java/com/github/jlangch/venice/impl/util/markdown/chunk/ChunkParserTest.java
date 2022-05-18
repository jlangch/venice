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
package com.github.jlangch.venice.impl.util.markdown.chunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class ChunkParserTest {
	
	// -----------------------------------------------------------------------------
	// EmptyChunk
	// -----------------------------------------------------------------------------

	@Test
	public void test_empty_chunk_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new EmptyChunk())).parse();
		
		assertEquals(0, chunks.size());
	}

	@Test
	public void test_empty_chunk_2() {
		final Chunks chunks = new ChunkParser(
									new Chunks()
										.add(new EmptyChunk())
										.add(new EmptyChunk())).parse();
		
		assertEquals(0, chunks.size());
	}
	
	
	// -----------------------------------------------------------------------------
	// TextChunk
	// -----------------------------------------------------------------------------
	
	@Test
	public void test_text_chunk_1() {
		Chunks chunks = new ChunkParser(new Chunks().add(new TextChunk(""))).parse();
		
		assertEquals(0, chunks.size());


		chunks = new ChunkParser(new Chunks().add(new TextChunk("*abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}
	
	@Test
	public void test_text_chunk_2() {
		Chunks chunks = new ChunkParser(new Chunks()
											.add(new TextChunk("*abc*"))
											.add(new TextChunk("*def*"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("*def*", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_text_chunk_3() {
		Chunks chunks = new ChunkParser(new Chunks().add(new TextChunk("*ab  c*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*ab c*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		
		chunks = new ChunkParser(new Chunks().add(new TextChunk("*a  b  c*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*a b c*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		
		chunks = new ChunkParser(new Chunks().add(new TextChunk("*a  \t  b  \t  c*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*a b c*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}
	
	
	// -----------------------------------------------------------------------------
	// RawChunk single
	// -----------------------------------------------------------------------------

	@Test
	public void test_single_text_empty() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk(""))).parse();
		
		assertEquals(0, chunks.size());
	}

	@Test
	public void test_single_text_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("a"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("a", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_2() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_inline_code_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("`abc`"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof InlineCodeChunk);
		assertEquals("abc", ((InlineCodeChunk)chunks.getChunks().get(0)).getText());
	}

	@Test
	public void test_single_text_inline_code_open() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("`abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("`abc", ((TextChunk)chunks.getChunks().get(0)).getText());
	}

	@Test
	public void test_single_text_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("*abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_italic_2_open() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("*abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("**abc**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("**abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("**abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("***abc***"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("***abc"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("***abc*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_single_text_bold_italic_1_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("***abc**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc**", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_url_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("[Google](http://google.com)"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof UrlChunk);
		assertEquals("Google", ((UrlChunk)chunks.getChunks().get(0)).getCaption());
		assertEquals("http://google.com", ((UrlChunk)chunks.getChunks().get(0)).getUrl());
	}

	@Test
	public void test_url_2() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("[Google](https://google.com)"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof UrlChunk);
		assertEquals("Google", ((UrlChunk)chunks.getChunks().get(0)).getCaption());
		assertEquals("https://google.com", ((UrlChunk)chunks.getChunks().get(0)).getUrl());
	}

	@Test
	public void test_url_3() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("[Google](#1234)"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof UrlChunk);
		assertEquals("Google", ((UrlChunk)chunks.getChunks().get(0)).getCaption());
		assertEquals("#1234", ((UrlChunk)chunks.getChunks().get(0)).getUrl());
	}

	
	
	// -----------------------------------------------------------------------------
	// RawChunk multiple
	// -----------------------------------------------------------------------------

	@Test
	public void test_multiple_text_empty() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(""))
													.add(new RawChunk(""))).parse();
		
		assertEquals(0, chunks.size());
	}

	@Test
	public void test_multiple_text_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("a"))
													.add(new RawChunk(""))
													.add(new RawChunk("b"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("a", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("b", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("abc"))
													.add(new RawChunk(""))
													.add(new RawChunk("def"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_inline_code_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("`abc`"))
													.add(new RawChunk("`def`"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof InlineCodeChunk);
		assertEquals("abc", ((InlineCodeChunk)chunks.getChunks().get(0)).getText());
		
		assertTrue(chunks.getChunks().get(1) instanceof InlineCodeChunk);
		assertEquals("def", ((InlineCodeChunk)chunks.getChunks().get(1)).getText());
	}

	@Test
	public void test_multiple_text_inline_code_open() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("`abc"))
													.add(new RawChunk("`def"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("`abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("`def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_inline_code_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("`abc`def"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof InlineCodeChunk);
		assertEquals("abc", ((InlineCodeChunk)chunks.getChunks().get(0)).getText());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_inline_code_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("`abc`def`"))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof InlineCodeChunk);
		assertEquals("abc", ((InlineCodeChunk)chunks.getChunks().get(0)).getText());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals("`", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

	@Test
	public void test_multiple_text_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("*abc*"))
													.add(new RawChunk("*def*"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_italic_2_open() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("*abc"))
													.add(new RawChunk("*def"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("*abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("*def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_italic_2_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("*abc*def"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_italic_2_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("*abc**def*"))).parse();
		
		assertEquals(1, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc**def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("**abc**"))
													.add(new RawChunk("**def**"))).parse();
		
		assertEquals(2, chunks.size());

		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(0)).getFormat());

		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("**abc*def"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc*def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("**abc*def*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("**abc*def*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_1_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("**abc*def**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc*def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_italic_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc***"))
													.add(new RawChunk("***def***"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("def", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(1)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_italic_1_open_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc*def"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc*def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_italic_1_open_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc*def*"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc*def*", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_italic_1_open_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc**def**"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("***abc**def**", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_bold_italic_1_open_4() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc**def***"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc**def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}
	
	
	// -----------------------------------------------------------------------------
	// RawChunk mixed
	// -----------------------------------------------------------------------------

	@Test
	public void test_multiple_text_mixed_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc**def***"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc**def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_mixed_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("***abc`xxx`def***"))).parse();
		
		assertEquals(1, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("abc`xxx`def", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_multiple_text_mixed_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(" `abc*xxx*def` "))).parse();
		
		assertEquals(3, chunks.size());
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(0)).getText());

		assertTrue(chunks.getChunks().get(1) instanceof InlineCodeChunk);
		assertEquals("abc*xxx*def", ((InlineCodeChunk)chunks.getChunks().get(1)).getText());

		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(2)).getText());
	}

	@Test
	public void test_multiple_text_mixed_4() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("a `abc*xxx*def` b"))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("a ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());

		assertTrue(chunks.getChunks().get(1) instanceof InlineCodeChunk);
		assertEquals("abc*xxx*def", ((InlineCodeChunk)chunks.getChunks().get(1)).getText());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" b", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

	@Test
	public void test_url_mixed_1() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("[Google1](http://google1.com)[Google2](http://google2.com)"))).parse();
		
		assertEquals(2, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof UrlChunk);
		assertEquals("Google1", ((UrlChunk)chunks.getChunks().get(0)).getCaption());
		assertEquals("http://google1.com", ((UrlChunk)chunks.getChunks().get(0)).getUrl());
		
		assertTrue(chunks.getChunks().get(1) instanceof UrlChunk);
		assertEquals("Google2", ((UrlChunk)chunks.getChunks().get(1)).getCaption());
		assertEquals("http://google2.com", ((UrlChunk)chunks.getChunks().get(1)).getUrl());
	}

	@Test
	public void test_url_mixed_2() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("[Google1](http://google1.com) --- [Google2](http://google2.com)"))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof UrlChunk);
		assertEquals("Google1", ((UrlChunk)chunks.getChunks().get(0)).getCaption());
		assertEquals("http://google1.com", ((UrlChunk)chunks.getChunks().get(0)).getUrl());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals(" --- ", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof UrlChunk);
		assertEquals("Google2", ((UrlChunk)chunks.getChunks().get(2)).getCaption());
		assertEquals("http://google2.com", ((UrlChunk)chunks.getChunks().get(2)).getUrl());
	}

	@Test
	public void test_url_mixed_3() {
		final Chunks chunks = new ChunkParser(new Chunks().add(new RawChunk("-[Google1](http://google1.com) --- [Google2](http://google2.com)-"))).parse();
		
		assertEquals(5, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals("-", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof UrlChunk);
		assertEquals("Google1", ((UrlChunk)chunks.getChunks().get(1)).getCaption());
		assertEquals("http://google1.com", ((UrlChunk)chunks.getChunks().get(1)).getUrl());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" --- ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
		
		assertTrue(chunks.getChunks().get(3) instanceof UrlChunk);
		assertEquals("Google2", ((UrlChunk)chunks.getChunks().get(3)).getCaption());
		assertEquals("http://google2.com", ((UrlChunk)chunks.getChunks().get(3)).getUrl());
		
		assertTrue(chunks.getChunks().get(4) instanceof TextChunk);
		assertEquals("-", ((TextChunk)chunks.getChunks().get(4)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(4)).getFormat());
	}

	
	// -----------------------------------------------------------------------------
	// Collapse whitespaces
	// -----------------------------------------------------------------------------

	@Test
	public void test_collapse_whitespaces_1() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(" a    b "))).parse();
		
		assertEquals(1, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" a b ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_2() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(" a \tb "))).parse();
		
		assertEquals(1, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" a b ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_3() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("\t a\t \tb \t "))).parse();
		
		assertEquals(1, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" a b ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_4() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("\t *a\t \tb* \t "))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("a b", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_5() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("\t **a\t \tb** \t "))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("a b", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.BOLD, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_6() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk("\t ***a\t \tb*** \t "))).parse();
		
		assertEquals(3, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("a b", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_7() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(" a ***b   c*** d  *e* "))).parse();
		
		assertEquals(5, chunks.size());
		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" a ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());
		
		assertTrue(chunks.getChunks().get(1) instanceof TextChunk);
		assertEquals("b c", ((TextChunk)chunks.getChunks().get(1)).getText());
		assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)chunks.getChunks().get(1)).getFormat());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" d ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
		
		assertTrue(chunks.getChunks().get(3) instanceof TextChunk);
		assertEquals("e", ((TextChunk)chunks.getChunks().get(3)).getText());
		assertEquals(TextChunk.Format.ITALIC, ((TextChunk)chunks.getChunks().get(3)).getFormat());
		
		assertTrue(chunks.getChunks().get(4) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(4)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(4)).getFormat());
	}

	@Test
	public void test_collapse_whitespaces_8() {
		final Chunks chunks = new ChunkParser(new Chunks()
													.add(new RawChunk(" `a   b` "))).parse();
		
		assertEquals(3, chunks.size());

		
		assertTrue(chunks.getChunks().get(0) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(0)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(0)).getFormat());

		assertTrue(chunks.getChunks().get(1) instanceof InlineCodeChunk);
		assertEquals("a b", ((InlineCodeChunk)chunks.getChunks().get(1)).getText());
		
		assertTrue(chunks.getChunks().get(2) instanceof TextChunk);
		assertEquals(" ", ((TextChunk)chunks.getChunks().get(2)).getText());
		assertEquals(TextChunk.Format.NORMAL, ((TextChunk)chunks.getChunks().get(2)).getFormat());
	}

}
