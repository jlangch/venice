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
package com.github.jlangch.venice.impl.specialforms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class SpecialFormsTest_deftype_or {

    @Test
    public void test_deftype_or_values_keyword() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :color :red :green :blue)       \n" +
                "  (.: :color :red))                             ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_invalid_name() {
        final String script =
                "(do                                        \n" +
                "  (deftype-or :color. :red :green :blue))    ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_type_builder() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :color :red :green :blue)       \n" +
                "  (def x (color. :red))                       \n" +
                "  x)                                            ";

        assertEquals("red", venice.eval(script));
    }

    @Test
    public void test_deftype_or_type_checker() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :color :red :green :blue)       \n" +
                "  (color? (color. :red)))                       ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_deftype_or_values_keyword_FAILED() {
        final String script =
                "(do                                           \n" +
                "  (deftype-or :color :red :green :blue)       \n" +
                "  (.: :color :yellow))                          ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_values_long() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)     \n" +
                "  (.: :digit 5))                                ";

        assertEquals(5L, venice.eval(script));
    }

    @Test
    public void test_deftype_or_values_long_FAILED() {
        final String script =
                "(do                                           \n" +
                "  (deftype-or :digit 0 1 2 3 4 5 6 7 8 9)     \n" +
                "  (.: :digit 999))                              ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_types() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :long :double)            \n" +
                "  (.: :test 5))                                ";

        assertEquals(5L, venice.eval(script));
    }

    @Test
    public void test_deftype_or_types_FAILED() {
        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :long :double)            \n" +
                "  (.: :test \"5\"))                             ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_no_values() {
        final String script =
                "(do                                           \n" +
                "  (deftype-or :user/numbers )                 \n" +
                "  (doc :user/color))                           ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_of_ns_hijack_1() {
        final String script =
                "(do                                           \n" +
                "  (ns foo)                                    \n" +
                "  (deftype-or :user/color :red :green :blue))   ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_of_ns_hijack_2() {
        final String script =
                "(do                                           \n" +
                "  (ns foo)                                    \n" +
                "  (deftype-or :core/color :red :green :blue))   ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_types_nillable_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :string nil)              \n" +
                "  (test. nil))                                ";

        assertNull(venice.eval(script));
    }

    @Test
    public void test_deftype_or_types_nillable_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :string nil)              \n" +
                "  (test. \"abc\"))                              ";

        assertEquals("abc", venice.eval(script));
    }

    @Test
    public void test_deftype_or_types_nillable_invalid_type() {
        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :string nil)              \n" +
                "  (test. 6))                                    ";

        assertThrows(VncException.class, () -> new Venice().eval(script));
    }

    @Test
    public void test_deftype_or_types_nonsense_but_valid() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (deftype-or :test :string \"23\")           \n" +
                "  (test. \"23\"))                               ";

        assertEquals("23", venice.eval(script));
    }

    @Test
    public void test_deftype_or_equality() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (deftype-or :color :red :green :blue)               \n" +
                "  (assert (color. :blue) (color. :blue)))             \n" +
                "  (assert (not (= (color. :blue) (color. :red))))     \n" +
                "  (assert (not (= (color. :blue) 100)))               \n" +
                "  nil)";

        venice.eval(script);
    }

    @Test
    public void test_deftype_or_equality_strict() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                    \n" +
                "  (deftype-or :color :red :green :blue)                \n" +
                "  (assert (== (color. :blue) (color. :blue)))          \n" +
                "  (assert (not (== (color. :blue) (color. :red))))     \n" +
                "  (assert (not (== (color. :blue) 100)))               \n" +
                "  nil)";

        venice.eval(script);
    }
}
