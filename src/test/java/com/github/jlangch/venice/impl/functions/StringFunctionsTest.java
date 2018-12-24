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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class StringFunctionsTest {
	@Test
	public void test_str_char() {
		final Venice venice = new Venice();
		
		assertEquals("A", venice.eval("(str/char 65)"));
	}

	@Test
	public void test_str_contains() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(str/contains? \"abcdef\" \"f\")"));
		assertFalse((Boolean)venice.eval("(str/contains? \"abcdef\" \"x\")"));
	}

	@Test
	public void test_str_ends_with() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(str/ends-with? \"abcdef\" \"def\")"));
		assertFalse((Boolean)venice.eval("(str/ends-with? \"abcdef\" \"x\")"));
	}

	@Test
	public void test_str_format() {
		final Venice venice = new Venice();

		assertEquals(":        ab:",   venice.eval("(str/format \":%10s:\" \"ab\")"));
		assertEquals(":ab        :",   venice.eval("(str/format \":%-10s:\" \"ab\")"));
		assertEquals("1.4500",  venice.eval("(str/format \"%.4f\" 1.45)"));
		assertEquals("0012",  venice.eval("(str/format \"%04d\" 12)"));
		assertEquals("0012::0034",  venice.eval("(str/format \"%04d::%04d\" 12 34)"));
	}

	@Test
	public void test_str_index_of() {
		final Venice venice = new Venice();

		assertEquals(0L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\")"));
		assertEquals(6L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ef\")"));
		assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\")"));

		assertEquals(0L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\" 0)"));
		assertEquals(3L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\" 1)"));
		assertEquals(6L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ef\" 3)"));
		assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\" 5)"));
		assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\" 99)"));
	}

	@Test
	public void test_str_join() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str/join \"\" '())"));
		assertEquals("", venice.eval("(str/join \"-\" '())"));

		assertEquals("ab", venice.eval("(str/join \"\" '(\"ab\"))"));
		assertEquals("ab", venice.eval("(str/join \"-\" '(\"ab\"))"));

		assertEquals("abcdef", venice.eval("(str/join \"\" '(\"ab\" \"cd\" \"ef\"))"));
		assertEquals("ab-cd-ef", venice.eval("(str/join \"-\" '(\"ab\" \"cd\" \"ef\"))"));
	}

	@Test
	public void test_str_last_index_of() {
		final Venice venice = new Venice();

		assertEquals(3L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\")"));
		assertEquals(6L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ef\")"));
		assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\")"));

		assertEquals(0L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 0)"));
		assertEquals(3L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 99)"));
		assertEquals(0L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 1)"));
		assertEquals(null, venice.eval("(str/last-index-of \"ab:ab:ef\" \"ef\" 3)"));
		assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\" 5)"));
		assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\" 99)"));
	}

	@Test
	public void test_str_lower_case() {
		final Venice venice = new Venice();
		
		assertEquals("abcdef", venice.eval("(str/lower-case \"abcdef\")"));
		assertEquals("abcdef", venice.eval("(str/lower-case \"aBcDeF\")"));
	}

	@Test
	public void test_str_quote() {
		final Venice venice = new Venice();

		assertEquals("|abc|", venice.eval("(str/quote \"abc\" \"|\")"));
		assertEquals("<abc>", venice.eval("(str/quote \"abc\" \"<\" \">\")"));
	}

	@Test
	public void test_str_repeat() {
		final Venice venice = new Venice();

		assertEquals("", venice.eval("(str/repeat \"abc\" 0)"));
		assertEquals("abcabcabc", venice.eval("(str/repeat \"abc\" 3)"));
		assertEquals("abc-abc-abc", venice.eval("(str/repeat \"abc\" 3  \"-\")"));
	}

	@Test
	public void test_str_replace_all() {
		final Venice venice = new Venice();

		assertEquals("ab:ab:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"**\" \"xy\"))"));
		assertEquals("xy:xy:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"xy\"))"));
		assertEquals("xy:xy:xy", venice.eval("(str (str/replace-all \"ab:ab:ab\" \"ab\" \"xy\"))"));
		assertEquals("ab:cd:xy", venice.eval("(str (str/replace-all \"ab:cd:ef\" \"ef\" \"xy\"))"));
		assertEquals("ab:xy:ef", venice.eval("(str (str/replace-all \"ab:cd:ef\" \"cd\" \"xy\"))"));
	}

	@Test
	public void test_str_replace_first() {
		final Venice venice = new Venice();

		assertEquals("ab:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"**\" \"xy\"))"));
		assertEquals("xy:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xy\"))"));
		assertEquals("ab:cd:xy", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"ef\" \"xy\"))"));
		assertEquals("ab:xy:ef", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"cd\" \"xy\"))"));
	}

	@Test
	public void test_str_replace_last() {
		final Venice venice = new Venice();

		assertEquals("ab:ab:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"**\" \"xy\"))"));
		assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"ab\" \"xy\"))"));
		assertEquals("ab:cd:xy", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"ef\" \"xy\"))"));
		assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"cd\" \"xy\"))"));
	}

	@Test
	public void test_str_split() {
		final Venice venice = new Venice();

		assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" \":\"))"));
		assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" \" *: *\"))"));
		assertEquals("(ab:cd:ef)", venice.eval("(str (str/split \"ab:cd:ef\" \" +\"))"));
	}

	@Test
	public void test_str_split_lines() {
		final Venice venice = new Venice();

		assertEquals("()", venice.eval("(str (str/split-lines nil))"));
		assertEquals("(ab)", venice.eval("(str (str/split-lines \"ab\"))"));
		assertEquals("(ab cd ef)", venice.eval("(str (str/split-lines \"ab\ncd\nef\"))"));
	}
	
	@Test
	public void test_starts_with() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(str/starts-with? \"abcdef\" \"abc\")"));
		assertFalse((Boolean)venice.eval("(str/starts-with? \"abcdef\" \"x\")"));
	}

	@Test
	public void test_str_strip_end() {
		final Venice venice = new Venice();

		assertEquals("abc", venice.eval("(str/strip-end \"abcdef\" \"def\")"));
		assertEquals("abcdef", venice.eval("(str/strip-end \"abcdef\" \"abc\")"));
	}

	@Test
	public void test_str_strip_indent() {
		final Venice venice = new Venice();

		assertEquals("abc", venice.eval("(str/strip-indent \"abc\")"));
		assertEquals("abc", venice.eval("(str/strip-indent \"  abc\")"));
		assertEquals("line1\n  line2\n  line3", venice.eval("(str/strip-indent \"  line1\n    line2\n    line3\")"));
	}

	@Test
	public void test_str_strip_margin() {
		final Venice venice = new Venice();

		assertEquals("abc", venice.eval("(str/strip-margin \"abc\")"));
		assertEquals("abc", venice.eval("(str/strip-margin \"  abc\")"));
		assertEquals("line1\n  line2\n  line3", venice.eval("(str/strip-margin \"line1\n  |  line2\n  |  line3\")"));
		assertEquals("line1\n line2\n line3", venice.eval("(str/strip-margin \" line1\n  |  line2\n  |  line3\")"));
	}

	@Test
	public void test_str_strip_start() {
		final Venice venice = new Venice();

		assertEquals("def", venice.eval("(str/strip-start \"abcdef\" \"abc\")"));
		assertEquals("abcdef", venice.eval("(str/strip-start \"abcdef\" \"def\")"));
	}

	@Test
	public void test_str_subs() {
		final Venice venice = new Venice();

		assertEquals("abcdef", venice.eval("(str/subs \"abcdef\" 0)"));
		assertEquals("ab", venice.eval("(str/subs \"abcdef\" 0 2)"));
		assertEquals("bcdef", venice.eval("(str/subs \"abcdef\" 1)"));
		assertEquals("f", venice.eval("(str/subs \"abcdef\" 5)"));
		assertEquals("", venice.eval("(str/subs \"abcdef\" 6)"));
	}

	@Test
	public void test_str_trim() {
		final Venice venice = new Venice();
		
		assertEquals("abcdef", venice.eval("(str/trim \"abcdef\")"));
		assertEquals("abcdef", venice.eval("(str/trim \"  abcdef  \")"));
		assertEquals("abcdef", venice.eval("(str/trim \"  abcdef\")"));
		assertEquals("abcdef", venice.eval("(str/trim \"abcdef  \")"));
		assertEquals("", venice.eval("(str/trim \"  \")"));
	}

	@Test
	public void test_str_trim_to_nil() {
		final Venice venice = new Venice();
		
		assertEquals("abcdef", venice.eval("(str/trim-to-nil \"abcdef\")"));
		assertEquals("abcdef", venice.eval("(str/trim-to-nil \"  abcdef  \")"));
		assertEquals("abcdef", venice.eval("(str/trim-to-nil \"  abcdef\")"));
		assertEquals("abcdef", venice.eval("(str/trim-to-nil \"abcdef  \")"));
		assertEquals(null, venice.eval("(str/trim-to-nil \"  \")"));
	}

	@Test
	public void test_str_truncate() {
		final Venice venice = new Venice();
		
		assertEquals(null, venice.eval("(str/truncate nil 20 \"...\")"));
		assertEquals("", venice.eval("(str/truncate \"\" 20 \"...\")"));
		assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 20 \"...\")"));
		assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 10 \"...\")"));
		assertEquals("abcdef...", venice.eval("(str/truncate \"abcdefghij\" 9 \"...\")"));
		assertEquals("a...", venice.eval("(str/truncate \"abcdefghij\" 4 \"...\")"));
	}

	@Test
	public void test_str_upper_case() {
		final Venice venice = new Venice();
		
		assertEquals("ABCDEF", venice.eval("(str/upper-case \"abcdef\")"));
		assertEquals("ABCDEF", venice.eval("(str/upper-case \"aBcDeF\")"));
	}
	
}
