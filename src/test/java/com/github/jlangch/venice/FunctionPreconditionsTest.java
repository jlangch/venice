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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class FunctionPreconditionsTest {

    @Test
    public void test_defn_precondition() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                           \n" +
                "   (defn sum [x y]            \n" +
                "         { :pre [(> x 0)] }   \n" +
                "         (+ x y))             \n" +
                "                              \n" +
                "   (sum 1 3)                  \n" +
                ") ";

        assertEquals(Long.valueOf(4), venice.eval(script1));
    }

    @Test
    public void test_defn_precondition_instanceof() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                                         \n" +
                "  (import :java.util.ArrayList)             \n" +
                "  (defn sum [l]                             \n" +
                "    { :pre [(instance-of? :ArrayList l)] }  \n" +
                "    nil)                                    \n" +
                "                                            \n" +
                "  (sum (. :ArrayList :new))                 \n" +
                ") ";

        assertNull(venice.eval(script1));
    }

    @Test
    public void test_defn_multi_arity_precondition() {
        final Venice venice = new Venice();

        final String s =
                "(do                                                   \n" +
                "   (defn arity ([] 0)                                 \n" +
                "               ([a] { :pre [(> a 0)] } 1)             \n" +
                "               ([a b] { :pre [(> a 0)] } 2)           \n" +
                "               ([a b c] { :pre [(> a 0)] } 3))        \n" +
                "   (str (arity ) (arity 1) (arity 1 2) (arity 1 2 3)))  ";

        assertEquals("0123", venice.eval(s));
    }

    @Test
    public void test_defn_precondition_failed() {
        final Venice venice = new Venice();

        final String script =
                "(do                           \n" +
                "   (defn sum [x y]            \n" +
                "         { :pre [(> x 0)] }   \n" +
                "         (+ x y))             \n" +
                "                              \n" +
                "   (sum 0 3)                  \n" +
                ") ";

        assertThrows(AssertionException.class, () -> venice.eval(script));
    }

}
