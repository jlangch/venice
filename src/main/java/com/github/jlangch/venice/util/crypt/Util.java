/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class Util {

    public static byte[] deriveKeyFromPassphrase(
            final String passphrase,
            final byte[] salt,
            final int iterationCount,
            final int keyLength
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }


    public static Class<?> classForName(final String name) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return Class.forName(name, true, contextClassLoader);
            }
            catch(Throwable ex) {
                // try next with current class loader
            }
        }

        // current class loader
        try {
            return Class.forName(name);
        }
        catch(Throwable ex) {
            throw new RuntimeException(
                        String.format("Failed to load class '%s'", name),
                        ex);
        }
    }


    public static boolean hasClass(final String classname) {
        try {
            final Class<?> clazz = Util.classForName(classname);
            return clazz != null;
        }
        catch(Exception ex) {
            return false;
        }
    }

}
