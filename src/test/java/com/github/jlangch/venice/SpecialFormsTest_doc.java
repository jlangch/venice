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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;


public class SpecialFormsTest_doc {

    @Test
    public void test_doc() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        new Venice().eval("(doc +)", params);
        new Venice().eval("(doc .)", params);
        new Venice().eval("(doc io/slurp)", params);
    }

    @Test
    public void test_doc_multiple() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        final String script =
                "(do          \n" +
                "  (doc +)    \n" +
                "  (doc +)    \n" +
                "  (doc +)    \n" +
                "  (doc +))   ";

        new Venice().eval(script, params);
    }

    @Test
    public void test_doc_self() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        new Venice().eval("(doc doc)", params);
    }

    @Test
    public void test_doc_not_found() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        assertThrows(VncException.class, () -> new Venice().eval("(doc foo-foo-foo)", params));
    }

    @Test
    public void test_doc_not_found_candidates() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        new Venice().eval("(doc slurp)", params);
    }

    @Test
    public void test_doc_customtype_protocol_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (defprotocol Lifecycle (start [c]) (stop [c]))    \n" +
                "  (deftype :xxx [name :string]                      \n" +
                "               Lifecycle (start [c] c)              \n" +
                "                         (stop [c] c))              \n" +
                "  (with-out-str (doc :xxx)))                          ";

        final String doc = (String)venice.eval(script);
        assertTrue(doc.contains("Protocol: user/Lifecycle"));
        assertTrue(doc.contains("start: (start [c])"));
        assertTrue(doc.contains("stop: (stop [c])"));
    }

    @Test
    public void test_doc_customtype_protocol_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                 \n" +
                "  (defprotocol Lifecycle (start [c]) (stop [c]))    \n" +
                "  (deftype :xxx [name :string])                     \n" +
                "  (extend :user/xxx                                 \n" +
                "      Lifecycle                                     \n" +
                "         (start [c] c)                              \n" +
                "         (stop [c] c))                              \n" +
                "  (with-out-str (doc :xxx)))                          ";

        final String doc = (String)venice.eval(script);
        assertTrue(doc.contains("Protocol: user/Lifecycle"));
        assertTrue(doc.contains("start: (start [c])"));
        assertTrue(doc.contains("stop: (stop [c])"));
    }

    @Test
    public void test_doc_bug_001() {
        final Map<String, Object> params = Parameters.of("*out*", null, "*err*", null);

        new Venice().eval("(doc io/file-matches-glob?)", params);
    }
}
