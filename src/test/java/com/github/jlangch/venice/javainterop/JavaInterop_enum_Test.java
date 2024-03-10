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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
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

        assertEquals(Color.red,   venice.eval("(. :com.github.jlangch.venice.support.Color :red)"));
        assertEquals(Color.green, venice.eval("(. :com.github.jlangch.venice.support.Color :green)"));
        assertEquals(Color.blue,  venice.eval("(. :com.github.jlangch.venice.support.Color :blue)"));
    }

    @Test
    public void testEnumValueType() {
        final Venice venice = new Venice();

        assertEquals(
                "com.github.jlangch.venice.support.Color",
                venice.eval("(type (. :com.github.jlangch.venice.support.Color :blue))"));
    }

    @Test
    public void testEnumValueToString() {
        final Venice venice = new Venice();

        assertEquals("red",   venice.eval("(str (. :com.github.jlangch.venice.support.Color :red))"));
        assertEquals("green", venice.eval("(str (. :com.github.jlangch.venice.support.Color :green))"));
        assertEquals("blue",  venice.eval("(str (. :com.github.jlangch.venice.support.Color :blue))"));

        assertEquals("red",   venice.eval("(pr-str (. :com.github.jlangch.venice.support.Color :red))"));
        assertEquals("green", venice.eval("(pr-str (. :com.github.jlangch.venice.support.Color :green))"));
        assertEquals("blue",  venice.eval("(pr-str (. :com.github.jlangch.venice.support.Color :blue))"));
    }

    @Test
    public void testEnumValues() {
        final Venice venice = new Venice();

        assertEquals(
                "[red green blue]",
                venice.eval(
                        "(pr-str (. :com.github.jlangch.venice.support.Color :values))"));

        assertEquals(
                Color.red,
                venice.eval(
                        "(first (. :com.github.jlangch.venice.support.Color :values))"));

        assertEquals(
                "com.github.jlangch.venice.support.Color",
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
                Color.red,
                venice.eval("(do                           " +
                            "  (. apple :setColor \"red\") " +
                            "  (. apple :getColor))        ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                Color.blue,
                venice.eval("(do                           " +
                            "  (. apple :setColor :blue)   " +
                            "  (. apple :getColor))        ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                Color.blue,
                venice.eval("(do                                                 " +
                            "  (import :com.github.jlangch.venice.support.Color) " +
                            "  (let [blue (. :Color :blue)]                      " +
                            "    (. apple :setColor blue)                        " +
                            "    (. apple :getColor)))                           ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                Color.green,
                venice.eval("(do                                              " +
                            "  (. apple :setColor (. apple :getColorDefault)) " +
                            "  (. apple :getColor))                           ",
                            Parameters.of("apple", new Apple())));
    }

    @Test
    public void testEnumBeanAccessor() {
        final Venice venice = new Venice();

        assertEquals(
                null,
                venice.eval("(:color apple)",
                            Parameters.of("apple", new Apple())));

        assertEquals(
                Color.blue,
                venice.eval("(do                           " +
                            "  (. apple :setColor :blue)   " +
                            "  (. apple :getColor))        ",
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
        		Color.red,
                venice.eval("(do                         " +
                            "  (. apple :setColor :red)  " +
                            "  (. apple :getColor))      ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
        		Color.red,
                venice.eval("(do                               " +
                            "  (. apple :setColor :Color.red)  " +
                            "  (. apple :getColor))            ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
        		Color.red,
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (. apple :setColor :Color.red)                     " +
                            "  (. apple :getColor))                               ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
        		Color.blue,
                venice.eval("(do                                    " +
                            " (. apple :setColor \"Color.blue\")    " +
                            " (. apple :getColor))                  ",
                            Parameters.of("apple", new Apple())));
    }

    @Test
    public void testEnumValueAccessor() {
        final Venice venice = new Venice();

        assertEquals(
                Color.blue,
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (let [c (. :Color :blue)]                          " +
                            "    (. apple :setColor c)                            " +
                            "    (. apple :getColor)))                            ",
                            Parameters.of("apple", new Apple())));
    }

    @Test
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

    @Test
    public void testEnumValueData() {
        final Venice venice = new Venice();

        assertEquals("Days",  venice.eval("(str (:rangeUnit (. :java.time.temporal.ChronoField :HOUR_OF_DAY)))"));
        assertEquals("Hours", venice.eval("(str (:baseUnit  (. :java.time.temporal.ChronoField :HOUR_OF_DAY)))"));
    }

    @Test
    public void testScriptParameter() {
        final Venice venice = new Venice();

        assertEquals(
        		Color.blue,
                venice.eval("color",
                            Parameters.of("color", Color.blue)));

        assertEquals(
        		Color.red,
                venice.eval("color",
                            Parameters.of("color", Color.red)));

        assertEquals(
        		Color.red,
                venice.eval("(first colors)",
                            Parameters.of("colors", Arrays.asList(Color.red, Color.blue))));

        assertEquals(
        		Color.blue,
                venice.eval("(second colors)",
                            Parameters.of("colors", Arrays.asList(Color.red, Color.blue))));
    }

    @Test
    public void testConvertToKeyword() {
        final Venice venice = new Venice();

        assertEquals(
        		":JANUARY",
                venice.eval("(pr-str (keyword (. :java.time.Month :JANUARY)))"));

        assertEquals(
        		":blue",
                venice.eval("(do                                        " +
                            "  (. apple :setColor :blue)                " +
                            "  (pr-str (keyword (. apple :getColor))))  ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
        		":blue",
                venice.eval("(pr-str (keyword (. :com.github.jlangch.venice.support.Color :blue)))"));

        assertEquals(
        		":blue",
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (pr-str (keyword (. :Color :blue))))               "));
    }

    @Test
    public void testConvertToName() {
        final Venice venice = new Venice();

        assertEquals(
        		"JANUARY",
                venice.eval("(name (. :java.time.Month :JANUARY))"));

        assertEquals(
        		"blue",
                venice.eval("(do                                        " +
                            "  (. apple :setColor :blue)                " +
                            "  (name (. apple :getColor)))  ",
                            Parameters.of("apple", new Apple())));

        assertEquals(
        		"blue",
                venice.eval("(name (. :com.github.jlangch.venice.support.Color :blue))"));

        assertEquals(
        		"blue",
                venice.eval("(do                                                  " +
                            "  (import :com.github.jlangch.venice.support.Color)  " +
                            "  (name (. :Color :blue)))                           "));
    }

    @Test
    public void testConvertToVncVal() {
        try {
            ThreadContext.setInterceptor(new AcceptAllInterceptor());

	        final Apple apple = new Apple();
	        apple.setColor(Color.blue);
	        apple.addShade(Color.blue);
	        apple.addShade(Color.green);

	        final VncVal val = JavaInteropUtil.convertToVncVal(apple);
	        assertTrue(Types.isVncJavaObject(val));

	        final VncJavaObject javaObj = (VncJavaObject)val;

	        assertTrue(javaObj.get(new VncKeyword("color")) instanceof VncJavaObject);
	        assertEquals(Color.blue, ((Apple)javaObj.getDelegate()).getColor());
	        assertEquals(Color.blue, javaObj.get(new VncKeyword("color")).convertToJavaObject());

	        final Object obj = val.convertToJavaObject();
	        assertTrue(obj instanceof Apple);

	        assertEquals(Color.blue,  ((Apple)obj).getColor());
	        assertEquals(Color.blue,  ((Apple)obj).getShades().get(0));
	        assertEquals(Color.green, ((Apple)obj).getShades().get(1));
        }
        finally {
            ThreadContext.remove();
        }
    }

}
