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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_extend {

    @Test
    public void test_extend_basic_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (ns test)                             \n" +
                "  (defprotocol P (foo [x]))             \n" +
                "  (extend :core/long P (foo [x] x)))      ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_extend_basic_2a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (ns test)                             \n" +
                "  (defprotocol P (foo [x]))             \n" +
                "  (extend :core/long P (foo [x] x))     \n" +
                "  (foo 10))";

        assertEquals(10L, venice.eval(script));
    }

    @Test
    public void test_extend_basic_2b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (ns test)                             \n" +
                "  (defprotocol P (foo [x]))             \n" +
                "  (extend :core/long P (foo [x] 1M x))  \n" +
                "  (foo 10))";

        assertEquals(10L, venice.eval(script));
    }

    @Test
    public void test_extend_basic_2c() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (ns test)                             \n" +
                "  (defprotocol P (foo [x] [x y] nil))   \n" +
                "  (extend :core/long P (foo [x] x))     \n" +
                "  (foo 1))                                ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_extend_basic_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                     \n" +
                "  (ns test)                             \n" +
                "  (defprotocol P (foo [x] [x y] nil))  \n" +
                "  (extend :core/long P (foo [x] x))     \n" +
                "  (foo 1 2))                              ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_extend_basic_4a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                            \n" +
                "  (ns test)                                    \n" +
                "  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
                "  (extend :core/long P (foo [x y] x))          \n" +
                "  (foo 1))                                       ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_extend_basic_4b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                            \n" +
                "  (ns test)                                    \n" +
                "  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
                "  (extend :core/long P (foo [x y] x))          \n" +
                "  (foo 2 100))                                   ";

        assertEquals(2L, venice.eval(script));
    }

    @Test
    public void test_extend_basic_4c() {
        final Venice venice = new Venice();

        final String script =
                "(do                                            \n" +
                "  (ns test)                                    \n" +
                "  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
                "  (extend :core/long P (foo [x y] x))          \n" +
                "  (foo 2 3 4))                                   ";

        assertEquals(null, venice.eval(script));
    }

    @Test
    public void test_extend_basic_4d() {
        final Venice venice = new Venice();

        final String script =
                "(do                                            \n" +
                "  (ns test)                                    \n" +
                "  (defprotocol P (foo [x] [x y] [x y z] nil))  \n" +
                "  (extend :core/long P (foo [x y] x))          \n" +
                "  (foo 2 3 4 5))                                   ";

        assertThrows(
                ArityException.class,
                () -> venice.eval(script));
    }

    @Test
    public void test_extend_basic_5() {
        final Venice venice = new Venice();

        final String script =
                "(do                                            \n" +
                "  (ns test)                                    \n" +
                "  (deftype :person [name :string])             \n" +
                "  (defprotocol P (foo [x]))                    \n" +
                "  (extend :test/person P (foo [x] (:name x)))  \n" +
                "  (foo (person. \"joe\")))                       ";

        assertEquals("joe", venice.eval(script));
    }

    @Test
    public void test_extend_on_deftype_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (defprotocol P (foo [x]))            \n" +
                "  (deftype :person [name :string]      \n" +
                "           P (foo [x] (:name x)))      \n" +
                "  (foo (person. \"joe\")))               ";

        assertEquals("joe", venice.eval(script));
    }

    @Test
    public void test_extend_on_deftype_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (defprotocol P (foo [x]) (bar [x]))               \n" +
                "  (deftype :person [name :string last :string]      \n" +
                "           P (foo [x] (:name x))                    \n" +
                "             (bar [x] (:last x)))                   \n" +
                "  (def p (person. \"joe\" \"smith\"))               \n" +
                "  (pr-str [(foo p) (bar p)]))";

        assertEquals("[\"joe\" \"smith\"]", venice.eval(script));
    }

    @Test
    public void test_namespaces_1a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (ns test)                                           \n" +
                "  (deftype :person [name :string])                    \n" +
                "                                                      \n" +
                "  (ns test1)                                          \n" +
                "  (defprotocol P (foo [x]))                           \n" +
                "  (extend :test/person P (foo [x] (:name x)))         \n" +
                "                                                      \n" +
                "  (ns test2)                                          \n" +
                "  (test1/foo (test/person. \"joe\")))                   ";

        assertEquals("joe", venice.eval(script));
    }

    @Test
    public void test_namespaces_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (ns testA)                                          \n" +
                "  (deftype :person [name :string])                    \n" +
                "                                                      \n" +
                "  (ns testB)                                          \n" +
                "  (deftype :company [name :string])                   \n" +
                "                                                      \n" +
                "  (ns test1)                                          \n" +
                "  (defprotocol P (foo [x]))                           \n" +
                "  (extend :testA/person P (foo [x] (:name x)))        \n" +
                "  (extend :testB/company P (foo [x] (:name x)))       \n" +
                "                                                      \n" +
                "  (ns test2)                                          \n" +
                "  (test1/foo (testA/person. \"joe\"))                 \n" +
                "  (test1/foo (testB/company. \"ABC Inc.\")))            ";

        assertEquals("ABC Inc.", venice.eval(script));
    }

    @Test
    public void test_namespaces_2a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (ns test)                                           \n" +
                "  (deftype :person [name :string])                    \n" +
                "                                                      \n" +
                "  (ns test1)                                          \n" +
                "  (defprotocol P (foo [x]))                           \n" +
                "                                                      \n" +
                "  (ns test2)                                          \n" +
                "  (extend :test/person test1/P (foo [x] (:name x)))   \n" +
                "                                                      \n" +
                "  (ns test3)                                          \n" +
                "  (test1/foo (test/person. \"joe\")))                   ";

        assertEquals("joe", venice.eval(script));
    }

    @Test
    public void test_namespaces_2b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (ns testA)                                          \n" +
                "  (deftype :person [name :string])                    \n" +
                "                                                      \n" +
                "  (ns testB)                                          \n" +
                "  (deftype :company [name :string])                   \n" +
                "                                                      \n" +
                "  (ns test1)                                          \n" +
                "  (defprotocol P (foo [x]))                           \n" +
                "                                                      \n" +
                "  (ns test2)                                          \n" +
                "  (extend :testA/person test1/P (foo [x] (:name x)))  \n" +
                "  (extend :testB/company test1/P (foo [x] (:name x))) \n" +
                "                                                      \n" +
                "  (ns test3)                                          \n" +
                "  (test1/foo (testA/person. \"joe\"))                 \n" +
                "  (test1/foo (testB/company. \"ABC Inc.\")))            ";

        assertEquals("ABC Inc.", venice.eval(script));
    }

    @Test
    public void test_namespaces_3a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (ns test)                               \n" +
                "  (defprotocol P (foo [x]))               \n" +
                "                                          \n" +
                "  (ns test1)                              \n" +
                "  (deftype :person [name :string]         \n" +
                "           test/P (foo [x] (:name x)))    \n" +
                "                                          \n" +
                "  (ns test2)                              \n" +
                "  (test/foo (test1/person. \"joe\")))       ";

        assertEquals("joe", venice.eval(script));
    }

    @Test
    public void test_namespaces_3b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "  (ns test)                                           \n" +
                "  (defprotocol P (foo [x]))                           \n" +
                "                                                      \n" +
                "  (ns testA)                                          \n" +
                "  (deftype :person [name :string]                     \n" +
                "           test/P (foo [x] (:name x)))                \n" +
                "                                                      \n" +
                "  (ns testB)                                          \n" +
                "  (deftype :company [name :string]                    \n" +
                "           test/P (foo [x] (:name x)))                \n" +
                "                                                      \n" +
                "  (ns test2)                                          \n" +
                "  (test/foo (testA/person. \"joe\"))                  \n" +
                "  (test/foo (testB/company. \"ABC Inc.\")))             ";

        assertEquals("ABC Inc.", venice.eval(script));
    }

}
