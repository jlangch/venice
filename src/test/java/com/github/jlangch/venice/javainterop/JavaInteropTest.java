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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.support.JavaObject;


public class JavaInteropTest {

    @Test
    public void test_Math_max_int() {
        final Venice venice = new Venice();

        final String script = "(. :java.lang.Math :max 10I 20I)";

        assertEquals(20, venice.eval(script));
    }

    @Test
    public void test_Math_max_long() {
        final Venice venice = new Venice();

        final String script = "(. :java.lang.Math :max 10 20)";

        assertEquals(20L, venice.eval(script));
    }

    @Test
    public void test_Math_max_double() {
        final Venice venice = new Venice();

        final String script = "(. :java.lang.Math :max 10.0 20.0)";

        assertEquals(20.0D, venice.eval(script));
    }


    @Test
    public void testVoidAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :_void)", symbols()));
    }

    @Test
    public void testStringAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getString)", symbols()));
        assertEquals("abc", venice.eval("(do (. jobj :setString \"abc\") (. jobj :getString))", symbols()));
    }

    @Test
    public void testStringAsCharSequence() {
        final Venice venice = new Venice();

        assertEquals("def", venice.eval("(do (. jobj :setCharSequence \"def\") (. jobj :getString))", symbols()));
    }

    @Test
    public void testBooleanAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getBoolean)", symbols()));
        assertEquals(true, venice.eval("(do (. jobj :setBoolean true) (. jobj :getBoolean))", symbols()));

        assertEquals(false, venice.eval("(. jobj :isPrimitiveBoolean)", symbols()));
        assertEquals(true, venice.eval("(do (. jobj :setPrimitiveBoolean true) (. jobj :isPrimitiveBoolean))", symbols()));
    }

    @Test
    public void testIntegerAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getInteger)", symbols()));
        assertEquals(100, venice.eval("(do (. jobj :setInteger 100) (. jobj :getInteger))", symbols()));

        assertEquals(0, venice.eval("(. jobj :getPrimitiveInt)", symbols()));
        assertEquals(100, venice.eval("(do (. jobj :setPrimitiveInt 100) (. jobj :getPrimitiveInt))", symbols()));
    }

    @Test
    public void testLongAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getLong)", symbols()));
        assertEquals(100L, venice.eval("(do (. jobj :setLong 100) (. jobj :getLong))", symbols()));

        assertEquals(0L, venice.eval("(. jobj :getPrimitiveLong)", symbols()));
        assertEquals(100L, venice.eval("(do (. jobj :setPrimitiveLong 100) (. jobj :getPrimitiveLong))", symbols()));
    }

    @Test
    public void testFloatAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getFloat)", symbols()));
        assertEquals(100.0D, venice.eval("(do (. jobj :setFloat 100.0) (. jobj :getFloat))", symbols()));

        assertEquals(0.0D, venice.eval("(. jobj :getPrimitiveFloat)", symbols()));
        assertEquals(100.0D, venice.eval("(do (. jobj :setPrimitiveFloat 100.0) (. jobj :getPrimitiveFloat))", symbols()));
    }

    @Test
    public void testDoubleAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getDouble)", symbols()));
        assertEquals(100.0D, venice.eval("(do (. jobj :setDouble 100.0) (. jobj :getDouble))", symbols()));

        assertEquals(0.0D, venice.eval("(. jobj :getPrimitiveDouble)", symbols()));
        assertEquals(100.0D, venice.eval("(do (. jobj :setPrimitiveDouble 100.0) (. jobj :getPrimitiveDouble))", symbols()));
    }

    @Test
    public void testBigDecimalAccessor() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getBigDecimal)", symbols()));
        assertEquals(new BigDecimal("100.0"), venice.eval("(do (. jobj :setBigDecimal (decimal \"100.0\")) (. jobj :getBigDecimal))", symbols()));
    }

    @Test
    public void testStringStringStringAccessor() {
        final Venice venice = new Venice();

        assertEquals("null,null,null", venice.eval("(. jobj :_StringStringString nil nil nil)", symbols()));
        assertEquals("a,null,null", venice.eval("(. jobj :_StringStringString \"a\" nil nil)", symbols()));
        assertEquals("a,b,null", venice.eval("(. jobj :_StringStringString \"a\" \"b\" nil)", symbols()));
        assertEquals("a,b,c", venice.eval("(. jobj :_StringStringString \"a\" \"b\" \"c\")", symbols()));
    }

    @Test
    public void testStringByteArrStringAccessor() {
        final Venice venice = new Venice();

        assertEquals("null,null,null", venice.eval("(. jobj :_StringByteArrString nil nil nil)", symbols()));
        assertEquals("a,null,null", venice.eval("(. jobj :_StringByteArrString \"a\" nil nil)", symbols()));
        assertEquals("a,2,null", venice.eval("(. jobj :_StringByteArrString \"a\" (bytebuf '(1 2)) nil)", symbols()));
        assertEquals("a,2,c", venice.eval("(. jobj :_StringByteArrString \"a\"(bytebuf '(1 2)) \"c\")", symbols()));
    }

    @Test
    public void testByteArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getByteArray)", symbols()));
        assertArrayEquals(new byte[] {1,2,3}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '(1 2 3))) (. jobj :getByteArray))", symbols())).array());
        assertArrayEquals(new byte[] {}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '())) (. jobj :getByteArray))", symbols())).array());
        assertArrayEquals(new byte[] {1}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray 1) (. jobj :getByteArray))", symbols())).array());
    }

    @Test
    public void testIntArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getIntArray)", symbols()));
        assertEquals("[1I 2I 3I]", venice.eval("(str (do (. jobj :setIntArray '(1 2 3)) (. jobj :getIntArray)))", symbols()));
        assertEquals("[]", venice.eval("(str (do (. jobj :setIntArray '()) (. jobj :getIntArray)))", symbols()));
        assertEquals("[1I]", venice.eval("(str (do (. jobj :setIntArray 1) (. jobj :getIntArray)))", symbols()));

        assertEquals("[1I 2I 3I]", venice.eval("(str (do (. jobj :setIntArray (int-array '(1I 2I 3I))) (. jobj :getIntArray)))", symbols()));
    }

    @Test
    public void testIntegerArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getIntegerArray)", symbols()));
        assertEquals("[1I 2I 3I]", venice.eval("(str (do (. jobj :setIntegerArray '(1 2 3)) (. jobj :getIntegerArray)))", symbols()));
        assertEquals("[]", venice.eval("(str (do (. jobj :setIntegerArray '()) (. jobj :getIntegerArray)))", symbols()));
        assertEquals("[1I]", venice.eval("(str (do (. jobj :setIntegerArray 1) (. jobj :getIntegerArray)))", symbols()));

        assertEquals("[nil 9I nil]", venice.eval("(str (do (. jobj :setIntegerArray (aset (make-array :java.lang.Integer 3) 1 9I)) (. jobj :getIntegerArray)))", symbols()));
    }

    @Test
    public void testCharArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getCharArray)", symbols()));
        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setCharArray \"abc\") (. jobj :getCharArray)))", symbols()));
        assertEquals("[]", venice.eval("(str (do (. jobj :setCharArray \"\") (. jobj :getCharArray)))", symbols()));
    }

    @Test
    public void testStringArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getStringArray)", symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", symbols()));
        assertEquals(":core/vector", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (type (. jobj :getStringArray))))", symbols()));

        assertEquals("[a B c]", venice.eval("(do                                                        " +
                                            "  (. jobj :setStringArray '(\"a\" \"b\" \"c\"))            " +
                                            "  (let [v (apply mutable-vector (. jobj :getStringArray))] " +
                                            "     (assoc! v 1 \"B\")                                    " +
                                            "     (. jobj :setStringArray v)                            " +
                                            "     (str (. jobj :getStringArray))))                      ",
                                            symbols()));

        assertEquals("[]", venice.eval("(str (do (. jobj :setStringArray '()) (. jobj :getStringArray)))", symbols()));

        assertEquals("[a]", venice.eval("(str (do (. jobj :setStringArray \"a\") (. jobj :getStringArray)))", symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray (string-array '(\"a\" \"b\" \"c\"))) (. jobj :getStringArray)))", symbols()));

        assertEquals("[nil 9 nil]", venice.eval("(str (do (. jobj :setStringArray (aset (make-array :java.lang.String 3) 1 \"9\")) (. jobj :getStringArray)))", symbols()));
    }

    @Test
    public void testOverloadedMethod() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getOverloaded)", symbols()));
        assertEquals(100L, venice.eval("(do (. jobj :setOverloaded 100) (. jobj :getOverloaded))", symbols()));
        assertEquals("abc", venice.eval("(do (. jobj :setOverloaded \"abc\") (. jobj :getOverloaded))", symbols()));
    }

    @Test
    public void testOverloadedMethod2() {
        final Venice venice = new Venice();

        assertEquals("", venice.eval("(. jobj :_Overloaded)", symbols()));
        assertEquals("a", venice.eval("(. jobj :_Overloaded \"a\")", symbols()));
        assertEquals("a,b", venice.eval("(. jobj :_Overloaded \"a\" \"b\")", symbols()));
        assertEquals("a,b,c", venice.eval("(. jobj :_Overloaded \"a\" \"b\" \"c\")", symbols()));
    }

    @Test
    public void testStaticMethod_1() {
        final Venice venice = new Venice();

        assertEquals(Long.valueOf(20L), venice.eval("(. :java.lang.Math :min 20 30)"));
    }

    @Test
    public void testStaticMethod_2() {
        final Venice venice = new Venice();

        final Object result = venice.eval("(. :java.lang.Math :random)");
        assertTrue(result instanceof Double);
    }

    @Test
    public void testLocalDate() {
        final Venice venice = new Venice();

        final LocalDate today = LocalDate.now();

        assertEquals(today, venice.eval("(. :java.time.LocalDate :now)"));
        assertEquals(today.plusDays(5), venice.eval("(. (. :java.time.LocalDate :now) :plusDays 5)"));

    }

    @Test
    public void testVarargs() {
        final Venice venice = new Venice();

        assertEquals("abc: 100", venice.eval("(. :java.lang.String :format \"%s: %d\" '(\"abc\" 100))", symbols()));
    }

    @Test
    public void testVarargs_2() {
        final Venice venice = new Venice();

        assertEquals("a.txt", venice.eval("(str (. :java.nio.file.Paths :get \"a.txt\" '()))", symbols()));
    }

    @Test
    public void testStaticVoid() {
        final Venice venice = new Venice();

        assertEquals("123", venice.eval("(. :com.github.jlangch.venice.support.JavaObject :staticVoid)"));
    }

    @Test
    public void testStaticNestedClass() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                 \n" +
                "  (import :com.github.jlangch.venice.javainterop.JavaInteropTest$NestedStaticClass) \n" +
                "                                                                                    \n" +
                "  (-> (. :JavaInteropTest$NestedStaticClass :new)                                   \n" +
                "      (. :message)))                                                                \n";

        assertEquals("NestedStaticClass::message()", venice.eval(script));
    }


    private Map<String, Object> symbols() {
        return Parameters.of("jobj", new JavaObject());
    }


    public static class NestedStaticClass {
        public String message() {
           return "NestedStaticClass::message()";
        }
    }

}
