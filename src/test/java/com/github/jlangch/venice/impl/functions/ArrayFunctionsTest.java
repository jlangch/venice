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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class ArrayFunctionsTest {

    @Test
    public void test_aset() {
        final Venice venice = new Venice();

        assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 0 20I)"))[0]);
        assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 1 20I)"))[1]);
        assertEquals(20, ((int[])venice.eval("(aset (int-array '(1I 2I 3I 4I 5I)) 4 20I)"))[4]);

        assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 0 20)"))[0]);
        assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 1 20)"))[1]);
        assertEquals(20L, ((long[])venice.eval("(aset (long-array '(1 2 3 4 5)) 4 20)"))[4]);

        assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 0 20.1)"))[0]);
        assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.1)"))[1]);
        assertEquals(20.1F, ((float[])venice.eval("(aset (float-array '(1.0 2.0 3.0 4.0 5.0)) 4 20.1)"))[4]);

        assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 0 20.1)"))[0]);
        assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.1)"))[1]);
        assertEquals(20.1D, ((double[])venice.eval("(aset (double-array '(1.0 2.0 3.0 4.0 5.0)) 4 20.1)"))[4]);

        assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0 \"20\")"))[0]);
        assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1 \"20\")"))[1]);
        assertEquals("20", ((String[])venice.eval("(aset (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4 \"20\")"))[4]);

        assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0 \"20\")"))[0]);
        assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1 \"20\")"))[1]);
        assertEquals("20", ((Object[])venice.eval("(aset (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4 \"20\")"))[4]);

        assertEquals(99, ((Integer[])venice.eval("(aset (make-array :java.lang.Integer 5) 3 99I)"))[3]);
        assertEquals(99.0D, ((Double[])venice.eval("(aset (make-array :java.lang.Double 5) 3 99.0)"))[3]);
        assertEquals("99", ((String[])venice.eval("(aset (make-array :java.lang.String 5) 3 \"99\")"))[3]);
    }

    @Test
    public void test_aget() {
        final Venice venice = new Venice();

        assertEquals(1, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 0)"));
        assertEquals(2, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 1)"));
        assertEquals(5, venice.eval("(aget (int-array '(1I 2I 3I 4I 5I)) 4)"));

        assertEquals(1L, venice.eval("(aget (long-array '(1 2 3 4 5)) 0)"));
        assertEquals(2L, venice.eval("(aget (long-array '(1 2 3 4 5)) 1)"));
        assertEquals(5L, venice.eval("(aget (long-array '(1 2 3 4 5)) 4)"));

        assertEquals(1.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 0)"));
        assertEquals(2.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 1)"));
        assertEquals(5.0D, venice.eval("(aget (float-array '(1.0 2.0 3.0 4.0 5.0)) 4)"));

        assertEquals(1.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 0)"));
        assertEquals(2.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 1)"));
        assertEquals(5.0D, venice.eval("(aget (double-array '(1.0 2.0 3.0 4.0 5.0)) 4)"));

        assertEquals("1", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0)"));
        assertEquals("2", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1)"));
        assertEquals("5", venice.eval("(aget (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4)"));

        assertEquals("1", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 0)"));
        assertEquals("2", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 1)"));
        assertEquals("5", venice.eval("(aget (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")) 4)"));

        assertEquals(null, venice.eval("(aget (make-array :java.lang.Integer 5) 0)"));
    }

    @Test
    public void test_alength() {
        final Venice venice = new Venice();

        assertEquals(0L, venice.eval("(alength (int-array '()))"));
        assertEquals(1L, venice.eval("(alength (int-array '(1I)))"));
        assertEquals(5L, venice.eval("(alength (int-array '(1I 2I 3I 4I 5I)))"));

        assertEquals(0L, venice.eval("(alength (long-array '()))"));
        assertEquals(1L, venice.eval("(alength (long-array '(1)))"));
        assertEquals(5L, venice.eval("(alength (long-array '(1 2 3 4 5)))"));

        assertEquals(0L, venice.eval("(alength (float-array '()))"));
        assertEquals(1L, venice.eval("(alength (float-array '(1.0)))"));
        assertEquals(5L, venice.eval("(alength (float-array '(1.0 2.0 3.0 4.0 5.0)))"));

        assertEquals(0L, venice.eval("(alength (double-array '()))"));
        assertEquals(1L, venice.eval("(alength (double-array '(1.0)))"));
        assertEquals(5L, venice.eval("(alength (double-array '(1.0 2.0 3.0 4.0 5.0)))"));

        assertEquals(0L, venice.eval("(alength (string-array '()))"));
        assertEquals(1L, venice.eval("(alength (string-array '(\"1\")))"));
        assertEquals(5L, venice.eval("(alength (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));

        assertEquals(0L, venice.eval("(alength (object-array '()))"));
        assertEquals(1L, venice.eval("(alength (object-array '(\"1\")))"));
        assertEquals(5L, venice.eval("(alength (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));

        assertEquals(5L, venice.eval("(alength (make-array :java.lang.Integer 5))"));
    }

    @Test
    public void test_asub() {
        final Venice venice = new Venice();

        assertEquals("[3]",       Arrays.toString((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 1)")));
        assertEquals("[3, 4]",    Arrays.toString((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 2)")));
        assertEquals("[3, 4, 5]", Arrays.toString((int[])venice.eval("(asub (int-array '(1I 2I 3I 4I 5I)) 2 3)")));

        assertEquals("[3]",       Arrays.toString((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 1)")));
        assertEquals("[3, 4]",    Arrays.toString((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 2)")));
        assertEquals("[3, 4, 5]", Arrays.toString((long[])venice.eval("(asub (long-array '(1 2 3 4 5)) 2 3)")));

        assertEquals("[3.0]",           Arrays.toString((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 1)")));
        assertEquals("[3.0, 4.0]",      Arrays.toString((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 2)")));
        assertEquals("[3.0, 4.0, 5.0]", Arrays.toString((float[])venice.eval("(asub (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 3)")));

        assertEquals("[3.0]",           Arrays.toString((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 1)")));
        assertEquals("[3.0, 4.0]",      Arrays.toString((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 2)")));
        assertEquals("[3.0, 4.0, 5.0]", Arrays.toString((double[])venice.eval("(asub (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 3)")));

        assertEquals("[c]",       Arrays.toString((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 1)")));
        assertEquals("[c, d]",    Arrays.toString((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 2)")));
        assertEquals("[c, d, e]", Arrays.toString((String[])venice.eval("(asub (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 3)")));

        assertEquals("[c]",       Arrays.toString((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 1)")));
        assertEquals("[c, d]",    Arrays.toString((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 2)")));
        assertEquals("[c, d, e]", Arrays.toString((Object[])venice.eval("(asub (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 3)")));
    }

    @Test
    public void test_acopy() {
        final Venice venice = new Venice();

        assertEquals("[1, 2, 3, 4, 5, 0, 0, 0]", Arrays.toString((int[])venice.eval("(acopy (int-array '(1I 2I 3I 4I 5I)) 0 (int-array 8 0I) 0 5)")));
        assertEquals("[0, 2, 3, 0, 0, 0, 0, 0]", Arrays.toString((int[])venice.eval("(acopy (int-array '(1I 2I 3I 4I 5I)) 1 (int-array 8 0I) 1 2)")));
        assertEquals("[0, 0, 0, 0, 3, 0, 0, 0]", Arrays.toString((int[])venice.eval("(acopy (int-array '(1I 2I 3I 4I 5I)) 2 (int-array 8 0I) 4 1)")));

        assertEquals("[1, 2, 3, 4, 5, 0, 0, 0]", Arrays.toString((long[])venice.eval("(acopy (long-array '(1 2 3 4 5)) 0 (long-array 8 0) 0 5)")));
        assertEquals("[0, 2, 3, 0, 0, 0, 0, 0]", Arrays.toString((long[])venice.eval("(acopy (long-array '(1 2 3 4 5)) 1 (long-array 8 0) 1 2)")));
        assertEquals("[0, 0, 0, 0, 3, 0, 0, 0]", Arrays.toString((long[])venice.eval("(acopy (long-array '(1 2 3 4 5)) 2 (long-array 8 0) 4 1)")));

        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 0.0, 0.0]", Arrays.toString((float[])venice.eval("(acopy (float-array '(1.0 2.0 3.0 4.0 5.0)) 0 (float-array 8 0.0) 0 5)")));
        assertEquals("[0.0, 2.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0]", Arrays.toString((float[])venice.eval("(acopy (float-array '(1.0 2.0 3.0 4.0 5.0)) 1 (float-array 8 0.0) 1 2)")));
        assertEquals("[0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0]", Arrays.toString((float[])venice.eval("(acopy (float-array '(1.0 2.0 3.0 4.0 5.0)) 2 (float-array 8 0.0) 4 1)")));

        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 0.0, 0.0]", Arrays.toString((double[])venice.eval("(acopy (double-array '(1.0 2.0 3.0 4.0 5.0)) 0 (double-array 8 0.0) 0 5)")));
        assertEquals("[0.0, 2.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0]", Arrays.toString((double[])venice.eval("(acopy (double-array '(1.0 2.0 3.0 4.0 5.0)) 1 (double-array 8 0.0) 1 2)")));
        assertEquals("[0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0]", Arrays.toString((double[])venice.eval("(acopy (double-array '(1.0 2.0 3.0 4.0 5.0)) 2 (double-array 8 0.0) 4 1)")));

        assertEquals("[a, b, c, d, e, -, -, -]", Arrays.toString((String[])venice.eval("(acopy (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 0 (string-array 8 \"-\") 0 5)")));
        assertEquals("[-, b, c, -, -, -, -, -]", Arrays.toString((String[])venice.eval("(acopy (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 1 (string-array 8 \"-\") 1 2)")));
        assertEquals("[-, -, -, -, c, -, -, -]", Arrays.toString((String[])venice.eval("(acopy (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 (string-array 8 \"-\") 4 1)")));

        assertEquals("[a, b, c, d, e, -, -, -]", Arrays.toString((Object[])venice.eval("(acopy (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 0 (object-array 8 \"-\") 0 5)")));
        assertEquals("[-, b, c, -, -, -, -, -]", Arrays.toString((Object[])venice.eval("(acopy (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 1 (object-array 8 \"-\") 1 2)")));
        assertEquals("[-, -, -, -, c, -, -, -]", Arrays.toString((Object[])venice.eval("(acopy (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")) 2 (object-array 8 \"-\") 4 1)")));
    }

    @Test
    public void test_amap() {
        final Venice venice = new Venice();

        assertEquals("[2, 3, 4, 5, 6]", Arrays.toString((int[])venice.eval("(amap (fn [x] (+ 1I x)) (int-array '(1I 2I 3I 4I 5I)))")));
        assertEquals("[2, 3, 4, 5, 6]", Arrays.toString((long[])venice.eval("(amap (fn [x] (+ 1 x)) (long-array '(1 2 3 4 5)))")));
        assertEquals("[2.0, 3.0, 4.0, 5.0, 6.0]", Arrays.toString((float[])venice.eval("(amap (fn [x] (+ 1.0 x)) (float-array '(1.0 2.0 3.0 4.0 5.0))))")));
        assertEquals("[2.0, 3.0, 4.0, 5.0, 6.0]", Arrays.toString((double[])venice.eval("(amap (fn [x] (+ 1.0 x)) (double-array '(1.0 2.0 3.0 4.0 5.0))))")));
        assertEquals("[>a, >b, >c, >d, >e]", Arrays.toString((String[])venice.eval("(amap (fn [x] (str \">\" x)) (string-array '(\"a\" \"b\" \"c\" \"d\" \"e\")))")));
        assertEquals("[>a, >b, >c, >d, >e]", Arrays.toString((Object[])venice.eval("(amap (fn [x] (str \">\" x)) (object-array '(\"a\" \"b\" \"c\" \"d\" \"e\")))")));

        assertEquals("[2, 3, 4, 5, 6]", Arrays.toString((long[])venice.eval("(amap (partial + 1) (long-array '(1 2 3 4 5)))")));
    }

    @Test
    public void test_amap_native() {
        new Venice().eval(
            "(do " +
            "  (perf (amap (fn [x] (+ 200 x)) (long-array 25000)) 200 100) " +
            "  (println (prof :data-formatted \"Metrics amap native: (fn [x] (+ 200 x))\")))");


        final long start = System.nanoTime();

        final long[] arrSrc = new long[25000];
        final long[] arrDst = new long[25000];
        for(int ii=0; ii<arrSrc.length; ii++) {
            arrDst[ii] = arrSrc[ii] + 200;
        }

        System.out.println("amap native: " + ((System.nanoTime() - start) / 1000) + " us");
    }

    @Test
    public void test_amap_native_2() {
        new Venice().eval(
            "(do " +
            "  (perf (amap (partial + 200) (long-array 25000)) 500 300) " +
            "  (println (prof :data-formatted \"Metrics amap native: (partial + 200)\")))");


        final long start = System.nanoTime();

        final long[] arrSrc = new long[25000];
        final long[] arrDst = new long[25000];
        for(int ii=0; ii<arrSrc.length; ii++) {
            arrDst[ii] = arrSrc[ii] + 200;
        }

        System.out.println("amap native: " + ((System.nanoTime() - start) / 1000) + " us");
    }

    @Test
    public void test_int_array() {
        final Venice venice = new Venice();

        assertEquals("[]",              Arrays.toString((int[])venice.eval("(int-array '())")));
        assertEquals("[1]",             Arrays.toString((int[])venice.eval("(int-array '(1I))")));
        assertEquals("[1, 2, 3, 4, 5]", Arrays.toString((int[])venice.eval("(int-array '(1I 2 3.0 4.0M 5I))")));

        assertEquals("[]",              Arrays.toString((int[])venice.eval("(int-array 0)")));
        assertEquals("[0, 0, 0, 0, 0]", Arrays.toString((int[])venice.eval("(int-array 5)")));

        assertEquals("[]",              Arrays.toString((int[])venice.eval("(int-array 0 9I)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((int[])venice.eval("(int-array 5 9I)")));

        assertEquals("[]",              Arrays.toString((int[])venice.eval("(int-array 0 9)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((int[])venice.eval("(int-array 5 9)")));

        assertEquals("[]",              Arrays.toString((int[])venice.eval("(int-array 0 9.0)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((int[])venice.eval("(int-array 5 9.0)")));
    }

    @Test
    public void test_long_array() {
        final Venice venice = new Venice();

        assertEquals("[]",              Arrays.toString((long[])venice.eval("(long-array '())")));
        assertEquals("[1]",             Arrays.toString((long[])venice.eval("(long-array '(1))")));
        assertEquals("[1, 2, 3, 4, 5]", Arrays.toString((long[])venice.eval("(long-array '(1 2I 3.0 4.0M 5))")));

        assertEquals("[]",              Arrays.toString((long[])venice.eval("(long-array 0)")));
        assertEquals("[0, 0, 0, 0, 0]", Arrays.toString((long[])venice.eval("(long-array 5)")));

        assertEquals("[]",              Arrays.toString((long[])venice.eval("(long-array 0 9)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((long[])venice.eval("(long-array 5 9)")));

        assertEquals("[]",              Arrays.toString((long[])venice.eval("(long-array 0 9I)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((long[])venice.eval("(long-array 5 9I)")));

        assertEquals("[]",              Arrays.toString((long[])venice.eval("(long-array 0 9.0)")));
        assertEquals("[9, 9, 9, 9, 9]", Arrays.toString((long[])venice.eval("(long-array 5 9.0)")));
    }

    @Test
    public void test_float_array() {
        final Venice venice = new Venice();

        assertEquals("[]",                        Arrays.toString((float[])venice.eval("(float-array '())")));
        assertEquals("[1.0]",                     Arrays.toString((float[])venice.eval("(float-array '(1.0))")));
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0]", Arrays.toString((float[])venice.eval("(float-array '(1 2I 3.0 4.0M 5))")));

        assertEquals("[]",                        Arrays.toString((float[])venice.eval("(float-array 0)")));
        assertEquals("[0.0, 0.0, 0.0, 0.0, 0.0]", Arrays.toString((float[])venice.eval("(float-array 5)")));

        assertEquals("[]",                        Arrays.toString((float[])venice.eval("(float-array 0 9.0)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((float[])venice.eval("(float-array 5 9.0)")));

        assertEquals("[]",                        Arrays.toString((float[])venice.eval("(float-array 0 9)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((float[])venice.eval("(float-array 5 9)")));

        assertEquals("[]",                        Arrays.toString((float[])venice.eval("(float-array 0 9I)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((float[])venice.eval("(float-array 5 9I)")));
    }

    @Test
    public void test_double_array() {
        final Venice venice = new Venice();

        assertEquals("[]",                        Arrays.toString((double[])venice.eval("(double-array '())")));
        assertEquals("[1.0]",                     Arrays.toString((double[])venice.eval("(double-array '(1.0))")));
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0]", Arrays.toString((double[])venice.eval("(double-array '(1 2I 3.0 4.0M 5))")));

        assertEquals("[]",                        Arrays.toString((double[])venice.eval("(double-array 0)")));
        assertEquals("[0.0, 0.0, 0.0, 0.0, 0.0]", Arrays.toString((double[])venice.eval("(double-array 5)")));

        assertEquals("[]",                        Arrays.toString((double[])venice.eval("(double-array 0 9.0)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((double[])venice.eval("(double-array 5 9.0)")));

        assertEquals("[]",                        Arrays.toString((double[])venice.eval("(double-array 0 9)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((double[])venice.eval("(double-array 5 9)")));

        assertEquals("[]",                        Arrays.toString((double[])venice.eval("(double-array 0 9I)")));
        assertEquals("[9.0, 9.0, 9.0, 9.0, 9.0]", Arrays.toString((double[])venice.eval("(double-array 5 9I)")));
    }

    @Test
    public void test_string_array() {
        final Venice venice = new Venice();

        assertEquals("[]",              Arrays.toString((String[])venice.eval("(string-array '())")));
        assertEquals("[a]",             Arrays.toString((String[])venice.eval("(string-array '(\"a\"))")));
        assertEquals("[a, b, c, d, e]", Arrays.toString((String[])venice.eval("(string-array '(\"a\" \"b\" \"c\" \"d\" \"e\"))")));

        assertEquals("[]",              Arrays.toString((String[])venice.eval("(string-array 0)")));
        assertEquals("[null, null, null, null, null]", Arrays.toString((String[])venice.eval("(string-array 5)")));

        assertEquals("[]",              Arrays.toString((String[])venice.eval("(string-array 0 \"-\")")));
        assertEquals("[-, -, -, -, -]", Arrays.toString((String[])venice.eval("(string-array 5 \"-\")")));

        assertEquals("[]",              Arrays.toString((String[])venice.eval("(string-array 0 \"-\")")));
        assertEquals("[-, -, -, -, -]", Arrays.toString((String[])venice.eval("(string-array 5 \"-\")")));
    }

    @Test
    public void test_object_array() {
        final Venice venice = new Venice();

        assertEquals("[]",              Arrays.toString((Object[])venice.eval("(object-array '())")));
        assertEquals("[a]",             Arrays.toString((Object[])venice.eval("(object-array '(\"a\"))")));
        assertEquals("[a, b, c, d, e]", Arrays.toString((Object[])venice.eval("(object-array '(\"a\" \"b\" \"c\" \"d\" \"e\"))")));

        assertEquals("[]",               Arrays.toString((Object[])venice.eval("(object-array 0)")));
        assertEquals("[null, null, null, null, null]", Arrays.toString((Object[])venice.eval("(object-array 5)")));

        assertEquals("[]",              Arrays.toString((Object[])venice.eval("(object-array 0 \"-\")")));
        assertEquals("[-, -, -, -, -]", Arrays.toString((Object[])venice.eval("(object-array 5 \"-\")")));

        assertEquals("[]",              Arrays.toString((Object[])venice.eval("(object-array 0 \"-\")")));
        assertEquals("[-, -, -, -, -]", Arrays.toString((Object[])venice.eval("(object-array 5 \"-\")")));
    }

    @Test
    public void test_make_array() {
        final Venice venice = new Venice();

        assertEquals("[0, 0, 0, 0, 0]", Arrays.toString((long[])venice.eval("(make-array :long 5)")));
        assertEquals("[null, null, null, null, null]", Arrays.toString((Long[])venice.eval("(make-array :java.lang.Long 5)")));
        assertEquals("[[0 0 0], [0 0 0]]", venice.eval("(str (make-array :long 2 3))"));
        assertEquals("[[0 0 0], [0 9 0]]", venice.eval("(str (let [arr (make-array :long 2 3)] (aset (aget arr 1) 1 9) arr))"));
    }

    @Test
    public void test_str() {
        final Venice venice = new Venice();

        assertEquals("[]", venice.eval("(str (int-array '()))"));
        assertEquals("[1I]", venice.eval("(str (int-array '(1I)))"));
        assertEquals("[1I, 2I, 3I, 4I, 5I]", venice.eval("(str (int-array '(1I 2I 3I 4I 5I)))"));

        assertEquals("[]", venice.eval("(str (long-array '()))"));
        assertEquals("[1]", venice.eval("(str (long-array '(1)))"));
        assertEquals("[1, 2, 3, 4, 5]", venice.eval("(str (long-array '(1 2 3 4 5)))"));

        assertEquals("[]", venice.eval("(str (float-array '()))"));
        assertEquals("[1.0]", venice.eval("(str (float-array '(1.0)))"));
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0]", venice.eval("(str (float-array '(1.0 2.0 3.0 4.0 5.0)))"));

        assertEquals("[]", venice.eval("(str (double-array '()))"));
        assertEquals("[1.0]", venice.eval("(str (double-array '(1.0)))"));
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0]", venice.eval("(str (double-array '(1.0 2.0 3.0 4.0 5.0)))"));

        assertEquals("[]", venice.eval("(str (string-array '()))"));
        assertEquals("[1]", venice.eval("(str (string-array '(\"1\")))"));
        assertEquals("[1, 2, 3, 4, 5]", venice.eval("(str (string-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));

        assertEquals("[]", venice.eval("(str (object-array '()))"));
        assertEquals("[1]", venice.eval("(str (object-array '(\"1\")))"));
        assertEquals("[1, 2, 3, 4, 5]", venice.eval("(str (object-array '(\"1\" \"2\" \"3\" \"4\" \"5\")))"));

        assertEquals("[nil, nil, nil, nil, nil]", venice.eval("(str (make-array :java.lang.Integer 5))"));
    }
}
