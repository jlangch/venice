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
package com.github.jlangch.venice.impl.util.csv;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.util.io.CharsetUtil;


/**
 * CSV Reader
 *
 * https://docs.fileformat.com/spreadsheet/csv/
 */
public class CSVReader {

    public CSVReader() {
        this(',', '"');
    }

    public CSVReader(final char separator, final char quote) {
        this.separator = separator;
        this.quote = quote;

        if (quote == separator) {
        	throw new RuntimeException(
        			"The quote and the separator character must not be identical!");
        }
    }

    public List<List<String>> parse(final String csv) {
        return parse(new CharacterReader(csv));
    }

    public List<List<String>> parse(final Reader rd) {
        return parse(new CharacterReader(rd));
    }

    public List<List<String>> parse(final InputStream is, final Charset charset) {
        return parse(new CharacterReader(is, CharsetUtil.charset(charset)));
    }


    private List<List<String>> parse(final CharacterReader rd) {
        final List<List<String>> records = new ArrayList<>();

        while(!rd.isEof()) {
            final int ch = rd.peek();
            if (ch == '\r' || ch == '\n') {
                rd.consume(); // skip CR / LF
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
        // empty line?
    	rd.skipAllOfChar('\r');  // skip CR
        if (rd.peek() == '\n') {
            rd.consume();
            return new ArrayList<>();
        }
        if (rd.isEof()) {
            return new ArrayList<>();
        }


        final List<String> record = new ArrayList<>();


        while(!rd.isEof()) {
            final int ch = rd.peek();

            if (ch == '\r') {
            	rd.skipAllOfChar('\r');  // skip CR
            }
            else if (ch == '\n') {
                rd.consume();
                break;
            }
            else if (ch == quote) {
            	rd.consume();
                record.add(parseQuotedField(rd));
            }
            else {
                record.add(parseField(rd));
            }

            if (rd.peek() == separator) {
                rd.consume();
                rd.skipAllOfChar('\r');
                if (rd.peek() == '\n' || rd.isEof()) {
                	record.add(null);
                }
            }
        }

        return record;
    }

    private String parseField(final CharacterReader rd) {
        final StringBuilder sb = new StringBuilder();

        while(!rd.isEof()) {
            final int ch = rd.peek();

            if (ch == separator || ch == '\r' || ch == '\n') {
                break;
            }
            else if (ch == quote) {
                throw new RuntimeException(
                        String.format(
                                "The quote char '%c' must not appear in a non quoted field at line %d, col %d.",
                                ch,
                                rd.getLineNr(),
                                rd.getColNr()));
            }
            else {
            	rd.consume();
                sb.append((char)ch);
            }
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    private String parseQuotedField(final CharacterReader rd) {
        final StringBuilder sb = new StringBuilder();

        // read field
        while(!rd.isEof()) {
            int ch = rd.peek();
            if (ch == quote) {
            	// trailing quote or escaped quote?
                rd.consume();

                int chNext = rd.peek();
                if (chNext == quote) {
                	sb.append(quote); // escaped quote
                    rd.consume();
                }
                else {
                	break;  // trailing quote
                }
            }
            else {
                sb.append((char)ch);
            	rd.consume();
            }
        }

        readTrailingFieldCharsUpToSeparator(rd);

        return sb.toString().trim();
    }

    private void readTrailingFieldCharsUpToSeparator(final CharacterReader rd) {
        rd.skipAllOfChar('\r');  // skip CR

        if (!rd.isEof()) {
            int ch = rd.peek();
            if (ch == separator || ch == '\n') {
                return;
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
