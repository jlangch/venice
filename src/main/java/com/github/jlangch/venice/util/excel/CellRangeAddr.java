/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

public class CellRangeAddr {

    public CellRangeAddr(final int firstRow, final int lastRow, final int firstCol, final int lastCol) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstCol = firstCol;
        this.lastCol = lastCol;
    }


    public int getFirstRow() {
        return firstRow;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getFirstCol() {
        return firstCol;
    }

    public int getLastCol() {
        return lastCol;
    }


    public CellRangeAddr mapToZeroBased() {
        return new CellRangeAddr(firstRow-1, lastRow-1, firstCol-1, lastCol-1);
    }

    public CellRangeAddr mapToOneBased() {
        return new CellRangeAddr(firstRow+1, lastRow+1, firstCol+1, lastCol+1);
    }


    @Override
    public String toString() {
        return String.format("[[%d,%d],[%d,%d]]", firstRow, firstCol, lastRow, lastCol);
    }


    private final int firstRow;
    private final int lastRow;
    private final int firstCol;
    private final int lastCol;
}
