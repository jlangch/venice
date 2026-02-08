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

import static com.github.jlangch.venice.util.ipc.QueuePersistence.TRANSIENT;
import static com.github.jlangch.venice.util.ipc.QueueType.BOUNDED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpAuthorizationAdminTest {

    @Test
    public void test_queue_mgmt() throws Exception {
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
            server.start();

            IO.sleep(300);

            clientTom.open("tom", "123");
            clientAdmin.open("admin", "123");

            // user with admin role can create queues
            try {
                clientAdmin.createQueue("queue/1", 100, BOUNDED, TRANSIENT);
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }

            // user with admin role can check if queue exists
            try {
                assertTrue(clientAdmin.existsQueue("queue/1"));
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }

            // user with admin role can remove queues
            try {
                clientAdmin.removeQueue("queue/1");
                assertFalse(clientAdmin.existsQueue("queue/1"));
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }



            // user without admin role cannot create queues
            try {
                clientTom.createQueue("queue/2", 100, BOUNDED, TRANSIENT);
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }

            // user without admin role can check if queue exists
            try {
                clientTom.existsQueue("queue/1");
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }

            // user without admin role can remove queues
            try {
                clientTom.removeQueue("queue/1");
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }
        }
    }


    @Test
    public void test_topic_mgmt() throws Exception {
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
            server.start();

            IO.sleep(300);

            clientTom.open("tom", "123");
            clientAdmin.open("admin", "123");

            // user with admin role can create topics
            try {
                clientAdmin.createTopic("topic/1");
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }

            // user with admin role can check if topic exists
            try {
                assertTrue(clientAdmin.existsTopic("topic/1"));
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }

            // user with admin role can remove topics
            try {
                clientAdmin.removeTopic("topic/1");
                assertFalse(clientAdmin.existsTopic("topic/1"));
            }
            catch(Exception ex) {
                fail("Should not reach here");
            }



            // user without admin role cannot create topics
            try {
                clientTom.createTopic("topic/1");
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }

            // user without admin role cannot check if topic exists
            try {
                clientTom.existsTopic("topic/1");
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }

            // user without admin role cannot remove topics
            try {
                clientTom.removeTopic("topic/1");
                fail("Should not reach here");
            }
            catch(Exception ex) {
            }
        }
    }
}
