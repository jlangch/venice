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
package com.github.jlangch.venice.impl.util.markdown.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.chunk.InlineCodeChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class ListBlockParserTest {

    // -----------------------------------------------------------------------------
    // Unordered: Basics
    // -----------------------------------------------------------------------------

    @Test
    public void test_list_block_unordered_1() {
        final String md = "* item 1";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_2() {
        final String md = "* item 1 \n" +
                          "* item 2";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_3() {
        final String md = " * item 1 \n" +
                          " *  item 2   ";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_4() {
        final String md = " * item 1 \n" +
                          " *    \n" +
                          " * item 2 ";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }



    // -----------------------------------------------------------------------------
    // Unordered: Long, multiline items
    // -----------------------------------------------------------------------------

    @Test
    public void test_list_block_unordered_long_1() {
        final String md = "* item 1 \n" +
                          "  lorem ispum";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1 lorem ispum", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_long_2() {
        final String md = "* item 1 \n" +
                          "  lorem ispum 1\n" +
                          "* item 2 \n" +
                          "  lorem ispum 2";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1 lorem ispum 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2 lorem ispum 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_long_3() {
        final String md = "* item 1¶\n" +
                          "  lorem ispum 1\n" +
                          "* item 2¶\n" +
                          "  lorem ispum 2";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(3, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());
        assertTrue(block1.getChunks().get(1) instanceof LineBreakChunk);
        assertEquals("lorem ispum 1", ((TextChunk)block1.getChunks().get(2)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(2)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(3, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
        assertTrue(block2.getChunks().get(1) instanceof LineBreakChunk);
        assertEquals("lorem ispum 2", ((TextChunk)block2.getChunks().get(2)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(2)).getFormat());
    }



    // -----------------------------------------------------------------------------
    // Unordered: styled items
    // -----------------------------------------------------------------------------

    @Test
    public void test_list_block_unordered_styled_1() {
        final String md = "* *item 1*";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.ITALIC, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_styled_2() {
        final String md = "* **item 1**";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.BOLD, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_styled_3() {
        final String md = "* ***item 1***";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.BOLD_ITALIC, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_unordered_styled_4() {
        final String md = "* `item 1`";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertFalse(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((InlineCodeChunk)block.getChunks().get(0)).getText());
    }



    // -----------------------------------------------------------------------------
    // Ordered: Basics
    // -----------------------------------------------------------------------------

    @Test
    public void test_list_block_ordered_1() {
        final String md = "1. item 1";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof ListBlock);
        assertTrue(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(1, ((ListBlock)blocks.get(0)).size());

        TextBlock block = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block.getChunks().size());
        assertEquals("item 1", ((TextChunk)block.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_ordered_2() {
        final String md = "1. item 1 \n" +
                          "2. item 2";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof ListBlock);
        assertTrue(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_list_block_ordered_3() {
        final String md = " 1. item 1 \n" +
                          " 1.  item 2   ";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof ListBlock);
        assertTrue(((ListBlock)blocks.get(0)).isOrdered());
        assertEquals(2, ((ListBlock)blocks.get(0)).size());

        TextBlock block1 = ((TextBlock)((ListBlock)blocks.get(0)).get(0));
        assertEquals(1, block1.getChunks().size());
        assertEquals("item 1", ((TextChunk)block1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block1.getChunks().get(0)).getFormat());

        TextBlock block2 = ((TextBlock)((ListBlock)blocks.get(0)).get(1));
        assertEquals(1, block2.getChunks().size());
        assertEquals("item 2", ((TextChunk)block2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)block2.getChunks().get(0)).getFormat());
    }

}
