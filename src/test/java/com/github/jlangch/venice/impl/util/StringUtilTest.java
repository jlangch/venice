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
package com.github.jlangch.venice.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;


public class StringUtilTest {

    @Test
    public void testTrimToNull() {
        assertNull(StringUtil.trimToNull(""));
        assertNull(StringUtil.trimToNull(" "));
        assertNull(StringUtil.trimToNull("  "));
        assertNull(StringUtil.trimToNull("\n"));
        assertNull(StringUtil.trimToNull("  \n\n"));
    }

    @Test
    public void testTrimLeft() {
        assertEquals(null, StringUtil.trimLeft(null));
        assertEquals("", StringUtil.trimLeft(""));
        assertEquals("", StringUtil.trimLeft(" "));
        assertEquals("", StringUtil.trimLeft("  "));

        assertEquals("a", StringUtil.trimLeft("a"));
        assertEquals("a", StringUtil.trimLeft(" a"));
        assertEquals("a", StringUtil.trimLeft("  a"));
    }

    @Test
    public void testTrimRight() {
        assertEquals(null, StringUtil.trimRight(null));
        assertEquals("", StringUtil.trimRight(""));
        assertEquals("", StringUtil.trimRight(" "));
        assertEquals("", StringUtil.trimRight("  "));
        assertEquals("", StringUtil.trimRight("\n"));
        assertEquals("", StringUtil.trimRight("  \n\n"));
        assertEquals("", StringUtil.trimRight("  \n\r\n"));

        assertEquals("a", StringUtil.trimRight("a"));
        assertEquals("a", StringUtil.trimRight("a "));
        assertEquals("a", StringUtil.trimRight("a  "));
        assertEquals("a", StringUtil.trimRight("a\n"));
        assertEquals("a", StringUtil.trimRight("a  \n\n"));
        assertEquals("a", StringUtil.trimRight("a  \n\r\n"));
    }

