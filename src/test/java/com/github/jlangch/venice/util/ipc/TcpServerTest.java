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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.BindException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.EofException;
import com.github.jlangch.venice.VncException;


public class TcpServerTest {

    @Test
    public void test_start_stop() throws Exception {
        final TcpServer server = new TcpServer(33333);

        final Function<IMessage,IMessage> handler = req -> null;

        server.start(handler);

        sleep(300);

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

            sleep(300);

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
    public void test_echo_server_client_abort() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<IMessage,IMessage> echoHandler = req -> { sleep(1000); return req; };

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.hello();

            // the server waits 1000ms with replying on the received request
            final Future<IMessage> future = client.sendMessageAsync(request);

            sleep(100);
            client.close();

            future.get();

            fail("Expected exception");
        }
        catch(ExecutionException ex) {
            assertTrue(ex.getCause() instanceof VncException);
        }
        finally {
            server.close();
        }
    }

    @Test
    public void test_echo_server_server_abort() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start(TcpServer.echoHandler());

        sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.hello();

            IMessage response = client.sendMessage(request);
            assertNotNull(response);

            server.close();
            sleep(500);
            assertFalse(server.isRunning());

            // this will cause a EofException or a VncException "broken pipe"
            response = client.sendMessage(request);

            fail("should not reach here");
        }
        catch(EofException ex) {
            assertTrue(true);
        }
        catch(VncException ex) {
            assertTrue(true);
        }
        finally {
            client.close();
        }
    }

    @Test
    public void test_server_status() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start(TcpServer.echoHandler());

        sleep(300);

        client.open();

        try {
            final IMessage request1 = MessageFactory.text("hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response1 = client.sendMessage(request1);
            assertEquals(Status.RESPONSE_OK, response1.getStatus());

            final IMessage request2 = MessageFactory.text("server/status", "text/plain", "UTF-8", "");

            final IMessage response2 = client.sendMessage(request2);
            assertEquals(Status.RESPONSE_OK, response2.getStatus());
            assertEquals("server/status", response2.getTopic());

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

        sleep(300);

        client.open();

        try {
            final IMessage request1 = MessageFactory.text("hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response1 = client.sendMessage(request1);
            assertEquals(Status.RESPONSE_OK, response1.getStatus());

            final IMessage request2 = MessageFactory.text("server/thread-pool-statistics", "text/plain", "UTF-8", "");

            final IMessage response2 = client.sendMessage(request2);
            assertEquals(Status.RESPONSE_OK, response2.getStatus());
            assertEquals("server/thread-pool-statistics", response2.getTopic());

            // System.out.println(response2.getText());
        }
        finally {
            client.close();
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
