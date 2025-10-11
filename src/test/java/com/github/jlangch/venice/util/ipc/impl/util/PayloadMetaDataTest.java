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

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static com.github.jlangch.venice.util.ipc.impl.util.PayloadMetaData.decode;
import static com.github.jlangch.venice.util.ipc.impl.util.PayloadMetaData.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.Topics;


public class PayloadMetaDataTest {

    @Test
    public void test_equals_1() {
        PayloadMetaData data = null;

        data = new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8");
        assertEquals(data, data);

        data = new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8");
        assertEquals(data, new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"));
    }

    @Test
    public void test_equals_2() {
        assertEquals(
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"));

        assertEquals(
                new PayloadMetaData(null, Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData(null, Topics.of("alpha"), "text/plain", "UTF-8"));

        assertEquals(
                new PayloadMetaData(null, Topics.of("alpha"), "text/plain", null),
                new PayloadMetaData(null, Topics.of("alpha"), "text/plain", null));

        assertEquals(
                new PayloadMetaData("queue", Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8"),
                new PayloadMetaData("queue", Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8"));

        assertEquals(
                new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8"),
                new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8"));

        assertEquals(
                new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", null),
                new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", null));

        assertNotEquals(
                new PayloadMetaData("queue1", Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData("queue2", Topics.of("alpha"), "text/plain", "UTF-8"));

        assertNotEquals(
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData("queue", Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8"));

        assertNotEquals(
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain1", "UTF-8"));

        assertNotEquals(
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8"),
                new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF"));
    }

    @Test
    public void test_encode_decode() {
        PayloadMetaData data = null;

        data = new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", "UTF-8");
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(null, Topics.of("alpha"), "text/plain", "UTF-8");
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData("queue", Topics.of("alpha"), "text/plain", null);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(null, Topics.of("alpha"), "text/plain", null);
        assertEquals(data, decode(encode(data)));


        data = new PayloadMetaData("queue", Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8");
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8");
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData("queue", Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8");
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(null, Topics.of(toSet("alpha", "beta")), "text/plain", null);
        assertEquals(data, decode(encode(data)));
   }

}
