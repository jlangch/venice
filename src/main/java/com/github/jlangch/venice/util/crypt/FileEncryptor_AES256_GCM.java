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

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files using "AES-256" with "GCM" and "NoPadding".
 *
 * Uses a random salt and IV for each file and writes the salt and the IV
 * to start of the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format
 *
 *    +-----------------------+
 *    |          IV           |   12 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_AES256_GCM extends AbstractFileEncryptor implements IFileEncryptor {

    private FileEncryptor_AES256_GCM(
            final SecretKeySpec keySpec
    ) {
        Objects.requireNonNull(keySpec);

        this.keySpec = keySpec;

    }

    public static FileEncryptor_AES256_GCM create(
            final String passphrase
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        return create(passphrase, KEY_SALT, KEY_ITERATIONS);
    }

    public static FileEncryptor_AES256_GCM create(
            final String passphrase,
            final byte[] keySalt,
            final int keyIterations
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);
        Objects.requireNonNull(keySalt);

        // Derive key from passphrase
        byte[] key = Util.deriveKeyFromPassphrase(passphrase, keySalt, keyIterations, KEY_LEN);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        return new FileEncryptor_AES256_GCM(keySpec) ;
    }


    /**
     * @return <code>true</code> if this encryptor is supported with this Java VM
     *         else <code>false</code>
     */
    public static boolean isSupported() {
        return true;
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        Objects.requireNonNull(data);

        try {
            // Generate a random IV
            byte[] iv = new byte[IV_LEN]; // GCM recommended 12 bytes IV
            new SecureRandom().nextBytes(iv);

            // Initialize GCM Parameters, 128 bit auth tag length
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

            // Initialize Cipher for AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // encrypt
            byte[] encryptedData = cipher.doFinal(data);

            // IV, and encrypted data
            byte[] outData = new byte[IV_LEN + encryptedData.length];
            System.arraycopy(iv, 0, outData, 0, IV_LEN);
            System.arraycopy(encryptedData, 0, outData, IV_LEN, encryptedData.length);

            return outData;
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt data", ex);
        }
    }

    @Override
    public byte[] decrypt(final byte[] data) {
        Objects.requireNonNull(data);

        try {
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(data, 0, iv, 0, IV_LEN);

            byte[] encryptedData = new byte[data.length - IV_LEN];
            System.arraycopy(data, IV_LEN, encryptedData, 0, data.length - IV_LEN);

            // Initialize GCM Parameters, 128 bit auth tag length
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

            // Initialize Cipher for AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            // encrypt
            return cipher.doFinal(encryptedData);
        }
        catch(Exception ex) {
            throw new FileException("Failed to encrypt data", ex);
        }
   }


    public boolean hasProvider(final String name) {
        return Security.getProvider(StringUtil.trimToEmpty(name)) != null;
    }


    public static int KEY_ITERATIONS = 3000;
    public static int KEY_LEN = 256;
    public static int IV_LEN = 12;

    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };


    private final SecretKeySpec keySpec;

}
