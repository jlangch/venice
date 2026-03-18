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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class RsaPartyAuthenticationTest {

    @Test
    public void test_rsa_party_authentication() throws Exception {
        final File dir = Files.createTempDirectory("test").toFile();

        try {
            final Venice venice = new Venice();

            final String script =
                    "(do                                                                               \n" +
                    "  (load-module :rsa)                                                              \n" +
                    "  (load-module :ipc)                                                              \n" +
                    "                                                                                  \n" +
                    "  ;; create RSA key pairs for the client and the server                           \n" +
                    "  (rsa/save-key-pair (rsa/generate-key-pair) dir \"client\")                      \n" +
                    "  (rsa/save-key-pair (rsa/generate-key-pair) dir \"server\")                      \n" +
                    "                                                                                  \n" +
                    "  ;; on the client side the client key pair and the server public key is required \n" +
                    "  (def client-key-pair (rsa/load-key-pair dir \"client\"))                        \n" +
                    "  (def server-public-key (rsa/public-key server-key-pair))                        \n" +
                    "                                                                                  \n" +
                    "  ;; on the server side the server key pair and the client public key is required \n" +
                    "  (def server-key-pair (rsa/load-key-pair dir \"server\"))                        \n" +
                    "  (def client-public-key (rsa/public-key client-key-pair))                        \n" +
                    "                                                                                  \n" +
                    "  (def counter (atom 0))                                                          \n" +
                    "  (defn echo-handler [m] (swap! counter inc) m)                                   \n" +
                    "                                                                                  \n" +
                    "  (try-with [server (ipc/server 33333                                             \n" +
                    "                          :encrypt true                                           \n" +
                    "                          :dh-rsa-sign true                                       \n" +
                    "                          :dh-rsa-server-key-pair server-key-pair                 \n" +
                    "                          :dh-rsa-client-public-key client-public-key             \n" +
                    "                          :server-log-dir dir)                                    \n" +
                    "             client (ipc/client 33333                                             \n" +
                    "                          :dh-rsa-client-key-pair client-key-pair                 \n" +
                    "                          :dh-rsa-server-public-key server-public-key)]           \n" +
                    "    (ipc/create-function server :echo echo-handler)                               \n" +
                    "    (ipc/send client :echo (ipc/plain-text-message \"1\" \"test\" \"hello 1\"))   \n" +
                    "    (sleep 100))                                                                  \n" +
                    "                                                                                  \n" +
                    "  (deref counter))                                                                ";

            assertEquals(1L, venice.eval(script, Parameters.of("dir", dir)));
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

}
