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


public class TcpAuthorizationAclFunctionTest {

    @Test
    public void test_function_non_authenticated() throws Exception {
        try (final Server server = Server.of(33333);
             final Client client = Client.of(33333);
        ) {
            server.createFunction("echo", m -> m);

            server.start();

            IO.sleep(300);

            client.open();


            final IMessage m = MessageFactory.text(null, "test", "text/plain", "UTF-8", "Hello!");

            // No authentication:  any user can send messages
            try {
                final IMessage m1 = client.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }
        }
    }

    @Test
    public void test_function_authenticated_no_acl() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("tom", "123");
        authenticator.addCredentials("admin", "123", true);

        try (final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(authenticator)
                                            .build());
             final Client clientTom = Client.of(33333);
             final Client clientAdmin = Client.of(33333)
        ) {
            server.createFunction("echo", m -> m);

            server.start();

            IO.sleep(300);

            clientTom.open("tom", "123");
            clientAdmin.open("admin", "123");

            final IMessage m = MessageFactory.text(null, "test", "text/plain", "UTF-8", "Hello!");

            // With authentication and no ACL:  admin user can send messages
            try {
                final IMessage m1 = clientAdmin.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }

            // With authentication and no ACL:  non admin user can send messages
            try {
                final IMessage m1 = clientTom.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }
        }
    }

    @Test
    public void test_function_authenticated_with_acl() throws Exception {
        final Authenticator authenticator = new Authenticator(true);
        authenticator.addCredentials("tom", "123");
        authenticator.addCredentials("jak", "123");
        authenticator.addCredentials("admin", "123", true);

        authenticator.setFunctionDefaultAcl(AccessMode.DENY);
        authenticator.setFunctionAcl("echo", AccessMode.EXECUTE, "tom");

        try (final Server server = Server.of(ServerConfig
                                            .builder()
                                            .conn(33333)
                                            .encrypt(true)
                                            .authenticator(authenticator)
                                            .build());
             final Client clientTom = Client.of(33333);
             final Client clientJak = Client.of(33333);
             final Client clientAdmin = Client.of(33333)
        ) {
            server.createFunction("echo", m -> m);

            server.start();

            IO.sleep(300);

            clientTom.open("tom", "123");
            clientJak.open("jak", "123");
            clientAdmin.open("admin", "123");

            final IMessage m = MessageFactory.text(null, "test", "text/plain", "UTF-8", "Hello!");

            // With authentication and ACL:  admin user can send messages
            try {
                final IMessage m1 = clientAdmin.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }


            // With authentication and ACL:  non admin user with ACL can send messages
            try {
                final IMessage m1 = clientTom.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.OK,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }


            // With authentication and ACL:  non admin user without ACL cannot send messages
            try {
                final IMessage m1 = clientJak.sendMessage(m, "echo");

                assertNotNull(m1);
                assertEquals(ResponseStatus.NO_PERMISSION,  m1.getResponseStatus());
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }
        }
    }
}
