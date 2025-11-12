/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.impl.macros;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class MacroListComprehensionTest {

    @Test
    public void test_list_comp() {
        final Venice venice = new Venice();

        assertEquals(
                "(nil)",
                venice.eval("(str (list-comp [] nil))"));

        assertEquals(
                "(nil)",
                venice.eval("(str (list-comp nil nil))"));

        assertEquals(
                "(nil)",
                venice.eval("(str (list-comp [nil '()] nil))"));

        assertEquals(
                "(nil)",
                venice.eval("(str (list-comp [nil nil] nil))"));

        assertEquals(
                "()",
                venice.eval("(str (list-comp [x '()] x))"));

        assertEquals(
                "()",
                venice.eval("(str (list-comp [x '(1 2) y '()] [x y]))"));

        assertEquals(
                "()",
                venice.eval("(str (list-comp [x '() y '()] [x y]))"));



        assertEquals(
                "(0 1 2 3 4)",
                venice.eval("(str (list-comp [x (range 5)] x))"));

        assertEquals(
                "(0 2 4 6 8)",
                venice.eval("(str (list-comp [x (range 5)] (* x 2)))"));



        assertEquals(
                "([0 0] [0 1] [1 0] [1 1])",
                venice.eval("(str (list-comp [x (range 2) y (range 2)] [x y]))"));

        assertEquals(
                "([0 0] [0 4] [2 0] [2 4])",
                venice.eval("(str (list-comp [x (range 2) y (range 2)] [(* x 2) (* y 4)]))"));

        assertEquals(
                "([a 0] [a 1] [b 0] [b 1])",
                venice.eval("(str (list-comp [x (seq \"ab\") y [0 1]] [x y]))"));



        assertEquals(
                "(1 3 5 7 9)",
                venice.eval("(str (list-comp [x (range 10) :when (odd? x)] x))"));

        assertEquals(
                "(2 6 10 14 18)",
                venice.eval("(str (list-comp [x (range 10) :when (odd? x)] (* x 2)))"));

        assertEquals(
                "([1 0] [1 1])",
                venice.eval("(str (list-comp [x '(0 1) y '(0 1) :when (odd? x)] [x y]))"));

        assertEquals(
                "([1 0] [1 1])",
                venice.eval("(str (list-comp [x '(0 1) :when (odd? x) y '(0 1)] [x y]))"));
    }


    @Test
    public void test_for_1() {
        final Venice venice = new Venice();

        assertEquals(
                "[]",
                venice.eval("(str (for [x '()] x))"));

        assertEquals(
                "[]",
                venice.eval("(str (for [x '(1 2) y '()] [x y]))"));

        assertEquals(
                "[]",
                venice.eval("(str (for [x '() y '()] [x y]))"));



        assertEquals(
                "[0 1 2 3 4]",
                venice.eval("(str (for [x (range 5)] x))"));

        assertEquals(
                "[0 2 4 6 8]",
                venice.eval("(str (for [x (range 5)] (* x 2)))"));



        assertEquals(
                "[[0 0] [0 1] [1 0] [1 1]]",
                venice.eval("(str (for [x (range 2) y (range 2)] [x y]))"));

        assertEquals(
                "[[0 0] [0 4] [2 0] [2 4]]",
                venice.eval("(str (for [x (range 2) y (range 2)] [(* x 2) (* y 4)]))"));

        assertEquals(
                "[[a 0] [a 1] [b 0] [b 1]]",
                venice.eval("(str (for [x (seq \"ab\") y [0 1]] [x y]))"));



        assertEquals(
                "[1 3 5 7 9]",
                venice.eval("(str (for [x (range 10) :when (odd? x)] x))"));

        assertEquals(
                "[2 6 10 14 18]",
                venice.eval("(str (for [x (range 10) :when (odd? x)] (* x 2)))"));

        assertEquals(
                "[[1 0] [1 1]]",
                venice.eval("(str (for [x '(0 1) y '(0 1) :when (odd? x)] [x y]))"));

        assertEquals(
                "[[1 0] [1 1]]",
                venice.eval("(str (for [x '(0 1) :when (odd? x) y '(0 1)] [x y]))"));
    }

}
