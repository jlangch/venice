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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.ipc.impl.Message;
import com.github.jlangch.venice.util.ipc.impl.util.IO;


public class TcpClientRuntimeConfigTest {

    @Test
    public void test_client_defaults() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start();

        IO.sleep(300);

        client.open();

        try {
            assertEquals(false, client.isEncrypted());
            assertEquals(-1, client.getCompressCutoffSize());
            assertEquals(Message.MESSAGE_LIMIT_MAX, client.getMaxMessageSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start();

        IO.sleep(300);

        client.setEncryption(false);

        client.open();

        try {
            assertEquals(false, client.isEncrypted());
            assertEquals(-1, client.getCompressCutoffSize());
            assertEquals(Message.MESSAGE_LIMIT_MAX, client.getMaxMessageSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.start();

        IO.sleep(300);

        client.setEncryption(true);

        client.open();

        try {
            assertEquals(true, client.isEncrypted());
            assertEquals(-1, client.getCompressCutoffSize());
            assertEquals(Message.MESSAGE_LIMIT_MAX, client.getMaxMessageSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_inherit_config_1() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.setEncryption(false);
        server.setMaxMessageSize(3000);
        server.setCompressCutoffSize(2000);

        server.start();

        IO.sleep(300);

        client.open();

        try {
            // check if the client obtained the config from the server
            assertEquals(false, client.isEncrypted());
            assertEquals(3000, client.getMaxMessageSize());
            assertEquals(2000, client.getCompressCutoffSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_inherit_config_2() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.setEncryption(true);
        server.setMaxMessageSize(3000);
        server.setCompressCutoffSize(2000);

        server.start();

        IO.sleep(300);

        client.open();

        try {
            // check if the client obtained the config from the server
            assertEquals(true, client.isEncrypted());
            assertEquals(3000, client.getMaxMessageSize());
            assertEquals(2000, client.getCompressCutoffSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_inherit_config_3() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.setEncryption(true);
        server.setMaxMessageSize(3000);
        server.setCompressCutoffSize(2000);

        server.start();

        IO.sleep(300);

        client.setEncryption(false);  // server is dominant

        client.open();

        try {
            // check if the client obtained the config from the server
            assertEquals(true, client.isEncrypted());
            assertEquals(3000, client.getMaxMessageSize());
            assertEquals(2000, client.getCompressCutoffSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }

    @Test
    public void test_client_inherit_config_4() throws Exception {
        final TcpServer server = new TcpServer(33333);
        final TcpClient client = new TcpClient(33333);

        server.setEncryption(true);
        server.setMaxMessageSize(3000);
        server.setCompressCutoffSize(2000);

        server.start();

        IO.sleep(300);

        client.setEncryption(true);

        client.open();

        try {
            // check if the client obtained the config from the server
            assertEquals(true, client.isEncrypted());
            assertEquals(3000, client.getMaxMessageSize());
            assertEquals(2000, client.getCompressCutoffSize());
        }
        catch(Exception ex) {
            // OK
        }
        finally {
            client.close();
            server.close();
        }
    }
}
