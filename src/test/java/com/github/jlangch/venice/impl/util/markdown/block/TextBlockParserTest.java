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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class TextBlockParserTest {

    @Test
    public void test_basic_1() {
        final String md = "Lorem ipsum dolor sit amet, consetetur";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text = (TextBlock)blocks.get(0);

        assertEquals(1, text.getChunks().size());

        assertEquals("Lorem ipsum dolor sit amet, consetetur", ((TextChunk)text.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_2() {
        final String md = "Lorem ipsum dolor \n" +
                          "sit amet, consetetur";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());

        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text = (TextBlock)blocks.get(0);

        assertEquals(1, text.getChunks().size());

        assertEquals("Lorem ipsum dolor sit amet, consetetur", ((TextChunk)text.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_3() {
        final String md = "Lorem ipsum dolor sit amet, consetetur.\n" +
                          "\n" +
                          "At vero eos et accusam et justo duo.";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(2, blocks.size());

        assertTrue(blocks.get(0) instanceof TextBlock);

        // block 1

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(1, text1.getChunks().size());

        assertEquals("Lorem ipsum dolor sit amet, consetetur.", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());

        // block 2

        TextBlock text2 = (TextBlock)blocks.get(1);

        assertEquals(1, text2.getChunks().size());

        assertEquals("At vero eos et accusam et justo duo.", ((TextChunk)text2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_basic_4() {
        final String md = "Lorem ipsum dolor \n" +
                          "sit amet, consetetur.\n" +
                          "\n\n" +
                          "At vero eos et accusam et\n" +
                          "justo duo.";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(2, blocks.size());

        assertTrue(blocks.get(0) instanceof TextBlock);

        // block 1

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(1, text1.getChunks().size());

        assertEquals("Lorem ipsum dolor sit amet, consetetur.", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());

        // block 2

        TextBlock text2 = (TextBlock)blocks.get(1);

        assertEquals(1, text2.getChunks().size());

        assertEquals("At vero eos et accusam et justo duo.", ((TextChunk)text2.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text2.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_linebreak_1() {
        final String md = "Lorem ipsum dolor¶";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(2, text1.getChunks().size());

        assertTrue(text1.getChunks().get(0) instanceof TextChunk);
        assertTrue(text1.getChunks().get(1) instanceof LineBreakChunk);

        assertEquals("Lorem ipsum dolor", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_linebreak_2() {
        final String md = "Lorem ¶ ipsum ¶ dolor ¶";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(6, text1.getChunks().size());

        assertTrue(text1.getChunks().get(0) instanceof TextChunk);
        assertTrue(text1.getChunks().get(1) instanceof LineBreakChunk);
        assertTrue(text1.getChunks().get(2) instanceof TextChunk);
        assertTrue(text1.getChunks().get(3) instanceof LineBreakChunk);
        assertTrue(text1.getChunks().get(4) instanceof TextChunk);
        assertTrue(text1.getChunks().get(5) instanceof LineBreakChunk);

        assertEquals("Lorem ", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());

        assertEquals(" ipsum ", ((TextChunk)text1.getChunks().get(2)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(2)).getFormat());

        assertEquals(" dolor ", ((TextChunk)text1.getChunks().get(4)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(4)).getFormat());
    }

    @Test
    public void test_linebreak_3() {
        final String md = "Lorem ipsum dolor¶¶";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(2, text1.getChunks().size());

        assertTrue(text1.getChunks().get(0) instanceof TextChunk);
        assertTrue(text1.getChunks().get(1) instanceof LineBreakChunk);

        assertEquals("Lorem ipsum dolor", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());
    }

    @Test
    public void test_linebreak_4() {
        final String md = "Lorem ipsum dolor¶ ¶";

        Blocks blocks = new BlockParser(md).parse();

        assertEquals(1, blocks.size());
        assertTrue(blocks.get(0) instanceof TextBlock);

        TextBlock text1 = (TextBlock)blocks.get(0);

        assertEquals(4, text1.getChunks().size());

        assertTrue(text1.getChunks().get(0) instanceof TextChunk);
        assertTrue(text1.getChunks().get(1) instanceof LineBreakChunk);
        assertTrue(text1.getChunks().get(2) instanceof TextChunk);
        assertTrue(text1.getChunks().get(3) instanceof LineBreakChunk);

        assertEquals("Lorem ipsum dolor", ((TextChunk)text1.getChunks().get(0)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(0)).getFormat());

        assertEquals(" ", ((TextChunk)text1.getChunks().get(2)).getText());
        assertEquals(TextChunk.Format.NORMAL, ((TextChunk)text1.getChunks().get(2)).getFormat());
    }

}
