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

import static com.github.jlangch.venice.impl.util.CollectionUtil.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpPubSubEncryptedTest {

    @Test
    public void test_pub_sub_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient clientSub = new TcpClient(33333);
        final TcpClient clientPub = new TcpClient(33333);

        server.start();

        IO.sleep(300);

        clientSub.setEncryption(true);
        clientPub.setEncryption(true);

        clientSub.open();
        clientPub.open();

        final List<IMessage> subMessages = new ArrayList<>();

        try {
            clientSub.subscribe("test", m -> subMessages.add(m));

            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;
                final IMessage request = MessageFactory.text(null, "test", "text/plain", "UTF-8", msg);
                clientPub.publish(request);
            }

            IO.sleep(200);
        }
        finally {
            clientPub.close();
            clientSub.close();

            IO.sleep(300);

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

        server.start();

        IO.sleep(300);

        clientPub.setEncryption(true);
        clientSub1.setEncryption(true);
        clientSub2.setEncryption(true);
        clientSub3.setEncryption(true);

        clientSub1.open();
        clientSub2.open();
        clientSub3.open();
        clientPub.open();

        final List<IMessage> subMessages1 = new ArrayList<>();
        final List<IMessage> subMessages2 = new ArrayList<>();
        final List<IMessage> subMessages3 = new ArrayList<>();

        try {
            clientSub1.subscribe("alpha", m -> subMessages1.add(m));

            clientSub2.subscribe(toSet("alpha", "beta"), m -> subMessages2.add(m));

            clientSub3.subscribe("gamma", m -> subMessages3.add(m));

            // 10x 'alpha'
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello alpha " + ii;
                final IMessage request = MessageFactory.text(null, "alpha", "text/plain", "UTF-8", msg);
                clientPub.publish(request);
            }

            // 5 'beta'
            for(int ii=0; ii<5; ii++) {
                final String msg = "Hello beta " + ii;
                final IMessage request = MessageFactory.text(null, "beta", "text/plain", "UTF-8", msg);
                clientPub.publish(request);
            }

            IO.sleep(200);

            assertEquals(18, server.getStatistics().getMessageCount());
            assertEquals(25, server.getStatistics().getPublishCount());
            assertEquals( 0, server.getStatistics().getDiscardedPublishCount());
            assertEquals( 0, server.getStatistics().getDiscardedResponseCount());
        }
        finally {
            clientPub.close();
            clientSub1.close();
            clientSub2.close();
            clientSub3.close();

            IO.sleep(300);

            server.close();
        }

        assertEquals(10, subMessages1.size());
        assertEquals(15, subMessages2.size());
        assertEquals( 0, subMessages3.size());

        for(int ii=0; ii<10; ii++) {
            assertEquals("Hello alpha " + ii, subMessages1.get(ii).getText());
        }

        for(int ii=0; ii<10; ii++) {
            assertEquals("Hello alpha " + ii, subMessages2.get(ii).getText());
        }
        for(int ii=0; ii<5; ii++) {
            assertEquals("Hello beta " + ii, subMessages2.get(ii + 10).getText());
        }
    }
}
