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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class TraceModuleTest {

    @Test
    public void test_qualified_name() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                            \n" +
                "   (load-module :trace)        \n" +
                "   (trace/qualified-name +))     ";

        assertEquals("core/+", venice.eval(script1));


        final String script2 =
                "(do                              \n" +
                "   (load-module :trace)          \n" +
                "   (defn foo [] nil)             \n" +
                "   (trace/qualified-name foo))     ";

        assertEquals("user/foo", venice.eval(script2));
    }

    @Test
    public void test_trace() {
        final Venice venice = new Venice();
        final Map<String,Object> params = Parameters.of("*out*", null);

        final String script1 =
                "(do                            \n" +
                "   (load-module :trace)        \n" +
                "   (trace/trace (+ 4 5)))        ";

        assertEquals(9L, venice.eval(script1, params));


        final String script2 =
                "(do                              \n" +
                "   (load-module :trace)          \n" +
                "   (trace/trace 9))                ";

        assertEquals(9L, venice.eval(script2, params));


        final String script3 =
                "(do                              \n" +
                "   (load-module :trace)          \n" +
                "   (trace/trace nil))              ";

        assertNull(venice.eval(script3, params));
    }

    @Test
    public void test_trace_nested() {
        final CapturingPrintStream cps = new CapturingPrintStream();

        final Venice venice = new Venice();
        final Map<String,Object> params = Parameters.of("*out*", cps);

        final String script1 =
                "(do                                              \n" +
                "          (load-module :trace ['trace :as 't])   \n" +
                "                                                 \n" +
                "          (defn foo [x] (+ x 2))                 \n" +
                "          (defn bar [x] (foo x))                 \n" +
                "                                                 \n" +
                "          (t/trace-var +)                        \n" +
                "          (t/trace-var foo)                      \n" +
                "          (t/trace-var bar)                      \n" +
                "                                                 \n" +
                "          (bar 5))                               ";

        assertEquals(7L, venice.eval(script1, params));

        // Must run on *nix and Windows
        assertEquals(
                "TRACE t00: (user/bar 5)\n" +
                "TRACE t00: | (user/foo 5)\n" +
                "TRACE t00: | | (core/+ 5 2)\n" +
                "TRACE t00: | | | => 7\n" +
                "TRACE t00: | | => 7\n" +
                "TRACE t00: | => 7\n",
                StringUtil.crlf_to_lf(cps.getOutput()).replaceAll("t[0-9]+:", "t00:"));
    }

    @Test
    public void test_trace_exceptipn() {
        final CapturingPrintStream cps = new CapturingPrintStream();

        final Venice venice = new Venice();
        final Map<String,Object> params = Parameters.of("*out*", cps);

        final String script1 =
                "(do                                              \n" +
                "          (load-module :trace ['trace :as 't])   \n" +
                "                                                 \n" +
                "          (defn foo [x] (/ x 0))                 \n" +
                "          (defn bar [x] (foo x))                 \n" +
                "                                                 \n" +
                "          (t/trace-var /)                        \n" +
                "          (t/trace-var foo)                      \n" +
                "          (t/trace-var bar)                      \n" +
                "                                                 \n" +
                "          (bar 5))                               ";

        assertThrows(VncException.class, () -> venice.eval(script1, params));

        // Must run on *nix and Windows
        assertEquals(
                "TRACE t00: (user/bar 5)\n" +
                "TRACE t00: | (user/foo 5)\n" +
                "TRACE t00: | | (core// 5 0)\n" +
                "TRACE t00: | | | => com.github.jlangch.venice.VncException: / by zero\n" +
                "TRACE t00: | | => com.github.jlangch.venice.VncException: / by zero\n" +
                "TRACE t00: | => com.github.jlangch.venice.VncException: / by zero\n",
                StringUtil.crlf_to_lf(cps.getOutput()).replaceAll("t[0-9]+:", "t00:"));
    }

    @Test
    public void test_traceable() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (trace/traceable? +))     ";

        assertTrue((Boolean)venice.eval(script1));


        final String script2 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (defn foo [] nil)       \n" +
                "   (trace/traceable? foo))     ";

        assertTrue((Boolean)venice.eval(script2));


        final String script3 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (def foo (fn [] nil))   \n" +
                "   (trace/traceable? foo))     ";

        assertTrue((Boolean)venice.eval(script3));


        final String script4 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (def foo 3)             \n" +
                "   (trace/traceable? foo))     ";

        assertFalse((Boolean)venice.eval(script4));
    }

    @Test
    public void test_traced_not_active() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (trace/traced? +))        ";

        assertFalse((Boolean)venice.eval(script1));


        final String script2 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (defn foo [] nil)       \n" +
                "   (trace/traced? foo))      ";

        assertFalse((Boolean)venice.eval(script2));


        final String script3 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (def foo (fn [] nil))   \n" +
                "   (trace/traced? foo))      ";

        assertFalse((Boolean)venice.eval(script3));
    }

    @Test
    public void test_traced_active() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (trace/trace-var +)     \n" +
                "   (trace/traced? +))        ";

        assertTrue((Boolean)venice.eval(script1));


        final String script2 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (defn foo [] nil)       \n" +
                "   (trace/trace-var foo)   \n" +
                "   (trace/traced? foo))      ";

        assertTrue((Boolean)venice.eval(script2));


        final String script3 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (def foo (fn [] nil))   \n" +
                "   (trace/trace-var foo)   \n" +
                "   (trace/traced? foo))      ";

        assertTrue((Boolean)venice.eval(script3));
    }

    @Test
    public void test_traced_active_revert() {
        final Venice venice = new Venice();

        final String script1 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (trace/trace-var +)     \n" +
                "   (trace/untrace-var +)   \n" +
                "   (trace/traced? +))        ";

        assertFalse((Boolean)venice.eval(script1));


        final String script2 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (defn foo [] nil)       \n" +
                "   (trace/trace-var foo)   \n" +
                "   (trace/untrace-var foo) \n" +
                "   (trace/traced? foo))      ";

        assertFalse((Boolean)venice.eval(script2));


        final String script3 =
                "(do                        \n" +
                "   (load-module :trace)    \n" +
                "   (def foo (fn [] nil))   \n" +
                "   (trace/trace-var foo)   \n" +
                "   (trace/untrace-var foo) \n" +
                "   (trace/traced? foo))      ";

        assertFalse((Boolean)venice.eval(script3));
    }

    @Test
    public void test_tee_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                          \n" +
                "  (load-module :trace)                       \n" +
                "  (with-out-str                              \n" +
                "    (-> 5                                    \n" +
                "       (+ 3)                                 \n" +
                "       (/ 2)                                 \n" +
                "       (trace/tee-> #(print \"trace:\" %))   \n" +
                "       (- 1))))                                ";

        assertEquals("trace: 4", venice.eval(script));
    }

    @Test
    public void test_tee_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (load-module :trace)                        \n" +
                "  (with-out-str                               \n" +
                "    (->> 5                                    \n" +
                "        (+ 3)                                 \n" +
                "        (/ 32)                                \n" +
                "        (trace/tee->> #(print \"trace:\" %))  \n" +
                "        (- 1))))                                ";

        assertEquals("trace: 4", venice.eval(script));
    }

    @Test
    public void test_tee_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                           \n" +
                "  (load-module :trace)                        \n" +
                "  (with-out-str                               \n" +
                "    (->> 5                                    \n" +
                "        (+ 3)                                 \n" +
                "        (/ 32)                                \n" +
                "        trace/tee                             \n" +
                "        (- 1))))                                ";

        // Must run on *nix and Windows
        assertEquals("trace: 4\n", StringUtil.crlf_to_lf((String)venice.eval(script)));
    }

}
