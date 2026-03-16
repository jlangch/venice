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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.crypt.PemConverter.TYPE;


public class PemConverter_Test {

    @Test
    public void test_convert_small() throws Exception {
        final byte[] b = new byte[6];
        new Random().nextBytes(b);

        final String pem = PemConverter.convertDerToPem(b, TYPE.PublicKey);

        assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"));

        final byte[] data = PemConverter.convertPemToDer(pem.getBytes(StandardCharsets.US_ASCII));

        assertArrayEquals(b, data);
    }

    @Test
    public void test_convert_large() throws Exception {
        final byte[] b = new byte[1012];
        new Random().nextBytes(b);

        final String pem = PemConverter.convertDerToPem(b, TYPE.PublicKey);
        final byte[] data = PemConverter.convertPemToDer(pem.getBytes(StandardCharsets.US_ASCII));

        assertArrayEquals(b, data);
    }

}
