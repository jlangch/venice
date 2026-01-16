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

import static com.github.jlangch.venice.util.ipc.impl.util.ByteBufUtil.readInt;
import static com.github.jlangch.venice.util.ipc.impl.util.ByteBufUtil.readLong;
import static com.github.jlangch.venice.util.ipc.impl.util.ByteBufUtil.writeInt;
import static com.github.jlangch.venice.util.ipc.impl.util.ByteBufUtil.writeLong;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class ByteBufUtilTest {

    @Test
    public void test_int() {
        final byte[] buf = new byte[4];

        writeInt(buf, 0, 0x00000);
        assertEquals(0x00000, readInt(buf, 0));

        writeInt(buf, 0, 0x000AA);
        assertEquals(0x000AA, readInt(buf, 0));

        writeInt(buf, 0, 0x0AA00);
        assertEquals(0x0AA00, readInt(buf, 0));

        writeInt(buf, 0, 0x055AA);
        assertEquals(0x055AA, readInt(buf, 0));

        writeInt(buf, 0, 0x0AA55);
        assertEquals(0x0AA55, readInt(buf, 0));
   }

    @Test
    public void test_long() {
        final byte[] buf = new byte[8];

        writeLong(buf, 0, 0x00000000);
        assertEquals(0x00000000, readLong(buf, 0));

        writeLong(buf, 0, 0x000000AA);
        assertEquals(0x000000AA, readLong(buf, 0));

        writeLong(buf, 0, 0x0000AA00);
        assertEquals(0x0000AA00, readLong(buf, 0));

        writeLong(buf, 0, 0x000055AA);
        assertEquals(0x000055AA, readLong(buf, 0));

        writeLong(buf, 0, 0x0000AA55);
        assertEquals(0x0000AA55, readLong(buf, 0));

        writeLong(buf, 0, 0x55AA55AA);
        assertEquals(0x055AA55AA, readLong(buf, 0));

        writeLong(buf, 0, 0xAA55AA55);
        assertEquals(0xAA55AA55, readLong(buf, 0));
   }

}
