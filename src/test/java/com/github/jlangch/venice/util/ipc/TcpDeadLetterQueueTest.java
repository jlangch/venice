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

import com.github.jlangch.venice.util.ipc.impl.ServerQueueManager;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpDeadLetterQueueTest {

    @Test
    public void test_dlq() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("tom", "123");
        authenticator.addCredentials("admin", "123", true);

        try (final Server server = Server.of(ServerConfig
                                                .builder()
                                                .conn(33333)
                                                .encrypt(true)
                                                .authenticator(authenticator)
                                                .build());
             final Client client = Client.of(33333)
        ) {
            server.start();

            IO.sleep(300);

            client.open("admin", "123");

            final IMessage m = client.poll(ServerQueueManager.DEAD_LETTER_QUEUE_NAME, 100);

            assertNotNull(m);
            assertEquals(ResponseStatus.QUEUE_EMPTY,  m.getResponseStatus());
        }
    }
}
