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

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;


public class ExcelCellStyleBuilder {

	public ExcelCellStyleBuilder(
			final ExcelBuilder excelBuilder,
			final Excel managedExcel,
			final String name
	) {
		this.parentBuilder = excelBuilder;
		this.managedExcel = managedExcel;
		this.name = name;
	}

	
	public ExcelCellStyleBuilder format(final String format) {
		this.format = format;
		return this;
	}

	public ExcelCellStyleBuilder font(final String fontRefName) {
		this.fontRefName = fontRefName;
		return this;
	}
	
	public ExcelCellStyleBuilder bgColor(final IndexedColors bgColor) {
		this.bgColorIndex = bgColor.getIndex();
		return this;
	}

	public ExcelCellStyleBuilder bgColor(final short bgColorIndex) {
		this.bgColorIndex = bgColorIndex;
		return this;
	}
	
	public ExcelCellStyleBuilder wrapText(final boolean wrapText) {
		this.wrapText = wrapText;
		return this;
	}

	public ExcelCellStyleBuilder hAlign(final HorizontalAlignment hAlign) {
		this.hAlign = hAlign;
		return this;
	}
	
	public ExcelCellStyleBuilder hAlignLeft() {
		this.hAlign = HorizontalAlignment.LEFT;
		return this;
	}
	
	public ExcelCellStyleBuilder hAlignCenter() {
		this.hAlign = HorizontalAlignment.CENTER;
		return this;
	}
	
	public ExcelCellStyleBuilder hAlignRight() {
		this.hAlign = HorizontalAlignment.RIGHT;
		return this;
	}

	public ExcelCellStyleBuilder vAlign(final VerticalAlignment vAlign) {
		this.vAlign = vAlign;
		return this;
	}
	
	public ExcelCellStyleBuilder vAlignTop() {
		this.vAlign = VerticalAlignment.TOP;
		return this;
	}
	
	public ExcelCellStyleBuilder vAlignCenter() {
		this.vAlign = VerticalAlignment.CENTER;
		return this;
	}
	
	public ExcelCellStyleBuilder vAlignBottom() {
		this.vAlign = VerticalAlignment.BOTTOM;
		return this;
	}

	public ExcelBuilder end() {
		managedExcel.registerCellFormat(
				name, format, fontRefName, bgColorIndex, wrapText, hAlign, vAlign);
		return parentBuilder;
	}


	private final ExcelBuilder parentBuilder;
	private final Excel managedExcel;
	private final String name;
	private String format;
	private String fontRefName;
	private Short bgColorIndex;
	private Boolean wrapText;
	private HorizontalAlignment hAlign;
	private VerticalAlignment vAlign;
}
