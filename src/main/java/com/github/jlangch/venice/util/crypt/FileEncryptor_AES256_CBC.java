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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Encrypt and decrypt files using "AES-256" with "CBC" and "PKCS5Padding".
 *
 * Uses a random salt and IV for each file and writes the salt and the IV
 * to start of the encrypted file.
 *
 * Note:  AES/CBC/PKCS5Padding is regarded as a broken or risky cryptographic
 *        algorithm (CWE-327, CWE-328)
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
public class FileEncryptor_AES256_CBC {

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
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize IV Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize Cipher for AES-CBC
        Cipher cipher = getCipherInstance();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        // Perform Encryption
        byte[] encryptedData = cipher.doFinal(fileData);

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
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        // Initialize IV Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize Cipher for AES-CBC
        Cipher cipher = getCipherInstance();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        // Perform Encryption
        byte[] encryptedData = cipher.doFinal(fileData);

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
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize IV Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize Cipher for AES-CBC
        Cipher cipher = getCipherInstance();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

        // Perform Decryption
        byte[] decryptedData = cipher.doFinal(encryptedData);

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

        // Initialize IV Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize Cipher for AES-CBC
        Cipher cipher = getCipherInstance();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

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

    private static Cipher getCipherInstance() throws Exception {
    	return Cipher.getInstance(String.join("/", new String[] {"AES", "CBC", "PKCS5Padding"}));
    }


    private static int SALT_LEN = 16;
    private static int IV_LEN = 16;
}
