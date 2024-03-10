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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.support.ClassWithEmbeddedEnum;
import com.github.jlangch.venice.support.ClassWithEmbeddedEnum.TextAlignment;


public class JavaInterop_embedded_enum_Test {

    @Test
    public void test() {
        ClassWithEmbeddedEnum c = new ClassWithEmbeddedEnum();

        c.setAlignment(TextAlignment.Centre);
    }

    @Test
    public void test_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum)               \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum.TextAlignment) \n" +
                "                                                                                   \n" +
                "   (def obj (. :ClassWithEmbeddedEnum :new))                                       \n" +
                "   (. obj :getAlignment)                                                           \n" +
                ")";

        assertEquals(TextAlignment.Left, venice.eval(script));
    }

    @Test
    public void test_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum)               \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum.TextAlignment) \n" +
                "                                                                                   \n" +
                "   (def obj (. :ClassWithEmbeddedEnum :new))                                       \n" +
                "   (. obj :setAlignment :Right)                                                    \n" +
                "   (. obj :getAlignment)                                                           \n" +
                ")";

        assertEquals(TextAlignment.Right, venice.eval(script));
    }

    @Test
    public void test_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum)               \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum$TextAlignment) \n" +
                "                                                                                   \n" +
                "   (def alignments { :centre  (. :ClassWithEmbeddedEnum$TextAlignment :Centre)     \n" +
                "                     :left    (. :ClassWithEmbeddedEnum$TextAlignment :Left )      \n" +
                "                     :right   (. :ClassWithEmbeddedEnum$TextAlignment :Right)})    \n" +
                "                                                                                   \n" +
                "   (def obj (. :ClassWithEmbeddedEnum :new))                                       \n" +
                "   (. obj :setAlignment (:right alignments))                                       \n" +
                "   (. obj :getAlignment)                                                           \n" +
                ")";

        assertEquals(TextAlignment.Right, venice.eval(script));
    }

    @Test
    public void test_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum)               \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum$TextAlignment) \n" +
                "                                                                                   \n" +
                "   (type  (. :ClassWithEmbeddedEnum$TextAlignment :Centre))                        \n" +
                ")";

        final String align = (String)venice.eval(script);

        assertEquals("com.github.jlangch.venice.support.ClassWithEmbeddedEnum$TextAlignment", align);
    }

    @Test
    public void test_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum)               \n" +
                "   (import :com.github.jlangch.venice.support.ClassWithEmbeddedEnum$TextAlignment) \n" +
                "                                                                                   \n" +
                "   (. :ClassWithEmbeddedEnum$TextAlignment :Centre)                                \n" +
                ")";

        assertEquals(TextAlignment.Centre, venice.eval(script));
    }

}
