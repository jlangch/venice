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
package com.github.jlangch.venice.impl.util.markdown.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class TableBlockParserTest {

    // -----------------------------------------------------------------------------
    // Basics - one row
    // -----------------------------------------------------------------------------

    @Test
    public void test_basic_1row_1() {
        final String md = "|c1|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_1row_2() {
        final String md = "|c1|c2|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(2, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_1row_3() {
        final String md = "|c1|c2|c3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(2).horzAlignment());

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());
    }


    // -----------------------------------------------------------------------------
    // Basics - two rows
    // -----------------------------------------------------------------------------

    @Test
    public void test_basic_2rows_1() {
        final String md = "|c1|\n" +
                          "|d1|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(2, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        // row 2

        assertEquals("d1", ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_2rows_2() {
        final String md = "|c1|c2|\n" +
                          "|d1|d2|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(2, table.cols());
        assertEquals(2, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        // row 2

        assertEquals("d1", ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getFormat());

        assertEquals("d2", ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_2rows_3() {
        final String md = "|c1|c2|c3|\n" +
                          "|d1|d2|d3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(2, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(2).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());

        // row 2

        assertEquals("d1", ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getFormat());

        assertEquals("d2", ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getFormat());

        assertEquals("d3", ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getFormat());
    }

    // -----------------------------------------------------------------------------
    // Format - one row
    // -----------------------------------------------------------------------------

    @Test
    public void test_format_1row_1() {
        final String md = "|:-|\n" +
                          "|c1|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_format_1row_2() {
        final String md = "|:-:|\n" +
                          "|c1|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.CENTER, table.format(0).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_format_1row_3() {
        final String md = "|-:|\n" +
                          "|c1|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.RIGHT, table.format(0).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());
    }


    @Test
    public void test_format_1row_4() {
        final String md = "|:-|-:|\n" +
                          "|c1|c2|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(2, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.RIGHT, table.format(1).horzAlignment());


        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_format_1row_5() {
        final String md = "|:-|:-:|-:|\n" +
                          "|c1|c2|c3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.CENTER, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.RIGHT, table.format(2).horzAlignment());

        // row 1
        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_format_2rows_1() {
        final String md = "|:-|:-:|-:|\n" +
                          "|c1|c2|c3|\n" +
                          "|d1|d2|d3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(2, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.CENTER, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.RIGHT, table.format(2).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());

        // row 2

        assertEquals("d1", ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getFormat());

        assertEquals("d2", ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getFormat());

        assertEquals("d3", ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_format_2rows_2() {
        final String md = "| :--- | :---: | ---: |\n" +
                          "|c1|c2|c3|\n" +
                          "|d1|d2|d3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(2, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.CENTER, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.RIGHT, table.format(2).horzAlignment());

        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());

        // row 2

        assertEquals("d1", ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 0).getChunks().get(0)).getFormat());

        assertEquals("d2", ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 1).getChunks().get(0)).getFormat());

        assertEquals("d3", ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(1, 2).getChunks().get(0)).getFormat());
    }


    @Test
    public void test_basic_escaped_chars() {
        final String md = "|\\u2020|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(1, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());

        assertEquals("\\u2020", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_escaped_pilcrow() {
        final String md = "|xx\\¶xx|'\\¶'|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertFalse(table.hasHeader());
        assertEquals(2, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.LEFT, table.format(1).horzAlignment());

        assertEquals("xx¶xx", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("'¶'", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());
    }


    // -----------------------------------------------------------------------------
    // Header - one row
    // -----------------------------------------------------------------------------

    @Test
    public void test_header() {
        final String md = "|h1|h2|h3|\n" +
        		          "| :-- | :--: | --: |\n" +
                          "|c1|c2|c3|";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TableBlock);

        TableBlock table = (TableBlock)blocks.get(0);

        assertTrue(table.hasHeader());
        assertEquals(3, table.cols());
        assertEquals(1, table.bodyRows());

        assertEquals(HorzAlignment.LEFT, table.format(0).horzAlignment());
        assertEquals(HorzAlignment.CENTER, table.format(1).horzAlignment());
        assertEquals(HorzAlignment.RIGHT, table.format(2).horzAlignment());


        // header

        assertEquals("h1", ((TextChunk)table.headerCell(0).getChunks().get(0)).getText());
        assertEquals("h2", ((TextChunk)table.headerCell(1).getChunks().get(0)).getText());
        assertEquals("h3", ((TextChunk)table.headerCell(2).getChunks().get(0)).getText());


        // row 1

        assertEquals("c1", ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 0).getChunks().get(0)).getFormat());

        assertEquals("c2", ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 1).getChunks().get(0)).getFormat());

        assertEquals("c3", ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)table.bodyCell(0, 2).getChunks().get(0)).getFormat());
    }

}
