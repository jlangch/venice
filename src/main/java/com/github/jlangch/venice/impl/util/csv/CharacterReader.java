/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import com.github.jlangch.venice.impl.util.io.CharsetUtil;


public class CharacterReader {

    public CharacterReader(final String s) {
        this(new StringReader(s == null ? "" : s));
    }

    public CharacterReader(final InputStream is, final Charset charset) {
        this(new InputStreamReader(is, CharsetUtil.charset(charset)));
    }

    public CharacterReader(final Reader r) {
        rd = r;

        final int ch = next();
        chNext = ch == EOF ? EOF : ch;
    }

    public int peek() {
        return chNext;
    }

    public void consume() {
        final int ch = next();

        if (ch != EOF) {
	        if (chNext == LF) {
	            lineNr++;
	            columnNr = 1;
	        }
	        else {
	            columnNr++;
	        }

	        chNext = ch == EOF ? EOF : ch;
        }
    }

    public boolean eof() {
        return chNext == EOF;
    }

    public int getLineNr() {
        return lineNr;
    }

    public int getColNr() {
        return columnNr;
    }


    private int next() {
        try {
            return rd.read();
        }
        catch(IOException ex) {
            throw new RuntimeException(
            		String.format(
            			"Failed to read next char from CSV reader at line %d, col %d.",
            			lineNr,
            			columnNr),
            		ex);
        }
    }


    private static final int EOF = -1;
    private static final int LF  = '\n';

    private final Reader rd;
    private int chNext;

    private int lineNr = 1;
    private int columnNr = 1;
}
