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
package com.github.jlangch.venice.util.ssl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public abstract class SSLSocketFactoryUtil {

    public static SSLSocketFactory getDefault() {
        return (SSLSocketFactory)SSLSocketFactory.getDefault();
    }

    public static SSLSocketFactory create(
            final TlsProtocol protocol
    ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return create(protocol, null);
    }

    public static SSLSocketFactory create(
            final TrustManager tm
    ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return create(null, tm);
    }

    public static SSLSocketFactory create(
            final TlsProtocol protocol,
            final TrustManager tm
    ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext =  protocol == null
                                        ? SSLContext.getDefault()
                                        : SSLContext.getInstance(protocol.toString());

        sslContext.init(
            null,
            tm == null ? null : new TrustManager[] { tm },
            new SecureRandom());

        return sslContext.getSocketFactory();
    }
}
