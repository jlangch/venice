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

        final DocSection section = new DocSection(
                                        "Excel",
                                        "Read/Write Excel files",
                                        "modules.excel",
                                        "Venice is compatible with Apache POI 5.x.\n\n" +
                                        "To use charts with Excel documents Apache POI 5.2.0 or " +
                                        "newer is required.",
                                        footer);

        final DocSection all = new DocSection("(load-module :excel)", id());
        section.addSection(all);

        final DocSection op = new DocSection("Create/Open", id());
        all.addSection(op);
        op.addItem(diBuilder.getDocItem("excel/create", false));
        op.addItem(diBuilder.getDocItem("excel/open", false));

        final DocSection sv = new DocSection("Save", id());
        all.addSection(sv);
        sv.addItem(diBuilder.getDocItem("excel/write->file", false));
        sv.addItem(diBuilder.getDocItem("excel/write->stream", false));
        sv.addItem(diBuilder.getDocItem("excel/write->bytebuf", false));

        final DocSection sheet = new DocSection("Sheet", id());
        all.addSection(sheet);
        sheet.addItem(diBuilder.getDocItem("excel/sheet", false));
        sheet.addItem(diBuilder.getDocItem("excel/sheet-count", false));
        sheet.addItem(diBuilder.getDocItem("excel/sheet-name", false));
        sheet.addItem(diBuilder.getDocItem("excel/sheet-index", false));
        sheet.addItem(diBuilder.getDocItem("excel/sheet-row-range", false));
        sheet.addItem(diBuilder.getDocItem("excel/sheet-col-range", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-sheet", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-column", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-merge-region", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-conditional-bg-color", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-conditional-font-color", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-conditional-border", false));
        sheet.addItem(diBuilder.getDocItem("excel/add-text-data-validation", false));
        sheet.addItem(diBuilder.getDocItem("excel/freeze-pane", false));
        sheet.addItem(diBuilder.getDocItem("excel/auto-size-columns", false));
        sheet.addItem(diBuilder.getDocItem("excel/auto-size-column", false));
        sheet.addItem(diBuilder.getDocItem("excel/hide-columns", false));
        sheet.addItem(diBuilder.getDocItem("excel/protect-sheet", false));

        final DocSection layout = new DocSection("Sheet Layout", id());
        layout.addItem(diBuilder.getDocItem("excel/print-layout", false));
        layout.addItem(diBuilder.getDocItem("excel/page-margins", false));
        layout.addItem(diBuilder.getDocItem("excel/header-margin", false));
        layout.addItem(diBuilder.getDocItem("excel/footer-margin", false));
        layout.addItem(diBuilder.getDocItem("excel/header", false));
        layout.addItem(diBuilder.getDocItem("excel/footer", false));
        layout.addItem(diBuilder.getDocItem("excel/display-grid-lines", false));
        all.addSection(layout);

        final DocSection cells = new DocSection("Cells", id());
        all.addSection(cells);
        cells.addItem(diBuilder.getDocItem("excel/cell-empty?", false));
        cells.addItem(diBuilder.getDocItem("excel/cell-lock", false));
        cells.addItem(diBuilder.getDocItem("excel/cell-locked?", false));
        cells.addItem(diBuilder.getDocItem("excel/cell-hidden?", false));
        cells.addItem(diBuilder.getDocItem("excel/cell-type", false));
        cells.addItem(diBuilder.getDocItem("excel/cell-data-format-string", false));
        cells.addItem(diBuilder.getDocItem("excel/copy-cell-style", false));
        cells.addItem(diBuilder.getDocItem("excel/addr->string", false));

        final DocSection rows = new DocSection("Rows", id());
        all.addSection(rows);
        rows.addItem(diBuilder.getDocItem("excel/row-height", false));
        rows.addItem(diBuilder.getDocItem("excel/clear-row", false));
        rows.addItem(diBuilder.getDocItem("excel/delete-row", false));
        rows.addItem(diBuilder.getDocItem("excel/copy-row", false));
        rows.addItem(diBuilder.getDocItem("excel/copy-row-to-end", false));
        rows.addItem(diBuilder.getDocItem("excel/insert-empty-row", false));

        final DocSection cols = new DocSection("Cols", id());
        all.addSection(cols);
        cols.addItem(diBuilder.getDocItem("excel/col->string", false));
        cols.addItem(diBuilder.getDocItem("excel/col-hidden?", false));
        cols.addItem(diBuilder.getDocItem("excel/col-width", false));

        final DocSection wr_data = new DocSection("Write Cells", id());
        all.addSection(wr_data);
        wr_data.addItem(diBuilder.getDocItem("excel/write-data", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-items", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-item", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-value", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-values", false));
        wr_data.addItem(diBuilder.getDocItem("excel/write-values-keep-style", false));

        final DocSection rd_data = new DocSection("Read Cells", id());
        all.addSection(rd_data);
        rd_data.addItem(diBuilder.getDocItem("excel/read-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-string-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-boolean-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-long-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-double-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-date-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-datetime-val", false));
        rd_data.addItem(diBuilder.getDocItem("excel/read-error-code", false));

        final DocSection formulas = new DocSection("Formulas", id());
        all.addSection(formulas);
        formulas.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        formulas.addItem(diBuilder.getDocItem("excel/evaluate-formula", false));
        formulas.addItem(diBuilder.getDocItem("excel/cell-formula-result-type", false));
        formulas.addItem(diBuilder.getDocItem("excel/cell-formula", false));
        formulas.addItem(diBuilder.getDocItem("excel/sum-formula", false));
        formulas.addItem(diBuilder.getDocItem("excel/evaluate-formulas", false));
        formulas.addItem(diBuilder.getDocItem("excel/remove-formula", false));

        final DocSection style = new DocSection("Styles", id());
        all.addSection(style);
        style.addItem(diBuilder.getDocItem("excel/add-font", false));
        style.addItem(diBuilder.getDocItem("excel/add-style", false));
        style.addItem(diBuilder.getDocItem("excel/add-merge-region", false));
        style.addItem(diBuilder.getDocItem("excel/add-conditional-bg-color", false));
        style.addItem(diBuilder.getDocItem("excel/add-conditional-font-color", false));
        style.addItem(diBuilder.getDocItem("excel/add-conditional-border", false));
        style.addItem(diBuilder.getDocItem("excel/row-height", false));
        style.addItem(diBuilder.getDocItem("excel/col-width", false));
        style.addItem(diBuilder.getDocItem("excel/cell-style", false));
        style.addItem(diBuilder.getDocItem("excel/cell-style-info", false));
        style.addItem(diBuilder.getDocItem("excel/bg-color", false));

        final DocSection image = new DocSection("Images", id());
        all.addSection(image);
        image.addItem(diBuilder.getDocItem("excel/add-image", false));

        final DocSection comments = new DocSection("Comments", id());
        all.addSection(comments);
        comments.addItem(diBuilder.getDocItem("excel/remove-comment", false));

        final DocSection hyperlink = new DocSection("Hyperlinks", id());
        all.addSection(hyperlink);
        hyperlink.addItem(diBuilder.getDocItem("excel/add-url-hyperlink", false));
        hyperlink.addItem(diBuilder.getDocItem("excel/add-email-hyperlink", false));
        hyperlink.addItem(diBuilder.getDocItem("excel/remove-hyperlink", false));

        final DocSection charts = new DocSection("Charts", id());
        all.addSection(charts);
        charts.addItem(diBuilder.getDocItem("excel/add-line-chart", false));
        charts.addItem(diBuilder.getDocItem("excel/add-bar-chart", false));
        charts.addItem(diBuilder.getDocItem("excel/add-area-chart", false));
        charts.addItem(diBuilder.getDocItem("excel/add-pie-chart", false));

        final DocSection charts_util = new DocSection("Charts Util", id());
        all.addSection(charts_util);
        charts_util.addItem(diBuilder.getDocItem("excel/line-data-series", false));
        charts_util.addItem(diBuilder.getDocItem("excel/bar-data-series", false));
        charts_util.addItem(diBuilder.getDocItem("excel/area-data-series", false));
        charts_util.addItem(diBuilder.getDocItem("excel/pie-data-series", false));
        charts_util.addItem(diBuilder.getDocItem("excel/cell-address-range", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