    @Test
    public void testSplitIntoLines() {
        List<String> lines;

        lines = StringUtil.splitIntoLines("");
        assertEquals(0, lines.size());



        lines = StringUtil.splitIntoLines("\n");
        assertEquals(1, lines.size());
        assertEquals("", lines.get(0));



        lines = StringUtil.splitIntoLines("1\n");
        assertEquals(1, lines.size());
        assertEquals("1", lines.get(0));

        lines = StringUtil.splitIntoLines("\n2");
        assertEquals(2, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("2", lines.get(1));

        lines = StringUtil.splitIntoLines("1\n2");
        assertEquals(2, lines.size());
        assertEquals("1", lines.get(0));
        assertEquals("2", lines.get(1));

        lines = StringUtil.splitIntoLines("\n\n");
        assertEquals(2, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("", lines.get(1));



        lines = StringUtil.splitIntoLines("1\n\n");
        assertEquals(2, lines.size());
        assertEquals("1", lines.get(0));
        assertEquals("", lines.get(1));

        lines = StringUtil.splitIntoLines("\n2\n");
        assertEquals(2, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("2", lines.get(1));

        lines = StringUtil.splitIntoLines("\n\n3");
        assertEquals(3, lines.size());
        assertEquals("", lines.get(0));
        assertEquals("", lines.get(1));
        assertEquals("3", lines.get(2));
    }

    @Test
    public void splitColumns() {
        assertEquals("||", String.join("|", StringUtil.splitColumns("",     new int[] {0,1,2})));
        assertEquals("||", String.join("|", StringUtil.splitColumns(" ",    new int[] {0,1,2})));
        assertEquals("||", String.join("|", StringUtil.splitColumns("  ",   new int[] {0,1,2})));
        assertEquals("||", String.join("|", StringUtil.splitColumns("   ",  new int[] {0,1,2})));
        assertEquals("||", String.join("|", StringUtil.splitColumns("    ", new int[] {0,1,2})));

        assertEquals("1|2|3", String.join("|", StringUtil.splitColumns("123", new int[] {0,1,2})));
        assertEquals("12|34|56", String.join("|", StringUtil.splitColumns("123456", new int[] {0,2,4})));
        assertEquals("1|2|3", String.join("|", StringUtil.splitColumns("1 2 3 ", new int[] {0,2,4})));

        assertEquals("||123", String.join("|", StringUtil.splitColumns("123", new int[] {1,0,0})));
        assertEquals("||123", String.join("|", StringUtil.splitColumns("123", new int[] {0,1,0})));
        assertEquals("||123", String.join("|", StringUtil.splitColumns("123", new int[] {0,0,0})));
        assertEquals("|12|3", String.join("|", StringUtil.splitColumns("123", new int[] {0,0,2})));

        assertEquals("123|456|", String.join("|", StringUtil.splitColumns("123456", new int[] {0,3,6})));
        assertEquals("123456||", String.join("|", StringUtil.splitColumns("123456", new int[] {0,6,12})));
        assertEquals("||",       String.join("|", StringUtil.splitColumns("123456", new int[] {20,30,40})));
    }

    @Test
    public void testRemoveStart() {
        assertEquals(null, StringUtil.removeStart(null, "-"));
        assertEquals("", StringUtil.removeStart("", "-"));
        assertEquals("", StringUtil.removeStart("-", "-"));
        assertEquals("x", StringUtil.removeStart("x", "-"));
        assertEquals("x", StringUtil.removeStart("-x", "-"));
        assertEquals("-x", StringUtil.removeStart("--x", "-"));
    }

    @Test
    public void testRemoveEnd() {
        assertEquals(null, StringUtil.removeEnd(null, "-"));
        assertEquals("", StringUtil.removeEnd("", "-"));
        assertEquals("", StringUtil.removeEnd("-", "-"));
        assertEquals("x", StringUtil.removeEnd("x", "-"));
        assertEquals("x", StringUtil.removeEnd("x-", "-"));
        assertEquals("x-", StringUtil.removeEnd("x--", "-"));
    }

    @Test
    public void testEscape() {
        assertEquals("", StringUtil.escape(""));
        assertEquals(" ", StringUtil.escape(" "));
        assertEquals("a", StringUtil.escape("a"));
        assertEquals("•", StringUtil.escape("•"));

        assertEquals("abc-123", StringUtil.escape("abc-123"));

        assertEquals(" \\n \\r \\t \\\" \\\\ ", StringUtil.escape(" \n \r \t \" \\ "));

        assertEquals("--•--", StringUtil.escape("--•--"));
    }

    @Test
    public void testStripMargin() {
        assertEquals("123", StringUtil.stripMargin("123", '|'));
        assertEquals("123", StringUtil.stripMargin("  |123", '|'));

        assertEquals("1\n2\n3", StringUtil.stripMargin("1\n  |2\n  |3", '|'));
        assertEquals("1\n 2\n 3", StringUtil.stripMargin("1\n  | 2\n  | 3", '|'));
    }

    @Test
    public void testStripIndent() {
        assertEquals(null, StringUtil.stripIndent(null));
        assertEquals("", StringUtil.stripIndent(""));
        assertEquals("123", StringUtil.stripIndent("123"));
        assertEquals("123", StringUtil.stripIndent("  123"));
        assertEquals("", StringUtil.stripIndent("\n"));
        assertEquals("\n  ", StringUtil.stripIndent("\n  "));
        assertEquals("\n123", StringUtil.stripIndent("\n123"));

        assertEquals("1\n2\n3", StringUtil.stripIndent("1\n2\n3"));
        assertEquals("1\n  2\n  3", StringUtil.stripIndent("1\n  2\n  3"));

        assertEquals("1\n2\n3", StringUtil.stripIndent("  1\n  2\n  3"));
    }

    @Test
    public void testStripIndent_CR() {
        assertEquals(null, StringUtil.stripIndent(null));
        assertEquals("", StringUtil.stripIndent(""));
        assertEquals("123", StringUtil.stripIndent("123"));
        assertEquals("123", StringUtil.stripIndent("  123"));
        assertEquals("", StringUtil.stripIndent("\r\n"));
        assertEquals("\n  ", StringUtil.stripIndent("\r\n  "));
        assertEquals("\n123", StringUtil.stripIndent("\r\n123"));

        assertEquals("1\n2\n3", StringUtil.stripIndent("1\r\n2\r\n3"));
        assertEquals("1\n  2\n  3", StringUtil.stripIndent("1\r\n  2\r\n  3"));

        assertEquals("1\n2\n3", StringUtil.stripIndent("  1\r\n  2\r\n  3"));
    }

    @Test
    public void testStripIndentIfFirstLineEmpty() {
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("123"));
        assertEquals("  123", StringUtil.stripIndentIfFirstLineEmpty("  123"));
        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\n"));
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("\n123"));
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("\n123\n"));
        assertEquals("123\n456", StringUtil.stripIndentIfFirstLineEmpty("\n123\n456"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("1\n2\n3"));
        assertEquals("1\n  2\n  3", StringUtil.stripIndentIfFirstLineEmpty("1\n  2\n  3"));

        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\n  "));
        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\n  \n"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\n  3"));
        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\n  3\n"));

        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\n    3"));
        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\n    3\n"));
    }

    @Test
    public void testStripIndentIfFirstLineEmpty_CR() {
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("123"));
        assertEquals("  123", StringUtil.stripIndentIfFirstLineEmpty("  123"));
        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\r\n"));
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("\r\n123"));
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("\r\n123\r\n"));
        assertEquals("123\n456", StringUtil.stripIndentIfFirstLineEmpty("\r\n123\r\n456"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("1\r\n2\r\n3"));
        assertEquals("1\n  2\n  3", StringUtil.stripIndentIfFirstLineEmpty("1\r\n  2\r\n  3"));

        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\r\n  "));
        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\r\n  \r\n"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\r\n  1\r\n  2\r\n  3"));
        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\r\n  1\r\n  2\r\n  3\r\n"));

        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\r\n  1\r\n   2\r\n    3"));
        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\r\n  1\r\n   2\r\n    3\r\n"));
    }

    @Test
    public void testStripIndentIfFirstLineEmpty_Join() {
        assertEquals("123456", StringUtil.stripIndentIfFirstLineEmpty("\n123\\\n456"));

        assertEquals("1\\\n2\\\n3", StringUtil.stripIndentIfFirstLineEmpty("1\\\n2\\\n3"));
        assertEquals("1\\\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("1\\\n2\n3"));
        assertEquals("1\n2\\\n3", StringUtil.stripIndentIfFirstLineEmpty("1\n2\\\n3"));
        assertEquals("1\\\n  2\\\n  3", StringUtil.stripIndentIfFirstLineEmpty("1\\\n  2\\\n  3"));

        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\n  "));
        assertEquals("", StringUtil.stripIndentIfFirstLineEmpty("\n  \\\n"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\n  3"));
        assertEquals("12\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n  2\n  3"));
        assertEquals("1\n23", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\\\n  3"));
        assertEquals("123", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n  2\\\n  3"));

        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\n  3\n"));
        assertEquals("12\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n  2\n  3\n"));
        assertEquals("1\n23", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\\\n  3\n"));
        assertEquals("1\n2\n3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n  2\n  3\\\n"));

        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\n    3"));
        assertEquals("1 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n   2\n    3"));
        assertEquals("1\n 2  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\\\n    3"));
        assertEquals("1 2  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n   2\\\n    3"));

        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\n    3\n"));
        assertEquals("1 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\\\n   2\n    3\n"));
        assertEquals("1\n 2  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\\\n    3\n"));
        assertEquals("1\n 2\n  3", StringUtil.stripIndentIfFirstLineEmpty("\n  1\n   2\n    3\\\n"));
    }
}
