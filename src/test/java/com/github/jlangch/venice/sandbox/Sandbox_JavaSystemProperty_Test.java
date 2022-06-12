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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.Interceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

public class Sandbox_JavaSystemProperty_Test {


    // ------------------------------------------------------------------------
    // Sandbox FAIL
    // ------------------------------------------------------------------------

    @Test
    public void test_RejectAccessToAllSystemProperties_RejectAllInterceptor() {
        assertThrows(SecurityException.class, () -> {
            new Venice(new RejectAllInterceptor()).eval("(system-prop \"db.password\")");
        });
    }

    @Test
    public void test_all_RejectAllInterceptor() {
        final HashMap<?,?> env = (HashMap<?,?>)new Venice(new RejectAllInterceptor()).eval("(system-prop)");
        assertTrue(env.isEmpty());
    }

    @Test
    public void test_RejectAccessToAllSystemProperties_EmptySandbox() {
        final Interceptor interceptor = new SandboxInterceptor(new SandboxRules());

        assertThrows(SecurityException.class, () -> {
            new Venice(interceptor).eval("(system-prop \"db.password\")");
        });
    }

    @Test
    public void test_RejectAccessToNonStandardSystemProperties() {
        final Interceptor interceptor = new SandboxInterceptor(
                                                new SandboxRules().withStandardSystemProperties());

        assertThrows(SecurityException.class, () -> {
            new Venice(interceptor).eval("(system-prop \"db.password\")");
        });
    }

    @Test
    public void test_RejectAccessToNonWhitelistedSystemProperties() {
        final Interceptor interceptor = new SandboxInterceptor(
                                                new SandboxRules().withSystemProperties("user.home"));

        assertThrows(SecurityException.class, () -> {
            new Venice(interceptor).eval("(system-prop \"db.password\")");
        });
    }



    // ------------------------------------------------------------------------
    // Sandbox PASS
    // ------------------------------------------------------------------------

    @Test
    public void test_NoSandbox() {
        new Venice().eval("(system-prop \"db.password\")");
        new Venice().eval("(system-prop \"user.home\")");
    }

    @Test
    public void test_all_NoSandbox() {
        final HashMap<?,?> env = (HashMap<?,?>)new Venice().eval("(system-prop)");
        assertFalse(env.isEmpty());
    }

    @Test
    public void test_AccessToAllSystemProperties() {
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().withAllSystemProperties());

        new Venice(interceptor).eval("(system-prop \"db.password\")");
        new Venice(interceptor).eval("(system-prop \"user.home\")");
    }

    @Test
    public void test_all_AccessToAllSystemProperties() {
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().withAllSystemProperties());

        final HashMap<?,?> env = (HashMap<?,?>)new Venice(interceptor).eval("(system-prop)");
        assertFalse(env.isEmpty());
    }

    @Test
    public void test_AccessToStandardSystemProperties() {
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().withStandardSystemProperties());

        new Venice(interceptor).eval("(system-prop \"user.home\")");
    }

    @Test
    public void test_AccessToWhitelistedSystemProperties() {
        final Interceptor interceptor =
                new SandboxInterceptor(new SandboxRules().withSystemProperties("db.password"));

        new Venice(interceptor).eval("(system-prop \"db.password\")");
    }

}
