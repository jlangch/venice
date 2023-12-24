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


public class AnonymousFunctionTest {

    @Test
    public void test_args_none() {
        final Venice venice = new Venice();

        assertEquals(
            10L,
            venice.eval(
                "(do                         \n" +
                "  (defn foo [] 10)          \n" +
                "  (#(foo)))          "));
    }

    @Test
    public void test_args_one() {
        final Venice venice = new Venice();

        assertEquals(
            1L,
            venice.eval(
                "(do                          \n" +
                "  (defn foo [x] x)           \n" +
                "  (#(foo %) 1))                "));

        assertEquals(
                1L,
                venice.eval(
                    "(do                      \n" +
                    "  (defn foo [x] x)       \n" +
                    "  (#(foo %1) 1))           "));
    }

    @Test
    public void test_args_two() {
        final Venice venice = new Venice();

        assertEquals(
            3L,
            venice.eval(
                "(do                          \n" +
                "  (defn foo [x y] (+ x y))   \n" +
                "  (#(foo %1 %2) 1 2))          "));
    }

    @Test
    public void test_args_three() {
        final Venice venice = new Venice();

        assertEquals(
            6L,
            venice.eval(
                "(do                              \n" +
                "  (defn foo [x y z] (+ x y z))   \n" +
                "  (#(foo %1 %2 %3) 1 2 3))         "));
    }

    @Test
    public void test_args_four() {
        final Venice venice = new Venice();

        assertEquals(
            10L,
            venice.eval(
                "(do                                  \n" +
                "  (defn foo [a b c d] (+ a b c d))   \n" +
                "  (#(foo %1 %2 %3 %4) 1 2 3 4))        "));
    }

    @Test
    public void test_args_five() {
        final Venice venice = new Venice();

        assertEquals(
            15L,
            venice.eval(
                "(do                                      \n" +
                "  (defn foo [a b c d e] (+ a b c d e))   \n" +
                "  (#(foo %1 %2 %3 %4 %5) 1 2 3 4 5))       "));
    }

    @Test
    public void test_args_six() {
        final Venice venice = new Venice();

        assertEquals(
            21L,
            venice.eval(
                "(do                                          \n" +
                "  (defn foo [a b c d e f] (+ a b c d e f))   \n" +
                "  (#(foo %1 %2 %3 %4 %5 %6) 1 2 3 4 5 6))      "));
    }

    @Test
    public void test_args_seven() {
        final Venice venice = new Venice();

        assertEquals(
            28L,
            venice.eval(
                "(do                                              \n" +
                "  (defn foo [a b c d e f g] (+ a b c d e f g))   \n" +
                "  (#(foo %1 %2 %3 %4 %5 %6 %7) 1 2 3 4 5 6 7))     "));
    }

    @Test
    public void test_args_eight() {
        final Venice venice = new Venice();

        assertEquals(
            36L,
            venice.eval(
                "(do                                                  \n" +
                "  (defn foo [a b c d e f g h] (+ a b c d e f g h))   \n" +
                "  (#(foo %1 %2 %3 %4 %5 %6 %7 %8) 1 2 3 4 5 6 7 8))    "));
    }

    @Test
    public void test_args_nine() {
        final Venice venice = new Venice();

        assertEquals(
            45L,
            venice.eval(
                "(do                                                      \n" +
                "  (defn foo [a b c d e f g h i] (+ a b c d e f g h i))   \n" +
                "  (#(foo %1 %2 %3 %4 %5 %6 %7 %8 %9) 1 2 3 4 5 6 7 8 9))   "));
    }

    @Test
    public void test_args_ten() {
        final Venice venice = new Venice();

        assertEquals(
            55L,
            venice.eval(
                "(do                                                             \n" +
                "  (defn foo [a b c d e f g h i j] (+ a b c d e f g h i j))      \n" +
                "  (#(foo %1 %2 %3 %4 %5 %6 %7 %8 %9 %10) 1 2 3 4 5 6 7 8 9 10))   "));
    }

