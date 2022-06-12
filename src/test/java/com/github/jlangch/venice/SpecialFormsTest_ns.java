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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_ns {

    @Test
    public void test_ns_curr() {
        final Venice venice = new Venice();

        assertEquals("user", venice.eval("*ns*"));
    }

    @Test
    public void test_ns() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns xxx)          \n" +
                "  (def xoo 1)       \n" +
                "  xxx/xoo)            ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_ns_remove() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns xxx)          \n" +
                "  (def xoo 1)       \n" +
                "  (ns foo)          \n" +
                "  (ns-remove xxx)   \n" +
                "  xxx/xoo)            ";

        assertThrows(SymbolNotFoundException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_remove_curr_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns xxx)          \n" +
                "  (def xoo 1)       \n" +
                "  (ns-remove *ns*)  \n" +
                "  *ns*)               ";

        // Must not remove current namespace!
        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_remove_curr_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns xxx)          \n" +
                "  (def xoo 1)       \n" +
                "  (ns-remove xxx)  \n" +
                "  *ns*)               ";

        // Must not remove current namespace!
        assertThrows(VncException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_remove_curr_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                 \n" +
                "  (ns xxx)          \n" +
                "  (def xoo 1)       \n" +
                "  (ns yyy)          \n" +
                "  (ns-remove xxx)   \n" +
                "  xxx/xoo)            ";

        assertThrows(SymbolNotFoundException.class, () -> venice.eval(script));
    }

    @Test
    public void test_ns_list_1() {
        final Venice venice = new Venice();

        assertEquals("()", venice.eval("(pr-str (ns-list *ns*))"));
        assertEquals("()", venice.eval("(pr-str (ns-list 'user))"));

        assertEquals("()", venice.eval("(pr-str (ns-list 'inexistent))"));
    }

    @Test
    public void test_ns_list_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                        \n" +
                "  (ns xxx)                 \n" +
                "  (def xoo 1)              \n" +
                "  (pr-str (ns-list *ns*)))   ";

        assertEquals("(xxx/xoo)", venice.eval(script));
    }

    @Test
    public void test_ns_list_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "  (ns xxx)                \n" +
                "  (def xoo 1)             \n" +
                "  (ns yyy)                \n" +
                "  (def yoo 1)             \n" +
                "  (pr-str (ns-list 'xxx)))   ";

        assertEquals("(xxx/xoo)", venice.eval(script));
    }


    @Test
    public void test_ns_list_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                       \n" +
                "  (ns xxx)                \n" +
                "  (def xoo 1)             \n" +
                "  (ns yyy)                \n" +
                "  (def yoo 1)             \n" +
                "  (ns-remove 'xxx)        \n" +
                "  (pr-str (ns-list 'xxx)))  ";

        assertEquals("()", venice.eval(script));
    }

    @Test
    public void test_unmap_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                   \n" +
                "  (ns foo)            \n" +
                "  (def x 100)         \n" +
                "  (ns-unmap *ns* 'x)  \n" +
                "  foo/x)                ";

        assertThrows(SymbolNotFoundException.class, () -> venice.eval(script));
    }

    @Test
    public void test_unmap_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                   \n" +
                "  (ns foo)            \n" +
                "  (def x 100)         \n" +
                "  (ns goo)            \n" +
                "  (ns-unmap 'foo 'x)  \n" +
                "  foo/x)                ";

        assertThrows(SymbolNotFoundException.class, () -> venice.eval(script));
    }

    @Test
    public void test_unmap_3() {
        final Venice venice = new Venice();

        final String script = "(ns-unmap 'core '+)";

        try {
            venice.eval(script);
            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("Cannot remove a symbol from namespace 'core'!", ex.getMessage());
        }
        catch(RuntimeException ex) {
            fail("Expected VncException");
        }
    }

    @Test
    public void test_remove_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                   \n" +
                "  (ns foo)            \n" +
                "  (def x 100)         \n" +
                "  (ns user)           \n" +
                "  (ns-remove 'foo)    \n" +
                "  foo/x)                ";

        assertThrows(SymbolNotFoundException.class, () -> venice.eval(script));
    }

    @Test
    public void test_remove_2() {
        final Venice venice = new Venice();

        final String script = "(ns-remove 'core)";

        try {
            venice.eval(script);
            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("Namespace 'core' cannot be removed!", ex.getMessage());
        }
        catch(RuntimeException ex) {
            fail("Expected VncException");
        }
    }

}
