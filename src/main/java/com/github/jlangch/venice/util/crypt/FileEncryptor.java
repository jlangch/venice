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
import java.nio.file.Files;

import com.github.jlangch.venice.impl.util.StringUtil;


/**
 * Encrypt and decrypt files.
 */
public class FileEncryptor {

    public static void encryptFileWithPassphrase(
            final String algorithm,
            final String passphrase,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.encryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.encryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.encryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "CHACHA20-BC":
                FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] encryptFileWithPassphrase(
            final String algorithm,
            final String passphrase,
            final byte[] fileData
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.encryptFileWithPassphrase(passphrase, fileData);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.encryptFileWithPassphrase(passphrase, fileData);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.encryptFileWithPassphrase(passphrase, fileData);
            case "CHACHA20-BC":
                return FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithPassphrase(passphrase, fileData);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void encryptFileWithKey(
            final String algorithm,
            final byte[] key,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.encryptFileWithKey(key, inputFile, outputFile);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.encryptFileWithKey(key, inputFile, outputFile);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.encryptFileWithKey(key, inputFile, outputFile);
                break;
            case "CHACHA20-BC":
                FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithKey(key, inputFile, outputFile);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
   }

    public static byte[] encryptFileWithKey(
            final String algorithm,
            final byte[] key,
            final byte[] fileData
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.encryptFileWithKey(key, fileData);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.encryptFileWithKey(key, fileData);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.encryptFileWithKey(key, fileData);
            case "CHACHA20-BC":
                return FileEncryptor_ChaCha20_BouncyCastle.encryptFileWithKey(key, fileData);
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void decryptFileWithPassphrase(
            final String algorithm,
            final String passphrase,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.decryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.decryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.decryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            case "CHACHA20-BC":
                FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithPassphrase(passphrase, inputFile, outputFile);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] decryptFileWithPassphrase(
            final String algorithm,
            final String passphrase,
            final byte[] fileData
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.decryptFileWithPassphrase(passphrase, fileData);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.decryptFileWithPassphrase(passphrase, fileData);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.decryptFileWithPassphrase(passphrase, fileData);
            case "CHACHA20-BC":
                return FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithPassphrase(passphrase, fileData);
           default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static void decryptFileWithKey(
            final String algorithm,
            final byte[] key,
            final File inputFile,
            final File outputFile
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                FileEncryptor_AES256_CBC.decryptFileWithKey(key, inputFile, outputFile);
                break;
            case "AES256-GCM":
                FileEncryptor_AES256_GCM.decryptFileWithKey(key, inputFile, outputFile);
                break;
            case "CHACHA20":
                FileEncryptor_ChaCha20.decryptFileWithKey(key, inputFile, outputFile);
                break;
            case "CHACHA20-BC":
                FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithKey(key, inputFile, outputFile);
                break;
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static byte[] decryptFileWithKey(
            final String algorithm,
            final byte[] key,
            final byte[] fileData
    ) throws Exception {
        switch(StringUtil.trimToEmpty(algorithm).toUpperCase()) {
            case "AES256-CBC":
                return FileEncryptor_AES256_CBC.decryptFileWithKey(key, fileData);
            case "AES256-GCM":
                return FileEncryptor_AES256_GCM.decryptFileWithKey(key, fileData);
            case "CHACHA20":
                return FileEncryptor_ChaCha20.decryptFileWithKey(key, fileData);
            case "CHACHA20-BC":
                return FileEncryptor_ChaCha20_BouncyCastle.decryptFileWithKey(key, fileData);
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
            case "CHACHA20-BC":
                return FileEncryptor_ChaCha20_BouncyCastle.isSupported();
            default:
                throw new RuntimeException("Unsupported algorith '" + algorithm + "'!");
        }
    }

    public static boolean addBouncyCastleProvider() {
        if (Security.getProvider("BC")) {
            return false;
        }
        else {
            Security.addProvider(new BouncyCastleProvider());
            return true;
        }
    
    public static boolean identical(
    		final File file1,
    		final File file2
    ) throws Exception {
        return identical(
        		Files.readAllBytes(file1.toPath()),
        		Files.readAllBytes(file1.toPath()));
    }

    public static boolean identical(
    		final byte[] buf1,
    		final byte[] buf2
    ) throws Exception {
        if (buf1.length == buf2.length) {
        	for(int ii=0; ii<buf1.length; ii++) {
        		if (buf1[ii] != buf2[ii]) {
        			return false;
        		}
        	}
        	return true;
        }
        else {
        	return false;
        }
    }
}
