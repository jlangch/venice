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
package com.github.jlangch.venice.util.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import javax.net.ssl.X509TrustManager;


public class Server_X509TrustManager implements X509TrustManager {

    public Server_X509TrustManager(
            final BiPredicate<List<X509Certificate>, String> checkFn
    ) {
        this.checkFn = checkFn;
    }

    @Override
    public void checkClientTrusted(
            final X509Certificate[] chain,
            final String authType
    ) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(
            final X509Certificate[] chain,
            final String authType
    ) throws CertificateException {
        if (checkFn != null) {
            try {
                if (!checkFn.test(Arrays.asList(chain), authType)) {
                    throw new CertificateException("Server not trusted");
                }
            }
            catch(Exception ex) {
                throw new CertificateException(ex.getMessage());
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    final BiPredicate<List<X509Certificate>, String> checkFn;
}
