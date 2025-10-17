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
package com.github.jlangch.venice.util.crypt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;


public class FileEncryptor_ChaCha20_Test {

    @Test
    public void test_single_1() throws Exception {
        if (Encryptor_ChaCha20.isSupported()) {
            final byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);

            final IEncryptor encryptor = Encryptor_ChaCha20.create("123");

            assertArrayEquals(data, encryptor.decrypt(encryptor.encrypt(data)));
        }
    }

    @Test
    public void test_single_2() throws Exception {
        if (Encryptor_ChaCha20.isSupported()) {
            byte[] SALT = new byte[] {0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e};


            final byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);

            final IEncryptor encryptor = Encryptor_ChaCha20.create("123", SALT, 3000);

            assertArrayEquals(data, encryptor.decrypt(encryptor.encrypt(data)));
        }
    }


    @Test
    public void test_many_1() throws Exception {
        if (Encryptor_ChaCha20.isSupported()) {
            final IEncryptor encryptor = Encryptor_ChaCha20.create("123");

            for(int ii=0; ii<1000; ii++) {
                final byte[] data = ("test " + ii).getBytes(StandardCharsets.UTF_8);
                assertArrayEquals(data, encryptor.decrypt(encryptor.encrypt(data)));
            }
        }
    }

    @Test
    public void test_many_2() throws Exception {
        if (Encryptor_ChaCha20.isSupported()) {
            byte[] SALT = new byte[] {0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e};

            final IEncryptor encryptor = Encryptor_ChaCha20.create("123", SALT, 3000);

            for(int ii=0; ii<1000; ii++) {
                final byte[] data = ("test " + ii).getBytes(StandardCharsets.UTF_8);
                assertArrayEquals(data, encryptor.decrypt(encryptor.encrypt(data)));
            }
        }
    }

}
