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
package com.github.jlangch.venice.util.excel;

import com.github.jlangch.venice.impl.util.excel.ExcelSheet;

public class ExcelSumFormulaBuilder<T> {

	public ExcelSumFormulaBuilder(
			final ExcelSheetBuilder<T> parentBuilder,
			final ExcelSheet sheet,
			final int row1,
			final int col1
	) {
		this.parentBuilder = parentBuilder;
		this.sheet = sheet;
		this.row0 = row1-1;
		this.col0 = col1-1;
	}

	public ExcelSumFormulaBuilder<T> cellFrom(final int row1, final int col1) {
		this.rowFrom0 = row1-1;
		this.colFrom0 = col1-1;
		return this;
	}

	public ExcelSumFormulaBuilder<T> cellTo(final int row1, final int col1) {
		this.rowTo0 = row0-1;
		this.colTo0 = col0-1;
		return this;
	}

	public ExcelSumFormulaBuilder<T> style(final String style) {
		this.style = style;
		return this;
	}
	
	public ExcelSheetBuilder<T> end() {
		final String formula = String.format(
								"SUM(%s:%s)", 
								sheet.getCellAddress(rowFrom0, colFrom0), 
								sheet.getCellAddress(rowTo0, colTo0));

		sheet.setFormula(row0, col0, formula, style);

		return parentBuilder;
	}


	private final ExcelSheetBuilder<T> parentBuilder;
	private final ExcelSheet sheet;
	private final int row0;
	private final int col0;
	private int rowFrom0;
	private int colFrom0;
	private int rowTo0;
	private int colTo0;
	private String style;
}
