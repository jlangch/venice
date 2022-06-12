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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.support.AuditEvent;
import com.github.jlangch.venice.support.AuditEventType;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class VeniceTest {

    @Test
    public void version() {
        final String version =  Version.VERSION;

        assertNotNull(version);
    }

    @Test
    public void evalTest1() {
        final Venice venice = new Venice();

        /**
         * EVAL:            :core/list > (first (rest (rest [1 2 3])))
         * EVAL SEQ VALUES: :core/list > ((rest (rest [1 2 3])))
         * EVAL:            :core/list > (rest (rest [1 2 3]))
         * EVAL SEQ VALUES: :core/list > ((rest [1 2 3]))
         * EVAL:            :core/list > (rest [1 2 3])
         * EVAL SEQ VALUES: :core/list > ([1 2 3])
         * EVAL:            :core/vector > [1 2 3]
         * EVAL VALUES:     :core/vector > [1 2 3]
         * EVAL SEQ VALUES: :core/vector > [1 2 3]
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/long > 2
         * EVAL VALUES:     :core/long > 2
         * EVAL:            :core/long > 3
         * EVAL VALUES:     :core/long > 3
         */
        assertEquals(3L, venice.eval("(first (rest (rest [1 2 3])))"));
    }

    @Test
    public void evalTest2() {
        final Venice venice = new Venice();

        /**
         * EVAL:            :core/list > (third (conj [1 2 (inc 2)] 4))
         * EVAL SEQ VALUES: :core/list > ((conj [1 2 (inc 2)] 4))
         * EVAL:            :core/list > (conj [1 2 (inc 2)] 4)
         * EVAL SEQ VALUES: :core/list > ([1 2 (inc 2)] 4)
         * EVAL:            :core/vector > [1 2 (inc 2)]
         * EVAL VALUES:     :core/vector > [1 2 (inc 2)]
         * EVAL SEQ VALUES: :core/vector > [1 2 (inc 2)]
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/long > 2
         * EVAL VALUES:     :core/long > 2
         * EVAL:            :core/list > (inc 2)
         * EVAL SEQ VALUES: :core/list > (2)
         * EVAL:            :core/long > 2
         * EVAL VALUES:     :core/long > 2
         * EVAL:            :core/long > 4
         * EVAL VALUES:     :core/long > 4
         */
        assertEquals(3L, venice.eval("(third (conj [1 2 (inc 2)] 4))"));
    }

    @Test
    public void evalTest3() {
        final Venice venice = new Venice();

        /**
         * EVAL:            :core/list > (+ 1 (third (conj [1 2] 3)))
         * EVAL SEQ VALUES: :core/list > (1 (third (conj [1 2] 3)))
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/list > (third (conj [1 2] 3))
         * EVAL SEQ VALUES: :core/list > ((conj [1 2] 3))
         * EVAL:            :core/list > (conj [1 2] 3)
         * EVAL SEQ VALUES: :core/list > ([1 2] 3)
         * EVAL:            :core/vector > [1 2]
         * EVAL VALUES:     :core/vector > [1 2]
         * EVAL SEQ VALUES: :core/vector > [1 2]
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/long > 2
         * EVAL VALUES:     :core/long > 2
         * EVAL:            :core/long > 3
         * EVAL VALUES:     :core/long > 3
         */
        assertEquals(4L, venice.eval("(+ 1 (third (conj [1 2] 3)))"));
    }

    @Test
    public void evalTest4() {
        final Venice venice = new Venice();

        /**
         * EVAL:            :core/list > (let [a 1] (+ a (third (conj [1 2] 3))))
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/list > (+ a (third (conj [1 2] 3)))
         * EVAL SEQ VALUES: :core/list > (a (third (conj [1 2] 3)))
         * EVAL:            :core/symbol > a
         * EVAL VALUES:     :core/symbol > a
         * EVAL:            :core/list > (third (conj [1 2] 3))
         * EVAL SEQ VALUES: :core/list > ((conj [1 2] 3))
         * EVAL:            :core/list > (conj [1 2] 3)
         * EVAL SEQ VALUES: :core/list > ([1 2] 3)
         * EVAL:            :core/vector > [1 2]
         * EVAL VALUES:     :core/vector > [1 2]
         * EVAL SEQ VALUES: :core/vector > [1 2]
         * EVAL:            :core/long > 1
         * EVAL VALUES:     :core/long > 1
         * EVAL:            :core/long > 2
         * EVAL VALUES:     :core/long > 2
         * EVAL:            :core/long > 3
         * EVAL VALUES:     :core/long > 3
         */
        assertEquals(4L, venice.eval("(let [a 1] (+ a (third (conj [1 2] 3))))"));
    }

    @Test
    public void evalWithIntegerAndLong() {
        final Venice venice = new Venice();

        assertEquals(7L, venice.eval("(+ 1 x)", Parameters.of("x", (short)6)));
        assertEquals(7L, venice.eval("(+ 1 x)", Parameters.of("x", 6)));
        assertEquals(7L, venice.eval("(+ 1 x)", Parameters.of("x", 6L)));
        assertEquals(7.2D, (Double)venice.eval("(+ 1 x)", Parameters.of("x", 6.2F)), 0.0001D);
        assertEquals(7.2D, (Double)venice.eval("(+ 1 x)", Parameters.of("x", 6.2D)), 0.0001D);
        assertEquals(247L, venice.eval("(+ 1 x)", Parameters.of("x", (byte)-10)));
    }

    @Test
    public void evalEnv() {
        final AuditEvent event = new AuditEvent(
                                        "su",
                                        2000L,
                                        AuditEventType.ALERT,
                                        "superuser",
                                        "webapp.started",
                                        "text");

        final Venice venice = new Venice();

        final Map<String,Object> symbols = Parameters.of("event", event);

        assertEquals("webapp.started", venice.eval("(get event :eventName)", symbols));
        assertEquals("superuser", venice.eval("(get event :eventKey)", symbols));
        assertEquals("ALERT", venice.eval("(get event :eventType)", symbols));
        assertEquals(2000L, venice.eval("(get event :elapsedTimeMillis)", symbols));
    }

    @Test
    public void evalWithObject() {
        final AuditEvent event1 = new AuditEvent(
                                        "su",
                                        2000L,
                                        AuditEventType.ALERT,
                                        "superuser",
                                        "webapp.started",
                                        "text");

        final AuditEvent event2 = new AuditEvent(
                                        "jd",
                                        2000L,
                                        AuditEventType.INFO,
                                        "john.doe",
                                        "login",
                                        "text");

        final Venice venice = new Venice();


        final String script =
                "(or (match? (get event :eventName) \"webapp[.](started|stopped)\") " +
                "    (== (get event :eventKey) \"superuser\") " +
                "    (== (get event :eventType) \"ALERT\") " +
                ")";

        assertEquals(Boolean.TRUE, venice.eval(script, Parameters.of("event", event1)));

        assertEquals(Boolean.FALSE, venice.eval(script, Parameters.of("event", event2)));
    }

    @Test
    public void test_version() {
        final Venice venice = new Venice();

        assertEquals(Venice.getVersion(), venice.eval("*version*"));
    }

    @Test
    public void test_loaded_modules() {
        final Venice venice = new Venice();

        assertEquals(10L, venice.eval("(count (sort *loaded-modules*))"));
    }

    @Test
    public void test_CapturingPrintStream() {
        try(CapturingPrintStream ps = new CapturingPrintStream()) {
            final Venice venice = new Venice();

            venice.eval("(print 10)", Parameters.of("*out*", ps));
            assertEquals("10", ps.getOutput());

            ps.reset();

            venice.eval("(print 10)", Parameters.of("*out*", null));
            assertEquals("", ps.getOutput());
        }
    }

    @Test
    public void test_CapturingPrintStream_BelowLimit() {
        try(CapturingPrintStream ps = new CapturingPrintStream()) {
            final Venice venice = new Venice();

            venice.eval("(range 1 10000)", Parameters.of("*out*", ps));
            assertNotNull(ps.getOutput());
        }
    }

    @Test
    public void test_CapturingPrintStream_Limit() {
        try(CapturingPrintStream ps = new CapturingPrintStream(10000)) {
            final Venice venice = new Venice();

            assertThrows(SecurityException.class, () -> {
                venice.eval("(map print (range 1 10000))", Parameters.of("*out*", ps));
            });
        }
    }

    @Test
    public void test_CapturingPrintStream_PreserveResult() {
        try(CapturingPrintStream ps = new CapturingPrintStream()) {
            final Venice venice = new Venice();

            final Object result = venice.eval(
                                    "(do (println [1 2]) 100)",
                                    Parameters.of("*out*", ps));

            assertEquals(100L, result);
            assertEquals("[1 2]\n", ps.getOutput());
        }
    }

}
