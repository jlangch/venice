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


public class TableBlock implements Block {

	public TableBlock() {
	}

	public TableBlock(
		final int cols,
		final List<List<String>> bodyRows
	) {
		addFormat(cols, new ArrayList<>());
		addHeaderRow(cols, new ArrayList<>());
		addBodyRows(cols, bodyRows);
	}

	public TableBlock(
		final int cols,
		final List<Alignment> format,
		final List<List<String>> bodyRows
	) {
		addFormat(cols, format);
		addHeaderRow(cols, new ArrayList<>());
		addBodyRows(cols, bodyRows);
	}


	public TableBlock(
		final int cols,
		final List<Alignment> format,
		final List<String> headerRow,
		final List<List<String>> bodyRows
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

	public Alignment getFormat(final int col) {
		return col >= format.size() ? Alignment.LEFT : format.get(col);
	}

	public String getHeaderCell(final int col) {
		return col >= headerRow.size() ? "" : headerRow.get(col);
	}

	public String getBodyCell(final int row, final int col) {
		if (row >= bodyRows.size()) {
			return "";
		}
		else {
			final List<String> bodyRow = bodyRows.get(row);
			return col >= bodyRow.size() ? "" : bodyRow.get(col);
		}
	}

	public boolean isEmpty() {
		return headerRow.isEmpty() && bodyRows.isEmpty();
	}
	
	
	private void addFormat(final int cols, final List<Alignment> formats) {
		for(int ii=0; ii<cols; ii++) {
			format.set(
				ii, 
				formats != null && ii<formats.size() ? formats.get(ii) : Alignment.LEFT);
		}
	}
	
	private void addHeaderRow(final int cols, final List<String> row) {
		if (row != null && !row.isEmpty()) {
			for(int ii=0; ii<cols; ii++) {
				headerRow.set(
					ii, 
					row != null && ii<row.size() ? row.get(ii) : "");
			}
		}
	}
	
	private void addBodyRows(final int cols, final List<List<String>> rows) {
		if (rows != null && !rows.isEmpty()) {
			for(List<String> row : rows) {
				final List<String> bodyRow = new ArrayList<>();
				bodyRows.add(bodyRow);
				
				for(int ii=0; ii<cols; ii++) {
					bodyRow.set(
						ii, 
						row != null && ii<row.size() ? row.get(ii) : "");
				}
			}
		}
	}
	
	
	public static enum Alignment { LEFT, CENTER, RIGHT };
	
	private List<Alignment> format = new ArrayList<>();
	private List<String> headerRow = new ArrayList<>();
	private List<List<String>> bodyRows = new ArrayList<>();
}
