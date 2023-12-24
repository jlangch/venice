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
package com.github.jlangch.venice.impl.util;


public class HexUtil {

    /**
     * Converts byte data to a hex string.
     *
     * <p>The following characters are used as hexadecimal digits:
     * 0123456789abcdef
     *
     * @param data the data
     * @return the converted hex string
     */
    public static String toString(final byte[] data) {
        final StringBuilder sb = new StringBuilder();

        for(int ii=0; ii<data.length; ii++) {
            String s = Integer.toHexString(0xFF & data[ii]);
            if (s.length() == 1) sb.append('0');
            sb.append(s);
        }

        return sb.toString();
    }

    /**
     * Converts byte data to a hex string.
     *
     * <p>The following characters are used as hexadecimal digits:
     * 0123456789ABCDEF
     *
     * @param data the data
     * @return the converted hex string
     */
    public static String toStringUpperCase(final byte[] data) {
        final StringBuilder sb = new StringBuilder();

        for(int ii=0; ii<data.length; ii++) {
            String s = Integer.toHexString(0xFF & data[ii]);
            if (s.length() == 1) sb.append('0');
            sb.append(s);
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Converts a hex string back to bytes.
     *
     * <p>Hexadecimal digits: 0123456789 abcdef ABCDEF
     *
     * @param hex a hex string
     * @return the converted bytes
     */
    public static byte[] toBytes(final String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("A hex string must not be null");
        }

        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "A hex string must have an even number of characters");
        }

        final byte[] data = new byte[hex.length() / 2];

        for(int ii=0; ii<hex.length() / 2; ii++) {
            final String h = hex.substring(ii * 2, ii * 2 + 2);
            data[ii] = (byte)Integer.parseInt(h, 16);
        }

        return data;
     }

}
