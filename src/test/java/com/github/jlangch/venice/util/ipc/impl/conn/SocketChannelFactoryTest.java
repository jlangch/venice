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
package com.github.jlangch.venice.util.ipc.impl.conn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;


public class SocketChannelFactoryTest {

    @Test
    public void test_URI_AF_INET_1() throws URISyntaxException {
        final URI uri = new URI("af-inet://localhost:3333");

        assertEquals("af-inet", uri.getScheme());
        assertEquals("localhost", uri.getHost());
        assertEquals(3333, uri.getPort());
    }

    @Test
    public void test_URI_AF_INET_2() throws URISyntaxException {
        final URI uri = new URI("af-inet://127.0.0.1:3333");

        assertEquals("af-inet", uri.getScheme());
        assertEquals("127.0.0.1", uri.getHost());
        assertEquals(3333, uri.getPort());
    }

    @Test
    public void test_URI_AF_UNIX_() throws URISyntaxException {
        final URI uri = new URI("af-unix:///path/to/your/socket");

        assertEquals("af-unix", uri.getScheme());
        assertEquals("/path/to/your/socket", uri.getPath());
    }

}
