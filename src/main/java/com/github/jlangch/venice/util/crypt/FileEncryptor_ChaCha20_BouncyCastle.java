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
import java.security.SecureRandom;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;


/**
 * Encrypt and decrypt files using "ChaCha20" (BouncyCastle).
 *
 * Uses a IV for each file and writes the IV to start of
 * the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format when passphrase is used
 *
 *    +-----------------------+
 *    |         salt          |   16 bytes
 *    +-----------------------+
 *    |          IV           |   8 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 *
 * <pre>
 *    Encrypted binary file format when key is used
 *
 *    +-----------------------+
 *    |          IV           |   8 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_ChaCha20_BouncyCastle {

    public static boolean isSupported() {
        return supported;
    }


    public static void encryptFileWithPassphrase(
            final String passphrase,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Encrypt
        byte[] encryptedData = encryptFileWithPassphrase(passphrase, fileData);

        // Write to output file
        Files.write(outputFile.toPath(), encryptedData);
    }

    public static byte[] encryptFileWithPassphrase(
            final String passphrase,
            final byte[] fileData
    ) throws Exception {
        // Generate a random salt
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);

        // Generate a random iv
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        // Derive key from passphrase
        byte[] key = KeyUtil.deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Encryption
        byte[] encryptedData = processData(Cipher.ENCRYPT_MODE, fileData, key, iv);

        // Combine salt, iv, and encrypted data
        byte[] outData = new byte[SALT_LEN + IV_LEN + encryptedData.length];
        System.arraycopy(salt, 0, outData, 0, salt.length);
        System.arraycopy(iv, 0, outData, SALT_LEN, iv.length);
        System.arraycopy(encryptedData, 0, outData, SALT_LEN + IV_LEN, encryptedData.length);

        return outData;
    }

    public static void encryptFileWithKey(
            final byte[] key,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Encrypt
        byte[] encryptedData = encryptFileWithKey(key, fileData);

        // Write to output file
        Files.write(outputFile.toPath(), encryptedData);
    }

    public static byte[] encryptFileWithKey(
            final byte[] key,
            final byte[] fileData
    ) throws Exception {
        // Generate a random iv
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        // Perform Encryption
        byte[] encryptedData = processData(Cipher.ENCRYPT_MODE, fileData, key, iv);

        // Combine iv and encrypted data
        byte[] outData = new byte[IV_LEN + encryptedData.length];
        System.arraycopy(iv, 0, outData, 0, iv.length);
        System.arraycopy(encryptedData, 0, outData, IV_LEN, encryptedData.length);

        return outData;
    }

    public static void decryptFileWithPassphrase(
            final String passphrase,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Decrypt
        byte[] decryptedData = decryptFileWithPassphrase(passphrase, fileData);

        // Write to output file
        Files.write(outputFile.toPath(), decryptedData);
    }

    public static byte[] decryptFileWithPassphrase(
            final String passphrase,
            final byte[] fileData
    ) throws Exception {
        // Extract salt, iv, and encrypted data
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(fileData, 0, salt, 0, SALT_LEN);

        byte[] iv = new byte[IV_LEN];
        System.arraycopy(fileData, SALT_LEN, iv, 0, IV_LEN);

        byte[] encryptedData = new byte[fileData.length - SALT_LEN - IV_LEN];
        System.arraycopy(fileData, SALT_LEN + IV_LEN, encryptedData, 0, encryptedData.length);

        // Derive key from passphrase
        byte[] key = KeyUtil.deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Decryption
        return processData(Cipher.DECRYPT_MODE, encryptedData, key, iv);
    }

    public static void decryptFileWithKey(
            final byte[] key,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Decrypt
        byte[] decryptedData = decryptFileWithKey(key, fileData);

        // Write to output file
        Files.write(outputFile.toPath(), decryptedData);
    }

    public static byte[] decryptFileWithKey(
            final byte[] key,
            final byte[] fileData
    ) throws Exception {
        // Extract iv and encrypted data
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(fileData, 0, iv, 0, IV_LEN);

        byte[] encryptedData = new byte[fileData.length - IV_LEN];
        System.arraycopy(fileData, IV_LEN, encryptedData, 0, encryptedData.length);

        // Perform Decryption
        return processData(Cipher.DECRYPT_MODE, encryptedData, key, iv);
    }

    private static byte[] processData(
    		final int mode,
            final byte[] data,
            final byte[] key,
            final byte[] iv
    ) throws Exception {
        ChaChaEngine chacha = new ChaChaEngine(ROUNDS);
        chacha.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] cryptData = new byte[data.length];
        chacha.processBytes(data, 0, data.length, cryptData, 0);

        return cryptData;
    }

    private static boolean checkSupported() {
        try {
            final Class<?> clazz = classForName("org.bouncycastle.crypto.engines.ChaChaEngine");
            return clazz != null;
        }
        catch(Exception ex) {
            return false;
        }
    }

    private static Class<?> classForName(final String name) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return Class.forName(name, true, contextClassLoader);
            }
            catch(Throwable ex) {
                // try next with current class loader
            }
        }

        // current class loader
        try {
            return Class.forName(name);
        }
        catch(Throwable ex) {
            throw new RuntimeException(String.format("Failed to load class '%s'", name));
        }
    }



    private static final boolean supported = checkSupported();

    private static int ROUNDS = 20;
    private static int SALT_LEN = 16;
    private static int IV_LEN = 8;
}
