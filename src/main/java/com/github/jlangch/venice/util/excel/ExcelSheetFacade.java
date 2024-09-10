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
package com.github.jlangch.venice.util.excel;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;

import com.github.jlangch.venice.impl.util.excel.ExcelColumnDef;
import com.github.jlangch.venice.impl.util.excel.ExcelSheet;
import com.github.jlangch.venice.util.excel.chart.AreaDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarGrouping;
import com.github.jlangch.venice.util.excel.chart.ImageType;
import com.github.jlangch.venice.util.excel.chart.LineDataSeries;
import com.github.jlangch.venice.util.excel.chart.PieDataSeries;
import com.github.jlangch.venice.util.excel.chart.Position;
import com.github.jlangch.venice.util.pdf.HtmlColor;



public class ExcelSheetFacade<T> {

    public ExcelSheetFacade(
            final ExcelFacade excelBuilder,
            final ExcelSheet sheet
    ) {
        this.parentBuilder = excelBuilder;
        this.sheet = sheet;
    }


    public String getName() {
        return sheet.getName();
    }

    public int getIndex() {
        return sheet.getIndex() + 1;  // 1-based
    }

    public int getFirstRowNum() {
        final int n = sheet.getFirstRowNum();
        return n < 0 ? n : n + 1;  // 1-based
    }

    public int getLastRowNum() {
        final int n = sheet.getLastRowNum();
        return n < 0 ? n : n + 1;  // 1-based
    }

    public int getFirstCellNum(final int row1) {
        final int n = sheet.getFirstCellNum(row1-1);
        return n < 0 ? n : n + 1;  // 1-based
    }

    public int getLastCellNum(final int row1) {
        // returns the last cell number PLUS ONE
        final int n = sheet.getLastCellNum(row1-1);
        return n; // no correction
    }

    public boolean isCellEmpty(final int row1, final int col1) {
        return sheet.isCellEmpty(row1-1, col1-1);
    }

    public String getCellType(final int row1, final int col1) {
        return sheet.getCellType(row1-1, col1-1);
    }

    public void lock(final int row1, final int col1, final boolean locked) {
        sheet.lock(row1-1, col1-1, locked);
    }

    public boolean isLocked(final int row1, final int col1) {
         return sheet.isLocked(row1-1, col1-1);
    }

    public boolean isHidden(final int row1, final int col1) {
         return sheet.isHidden(row1-1, col1-1);
    }

    public boolean isColumnHidden(final int col1) {
        return sheet.isColumnHidden(col1-1);
    }

    public String getFormula(final int row1, final int col1) {
        return sheet.getFormula(row1-1, col1-1);
    }

    public String getDataFormatString(final int row1, final int col1) {
        return sheet.getDataFormatString(row1-1, col1-1);
    }

    public ExcelSheetFacade<T> evaluateAllFormulas() {
        sheet.evaluateAllFormulas();
        return this;
    }

    public void evaluateCell(final int row1, final int col1) {
        sheet.evaluateCell(row1-1, col1-1);
    }

    public ExcelFacade end() {
        return parentBuilder;
    }


    // ------------------------------------------------------------------------
    // Writer functions
    // ------------------------------------------------------------------------

    public void deleteRow(final int row1) {
        sheet.deleteRow(row1-1);
    }

    public void copyRow(final int row1From, final int row1To,  final boolean copyValues, final boolean copyStyles) {
        sheet.copyRow(row1From-1, row1To-1, copyValues, copyStyles);
    }

    public void copyRowToEndOfSheet(final int row1, final boolean copyValues, final boolean copyStyles) {
        sheet.copyRowToEndOfSheet(row1-1, copyValues, copyStyles);
    }

    public void clearRow(final int row1, final boolean clearValues, final boolean clearStyles) {
        sheet.clearRow(row1-1, clearValues, clearStyles);
    }

    public void insertEmptyRow(final int row1) {
        sheet.insertEmptyRow(row1-1);
    }

    public void insertEmptyRows(final int row1, final int count) {
        sheet.insertEmptyRows(row1-1, count);
    }

