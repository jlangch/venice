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
package com.github.jlangch.venice.util.ipc.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import javax.crypto.AEADBadTagException;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.dh.DiffieHellmanKeys;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;
import com.github.jlangch.venice.util.ipc.IpcException;


public class EncryptorTest {

    @Test
    public void test() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
        final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

        final byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data)));
        assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data)));

        assertArrayEquals(data, serverEncryptor.decrypt(clientEncryptor.encrypt(data)));
        assertArrayEquals(data, clientEncryptor.decrypt(serverEncryptor.encrypt(data)));
    }

    @Test
    public void test_many() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
        final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

        for(int ii=0; ii<1_000; ii++) {
            final byte[] data = ("hello world " + ii).getBytes(StandardCharsets.UTF_8);

            assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data)));
            assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data)));

            assertArrayEquals(data, serverEncryptor.decrypt(clientEncryptor.encrypt(data)));
            assertArrayEquals(data, clientEncryptor.decrypt(serverEncryptor.encrypt(data)));
        }
    }

    @Test
    public void test_tamper() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());

        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);

        final byte[] data = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr".getBytes(StandardCharsets.UTF_8);

        final byte[] encrypted = clientEncryptor.encrypt(data);

        // tamper the encrypted data
        encrypted[10] = (byte)(encrypted[10] ^ 0x055);

        assertThrows(Exception.class, () -> clientEncryptor.decrypt(encrypted));
    }

    @Test
    public void test_AAD() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
        final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

        final byte[] aad = "1234".getBytes(StandardCharsets.UTF_8);
        final byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data, aad), aad, true));
        assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data, aad), aad, true));
    }

    @Test
    public void test_AAD_fail() {
        try {
            final DiffieHellmanKeys client = DiffieHellmanKeys.create();
            final DiffieHellmanKeys server = DiffieHellmanKeys.create();

            final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
            final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

            // The secrets must be identical
            assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


            final Encryptor clientEncryptor = Encryptor.aes(clientSecret);
            final Encryptor serverEncryptor = Encryptor.aes(serverSecret);

            final byte[] aad = "1234".getBytes(StandardCharsets.UTF_8);
            final byte[] aad_bad = "1111".getBytes(StandardCharsets.UTF_8);
            final byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

            assertArrayEquals(data, clientEncryptor.decrypt(clientEncryptor.encrypt(data, aad), aad_bad, true));
            assertArrayEquals(data, serverEncryptor.decrypt(serverEncryptor.encrypt(data, aad), aad_bad, true));
        }
        catch(IpcException ex) {
            assertTrue(ex.getCause().getCause() instanceof AEADBadTagException);
        }
    }

    @Test
    public void test_AAD_buf_size_1() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);

        final byte[] aad = "1234".getBytes(StandardCharsets.UTF_8);
        final byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        final byte[] dataEncrypted = clientEncryptor.encrypt(data);
        final byte[] dataEncryptedAAD= clientEncryptor.encrypt(data, aad);

        assertEquals(dataEncrypted.length, dataEncryptedAAD.length);
        assertEquals(data.length + 28, dataEncrypted.length);
    }

    @Test
    public void test_AAD_buf_size_2() {
        final DiffieHellmanKeys client = DiffieHellmanKeys.create();
        final DiffieHellmanKeys server = DiffieHellmanKeys.create();

        final DiffieHellmanSharedSecret clientSecret = client.generateSharedSecret(server.getPublicKeyBase64());
        final DiffieHellmanSharedSecret serverSecret = server.generateSharedSecret(client.getPublicKeyBase64());

        // The secrets must be identical
        assertArrayEquals(clientSecret.getSecret(), serverSecret.getSecret());


        final Encryptor clientEncryptor = Encryptor.aes(clientSecret);

        final byte[] aad = "1234".getBytes(StandardCharsets.UTF_8);
        final byte[] data = new byte[20_000];

        final byte[] dataEncrypted = clientEncryptor.encrypt(data);
        final byte[] dataEncryptedAAD= clientEncryptor.encrypt(data, aad);

        assertEquals(dataEncrypted.length, dataEncryptedAAD.length);
        assertEquals(data.length + 28, dataEncrypted.length);
   }

}
