/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.excel.chart;

import com.github.jlangch.venice.util.excel.CellRangeAddr;


public class BarDataSeries {

    public BarDataSeries(
            final String title,
            final CellRangeAddr cellRangeAddr
    ) {
        this.title = title;
        this.cellRangeAddr = cellRangeAddr;
    }


    public String getTitle() {
        return title;
    }

    public CellRangeAddr getCellRangeAddr() {
        return cellRangeAddr;
    }


    public BarDataSeries mapToZeroBasedAddresses() {
        return new BarDataSeries(
                    title,
                    cellRangeAddr.mapToZeroBased());
    }

    public BarDataSeries mapToOneBasedAddresses() {
        return new BarDataSeries(
                    title,
                    cellRangeAddr.mapToOneBased());
    }



    private final String title;
    private final CellRangeAddr cellRangeAddr;
}
