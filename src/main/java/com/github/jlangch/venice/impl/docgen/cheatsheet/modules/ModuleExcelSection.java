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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleExcelSection implements ISectionBuilder {

    public ModuleExcelSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final String footer = "Install the required Apache POI 5.x libraries:\n\n" +
                              "```                                           \n" +
                              "(do                                           \n" +
                              "  (load-module :excel-install)                \n" +
                              "  (excel-install/install :dir (repl/libs-dir) \n" +
                              "                         :silent false))      \n" +
                              "```\n";

        final DocSection section = new DocSection("Excel", "Read/Write Excel files", "modules.excel", null, footer);

        final DocSection all = new DocSection("(load-module :excel)", id());
        section.addSection(all);

        final DocSection wr = new DocSection("Writer", id());
        all.addSection(wr);
        wr.addItem(diBuilder.getDocItem("excel/writer", false));
        wr.addItem(diBuilder.getDocItem("excel/add-sheet", false));
        wr.addItem(diBuilder.getDocItem("excel/add-column", false));
        wr.addItem(diBuilder.getDocItem("excel/add-merge-region", false));

        final DocSection wr_data = new DocSection("Writer Data", id());
        all.addSection(wr_data);
        wr_data.addItem(diBuilder.getDocItem("excel/write-data", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-items", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-item", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-value", false));

        final DocSection wr_io = new DocSection("Writer I/O", id());
        all.addSection(wr_io);
        wr_io.addItem(diBuilder.getDocItem("excel/write->file", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->stream", false));
        wr_io.addItem(diBuilder.getDocItem("excel/write->bytebuf", false));

        final DocSection wr_util = new DocSection("Writer Util", id());
        all.addSection(wr_util);
        wr_util.addItem(diBuilder.getDocItem("excel/sheet-count", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sheet-name", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sheet-index", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sheet-row-range", false));
        wr_util.addItem(diBuilder.getDocItem("excel/sheet-col-range", false));
        wr_util.addItem(diBuilder.getDocItem("excel/convert->reader", false));
        wr_util.addItem(diBuilder.getDocItem("excel/col->string", false));
        wr_util.addItem(diBuilder.getDocItem("excel/addr->string", false));

        final DocSection wr_fromula = new DocSection("Writer Formulas", id());
        all.addSection(wr_fromula);
        wr_fromula.addItem(diBuilder.getDocItem("excel/cell-formula", false));
        wr_fromula.addItem(diBuilder.getDocItem("excel/sum-formula", false));
        wr_fromula.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));

        final DocSection wr_style = new DocSection("Writer Styling", id());
        all.addSection(wr_style);
        wr_style.addItem(diBuilder.getDocItem("excel/add-font", false));
        wr_style.addItem(diBuilder.getDocItem("excel/add-style", false));
        wr_style.addItem(diBuilder.getDocItem("excel/row-height", false));
        wr_style.addItem(diBuilder.getDocItem("excel/column-width", false));
        wr_style.addItem(diBuilder.getDocItem("excel/cell-style", false));
        wr_style.addItem(diBuilder.getDocItem("excel/bg-color", false));
        wr_style.addItem(diBuilder.getDocItem("excel/auto-size-columns", false));
        wr_style.addItem(diBuilder.getDocItem("excel/auto-size-column", false));

        final DocSection rd = new DocSection("Reader", id());
        all.addSection(rd);
        rd.addItem(diBuilder.getDocItem("excel/open", false));
        rd.addItem(diBuilder.getDocItem("excel/sheet", false));
        rd.addItem(diBuilder.getDocItem("excel/read-string-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-boolean-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-long-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-double-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-date-val", false));
        rd.addItem(diBuilder.getDocItem("excel/read-datetime-val", false));

        final DocSection rd_util = new DocSection("Reader Util", id());
        all.addSection(rd_util);
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-count", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-name", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-index", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-row-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/sheet-col-range", false));
        rd_util.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-empty?", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-type", false));
        rd_util.addItem(diBuilder.getDocItem("excel/cell-formula-result-type", false));
        rd_util.addItem(diBuilder.getDocItem("excel/convert->writer", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
