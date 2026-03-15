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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;


public class PemToDerConverter {

    public String convertDerToPem(final byte[] derBytes, final TYPE type) {
        Objects.requireNonNull(derBytes);
        Objects.requireNonNull(type);

        final String base64Encoded = Base64.getEncoder().encodeToString(derBytes);

        final String stype = getTypeString(type);

        // PEM formatting (linefeed every 64 characters)
        final String pem = "-----BEGIN " + stype + "-----\n" +
                           base64Encoded.replaceAll("(.{64})", "$1\n") +
                           "\n-----END " + stype + "-----";

        return pem;
    }

    public byte[] convertPemToDer(final byte[] pemBytes) {
        // This works for common PEM types like:
        //   • -----BEGIN CERTIFICATE-----
        //   • -----BEGIN PUBLIC KEY-----
        //   • -----BEGIN PRIVATE KEY-----
        //   • -----BEGIN X509 CRL-----

        Objects.requireNonNull(pemBytes);

        final String pem = new String(pemBytes, StandardCharsets.US_ASCII);

        final String base64 = pem
                                .replaceAll("-----BEGIN [A-Z0-9 ]+-----", "")
                                .replaceAll("-----END [A-Z0-9 ]+-----", "")
                                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }


    private static String getTypeString(final TYPE type) {
        switch(type) {
            case Certificate: return "CERTIFICATE";
            case PublicKey:   return "PUBLIC KEY";
            case PrivateKey:  return "PRIVATE KEY";
            case X509:        return "X509 CRL";
            default: throw new RuntimeException("Invalid PEM type");
        }

    }

    public static enum TYPE {
        Certificate,
        PublicKey,
        PrivateKey,
        X509;
    }

}
