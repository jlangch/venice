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

import com.github.jlangch.venice.impl.util.junit.EnableOnMac;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpRequestResponseLoadTest {

    @Test
    @EnableOnMac
    public void test_load1() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            for(int ii=0; ii<10_000; ii++) {
                final IMessage response = client.sendMessage(request, "echo");

                validateResponse(request, response);
            }


            assertEquals(10_000L, client.getMessageSendCount());
            assertEquals(10_000L, client.getMessageReceiveCount());

            assertEquals(1L, server.getStatistics().getConnectionCount());

            assertEquals(10_000L, server.getStatistics().getMessageCount());
            assertEquals(0L, server.getStatistics().getPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedResponseCount());
       }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    @EnableOnMac
    public void test_load2() throws Exception {
        final Server server = Server.of(33333);
        final Client client1 = Client.of(33333);
        final Client client2 = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client1.open();
        client2.open();

        try {
            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            for(int ii=0; ii<5_000; ii++) {
                final IMessage response1 = client1.sendMessage(request, "echo");
                final IMessage response2 = client2.sendMessage(request, "echo");

                validateResponse(request, response1);
                validateResponse(request, response2);
            }


            assertEquals(5_000L, client1.getMessageSendCount());
            assertEquals(5_000L, client1.getMessageReceiveCount());

            assertEquals(5_000L, client2.getMessageSendCount());
            assertEquals(5_000L, client2.getMessageReceiveCount());

            assertEquals(2L, server.getStatistics().getConnectionCount());

            assertEquals(10_000L, server.getStatistics().getMessageCount());
            assertEquals(0L, server.getStatistics().getPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedResponseCount());
       }
        finally {
            client1.close();
            client2.close();
            server.close();
        }
    }

    @Test
    @EnableOnMac
    public void test_load_oneway() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.text(null, "hello-onway", "text/plain", "UTF-8", "Hello!");

            for(int ii=0; ii<10_000; ii++) {
                client.sendMessageOneway(request, "echo");
            }

            // send a final message with a response to guarantee
            // that server has processed all oneway messages before
            // and then do the count checks

            final IMessage finalRequest = MessageFactory.text(null, "hello-final", "text/plain", "UTF-8", "Hello!");
            final IMessage response = client.sendMessage(finalRequest, "echo");
            validateResponse(finalRequest, response);


            assertEquals(10_001L, client.getMessageSendCount());
            assertEquals(1L, client.getMessageReceiveCount());

            assertEquals(1L, server.getStatistics().getConnectionCount());

            assertEquals(10_001L, server.getStatistics().getMessageCount());
            assertEquals(0L, server.getStatistics().getPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedPublishCount());
            assertEquals(0L, server.getStatistics().getDiscardedResponseCount());
        }
        finally {
            client.close();
            server.close();
        }
    }


    private void validateResponse(final IMessage request, final IMessage response) {
        assertNotNull(response);
        assertEquals(ResponseStatus.OK,      response.getResponseStatus());
        assertEquals(request.getSubject(),   response.getSubject());
        assertEquals(request.getMimetype(),  response.getMimetype());
        assertEquals(request.getCharset(),   response.getCharset());
        assertEquals(request.getText(),      response.getText());
    }
}
