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
package com.github.jlangch.venice.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;


public class StringUtilTest {

	@Test
	public void testTrimToNull() {
		assertNull(StringUtil.trimToNull(""));
		assertNull(StringUtil.trimToNull(" "));
		assertNull(StringUtil.trimToNull("  "));
		assertNull(StringUtil.trimToNull("\n"));
		assertNull(StringUtil.trimToNull("  \n\n"));
	}

	@Test
	public void testTrimLeft() {
		assertEquals(null, StringUtil.trimLeft(null));
		assertEquals("", StringUtil.trimLeft(""));
		assertEquals("", StringUtil.trimLeft(" "));
		assertEquals("", StringUtil.trimLeft("  "));
		
		assertEquals("a", StringUtil.trimLeft("a"));
		assertEquals("a", StringUtil.trimLeft(" a"));
		assertEquals("a", StringUtil.trimLeft("  a"));
	}

	@Test
	public void testTrimRight() {
		assertEquals(null, StringUtil.trimRight(null));
		assertEquals("", StringUtil.trimRight(""));
		assertEquals("", StringUtil.trimRight(" "));
		assertEquals("", StringUtil.trimRight("  "));
		assertEquals("", StringUtil.trimRight("\n"));
		assertEquals("", StringUtil.trimRight("  \n\n"));
		assertEquals("", StringUtil.trimRight("  \n\r\n"));
		
		assertEquals("a", StringUtil.trimRight("a"));
		assertEquals("a", StringUtil.trimRight("a "));
		assertEquals("a", StringUtil.trimRight("a  "));
		assertEquals("a", StringUtil.trimRight("a\n"));
		assertEquals("a", StringUtil.trimRight("a  \n\n"));
		assertEquals("a", StringUtil.trimRight("a  \n\r\n"));
	}

	@Test
	public void testSplitIntoLines() {
		List<String> lines;

		lines = StringUtil.splitIntoLines("");	
		assertEquals(0, lines.size());

		
		
		lines = StringUtil.splitIntoLines("\n");	
		assertEquals(1, lines.size());
		assertEquals("", lines.get(0));

		
		
		lines = StringUtil.splitIntoLines("1\n");	
		assertEquals(1, lines.size());
		assertEquals("1", lines.get(0));

		lines = StringUtil.splitIntoLines("\n2");	
		assertEquals(2, lines.size());
		assertEquals("", lines.get(0));
		assertEquals("2", lines.get(1));

		lines = StringUtil.splitIntoLines("1\n2");	
		assertEquals(2, lines.size());
		assertEquals("1", lines.get(0));
		assertEquals("2", lines.get(1));

		lines = StringUtil.splitIntoLines("\n\n");	
		assertEquals(2, lines.size());
		assertEquals("", lines.get(0));
		assertEquals("", lines.get(1));

		
		
		lines = StringUtil.splitIntoLines("1\n\n");	
		assertEquals(2, lines.size());
		assertEquals("1", lines.get(0));
		assertEquals("", lines.get(1));

		lines = StringUtil.splitIntoLines("\n2\n");	
		assertEquals(2, lines.size());
		assertEquals("", lines.get(0));
		assertEquals("2", lines.get(1));

		lines = StringUtil.splitIntoLines("\n\n3");	
		assertEquals(3, lines.size());
		assertEquals("", lines.get(0));
		assertEquals("", lines.get(1));
		assertEquals("3", lines.get(2));
	}

}
