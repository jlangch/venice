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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class HexdumpModuleTest {

    @Test
    public void test_dump_empty() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (load-module :hexdump)               \n" +
                "  (with-out-str                        \n" +
                "    (hexdump/dump [])))                  ";

        assertEquals(
                "\n",
                venice.eval(script));
    }

    @Test
    public void test_dump_simple_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (load-module :hexdump)               \n" +
                "  (with-out-str                        \n" +
                "    (hexdump/dump [0 1 2 3])))         ";

        assertEquals(
                "00000000: 0001 0203                                ....            \n\n",
                venice.eval(script));
    }

    @Test
    public void test_dump_single_line() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (load-module :hexdump)               \n" +
                "  (with-out-str                        \n" +
                "    (hexdump/dump (range 1))           \n" +
                "    (hexdump/dump (range 2))           \n" +
                "    (hexdump/dump (range 3))           \n" +
                "    (hexdump/dump (range 4))           \n" +
                "    (hexdump/dump (range 5))           \n" +
                "    (hexdump/dump (range 6))           \n" +
                "    (hexdump/dump (range 7))           \n" +
                "    (hexdump/dump (range 8))           \n" +
                "    (hexdump/dump (range 9))           \n" +
                "    (hexdump/dump (range 10))          \n" +
                "    (hexdump/dump (range 11))          \n" +
                "    (hexdump/dump (range 12))          \n" +
                "    (hexdump/dump (range 13))          \n" +
                "    (hexdump/dump (range 14))          \n" +
                "    (hexdump/dump (range 15))          \n" +
                "    (hexdump/dump (range 16))          \n" +
                "    (hexdump/dump (range 0x40 0x50)))) ";

        assertEquals(
                "00000000: 00                                       .               \n\n" +
                "00000000: 0001                                     ..              \n\n" +
                "00000000: 0001 02                                  ...             \n\n" +
                "00000000: 0001 0203                                ....            \n\n" +
                "00000000: 0001 0203 04                             .....           \n\n" +
                "00000000: 0001 0203 0405                           ......          \n\n" +
                "00000000: 0001 0203 0405 06                        .......         \n\n" +
                "00000000: 0001 0203 0405 0607                      ........        \n\n" +
                "00000000: 0001 0203 0405 0607 08                   .........       \n\n" +
                "00000000: 0001 0203 0405 0607 0809                 ..........      \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a              ...........     \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a0b            ............    \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c         .............   \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c0d       ..............  \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c0d 0e    ............... \n\n" +
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c0d 0e0f  ................\n\n" +
                "00000000: 4041 4243 4445 4647 4849 4a4b 4c4d 4e4f  @ABCDEFGHIJKLMNO\n\n",
                venice.eval(script));
    }

    @Test
    public void test_dump_multi_line_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (load-module :hexdump)               \n" +
                "  (with-out-str                        \n" +
                "    (hexdump/dump (range 99))))         ";

        assertEquals(
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c0d 0e0f  ................\n" +
                "00000010: 1011 1213 1415 1617 1819 1a1b 1c1d 1e1f  ................\n" +
                "00000020: 2021 2223 2425 2627 2829 2a2b 2c2d 2e2f   !\"#$%&'()*+,-./\n" +
                "00000030: 3031 3233 3435 3637 3839 3a3b 3c3d 3e3f  0123456789:;<=>?\n" +
                "00000040: 4041 4243 4445 4647 4849 4a4b 4c4d 4e4f  @ABCDEFGHIJKLMNO\n" +
                "00000050: 5051 5253 5455 5657 5859 5a5b 5c5d 5e5f  PQRSTUVWXYZ[\\]^_\n" +
                "00000060: 6061 62                                  `ab             \n" +
                "\n",
                venice.eval(script));
    }

    @Test
    public void test_dump_multi_line_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                    \n" +
                "  (load-module :hexdump)               \n" +
                "  (with-out-str                        \n" +
                "    (hexdump/dump (range 100))))         ";

        assertEquals(
                "00000000: 0001 0203 0405 0607 0809 0a0b 0c0d 0e0f  ................\n" +
                "00000010: 1011 1213 1415 1617 1819 1a1b 1c1d 1e1f  ................\n" +
                "00000020: 2021 2223 2425 2627 2829 2a2b 2c2d 2e2f   !\"#$%&'()*+,-./\n" +
                "00000030: 3031 3233 3435 3637 3839 3a3b 3c3d 3e3f  0123456789:;<=>?\n" +
                "00000040: 4041 4243 4445 4647 4849 4a4b 4c4d 4e4f  @ABCDEFGHIJKLMNO\n" +
                "00000050: 5051 5253 5455 5657 5859 5a5b 5c5d 5e5f  PQRSTUVWXYZ[\\]^_\n" +
                "00000060: 6061 6263                                `abc            \n" +
                "\n",
                venice.eval(script));
    }

}
