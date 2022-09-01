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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Sandbox_PreCompiledScript_Test {

    @Test
    public void test_RejectAllInterceptor_1() {
        final PreCompiled pre = new Venice(new AcceptAllInterceptor())
                                    .precompile("test","(gc)");

        assertThrows(SecurityException.class, () -> {
            // RejectAllInterceptor -> gc is blacklisted
            new Venice(new RejectAllInterceptor()).eval(pre);
        });
    }

    @Test
    public void test_RejectAllInterceptor_2() {
        final PreCompiled pre = new Venice(new RejectAllInterceptor())
                                    .precompile("test","(gc)");

        assertThrows(SecurityException.class, () -> {
            // RejectAllInterceptor -> gc is blacklisted
            new Venice(new RejectAllInterceptor()).eval(pre);
        });
    }

    @Test
    public void test_AcceptAllInterceptor_1() {
        final PreCompiled pre = new Venice(new AcceptAllInterceptor())
                                    .precompile("test","(gc)");

        new Venice(new AcceptAllInterceptor()).eval(pre);
    }

    @Test
    public void test_AcceptAllInterceptor_2() {
        final PreCompiled pre = new Venice(new RejectAllInterceptor())
                                    .precompile("test","(gc)");

        new Venice(new AcceptAllInterceptor()).eval(pre);
    }

    @Test
    public void test_SandboxInterceptor_1a() {
        final PreCompiled pre = new Venice(new AcceptAllInterceptor())
                                    .precompile("test","(gc)");

        new Venice(new SandboxInterceptor(new SandboxRules())).eval(pre);
    }

    @Test
    public void test_SandboxInterceptor_1b() {
        final PreCompiled pre = new Venice(new AcceptAllInterceptor())
                                    .precompile("test","(gc)");

        final SandboxRules rules = new SandboxRules()
        								.rejectVeniceFunctions("*io*")
        								.rejectVeniceFunctions("*system*");

        assertThrows(SecurityException.class, () -> {
            // SandboxInterceptor with I/O functions rejected -> gc is blacklisted
            new Venice(new SandboxInterceptor(rules)).eval(pre);
        });
    }

    @Test
    public void test_SandboxInterceptor_2a() {
        final PreCompiled pre = new Venice(new RejectAllInterceptor())
                                    .precompile("test","(gc)");

        new Venice(new SandboxInterceptor(new SandboxRules())).eval(pre);
    }

    @Test
    public void test_SandboxInterceptor_2b() {
        final PreCompiled pre = new Venice(new RejectAllInterceptor())
                                    .precompile("test","(gc)");

        final SandboxRules rules = new SandboxRules()
        								.rejectVeniceFunctions("*io*")
        								.rejectVeniceFunctions("*system*");

        assertThrows(SecurityException.class, () -> {
            // SandboxInterceptor with I/O functions rejected -> gc is blacklisted
            new Venice(new SandboxInterceptor(rules)).eval(pre);
        });
    }

}
