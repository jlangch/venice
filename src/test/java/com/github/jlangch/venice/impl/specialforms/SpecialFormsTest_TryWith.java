/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class SpecialFormsTest_TryWith {

    @Test
    public void test_try_with_1() {
        final Venice venice = new Venice();

        final String lisp =
                "(do                                                      \n" +
                "   (import :java.io.FileInputStream)                     \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]        \n" +
                "        (io/spit file \"123456789\" :append true)        \n" +
                "        (try-with [is (. :FileInputStream :new file)]    \n" +
                "           (io/slurp-stream is :binary false)))          \n" +
                ")";

        assertEquals("123456789", venice.eval(lisp));
    }

    @Test
    public void test_try_with_2() {
        final CapturingPrintStream ps = new CapturingPrintStream();

        final Venice venice = new Venice();

        final String lisp =
                "(do                                                         \n" +
                "   (import :java.io.FileInputStream)                        \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]           \n" +
                "        (io/spit file \"123456789\" :append true)           \n" +
                "        (try-with [is (. :FileInputStream :new file)]       \n" +
                "           (print 100)                                      \n" +
                "           (print 101)                                      \n" +
                "           (io/slurp-stream is :binary false)               \n" +
                "           (catch :java.lang.IllegalArgumentException ex    \n" +
                "                  -1)                                       \n" +
                "           (catch :VncException ex                          \n" +
                "                  -2)                                       \n" +
                "           (finally                                         \n" +
                "              (print 300)                                   \n" +
                "              (print 301))))                                \n" +
                ")";

        assertEquals("123456789", venice.eval(lisp, Parameters.of("*out*", ps)));
        assertEquals("100101300301", ps.getOutput());
    }

    @Test
    public void test_try_with_3() {
        final CapturingPrintStream ps = new CapturingPrintStream();

        final Venice venice = new Venice();

        final String lisp =
                "(do                                                         \n" +
                "   (import :java.io.FileInputStream)                        \n" +
                "   (let [file (io/temp-file \"test-\", \".txt\")]           \n" +
                "        (io/spit file \"123456789\" :append true)           \n" +
                "        (try-with [is (. :FileInputStream :new file)]       \n" +
                "           (print 100)                                      \n" +
                "           (print 101)                                      \n" +
                "           (io/slurp-stream nil :binary false)              \n" +
                "           (catch :java.lang.Exception ex                   \n" +
                "                  (print 200)                               \n" +
                "                  (print 201)                               \n" +
                "                  -1)                                       \n" +
                "           (catch :VncException ex                          \n" +
                "                  -2)                                       \n" +
                "           (finally                                         \n" +
                "              (print 300)                                   \n" +
                "              (print 301))))                                \n" +
                ")";

        assertEquals(-1L, venice.eval(lisp, Parameters.of("*out*", ps)));
        assertEquals("100101200201300301", ps.getOutput());
    }

    @Test
    public void test_try_with_ex_precedence_autocloseable_1() {
        final Venice venice = new Venice();

        // if the 'try-with' body throws an exception it has precedence of an exception
        // thrown on auto-close. the latter one will be suppressed.
        final String script =
                "(do                                                                   \n" +
                "   (import :com.github.jlangch.venice.support.AutoCloseableString)    \n" +
                "   (try-with [r1 (. :AutoCloseableString :new \"hello-1\" false)      \n" +
                "              r2 (. :AutoCloseableString :new \"hello-2\" false)]     \n" +
                "     (throw (ex :VncException \"EX-BODY\"))))                         ";

        try {
            venice.eval(script);
            fail("Expected a VncException");
        }
        catch(VncException ex) {
            final String msg = ex.getMessage();
            assertEquals("EX-BODY", msg);
        }
        catch(Exception ex) {
            fail("Expected a VncException");
        }
    }

    @Test
    public void test_try_with_ex_precedence_autocloseable_2() {
        final Venice venice = new Venice();

        // if the 'try-with' body throws an exception it has precedence of an exception
        // thrown on auto-close. the latter one will be suppressed.
        final String script =
                "(do                                                                   \n" +
                "   (import :com.github.jlangch.venice.support.AutoCloseableString)    \n" +
                "   (try-with [r1 (. :AutoCloseableString :new \"hello-1\" true)       \n" +
                "              r2 (. :AutoCloseableString :new \"hello-2\" true)]      \n" +
                "     (throw (ex :VncException \"EX-BODY\"))))                         ";

        try {
            venice.eval(script);
            fail("Expected a VncException");
        }
        catch(VncException ex) {
            final String msg = ex.getMessage();
            assertEquals("EX-BODY", msg);
        }
        catch(Exception ex) {
            fail("Expected a VncException");
        }
    }

    @Test
    public void test_try_with_ex_precedence_autocloseable_3() {
        final Venice venice = new Venice();

        // if the 'try-with' body throws an exception it has precedence of an exception
        // thrown on auto-close. the latter one will be suppressed.
        final String script =
                "(do                                                                   \n" +
                "   (import :com.github.jlangch.venice.support.AutoCloseableString)    \n" +
                "   (try-with [r1 (. :AutoCloseableString :new \"hello-1\" true)       \n" +
                "              r2 (. :AutoCloseableString :new \"hello-2\" true)]      \n" +
                "     nil))                                                            ";

        try {
            venice.eval(script);
            fail("Expected a VncException");
        }
        catch(VncException ex) {
            final String msg = ex.getMessage();
            assertEquals("'try-with' failed to close resource r2.", msg);
        }
        catch(Exception ex) {
            fail("Expected a VncException");
        }
    }
}