    public void copyCellStyle(
            final int cellRowFrom1,
            final int cellColFrom1,
            final int cellRowTo1,
            final int cellColTo1
    ) {
        sheet.copyCellStyle(cellRowFrom1-1, cellColFrom1-1, cellRowTo1-1, cellColTo1-1);
    }

    public void addConditionalBackgroundColor(
            final String condRule,     // "ISBLANK(A1)"
            final String colorHtml,    // "#CC636A"
            final int regionFirstRow1,
            final int regionLastRow1,
            final int regionFirstCol1,
            final int regionLastCol1
    ) {
        sheet.addConditionalBackgroundColor(
                condRule,
                colorHtml,
                regionFirstRow1-1,
                regionLastRow1-1,
                regionFirstCol1-1,
                regionLastCol1-1);
    }

    public void addConditionalFontColor(
            final String condRule,     // "$A$1 > 5"
            final String colorHtml,    // "#CC636A"
            final int regionFirstRow1,
            final int regionLastRow1,
            final int regionFirstCol1,
            final int regionLastCol1
    ) {
        sheet.addConditionalFontColor(
                condRule,
                colorHtml,
                regionFirstRow1-1,
                regionLastRow1-1,
                regionFirstCol1-1,
                regionLastCol1-1);
    }

    public void addConditionalBorder(
            final String condRule,       // "$A$1 > 5"
            final BorderStyle borderTopStyle,
            final BorderStyle borderRightStyle,
            final BorderStyle borderBottomStyle,
            final BorderStyle borderLeftStyle,
            final String borderTopColorHtml,   // "#CC636A"
            final String borderRightColorHtml,
            final String borderBottomColorHtml,
            final String borderLeftColorHtml,
            final int regionFirstRow1,
            final int regionLastRow1,
            final int regionFirstCol1,
            final int regionLastCol1
    ) {
        sheet.addConditionalBorder(
                condRule,
                borderTopStyle,
                borderRightStyle,
                borderBottomStyle,
                borderLeftStyle,
                borderTopColorHtml,
                borderRightColorHtml,
                borderBottomColorHtml,
                borderLeftColorHtml,
                regionFirstRow1-1,
                regionLastRow1-1,
                regionFirstCol1-1,
                regionLastCol1-1);
    }

    public void addTextDataValidation(
            final List<String> validValues,
            final boolean emptyCellAllowed,
            final String errTitle,
            final String errText,
            final int regionFirstRow1,
            final int regionLastRow1,
            final int regionFirstCol1,
            final int regionLastCol1
    ) {
        sheet.addTextDataValidation(
                validValues,
                emptyCellAllowed,
                errTitle,
                errText,
                regionFirstRow1-1,
                regionLastRow1-1,
                regionFirstCol1-1,
                regionLastCol1-1);
    }

    public ExcelSheetFacade<T> noHeader() {
        this.noHeader = true;
        return this;
    }

    public ExcelSheetFacade<T> createFreezePane(final int cols, final int rows) {
        sheet.createFreezePane(Math.max(0, cols), Math.max(0, rows));
        return this;
    }

    public ExcelSheetFacade<T> defaultHeaderStyle(final String style) {
        this.defaultHeaderStyle = style;
        return this;
    }

    public ExcelSheetFacade<T> defaultBodyStyle(final String style) {
        this.defaultBodyStyle = style;
        return this;
    }

    public ExcelSheetFacade<T> defaultFooterStyle(final String style) {
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
        return new ExcelColumnBuilder<T>(this, columnDefs, colHeaderName)
                    .colMapper(colMapper);
    }

    public ExcelColumnBuilder<T> withColumn(
            final String colHeaderName,
            final String fieldName
    ) {
        return new ExcelColumnBuilder<T>(this, columnDefs, colHeaderName)
                    .colMapper(e -> ((DataRecord)e).get(fieldName));
    }

