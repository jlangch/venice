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
package com.github.jlangch.venice.util.ipc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpTempQueueTest {

    @Test
    public void test_temp_queue_1() throws Exception {
        final Authenticator auth = new Authenticator(true);
        auth.addCredentials("u1", "123", true);

        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(auth)
                                            .build());

        final Client client1 = Client.of(33333);
        final Client client2 = Client.of(33333);

        server.createQueue("queue/1", 10, true, false);
        server.createQueue("queue/2", 10, true, false);

        server.start();

        IO.sleep(100);

        client1.open("u1", "123");
        client2.open("u1", "123");

        try {
            final String tmpQueue = client1.createTemporaryQueue(10);
            assertNotNull(tmpQueue);

            assertTrue(client1.existsQueue("queue/1"));
            assertTrue(client1.existsQueue("queue/2"));
            assertFalse(client1.existsQueue("queue/---"));
            assertTrue(client1.existsQueue(tmpQueue));

            assertTrue(client2.existsQueue("queue/1"));
            assertTrue(client2.existsQueue("queue/2"));
            assertFalse(client2.existsQueue("queue/---"));
            assertTrue(client2.existsQueue(tmpQueue));

            // client 1
            final Map<String,Object> status1 = client1.getServerStatus();
            assertEquals(2L, status1.get("queue-count"));
            assertEquals(1L, status1.get("temp-queue-total-count"));
            assertEquals(1L, status1.get("temp-queue-connection-count"));

            // client 2
            final Map<String,Object> status2 = client2.getServerStatus();
            assertEquals(2L, status2.get("queue-count"));
            assertEquals(1L, status2.get("temp-queue-total-count"));
            assertEquals(0L, status2.get("temp-queue-connection-count"));

            client1.close();

            IO.sleep(100);

            // client 2 (after closing client 1) => removed temporary queue
            final Map<String,Object> status2b = client2.getServerStatus();
            assertEquals(2L, status2b.get("queue-count"));
            assertEquals(0L, status2b.get("temp-queue-total-count"));
            assertEquals(0L, status2b.get("temp-queue-connection-count"));

            IO.sleep(50);
        }
        finally {
            client2.close();

            IO.sleep(100);

            server.close();
        }
    }

    @Test
    public void test_temp_queue_2() throws Exception {
        final Server server = Server.of(33333);
        final Client client1 = Client.of(33333);
        final Client client2 = Client.of(33333);

        server.start();

        IO.sleep(300);

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
            assertEquals("queue-test",       m1.getSubject());


            final IMessage m2 = client2.poll(tmpQueue, 1_000);

            assertNotNull(m2);
            assertEquals(ResponseStatus.OK,  m2.getResponseStatus());
            assertEquals("queue-test",       m2.getSubject());
            assertEquals("text/plain",       m2.getMimetype());
            assertEquals("UTF-8",            m2.getCharset());
            assertEquals("Hello!",           m2.getText());

            IO.sleep(200);
        }
        finally {
            client2.close();
            client1.close();

            IO.sleep(300);

            server.close();
        }
    }
}
