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



    // ------------------------------------------------------------------------
    // Multi arity tests
    // ------------------------------------------------------------------------

    @Test
    public void test_fn_call_multiarity_ok_1() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f))                                             ";

        assertEquals(0L, venice.eval(script1));

        final String script2 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f 1))                                           ";

        assertEquals(1L, venice.eval(script2));

        final String script3 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f 1 2))                                         ";

        assertEquals(3L, venice.eval(script3));

        final String script4 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f 1 2 3 4 5))                                   ";

        assertEquals(15L, venice.eval(script4));
    }

    @Test
    public void test_fn_call_multiarity_fail_1() {
        final Venice venice = new Venice();

        final String script2 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f :foo))                                        ";

        assertThrows(AssertionException.class, () -> venice.eval(script2));


        final String script3 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f 1 :foo))                                      ";

        assertThrows(AssertionException.class, () -> venice.eval(script3));


        final String script4 =
                "(do                                                 \n" +
                "   (defn f                                          \n" +
                "      ([] 0)                                        \n" +
                "      ([^:long x] x)                                \n" +
                "      ([^:long x ^:long y] (+ x y))                 \n" +
                "      ([^:long x ^:long y & xs] (apply + x y xs)))  \n" +
                "   (f 1 :foo 3 4 5))                                ";

        assertThrows(AssertionException.class, () -> venice.eval(script4));
    }



    // ------------------------------------------------------------------------
    // Sequential destructuring tests
    // ------------------------------------------------------------------------

    @Test
    public void test_fn_call_sequential_destructure_ok_1() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [[^:long x ^:long y]] (+ x y))           \n" +
                "   (f [1 2]))                                       ";

        assertEquals(3L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_ok_2() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [[^:long x _ ^:long y]] (+ x y))         \n" +
                "   (f [1 2 3]))                                     ";

        assertEquals(4L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_ok_3() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                      \n" +
                "   (defn f [[^:long x ^:long y & xs]] (apply + x y xs))  \n" +
                "   (f [1 2 3 4 5]))                                      ";

        assertEquals(15L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_ok_4() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                    \n" +
                "   (defn f [[^:long x [^:long y ^:long z]]] (+ x y z)) \n" +
                "   (f [1 [2 3]]))                                      ";

        assertEquals(6L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_fail_1() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [[^:long x ^:long y]] (+ x y))           \n" +
                "   (f [1 :foo]))                                       ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_fail_2() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [[^:long x _ ^:long y]] (+ x y))         \n" +
                "   (f [1 2 :foo]))                                     ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_fail_3() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                      \n" +
                "   (defn f [[^:long x ^:long y & xs]] (apply + x y xs))  \n" +
                "   (f [1 :foo 3 4 5]))                                      ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

    @Test
    public void test_fn_call_sequential_destructure_fail_4() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                    \n" +
                "   (defn f [[^:long x [^:long y ^:long z]]] (+ x y z)) \n" +
                "   (f [1 [2 :foo]]))                                      ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }


    // ------------------------------------------------------------------------
    // Associative destructuring tests
    // ------------------------------------------------------------------------

    @Test
    public void test_fn_call_associative_destructure_ok_1() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:keys [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {:x 1 :y 2}))                                 ";

        assertEquals(3L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_associative_destructure_ok_2() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:syms [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {'x 1 'y 2}))                                 ";

        assertEquals(3L, venice.eval(script1));
    }

    @Test
    public void test_fn_call_associative_destructure_ok_3() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:strs [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {\"x\" 1 \"y\" 2}))                           ";

        assertEquals(3L, venice.eval(script1));
    }


    @Test
    public void test_fn_call_associative_destructure_fail_1() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:keys [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {:x 1 :y :foo}))                              ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

    @Test
    public void test_fn_call_associative_destructure_fail_2() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:syms [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {'x 1 'y :foo}))                              ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

    @Test
    public void test_fn_call_associative_destructure_fail_3() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                                 \n" +
                "   (defn f [{:strs [^:long x ^:long y]}] (+ x y))   \n" +
                "   (f {\"x\" 1 \"y\" :foo}))                        ";

        assertThrows(AssertionException.class, () -> venice.eval(script1));
    }

}
