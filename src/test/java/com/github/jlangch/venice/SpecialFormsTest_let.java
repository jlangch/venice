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


public class SpecialFormsTest_let {

    @Test
    public void test_let() {
        final Venice venice = new Venice();

        assertEquals(10L, venice.eval("(let [x 10] x)"));
        assertEquals(21L, venice.eval("(let [x 10 y 11] (+ x y))"));
        assertEquals(60L, venice.eval("(let [x 10 y (* x 5)] (+ x y))"));
    }

    @Test
    public void test_let_impaired() {
        final Venice venice = new Venice();

        assertThrows(VncException.class, () -> venice.eval("(let [x] x)"));
        assertThrows(VncException.class, () -> venice.eval("(let [x 10, y] x)"));
        assertThrows(VncException.class, () -> venice.eval("(let [[x y]] x)"));
    }

    @Test
    public void test_let_destructure() {
        final Venice venice = new Venice();

        assertEquals(21L, venice.eval("(let [[x y] [10 11]] (+ x y))"));
        assertEquals(71L, venice.eval("(let [[x y] [10 11] z (* x 5)] (+ x y z))"));
    }

    @Test
    public void test_let_9() {
        final Venice venice = new Venice();

        assertEquals(
            "[10 1 11]",
            venice.eval(
                    "(do                           \n" +
                    "  (defn sum [x y]             \n" +
                    "    (let [u 10]               \n" +
                    "      (let [y 11]             \n" +
                    "        [u x y])))            \n" +
                    "  (pr-str (sum 1 2)))"));
    }

    @Test
    public void test_let_10() {
        final Venice venice = new Venice();

        assertEquals(
            "[100 11 1 2]",
            venice.eval(
                    "(do                             \n" +
                    "  (let [a 100]                  \n" +
                    "    (defn sum [x y]             \n" +
                    "      (let [u 10]               \n" +
                    "        (let [u 11]             \n" +
                    "          [a u x y]))))         \n" +
                    "  (pr-str (sum 1 2)))"));
    }
}
