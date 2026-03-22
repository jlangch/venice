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
package com.github.jlangch.venice.impl.repl.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class ReplRemotingConfigTest {

    @Test
    public void test_parse_json_1() {
        final String json =
                "{ \"host\": \"localhost\",\n"
                + "  \"port\": 33334,\n"
                + "  \"password\": \"123\",\n"
                + "  \"encrypt\": true, \n"
                + "  \"compress\": false,\n"
                + "  \"sessionTimeoutMinutes\": 30,\n"
                + "  \"signKeys\": true,\n"
                + "  \"serverPublicKeyFile\": \"/foo/server-public.pem\",\n"
                + "  \"serverPrivateKeyFile\": \"/foo/server-private.pem\",\n"
                + "  \"clientPublicKeyFile\": \"/foo/client-public.pem\",\n"
                + "  \"clientPrivateKeyFile\": \"/foo/client-private.pem\"\n"
                + "}";

        final ReplRemotingConfig cfg = ReplRemotingConfig.of(json);

        assertEquals("localhost", cfg.getHost());
        assertEquals(33334, cfg.getPort());
        assertEquals("123", cfg.getPassword());
        assertEquals(true, cfg.isEncrypt());
        assertEquals(false, cfg.isCompress());
        assertEquals(30, cfg.getSessionTimeoutMinutes());
        assertEquals(true, cfg.isSignKeys());
        assertEquals("/foo/server-public.pem", cfg.getServerPublicKeyFile());
        assertEquals("/foo/server-private.pem", cfg.getServerPrivateKeyFile());
        assertEquals("/foo/client-public.pem", cfg.getClientPublicKeyFile());
        assertEquals("/foo/client-private.pem", cfg.getClientPrivateKeyFile());
    }

    @Test
    public void test_parse_json_2() {
        final String json =
                "{ \"host\": \"localhost\",\n"
                + "  \"port\": 33334,\n"
                + "  \"password\": \"123\",\n"
                + "  \"encrypt\": true, \n"
                + "  \"compress\": false,\n"
                + "  \"sessionTimeoutMinutes\": 30,\n"
                + "  \"signKeys\": false,\n"
                + "  \"serverPublicKeyFile\": null,\n"
                + "  \"serverPrivateKeyFile\": null,\n"
                + "  \"clientPublicKeyFile\": null,\n"
                + "  \"clientPrivateKeyFile\": null\n"
                + "}";

        final ReplRemotingConfig cfg = ReplRemotingConfig.of(json);

        assertEquals("localhost", cfg.getHost());
        assertEquals(33334, cfg.getPort());
        assertEquals("123", cfg.getPassword());
        assertEquals(true, cfg.isEncrypt());
        assertEquals(false, cfg.isCompress());
        assertEquals(30, cfg.getSessionTimeoutMinutes());
        assertEquals(false, cfg.isSignKeys());
        assertEquals(null, cfg.getServerPublicKeyFile());
        assertEquals(null, cfg.getServerPrivateKeyFile());
        assertEquals(null, cfg.getClientPublicKeyFile());
        assertEquals(null, cfg.getClientPrivateKeyFile());
    }

}
