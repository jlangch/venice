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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;

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
    public void test_sign_1() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final String signature = RSA.sign("Hello World", keyPair.getPrivate());

        assertTrue(RSA.verify(signature, "Hello World", keyPair.getPublic()));
    }

    @Test
    public void test_sign_2() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final String signature = RSA.sign(new byte[] {1,2,3,4,5,6,7,8}, keyPair.getPrivate());

        assertTrue(RSA.verify(signature, new byte[] {1,2,3,4,5,6,7,8}, keyPair.getPublic()));
    }

}
