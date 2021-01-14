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

import org.apache.poi.ss.usermodel.IndexedColors;

import com.github.jlangch.venice.impl.util.excel.Excel;


public class ExcelFontBuilder {

	public ExcelFontBuilder(
			final ExcelBuilder excelBuilder,
			final Excel managedExcel,
			final String id
	) {
		this.parentBuilder = excelBuilder;
		this.managedExcel = managedExcel;
		this.id = id;
	}

	

	public ExcelFontBuilder name(final String fontName) {
		this.fontName = fontName;
		return this;
	}

	public ExcelFontBuilder heightInPoints(final int heightInPoints) {
		this.heightInPoints = heightInPoints;
		return this;
	}

	public ExcelFontBuilder bold() {
		this.bold = true;
		return this;
	}

	public ExcelFontBuilder italic() {
		this.italic = true;
		return this;
	}

	public ExcelFontBuilder color(final IndexedColors color) {
		this.colorIndex = color.index;
		return this;
	}

	public ExcelFontBuilder color(final short colorIndex) {
		this.colorIndex = colorIndex;
		return this;
	}

	public ExcelBuilder end() {
		managedExcel.registerFont(id, fontName, heightInPoints, bold, italic, colorIndex);
		return parentBuilder;
	}


	private final ExcelBuilder parentBuilder;
	private final Excel managedExcel;
	private final String id;
	private String fontName;
	private Integer heightInPoints;
	private boolean bold;
	private boolean italic;
	private Short colorIndex;
}
