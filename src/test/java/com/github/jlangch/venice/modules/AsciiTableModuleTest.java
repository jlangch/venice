/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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


public class AsciiTableModuleTest {

    @Test
    public void test_raw_001() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     []                                    \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     0)))                                  ";

        assertEquals("++\n"
                   + "++", venice.eval(script));
    }

    @Test
    public void test_raw_002() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     []                                    \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+\n"
                   + "+--+", venice.eval(script));
    }

    @Test
    public void test_raw_003() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     []                                    \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     2)))                                  ";

        assertEquals("+----+\n"
                   + "+----+", venice.eval(script));
    }


    @Test
    public void test_raw_010() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     [[\"\"]]                              \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+\n"
                   + "+--+", venice.eval(script));
    }

    @Test
    public void test_raw_011() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     [[\"\"] [\"\"]]                       \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+\n"
                   + "+--+\n"
                   + "+--+", venice.eval(script));
    }

    @Test
    public void test_raw_012() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     [[\"\"] [\"\"]]                       \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+\n"
                   + "+--+\n"
                   + "+--+", venice.eval(script));
    }

    @Test
    public void test_raw_013() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     [[\"\" \"\"] [\"\" \"\"]]             \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+--+\n"
                   + "+--+--+\n"
                   + "+--+--+", venice.eval(script));
    }

    @Test
    public void test_raw_020() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     [\"\" \"\"]                           \n" +
                              "     [[\"\" \"\"] [\"\" \"\"]]             \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+--+\n"
                   + "+--+--+\n"
                   + "+--+--+", venice.eval(script));
    }

    @Test
    public void test_raw_021() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     [\"\" \"\"]                           \n" +
                              "     [[\"\" \"\"] [\"\" \"\"]]             \n" +
                              "     [\"\" \"\"]                           \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+--+--+\n"
                   + "+--+--+\n"
                   + "+--+--+", venice.eval(script));
    }





    @Test
    public void test_base() throws Exception {
        final Venice venice = new Venice();

        final String script = "(do                                        \n" +
                              "  (load-module :ascii-table)               \n" +
                              "  (ascii-table/render                      \n" +
                              "     nil                                   \n" +
                              "     [[\"1 1\" \"1 2\"] [\"2 1\" \"2 2\"]] \n" +
                              "     nil                                   \n" +
                              "     :standard                             \n" +
                              "     1)))                                  ";

        assertEquals("+-----+-----+\n"
                   + "| 1 1 | 1 2 |\n"
                   + "+-----+-----+\n"
                   + "| 2 1 | 2 2 |\n"
                   + "+-----+-----+", venice.eval(script));
    }

}
