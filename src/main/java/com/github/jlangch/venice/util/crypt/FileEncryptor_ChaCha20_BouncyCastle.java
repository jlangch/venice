/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import java.io.File;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;


/**
 * Encrypt and decrypt files using "ChaCha20" (BouncyCastle).
 *
 * Uses a nonce for each file and writes the nonce to start of
 * the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format when passphrase is used
 *
 *    +-----------------------+
 *    |         salt          |   16 bytes
 *    +-----------------------+
 *    |         nonce         |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 *
 * <pre>
 *    Encrypted binary file format when key is used
 *
 *    +-----------------------+
 *    |         nonce         |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_ChaCha20_BouncyCastle {

    public static boolean isSupported() {
        try {
            final Class<?> clazz = ReflectionUtil.classForName("org.bouncycastle.crypto.engines.ChaChaEngine");
            return clazz != null;
        }
        catch(Exception ex) {
            return false;
        }
    }


    public static void encryptFileWithPassphrase(
            final File inputFile,
            final File outputFile,
            final String passphrase
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Encrypt
        byte[] encryptedData = encryptFileWithPassphrase(fileData, passphrase);

        // Write to output file
        Files.write(outputFile.toPath(), encryptedData);
    }

    public static byte[] encryptFileWithPassphrase(
            final byte[] fileData,
            final String passphrase
    ) throws Exception {
        // Generate a random salt
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);

        // Generate a random nonce
        byte[] nonce = new byte[NONCE_LEN];
        new SecureRandom().nextBytes(nonce);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Encryption
        byte[] encryptedData = encryptData(fileData, key, nonce);

        // Combine salt, nonce, and encrypted data
        byte[] outData = new byte[SALT_LEN + NONCE_LEN + encryptedData.length];
        System.arraycopy(salt, 0, outData, 0, salt.length);
        System.arraycopy(nonce, 0, outData, SALT_LEN, nonce.length);
        System.arraycopy(encryptedData, 0, outData, SALT_LEN + NONCE_LEN, encryptedData.length);

        return outData;
    }

    public static void encryptFileWithKey(
            final File inputFile,
            final File outputFile,
            final byte[] key
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Encrypt
        byte[] encryptedData = encryptFileWithKey(fileData, key);

        // Write to output file
        Files.write(outputFile.toPath(), encryptedData);
    }

    public static byte[] encryptFileWithKey(
            final byte[] fileData,
            final byte[] key
    ) throws Exception {
        // Generate a random nonce
        byte[] nonce = new byte[NONCE_LEN];
        new SecureRandom().nextBytes(nonce);

        // Perform Encryption
        byte[] encryptedData = encryptData(fileData, key, nonce);

        // Combine nonce and encrypted data
        byte[] outData = new byte[NONCE_LEN + encryptedData.length];
        System.arraycopy(nonce, 0, outData, 0, nonce.length);
        System.arraycopy(encryptedData, 0, outData, NONCE_LEN, encryptedData.length);

        return outData;
    }

    public static void decryptFileWithPassphrase(
            final File inputFile,
            final File outputFile,
            final String passphrase
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Decrypt
        byte[] decryptedData = decryptFileWithPassphrase(fileData, passphrase);

        // Write to output file
        Files.write(outputFile.toPath(), decryptedData);
    }

    public static byte[] decryptFileWithPassphrase(
            final byte[] fileData,
            final String passphrase
    ) throws Exception {
        // Extract salt, nonce, counter, and encrypted data
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(fileData, 0, salt, 0, SALT_LEN);

        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(fileData, SALT_LEN, nonce, 0, NONCE_LEN);

        byte[] encryptedData = new byte[fileData.length - SALT_LEN - NONCE_LEN];
        System.arraycopy(fileData, SALT_LEN + NONCE_LEN, encryptedData, 0, encryptedData.length);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Decryption
        return decryptData(encryptedData, key, nonce);
    }

    public static void decryptFileWithKey(
            final File inputFile,
            final File outputFile,
            final byte[] key
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Decrypt
        byte[] decryptedData = decryptFileWithKey(fileData, key);

        // Write to output file
        Files.write(outputFile.toPath(), decryptedData);
    }

    public static byte[] decryptFileWithKey(
            final byte[] fileData,
            final byte[] key
    ) throws Exception {
        // Extract nonce, counter, and encrypted data
        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(fileData, 0, nonce, 0, NONCE_LEN);

        byte[] encryptedData = new byte[fileData.length - NONCE_LEN];
        System.arraycopy(fileData, NONCE_LEN, encryptedData, 0, encryptedData.length);

        // Perform Decryption
        return decryptData(encryptedData, key, nonce);
    }

    private static byte[] encryptData(
            final byte[] data,
            final byte[] key,
            final byte[] iv
    ) throws Exception {
        ChaChaEngine chacha = new ChaChaEngine(ROUNDS);
        chacha.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] encryptedData = new byte[data.length];
        chacha.processBytes(data, 0, data.length, encryptedData, 0);

        return encryptedData;
    }

    private static byte[] decryptData(
            final byte[] data,
            final byte[] key,
            final byte[] iv
    ) throws Exception {
        ChaChaEngine chacha = new ChaChaEngine(ROUNDS);
        chacha.init(false, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] decryptedData = new byte[data.length];
        chacha.processBytes(data, 0, data.length, decryptedData, 0);

        return decryptedData;
    }

    private static byte[] deriveKeyFromPassphrase(
            final String passphrase,
            final byte[] salt,
            final int iterationCount,
            final int keyLength
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }


    private static int ROUNDS = 20;
    private static int SALT_LEN = 16;
    private static int NONCE_LEN = 8;
}
