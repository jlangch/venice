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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.util.io.CharsetUtil;


public class CSVReader2 {

    public CSVReader2() {
        this(',', '"');
    }

    public CSVReader2(final char separator, final char quote) {
        this.separator = separator;
        this.quote = quote;
    }

    public List<List<String>> parse(final String csv) {
        return parse(new CharacterReader(csv));
    }

    public List<List<String>> parse(final InputStream is, final Charset charset) {
        return parse(new CharacterReader(is, CharsetUtil.charset(charset)));
    }

    public List<List<String>> parse(final CharacterReader rd) {
        final List<List<String>> records = new ArrayList<>();

        while(!rd.eof()) {
            final int ch = rd.peek();
            if (ch == '\r') {
                rd.consume(); // skip CR
            }
            else if (ch == '\n') {
                rd.consume(); // skip LF
            }
            else {
                final List<String> record = parseRecord(rd);
                if (!record.isEmpty()) {
                    records.add(record);
                }
            }
        }

        return records;
    }

    private List<String> parseRecord(final CharacterReader rd) {
        final List<String> record = new ArrayList<>();

        if (separator != ' ') {
            skipChars(rd, ' ');  // skip field leading spaces
        }

        while(!rd.eof()) {
             final int ch = rd.peek();

            if (ch == separator) {
                rd.consume();
                record.add("");  // empty field
            }
            else if (ch == '\r') {
                rd.consume(); // skip CR
             }
            else if (ch == '\n') {
                rd.consume();
                break;
             }
            else if (ch == quote) {
                record.add(parseQuotedField(rd));
            }
            else {
                record.add(parseField(rd));
            }
        }

        return record;
    }

    private String parseField(final CharacterReader rd) {
        final StringBuilder sb = new StringBuilder();

        while(!rd.eof()) {
             final int ch = rd.peek();

            if (ch == separator) {
                rd.consume();
                break;
            }
            else if (ch == '\r') {
                rd.consume(); // skip CR
             }
            else if (ch == '\n') {
                rd.consume();
                break;
             }
            else {
                sb.append((char)ch);
            }
        }

        return sb.toString().trim();
    }

    private String parseQuotedField(final CharacterReader rd) {
        final StringBuilder sb = new StringBuilder();

        readLeadingFieldQuote(rd);

        // read field
        while(!rd.eof()) {
            int ch = rd.peek();

            if (ch == quote) {
                rd.consume();

                int chNext = rd.peek();
                if (chNext == quote) {
                	sb.append(quote);
                    rd.consume();
                }
                else {
                	break;  // read trailing field quote
                }
            }
            else {
                sb.append((char)ch);
            }
        }

        readTrailingFieldCharsUpToSeparator(rd);

        return sb.toString().trim();
    }

    private void skipChars(final CharacterReader rd, final char skipCh) {
        while(rd.peek() == skipCh) rd.consume();
    }

    private void readLeadingFieldQuote(final CharacterReader rd) {
        int ch = rd.peek();
        if (ch != quote) {
            throw new RuntimeException(
                    String.format(
                            "Expected CSV leading fieldquote '%c' at line %d, col %d.",
                            quote,
                            rd.getLineNr(),
                            rd.getColNr()));
        }
        rd.consume();
    }

    private void readTrailingFieldCharsUpToSeparator(final CharacterReader rd) {
        if (separator != ' ') {
            skipChars(rd, ' ');  // skip field trailing spaces
        }
        skipChars(rd, '\r');  // skip CR

        if (!rd.eof()) {
            int ch = rd.peek();
            if (ch == separator) {
                rd.consume();
            }
            else if (ch == '\n') {
                rd.consume();
            }
            else {
                throw new RuntimeException(
                        String.format(
                                "Unexpected char '%c' after quoted field at line %d, col %d.",
                                ch,
                                rd.getLineNr(),
                                rd.getColNr()));
             }
        }
    }


    private final char separator;
    private final char quote;
}
