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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files using "AES-256" with "CBC" and "PKCS5Padding".
 *
 * Uses a random salt and IV for each file and writes the salt and the IV
 * to start of the encrypted file.
 *
 * <b>DO NOT USE this cipher AES CBC in production!!</b>
 *
 * <b>AES CBC with PKCS5Padding padding scheme can lead to padding oracle attacks.</b>
 *
 * <pre>
 *    Encrypted binary file format
 *
 *     AES256-CBC               AES256-CBC            AES256-CBC
 * AES/CBC/PKCS5Padding    AES/CBC/PKCS5Padding    AES/CBC/PKCS5Padding
 *     random IV            custom ID, added       custom ID, not added
 * +------------------+    +------------------+    +------------------+
 * |      iv  (16)    |    |      iv  (16)    |    |      data (n)    |
 * +------------------+    +------------------+    +------------------+
 * |      data (n)    |    |      data (n)    |
 * +------------------+    +------------------+
 * </pre>
 */
public class FileEncryptor_AES256_CBC extends AbstractFileEncryptor implements IFileEncryptor {

    private FileEncryptor_AES256_CBC(
            final SecretKeySpec keySpec,
            final byte[] customIV,
            final boolean addCustomIvToEncryptedData
    ) {
        Objects.requireNonNull(keySpec);

        this.keySpec = keySpec;
        this.customIV = customIV;
        this.addIvToEncryptedData = customIV == null || addCustomIvToEncryptedData;
    }

    public static FileEncryptor_AES256_CBC create(
            final String passphrase
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        return create(passphrase, KEY_SALT, KEY_ITERATIONS, null, false);
    }

    public static FileEncryptor_AES256_CBC create(
            final String passphrase,
            final byte[] keySalt,
            final Integer keyIterations
    ) throws GeneralSecurityException {
        return create(passphrase, KEY_SALT, KEY_ITERATIONS, null, false);
    }

    public static FileEncryptor_AES256_CBC create(
            final String passphrase,
            final byte[] keySalt,
            final Integer keyIterations,
            final byte[] customIV,
            final boolean addCustomIvToEncryptedData
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        if (customIV != null && customIV.length != IV_LEN) {
            throw new IllegalArgumentException("A custom IV must have " + IV_LEN + " bytes!");
        }

        // Derive key from passphrase
        byte[] key = Util.deriveKeyFromPassphrase(
                                passphrase,
                                SECRET_KEY_FACTORY,
                                keySalt == null ? KEY_SALT : keySalt,
                                keyIterations == null ? KEY_ITERATIONS : keyIterations,
                                KEY_LEN);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        return new FileEncryptor_AES256_CBC(
                        keySpec,
                        emptyToNull(customIV),
                        addCustomIvToEncryptedData) ;
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
            // IV
            final byte[] iv = customIV == null ? randomIV() : customIV;

            // Initialize Cipher for AES-GCM
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

            // encrypt
            byte[] encryptedData = cipher.doFinal(data);

            if (addIvToEncryptedData) {
                // IV, and encrypted data
                byte[] outData = new byte[IV_LEN + encryptedData.length];
                System.arraycopy(iv, 0, outData, 0, IV_LEN);
                System.arraycopy(encryptedData, 0, outData, IV_LEN, encryptedData.length);

                return outData;
            }
            else {
                return encryptedData;
            }
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt data", ex);
        }
    }

    @Override
    public byte[] decrypt(final byte[] data) {
        Objects.requireNonNull(data);

        try {
            final byte[] iv;
            final byte[] encryptedData;

            if (addIvToEncryptedData) {
                iv = new byte[IV_LEN];
                System.arraycopy(data, 0, iv, 0, IV_LEN);

                encryptedData = new byte[data.length - IV_LEN];
                System.arraycopy(data, IV_LEN, encryptedData, 0, data.length - IV_LEN);
           }
            else {
                iv = customIV;
                encryptedData = data;
            }

            // Initialize Cipher for AES-GCM
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            // decrypt
            return cipher.doFinal(encryptedData);
        }
        catch(Exception ex) {
            throw new FileException("Failed to encrypt data", ex);
        }
   }


    public boolean hasProvider(final String name) {
        return Security.getProvider(StringUtil.trimToEmpty(name)) != null;
    }

    private byte[] randomIV() {
        final byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static byte[] emptyToNull(final byte[] data) {
        return data != null && data.length == 0 ? null : data;
    }


    public static int KEY_ITERATIONS = 3000;
    public static int KEY_LEN = 256;
    public static int IV_LEN = 16;

    public static String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA256";

    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };


    private final SecretKeySpec keySpec;
    private final byte[] customIV;
    private final boolean addIvToEncryptedData;
}
