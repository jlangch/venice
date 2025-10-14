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
package com.github.jlangch.venice.util.ipc.impl.cipher;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.crypt.Util;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


/**
 * DO NOT USE this cipher AES CBC in production!!
 *
 * AES CBC with PKCS5Padding padding scheme can lead to padding oracle attacks
 */
public class CipherAesCbc implements ICipher {

    private CipherAesCbc(
            final SecretKeySpec keySpec,
            final byte[] staticIV
    ) {
        this.keySpec = keySpec;
        this.staticIV = staticIV;
    }


    public static CipherAesCbc create(final DiffieHellmanSharedSecret secret) {
        return create(secret, KEY_ITERATIONS, KEY_LEN, KEY_SALT, null);
    }

    public static CipherAesCbc create(
            final DiffieHellmanSharedSecret secret,
            final int keyIterationCount,
            final int keyLength,
            final byte[] keySalt,
            final byte[] staticIV
    ) {
        Objects.requireNonNull(secret);

        try {
            // Derive key from passphrase
            byte[] key = Util.deriveKeyFromPassphrase(
                                secret.getSecretBase64(),
                                CipherUtils.isArrayEmpty(keySalt) ? KEY_SALT : keySalt,
                                keyIterationCount,
                                keyLength);

            return new CipherAesCbc(
                    new SecretKeySpec(key, "AES"),
                    CipherUtils.emptyToNull(staticIV));
        }
        catch(GeneralSecurityException ex) {
            throw new VncException("Failed to initialize AES encryption", ex);
        }
    }

    @Override
    public byte[] encrypt(final byte[] data) throws GeneralSecurityException {
        return staticIV == null
                ? encryptRandomIV(data)
                : encryptStaticIV(data);
    }

    @Override
    public byte[] decrypt(final byte[] data) throws GeneralSecurityException {
        return staticIV == null
                ? decryptRandomIV(data)
                : decryptStaticIV(data);
    }


    private byte[] encryptRandomIV(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = CipherUtils.randomIV(IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

        final byte[] encrypted = cipher.doFinal(data);

        return CipherUtils.concat(iv, encrypted);
    }

    private byte[] decryptRandomIV(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = CipherUtils.extract(data, 0, IV_LEN);
        final byte[] dataRaw = CipherUtils.extract(data, IV_LEN, data.length - IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

        return cipher.doFinal(dataRaw);
    }

    private byte[] encryptStaticIV(final byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(ENCRYPT_MODE, keySpec, new IvParameterSpec(staticIV));

        return cipher.doFinal(data);
    }

    private byte[] decryptStaticIV(final byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(DECRYPT_MODE, keySpec, new IvParameterSpec(staticIV));

        return cipher.doFinal(data);
    }


    private static int IV_LEN = 16;
    private static int KEY_ITERATIONS = 3000;
    private static int KEY_LEN = 256;

    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };


    private final SecretKeySpec keySpec;
    private final byte[] staticIV;
}
