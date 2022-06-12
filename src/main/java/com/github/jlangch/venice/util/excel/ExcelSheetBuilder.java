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
package com.github.jlangch.venice.util.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;

import com.github.jlangch.venice.impl.util.excel.ExcelColumnDef;
import com.github.jlangch.venice.impl.util.excel.ExcelSheet;



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

    public ExcelSheetBuilder<T> renderItems(final List<T> items) {
        renderHeader();

        final int bodyRowStart = currRow0;

        items.forEach(v -> renderBodyItem(v));

        final int bodyRowEnd = currRow0 - 1;

        renderFooter(bodyRowStart, bodyRowEnd);

        return this;
    }

    public ExcelSheetBuilder<T> renderItem(final T item) {
        renderHeader();
        renderBodyItem(item);
        return this;
    }

    public ExcelSheetBuilder<T> value(final int row1, final int col1, final Object value) {
        sheet.setValue(row1-1, col1-1, value);
        return this;
    }

    public ExcelSheetBuilder<T> value(final int row1, final int col1, final Object value, final String stylename) {
        sheet.setValue(row1-1, col1-1, value, stylename);
        return this;
    }

    public ExcelSheetBuilder<T> formula(final int row1, final int col1, final String formula) {
        sheet.setFormula(row1-1, col1-1, formula);
        return this;
    }

    public ExcelSheetBuilder<T> formula(final int row1, final int col1, final String formula, final String stylename) {
        sheet.setFormula(row1-1, col1-1, formula, stylename);
        return this;
    }

    public ExcelSumFormulaBuilder<T> withSum(final int row1, final int col1) {
        return new ExcelSumFormulaBuilder<T>(this, sheet, row1, col1);
    }

    public ExcelSheetBuilder<T> skipRows(final int count) {
        skipRows = Math.max(0, count);
        return this;
    }

    public ExcelSheetBuilder<T> rowHeightInPoints(final int row1, final int height) {
        sheet.rowHeightInPoints(row1-1, height);
        return this;
    }

    public ExcelSheetBuilder<T> autoSizeColumns() {
        sheet.autoSizeColumns();
        return this;
    }

    public ExcelSheetBuilder<T> autoSizeColumn(final int col1) {
        sheet.autoSizeColumn(col1-1);
        return this;
    }

    public ExcelSheetBuilder<T> addMergedRegion(final int rowFrom1, final int rowTo1, final int colFrom1, final int colTo1) {
        sheet.addMergedRegion(rowFrom1, rowTo1, colFrom1, colTo1);
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

    public ExcelSheetBuilder<T> setDefaultColumnWidthInPoints(final int width) {
        columnWidth = width;
        return this;
    }

    public String sumFormula(final int rowFrom1, final int rowTo1, final int colFrom1, final int colTo1) {
        return String.format(
                "SUM(%s:%s)",
                sheet.getCellAddress(rowFrom1-1, colFrom1-1),
                sheet.getCellAddress(rowTo1-1, colTo1-1));
    }

    public String cellAddress(final int row1, final int col1) {
        return sheet.getCellAddress(row1-1, col1-1);
    }

    public ExcelSheetReader reader() {
        return new ExcelSheetReader(sheet);
    }

    public ExcelBuilder end() {
        return parentBuilder;
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
                        sheet.setValue(
                                currRow0,
                                col0,
                                colDef.colMapper.apply(item),
                                getColumnBodyStyle(col0));
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

    private final ExcelBuilder parentBuilder;
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
