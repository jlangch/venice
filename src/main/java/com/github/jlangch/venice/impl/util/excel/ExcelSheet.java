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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.jlangch.venice.ExcelException;
import com.github.jlangch.venice.impl.util.TimeUtil;


/**
 * An Excel sheet 
 * 
 * @author juerg
 */
public class ExcelSheet {

	public ExcelSheet(
		final Sheet sheet,
		final ExcelCellStyles cellFormats,
		final FormulaEvaluator evaluator
	) {
		this.sheet = sheet;
		this.cellStyles = cellFormats;
		this.evaluator = evaluator;
	}
	
	public String getName() {
		return sheet.getSheetName();
	}

	public int getFirstRowNum() {
		return sheet.getFirstRowNum();
	}

	public int getLastRowNum() {
		return sheet.getLastRowNum();
	}

	public int getFirstCellNum(final int row) {
		final Row r = sheet.getRow(row);
		return r == null ? -1 : r.getFirstCellNum();
	}

	public int getLastCellNum(final int row) {
		final Row r = sheet.getRow(row);
		return r == null ? -1 : r.getLastCellNum();
	}

	public boolean isCellEmpty(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		return cell == null || cell.getCellType() == CellType.BLANK;
	}

	public String getCellAddress(final int row, final int col) {
		return new CellAddress(row, col).formatAsString();
	}
	
