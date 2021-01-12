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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;



public class ExcelSheetBuilder<T> {

	public ExcelSheetBuilder(
			final ExcelBuilder excelBuilder,
			final ExcelSheet sheet
	) {
		this.parentBuilder = excelBuilder;
		this.sheet = sheet;
	}
	
	public ExcelSheetBuilder<T> noHeader() {
		this.noHeader = true;
		return this;
	}

	public ExcelSheetBuilder<T> defaultHeaderStyle(final String style) {
		this.defaultHeaderStyle = style;
		return this;
	}
	
	public ExcelSheetBuilder<T> defaultBodyStyle(final String style) {
		this.defaultBodyStyle = style;
		return this;
	}
	
	public ExcelSheetBuilder<T> defaultFooterStyle(final String style) {
		this.defaultFooterStyle = style;
		return this;
	}

	public ExcelColumnBuilder<T> withColumn(final String colHeaderName) {		
		return new ExcelColumnBuilder<T>(this, columnDefs, colHeaderName);
	}

	public ExcelColumnBuilder<T> withColumn(
			final String colHeaderName,
			final Function<? super T, ?> colMapper
	) {		
		final ExcelColumnBuilder<T> builder = new ExcelColumnBuilder<T>(this, columnDefs, colHeaderName);
		builder.colMapper(colMapper);
		return builder;
	}

	public ExcelColumnBuilder<T> withColumn(
			final String colHeaderName,
			final String fieldName
	) {	
		return withColumn(colHeaderName, e -> ((EntityRecord)e).get(fieldName));
	}

	public ExcelSheetBuilder<T> renderDataItems(final List<T> items) {
		return renderData(items);
	}

	public ExcelSheetBuilder<T> renderData(final List<T> items) {	
		int col = 0;
		for(ExcelColumnDef<T> colDef : columnDefs) {
			if (colDef.width != null) sheet.setColumnWidthInPoints(col, colDef.width);
			col++;
		}
		
		renderHeader();
		
		final int bodyRowStart = currRow;
		if (items != null) {
			items.stream().forEach(v -> renderItem(v, currRow++));
		}
		
		renderFooter(bodyRowStart, bodyRowStart + items.size()-1, items == null || items.isEmpty());
		
		return this;
	}
	
	public ExcelSheetBuilder<T> renderData(final T item) {
		renderHeader();
		renderItem(item, currRow++);
		return this;
	}
	
	public ExcelSheetBuilder<T> value(final int row, final int col, final Object value) {
		sheet.setValue(row, col, value);
		return this;
	}
	
	public ExcelSheetBuilder<T> value(final int row, final int col, final Object value, final String stylename) {
		sheet.setValue(row, col, value, stylename);
		return this;
	}

	public ExcelSheetBuilder<T> formula(final int row, final int col, final String formula) {
		sheet.setFormula(row, col, formula);
		return this;
	}

	public ExcelSheetBuilder<T> formula(final int row, final int col, final String formula, final String stylename) {
		sheet.setFormula(row, col, formula, stylename);
		return this;
	}
	
	public ExcelSumFormulaBuilder<T> withSum(final int row, final int col) {	
		return new ExcelSumFormulaBuilder<T>(this, sheet, row, col);
	}

	public ExcelSheetBuilder<T> skipRows(final int count) {	
		renderHeader();
		currRow += Math.min(0, count);
		return this;
	}

	public ExcelSheetBuilder<T> autoSizeColumns() {
		sheet.autoSizeColumns();
		return this;
	}

	public ExcelSheetBuilder<T> autoSizeColumn(final int col) {
		sheet.autoSizeColumn(col);
		return this;
	}
	
	public ExcelSheetBuilder<T> addMergedRegion(final int rowFrom, final int rowTo, final int colFrom, final int colTo) {
		sheet.addMergedRegion(rowFrom, rowTo, colFrom, colTo);
		return this;
	}

	public ExcelSheetBuilder<T> evaluateAllFormulas() {
		sheet.evaluateAllFormulas();
		return this;
	}

    public ExcelSheetBuilder<T> displayZeros(final boolean value) {
		sheet.setDisplayZeros(value);
		return this;
    }

	public ExcelSheetBuilder<T> setColumnWidthInPoints(final int col, final int width) {
		sheet.setColumnWidthInPoints(col, width);
		return this;
	}

	public ExcelBuilder end() {
		return parentBuilder;
	}
	
	public String sumFormula(final int rowFrom, final int rowTo, final int colFrom, final int colTo) {
		return String.format(
				"SUM(%s:%s)", 
				sheet.getCellAddress(rowFrom, colFrom), 
				sheet.getCellAddress(rowTo, colTo));
	}
	
