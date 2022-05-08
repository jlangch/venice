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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.Venice;


/**
 * Parsifal unit tests
 * 
 * @see <a href="https://github.com/sjl/p/blob/docs/docs/guide.markdown">Examples</a>
 */
public class ParsifalModuleTest {

	@Test
	public void test_basic() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (p/run (p/char #\\H)  \"Hello, world!\"))  ";

		assertEquals('H', (Character)new Venice().eval(script));
	}

	@Test
	public void test_basic_input() {
		final String script =
				"(do                                               \n" +
				"   (load-module :parsifal ['parsifal :as 'p])     \n" +
				"                                                  \n" +
				"   (p/run (p/token #{1 2}) [1 \"cats\" :dogs]))   ";

		assertEquals(1L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_basic_error() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (p/run (p/char #\\Q)  \"Hello, world!\"))  ";
		
		assertThrows(ParseError.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_basic_parser() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (def h-parser (p/char #\\H))               \n" +
				"   (p/run h-parser \"Hello, world!\"))        ";

		assertEquals('H', (Character)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_token() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (defn less-than-five [i] (< i 5))          \n" +
				"   (p/run (p/token less-than-five) [3]))      ";

		assertEquals(3L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_token_number() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (p/run (p/token number?) [10 20 30]))      ";

		assertEquals(10L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_char() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal ['parsifal :as 'p]) \n" +
				"                                              \n" +
				"   (p/run (p/char #\\H) \"Hello, world!\"))   ";

		assertEquals('H', (Character)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_not_char() {
		final String script1 =
				"(do                                              \n" +
				"   (load-module :parsifal ['parsifal :as 'p])    \n" +
				"                                                 \n" +
				"   (p/run (p/not-char #\\C) \"Hello, world!\"))  ";

		assertEquals('H', (Character)new Venice().eval(script1));
		
		
		final String script2 =
				"(do                                              \n" +
				"   (load-module :parsifal)                       \n" +
				"   (ns-alias 'p 'parsifal)                       \n" +
				"                                                 \n" +
				"   (p/run (p/not-char #\\H) \"Hello, world!\"))  ";

		assertThrows(ParseError.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_anychar() {
		final String script1 =
				"(do                                 \n" +
				"   (load-module :parsifal)          \n" +
				"   (ns-alias 'p 'parsifal)          \n" +
				"                                    \n" +
				"   (p/run (p/any-char) \"Cats\"))   ";

		assertEquals('C', (Character)new Venice().eval(script1));

		
		final String script2 =
				"(do                                      \n" +
				"   (load-module :parsifal)               \n" +
				"   (ns-alias 'p 'parsifal)               \n" +
				"                                         \n" +
				"   (p/run (p/any-char) (seq \"Cats\")))  ";

		assertEquals('C', (Character)new Venice().eval(script2));

		
		final String script3 =
				"(do                                 \n" +
				"   (load-module :parsifal)          \n" +
				"   (ns-alias 'p 'parsifal)          \n" +
				"                                    \n" +
				"   (p/run (p/any-char) [1 2 3]))    ";

		assertThrows(ParseError.class, () -> new Venice().eval(script3));
	}

	@Test
	public void test_builtin_parser_letter() {
		final String script =
				"(do                                \n" +
				"   (load-module :parsifal)         \n" +
				"   (ns-alias 'p 'parsifal)         \n" +
				"                                   \n" +
				"   (p/run (p/letter) \"Dogs\"))    ";

		assertEquals('D', (Character)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_digit() {
		final String script =
				"(do                               \n" +
				"   (load-module :parsifal)        \n" +
				"   (ns-alias 'p 'parsifal)        \n" +
				"                                  \n" +
				"   (p/run (p/digit)  \"1234\"))   ";

		assertEquals('1', (Character)new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_string() {
		final String script =
				"(do                                                \n" +
				"   (load-module :parsifal)                         \n" +
				"   (ns-alias 'p 'parsifal)                         \n" +
				"                                                   \n" +
				"   (p/run (p/string \"Hello\") \"Hello, world!\")) ";

		assertEquals("Hello", new Venice().eval(script));
	}

	@Test
	public void test_builtin_parser_any_char_of() {
		final String script1 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (p/run (p/any-char-of \"ABC\") \"Cats\"))  ";

		assertEquals('C', (Character)new Venice().eval(script1));

		
		final String script2 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (p/run (p/any-char-of \"DEF\") \"Cats\"))  ";

		assertThrows(ParseError.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_none_char_of() {
		final String script1 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (p/run (p/none-char-of \"DEF\") \"Cats\")) ";

		assertEquals('C', (Character)new Venice().eval(script1));

		
		final String script2 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (p/run (p/none-char-of \"ABC\") \"Cats\")) ";

		assertThrows(ParseError.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_eof() {
		final String script1 =
				"(do                         \n" +
				"   (load-module :parsifal)  \n" +
				"   (ns-alias 'p 'parsifal)  \n" +
				"                            \n" +
				"   (p/run (p/eof) \"\"))    ";

		assertNull(new Venice().eval(script1));

	
		final String script2 =
				"(do                         \n" +
				"   (load-module :parsifal)  \n" +
				"   (ns-alias 'p 'parsifal)  \n" +
				"                            \n" +
				"   (p/run (p/eof) \"a\"))   ";

		assertThrows(ParseError.class, () -> new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_composition() {
		final String script1 =
				"(do                                     \n" +
				"   (load-module :parsifal)              \n" +
				"   (ns-alias 'p 'parsifal)              \n" +
				"                                        \n" +
				"   (def my-parser (p/>> (p/char #\\a)   \n" +
				"                        (p/digit)))     \n" +
				"   (p/run my-parser \"a5\"))            ";

		assertEquals('5', (Character)new Venice().eval(script1));

	
		final String script2 =
				"(do                                      \n" +
				"   (load-module :parsifal)               \n" +
				"   (ns-alias 'p 'parsifal)               \n" +
				"                                         \n" +
				"   (def my-parser (p/>> (p/char #\\a)    \n" +
				"                        (p/digit)))      \n" +
				"   (p/run my-parser \"5a\"))             ";

		assertThrows(ParseError.class, () -> new Venice().eval(script2));

		
		final String script3 =
				"(do                                      \n" +
				"   (load-module :parsifal)               \n" +
				"   (ns-alias 'p 'parsifal)               \n" +
				"                                         \n" +
				"   (def my-parser (p/>> (p/char #\\a)    \n" +
				"                         (p/digit)))     \n" +
				"   (p/run my-parser \"b5\"))             ";

		assertThrows(ParseError.class, () -> new Venice().eval(script3));

		
		final String script4 =
				"(do                                       \n" +
				"   (load-module :parsifal)                \n" +
				"   (ns-alias 'p 'parsifal)                \n" +
				"                                          \n" +
				"   (def my-parser (p/>> (p/char #\\a)     \n" +
				"                         (p/digit)))      \n" +
				"   (p/run my-parser \"aq\"))              ";

		assertThrows(ParseError.class, () -> new Venice().eval(script4));

		
		final String script5 =
				"(do                                       \n" +
				"   (load-module :parsifal)                \n" +
				"   (ns-alias 'p 'parsifal)                \n" +
				"                                          \n" +
				"   (def my-parser (p/>> (p/digit)         \n" +
				"                        (p/eof)))         \n" +
				"   (p/run my-parser \"1\"))               ";

		assertNull(new Venice().eval(script5));

		
		final String script6 =
				"(do                                       \n" +
				"   (load-module :parsifal)                \n" +
				"   (ns-alias 'p 'parsifal)                \n" +
				"                                          \n" +
				"   (def my-parser (p/>> (p/digit)         \n" +
				"                        (p/eof)))         \n" +
				"   (p/run my-parser \"1 cat\"))           ";

		assertThrows(ParseError.class, () -> new Venice().eval(script6));
	}

	@Test
	public void test_builtin_parser_times() {
		final String script1 =
				"(do                                      \n" +
				"   (load-module :parsifal)               \n" +
				"   (ns-alias 'p 'parsifal)               \n" +
				"                                         \n" +
				"   (pr-str                               \n" +
				"     (p/run (p/times 5 (p/letter))       \n" +
				"            \"Hello, world!\")))         ";

		assertEquals("[#\\H #\\e #\\l #\\l #\\o]", new Venice().eval(script1));


		final String script2 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (def my-parser (p/>> (p/letter)            \n" +
				"                        (p/letter)            \n" +
				"                        (p/letter)            \n" +
				"                        (p/letter)            \n" +
				"                        (p/letter)))          \n" +
				"   (p/run my-parser \"Hello, world!\"))       ";

		assertEquals('o', (Character)new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_many() {
		final String script1 =
				"(do                                   \n" +
				"   (load-module :parsifal)            \n" +
				"   (ns-alias 'p 'parsifal)            \n" +
				"                                      \n" +
				"   (pr-str                            \n" +
				"     (p/run (p/many (p/digit))        \n" +
				"                    \"100 cats\")))   ";

		assertEquals("[#\\1 #\\0 #\\0]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                                    \n" +
				"   (load-module :parsifal)                             \n" +
				"   (ns-alias 'p 'parsifal)                             \n" +
				"                                                       \n" +
				"   (def number-parser (p/many (p/digit)))              \n" +
				"   (def whitespace-parser (p/many                      \n" +
				"                          (p/token str/whitespace?)))  \n" +
				"                                                       \n" +
				"   (pr-str                                             \n" +
				"     (p/run (p/>> number-parser                        \n" +
				"                  whitespace-parser                    \n" +
				"                  number-parser)                       \n" +
				"            \"100  400\")))                            ";

		assertEquals("[#\\4 #\\0 #\\0]", new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_many1() {
		final String script1 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (def number-parser (p/many (p/digit)))     \n" +
				"   (def number-parser1 (p/many1 (p/digit)))   \n" +
				"                                              \n" +
				"   (pr-str (p/run number-parser \"\")))       ";

		assertEquals("[]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (def number-parser (p/many (p/digit)))     \n" +
				"   (def number-parser1 (p/many1 (p/digit)))   \n" +
				"                                              \n" +
				"   (pr-str                                    \n" +
				"     (p/run number-parser \"100\")))          ";

		assertEquals("[#\\1 #\\0 #\\0]", new Venice().eval(script2));
		
		
		final String script3 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (def number-parser (p/many (p/digit)))     \n" +
				"   (def number-parser1 (p/many1 (p/digit)))   \n" +
				"                                              \n" +
				"   (pr-str                                    \n" +
				"     (p/run number-parser1 \"\")))            ";

		assertThrows(ParseError.class, () -> new Venice().eval(script3));

	
		final String script4 =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (def number-parser (p/many (p/digit)))     \n" +
				"   (def number-parser1 (p/many1 (p/digit)))   \n" +
				"                                              \n" +
				"   (pr-str                                    \n" +
				"     (p/run number-parser1 \"100\")))         ";

		assertEquals("[#\\1 #\\0 #\\0]", new Venice().eval(script4));
	}

	@Test
	public void test_builtin_parser_choice() {
		final String script1 =
				"(do                                             \n" +
				"   (load-module :parsifal)                      \n" +
				"   (ns-alias 'p 'parsifal)                      \n" +
				"                                                \n" +
				"   (def number (p/many1 (p/digit)))             \n" +
				"   (def word (p/many1 (p/letter)))              \n" +
				"   (def number-or-word (p/choice number word))  \n" +
				"                                                \n" +
				"   (pr-str                                      \n" +
				"     (p/run number-or-word \"dog\")))           ";

		assertEquals("[#\\d #\\o #\\g]", new Venice().eval(script1));

	
		final String script2 =
				"(do                                             \n" +
				"   (load-module :parsifal)                      \n" +
				"   (ns-alias 'p 'parsifal)                      \n" +
				"                                                \n" +
				"   (def number (p/many1 (p/digit)))             \n" +
				"   (def word (p/many1 (p/letter)))              \n" +
				"   (def number-or-word (p/choice number word))  \n" +
				"                                                \n" +
				"   (pr-str                                      \n" +
				"     (p/run number-or-word \"42\")))            ";

		assertEquals("[#\\4 #\\2]", new Venice().eval(script2));
	}

	@Test
	public void test_builtin_parser_between() {
		final String script =
				"(do                                                     \n" +
				"   (load-module :parsifal)                              \n" +
				"   (ns-alias 'p 'parsifal)                              \n" +
				"                                                        \n" +
				"   (def whitespace-char (p/token str/whitespace?))      \n" +
				"   (def optional-whitespace (p/many whitespace-char))   \n" +
				"                                                        \n" +
				"   (def open-paren (p/char \"(\"))                      \n" +
				"   (def close-paren (p/char \")\"))                     \n" +
				"                                                        \n" +
				"   (def number (p/many1 (p/digit)))                     \n" +
				"                                                        \n" +
				"   (pr-str                                              \n" +
				"     (p/run                                             \n" +
				"          (p/between                                    \n" +
				"               (p/>> open-paren optional-whitespace)    \n" +
				"               (p/>> optional-whitespace  close-paren)  \n" +
				"               number)                                  \n" +
				"          \"(123    )\")))                              ";

		assertEquals("[#\\1 #\\2 #\\3]", new Venice().eval(script));
	}

	@Test
	public void test_lineno() {
		final String script =     
				"(do                                                        \n" +
				"   (load-module :parsifal)                                 \n" +
				"   (ns-alias 'p 'parsifal)                                 \n" +
				"                                                           \n" +
				"   (p/run (p/let->> [_     (p/many (p/not-char #\\w))      \n" +
				"                     match (p/char #\\w)                   \n" +
				"                     pos   (p/extract :pos)]               \n" +
				"             (p/always (str match                          \n" +
				"                            \": \"                         \n" +
				"                            (:line pos)                    \n" +
				"                            \",\"                          \n" +
				"                            (:column pos))))               \n" +
				"          \"Hello, world!\"))                              ";

		assertEquals("w: 1,9", new Venice().eval(script));
	}

	@Test
	public void test_defparser_always() {
		final String script =
				"(do                                              \n" +
				"   (load-module :parsifal)                       \n" +
				"   (ns-alias 'p 'parsifal)                       \n" +
				"                                                 \n" +
				"   (p/defparser sample []                        \n" +
				"     (p/string \"Hello\")                        \n" +
				"     (p/always 42))                              \n" +
				"                                                 \n" +
				"   (def my-sample-parser (sample))               \n" +
				"                                                 \n" +
				"   (p/run my-sample-parser \"Hello, world!\"))   ";
		
		assertEquals(42L, (Long)new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_1() {
		final String script =
				"(do                                           \n" +
				"   (load-module :parsifal)                    \n" +
				"   (ns-alias 'p 'parsifal)                    \n" +
				"                                              \n" +
				"   (p/defparser word []                       \n" +
				"     (p/many1 (p/letter)))                    \n" +
				"                                              \n" +
				"   (p/defparser first-word []                 \n" +
				"     (p/let->> [name (word)]                  \n" + 
				"        (let [name (apply str name)]          \n" + 
				"          (p/always name))))                  \n" +
				"                                              \n" +
				"   (p/run (first-word) \"Hello, Cat!\"))      ";
		
		assertEquals("Hello", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2a() {
		final String script =
				"(do                                                \n" +
				"   (load-module :parsifal)                         \n" +
				"   (ns-alias 'p 'parsifal)                         \n" +
				"                                                   \n" +
				"   (p/defparser sample []                          \n" +
				"     (p/let->> [sign (p/choice (p/char #\\+)       \n" + 
				"                               (p/char #\\-))      \n" + 
				"                word (if (== sign #\\+)            \n" + 
				"                       (p/string \"plus\")         \n" + 
				"                       (p/string \"minus\"))]      \n" + 
				"         (p/always [sign word])))                  \n" +
				"                                                   \n" +
				"   (pr-str (p/run (sample) \"+plus\"))))           ";
		
		assertEquals("[#\\+ \"plus\"]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2b() {
		final String script =
				"(do                                                \n" +
				"   (load-module :parsifal)                         \n" +
				"   (ns-alias 'p 'parsifal)                         \n" +
				"                                                   \n" +
				"   (p/defparser sample []                          \n" +
				"     (p/let->> [sign (p/choice (p/char #\\+)       \n" + 
				"                               (p/char #\\-))      \n" + 
				"                word (if (== sign #\\+)            \n" + 
				"                       (p/string \"plus\")         \n" + 
				"                       (p/string \"minus\"))]      \n" + 
				"         (p/always [sign word])))                  \n" +
				"                                                   \n" +
				"   (pr-str (p/run (sample) \"-minus\"))))          ";
		
		assertEquals("[#\\- \"minus\"]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_2c() {
		final String script =
				"(do                                                \n" +
				"   (load-module :parsifal)                         \n" +
				"   (ns-alias 'p 'parsifal)                         \n" +
				"                                                   \n" +
				"   (p/defparser sample []                          \n" +
				"     (p/let->> [sign (p/choice (p/char #\\+)       \n" + 
				"                               (p/char #\\-))      \n" + 
				"                word (if (== sign #\\+)            \n" + 
				"                       (p/string \"plus\")         \n" + 
				"                       (p/string \"minus\"))]      \n" + 
				"         (p/always [sign word])))                  \n" +
				"                                                   \n" +
				"   (pr-str (p/run (sample) \"-plus\"))))           ";
		
		assertThrows(ParseError.class, () -> new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_3() {
		final String script =
				"(do                                                         \n" +
				"   (load-module :parsifal)                                  \n" +
				"   (ns-alias 'p 'parsifal)                                  \n" +
				"                                                            \n" +
				"   (p/defparser word []                                     \n" +
				"     (p/many1 (p/letter)))                                  \n" +
				"                                                            \n" +
				"   (p/defparser greeting []                                 \n" +
				"     (p/let->> [prefix      (p/string \"Hello, \")          \n" +
				"                name        (word)                          \n" +
				"                punctuation (p/choice (p/char #\\.)         \n" +
				"                                      (p/char #\\!))]       \n" +
				"       (if (== punctuation #\\!)                            \n" +
				"          (p/always [(apply str name) :excited])            \n" +
				"          (p/always [(apply str name) :not-excited]))))     \n" +
				"                                                            \n" +
				"   (pr-str (p/run (greeting) \"Hello, Cat!\")))             ";
		
		assertEquals("[\"Cat\" :excited]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_4() {
		final String script =
				"(do                                                         \n" +
				"   (load-module :parsifal)                                  \n" +
				"   (ns-alias 'p 'parsifal)                                  \n" +
				"                                                            \n" +
				"   (p/defparser word []                                     \n" +
				"     (p/many1 (p/letter)))                                  \n" +
				"                                                            \n" +
				"   (p/defparser greeting []                                 \n" +
				"     (p/let->> [prefix      (p/string \"Hello, \")          \n" +
				"                name        (word)                          \n" +
				"                punctuation (p/choice (p/char #\\.)         \n" +
				"                                      (p/char #\\!))]       \n" +
				"       (if (== punctuation #\\!)                            \n" +
				"          (p/always [(apply str name) :excited])            \n" +
				"          (p/always [(apply str name) :not-excited]))))     \n" +
				"                                                            \n" +
				"   (pr-str (p/run (greeting) \"Hello, Dog.\")))             ";
		
		assertEquals("[\"Dog\" :not-excited]", new Venice().eval(script));
	}

	@Test
	public void test_defparser_let_5() {
		final String script =
				"(do                                                              \n" +
				"   (load-module :parsifal)                                       \n" +
				"   (ns-alias 'p 'parsifal)                                       \n" +
				"                                                                 \n" +
				"   (p/defparser float []                                         \n" +
				"     (p/let->> [integral   (p/many1 (p/digit))                   \n" + 
				"                _          (p/char #\\.)                         \n" + 
				"                fractional (p/many1 (p/digit))]                  \n" + 
				"        (let [integral   (apply str integral)                    \n" +
				"              fractional (apply str fractional)]                 \n" +
				"          (p/always (double (str integral #\\. fractional))))))  \n" +
				"                                                                 \n" +
				"   (pr-str [ (p/run (float) \"1.4\")                             \n" +
				"             (p/run (float) \"1.04\")                            \n" +
				"             (p/run (float) \"1.04000\")]))                      ";
			
		assertEquals("[1.4 1.04 1.04]", new Venice().eval(script));
	}

}
