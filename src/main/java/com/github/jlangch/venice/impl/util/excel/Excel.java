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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.jlangch.venice.ExcelException;
import com.github.jlangch.venice.FileException;
import com.github.jlangch.venice.impl.util.MimeTypes;


/**
 * An Excel reader/writer for scripts. It supports HSSF (xls) and XSSF (xlsx)
 * Excel file formats. When opening an excel file the format (xls, xlsx) will
 * be detected automatically.
 *
 * <p>The following cell formats are pre-registered and can be changed any
 * time:
 * <ul>
 *   <li>"integer" -&gt; "#0"</li>
 *   <li>"float" -&gt; "#,##0.00"</li>
 *   <li>"date" -&gt; "d.m.yyyy"</li>
 *   <li>"datetime" -&gt; "d.m.yyyy hh:mm:ss"</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>
 *    final List<QueryStatistics> stats = getQueryStatistics();
 *
 *    final Excel excel = Excel.createXlsx();
 *
 *    final ExcelSheet sheet = excel.createSheet("Queries");
 *    sheet.setString(0, 0, "Query");
 *    sheet.setString(0, 1, "Hits");
 *    sheet.setString(0, 2, "Min [ms]");
 *    sheet.setString(0, 3, "Max [ms]");
 *    sheet.setString(0, 4, "Avg [ms]");
 *
 *    stats.stream()
 *         .collect(Collectors.toMap(stats::indexOf, a -> a))
 *         .forEach((idx2, v) -> {
 *             sheet.setString( idx2+1, 0, v.getName());
 *             sheet.setInteger(idx2+1, 1, v.getHits());
 *             sheet.setInteger(idx2+1, 2, v.getMinElapsedTime());
 *             sheet.setInteger(idx2+1, 3, v.getMaxElapsedTime());
 *             sheet.setInteger(idx2+1, 4, v.getAvgElapsedTime());
 *          });
 *
 *    sheet.autoSizeColumns();
 *
 *    return excel.writeToDocument("ArangoDbQueries");
 * </pre>
 *
 * @author juerg
 */
public class Excel implements Closeable {

