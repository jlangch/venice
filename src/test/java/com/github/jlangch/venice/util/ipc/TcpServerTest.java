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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;


public class TcpServerTest {

    @Test
    public void test_start_stop() throws Exception {
        final TcpServer server = new TcpServer(33333);

        final Function<Message,Message> handler = req -> { return null; };

        server.start(handler);

        Thread.sleep(300);

        assertTrue(server.isRunning());

        server.close();

        assertFalse(server.isRunning());
    }

    @Test
    public void test_start_stop_err() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpServer server2 = new TcpServer(33333);

        final Function<Message,Message> handler = req -> { return null; };

        try {
            server.start(handler);

            Thread.sleep(300);

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
    public void test_echo_server_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            final Message request = Message.hello();

            final Message response = client.sendMessage(request);

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,    response.getStatus());
            assertEquals(request.getTopic(),    response.getTopic());
            assertEquals(request.getMimetype(), response.getMimetype());
            assertEquals(request.getCharset(),  response.getCharset());
            assertEquals(request.getText(),     response.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_echo_server_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            final Message request = Message.echo();

            final Message response = client.sendMessage(request);

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK, response.getStatus());
            assertEquals("echo",             response.getTopic());
            assertEquals("text/plain",       response.getMimetype());
            assertEquals("UTF-8",            response.getCharset());
            assertEquals("Hello!",           response.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_async() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            final Message request = Message.hello();

            final Future<Message> future = client.sendMessageAsync(request);

            final Message response = future.get();

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,    response.getStatus());
            assertEquals(request.getTopic(),    response.getTopic());
            assertEquals(request.getMimetype(), response.getMimetype());
            assertEquals(request.getCharset(),  response.getCharset());
            assertEquals(request.getText(),     response.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_echo_server_multiple_messages() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;

                final Message request = Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", msg);

                final Message response = client.sendMessage(request);

                assertNotNull(response);
                assertEquals(Status.RESPONSE_OK, response.getStatus());
                assertEquals("hello",            response.getTopic());
                assertEquals("text/plain",       response.getMimetype());
                assertEquals("UTF-8",            response.getCharset());
                assertEquals(msg,                response.getText());
            }
        }
        finally {
            client.close();

            Thread.sleep(300);

            server.close();
        }
    }


    @Test
    public void test_echo_server_multiple_messages_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;

                final Message request = Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", msg);

                client.sendMessageOneWay(request);

                final Message response = client.receiveMessageAsync().get();

                assertNotNull(response);
                assertEquals(Status.RESPONSE_OK, response.getStatus());
                assertEquals("hello",            response.getTopic());
                assertEquals("text/plain",       response.getMimetype());
                assertEquals("UTF-8",            response.getCharset());
                assertEquals(msg,                response.getText());
            }
        }
        finally {
            client.close();

            Thread.sleep(300);

            server.close();
        }
    }

    @Test
    public void test_echo_server_multiple_messages_without_response() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> handler = req -> { return null; };

        server.start(handler);

        Thread.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;

                final Message request = Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", msg);

                client.sendMessageOneWay(request);
            }
        }
        finally {
            client.close();

            Thread.sleep(300);

            server.close();
        }
    }

    @Test
    public void test_echo_server_multiple_clients() throws Exception {

        final TcpServer server = new TcpServer(33333);

        final Function<Message,Message> echoHandler = req -> { return req.asEcho(); };

        server.start(echoHandler);

        Thread.sleep(300);

        try {
            final ThreadPoolExecutor es = (ThreadPoolExecutor)Executors.newCachedThreadPool();

            // start 10 clients (the server supports up to 20 parallel connections as default)
            final List<Future<?>> futures = new ArrayList<>();
            for(int cc=0; cc<10; cc++) {
                final int clientNr = cc;
                futures.add(es.submit(() -> {
                    final TcpClient client = new TcpClient(33333);
                    try {
                        client.open();

                        // 10 messages per client
                        for(int ii=0; ii<10; ii++) {
                            final String msg = "Hello " + clientNr + " / " + ii;

                            final Message request = Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", msg);

                            final Message response = client.sendMessage(request);

                            assertNotNull(response);
                            assertEquals(Status.RESPONSE_OK, response.getStatus());
                            assertEquals("hello",            response.getTopic());
                            assertEquals("text/plain",       response.getMimetype());
                            assertEquals("UTF-8",            response.getCharset());
                            assertEquals(msg,                response.getText());

                            // synchronized (server) { System.out.println(msg); }
                        }
                    }
                    finally {
                        try { client.close(); } catch (Exception ignore) {}
                    }
                }));
            }

            // wait for all clients to be finished
            futures.forEach(f ->  { try { f.get(); } catch (Exception ignore) {}});
        }
        finally {
            server.close();
        }
    }

    @Test
    public void test_remote_code_execution() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Venice venice = new Venice();

        final Function<Message,Message> echoHandler = req -> {
            final String result = venice.eval(req.getText()).toString();
            return Message.text(
                        Status.RESPONSE_OK,
                        "venice.response",
                        "text/plain",
                        "UTF-8",
                        result);
        };

        server.start(echoHandler);

        Thread.sleep(300);

        client.open();

        try {
            final Message request = Message.text(
                                        Status.REQUEST,
                                        "venice",
                                        "application/venice",
                                        "UTF-8",
                                        "(+ 1 2)");

            final Message response = client.sendMessage(request);

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK, response.getStatus());
            assertEquals("venice.response",  response.getTopic());
            assertEquals("text/plain",       response.getMimetype());
            assertEquals("UTF-8",            response.getCharset());
            assertEquals("3",                response.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

}
