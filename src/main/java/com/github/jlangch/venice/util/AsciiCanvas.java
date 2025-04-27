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

/**
 * Defines an ASCII based canvas for drawing ASCII characters
 */
public class AsciiCanvas {

	/**
	 * Creates a canvas in the given dimension. Initializes the canvas with
	 * spaces.
	 *
	 * @param width the width (characters), must be in the range 1..1000
	 * @param height the height (characters), must be in the range 1..1000
	 */
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


    /**
     * @return the canvas's width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the canvas's height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the character as position (x,y)
     *
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     * @return The character at position (x,y)
     */
    public char getCharAt(final int x, final int y) {
        if ( x < 0 ||  x >= width) {
            throw new IndexOutOfBoundsException("The x is out of bounds [0," + (width-1) + "]");
        }
        if (y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("The y is out of bounds [0," + (height-1) + "]");
        }

        return getCellAt(x,y).val;
    }

    /**
     * Clears the canvas by filling he canvas with spaces.
     *
     * @return this canvas
     */
    public AsciiCanvas clear() {
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                canvas[y][x] = new Cell();
            }
        }
        return this;
    }

    /**
     * Draw a character at the position (x,y)
     *
     * @param ch The character
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas draw(final char ch, final int x, final int y) {
        draw(ch, "", x, y);
        return this;
    }

    public AsciiCanvas draw(final char ch, final String ansiFormat, final int x, final int y) {
        if (inbound(x,y)) {
            canvas[y][x] = new Cell(ch, StringUtil.trimToEmpty(ansiFormat));
        }
        return this;
    }

    /**
     * Draw text at the position (x,y). The text is clipped at the canvas' border
     *
     * @param text The text
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawText(final String text, final int x, final int y) {
    	drawHorizontalRight(text, "", x, y);
        return this;
    }

    public AsciiCanvas drawText(final String text, final String ansiFormat, final int x, final int y) {
        drawHorizontalRight(text, ansiFormat, x, y);
        return this;
    }

    /**
     * Draw a horizontal string starting at the position (x,y). The text is clipped at the
     * canvas' border
     *
     * @param str The string
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawHorizontalRight(final String str, final int x, final int y) {
        drawHorizontalRight(str, "", x, y);
        return this;
    }

    public AsciiCanvas drawHorizontalRight(final String str, final String ansiFormat, final int x, final int y) {
        if (str == null) {
            return this;
        }

        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, ansiFormat, x + ii++, y);
        }
        return this;
    }

    /**
     * Draw a character repeating it horizontally n-times starting at the position (x,y).
     * The text is clipped at the canvas' border
     *
     * @param ch The character
     * @param repeat The number of repetitions
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawHorizontalRight(final char ch, final int repeat, final int x, final int y) {
        drawHorizontalRight(ch, repeat, "", x, y);
        return this;
    }

    public AsciiCanvas drawHorizontalRight(final char ch, final int repeat, final String ansiFormat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
            draw(ch, ansiFormat, x+ii, y);
        }
        return this;
    }


    /**
     * Draw a horizontal string starting at the position (x,y). The text is clipped at the
     * canvas' border
     *
     * @param str The string
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawHorizontalLeft(final String str, final int x, final int y) {
        drawHorizontalLeft(str, "", x, y);
        return this;
    }

    public AsciiCanvas drawHorizontalLeft(final String str, final String ansiFormat, final int x, final int y) {
        if (str == null) {
            return this;
        }

        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, ansiFormat, x + ii--, y);
        }
        return this;
    }

    /**
     * Draw a character repeating it horizontally n-times starting at the position (x,y).
     * The text is clipped at the canvas' border
     *
     * @param ch The character
     * @param repeat The number of repetitions
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawHorizontalLeft(final char ch, final int repeat, final int x, final int y) {
        drawHorizontalLeft(ch, repeat, "", x, y);
        return this;
    }

    public AsciiCanvas drawHorizontalLeft(final char ch, final int repeat, final String ansiFormat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
            draw(ch, ansiFormat, x-ii, y);
        }
        return this;
    }

    /**
     * Draw a vertical string up starting at the position (x,y). The text is clipped at the
     * canvas' border
     *
     * @param str The string
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawVerticalUp(final String str, final int x, final int y) {
        if (str == null) {
            return this;
        }

        drawVerticalUp(str, "", x, y);
        return this;
    }

    public AsciiCanvas drawVerticalUp(final String str, final String ansiFormat, final int x, final int y) {
        if (str == null) {
            return this;
        }

        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, ansiFormat, x, y + ii++);
        }
        return this;
    }

    /**
     * Draw a character repeating it vertically n-times starting at the position (x,y).
     * The text is clipped at the canvas' border
     *
     * @param ch The character
     * @param repeat The number of repetitions
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawVerticalUp(final char ch, final int repeat, final int x, final int y) {
        drawVerticalUp(ch, repeat, "", x, y);
        return this;
    }

    public AsciiCanvas drawVerticalUp(final char ch, final int repeat, final String ansiFormat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
            draw(ch, ansiFormat, x, y+ii);
        }
        return this;
    }


    /**
     * Draw a vertical string up starting at the position (x,y). The text is clipped at the
     * canvas' border
     *
     * @param str The string
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawVerticalDown(final String str, final int x, final int y) {
        if (str == null) {
            return this;
        }

        drawVerticalDown(str, "", x, y);
        return this;
    }

    public AsciiCanvas drawVerticalDown(final String str, final String ansiFormat, final int x, final int y) {
        if (str == null) {
            return this;
        }

        int ii=0;
        for(char ch : str.toCharArray()) {
            draw(ch, ansiFormat, x, y + ii--);
        }
        return this;
    }

    /**
     * Draw a character repeating it vertically n-times starting at the position (x,y).
     * The text is clipped at the canvas' border
     *
     * @param ch The character
     * @param repeat The number of repetitions
     * @param x The x position (0..width-1)
     * @param y The y position (0..height-1)
     *
     * @return this canvas
     */
    public AsciiCanvas drawVerticalDown(final char ch, final int repeat, final int x, final int y) {
        drawVerticalDown(ch, repeat, "", x, y);
        return this;
    }

    public AsciiCanvas drawVerticalDown(final char ch, final int repeat, final String ansiFormat, final int x, final int y) {
        for(int ii=0; ii<repeat; ii++) {
            draw(ch, ansiFormat, x, y-ii);
        }
        return this;
    }

    /**
     * Draw box at the position (x,y) with the given width and height
     * The box is clipped at the canvas' border
     *
     * <p>
     * The box' border is specified by a string of 8 characters
     *
     * <ol>
     * <li>top left char</li>
     * <li>top right char</li>
     * <li>bottom right char</li>
     * <li>bottom left char</li>
     * <li>top bar char</li>
     * <li>right bar char</li>
     * <li>bottom bar char</li>
     * <li>left bar char</li>
     * </ol>
     *
     * <p>E.g.: "┌┐┘└─│─│" forms a box:
     * <pre>
     * ┌────┐
     * │    │
     * └────┘
     * </pre>
     *
     * @param x The x position
     * @param y The y position
     * @param w The width
     * @param h The height
     * @param border The box' border characters (must be exactly 8 chars)
     *
     * @return this canvas
     */
    public AsciiCanvas drawBox(final int x, final int y, final int w, final int h, final String border) {
    	drawBox(x, y, w, h, border, "");
        return this;
    }

    public AsciiCanvas drawBox(final int x, final int y, final int w, final int h, final String border, final String ansiFormat) {
        if (border == null || border.length() != 8) {
            throw new IllegalArgumentException(
                    "The box' border must have 8 chars: "
                    + "[topLeft,topRight,bottomRight,bottomLeft,topBar,rightBar,bottomBar,leftBar]. "
                    + "E.g. \"┌┐┘└─│─│\"");
        }

        final char topLeft = border.charAt(0);
        final char topRight = border.charAt(1);
        final char bottomRight = border.charAt(2);
        final char bottomLeft = border.charAt(3);
        final char topBar = border.charAt(4);
        final char rightBar = border.charAt(5);
        final char bottomBar = border.charAt(6);
        final char leftBar = border.charAt(7);

        draw(topLeft,     ansiFormat, x,     y+h-1);
        draw(topRight,    ansiFormat, x+w-1, y+h-1);
        draw(bottomLeft,  ansiFormat, x,     y);
        draw(bottomRight, ansiFormat, x+w-1, y);

        drawHorizontalRight(topBar,    w-2, ansiFormat, x+1, y+h-1);
        drawHorizontalRight(bottomBar, w-2, ansiFormat, x+1, y);

        drawVerticalUp(leftBar,  h-2, ansiFormat, x,     y+1);
        drawVerticalUp(rightBar, h-2, ansiFormat, x+w-1, y+1);
        return this;
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

    /**
     * Returns the canvas as a list of strings
     *
     * <pre>
     *
     * y ^
     *   .
     *   * (0,5)
     *   .        * (9,4)
     *   .
     *   .
     *   .
     *   ....................&gt;
     *  (0,0)                x
     *
     * </pre>
     *
     * <p>E.g.:
     *
     * <pre>
     *
     * y ^
     *   .
     *   .  +---------+
     *   .  |         |
     *   .  +--+   +--+
     *   .     |   |
     *   .     +---+
     *   .
     *   ....................&gt;
     *  (0,0)                x
     *
     * </pre>
     *
     * <pre>
     * Arrays.toList(new String[] {
     *   "                ",
     *   "  +---------+   ",
     *   "  |         |   ",
     *   "  +--+   +--+   ",
     *   "     |   |      ",
     *   "     +---+      ",
     *   "                " } );
     * </pre>
      *
     *
     * @return the canvas as a list of strings
     */
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


    private static final String ESC = "\u001b";
    private static final String ANSI_RESET = ESC + "[0m";

    private final int width;
    private final int height;
    private final Cell[][] canvas;
}
