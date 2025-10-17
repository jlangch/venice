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

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files using "ChaCha20". Available on Java 11+
 *
 * Uses a counter and a nonce for each file and writes the counter
 * and the nonce to start of the encrypted file.
 *
 * <pre>
 *    Encrypted binary file format
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
public class Encryptor_ChaCha20 extends AbstractEncryptor implements IEncryptor {

    private Encryptor_ChaCha20(
            final SecretKeySpec keySpec
    ) {
        Objects.requireNonNull(keySpec);

        this.keySpec = keySpec;

    }


    public static Encryptor_ChaCha20 create(
            final String passphrase
    ) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase);

        return create(passphrase, KEY_SALT, KEY_ITERATIONS);
    }

    public static Encryptor_ChaCha20 create(
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

        return new Encryptor_ChaCha20(new SecretKeySpec(key, "ChaCha20"));
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
            // Generate a random nonce
            byte[] nonce = new byte[NONCE_LEN];
            new SecureRandom().nextBytes(nonce);

            // Generate a counter
            int counter = new SecureRandom().nextInt();
            byte[] counterData = counterToBytes(counter);

            // Initialize ChaCha20 Parameters
            AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

            // Initialize Cipher for ChaCha20
            Cipher cipher = Cipher.getInstance("ChaCha20");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, param);

            // encryption
            final byte[] encryptedData = cipher.doFinal(data);

            byte[] outData = new byte[NONCE_LEN + COUNTER_LEN + encryptedData.length];

            System.arraycopy(nonce, 0, outData, 0, nonce.length);
            System.arraycopy(counterData, 0, outData, NONCE_LEN, counterData.length);
            System.arraycopy(encryptedData, 0, outData, NONCE_LEN + COUNTER_LEN, encryptedData.length);

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
            byte[] nonce = new byte[NONCE_LEN];
            System.arraycopy(data, 0, nonce, 0, NONCE_LEN);

            byte[] counterBytes = new byte[COUNTER_LEN];
            System.arraycopy(data, NONCE_LEN, counterBytes, 0, COUNTER_LEN);

            byte[] encryptedData = new byte[data.length - NONCE_LEN - COUNTER_LEN];
            System.arraycopy(data, NONCE_LEN + COUNTER_LEN, encryptedData, 0, encryptedData.length);

            int counter = counterToInt(counterBytes);

            // Initialize ChaCha20 Parameters
            AlgorithmParameterSpec param = createChaCha20ParameterSpec(nonce, counter);

            // Initialize Cipher for ChaCha20
            Cipher cipher = Cipher.getInstance("ChaCha20");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, param);

            // decryption
            return cipher.doFinal(encryptedData);
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt data", ex);
        }
   }


    public boolean hasProvider(final String name) {
        return Security.getProvider(StringUtil.trimToEmpty(name)) != null;
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

    private static AlgorithmParameterSpec createChaCha20ParameterSpec(
            final byte[] nonce,
            final int counter
    ) {
        // return new ChaCha20ParameterSpec(nonce, counter);

        // Note: ChaCha20 is only available with Java11+
        try {
            Class<?> clazz = Util.classForName("javax.crypto.spec.ChaCha20ParameterSpec");
            Constructor<?> c = clazz.getConstructor(new Class[]{byte[].class, int.class});
            return (AlgorithmParameterSpec)c.newInstance(nonce, counter);
        }
        catch(Exception ex) {
            throw new RuntimeException("Java Crypto algorithm ChaCha20 is not available!");
        }
    }


   private static final boolean supported = Util.hasClass("javax.crypto.spec.ChaCha20ParameterSpec");


   private static ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN; // ensure same byte order on all machines


   public static int KEY_ITERATIONS = 3000;
   public static int KEY_LEN = 256;
   private static int NONCE_LEN = 12;
   private static int COUNTER_LEN = 4;

   public static String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA256";

    private static byte[] KEY_SALT = new byte[] {
            0x45, 0x1a, 0x79, 0x67, (byte)0xba, (byte)0xfa, 0x0d, 0x5e,
            0x03, 0x71, 0x44, 0x2f, (byte)0xc3, (byte)0xa5, 0x6e, 0x4f };

    private final SecretKeySpec keySpec;
}
