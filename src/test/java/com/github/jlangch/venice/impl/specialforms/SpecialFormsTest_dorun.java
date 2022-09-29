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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_dorun {

    @Test
    public void test() {
        final Venice venice = new Venice();

        final String script ="(+ (+ 1 1) 2)";

        assertEquals(4L, venice.eval(script));
    }

    @Test
    public void test_dorun_expr() {
        final Venice venice = new Venice();

        final String script =
                "(with-out-str                 \n" +
                "  (dorun 3 (print (+ 1 1))))  ";

        assertEquals("222", venice.eval(script));
    }

    @Test
    public void test_dorun_expr_time() {
        final Venice venice = new Venice();

        final String script =
                "(with-out-str                        \n" +
                "  (time (dorun 3 (print (+ 1 1)))))    ";

        final String result = (String)venice.eval(script);
        assertTrue(result.startsWith("222Elapsed time: "));
    }

    @Test
    public void test_dorun_fn() {
        final Venice venice = new Venice();

        final String script =
                "(with-out-str                       \n" +
                "  (let [f (fn [] (print (+ 1 1)))]  \n" +
                "    (dorun 3 f)))                      ";

        assertEquals("222", venice.eval(script));
    }

    @Test
    public void test_dorun_fn_arg() {
        final Venice venice = new Venice();

        final String script =
                "(with-out-str                       \n" +
                "  (let [f (fn [x] (print (+ x 1)))] \n" +
                "    (dorun 3 f)))                      ";

        assertEquals("123", venice.eval(script));
    }
}
