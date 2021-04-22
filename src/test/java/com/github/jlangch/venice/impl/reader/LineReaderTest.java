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
package com.github.jlangch.venice.impl.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


public class LineReaderTest {

	@Test
	public void test_null() {
		final LineReader rd = new LineReader(null);

		assertEquals(0, rd.size());

		assertNull(rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(1, rd.getLineNr());
	}

	@Test
	public void test_one_line_empty() {
		final LineReader rd = new LineReader("");

		assertEquals(1, rd.size());
		
		assertEquals("", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(1, rd.getLineNr());
	}

	@Test
	public void test_one_line() {
		final LineReader rd = new LineReader("1");

		assertEquals(1, rd.size());
		
		assertEquals("1", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(1, rd.getLineNr());
	}

	@Test
	public void test_two_lines_1() {
		final LineReader rd = new LineReader("\n");

		assertEquals(2, rd.size());
		
		assertEquals("", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("", rd.peek());
		assertEquals(2, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(2, rd.getLineNr());
	}

	@Test
	public void test_two_lines_2() {
		final LineReader rd = new LineReader("1\n");

		assertEquals(2, rd.size());
		
		assertEquals("1", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("", rd.peek());
		assertEquals(2, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(2, rd.getLineNr());
	}

	@Test
	public void test_two_lines_3() {
		final LineReader rd = new LineReader("1\n2");

		assertEquals(2, rd.size());
		
		assertEquals("1", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("2", rd.peek());
		assertEquals(2, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(2, rd.getLineNr());
	}

	@Test
	public void test_three_lines_1() {
		final LineReader rd = new LineReader("\n\n");

		assertEquals(3, rd.size());
		
		assertEquals("", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("", rd.peek());
		assertEquals(2, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("", rd.peek());
		assertEquals(3, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(3, rd.getLineNr());
	}

	@Test
	public void test_three_lines_2() {
		final LineReader rd = new LineReader("1\n2\n3");

		assertEquals(3, rd.size());
		
		assertEquals("1", rd.peek());
		assertEquals(1, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("2", rd.peek());
		assertEquals(2, rd.getLineNr());
		
		rd.consume();
		
		assertEquals("3", rd.peek());
		assertEquals(3, rd.getLineNr());
		
		rd.consume();
		
		assertNull(rd.peek());
		assertEquals(3, rd.getLineNr());
	}

}
