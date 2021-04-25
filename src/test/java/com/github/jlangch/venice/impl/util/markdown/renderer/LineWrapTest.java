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


public class LineWrapTest {

	@Test
	public void test_hard_wrap_1() {
		assertEquals(0, LineWrap.wrap(null, 1).size());
		
		assertEquals(0, LineWrap.wrap("", 1).size());
		
		assertEquals(1, LineWrap.wrap("1", 1).size());
		assertEquals("1", LineWrap.wrap("1", 1).get(0));
		
		assertEquals(2, LineWrap.wrap("12", 1).size());
		assertEquals("1", LineWrap.wrap("12", 1).get(0));
		assertEquals("2", LineWrap.wrap("12", 1).get(1));
		
		assertEquals(3, LineWrap.wrap("123", 1).size());
		assertEquals("1", LineWrap.wrap("123", 1).get(0));
		assertEquals("2", LineWrap.wrap("123", 1).get(1));
		assertEquals("3", LineWrap.wrap("123", 1).get(2));
		
		assertEquals(4, LineWrap.wrap("1234", 1).size());
		assertEquals("1", LineWrap.wrap("1234", 1).get(0));
		assertEquals("2", LineWrap.wrap("1234", 1).get(1));
		assertEquals("3", LineWrap.wrap("1234", 1).get(2));
		assertEquals("4", LineWrap.wrap("1234", 1).get(3));
	}

	@Test
	public void test_hard_wrap_2() {
		assertEquals(0, LineWrap.wrap(null, 2).size());
		
		assertEquals(0, LineWrap.wrap("", 2).size());
		
		assertEquals(1, LineWrap.wrap("1", 2).size());
		assertEquals("1", LineWrap.wrap("1", 2).get(0));
		
		assertEquals(1, LineWrap.wrap("12", 2).size());
		assertEquals("12", LineWrap.wrap("12", 2).get(0));
		
		assertEquals(2, LineWrap.wrap("123", 2).size());
		assertEquals("12", LineWrap.wrap("123", 2).get(0));
		assertEquals("3", LineWrap.wrap("123", 2).get(1));
		
		assertEquals(2, LineWrap.wrap("1234", 2).size());
		assertEquals("12", LineWrap.wrap("1234", 2).get(0));
		assertEquals("34", LineWrap.wrap("1234", 2).get(1));
	}

	@Test
	public void test_hard_wrap_3() {
		assertEquals(0, LineWrap.wrap(null, 3).size());
		
		assertEquals(0, LineWrap.wrap("", 3).size());
		
		assertEquals(1, LineWrap.wrap("1", 3).size());
		assertEquals("1", LineWrap.wrap("1", 3).get(0));
		
		assertEquals(1, LineWrap.wrap("12", 3).size());
		assertEquals("12", LineWrap.wrap("12", 3).get(0));
		
		assertEquals(1, LineWrap.wrap("123", 3).size());
		assertEquals("123", LineWrap.wrap("123", 3).get(0));
		
		assertEquals(2, LineWrap.wrap("1234", 3).size());
		assertEquals("123", LineWrap.wrap("1234", 3).get(0));
		assertEquals("4", LineWrap.wrap("1234", 3).get(1));
	}

	@Test
	public void test_hard_wrap_4() {
		assertEquals(0, LineWrap.wrap(null, 4).size());
		
		assertEquals(0, LineWrap.wrap("", 4).size());
		
		assertEquals(1, LineWrap.wrap("1", 4).size());
		assertEquals("1", LineWrap.wrap("1", 4).get(0));
		
		assertEquals(1, LineWrap.wrap("12", 4).size());
		assertEquals("12", LineWrap.wrap("12", 4).get(0));
		
		assertEquals(1, LineWrap.wrap("123", 4).size());
		assertEquals("123", LineWrap.wrap("123", 4).get(0));
		
		assertEquals(1, LineWrap.wrap("1234", 4).size());
		assertEquals("1234", LineWrap.wrap("1234", 4).get(0));
	}

	@Test
	public void test_wrap_with_spaces_1() {
		// soft wrap
		assertEquals(1,   LineWrap.wrap(" 1", 4).size());
		assertEquals("1", LineWrap.wrap(" 1", 4).get(0));

		// soft wrap
		assertEquals(1,   LineWrap.wrap("1 ", 4).size());
		assertEquals("1", LineWrap.wrap("1 ", 4).get(0));

		// soft wrap
		assertEquals(1,   LineWrap.wrap("     1", 4).size());
		assertEquals("1", LineWrap.wrap("     1", 4).get(0));

		// soft wrap
		assertEquals(1,   LineWrap.wrap("1     ", 4).size());
		assertEquals("1", LineWrap.wrap("1     ", 4).get(0));
		
		// soft wrap
		assertEquals(1,   LineWrap.wrap(" 1 ", 4).size());
		assertEquals("1", LineWrap.wrap(" 1 ", 4).get(0));
		
		// soft wrap
		assertEquals(1,   LineWrap.wrap("   1   ", 4).size());
		assertEquals("1", LineWrap.wrap("   1   ", 4).get(0));
	}

