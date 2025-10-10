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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyAgreement;


public class DiffieHellmanKeys {

    private DiffieHellmanKeys(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public static DiffieHellmanKeys create() {
        try {
            // Generate a Diffie-Hellman key pair
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            return new DiffieHellmanKeys(keyPairGenerator.generateKeyPair());
        }
        catch(NoSuchAlgorithmException ex) {
            throw new RuntimeException("Failed to create Diffie-Hellman keys", ex);
        }
    }

    public DiffieHellmanSharedSecret generateSharedSecret(
            final String otherPartyPublicKey
    ) {
        try {
            // Decode the other party's public key
            final byte[] publicKeyBytes = Base64.getDecoder().decode(otherPartyPublicKey);
            final KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
            final PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Calculate the shared secret using this party's private key and the other party's public key
            final KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(publicKey, true);
            return new DiffieHellmanSharedSecret(keyAgreement.generateSecret());
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to create Diffie-Hellman shared secret", ex);
        }
    }


    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(getPublicKey());
    }

    public byte[] getPublicKey() {
        return keyPair.getPublic().getEncoded();
    }


    private final KeyPair keyPair;
}
