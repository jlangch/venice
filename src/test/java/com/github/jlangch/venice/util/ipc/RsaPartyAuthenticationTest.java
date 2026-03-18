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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class RsaPartyAuthenticationTest {

    @Test
    public void test_rsa_party_authentication() throws Exception {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                   \n" +
                "  (load-module :rsa)                                                                  \n" +
                "  (load-module :ipc)                                                                  \n" +
                "                                                                                      \n" +
                "  (with-tmp-dir \"test\"                                                              \n" +
                "    ;; create RSA key pairs for the client and the server, manually created           \n" +
                "    ;; once and deployed to client and server                                         \n" +
                "    ;;   - client-public.pem, client-private.pem                                      \n" +
                "    ;;   - server-public.pem, server-private.pem                                      \n" +
                "    (rsa/save-key-pair (rsa/generate-key-pair) *tmp-dir* \"client\")                  \n" +
                "    (rsa/save-key-pair (rsa/generate-key-pair) *tmp-dir* \"server\")                  \n" +
                "                                                                                      \n" +
                "    (def counter (atom 0))                                                            \n" +
                "    (defn echo-handler [m] (swap! counter inc) m)                                     \n" +
                "                                                                                      \n" +
                "    ;; load key pairs at client/server start                                          \n" +
                "    (let [client-key-pair (rsa/load-key-pair *tmp-dir* \"client\")                    \n" +
                "          server-key-pair (rsa/load-key-pair *tmp-dir* \"server\")                    \n" +
                "                                                                                      \n" +
                "          ;; share public keys between client and server                              \n" +
                "          client-public-key (rsa/public-key client-key-pair)                          \n" +
                "          server-public-key (rsa/public-key server-key-pair)]                         \n" +
                "                                                                                      \n" +
                "      (try-with [server (ipc/server 33333                                             \n" +
                "                              :encrypt true                                           \n" +
                "                              :dh-rsa-sign true                                       \n" +
                "                              :dh-rsa-server-key-pair server-key-pair                 \n" +
                "                              :dh-rsa-client-public-key client-public-key             \n" +
                "                              :server-log-dir *tmp-dir*)                              \n" +
                "                 client (ipc/client 33333                                             \n" +
                "                              :dh-rsa-client-key-pair client-key-pair                 \n" +
                "                              :dh-rsa-server-public-key server-public-key)]           \n" +
                "                                                                                      \n" +
                "        (ipc/create-function server :echo echo-handler)                               \n" +
                "                                                                                      \n" +
                "        (ipc/send client :echo (ipc/plain-text-message \"1\" \"test\" \"hello 1\"))   \n" +
                "                                                                                      \n" +
                "        (sleep 100)))                                                                 \n" +
                "                                                                                      \n" +
                "    (deref counter)))                                                                 ";

        assertEquals(1L, venice.eval(script));
    }

    @Test
    public void test_rsa_party_authentication_multi_client() throws Exception {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                      \n" +
                "  (load-module :rsa)                                                                     \n" +
                "  (load-module :ipc)                                                                     \n" +
                "                                                                                         \n" +
                "  (with-tmp-dir \"test\"                                                                 \n" +
                "    ;; create RSA key pairs for the client and the server, manually created              \n" +
                "    ;; once and deployed to client and server                                            \n" +
                "    ;;   - clientA-public.pem, clientA-private.pem                                       \n" +
                "    ;;   - clientB-public.pem, clientB-private.pem                                       \n" +
                "    ;;   - server-public.pem, server-private.pem                                         \n" +
                "    (rsa/save-key-pair (rsa/generate-key-pair) *tmp-dir* \"clientA\")                    \n" +
                "    (rsa/save-key-pair (rsa/generate-key-pair) *tmp-dir* \"clientB\")                    \n" +
                "    (rsa/save-key-pair (rsa/generate-key-pair) *tmp-dir* \"server\")                     \n" +
                "                                                                                         \n" +
                "    (def counter (atom 0))                                                               \n" +
                "    (defn echo-handler [m] (swap! counter inc) m)                                        \n" +
                "                                                                                         \n" +
                "    ;; load key pairs at client/server start                                             \n" +
                "    (let [clientA-key-pair (rsa/load-key-pair *tmp-dir* \"clientA\")                     \n" +
                "          clientB-key-pair (rsa/load-key-pair *tmp-dir* \"clientB\")                     \n" +
                "          server-key-pair  (rsa/load-key-pair *tmp-dir* \"server\")                      \n" +
                "                                                                                         \n" +
                "          ;; share public keys between client and server                                 \n" +
                "          clientA-public-key (rsa/public-key clientA-key-pair)                           \n" +
                "          clientB-public-key (rsa/public-key clientB-key-pair)                           \n" +
                "          server-public-key  (rsa/public-key server-key-pair)]                           \n" +
                "                                                                                         \n" +
                "      (try-with [server (ipc/server 33333                                                \n" +
                "                              :encrypt true                                              \n" +
                "                              :dh-rsa-sign true                                          \n" +
                "                              :dh-rsa-server-key-pair server-key-pair                    \n" +
                "                              :dh-rsa-client-public-key [clientA-public-key              \n" +
                "                                                         clientB-public-key]             \n" +
                "                              :server-log-dir *tmp-dir*)                                 \n" +
                "                 clientA1 (ipc/client 33333                                              \n" +
                "                              :dh-rsa-client-key-pair clientA-key-pair                   \n" +
                "                              :dh-rsa-server-public-key server-public-key)               \n" +
                "                 clientA2 (ipc/client 33333                                              \n" +
                "                              :dh-rsa-client-key-pair clientA-key-pair                   \n" +
                "                              :dh-rsa-server-public-key server-public-key)               \n" +
                "                 clientB (ipc/client 33333                                               \n" +
                "                              :dh-rsa-client-key-pair clientB-key-pair                   \n" +
                "                              :dh-rsa-server-public-key server-public-key)]              \n" +
                "        (ipc/create-function server :echo echo-handler)                                  \n" +
                "                                                                                         \n" +
                "        (ipc/send clientA1 :echo (ipc/plain-text-message \"A1\" \"test\" \"hello A1\"))  \n" +
                "        (ipc/send clientA2 :echo (ipc/plain-text-message \"A2\" \"test\" \"hello A2\"))  \n" +
                "        (ipc/send clientB  :echo (ipc/plain-text-message \"B\"  \"test\" \"hello B\"))   \n" +
                "                                                                                         \n" +
                "        (sleep 100)))                                                                    \n" +
                "                                                                                         \n" +
                "    (deref counter)))                                                                    ";

        assertEquals(3L, venice.eval(script));
    }

}
