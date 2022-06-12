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
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

public class Sandbox_MaxExecTime_Test {

    @Test
    public void test_too_long() {
        try {
            Thread.interrupted(); // reset the thread's interrupt status

            final Interceptor interceptor =
                    new SandboxInterceptor(new SandboxRules().withMaxExecTimeSeconds(2));

            final StopWatch sw = new StopWatch();

            // Returns after ~2s with a SecurityException
            assertThrows(
                    SecurityException.class,
                    () -> new Venice(interceptor).eval("(do (+ 1 1) (sleep 30000) (+ 1 2))"));

            final long elapsed = sw.stop().elapsedMillis();
            assertTrue(1900 < elapsed && elapsed < 2500, "Elapsed: " + elapsed);
        }
        finally {
            Thread.interrupted();
        }
    }

    @Test
    public void test_future_too_long() {
        try {
            Thread.interrupted(); // reset the thread's interrupt status

            final Interceptor interceptor =
                    new SandboxInterceptor(new SandboxRules().withMaxExecTimeSeconds(2));

            final Venice venice = new Venice(interceptor);

            final String script =
                    "(do                                          " +
                    "   (def wait (fn [] (do (sleep 30000) 100))) " +
                    "                                             " +
                    "   (let [f (future wait)]                    " +
                    "        (deref f))                           " +
                    ") ";

            final StopWatch sw = new StopWatch();

            // Returns after ~2s with a SecurityException
            assertThrows(SecurityException.class, () -> venice.eval(script));

            final long elapsed = sw.stop().elapsedMillis();
            assertTrue(1900 < elapsed && elapsed < 2500);
        }
        finally {
            Thread.interrupted();
        }
    }

    @Test
    public void test_ok() {
        try {
            Thread.interrupted(); // reset the thread's interrupt status

            final Interceptor interceptor =
                    new SandboxInterceptor(new SandboxRules().withMaxExecTimeSeconds(2));

            assertDoesNotThrow(() -> new Venice(interceptor).eval("(do (+ 1 1) (sleep 1000) (+ 1 2))"));
        }
        finally {
            Thread.interrupted();
        }
    }

}
