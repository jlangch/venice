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
package com.github.jlangch.venice.impl.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.ParseError;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.VeniceInterpreter;
import com.github.jlangch.venice.impl.modules.ModuleLoader;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;


public class ReaderTest {

    @Test
    public void test_too_much_input() {
        final VeniceInterpreter vi = new VeniceInterpreter(new AcceptAllInterceptor());

        final VncVal ast = vi.READ("(+ 1 2)\n(+ 3 4)", "test");

        assertTrue(ast instanceof VncList);
        assertEquals(3, ((VncList)ast).size());
    }

    @Test
    public void testMeta() {
        assertEquals(100L, new Venice().eval("(do (def ^{:a 200} x 100) x)"));
        assertEquals(200L, new Venice().eval("(do (def ^{:a 200} x 100) (:a (meta x)))"));
        assertEquals("(xx yy)", new Venice().eval("(do (def ^{:a '(\"xx\" \"yy\")} x 100) (str (:a (meta x))))"));

        assertEquals(true, new Venice().eval("(do (def ^:private x 100) (:private (meta x))))"));
        assertEquals(true, new Venice().eval("(do (ns test) (def ^:private x 100) (:private (meta x))))"));

        assertEquals(true, new Venice().eval("(do (def ^:dynamic x 100) (:dynamic (meta x))))"));
        assertEquals(true, new Venice().eval("(do (ns test) (def ^:dynamic x 100) (:dynamic (meta x))))"));
   }

    @Test
    public void testAtomInteger() {
        assertEquals(Integer.valueOf(100), new Venice().eval("(do 100I)"));
    }

    @Test
    public void testAtomLong() {
        assertEquals(Long.valueOf(100), new Venice().eval("(do 100)"));
    }

    @Test
    public void testAtomDouble() {
        assertEquals(Double.valueOf(100.2), new Venice().eval("(do 100.2)"));
    }

    @Test
    public void testAtomDecimal() {
        assertEquals(new BigDecimal("100.123"), new Venice().eval("(do 100.123M)"));
    }

    @Test
    public void testAtomBigInteger() {
        assertEquals(new BigInteger("100"), new Venice().eval("(do 100N)"));
    }

    @Test
    public void testAtomNumber_invalid() {
    	assertThrows(
    			ParseError.class,
                () -> new Venice().eval("[0 1 2false]"));
    }

    @Test
    public void testAtomNil() {
        assertEquals(null, new Venice().eval("(do nil)"));
    }

    @Test
    public void testAtomTrue() {
        assertEquals(Boolean.TRUE, new Venice().eval("(do true)"));
    }

    @Test
    public void testAtomFalse() {
        assertEquals(Boolean.FALSE, new Venice().eval("(do false)"));
    }

    @Test
    public void testAtomString() {
        assertEquals("abc", new Venice().eval("(do \"abc\")"));
        assertEquals("a\nb\nc", new Venice().eval("(do \"a\nb\nc\")"));
    }

    @Test
    public void testAtomString_escape() {
        final String script = "(do \"a\\nb\\tc\\\\d\")";
        final String result = (String)new Venice().eval(script);

        assertEquals("a\nb\tc\\d", result);
        assertEquals('a',  result.charAt(0));
        assertEquals('\n', result.charAt(1));
        assertEquals('b',  result.charAt(2));
        assertEquals('\t', result.charAt(3));
        assertEquals('c',  result.charAt(4));
        assertEquals('\\', result.charAt(5));
        assertEquals('d',  result.charAt(6));
    }

    @Test
    public void testAtomString_unicode() {
        assertEquals("abc", new Venice().eval("(do \"\\u0061\\u0062\\u0063\")"));
    }

