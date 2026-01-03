/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class StopWatchModuleTest {

    @Test
    public void test_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/elapsed sw)))";

        assertEquals(0L, venice.eval(script));
    }

    @Test
    public void test_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/stop sw)                                  \n" +
                "     (sw/elapsed sw)))";

        assertTrue((long)venice.eval(script) >= 100);
        assertTrue((long)venice.eval(script) <= 150);
    }

    @Test
    public void test_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/split sw :milliseconds)))";

        assertTrue((long)venice.eval(script) >= 100);
        assertTrue((long)venice.eval(script) <= 150);
    }

    @Test
    public void test_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/start sw)                                 \n" +
                "     (sw/stop sw)                                  \n" +
                "     (sw/elapsed sw)))";

        assertTrue((long)venice.eval(script) < 50);
    }

    @Test
    public void test_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/start sw)                                 \n" +
                "     (sw/split sw :milliseconds)))";

        assertTrue((long)venice.eval(script) < 50);
    }

    @Test
    public void test_6() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/stop sw)                                  \n" +
                "     (sw/elapsed (sw/copy sw))))";

        assertTrue((long)venice.eval(script) >= 100);
        assertTrue((long)venice.eval(script) <= 150);
    }

    @Test
    public void test_7() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])   \n" +
                "   (let [sw (sw/create)]                           \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/stop sw)                                  \n" +
                "     (sw/resume sw)                                \n" +
                "     (sleep 100)                                   \n" +
                "     (sw/elapsed sw)))";

        assertTrue((long)venice.eval(script) >= 100);
        assertTrue((long)venice.eval(script) <= 150);
    }

    @Test
    public void test_8() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])        \n" +
                "   (let [sw (sw/create-time-limit :milliseconds 1000)]  \n" +
                "     (sleep 100)                                        \n" +
                "     (sw/expired? sw)))";

        assertFalse((boolean)venice.eval(script));
    }

    @Test
    public void test_9() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "   (load-module :stopwatch ['stopwatch :as 'sw])        \n" +
                "   (let [sw (sw/create-time-limit :milliseconds 50)]    \n" +
                "     (sleep 100)                                        \n" +
                "     (sw/expired? sw)))";

        assertTrue((boolean)venice.eval(script));
    }
}
