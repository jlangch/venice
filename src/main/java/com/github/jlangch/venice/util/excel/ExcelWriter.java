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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.github.jlangch.venice.impl.util.excel.Excel;


/**
 * Excel builder
 *
 * <p><b>Example 1:</b>
 * <pre>
 *    final byte[] data = ExcelWriter
 *                           .createXlsx()
 *                           .withSheet("Persons", Person.class)
 *                             .withColumn("FirstName", Person::getFirstName)
 *                             .withColumn("LastName", Person::getLastName)
 *                             .withColumn("Age", Person::getAge)
 *                             .renderData(persons())
 *                             .autoSizeColumns()
 *                             .end()
 *                           .writeToBytes();
 * </pre>
 *
 * <p><b>Example 2 (header row format):</b>
 * <pre>
 *    final byte[] data = ExcelWriter
 *                           .createXlsx()
 *                           .withFont("bold").bold().end()
 *                           .withFont("italic").italic().end()
 *                           .withCellStyle("header").font("bold").end()
 *                           .withSheet("Persons", Person.class)
 *                             .defaultHeaderStyle("header")
 *                             .withColumn("FirstName", Person::getFirstName)
 *                             .withColumn("LastName", Person::getLastName)
 *                             .withColumn("Age", Person::getAge)
 *                             .renderData(persons)
 *                             .renderData(getQueryStatistics())
 *                             .autoSizeColumns()
 *                             .end()
 *                           .writeToBytes();
 * </pre>
 *
 * <p><b>Example 3 (footer SUM):</b>
 * <pre>
 *    final List&lt;DataRecord&gt; persons = persons();
 *    final byte[] data = ExcelWriter
 *                           .createXlsx()
 *                           .withSheet("Persons", Person.class)
 *                              .withColumn("FirstName")
 *                                 .colMapper(Person::getFirstName)
 *                                 .footerTextValue("SUM age")
 *                                 .end()
 *                              .withColumn("LastName")
 *                                 .colMapper(Person::getLastName)
 *                                 .end()
 *                              .withColumn("Age")
 *                                 .colMapper(Person::getAge)
 *                                 .footerSum()
 *                                 .end()
 *                              .renderData(persons)
 *                              .autoSizeColumns()
 *                              .end()
 *                           .writeToBytes();
 * </pre>
 *
 * <p><b>Example 4 (footer SUM with styles):</b>
 * <pre>
 *    final List&lt;DataRecord&gt; persons = persons();
 *    final byte[] data = ExcelWriter
 *                           .createXlsx()
 *                           .withFont("bold").bold().end()
 *                           .withFont("bold-blue").bold().color(IndexedColors.BLUE).end()
 *                           .withCellStyle("header").font("bold").bgColor(IndexedColors.GREY_25_PERCENT).end()
 *                           .withCellStyle("age").format("#,##0").end()
 *                           .withCellStyle("sum-header").font("bold").end()
 *                           .withCellStyle("sum-age").font("bold-blue").format("#,##0").end()
 *                           .withSheet("Persons", Person.class)
 *                              .defaultHeaderStyle("header")
 *                              .withColumn("FirstName")
 *                                 .colMapper(Person::getFirstName)
 *                                 .footerTextValue("SUM age")
 *                                 .footerStyle("sum-header")
 *                                 .end()
 *                              .withColumn("LastName")
 *                                 .colMapper(Person::getLastName)
 *                                 .end()
 *                              .withColumn("Age")
 *                                 .colMapper(Person::getAge)
 *                                 .bodyStyle("age")
 *                                 .footerSum()
 *                                 .footerStyle("sum-age")
 *                                 .end()
 *                              .renderData(persons)
 *                              .autoSizeColumns()
 *                              .end()
 *                           .writeToBytes();
 * </pre>
 *
 * @author juerg
 */
public class ExcelWriter {

    public ExcelWriter(final Excel excel) {
        this.excel = excel;
    }


    public static ExcelWriter open(final byte[] document) {
        return new ExcelWriter(Excel.open(new ByteArrayInputStream(document)));
    }

    public static ExcelWriter open(final ByteBuffer document) {
        return new ExcelWriter(Excel.open(new ByteArrayInputStream(document.array())));
    }

    public static ExcelWriter open(final File file) {
        return new ExcelWriter(Excel.open(file));
    }

    public static ExcelWriter open(final InputStream is) {
        return new ExcelWriter(Excel.open(is));
    }


    public static ExcelWriter createXls() {
        return new ExcelWriter(Excel.createXls());
    }

    public static ExcelWriter createXlsx() {
        return new ExcelWriter(Excel.createXlsx());
    }


    public int getNumberOfSheets() {
        return excel.getNumberOfSheets();
    }

    public void evaluateAllFormulas() {
        excel.evaluateAllFormulas();
    }


    public ExcelFontBuilder withFont(final String name) {
        return new ExcelFontBuilder(this, excel, name);
    }

    public ExcelCellStyleBuilder withCellStyle(final String name) {
        return new ExcelCellStyleBuilder(this, excel, name);
    }

    public <T> ExcelSheetWriter<T> withSheet(final String name, final Class<T> type) {
        return new ExcelSheetWriter<T>(this, excel.createSheet(name));
    }


    public void write(final File file) {
        excel.write(file);
    }

    public void write(final OutputStream outputStream) {
        excel.write(outputStream);
    }

    public byte[] writeToBytes() {
        return excel.writeToBytes();
    }

    public ExcelWriter end() {
        excel.close();
        return this;
    }


    public <T> ExcelSheetWriter<T> getSheet(final String name) {
        return new ExcelSheetWriter<T>(this, excel.getSheet(name));
    }

    public <T> ExcelSheetWriter<T> getSheetAt(final int sheetIdx) {
        return new ExcelSheetWriter<T>(this, excel.getSheetAt(sheetIdx-1));
    }


    public Excel toExcel() {
        return excel;
    }

    private final Excel excel;
}
