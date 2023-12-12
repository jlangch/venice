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
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.impl.util.reflect.ReflectionUtil;


/**
 * Encrypt and decrypt files using "ChaCha20". Available on Java 11+
 *
 * Uses a counter and a nonce for each file and writes the counter
 * and the nonce to start of the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format when passphrase is used
 *
 *    +-----------------------+
 *    |         salt          |   16 bytes
 *    +-----------------------+
 *    |         nonce         |   12 bytes
 *    +-----------------------+
 *    |        counter        |   4 bytes
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
 *    |        counter        |   4 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_ChaCha20 {

	public static boolean isSupported() {
        try {
            final Class<?> clazz = ReflectionUtil.classForName("javax.crypto.spec.ChaCha20ParameterSpec");
            return clazz != null;
        }
        catch(Exception ex) {
            return false;
        }
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

        // Generate a random nonce
        byte[] nonce = new byte[NONCE_LEN];
        new SecureRandom().nextBytes(nonce);

        // Generate an counter
        int counter = new SecureRandom().nextInt();
        byte[] counterData = counterToBytes(counter);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize ChaCha20 Parameters
        AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

        // Initialize Cipher for ChaCha20
        Cipher cipher = Cipher.getInstance("ChaCha20");
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, param);

        // Perform Encryption
        byte[] encryptedData = cipher.doFinal(fileData);

        // Combine salt, nonce, counter, and encrypted data
        byte[] outData = new byte[SALT_LEN + NONCE_LEN + COUNTER_LEN + encryptedData.length];
        System.arraycopy(salt, 0, outData, 0, salt.length);
        System.arraycopy(nonce, 0, outData, SALT_LEN, nonce.length);
        System.arraycopy(counterData, 0, outData, SALT_LEN + NONCE_LEN, counterData.length);
        System.arraycopy(encryptedData, 0, outData, SALT_LEN + NONCE_LEN + COUNTER_LEN, encryptedData.length);

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
        // Generate a random nonce
        byte[] nonce = new byte[NONCE_LEN];
        new SecureRandom().nextBytes(nonce);

        // Generate an counter
        int counter = new SecureRandom().nextInt();
        byte[] counterData = counterToBytes(counter);

        // Secret key
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");

        // Initialize ChaCha20 Parameters
        AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

        // Initialize Cipher for ChaCha20
        Cipher cipher = Cipher.getInstance("ChaCha20");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, param);

        // encryption
        byte[] encryptedData = cipher.doFinal(fileData);

        // Combine salt, nonce, counter, and encrypted data
        byte[] outData = new byte[NONCE_LEN + COUNTER_LEN + encryptedData.length];
        System.arraycopy(nonce, 0, outData, 0, nonce.length);
        System.arraycopy(counterData, 0, outData, NONCE_LEN, counterData.length);
        System.arraycopy(encryptedData, 0, outData, NONCE_LEN + COUNTER_LEN, encryptedData.length);

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
        // Extract salt, nonce, counter, and encrypted data
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(fileData, 0, salt, 0, SALT_LEN);

        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(fileData, SALT_LEN, nonce, 0, NONCE_LEN);

        byte[] counterBytes = new byte[COUNTER_LEN];
        System.arraycopy(fileData, SALT_LEN + NONCE_LEN, counterBytes, 0, COUNTER_LEN);

        byte[] encryptedData = new byte[fileData.length - SALT_LEN - NONCE_LEN - COUNTER_LEN];
        System.arraycopy(fileData, SALT_LEN + NONCE_LEN + COUNTER_LEN, encryptedData, 0, encryptedData.length);

        int counter = counterToInt(counterBytes);

        // Derive key from passphrase
        byte[] key = deriveKeyFromPassphrase(passphrase, salt, 65536, 256);

        // Initialize ChaCha20 Parameters
        AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

        // Initialize Cipher for ChaCha20
        Cipher cipher = Cipher.getInstance("ChaCha20");
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, param);

        // Perform Decryption
        return cipher.doFinal(encryptedData);
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
        // Extract nonce, counter, and encrypted data
        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(fileData, 0, nonce, 0, NONCE_LEN);

        byte[] counterBytes = new byte[COUNTER_LEN];
        System.arraycopy(fileData, NONCE_LEN, counterBytes, 0, COUNTER_LEN);

        byte[] encryptedData = new byte[fileData.length - NONCE_LEN - COUNTER_LEN];
        System.arraycopy(fileData, NONCE_LEN + COUNTER_LEN, encryptedData, 0, encryptedData.length);

        int counter = counterToInt(counterBytes);

        // Secret key
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");

        // Initialize ChaCha20 Parameters
        AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

        // Initialize Cipher for ChaCha20
        Cipher cipher = Cipher.getInstance("ChaCha20");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, param);

        // decryption
        return cipher.doFinal(encryptedData);
    }


    private static AlgorithmParameterSpec createChaCha20ParameterSpec(
            final byte[] nonce,
            final int counter
    ) {
        // return new ChaCha20ParameterSpec(nonce, counter);

        // Note: ChaCha20 is only available with Java11+
        try {
            Class<?> clazz = ReflectionUtil.classForName("javax.crypto.spec.ChaCha20ParameterSpec");
            Constructor<?> c = clazz.getConstructor(new Class[]{byte[].class, int.class});
            return (AlgorithmParameterSpec)c.newInstance(nonce, counter);
        }
        catch(Exception ex) {
            throw new RuntimeException("Java Crypto algorithm ChaCha20 is not available!");
        }
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

    private static byte[] counterToBytes(final int counter) {
        // convert int to byte[]
    	return ByteBuffer.allocate(COUNTER_LEN)
    	                 .order(ENDIAN)
    	                 .putInt(counter)
    	                 .array();
    }

    private static int counterToInt(final byte[] counter) {
        // convert byte[] to int
    	return ByteBuffer.wrap(counter)
    	                 .order(ENDIAN)
                         .getInt(0);
    }


    private static ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN; // ensure same byte order on all machines

    private static int SALT_LEN = 16;
    private static int NONCE_LEN = 12;
    private static int COUNTER_LEN = 4;
}
