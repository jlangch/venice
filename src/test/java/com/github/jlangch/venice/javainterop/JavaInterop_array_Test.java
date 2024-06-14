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

import java.nio.ByteBuffer;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class JavaInterop_array_Test {

    // ------------------------------------------------------------------------
    // Array of primitive types
    // ------------------------------------------------------------------------

    @Test
    public void testByteArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getByteArray)", primitive_symbols()));
        assertArrayEquals(new byte[] {1,2,3}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '(1 2 3))) (. jobj :getByteArray))", primitive_symbols())).array());
        assertArrayEquals(new byte[] {}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray (bytebuf '())) (. jobj :getByteArray))", primitive_symbols())).array());
        assertArrayEquals(new byte[] {1}, ((ByteBuffer)venice.eval("(do (. jobj :setByteArray 1) (. jobj :getByteArray))", primitive_symbols())).array());
    }

    @Test
    public void testIntArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getIntArray)", primitive_symbols()));
        assertEquals("[1I 2I 3I]", venice.eval("(pr-str (do (. jobj :setIntArray '(1 2 3)) (. jobj :getIntArray)))", primitive_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setIntArray '()) (. jobj :getIntArray)))", primitive_symbols()));
        assertEquals("[1I]", venice.eval("(pr-str (do (. jobj :setIntArray 1) (. jobj :getIntArray)))", primitive_symbols()));

        assertEquals("[1I 2I 3I]", venice.eval(
                                     "(do                                             " +
                                     "  (. jobj :setIntArray (int-array '(1I 2I 3I))) " +
                                     "  (pr-str (. jobj :getIntArray)))               ",
                                     primitive_symbols()));
    }

    @Test
    public void testLongArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getLongArray)", primitive_symbols()));
        assertEquals("[1 2 3]", venice.eval("(pr-str (do (. jobj :setLongArray '(1 2 3)) (. jobj :getLongArray)))", primitive_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setLongArray '()) (. jobj :getLongArray)))", primitive_symbols()));
        assertEquals("[1]", venice.eval("(pr-str (do (. jobj :setLongArray 1) (. jobj :getLongArray)))", primitive_symbols()));

        assertEquals("[1 2 3]", venice.eval(
                                  "(do                                              " +
                                  "  (. jobj :setLongArray (long-array '(1 2 3)))   " +
                                  "  (pr-str (. jobj :getLongArray)))               ",
                                  primitive_symbols()));
    }

    @Test
    public void testFloatArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getFloatArray)", primitive_symbols()));
        assertEquals("[1.0 2.0 3.0]", venice.eval("(pr-str (do (. jobj :setFloatArray '(1.0 2.0 3.0)) (. jobj :getFloatArray)))", primitive_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setFloatArray '()) (. jobj :getFloatArray)))", primitive_symbols()));
        assertEquals("[1.0]", venice.eval("(pr-str (do (. jobj :setFloatArray 1.0) (. jobj :getFloatArray)))", primitive_symbols()));

        assertEquals("[1.0 2.0 3.0]", venice.eval(
                                        "(do                                                    " +
                                        "  (. jobj :setFloatArray (float-array '(1.0 2.0 3.0))) " +
                                        "  (pr-str (. jobj :getFloatArray)))                    ",
                                        primitive_symbols()));
    }

    @Test
    public void testDoubleArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getDoubleArray)", primitive_symbols()));
        assertEquals("[1.0 2.0 3.0]", venice.eval("(pr-str (do (. jobj :setDoubleArray '(1.0 2.0 3.0)) (. jobj :getDoubleArray)))", primitive_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setDoubleArray '()) (. jobj :getDoubleArray)))", primitive_symbols()));
        assertEquals("[1.0]", venice.eval("(pr-str (do (. jobj :setDoubleArray 1.0) (. jobj :getDoubleArray)))", primitive_symbols()));

        assertEquals("[1.0 2.0 3.0]", venice.eval(
                                        "(do                                                      " +
                                        "  (. jobj :setDoubleArray (double-array '(1.0 2.0 3.0))) " +
                                        "  (pr-str (. jobj :getDoubleArray)))                     ",
                                        primitive_symbols()));
    }

    @Test
    public void testCharArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getCharArray)", primitive_symbols()));
        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setCharArray \"abc\") (. jobj :getCharArray)))", primitive_symbols()));
        assertEquals("[]", venice.eval("(str (do (. jobj :setCharArray \"\") (. jobj :getCharArray)))", primitive_symbols()));
    }



    // ------------------------------------------------------------------------
    // Array of object types
    // ------------------------------------------------------------------------

    @Test
    public void testObjIntegerArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getIntArray)", object_symbols()));
        assertEquals("[1I 2I 3I]", venice.eval("(pr-str (do (. jobj :setIntArray '(1 2 3)) (. jobj :getIntArray)))", object_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setIntArray '()) (. jobj :getIntArray)))", object_symbols()));
        assertEquals("[1I]", venice.eval("(pr-str (do (. jobj :setIntArray 1) (. jobj :getIntArray)))", object_symbols()));

        assertEquals("[nil 9I nil]", venice.eval(
                                       "(do                                                                   " +
                                       "  (. jobj :setIntArray (aset (make-array :java.lang.Integer 3) 1 9I)) " +
                                       "  (pr-str (. jobj :getIntArray)))                                     ",
                                       object_symbols()));
    }

    @Test
    public void testObjLongArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getLongArray)", object_symbols()));
        assertEquals("[1 2 3]", venice.eval("(pr-str (do (. jobj :setLongArray '(1 2 3)) (. jobj :getLongArray)))", object_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setLongArray '()) (. jobj :getLongArray)))", object_symbols()));
        assertEquals("[1]", venice.eval("(pr-str (do (. jobj :setLongArray 1) (. jobj :getLongArray)))", object_symbols()));

        assertEquals("[nil 9 nil]", venice.eval(
                                      "(do                                                                " +
                                      "  (. jobj :setLongArray (aset (make-array :java.lang.Long 3) 1 9)) " +
                                      "  (pr-str (. jobj :getLongArray)))                                 ",
                                      object_symbols()));
    }

    @Test
    public void testObjFloatArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getFloatArray)", object_symbols()));
        assertEquals("[1.0 2.0 3.0]", venice.eval("(pr-str (do (. jobj :setFloatArray '(1.0 2.0 3.0)) (. jobj :getFloatArray)))", object_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setFloatArray '()) (. jobj :getFloatArray)))", object_symbols()));
        assertEquals("[1.0]", venice.eval("(pr-str (do (. jobj :setFloatArray 1) (. jobj :getFloatArray)))", object_symbols()));

        assertEquals("[nil 9.0 nil]", venice.eval(
                                        "(do                                                                            " +
                                        "  (. jobj :setFloatArray (aset (make-array :java.lang.Float 3) 1 (float 9.0))) " +
                                        "  (pr-str (. jobj :getFloatArray)))                                            ",
                                        object_symbols()));
    }

    @Test
    public void testObjDoubleArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getDoubleArray)", object_symbols()));
        assertEquals("[1.0 2.0 3.0]", venice.eval("(pr-str (do (. jobj :setDoubleArray '(1.0 2.0 3.0)) (. jobj :getDoubleArray)))", object_symbols()));
        assertEquals("[]", venice.eval("(pr-str (do (. jobj :setDoubleArray '()) (. jobj :getDoubleArray)))", object_symbols()));
        assertEquals("[1.0]", venice.eval("(pr-str (do (. jobj :setDoubleArray 1) (. jobj :getDoubleArray)))", object_symbols()));

        assertEquals("[nil 9.0 nil]", venice.eval(
                                        "(do                                                                      " +
                                        "  (. jobj :setDoubleArray (aset (make-array :java.lang.Double 3) 1 9.0)) " +
                                        "  (pr-str (. jobj :getDoubleArray)))                                     ",
                                        object_symbols()));
    }

    @Test
    public void testObjStringArray() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(. jobj :getStringArray)", object_symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", object_symbols()));
        assertEquals(":core/vector", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (type (. jobj :getStringArray))))", object_symbols()));

        assertEquals("[a B c]", venice.eval("(do                                                        " +
                                            "  (. jobj :setStringArray '(\"a\" \"b\" \"c\"))            " +
                                            "  (let [v (apply mutable-vector (. jobj :getStringArray))] " +
                                            "     (assoc! v 1 \"B\")                                    " +
                                            "     (. jobj :setStringArray v)                            " +
                                            "     (str (. jobj :getStringArray))))                      ",
                                            object_symbols()));

        assertEquals("[]", venice.eval("(str (do (. jobj :setStringArray '()) (. jobj :getStringArray)))", object_symbols()));

        assertEquals("[a]", venice.eval("(str (do (. jobj :setStringArray \"a\") (. jobj :getStringArray)))", object_symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray '(\"a\" \"b\" \"c\")) (. jobj :getStringArray)))", object_symbols()));

        assertEquals("[a b c]", venice.eval("(str (do (. jobj :setStringArray (string-array '(\"a\" \"b\" \"c\"))) (. jobj :getStringArray)))", object_symbols()));

        assertEquals("[nil 9 nil]", venice.eval("(str (do (. jobj :setStringArray (aset (make-array :java.lang.String 3) 1 \"9\")) (. jobj :getStringArray)))", object_symbols()));
    }



    private Map<String, Object> primitive_symbols() {
        return Parameters.of("jobj", new PrimitiveJavaObject());
    }

    private Map<String, Object> object_symbols() {
        return Parameters.of("jobj", new JavaObject());
    }



    public static class PrimitiveJavaObject {
        public char[] getCharArray() {
            return charArray;
        }
        public void setCharArray(char[] charArray) {
            this.charArray = charArray;
        }
        public byte[] getByteArray() {
            return byteArray;
        }
        public void setByteArray(byte[] byteArray) {
            this.byteArray = byteArray;
        }
        public int[] getIntArray() {
            return intArray;
        }
        public void setIntArray(int[] intArray) {
            this.intArray = intArray;
        }
        public long[] getLongArray() {
            return longArray;
        }
        public void setLongArray(long[] longArray) {
            this.longArray = longArray;
        }
        public float[] getFloatArray() {
            return floatArray;
        }
        public void setFloatArray(float[] floatArray) {
            this.floatArray = floatArray;
        }
        public double[] getDoubleArray() {
            return doubleArray;
        }
        public void setDoubleArray(double[] doubleArray) {
            this.doubleArray = doubleArray;
        }

        private char[] charArray;
        private byte[] byteArray;
        private int[] intArray;
        private long[] longArray;
        private float[] floatArray;
        private double[] doubleArray;
    }


    public static class JavaObject {
        public Integer[] getIntArray() {
            return intArray;
        }
        public void setIntArray(Integer[] intArray) {
            this.intArray = intArray;
        }
        public Long[] getLongArray() {
            return longArray;
        }
        public void setLongArray(Long[] longArray) {
            this.longArray = longArray;
        }
        public Float[] getFloatArray() {
            return floatArray;
        }
        public void setFloatArray(Float[] floatArray) {
            this.floatArray = floatArray;
        }
        public Double[] getDoubleArray() {
            return doubleArray;
        }
        public void setDoubleArray(Double[] doubleArray) {
            this.doubleArray = doubleArray;
        }
        public String[] getStringArray() {
            return stringArray;
        }
        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }

        private Integer[] intArray;
        private Long[] longArray;
        private Float[] floatArray;
        private Double[] doubleArray;
        private String[] stringArray;
    }
}
