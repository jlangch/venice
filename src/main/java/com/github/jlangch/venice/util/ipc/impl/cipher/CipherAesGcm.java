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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.crypt.Util;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


public class CipherAesGcm implements ICipher {

    private CipherAesGcm(final SecretKeySpec keySpec) {
        this.keySpec = keySpec;
    }


    public static CipherAesGcm create(final DiffieHellmanSharedSecret secret) {
        Objects.requireNonNull(secret);

        try {
            byte[] salt = new byte[SALT_LEN];
            System.arraycopy(secret.getSecret(), 0, salt, 0, salt.length);

            // Derive key from passphrase
            byte[] key = Util.deriveKeyFromPassphrase(secret.getSecretBase64(), salt, 3000, 256);

            return new CipherAesGcm(new SecretKeySpec(key, "AES"));
        }
        catch(GeneralSecurityException ex) {
            throw new VncException("Failed to initialize AES encryption", ex);
        }
    }

    @Override
    public byte[] encrypt(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = randomIV();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));
        cipher.updateAAD(aadData); // add AAD tag data before encrypting

        final byte[] encrypted = cipher.doFinal(data);

        return concat(iv, encrypted);
    }

    @Override
    public byte[] decrypt(final byte[] data) throws GeneralSecurityException {
        final byte[] iv = extractIV(data);
        final byte[] dataRaw = extractPayload(data);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(DECRYPT_MODE, keySpec, new GCMParameterSpec(128, iv));
        cipher.updateAAD(aadData);  // add AAD details before decrypting

        return cipher.doFinal(dataRaw);
    }


    private static byte[] randomIV() {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static byte[] concat(final byte[] iv, final byte[] data) {
        byte[] out = new byte[iv.length + data.length];
        System.arraycopy(iv, 0, out, 0, IV_LEN);
        System.arraycopy(data, 0, out, IV_LEN, data.length);
        return out;
    }

    private static byte[] extractIV(final byte[] data) {
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(data, 0, iv, 0, IV_LEN);
        return iv;
    }

    private static byte[] extractPayload(final byte[] data) {
        int payloadLen = data.length - IV_LEN;
        byte[] payload = new byte[payloadLen];
        System.arraycopy(data, IV_LEN, payload, 0, payloadLen);
        return payload;
    }


    private static int SALT_LEN = 16;
    private static int IV_LEN = 12;

    // An AAD (Additional Authenticated Data) tag in AES-GCM is a data string
    // that is authenticated, but not encrypted, alongside the ciphertext. It's
    // used to ensure the integrity of information that is not secret, like headers,
    // by including it in the authentication tag calculation. This provides an
    // extra layer of security, helping to protect against attacks by ensuring
    // the AAD and ciphertext haven't been tampered with
    private static byte[] aadData = "ipcmsg".getBytes(StandardCharsets.UTF_8);

    private final SecretKeySpec keySpec;
}
