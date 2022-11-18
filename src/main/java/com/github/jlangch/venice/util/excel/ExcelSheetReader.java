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

import java.time.LocalDateTime;

import com.github.jlangch.venice.impl.util.excel.ExcelSheet;


/**
 * An Excel sheet reader
 *
 * @author juerg
 */
public class ExcelSheetReader {

    public ExcelSheetReader(final ExcelSheet sheet) {
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

    public String getCellAddress(final int row1, final int col1) {
        return sheet.getCellAddress(row1-1, col1-1);
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

    public String getFormula(final int row1, final int col1) {
        return sheet.getFormula(row1-1, col1-1);
    }

    public void evaluateAllFormulas() {
        sheet.evaluateAllFormulas();
    }



    private final ExcelSheet sheet;
}
