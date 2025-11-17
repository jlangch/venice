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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;


public class TcpClientTest {

    @Test
    public void test_epoch_millis() throws Exception {
        long millis1 = Instant.now().toEpochMilli();
        long millis2 = System.currentTimeMillis();

        assertTrue(Math.abs(millis2 - millis1) < 50);
    }

    @Test
    public void test_client_max_size() throws Exception {
        try (TcpClient client = new TcpClient(33333)) {
            assertEquals(TcpClient.MESSAGE_LIMIT_MAX, client.getMaximumMessageSize());

            // below minimum
            client.setMaximumMessageSize(100L);
            assertEquals(TcpClient.MESSAGE_LIMIT_MIN, client.getMaximumMessageSize());

            // in range
            client.setMaximumMessageSize(100L * 1024L);
            assertEquals(100L * 1024L, client.getMaximumMessageSize());

            // above maximum
            client.setMaximumMessageSize(800L * 1024L * 1024L);
            assertEquals(TcpClient.MESSAGE_LIMIT_MAX, client.getMaximumMessageSize());
        }
    }

}
