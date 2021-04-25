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
package com.github.jlangch.venice.impl.util.markdown.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;


public class TextTableRendererTest {

	
	// -----------------------------------------------------------------------------
	// Simple table, no wrap
	// -----------------------------------------------------------------------------

	@Test
	public void test_001() {
		final String md5 = 
			"|c1|";
		
		final String expected =
			"c1";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_002() {
		final String md5 = 
			"|c1|c2|";
		
		final String expected =
			"c1  c2";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_003() {
		final String md5 = 
			"|c1|c2|\n" +
			"|d1|d2|";
		
		final String expected =
			"c1  c2\n" +
			"d1  d2";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_004() {
		final String md5 = 
			"|c1..1|c2|\n" +
			"|d1|d2|";
		
		final String expected =
			"c1..1  c2\n" +
			"d1     d2";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_005() {
		final String md5 = 
			"|c1..1|c2|\n" +
			"|d1|d2..2|";
		
		final String expected =
			"c1..1  c2\n" +
			"d1     d2..2";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}

	
	// -----------------------------------------------------------------------------
	// Simple table, align, no wrap
	// -----------------------------------------------------------------------------

	@Test
	public void test_101() {
		final String md5 = 
			"|:-|\n" +
			"|c1|";
		
		final String expected =
			"c1";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_102() {
		final String md5 = 
			"|:-|:-:|-:|\n" +
			"|c1|c2|c3|";
		
		final String expected =
			"c1  c2  c3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_103() {
		final String md5 = 
			"|:-|:-:|-:|\n" +
			"|c1|c2|c3|\n" +
			"|d1|d2|d3|";
		
		final String expected =
			"c1  c2  c3\n" +
			"d1  d2  d3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_104() {
		final String md5 = 
			"|:-|:-:|-:|\n" +
			"|c1...|c2.|c3...|\n" +
			"|d1...|d2.....|d3...|\n" +
			"|e1|e2.|e3|";
		
		final String expected =
			"c1...    c2.    c3...\n" +
			"d1...  d2.....  d3...\n" +
			"e1       e2.       e3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_105() {
		final String md5 = 
			"|:-|:-:|-:|\n" +
			"|c1...|c2.|c3...|\n" +
			"|d1...|d2.....|d3...|\n" +
			"||e2.||";
		
		final String expected =
			"c1...    c2.    c3...\n" +
			"d1...  d2.....  d3...\n" +
			"         e2.";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}


	
	// -----------------------------------------------------------------------------
	// Simple table, title, align, no wrap
	// -----------------------------------------------------------------------------


	@Test
	public void test_201() {
		final String md5 = 
			"|T1|\n" +
			"|:-|\n" +
			"|c1|";
		
		final String expected =
			"T1\n" +
			"--\n" +
			"c1";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_202() {
		final String md5 = 
			"|T1|T2|T3|\n" +
			"|:-|:-:|-:|\n" +
			"|c1|c2|c3|";
		
		final String expected =
			"T1  T2  T3\n" +
			"--  --  --\n" +
			"c1  c2  c3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_203() {
		final String md5 = 
			"|T1...|T2....|T3...|\n" +
			"|:-|:-:|-:|\n" +
			"|c1|c2|c3|\n" +
			"|d1|d2|d3|";
		
		final String expected =
			"T1...  T2....  T3...\n" +
			"-----  ------  -----\n" +
			"c1       c2       c3\n" +
			"d1       d2       d3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_204() {
		final String md5 = 
			"|T1|T2|T3|\n" +
			"|:-|:-:|-:|\n" +
			"|c1...|c2.|c3...|\n" +
			"|d1...|d2.....|d3...|\n" +
			"|e1|e2.|e3|";
		
		final String expected =
			"T1       T2        T3\n" +
			"-----  -------  -----\n" +
			"c1...    c2.    c3...\n" +
			"d1...  d2.....  d3...\n" +
			"e1       e2.       e3";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}
	
	@Test
	public void test_205() {
		final String md5 = 
			"|T1|T2|T3|\n" +
			"|:-|:-:|-:|\n" +
			"|c1...|c2.|c3...|\n" +
			"|d1...|d2.....|d3...|\n" +
			"||e2.||";
		
		final String expected =
			"T1       T2        T3\n" +
			"-----  -------  -----\n" +
			"c1...    c2.    c3...\n" +
			"d1...  d2.....  d3...\n" +
			"         e2.";

		
		final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
		final String rendered = new TextTableRendrer(block, 80).render() ;

		assertEquals(expected, rendered);
	}

}
