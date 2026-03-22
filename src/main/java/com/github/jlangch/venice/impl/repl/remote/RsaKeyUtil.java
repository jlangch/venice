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
package com.github.jlangch.venice.impl.repl.remote;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.util.crypt.RSA;


public class RsaKeyUtil {

    public static KeyPair createKeyPair(final PublicKey pubKey, final PrivateKey privKey) {
        return pubKey != null && privKey != null
                ? new KeyPair(pubKey, privKey)
                : null;
    }

    public static PublicKey loadPublicKey(final String keyFile) {
        if (StringUtil.isNotBlank(keyFile)) {
            try {
                return RSA.loadPublicKey_X509PEM(new File(keyFile));
            }
            catch(Exception ex) {
                throw new VncException("Failed to load RSA public key from " + keyFile);
            }
        }
        else {
            return null;
        }
    }

    public static PrivateKey loadPrivateKey(final String keyFile) {
        if (StringUtil.isNotBlank(keyFile)) {
            try {
                return RSA.loadPrivateKey_X509PEM(new File(keyFile));
            }
            catch(Exception ex) {
                throw new VncException("Failed to load RSA private key from " + keyFile);
            }
        }
        else {
            return null;
        }
    }

}
