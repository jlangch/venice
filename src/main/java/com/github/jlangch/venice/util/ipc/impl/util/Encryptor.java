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
package com.github.jlangch.venice.util.ipc.impl.util;

import java.util.Objects;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.crypt.Encryptor_AES256_GCM;
import com.github.jlangch.venice.util.crypt.IEncryptor;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


public class Encryptor {

    private Encryptor(final IEncryptor ecryptor) {
        this.ecryptor = ecryptor;
    }


    public static Encryptor aes(final DiffieHellmanSharedSecret secret) {
        Objects.requireNonNull(secret);

        try {
            return new Encryptor(Encryptor_AES256_GCM.create(secret.getSecretBase64()));
        }
        catch(Exception ex) {
            throw new VncException("Failed to createencryptor", ex);
        }
    }

    public static Encryptor off() {
        return new Encryptor(null);
    }


    public byte[] encrypt(final byte[] data) {
        return encrypt(data, isActive());
    }

    public byte[] encrypt(final byte[] data, final boolean encrypt) {
        Objects.requireNonNull(data);
        if (encrypt) {
            try {
                return ecryptor.encrypt(data);
            }
            catch(Exception ex) {
                throw new VncException("Failed to encrypt message payload data", ex);
            }
        }
        else {
            return data;
        }
    }

    public byte[] decrypt(final byte[] data) {
        return decrypt(data, isActive());
    }

    public byte[] decrypt(final byte[] data, final boolean decrypt) {
        Objects.requireNonNull(data);
        if (decrypt) {
            try {
                return ecryptor.decrypt(data);
            }
            catch(Exception ex) {
                throw new VncException("Failed to decrypt message payload data", ex);
            }
        }
        else {
            return data;
        }
    }

    public boolean isActive() {
        return ecryptor != null;
    }


    private final IEncryptor ecryptor;
}
