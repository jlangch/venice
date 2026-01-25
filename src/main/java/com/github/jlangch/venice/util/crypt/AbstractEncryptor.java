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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;

import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.util.Base64Schema;


public abstract class AbstractEncryptor implements IEncryptor{

    @Override
    abstract public byte[] encrypt(final byte[] data);

    @Override
    abstract public byte[] decrypt(final byte[] data);


    @Override
    public byte[] encrypt(final byte[] data, final byte[] aad) {
        throw new RuntimeException(
                "Encryption with AAD (authenticated additional data) "
                + "is  not supported by this encryptor!");
    }

    @Override
    public byte[] decrypt(final byte[] data, final byte[] aad) {
        throw new RuntimeException(
                "Encryption with AAD (authenticated additional data) "
                + "is  not supported by this encryptor!");
    }


    @Override
    public String encrypt(
            final String text,
            final Base64Schema schema
    ) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(schema);

        final byte[] encryptedBytes = encrypt(text.getBytes(UTF_8));
        return new String(encoder(schema).encode(encryptedBytes), UTF_8);
    }

    @Override
    public String decrypt(
            final String base64,
            final Base64Schema schema
    ) {
        Objects.requireNonNull(base64);
        Objects.requireNonNull(schema);

        final byte[] encryptedBytes = decoder(schema).decode(base64.getBytes(UTF_8));
        final byte[] decryptedBytes = decrypt(encryptedBytes);
        return new String(decryptedBytes, UTF_8);
    }


    @Override
    public void encrypt(
            final File inputFile,
            final File outputFile,
            final boolean overwrite
    ) {
        Objects.requireNonNull(inputFile);
        Objects.requireNonNull(outputFile);

        try {
            final byte[] data = Files.readAllBytes(inputFile.toPath());

            Files.write(
                    outputFile.toPath(),
                    encrypt(data),
                    getOpenOptions(overwrite));
        }
        catch(Exception ex) {
            throw new FileException("Failed to encrypt file " + inputFile, ex);
        }
    }

    @Override
    public void decrypt(
            final File inputFile,
            final File outputFile,
            final boolean overwrite
    ) {
        Objects.requireNonNull(inputFile);
        Objects.requireNonNull(outputFile);

        try {
            final byte[] data = Files.readAllBytes(inputFile.toPath());

            Files.write(
                    outputFile.toPath(),
                    decrypt(data),
                    getOpenOptions(overwrite));
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt file " + inputFile, ex);
        }
    }


    private Encoder encoder(final Base64Schema scheme) {
        switch(scheme) {
            case Standard: return Base64.getEncoder();
            case UrlSafe:  return Base64.getUrlEncoder();
            default:
                throw new IllegalArgumentException("Invalid Base64 scheme '" + scheme.name() + "'");
        }
    }

    private Decoder decoder(final Base64Schema scheme) {
        switch(scheme) {
            case Standard: return Base64.getDecoder();
            case UrlSafe:  return Base64.getUrlDecoder();
            default:
                throw new IllegalArgumentException("Invalid Base64 scheme '" + scheme.name() + "'");
        }
    }

    private OpenOption[] getOpenOptions(final boolean overwrite) {
        return overwrite
                ? new OpenOption[] {
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING }
                : new OpenOption[] {
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE_NEW};
    }
}
