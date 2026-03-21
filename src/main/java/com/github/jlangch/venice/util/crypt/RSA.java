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

import java.io.File;
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
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;

import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.util.crypt.PemConverter.TYPE;


public class RSA {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return generateKeyPair(KEY_SIZE);
    }

    public static KeyPair generateKeyPair(final int keySize) throws NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize, new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] toBytes(
            final PrivateKey key
    ) throws IOException {
        Objects.requireNonNull(key);

        final byte[] privateKeyBytes = key.getEncoded();
        final PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // "private_key.der"
        return pkcs8EncodedKeySpec.getEncoded();
    }

    public static byte[] toBytes(
            final PublicKey key
    ) throws IOException {
        Objects.requireNonNull(key);

        final byte[] privateKeyBytes = key.getEncoded();
        final X509EncodedKeySpec pkcs8EncodedKeySpec = new X509EncodedKeySpec(privateKeyBytes);

        // "public_key.der"
        return pkcs8EncodedKeySpec.getEncoded();
    }

    public static byte[] encrypt(
            final byte[] uncryptedBytes,
            final PublicKey publicKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(uncryptedBytes);
        Objects.requireNonNull(publicKey);

        final Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(uncryptedBytes);
     }

    public static String encrypt(
            final String message,
            final PublicKey publicKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(message);
        Objects.requireNonNull(publicKey);

        final Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        final byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static byte[] decrypt(
            final byte[] encryptedBytes,
            final PrivateKey privateKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(encryptedBytes);
        Objects.requireNonNull(privateKey);

        final Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return decryptedBytes;
    }

    public static String decrypt(
            final String messageBase64,
            final PrivateKey privateKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(messageBase64);
        Objects.requireNonNull(privateKey);

        final byte[] encryptedBytes = Base64.getDecoder().decode(messageBase64);
        final Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String sign(
            final byte[] message,
            final PrivateKey privateKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(message);
        Objects.requireNonNull(privateKey);

        final Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(message);

        final byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public static String sign(
            final String message,
            final PrivateKey privateKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(message);
        Objects.requireNonNull(privateKey);

        return sign(message.getBytes(StandardCharsets.UTF_8), privateKey);
    }

    public static boolean verify(
            final String signature,
            final byte[] message,
            final PublicKey publicKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(signature);
        Objects.requireNonNull(message);
        Objects.requireNonNull(publicKey);

        final Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(message);

        final byte[] signatureRaw = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureRaw);
    }

    public static boolean verify(
            final String signature,
            final String message,
            final PublicKey publicKey
    ) throws GeneralSecurityException {
        Objects.requireNonNull(signature);
        Objects.requireNonNull(message);
        Objects.requireNonNull(publicKey);

        return verify(signature, message.getBytes(StandardCharsets.UTF_8), publicKey);
    }


    // ------------------------------------------------------------------------
    // File IO X509 DER
    // ------------------------------------------------------------------------

    public static void storePrivateKey_X509DER(
            final PrivateKey key,
            final OutputStream os
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(os);

        // "private_key.der"
        os.write(toBytes(key));
    }

    public static void storePrivateKey_X509DER(
            final PrivateKey key,
            final File f
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);
        validateDerFileExtension(f);

        // "private_key.der"
        FileUtil.save(toBytes(key), f, true);
    }

    public static void storePublicKey_X509DER(
            final PublicKey key,
            final OutputStream os
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(os);

        // "public_key.der"
        os.write(toBytes(key));
    }

    public static void storePublicKey_X509DER(
            final PublicKey key,
            final File f
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);
        validateDerFileExtension(f);

        // "public_key.der"
        FileUtil.save(toBytes(key), f, true);
    }

    public static PrivateKey loadPrivateKey_X509DER(
            final byte[] privateKeyBytes
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(privateKeyBytes);

        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static PrivateKey loadPrivateKey_X509DER(
            final InputStream is
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(is);

        return loadPrivateKey_X509DER(IOStreamUtil.copyIStoByteArray(is));
    }

    public static PrivateKey loadPrivateKey_X509DER(
            final File f
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(f);
        validateDerFileExtension(f);

        return loadPrivateKey_X509DER(FileUtil.load(f));
    }

    public static PublicKey loadPublicKey_X509DER(
            final byte[] publicKeyBytes
    )  throws IOException, GeneralSecurityException {
        Objects.requireNonNull(publicKeyBytes);

        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    public static PublicKey loadPublicKey_X509DER(
            final InputStream is
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(is);

        return loadPublicKey_X509DER(IOStreamUtil.copyIStoByteArray(is));
    }

    public static PublicKey loadPublicKey_X509DER(
            final File f
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(f);
        validateDerFileExtension(f);

        return loadPublicKey_X509DER(FileUtil.load(f));
    }


    // ------------------------------------------------------------------------
    // File IO X509 PEM
    // ------------------------------------------------------------------------

    public static void storePrivateKey_X509PEM(
            final PrivateKey key,
            final OutputStream os
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(os);

        // "private_key.der"
        final String pem = PemConverter.convertDerToPem(
                                toBytes(key),
                                PemConverter.TYPE.PrivateKey);
        os.write(pem.getBytes(StandardCharsets.US_ASCII));
    }

    public static void storePrivateKey_X509PEM(
            final PrivateKey key,
            final File f
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);
        validatePemFileExtension(f);

        // "private_key.der"
        final String pem = PemConverter.convertDerToPem(
                                toBytes(key),
                                PemConverter.TYPE.PrivateKey);
        FileUtil.save(pem.getBytes(StandardCharsets.US_ASCII), f, true);
    }

    public static void storePublicKey_X509PEM(
            final PublicKey key,
            final OutputStream os
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(os);

        // "public_key.der"
        final String pem = PemConverter.convertDerToPem(
                                toBytes(key),
                                PemConverter.TYPE.PublicKey);
        os.write(pem.getBytes(StandardCharsets.US_ASCII));
    }

    public static void storePublicKey_X509PEM(
            final PublicKey key,
            final File f
    ) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);
        validatePemFileExtension(f);

        // "public_key.der"
        final String pem = PemConverter.convertDerToPem(
                                toBytes(key),
                                PemConverter.TYPE.PublicKey);

        FileUtil.save(pem.getBytes(StandardCharsets.US_ASCII), f, true);
    }

    public static PrivateKey loadPrivateKey_X509PEM(
            final String pem
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(pem);
        validatePrivateKeyPem(pem);

        final byte[] privateKeyBytes = PemConverter.convertPemToDer(pem.getBytes(StandardCharsets.US_ASCII));

        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static PrivateKey loadPrivateKey_X509PEM(
            final InputStream is
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(is);

        final String pem = new String(IOStreamUtil.copyIStoByteArray(is), StandardCharsets.US_ASCII);
        return loadPrivateKey_X509PEM(pem);
    }

    public static PrivateKey loadPrivateKey_X509PEM(
            final File f
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(f);
        validatePemFileExtension(f);

        final String pem = new String(FileUtil.load(f), StandardCharsets.US_ASCII);
        return loadPrivateKey_X509PEM(pem);
    }

    public static PublicKey loadPublicKey_X509PEM(
            final String pem
    )  throws IOException, GeneralSecurityException {
        Objects.requireNonNull(pem);
        validatePublicKeyPem(pem);

        final byte[] publicKeyBytes = PemConverter.convertPemToDer(pem.getBytes(StandardCharsets.US_ASCII));

        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    public static PublicKey loadPublicKey_X509PEM(
            final InputStream is
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(is);

        final String pem = new String(IOStreamUtil.copyIStoByteArray(is), StandardCharsets.US_ASCII);
        return loadPublicKey_X509PEM(pem);
   }

    public static PublicKey loadPublicKey_X509PEM(
            final File f
    ) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(f);
        validatePemFileExtension(f);

        final String pem = new String(FileUtil.load(f), StandardCharsets.US_ASCII);
        return loadPublicKey_X509PEM(pem);
    }


    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static Cipher getCipher() throws GeneralSecurityException {
        return Cipher.getInstance("RSA/ECB/OAEPPadding");
    }

    private static void validateDerFileExtension(final File file) {
        Objects.requireNonNull(file);
        if (!file.getName().endsWith(".der")) {
            throw new IllegalArgumentException("A DER key file must have the file extension '.der'");
        }
    }

    private static void validatePemFileExtension(final File file) {
        Objects.requireNonNull(file);
        if (!file.getName().endsWith(".pem")) {
            throw new IllegalArgumentException("A PEM key file must have the file extension '.pem'");
        }
    }

    private static void validatePublicKeyPem(final String pem) {
        Objects.requireNonNull(pem);
        if (!PemConverter.isPem(pem, TYPE.PublicKey)) {
            throw new IllegalArgumentException("Not a public key in PEM format");
        }
    }

    private static void validatePrivateKeyPem(final String pem) {
        Objects.requireNonNull(pem);
        if (!PemConverter.isPem(pem, TYPE.PrivateKey)) {
            throw new IllegalArgumentException("Not a private key in PEM format");
        }
    }


    private static int KEY_SIZE = 2048;  // key size in bits
}
