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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;


public class AsciiCanvasTest {

    @Test
    public void test1() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawHorizontalRight('-', w, 0, 0);
        canvas.drawHorizontalRight('-', w, 0, h-1);

        canvas.drawVerticalUp('|', h, 0,   0);
        canvas.drawVerticalUp('|', h, w-1, 0);

        canvas.draw('+', 0,   0);
        canvas.draw('+', w-1, 0);
        canvas.draw('+', 0,   h-1);
        canvas.draw('+', w-1, h-1);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("+--------+", lines.get(0));
        assertEquals("|        |", lines.get(1));
        assertEquals("|        |", lines.get(2));
        assertEquals("|        |", lines.get(3));
        assertEquals("+--------+", lines.get(4));
    }

    @Test
    public void test2() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawBox(0,0,10,5,"++++-|-|");

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("+--------+", lines.get(0));
        assertEquals("|        |", lines.get(1));
        assertEquals("|        |", lines.get(2));
        assertEquals("|        |", lines.get(3));
        assertEquals("+--------+", lines.get(4));
    }

    @Test
    public void test3() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawBox(0,0,10,5,"┌┐┘└─│─│");

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("┌────────┐", lines.get(0));
        assertEquals("│        │", lines.get(1));
        assertEquals("│        │", lines.get(2));
        assertEquals("│        │", lines.get(3));
        assertEquals("└────────┘", lines.get(4));
    }

    @Test
    public void test4() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawText("Hello", 0, 4);
        canvas.drawText("Hello", 1, 3);
        canvas.drawText("Hello", 2, 2);
        canvas.drawText("Hello", 3, 1);
        canvas.drawText("Hello", 4, 0);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("Hello     ", lines.get(0));
        assertEquals(" Hello    ", lines.get(1));
        assertEquals("  Hello   ", lines.get(2));
        assertEquals("   Hello  ", lines.get(3));
        assertEquals("    Hello ", lines.get(4));
    }

    @Test
    public void test5() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawText("Hello", -2, 4);
        canvas.drawText("Hello", -1, 3);
        canvas.drawText("Hello",  7, 2);
        canvas.drawText("Hello",  8, 1);
        canvas.drawText("Hello",  9, 0);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("llo       ", lines.get(0));
        assertEquals("ello      ", lines.get(1));
        assertEquals("       Hel", lines.get(2));
        assertEquals("        He", lines.get(3));
        assertEquals("         H", lines.get(4));
    }

    @Test
    public void test6() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawHorizontalRight("Hello", -2, 4);
        canvas.drawHorizontalRight("Hello", -1, 3);
        canvas.drawHorizontalRight("Hello",  7, 2);
        canvas.drawHorizontalRight("Hello",  8, 1);
        canvas.drawHorizontalRight("Hello",  9, 0);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("llo       ", lines.get(0));
        assertEquals("ello      ", lines.get(1));
        assertEquals("       Hel", lines.get(2));
        assertEquals("        He", lines.get(3));
        assertEquals("         H", lines.get(4));
    }

    @Test
    public void test7() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawHorizontalLeft("Hello",  2, 4);
        canvas.drawHorizontalLeft("Hello",  4, 3);
        canvas.drawHorizontalLeft("Hello",  6, 2);
        canvas.drawHorizontalLeft("Hello",  8, 1);
        canvas.drawHorizontalLeft("Hello", 10, 0);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("leH       ", lines.get(0));
        assertEquals("olleH     ", lines.get(1));
        assertEquals("  olleH   ", lines.get(2));
        assertEquals("    olleH ", lines.get(3));
        assertEquals("      olle", lines.get(4));
    }

    @Test
    public void test8() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawVerticalUp("Hello", 0, -2);
        canvas.drawVerticalUp("Hello", 2,  0);
        canvas.drawVerticalUp("Hello", 4,  2);
        canvas.drawVerticalUp("Hello", 6,  4);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("  o l H   ", lines.get(0));
        assertEquals("  l e     ", lines.get(1));
        assertEquals("o l H     ", lines.get(2));
        assertEquals("l e       ", lines.get(3));
        assertEquals("l H       ", lines.get(4));
    }

    @Test
    public void test9() {
        final int w = 10;
        final int h = 5;

        final AsciiCanvas canvas = new AsciiCanvas(w, h);

        canvas.drawVerticalDown("Hello", 0,  4);
        canvas.drawVerticalDown("Hello", 2,  3);
        canvas.drawVerticalDown("Hello", 4,  2);
        canvas.drawVerticalDown("Hello", 6,  1);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("H         ", lines.get(0));
        assertEquals("e H       ", lines.get(1));
        assertEquals("l e H     ", lines.get(2));
        assertEquals("l l e H   ", lines.get(3));
        assertEquals("o l l e   ", lines.get(4));
    }
}
