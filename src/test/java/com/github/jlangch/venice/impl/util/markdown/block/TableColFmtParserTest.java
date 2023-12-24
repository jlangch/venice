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

import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.CENTER;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.LEFT;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment.RIGHT;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit.AUTO;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit.EM;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit.PERCENT;
import static com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.WidthUnit.PX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.Width;


public class TableColFmtParserTest {

    @Test
    public void test_md_format_empty() {
        final TableColFmtParser parser = new TableColFmtParser();

        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse(" "));
        assertNull(parser.parse("    "));
    }

    @Test
    public void test_md_format_unknown() {
        final TableColFmtParser parser = new TableColFmtParser();

        assertNull(parser.parse("x"));
        assertNull(parser.parse(" x "));

        assertNull(parser.parse("-"));
        assertNull(parser.parse("--"));

        assertNull(parser.parse(" - "));
        assertNull(parser.parse(" -- "));

        assertNull(parser.parse(":"));
        assertNull(parser.parse("::"));
        assertNull(parser.parse(":::"));
    }

    @Test
    public void test_md_alignment() {
        final TableColFmtParser parser = new TableColFmtParser();

        assertEquals(LEFT, parser.parse(":-").horzAlignment());
        assertEquals(LEFT, parser.parse(":--").horzAlignment());
        assertEquals(LEFT, parser.parse(":---").horzAlignment());
        assertEquals(LEFT, parser.parse(":----").horzAlignment());

        assertEquals(CENTER, parser.parse(":-:").horzAlignment());
        assertEquals(CENTER, parser.parse(":--:").horzAlignment());
        assertEquals(CENTER, parser.parse(":---:").horzAlignment());

        assertEquals(CENTER, parser.parse("---").horzAlignment());
        assertEquals(CENTER, parser.parse("----").horzAlignment());
        assertEquals(CENTER, parser.parse("-----").horzAlignment());

        assertEquals(RIGHT, parser.parse("-:").horzAlignment());
        assertEquals(RIGHT, parser.parse("--:").horzAlignment());
        assertEquals(RIGHT, parser.parse("---:").horzAlignment());
        assertEquals(RIGHT, parser.parse("----:").horzAlignment());
    }

    @Test
    public void test_css_alignment() {
        final TableColFmtParser parser = new TableColFmtParser();

        assertEquals(LEFT, parser.parse("[![text-align: left]]").horzAlignment());
        assertEquals(LEFT, parser.parse("[![text-align: left;]]").horzAlignment());

        assertEquals(CENTER, parser.parse("[![text-align: center]]").horzAlignment());
        assertEquals(CENTER, parser.parse("[![text-align: center;]]").horzAlignment());

        assertEquals(RIGHT, parser.parse("[![text-align: right]]").horzAlignment());
        assertEquals(RIGHT, parser.parse("[![text-align: right;]]").horzAlignment());
    }

    @Test
    public void test_css_width() {
        final TableColFmtParser parser = new TableColFmtParser();

        Width width;

        width = parser.parse("[![width: auto]]").width();
        assertEquals(AUTO, width.getUnit());

        width = parser.parse("[![width: 10%]]").width();
        assertEquals(PERCENT, width.getUnit());
        assertEquals(10L, width.getValue());

        width = parser.parse("[![width: 10px]]").width();
        assertEquals(PX, width.getUnit());
        assertEquals(10L, width.getValue());

        width = parser.parse("[![width: 10em]]").width();
        assertEquals(EM, width.getUnit());
        assertEquals(10L, width.getValue());
    }

}
