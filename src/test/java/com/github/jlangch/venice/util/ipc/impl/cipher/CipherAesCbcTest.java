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
package com.github.jlangch.venice.util.ipc.impl.cipher;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


public class CipherAesCbcTest {

    @Test
    public void test_randomIV() throws Exception {
        final DiffieHellmanKeys keys = DiffieHellmanKeys.create();
        final DiffieHellmanSharedSecret secret = keys.generateSharedSecret(keys.getPublicKeyBase64());

        final byte[] data = "hello world".getBytes(Charset.forName("UTF-8"));

        final CipherAesCbc cipher = CipherAesCbc.create(secret);

        assertArrayEquals(data, cipher.decrypt(cipher.encrypt(data)));
    }

    @Test
    public void test_randomIV_many() throws Exception {
        final DiffieHellmanKeys keys = DiffieHellmanKeys.create();
        final DiffieHellmanSharedSecret secret = keys.generateSharedSecret(keys.getPublicKeyBase64());

        final CipherAesCbc cipher = CipherAesCbc.create(secret);

        for(int ii=0; ii<1_000; ii++) {
            final byte[] data = ("hello world " + ii).getBytes(Charset.forName("UTF-8"));
            assertArrayEquals(data, cipher.decrypt(cipher.encrypt(data)));
        }
    }

    @Test
    public void test_staticIV() throws Exception {
        final DiffieHellmanKeys keys = DiffieHellmanKeys.create();
        final DiffieHellmanSharedSecret secret = keys.generateSharedSecret(keys.getPublicKeyBase64());

        final byte[] data = "hello world".getBytes(Charset.forName("UTF-8"));

        final CipherAesCbc cipher = CipherAesCbc.create(secret, 3000, 256, KEY_SALT, STATIC_IV);

        assertArrayEquals(data, cipher.decrypt(cipher.encrypt(data)));
    }

    @Test
    public void test_staticIV_many() throws Exception {
        final DiffieHellmanKeys keys = DiffieHellmanKeys.create();
        final DiffieHellmanSharedSecret secret = keys.generateSharedSecret(keys.getPublicKeyBase64());

        final CipherAesCbc cipher = CipherAesCbc.create(secret, 3000, 256, KEY_SALT, STATIC_IV);

        for(int ii=0; ii<1_000; ii++) {
            final byte[] data = ("hello world " + ii).getBytes(Charset.forName("UTF-8"));
            assertArrayEquals(data, cipher.decrypt(cipher.encrypt(data)));
        }
    }


    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, 0x5e,
            0x03, 0x71, 0x44, 0x2f, 0x4f };

    private static byte[] STATIC_IV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0};
}
