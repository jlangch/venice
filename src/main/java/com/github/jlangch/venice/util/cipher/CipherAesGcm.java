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
package com.github.jlangch.venice.util.cipher;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.VncException;


/**
 * Encrypt and decrypt data using "AES-256 GCM" with "NoPadding".
 *
 * Uses a random or static IV for each encryption run
 *
 * <pre>
 *    Encrypted binary data format when random IV is used
 *
 *    +-----------------------+
 *    |          IV           |   12 bytes
 *    +-----------------------+
 *    |     encrypted data    |   n bytes
 *    +-----------------------+
 * </pre>
 *
 * <pre>
 *    Encrypted binary data format when static IV is used
 *
 *    +-----------------------+
 *    |     encrypted data    |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class CipherAesGcm extends AbstractCipher implements ICipher {

    private CipherAesGcm(
            final SecretKeySpec keySpec,
            final byte[] aadData,
            final byte[] staticIV
    ) {
        this.keySpec = keySpec;
        this.aadData = aadData;
        this.staticIV = staticIV;
    }

    public static CipherAesGcm create(final String secret) {
        return create(secret, SECRET_KEY_FACTORY, KEY_ITERATIONS, KEY_LEN, KEY_SALT, AAD_DATA, null);
    }

    public static CipherAesGcm create(
            final String secret,
            final String secretKeyFactoryName,
            final int keyIterationCount,
            final int keyLength,
            final byte[] keySalt,
            final byte[] aadData,
            final byte[] staticIV
    ) {
        Objects.requireNonNull(secret);
        Objects.requireNonNull(secretKeyFactoryName);

        if (staticIV != null && staticIV.length != IV_LEN) {
            throw new IllegalArgumentException("A static IV must have 12 bytes!");
        }

        try {
            // Derive key from passphrase
            byte[] key = CipherUtils.deriveKeyFromPassphrase(
                            secret,
                            secretKeyFactoryName,
                            CipherUtils.isEmpty(keySalt) ? KEY_SALT : keySalt,
                            keyIterationCount,
                            keyLength);

            return new CipherAesGcm(
                        new SecretKeySpec(key, "AES"),
                        CipherUtils.isEmpty(keySalt) ? AAD_DATA : aadData,
                        CipherUtils.emptyToNull(staticIV));
        }
        catch(GeneralSecurityException ex) {
            throw new VncException("Failed to initialize AES encryption", ex);
        }
    }

    @Override
    public byte[] encrypt(final byte[] data) throws GeneralSecurityException {
        Objects.requireNonNull(data);

        return staticIV == null
                ? encryptRandomIV(data)
                : encryptStaticIV(data);
    }

    @Override
    public byte[] decrypt(final byte[] data) throws GeneralSecurityException {
        Objects.requireNonNull(data);

        return staticIV == null
                ? decryptRandomIV(data)
                : decryptStaticIV(data);
    }


    private byte[] encryptRandomIV(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = CipherUtils.randomIV(IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));

        if (!CipherUtils.isEmpty(aadData)) {
            cipher.updateAAD(aadData); // add AAD tag data before encrypting
        }

        final byte[] encrypted = cipher.doFinal(data);

        return CipherUtils.concat(iv, encrypted);
    }

    private byte[] encryptStaticIV(final byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, staticIV));

        if (!CipherUtils.isEmpty(aadData)) {
            cipher.updateAAD(aadData); // add AAD tag data before encrypting
        }

        return cipher.doFinal(data);
    }

    private byte[] decryptRandomIV(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = CipherUtils.extract(data, 0, IV_LEN);
        final byte[] dataRaw = CipherUtils.extract(data, IV_LEN, data.length - IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(DECRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));

        if (!CipherUtils.isEmpty(aadData)) {
            cipher.updateAAD(aadData); // add AAD tag data before decrypting
        }

        return cipher.doFinal(dataRaw);
    }

    private byte[] decryptStaticIV(final byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(DECRYPT_MODE, keySpec, new GCMParameterSpec(128, staticIV));

        if (!CipherUtils.isEmpty(aadData)) {
            cipher.updateAAD(aadData); // add AAD tag data before decrypting
        }

        return cipher.doFinal(data);
    }


    public static String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA256";

    public static int IV_LEN = 12;
    public static int KEY_ITERATIONS = 3000;
    public static int KEY_LEN = 256;


    // An AAD (Additional Authenticated Data) tag in AES-GCM is a data string
    // that is authenticated, but not encrypted, alongside the ciphertext. It's
    // used to ensure the integrity of information that is not secret, like headers,
    // by including it in the authentication tag calculation. This provides an
    // extra layer of security, helping to protect against attacks by ensuring
    // the AAD and ciphertext haven't been tampered with
    private static byte[] AAD_DATA = "ipcmsg".getBytes(StandardCharsets.UTF_8);


    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };


    private final SecretKeySpec keySpec;
    private final byte[] aadData;
    private final byte[] staticIV;
}
