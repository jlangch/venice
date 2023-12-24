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


public class ModuleMatrixSection implements ISectionBuilder {

    public ModuleMatrixSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Matrix",
                                            "Simple matrix functions. To process large "
                                              + "matrices use the \"Efficient Java Matrix Library\" "
                                              + "(EJML) http://ejml.org/wiki/) instead.",
                                            "modules.matrix");

        final DocSection all = new DocSection("(load-module :matrix)", id());
        section.addSection(all);

        final DocSection main = new DocSection("Matrix", id());
        all.addSection(main);
        main.addItem(diBuilder.getDocItem("matrix/validate"));
        main.addItem(diBuilder.getDocItem("matrix/vector2d"));
        main.addItem(diBuilder.getDocItem("matrix/empty?"));
        main.addItem(diBuilder.getDocItem("matrix/rows"));
        main.addItem(diBuilder.getDocItem("matrix/columns"));
        main.addItem(diBuilder.getDocItem("matrix/row"));
        main.addItem(diBuilder.getDocItem("matrix/column"));

        final DocSection form = new DocSection("Format", id());
        all.addSection(form);
        form.addItem(diBuilder.getDocItem("matrix/format"));

        final DocSection ele = new DocSection("Elements", id());
        all.addSection(ele);
        ele.addItem(diBuilder.getDocItem("matrix/element"));
        ele.addItem(diBuilder.getDocItem("matrix/assoc-element"));

        final DocSection add = new DocSection("Add", id());
        all.addSection(add);
        add.addItem(diBuilder.getDocItem("matrix/add-column-at-start"));
        add.addItem(diBuilder.getDocItem("matrix/add-column-at-end"));
        add.addItem(diBuilder.getDocItem("matrix/add-row-at-start"));
        add.addItem(diBuilder.getDocItem("matrix/add-row-at-end"));

        final DocSection remove = new DocSection("Remove", id());
        all.addSection(remove);
        remove.addItem(diBuilder.getDocItem("matrix/remove-column"));
        remove.addItem(diBuilder.getDocItem("matrix/remove-row"));

        final DocSection linalg = new DocSection("LinAlg", id());
        all.addSection(linalg);
        linalg.addItem(diBuilder.getDocItem("matrix/transpose"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
