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

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.util.StringUtil;


public class AsciiCanvas {

    public AsciiCanvas(
            final int width,
            final int height
    ) {
        this.width = width;
        this.height = height;
        this.canvas = new Cell[height][width];
        clear();
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char getCharAt(final int row, final int col) {
        if (row < 0 || row >= height) {
        	throw new IndexOutOfBoundsException("The row is out of bounds [0," + (height-1) + "]");
        }
        if (col < 0 || col >= width) {
        	throw new IndexOutOfBoundsException("The col is out of bounds [0," + (width-1) + "]");
        }

        return canvas[row][col].val;
    }

    public void clear() {
        for(int h=0; h<height; h++) {
            for(int w=0; w<height; w++) {
                canvas[h][w] = new Cell();
            }
        }
    }

    public void draw(final char ch, final int row, final int col) {
    	draw(ch, "", row, col);
    }

    public void draw(final char ch, final String format, final int row, final int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            canvas[row][col] = new Cell(ch, StringUtil.trimToEmpty(format));
        }
    }

    public void drawHorizontal(final String str, final int row, final int col) {
    	drawHorizontal(str, "", row, col);
    }

    public void drawHorizontal(final String str, final String format, final int row, final int col) {
        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, format, row, col + ii++);
        }
    }

    public void drawHorizontal(final char ch, final int repeat, final int row, final int col) {
    	drawHorizontal(ch, "", repeat, row, col);
    }

    public void drawHorizontal(final char ch, final String format, final int repeat, final int row, final int col) {
        for(int ii=0; ii<repeat; ii++) {
        	draw(ch, format, row, col+ii);
        }
    }

    public void drawVertical(final String str, final int row, final int col) {
    	drawVertical(str, "", row, col);
    }

    public void drawVertical(final String str, final String format, final int row, final int col) {
        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, format, row + ii++, col);
        }
    }

    public void drawVertical(final char ch, final int repeat, final int row, final int col) {
    	drawVertical(ch, "", repeat, row, col);
    }

    public void drawVertical(final char ch, final String format, final int repeat, final int row, final int col) {
        for(int ii=0; ii<repeat; ii++) {
        	draw(ch, format, row+ii, col);
        }
    }

    public List<String> toLines() {
        final List<String> lines = new ArrayList<>();

        for(int h=height-1; h>=0; h--) {
            final StringBuilder line = new StringBuilder();
            for(int w=0; w<height; w++) {
                line.append(canvas[h][w].toString());
            }
            lines.add(line.toString());
        }

        return lines;
    }

    @Override
    public String toString() {
        return String.join("\n", toLines());
    }


    private static class Cell {
        public Cell() {
            this(' ', "");
        }

        public Cell(
            final char val,
            final String format
        ) {
            this.val = val;
            this.format = format;
        }


        @Override
        public String toString() {
            return format + val + ANSI_RESET;
        }

        private final char val;
        private final String format;
    }


    private static final String ANSI_RESET = "\u001b[0m";

    private final int width;
    private final int height;
    private final Cell[][] canvas;
}
