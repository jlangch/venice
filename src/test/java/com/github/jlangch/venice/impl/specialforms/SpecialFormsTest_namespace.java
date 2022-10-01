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
package com.github.jlangch.venice.impl.specialforms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class SpecialFormsTest_namespace {

    @Test
    public void test_namespace() {
        final Venice venice = new Venice();

        assertEquals(null, venice.eval("(namespace :alpha)"));
        assertEquals("foo", venice.eval("(namespace :foo/alpha)"));

        assertEquals(null, venice.eval("(namespace 'alpha)"));
        assertEquals("foo", venice.eval("(namespace 'foo/alpha)"));

        assertEquals("user", venice.eval("(do (defn alpha [] 100) (namespace alpha)))"));
        assertEquals("user", venice.eval("(do (let [x (fn alpha [] 100)] (namespace x)))"));

        assertEquals("user", venice.eval("(namespace *ns*)"));
    }

    @Test
    public void test_fn_name() {
        final Venice venice = new Venice();

        assertEquals("user/alpha", venice.eval("(do (defn alpha [] 100) (fn-name alpha)))"));
        assertEquals("user/alpha", venice.eval("(do (let [x (fn alpha [] 100)] (fn-name x)))"));
    }

    @Test
    public void test_namespace_div() {
        final Venice venice = new Venice();

        assertEquals(2L, venice.eval("(/ 4 2)"));
        assertEquals(2L, venice.eval("(core// 4 2)"));

        assertEquals(null, venice.eval("(namespace /)"));
        assertEquals(null, venice.eval("(namespace core//)"));
    }

    @Test
    public void test_namespace_function() {
        final Venice venice = new Venice();

        final String script =
                "(do                         \n" +
                "   (ns xxx)                 \n" +
                "   (defn f1 [x] (+ x 1))    \n" +
                "   (namespace f1))            ";

        assertEquals("xxx", venice.eval(script));
    }

    @Test
    public void test_namespace_anonymous_function() {
        final Venice venice = new Venice();

        final String script =
                "(do                               \n" +
                "   (ns xxx)                       \n" +
                "   (defn f1 [f] (namespace f))    \n" +
                "   (f1 #(+ 1)))                     ";

        assertEquals("xxx", venice.eval(script));
    }

    @Test
    public void test_nsQ() {
        final Venice venice = new Venice();

        final String script =
                "(do             \n" +
                "   (ns foo)     \n" +
                "   (ns? foo)) ";

        assertTrue((Boolean)venice.eval(script));

        assertTrue((Boolean)venice.eval("(ns? user)"));
        assertTrue((Boolean)venice.eval("(ns? 'user)"));

        assertFalse((Boolean)venice.eval("(ns? unknown_)"));
        assertFalse((Boolean)venice.eval("(ns? 'unknown_)"));
    }

    @Test
    public void test_ns_meta() {
        final Venice venice = new Venice();

        final String script =
                "(do                        \n" +
                "   (ns foo)                \n" +
                "   (pr-str (ns-meta foo))) ";

        assertEquals("{}", venice.eval(script));
    }

    @Test
    public void test_reset_ns_meta() {
        final Venice venice = new Venice();

        final String script =
                "(do                             \n" +
                "   (ns foo)                     \n" +
                "   (reset-ns-meta! foo {:a 1})  \n" +
                "   (pr-str (ns-meta foo))) ";

        assertEquals("{:a 1}", venice.eval(script));
    }

    @Test
    public void test_alter_ns_meta_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (ns foo)                         \n" +
                "   (alter-ns-meta! foo assoc :a 1)  \n" +
                "   (pr-str (ns-meta foo))) ";

        assertEquals("{:a 1}", venice.eval(script));
   }

    @Test
    public void test_alter_ns_meta_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                 \n" +
                "   (ns foo)                         \n" +
                "   (alter-ns-meta! foo assoc :a 1)  \n" +
                "   (alter-ns-meta! foo assoc :b 2)  \n" +
                "   (pr-str (ns-meta foo))) ";

        assertEquals("{:a 1 :b 2}", venice.eval(script));
   }

}
