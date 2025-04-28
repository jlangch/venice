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

    @Test
    public void test_003_box() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/box 0 0 10 5 \"┌┐┘└─│─│\")                \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("┌────────┐\n" +
                     "│        │\n" +
                     "│        │\n" +
                     "│        │\n" +
                     "└────────┘", venice.eval(script));
    }

    @Test
    public void test_004_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-text \"Hello\" 0 4)                  \n" +
                              "        (ac/draw-text \"Hello\" 1 3)                  \n" +
                              "        (ac/draw-text \"Hello\" 2 2)                  \n" +
                              "        (ac/draw-text \"Hello\" 3 1)                  \n" +
                              "        (ac/draw-text \"Hello\" 4 0)                  \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("Hello     \n" +
                     " Hello    \n" +
                     "  Hello   \n" +
                     "   Hello  \n" +
                     "    Hello ", venice.eval(script));
    }

    @Test
    public void test_005_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-text \"Hello\" -1 4)                 \n" +
                              "        (ac/draw-text \"Hello\"  1 3)                 \n" +
                              "        (ac/draw-text \"Hello\"  3 2)                 \n" +
                              "        (ac/draw-text \"Hello\"  5 1)                 \n" +
                              "        (ac/draw-text \"Hello\"  7 0)                 \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("ello      \n" +
                     " Hello    \n" +
                     "   Hello  \n" +
                     "     Hello\n" +
                     "       Hel", venice.eval(script));
    }

    @Test
    public void test_006_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-horizontal-right \"Hello\" -1 4)     \n" +
                              "        (ac/draw-horizontal-right \"Hello\"  1 3)     \n" +
                              "        (ac/draw-horizontal-right \"Hello\"  3 2)     \n" +
                              "        (ac/draw-horizontal-right \"Hello\"  5 1)     \n" +
                              "        (ac/draw-horizontal-right \"Hello\"  7 0)     \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("ello      \n" +
                     " Hello    \n" +
                     "   Hello  \n" +
                     "     Hello\n" +
                     "       Hel", venice.eval(script));
    }

    @Test
    public void test_007_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-horizontal-left \"Hello\"  9 4)      \n" +
                              "        (ac/draw-horizontal-left \"Hello\"  7 3)      \n" +
                              "        (ac/draw-horizontal-left \"Hello\"  5 2)      \n" +
                              "        (ac/draw-horizontal-left \"Hello\"  3 1)      \n" +
                              "        (ac/draw-horizontal-left \"Hello\"  1 0)      \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("     olleH\n" +
                     "   olleH  \n" +
                     " olleH    \n" +
                     "lleH      \n" +
                     "eH        ", venice.eval(script));
    }

    @Test
    public void test_008_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-vertical-down \"Hello\"  0 4)      \n" +
                              "        (ac/draw-vertical-down \"Hello\"  2 3)      \n" +
                              "        (ac/draw-vertical-down \"Hello\"  4 2)      \n" +
                              "        (ac/draw-vertical-down \"Hello\"  6 1)      \n" +
                              "        (ac/draw-vertical-down \"Hello\"  8 0)      \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("H         \n" +
                     "e H       \n" +
                     "l e H     \n" +
                     "l l e H   \n" +
                     "o l l e H ", venice.eval(script));
    }

    @Test
    public void test_009_text() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/draw-vertical-up \"Hello\"  0 0)          \n" +
                              "        (ac/draw-vertical-up \"Hello\"  2 1)          \n" +
                              "        (ac/draw-vertical-up \"Hello\"  4 2)          \n" +
                              "        (ac/draw-vertical-up \"Hello\"  6 3)          \n" +
                              "        (ac/draw-vertical-up \"Hello\"  8 4)          \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("o l l e H \n" +
                     "l l e H   \n" +
                     "l e H     \n" +
                     "e H       \n" +
                     "H         ", venice.eval(script));
    }

    @Test
    public void test_010_fill() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                   \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac]) \n" +
                              "                                                      \n" +
                              "  (let [w 10, h 5]                                    \n" +
                              "    (-> (ac/create w h)                               \n" +
                              "        (ac/box 0 0 10 5 \"++++-|-|\")                \n" +
                              "        (ac/fill #\\* 1 1 8 3)                        \n" +
                              "        (ac/string-ascii))))                          ";


        assertEquals("+--------+\n" +
                     "|********|\n" +
                     "|********|\n" +
                     "|********|\n" +
                     "+--------+", venice.eval(script));
    }
}
