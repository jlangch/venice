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

        canvas.drawHorizontal('-', w, 0, 0);
        canvas.drawHorizontal('-', w, 0, h-1);

        canvas.drawVertical('|', h, 0, 0);
        canvas.drawVertical('|', h, w-1, 0);

        canvas.draw('+', 0, 0);
        canvas.draw('+', w-1, 0);
        canvas.draw('+', 0, h-1);
        canvas.draw('+', w-1, h-1);

        final List<String> lines = canvas.toAsciiLines();

        assertEquals("+--------+", lines.get(0));
        assertEquals("|        |", lines.get(1));
        assertEquals("|        |", lines.get(2));
        assertEquals("|        |", lines.get(3));
        assertEquals("+--------+", lines.get(4));
    }
}
