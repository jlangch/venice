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
package com.github.jlangch.venice.impl.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.util.StopWatch;


public class HighlighterTest {

	@Test
	public void test_highlight_core() {
		final String core = ModuleLoader.loadModule("core");
		final StopWatch sw = new StopWatch();
		final List<HighlightItem> items = Highlighter.highlight("(do\n" + core + "\n)");
		System.out.println("Highlighting :core module with Highlighter: " + sw.stop().toString());
		assertTrue(!items.isEmpty());
		
		final String core_ = items.subList(3, items.size()-2)
								  .stream()
								  .map(i -> i.getForm())
								  .collect(Collectors.joining());
		
		assertEquals(core.length(), core_.length());
		assertEquals(core, core_);
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
//				System.out.println(String.format("Diff: %d/%d   '%c' :: '%c'", line, col, u, v));
//				break;
//			}
//		}
//	}
}
