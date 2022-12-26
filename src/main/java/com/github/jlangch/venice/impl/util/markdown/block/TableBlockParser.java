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
package com.github.jlangch.venice.impl.util.markdown.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.reader.CharacterReader;
import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.UTF8;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.RawChunk;


public class TableBlockParser {

    public TableBlockParser(final LineReader reader) {
        this.reader = reader;
    }

    public TableBlock parse() {
        if (reader.eof()) {
            return new TableBlock();
        }

        final List<String> rawRows = parseRawRows();
        if (rawRows.isEmpty()) {
            return new TableBlock();
        }

        final List<List<String>> cells = rawRows
                                            .stream()
                                            .map(r -> split(r))
                                            .collect(Collectors.toList());

        final int cols = cells.get(0).size();

        if (isFormatRow(cells.get(0))) {
            final List<String> formatRow = cells.get(0);
            final List<List<String>> body = cells.subList(1, cells.size());

            final TableBlock block = new TableBlock(
                                            cols,
                                            parseColFormats(formatRow),
                                            toChunks2(body));
            block.parseChunks();
            return block;
        }
        else if (cells.size() > 1 && isFormatRow(cells.get(1))) {
            final List<String> headerRow = cells.get(0);
            final List<String> formatRow = cells.get(1);
            final List<List<String>> body = cells.subList(2, cells.size());

            final TableBlock block = new TableBlock(
                                            cols,
                                            parseColFormats(formatRow),
                                            toChunks(headerRow),
                                            toChunks2(body));
            block.parseChunks();
            return block;
        }
        else {
            final List<List<String>> body = cells;

            final TableBlock block = new TableBlock(cols, toChunks2(body));
            block.parseChunks();
            return block;
        }
    }


    public static boolean isBlockStart(final String line) {
        return isRow(line);
    }

    private static boolean isRow(final String line) {
        return line.matches(" *[|].*[|] *");
    }

    private List<String> parseRawRows() {
        final List<String> rows = new ArrayList<>();

        String line = reader.peek();

        while(line != null && isRow(line)) {
            reader.consume();

            rows.add(line.trim().replaceAll("&nbsp;", String.valueOf(UTF8.ZERO_WIDTH_SPACE)));

            line = reader.peek();
        }

        return rows;
    }

    private List<String> split(final String line) {
        final CharacterReader reader = new CharacterReader(line);

        final List<String> cells = new ArrayList<>();

        StringBuilder cell = new StringBuilder();

        int ch = reader.peek();
        if (ch == '|') reader.consume();

        while(true) {
            ch = reader.peek();
            reader.consume();

            if (ch == EOF) {
                break;
            }
            else if (ch == '\\') {
                int next = reader.peek();
                if (next == '|') {
                    reader.consume();
                    cell.append((char)next);
                }
                else {
                    cell.append((char)ch);
                }
            }
            else if (ch == '|') {
                cells.add(cell.toString().trim());
                cell = new StringBuilder();
            }
            else {
                cell.append((char)ch);
            }
        }

        return cells;
    }

    private List<TableColFmt> parseColFormats(final List<String> rows) {
        final List<TableColFmt> align = new ArrayList<>();

        final TableColFmtParser parser = new TableColFmtParser();

        for(String c : rows) {
            final TableColFmt fmt = parser.parse(c);

            align.add(fmt == null ? new TableColFmt() : fmt);
        }

        return align;
    }

    private boolean isFormatRow(final List<String> row) {
        for(String col : row) {
            if (new TableColFmtParser().parse(col) != null) {
                return true;
            }
        }
        return false;
    }

    private List<Chunks> toChunks(final List<String> list) {
        return list.stream()
                   .map(s -> parseLine(s))
                   .collect(Collectors.toList());
    }

    private List<List<Chunks>> toChunks2(final List<List<String>> list) {
        return list.stream()
                   .map(l -> toChunks(l))
                   .collect(Collectors.toList());
    }

    private Chunks parseLine(final String line) {
        final Chunks chunks = new Chunks();

        if (line.contains("¶")) {
            final CharacterReader reader = new CharacterReader(line);

            String chunk = "";

            while(true) {
                int ch = reader.peek();
                reader.consume();

                if (ch == EOF) {
                    break;
                }
                else if (ch == '\\') {
                    int next = reader.peek();

                    if (next == '¶') {
                        // escaped pilcrow -> no line break
                        reader.consume();
                        chunk = chunk + (char)next;
                    }
                    else {
                        chunk = chunk + (char)ch;
                    }
                }
                else if (ch == '¶') {
                    chunks.add(new RawChunk(chunk.trim()));
                    chunks.add(new LineBreakChunk());
                    chunk = "";
                }
                else {
                    chunk = chunk + (char)ch;
                }
            }

            chunks.add(new RawChunk(chunk.trim()));
        }
        else {
            chunks.add(new RawChunk(line.trim()));
        }

        return chunks;
    }



    private static final int EOF = -1;
    private final LineReader reader;
}
