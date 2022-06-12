/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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

import com.github.jlangch.venice.impl.util.markdown.chunk.ChunkParser;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;


public class TableBlock implements Block {

    public TableBlock() {
    }

    public TableBlock(
        final int cols,
        final List<List<Chunks>> bodyRows
    ) {
        addFormat(cols, new ArrayList<>());
        addHeaderRow(cols, new ArrayList<>());
        addBodyRows(cols, bodyRows);
    }

    public TableBlock(
        final int cols,
        final List<TableColFmt> format,
        final List<List<Chunks>> bodyRows
    ) {
        addFormat(cols, format);
        addHeaderRow(cols, new ArrayList<>());
        addBodyRows(cols, bodyRows);
    }


    public TableBlock(
        final int cols,
        final List<TableColFmt> format,
        final List<Chunks> headerRow,
        final List<List<Chunks>> bodyRows
    ) {
        addFormat(cols, format);
        addHeaderRow(cols, headerRow);
        addBodyRows(cols, bodyRows);
    }


    public int cols() {
        return format.size();
    }

    public int bodyRows() {
        return bodyRows.size();
    }

    public boolean hasHeader() {
        return !headerRow.isEmpty();
    }

    public TableColFmt format(final int col) {
        return col >= format.size() ? new TableColFmt() : format.get(col);
    }

    public Chunks headerCell(final int col) {
        return col >= headerRow.size() ? new Chunks() : headerRow.get(col);
    }

    public Chunks bodyCell(final int row, final int col) {
        if (row >= bodyRows.size()) {
            return new Chunks();
        }
        else {
            final List<Chunks> bodyRow = bodyRows.get(row);
            return col >= bodyRow.size() ? new Chunks() : bodyRow.get(col);
        }
    }

    @Override
    public boolean isEmpty() {
        return headerRow.isEmpty() && bodyRows.isEmpty();
    }

    @Override
    public void parseChunks() {
        headerRow = parseChunks(headerRow);

        for(int ii=0; ii<bodyRows.size(); ii++) {
            bodyRows.set(ii, parseChunks(bodyRows.get(ii)));
        }
    }

    private void addFormat(final int cols, final List<TableColFmt> formats) {
        for(int ii=0; ii<cols; ii++) {
            format.add(
                formats != null && ii<formats.size() ? formats.get(ii) : new TableColFmt());
        }
    }

    private void addHeaderRow(final int cols, final List<Chunks> row) {
        if (row == null || row.isEmpty()) {
            return;
        }

        for(int ii=0; ii<cols; ii++) {
            headerRow.add(
                ii<row.size() ? row.get(ii) : new Chunks());
        }
    }

    private void addBodyRows(final int cols, final List<List<Chunks>> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        for(List<Chunks> row : rows) {
            final List<Chunks> bodyRow = new ArrayList<>();
            bodyRows.add(bodyRow);

            for(int ii=0; ii<cols; ii++) {
                bodyRow.add(
                    row != null && ii<row.size() ? row.get(ii) : new Chunks());
            }
        }
    }

    private List<Chunks> parseChunks(final List<Chunks> chunks) {
        return chunks.stream()
                     .map(c -> new ChunkParser(c).parse())
                     .collect(Collectors.toList());
    }


    private List<TableColFmt> format = new ArrayList<>();
    private List<Chunks> headerRow = new ArrayList<>();
    private List<List<Chunks>> bodyRows = new ArrayList<>();
}
