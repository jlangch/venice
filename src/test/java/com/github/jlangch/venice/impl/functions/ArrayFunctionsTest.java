/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class ArrayFunctionsTest {

	@Test
	public void test_aset() {
		final Venice venice = new Venice();

		assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 0 20I)"))[0]);
		assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 1 20I)"))[1]);
		assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 4 20I)"))[4]);

		assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 0 20)"))[0]);
		assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 1 20)"))[1]);
		assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 4 20)"))[4]);

		assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 0 20.1)"))[0]);
		assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.1)"))[1]);
		assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 4 20.1)"))[4]);

		assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 0 20.1)"))[0]);
		assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.1)"))[1]);
		assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 4 20.1)"))[4]);

		assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0 \"20\")"))[0]);
		assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1 \"20\")"))[1]);
		assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4 \"20\")"))[4]);

		assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0 \"20\")"))[0]);
		assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1 \"20\")"))[1]);
		assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4 \"20\")"))[4]);
	}

	@Test
	public void test_aget() {
		final Venice venice = new Venice();

		assertEquals(1, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 0)"));
		assertEquals(2, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 1)"));
		assertEquals(5, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 4)"));

		assertEquals(1L, venice.eval("(aget (long-array '(1 2 3 4 5)) 0)"));
		assertEquals(2L, venice.eval("(aget (long-array '(1 2 3 4 5)) 1)"));
		assertEquals(5L, venice.eval("(aget (long-array '(1 2 3 4 5)) 4)"));

		assertEquals(1.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 0)"));
		assertEquals(2.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 1)"));
		assertEquals(5.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 4)"));

		assertEquals(1.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 0)"));
		assertEquals(2.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 1)"));
		assertEquals(5.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 4)"));

		assertEquals("1", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0)"));
		assertEquals("2", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1)"));
		assertEquals("5", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4)"));

		assertEquals("1", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0)"));
		assertEquals("2", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1)"));
		assertEquals("5", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4)"));
	}

	@Test
	public void test_alength() {
		final Venice venice = new Venice();

		assertEquals(0L, venice.eval("(alength (int-array '()))"));
		assertEquals(1L, venice.eval("(alength (int-array '(1I)))"));
		assertEquals(5L, venice.eval("(alength (int-array '(1I 2I 3I 4I 5I)))"));

		assertEquals(0L, venice.eval("(alength (long-array '()))"));
		assertEquals(1L, venice.eval("(alength (long-array '(1)))"));
		assertEquals(5L, venice.eval("(alength (long-array '(1 2 3 4 5)))"));

		assertEquals(0L, venice.eval("(alength (float-array '()))"));
		assertEquals(1L, venice.eval("(alength (float-array '(1.0)))"));
		assertEquals(5L, venice.eval("(alength (float-array '(1.0 2.0 3.0 4.0 5.0)))"));

		assertEquals(0L, venice.eval("(alength (double-array '()))"));
		assertEquals(1L, venice.eval("(alength (double-array '(1.0)))"));
		assertEquals(5L, venice.eval("(alength (double-array '(1.0 2.0 3.0 4.0 5.0)))"));

		assertEquals(0L, venice.eval("(alength (string-array '()))"));
		assertEquals(1L, venice.eval("(alength (string-array '(\"1\")))"));
		assertEquals(5L, venice.eval("(alength (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));

		assertEquals(0L, venice.eval("(alength (object-array '()))"));
		assertEquals(1L, venice.eval("(alength (object-array '(\"1\")))"));
		assertEquals(5L, venice.eval("(alength (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));
	}

	@Test
	public void test_asub() {
		final Venice venice = new Venice();

		assertEquals("[3]",       Arrays.toString(((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 1)"))));
		assertEquals("[3, 4]",    Arrays.toString(((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 2)"))));
		assertEquals("[3, 4, 5]", Arrays.toString(((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 3)"))));

		assertEquals("[3]",       Arrays.toString(((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 1)"))));
		assertEquals("[3, 4]",    Arrays.toString(((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 2)"))));
		assertEquals("[3, 4, 5]", Arrays.toString(((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 3)"))));

		assertEquals("[3.0]",           Arrays.toString(((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 1)"))));
		assertEquals("[3.0, 4.0]",      Arrays.toString(((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 2)"))));
		assertEquals("[3.0, 4.0, 5.0]", Arrays.toString(((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 3)"))));

		assertEquals("[3.0]",           Arrays.toString(((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 1)"))));
		assertEquals("[3.0, 4.0]",      Arrays.toString(((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 2)"))));
		assertEquals("[3.0, 4.0, 5.0]", Arrays.toString(((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 3)"))));

		assertEquals("[c]",       Arrays.toString(((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 1)"))));
		assertEquals("[c, d]",    Arrays.toString(((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 2)"))));
		assertEquals("[c, d, e]", Arrays.toString(((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 3)"))));

		assertEquals("[c]",       Arrays.toString(((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 1)"))));
		assertEquals("[c, d]",    Arrays.toString(((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 2)"))));
		assertEquals("[c, d, e]", Arrays.toString(((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 3)"))));
	}
}
