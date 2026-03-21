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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.Test;


public class RSA_IO_Test {

    // ------------------------------------------------------------------------
    // DER
    // ------------------------------------------------------------------------

    @Test
    public void test_DER_load_strore_private_key_byte_array() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final byte[] data = RSA.toBytes(keyPair.getPrivate());

        final PrivateKey key = RSA.loadPrivateKey_X509DER(data);

        assertNotNull(key);
    }

    @Test
    public void test_DER_load_strore_public_key_byte_array() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final byte[] data = RSA.toBytes(keyPair.getPublic());

        final PublicKey key = RSA.loadPublicKey_X509DER(data);

        assertNotNull(key);
    }

    @Test
    public void test_DER_load_strore_private_key_stream() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePrivateKey_X509DER(keyPair.getPrivate(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PrivateKey key = RSA.loadPrivateKey_X509DER(is);

        assertNotNull(key);
    }

    @Test
    public void test_DER_load_strore_public_key_stream() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePublicKey_X509DER(keyPair.getPublic(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PublicKey key = RSA.loadPublicKey_X509DER(is);

        assertNotNull(key);
    }

    @Test
    public void test_DER_load_strore_private_key_file() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final File file = Files.createTempFile("test", "-private.der").normalize().toFile();
        file.deleteOnExit();

        RSA.storePrivateKey_X509DER(keyPair.getPrivate(), file);

        final PrivateKey key = RSA.loadPrivateKey_X509DER(file);

        assertNotNull(key);
    }

    @Test
    public void test_DER_load_strore_public_key_file() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final File file = Files.createTempFile("test", "-public.der").normalize().toFile();
        file.deleteOnExit();

        RSA.storePublicKey_X509DER(keyPair.getPublic(), file);

        final PublicKey key = RSA.loadPublicKey_X509DER(file);

        assertNotNull(key);
    }


    // ------------------------------------------------------------------------
    // PEM
    // ------------------------------------------------------------------------

    @Test
    public void test_PEM_load_strore_private_key_byte_array() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final byte[] data = RSA.toBytes(keyPair.getPrivate());

        final String pem = PemConverter.convertDerToPem(data, PemConverter.TYPE.PrivateKey);
        assertTrue(PemConverter.isPem(pem, PemConverter.TYPE.PrivateKey));

        final PrivateKey key = RSA.loadPrivateKey_X509DER(
                                      PemConverter.convertPemToDer(
                                          pem.getBytes(StandardCharsets.US_ASCII)));

        assertNotNull(key);
    }

    @Test
    public void test_PEM_load_strore_public_key_byte_array() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final byte[] data = RSA.toBytes(keyPair.getPrivate());

        final String pem = PemConverter.convertDerToPem(data, PemConverter.TYPE.PublicKey);
        assertTrue(PemConverter.isPem(pem, PemConverter.TYPE.PublicKey));

        final PrivateKey key = RSA.loadPrivateKey_X509DER(
                                      PemConverter.convertPemToDer(
                                          pem.getBytes(StandardCharsets.US_ASCII)));

        assertNotNull(key);
    }

    @Test
    public void test_PEM_load_strore_private_key_stream() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePrivateKey_X509PEM(keyPair.getPrivate(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PrivateKey key = RSA.loadPrivateKey_X509PEM(is);

        assertNotNull(key);
    }

    @Test
    public void test_PEM_load_strore_public_key_stream() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        RSA.storePublicKey_X509PEM(keyPair.getPublic(), os);

        final byte[] data = os.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(data);

        final PublicKey key = RSA.loadPublicKey_X509PEM(is);

        assertNotNull(key);
    }

    @Test
    public void test_PEM_load_strore_private_key_file() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final File file = Files.createTempFile("test", "-private.pem").normalize().toFile();
        file.deleteOnExit();

        RSA.storePrivateKey_X509PEM(keyPair.getPrivate(), file);

        final PrivateKey key = RSA.loadPrivateKey_X509PEM(file);

        assertNotNull(key);
    }

    @Test
    public void test_PEM_load_strore_public_key_file() throws Exception {
        final KeyPair keyPair = RSA.generateKeyPair();

        final File file = Files.createTempFile("test", "-public.pem").normalize().toFile();
        file.deleteOnExit();

        RSA.storePublicKey_X509PEM(keyPair.getPublic(), file);

        final PublicKey key = RSA.loadPublicKey_X509PEM(file);

        assertNotNull(key);
    }
}
