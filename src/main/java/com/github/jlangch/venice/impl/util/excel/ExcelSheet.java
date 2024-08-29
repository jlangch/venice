/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Picture;
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
import com.github.jlangch.venice.impl.util.time.TimeUtil;
import com.github.jlangch.venice.util.excel.CellAddr;
import com.github.jlangch.venice.util.excel.CellRangeAddr;
import com.github.jlangch.venice.util.excel.chart.AreaDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarGrouping;
import com.github.jlangch.venice.util.excel.chart.ImageType;
import com.github.jlangch.venice.util.excel.chart.LineDataSeries;
import com.github.jlangch.venice.util.excel.chart.PieDataSeries;
import com.github.jlangch.venice.util.excel.chart.Position;


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

    public void createFreezePane(final int colSplit, final int rowSplit) {
        sheet.createFreezePane(colSplit, rowSplit);
    }

    public boolean isCellEmpty(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    public String getCellAddress_A1_style(final int row, final int col) {
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

    public void deleteRow(final int row) {
        final int lastRowNum = sheet.getLastRowNum();
        if (row >= 0 && row < lastRowNum) {
            sheet.shiftRows(row + 1, lastRowNum, -1);
        }
        if (row == lastRowNum) {
            Row lastRow = sheet.getRow(row);
            if (lastRow != null) {
                sheet.removeRow(lastRow);
            }
        }
    }

    public void clearRow(
            final int row,
            final boolean clearValues,
            final boolean clearStyles
    ) {
        final Row sourceRow = sheet.getRow(row);
        if (sourceRow == null) {
            return;
        }

        for (int ii = 0; ii < sourceRow.getLastCellNum(); ii++) {
            final Cell cell = sourceRow.getCell(ii);
            if (cell != null) {
                if (clearValues) {
                    cell.setBlank();
                }
                else if (clearStyles) {
                    cell.setCellStyle(null);
                }
            }
        }
    }

    public void copyRowToEndOfSheet(
            final int row,
            final boolean copyValues,
            final boolean copyStyles
    ) {
        final Row sourceRow = sheet.getRow(row);
        if (sourceRow == null) {
            return;
        }

        final int lastRowNum = sheet.getLastRowNum();
        final Row newRow = sheet.createRow(lastRowNum + 1);

        // Copy each cell from the source row to the new row
        for (int ii = 0; ii < sourceRow.getLastCellNum(); ii++) {
            final Cell oldCell = sourceRow.getCell(ii);
            final Cell newCell = newRow.createCell(ii);

            if (oldCell != null) {
                // Copy style
                if (copyStyles) {
                    newCell.setCellStyle(oldCell.getCellStyle());
                }

                // Copy value
                if (copyValues) {
                    copyCellValue(oldCell, newCell);
                }
            }
        }
    }

    public void copyRow(
            final int rowFrom,
            final int rowTo,
            final boolean copyValues,
            final boolean copyStyles
    ) {
        final int lastRowNum = sheet.getLastRowNum();

        if (rowTo > lastRowNum) {
            copyRowToEndOfSheet(rowFrom, copyValues, copyStyles);
        }
        else {
            final Row sourceRow = sheet.getRow(rowFrom);
            if (sourceRow == null) {
                 return;
            }
            final Row destRow = sheet.getRow(rowTo);
            if (destRow == null) {
                 return;
            }

            clearRow(rowTo, true, copyStyles);

            // Copy each cell from the source row to the dest row
            for (int ii = 0; ii < sourceRow.getLastCellNum(); ii++) {
                final Cell oldCell = sourceRow.getCell(ii);
                final Cell destCell = destRow.getCell(ii, MissingCellPolicy.CREATE_NULL_AS_BLANK);

                if (oldCell != null) {
                    // Copy style
                    if (copyStyles) {
                        destCell.setCellStyle(oldCell.getCellStyle());
                    }

                    // Copy value
                    if (copyValues) {
                        copyCellValue(oldCell, destCell);
                    }
                }
            }
        }
    }

    public void insertEmptyRow(final int row) {
        final int lastRowNum = sheet.getLastRowNum();

        // Shift rows from the specified row index down by 1 row to make space for the new row
        sheet.shiftRows(row, lastRowNum, 1);

        // Create a new empty row at the specified index
        sheet.createRow(row);
    }

    public void insertEmptyRows(final int row, final int count) {
        for(int ii=0; ii<count; ii++) {
            final int lastRowNum = sheet.getLastRowNum();

            // Shift rows from the specified row index down by 1 row to make space for the new row
            sheet.shiftRows(row, lastRowNum, 1);

            // Create a new empty row at the specified index
            sheet.createRow(row);
        }
    }

    public void copyCellStyle(
            final int cellRowFrom,
            final int cellColFrom,
            final int cellRowTo,
            final int cellColTo
    ) {
        final Cell cellFrom = getCell(cellRowFrom, cellColFrom);

        if (cellFrom != null) {
            final Cell cellTo = getCellOrCreate(cellRowTo, cellColTo);

            final CellStyle style = cellFrom.getCellStyle();
            cellTo.setCellStyle(style);
        }
    }

    public Object getValue(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getValue(cell);
    }

    public String getString(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getString(cell);
    }

    public Boolean getBoolean(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getBoolean(cell);
    }

    public Long getInteger(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getInteger(cell);
    }

    public Double getFloat(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getFloat(cell);
    }

    public LocalDateTime getDate(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getDate(cell);
    }

    public String getFormula(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getFormula(cell);
    }

    public String getErrorCode(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getErrorCode(cell);
    }

    public String getDataFormatString(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return getDataFormatString(cell);
    }

    public boolean isLocked(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return isLocked(cell);
    }

    public boolean isHidden(final int row, final int col) {
        final Cell cell = getCell(row, col);
        return isHidden(cell);
    }

    public boolean isColumnHidden(final int col) {
        return sheet.isColumnHidden(col);
    }

    public void setString(
        final int row, final int col, final String value, final String styleName
    ) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setString(final int row, final int col, final String value) {
        setCellValue(getCellOrCreate(row, col), value, "string");
    }

    public void setBoolean(final int row, final int col, final Boolean value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setBoolean(final int row, final int col, final Boolean value) {
        setCellValue(getCellOrCreate(row, col), value, "boolean");
    }

    public void setInteger(final int row, final int col, final Integer value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setInteger(final int row, final int col, final Integer value ) {
        setCellValue(getCellOrCreate(row, col), value, "integer");
    }

    public void setInteger(final int row, final int col, final Long value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setInteger(final int row, final int col, final Long value ) {
        setCellValue(getCellOrCreate(row, col), value, "integer");
    }

    public void setFloat(final int row, final int col, final Float value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setFloat(final int row, final int col, final Float value) {
        setCellValue(getCellOrCreate(row, col), value, "float");
    }

    public void setFloat(final int row, final int col, final Double value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setFloat(final int row, final int col, final Double value) {
        setCellValue(getCellOrCreate(row, col), value, "float");
    }

    public void setDate(final int row, final int col, final Date value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final LocalDate value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final LocalDateTime value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final ZonedDateTime value, final String styleName) {
        setCellValue(getCellOrCreate(row, col), value, styleName);
    }

    public void setDate(final int row, final int col, final Date value) {
        setCellValue(getCellOrCreate(row, col), value, "date");
    }

    public void setDate(final int row, final int col, final LocalDate value) {
        setCellValue(getCellOrCreate(row, col), value, "date");
    }

    public void setDate(final int row, final int col, final LocalDateTime value) {
        setCellValue(getCellOrCreate(row, col), value, "date");
    }

    public void setDate(final int row, final int col, final ZonedDateTime value) {
        setCellValue(getCellOrCreate(row, col), value, "date");
    }

    public void setBlank(final int row, final int col) {
        getCellOrCreate(row, col).setBlank();
    }

    public void addImage(
            final CellAddr anchor,
            final byte[] data,
            final ImageType type,
            final Double scaleX,
            final Double scaleY
    ) {
        switch(type) {
            case PNG:
                setImage(anchor, data, Workbook.PICTURE_TYPE_PNG, scaleX, scaleY);
                break;
            case JPEG:
                setImage(anchor, data, Workbook.PICTURE_TYPE_JPEG, scaleX, scaleY);
                break;
            default:
                throw new ExcelException(String.format(
                        "Excel cell %s in sheet '%s': Invalid image type. Use PNG or JPEG",
                        anchor.mapToOneBased(),
                        sheet.getSheetName()));
        }
    }

    public void addLineChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<LineDataSeries> series
    ) {
        new ExcelCharts(sheet).addLineChart(
                                title,
                                areaCellRangeAddr,
                                legendPosition,
                                categoryAxisTitle,
                                categoryAxisPosition,
                                valueAxisTitle,
                                valueAxisPosition,
                                threeDimensional,
                                varyColors,
                                categoriesCellRangeAddr,
                                series);
    }

    public void addBarChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final boolean directionBar,
            final BarGrouping grouping,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<BarDataSeries> series
    ) {
        new ExcelCharts(sheet).addBarChart(
                                    title,
                                    areaCellRangeAddr,
                                    legendPosition,
                                    categoryAxisTitle,
                                    categoryAxisPosition,
                                    valueAxisTitle,
                                    valueAxisPosition,
                                    threeDimensional,
                                    directionBar,
                                    grouping,
                                    varyColors,
                                    categoriesCellRangeAddr,
                                    series);
    }

    public void addAreaChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<AreaDataSeries> series
    ) {
        new ExcelCharts(sheet).addAreaChart(
                    title,
                    areaCellRangeAddr,
                    legendPosition,
                    categoryAxisTitle,
                    categoryAxisPosition,
                    valueAxisTitle,
                    valueAxisPosition,
                    threeDimensional,
                    categoriesCellRangeAddr,
                    series);
    }

    public void addPieChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final boolean threeDimensional,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<PieDataSeries> series
    ) {
        new ExcelCharts(sheet).addPieChart(
                                title,
                                areaCellRangeAddr,
                                legendPosition,
                                threeDimensional,
                                varyColors,
                                categoriesCellRangeAddr,
                                series);
    }

    public void setColumnWidthInPoints(final int col, final int width) {
        sheet.setColumnWidth(col, (int)(width * COL_WIDTH_MAGIC_FACTOR));
    }

    public void setRowHeightInPoints(final int row, final int height) {
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

    public void setValueKeepCellStyle(final int row, final int col, final Object value) {
        if (value == null) {
            setCellValue(getCellCreate(row, col), null, null);
        }
        else {
            if (value instanceof String)             setString(row, col,  (String)value,                     null);
            else if (value instanceof Boolean)       setBoolean(row, col, (Boolean)value,                    null);
            else if (value instanceof Integer)       setInteger(row, col, (Integer)value,                    null);
            else if (value instanceof Long)          setInteger(row, col, (Long)value,                       null);
            else if (value instanceof Float)         setFloat(row, col,   (Float)value,                      null);
            else if (value instanceof Double)        setFloat(row, col,   (Double)value,                     null);
            else if (value instanceof BigDecimal)    setFloat(row, col,   ((BigDecimal)value).doubleValue(), null);
            else if (value instanceof BigInteger)    setInteger(row, col, ((BigInteger)value).longValue(),   null);
            else if (value instanceof LocalDate)     setDate(row, col,    (LocalDate)value,                  null);
            else if (value instanceof LocalDateTime) setDate(row, col,    (LocalDateTime)value,              null);
            else if (value instanceof ZonedDateTime) setDate(row, col,    (ZonedDateTime)value,              null);
            else if (value instanceof Date)          setDate(row, col,    (Date)value,                       null);
            else throw new IllegalArgumentException("Invalid value type " + value.getClass().getSimpleName());
        }
    }

    public void setBgColor(final int row, final int col, final Color bgColor) {
        final Cell cell = getCellOrCreate(row, col);
        if (cell != null) {
            setBgColor(cell, bgColor);
        }
    }

    public void setBgColorIndex(final int row, final int col, final short bgColor) {
        final Cell cell = getCellOrCreate(row, col);
        if (cell != null) {
            setBgColorIndex(cell, bgColor);
        }
    }

    public void setStyle(final int row, final int col, final String styleName) {
        final Cell cell = getCellOrCreate(row, col);
        if (cell != null) {
            setStyle(cell, styleName);
        }
    }

    public void setFormula(final int row, final int col, final String formula) {
        setFormula(row, col, formula, null);
    }

    public void setFormula(final int row, final int col, final String formula, final String styleName) {
        final Cell cell = getCellOrCreate(row, col);
        cell.setCellFormula(formula);

        final CellStyle style = cellStyles.getCellStyle(styleName);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    public Map<String,Object> getCellStyleInfo(final int row, final int col) {
        final Map<String,Object> info = new HashMap<>();

        final Cell cell = getCell(row, col);

        if (cell != null) {
            final CellStyle style = cell.getCellStyle();
            if (style != null) {
                // Font details
                final Font font = cellStyles.getFont(style);
                info.put("font.name",   font.getFontName());
                info.put("font.size",   font.getFontHeightInPoints());
                info.put("font.bold",   font.getBold());
                info.put("font.italic", font.getItalic());

                // Alignment details
                info.put("h-align", style.getAlignment().name().toLowerCase());
                info.put("v-align", style.getVerticalAlignment().name().toLowerCase());

                // Border details
                info.put("border.top",    style.getBorderTop().name().toLowerCase());
                info.put("border.bottom", style.getBorderBottom().name().toLowerCase());
                info.put("border.left",   style.getBorderLeft().name().toLowerCase());
                info.put("border.right",  style.getBorderRight().name().toLowerCase());

                // Fill details
                info.put("fill.bg.color", style.getFillBackgroundColor());
                info.put("fill.fg.color", style.getFillForegroundColor());
                info.put("fill.pattern",  style.getFillPattern().name().toLowerCase());
            }
        }

        return info;
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

    public void setColumnHidden(final int col, final boolean hidden) {
        sheet.setColumnHidden(col, hidden);
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

    private void setImage(
            final CellAddr anchorAddr,
            final byte[] data,
            final int imageType,
            final Double scaleX,
            final Double scaleY
    ) {
        final CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        final Drawing<?> drawing = sheet.createDrawingPatriarch();

        final int pictureIdx = sheet.getWorkbook().addPicture(data, imageType);

        final ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(anchorAddr.getCol());
        anchor.setRow1(anchorAddr.getRow());

        final Picture pict = drawing.createPicture(anchor, pictureIdx);
        if (scaleX == null || scaleY== null) {
            pict.resize();
        }
        else {
            pict.resize(scaleX, scaleY);
        }
    }

    private void setCellValue(final Cell cell, final Object value, final String styleName) {
        if (styleName != null) {
            final CellStyle style = cellStyles.getCellStyle(styleName);
            if (style != null) {
                cell.setCellStyle(style);
            }
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
        return r == null ? null : r.getCell(col, MissingCellPolicy.RETURN_NULL_AND_BLANK);
    }

    private Cell getCellCreate(final int row, final int col) {
        return getRowCreate(row).createCell(col);
    }

    private Cell getCellOrCreate(final int row, final int col) {
        final Row r = getRowCreate(row);
        final Cell cell = r.getCell(col, MissingCellPolicy.RETURN_NULL_AND_BLANK);
        return cell == null ? r.createCell(col) : cell;
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

    private void setStyle(final Cell cell, final String styleName) {
        final CellStyle style = cellStyles.getCellStyle(styleName);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private Object getValue(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        switch (cellValue.getCellType()) {
            case BLANK:
                return null;
            case STRING:
                return cellValue.getStringValue();
            case BOOLEAN:
                return cellValue.getBooleanValue();
            case NUMERIC:
                return DateUtil.isCellDateFormatted(cell)
                        ? cell.getLocalDateTimeCellValue()
                        : cellValue.getNumberValue();
            case ERROR:
                return null;
            default:
                throw new ExcelException(String.format(
                    "Excel cell %s in sheet '%s': failed to read value",
                    new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                    sheet.getSheetName()));
        }
    }

    private String getString(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        switch (cellValue.getCellType()) {
            case BLANK:
                return null;
            case STRING:
                return cellValue.getStringValue();
            case BOOLEAN:
                return Boolean.toString(cellValue.getBooleanValue());
            case NUMERIC:
                return DateUtil.isCellDateFormatted(cell)
                        ? cell.getLocalDateTimeCellValue().toString()
                        : Double.toString(cellValue.getNumberValue());
            default:
                throw new ExcelException(String.format(
                        "Excel cell %s in sheet '%s': does not contain a string value",
                        new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                        sheet.getSheetName()));
        }
    }

    private Boolean getBoolean(final Cell cell) {
        if (cell == null) {
            return NULL_BOOLEAN;  // fooling 'Find Bugs' :-)
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        if (cellValue.getCellType() == CellType.BLANK) {
            return NULL_BOOLEAN;  // fooling 'Find Bugs' :-)
        }
        else if (cellValue.getCellType() == CellType.BOOLEAN) {
            return cellValue.getBooleanValue();
        }
        else {
            throw new ExcelException(String.format(
                "Excel cell %s in sheet '%s': does not contain a boolean value",
                new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                sheet.getSheetName()));
        }
    }

    private Long getInteger(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        if (cellValue.getCellType() == CellType.BLANK) {
            return null;
        }
        else if (cellValue.getCellType() == CellType.NUMERIC) {
            return (long)(cellValue.getNumberValue() + 0.5);
        }
        else {
            throw new ExcelException(String.format(
                "Excel cell [%s in sheet '%s': does not contain an integer value",
                new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                sheet.getSheetName()));
        }
    }

    private Double getFloat(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        if (cellValue.getCellType() == CellType.BLANK) {
            return null;
        }
        else if (cellValue.getCellType() == CellType.NUMERIC) {
            return cellValue.getNumberValue();
        }
        else {
            throw new ExcelException(String.format(
                    "Excel cell %s in sheet '%s': does not contain a float value. "
                        + "It actually holds a %s.",
                    new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                    sheet.getSheetName(),
                    cell.getCellType().name()));
        }
    }

    private LocalDateTime getDate(final Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.BLANK) {
            return null;
        }
        else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        else if (cell.getCellType() == CellType.FORMULA) {
            final Cell cellEval = evaluator.evaluateInCell(cell);

            switch (cellEval.getCachedFormulaResultType()) {
                case BLANK:   return null;
                case NUMERIC: return cellEval.getLocalDateTimeCellValue();
                default:
                    throw new ExcelException(String.format(
                            "Excel formula cell %s in sheet '%s': does not contain a date. "
                                + "It actually holds a %s.",
                            new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                            sheet.getSheetName(),
                            cell.getCellType().name()));
            }
        }
        else {
            throw new ExcelException(String.format(
                    "Excel cell %s in sheet '%s': does not contain a date. "
                        + "It actually holds a %s.",
                    new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                    sheet.getSheetName(),
                    cell.getCellType().name()));
        }
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
                    "Excel cell %s in sheet '%s': does not contain a formula. "
                        + "It actually holds a %s.",
                    new CellAddr(cell.getRowIndex(),cell.getColumnIndex()).mapToOneBased(),
                    sheet.getSheetName(),
                    cell.getCellType().name()));
        }
    }

    private String getErrorCode(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellValue cellValue = evaluator.evaluate(cell);

        if (cellValue.getCellType() == CellType.ERROR) {
            return cellValue.formatAsString();
        }
        else {
            return null;
        }
    }

    private String getDataFormatString(final Cell cell) {
        if (cell == null) {
            return null;
        }

        final CellStyle style = cell.getCellStyle();
        if (style == null) {
            return null;
        }

        return style.getDataFormatString();
    }

    private boolean isLocked(final Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellStyle style = cell.getCellStyle();
        return style == null ? false : style.getLocked();
    }

    private boolean isHidden(final Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellStyle style = cell.getCellStyle();
        return style == null ? false : style.getHidden();
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

    private void copyCellValue(final Cell from, final Cell to) {
        switch (from.getCellType()) {
            case STRING:
                to.setCellValue(from.getStringCellValue());
                break;
            case NUMERIC:
                to.setCellValue(from.getNumericCellValue());
                break;
            case BOOLEAN:
                to.setCellValue(from.getBooleanCellValue());
                break;
            case FORMULA:
                to.setCellFormula(from.getCellFormula());
                break;
            case BLANK:
                to.setBlank();
                break;
            default:
                break;
        }
    }



    // The Excel's magic conversion factor
    public static final float COL_WIDTH_MAGIC_FACTOR = 46.4f; // to points (1/72 inch)

    private static final Boolean NULL_BOOLEAN = null;

    private final Sheet sheet;
    private final ExcelCellStyles cellStyles;
    private final FormulaEvaluator evaluator;
}
