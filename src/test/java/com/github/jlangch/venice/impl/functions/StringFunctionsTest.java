/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.CollectionUtil;


public class StringFunctionsTest {

    @Test
    public void test_str_blank_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/blank? nil)"));
        assertTrue((Boolean)venice.eval("(str/blank? \"\")"));
        assertTrue((Boolean)venice.eval("(str/blank? \"  \")"));

        assertFalse((Boolean)venice.eval("(str/blank? \"abc\")"));
        assertFalse((Boolean)venice.eval("(str/blank? \" bc\")"));
        assertFalse((Boolean)venice.eval("(str/blank? \"  c\")"));
        assertFalse((Boolean)venice.eval("(str/blank? \"ab \")"));
        assertFalse((Boolean)venice.eval("(str/blank? \"a  \")"));
    }

    @Test
    public void test_str_not_blank_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/not-blank? \"abc\")"));
        assertTrue((Boolean)venice.eval("(str/not-blank? \" bc\")"));
        assertTrue((Boolean)venice.eval("(str/not-blank? \"  c\")"));
        assertTrue((Boolean)venice.eval("(str/not-blank? \"ab \")"));
        assertTrue((Boolean)venice.eval("(str/not-blank? \"a  \")"));

        assertFalse((Boolean)venice.eval("(str/not-blank? nil)"));
        assertFalse((Boolean)venice.eval("(str/not-blank? \"\")"));
        assertFalse((Boolean)venice.eval("(str/not-blank? \"  \")"));
    }

    @Test
    public void test_str_contains() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/contains? \"abcdef\" \"f\")"));
        assertTrue((Boolean)venice.eval("(str/contains? \"abcdef\" \"abc\")"));
        assertTrue((Boolean)venice.eval("(str/contains? \"abcdef\" \"abcdef\")"));
        assertFalse((Boolean)venice.eval("(str/contains? \"abcdef\" \"\")"));
        assertFalse((Boolean)venice.eval("(str/contains? \"abcdef\" \"x\")"));

        assertTrue((Boolean)venice.eval("(str/contains? \"abcdef\" (char \"f\"))"));
        assertFalse((Boolean)venice.eval("(str/contains? \"abcdef\" (char \"x\"))"));
    }

    @Test
    public void test_str_digit_Q() {
        final Venice venice = new Venice();

        final HashSet<Character> digits = new HashSet<>(toCharList("0123456789"));

        for (int ii=0; ii<128; ii++) {
            final String e = "(str/digit? (char " + ii + "))";

            assertEquals(digits.contains((char)ii),venice.eval(e));
        }
    }

    @Test
    public void test_str_hexdigit_Q() {
        final Venice venice = new Venice();

        final HashSet<Character> digits = new HashSet<>(toCharList("0123456789abcdefABCDEF"));

        for (int ii=0; ii<128; ii++) {
            final String e = "(str/hexdigit? (char " + ii + "))";

            assertEquals(digits.contains((char)ii),venice.eval(e));
        }
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

        final Locale old = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("de", "CH")); // Java 11

            assertEquals(":        ab:", venice.eval("(str/format \":%10s:\" \"ab\")"));
            assertEquals(":ab        :", venice.eval("(str/format \":%-10s:\" \"ab\")"));
            assertEquals("1.4500", venice.eval("(str/format \"%.4f\" 1.45)"));
            assertEquals("0012", venice.eval("(str/format \"%04d\" 12)"));
            assertEquals("0012::0034", venice.eval("(str/format \"%04d::%04d\" 12 34)"));
        }
        finally {
            Locale.setDefault(old);
        }
    }

    @Test
    public void test_str_format_with_local() {
        final Venice venice = new Venice();

        final Locale old = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("de", "CH")); // Java 11

            assertEquals("1,4500", venice.eval("(str/format (. :java.util.Locale :new \"de\" \"DE\") \"%.4f\" 1.45)"));
            assertEquals("1,4500", venice.eval("(str/format (. :java.util.Locale :GERMANY) \"%.4f\" 1.45)"));
            assertEquals("1,4500", venice.eval("(str/format [ \"de\" ] \"%.4f\" 1.45)"));
            assertEquals("1,4500", venice.eval("(str/format [ \"de\" \"DE\" ] \"%.4f\" 1.45)"));
        }
        finally {
            Locale.setDefault(old);
        }
    }

    @Test
    public void test_str_index_of() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/index-of nil nil)"));
        assertEquals(null, venice.eval("(str/index-of nil \"\")"));
        assertEquals(null, venice.eval("(str/index-of \"\" nil)"));
        assertEquals(null, venice.eval("(str/index-of \"\" \"\")"));

        assertEquals(null, venice.eval("(str/index-of nil \"ab\")"));
        assertEquals(null, venice.eval("(str/index-of \"abc\" nil)"));
        assertEquals(null, venice.eval("(str/index-of \"\" \"ab\")"));
        assertEquals(null, venice.eval("(str/index-of \"abc\" \"\")"));

        assertEquals(0L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\")"));
        assertEquals(6L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ef\")"));
        assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\")"));

        assertEquals(0L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\" 0)"));
        assertEquals(3L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ab\" 1)"));
        assertEquals(6L,   venice.eval("(str/index-of \"ab:ab:ef\" \"ef\" 3)"));
        assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\" 5)"));
        assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\" 99)"));
        assertEquals(null, venice.eval("(str/index-of \"ab:cd:ef\" \"xy\" -1)"));
    }

    @Test
    public void test_str_index_one_char_of() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/index-one-char-of nil nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-of nil \"\")"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"\" nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"\" \"\")"));

        assertEquals(null, venice.eval("(str/index-one-char-of nil \"ab\")"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"abc\" nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"\" \"ab\")"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"abc\" \"\")"));

        assertEquals(0L,   venice.eval("(str/index-one-char-of \"123\" \"01\")"));
        assertEquals(0L,   venice.eval("(str/index-one-char-of \"123\" \"01abcdef\")"));
        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" \"01\")"));
        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" \"12\")"));
        assertEquals(7L,   venice.eval("(str/index-one-char-of \"------123\" \"23\")"));
        assertEquals(8L,   venice.eval("(str/index-one-char-of \"------123\" \"34\")"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" \"45\")"));

        assertEquals(0L,   venice.eval("(str/index-one-char-of \"123\" (seq \"01\"))"));
        assertEquals(0L,   venice.eval("(str/index-one-char-of \"123\" (seq \"01abcdef\"))"));
        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"01\"))"));
        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"12\"))"));
        assertEquals(7L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"23\"))"));
        assertEquals(8L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"34\"))"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" (seq \"45\"))"));

        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" \"012\" 0)"));
        assertEquals(7L,   venice.eval("(str/index-one-char-of \"------123\" \"012\" 7)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" \"456\" 4)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" \"012\" 99)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" \"012\" -1)"));

        assertEquals(6L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"012\") 0)"));
        assertEquals(7L,   venice.eval("(str/index-one-char-of \"------123\" (seq \"012\") 7)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" (seq \"456\") 4)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" (seq \"012\") 99)"));
        assertEquals(null, venice.eval("(str/index-one-char-of \"------123\" (seq \"012\") -1)"));
    }

    @Test
    public void test_str_index_one_char_not_of() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/index-one-char-not-of nil nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of nil \"\")"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"\" nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"\" \"\")"));

        assertEquals(null, venice.eval("(str/index-one-char-not-of nil \"ab\")"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"abc\" nil)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"\" \"ab\")"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"abc\" \"\")"));

        assertEquals(0L,   venice.eval("(str/index-one-char-not-of \"123456\" \"abc\")"));
        assertEquals(1L,   venice.eval("(str/index-one-char-not-of \"123456\" \"1abc\")"));
        assertEquals(2L,   venice.eval("(str/index-one-char-not-of \"123456\" \"12abc\")"));
        assertEquals(3L,   venice.eval("(str/index-one-char-not-of \"123456\" \"123abc\")"));
        assertEquals(4L,   venice.eval("(str/index-one-char-not-of \"123456\" \"1234abc\")"));
        assertEquals(5L,   venice.eval("(str/index-one-char-not-of \"123456\" \"12345abc\")"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" \"123456abc\")"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" \"1234567abc\")"));

        assertEquals(0L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"abc\"))"));
        assertEquals(1L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"1abc\"))"));
        assertEquals(2L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"12abc\"))"));
        assertEquals(3L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"123abc\"))"));
        assertEquals(4L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"1234abc\"))"));
        assertEquals(5L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"12345abc\"))"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" (seq \"123456abc\"))"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" (seq \"1234567abc\"))"));

        assertEquals(0L,   venice.eval("(str/index-one-char-not-of \"123456\" \"abc\" 0)"));
        assertEquals(4L,   venice.eval("(str/index-one-char-not-of \"123456\" \"34abc\" 2)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" \"123456\" 4)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" \"12\" 99)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" \"12\" -1)"));

        assertEquals(0L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"abc\") 0)"));
        assertEquals(4L,   venice.eval("(str/index-one-char-not-of \"123456\" (seq \"34abc\") 2)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" (seq \"123456\") 4)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" (seq \"12\") 99)"));
        assertEquals(null, venice.eval("(str/index-one-char-not-of \"123456\" (seq \"12\") -1)"));
    }

    @Test
    public void test_str_join() {
        final Venice venice = new Venice();

        assertEquals("", venice.eval("(str/join nil)"));
        assertEquals("", venice.eval("(str/join \"\" nil)"));
        assertEquals("", venice.eval("(str/join \"-\" nil)"));
        assertEquals("", venice.eval("(str/join #\\- nil)"));

        assertEquals("", venice.eval("(str/join '())"));
        assertEquals("", venice.eval("(str/join \"\" '())"));
        assertEquals("", venice.eval("(str/join \"-\" '())"));
        assertEquals("", venice.eval("(str/join #\\- '())"));

        assertEquals("ab", venice.eval("(str/join '(\"ab\"))"));
        assertEquals("ab", venice.eval("(str/join \"\" '(\"ab\"))"));
        assertEquals("ab", venice.eval("(str/join \"-\" '(\"ab\"))"));
        assertEquals("ab", venice.eval("(str/join #\\- '(\"ab\"))"));

        assertEquals("abcdef", venice.eval("(str/join '(\"ab\" \"cd\" \"ef\"))"));
        assertEquals("abcdef", venice.eval("(str/join \"\" '(\"ab\" \"cd\" \"ef\"))"));
        assertEquals("ab-cd-ef", venice.eval("(str/join \"-\" '(\"ab\" \"cd\" \"ef\"))"));
        assertEquals("ab-cd-ef", venice.eval("(str/join #\\- '(\"ab\" \"cd\" \"ef\"))"));

        assertEquals("abcdef", venice.eval("(str/join (str/chars \"abcdef\"))"));
    }

    @Test
    public void test_str_last_index_of() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/last-index-of nil nil)"));
        assertEquals(null, venice.eval("(str/last-index-of nil \"\")"));
        assertEquals(null, venice.eval("(str/last-index-of \"\" nil)"));
        assertEquals(null, venice.eval("(str/last-index-of \"\" \"\")"));

        assertEquals(null, venice.eval("(str/last-index-of nil \"ab\")"));
        assertEquals(null, venice.eval("(str/last-index-of \"abc\" nil)"));
        assertEquals(null, venice.eval("(str/last-index-of \"\" \"ab\")"));
        assertEquals(null, venice.eval("(str/last-index-of \"abc\" \"\")"));

        assertEquals(3L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\")"));
        assertEquals(6L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ef\")"));
        assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\")"));

        assertEquals(0L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 0)"));
        assertEquals(3L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 99)"));
        assertEquals(0L,   venice.eval("(str/last-index-of \"ab:ab:ef\" \"ab\" 1)"));
        assertEquals(null, venice.eval("(str/last-index-of \"ab:ab:ef\" \"ef\" 3)"));
        assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\" 5)"));
        assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\" 99)"));
        assertEquals(null, venice.eval("(str/last-index-of \"ab:cd:ef\" \"xy\" -1)"));
    }

    @Test
    public void test_str_letter_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/letter? #\\x)"));
        assertFalse((Boolean)venice.eval("(str/letter? #\\8)"));
    }

    @Test
    public void test_str_letter_or_digit_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/letter-or-digit? #\\x)"));
        assertTrue((Boolean)venice.eval("(str/letter-or-digit? #\\X)"));
        assertTrue((Boolean)venice.eval("(str/letter-or-digit? #\\8)"));
        assertFalse((Boolean)venice.eval("(str/letter-or-digit? #\\!)"));
    }

    @Test
    public void test_str_levenshtein() {
        final Venice venice = new Venice();

        assertEquals(2L, venice.eval("(str/levenshtein \"Tier\" \"Tor\")"));
        assertEquals(3L, venice.eval("(str/levenshtein \"tier\" \"Tor\")"));
    }

    @Test
    public void test_str_linefeed_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/linefeed? #\\newline)"));
        assertTrue((Boolean)venice.eval("(str/linefeed? (first \"\n\"))"));

        assertEquals("#\\newline", venice.eval("(pr-str (first \"\n----\"))"));
    }

    @Test
    public void test_str_lower_case() {
        final Venice venice = new Venice();

        // chars
        assertEquals('a', (Character)venice.eval("(str/lower-case #\\A)"));

        // strings
        assertEquals("abcdef", venice.eval("(str/lower-case \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/lower-case \"aBcDeF\")"));
        assertEquals("abcdef", venice.eval("(str/lower-case (. :java.util.Locale :new \"de\" \"DE\") \"aBcDeF\")"));
        assertEquals("abcdef", venice.eval("(str/lower-case [\"de\" \"DE\"] \"aBcDeF\")"));
    }

    @Test
    public void test_str_lower_case_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/lower-case? #\\a)"));
        assertFalse((Boolean)venice.eval("(str/lower-case? #\\A)"));
        assertFalse((Boolean)venice.eval("(str/lower-case? #\\8)"));
    }

    @Test
    public void test_str_nfirst() {
        final Venice venice = new Venice();

        assertEquals("",    venice.eval("(str/nfirst \"abc\" 0)"));
        assertEquals("a",   venice.eval("(str/nfirst \"abc\" 1)"));
        assertEquals("ab",  venice.eval("(str/nfirst \"abc\" 2)"));
        assertEquals("abc", venice.eval("(str/nfirst \"abc\" 3)"));
        assertEquals("abc", venice.eval("(str/nfirst \"abc\" 4)"));
    }

    @Test
    public void test_str_nlast() {
        final Venice venice = new Venice();

        assertEquals("",    venice.eval("(str/nlast \"abc\" 0)"));
        assertEquals("c",   venice.eval("(str/nlast \"abc\" 1)"));
        assertEquals("bc",  venice.eval("(str/nlast \"abc\" 2)"));
        assertEquals("abc", venice.eval("(str/nlast \"abc\" 3)"));
        assertEquals("abc", venice.eval("(str/nlast \"abc\" 4)"));
    }

    @Test
    public void test_str_pos() {
        final Venice venice = new Venice();

        assertEquals(0L, venice.eval("(:row (str/pos \"0\" 0))"));
        assertEquals(0L, venice.eval("(:col (str/pos \"0\" 0))"));

        assertEquals(-1L, venice.eval("(:row (str/pos \"0\" 2))"));
        assertEquals(-1L, venice.eval("(:col (str/pos \"0\" 2))"));

        assertEquals(0L, venice.eval("(:row (str/pos \"0123456789\" 0))"));
        assertEquals(0L, venice.eval("(:col (str/pos \"0123456789\" 0))"));

        assertEquals(0L, venice.eval("(:row (str/pos \"0123456789\" 3))"));
        assertEquals(3L, venice.eval("(:col (str/pos \"0123456789\" 3))"));

        assertEquals(0L, venice.eval("(:row (str/pos \"01234\n56789\" 3))"));
        assertEquals(3L, venice.eval("(:col (str/pos \"01234\n56789\" 3))"));

        assertEquals(0L, venice.eval("(:row (str/pos \"01234\n56789\" 4))"));
        assertEquals(4L, venice.eval("(:col (str/pos \"01234\n56789\" 4))"));

        assertEquals(0L, venice.eval("(:row (str/pos \"01234\n56789\" 5))"));
        assertEquals(5L, venice.eval("(:col (str/pos \"01234\n56789\" 5))"));

        assertEquals(1L, venice.eval("(:row (str/pos \"01234\n56789\" 6))"));
        assertEquals(0L, venice.eval("(:col (str/pos \"01234\n56789\" 6))"));
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

        assertEquals("", venice.eval("(str/repeat #\\* 0)"));
        assertEquals("***", venice.eval("(str/repeat #\\* 3)"));
        assertEquals("*-*-*", venice.eval("(str/repeat #\\* 3 #\\-)"));
    }

    @Test
    public void test_str_replace_all() {
        final Venice venice = new Venice();

        assertEquals("ab:ab:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"HH\" \"xy\"))"));
        assertEquals("xy:xy:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"xy\"))"));
        assertEquals("xy:xy:xy", venice.eval("(str (str/replace-all \"ab:ab:ab\" \"ab\" \"xy\"))"));
        assertEquals("ab:cd:xy", venice.eval("(str (str/replace-all \"ab:cd:ef\" \"ef\" \"xy\"))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-all \"ab:cd:ef\" \"cd\" \"xy\"))"));

        assertEquals("::ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"\"))"));
        assertEquals("ab::", venice.eval("(str (str/replace-all \"ab:ef:ef\" \"ef\" \"\"))"));

        assertEquals("x:x:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"x\"))"));
        assertEquals("ab:x:x", venice.eval("(str (str/replace-all \"ab:ef:ef\" \"ef\" \"x\"))"));

        assertEquals("xy:xy:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"xy\"))"));
        assertEquals("ab:xy:xy", venice.eval("(str (str/replace-all \"ab:ef:ef\" \"ef\" \"xy\"))"));

        assertEquals("xyz:xyz:ef", venice.eval("(str (str/replace-all \"ab:ab:ef\" \"ab\" \"xyz\"))"));
        assertEquals("ab:xyz:xyz", venice.eval("(str (str/replace-all \"ab:ef:ef\" \"ef\" \"xyz\"))"));

        assertEquals("", venice.eval("(str (str/replace-all \"ab\" \"ab\" \"\"))"));
        assertEquals("x", venice.eval("(str (str/replace-all \"ab\" \"ab\" \"x\"))"));
        assertEquals("xy", venice.eval("(str (str/replace-all \"ab\" \"ab\" \"xy\"))"));
        assertEquals("xyz", venice.eval("(str (str/replace-all \"ab\" \"ab\" \"xyz\"))"));
    }

    @Test
    public void test_str_replace_all_regex() {
        final Venice venice = new Venice();

        assertEquals("ab:_:ef", venice.eval("(str (str/replace-all \"ab:0:ef\" (regex/pattern \"[0-9]+\") \"_\"))"));
        assertEquals("ab:_:ef", venice.eval("(str (str/replace-all \"ab:00:ef\" (regex/pattern \"[0-9]+\") \"_\"))"));
        assertEquals("ab:_:ef", venice.eval("(str (str/replace-all \"ab:000:ef\" (regex/pattern \"[0-9]+\") \"_\"))"));

        assertEquals("ab:___:ef", venice.eval("(str (str/replace-all \"ab:0:ef\" (regex/pattern \"[0-9]+\") \"___\"))"));
        assertEquals("ab:___:ef", venice.eval("(str (str/replace-all \"ab:00:ef\" (regex/pattern \"[0-9]+\") \"___\"))"));
        assertEquals("ab:___:ef", venice.eval("(str (str/replace-all \"ab:000:ef\" (regex/pattern \"[0-9]+\") \"___\"))"));

        assertEquals("ab:cd:___", venice.eval("(str (str/replace-all \"ab:cd:0\" (regex/pattern \"[0-9]+\") \"___\"))"));
        assertEquals("ab:cd:___", venice.eval("(str (str/replace-all \"ab:cd:00\" (regex/pattern \"[0-9]+\") \"___\"))"));
        assertEquals("ab:cd:___", venice.eval("(str (str/replace-all \"ab:cd:000\" (regex/pattern \"[0-9]+\") \"___\"))"));
    }

    @Test
    public void test_str_replace_first() {
        final Venice venice = new Venice();

        assertEquals("ab:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"HH\" \"xy\"))"));
        assertEquals("xy:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xy\"))"));
        assertEquals("ab:cd:xy", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"ef\" \"xy\"))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"cd\" \"xy\"))"));

        assertEquals(":ab:ef",    venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"\"))"));
        assertEquals("x:ab:ef",   venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"x\"))"));
        assertEquals("xy:ab:ef",  venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xy\"))"));
        assertEquals("xyz:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xyz\"))"));

        assertEquals("",    venice.eval("(str (str/replace-first \"ab\" \"ab\" \"\"))"));
        assertEquals("x",   venice.eval("(str (str/replace-first \"ab\" \"ab\" \"x\"))"));
        assertEquals("xy",  venice.eval("(str (str/replace-first \"ab\" \"ab\" \"xy\"))"));
        assertEquals("xyz", venice.eval("(str (str/replace-first \"ab\" \"ab\" \"xyz\"))"));
    }

    @Test
    public void test_str_replace_first_ignoreCase() {
        final Venice venice = new Venice();

        // ignore-case false (matching)
        assertEquals("ab:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"HH\" \"xy\" :ignore-case false))"));
        assertEquals("xy:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("ab:cd:xy", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-first \"ab:cd:ef\" \"cd\" \"xy\" :ignore-case false))"));

        assertEquals(":ab:ef",    venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("x:ab:ef",   venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("xy:ab:ef",  venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("xyz:ab:ef", venice.eval("(str (str/replace-first \"ab:ab:ef\" \"ab\" \"xyz\" :ignore-case false))"));

        assertEquals("",    venice.eval("(str (str/replace-first \"ab\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("x",   venice.eval("(str (str/replace-first \"ab\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("xy",  venice.eval("(str (str/replace-first \"ab\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("xyz", venice.eval("(str (str/replace-first \"ab\" \"ab\" \"xyz\" :ignore-case false))"));

        // ignore-case false (not matching)
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"HH\" \"xy\" :ignore-case false))"));
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("AB:CD:EF", venice.eval("(str (str/replace-first \"AB:CD:EF\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("AB:CD:EF", venice.eval("(str (str/replace-first \"AB:CD:EF\" \"cd\" \"xy\" :ignore-case false))"));

        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xyz\" :ignore-case false))"));

        assertEquals("AB", venice.eval("(str (str/replace-first \"AB\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-first \"AB\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-first \"AB\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-first \"AB\" \"ab\" \"xyz\" :ignore-case false))"));

        // ignore-case true
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"HH\" \"xy\" :ignore-case true))"));
        assertEquals("xy:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case true))"));
        assertEquals("AB:CD:xy", venice.eval("(str (str/replace-first \"AB:CD:EF\" \"ef\" \"xy\" :ignore-case true))"));
        assertEquals("AB:xy:EF", venice.eval("(str (str/replace-first \"AB:CD:EF\" \"cd\" \"xy\" :ignore-case true))"));

        assertEquals(":AB:EF",    venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"\" :ignore-case true))"));
        assertEquals("x:AB:EF",   venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"x\" :ignore-case true))"));
        assertEquals("xy:AB:EF",  venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case true))"));
        assertEquals("xyz:AB:EF", venice.eval("(str (str/replace-first \"AB:AB:EF\" \"ab\" \"xyz\" :ignore-case true))"));

        assertEquals("",    venice.eval("(str (str/replace-first \"AB\" \"ab\" \"\" :ignore-case true))"));
        assertEquals("x",   venice.eval("(str (str/replace-first \"AB\" \"ab\" \"x\" :ignore-case true))"));
        assertEquals("xy",  venice.eval("(str (str/replace-first \"AB\" \"ab\" \"xy\" :ignore-case true))"));
        assertEquals("xyz", venice.eval("(str (str/replace-first \"AB\" \"ab\" \"xyz\" :ignore-case true))"));
    }

    @Test
    public void test_str_replace_last() {
        final Venice venice = new Venice();

        assertEquals("ab:ab:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"HH\" \"xy\"))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"ab\" \"xy\"))"));
        assertEquals("ab:cd:xy", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"ef\" \"xy\"))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"cd\" \"xy\"))"));

        assertEquals("ab:ef:",    venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"\"))"));
        assertEquals("ab:ef:x",   venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"x\"))"));
        assertEquals("ab:ef:xy",  venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"xy\"))"));
        assertEquals("ab:ef:xyz", venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"xyz\"))"));

        assertEquals("",    venice.eval("(str (str/replace-last \"ab\" \"ab\" \"\"))"));
        assertEquals("x",   venice.eval("(str (str/replace-last \"ab\" \"ab\" \"x\"))"));
        assertEquals("xy",  venice.eval("(str (str/replace-last \"ab\" \"ab\" \"xy\"))"));
        assertEquals("xyz", venice.eval("(str (str/replace-last \"ab\" \"ab\" \"xyz\"))"));
    }

    @Test
    public void test_str_replace_last_ignore_case() {
        final Venice venice = new Venice();

        // ignore-case false (matching)
        assertEquals("ab:ab:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"HH\" \"xy\" :ignore-case false))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:ab:ef\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("ab:cd:xy", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("ab:xy:ef", venice.eval("(str (str/replace-last \"ab:cd:ef\" \"cd\" \"xy\" :ignore-case false))"));

        assertEquals("ab:ef:",    venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"\" :ignore-case false))"));
        assertEquals("ab:ef:x",   venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"x\" :ignore-case false))"));
        assertEquals("ab:ef:xy",  venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("ab:ef:xyz", venice.eval("(str (str/replace-last \"ab:ef:ef\" \"ef\" \"xyz\" :ignore-case false))"));

        assertEquals("",    venice.eval("(str (str/replace-last \"ab\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("x",   venice.eval("(str (str/replace-last \"ab\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("xy",  venice.eval("(str (str/replace-last \"ab\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("xyz", venice.eval("(str (str/replace-last \"ab\" \"ab\" \"xyz\" :ignore-case false))"));

        // ignore-case false (not matching)
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-last \"AB:AB:EF\" \"HH\" \"xy\" :ignore-case false))"));
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-last \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("AB:CD:EF", venice.eval("(str (str/replace-last \"AB:CD:EF\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("AB:CD:EF", venice.eval("(str (str/replace-last \"AB:CD:EF\" \"cd\" \"xy\" :ignore-case false))"));

        assertEquals("AB:EF:EF", venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"\" :ignore-case false))"));
        assertEquals("AB:EF:EF", venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"x\" :ignore-case false))"));
        assertEquals("AB:EF:EF", venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"xy\" :ignore-case false))"));
        assertEquals("AB:EF:EF", venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"xyz\" :ignore-case false))"));

        assertEquals("AB", venice.eval("(str (str/replace-last \"AB\" \"ab\" \"\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-last \"AB\" \"ab\" \"x\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-last \"AB\" \"ab\" \"xy\" :ignore-case false))"));
        assertEquals("AB", venice.eval("(str (str/replace-last \"AB\" \"ab\" \"xyz\" :ignore-case false))"));

        // ignore-case true (matching)
        assertEquals("AB:AB:EF", venice.eval("(str (str/replace-last \"AB:AB:EF\" \"HH\" \"xy\" :ignore-case true))"));
        assertEquals("AB:xy:EF", venice.eval("(str (str/replace-last \"AB:AB:EF\" \"ab\" \"xy\" :ignore-case true))"));
        assertEquals("AB:CD:xy", venice.eval("(str (str/replace-last \"AB:CD:EF\" \"ef\" \"xy\" :ignore-case true))"));
        assertEquals("AB:xy:EF", venice.eval("(str (str/replace-last \"AB:CD:EF\" \"cd\" \"xy\" :ignore-case true))"));

        assertEquals("AB:EF:",    venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"\" :ignore-case true))"));
        assertEquals("AB:EF:x",   venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"x\" :ignore-case true))"));
        assertEquals("AB:EF:xy",  venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"xy\" :ignore-case true))"));
        assertEquals("AB:EF:xyz", venice.eval("(str (str/replace-last \"AB:EF:EF\" \"ef\" \"xyz\" :ignore-case true))"));

        assertEquals("",    venice.eval("(str (str/replace-last \"AB\" \"ab\" \"\" :ignore-case true))"));
        assertEquals("x",   venice.eval("(str (str/replace-last \"AB\" \"ab\" \"x\" :ignore-case true))"));
        assertEquals("xy",  venice.eval("(str (str/replace-last \"AB\" \"ab\" \"xy\" :ignore-case true))"));
        assertEquals("xyz", venice.eval("(str (str/replace-last \"AB\" \"ab\" \"xyz\" :ignore-case true))"));
    }

    @Test
    public void test_str_butlast() {
        final Venice venice = new Venice();

        assertEquals("12", venice.eval("(str/butlast \"123\")"));
        assertEquals("1",  venice.eval("(str/butlast \"12\")"));
        assertEquals("",   venice.eval("(str/butlast \"1\")"));
        assertEquals("",   venice.eval("(str/butlast \"\")"));
        assertEquals(null, venice.eval("(str/butlast nil)"));
    }

    @Test
    public void test_str_butnlast() {
        final Venice venice = new Venice();

        assertEquals("123", venice.eval("(str/butnlast \"123\" 0)"));
        assertEquals("12",  venice.eval("(str/butnlast \"12\" 0)"));
        assertEquals("1",   venice.eval("(str/butnlast \"1\" 0)"));
        assertEquals("",    venice.eval("(str/butnlast \"\" 0)"));
        assertEquals(null,  venice.eval("(str/butnlast nil 0)"));

        assertEquals("12", venice.eval("(str/butnlast \"123\" 1)"));
        assertEquals("1",  venice.eval("(str/butnlast \"12\" 1)"));
        assertEquals("",   venice.eval("(str/butnlast \"1\" 1)"));
        assertEquals("",   venice.eval("(str/butnlast \"\" 1)"));
        assertEquals(null, venice.eval("(str/butnlast nil 1)"));

        assertEquals("1",  venice.eval("(str/butnlast \"123\" 2)"));
        assertEquals("",   venice.eval("(str/butnlast \"12\" 2)"));
        assertEquals("",   venice.eval("(str/butnlast \"1\" 2)"));
        assertEquals("",   venice.eval("(str/butnlast \"\" 2)"));
        assertEquals(null, venice.eval("(str/butnlast nil 2)"));

        assertEquals("",   venice.eval("(str/butnlast \"123\" 3)"));
        assertEquals("",   venice.eval("(str/butnlast \"12\" 3)"));
        assertEquals("",   venice.eval("(str/butnlast \"1\" 3)"));
        assertEquals("",   venice.eval("(str/butnlast \"\" 3)"));
        assertEquals(null, venice.eval("(str/butnlast nil 3)"));
   }

    @Test
    public void test_str_rest() {
        final Venice venice = new Venice();

        assertEquals("23", venice.eval("(str/rest \"123\")"));
        assertEquals("2",  venice.eval("(str/rest \"12\")"));
        assertEquals("",   venice.eval("(str/rest \"1\")"));
        assertEquals("",   venice.eval("(str/rest \"\")"));
        assertEquals(null, venice.eval("(str/rest nil)"));
    }

    @Test
    public void test_str_nrest() {
        final Venice venice = new Venice();

        assertEquals("123", venice.eval("(str/nrest \"123\" 0)"));
        assertEquals("12",  venice.eval("(str/nrest \"12\" 0)"));
        assertEquals("1",   venice.eval("(str/nrest \"1\" 0)"));
        assertEquals("",    venice.eval("(str/nrest \"\" 0)"));
        assertEquals(null,  venice.eval("(str/nrest nil 0)"));

        assertEquals("23", venice.eval("(str/nrest \"123\" 1)"));
        assertEquals("2",  venice.eval("(str/nrest \"12\" 1)"));
        assertEquals("",   venice.eval("(str/nrest \"1\" 1)"));
        assertEquals("",   venice.eval("(str/nrest \"\" 1)"));
        assertEquals(null, venice.eval("(str/nrest nil 1)"));

        assertEquals("3",  venice.eval("(str/nrest \"123\" 2)"));
        assertEquals("",   venice.eval("(str/nrest \"12\" 2)"));
        assertEquals("",   venice.eval("(str/nrest \"1\" 2)"));
        assertEquals("",   venice.eval("(str/nrest \"\" 2)"));
        assertEquals(null, venice.eval("(str/nrest nil 2)"));

        assertEquals("",   venice.eval("(str/nrest \"123\" 3)"));
        assertEquals("",   venice.eval("(str/nrest \"12\" 3)"));
        assertEquals("",   venice.eval("(str/nrest \"1\" 3)"));
        assertEquals("",   venice.eval("(str/nrest \"\" 3)"));
        assertEquals(null, venice.eval("(str/nrest nil 3)"));
   }

    @Test
    public void test_str_reverse() {
        final Venice venice = new Venice();

        assertEquals("321", venice.eval("(str/reverse \"123\")"));
        assertEquals("21", venice.eval("(str/reverse \"12\")"));
        assertEquals("1", venice.eval("(str/reverse \"1\")"));
        assertEquals("", venice.eval("(str/reverse \"\")"));
        assertEquals(null, venice.eval("(str/reverse nil)"));
    }

    @Test
    public void test_str_split() {
        final Venice venice = new Venice();

        assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" \":\"))"));
        assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" \" *: *\"))"));
        assertEquals("(ab:cd:ef)", venice.eval("(str (str/split \"ab:cd:ef\" \" +\"))"));

        assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" #\":\"))"));
        assertEquals("(ab cd ef)", venice.eval("(str (str/split \"ab:cd:ef\" #\" *: *\"))"));
        assertEquals("(ab:cd:ef)", venice.eval("(str (str/split \"ab:cd:ef\" #\" +\"))"));

        assertEquals(
            "(\"q\" \"w\" \"e\" \"r\" \"t\" \"y\" \"u\" \"i\" \"o\" \"p\")",
            venice.eval("(pr-str (str/split \"q1w2e3r4t5y6u7i8o9p0\" #\"\\d+\"))"));
        assertEquals(
            "(\"q\" \"w\" \"e\" \"r\" \"t5y6u7i8o9p0\")",
            venice.eval("(pr-str (str/split \"q1w2e3r4t5y6u7i8o9p0\" #\"\\d+\" 5))"));
    }

    @Test
    public void test_str_split_at() {
        final Venice venice = new Venice();

        assertEquals("(\"\" \"\")", venice.eval("(pr-str (str/split-at nil 1))"));
        assertEquals("(\"\" \"\")", venice.eval("(pr-str (str/split-at \"\" 1))"));
        assertEquals("(\"\" \"abc\")", venice.eval("(pr-str (str/split-at \"abc\" 0))"));
        assertEquals("(\"a\" \"bc\")", venice.eval("(pr-str (str/split-at \"abc\" 1))"));
        assertEquals("(\"ab\" \"c\")", venice.eval("(pr-str (str/split-at \"abc\" 2))"));
        assertEquals("(\"abc\" \"\")", venice.eval( "(pr-str (str/split-at \"abc\" 3))"));
    }

    @Test
    public void test_str_split_lines() {
        final Venice venice = new Venice();

        assertEquals("()", venice.eval("(str (str/split-lines nil))"));
        assertEquals("(ab)", venice.eval("(str (str/split-lines \"ab\"))"));
        assertEquals("(ab cd ef)", venice.eval("(str (str/split-lines \"ab\ncd\nef\"))"));
    }

    @Test
    public void test_str_split_columns() {
        final Venice venice = new Venice();

        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \"\"        [0 1 2]))"));
        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \" \",      [0 1 2]))"));
        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \"  \",     [0 1 2]))"));
        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \"   \",    [0 1 2]))"));
        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \"    \",   [0 1 2]))"));

        assertEquals("1|2|3",    venice.eval("(str/join \"|\" (str/split-columns \"123\",    [0 1 2]))"));
        assertEquals("12|34|56", venice.eval("(str/join \"|\" (str/split-columns \"123456\", [0 2 4]))"));
        assertEquals("1|2|3",    venice.eval("(str/join \"|\" (str/split-columns \"1 2 3 \", [0 2 4]))"));

        assertEquals("||123",    venice.eval("(str/join \"|\" (str/split-columns \"123\",    [1 0 0]))"));
        assertEquals("||123",    venice.eval("(str/join \"|\" (str/split-columns \"123\",    [0 1 0]))"));
        assertEquals("||123",    venice.eval("(str/join \"|\" (str/split-columns \"123\",    [0 0 0]))"));
        assertEquals("|12|3",    venice.eval("(str/join \"|\" (str/split-columns \"123\",    [0 0 2]))"));

        assertEquals("123|456|", venice.eval("(str/join \"|\" (str/split-columns \"123456\", [0 3 6]))"));
        assertEquals("123456||", venice.eval("(str/join \"|\" (str/split-columns \"123456\", [0 6 12]))"));
        assertEquals("||",       venice.eval("(str/join \"|\" (str/split-columns \"123456\", [20 30 40]))"));
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

        assertEquals("abcdef", venice.eval("(str/strip-end \"abcdef\" \"\")"));

        assertEquals("abc", venice.eval("(str/strip-end \"abcdef\" \"def\")"));
        assertEquals("abcdef", venice.eval("(str/strip-end \"abcdef\" \"abc\")"));
        assertEquals("", venice.eval("(str/strip-end \"abcdef\" \"abcdef\")"));
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
        assertEquals("  abc", venice.eval("(str/strip-margin \"  abc\")"));
        assertEquals("line1\n  line2\n  line3", venice.eval("(str/strip-margin \"line1\n  |  line2\n  |  line3\")"));
    }

    @Test
    public void test_str_strip_start() {
        final Venice venice = new Venice();

        assertEquals("abcdef", venice.eval("(str/strip-start \"abcdef\" \"\")"));

        assertEquals("def", venice.eval("(str/strip-start \"abcdef\" \"abc\")"));
        assertEquals("abcdef", venice.eval("(str/strip-start \"abcdef\" \"def\")"));
        assertEquals("", venice.eval("(str/strip-start \"abcdef\" \"abcdef\")"));
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
    public void test_str_trim_left() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/trim-left nil)"));
        assertEquals("", venice.eval("(str/trim-left \"\")"));
        assertEquals("abcdef", venice.eval("(str/trim-left \"abcdef\")"));
        assertEquals("abcdef  ", venice.eval("(str/trim-left \"  abcdef  \")"));
        assertEquals("abcdef", venice.eval("(str/trim-left \"  abcdef\")"));
        assertEquals("abcdef  ", venice.eval("(str/trim-left \"abcdef  \")"));
        assertEquals("", venice.eval("(str/trim-left \"  \")"));
        assertEquals("", venice.eval("(str/trim-left \" \f\t\r\n \")"));
    }

    @Test
    public void test_str_trim_right() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(str/trim-right nil)"));
        assertEquals("", venice.eval("(str/trim-right \"\")"));
        assertEquals("abcdef", venice.eval("(str/trim-right \"abcdef\")"));
        assertEquals("  abcdef", venice.eval("(str/trim-right \"  abcdef  \")"));
        assertEquals("  abcdef", venice.eval("(str/trim-right \"  abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/trim-right \"abcdef  \")"));
        assertEquals("", venice.eval("(str/trim-right \"  \")"));
        assertEquals("", venice.eval("(str/trim-right \" \f\t\r\n \")"));
    }

    @Test
    public void test_str_align() {
        final Venice venice = new Venice();

        assertEquals("      ", venice.eval("(str/align 6 :left   :clip-left \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :center :clip-left \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :right  :clip-left \"\")"));

        assertEquals("      ", venice.eval("(str/align 6 :left   :clip-right \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :center :clip-right \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :right  :clip-right \"\")"));

        assertEquals("      ", venice.eval("(str/align 6 :left   :ellipsis-left \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :center :ellipsis-left \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :right  :ellipsis-left \"\")"));

        assertEquals("      ", venice.eval("(str/align 6 :left   :ellipsis-right \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :center :ellipsis-right \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :right  :ellipsis-right \"\")"));

        assertEquals("      ", venice.eval("(str/align 6 :left   :newline \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :center :newline \"\")"));
        assertEquals("      ", venice.eval("(str/align 6 :right  :newline \"\")"));


        assertEquals("a     ", venice.eval("(str/align 6 :left   :clip-left \"a\")"));
        assertEquals("   a  ", venice.eval("(str/align 6 :center :clip-left \"a\")"));
        assertEquals("     a", venice.eval("(str/align 6 :right  :clip-left \"a\")"));

        assertEquals("a     ", venice.eval("(str/align 6 :left   :clip-right \"a\")"));
        assertEquals("   a  ", venice.eval("(str/align 6 :center :clip-right \"a\")"));
        assertEquals("     a", venice.eval("(str/align 6 :right  :clip-right \"a\")"));

        assertEquals("a     ", venice.eval("(str/align 6 :left   :ellipsis-left \"a\")"));
        assertEquals("   a  ", venice.eval("(str/align 6 :center :ellipsis-left \"a\")"));
        assertEquals("     a", venice.eval("(str/align 6 :right  :ellipsis-left \"a\")"));

        assertEquals("a     ", venice.eval("(str/align 6 :left   :ellipsis-right \"a\")"));
        assertEquals("   a  ", venice.eval("(str/align 6 :center :ellipsis-right \"a\")"));
        assertEquals("     a", venice.eval("(str/align 6 :right  :ellipsis-right \"a\")"));

        assertEquals("a     ", venice.eval("(str/align 6 :left   :newline \"a\")"));
        assertEquals("   a  ", venice.eval("(str/align 6 :center :newline \"a\")"));
        assertEquals("     a", venice.eval("(str/align 6 :right  :newline \"a\")"));


        assertEquals("ab    ", venice.eval("(str/align 6 :left   :clip-left \"ab\")"));
        assertEquals("  ab  ", venice.eval("(str/align 6 :center :clip-left \"ab\")"));
        assertEquals("    ab", venice.eval("(str/align 6 :right  :clip-left \"ab\")"));

        assertEquals("ab    ", venice.eval("(str/align 6 :left   :clip-right \"ab\")"));
        assertEquals("  ab  ", venice.eval("(str/align 6 :center :clip-right \"ab\")"));
        assertEquals("    ab", venice.eval("(str/align 6 :right  :clip-right \"ab\")"));

        assertEquals("ab    ", venice.eval("(str/align 6 :left   :ellipsis-left \"ab\")"));
        assertEquals("  ab  ", venice.eval("(str/align 6 :center :ellipsis-left \"ab\")"));
        assertEquals("    ab", venice.eval("(str/align 6 :right  :ellipsis-left \"ab\")"));

        assertEquals("ab    ", venice.eval("(str/align 6 :left   :ellipsis-right \"ab\")"));
        assertEquals("  ab  ", venice.eval("(str/align 6 :center :ellipsis-right \"ab\")"));
        assertEquals("    ab", venice.eval("(str/align 6 :right  :ellipsis-right \"ab\")"));

        assertEquals("ab    ", venice.eval("(str/align 6 :left   :newline \"ab\")"));
        assertEquals("  ab  ", venice.eval("(str/align 6 :center :newline \"ab\")"));
        assertEquals("    ab", venice.eval("(str/align 6 :right  :newline \"ab\")"));


        assertEquals("abc   ", venice.eval("(str/align 6 :left   :clip-left \"abc\")"));
        assertEquals("  abc ", venice.eval("(str/align 6 :center :clip-left \"abc\")"));
        assertEquals("   abc", venice.eval("(str/align 6 :right  :clip-left \"abc\")"));

        assertEquals("abc   ", venice.eval("(str/align 6 :left   :clip-right \"abc\")"));
        assertEquals("  abc ", venice.eval("(str/align 6 :center :clip-right \"abc\")"));
        assertEquals("   abc", venice.eval("(str/align 6 :right  :clip-right \"abc\")"));

        assertEquals("abc   ", venice.eval("(str/align 6 :left   :ellipsis-left \"abc\")"));
        assertEquals("  abc ", venice.eval("(str/align 6 :center :ellipsis-left \"abc\")"));
        assertEquals("   abc", venice.eval("(str/align 6 :right  :ellipsis-left \"abc\")"));

        assertEquals("abc   ", venice.eval("(str/align 6 :left   :ellipsis-right \"abc\")"));
        assertEquals("  abc ", venice.eval("(str/align 6 :center :ellipsis-right \"abc\")"));
        assertEquals("   abc", venice.eval("(str/align 6 :right  :ellipsis-right \"abc\")"));

        assertEquals("abc   ", venice.eval("(str/align 6 :left   :newline \"abc\")"));
        assertEquals("  abc ", venice.eval("(str/align 6 :center :newline \"abc\")"));
        assertEquals("   abc", venice.eval("(str/align 6 :right  :newline \"abc\")"));


        assertEquals("abcd  ", venice.eval("(str/align 6 :left   :clip-left \"abcd\")"));
        assertEquals(" abcd ", venice.eval("(str/align 6 :center :clip-left \"abcd\")"));
        assertEquals("  abcd", venice.eval("(str/align 6 :right  :clip-left \"abcd\")"));

        assertEquals("abcd  ", venice.eval("(str/align 6 :left   :clip-right \"abcd\")"));
        assertEquals(" abcd ", venice.eval("(str/align 6 :center :clip-right \"abcd\")"));
        assertEquals("  abcd", venice.eval("(str/align 6 :right  :clip-right \"abcd\")"));

        assertEquals("abcd  ", venice.eval("(str/align 6 :left   :ellipsis-left \"abcd\")"));
        assertEquals(" abcd ", venice.eval("(str/align 6 :center :ellipsis-left \"abcd\")"));
        assertEquals("  abcd", venice.eval("(str/align 6 :right  :ellipsis-left \"abcd\")"));

        assertEquals("abcd  ", venice.eval("(str/align 6 :left   :ellipsis-right \"abcd\")"));
        assertEquals(" abcd ", venice.eval("(str/align 6 :center :ellipsis-right \"abcd\")"));
        assertEquals("  abcd", venice.eval("(str/align 6 :right  :ellipsis-right \"abcd\")"));

        assertEquals("abcd  ", venice.eval("(str/align 6 :left   :newline \"abcd\")"));
        assertEquals(" abcd ", venice.eval("(str/align 6 :center :newline \"abcd\")"));
        assertEquals("  abcd", venice.eval("(str/align 6 :right  :newline \"abcd\")"));


        assertEquals("abcde ", venice.eval("(str/align 6 :left   :clip-left \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :center :clip-left \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :right  :clip-left \"abcde\")"));

        assertEquals("abcde ", venice.eval("(str/align 6 :left   :clip-right \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :center :clip-right \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :right  :clip-right \"abcde\")"));

        assertEquals("abcde ", venice.eval("(str/align 6 :left   :ellipsis-left \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :center :ellipsis-left \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :right  :ellipsis-left \"abcde\")"));

        assertEquals("abcde ", venice.eval("(str/align 6 :left   :ellipsis-right \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :center :ellipsis-right \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :right  :ellipsis-right \"abcde\")"));

        assertEquals("abcde ", venice.eval("(str/align 6 :left   :newline \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :center :newline \"abcde\")"));
        assertEquals(" abcde", venice.eval("(str/align 6 :right  :newline \"abcde\")"));


        assertEquals("abcdef", venice.eval("(str/align 6 :left   :clip-left \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :clip-left \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :clip-left \"abcdef\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :clip-right \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :clip-right \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :clip-right \"abcdef\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :ellipsis-left \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :ellipsis-left \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :ellipsis-left \"abcdef\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :ellipsis-right \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :ellipsis-right \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :ellipsis-right \"abcdef\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :newline \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :newline \"abcdef\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :newline \"abcdef\")"));


        assertEquals("bcdefg", venice.eval("(str/align 6 :left   :clip-left \"abcdefg\")"));
        assertEquals("bcdefg", venice.eval("(str/align 6 :center :clip-left \"abcdefg\")"));
        assertEquals("bcdefg", venice.eval("(str/align 6 :right  :clip-left \"abcdefg\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :clip-right \"abcdefg\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :clip-right \"abcdefg\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :clip-right \"abcdefg\")"));

        assertEquals("…cdefg", venice.eval("(str/align 6 :left   :ellipsis-left \"abcdefg\")"));
        assertEquals("…cdefg", venice.eval("(str/align 6 :center :ellipsis-left \"abcdefg\")"));
        assertEquals("…cdefg", venice.eval("(str/align 6 :right  :ellipsis-left \"abcdefg\")"));

        assertEquals("abcde…", venice.eval("(str/align 6 :left   :ellipsis-right \"abcdefg\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :center :ellipsis-right \"abcdefg\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :right  :ellipsis-right \"abcdefg\")"));

        assertEquals("abcdef\ng     ", venice.eval("(str/align 6 :left   :newline \"abcdefg\")"));
        assertEquals("abcdef\n   g  ", venice.eval("(str/align 6 :center :newline \"abcdefg\")"));
        assertEquals("abcdef\n     g", venice.eval("(str/align 6 :right  :newline \"abcdefg\")"));


        assertEquals("cdefgh", venice.eval("(str/align 6 :left   :clip-left \"abcdefgh\")"));
        assertEquals("cdefgh", venice.eval("(str/align 6 :center :clip-left \"abcdefgh\")"));
        assertEquals("cdefgh", venice.eval("(str/align 6 :right  :clip-left \"abcdefgh\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :clip-right \"abcdefgh\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :clip-right \"abcdefgh\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :clip-right \"abcdefgh\")"));

        assertEquals("…defgh", venice.eval("(str/align 6 :left   :ellipsis-left \"abcdefgh\")"));
        assertEquals("…defgh", venice.eval("(str/align 6 :center :ellipsis-left \"abcdefgh\")"));
        assertEquals("…defgh", venice.eval("(str/align 6 :right  :ellipsis-left \"abcdefgh\")"));

        assertEquals("abcde…", venice.eval("(str/align 6 :left   :ellipsis-right \"abcdefgh\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :center :ellipsis-right \"abcdefgh\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :right  :ellipsis-right \"abcdefgh\")"));

        assertEquals("abcdef\ngh    ", venice.eval("(str/align 6 :left   :newline \"abcdefgh\")"));
        assertEquals("abcdef\n  gh  ", venice.eval("(str/align 6 :center :newline \"abcdefgh\")"));
        assertEquals("abcdef\n    gh", venice.eval("(str/align 6 :right  :newline \"abcdefgh\")"));


        assertEquals("cdef h", venice.eval("(str/align 6 :left   :clip-left \"abcdef h\")"));
        assertEquals("cdef h", venice.eval("(str/align 6 :center :clip-left \"abcdef h\")"));
        assertEquals("cdef h", venice.eval("(str/align 6 :right  :clip-left \"abcdef h\")"));

        assertEquals("abcdef", venice.eval("(str/align 6 :left   :clip-right \"abcdef h\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :center :clip-right \"abcdef h\")"));
        assertEquals("abcdef", venice.eval("(str/align 6 :right  :clip-right \"abcdef h\")"));

        assertEquals("…def h", venice.eval("(str/align 6 :left   :ellipsis-left \"abcdef h\")"));
        assertEquals("…def h", venice.eval("(str/align 6 :center :ellipsis-left \"abcdef h\")"));
        assertEquals("…def h", venice.eval("(str/align 6 :right  :ellipsis-left \"abcdef h\")"));

        assertEquals("abcde…", venice.eval("(str/align 6 :left   :ellipsis-right \"abcdef h\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :center :ellipsis-right \"abcdef h\")"));
        assertEquals("abcde…", venice.eval("(str/align 6 :right  :ellipsis-right \"abcdefgh\")"));

        assertEquals("abcdef\nh     ", venice.eval("(str/align 6 :left   :newline \"abcdef h\")"));
        assertEquals("abcdef\n   h  ", venice.eval("(str/align 6 :center :newline \"abcdef h\")"));
        assertEquals("abcdef\n     h", venice.eval("(str/align 6 :right  :newline \"abcdef h\")"));
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

        // mode :start
        assertEquals(null, venice.eval("(str/truncate nil 20 \"...\" :start)"));
        assertEquals("", venice.eval("(str/truncate \"\" 20 \"...\" :start)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 20 \"...\" :start)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 10 \"...\" :start)"));
        assertEquals("...efghij", venice.eval("(str/truncate \"abcdefghij\" 9 \"...\" :start)"));
        assertEquals("...j", venice.eval("(str/truncate \"abcdefghij\" 4 \"...\" :start)"));

        // mode :middle
        assertEquals(null, venice.eval("(str/truncate nil 20 \"...\" :middle)"));
        assertEquals("", venice.eval("(str/truncate \"\" 20 \"...\" :middle)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 20 \"...\" :middle)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 10 \"...\" :middle)"));
        assertEquals("abc...hij", venice.eval("(str/truncate \"abcdefghij\" 9 \"...\" :middle)"));
        assertEquals("abc...ij", venice.eval("(str/truncate \"abcdefghij\" 8 \"...\" :middle)"));
        assertEquals("ab...ij", venice.eval("(str/truncate \"abcdefghij\" 7 \"...\" :middle)"));
        assertEquals("ab...j", venice.eval("(str/truncate \"abcdefghij\" 6 \"...\" :middle)"));
        assertEquals("a...j", venice.eval("(str/truncate \"abcdefghij\" 5 \"...\" :middle)"));
        assertEquals("a...", venice.eval("(str/truncate \"abcdefghij\" 4 \"...\" :middle)"));

        // mode :end
        assertEquals(null, venice.eval("(str/truncate nil 20 \"...\" :end)"));
        assertEquals("", venice.eval("(str/truncate \"\" 20 \"...\" :end)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 20 \"...\" :end)"));
        assertEquals("abcdefghij", venice.eval("(str/truncate \"abcdefghij\" 10 \"...\" :end)"));
        assertEquals("abcdef...", venice.eval("(str/truncate \"abcdefghij\" 9 \"...\" :end)"));
        assertEquals("a...", venice.eval("(str/truncate \"abcdefghij\" 4 \"...\" :end)"));
    }


    @Test
    public void test_str_expand() {
        final Venice venice = new Venice();

        assertEquals("..........", venice.eval("(str/expand nil 10 \".\")"));
        assertEquals("..........", venice.eval("(str/expand \"\" 10 \".\")"));
        assertEquals("a.........", venice.eval("(str/expand \"a\" 10 \".\")"));
        assertEquals("ab........", venice.eval("(str/expand \"ab\" 10 \".\")"));
        assertEquals("abc.......", venice.eval("(str/expand \"abc\" 10 \".\")"));
        assertEquals("abcd......", venice.eval("(str/expand \"abcd\" 10 \".\")"));
        assertEquals("abcde.....", venice.eval("(str/expand \"abcde\" 10 \".\")"));
        assertEquals("abcdef....", venice.eval("(str/expand \"abcdef\" 10 \".\")"));
        assertEquals("abcdefg...", venice.eval("(str/expand \"abcdefg\" 10 \".\")"));
        assertEquals("abcdefgh..", venice.eval("(str/expand \"abcdefgh\" 10 \".\")"));
        assertEquals("abcdefghi.", venice.eval("(str/expand \"abcdefghi\" 10 \".\")"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \".\")"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \".\")"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \".\")"));

        assertEquals("..........", venice.eval("(str/expand nil 10 \".\" :end)"));
        assertEquals("..........", venice.eval("(str/expand \"\" 10 \".\" :end)"));
        assertEquals("a.........", venice.eval("(str/expand \"a\" 10 \".\" :end)"));
        assertEquals("ab........", venice.eval("(str/expand \"ab\" 10 \".\" :end)"));
        assertEquals("abc.......", venice.eval("(str/expand \"abc\" 10 \".\" :end)"));
        assertEquals("abcd......", venice.eval("(str/expand \"abcd\" 10 \".\" :end)"));
        assertEquals("abcde.....", venice.eval("(str/expand \"abcde\" 10 \".\" :end)"));
        assertEquals("abcdef....", venice.eval("(str/expand \"abcdef\" 10 \".\" :end)"));
        assertEquals("abcdefg...", venice.eval("(str/expand \"abcdefg\" 10 \".\" :end)"));
        assertEquals("abcdefgh..", venice.eval("(str/expand \"abcdefgh\" 10 \".\" :end)"));
        assertEquals("abcdefghi.", venice.eval("(str/expand \"abcdefghi\" 10 \".\" :end)"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \".\" :end)"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \".\" :end)"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \".\" :end)"));

        assertEquals("..........", venice.eval("(str/expand nil 10 \".\" :start)"));
        assertEquals("..........", venice.eval("(str/expand \"\" 10 \".\" :start)"));
        assertEquals(".........a", venice.eval("(str/expand \"a\" 10 \".\" :start)"));
        assertEquals("........ab", venice.eval("(str/expand \"ab\" 10 \".\" :start)"));
        assertEquals(".......abc", venice.eval("(str/expand \"abc\" 10 \".\" :start)"));
        assertEquals("......abcd", venice.eval("(str/expand \"abcd\" 10 \".\" :start)"));
        assertEquals(".....abcde", venice.eval("(str/expand \"abcde\" 10 \".\" :start)"));
        assertEquals("....abcdef", venice.eval("(str/expand \"abcdef\" 10 \".\" :start)"));
        assertEquals("...abcdefg", venice.eval("(str/expand \"abcdefg\" 10 \".\" :start)"));
        assertEquals("..abcdefgh", venice.eval("(str/expand \"abcdefgh\" 10 \".\" :start)"));
        assertEquals(".abcdefghi", venice.eval("(str/expand \"abcdefghi\" 10 \".\" :start)"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \".\" :start)"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \".\" :start)"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \".\" :start)"));

        assertEquals("1231231231", venice.eval("(str/expand nil 10 \"123\")"));
        assertEquals("1231231231", venice.eval("(str/expand \"\" 10 \"123\")"));
        assertEquals("a123123123", venice.eval("(str/expand \"a\" 10 \"123\")"));
        assertEquals("ab12312312", venice.eval("(str/expand \"ab\" 10 \"123\")"));
        assertEquals("abc1231231", venice.eval("(str/expand \"abc\" 10 \"123\")"));
        assertEquals("abcd123123", venice.eval("(str/expand \"abcd\" 10 \"123\")"));
        assertEquals("abcde12312", venice.eval("(str/expand \"abcde\" 10 \"123\")"));
        assertEquals("abcdef1231", venice.eval("(str/expand \"abcdef\" 10 \"123\")"));
        assertEquals("abcdefg123", venice.eval("(str/expand \"abcdefg\" 10 \"123\")"));
        assertEquals("abcdefgh12", venice.eval("(str/expand \"abcdefgh\" 10 \"123\")"));
        assertEquals("abcdefghi1", venice.eval("(str/expand \"abcdefghi\" 10 \"123\")"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \"123\")"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \"123\")"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \"123\")"));

        assertEquals("1231231231", venice.eval("(str/expand nil 10 \"123\" :end)"));
        assertEquals("1231231231", venice.eval("(str/expand \"\" 10 \"123\" :end)"));
        assertEquals("a123123123", venice.eval("(str/expand \"a\" 10 \"123\" :end)"));
        assertEquals("ab12312312", venice.eval("(str/expand \"ab\" 10 \"123\" :end)"));
        assertEquals("abc1231231", venice.eval("(str/expand \"abc\" 10 \"123\" :end)"));
        assertEquals("abcd123123", venice.eval("(str/expand \"abcd\" 10 \"123\" :end)"));
        assertEquals("abcde12312", venice.eval("(str/expand \"abcde\" 10 \"123\" :end)"));
        assertEquals("abcdef1231", venice.eval("(str/expand \"abcdef\" 10 \"123\" :end)"));
        assertEquals("abcdefg123", venice.eval("(str/expand \"abcdefg\" 10 \"123\" :end)"));
        assertEquals("abcdefgh12", venice.eval("(str/expand \"abcdefgh\" 10 \"123\" :end)"));
        assertEquals("abcdefghi1", venice.eval("(str/expand \"abcdefghi\" 10 \"123\" :end)"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \"123\" :end)"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \"123\" :end)"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \"123\" :end)"));

        assertEquals("1231231231", venice.eval("(str/expand nil 10 \"123\" :start)"));
        assertEquals("1231231231", venice.eval("(str/expand \"\" 10 \"123\" :start)"));
        assertEquals("123123123a", venice.eval("(str/expand \"a\" 10 \"123\" :start)"));
        assertEquals("12312312ab", venice.eval("(str/expand \"ab\" 10 \"123\" :start)"));
        assertEquals("1231231abc", venice.eval("(str/expand \"abc\" 10 \"123\" :start)"));
        assertEquals("123123abcd", venice.eval("(str/expand \"abcd\" 10 \"123\" :start)"));
        assertEquals("12312abcde", venice.eval("(str/expand \"abcde\" 10 \"123\" :start)"));
        assertEquals("1231abcdef", venice.eval("(str/expand \"abcdef\" 10 \"123\" :start)"));
        assertEquals("123abcdefg", venice.eval("(str/expand \"abcdefg\" 10 \"123\" :start)"));
        assertEquals("12abcdefgh", venice.eval("(str/expand \"abcdefgh\" 10 \"123\" :start)"));
        assertEquals("1abcdefghi", venice.eval("(str/expand \"abcdefghi\" 10 \"123\" :start)"));
        assertEquals("abcdefghij", venice.eval("(str/expand \"abcdefghij\" 10 \"123\" :start)"));
        assertEquals("abcdefghijk", venice.eval("(str/expand \"abcdefghijk\" 10 \"123\" :start)"));
        assertEquals("abcdefghijkl", venice.eval("(str/expand \"abcdefghijkl\" 10 \"123\" :start)"));
    }

    @Test
    public void test_str_upper_case() {
        final Venice venice = new Venice();

        // chars
        assertEquals('A', (Character)venice.eval("(str/upper-case #\\a)"));

        // strings
        assertEquals("ABCDEF", venice.eval("(str/upper-case \"ABCDEF\")"));
        assertEquals("ABCDEF", venice.eval("(str/upper-case \"aBcDeF\")"));
        assertEquals("ABCDEF", venice.eval("(str/upper-case (. :java.util.Locale :new \"de\" \"DE\") \"aBcDeF\")"));
        assertEquals("ABCDEF", venice.eval("(str/upper-case [\"de\" \"DE\"] \"aBcDeF\")"));
    }

    @Test
    public void test_str_upper_case_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/upper-case? #\\A)"));
        assertFalse((Boolean)venice.eval("(str/upper-case? #\\a)"));
        assertFalse((Boolean)venice.eval("(str/upper-case? #\\8)"));
    }

    @Test
    public void test_str_valid_email_addr_Q() {
        final List<String> emails = CollectionUtil.toList(
                                        "user@domain.com",
                                        "user@domain.co.in",
                                        "user.name@domain.com",
                                        "user_name@domain.com",
                                        "username@yahoo.corporate.in");

        final List<String> invalidEmails = CollectionUtil.toList(
                                                ".username@yahoo.com",
                                                "username@yahoo.com.",
                                                "username@yahoo..com",
                                                "username@yahoo.c",
                                                "username@yahoo.corporate");

        final Venice venice = new Venice();

        emails.forEach(
                e -> assertTrue((Boolean)venice.eval("(str/valid-email-addr? \"" + e + "\")")));

        invalidEmails.forEach(
                e -> assertFalse((Boolean)venice.eval("(str/valid-email-addr? \"" + e + "\")")));
    }

    @Test
    public void test_str_whitespace_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(str/whitespace? #\\space)"));
        assertTrue((Boolean)venice.eval("(str/whitespace? #\\newline)"));
        assertTrue((Boolean)venice.eval("(str/whitespace? #\\return)"));
        assertFalse((Boolean)venice.eval("(str/whitespace? \"8\")"));
    }


    private static List<Character> toCharList(final String s) {
        return s.chars().mapToObj(e->(char)e).collect(Collectors.toList());
    }
}
