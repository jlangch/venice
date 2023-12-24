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
package com.github.jlangch.venice.impl.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class CharTest {

    @Test
    public void test_use() {
        final Venice venice = new Venice();

        assertEquals('A', (Character)venice.eval("#\\A"));
        assertEquals('\\', (Character)venice.eval("#\\\\"));
    }

    @Test
    public void test_unicode() {
        final Venice venice = new Venice();

        assertEquals('•', (Character)venice.eval("#\\•"));
        assertEquals('•', (Character)venice.eval("#\\u2022"));

        assertEquals('π', (Character)venice.eval("#\\π"));
        assertEquals('π', (Character)venice.eval("#\\u03C0"));
    }

    @Test
    public void test_symbols() {
        final Venice venice = new Venice();

        assertEquals(' ',  (Character)venice.eval("#\\space"));
        assertEquals('\n', (Character)venice.eval("#\\newline"));
        assertEquals('\r', (Character)venice.eval("#\\return"));
        assertEquals('\t', (Character)venice.eval("#\\tab"));
        assertEquals('\f', (Character)venice.eval("#\\formfeed"));
        assertEquals('\b', (Character)venice.eval("#\\backspace"));
    }

    @Test
    public void test_reader_special_chars() {
        final Venice venice = new Venice();

        for(char ch : "()[]{}^'`~@\",;".toCharArray()) {
            assertEquals(ch, (Character)venice.eval("#\\" + ch));
        }
    }

    @Test
    public void test_type() {
        final Venice venice = new Venice();

        assertEquals("core/char", venice.eval("(type #\\A)"));

        assertEquals("core/char", venice.eval("(type #\\space)"));

        // reader special chars
        for(char ch : "()[]{}^'`~@\",;".toCharArray()) {
            assertEquals("core/char", venice.eval("(type #\\" + ch + ")"));
        }
    }

    @Test
    public void test_all_ascii_chars() {
        final Venice venice = new Venice();

        for(char ch=33; ch<127; ch++) {
            assertEquals(ch, (Character)venice.eval("#\\" + ch));

            assertEquals("core/char", venice.eval("(type #\\" + ch + ")"));
        }
    }

    @Test
    public void test_pr_str() {
        final Venice venice = new Venice();

        assertEquals("#\\u0000", venice.eval("(pr-str #\\u0000)"));

        assertEquals("#\\space", venice.eval("(pr-str #\\space)"));
        assertEquals("#\\space", venice.eval("(pr-str #\\u0020)"));

        assertEquals("#\\A",     venice.eval("(pr-str #\\A)"));
        assertEquals("#\\A",     venice.eval("(pr-str #\\u0041)"));

        assertEquals("#\\π",     venice.eval("(pr-str #\\u03c0)"));
        assertEquals("#\\π",     venice.eval("(pr-str #\\π)"));
    }

    @Test
    public void test_str() {
        final Venice venice = new Venice();

        assertEquals(" ", venice.eval("(str #\\u0020)"));

        assertEquals("A",     venice.eval("(str #\\A)"));
        assertEquals("A",     venice.eval("(str #\\u0041)"));

        assertEquals("π", venice.eval("(str #\\u03c0)"));
        assertEquals("π", venice.eval("(str #\\π)"));
    }

    @Test
    public void test_toString() {
        assertEquals("\u0000",    new VncChar('\u0000').toString(false));
        assertEquals("#\\u0000",  new VncChar('\u0000').toString(true));

        assertEquals(" ",         new VncChar(' ').toString(false));
        assertEquals("#\\space",  new VncChar(' ').toString(true));

        assertEquals(" ",         new VncChar('\u0020').toString(false));
        assertEquals("#\\space",  new VncChar('\u0020').toString(true));

        assertEquals("A",        new VncChar('A').toString(false));
        assertEquals("#\\A",     new VncChar('A').toString(true));

        assertEquals("A",        new VncChar('\u0041').toString(false));
        assertEquals("#\\A",     new VncChar('\u0041').toString(true));

        assertEquals("π",        new VncChar('π').toString(false));
        assertEquals("#\\π",     new VncChar('π').toString(true));

        assertEquals("π",        new VncChar('\u03c0').toString(false));
        assertEquals("#\\π",     new VncChar('\u03c0').toString(true));
    }

}
