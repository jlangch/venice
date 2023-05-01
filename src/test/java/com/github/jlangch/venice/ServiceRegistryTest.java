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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class ServiceRegistryTest {

    @Test
    public void test_service_register() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.register("Calculator", new Calculator());

        final long r1 = (Long)venice.eval("(service :Calculator :mul 10 20)");
        final long r2 = (Long)venice.eval("(service :Calculator :add 10 20)");

        assertEquals(200L, r1);
        assertEquals(30L,  r2);
    }

    @Test
    public void test_service_register_all() {
        final Venice venice = new Venice();

        final Map<String,Object> services = new HashMap<>();
        services.put("Calculator", new Calculator());

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.registerAll(services);

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

    @Test
    public void test_service_lookup() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.register("Calculator", new Calculator());

        assertNotNull(registry.lookup("Calculator"));
        assertTrue(registry.lookup("Calculator") instanceof Calculator);
    }

    @Test
    public void test_service_clear() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();
        registry.register("Calculator", new Calculator());

        assertTrue((Boolean)venice.eval("(service? :Calculator)"));

        registry.unregisterAll();

        assertFalse((Boolean)venice.eval("(service? :Calculator)"));
    }

    @Test
    public void test_service_register_blank_name() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();

        assertThrows(AssertionException.class, () -> registry.register(null, new Calculator()));
        assertThrows(AssertionException.class, () -> registry.register("", new Calculator()));
        assertThrows(AssertionException.class, () -> registry.register("  ", new Calculator()));
    }

    @Test
    public void test_service_register_null_service() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();

        assertThrows(AssertionException.class, () -> registry.register("Calculator", null));
    }

    @Test
    public void test_service_register_blank_name_and_null_service() {
        final Venice venice = new Venice();

        final IServiceRegistry registry = venice.getServiceRegistry();

        assertThrows(AssertionException.class, () -> registry.register(null, null));
        assertThrows(AssertionException.class, () -> registry.register("", null));
        assertThrows(AssertionException.class, () -> registry.register("  ", null));
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
