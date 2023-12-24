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
package com.github.jlangch.venice.impl.util.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;


public class MethodHandleUtilTest {

    @Test
    public void test_instanceField_get() throws Throwable {
        final TestObject to = new TestObject();

        final Field f = TestObject.class.getField("field_string");

        final MethodHandle get_ = MethodHandleUtil.instanceField_get(f);
        final MethodHandle set_ = MethodHandleUtil.instanceField_set(f);

        set_.invoke(to, "arg1");
        assertEquals("arg1", get_.invoke(to));

        set_.invoke(to, null);
        assertEquals(null, get_.invoke(to));
    }

    @Test
    public void test_staticField_get() throws Throwable {
        final Field f = TestObject.class.getField("field_static_string");

        final MethodHandle get_ = MethodHandleUtil.staticField_get(f);
        final MethodHandle set_ = MethodHandleUtil.staticField_set(f);

        set_.invoke("arg1");
        assertEquals("arg1", get_.invoke());

        set_.invoke(null);
        assertEquals(null, get_.invoke());
    }


    @SuppressWarnings("unused")
    private static class TestObject {

        public String field_string;

        public static String field_static_string;
    }

}