	public String cellAddress(final int row, final int col) {
		return sheet.getCellAddress(row, col);
	}
	
	
	
	private String getColumnHeaderStyle(final int col) {
		final String style = col < 0 || (col > columnDefs.size()-1) ? null : columnDefs.get(col).headerStyle;		
		return style == null ? defaultHeaderStyle : style;
	}
	
	private String getColumnBodyStyle(final int col) {
		final String style = col < 0 || (col > columnDefs.size()-1) ? null : columnDefs.get(col).bodyStyle;		
		return style == null ? defaultBodyStyle : style;
	}
	
	private String getColumnFooterStyle(final int col) {
		final String style = col < 0 || (col > columnDefs.size()-1) ? null : columnDefs.get(col).footerStyle;		
		return style == null ? defaultFooterStyle : style;
	}
	
	private List<String> getHeaderStrings() {
		return columnDefs.stream().map(c -> c.header).collect(Collectors.toList());
	}

	private void setHeaderValues(final int row, final List<?> values) {
		int col = 0;
		for(Object v : values) {
			if (v != null) {
				sheet.setValue(row, col, v, getColumnHeaderStyle(col));
			}
			col++;
		}
	}
	
	private boolean hasFooter() {
		return columnDefs.stream().anyMatch(c -> c.footerType != ExcelColumnDef.FooterType.NONE);	
	}
	
	private void renderHeader() {
		if (!headerRendered) {
			if (!noHeader) {
				setHeaderValues(currRow++, getHeaderStrings());
			}
			headerRendered = true;
		}
	}
	
	private void renderFooter(final int rowFrom, final int rowTo, boolean emptyBody) {
		if (hasFooter()) {
			int col = 0;
			for(ExcelColumnDef<T> colDef : columnDefs) {
				switch (colDef.footerType) {
					case NONE:
						sheet.setValue(currRow, col, null, null);
						break;
					case TEXT:
						sheet.setValue(currRow, col, (String)colDef.footerValue, getColumnFooterStyle(col));
						break;
					case NUMBER:
						sheet.setValue(currRow, col, (Number)colDef.footerValue, getColumnFooterStyle(col));
						break;
					case FORMULA:
						sheet.setValue(currRow, col, null, null);  // TODO
						break;
					case SUM:
						if (emptyBody) {
							sheet.setValue(currRow, col, null, null);
						}
						else {
							final String formula = String.format(
									"SUM(%s:%s)", 
									new CellAddress(rowFrom, col).formatAsString(),
									new CellAddress(rowTo, col).formatAsString());

							sheet.setFormula(currRow, col, formula, getColumnFooterStyle(col));
						}
						break;
					case MIN:
						if (emptyBody) {
							sheet.setValue(currRow, col, null, getColumnFooterStyle(col));
						}
						else {
							final String formula = String.format(
									"MIN(%s:%s)", 
									new CellAddress(rowFrom, col).formatAsString(),
									new CellAddress(rowTo, col).formatAsString());

							sheet.setFormula(currRow, col, formula, getColumnFooterStyle(col));
						}
						break;
					case MAX:
						if (emptyBody) {
							sheet.setValue(currRow, col, null, getColumnFooterStyle(col));
						}
						else {
							final String formula = String.format(
									"MAX(%s:%s)", 
									new CellAddress(rowFrom, col).formatAsString(),
									new CellAddress(rowTo, col).formatAsString());

							sheet.setFormula(currRow, col, formula, getColumnFooterStyle(col));
						}
						break;
					case AVERAGE:
						if (emptyBody) {
							sheet.setValue(currRow, col, null, getColumnFooterStyle(col));
						}
						else {
							final String formula = String.format(
									"AVERAGE(%s:%s)", 
									new CellAddress(rowFrom, col).formatAsString(),
									new CellAddress(rowTo, col).formatAsString());

							sheet.setFormula(currRow, col, formula, getColumnFooterStyle(col));
						}
						break;
				}
				col++;
			}
		}
		currRow++;
	}

	private void renderItem(final T item, final int row) {
		if (item != null) {
			int col = 0;
			for(ExcelColumnDef<T> colDef : columnDefs) {
				sheet.setValue(row, col, (Object)colDef.colMapper.apply(item), getColumnBodyStyle(col));
				col++;
			}
		}
	}
		
		
	
	public static final int DEFAULT_FONT_SIZE = XSSFFont.DEFAULT_FONT_SIZE;

	private final ExcelBuilder parentBuilder;
	private final ExcelSheet sheet;
	private final List<ExcelColumnDef<T>> columnDefs = new ArrayList<>();
	private boolean noHeader = false;
	private boolean headerRendered = false;
	private int currRow = 0;
	private String defaultHeaderStyle;
	private String defaultBodyStyle;
	private String defaultFooterStyle;
}
