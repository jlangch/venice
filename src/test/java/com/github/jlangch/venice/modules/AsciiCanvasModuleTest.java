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

    @Test
    public void test_010_bar_chart() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                                                            \n" +
                              "  (load-module :ascii-canvas ['ascii-canvas :as 'ac])                          \n" +
                              "                                                                               \n" +
                              "  (let [w     61                                                               \n" +
                              "        h     26                                                               \n" +
                              "        cv    (ac/create w h)                                                  \n" +
                              "        title (str/align w :center :ellipsis-left \"Bar Chart\")               \n" +
                              "        vals  [10 35 0 40 56 100 30 40 50 78 89 30 59]                         \n" +
                              "        ix    8                                                                \n" +
                              "        iy    2                                                                \n" +
                              "        iw    (* (count vals) 3)                                               \n" +
                              "        ih    21]                                                              \n" +
                              "                                                                               \n" +
                              "    ;; title                                                                   \n" +
                              "    (ac/draw-text cv title 0 (dec h))                                          \n" +
                              "                                                                               \n" +
                              "    ;; ticks                                                                   \n" +
                              "    (let [ticks-x (count vals), ticks-y 6]                                     \n" +
                              "      ;; x-axis ticks                                                          \n" +
                              "      (doseq [n (range ticks-x)]                                               \n" +
                              "        (let [x (+ 8 (* n 4))]                                                 \n" +
                              "          (ac/draw-text cv (str/format \"%02d\" n) x 0)))                      \n" +
                              "      ;; y-axis ticks                                                          \n" +
                              "      (doseq [n (range ticks-y)]                                               \n" +
                              "        (let [y (+ iy (* n 4))]                                                \n" +
                              "          (ac/draw-text cv (str/format \"%3d%% -\" (* n 20)) 0 y))))           \n" +
                              "                                                                               \n" +
                              "    ;; bars                                                                    \n" +
                              "    (doseq [n (range (count vals))]                                            \n" +
                              "      (let [v   (-> (nth vals n) (* ih) (/ 100))                               \n" +
                              "            x   (+ ix (* n 4))                                                 \n" +
                              "            bar (if (zero? v) \"▁\" (str/repeat \"▇\" v))]                     \n" +
                              "        (ac/draw-vertical-up cv bar x iy)                                      \n" +
                              "        (ac/draw-vertical-up cv bar (inc x) iy)))                              \n" +
                              "                                                                               \n" +
                              "    (ac/string-ascii cv)))                                                     ";

        //System.out.println(venice.eval(script));

        assertEquals(
                      "                          Bar Chart                          \n"
                    + "                                                             \n"
                    + "                                                             \n"
                    + "100% -                      ▇▇                               \n"
                    + "                            ▇▇                               \n"
                    + "                            ▇▇                               \n"
                    + "                            ▇▇                  ▇▇           \n"
                    + " 80% -                      ▇▇                  ▇▇           \n"
                    + "                            ▇▇              ▇▇  ▇▇           \n"
                    + "                            ▇▇              ▇▇  ▇▇           \n"
                    + "                            ▇▇              ▇▇  ▇▇           \n"
                    + " 60% -                      ▇▇              ▇▇  ▇▇           \n"
                    + "                            ▇▇              ▇▇  ▇▇      ▇▇   \n"
                    + "                        ▇▇  ▇▇              ▇▇  ▇▇      ▇▇   \n"
                    + "                        ▇▇  ▇▇          ▇▇  ▇▇  ▇▇      ▇▇   \n"
                    + " 40% -                  ▇▇  ▇▇          ▇▇  ▇▇  ▇▇      ▇▇   \n"
                    + "                    ▇▇  ▇▇  ▇▇      ▇▇  ▇▇  ▇▇  ▇▇      ▇▇   \n"
                    + "            ▇▇      ▇▇  ▇▇  ▇▇      ▇▇  ▇▇  ▇▇  ▇▇      ▇▇   \n"
                    + "            ▇▇      ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + " 20% -      ▇▇      ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + "            ▇▇      ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + "            ▇▇      ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + "        ▇▇  ▇▇      ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + "  0% -  ▇▇  ▇▇  ▁▁  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇  ▇▇   \n"
                    + "                                                             \n"
                    + "        00  01  02  03  04  05  06  07  08  09  10  11  12   ",
                  venice.eval(script));
    }
}