	@Test
	public void test_wrap_with_spaces_2() {
		// hard wrap
		assertEquals(8,    LineWrap.wrap("12 34 56 78", 1).size());
		assertEquals("1", LineWrap.wrap("12 34 56 78", 1).get(0));
		assertEquals("2", LineWrap.wrap("12 34 56 78", 1).get(1));
		assertEquals("3", LineWrap.wrap("12 34 56 78", 1).get(2));
		assertEquals("4", LineWrap.wrap("12 34 56 78", 1).get(3));
		assertEquals("5", LineWrap.wrap("12 34 56 78", 1).get(4));
		assertEquals("6", LineWrap.wrap("12 34 56 78", 1).get(5));
		assertEquals("7", LineWrap.wrap("12 34 56 78", 1).get(6));
		assertEquals("8", LineWrap.wrap("12 34 56 78", 1).get(7));

		// soft wrap
		assertEquals(4,    LineWrap.wrap("12 34 56 78", 2).size());
		assertEquals("12", LineWrap.wrap("12 34 56 78", 2).get(0));
		assertEquals("34", LineWrap.wrap("12 34 56 78", 2).get(1));
		assertEquals("56", LineWrap.wrap("12 34 56 78", 2).get(2));
		assertEquals("78", LineWrap.wrap("12 34 56 78", 2).get(3));

		// soft wrap
		assertEquals(4,    LineWrap.wrap("12 34 56 78", 3).size());
		assertEquals("12", LineWrap.wrap("12 34 56 78", 3).get(0));
		assertEquals("34", LineWrap.wrap("12 34 56 78", 3).get(1));
		assertEquals("56", LineWrap.wrap("12 34 56 78", 3).get(2));
		assertEquals("78", LineWrap.wrap("12 34 56 78", 3).get(3));

		// soft wrap
		assertEquals(4,    LineWrap.wrap("12 34 56 78", 4).size());
		assertEquals("12", LineWrap.wrap("12 34 56 78", 4).get(0));
		assertEquals("34", LineWrap.wrap("12 34 56 78", 4).get(1));
		assertEquals("56", LineWrap.wrap("12 34 56 78", 4).get(2));
		assertEquals("78", LineWrap.wrap("12 34 56 78", 4).get(3));

		// soft wrap
		assertEquals(2,       LineWrap.wrap("12 34 56 78", 5).size());
		assertEquals("12 34", LineWrap.wrap("12 34 56 78", 5).get(0));
		assertEquals("56 78", LineWrap.wrap("12 34 56 78", 5).get(1));

		// soft wrap
		assertEquals(2,       LineWrap.wrap("12 34 56 78", 6).size());
		assertEquals("12 34", LineWrap.wrap("12 34 56 78", 6).get(0));
		assertEquals("56 78", LineWrap.wrap("12 34 56 78", 6).get(1));

		// soft wrap
		assertEquals(2,       LineWrap.wrap("12 34 56 78", 7).size());
		assertEquals("12 34", LineWrap.wrap("12 34 56 78", 7).get(0));
		assertEquals("56 78", LineWrap.wrap("12 34 56 78", 7).get(1));

		// soft wrap
		assertEquals(2,          LineWrap.wrap("12 34 56 78", 8).size());
		assertEquals("12 34 56", LineWrap.wrap("12 34 56 78", 8).get(0));
		assertEquals("78",       LineWrap.wrap("12 34 56 78", 8).get(1));

		// soft wrap
		assertEquals(2,          LineWrap.wrap("12 34 56 78", 9).size());
		assertEquals("12 34 56", LineWrap.wrap("12 34 56 78", 9).get(0));
		assertEquals("78",       LineWrap.wrap("12 34 56 78", 9).get(1));

		// soft wrap
		assertEquals(2,          LineWrap.wrap("12 34 56 78", 10).size());
		assertEquals("12 34 56", LineWrap.wrap("12 34 56 78", 10).get(0));
		assertEquals("78",       LineWrap.wrap("12 34 56 78", 10).get(1));

		// no wrap
		assertEquals(1,             LineWrap.wrap("12 34 56 78", 11).size());
		assertEquals("12 34 56 78", LineWrap.wrap("12 34 56 78", 11).get(0));

		// no wrap
		assertEquals(1,             LineWrap.wrap("12 34 56 78", 12).size());
		assertEquals("12 34 56 78", LineWrap.wrap("12 34 56 78", 12).get(0));

		// no wrap
		assertEquals(1,             LineWrap.wrap("12 34 56 78", 13).size());
		assertEquals("12 34 56 78", LineWrap.wrap("12 34 56 78", 13).get(0));
	}

