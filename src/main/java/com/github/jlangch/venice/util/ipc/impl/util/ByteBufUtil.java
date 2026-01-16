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
package com.github.jlangch.venice.util.ipc.impl.util;


public class ByteBufUtil {

    public static void writeInt(byte[] buf, int pos, int v) {
        for (int i = 3; i >= 0; i--) {
            buf[pos++] = (byte) (v >>> (i * 8));
        }
    }

    public static int readInt(byte[] buf, int pos) {
        int v = 0;
        for (int i = 0; i < 4; i++) {
            v = (v << 8) | (buf[pos++] & 0xff);
        }
        return v;
    }

    public static void writeLong(byte[] buf, int pos, long v) {
        for (int i = 7; i >= 0; i--) {
            buf[pos++] = (byte) (v >>> (i * 8));
        }
    }

    public static long readLong(byte[] buf, int pos) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v = (v << 8) | (buf[pos++] & 0xff);
        }
        return v;
    }

}
