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
package com.github.jlangch.venice.util.ipc.impl.util;

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


public class Encryptor {

    private Encryptor(
            final DiffieHellmanSharedSecret secret,
            final Cipher cipherEncrypt,
            final Cipher cipherDecrypt
    ) {
        this.secret = secret;
        this.cipherEncrypt = cipherEncrypt;
        this.cipherDecrypt = cipherDecrypt;
    }


    public static Encryptor aes(final DiffieHellmanSharedSecret secret) {
        Objects.requireNonNull(secret);

        try {
            byte[] salt = new byte[16];
            System.arraycopy(secret.getSecret(), 0, salt, 0, salt.length);

            byte[] iv = new byte[16];
            System.arraycopy(secret.getSecret(), 0, iv, 0, iv.length);

            // Derive key from passphrase
            byte[] key = Util.deriveKeyFromPassphrase(secret.getSecretBase64(), salt, 65536, 256);

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            Cipher cipherEncrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Create the ciphers
            cipherEncrypt.init(ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            cipherDecrypt.init(DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            return new Encryptor(secret, cipherEncrypt, cipherDecrypt);
        }
        catch(GeneralSecurityException ex) {
            throw new VncException("Failed to initialize AES encryption", ex);
        }
    }

    public static Encryptor off() {
        return new Encryptor(null, null, null);
    }


    public byte[] encrypt(final byte[] data) {
        return encrypt(data, isActive());
    }

    public byte[] encrypt(final byte[] data, final boolean encrypt) {
        Objects.requireNonNull(data);
        if (encrypt) {
            try {
                return cipherEncrypt.doFinal(data);
            }
            catch(Exception ex) {
                throw new VncException("Failed to encrypt message payload data", ex);
            }
        }
        else {
            return data;
        }
    }

    public byte[] decrypt(final byte[] data) {
        return decrypt(data, isActive());
    }

    public byte[] decrypt(final byte[] data, final boolean decrypt) {
        Objects.requireNonNull(data);
        if (decrypt) {
            try {
                return cipherDecrypt.doFinal(data);
            }
            catch(Exception ex) {
                throw new VncException("Failed to decrypt message payload data", ex);
            }
        }
        else {
            return data;
        }
    }

    public boolean isActive() {
        return secret != null;
    }


    private final DiffieHellmanSharedSecret secret;

    private final Cipher cipherEncrypt;
    private final Cipher cipherDecrypt;
}
