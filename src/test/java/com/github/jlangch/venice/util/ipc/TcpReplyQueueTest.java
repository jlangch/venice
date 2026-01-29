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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpReplyQueueTest {

    @Test
    public void test_reply_queue() throws Exception {
        final Server server = Server.of(33333);
        final Client client1 = Client.of(33333);
        final Client client2 = Client.of(33333);

        server.createQueue("queue/1", 10, true, false);

        server.start();

        IO.sleep(100);

        client1.open();
        client2.open();

        try {
            final String tmpQueue = client1.createTemporaryQueue(10);
            assertNotNull(tmpQueue);


            // client1: offer message to "queue/1" with reply-to queue

            final IMessage m1 = client1.offer(
                                    MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Hello!"),
                                    "queue/1", tmpQueue,
                                    1_000);

            assertNotNull(m1);
            assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            assertEquals("queue-test",       m1.getSubject());


            // client2: poll message from "queue/1"

            final IMessage m2 = client2.poll("queue/1", 1_000);

            assertNotNull(m2);
            assertEquals(ResponseStatus.OK,  m2.getResponseStatus());
            assertEquals("queue-test",       m2.getSubject());
            assertEquals("text/plain",       m2.getMimetype());
            assertEquals("UTF-8",            m2.getCharset());
            assertEquals("Hello!",           m2.getText());


            // client2: offer reply message to reply-to queue

            final IMessage m3 = client2.offer(
                                    MessageFactory.text("1", "queue-test", "text/plain", "UTF-8", "Good-By!"),
                                    m2.getReplyToQueueName(), null,
                                    1_000);

            assertNotNull(m3);
            assertEquals(ResponseStatus.OK,  m3.getResponseStatus());
            assertEquals("queue-test",       m3.getSubject());


            // client1: poll message from reply queue

            final IMessage m4 = client1.poll(tmpQueue, 1_000);

            assertNotNull(m4);
            assertEquals(ResponseStatus.OK,  m4.getResponseStatus());
            assertEquals("queue-test",       m4.getSubject());
            assertEquals("text/plain",       m4.getMimetype());
            assertEquals("UTF-8",            m4.getCharset());
            assertEquals("Good-By!",         m4.getText());

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
