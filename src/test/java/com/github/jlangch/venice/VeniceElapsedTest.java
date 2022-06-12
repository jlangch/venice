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

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.support.AuditEvent;
import com.github.jlangch.venice.support.AuditEventType;
import com.github.jlangch.venice.util.FunctionExecutionMeter;


public class VeniceElapsedTest {

    @BeforeAll
    public static void test() {
        System.out.println("Performance tests (VeniceElapsedTest):");
    }

    @Test
    public void evalWithObject() {
        final AuditEvent event = new AuditEvent(
                                        "jd",
                                        2000L,
                                        AuditEventType.ALERT,
                                        "john.doe",
                                        "login",
                                        "text");

        final Venice venice = new Venice();

        final String script1 =
                "(or (match? (get event :eventName) \"webapp[.](started|stopped)\") " +
                "    (== (get event :eventKey) \"superuser\") " +
                "    (== (get event :eventType) \"ALERT\") " +
                ")";

        final String script2 =
                "(or (match? eventName \"webapp[.](started|stopped)\") " +
                "    (== eventKey \"superuser\") " +
                "    (== eventType \"ALERT\") " +
                ")";

        final PreCompiled compiled1 = venice.precompile("script1", script1);
        final PreCompiled compiled2 = venice.precompile("script2", script2);

        // --------------------------------------------------------
        // Java
        // --------------------------------------------------------
        // JIT warm.up
        @SuppressWarnings("unused")
        int result = 0;
        for(int ii=0; ii<12000; ii++) {
            boolean res = event.getEventName().matches("webapp[.](started|stopped)")
                            || event.getEventKey().equals("superuser")
                            || event.getEventType() == AuditEventType.ALERT;
            result += res ? 0 : 1;
        }
        System.gc();
        final StopWatch	sw = StopWatch.millis();
        for(int ii=0; ii<1000; ii++) {
            boolean res = event.getEventName().matches("webapp[.](started|stopped)")
                            || event.getEventKey().equals("superuser")
                            || event.getEventType() == AuditEventType.ALERT;
            result += res ? 0 : 1;
        }
        System.out.println("Elapsed (Java reference, 1000 calls): " + sw.stop().toString());


        // --------------------------------------------------------
        // not compiled, implicit symbol conversion
        // --------------------------------------------------------
        // JIT warm.up
        for(int ii=0; ii<12000; ii++) {
            venice.eval(script1, Parameters.of("event", event));
        }
        System.gc();
        sw.start();
        for(int ii=0; ii<1000; ii++) {
            venice.eval(script1, Parameters.of("event", event));
        }
        System.out.println("Elapsed (1000 calls): " + sw.stop().toString());



        // --------------------------------------------------------
        // precompiled, implicit symbol conversion
        // --------------------------------------------------------
        // JIT warm.up
        for(int ii=0; ii<12000; ii++) {
            venice.eval(compiled1, Parameters.of("event", event));
        }
        System.gc();
        sw.start();
        for(int ii=0; ii<1000; ii++) {
            // implicitly convert AuditEvent symbol (JavaInteropUtil with reflection)
            venice.eval(compiled1, Parameters.of("event", event));
        }
        System.out.println("Elapsed (precompiled, implicit params, 1000 calls): " + sw.stop().toString());


        // --------------------------------------------------------
        // precompiled, explicit symbol conversion
        // --------------------------------------------------------
        // JIT warm.up
        for(int ii=0; ii<12000; ii++) {
            venice.eval(compiled1, Parameters.of("event", toMap(event)));
        }
        System.gc();
        sw.start();
        for(int ii=0; ii<1000; ii++) {
            // explicitly convert AuditEvent symbol
            venice.eval(compiled1, Parameters.of("event", toMap(event)));
        }
        System.out.println("Elapsed (precompiled, explicit params, 1000 calls): " + sw.stop().toString());


        // --------------------------------------------------------
        // precompiled, explicit symbol conversion
        // --------------------------------------------------------
        // JIT warm.up
        for(int ii=0; ii<12000; ii++) {
            venice.eval(compiled2,
                    Parameters.of(
                            "eventName", event.getEventName(),
                            "eventType", event.getEventType(),
                            "eventKey", event.getEventKey()));
        }
        System.gc();
        sw.start();
        for(int ii=0; ii<1000; ii++) {
            // explicitly convert AuditEvent symbol
            venice.eval(compiled2,
                    Parameters.of(
                            "eventName", event.getEventName(),
                            "eventType", event.getEventType(),
                            "eventKey", event.getEventKey()));
        }
        System.out.println("Elapsed (precompiled, simple params, 1000 calls): " + sw.stop().toString());
    }


    @Test
    public void evalWithTimer() {
        final Venice venice = new Venice();
        final FunctionExecutionMeter meter = venice.getFunctionExecutionMeter();

        meter.enable();

        assertEquals(Long.valueOf(7), venice.eval("(+ 1 x)", Parameters.of("x", 6L)));

        String timerData = meter.getDataFormatted("evalWithTimer()");
        assertNotNull(timerData);
        //System.out.println(timerData);
    }

    @Test
    public void evalWithTimer_Warmup() {
        final Venice venice = new Venice();
        final FunctionExecutionMeter meter = venice.getFunctionExecutionMeter();

        // warmup
        for(int ii=0; ii<2000; ii++) {
            venice.eval("(+ 1 x)", Parameters.of("x", 6L));
        }

        System.gc();

        meter.reset();
        meter.enable();

        assertEquals(Long.valueOf(7), venice.eval("(+ 1 x)", Parameters.of("x", 6L)));

        String timerData = meter.getDataFormatted("evalWithTimer_Warmup()");
        assertNotNull(timerData);
        //System.out.println(timerData);
    }

    @Test
    public void evalWithTimer_Precompiled() {
        final Venice venice = new Venice();
        final FunctionExecutionMeter meter = venice.getFunctionExecutionMeter();

        final PreCompiled precomp = venice.precompile("test", "(+ 1 x)");

        meter.reset();
        meter.enable();

        assertEquals(Long.valueOf(7), venice.eval(precomp, Parameters.of("x", 6L)));

        String timerData = meter.getDataFormatted("evalWithTimer_Precompiled()");
        assertNotNull(timerData);
        //System.out.println(timerData);
    }

    @Test
    public void evalWithTimer_Precompiled_Warmup() {
        final Venice venice = new Venice();
        final FunctionExecutionMeter meter = venice.getFunctionExecutionMeter();

        final PreCompiled precomp = venice.precompile("test", "(+ 1 x)");

        // warmup
        for(int ii=0; ii<2000; ii++) {
            venice.eval(precomp, Parameters.of("x", 6L));
        }

        System.gc();

        meter.reset();
        meter.enable();

        assertEquals(Long.valueOf(7), venice.eval(precomp, Parameters.of("x", 6L)));

        String timerData = meter.getDataFormatted("evalWithTimer_Precompiled_Warmup()");
        assertNotNull(timerData);
        //System.out.println(timerData);
    }

    private Map<String,Object> toMap(final AuditEvent event) {
        return Parameters.of(
                "principal", 		 event.getPrincipal(),
                "elapsedTimeMillis", event.getElapsedTimeMillis(),
                "eventType", 		 event.getEventType(),
                "eventKey",  		 event.getEventKey(),
                "eventName", 		 event.getEventName(),
                "eventMessage", 	 event.getEventMessage());
    }

}
