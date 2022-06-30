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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class LazySequencesSection implements ISectionBuilder {

    public LazySequencesSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Lazy Sequences", "lazyseq");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "lazyseq.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("lazy-seq"));

        final DocSection realize = new DocSection("Realize", "lazyseq.realize");
        all.addSection(realize);
        realize.addItem(diBuilder.getDocItem("doall"));

        final DocSection test = new DocSection("Test", "lazyseq.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("lazy-seq?"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
