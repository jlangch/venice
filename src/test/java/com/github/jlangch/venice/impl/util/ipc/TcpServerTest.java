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
package com.github.jlangch.venice.impl.util.ipc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.BindException;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

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

}
