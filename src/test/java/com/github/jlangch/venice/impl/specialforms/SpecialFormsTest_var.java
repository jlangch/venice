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


public class SpecialFormsTest_var {

    @Test
    public void test_var_get() {
        assertEquals(100L, new Venice().eval("(do (def x 100) (var-get x))"));
        assertEquals(100L, new Venice().eval("(do (let [x 100] (var-get x)))"));
        assertEquals(100L, new Venice().eval("(do (binding [x 100] (var-get x)))"));
    }

    @Test
    public void test_var_name() {
        assertEquals("x", new Venice().eval("(do (def x 100) (var-name x))"));
        assertEquals("x", new Venice().eval("(do (let [x 100] (var-name x)))"));
        assertEquals("x", new Venice().eval("(do (binding [x 100] (var-name x)))"));

        assertEquals("+", new Venice().eval("(var-name +)"));
        assertEquals("+", new Venice().eval("(var-name core/+)"));
        assertEquals("split", new Venice().eval("(var-name str/split)"));
    }

    @Test
    public void test_var_ns() {
        assertEquals("core", new Venice().eval("(var-ns +)"));
        assertEquals("core", new Venice().eval("(var-ns core/+)"));
        assertEquals("str", new Venice().eval("(var-ns str/split)"));
        assertEquals(null, new Venice().eval("(let [x 100] (var-ns x))"));
    }

    @Test
    public void test_var_sym_meta() {
        // def
        assertEquals(true, new Venice().eval("(do                            " +
                                             "  (def ^:private x 100)        " +
                                             "  (:private (var-sym-meta 'x)))"));

        assertEquals(3L, new Venice().eval("(do                        " +
                                           "  (def ^{:foo 3} x 100)    " +
                                           "  (:foo (var-sym-meta 'x)))"));

        // let
        assertEquals(3L, new Venice().eval("(do                           " +
                                           "  (let [^{:foo 3} x 100]      " +
                                           "    (:foo (var-sym-meta 'x))))"));

        // binding
        assertEquals(3L, new Venice().eval("(do                           " +
                                           "  (binding [^{:foo 3} x 100]  " +
                                           "    (:foo (var-sym-meta 'x))))"));

        // function arg
        assertEquals(3L, new Venice().eval("(do                           " +
                                           "  (defn bar [^{:foo 3} x]     " +
                                           "    (:foo (var-sym-meta 'x))) " +
                                           "  (bar 100))                  "));

        assertEquals(3L, new Venice().eval("(do                                  " +
                                           "  (defn bar [^{:foo 3} x]            " +
                                           "    (:foo (var-sym-meta 'x)))        " +
                                           "  (bar (vary-meta 100 assoc :foo 4)))"));
    }

    @Test
    public void test_var_val_meta() {
        // def
        assertEquals(4L, new Venice().eval("(do                                    " +
                                           "  (def x ^{:foo 4} 100)                " +
                                           "  (:foo (var-val-meta 'x)))            "));

        assertEquals(4L, new Venice().eval("(do                                    " +
                                           "  (def x (vary-meta 100 assoc :foo 4)) " +
                                           "  (:foo (var-val-meta 'x)))            "));

        assertEquals(4L, new Venice().eval("(do                                              " +
                                           "  (def ^{:foo 3} x (vary-meta 100 assoc :bar 4)) " +
                                           "  (:bar (var-val-meta 'x)))                      "));

        assertEquals(3L, new Venice().eval("(do                                              " +
                                           "  (def ^{:foo 3} x (vary-meta 100 assoc :bar 4)) " +
                                           "  (:foo (var-sym-meta 'x)))                      "));

        // let
        assertEquals(3L, new Venice().eval("(do                           " +
                                           "  (let [x ^{:foo 3} 100]      " +
                                           "    (:foo (var-val-meta 'x))))"));

        // binding
        assertEquals(3L, new Venice().eval("(do                           " +
                                           "  (binding [x ^{:foo 3} 100]  " +
                                           "    (:foo (var-val-meta 'x))))"));

        // function arg
        assertEquals(3L, new Venice().eval("(do                                  " +
                                           "  (defn bar [x]                      " +
                                           "    (:foo (var-val-meta 'x)))        " +
                                           "  (bar (vary-meta 100 assoc :foo 3)))"));

        assertEquals(3L, new Venice().eval("(do                                  " +
                                           "  (defn bar [^{:foo 4} x]            " +
                                           "    (:foo (var-val-meta 'x)))        " +
                                           "  (bar (vary-meta 100 assoc :foo 3)))"));
   }

    @Test
    public void test_var_global() {
        assertTrue( (Boolean)new Venice().eval("(do (def x 100) (var-global? x))"));
        assertFalse((Boolean)new Venice().eval("(do (def x 100) (var-local? x))"));
        assertFalse((Boolean)new Venice().eval("(do (def x 100) (var-thread-local? x))"));
    }

    @Test
    public void test_var_local() {
        assertFalse((Boolean)new Venice().eval("(do (let [x 100] (var-global? x)))"));
        assertTrue( (Boolean)new Venice().eval("(do (let [x 100] (var-local? x)))"));
        assertFalse((Boolean)new Venice().eval("(do (let [x 100] (var-thread-local? x)))"));

        assertFalse((Boolean)new Venice().eval("(do (defn foo [x] (var-global? x)) (foo 0))"));
        assertTrue( (Boolean)new Venice().eval("(do (defn foo [x] (var-local? x)) (foo 0))"));
        assertFalse((Boolean)new Venice().eval("(do (defn foo [x] (var-thread-local? x)) (foo 0))"));
    }

    @Test
    public void test_var_threadlocal() {
        assertFalse((Boolean)new Venice().eval("(do (binding [x 100] (var-global? x)))"));
        assertFalse((Boolean)new Venice().eval("(do (binding [x 100] (var-local? x)))"));
        assertTrue( (Boolean)new Venice().eval("(do (binding [x 100] (var-thread-local? x)))"));
    }
}
