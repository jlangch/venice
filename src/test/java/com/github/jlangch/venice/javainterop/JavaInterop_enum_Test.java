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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.support.Apple;
import com.github.jlangch.venice.support.Color;


public class JavaInterop_enum_Test {

    @Test
    public void testEnumQ() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(enum? :com.github.jlangch.venice.support.Color)"));
    }

    @Test
    public void testEnumValue() {
        final Venice venice = new Venice();

        assertEquals("red",   venice.eval("(. :com.github.jlangch.venice.support.Color :red)"));
        assertEquals("green", venice.eval("(. :com.github.jlangch.venice.support.Color :green)"));
        assertEquals("blue",  venice.eval("(. :com.github.jlangch.venice.support.Color :blue)"));

        assertEquals(
                "core/string",
                venice.eval("(type (. :com.github.jlangch.venice.support.Color :blue))"));
    }

    @Test
    public void testEnumValues() {
        final Venice venice = new Venice();

        assertEquals(
                "[\"red\" \"green\" \"blue\"]",
                venice.eval(
                        "(pr-str (. :com.github.jlangch.venice.support.Color :values))"));

        assertEquals(
                "red",
                venice.eval(
                        "(first (. :com.github.jlangch.venice.support.Color :values))"));

        assertEquals(
                "core/string",
                venice.eval(
                        "(type (first (. :com.github.jlangch.venice.support.Color :values)))"));
    }

    @Test
    public void testEnumAccessor() {
        final Venice venice = new Venice();

        assertEquals(
                null,
                venice.eval("(. apple :getColor)",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "red",
                venice.eval("(do                           " +
                            "  (. apple :setColor \"red\") " +
                            "  (. apple :getColor))        ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "blue",
                venice.eval("(do                           " +
                            "  (. apple :setColor :blue)   " +
                            "  (. apple :getColor))        ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "green",
                venice.eval("(do                                              " +
                            "  (. apple :setColor (. apple :getColorDefault)) " +
                            "  (. apple :getColor))                           ",
                            Parameters.of("apple", new Apple())));
   }

    @Test
    public void testScopedEnumAccessor() {
        final Venice venice = new Venice();

        assertEquals(
                null,
                venice.eval("(. apple :getColor)",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "red",
                venice.eval("(do                         " +
                            "  (. apple :setColor :red)  " +
                            "  (. apple :getColor))      ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "red",
                venice.eval("(do                               " +
                            "  (. apple :setColor :Color.red)  " +
                            "  (. apple :getColor))            ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "red",
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (. apple :setColor :Color.red)                     " +
                            "  (. apple :getColor))                               ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                "blue",
                venice.eval("(do                                    " +
                            " (. apple :setColor \"Color.blue\")    " +
                            " (. apple :getColor))                  ",
                            Parameters.of("apple", new Apple())));
    }

    @Test
    public void testEnumValueAccessor() {
        final Venice venice = new Venice();

        assertEquals(
                "blue",
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (let [c (. :Color :blue)]                          " +
                            "    (. apple :setColor c)                            " +
                            "    (. apple :getColor)))                            ",
                            Parameters.of("apple", new Apple())));
    }

    @Test
    @Disabled
    public void testEnumListAccessor() {
        final Venice venice = new Venice();

        @SuppressWarnings("unchecked")
        List<Color> shades = (List<Color>)venice.eval(
                                "(do                                                   " +
                                 "  (import :com.github.jlangch.venice.support.Color)  " +
                                 "  (let [r (. :Color :red)                            " +
                                 "        g (. :Color :green)                          " +
                                 "        b (. :Color :blue)]                          " +
                                 "    (. apple :setShades [r g b])                     " +
                                 "    (. apple :getShades)))                           ",
                                 Parameters.of("apple", new Apple()));

        assertEquals(Color.red,   shades.get(0));
        assertEquals(Color.green, shades.get(1));
        assertEquals(Color.blue,  shades.get(2));
    }
}
