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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class TimingModuleTest {

    @Test
    public void test_1a() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (timing/run (fn [] (sleep 100) 10)))      ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_1b() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (timing/run (fn [] (sleep 100) 10) nil))  ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_1c() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                              \n" +
                "   (load-module :timing)                         \n" +
                "   (timing/run (fn [] (sleep 100) 10) nil nil))  ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_1d() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (with-out-str                             \n" +
                "     (timing/run (fn [] (sleep 100) 10))))   ";

        final String ret = (String)venice.eval(script1);

        assertTrue(ret.matches("Elapsed: 1[0-9]{2}ms\n"));
    }

    @Test
    public void test_2a() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (timing/run (fn [] (sleep 100) 10)        \n" +
                "               \"Started\"))                 ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_2b() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (with-out-str                             \n" +
                "     (timing/run (fn [] (sleep 100) 10)      \n" +
                "                 \"Started\")))              ";

        final String ret = (String)venice.eval(script1);

        assertTrue(ret.matches("Started\nElapsed: 1[0-9]{2}ms\n"));
    }

    @Test
    public void test_3a() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (timing/run (fn [] (sleep 100) 10)        \n" +
                "               \"Started\"                   \n" +
                "               \"Done\"))                    ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_3b() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                          \n" +
                "   (load-module :timing)                     \n" +
                "   (with-out-str                             \n" +
                "     (timing/run (fn [] (sleep 100) 10)      \n" +
                "                 \"Started\"                 \n" +
                "                 \"Done\")))                 ";

        final String ret = (String)venice.eval(script1);

        assertTrue(ret.matches("Started\nDone\nElapsed: 1[0-9]{2}ms\n"));
    }

    @Test
    public void test_4a() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                           \n" +
                "   (load-module :timing)                      \n" +
                "   (timing/run (fn [] (sleep 100) 10)         \n" +
                "               \"Started\"                    \n" +
                "               (fn [x] (println (+ x 10)))))  ";

        assertEquals(10L, venice.eval(script1));
    }

    @Test
    public void test_4b() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                              \n" +
                "   (load-module :timing)                         \n" +
                "   (with-out-str                                 \n" +
                "     (timing/run (fn [] (sleep 100) 10)          \n" +
                "                 \"Started\"                     \n" +
                "                 (fn [x] (println (+ x 10))))))  ";


        final String ret = (String)venice.eval(script1);

        assertTrue(ret.matches("Started\n20\nElapsed: 1[0-9]{2}ms\n"));
    }
}
