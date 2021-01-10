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
package com.github.jlangch.venice.impl.util.excel;


public class ExcelSumFormulaBuilder<T> {

	public ExcelSumFormulaBuilder(
			final ExcelSheetBuilder<T> parentBuilder,
			final ExcelSheet sheet,
			final int row,
			final int col
	) {
		this.parentBuilder = parentBuilder;
		this.sheet = sheet;
		this.row = row;
		this.col = col;
	}

	public ExcelSumFormulaBuilder<T> cellFrom(final int row, final int col) {
		this.rowFrom = row;
		this.colFrom = col;
		return this;
	}

	public ExcelSumFormulaBuilder<T> cellTo(final int row, final int col) {
		this.rowTo = row;
		this.colTo = col;
		return this;
	}

	public ExcelSumFormulaBuilder<T> style(final String style) {
		this.style = style;
		return this;
	}
	
	public ExcelSheetBuilder<T> end() {
		final String formula = String.format(
								"SUM(%s:%s)", 
								sheet.getCellAddress(rowFrom, colFrom), 
								sheet.getCellAddress(rowTo, colTo));

		sheet.setFormula(row, col, formula, style);

		return parentBuilder;
	}


	private final ExcelSheetBuilder<T> parentBuilder;
	private final ExcelSheet sheet;
	private final int row;
	private final int col;
	private int rowFrom;
	private int colFrom;
	private int rowTo;
	private int colTo;
	private String style;
}