    @Test
    public void testAtomString_TripleQuotes() {
        assertEquals("abc", new Venice().eval("(do \"\"\"abc\"\"\")"));
        assertEquals("abc", new Venice().eval("(do \"\"\"\\u0061\\u0062\\u0063\"\"\")"));
        assertEquals("a\"b\"c", new Venice().eval("(do \"\"\"a\"b\"c\"\"\")"));
        assertEquals("a\nb\nc", new Venice().eval("(do \"\"\"a\nb\nc\"\"\")"));

        assertEquals("   1\n   2\n   3", new Venice().eval("(do \"\"\"   1\n   2\n   3\"\"\")"));
    }

    @Test
    public void testAtomString_TripleQuotes_indent() {
        assertEquals("1\n2\n3", new Venice().eval("(do \"\"\"\n   1\n   2\n   3\"\"\")"));
        assertEquals("1\n2\n3", new Venice().eval("(do \"\"\"\n   1\n   2\n   3\n\"\"\")"));
        assertEquals("1\n2\n3", new Venice().eval("(do \"\"\"\n   1\n   2\n   3\n   \"\"\")"));

        assertEquals("1\n 2\n 3", new Venice().eval("(do \"\"\"\n   1\n    2\n    3\"\"\")"));
        assertEquals("1\n 2\n 3", new Venice().eval("(do \"\"\"\n   1\n    2\n    3\n\"\"\")"));
        assertEquals("1\n 2\n 3", new Venice().eval("(do \"\"\"\n   1\n    2\n    3\n   \"\"\")"));
    }

    @Test
    public void testAtomString_TripleQuotes_line_join() {
        final String s1 =
                "(do\n" +
                "   \"\"\"\n" +
                "   123\n" +
                "   456\n" +
                "   \"\"\"\n" +
                ")";

        final String s2 =
                "(do\n" +
                "   \"\"\"\n" +
                "   123  \\\n" +
                "   456\n" +
                "   \"\"\"\n" +
                ")";

        final String s3 =
                "(do\n" +
                "   \"\"\"\n" +
                "   123\\\n" +
                "     456\n" +
                "   \"\"\"\n" +
                ")";

        final String s4 =
                "(do\n" +
                "   \"\"\"\n" +
                "   123  \\\n" +
                "   456  \\\n" +
                "   \"\"\"\n" +
                ")";

        assertEquals("123\n456", new Venice().eval(s1));
        assertEquals("123  456", new Venice().eval(s2));
        assertEquals("123456", new Venice().eval(s3));
        assertEquals("123  456  ", new Venice().eval(s4));
    }

    @Test
    public void testAtomKeyword() {
        assertEquals(":abc", new Venice().eval("(do (str :abc))"));
    }

    @Test
    public void testAtomSymbol() {
        assertEquals(Long.valueOf(100), new Venice().eval("(do (let [abc 100] abc))"));
    }


    @Test
    public void testStringInterpolation_single_simple_value() {
        assertEquals("100", new Venice().eval("(do (def x 100) \"~{x}\")"));
        assertEquals(" 100", new Venice().eval("(do (def x 100) \" ~{x}\")"));
        assertEquals("100 ", new Venice().eval("(do (def x 100) \"~{x} \")"));
        assertEquals(" 100 ", new Venice().eval("(do (def x 100) \" ~{x} \")"));

        assertEquals("100 200", new Venice().eval("(do (def x 100) (def y 200) \"~{x} ~{y}\")"));
        assertEquals(" 100 200", new Venice().eval("(do (def x 100) (def y 200) \" ~{x} ~{y}\")"));
        assertEquals("100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"~{x} ~{y} \")"));
        assertEquals(" 100 200 ", new Venice().eval("(do (def x 100) (def y 200) \" ~{x} ~{y} \")"));

