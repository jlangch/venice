/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import com.github.jlangch.venice.impl.util.io.IOStreamUtil;


public class RSA {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    public static void storePrivateKey_X509DER(final PrivateKey key, final OutputStream os) throws IOException {
        final byte[] privateKeyBytes = key.getEncoded();
        final PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // "private_key.der"
        os.write(pkcs8EncodedKeySpec.getEncoded());
    }

    public static void storePublicKey_X509DER(final PublicKey key, final OutputStream os) throws IOException {
        final byte[] privateKeyBytes = key.getEncoded();
        final X509EncodedKeySpec pkcs8EncodedKeySpec = new X509EncodedKeySpec(privateKeyBytes);

        // "public_key.der"
        os.write(pkcs8EncodedKeySpec.getEncoded());
    }

    public static PrivateKey loadPrivateKey_X509DER(final InputStream is)
            throws IOException, GeneralSecurityException
    {
        final byte[] privateKeyBytes = IOStreamUtil.copyIStoByteArray(is);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static PublicKey loadPublicKey_X509DER(final InputStream is)
            throws IOException, GeneralSecurityException
    {
        final byte[] publicKeyBytes = IOStreamUtil.copyIStoByteArray(is);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    public static String encrypt(final String message, final PublicKey publicKey)
            throws GeneralSecurityException
    {
        final Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        final byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(final String message, final PrivateKey privateKey)
            throws GeneralSecurityException
    {
        final byte[] encryptedBytes = Base64.getDecoder().decode(message);

        final Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public String convertDerToPem(final byte[] derBytes) {
        final String base64Encoded = Base64.getEncoder().encodeToString(derBytes);

        // PEM formatting (linefeed every 64 characters)
        final String pem = "-----BEGIN CERTIFICATE-----\n" +
                           base64Encoded.replaceAll("(.{64})", "$1\n") +
                           "\n-----END CERTIFICATE-----";

        return pem;
    }


    private static Cipher getCipher() throws GeneralSecurityException {
        return Cipher.getInstance("RSA/ECB/OAEPPadding");
    }


    private static int KEY_SIZE = 2048;  // key size in bits
}
