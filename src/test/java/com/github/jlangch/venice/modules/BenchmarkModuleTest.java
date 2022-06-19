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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class BenchmarkModuleTest {

    @Test
    public void test_load() {
        assertEquals("benchmark", new Venice().eval("(load-module :benchmark)"));
    }

    @Test
    public void test_1() {
        final String script =
            "(do                                               \n" +
            "  (load-module :benchmark ['benchmark :as 'b])    \n" +
            "  (b/benchmark (+ 1 2 3 4) 120_000 10_000))            ";

         new Venice().eval(script);
    }

    @Test
    public void test_2() {
        final String script =
            "(do                                               \n" +
            "  (load-module :benchmark ['benchmark :as 'b])    \n" +
            "  (b/benchmark (sleep 100) 30 30))              ";

         new Venice().eval(script);
    }

}
