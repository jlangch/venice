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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;


public class TcpPubSubTest {

    @Test
    public void test_pub_sub_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient clientSub = new TcpClient(33333);
        final TcpClient clientPub = new TcpClient(33333);

        final Function<Message,Message> serverHandler = req -> { return req.asEchoResponse(); };
        server.start(serverHandler);

        sleep(300);

        clientSub.open();
        clientPub.open();

        final List<Message> subMessages = new ArrayList<>();

        try {
            final Consumer<Message> subHandler = m -> subMessages.add(m);
            clientSub.subscribe("test", subHandler);

            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;
                final Message request = Message.text(Status.REQUEST, "test", "text/plain", "UTF-8", msg);
                clientPub.publish(request);
            }
        }
        finally {
            clientPub.close();
            clientSub.close();

            sleep(300);

            server.close();
        }

        assertEquals(10, subMessages.size());

        for(int ii=0; ii<10; ii++) {
            assertEquals("Hello " + ii, subMessages.get(ii).getText());
        }
    }

    @Test
    public void test_pub_sub_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient clientPub = new TcpClient(33333);
        final TcpClient clientSub1 = new TcpClient(33333);
        final TcpClient clientSub2 = new TcpClient(33333);
        final TcpClient clientSub3 = new TcpClient(33333);

        final Function<Message,Message> serverHandler = req -> { return req.asEchoResponse(); };
        server.start(serverHandler);

        sleep(300);

        clientSub1.open();
        clientSub2.open();
        clientSub3.open();
        clientPub.open();

        final List<Message> subMessages1 = new ArrayList<>();
        final List<Message> subMessages2 = new ArrayList<>();
        final List<Message> subMessages3 = new ArrayList<>();

        try {
            clientSub1.subscribe("test", m -> subMessages1.add(m));
            clientSub2.subscribe("test", m -> subMessages2.add(m));
            clientSub3.subscribe("test", m -> subMessages3.add(m));

            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;
                final Message request = Message.text(Status.REQUEST, "test", "text/plain", "UTF-8", msg);
                clientPub.publish(request);
            }
        }
        finally {
            clientPub.close();
            clientSub1.close();
            clientSub2.close();
            clientSub3.close();

            sleep(300);

            server.close();
        }

        assertEquals(10, subMessages1.size());
        assertEquals(10, subMessages2.size());
        assertEquals(10, subMessages3.size());

        for(int ii=0; ii<10; ii++) {
            assertEquals("Hello " + ii, subMessages1.get(ii).getText());
            assertEquals("Hello " + ii, subMessages2.get(ii).getText());
            assertEquals("Hello " + ii, subMessages3.get(ii).getText());
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
