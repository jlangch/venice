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
    	if (width < 1 || width > 1000) {
    		throw new IllegalArgumentException("A width must be in the range [1..1000]");
    	}
    	if (height < 1 || height > 1000) {
    		throw new IllegalArgumentException("A height must be in the range [1..1000]");
   	    }

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

    public char getCharAt(final int x, final int y) {
        if ( x < 0 ||  x >= width) {
        	throw new IndexOutOfBoundsException("The x is out of bounds [0," + (width-1) + "]");
        }
        if (y < 0 || y >= height) {
        	throw new IndexOutOfBoundsException("The y is out of bounds [0," + (height-1) + "]");
        }

        return getCellAt(x,y).val;
    }

    public void clear() {
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                canvas[y][x] = new Cell();
            }
        }
    }

    public void draw(final char ch, final int x, final int y) {
    	draw(ch, "", x, y);
    }

    public void draw(final char ch, final String format, final int x, final int y) {
        if (inbound(x,y)) {
            canvas[y][x] = new Cell(ch, StringUtil.trimToEmpty(format));
        }
    }

    public void drawText(final String text, final int x, final int y) {
    	drawText(text, "", x, y);
    }

    public void drawText(final String text, final String format, final int x, final int y) {
    	if (text == null) {
    		return;
    	}

    	for(int ii=0; ii<text.length(); ii++) {
    		draw(text.charAt(ii), format, x + ii, y);
    	}
    }

    public void drawHorizontal(final String str, final int x, final int y) {
    	drawHorizontal(str, "", x, y);
    }

    public void drawHorizontal(final String str, final String format, final int x, final int y) {
        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, format, x + ii++, y);
        }
    }

    public void drawHorizontal(final char ch, final int repeat, final int x, final int y) {
    	drawHorizontal(ch, "", repeat, x, y);
    }

    public void drawHorizontal(final char ch, final String format, final int repeat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
        	draw(ch, format, x+ii, y);
        }
    }

    public void drawVertical(final String str, final int x, final int y) {
    	drawVertical(str, "", x, y);
    }

    public void drawVertical(final String str, final String format, final int x, final int y) {
        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, format, x, y + ii++);
        }
    }

    public void drawVertical(final char ch, final int repeat, final int x, final int y) {
    	drawVertical(ch, "", repeat, x, y);
    }

    public void drawVertical(final char ch, final String format, final int repeat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
        	draw(ch, format, x, y+ii);
        }
    }

    public void drawBox(final int x, final int y, final int w, final int h, final String elements) {
    	if (elements == null || elements.length() != 8) {
    		throw new IllegalArgumentException(
    				"The box elements must have 8 chars: "
    				+ "[topLeft,topRight,bottomRight,bottomLeft,topBar,rightBar,bottomBar,leftBar]. "
    				+ "E.g. \"┌┐┘└─│─│\"");
    	}

    	final char topLeft = elements.charAt(0);
    	final char topRight = elements.charAt(1);
    	final char bottomRight = elements.charAt(2);
    	final char bottomLeft = elements.charAt(3);
    	final char topBar = elements.charAt(4);
    	final char rightBar = elements.charAt(5);
    	final char bottomBar = elements.charAt(6);
    	final char leftBar = elements.charAt(7);

        draw(topLeft,     x,     y+h-1);
        draw(topRight,    x+w-1, y+h-1);
        draw(bottomLeft,  x,     y);
        draw(bottomRight, x+w-1, y);

        drawHorizontal(topBar,    w-2, x+1, y+h-1);
        drawHorizontal(bottomBar, w-2, x+1, y);

        drawVertical(leftBar,  h-2, x,     y+1);
        drawVertical(rightBar, h-2, x+w-1, y+1);
    }

    public List<String> toAnsiLines() {
        final List<String> lines = new ArrayList<>();

        for(int y=height-1; y>=0; y--) {
            final StringBuilder line = new StringBuilder();
            for(int x=0; x<width; x++) {
                line.append(getCellAt(x,y).toString());
            }
            lines.add(line.toString());
        }

        return lines;
    }

    public List<String> toAsciiLines() {
        final List<String> lines = new ArrayList<>();

        for(int y=height-1; y>=0; y--) {
            final StringBuilder line = new StringBuilder();
            for(int x=0; x<width; x++) {
                line.append(getCellAt(x,y).val);
            }
            lines.add(line.toString());
        }

        return lines;
    }

    @Override
    public String toString() {
        return String.join("\n", toAnsiLines());
    }

    private Cell getCellAt(final int x, final int y) {
        if ( x < 0 ||  x >= width) {
        	throw new IndexOutOfBoundsException("The x is out of bounds [0," + (width-1) + "]");
        }
        if (y < 0 || y >= height) {
        	throw new IndexOutOfBoundsException("The y is out of bounds [0," + (height-1) + "]");
        }

        return canvas[y][x];
    }

    private boolean inbound(final int x, final int y) {
    	return (y >= 0 && y < height && x >= 0 &&  x < width);
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
