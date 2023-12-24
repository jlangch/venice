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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class TransducersSection implements ISectionBuilder {

    public TransducersSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Transducers", "transducers");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection run = new DocSection("Use", "transducers.use");
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("transduce"));

        final DocSection func = new DocSection("Functions", "transducers.functions");
        all.addSection(func);
        func.addItem(diBuilder.getDocItem("map"));
        func.addItem(diBuilder.getDocItem("map-indexed"));
        func.addItem(diBuilder.getDocItem("filter"));
        func.addItem(diBuilder.getDocItem("drop"));
        func.addItem(diBuilder.getDocItem("drop-while"));
        func.addItem(diBuilder.getDocItem("drop-last"));
        func.addItem(diBuilder.getDocItem("take"));
        func.addItem(diBuilder.getDocItem("take-while"));
        func.addItem(diBuilder.getDocItem("take-last"));
        func.addItem(diBuilder.getDocItem("keep"));
        func.addItem(diBuilder.getDocItem("remove"));
        func.addItem(diBuilder.getDocItem("dedupe"));
        func.addItem(diBuilder.getDocItem("distinct"));
        func.addItem(diBuilder.getDocItem("sorted"));
        func.addItem(diBuilder.getDocItem("reverse"));
        func.addItem(diBuilder.getDocItem("flatten"));
        func.addItem(diBuilder.getDocItem("halt-when"));

        final DocSection red = new DocSection("Reductions", "transducers.reductions");
        all.addSection(red);
        red.addItem(diBuilder.getDocItem("rf-first"));
        red.addItem(diBuilder.getDocItem("rf-last"));
        red.addItem(diBuilder.getDocItem("rf-every?"));
        red.addItem(diBuilder.getDocItem("rf-any?"));

        final DocSection early = new DocSection("Early", "transducers.early");
        all.addSection(early);
        early.addItem(diBuilder.getDocItem("reduced"));
        early.addItem(diBuilder.getDocItem("reduced?"));
        early.addItem(diBuilder.getDocItem("deref"));
        early.addItem(diBuilder.getDocItem("deref?"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