    Excel(final Workbook workbook) {
        this.workbook = workbook;
        this.cellDataStyles = new ExcelCellStyles(workbook, fonts);
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    public static Excel create() {
        return createXls();
    }

    public static Excel createXls() {
        return new Excel(new HSSFWorkbook());
    }

    public static Excel createXlsx() {
        return new Excel(new XSSFWorkbook());
    }

    public static Excel open(final byte[] document) {
        return open(new ByteArrayInputStream(document));
    }

    public static Excel open(final ByteBuffer document) {
        return open(new ByteArrayInputStream(document.array()));
    }

    public static Excel open(final File file) {
        try {
            return open(new FileInputStream(file));
        }
        catch(Exception ex) {
            throw new ExcelException(String.format(
                    "Failed to open the Excel file '%s'. File not found!",
                    file.getPath()));
        }
    }

    public static Excel open(final InputStream document) {
        try {
            return new Excel(WorkbookFactory.create(document));
        }
        catch(Exception ex) {
            throw new ExcelException("Failed to open the Excel document from the input stream");
        }
    }

    public ExcelSheet createSheet(final String name) {
        return new ExcelSheet(workbook.createSheet(name), cellDataStyles, evaluator);
    }

    public ExcelSheet getSheet(final String name) {
    	final Sheet sheet = workbook.getSheet(name);
    	if (sheet == null) {
    		throw new ExcelException(String.format("The sheet '%s' does not exist", name));
    	}
    	else {
    		return new ExcelSheet(sheet, cellDataStyles, evaluator);
    	}
    }

    public ExcelSheet getSheetAt(final int sheetIdx) {
    	final Sheet sheet = workbook.getSheetAt(sheetIdx);
    	if (sheet == null) {
    		throw new ExcelException(String.format("The sheet at the index '%d' does not exist", sheetIdx+1));
    	}
    	else {
    		return new ExcelSheet(sheet, cellDataStyles, evaluator);
    	}
    }

    public int getNumberOfSheets() {
        return workbook.getNumberOfSheets();
    }

    public void evaluateAllFormulas() {
        evaluator.clearAllCachedResultValues();
        evaluator.evaluateAll();
    }

    public void registerFont(
            final String id,
            final String fontName,
            final int heightInPoints
    ) {
        registerFont(id, fontName, heightInPoints, false, false, (Short)null);
    }

    public void registerFont(
            final String id,
            final String fontName,
            final Integer heightInPoints,
            final boolean bold,
            final boolean italic,
            final Short colorIndex
    ) {
        final Font font = workbook.createFont();
        if (fontName != null) {
            font.setFontName(fontName);
        }
        if (heightInPoints != null) {
            font.setFontHeightInPoints(heightInPoints.shortValue());
        }
        font.setBold(bold);
        font.setItalic(italic);
        if (colorIndex != null) {
            font.setColor(colorIndex);
        }
        fonts.put(id, font);
    }

    public void registerFont(
            final String id,
            final String fontName,
            final Integer heightInPoints,
            final boolean bold,
            final boolean italic,
            final Color color
    ) {
        final Font font = workbook.createFont();
        if (fontName != null) {
            font.setFontName(fontName);
        }
        if (heightInPoints != null) {
            font.setFontHeightInPoints(heightInPoints.shortValue());
        }
        font.setBold(bold);
        font.setItalic(italic);
        if (color != null) {
            if (font instanceof XSSFFont) {
                ((XSSFFont)font).setColor(new XSSFColor(color, null));
            }
            else if (font instanceof HSSFFont) {
                font.setColor(ColorUtil.bestHSSFColor((HSSFWorkbook)workbook, color).getIndex());
            }
        }
        fonts.put(id, font);
    }

    public void registerCellFormat(final String id, final String format) {
        registerCellFormat(
                id, format, null, (Short)null, null, null, null, (Short)null,
                null, null, null, null);
    }

    public void registerCellFormat(
            final String id,
            final String format,
            final String fontRefName,
            final Short bgColorIndex,
            final Boolean wrapText,
            final HorizontalAlignment hAlign,
            final VerticalAlignment vAlign,
            final Short rotation,
            final BorderStyle borderTopStyle,
            final BorderStyle borderRightStyle,
            final BorderStyle borderBottomStyle,
            final BorderStyle borderLeftStyle
    ) {
        cellDataStyles.registerCellFormat(
                id, format, fontRefName, bgColorIndex, wrapText, hAlign, vAlign, rotation,
                borderTopStyle, borderRightStyle, borderBottomStyle, borderLeftStyle);
    }

    public void registerCellFormat(
            final String id,
            final String format,
            final String fontRefName,
            final Color bgColor,
            final Boolean wrapText,
            final HorizontalAlignment hAlign,
            final VerticalAlignment vAlign,
            final Short rotation,
            final BorderStyle borderTopStyle,
            final BorderStyle borderRightStyle,
            final BorderStyle borderBottomStyle,
            final BorderStyle borderLeftStyle
    ) {
        cellDataStyles.registerCellFormat(
                id, format, fontRefName, bgColor, wrapText, hAlign, vAlign, rotation,
                borderTopStyle, borderRightStyle, borderBottomStyle, borderLeftStyle);
    }

    public void write(final OutputStream outputStream) {
        if (outputStream == null) {
            close();
            throw new IllegalArgumentException("An 'outputStream' must not be null");
        }

        try {
            this.workbook.write(outputStream);
        }
        catch(Exception ex) {
            throw new ExcelException("Failed to write the Excel document to the output stream", ex);
        }
        finally {
            close();
        }
    }

    public byte[] writeToBytes() {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        write(bs);
        return bs.toByteArray();
    }

    public ByteBuffer writeToByteBuffer() {
        return ByteBuffer.wrap(writeToBytes());
    }

    public void writeToFile(final File file) {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(writeToBytes());
        }
        catch(Exception ex) {
            throw new FileException("Failed to write the Excel to a file", ex);
        }
    }

    public Map<String,Object> writeToDocument(final String docName) {
        final Map<String,Object> doc = new HashMap<>();
        doc.put("binary", writeToBytes());
        doc.put("name", docName);
        doc.put("filename", makeFilename(docName));
        doc.put("mimetype", isXls() ? MimeTypes.APPLICATION_XLS : MimeTypes.APPLICATION_XLSX);
        return doc;
    }

    @Override
    public void close() {
        try {
            this.workbook.close();
        }
        catch(Exception ex) {
            // silently close
        }
    }

    public String makeFilename(final String name) {
        return (name.endsWith(".xls") || name.endsWith(".xlsx"))
                ? name
                : isXlsx() ? name + ".xlsx" : name + ".xls";
    }

    public boolean isXls() {
        return workbook instanceof HSSFWorkbook;
    }

    public boolean isXlsx() {
        return workbook instanceof XSSFWorkbook;
    }


    public static final int DEFAULT_FONT_SIZE = XSSFFont.DEFAULT_FONT_SIZE;

    private final Workbook workbook;
    private final ExcelCellStyles cellDataStyles;
    private final Map<String,Font> fonts = new HashMap<>();
    private final FormulaEvaluator evaluator;
}
