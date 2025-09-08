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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class TcpRequestResponseTest {

    @Test
    public void test_echo_server_text() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> req.asEchoResponse();

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            final Message request = Message.text(Status.REQUEST, "hello", "text/plain", "UTF-8", "Hello!");;

            final Message response = client.sendMessage(request);

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,     response.getStatus());
            assertEquals(request.getTimestamp(), response.getTimestamp());
            assertEquals(request.getTopic(),     response.getTopic());
            assertEquals(request.getMimetype(),  response.getMimetype());
            assertEquals(request.getCharset(),   response.getCharset());
            assertEquals(request.getText(),      response.getText());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_echo_server_binary() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> req.asEchoResponse();

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            final byte[] data = new byte[] {0,1,2,3};

            final Message request = Message.binary(Status.REQUEST, "hello", "application/octet", data);

            final Message response = client.sendMessage(request);

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,     response.getStatus());
            assertEquals(request.getTimestamp(), response.getTimestamp());
            assertEquals("hello",                response.getTopic());
            assertEquals("application/octet",    response.getMimetype());
            assertEquals(null,                   response.getCharset());
            assertArrayEquals(data,              response.getData());
        }
        finally {
            client.close();
            server.close();
        }
    }


    @Test
    public void test_echo_server_binary_integrity_check() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> echoHandler = req -> req.asEchoResponse();

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            final byte[] data = new byte[] {0,1,2,3};

            final Message request = Message.binary(Status.REQUEST, "hello", "application/octet", data);

            final Message response = client.sendMessage(request);

            // modify the request binary data to verify that the data buffer
            // is not looped through to the response
            data[0] = 15;
            data[1] = 15;
            data[2] = 15;
            data[3] = 15;

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,        response.getStatus());
            assertEquals(request.getTimestamp(),    response.getTimestamp());
            assertEquals("hello",                   response.getTopic());
            assertEquals("application/octet",       response.getMimetype());
            assertEquals(null,                      response.getCharset());
            assertArrayEquals(new byte[] {0,1,2,3}, response.getData());
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

        final Function<Message,Message> echoHandler = req -> { return req.asEchoResponse(); };

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            final Message request = Message.hello();

            final Future<Message> future = client.sendMessageAsync(request);

            final Message response = future.get();

            assertNotNull(response);
            assertEquals(Status.RESPONSE_OK,     response.getStatus());
            assertEquals(request.getTimestamp(), response.getTimestamp());
            assertEquals(request.getTopic(),     response.getTopic());
            assertEquals(request.getMimetype(),  response.getMimetype());
            assertEquals(request.getCharset(),   response.getCharset());
            assertEquals(request.getText(),      response.getText());
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

        final Function<Message,Message> echoHandler = req -> { return req.asEchoResponse(); };

        server.start(echoHandler);

        sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String topic = "hello";
                final String mimetype = "text/plain";
                final String charset = "UTF-8";
                final String msg = "Hello " + ii;

                final Message request = Message.text(Status.REQUEST, topic, mimetype, charset, msg);

                final Message response = client.sendMessage(request);

                assertNotNull(response);
                assertEquals(Status.RESPONSE_OK,     response.getStatus());
                assertEquals(request.getTimestamp(), response.getTimestamp());
                assertEquals(topic,                  response.getTopic());
                assertEquals(mimetype,               response.getMimetype());
                assertEquals(charset,                response.getCharset());
                assertEquals(msg,                    response.getText());
            }
        }
        finally {
            client.close();

            sleep(300);

            server.close();
        }
    }


    @Test
    public void test_echo_server_multiple_messages_oneway() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        final Function<Message,Message> handler = req -> { return null; };

        server.start(handler);

        sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;

                final Message request = Message.text(Status.REQUEST_ONE_WAY, "hello", "text/plain", "UTF-8", msg);

                // one way message -> no response
                client.sendMessage(request);
            }
        }
        finally {
            client.close();

            sleep(300);

            server.close();
        }
    }

    @Test
    public void test_echo_server_multiple_clients() throws Exception {

        final TcpServer server = new TcpServer(33333);

        // increase connections to support the test client count
        server.setMaximumParallelConnections(50);

        final int clients = 30;
        final int messagesPerClient = 25;

        final Function<Message,Message> echoHandler = req -> { return req.asEchoResponse(); };

        server.start(echoHandler);

        sleep(300);

        try {
            final ThreadPoolExecutor es = (ThreadPoolExecutor)Executors.newCachedThreadPool();

            final List<Future<?>> futures = new ArrayList<>();
            for(int cc=0; cc<clients; cc++) {
                final int clientNr = cc;

                futures.add(es.submit(() -> {
                    final TcpClient client = new TcpClient(33333);
                    try {
                        client.open();

                        for(int msgIdx=0; msgIdx<messagesPerClient; msgIdx++) {
                            final String topic = "hello";
                            final String mimetype = "text/plain";
                            final String charset = "UTF-8";
                            final String msg = "Hello " + clientNr + " / " + msgIdx;

                            final Message request = Message.text(Status.REQUEST, topic, mimetype, charset, msg);

                            final Message response = client.sendMessage(request);

                            assertNotNull(response);
                            assertEquals(Status.RESPONSE_OK, response.getStatus());
                            assertEquals(topic,              response.getTopic());
                            assertEquals(mimetype,           response.getMimetype());
                            assertEquals(charset,            response.getCharset());
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

        sleep(300);

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


    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception ignore) {
        }
    }
}
