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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


/**
 * Parsatron unit tests
 * 
 * @see <a href="https://github.com/sjl/parsatron/blob/docs/docs/guide.markdown">Examples</a>
 */
public class ParsatronModuleTest {

	@Test
	public void test_basic() {
		final String script =
				"(do                                               \n" +
				"   (load-module :parsatron)                       \n" +
				"   (parsatron/run (parsatron/char \"H\")          \n" +
				"                  \"Hello, world!\"))              ";

		assertEquals("H", new Venice().eval(script));
	}

	@Test
	public void test_basic_input() {
		final String script =
				"(do                                               \n" +
				"   (load-module :parsatron)                       \n" +
				"   (parsatron/run (parsatron/token #{1 2})        \n" +
				"                  [1 \"cats\" :dogs]))              ";

		assertEquals(1L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_basic_error() {
		final String script =
				"(do                                               \n" +
				"   (load-module :parsatron)                       \n" +
				"   (parsatron/run (parsatron/char \"Q\")          \n" +
				"                  \"Hello, world!\"))              ";
		
		assertThrows(VncException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_basic_parser() {
		final String script =
				"(do                                               \n" +
				"   (load-module :parsatron)                       \n" +
				"   (def h-parser (parsatron/char \"H\"))          \n" +
				"   (parsatron/run h-parser                        \n" +
				"                  \"Hello, world!\"))              ";

		assertEquals("H", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_token() {
		final String script =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (defn less-than-five [i] (< i 5))                \n" +
				"   (parsatron/run (parsatron/token less-than-five)  \n" +
				"                  [3]))                               ";

		assertEquals(3L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_token_number() {
		final String script =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/token number?)         \n" +
				"                  [10 20 30]))                        ";

		assertEquals(10L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_char() {
		final String script =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/char \"H\")            \n" +
				"                  \"Hello, world!\"))                 ";

		assertEquals("H", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_not_char() {
		final String script1 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/not-char \"C\")        \n" +
				"                  \"Hello, world!\"))                 ";

		assertEquals("H", new Venice().eval(script1));
		
		
		final String script2 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/not-char \"H\")        \n" +
				"                  \"Hello, world!\"))                 ";

		assertThrows(VncException.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_anychar() {
		final String script1 =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/any-char)  \n" +
				"                  \"Cats\"))               ";

		assertEquals("C", new Venice().eval(script1));

		
		final String script2 =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/any-char)  \n" +
				"                  (seq \"Cats\")))        ";

		assertEquals("C", new Venice().eval(script2));

		
		final String script3 =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/any-char)  \n" +
				"                  [1 2 3]))               ";

		assertThrows(VncException.class, () -> new Venice().eval(script3));
	}

	@Test
	public void test_builtin_parser_letter() {
		final String script =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/letter)    \n" +
				"                  \"Dogs\"))              ";

		assertEquals("D", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_digit() {
		final String script =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/digit)     \n" +
				"                  \"1234\"))              ";

		assertEquals("1", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_string() {
		final String script =
				"(do                                                \n" +
				"   (load-module :parsatron)                        \n" +
				"   (parsatron/run (parsatron/string \"Hello\")     \n" +
				"                  \"Hello, world!\"))                ";

		assertEquals("Hello", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_any_char_of() {
		final String script1 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/any-char-of \"ABC\")   \n" +
				"                  \"Cats\"))                          ";

		assertEquals("C", new Venice().eval(script1));

		
		final String script2 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/any-char-of \"DEF\")   \n" +
				"                  \"Cats\"))                          ";

		assertThrows(VncException.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_none_char_of() {
		final String script1 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/none-char-of \"DEF\")  \n" +
				"                  \"Cats\"))                          ";

		assertEquals("C", new Venice().eval(script1));

		
		final String script2 =
				"(do                                                 \n" +
				"   (load-module :parsatron)                         \n" +
				"   (parsatron/run (parsatron/none-char-of \"ABC\")  \n" +
				"                  \"Cats\"))                          ";

		assertThrows(VncException.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_eof() {
		final String script1 =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/eof)       \n" +
				"                  \"\"))                 ";

		assertNull(new Venice().eval(script1));

	
		final String script2 =
				"(do                                     \n" +
				"   (load-module :parsatron)             \n" +
				"   (parsatron/run (parsatron/eof)       \n" +
				"                  \"a\"))                 ";

		assertThrows(VncException.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_composition() {
		final String script1 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/char \"a\")           \n" +
				"                                (parsatron/digit)))              \n" +
				"   (parsatron/run my-parser \"a5\"))                               ";

		assertEquals("5", new Venice().eval(script1));

	
		final String script2 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/char \"a\")           \n" +
				"                                (parsatron/digit)))              \n" +
				"   (parsatron/run my-parser \"5a\"))                               ";

		assertThrows(VncException.class, () -> new Venice().eval(script2));

		
		final String script3 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/char \"a\")           \n" +
				"                                (parsatron/digit)))              \n" +
				"   (parsatron/run my-parser \"b5\"))                               ";

		assertThrows(VncException.class, () -> new Venice().eval(script3));

		
		final String script4 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/char \"a\")           \n" +
				"                                (parsatron/digit)))              \n" +
				"   (parsatron/run my-parser \"aq\"))                               ";

		assertThrows(VncException.class, () -> new Venice().eval(script4));

		
		final String script5 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/digit)                \n" +
				"                                (parsatron/eof)))                \n" +
				"   (parsatron/run my-parser \"1\"))                                ";

		assertNull(new Venice().eval(script5));

		
		final String script6 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/digit)                \n" +
				"                                (parsatron/eof)))                \n" +
				"   (parsatron/run my-parser \"1 cat\"))                            ";

		assertThrows(VncException.class, () -> new Venice().eval(script6));
	}

	@Test
	public void test_builtin_parser_times() {
		final String script1 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (pr-str                                                       \n" +
				"     (parsatron/run (parsatron/times 5 (parsatron/letter))       \n" +
				"                    \"Hello, world!\")))                           ";

		assertEquals("[\"H\" \"e\" \"l\" \"l\" \"o\"]", new Venice().eval(script1));


		final String script2 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (def my-parser (parsatron/>> (parsatron/letter)               \n" +
				"                                (parsatron/letter)               \n" +
				"                                (parsatron/letter)               \n" +
				"                                (parsatron/letter)               \n" +
				"                                (parsatron/letter)))             \n" +
				"   (parsatron/run my-parser \"Hello, world!\"))                    ";

		assertEquals("o", new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_many() {
		final String script1 =
				"(do                                                              \n" +
				"   (load-module :parsatron)                                      \n" +
				"   (pr-str                                                       \n" +
				"     (parsatron/run (parsatron/many (parsatron/digit))           \n" +
				"                    \"100 cats\")))                                ";

		assertEquals("[\"1\" \"0\" \"0\"]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number-parser (parsatron/many (parsatron/digit)))               \n" +
				"   (def whitespace-parser (parsatron/many                               \n" +
				"                              (parsatron/token str/whitespace?)))       \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run (parsatron/>> number-parser                         \n" +
				"                                  whitespace-parser                     \n" +
				"                                  number-parser)                        \n" +
				"                    \"100  400\")))                                       ";

		assertEquals("[\"4\" \"0\" \"0\"]", new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_many1() {
		final String script1 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number-parser (parsatron/many (parsatron/digit)))               \n" +
				"   (def number-parser1 (parsatron/many1 (parsatron/digit)))             \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-parser \"\")))                                 ";

		assertEquals("[]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number-parser (parsatron/many (parsatron/digit)))               \n" +
				"   (def number-parser1 (parsatron/many1 (parsatron/digit)))             \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-parser \"100\")))                              ";

		assertEquals("[\"1\" \"0\" \"0\"]", new Venice().eval(script2));
		
		
		final String script3 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number-parser (parsatron/many (parsatron/digit)))               \n" +
				"   (def number-parser1 (parsatron/many1 (parsatron/digit)))             \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-parser1 \"\")))                                 ";

		assertThrows(VncException.class, () -> new Venice().eval(script3));

	
		final String script4 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number-parser (parsatron/many (parsatron/digit)))               \n" +
				"   (def number-parser1 (parsatron/many1 (parsatron/digit)))             \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-parser1 \"100\")))                              ";

		assertEquals("[\"1\" \"0\" \"0\"]", new Venice().eval(script4));
	}

	@Test
	public void test_builtin_parser_choice() {
		final String script1 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number (parsatron/many1 (parsatron/digit)))                     \n" +
				"   (def word (parsatron/many1 (parsatron/letter)))                      \n" +
				"   (def number-or-word (parsatron/choice number word))                  \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-or-word \"dog\")))                             ";

		assertEquals("[\"d\" \"o\" \"g\"]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def number (parsatron/many1 (parsatron/digit)))                     \n" +
				"   (def word (parsatron/many1 (parsatron/letter)))                      \n" +
				"   (def number-or-word (parsatron/choice number word))                  \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run number-or-word \"42\")))                             ";

		assertEquals("[\"4\" \"2\"]", new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_between() {
		final String script =
				"(do                                                                     \n" +
				"   (load-module :parsatron)                                             \n" +
				"                                                                        \n" +
				"   (def whitespace-char (parsatron/token str/whitespace?))              \n" +
				"   (def optional-whitespace (parsatron/many whitespace-char))           \n" +
				"                                                                        \n" +
				"   (def open-paren (parsatron/char \"(\"))                              \n" +
				"   (def close-paren (parsatron/char \")\"))                             \n" +
				"                                                                        \n" +
				"   (def number (parsatron/many1 (parsatron/digit)))                     \n" +
				"                                                                        \n" +
				"   (pr-str                                                              \n" +
				"     (parsatron/run                                                     \n" +
				"          (parsatron/between                                            \n" +
				"               (parsatron/>> open-paren optional-whitespace)            \n" +
				"               (parsatron/>> optional-whitespace  close-paren)          \n" +
				"               number)                                                  \n" +
				"          \"(123    )\")))                                                ";

		assertEquals("[\"1\" \"2\" \"3\"]", new Venice().eval(script));
	}

	@Test
	public void test_lineno() {
		final String script =     
				"(do                                                                        \n" +
				"   (load-module :parsatron)                                                \n" +
				"   (parsatron/run (parsatron/let->> [_     (parsatron/many                 \n" +
				"                                              (parsatron/not-char \"w\"))  \n" +
				"                                     match (parsatron/char \"w\")          \n" +
				"                                     pos   (parsatron/extract :pos)]       \n" +
				"                     (parsatron/always (str match                          \n" +
				"                                            \": \"                         \n" +
				"                                            (:line pos)                    \n" +
				"                                            \",\"                          \n" +
				"                                            (:column pos))))               \n" +
				"                  \"Hello, world!\"))                                       ";

		assertEquals("w: 1,9", new Venice().eval(script));
	}

	@Test
	public void test_defparser_always() {
		final String script =
				"(do                                                      \n" +
				"   (load-module :parsatron)                              \n" +
				"                                                         \n" +
				"   (parsatron/defparser sample []                        \n" +
				"     (parsatron/string \"Hello\")                        \n" +
				"     (parsatron/always 42))                              \n" +
				"                                                         \n" +
				"   (def my-sample-parser (sample))                       \n" +
				"                                                         \n" +
				"   (parsatron/run my-sample-parser \"Hello, world!\"))     ";
		
		assertEquals(42L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_1() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser word []                                      \n" +
				"     (parsatron/many1 (parsatron/letter)))                           \n" +
				"                                                                     \n" +
				"   (parsatron/defparser first-word []                                \n" +
				"     (parsatron/let->> [name (word)]                                 \n" + 
				"         (let [name (apply str name)]                                \n" + 
				"           (parsatron/always name))))                                \n" +
				"                                                                     \n" +
				"   (parsatron/run (first-word) \"Hello, Cat!\"))                       ";
		
		assertEquals("Hello", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2a() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser sample []                                    \n" +
				"     (parsatron/let->> [sign (parsatron/choice                       \n" + 
				"                                 (parsatron/char \"+\")              \n" + 
				"                                 (parsatron/char \"-\"))             \n" + 
				"                        word (if (== sign \"+\")                     \n" + 
				"                                 (parsatron/string \"plus\")         \n" + 
				"                                 (parsatron/string \"minus\"))]      \n" + 
				"         (parsatron/always [sign word])))                            \n" +
				"                                                                     \n" +
				"   (pr-str (parsatron/run (sample) \"+plus\"))))                       ";
		
		assertEquals("[\"+\" \"plus\"]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2b() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser sample []                                    \n" +
				"     (parsatron/let->> [sign (parsatron/choice                       \n" + 
				"                                 (parsatron/char \"+\")              \n" + 
				"                                 (parsatron/char \"-\"))             \n" + 
				"                        word (if (== sign \"+\")                     \n" + 
				"                                 (parsatron/string \"plus\")         \n" + 
				"                                 (parsatron/string \"minus\"))]      \n" + 
				"         (parsatron/always [sign word])))                            \n" +
				"                                                                     \n" +
				"   (pr-str (parsatron/run (sample) \"-minus\"))))                       ";
		
		assertEquals("[\"-\" \"minus\"]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2c() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser sample []                                    \n" +
				"     (parsatron/let->> [sign (parsatron/choice                       \n" + 
				"                                 (parsatron/char \"+\")              \n" + 
				"                                 (parsatron/char \"-\"))             \n" + 
				"                        word (if (== sign plus)                      \n" + 
				"                                 (parsatron/string \"plus\")         \n" + 
				"                                 (parsatron/string \"minus\"))]      \n" + 
				"         (parsatron/always [sign word])))                            \n" +
				"                                                                     \n" +
				"   (pr-str (parsatron/run (sample) \"-plus\"))))                       ";
		
		assertThrows(VncException.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_3() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser word []                                      \n" +
				"     (parsatron/many1 (parsatron/letter)))                           \n" +
				"                                                                     \n" +
				"   (parsatron/defparser greeting []                                  \n" +
				"     (parsatron/let->> [prefix      (parsatron/string \"Hello, \")   \n" +
				"                        name        (word)                           \n" +
				"                        punctuation (parsatron/choice                \n" +
				"                                       (parsatron/char \".\")        \n" +
				"                                       (parsatron/char \"!\"))]      \n" +
				"       (if (== punctuation \"!\")                                    \n" +
				"          (parsatron/always [(apply str name) :excited])             \n" +
				"          (parsatron/always [(apply str name) :not-excited]))))      \n" +
				"                                                                     \n" +
				"   (pr-str (parsatron/run (greeting) \"Hello, Cat!\")))                ";
		
		assertEquals("[\"Cat\" :excited]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_4() {
		final String script =
				"(do                                                                  \n" +
				"   (load-module :parsatron)                                          \n" +
				"                                                                     \n" +
				"   (parsatron/defparser word []                                      \n" +
				"     (parsatron/many1 (parsatron/letter)))                           \n" +
				"                                                                     \n" +
				"   (parsatron/defparser greeting []                                  \n" +
				"     (parsatron/let->> [prefix      (parsatron/string \"Hello, \")   \n" +
				"                        name        (word)                           \n" +
				"                        punctuation (parsatron/choice                \n" +
				"                                       (parsatron/char \".\")        \n" +
				"                                       (parsatron/char \"!\"))]      \n" +
				"       (if (== punctuation \"!\")                                    \n" +
				"          (parsatron/always [(apply str name) :excited])             \n" +
				"          (parsatron/always [(apply str name) :not-excited]))))      \n" +
				"                                                                     \n" +
				"   (pr-str (parsatron/run (greeting) \"Hello, Dog.\")))                ";
		
		assertEquals("[\"Dog\" :not-excited]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_5() {
		final String script =
				"(do                                                                      \n" +
				"   (load-module :parsatron)                                              \n" +
				"                                                                         \n" +
				"   (parsatron/defparser float []                                         \n" +
				"     (parsatron/let->> [integral   (parsatron/many1 (parsatron/digit))   \n" + 
				"                        _          (parsatron/char \".\")                \n" + 
				"                        fractional (parsatron/many1 (parsatron/digit))]  \n" + 
				"        (let [integral   (apply str integral)                            \n" +
				"              fractional (apply str fractional)]                         \n" +
				"          (parsatron/always (double (str integral \".\" fractional)))))) \n" +
				"                                                                         \n" +
				"   (pr-str [ (parsatron/run (float) \"1.4\")                             \n" +
				"             (parsatron/run (float) \"1.04\")                            \n" +
				"             (parsatron/run (float) \"1.04000\")]))                        ";
			
		assertEquals("[1.4 1.04 1.04]", new Venice().eval(script));
	}

}
