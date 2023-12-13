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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Encrypt and decrypt files using "AES-256" with "GCM" and "NoPadding".
 *
 * Uses a random salt and IV for each file and writes the salt and the IV
 * to start of the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format when passphrase is used
 *
 *    +-----------------------+
 *    |         salt          |   16 bytes
 *    +-----------------------+
 *    |          IV           |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 *
 * <pre>
 *    Encrypted binary file format when key is used
 *
 *    +-----------------------+
 *    |          IV           |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_AES256_GCM {

	public static boolean isSupported() {
		return true;
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

        // Generate a random IV
        byte[] iv = new byte[IV_LEN]; // GCM recommended 12 bytes IV
        new SecureRandom().nextBytes(iv);

        // Derive key from passphrase
        byte[] key = KeyUtil.deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Encryption
        byte[] encryptedData = processData(Cipher.ENCRYPT_MODE, fileData, key, iv);

        // Combine salt, IV, and encrypted data
        byte[] outData = new byte[SALT_LEN + IV_LEN + encryptedData.length];
        System.arraycopy(salt, 0, outData, 0, SALT_LEN);
        System.arraycopy(iv, 0, outData, SALT_LEN, IV_LEN);
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
        // Generate a random IV
        byte[] iv = new byte[IV_LEN]; // GCM recommended 12 bytes IV
        new SecureRandom().nextBytes(iv);

        // Perform Encryption
        byte[] encryptedData = processData(Cipher.ENCRYPT_MODE, fileData, key, iv);

        // Combine IV and encrypted data
        byte[] outData = new byte[IV_LEN + encryptedData.length];
        System.arraycopy(iv, 0, outData, 0, IV_LEN);
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
        // Extract salt, IV, and encrypted data
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(fileData, 0, salt, 0, SALT_LEN);

        byte[] iv = new byte[IV_LEN];
        System.arraycopy(fileData, SALT_LEN, iv, 0, IV_LEN);

        byte[] encryptedData = new byte[fileData.length - SALT_LEN - IV_LEN];
        System.arraycopy(fileData, SALT_LEN + IV_LEN, encryptedData, 0, encryptedData.length);

        // Derive key from passphrase
        byte[] key = KeyUtil.deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Perform Decryption
        byte[] decryptedData = processData(Cipher.DECRYPT_MODE, encryptedData, key, iv);

        return decryptedData;
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
        // Extract IV and encrypted data
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(fileData, 0, iv, 0, IV_LEN);

        byte[] encryptedData = new byte[fileData.length - IV_LEN];
        System.arraycopy(fileData, IV_LEN, encryptedData, 0, encryptedData.length);

        // Perform Decryption
        byte[] decryptedData = processData(Cipher.DECRYPT_MODE, encryptedData, key, iv);

        return decryptedData;
    }


    private static byte[] processData(
    		final int mode,
            final byte[] data,
            final byte[] key,
            final byte[] iv
    ) throws Exception {
        // Initialize GCM Parameters, 128 bit auth tag length
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        // Initialize Cipher for AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(mode, keySpec, gcmParameterSpec);

        // Compute
        return cipher.doFinal(data);
    }


    private static int SALT_LEN = 16;
    private static int IV_LEN = 12;
}