    @Test
    public void test_args_invalid() {
        final Venice venice = new Venice();

        // possible anonymous args: %1, %2, %3, %4, %5, %6, %7, %8, %9, %10, %, %&
        // %11 is not taken as recognized function arg
        assertThrows(
                ArityException.class,
                () -> venice.eval(
                        "(do                                                                    \n" +
                        "  (defn foo [a b c d e f g h i j k] (+ a b c d e f g h i j k))         \n" +
                        "  (#(foo %1 %2 %3 %4 %5 %6 %7 %8 %9 %10 %11) 1 2 3 4 5 6 7 8 9 10 11))   "));
    }


    @Test
    public void test_args_variadic_0() {
        final Venice venice = new Venice();

        assertEquals(
            0L,
            venice.eval(
                "(do                               \n" +
                "  (defn foo [x] (apply + x))      \n" +
                "  (#(foo %&)))                      "));
    }

    @Test
    public void test_args_variadic_1() {
        final Venice venice = new Venice();

        assertEquals(
            1L,
            venice.eval(
                "(do                               \n" +
                "  (defn foo [x] (apply + x))      \n" +
                "  (#(foo %&) 1))                    "));
    }

    @Test
    public void test_args_variadic_2() {
        final Venice venice = new Venice();

        assertEquals(
            3L,
            venice.eval(
                "(do                               \n" +
                "  (defn foo [x] (apply + x))      \n" +
                "  (#(foo %&) 1 2))                  "));
    }

    @Test
    public void test_args_variadic_3() {
        final Venice venice = new Venice();

        assertEquals(
            6L,
            venice.eval(
                "(do                               \n" +
                "  (defn foo [x] (apply + x))      \n" +
                "  (#(foo %&) 1 2 3))                "));
    }



    @Test
    public void test_args_variadic_mixed_1() {
        final Venice venice = new Venice();

        assertEquals(
            1L,
            venice.eval(
                "(do                                   \n" +
                "  (defn foo [x z] (apply + x z))      \n" +
                "  (#(foo %1 %&) 1))                     "));

        assertEquals(
            3L,
            venice.eval(
                "(do                                   \n" +
                "  (defn foo [x z] (apply + x z))      \n" +
                "  (#(foo %1 %&) 1 2))                   "));

        assertEquals(
            6L,
            venice.eval(
                "(do                                   \n" +
                "  (defn foo [x z] (apply + x z))      \n" +
                "  (#(foo %1 %&) 1 2 3))                 "));
    }

    @Test
    public void test_args_variadic_mixed_1_alt() {
        final Venice venice = new Venice();

        // Symbol '%' not found. '%' cannot be mixed with others -> use '%1'
        assertThrows(
            VncException.class,
            () -> venice.eval(
                    "(do                                   \n" +
                    "  (defn foo [x z] (apply + x z))      \n" +
                    "  (#(foo % %&) 1))                     "));
    }

    @Test
    public void test_args_variadic_mixed_2() {
        final Venice venice = new Venice();

        assertEquals(
            3L,
            venice.eval(
                "(do                                       \n" +
                "  (defn foo [x y z] (apply + x y z))      \n" +
                "  (#(foo %1 %2 %&) 1 2))                    "));

        assertEquals(
            6L,
            venice.eval(
                "(do                                       \n" +
                "  (defn foo [x y z] (apply + x y z))      \n" +
                "  (#(foo %1 %2 %&) 1 2 3))                  "));

        assertEquals(
            10L,
            venice.eval(
                "(do                                       \n" +
                "  (defn foo [x y z] (apply + x y z))      \n" +
                "  (#(foo %1 %2 %&) 1 2 3 4))                "));
    }

    @Test
    public void test_wrong_number_of_args() {
        final Venice venice = new Venice();

        assertThrows(
            ArityException.class,
            () -> venice.eval("(map #(+) [1 2 3])"));
    }

}
