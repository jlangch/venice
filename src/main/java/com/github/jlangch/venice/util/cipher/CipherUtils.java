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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class CipherUtils {

    public static boolean isEmpty(final byte[] data) {
        return data == null || data.length == 0;
    }

    public static byte[] emptyToNull(final byte[] data) {
        return data != null && data.length == 0 ? null : data;
    }

    public static byte[] randomIV(final int ivLen) {
        byte[] iv = new byte[ivLen];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static byte[] concat(final byte[] buf1, final byte[] buf2) {
        byte[] out = new byte[buf1.length + buf2.length];
        System.arraycopy(buf1, 0, out, 0, buf1.length);
        System.arraycopy(buf2, 0, out, buf1.length, buf2.length);
        return out;
    }

    public static byte[] deriveKeyFromPassphrase(
            final String passphrase,
            final String secretKeyFactoryName,
            final byte[] salt,
            final int iterationCount,
            final int keyLength
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyFactoryName);
        return factory.generateSecret(spec).getEncoded();
    }

}
