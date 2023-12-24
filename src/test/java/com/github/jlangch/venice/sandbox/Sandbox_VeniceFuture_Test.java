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
package com.github.jlangch.venice.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_VeniceFuture_Test {

    @Test
    public void test_future_not_sandboxed() {
            final Venice venice = new Venice();

            final String script =
                    "(do                                        " +
                    "   (def wait (fn [] (sandboxed?)))         " +
                    "                                           " +
                    "   (let [f (future wait)]                  " +
                    "        (deref f))                         " +
                    ") ";

            assertFalse((Boolean)venice.eval(script));
    }

    @Test
    public void test_future_sandboxed() {
        // all venice 'file' function blacklisted
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().rejectVeniceFunctions("io/file"));

        final Venice venice = new Venice(interceptor);

        final String script =
                "(do                                        " +
                "   (def wait (fn [] (sandboxed?)))         " +
                "                                           " +
                "   (let [f (future wait)]                  " +
                "        (deref f))                         " +
                ") ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_future_sandbox_violation() {
        // all venice 'file' function blacklisted
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().rejectVeniceFunctions("io/file"));

        final Venice venice = new Venice(interceptor);

        // 'io/file' is black listed, thus a call to 'io/file' must
        // throw a SecurityException!
        final String script =
                "(do                                        " +
                "   (def wait (fn [] (io/file \"a.txt\")))  " +
                "                                           " +
                "   (let [f (future wait)]                  " +
                "        (deref f))                         " +
                ") ";

        assertThrows(SecurityException.class, () -> venice.eval(script));
    }

    @Test
    public void test_future_sandbox_ok() {
        // all venice 'file' function blacklisted
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().rejectVeniceFunctions("io/slurp"));

        final Venice venice = new Venice(interceptor);

        final String script =
                "(do                                        " +
                "   (def wait (fn [] (io/file \"a.txt\")))  " +
                "                                           " +
                "   (let [f (future wait)]                  " +
                "        (deref f))                         " +
                ") ";

        final File file = (File)venice.eval(script);
        assertEquals("a.txt", file.getName());
    }

}
