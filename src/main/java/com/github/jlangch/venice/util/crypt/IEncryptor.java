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

import com.github.jlangch.venice.util.Base64Schema;


public interface IEncryptor {

    /**
     * Encrypts binary data
     *
     * @param data the binary data to encrypt
     * @return the encrypted binary data
     */
    byte[] encrypt(final byte[] data);

    /**
     * Decrypts binary data
     *
     * @param data the binary data to decrypt
     * @return the decrypted binary data
     */
    byte[] decrypt(final byte[] data);


    /**
     * Encrypts binary data with AAD (authenticated additional data)
     *
     * @param data the binary data to encrypt
     * @param aad the binary authenticated additional data
     * @return the encrypted binary data
     */
    byte[] encrypt(final byte[] data, final byte[] aad);

    /**
     * Decrypts binary data with AAD (authenticated additional data)
     *
     * @param data the binary data to decrypt
     * @param aad the binary authenticated additional data
     * @return the decrypted binary data
     */
    byte[] decrypt(final byte[] data, final byte[] aad);


    /**
     * Encrypts a string to a Base64 encoded encrypted data
     *
     * @param text the string to encrypt
     * @param schema the Base64 schema to use
     * @return the Base64 encoded encrypted data
     */
    String encrypt(final String text, final Base64Schema schema);

    /**
     * Decrypts a Base64 encoded encrypted data to a string
     *
     * @param base64 the Base64 encoded encrypted data
     * @param schema the Base64 schema to use
     * @return the decrypted string
     */
    String decrypt(final String base64, final Base64Schema schema);

    /**
     * Encrypts a file
     *
     * @param inputFile the file to encrypt
     * @param outputFile the encrypted output file
     * @param overwrite if <code>true</code> overwrites an existing output
     *                  file else throws a FileException
     */
    void encrypt(
            final File inputFile,
            final File outputFile,
            final boolean overwrite
    );

    /**
     * Decrypts a file
     *
     * @param inputFile the file to decrypt
     * @param outputFile the decrypted output file
     * @param overwrite if <code>true</code> overwrites an existing output
     *                  file else throws a FileException
     */
    void decrypt(
            final File inputFile,
            final File outputFile,
            final boolean overwrite
    );

 }
