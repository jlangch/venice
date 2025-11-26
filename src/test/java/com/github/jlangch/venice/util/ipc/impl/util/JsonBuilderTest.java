/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.ipc.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class JsonBuilderTest {

    @Test
    public void test_string() {
        final String response = new JsonBuilder()
                                     .add("name", "xxx")
                                     .toJson(false);


        assertEquals("{\"name\":\"xxx\"}", response);
   }

    @Test
    public void test_null() {
        final String response = new JsonBuilder()
                                     .add("null", null)
                                     .toJson(false);


        assertEquals("{\"null\":null}", response);
   }

    @Test
    public void test_long() {
        final String response = new JsonBuilder()
                                     .add("long", 100L)
                                     .toJson(false);


        assertEquals("{\"long\":100}", response);
   }

    @Test
    public void test_int() {
        final String response = new JsonBuilder()
                                     .add("int", 100)
                                     .toJson(false);


        assertEquals("{\"int\":100}", response);
   }

    @Test
    public void test_boolean() {
        final String response = new JsonBuilder()
                                     .add("bool", true)
                                     .toJson(false);


        assertEquals("{\"bool\":true}", response);
   }

}
