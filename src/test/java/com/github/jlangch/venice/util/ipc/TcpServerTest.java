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
import static org.junit.jupiter.api.Assertions.fail;

import java.net.BindException;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.ipc.impl.Messages;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpServerTest {

    @Test
    public void test_start_stop() throws Exception {
        final TcpServer server = new TcpServer(33333);

        final Function<IMessage,IMessage> handler = req -> null;

        server.start(handler);

        IO.sleep(300);

        assertTrue(server.isRunning());

        server.close();

        assertFalse(server.isRunning());
    }

    @Test
    public void test_start_stop_err() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpServer server2 = new TcpServer(33333);

        final Function<IMessage,IMessage> handler = req -> null;

        try {
            server.start(handler);

            IO.sleep(300);

            assertTrue(server.isRunning());

            // try to start a 2nd server on the same port -> expecting BindException, port already in use!
            try {
                server2.start(handler);
                fail();
            }
            catch(VncException ex) {
                assertTrue(ex.getCause() instanceof BindException);
            }
        }
        finally {
            server.close();
            server2.close();
            assertFalse(server.isRunning());
        }
    }

    @Test
    public void test_client_without_server() throws Exception {
        try {
            final TcpClient client = new TcpClient(33333);
            client.open();

            fail("Expected exception");

            client.close();
        }
        catch(VncException ex) {
        }
    }

    @Test
    public void test_client_abort() {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<IMessage,IMessage> echoHandler = req -> { IO.sleep(1000); return req; };

        server.start(echoHandler);

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.hello();

            // this will abort the next message send/receive
            Thread th = new Thread(() -> { try { IO.sleep(200); client.close();} catch (Exception ex) {} });
            th.start();

            // the server waits 1000ms with replying on the received request
            client.sendMessage(request);

            fail("Expected exception");
        }
        catch(Exception ex) {
            // OK
            assertTrue(true);
        }
        finally {
            try { server.close(); } catch(Exception ex) {}
        }
    }

    @Test
    public void test_echo_server_server_abort() {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start(TcpServer.echoHandler());

        IO.sleep(300);

        client.open();

        final IMessage request = MessageFactory.hello();

        // Phase 1: send a message => OK
        try {
            IMessage response = client.sendMessage(request);
            assertNotNull(response);
        }
        catch(Exception ex) {
            fail("No exception expected yet");
        }

        // Phase 2: close server
        try { server.close(); } catch(Exception ex) {}
        IO.sleep(500);
        assertFalse(server.isRunning());

        // Phase 3: send a message => FAIL
        try {
            // this will cause a EofException or a VncException "broken pipe"
            client.sendMessage(request);

            fail("should not reach here");
        }
        catch(Exception ex) {
            assertTrue(true);
        }

        // finally close the client
        try { client.close(); } catch(Exception ex) {}
    }

    @Test
    public void test_server_status() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start(TcpServer.echoHandler());

        IO.sleep(300);

        client.open();

        try {
            final IMessage request1 = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response1 = client.sendMessage(request1);
            assertEquals(ResponseStatus.OK, response1.getResponseStatus());

            final IMessage request2 = MessageFactory.text(null, Messages.TOPIC_SERVER_STATUS, "text/plain", "UTF-8", "");

            final IMessage response2 = client.sendMessage(request2);
            assertEquals(ResponseStatus.OK, response2.getResponseStatus());
            assertEquals(Messages.TOPIC_SERVER_STATUS, response2.getTopic());

            // System.out.println(response2.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_server_threadpool_stats() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start(TcpServer.echoHandler());

        IO.sleep(300);

        client.open();

        try {
            final IMessage request1 = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response1 = client.sendMessage(request1);
            assertEquals(ResponseStatus.OK, response1.getResponseStatus());

            final IMessage request2 = MessageFactory.text(null, Messages.TOPIC_SERVER_THREAD_POOL_STATS, "text/plain", "UTF-8", "");

            final IMessage response2 = client.sendMessage(request2);
            assertEquals(ResponseStatus.OK, response2.getResponseStatus());
            assertEquals(Messages.TOPIC_SERVER_THREAD_POOL_STATS, response2.getTopic());

            // System.out.println(response2.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_server_max_size() throws Exception {
        try (TcpServer server = new TcpServer(33333)) {
            assertEquals(Messages.MESSAGE_LIMIT_MAX, server.getMaxMessageSize());

            // below minimum
            server.setMaxMessageSize(100L);
            assertEquals(Messages.MESSAGE_LIMIT_MIN, server.getMaxMessageSize());

            // in range
            server.setMaxMessageSize(100L * 1024L);
            assertEquals(100L * 1024L, server.getMaxMessageSize());

            // above maximum
            server.setMaxMessageSize(800L * 1024L * 1024L);
            assertEquals(Messages.MESSAGE_LIMIT_MAX, server.getMaxMessageSize());
        }
    }

    @Test
    public void test_queues() throws Exception {
        try (TcpServer server = new TcpServer(33333)) {
            server.createQueue("queue/1", 10, true, false);
            server.createQueue("queue/2", 10, true, false);

            assertTrue(server.existsQueue("queue/1"));
            assertTrue(server.existsQueue("queue/2"));

            server.removeQueue("queue/1");

            assertFalse(server.existsQueue("queue/1"));
            assertTrue(server.existsQueue("queue/2"));

            server.removeQueue("queue/2");

            assertFalse(server.existsQueue("queue/1"));
            assertFalse(server.existsQueue("queue/2"));
        }
    }
}
