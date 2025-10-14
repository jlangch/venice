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
package com.github.jlangch.venice.util.cipher;

import java.security.GeneralSecurityException;



public interface ICipher {

    /**
     * Encrypts data
     *
     * @param data the binary data to encrypt
     * @return the encrypted binary data
     * @throws GeneralSecurityException on encryption errors
     */
    byte[] encrypt(final byte[] data) throws GeneralSecurityException;

    /**
     * Decrypts data
     *
     * @param data the binary data to decrypt
     * @return the decrypted binary data
     * @throws GeneralSecurityException on decryption errors
     */
    byte[] decrypt(final byte[] data) throws GeneralSecurityException;


    /**
     * Encrypts a string
     *
     * @param data the string to encrypt
     * @param scheme the Base64 scheme to use
     * @return the Base64 encode encrypted data
     * @throws GeneralSecurityException on encryption errors
     */
    String encrypt(final String data, final Base64Scheme scheme) throws GeneralSecurityException;

    /**
     * Decrypts Base64 encoded encrypted data
     *
     * @param dataBase64 the Base64 encoded encrypted string
     * @param scheme the Base64 scheme to use
     * @return the decrypted string
     * @throws GeneralSecurityException on encryption errors
     */
    String decrypt(final String dataBase64, final Base64Scheme scheme) throws GeneralSecurityException;

 }
