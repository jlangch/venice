/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files.
 */
public class FileEncryptor {

    public static void encryptFileWithPassphrase(
            final String algorithm,
            final File inputFile,
            final File outputFile,
            final String passphrase
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.encryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.encryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.encryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            case "CHACHA20-BOUNCYCASTLE":
                FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] encryptFileWithPassphrase(
            final String algorithm,
            final byte[] fileData,
            final String passphrase
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.encryptFileWithPassphrase(fileData, passphrase);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.encryptFileWithPassphrase(fileData, passphrase);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.encryptFileWithPassphrase(fileData, passphrase);
            case "CHACHA20-BOUNCYCASTLE":
                return FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithPassphrase(fileData, passphrase);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void encryptFileWithKey(
            final String algorithm,
            final File inputFile,
            final File outputFile,
            final byte[] key
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.encryptFileWithKey(inputFile, outputFile, key);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.encryptFileWithKey(inputFile, outputFile, key);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.encryptFileWithKey(inputFile, outputFile, key);
                break;
            case "CHACHA20-BOUNCYCASTLE":
                FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithKey(inputFile, outputFile, key);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
   }

    public static byte[] encryptFileWithKey(
            final String algorithm,
            final byte[] fileData,
            final byte[] key
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.encryptFileWithKey(fileData, key);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.encryptFileWithKey(fileData, key);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.encryptFileWithKey(fileData, key);
            case "CHACHA20-BOUNCYCASTLE":
                return FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithKey(fileData, key);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void decryptFileWithPassphrase(
            final String algorithm,
            final File inputFile,
            final File outputFile,
            final String passphrase
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.decryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.decryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.decryptFileWithPassphrase(inputFile, outputFile, passphrase);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] decryptFileWithPassphrase(
            final String algorithm,
            final byte[] fileData,
            final String passphrase
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.decryptFileWithPassphrase(fileData, passphrase);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.decryptFileWithPassphrase(fileData, passphrase);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.decryptFileWithPassphrase(fileData, passphrase);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void decryptFileWithKey(
            final String algorithm,
            final File inputFile,
            final File outputFile,
            final byte[] key
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.decryptFileWithKey(inputFile, outputFile, key);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.decryptFileWithKey(inputFile, outputFile, key);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.decryptFileWithKey(inputFile, outputFile, key);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] decryptFileWithKey(
            final String algorithm,
            final byte[] fileData,
            final byte[] key
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.decryptFileWithKey(fileData, key);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.decryptFileWithKey(fileData, key);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.decryptFileWithKey(fileData, key);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static boolean supports(
    		final String algorithm
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.isSupported();
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.isSupported();
            case "CHACHA20":
                return FileEncryptor_ChaCha20.isSupported();
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }
}
