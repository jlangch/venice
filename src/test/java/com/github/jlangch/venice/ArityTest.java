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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;


public class ArityTest {

    @Test
    public void test_special_form_arity_ex() {
        final Venice venice = new Venice();

        try {
            venice.eval("(locking)");

            fail("Expected ArityException");
        }
        catch(ArityException ex) {
            final String msg = ex.getMessage();
            assertTrue(msg.startsWith("Wrong number of args (0) passed to special form locking."));
        }
    }

    @Test
    public void test_macro_form_arity_ex() {
        final Venice venice = new Venice();

        try {
            venice.eval("(time)");

            fail("Expected ArityException");
        }
        catch(ArityException ex) {
            final String msg = ex.getMessage();
            assertTrue(msg.startsWith("Wrong number of args (0) passed to macro time."));
        }
    }

    @Test
    public void test_macro_form_arity_ex_2() {
        final Venice venice = new Venice();

        try {
            venice.eval("(while)");

            fail("Expected ArityException");
        }
        catch(ArityException ex) {
            final String msg = ex.getMessage();
            assertTrue(msg.startsWith("Wrong number of args (0) passed to the variadic macro while that requires at least 1 arg."));
        }
    }

    @Test
    public void test_function_form_arity_ex() {
        final Venice venice = new Venice();

        try {
            venice.eval("(count)");

            fail("Expected ArityException");
        }
        catch(ArityException ex) {
            final String msg = ex.getMessage();
            assertTrue(msg.startsWith("Wrong number of args (0) passed to function count."));
        }
    }

    @Test
    public void test_multiarity_eval_args_problem_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                     \n" +
                "  (ns foo)              \n" +
                "  (defn m [n] n)        \n" +
                "  (ns bar)              \n" +
                "  (foo/m *ns*))         ";

        assertEquals("bar", venice.eval(script));
    }

    @Test
    public void test_multiarity_eval_args_problem_2() {
        // (ns foo)
        //
        // (defn f
        //   ([] (f *ns*))
        //   ([n] n))
        //
        // (ns bar)
        // (println (str (foo/f)))  ;; => "bar"

        final Venice venice = new Venice();

        final String script =
                "(do                     \n" +
                "  (ns foo)              \n" +
                "                        \n" +
                "  (defn m               \n" +
                "    ([] (m *ns*))       \n" +
                "    ([n] n))            \n" +
                "                        \n" +
                "  (ns bar)              \n" +
                "                        \n" +
                "  (foo/m))              ";

        assertEquals("foo", venice.eval(script));
    }

    @Test
    public void test_multiarity_eval_args_problem_3() {
        // (ns foo)
        // (def y "foo")
        //
        // (defn f
        //   ([] (f y))
        //   ([n] n))
        //
        // (ns bar)
        // (def y "bar")
        // (println (str (foo/f)))  ;; => "foo"

        final Venice venice = new Venice();

        final String script =
                "(do                     \n" +
                "  (ns foo)              \n" +
                "                        \n" +
                "  (def y \"foo\")       \n" +
                "                        \n" +
                "  (defn m               \n" +
                "    ([] (m y))          \n" +
                "    ([n] n))            \n" +
                "                        \n" +
                "  (ns bar)              \n" +
                "                        \n" +
                "  (def y \"bar\")       \n" +
                "                        \n" +
                "  (foo/m))              ";

        assertEquals("foo", venice.eval(script));
    }
}
