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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpAuthenticationTest {

    @Test
    public void test_auth_ok() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("tom", "123");
        authenticator.addCredentials("admin", "123", true);

        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(authenticator)
                                            .build());

        final Client clientTom = Client.of(33333);
        final Client clientAdmin = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        clientTom.open("tom", "123");
        clientAdmin.open("admin", "123");

        try {
            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            IMessage response = clientTom.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals(request.getSubject(),   response.getSubject());
            assertEquals(request.getMimetype(),  response.getMimetype());
            assertEquals(request.getCharset(),   response.getCharset());
            assertEquals(request.getText(),      response.getText());


            response = clientAdmin.sendMessage(request, "echo");

            assertNotNull(response);
            assertEquals(ResponseStatus.OK,      response.getResponseStatus());
            assertEquals(request.getSubject(),   response.getSubject());
            assertEquals(request.getMimetype(),  response.getMimetype());
            assertEquals(request.getCharset(),   response.getCharset());
            assertEquals(request.getText(),      response.getText());
        }
        finally {
            clientTom.close();
            clientAdmin.close();
            server.close();
        }
    }

    @Test
    public void test_auth_failure_1() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("usr-1", "test-1");
        authenticator.addCredentials("usr-2", "test-2");

        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(authenticator)
                                            .build());

        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        try {
            client.open("usr-1", "bad-password");

            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            client.sendMessage(request, "echo");

            fail("Should not reach here");
        }
        catch(IpcException ex) {
            IpcException cause = (IpcException)ex.getCause();
            assertEquals("Authentication failure! Bad user credentials!", cause.getMessage());
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_auth_failure_2() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("usr-1", "test-1");
        authenticator.addCredentials("usr-2", "test-2");

        final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(authenticator)
                                            .build());

        final Client client = Client.of(33333);

        server.createFunction("echo", Server.echoHandler());
        server.start();

        IO.sleep(300);

        try {
            client.open();  // open without authentication => error

            final IMessage request = MessageFactory.text(null, "hello", "text/plain", "UTF-8", "Hello!");

            client.sendMessage(request, "echo");

            fail("Should not reach here");
        }
        catch(IpcException ex) {
            IpcException cause = (IpcException)ex.getCause();
            assertEquals(
                "The IPC server requires authentication! Please pass a user name and a "
                + "password for opening an IPC client!",
                cause.getMessage());
        }
        finally {
            client.close();
            server.close();
        }
    }
}
