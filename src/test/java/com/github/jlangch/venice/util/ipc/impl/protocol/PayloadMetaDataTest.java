/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.ipc.impl.protocol;

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static com.github.jlangch.venice.util.ipc.impl.protocol.PayloadMetaData.decode;
import static com.github.jlangch.venice.util.ipc.impl.protocol.PayloadMetaData.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.MessageType;
import com.github.jlangch.venice.util.ipc.ResponseStatus;
import com.github.jlangch.venice.util.ipc.impl.Topics;


public class PayloadMetaDataTest {

    @Test
    public void test_equals_1() {
        final UUID id = UUID.randomUUID();

        PayloadMetaData data = null;

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id);
        assertEquals(data, data);

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id);
        assertEquals(data, new PayloadMetaData(
                                false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                                "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id));
    }

    @Test
    public void test_equals_2() {
        final UUID id = UUID.randomUUID();

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id));

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", "UTF-8", id));

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", null, id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", null, id));

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id));

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,

                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id));

        assertEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", null, id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", null, id));

        assertNotEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue1", null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue2", null, Topics.of("alpha"), "text/plain", "UTF-8", id));

        assertNotEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id));

        assertNotEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain1", "UTF-8", id));

        assertNotEquals(
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id),
                new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF", id));
    }

    @Test
    public void test_encode_decode() {
        final UUID id = UUID.randomUUID();

        PayloadMetaData data = null;

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", "UTF-8", id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of("alpha"), "text/plain", null, id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of("alpha"), "text/plain", null, id);
        assertEquals(data, decode(encode(data)));


        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        "queue", null, Topics.of(toSet("alpha", "beta")), "text/plain", "UTF-8", id);
        assertEquals(data, decode(encode(data)));

        data = new PayloadMetaData(
                        false, false, false, null, MessageType.REQUEST, ResponseStatus.OK,
                        null, null, Topics.of(toSet("alpha", "beta")), "text/plain", null, id);
        assertEquals(data, decode(encode(data)));
    }

    @Test
    @Disabled
    public void test_encode_decode_time() {
        final UUID id = UUID.randomUUID();

        int count = 100_000;

        // warmup
        for(int ii=0; ii<1_000; ii++) {
            PayloadMetaData data = new PayloadMetaData(
                                        false, false, false, String.valueOf(count), MessageType.REQUEST, ResponseStatus.OK,
                                        "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id);
            assertEquals(data, decode(encode(data)));
        }

        final long start = System.currentTimeMillis();

        for(int ii=0; ii<count; ii++) {
           PayloadMetaData data = new PayloadMetaData(
                                           false, false, false, String.valueOf(count), MessageType.REQUEST, ResponseStatus.OK,
                                           "queue", null, Topics.of("alpha"), "text/plain", "UTF-8", id);
           assertEquals(data, decode(encode(data)));
        }

        final long elapsed = System.currentTimeMillis() - start;
        System.out.println("" + elapsed * 1_000_000L / count + "ns");
    }

//    @Test
//    public void test_perf_1() {
//        final long start = System.currentTimeMillis();
//
//        for(int ii=0; ii<1_000_000; ii++) {
//            String s = "dsfshfksahfkhsdakjfhskafhksadf";
//            byte[] arr = s.getBytes(StandardCharsets.UTF_8);
//            if (arr == null) fail("?");
//        }
//
//        System.out.println(System.currentTimeMillis() - start);
//    }
//
//    @Test
//    public void test_perf_2() {
//        String r = "dsfshfksahfkhsdakjfhskafhksadf";
//        byte[] arr = r.getBytes(StandardCharsets.UTF_8);
//
//        final long start = System.currentTimeMillis();
//
//        for(int ii=0; ii<1_000_000; ii++) {
//            String s = new String(arr, StandardCharsets.UTF_8);
//            if (s.isEmpty()) fail("?");
//        }
//
//        System.out.println(System.currentTimeMillis() - start);
//    }

}
