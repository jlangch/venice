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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;


public abstract class AbstractCipher implements ICipher {

    @Override
    public String encrypt(final String data, final Base64Scheme scheme)
    throws GeneralSecurityException {
        Objects.requireNonNull(data);
        Objects.requireNonNull(scheme);

        final byte[] encryptedBytes = encrypt(data.getBytes(UTF_8));
        return new String(encoder(scheme).encode(encryptedBytes), UTF_8);
     }

    @Override
    public  String decrypt(final String dataBase64, final Base64Scheme scheme)
    throws GeneralSecurityException {
        Objects.requireNonNull(dataBase64);
        Objects.requireNonNull(scheme);

        final byte[] encryptedBytes = decoder(scheme).decode(dataBase64.getBytes(UTF_8));
        final byte[] decryptedBytes = decrypt(encryptedBytes);
        return new String(decryptedBytes, UTF_8);
    }


    private Encoder encoder(final Base64Scheme scheme) {
        switch(scheme) {
            case Standard: return Base64.getEncoder();
            case UrlSafe:  return Base64.getUrlEncoder();
            default:
                throw new IllegalArgumentException("Invalid Base64 scheme '" + scheme.name() + "'");
        }
    }

    private Decoder decoder(final Base64Scheme scheme) {
        switch(scheme) {
            case Standard: return Base64.getDecoder();
            case UrlSafe:  return Base64.getUrlDecoder();
            default:
                throw new IllegalArgumentException("Invalid Base64 scheme '" + scheme.name() + "'");
        }
    }

}
