/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;
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
			
			return new TableBlock(cols, parseAlignments(formatRow), toChunks2(body));
		}
		else if (cells.size() > 1 && isFormatRow(cells.get(1))) {
			final List<String> headerRow = cells.get(0);
			final List<String> formatRow = cells.get(1);
			final List<List<String>> body = cells.subList(2, cells.size());

			return new TableBlock(
						cols, 
						parseAlignments(formatRow), 
						toChunks(headerRow), 
						toChunks2(body));
		}
		else {
			final List<List<String>> body = cells;

			return new TableBlock(cols, toChunks2(body));
		}
	}
	
	
	public static boolean isBlockStart(final String line) {
		return isRow(line);
	}

	private static boolean isRow(final String line) {
		return line.matches(" *|.*| *");
	}

	private List<String> parseRawRows() {
		final List<String> rows = new ArrayList<>();
				
		String line = reader.peek();

		while(isRow(line)) {
			reader.consume();

			rows.add(line.trim());
			
			line = reader.peek();
		}
				
		return rows;
	}
	
	private List<String> split(final String line) {
		final CharacterReader reader = new CharacterReader(line);

		final List<String> cols = new ArrayList<>();
		
		StringBuilder col = new StringBuilder();

		int ch = reader.peek();
		if (ch == '|') reader.consume();
			
		while(true) {
			ch = reader.peek();
			reader.consume();
			
			if (ch == EOF) {
				break;
			}
			else if (ch == '\\') {
				ch = reader.peek();
				if (ch != EOF) {
					reader.consume();
					col.append((char)ch);
				}
			}
			else if (ch == '|') {
				cols.add(col.toString().trim());
				col = new StringBuilder();
			}
			else {
				col.append((char)ch);
			}
		}
		
		return cols;
	}

	private List<TableBlock.Alignment> parseAlignments(final List<String> row) {
		final List<TableBlock.Alignment> align = new ArrayList<>();

		for(String s : row) {
			if (isCenterAlign(s)) {
				align.add(TableBlock.Alignment.CENTER);
			}
			else if (isLeftAlign(s)) {
				align.add(TableBlock.Alignment.LEFT);
			}
			else if (isRightAlign(s)) {
				align.add(TableBlock.Alignment.RIGHT);
			}
			else {
				align.add(TableBlock.Alignment.LEFT);
			}
		}
		
		return align;
	}

	private boolean isFormatRow(final List<String> row) {
		for(String col : row) {
			if (isCenterAlign(col) || isLeftAlign(col) || isRightAlign(col)) {
				return true;
			}
		}
		return false;
	}

	private boolean isCenterAlign(final String s) {
		return s.matches("---+");
	}
	
	private boolean isLeftAlign(final String s) {
		return s.matches("[:]--+");
	}
	
	private boolean isRightAlign(final String s) {
		return s.matches("-+-[:]");
	}
	
	private List<Chunks> toChunks(final List<String> list) {
		return list.stream()
				   .map(s -> new Chunks().add(new RawChunk(s)))
				   .collect(Collectors.toList());
	}
	
	private List<List<Chunks>> toChunks2(final List<List<String>> list) {
		return list.stream()
				   .map(l -> toChunks(l))
				   .collect(Collectors.toList());
	}

	
	private static final int EOF = -1;
	private final LineReader reader;
}
