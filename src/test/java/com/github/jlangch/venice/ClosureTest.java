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


public class ClosureTest {

    @Test
    public void test_closure_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                          \n" +
                "  (defn pow [n]                                                              \n" +
                "    (fn [x] (apply * (repeat n x))))  ; closes over n                        \n" +
                "                                                                             \n" +
                "  ;; n is provided here as 2 and 3, then n goes out of scope                 \n" +
                "  (def square (pow 2))                                                       \n" +
                "  (def cubic (pow 3))                                                        \n" +
                "                                                                             \n" +
                "  ;; n value still available because square and cubic are closures           \n" +
                "  (pr-str [ (square 4) ; => 16   effectively as (apply * (repeat 2 4))       \n" +
                "            (cubic 4)  ; => 64   effectively as (apply * (repeat 3 4))       \n" +
                "          ]))                                                                ";

        assertEquals("[16 64]", venice.eval(script));
    }

    @Test
    public void test_closure_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                        \n" +
                "  (defn do-it []                                           \n" +
                "    (let [a [\"a\" \"b\" \"c\"]                            \n" +
                "          b [\"x\" \"y\" \"z\"]                            \n" +
                "          counter (atom 0)]                                \n" +
                "      (docoll (fn [foo]                                    \n" +
                "                (reset! counter 0)                         \n" +
                "                (docoll (fn [bar]                          \n" +
                "                          (swap! counter inc)              \n" +
                "                          (println [:counter @counter      \n" +
                "                                    :foo foo               \n" +
                "                                    :bar bar]))            \n" +
                "                        b))                                \n" +
                "              a)))                                         \n" +
                "   (with-out-str (do-it)))                                 ";

        assertEquals(
                "[:counter 1 :foo a :bar x]\n" +
                "[:counter 2 :foo a :bar y]\n" +
                "[:counter 3 :foo a :bar z]\n" +
                "[:counter 1 :foo b :bar x]\n" +
                "[:counter 2 :foo b :bar y]\n" +
                "[:counter 3 :foo b :bar z]\n" +
                "[:counter 1 :foo c :bar x]\n" +
                "[:counter 2 :foo c :bar y]\n" +
                "[:counter 3 :foo c :bar z]\n",
                venice.eval(script));
    }

}
