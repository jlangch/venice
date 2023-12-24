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
package com.github.jlangch.venice.impl.util.markdown.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;
import com.github.jlangch.venice.impl.util.markdown.renderer.text.TextTableRendrer;


public class TextTableRendererTest {

    // -----------------------------------------------------------------------------
    // Simple table, no wrap
    // -----------------------------------------------------------------------------

    @Test
    public void test_001() {
        final String md5 =
            "|c1|";

        final String expected =
            "c1";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_002() {
        final String md5 =
            "|c1|c2|";

        final String expected =
            "c1  c2";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_003() {
        final String md5 =
            "|c1|c2|\n" +
            "|d1|d2|";

        final String expected =
            "c1  c2\n" +
            "d1  d2";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_004() {
        final String md5 =
            "|c1..1|c2|\n" +
            "|d1|d2|";

        final String expected =
            "c1..1  c2\n" +
            "d1     d2";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_005() {
        final String md5 =
            "|c1..1|c2|\n" +
            "|d1|d2..2|";

        final String expected =
            "c1..1  c2\n" +
            "d1     d2..2";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_006_reservied_char() {
        final String md5 =
            "|c1\\|\\|1|c2|\n" +
            "|d1|d2..2|";

        final String expected =
            "c1||1  c2\n" +
            "d1     d2..2";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }


    // -----------------------------------------------------------------------------
    // Simple table, align, no wrap
    // -----------------------------------------------------------------------------

    @Test
    public void test_101() {
        final String md5 =
            "|:-|\n" +
            "|c1|";

        final String expected =
            "c1";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_102() {
        final String md5 =
            "|:-|:-:|-:|\n" +
            "|c1|c2|c3|";

        final String expected =
            "c1  c2  c3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_103() {
        final String md5 =
            "|:-|:-:|-:|\n" +
            "|c1|c2|c3|\n" +
            "|d1|d2|d3|";

        final String expected =
            "c1  c2  c3\n" +
            "d1  d2  d3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_104() {
        final String md5 =
            "|:-|:-:|-:|\n" +
            "|c1...|c2.|c3...|\n" +
            "|d1...|d2.....|d3...|\n" +
            "|e1|e2.|e3|";

        final String expected =
            "c1...    c2.    c3...\n" +
            "d1...  d2.....  d3...\n" +
            "e1       e2.       e3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_105() {
        final String md5 =
            "|:-|:-:|-:|\n" +
            "|c1...|c2.|c3..|\n" +
            "|d1..|d2.....|d3...|\n" +
            "||e2.||";

        final String expected =
            "c1...    c2.     c3..\n" +
            "d1..   d2.....  d3...\n" +
            "         e2.";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }



    // -----------------------------------------------------------------------------
    // Simple table, title, align, no wrap
    // -----------------------------------------------------------------------------

    @Test
    public void test_201() {
        final String md5 =
            "|T1|\n" +
            "|:-|\n" +
            "|c1|";

        final String expected =
            "T1\n" +
            "--\n" +
            "c1";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_202() {
        final String md5 =
            "|T1|T2|T3|\n" +
            "|:-|:-:|-:|\n" +
            "|c1|c2|c3|";

        final String expected =
            "T1  T2  T3\n" +
            "--  --  --\n" +
            "c1  c2  c3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_203() {
        final String md5 =
            "|T1...|T2....|T3...|\n" +
            "|:-|:-:|-:|\n" +
            "|c1|c2|c3|\n" +
            "|d1|d2|d3|";

        final String expected =
            "T1...  T2....  T3...\n" +
            "-----  ------  -----\n" +
            "c1       c2       c3\n" +
            "d1       d2       d3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_204() {
        final String md5 =
            "|T1|T2|T3|\n" +
            "|:-|:-:|-:|\n" +
            "|c1..|c2.|c3...|\n" +
            "|d1...|d2.....|d3..|\n" +
            "|e1|e2.|e3|";

        final String expected =
            "T1       T2        T3\n" +
            "-----  -------  -----\n" +
            "c1..     c2.    c3...\n" +
            "d1...  d2.....   d3..\n" +
            "e1       e2.       e3";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_205() {
        final String md5 =
            "|T1|T2|T3|\n" +
            "|:-|:-:|-:|\n" +
            "|c1...|c2.|c3...|\n" +
            "|d1...|d2.....|d3...|\n" +
            "||e2.||";

        final String expected =
            "T1       T2        T3\n" +
            "-----  -------  -----\n" +
            "c1...    c2.    c3...\n" +
            "d1...  d2.....  d3...\n" +
            "         e2.";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_206() {
        final String md5 =
            "|T1|T2|T3|\n" +
            "|:-|:-:|-:|\n" +
            "|c1###|c2##|c3###|\n" +
            "|d1###|d2#####|d3###|\n" +
            "||e2#||";

        final String expected =
            "T1...  ..T2...  ...T3\n" +
            "-----  -------  -----\n" +
            "c1###  .c2##..  c3###\n" +
            "d1###  d2#####  d3###\n" +
            ".....  ..e2#..  .....";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80, '.', "  ").render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_207() {
        final String md5 =
            "|T1|T2|\n" +
            "|:-|:-|\n" +
            "|c1|Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed|\n" +
            "|d1|ipsum dolor sit amet, consetetur sadipscing elitr, sed diam|";

        final String expected =
            "T1  T2\n" +
            "--  ------------------------------------------------------------\n" +
            "c1  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed\n" +
            "d1  ipsum dolor sit amet, consetetur sadipscing elitr, sed diam";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }



    // -----------------------------------------------------------------------------
    // Table, title, align, with line wrap
    // -----------------------------------------------------------------------------

    @Test
    public void test_301() {
        final String md5 =
            "|T1|T2|\n" +
            "|:-|:-|\n" +
            "|c1|Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed|\n" +
            "|d1|ipsum dolor sit amet, consetetur sadipscing elitr, sed diam|";

        final String expected =
            "T1  T2\n" +
            "--  -------------------------------\n" +
            "c1  Lorem ipsum dolor sit amet,\n" +
            "    consetetur sadipscing elitr,\n" +
            "    sed\n" +
            "d1  ipsum dolor sit amet,\n" +
            "    consetetur sadipscing elitr,\n" +
            "    sed diam";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 35).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_302() {
        final String md5 =
            "|T1|T2|\n" +
            "|:-|:-|\n" +
            "|c1|Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed|\n" +
            "|d1|ipsum dolor sit amet, consetetur sadipscing elitr, sed diam|";

        final String expected =
            "T1  T2\n" +
            "--  ----------------------------------------------\n" +
            "c1  Lorem ipsum dolor sit amet, consetetur\n" +
            "    sadipscing elitr, sed\n" +
            "d1  ipsum dolor sit amet, consetetur sadipscing\n" +
            "    elitr, sed diam";


        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 50).render() ;

        assertEquals(expected, rendered);
    }

    @Test
    public void test_303() {
        final String md5 =
            "|T1|T2|\n" +
            "|:-|:-|\n" +
            "|c1|Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
                    + "sed diam nonumy eirmod tempor invidunt ut labore et "
                    + "dolore magna aliquyam erat, sed diam voluptua. At vero "
                    + "eos et accusam et justo duo dolores et ea rebum. Stet "
                    + "clita kasd gubergren, no sea takimata sanctus est "
                    + "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, "
                    + "consetetur sadipscing elitr, sed diam nonumy eirmod "
                    + "tempor invidunt ut labore et dolore magna aliquyam "
                    + "erat, sed diam voluptua.|\n" +
            "|d1|At vero eos et accusam et justo duo dolores et ea "
                    + "rebum. Stet clita kasd gubergren, no sea takimata "
                    + "sanctus est Lorem ipsum dolor sit amet.|";

        final String expected =
            "T1  T2\n" +
            "--  ----------------------------------------------------------------------------\n" +
            "c1  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy\n" +
            "    eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam\n" +
            "    voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet\n" +
            "    clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit\n" +
            "    amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\n" +
            "    nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed\n" +
            "    diam voluptua.\n" +
            "d1  At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd\n" +
            "    gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

        final TableBlock block = (TableBlock)Markdown.parse(md5).blocks().get(0);
        final String rendered = new TextTableRendrer(block, 80).render() ;

        assertEquals(expected, rendered);
    }

}
