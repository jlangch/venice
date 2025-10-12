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
package com.github.jlangch.venice.util.ipc.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


public class EncryptorTest {

    @Test
    public void test_encryption() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
        final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data)));
        assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data)));

        assertArrayEquals(data, serverEncryptor.decrypt(clientEncryptor.encrypt(data)));
        assertArrayEquals(data, clientEncryptor.decrypt(serverEncryptor.encrypt(data)));
    }

    @Test
    public void test_encryption_reuse() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
        final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        for(int ii=0; ii<1_000; ii++) {
            assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data)));
            assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data)));

            assertArrayEquals(data, serverEncryptor.decrypt(clientEncryptor.encrypt(data)));
            assertArrayEquals(data, clientEncryptor.decrypt(serverEncryptor.encrypt(data)));
        }
    }

}