	public String getString(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.BLANK) { 
			return null;
		}
		else if (cell.getCellType() == CellType.STRING) { 
			return cell.getStringCellValue();
		}
		else if (cell.getCellType() == CellType.BOOLEAN) { 
			return Boolean.toString(cell.getBooleanCellValue());
		}
		else if (cell.getCellType() == CellType.NUMERIC) { 
			return Double.toString(cell.getNumericCellValue());
		}
		else {
			throw new ExcelException(String.format(
				"The Excel cell [%d,%d] does not contain a string value", row, col));
		}
	}

	public Boolean getBoolean(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		if (cell == null) {
			return NULL_BOOLEAN;  // fooling 'Find Bugs' :-)
		}
		if (cell.getCellType() == CellType.BLANK) { 
			return NULL_BOOLEAN;  // fooling 'Find Bugs' :-)
		}
		else if (cell.getCellType() == CellType.BOOLEAN) { 
			return cell.getBooleanCellValue();
		}
		else {
			throw new ExcelException(String.format(
				"The Excel cell [%d,%d] does not contain a boolean value", row, col));
		}
	}

	public Long getInteger(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.BLANK) { 
			return null;
		}
		else if (cell.getCellType() == CellType.NUMERIC) { 
			return (long)(cell.getNumericCellValue() + 0.5);
		}
		else if (cell.getCellType() == CellType.FORMULA) { 
			final CellValue cellValue = evaluator.evaluate(cell);
			if (cellValue.getCellType() == CellType.NUMERIC) { 
		        return (long)(cellValue.getNumberValue() + 0.5);
			}
			else {
				throw new ExcelException(String.format(
						"The Excel cell [%d,%d] formula does not evaluate to an integer value", row, col));
			}
		}
		else {
			throw new ExcelException(String.format(
				"The Excel cell [%d,%d] does not contain an integer value", row, col));
		}
	}

	public Double getFloat(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.BLANK) { 
			return null;
		}
		else if (cell.getCellType() == CellType.NUMERIC) { 
			return cell.getNumericCellValue();
		}
		else {
			throw new ExcelException(String.format(
				"The Excel cell [%d,%d] does not contain a float value", row, col));
		}
	}

	public LocalDateTime getDate(final int row, final int col) {
		final Cell cell = getCell(row, col);		
		final Date date = cell == null ? null : cell.getDateCellValue();
		return date == null ? null : TimeUtil.convertDateToLocalDateTime(date);
	}

	public void setString(
		final int row, final int col, final String value, final String format
	) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setString(final int row, final int col, final String value) {
		setCellValue(getCellCreate(row, col), value, "string");
	}

	public void setBoolean(final int row, final int col, final Boolean value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}
	
	public void setBoolean(final int row, final int col, final Boolean value) {
		setCellValue(getCellCreate(row, col), value, "boolean");
	}

	public void setInteger(final int row, final int col, final Integer value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}
	
	public void setInteger(final int row, final int col, final Integer value ) {
		setCellValue(getCellCreate(row, col), value, "integer");
	}

	public void setInteger(final int row, final int col, final Long value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}
	
	public void setInteger(final int row, final int col, final Long value ) {
		setCellValue(getCellCreate(row, col), value, "integer");
	}

	public void setFloat(final int row, final int col, final Float value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setFloat(final int row, final int col, final Float value) {
		setCellValue(getCellCreate(row, col), value, "float");
	}

	public void setFloat(final int row, final int col, final Double value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setFloat(final int row, final int col, final Double value) {
		setCellValue(getCellCreate(row, col), value, "float");
	}

	public void setDate(final int row, final int col, final Date value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setDate(final int row, final int col, final LocalDate value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setDate(final int row, final int col, final LocalDateTime value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setDate(final int row, final int col, final ZonedDateTime value, final String format) {
		setCellValue(getCellCreate(row, col), value, format);
	}

	public void setDate(final int row, final int col, final Date value) {
		setCellValue(getCellCreate(row, col), value, "date");
	}

	public void setDate(final int row, final int col, final LocalDate value) {
		setCellValue(getCellCreate(row, col), value, "date");
	}

	public void setDate(final int row, final int col, final LocalDateTime value) {
		setCellValue(getCellCreate(row, col), value, "date");
	}

	public void setDate(final int row, final int col, final ZonedDateTime value) {
		setCellValue(getCellCreate(row, col), value, "date");
	}

	public void setColumnWidthInPoints(final int col, final int width) {
		sheet.setColumnWidth(col, (int)((float)width * COL_WIDTH_MAGIC_FACTOR));
	}
	
	public void setValue(final int row, final int col, final Object value) {
		setValue(row, col, value, null);
	}
	
	public void setValue(final int row, final int col, final Object value, final String format) {
		if (value == null) {
			setCellValue(getCellCreate(row, col), null, null);
		} 
		else {
			if (value instanceof String) setString(row, col, (String)value, coalesce(format, "string"));
			else if (value instanceof Boolean) setBoolean(row, col, (Boolean)value, coalesce(format, "boolean"));
			else if (value instanceof Integer) setInteger(row, col, (Integer)value, coalesce(format, "integer"));
			else if (value instanceof Long) setInteger(row, col, (Long)value, coalesce(format, "integer"));
			else if (value instanceof Float) setFloat(row, col, (Float)value, coalesce(format, "float"));
			else if (value instanceof Double) setFloat(row, col, (Double)value, coalesce(format, "float"));
			else if (value instanceof BigDecimal) setFloat(row, col, ((BigDecimal)value).doubleValue(), coalesce(format, "float"));
			else if (value instanceof BigInteger) setInteger(row, col, ((BigInteger)value).longValue(), coalesce(format, "integer"));
			else if (value instanceof LocalDate) setDate(row, col, (LocalDate)value, coalesce(format, "date"));
			else if (value instanceof LocalDateTime) setDate(row, col, (LocalDateTime)value, coalesce(format, "datetime"));
			else if (value instanceof ZonedDateTime) setDate(row, col, (ZonedDateTime)value, coalesce(format, "datetime"));
			else if (value instanceof Date) setDate(row, col, (Date)value, coalesce(format, "datetime"));
			else throw new IllegalArgumentException("Invalid value type " + value.getClass().getSimpleName());
		}
	}
	
	public void setFormula(final int row, final int col, final String formula) {
		setFormula(row, col, formula, null);
	}
	
	public void setFormula(final int row, final int col, final String formula, final String styleName) {
		final Cell cell = getCellCreate(row, col);
		cell.setCellFormula(formula);
		
		final CellStyle style = cellStyles.getCellStyle(styleName);
		if (style != null) {
			cell.setCellStyle(style);
		}
	}
	
	public void addMergedRegion(final int rowFrom, final int rowTo, final int colFrom, final int colTo) {
		sheet.addMergedRegion(new CellRangeAddress(rowFrom, rowTo, colFrom, colTo));
	}

	public void addMergedRegion(final String ref) {
		sheet.addMergedRegion(CellRangeAddress.valueOf(ref));
	}
	
    public void setDisplayZeros(final boolean value) {
		sheet.setDisplayZeros(value);
    }
	
	public void autoSizeColumn(final int col) {
		sheet.autoSizeColumn(col);
	}

	public void autoSizeColumns() {
		final int firstRow = sheet.getFirstRowNum();
		if (firstRow >= 0) {
			final Row row = sheet.getRow(firstRow);
			if (row != null) {
				for(int col = 0; col<row.getLastCellNum(); col++) {   
					sheet.autoSizeColumn(col);
				}
			}
		}
	}
	
	public void evaluateAllFormulas() {
		evaluator.evaluateAll();
	}

	
	private void setCellValue(Cell cell, Object value, String format) {
		final CellStyle style = cellStyles.getCellStyle(format);
		if (style != null) {
			cell.setCellStyle(style);
		}
		
		if (value == null) {
			cell.setBlank();
		}
		else if (value instanceof String) {
			if (sheet.getWorkbook() instanceof XSSFWorkbook) {
				cell.setCellValue(new XSSFRichTextString(value.toString()));
			}
			else {
				cell.setCellValue(value.toString());
			}
		}
		else if (value instanceof Boolean) {
		    cell.setCellValue(((Boolean)value).booleanValue());
		}
		else if (value instanceof Integer) {
			cell.setCellValue(((Integer)value).longValue());
		}
		else if (value instanceof Long) {
			cell.setCellValue(((Long)value).longValue());
		}
		else if (value instanceof Float) {
			cell.setCellValue(((Float)value).doubleValue());
		}
		else if (value instanceof Double) {
			cell.setCellValue(((Double)value).doubleValue());
		}
		else if (value instanceof BigDecimal) {
			cell.setCellValue(((BigDecimal)value).doubleValue());
		}
		else if (value instanceof Date) {
			cell.setCellValue((Date)value);
		}
		else if (value instanceof LocalDate) {
			cell.setCellValue(TimeUtil.convertLocalDateToDate((LocalDate)value));
		}
		else if (value instanceof LocalDateTime) {
			cell.setCellValue(TimeUtil.convertLocalDateTimeToDate((LocalDateTime)value));
		}
		else if (value instanceof ZonedDateTime) {
			cell.setCellValue(TimeUtil.convertZonedDateTimeToDate((ZonedDateTime)value));
		}
	}

	private Cell getCell(final int row, final int col) {
		final Row r = sheet.getRow(row);
		return r == null ? null : r.getCell(col, MissingCellPolicy.RETURN_BLANK_AS_NULL);
	}

	private Cell getCellCreate(final int row, final int col) {
		return getRowCreate(row).createCell(col);
	}

	private Row getRowCreate(final int row) {
		final Row r = sheet.getRow(row);
		return (r != null) ? r :sheet.createRow(row);
	}
	
	private String coalesce(final String s1, final String s2) {
		return s1 != null ? s1 : s2;
	}
	
	
	// The Excel's magic conversion factor
	public static final float COL_WIDTH_MAGIC_FACTOR = 46.4f; // to points (1/72 inch)
	
	private static final Boolean NULL_BOOLEAN = (Boolean)null;

	private final Sheet sheet;
	private final ExcelCellStyles cellStyles;
	private final FormulaEvaluator evaluator;
}
