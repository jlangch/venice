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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class ServiceRegistryTest {

    @Test
    public void test_service() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.register("Calculator", new Calculator());

        assertEquals(200L, venice.eval("(service :Calculator :mul 10 20)"));
        assertEquals(30L,  venice.eval("(service :Calculator :add 10 20)"));
    }

    @Test
    public void test_service_exists() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.register("Calculator", new Calculator());

        assertTrue((Boolean)venice.eval("(service? :Calculator)"));
        assertFalse((Boolean)venice.eval("(service? :Xxxxxxxx)"));
    }


    public static class Calculator {
        public long mul(final long v1, final long v2) {
            return v1 * v2;
        }
        public long add(final long v1, final long v2) {
            return v1 + v2;
        }
    }

}
