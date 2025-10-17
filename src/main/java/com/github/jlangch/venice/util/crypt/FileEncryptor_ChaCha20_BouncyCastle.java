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
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files using "ChaCha20" (BouncyCastle).
 *
 * Uses a IV for each file and writes the IV to start of
 * the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format
 *
 *    +-----------------------+
 *    |          IV           |   8 bytes
 *    +-----------------------+
 *    |  encrypted file data  |   n bytes
 *    +-----------------------+
 * </pre>
 */
public class FileEncryptor_ChaCha20_BouncyCastle extends AbstractFileEncryptor implements IFileEncryptor {

    private FileEncryptor_ChaCha20_BouncyCastle(
            final byte[] key
    ) {
        Objects.requireNonNull(key);

        this.key = key;

    }


    public static FileEncryptor_ChaCha20_BouncyCastle create(
            final String passphrase
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        return create(passphrase, KEY_SALT, KEY_ITERATIONS);
    }

    public static FileEncryptor_ChaCha20_BouncyCastle create(
            final String passphrase,
            final byte[] keySalt,
            final Integer keyIterations
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        // Derive key from passphrase
        byte[] key = Util.deriveKeyFromPassphrase(
                        passphrase,
                        SECRET_KEY_FACTORY,
                        keySalt == null ? KEY_SALT : keySalt,
                        keyIterations == null ? KEY_ITERATIONS : keyIterations,
                        KEY_LEN);

        return new FileEncryptor_ChaCha20_BouncyCastle(key) ;
    }


    /**
     * @return <code>true</code> if this encryptor is supported with this Java VM
     *         else <code>false</code>
     */
    public static boolean isSupported() {
        return supported;
    }

    @Override
    public byte[] encrypt(final byte[] data) {
        Objects.requireNonNull(data);

        try {
            // generate a random IV
            byte[] iv = new byte[IV_LEN]; // GCM recommended 12 bytes IV
            new SecureRandom().nextBytes(iv);

            // encrypt
            ChaChaEngine chacha = new ChaChaEngine(ROUNDS);
            chacha.init(true, new ParametersWithIV(new KeyParameter(key), iv));

            byte[] outData = new byte[IV_LEN + data.length];
            System.arraycopy(iv, 0, outData, 0, iv.length);

            chacha.processBytes(data, 0, data.length, outData, IV_LEN);

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

            ChaChaEngine chacha = new ChaChaEngine(ROUNDS);
            chacha.init(true, new ParametersWithIV(new KeyParameter(key), iv));

            byte[] outData = new byte[data.length - IV_LEN];

            chacha.processBytes(data, IV_LEN, data.length - IV_LEN, outData, 0);

            return outData;
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt data", ex);
        }
    }

    public static boolean hasProvider(final String name) {
        return Security.getProvider(StringUtil.trimToEmpty(name)) != null;
    }

    public static boolean addBouncyCastleProvider() {
        synchronized(FileEncryptor_ChaCha20_BouncyCastle.class) {
            if (Security.getProvider("BC") != null) {
                return false;
            }
            else {
                try {
                    // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

                    // need to be fully dynamic to work under
                    //  - Java 8 (without/with optional BouncyCastle libs)
                    //  - Java 11+ (without/with optional BouncyCastle libs)

                    Security.addProvider(
                        (Provider)Util.classForName("org.bouncycastle.jce.provider.BouncyCastleProvider")
                                      .getConstructor()
                                      .newInstance());

                    return true;
                }
                catch(Exception ex) {
                    return false;
                }
            }
        }
    }

    private static final boolean supported = Util.hasClass("org.bouncycastle.crypto.engines.ChaChaEngine");


    public static int KEY_ITERATIONS = 3000;
    public static int KEY_LEN = 256;
    public static int IV_LEN = 8;
    public static int ROUNDS = 20;

    public static String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA256";

    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };

    private final byte[] key;
}
