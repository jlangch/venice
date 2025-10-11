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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.crypt.FileEncryptor_AES256_GCM;
import com.github.jlangch.venice.util.dh.DiffieHellmanSharedSecret;


public class Encryptor {

    private Encryptor(final DiffieHellmanSharedSecret secret) {
        this.secret = secret;
    }


    public static Encryptor aes(final DiffieHellmanSharedSecret secret) {
        return new Encryptor(secret);
    }

    public static Encryptor off() {
        return new Encryptor(null);
    }


    public byte[] encrypt(final byte[] data) {
        if (isActive()) {
            try {
                return FileEncryptor_AES256_GCM.encryptFileWithPassphrase(secret.getSecretBase64(), data);
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
        if (isActive()) {
            try {
                return FileEncryptor_AES256_GCM.decryptFileWithPassphrase(secret.getSecretBase64(), data);
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
        return secret != null;
    }


    private final DiffieHellmanSharedSecret secret;
}
