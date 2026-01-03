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
package com.github.jlangch.venice.util.dh;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class DiffieHellmanSharedSecret implements AutoCloseable {

    public DiffieHellmanSharedSecret(final byte[] secret) {
        Objects.requireNonNull(secret);

        this.secret = secret;
    }

    public byte[] getSecret() {
        if (closed.get()) {
            throw new IllegalStateException("Secret already closed.");
        }

        return secret;
    }

    public String getSecretBase64() {
        if (closed.get()) {
            throw new IllegalStateException("Secret already closed.");
        }

        return Base64.getEncoder().encodeToString(secret);
    }

    @Override
    public void close() {
        Arrays.fill(secret, (byte) 0);
        closed.set(true);
    }


    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final byte[] secret;
}
