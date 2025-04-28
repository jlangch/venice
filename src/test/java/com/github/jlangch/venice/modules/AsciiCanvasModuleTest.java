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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class AsciiCanvasModuleTest {

    @Test
    public void test_001_box() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-horizontal-right #\\- w 0 0)         \n" +
                              "        (ac/draw-horizontal-right #\\- w 0 (dec h))   \n" +
                              "        (ac/draw-vertical-up #\\| h 0   0)            \n" +
                              "        (ac/draw-vertical-up #\\| h (dec w) 0)        \n" +
                              "        (ac/draw #\\+ 0 0)                            \n" +
                              "        (ac/draw #\\+ (dec w) 0)                      \n" +
                              "        (ac/draw #\\+ 0   (dec h))                    \n" +
                              "        (ac/draw #\\+ (dec w) (dec h))                \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("+--------+\n" +
                     "|        |\n" +
                     "|        |\n" +
                     "|        |\n" +
                     "+--------+", venice.eval(script));
    }

    @Test
    public void test_002_box() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/box 0 0 10 5 \"++++-|-|\")                \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("+--------+\n" +
                     "|        |\n" +
                     "|        |\n" +
                     "|        |\n" +
                     "+--------+", venice.eval(script));
    }
}
