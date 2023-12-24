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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.ValueException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class ExceptionFunctionsTest {

    @Test
    public void test_ex_create_1() {
        final Venice venice = new Venice();

        final String script =
                "(ex :RuntimeException \"#test\")";

        RuntimeException ex = (RuntimeException)venice.eval(script);

        assertEquals("#test", ex.getMessage());
    }

    @Test
    public void test_ex_create_2() {
        final Venice venice = new Venice();

        final String script =
                "(ex :java.text.ParseException \"#test\" 100)";

        ParseException ex = (ParseException)venice.eval(script);

        assertEquals("#test", ex.getMessage());
        assertEquals(100, ex.getErrorOffset());
    }

    @Test
    public void test_ex() {
        final Venice venice = new Venice();

        // (ex :RuntimeException)
        final String script =
                "(do                                                     \n" +
                "   (try                                                 \n" +
                "     (throw (ex :RuntimeException))                     \n" +
                "     (catch :RuntimeException e \"caugth exception\")))   ";

        assertEquals("caugth exception", venice.eval(script));
    }

    @Test
    public void test_ex_catch_converted_checked_exception() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (try                                          \n" +
                "     (throw (ex :Exception \"#test\"))           \n" +
                "     (catch :RuntimeException e (:message e))))    ";

        assertEquals("java.lang.Exception: #test", venice.eval(script));
    }

    @Test
    public void test_ex_catch_basetype() {
        final Venice venice = new Venice();

        final String script =
                "(do                                              \n" +
                "   (try                                          \n" +
                "     (throw (ex :RuntimeException \"#test\"))    \n" +
                "     (catch :Exception e (:message e))))           ";

        assertEquals("#test", venice.eval(script));
    }

    @Test
    public void test_ex_multiple_args() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                           \n" +
                "   ;; ParseException is a checked exception!                  \n" +
                "   (import :java.text.ParseException)                         \n" +
                "   (try                                                       \n" +
                "     (throw (ex :ParseException \"#test\" 100))               \n" +
                "     (catch :RuntimeException e (:errorOffset (:cause e)))))    ";

        assertEquals(100, venice.eval(script));
    }

    @Test
    public void test_ex_with_cause() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                              \n" +
                "   (import :java.io.IOException)                                 \n" +
                "   (try                                                          \n" +
                "      (throw (ex :IOException \"#test1\"))                       \n" +
                "      (catch :Exception e                                        \n" +
                "             (throw (ex :VncException \"#test2\" (:cause e))))))   ";

        try {
            venice.eval(script);

            fail("Expected VncException");
        }
        catch(VncException ex) {
            assertEquals("#test2", ex.getMessage());

            final Throwable cause = ex.getCause();
            if (cause == null) {
                fail("Expected an exception cause.");
            }
            else {
                if (!"java.io.IOException".equals(cause.getClass().getName())) {
                    fail("Expected an exception cause of type java.io.IOException");
                }
                assertEquals("#test1", cause.getMessage());
            }
        }
        catch(Exception ex) {
            fail("Expected VncException instead of " + ex.getClass().getSimpleName());
        }
    }

    @Test
    public void test_ex_ValueException() {
        final Venice venice = new Venice();

        // (ex :ValueException 100)
        final String script =
                "(do                                             \n" +
                "   (try                                         \n" +
                "      (throw 100)                               \n" +
                "      (catch :ValueException e (:value e))))    ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ex_selector_predicate_1a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (try                                  \n" +
                "      (throw 100)                        \n" +
                "      (catch #(long? %) e (:value e))))    ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ex_selector_predicate_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (try                                  \n" +
                "      (throw 100)                        \n" +
                "      (catch long? e (:value e))))         ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ex_selector_predicate_1c() {
        final Venice venice = new Venice();

        final String script =
                "(do                                      \n" +
                "   (try                                  \n" +
                "      (throw 100)                        \n" +
                "      (catch #(int? %) e (:value e))))    ";

        try {
            venice.eval(script);

            fail("Expected ValueException");
        }
        catch(ValueException ex) {
            assertEquals(100L, ex.getValue());
        }
    }

    @Test
    public void test_ex_selector_predicate_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "   (try                                   \n" +
                "      (throw 100)                         \n" +
                "      (catch #(= 100 %) e (:value e))))      ";

        assertEquals(100L, venice.eval(script));
    }

    @Test
    public void test_ex_selector_predicate_customtype_3a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                   \n" +
                "   (deftype :my-exception-1 [x :long, y :long])       \n" +
                "   (deftype :my-exception-2 [a :long, b :long])       \n" +
                "   (try                                               \n" +
                "      (throw (my-exception-1. 0 0))                   \n" +
                "      (catch my-exception-1? e (pr-str (:value e)))   \n" +
                "      (catch my-exception-2? e (pr-str (:value e)))))   ";

        assertEquals("{:custom-type* :user/my-exception-1 :x 0 :y 0}", venice.eval(script));
    }

    @Test
    public void test_ex_selector_predicate_customtype_3b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                    \n" +
                "   (deftype :my-exception-1 [x :long, y :long])        \n" +
                "   (deftype :my-exception-2 [a :long, b :long])        \n" +
                "   (try                                                \n" +
                "      (throw (my-exception-2. 0 0))                    \n" +
                "      (catch my-exception-1? e (pr-str (:value e)))    \n" +
                "      (catch my-exception-2? e (pr-str (:value e)))))   ";

        assertEquals("{:custom-type* :user/my-exception-2 :a 0 :b 0}", venice.eval(script));
    }

    @Test
    public void test_ex_selector_list_1a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                \n" +
                "   (try                            \n" +
                "      (throw {:a 100, :b 200})     \n" +
                "      (catch [:a 100] e            \n" +
                "         (pr-str (:value e)))))    ";

        assertEquals("{:a 100 :b 200}", venice.eval(script));
    }

    @Test
    public void test_ex_selector_list_1b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                \n" +
                "   (try                            \n" +
                "      (throw {:a 100, :b 200})     \n" +
                "      (catch [:a 200] e            \n" +
                "         (pr-str (:value e)))))    ";

        try {
            venice.eval(script);

            fail("Expected ValueException");
        }
        catch(ValueException ex) {
            assertEquals("{a=100, b=200}", ex.getValue().toString());
        }
    }

    @Test
    public void test_ex_selector_list_2a() {
        final Venice venice = new Venice();

        final String script =
                "(do                                \n" +
                "   (try                            \n" +
                "      (throw {:a 100, :b 200})     \n" +
                "      (catch [:a 100 :b 200] e     \n" +
                "          (pr-str (:value e)))))     ";

        assertEquals("{:a 100 :b 200}", venice.eval(script));
    }

    @Test
    public void test_ex_selector_list_2b() {
        final Venice venice = new Venice();

        final String script =
                "(do                                \n" +
                "   (try                            \n" +
                "      (throw {:a 100, :b 200})     \n" +
                "      (catch [:a 100 :b 201] e     \n" +
                "          (pr-str (:value e)))))     ";

        try {
            venice.eval(script);

            fail("Expected ValueException");
        }
        catch(ValueException ex) {
            assertEquals("{a=100, b=200}", ex.getValue().toString());
        }
    }

    @Test
    public void test_ex_selector_list_with_cause() {
        final Venice venice = new Venice();

        final String script =
                "(try                                                      \n" +
                "   (throw (ex :java.io.IOException \"test\"))             \n" +
                "   (catch [:cause-type :java.io.IOException] e            \n" +
                "      \"IOException, message: ~(:message (:cause e))\")   \n" +
                "   (catch :RuntimeException  e                            \n" +
                "     \"RuntimeException, message: ~(:message e)\")))";

        assertEquals("IOException, message: test", venice.eval(script));
    }
}
