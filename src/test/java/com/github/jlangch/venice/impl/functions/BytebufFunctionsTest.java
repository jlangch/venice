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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class BytebufFunctionsTest {

    @Test
    public void test_bytebuf() {
        final Venice venice = new Venice();

        assertArrayEquals(new byte[0], ((ByteBuffer)venice.eval("(bytebuf)")).array());
        assertArrayEquals(new byte[] {0,1,2}, ((ByteBuffer)venice.eval("(bytebuf [0 1 2])")).array());
        assertEquals("(0 1 2)", venice.eval("(str (into '() (bytebuf [0 1 2])))"));
        assertEquals("(97 98 99)", venice.eval("(str (into '() (bytebuf \"abc\")))"));
        assertEquals("[0 1 2]", venice.eval("(str (into [] (bytebuf [0 1 2])))"));
        assertEquals("[97 98 99]", venice.eval("(str (into [] (bytebuf \"abc\")))"));
    }

    @Test
    public void test_bytebuf_allocate() {
        final Venice venice = new Venice();

        assertArrayEquals(new byte[] {0,0}, ((ByteBuffer)venice.eval("(bytebuf-allocate 2)")).array());
    }

    @Test
    public void test_bytebuf_Q() {
        final Venice venice = new Venice();

        assertTrue((Boolean)venice.eval("(bytebuf? (bytebuf))"));
        assertFalse((Boolean)venice.eval("(bytebuf? 1)"));
    }

    @Test
    public void test_bytebuf_merge() {
        final Venice venice = new Venice();

        assertEquals("[]", venice.eval("(str (into [] (bytebuf-merge (bytebuf))))"));
        assertEquals("[]", venice.eval("(str (into [] (bytebuf-merge (bytebuf) (bytebuf))))"));
        assertEquals("[]", venice.eval("(str (into [] (bytebuf-merge (bytebuf) (bytebuf) (bytebuf))))"));

        assertEquals("[]", venice.eval("(str (into [] (bytebuf-merge (bytebuf))))"));
        assertEquals("[1]", venice.eval("(str (into [] (bytebuf-merge (bytebuf) (bytebuf [1]))))"));
        assertEquals("[1 2]", venice.eval("(str (into [] (bytebuf-merge (bytebuf) (bytebuf [1]) (bytebuf [2]))))"));

        assertEquals("[1]", venice.eval("(str (into [] (bytebuf-merge (bytebuf [1]))))"));
        assertEquals("[1 2 3]", venice.eval("(str (into [] (bytebuf-merge (bytebuf [1]) (bytebuf [2 3]))))"));
        assertEquals("[1 2 3 4 5 6]", venice.eval("(str (into [] (bytebuf-merge (bytebuf [1]) (bytebuf [2 3]) (bytebuf [4 5 6]))))"));
    }

    @Test
    public void test_bytebuf_order() {
        final Venice venice = new Venice();

        assertEquals("big-endian", venice.eval("(let [buf (bytebuf)]                    \n" +
                                               "  (bytebuf-byte-order! buf :big-endian) \n" +
                                               "  (bytebuf-byte-order buf))             "));

        assertEquals("little-endian", venice.eval("(let [buf (bytebuf)]                       \n" +
                                                  "  (bytebuf-byte-order! buf :little-endian) \n" +
                                                  "  (bytebuf-byte-order buf))                "));
    }

    @Test
    public void test_bytebuf_from_string() {
        final Venice venice = new Venice();

        assertArrayEquals(
                new byte[] {97,98,99,100,101,102},
                ((ByteBuffer)venice.eval("(bytebuf-from-string \"abcdef\" :UTF-8)")).array());

        // limit to 3 bytes
        assertArrayEquals(
                new byte[] {97,98,99},
                ((ByteBuffer)venice.eval("(bytebuf-from-string \"abcdef\" :UTF-8 3 0x00)")).array());

        // fill up to 10 bytes
        assertArrayEquals(
                new byte[] {97,98,99,100,101,102,0,0,0,0},
                ((ByteBuffer)venice.eval("(bytebuf-from-string \"abcdef\" :UTF-8 10 0x00)")).array());

        assertArrayEquals(
                new byte[] {97,98,99,100,101,102,5,5},
                ((ByteBuffer)venice.eval("(bytebuf-from-string \"abcdef\" :UTF-8 8 0x05)")).array());
    }

    @Test
    public void test_bytebuf_to_string() {
        final Venice venice = new Venice();

        assertEquals("abcdef",  venice.eval("(bytebuf-to-string (bytebuf [97 98 99 100 101 102]) :UTF-8)"));
    }

    @Test
    public void test_bytebuf_to_list() {
        final Venice venice = new Venice();

        assertEquals("(97I 98I 99I 100I)",  venice.eval("(pr-str (doall (bytebuf-to-list (bytebuf [97 98 99 100]))))"));
    }

    @Test
    public void test_bytebuf_sub() {
        final Venice venice = new Venice();

        assertArrayEquals(new byte[] {3,4,5}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 3)")).array());
        assertArrayEquals(new byte[] {0,1,2}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 0 3)")).array());
        assertArrayEquals(new byte[] {2,3,4}, ((ByteBuffer)venice.eval("(bytebuf-sub (bytebuf [0 1 2 3 4 5]) 2 5)")).array());
    }

    @Test
    public void test_bytebuf_get_byte() {
        final Venice venice = new Venice();

        assertEquals(
            10,
            venice.eval(
                "(-> (bytebuf-allocate 4)   \n" +
                "    (bytebuf-put-byte! 10)  \n" +
                "    (bytebuf-put-byte! 20)  \n" +
                "    (bytebuf-get-byte 0))"));
    }

    @Test
    public void test_bytebuf_get_int() {
        final Venice venice = new Venice();

        assertEquals(
            10203040,
            venice.eval(
                "(let [buf (bytebuf-allocate 4)]         \n" +
                "  (bytebuf-put-int! buf 10203040I)      \n" +
                "  (bytebuf-get-int buf 0))                "));
    }

    @Test
    public void test_bytebuf_get_int_endian() {
        final Venice venice = new Venice();

        assertEquals(
            "(0I 155I 175I 160I)",
            venice.eval(
                "(let [buf (bytebuf-allocate 4)]            \n" +
                "  (bytebuf-byte-order! buf :big-endian)    \n" +
                "  (bytebuf-put-int! buf 10203040I)         \n" +
                "  (str (doall (bytebuf-to-list buf 0))))   "));

        assertEquals(
            "(160I 175I 155I 0I)",
            venice.eval(
                "(let [buf (bytebuf-allocate 4)]            \n" +
                "  (bytebuf-byte-order! buf :little-endian) \n" +
                "  (bytebuf-put-int! buf 10203040I)         \n" +
                "  (str (doall (bytebuf-to-list buf 0))))   "));
    }

    @Test
    public void test_bytebuf_put_buf_BANG() {
        final Venice venice = new Venice();

        assertArrayEquals(new byte[] {1,2,3,0,0}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 0) (bytebuf-put-buf! (bytebuf [1 2 3]) 0 3))")).array());
        assertArrayEquals(new byte[] {0,1,2,3,0}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 1) (bytebuf-put-buf! (bytebuf [1 2 3]) 0 3))")).array());
        assertArrayEquals(new byte[] {0,0,1,2,3}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 2) (bytebuf-put-buf! (bytebuf [1 2 3]) 0 3))")).array());

        assertArrayEquals(new byte[] {3,4,5,0,0}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 0) (bytebuf-put-buf! (bytebuf [1 2 3 4 5]) 2 3))")).array());
        assertArrayEquals(new byte[] {0,3,4,5,0}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 1) (bytebuf-put-buf! (bytebuf [1 2 3 4 5]) 2 3))")).array());
        assertArrayEquals(new byte[] {0,0,3,4,5}, ((ByteBuffer)venice.eval("(-> (bytebuf-allocate 5) (bytebuf-pos! 2) (bytebuf-put-buf! (bytebuf [1 2 3 4 5]) 2 3))")).array());
    }

}
