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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class JavaModuleTest {


    @Test
    public void test_as_runnable() {
        final Venice venice = new Venice();

        final String script =
               "(do                                         \n" +
               "  (load-module :java ['java :as 'j])        \n" +
               "                                            \n" +
               "  (. (j/as-runnable (fn [] nil)) :run))     ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_as_callable() {
        final Venice venice = new Venice();

        final String script =
               "(do                                         \n" +
               "  (load-module :java ['java :as 'j])        \n" +
               "                                            \n" +
               "  (. (j/as-callable (fn [] 4)) :call))      ";

        assertEquals(4L, venice.eval(script));
    }

    @Test
    public void test_as_function() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                \n" +
               "  (load-module :java ['java :as 'j])               \n" +
               "                                                   \n" +
               "  (. (j/as-function (fn [x] (+ x 1))) :apply 4))   ";

        assertEquals(5L, venice.eval(script));
    }

    @Test
    public void test_as_consumer() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                \n" +
               "  (load-module :java ['java :as 'j])               \n" +
               "                                                   \n" +
               "  (. (j/as-consumer (fn [x] nil)) :accept 4))      ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_as_supplier() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                \n" +
               "  (load-module :java ['java :as 'j])               \n" +
               "                                                   \n" +
               "  (. (j/as-supplier (fn [] 5)) :get))              ";

        assertEquals(5L, venice.eval(script));
    }

    @Test
    public void test_as_predicate() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                  \n" +
               "  (load-module :java ['java :as 'j])                 \n" +
               "                                                     \n" +
               "  (. (j/as-predicate (fn [x] (some? x))) :test 1))   ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_as_bipredicate() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                              \n" +
               "  (load-module :java ['java :as 'j])                             \n" +
               "                                                                 \n" +
               "  (. (j/as-bipredicate (fn [x y] (> x y))) :test 2 1))           ";

        assertEquals(true, venice.eval(script));
    }

    @Test
    public void test_as_bifunction() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                              \n" +
               "  (load-module :java ['java :as 'j])                             \n" +
               "                                                                 \n" +
               "  (. (j/as-bifunction (fn [x y] (+ x y))) :apply 1 2))          ";

        assertEquals(3L, venice.eval(script));
    }

    @Test
    public void test_as_biconsumer() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                              \n" +
               "  (load-module :java ['java :as 'j])                             \n" +
               "                                                                 \n" +
               "  (. (j/as-biconsumer (fn [x y] nil)) :accept 1 2))              ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_as_unaryoperator() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                              \n" +
               "  (load-module :java ['java :as 'j])                             \n" +
               "                                                                 \n" +
               "  (. (j/as-unaryoperator (fn [x] (+ x 1))) :apply 1))            ";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_as_binaryoperator() {
        final Venice venice = new Venice();

        final String script =

               "(do                                                              \n" +
               "  (load-module :java ['java :as 'j])                             \n" +
               "                                                                 \n" +
               "  (. (j/as-binaryoperator (fn [x y] (+ x y))) :apply 1 2))           ";

        assertEquals(3L, venice.eval(script));
    }

}

