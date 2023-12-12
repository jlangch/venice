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
import java.security.MessageDigest;
import java.util.Base64;


/**
 * Computes hashes from files and verifies file hashes to detect modified
 * files.
 *
 * <p>
 * Supports any algorithms supplied by the Java VM like MD5, SHA-1,
 * SHA-512, ..
 *
 * <p>
 * Note: MD5 is known as a weak cryptographic algorithm
 */
public class FileHasher {

    public static String hashFile(
            final String algorithm,
            final String salt,
            final File inputFile
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Hash
        return hashFile(algorithm, salt, fileData);
    }

    public static String hashFile(
            final String algorithm,
            final String salt,
            final byte[] fileData
    ) throws Exception {
        // Init digest
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();

        // Supply data
        md.update(salt.getBytes("UTF-8"));
        md.update(fileData);

        // Get digest
        return encodeBase64(md.digest());
    }

    public static boolean verifyFileHash(
            final String algorithm,
            final String salt,
            final File inputFile,
            final String hash
    ) throws Exception {
        // Read file data
        byte[] fileData = Files.readAllBytes(inputFile.toPath());

        // Verify hash
        return verifyFileHash(algorithm, salt, fileData, hash);
    }

    public static boolean verifyFileHash(
            final String algorithm,
            final String salt,
            final byte[] fileData,
            final String hash
    ) throws Exception {
        // Hash file data
        String fileDataHash = hashFile(algorithm, salt, fileData);

        // Verify  digest
        return hash.equals(fileDataHash);
    }



    public static String encodeBase64(final byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeBase64(final String data) {
        return Base64.getDecoder().decode(data);
    }
}
