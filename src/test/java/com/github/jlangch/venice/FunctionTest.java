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

import org.junit.jupiter.api.Test;


public class FunctionTest {

    @Test
    public void test_fn_call() {
        final Venice venice = new Venice();

        final String script =
                "(do                        \n" +
                "   (defn f1 [x] 100)       \n" +
                "   (defn f2 [x] (f1 x))    \n" +
                "                           \n" +
                "   (f2 10))                 ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_body_empty() {
        final Venice venice = new Venice();

        assertEquals(
            null,
            venice.eval(
                "(do                \n" +
                "  (defn foo [] )   \n" +
                "  (foo))             "));
    }

    @Test
    public void test_body_no_side_effects_1() {
        final Venice venice = new Venice();

        assertEquals(
            1L,
            venice.eval(
                "(do                \n" +
                "  (defn foo [] 1)  \n" +
                "  (foo))             "));
    }

    @Test
    public void test_body_no_side_effects_2() {
        final Venice venice = new Venice();

        assertEquals(
            3L,
            venice.eval(
                "(do                      \n" +
                "  (defn foo [] (+ 1 2))  \n" +
                "  (foo))                   "));
    }

    @Test
    public void test_body_with_side_effects_1() {
        final Venice venice = new Venice();

        assertEquals(
            "[1 11]",
            venice.eval(
                "(do                                    \n" +
                "  (def counter (atom 10))              \n" +
                "  (defn foo [] (swap! counter inc) 1)  \n" +
                "  (pr-str [(foo) (deref counter)]))      "));
    }

    @Test
    public void test_body_with_side_effects_2() {
        final Venice venice = new Venice();

        assertEquals(
            "[3 12]",
            venice.eval(
                "(do                                    \n" +
                "  (def counter (atom 10))              \n" +
                "  (defn foo []                         \n" +
                "     (swap! counter inc)               \n" +
                "     (swap! counter inc)               \n" +
                "     (+ 1 2))                          \n" +
                "  (pr-str [(foo) (deref counter)]))      "));
    }

    @Test
    public void test_body_with_side_effects_3() {
        final Venice venice = new Venice();

        assertEquals(
            "[3 13]",
            venice.eval(
                "(do                                    \n" +
                "  (def counter (atom 10))              \n" +
                "  (defn foo []                         \n" +
                "     (swap! counter inc)               \n" +
                "     (swap! counter inc)               \n" +
                "     (swap! counter inc)               \n" +
                "     (+ 1 2))                          \n" +
                "  (pr-str [(foo) (deref counter)]))      "));
    }

    @Test
    public void test_fn_name() {
        final Venice venice = new Venice();

        assertEquals("+", venice.eval("(fn-name +)"));

        assertEquals("println", venice.eval("(fn-name println)"));

        assertEquals("user/sum", venice.eval("(fn-name (fn sum [x y] (+ x y)))"));

        assertEquals("user/math", venice.eval("(fn-name (defmulti math (fn [op _ _] op))))"));
    }

    @Test
    public void test_fn_about() {
        final Venice venice = new Venice();

        venice.eval("(fn-about +)");

        venice.eval("(fn-about println)");

        venice.eval("(fn-about or)");

    }

}
