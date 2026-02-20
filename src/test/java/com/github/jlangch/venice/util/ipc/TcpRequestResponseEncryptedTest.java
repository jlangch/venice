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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpRequestResponseEncryptedTest {

    @Test
    public void test_echo_server_text() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                                .builder()
                                                .conn(33333)
                                                .encrypt(true)
                                                .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response = client.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals(request.getSubject(),   response.getSubject());
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
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final byte[] data = new byte[] {0,1,2,3};

            final IMessage request = MessageFactory.binary(null, "hello", "application/octet", data);

            final IMessage response = client.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals("hello",                response.getSubject());
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
    public void test_echo_server_text_enforced_encryption() throws Exception {
        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            final IMessage response = client.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals(request.getSubject(),   response.getSubject());
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
    public void test_echo_server_binary_enforced_encryption() throws Exception {
        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final byte[] data = new byte[] {0,1,2,3};

            final IMessage request = MessageFactory.binary(null, "hello", "application/octet", data);

            final IMessage response = client.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals("hello",                response.getSubject());
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
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final byte[] data = new byte[] {0,1,2,3};

            final IMessage request = MessageFactory.binary(null, "hello", "application/octet", data);

            final IMessage response = client.sendMessage(request, "echo");

            // modify the request binary data to verify that the data buffer
            // is not looped through to the response
            data[0] = 15;
            data[1] = 15;
            data[2] = 15;
            data[3] = 15;

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,         response.getResponseStatus());
            assertEquals("hello",                   response.getSubject());
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
    public void test_echo_server_multiple_messages() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String topic = "hello";
                final String mimetype = "text/plain";
                final String charset = "UTF-8";
                final String msg = "Hello " + ii;

                final IMessage request = MessageFactory.text(null, topic, mimetype, charset, msg);

                final IMessage response = client.sendMessage(request, "echo");

                assertNotNull(response);
                assertEquals(ResponseStatus.OK,      response.getResponseStatus());
                assertEquals(topic,                  response.getSubject());
                assertEquals(mimetype,               response.getMimetype());
                assertEquals(charset,                response.getCharset());
                assertEquals(msg,                    response.getText());
            }
        }
        finally {
            client.close();

            IO.sleep(300);

            server.close();
        }
    }


    @Test
    public void test_echo_server_multiple_messages_oneway() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                final String msg = "Hello " + ii;

                final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", msg);

                // one way message -> no response
                client.sendMessageOneway(request, "echo");
            }
        }
        finally {
            client.close();

            IO.sleep(300);

            server.close();
        }
    }


    @Test
    public void test_echo_server_multiple_messages_mixed() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        client.open();

        try {
            for(int ii=0; ii<10; ii++) {
                client.sendMessageOneway(
                    MessageFactory.text(null, "hello" + ii, "text/plain", "UTF-8", "Hello " + ii),
                    "echo");

                final IMessage r1 = client.sendMessage(
                                        MessageFactory.text(null, "hello" + ii, "text/plain", "UTF-8", "Hello-2 " + ii),
                                        "echo");
                assertEquals(ResponseStatus.OK, r1.getResponseStatus());
                assertEquals("Hello-2 " + ii, r1.getText());

                final IMessage r2 = client.sendMessage(
                                        MessageFactory.text(null, "hello" + ii, "text/plain", "UTF-8", "Hello-3 " + ii),
                                        "echo");
                assertEquals("Hello-3 " + ii, r2.getText());
            }
        }
        finally {
            client.close();

            IO.sleep(300);

            server.close();
        }
    }

    @Test
    public void test_echo_server_multiple_clients() throws Exception {
        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            // increase connections to support the test client count
                                            .maxParallelConnections(50)
                                            .build());

        final int clients = 10;
        final int messagesPerClient = 25;

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        final ClientConfig clientCfg = ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build();

        try {
            final ThreadPoolExecutor es = (ThreadPoolExecutor)Executors.newCachedThreadPool();
            es.setMaximumPoolSize(clients);

            final AtomicLong errors = new AtomicLong();

            final List<Future<?>> futures = new ArrayList<>();

            for(int cc=1; cc<=clients; cc++) {
                final int clientNr = cc;

                // run each client test as future
                futures.add(es.submit(() -> {
                    final Client client = Client.of(clientCfg);

                    try {
                        client.open();

                        for(int msgIdx=1; msgIdx<=messagesPerClient; msgIdx++) {
                            final String subject = "hello";
                            final String mimetype = "text/plain";
                            final String charset = "UTF-8";
                            final String msg = "Hello " + clientNr + " / " + msgIdx;

                            final IMessage request = MessageFactory.text(null, subject, mimetype, charset, msg);

                            try {
                                final IMessage response = client.sendMessage(request, "echo");

                                assertNotNull(response);
                                assertEquals(ResponseStatus.OK,  response.getResponseStatus());
                                assertEquals(subject,            response.getSubject());
                                assertEquals(mimetype,           response.getMimetype());
                                assertEquals(charset,            response.getCharset());
                                assertEquals(msg,                response.getText());
                            }
                            catch(Exception ex) {
                                System.err.println(String.format(
                                        "Message err: client %d, msg %d/%d: %s",
                                        clientNr, msgIdx, messagesPerClient, ex.getMessage()));
                                errors.incrementAndGet();
                                break;  // to reduce the error count
                            }
                        }
                    }
                    catch(Exception ex) {
                        System.err.println("Client " + clientNr + ": " + ex.getMessage());
                        errors.incrementAndGet();
                    }
                    finally {
                        try { client.close(); } catch (Exception ignore) {}
                    }
                }));
            }

            // wait for all clients to be finished
            futures.forEach(f ->  { try { f.get(); } catch (Exception ignore) {}});

            assertEquals(0, errors.get());
        }
        finally {
            server.close();
        }
    }

    @Test
    public void test_remote_code_execution() throws Exception {
        final Server server = Server.of(33333);
        final Client client = Client.of(ClientConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .build());

        final Venice venice = new Venice();

        final Function<IMessage,IMessage> execHandler = req -> {
            final String code = req.getText();
            final String result = venice.eval(code).toString();
            return MessageFactory.text(
                        null,
                        "venice.response",
                        "text/plain",
                        "UTF-8",
                        result);
        };

        server.createFunction("echo", execHandler);
        server.start();

        IO.sleep(300);

        client.open();

        try {
            final IMessage request = MessageFactory.text(
                                        null,
                                        "venice.request",
                                        "application/venice",
                                        "UTF-8",
                                        "(+ 1 2)");

            final IMessage response = client.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,  response.getResponseStatus());
            assertEquals("venice.response",  response.getSubject());
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
