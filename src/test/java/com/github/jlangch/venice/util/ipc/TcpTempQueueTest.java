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
package com.github.jlangch.venice.util.ipc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.util.Coerce;


public class TcpTempQueueTest {

    @Test
    public void test_temp_queue_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);
        final TcpClient client2 = new TcpClient(33333);

        server.createQueue("queue/1", 10);
        server.createQueue("queue/2", 10);

        server.start();

        sleep(100);

        client1.open();
        client2.open();

        try {
            final String tmpQueue = client1.createTemporaryQueue(10);
            assertNotNull(tmpQueue);

            final IMessage request = MessageFactory.text(
                                        null,
                                        "tcp-server/status",
                                        "appliaction/json",
                                        "UTF-8",
                                        "");

            // client 1

            final IMessage response1 = client1.sendMessage(request);

            final VncMap data1 = (VncMap)response1.getVeniceData();

            assertEquals(3L, Coerce
                                .toVncLong(data1.get(new VncKeyword("queue-count")))
                                .toJavaLong());

            assertEquals(1L, Coerce
                                .toVncLong(data1.get(new VncKeyword("temp-queue-count")))
                                .toJavaLong());

            assertEquals(1L, Coerce
                                .toVncLong(data1.get(new VncKeyword("temp-queue-this-client-count")))
                                .toJavaLong());


            // client 2

            final IMessage response2 = client2.sendMessage(request);

            final VncMap data2 = (VncMap)response2.getVeniceData();

            assertEquals(3L, Coerce
                                .toVncLong(data2.get(new VncKeyword("queue-count")))
                                .toJavaLong());

            assertEquals(1L, Coerce
                                .toVncLong(data2.get(new VncKeyword("temp-queue-count")))
                                .toJavaLong());

            assertEquals(0L, Coerce
                                .toVncLong(data2.get(new VncKeyword("temp-queue-this-client-count")))
                                .toJavaLong());

            client1.close();

            sleep(100);

            // client 2 (after closing client 1) => removed temporary queue

            final IMessage response3 = client2.sendMessage(request);

            final VncMap data3 = (VncMap)response3.getVeniceData();

            assertEquals(2L, Coerce
                                .toVncLong(data3.get(new VncKeyword("queue-count")))
                                .toJavaLong());

            assertEquals(0L, Coerce
                                .toVncLong(data3.get(new VncKeyword("temp-queue-count")))
                                .toJavaLong());

            assertEquals(0L, Coerce
                                .toVncLong(data3.get(new VncKeyword("temp-queue-this-client-count")))
                                .toJavaLong());

            sleep(50);
        }
        finally {
            client2.close();

            sleep(100);

            server.close();
        }
    }

    @Test
    public void test_temp_queue_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);
        final TcpClient client2 = new TcpClient(33333);

        server.start();

        sleep(300);

        client1.open();
        client2.open();

        try {
            final String tmpQueue = client1.createTemporaryQueue(10);
            assertNotNull(tmpQueue);

            final IMessage m1 = client1.offer(
                                    MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Hello!"),
                                    tmpQueue, null,
                                    1_000);

            assertNotNull(m1);
            assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            assertEquals("queue-test",       m1.getTopic());


            final IMessage m2 = client2.poll(tmpQueue, 1_000);

            assertNotNull(m2);
            assertEquals(ResponseStatus.OK,  m2.getResponseStatus());
            assertEquals("queue-test",       m2.getTopic());
            assertEquals("text/plain",       m2.getMimetype());
            assertEquals("UTF-8",            m2.getCharset());
            assertEquals("Hello!",           m2.getText());

            sleep(200);
        }
        finally {
            client2.close();
            client1.close();

            sleep(300);

            server.close();
        }
    }


    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception ignore) {
        }
    }
}
