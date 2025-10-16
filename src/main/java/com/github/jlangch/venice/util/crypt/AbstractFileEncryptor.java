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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.github.jlangch.venice.FileException;


public abstract class AbstractFileEncryptor implements IFileEncryptor{

    @Override
    abstract public byte[] encrypt(final byte[] data);

    @Override
    abstract public byte[] decrypt(final byte[] data);

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
                    overwrite ? StandardOpenOption.TRUNCATE_EXISTING
                              : StandardOpenOption.CREATE_NEW);
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
                    overwrite ? StandardOpenOption.TRUNCATE_EXISTING
                              : StandardOpenOption.CREATE_NEW);
        }
        catch(Exception ex) {
            throw new FileException("Failed to decrypt file " + inputFile, ex);
        }
    }

 }
