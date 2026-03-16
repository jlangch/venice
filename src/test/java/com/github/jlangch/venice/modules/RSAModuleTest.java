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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class RSAModuleTest {

    @Test
    public void test_generate_key_pair() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                         \n" +
                "    (instance-of? :java.security.KeyPair key-pair)))              ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_generate_public_key() {
        final Venice venice = new Venice();

        final String script =
                "(do                                          \n" +
                "  (load-module :rsa)                         \n" +
                "  (let [key-pair (rsa/generate-key-pair)]    \n" +
                "    (rsa/public-key key-pair)))              ";

        assertTrue(venice.eval(script) instanceof PublicKey);
    }

    @Test
    public void test_generate_private_key() {
        final Venice venice = new Venice();

        final String script =
                "(do                                          \n" +
                "  (load-module :rsa)                         \n" +
                "  (let [key-pair (rsa/generate-key-pair)]    \n" +
                "    (rsa/private-key key-pair)))             ";

        assertTrue(venice.eval(script) instanceof PrivateKey);
    }

    @Test
    public void test_save_load() {
        final Venice venice = new Venice();

        // TODO: implement
    }

    @Test
    public void test_encrypt_decrypt() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                         \n" +
                "    (-> (rsa/encrypt \"Hello World\" (rsa/public-key key-pair))   \n" +
                "        (rsa/decrypt (rsa/private-key key-pair)))))               ";

        assertEquals("Hello World", venice.eval(script));
    }

}
