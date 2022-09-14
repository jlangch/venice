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


public class MultiDataEvaluationTest {

    @Test
    public void test_1() {
        final Venice venice = new Venice();

        // the keys/values of {:a 100} must only get evaluated once
        // when passed to f2 but not when passed further down to f1.
        //
        //    EVAL VALUES:     :core/hash-map > {:a 100}
        //    EVAL VALUES:     :core/keyword > :a
        //    EVAL VALUES:     :core/long > 100
        //    EVAL VALUES:     :core/symbol > x
        //    EVAL VALUES:     :core/symbol > x
        //    EVAL VALUES:     :core/symbol > x
        //    EVAL VALUES:     :core/symbol > x

        final String s =
                "(do                               \n" +
                "   (defn f1 [x] x)                \n" +
                "   (defn f2 [x] (f1 x) (f1 x))    \n" +
                "                                  \n" +
                "   (pr-str (f2 {:a 100})))        ";

        assertEquals("{:a 100}", venice.eval(s));
    }
}
