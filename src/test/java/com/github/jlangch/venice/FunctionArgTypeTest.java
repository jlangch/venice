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
package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class FunctionArgTypeTest {

    // ------------------------------------------------------------------------
    // Note on clojure
    //
    // (do
    //   (defn ^Long add [^Long x ^Long y] (+ x y))
    //   (println (add 1 2)))
    //
    // 1. Clojure applies dynamic type coercion (one can pass a double to a long
    // 2. Clojure does not enforce return type hints
    // 3. If a type hint is given the value nil cannot be passed
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // Basic tests
    // ------------------------------------------------------------------------

    @Test
    public void test_fn_call_ok_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f 10))                     ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_ok_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:core/long x] 0)  \n" +
                "   (f 10))                     ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_fail_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f 10I))                    ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_fn_call_fail_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f :err))                   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_fn_call_fail_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f nil))                    ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }



    // ------------------------------------------------------------------------
    // Polymorphism tests
    // ------------------------------------------------------------------------

    @Test
    public void test_fn_call_poly_ok_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:number x] 0)     \n" +
                "   (f 10))                     ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_poly_ok_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                              \n" +
                "   (defn f [^:core/number x] 0)  \n" +
                "   (f 10))                       ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_poly_ok_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:number x] 0)     \n" +
                "   (f 10345.89M))              ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_poly_ok_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                              \n" +
                "   (defn f [^:core/number x] 0)  \n" +
                "   (f 10000N))                   ";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_fn_call_poly_fail_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f true))                   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

    @Test
    public void test_fn_call_poly_fail_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                            \n" +
                "   (defn f [^:long x] 0)       \n" +
                "   (f :err))                   ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

}