    public ExcelSheetFacade<T> renderItems(final List<T> items) {
        renderHeader();

        final int bodyRowStart = currRow0;
        items.forEach(v -> renderBodyItem(v));

        final int bodyRowEnd = currRow0 - 1;
        renderFooter(bodyRowStart, bodyRowEnd);

        return this;
    }

    public ExcelSheetFacade<T> renderItem(final T item) {
        renderHeader();
        renderBodyItem(item);
        return this;
    }

    public ExcelSheetFacade<T> value(final int row1, final int col1, final Object value) {
        sheet.setValue(row1-1, col1-1, value);
        return this;
    }

    public ExcelSheetFacade<T> value(final int row1, final int col1, final Object value, final String stylename) {
        sheet.setValue(row1-1, col1-1, value, stylename);
        return this;
    }

    public ExcelSheetFacade<T> valueKeepCellStyle(final int row1, final int col1, final Object value) {
        sheet.setValueKeepCellStyle(row1-1, col1-1, value);
        return this;
    }

    public ExcelSheetFacade<T> image(
            final int row1,
            final int col1,
            final byte[] data,
            final ImageType type,
            final Double scaleX,
            final Double scaleY
    ) {
        sheet.addImage(new CellAddr(row1, col1), data, type, scaleX, scaleY);
        return this;
    }

    public ExcelSheetFacade<T> lineChart(
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
        final CellRangeAddr area = areaCellRangeAddr.mapToZeroBased();

        sheet.addLineChart(
                title,
                new CellRangeAddr(
                        area.getFirstRow(),
                        area.getLastRow()+1,
                        area.getFirstCol(),
                        area.getLastCol()+1),
                legendPosition,
                categoryAxisTitle,
                categoryAxisPosition,
                valueAxisTitle,
                valueAxisPosition,
                threeDimensional,
                varyColors,
                categoriesCellRangeAddr.mapToZeroBased(),
                series.stream().map(s -> s.mapToZeroBasedAddresses()).collect(Collectors.toList()));

        return this;
    }

    public ExcelSheetFacade<T> barChart(
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
        final CellRangeAddr area = areaCellRangeAddr.mapToZeroBased();

        sheet.addBarChart(
                title,
                new CellRangeAddr(
                        area.getFirstRow(),
                        area.getLastRow()+1,
                        area.getFirstCol(),
                        area.getLastCol()+1),
                legendPosition,
                categoryAxisTitle,
                categoryAxisPosition,
                valueAxisTitle,
                valueAxisPosition,
                threeDimensional,
                directionBar,
                grouping,
                varyColors,
                categoriesCellRangeAddr.mapToZeroBased(),
                series.stream().map(s -> s.mapToZeroBasedAddresses()).collect(Collectors.toList()));

        return this;
    }

    public ExcelSheetFacade<T> areaChart(
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
        final CellRangeAddr area = areaCellRangeAddr.mapToZeroBased();

        sheet.addAreaChart(
                title,
                new CellRangeAddr(
                        area.getFirstRow(),
                        area.getLastRow()+1,
                        area.getFirstCol(),
                        area.getLastCol()+1),
                legendPosition,
                categoryAxisTitle,
                categoryAxisPosition,
                valueAxisTitle,
                valueAxisPosition,
                threeDimensional,
                categoriesCellRangeAddr.mapToZeroBased(),
                series.stream().map(s -> s.mapToZeroBasedAddresses()).collect(Collectors.toList()));

        return this;
    }

