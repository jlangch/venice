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

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Encrypt and decrypt files using the "AES/GCM/NoPadding" algorithm.
 *
 * Uses a random salt and IV for each file and writes the salt and the IV
 * to start of the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format
 *
 *    +-----------------------+
 *    |         salt          |   16 bytes
 *    +-----------------------+
 *    |          IV           |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * <pre/>
 */
public class FileEncryptor {

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
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        // Generate a random IV
        byte[] iv = new byte[12]; // GCM recommended 12 bytes IV
        new SecureRandom().nextBytes(iv);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize GCM Parameters, 128 bit auth tag length
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        // Initialize Cipher for AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Encryption
        byte[] encryptedData = cipher.doFinal(fileData);

        // Combine salt, IV, and encrypted data
        byte[] saltIvAndEncryptedData = new byte[salt.length + iv.length + encryptedData.length];
        System.arraycopy(salt, 0, saltIvAndEncryptedData, 0, salt.length);
        System.arraycopy(iv, 0, saltIvAndEncryptedData, salt.length, iv.length);
        System.arraycopy(encryptedData, 0, saltIvAndEncryptedData, salt.length + iv.length, encryptedData.length);

        return saltIvAndEncryptedData;
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
        // Generate a random IV
        byte[] iv = new byte[12]; // GCM recommended 12 bytes IV
        new SecureRandom().nextBytes(iv);

        // Initialize GCM Parameters, 128 bit auth tag length
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        // Initialize Cipher for AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Encryption
        byte[] encryptedData = cipher.doFinal(fileData);

        // Combine IV and encrypted data
        byte[] ivAndEncryptedData = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncryptedData, 0, iv.length);
        System.arraycopy(encryptedData, 0, ivAndEncryptedData, iv.length, encryptedData.length);

        return ivAndEncryptedData;
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
        // Extract salt, IV, and encrypted data
        byte[] salt = new byte[16];
        System.arraycopy(fileData, 0, salt, 0, salt.length);
        byte[] iv = new byte[12];
        System.arraycopy(fileData, salt.length, iv, 0, iv.length);
        byte[] encryptedData = new byte[fileData.length - salt.length - iv.length];
        System.arraycopy(fileData, salt.length + iv.length, encryptedData, 0, encryptedData.length);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize GCM Parameters, 128 bit auth tag length
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        // Initialize Cipher for AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Decryption
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return decryptedData;
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
        // Extract IV and encrypted data
        byte[] iv = new byte[12];
        System.arraycopy(fileData, 0, iv, 0, iv.length);
        byte[] encryptedData = new byte[fileData.length - iv.length];
        System.arraycopy(fileData, iv.length, encryptedData, 0, encryptedData.length);

        // Initialize GCM Parameters, 128 bit auth tag length
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        // Initialize Cipher for AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        // Perform Decryption
        byte[] decryptedData = cipher.doFinal(encryptedData);

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
}
