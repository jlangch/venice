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
package com.github.jlangch.venice.util.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.Test;


public class RSA_Test {

    @Test
    public void test_encrypt_decrypt() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final String encrypted = RSA.encrypt("1234567890", keyPair.getPublic());

        final String decrypted = RSA.decrypt(encrypted, keyPair.getPrivate());

        assertEquals("1234567890", decrypted);
    }

    @Test
    public void test_load_strore_private_key() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePrivateKey_X509DER(keyPair.getPrivate(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PrivateKey key = RSA.loadPrivateKey_X509DER(is);

        assertNotNull(key);
    }

    @Test
    public void test_load_strore_public_key() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePublicKey_X509DER(keyPair.getPublic(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PublicKey key = RSA.loadPublicKey_X509DER(is);

        assertNotNull(key);
    }

}
