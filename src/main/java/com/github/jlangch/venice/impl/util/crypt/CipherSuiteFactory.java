/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.util.crypt;

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Cipher suite factory
 */
public class CipherSuiteFactory {
	
	/**
	 * Creates a cipher suite.
	 * 
	 * @param algorithm An algorithm {"DES", "3DES", "AES256"}
	 * @param passphrase A passphrase
	 * @param urlSafe 
	 * 			if true this encoder will emit '-' and '_' instead of the 
	 * 			usual '+' and '/' characters. 
	 * 			Note: no padding is added when encoding using the URL-safe alphabet.
	 * @return A cipher suite
	 * @throws EncryptionException if the algorithm is not supported
	 */
	public static CipherSuite create(
			final String algorithm, 
			final String passphrase,
			final boolean urlSafe
	) {
		try {
			if ("DES".equalsIgnoreCase(algorithm)) {
				return createCipherSuite(passphrase, "PBEWithMD5AndDES", "{DES}", urlSafe);
			}
			else if ("PBEWithMD5AndDES".equalsIgnoreCase(algorithm)) {
				return createCipherSuite(passphrase, "PBEWithMD5AndDES", "{DES}", urlSafe);
			}
			else if ("3DES".equalsIgnoreCase(algorithm)) {
				return createCipherSuite(passphrase, "PBEWithMD5AndTripleDES", "{3DES}", urlSafe);
			}
			else if ("PBEWithMD5AndTripleDES".equalsIgnoreCase(algorithm)) {
				return createCipherSuite(passphrase, "PBEWithMD5AndTripleDES", "{3DES}", urlSafe);
			}
			else if ("AES256".equalsIgnoreCase(algorithm)) {
				return createCipherSuite_AES256(passphrase, "AES256", "{AES256}", urlSafe);
			}
			else {
				throw new EncryptionException("Invalid cipher algorithm name '" + algorithm + "'");
			}
        }
		catch (Exception ex) {
			throw new EncryptionException("Failed to create cipher suite.", ex);
		} 
	}

	private static CipherSuite createCipherSuite(
			final String passphrase, 
			final String algorithm, 
			final String prefix,
			final boolean urlSafe
	) throws GeneralSecurityException {
        // Create the key
        KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), SALT, ITERATIONS);
        SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
        Cipher cipherEncrypt = Cipher.getInstance(key.getAlgorithm());
        Cipher cipherDecrypt = Cipher.getInstance(key.getAlgorithm());

        // Prepare the parameter to the ciphers
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATIONS);

        // Create the ciphers
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        cipherDecrypt.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return new CipherSuite(cipherEncrypt, cipherDecrypt, prefix, urlSafe);
	}


	private static CipherSuite createCipherSuite_AES256(
			final String passphrase, 
			final String algorithm, 
			final String prefix,
			final boolean urlSafe
	) throws GeneralSecurityException {
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
         
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), SALT, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
         
        Cipher cipherEncrypt = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
		return new CipherSuite(cipherEncrypt, cipherDecrypt, prefix, urlSafe);
	}

	
    private static final byte[] SALT = {
        (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
    };

    private static final int ITERATIONS = 19;
}
