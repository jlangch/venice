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
package com.github.jlangch.venice.impl.util.excel;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
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

    public int getIndex() {
        return sheet.getWorkbook().getSheetIndex(sheet);
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

    public String getCellType(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return "notfound";
        }
        else {
            return getCellType(cell.getCellType());
        }
    }

    public String getCellFormulaResultType(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return "notfound";
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getCellType(cell.getCachedFormulaResultType());
        }
        else {
            return getCellType(cell.getCellType());
        }
    }

    public String getString(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return null;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getString(evaluator.evaluateInCell(cell));
        }
        else {
            return getString(cell);
        }
    }

    public Boolean getBoolean(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return NULL_BOOLEAN;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getBoolean(evaluator.evaluateInCell(cell));
        }
        else {
            return getBoolean(cell);
        }
    }

    public Long getInteger(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return null;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getInteger(evaluator.evaluateInCell(cell));
        }
        else {
            return getInteger(cell);
        }
    }

    public Double getFloat(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return null;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getFloat(evaluator.evaluateInCell(cell));
        }
        else {
            return getFloat(cell);
        }
    }

    public LocalDateTime getDate(final int row, final int col) {
        final Cell cell = getCell(row, col);
        if (cell == null) {
            return null;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return getDate(evaluator.evaluateInCell(cell));
        }
        else {
            return getDate(cell);
        }
    }

    public String getFormula(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getFormula(cell);
    }

    public void setString(
        final int row, final int col, final String value, final String styleName
    ) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setString(final int row, final int col, final String value) {
        setCellValue(getCellCreate(row, col), value, "string");
    }

    public void setBoolean(final int row, final int col, final Boolean value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setBoolean(final int row, final int col, final Boolean value) {
        setCellValue(getCellCreate(row, col), value, "boolean");
    }

    public void setInteger(final int row, final int col, final Integer value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setInteger(final int row, final int col, final Integer value ) {
        setCellValue(getCellCreate(row, col), value, "integer");
    }

    public void setInteger(final int row, final int col, final Long value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setInteger(final int row, final int col, final Long value ) {
        setCellValue(getCellCreate(row, col), value, "integer");
    }

    public void setFloat(final int row, final int col, final Float value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setFloat(final int row, final int col, final Float value) {
        setCellValue(getCellCreate(row, col), value, "float");
    }

    public void setFloat(final int row, final int col, final Double value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setFloat(final int row, final int col, final Double value) {
        setCellValue(getCellCreate(row, col), value, "float");
    }

    public void setDate(final int row, final int col, final Date value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final LocalDate value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final LocalDateTime value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final ZonedDateTime value, final String styleName) {
        setCellValue(getCellCreate(row, col), value, styleName);
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
        sheet.setColumnWidth(col, (int)(width * COL_WIDTH_MAGIC_FACTOR));
    }

    public void rowHeightInPoints(final int row, final int height) {
        // Set the row's height or set to ff (-1) for undefined/default-height.
        // Set the height in "twips" or 1/20th of a point.
        getRowCreate(row).setHeight((short)(height * 20));
    }

    public void setValue(final int row, final int col, final Object value) {
        setValue(row, col, value, null);
    }

    public void setValue(final int row, final int col, final Object value, final String styleName) {
        if (value == null) {
            setCellValue(getCellCreate(row, col), null, null);
        }
        else {
            if (value instanceof String)             setString(row, col,  (String)value,                     coalesce(styleName, "string"));
            else if (value instanceof Boolean)       setBoolean(row, col, (Boolean)value,                    coalesce(styleName, "boolean"));
            else if (value instanceof Integer)       setInteger(row, col, (Integer)value,                    coalesce(styleName, "integer"));
            else if (value instanceof Long)          setInteger(row, col, (Long)value,                       coalesce(styleName, "integer"));
            else if (value instanceof Float)         setFloat(row, col,   (Float)value,                      coalesce(styleName, "float"));
            else if (value instanceof Double)        setFloat(row, col,   (Double)value,                     coalesce(styleName, "float"));
            else if (value instanceof BigDecimal)    setFloat(row, col,   ((BigDecimal)value).doubleValue(), coalesce(styleName, "float"));
            else if (value instanceof BigInteger)    setInteger(row, col, ((BigInteger)value).longValue(),   coalesce(styleName, "integer"));
            else if (value instanceof LocalDate)     setDate(row, col,    (LocalDate)value,                  coalesce(styleName, "date"));
            else if (value instanceof LocalDateTime) setDate(row, col,    (LocalDateTime)value,              coalesce(styleName, "datetime"));
            else if (value instanceof ZonedDateTime) setDate(row, col,    (ZonedDateTime)value,              coalesce(styleName, "datetime"));
            else if (value instanceof Date)          setDate(row, col,    (Date)value,                       coalesce(styleName, "datetime"));
            else throw new IllegalArgumentException("Invalid value type " + value.getClass().getSimpleName());
        }
    }

    public void setBgColor(final int row, final int col, final Color bgColor) {
        setBgColor(getCell(row, col), bgColor);
    }

    public void setBgColorIndex(final int row, final int col, final short bgColor) {
        setBgColorIndex(getCell(row, col), bgColor);
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


    private void setCellValue(Cell cell, Object value, String styleName) {
        final CellStyle style = cellStyles.getCellStyle(styleName);
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

    private void setBgColor(final Cell cell, final Color bgColor) {
        final Workbook workbook = sheet.getWorkbook();

        final CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());

        if (workbook instanceof XSSFWorkbook) {
            ((XSSFCellStyle)style).setFillForegroundColor(
                    new XSSFColor(bgColor, null));

        }
        else if (workbook instanceof HSSFWorkbook) {
            final HSSFColor hssfColor = ColorUtil.bestHSSFColor((HSSFWorkbook)workbook, bgColor);
            style.setFillForegroundColor(hssfColor.getIndex());
        }

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cell.setCellStyle(style);
    }


    private void setBgColorIndex(final Cell cell, final short bgColor) {
        final Workbook workbook = sheet.getWorkbook();

        final CellStyle style = workbook.createCellStyle();;
        style.cloneStyleFrom(cell.getCellStyle());

        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cell.setCellStyle(style);
    }

    private String getString(final Cell cell) {
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
                "The Excel cell [%d,%d] does not contain a string value",
                cell.getRowIndex(),
                cell.getColumnIndex()));
        }
    }

    private Boolean getBoolean(final Cell cell) {
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
                "The Excel cell [%d,%d] does not contain a boolean value",
                cell.getRowIndex(),
                cell.getColumnIndex()));
        }
    }

    private Long getInteger(final Cell cell) {
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
                        "The Excel cell [%d,%d] formula does not evaluate to an integer value",
                        cell.getRowIndex(),
                        cell.getColumnIndex()));
            }
        }
        else {
            throw new ExcelException(String.format(
                "The Excel cell [%d,%d] does not contain an integer value",
                cell.getRowIndex(),
                cell.getColumnIndex()));
        }
    }

    private Double getFloat(final Cell cell) {
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
                    "The Excel cell [%d,%d] does not contain a float value. "
                        + "It actually holds a %s.",
                    cell.getRowIndex(),
                    cell.getColumnIndex(),
                    cell.getCellType().name()));
        }
    }

    private LocalDateTime getDate(final Cell cell) {
        final Date date = cell == null ? null : cell.getDateCellValue();
        return date == null ? null : TimeUtil.convertDateToLocalDateTime(date);
    }

    private String getFormula(final Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.BLANK) {
            return null;
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            return cell.getCellFormula();
        }
        else {
            throw new ExcelException(String.format(
                    "The Excel cell [%d,%d] does not contain a formula. "
                        + "It actually holds a %s.",
                    cell.getRowIndex(),
                    cell.getColumnIndex(),
                    cell.getCellType().name()));
        }
    }

    public String getCellType(final CellType type) {
        if (type == CellType.BLANK) {
            return "blank";
        }
        else if (type == CellType.STRING) {
            return "string";
        }
        else if (type == CellType.BOOLEAN) {
            return "boolean";
        }
        else if (type == CellType.NUMERIC) {
            return "numeric";
        }
        else if (type == CellType.FORMULA) {
            return "formula";
        }
        else if (type == CellType.ERROR) {
            return "error";
        }
        else {
            return "unknown";
        }
    }

    // The Excel's magic conversion factor
    public static final float COL_WIDTH_MAGIC_FACTOR = 46.4f; // to points (1/72 inch)

    private static final Boolean NULL_BOOLEAN = null;

    private final Sheet sheet;
    private final ExcelCellStyles cellStyles;
    private final FormulaEvaluator evaluator;
}
