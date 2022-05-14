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

import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.CENTER;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.LEFT;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.RIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


public class TableColFmtParserTest {

	@Test
	public void test_md_format_empty() {
		final TableColFmtParser parser = new TableColFmtParser();
		
		assertNull(parser.parse(null));
		assertNull(parser.parse(""));
		assertNull(parser.parse(" "));
		assertNull(parser.parse("    "));
	}

	@Test
	public void test_md_format_unknown() {
		final TableColFmtParser parser = new TableColFmtParser();
		
		assertNull(parser.parse("x"));
		assertNull(parser.parse(" x "));
		
		assertNull(parser.parse("-"));
		assertNull(parser.parse("--"));

		assertNull(parser.parse(" - "));
		assertNull(parser.parse(" -- "));

		assertNull(parser.parse(":"));
		assertNull(parser.parse("::"));
		assertNull(parser.parse(":::"));
	}

	@Test
	public void test_md_alignment() {
		final TableColFmtParser parser = new TableColFmtParser();
		
		assertEquals(LEFT, parser.parse(":-").horzAlignment());
		assertEquals(LEFT, parser.parse(":--").horzAlignment());
		assertEquals(LEFT, parser.parse(":---").horzAlignment());
		assertEquals(LEFT, parser.parse(":----").horzAlignment());
		
		assertEquals(CENTER, parser.parse(":-:").horzAlignment());
		assertEquals(CENTER, parser.parse(":--:").horzAlignment());
		assertEquals(CENTER, parser.parse(":---:").horzAlignment());
		
		assertEquals(CENTER, parser.parse("---").horzAlignment());
		assertEquals(CENTER, parser.parse("----").horzAlignment());
		assertEquals(CENTER, parser.parse("-----").horzAlignment());
		
		assertEquals(RIGHT, parser.parse("-:").horzAlignment());
		assertEquals(RIGHT, parser.parse("--:").horzAlignment());
		assertEquals(RIGHT, parser.parse("---:").horzAlignment());
		assertEquals(RIGHT, parser.parse("----:").horzAlignment());
	}

	@Test
	public void test_css_alignment() {
		final TableColFmtParser parser = new TableColFmtParser();
		
		assertEquals(LEFT, parser.parse("[![text-align: left]]").horzAlignment());
		assertEquals(LEFT, parser.parse("[![text-align: left;]]").horzAlignment());
		
		assertEquals(CENTER, parser.parse("[![text-align: center]]").horzAlignment());
		assertEquals(CENTER, parser.parse("[![text-align: center;]]").horzAlignment());
		
		assertEquals(RIGHT, parser.parse("[![text-align: right]]").horzAlignment());
		assertEquals(RIGHT, parser.parse("[![text-align: right;]]").horzAlignment());
	}

	@Test
	public void test_css_width() {
		final TableColFmtParser parser = new TableColFmtParser();
		
		parser.parse("[![width: 30%]]");
	}

}
