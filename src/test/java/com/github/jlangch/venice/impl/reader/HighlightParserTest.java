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
package com.github.jlangch.venice.impl.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.impl.util.StringUtil;


public class HighlightParserTest {

	@Test
	public void test_empty() {	
		final List<HighlightItem> items = HighlightParser.parse("");
		assertEquals(0, items.size());
	}

	@Test
	public void test_whitespaces() {	
		List<HighlightItem> items = HighlightParser.parse(" \t \r ");
		assertEquals(1, items.size());

		items = HighlightParser.parse(" , \t , \r , ");
		assertEquals(7, items.size());
	}

	@Test
	public void test_braces() {	
		List<HighlightItem> items = HighlightParser.parse("(");
		assertEquals(1, items.size());

		items = HighlightParser.parse("((");
		assertEquals(2, items.size());

		items = HighlightParser.parse("({)");
		assertEquals(3, items.size());

		items = HighlightParser.parse("(({{)");
		assertEquals(5, items.size());
	}

	@Test
	public void test_single_quoted_string_unbalanced() {	
		List<HighlightItem> items = HighlightParser.parse("\"");
		assertEquals(1, items.size());
		assertEquals("\"", items.get(0).getForm());
		assertEquals(HighlightClass.STRING, items.get(0).getClazz());
		
		items = HighlightParser.parse("\"a");
		assertEquals(1, items.size());
		assertEquals("\"a", items.get(0).getForm());
		assertEquals(HighlightClass.STRING, items.get(0).getClazz());
	}

	@Test
	public void test_triple_quoted_string_unbalanced() {	
		List<HighlightItem> items = HighlightParser.parse("\"\"\"");
		assertEquals(1, items.size());
		assertEquals("\"\"\"", items.get(0).getForm());
		assertEquals(HighlightClass.STRING, items.get(0).getClazz());
		
		items = HighlightParser.parse("\"\"\"a");
		assertEquals(1, items.size());
		assertEquals("\"\"\"a", items.get(0).getForm());
		assertEquals(HighlightClass.STRING, items.get(0).getClazz());
	}
	
	@Test
	public void test_unprocessed_input() {
		// no unprocessed chars
		List<HighlightItem> items = HighlightParser.parse("(+ 1 2)");
		assertEquals(7, items.size());

		// only unprocessed whitespaces -> OK
		items = HighlightParser.parse("(+ 1 2)  ");
		assertEquals(8, items.size());

		// only unprocessed whitespaces -> OK
		items = HighlightParser.parse("(+ 1 2) \n ");
		assertEquals(8, items.size());

		items = HighlightParser.parse("(+ 1 2)  ,");
		assertEquals(9, items.size());

		items = HighlightParser.parse("(+ 1 2)  , ");
		assertEquals(10, items.size());
		
		
		// cases with unprocessed input ...
		
		items = HighlightParser.parse("(+ 1 2)  (+ 3 4)");
		assertEquals(9, items.size());
		assertEquals(HighlightClass.UNPROCESSED, items.get(8).getClazz());
		assertEquals("(+ 3 4)", items.get(8).getForm());

		items = HighlightParser.parse("(+ 1 2),(+ 3 4)");
		assertEquals(9, items.size());
		assertEquals(HighlightClass.UNPROCESSED, items.get(8).getClazz());
		assertEquals("(+ 3 4)", items.get(8).getForm());

		items = HighlightParser.parse("(+ 1 2) , (+ 3 4)");
		assertEquals(11, items.size());
		assertEquals(HighlightClass.UNPROCESSED, items.get(10).getClazz());
		assertEquals("(+ 3 4)", items.get(10).getForm());

		items = HighlightParser.parse("(+ 1 2)\n(+ 3 4)");
		assertEquals(9, items.size());
		assertEquals(HighlightClass.UNPROCESSED, items.get(8).getClazz());
		assertEquals("(+ 3 4)", items.get(8).getForm());

		items = HighlightParser.parse("(+ 1 2) \n (+ 3 4)");
		assertEquals(9, items.size());
		assertEquals(HighlightClass.UNPROCESSED, items.get(8).getClazz());
		assertEquals("(+ 3 4)", items.get(8).getForm());
	}

	@Test
	public void test_core() {
		final String core = ModuleLoader.loadModule("core");
		final String core_ = "(do\n" + core + "\n)";
		final long lines = StringUtil.splitIntoLines(core_).size();

		final StopWatch sw = new StopWatch();
		final List<HighlightItem> items = HighlightParser.parse(core_);
		sw.stop();
		
		System.out.println(String.format(
				"Highlighting :core module in %s at %d lines/s",
				sw.toString(),
				(lines * 1000L) / sw.elapsedMillis()));
		
		final String joined = items.subList(3, items.size()-2)
								  .stream()
								  .map(i -> i.getForm())
								  .collect(Collectors.joining());
		
		assertEquals(core.length(), joined.length());
		assertEquals(core, joined);
	}

//	private void diff(final String s1, final String s2) {
//		int line = 1;
//		int col = 1;
//		for(int ii=0; ii<Math.min(s1.length(), s2.length()); ii++) {
//			final char u = s1.charAt(ii);
//			final char v = s2.charAt(ii);
//			
//			if (u == '\n') {
//				line++;
//				col = 1;
//			}
//			else {
//				col++;
//			}
//			
//			if (u != v) {
//				for(int jj=-3; jj<=3; jj++) System.out.print("'" + s1.charAt(ii+jj) + "', ");
//				System.out.println();
//				for(int jj=-3; jj<=3; jj++) System.out.print("'" + s2.charAt(ii+jj) + "', ");
//				System.out.println();
//				System.out.println(String.format("Diff: line=%d / col=%d   '%c' :: '%c'", line, col, u, v));
//				break;
//			}
//		}
//	}
}
