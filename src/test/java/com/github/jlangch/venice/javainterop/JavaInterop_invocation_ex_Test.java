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
package com.github.jlangch.venice.javainterop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.JavaMethodInvocationException;
import com.github.jlangch.venice.Venice;


public class JavaInterop_invocation_ex_Test {


    @Test
    public void test_ok() {
        final Venice venice = new Venice();

        final String script = "(. :com.github.jlangch.venice.javainterop.JavaInterop_invocation_ex_Test$TestObj :methodA 100) ";

        assertEquals(100, venice.eval(script));
    }

    @Test
    @Disabled
    public void test_invalid_param() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                         \n" +
                "  (def clazz :com.github.jlangch.venice.javainterop.JavaInterop_invocation_ex_Test$TestObj) \n" +
                "  (defn run []                                                                              \n" +
                "    (. clazz :methodC true))                                                                \n" +
                "  (run))";

        try {
            venice.eval(script);
        }
        catch(JavaMethodInvocationException ex) {
            System.out.println(ex.printVeniceStackTraceToString());
        }
    }

    @Test
    @Disabled
    public void test_fail() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                         \n" +
                "  (def clazz :com.github.jlangch.venice.javainterop.JavaInterop_invocation_ex_Test$TestObj) \n" +
                "  (defn run []                                                                              \n" +
                "    (. clazz :methodC 100))                                                                 \n" +
                "  (run))";

        try {
            venice.eval(script);
        }
        catch(JavaMethodInvocationException ex) {
            System.out.println(ex.printVeniceStackTraceToString());
        }
    }


    public static class TestObj {

        public static int methodErr(int a) {
            throw new RuntimeException("Failed on methodErr");
        }

        public static int methodA(int a) {
            return a;
        }

        public static int methodB(int b) {
            return methodA(b);
        }

        public static int methodC(int c) {
            return methodErr(c);
        }
    }
}
