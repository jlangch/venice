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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

public class CompressorTest {

    @Test
    public void test_needsCompression() {
        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        assertFalse(Compressor.off().needsCompression(data));
        assertFalse(new Compressor(-1).needsCompression(data));
        assertTrue(new Compressor(0).needsCompression(data));
        assertTrue(new Compressor(1).needsCompression(data));
        assertTrue(new Compressor(2).needsCompression(data));
        assertTrue(new Compressor(3).needsCompression(data));
        assertTrue(new Compressor(4).needsCompression(data));
        assertTrue(new Compressor(5).needsCompression(data));
        assertFalse(new Compressor(100).needsCompression(data));
   }

    @Test
    public void test_cutoffSize() {
        assertEquals(-1L, Compressor.off().cutoffSize());
        assertEquals(-1L, new Compressor(-1).cutoffSize());
        assertEquals(-1L, new Compressor(-5).cutoffSize());
        assertEquals(0L, new Compressor(0).cutoffSize());
        assertEquals(1L, new Compressor(1).cutoffSize());
        assertEquals(100L, new Compressor(100).cutoffSize());
   }

    @Test
    public void test_no_compression() {
        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        assertArrayEquals(data, Compressor.off().compress(data));
        assertArrayEquals(data, new Compressor(-1).compress(data));
        assertArrayEquals(data, new Compressor(100).compress(data));

        assertArrayEquals(data, Compressor.off().compress(data, false));
        assertArrayEquals(data, new Compressor(-1).compress(data, false));
        assertArrayEquals(data, new Compressor(0).compress(data, false));
    }

    @Test
    public void test_no_decompression() {
        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        assertArrayEquals(data, Compressor.off().decompress(data, false));
        assertArrayEquals(data, new Compressor(-1).decompress(data, false));
        assertArrayEquals(data, new Compressor(0).decompress(data, false));
        assertArrayEquals(data, new Compressor(100).decompress(data, false));
    }

    @Test
    public void test_compress_decompress_1() {
        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        Compressor compressor;

        compressor = Compressor.off();
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), false));

        compressor = new Compressor(-1);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), false));

        compressor = new Compressor(0);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), true));

        compressor = new Compressor(4);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), true));

        compressor = new Compressor(5);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), true));

        compressor = new Compressor(6);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), false));

        compressor = new Compressor(100);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data), false));
    }

    @Test
    public void test_compress_decompress_2() {
        final byte[] data = "hello".getBytes(Charset.forName("UTF-8"));

        Compressor compressor;

        compressor = Compressor.off();
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(-1);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(0);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(4);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(5);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(6);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));

        compressor = new Compressor(100);
        assertArrayEquals(data, compressor.decompress(compressor.compress(data, true), true));
    }

}
