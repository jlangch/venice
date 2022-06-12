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

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_fn {

    @Test
    public void test_fn_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "  (def sum (fn [x] (+ x 1)))  \n" +
                "  (sum 2))                    ";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_fn_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                \n" +
                "  (def sum (fn sum_ [x] (+ x 1)))  \n" +
                "  (sum 2))                         ";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_fn_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "  (def sum (fn [x] (+ x 1)))  \n" +
                "  (user/sum 2))               ";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_fn_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "  (ns foo)                    \n" +
                "  (def sum (fn [x] (+ x 1)))  \n" +
                "  (ns bar)                    \n" +
                "  (foo/sum 2))                ";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_fn_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                   \n" +
                "  (ns foo)                            \n" +
                "  (defmacro foo/fn ([x] `(+ 1 ~x)))   \n" +
                "  (defn foo/test-fn [x] x)            \n" +
                "  (foo/test-fn 3))                    ";

        assertEquals(3L, venice.eval(script));
    }

}