        assertEquals("100200", new Venice().eval("(do (def x 100) (def y 200) \"~{x}~{y}\")"));
        assertEquals(" 100200", new Venice().eval("(do (def x 100) (def y 200) \" ~{x}~{y}\")"));
        assertEquals("100200 ", new Venice().eval("(do (def x 100) (def y 200) \"~{x}~{y} \")"));
        assertEquals(" 100200 ", new Venice().eval("(do (def x 100) (def y 200) \" ~{x}~{y} \")"));
    }

    @Test
    public void testStringInterpolation_single_expression() {
        assertEquals("101", new Venice().eval("(do (def x 100) \"~(inc x)\")"));
        assertEquals(" 101", new Venice().eval("(do (def x 100) \" ~(inc x)\")"));
        assertEquals("101 ", new Venice().eval("(do (def x 100) \"~(inc x) \")"));
        assertEquals(" 101 ", new Venice().eval("(do (def x 100) \" ~(inc x) \")"));

        assertEquals("101 99", new Venice().eval("(do (def x 100) \"~(inc x) ~(dec x)\")"));
        assertEquals(" 101 99", new Venice().eval("(do (def x 100) \" ~(inc x) ~(dec x)\")"));
        assertEquals("101 99 ", new Venice().eval("(do (def x 100) \"~(inc x) ~(dec x) \")"));
        assertEquals(" 101 99 ", new Venice().eval("(do (def x 100) \" ~(inc x) ~(dec x) \")"));

        assertEquals("10199", new Venice().eval("(do (def x 100) \"~(inc x)~(dec x)\")"));
        assertEquals(" 10199", new Venice().eval("(do (def x 100) \" ~(inc x)~(dec x)\")"));
        assertEquals("10199 ", new Venice().eval("(do (def x 100) \"~(inc x)~(dec x) \")"));
        assertEquals(" 10199 ", new Venice().eval("(do (def x 100) \" ~(inc x)~(dec x) \")"));
    }

    @Test
    public void testStringInterpolation_single_mixed() {
        assertEquals("100 101", new Venice().eval("(do (def x 100) \"~{x} ~(inc x)\")"));
        assertEquals(" 100 101", new Venice().eval("(do (def x 100) \" ~{x} ~(inc x)\")"));
        assertEquals("100 101 ", new Venice().eval("(do (def x 100) \"~{x} ~(inc x) \")"));
        assertEquals(" 100 101 ", new Venice().eval("(do (def x 100) \" ~{x} ~(inc x) \")"));

        assertEquals("100101", new Venice().eval("(do (def x 100) \"~{x}~(inc x)\")"));
        assertEquals(" 100101", new Venice().eval("(do (def x 100) \" ~{x}~(inc x)\")"));
        assertEquals("100101 ", new Venice().eval("(do (def x 100) \"~{x}~(inc x) \")"));
        assertEquals(" 100101 ", new Venice().eval("(do (def x 100) \" ~{x}~(inc x) \")"));

        assertEquals("101 100", new Venice().eval("(do (def x 100) \"~(inc x) ~{x}\")"));
        assertEquals(" 101 100", new Venice().eval("(do (def x 100) \" ~(inc x) ~{x}\")"));
        assertEquals("101 100 ", new Venice().eval("(do (def x 100) \"~(inc x) ~{x} \")"));
        assertEquals(" 101 100 ", new Venice().eval("(do (def x 100) \" ~(inc x) ~{x} \")"));

        assertEquals("101100", new Venice().eval("(do (def x 100) \"~(inc x)~{x}\")"));
        assertEquals(" 101100", new Venice().eval("(do (def x 100) \" ~(inc x)~{x}\")"));
        assertEquals("101100 ", new Venice().eval("(do (def x 100) \"~(inc x)~{x} \")"));
        assertEquals(" 101100 ", new Venice().eval("(do (def x 100) \" ~(inc x)~{x} \")"));
    }

    @Test
    public void testStringInterpolation_triple_simple_value() {
        assertEquals("100", new Venice().eval("(do (def x 100) \"\"\"~{x}\"\"\")"));
        assertEquals(" 100", new Venice().eval("(do (def x 100) \"\"\" ~{x}\"\"\")"));
        assertEquals("100 ", new Venice().eval("(do (def x 100) \"\"\"~{x} \"\"\")"));
        assertEquals(" 100 ", new Venice().eval("(do (def x 100) \"\"\" ~{x} \"\"\")"));

        assertEquals("100 200", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x} ~{y}\"\"\")"));
        assertEquals(" 100 200", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x} ~{y}\"\"\")"));
        assertEquals("100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x} ~{y} \"\"\")"));
        assertEquals(" 100 200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x} ~{y} \"\"\")"));

        assertEquals("100200", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x}~{y}\"\"\")"));
        assertEquals(" 100200", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x}~{y}\"\"\")"));
        assertEquals("100200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\"~{x}~{y} \"\"\")"));
        assertEquals(" 100200 ", new Venice().eval("(do (def x 100) (def y 200) \"\"\" ~{x}~{y} \"\"\")"));
    }

    @Test
    public void testStringInterpolation_triple_expression() {
        assertEquals("101", new Venice().eval("(do (def x 100) \"\"\"~(inc x)\"\"\")"));
        assertEquals(" 101", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)\"\"\")"));
        assertEquals("101 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) \"\"\")"));
        assertEquals(" 101 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) \"\"\")"));

        assertEquals("101 99", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~(dec x)\"\"\")"));
        assertEquals(" 101 99", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~(dec x)\"\"\")"));
        assertEquals("101 99 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~(dec x) \"\"\")"));
        assertEquals(" 101 99 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~(dec x) \"\"\")"));

        assertEquals("10199", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~(dec x)\"\"\")"));
        assertEquals(" 10199", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~(dec x)\"\"\")"));
        assertEquals("10199 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~(dec x) \"\"\")"));
        assertEquals(" 10199 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~(dec x) \"\"\")"));
    }

    @Test
    public void testString_triple_quoted() {
        assertEquals("123", new Venice().eval("\"\"\"123\"\"\")"));
        assertEquals("  123  ", new Venice().eval("\"\"\"  123  \"\"\")"));
        assertEquals("1\n2", new Venice().eval("\"\"\"\n  1\n  2\"\"\")"));
        assertEquals("\"1 2 3\"", new Venice().eval("\"\"\"\n  \"1 2 3\"\n  \"\"\")"));
    }

    @Test
    public void testStringInterpolation_triple_mixed() {
        assertEquals("100 101", new Venice().eval("(do (def x 100) \"\"\"~{x} ~(inc x)\"\"\")"));
        assertEquals(" 100 101", new Venice().eval("(do (def x 100) \"\"\" ~{x} ~(inc x)\"\"\")"));
        assertEquals("100 101 ", new Venice().eval("(do (def x 100) \"\"\"~{x} ~(inc x) \"\"\")"));
        assertEquals(" 100 101 ", new Venice().eval("(do (def x 100) \"\"\" ~{x} ~(inc x) \"\"\")"));

        assertEquals("100101", new Venice().eval("(do (def x 100) \"\"\"~{x}~(inc x)\"\"\")"));
        assertEquals(" 100101", new Venice().eval("(do (def x 100) \"\"\" ~{x}~(inc x)\"\"\")"));
        assertEquals("100101 ", new Venice().eval("(do (def x 100) \"\"\"~{x}~(inc x) \"\"\")"));
        assertEquals(" 100101 ", new Venice().eval("(do (def x 100) \"\"\" ~{x}~(inc x) \"\"\")"));

        assertEquals("101 100", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~{x}\"\"\")"));
        assertEquals(" 101 100", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~{x}\"\"\")"));
        assertEquals("101 100 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x) ~{x} \"\"\")"));
        assertEquals(" 101 100 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x) ~{x} \"\"\")"));

        assertEquals("101100", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~{x}\"\"\")"));
        assertEquals(" 101100", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~{x}\"\"\")"));
        assertEquals("101100 ", new Venice().eval("(do (def x 100) \"\"\"~(inc x)~{x} \"\"\")"));
        assertEquals(" 101100 ", new Venice().eval("(do (def x 100) \"\"\" ~(inc x)~{x} \"\"\")"));
    }

    @Test
    public void testStringInterpolation_triple_with_quotes_1() {
        assertEquals("100", new Venice().eval("(do (def x 100) \"\"\"~{x}\"\"\")"));
        assertEquals(" 100 ", new Venice().eval("(do (def x 100) \"\"\" ~{x} \"\"\")"));
        assertEquals(" '100' ", new Venice().eval("(do (def x 100) \"\"\" '~{x}' \"\"\")"));
        assertEquals(" \"100 ", new Venice().eval("(do (def x 100) \"\"\" \"~{x} \"\"\")"));
        assertEquals(" \"100\" ", new Venice().eval("(do (def x 100) \"\"\" \"~{x}\" \"\"\")"));

        assertEquals("100200", new Venice().eval("(do (let [x 100 y 200] \"\"\"~{x}~{y}\"\"\"))"));
        assertEquals(" 100 200 ", new Venice().eval("(do (let [x 100 y 200] \"\"\" ~{x} ~{y} \"\"\"))"));
        assertEquals(" '100' '200' ", new Venice().eval("(do (let [x 100 y 200] \"\"\" '~{x}' '~{y}' \"\"\"))"));
        assertEquals(" \"100 \"200", new Venice().eval("(do (let [x 100 y 200] \"\"\" \"~{x} \"~{y}\"\"\"))"));
        assertEquals(" \"100\" \"200\" ", new Venice().eval("(do (let [x 100 y 200] \"\"\" \"~{x}\" \"~{y}\" \"\"\"))"));
    }

    @Test
    public void testStringInterpolation_triple_with_quotes_2() {
        assertEquals("100", new Venice().eval("(do (def x 100) \"\"\"~(str x)\"\"\")"));
        assertEquals(" 100 ", new Venice().eval("(do (def x 100) \"\"\" ~(str x) \"\"\")"));
        assertEquals(" '100' ", new Venice().eval("(do (def x 100) \"\"\" '~(str x)' \"\"\")"));
        assertEquals(" \"100 ", new Venice().eval("(do (def x 100) \"\"\" \"~(str x) \"\"\")"));
        assertEquals(" \"100\" ", new Venice().eval("(do (def x 100) \"\"\" \"~(str x)\" \"\"\")"));

        assertEquals("100200", new Venice().eval("(do (let [x 100 y 200] \"\"\"~(str x)~(str y)\"\"\"))"));
        assertEquals(" 100 200 ", new Venice().eval("(do (let [x 100 y 200] \"\"\" ~(str x) ~(str y) \"\"\"))"));
        assertEquals(" '100' '200' ", new Venice().eval("(do (let [x 100 y 200] \"\"\" '~(str x)' '~(str y)' \"\"\"))"));
        assertEquals(" \"100 \"200", new Venice().eval("(do (let [x 100 y 200] \"\"\" \"~(str x) \"~(str y)\"\"\"))"));
        assertEquals(" \"100\" \"200\" ", new Venice().eval("(do (let [x 100 y 200] \"\"\" \"~(str x)\" \"~(str y)\" \"\"\"))"));
    }

    @Test
    public void test_interpolate_1() {
        final VncVal val = Reader.interpolate(" \"~{x}\" ", "test", 1, 1);
        assertNotNull(val);
    }

    @Test
    public void test_interpolate_2() {
        final VncVal val = Reader.interpolate(" \"~(str x)\" ", "test", 1, 1);
        assertNotNull(val);
    }

    @Test
    public void test_read_core() {
        final String core = ModuleLoader.loadModule("core");
        final String core_ = "(do\n" + core + "\n)";
        final long lines = StringUtil.splitIntoLines(core_).size();

        final StopWatch sw = new StopWatch();
        final VncVal ast = Reader.read_str(core_, "core");
        sw.stop();

        System.out.println(String.format(
                "Reading :core module in %s at %d lines/s",
                sw.toString(),
                (lines * 1000L) / sw.elapsedMillis()));

        assertNotNull(ast);
    }

}
