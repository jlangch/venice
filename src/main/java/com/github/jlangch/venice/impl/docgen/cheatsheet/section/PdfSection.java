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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class PdfSection implements ISectionBuilder {

    public PdfSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final String footer = "Install the required PDF libraries:\n\n" +
                              "```                                           \n" +
                              "(do                                           \n" +
                              "  (load-module :pdf-install)                  \n" +
                              "  (pdf-install/install :dir (repl/libs-dir))  \n" +
                              "                       :silent false))        \n" +
                              "```\n";


        final DocSection section = new DocSection("PDF", null, "pdf", null, footer);


        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection pdf = new DocSection("PDF", "pdf.pdf");
        all.addSection(pdf);
        pdf.addItem(diBuilder.getDocItem("pdf/render", false));
        pdf.addItem(diBuilder.getDocItem("pdf/text-to-pdf", false));
        pdf.addItem(diBuilder.getDocItem("pdf/available?", false));
        pdf.addItem(diBuilder.getDocItem("pdf/check-required-libs", false));

        final DocSection tools = new DocSection("PDF Tools", "pdf.pdftools");
        all.addSection(tools);
        tools.addItem(diBuilder.getDocItem("pdf/merge", false));
        tools.addItem(diBuilder.getDocItem("pdf/copy", false));
        tools.addItem(diBuilder.getDocItem("pdf/pages"));
        tools.addItem(diBuilder.getDocItem("pdf/watermark", false));
        tools.addItem(diBuilder.getDocItem("pdf/to-text", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
