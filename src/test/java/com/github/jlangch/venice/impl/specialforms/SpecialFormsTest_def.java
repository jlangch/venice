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


public class SpecialFormsTest_def {

    @Test
    public void test_def_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                    \n" +
                "   (def x 100)         \n" +
                "   x)                    ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_def_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "  (def x 100)             \n" +
                "  (pr-str                 \n" +
                "    [ x                   \n" +
                "      (do (def x 10) x)   \n" +
                "      (do (def x 20) x)   \n" +
                "    ]))";

        assertEquals("[100 10 20]", venice.eval(script));
    }

    @Test
    public void test_def_threads() {
        final Venice venice = new Venice();

        final String script =
                "(do                                        \n" +
                "  (def x 100)                              \n" +
                "  (pr-str                                  \n" +
                "    [ x                                    \n" +
                "      @(future #(do (def x 10) x))         \n" +
                "      x                                    \n" +
                "      @(future #(do (def x 20) x))         \n" +
                "      x ]))";

        assertEquals("[100 10 10 20 20]", venice.eval(script));
    }

}