	@Test
	public void test_wrap_with_spaces_3() {
		// hard wrap
		assertEquals(8,    LineWrap.wrap("1234 5678", 1).size());
		assertEquals("1", LineWrap.wrap("1234 5678", 1).get(0));
		assertEquals("2", LineWrap.wrap("1234 5678", 1).get(1));
		assertEquals("3", LineWrap.wrap("1234 5678", 1).get(2));
		assertEquals("4", LineWrap.wrap("1234 5678", 1).get(3));
		assertEquals("5", LineWrap.wrap("1234 5678", 1).get(4));
		assertEquals("6", LineWrap.wrap("1234 5678", 1).get(5));
		assertEquals("7", LineWrap.wrap("1234 5678", 1).get(6));
		assertEquals("8", LineWrap.wrap("1234 5678", 1).get(7));

		// hard wrap
		assertEquals(4,    LineWrap.wrap("1234 5678", 2).size());
		assertEquals("12", LineWrap.wrap("1234 5678", 2).get(0));
		assertEquals("34", LineWrap.wrap("1234 5678", 2).get(1));
		assertEquals("56", LineWrap.wrap("1234 5678", 2).get(2));
		assertEquals("78", LineWrap.wrap("1234 5678", 2).get(3));

		// hard/soft wrap
		assertEquals(4,     LineWrap.wrap("1234 5678", 3).size());
		assertEquals("123", LineWrap.wrap("1234 5678", 3).get(0));
		assertEquals("4",   LineWrap.wrap("1234 5678", 3).get(1));
		assertEquals("567", LineWrap.wrap("1234 5678", 3).get(2));
		assertEquals("8",   LineWrap.wrap("1234 5678", 3).get(3));

		// soft wrap
		assertEquals(2,      LineWrap.wrap("1234 5678", 4).size());
		assertEquals("1234", LineWrap.wrap("1234 5678", 4).get(0));
		assertEquals("5678", LineWrap.wrap("1234 5678", 4).get(1));

		// soft wrap
		assertEquals(2,      LineWrap.wrap("1234 5678", 5).size());
		assertEquals("1234", LineWrap.wrap("1234 5678", 5).get(0));
		assertEquals("5678", LineWrap.wrap("1234 5678", 5).get(1));

		// soft wrap
		assertEquals(2,      LineWrap.wrap("1234 5678", 6).size());
		assertEquals("1234", LineWrap.wrap("1234 5678", 6).get(0));
		assertEquals("5678", LineWrap.wrap("1234 5678", 6).get(1));

		// soft wrap
		assertEquals(2,      LineWrap.wrap("1234 5678", 7).size());
		assertEquals("1234", LineWrap.wrap("1234 5678", 7).get(0));
		assertEquals("5678", LineWrap.wrap("1234 5678", 7).get(1));

		// soft wrap
		assertEquals(2,      LineWrap.wrap("1234 5678", 8).size());
		assertEquals("1234", LineWrap.wrap("1234 5678", 8).get(0));
		assertEquals("5678", LineWrap.wrap("1234 5678", 8).get(1));

		// no wrap
		assertEquals(1,           LineWrap.wrap("1234 5678", 9).size());
		assertEquals("1234 5678", LineWrap.wrap("1234 5678", 9).get(0));

		// no wrap
		assertEquals(1,           LineWrap.wrap("1234 5678", 10).size());
		assertEquals("1234 5678", LineWrap.wrap("1234 5678", 10).get(0));

		// no wrap
		assertEquals(1,           LineWrap.wrap("1234 5678", 11).size());
		assertEquals("1234 5678", LineWrap.wrap("1234 5678", 11).get(0));

		// no wrap
		assertEquals(1,           LineWrap.wrap("1234 5678", 12).size());
		assertEquals("1234 5678", LineWrap.wrap("1234 5678", 12).get(0));
	}
	

	@Test
	public void test_large() {
		final String test = 
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed " +
			"diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
			"aliquyam erat, sed diam voluptua. At vero eos et accusam et " +
			"justo duo dolores et ea rebum. Stet clita kasd gubergren, no " +
			"sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem " +
			"ipsum dolor sit amet, consetetur sadipscing elitr, sed diam " +
			"nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam " +
			"erat, sed diam voluptua. At vero eos et accusam et justo duo " +
			"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
			"sanctus est Lorem ipsum dolor sit amet.";
		
		final String[] wrapped = new String[] {
			//-------------------------------------------------v
			"Lorem ipsum dolor sit amet, consetetur sadipscing",
			"elitr, sed diam nonumy eirmod tempor invidunt ut",
			"labore et dolore magna aliquyam erat, sed diam",
			"voluptua. At vero eos et accusam et justo duo",
			"dolores et ea rebum. Stet clita kasd gubergren, no",
			"sea takimata sanctus est Lorem ipsum dolor sit",
			"amet. Lorem ipsum dolor sit amet, consetetur",
			"sadipscing elitr, sed diam nonumy eirmod tempor",
			"invidunt ut labore et dolore magna aliquyam erat,",
			"sed diam voluptua. At vero eos et accusam et justo",
			"duo dolores et ea rebum. Stet clita kasd",
			"gubergren, no sea takimata sanctus est Lorem ipsum",
			"dolor sit amet."
		};

		assertEquals(wrapped.length, LineWrap.wrap(test, 50).size());
		for(int ii=0; ii<wrapped.length; ii++) {
			assertEquals(wrapped[ii], LineWrap.wrap(test, 50).get(ii));
		}
	}
}