    public ExcelSheetFacade<T> pieChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final boolean threeDimensional,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<PieDataSeries> series
    ) {
        final CellRangeAddr area = areaCellRangeAddr.mapToZeroBased();

        sheet.addPieChart(
                title,
                new CellRangeAddr(
                        area.getFirstRow(),
                        area.getLastRow()+1,
                        area.getFirstCol(),
                        area.getLastCol()+1),
                legendPosition,
                threeDimensional,
                varyColors,
                categoriesCellRangeAddr.mapToZeroBased(),
                series.stream().map(s -> s.mapToZeroBasedAddresses()).collect(Collectors.toList()));

        return this;
    }

    public ExcelSheetFacade<T> formula(final int row1, final int col1, final String formula) {
        sheet.setFormula(row1-1, col1-1, formula);
        return this;
    }

    public ExcelSheetFacade<T> formula(final int row1, final int col1, final String formula, final String stylename) {
        sheet.setFormula(row1-1, col1-1, formula, stylename);
        return this;
    }

    public ExcelSheetFacade<T> style(final int row1, final int col1, final String stylename) {
        sheet.setStyle(row1-1, col1-1, stylename);
        return this;
    }

    public ExcelSheetFacade<T> bgColor(final int row1, final int col1, final Color bgColor) {
        sheet.setBgColor(row1-1, col1-1, bgColor);
        return this;
    }

    public ExcelSheetFacade<T> bgColor(final int row1, final int col1, final String bgColorHtml) {
        sheet.setBgColor(row1-1, col1-1, HtmlColor.getColor(bgColorHtml));
        return this;
    }

    public ExcelSheetFacade<T> bgColor(final int row1, final int col1, final short bgColor) {
        sheet.setBgColorIndex(row1-1, col1-1, bgColor);
        return this;
    }

    public ExcelSumFormulaBuilder<T> withSum(final int row1, final int col1) {
        return new ExcelSumFormulaBuilder<T>(this, sheet, row1, col1);
    }

    public ExcelSheetFacade<T> skipRows(final int count) {
        skipRows = Math.max(0, count);
        return this;
    }

    public ExcelSheetFacade<T> rowHeightInPoints(final int row1, final int height) {
        sheet.setRowHeightInPoints(row1-1, height);
        return this;
    }

    public ExcelSheetFacade<T> colWidthInPoints(final int row1, final int width) {
        sheet.setColumnWidthInPoints(row1-1, width);
        return this;
    }

    public ExcelSheetFacade<T> autoSizeColumns() {
        sheet.autoSizeColumns();
        return this;
    }

    public ExcelSheetFacade<T> autoSizeColumn(final int col1) {
        sheet.autoSizeColumn(col1-1);
        return this;
    }

    public ExcelSheetFacade<T> hideColumn(final int col1) {
        sheet.setColumnHidden(col1-1, true);
        return this;
    }

    public ExcelSheetFacade<T> hideColumns(final int... col1s) {
        for(int c : col1s) hideColumn(c);
        return this;
    }

    public ExcelSheetFacade<T> hideColumn(final String colID) {
        if (colID != null) {
            int colNr1 = 1;
            for(ExcelColumnDef<T> colDef : columnDefs) {
                if (colID.equals(colDef.id)) {
                    hideColumn(colNr1);
                    break;
                }
                colNr1++;
            }
        }
        return this;
    }

    public ExcelSheetFacade<T> hideColumns(final String... colIDs) {
        for(String id : colIDs) hideColumn(id);
        return this;
    }

    public ExcelSheetFacade<T> addMergedRegion(final int rowFrom1, final int rowTo1, final int colFrom1, final int colTo1) {
        sheet.addMergedRegion(rowFrom1-1, rowTo1-1, colFrom1-1, colTo1-1);
        return this;
    }

    public ExcelSheetFacade<T> displayZeros(final boolean value) {
        sheet.setDisplayZeros(value);
        return this;
    }

    public ExcelSheetFacade<T> setDefaultColumnWidthInPoints(final int width) {
        columnWidth = width;
        return this;
    }

    public String sumFormula(final int rowFrom1, final int rowTo1, final int colFrom1, final int colTo1) {
        return String.format(
                "SUM(%s:%s)",
                sheet.getCellAddress_A1_style(rowFrom1-1, colFrom1-1),
                sheet.getCellAddress_A1_style(rowTo1-1, colTo1-1));
    }

    public void setUrlHyperlink(final int row1, final int col1, final String text, final String urlAddress) {
        sheet.setUrlHyperlink(row1-1, col1-1, text, urlAddress);
    }

    public void setEmailHyperlink(final int row1, final int col1, final String text, final String emailAddress) {
        sheet.setEmailHyperlink(row1-1, col1-1, text, emailAddress);
    }

    public void removeFormula(final int row1, final int col1) {
    	sheet.removeFormula(row1-1, col1-1);
    }

    public void removeHyperlink(final int row1, final int col1) {
    	sheet.removeHyperlink(row1-1, col1-1);
    }

    public void removeComment(final int row1, final int col1) {
    	sheet.removeComment(row1-1, col1-1);
    }


    public Map<String,Object> getCellStyleInfo(final int row1, final int col1) {
        return sheet.getCellStyleInfo(row1-1, col1-1);
    }

    public String cellAddress_A1_style(final int row1, final int col1) {
        return sheet.getCellAddress_A1_style(row1-1, col1-1);
    }


    // ------------------------------------------------------------------------
    // Reader functions
    // ------------------------------------------------------------------------


    public String getCellFormulaResultType(final int row1, final int col1) {
        return sheet.getCellFormulaResultType(row1-1, col1-1);
    }

    public String getCellAddress_A1_style(final int row1, final int col1) {
        return sheet.getCellAddress_A1_style(row1-1, col1-1);
    }

    public Object getValue(final int row1, final int col1) {
        return sheet.getValue(row1-1, col1-1);
    }

    public String getString(final int row1, final int col1) {
        return sheet.getString(row1-1, col1-1);
    }

    public Boolean getBoolean(final int row1, final int col1) {
        return sheet.getBoolean(row1-1, col1-1);
    }

    public Long getInteger(final int row1, final int col1) {
        return sheet.getInteger(row1-1, col1-1);
    }

    public Double getFloat(final int row1, final int col1) {
        return sheet.getFloat(row1-1, col1-1);
    }

    public LocalDateTime getDate(final int row1, final int col1) {
        return sheet.getDate(row1-1, col1-1);
    }

    public String getErrorCode(final int row1, final int col1) {
         return sheet.getErrorCode(row1-1, col1-1);
    }

    private String getColumnHeaderStyle(final int col0) {
        final String style = col0 < 0 || (col0 > columnDefs.size()-1) ? null : columnDefs.get(col0).headerStyle;
        return style == null ? defaultHeaderStyle : style;
    }

    private String getColumnBodyStyle(final int col0) {
        final String style = col0 < 0 || (col0 > columnDefs.size()-1) ? null : columnDefs.get(col0).bodyStyle;
        return style == null ? defaultBodyStyle : style;
    }

    private String getColumnFooterStyle(final int col0) {
        final String style = col0 < 0 || (col0 > columnDefs.size()-1) ? null : columnDefs.get(col0).footerStyle;
        return style == null ? defaultFooterStyle : style;
    }

    private List<String> getHeaderStrings() {
        return columnDefs.stream().map(c -> c.header).collect(Collectors.toList());
    }

    private void setHeaderValues(final int row0, final List<?> values) {
        int col0 = 0;
        for(Object v : values) {
            if (v != null) {
                sheet.setValue(row0, col0, v, getColumnHeaderStyle(col0));
            }
            col0++;
        }
    }

    private boolean hasFooter() {
        return columnDefs.stream().anyMatch(c -> c.footerType != ExcelColumnDef.FooterType.NONE);
    }

    private void renderHeader() {
        if (!headerRendered) {
            renderColumnWidths();

            if (!noHeader) {
                setHeaderValues(currRow0++, getHeaderStrings());
            }

            headerRendered = true;
        }
    }

    private void renderFooter(final int bodyRowFrom0, final int bodyRowTo0) {
        final boolean emptyBody = bodyRowTo0 < bodyRowFrom0;

        if (hasFooter()) {
            int col0 = 0;
            for(ExcelColumnDef<T> colDef : columnDefs) {
                switch (colDef.footerType) {
                    case NONE:
                        sheet.setValue(currRow0, col0, null, null);
                        break;
                    case TEXT:
                        sheet.setValue(currRow0, col0, colDef.footerValue, getColumnFooterStyle(col0));
                        break;
                    case NUMBER:
                        sheet.setValue(currRow0, col0, colDef.footerValue, getColumnFooterStyle(col0));
                        break;
                    case FORMULA:
                        sheet.setValue(currRow0, col0, null, null);  // TODO
                        break;
                    case SUM:
                        if (emptyBody) {
                            sheet.setValue(currRow0, col0, null, null);
                        }
                        else {
                            final String formula = String.format(
                                    "SUM(%s:%s)",
                                    new CellAddress(bodyRowFrom0, col0).formatAsString(),
                                    new CellAddress(bodyRowTo0, col0).formatAsString());

                            sheet.setFormula(currRow0, col0, formula, getColumnFooterStyle(col0));
                        }
                        break;
                    case MIN:
                        if (emptyBody) {
                            sheet.setValue(currRow0, col0, null, getColumnFooterStyle(col0));
                        }
                        else {
                            final String formula = String.format(
                                    "MIN(%s:%s)",
                                    new CellAddress(bodyRowFrom0, col0).formatAsString(),
                                    new CellAddress(bodyRowTo0, col0).formatAsString());

                            sheet.setFormula(currRow0, col0, formula, getColumnFooterStyle(col0));
                        }
                        break;
                    case MAX:
                        if (emptyBody) {
                            sheet.setValue(currRow0, col0, null, getColumnFooterStyle(col0));
                        }
                        else {
                            final String formula = String.format(
                                    "MAX(%s:%s)",
                                    new CellAddress(bodyRowFrom0, col0).formatAsString(),
                                    new CellAddress(bodyRowTo0, col0).formatAsString());

                            sheet.setFormula(currRow0, col0, formula, getColumnFooterStyle(col0));
                        }
                        break;
                    case AVERAGE:
                        if (emptyBody) {
                            sheet.setValue(currRow0, col0, null, getColumnFooterStyle(col0));
                        }
                        else {
                            final String formula = String.format(
                                    "AVERAGE(%s:%s)",
                                    new CellAddress(bodyRowFrom0, col0).formatAsString(),
                                    new CellAddress(bodyRowTo0, col0).formatAsString());

                            sheet.setFormula(currRow0, col0, formula, getColumnFooterStyle(col0));
                        }
                        break;
                }
                col0++;
            }
        }
        currRow0++;
    }

    private void renderBodyItem(final T item) {
        if (skipRows > 0) {
            skipRows--;
        }
        else {
            if (item != null) {
                int col0 = 0;
                for(ExcelColumnDef<T> colDef : columnDefs) {
                    if (colDef.colMapper != null) {
                        final Object cellVal = colDef.colMapper.apply(item);
                        if (cellVal instanceof Formula) {
                            sheet.setFormula(currRow0, col0, ((Formula)cellVal).getFormula());
                        }
                        else {
                            sheet.setValue(
                                    currRow0,
                                    col0,
                                    cellVal,
                                    getColumnBodyStyle(col0));
                        }
                    }
                    col0++;
                }
            }
            currRow0++;
        }
    }

    private void renderColumnWidths() {
        int col0 = 0;
        for(ExcelColumnDef<T> colDef : columnDefs) {
            if (colDef.width != null) {
                sheet.setColumnWidthInPoints(col0, colDef.width);
            }
            else  if (columnWidth != null) {
                sheet.setColumnWidthInPoints(col0, columnWidth);
            }
            col0++;
        }
    }


    public static final int DEFAULT_FONT_SIZE = XSSFFont.DEFAULT_FONT_SIZE;

    private final ExcelFacade parentBuilder;
    private final ExcelSheet sheet;
    private final List<ExcelColumnDef<T>> columnDefs = new ArrayList<>();
    private boolean noHeader = false;
    private boolean headerRendered = false;
    private int currRow0 = 0;  // zero based
    private int skipRows = 0;
    private Integer columnWidth;
    private String defaultHeaderStyle;
    private String defaultBodyStyle;
    private String defaultFooterStyle;
}
