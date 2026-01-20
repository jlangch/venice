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


public class TcpOfferPollEncryptedTest {

    @Test
    public void test_queue() throws Exception {
        final TcpServer server = TcpServer.of(33333);
        final Client client1 = Client.of(ClientConfig
                                                    .builder()
                                                    .conn(33333)
                                                    .encrypt(true)
                                                    .build());
        final Client client2 = Client.of(ClientConfig
                                                    .builder()
                                                    .conn(33333)
                                                    .encrypt(true)
                                                    .build());

        server.createQueue("queue-1", 10, true, false);

        server.start();

        IO.sleep(300);

        client1.open();
        client2.open();

        try {
            final IMessage m1 = client1.offer(
                                    MessageFactory.text(null, "queue-test", "text/plain", "UTF-8", "Hello!"),
                                    "queue-1",
                                    null,
                                    1_000);

            assertNotNull(m1);
            assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            assertEquals("queue-test",       m1.getTopic());


            final IMessage m2 = client2.poll("queue-1", 1000);

            assertNotNull(m2);
            assertEquals(ResponseStatus.OK,  m2.getResponseStatus());
            assertEquals("queue-test",       m2.getTopic());
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
