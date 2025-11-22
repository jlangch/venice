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


public class TcpOfferPollTest {

    @Test
    public void test_queue() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);
        final TcpClient client2 = new TcpClient(33333);

        server.createQueue("queue-1", 10);

        server.start();

        sleep(300);

        client1.open();
        client2.open();

        try {
            final IMessage m1 = client1.offer(
                                    MessageFactory.text(null, "queue-test", "text/plain", "UTF-8", "Hello!"),
                                    "queue-1", null,
                                    1_000);

            assertNotNull(m1);
            assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            assertEquals("queue-test",       m1.getTopic());


            final IMessage m2 = client2.poll("queue-1", 1_000);

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

    @Test
    public void test_queue_invalid() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);

        server.createQueue("queue", 3);

        server.start();

        sleep(300);

        client1.open();

        try {
            final IMessage m = MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Hello!");

            final IMessage r = client1.offer(m, "queue-XXX", null, 1_000);

            assertEquals(ResponseStatus.QUEUE_NOT_FOUND, r.getResponseStatus());

            sleep(200);
        }
        finally {
            client1.close();

            sleep(300);

            server.close();
        }
    }

    @Test
    public void test_queue_full() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);

        server.createQueue("queue", 3);

        server.start();

        sleep(300);

        client1.open();

        try {
            final IMessage m1 = MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Hello!");
            final IMessage m2 = MessageFactory.text("2", "queue-test", "text/plain", "UTF-8", "Hello!");
            final IMessage m3 = MessageFactory.text("3", "queue-test", "text/plain", "UTF-8", "Hello!");
            final IMessage m4 = MessageFactory.text("4", "queue-test", "text/plain", "UTF-8", "Hello!");

            final IMessage r1 = client1.offer(m1, "queue", null, 1_000);
            final IMessage r2 = client1.offer(m2, "queue", null, 1_000);
            final IMessage r3 = client1.offer(m3, "queue", null, 1_000);

            final IMessage r4 = client1.offer(m4, "queue", null, 1_000);

            assertEquals(ResponseStatus.OK, r1.getResponseStatus());
            assertEquals(ResponseStatus.OK, r2.getResponseStatus());
            assertEquals(ResponseStatus.OK, r3.getResponseStatus());
            assertEquals(ResponseStatus.QUEUE_FULL, r4.getResponseStatus());

            sleep(200);
        }
        finally {
            client1.close();

            sleep(300);

            server.close();
        }
    }


    @Test
    public void test_queue_empty() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client1 = new TcpClient(33333);

        server.createQueue("queue", 3);

        server.start();

        sleep(300);

        client1.open();

        try {
            final IMessage m1 = MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Hello!");
            final IMessage m2 = MessageFactory.text("2", "queue-test", "text/plain", "UTF-8", "Hello!");

            final IMessage r1 = client1.offer(m1, "queue", null, 1_000);
            final IMessage r2 = client1.offer(m2, "queue", null, 1_000);

            final IMessage r3 = client1.poll("queue", 1_000);
            final IMessage r4 = client1.poll("queue", 1_000);
            final IMessage r5 = client1.poll("queue", 1_000);

            // offer
            assertEquals(ResponseStatus.OK, r1.getResponseStatus());
            assertEquals(ResponseStatus.OK, r2.getResponseStatus());

            // poll
            assertEquals(ResponseStatus.OK, r3.getResponseStatus());
            assertEquals(ResponseStatus.OK, r4.getResponseStatus());
            assertEquals(ResponseStatus.QUEUE_EMPTY, r5.getResponseStatus());

            sleep(200);
        }
        finally {
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
