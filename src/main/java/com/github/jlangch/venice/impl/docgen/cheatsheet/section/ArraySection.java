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


public class ArraySection implements ISectionBuilder {

    public ArraySection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Arrays", "arrays");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "arrays.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("make-array"));
        create.addItem(diBuilder.getDocItem("object-array"));
        create.addItem(diBuilder.getDocItem("string-array"));
        create.addItem(diBuilder.getDocItem("int-array"));
        create.addItem(diBuilder.getDocItem("long-array"));
        create.addItem(diBuilder.getDocItem("float-array"));
        create.addItem(diBuilder.getDocItem("double-array"));

        final DocSection use = new DocSection("Use", "arrays.use");
        all.addSection(use);
        use.addItem(diBuilder.getDocItem("aget"));
        use.addItem(diBuilder.getDocItem("aset"));
        use.addItem(diBuilder.getDocItem("alength"));
        use.addItem(diBuilder.getDocItem("asub"));
        use.addItem(diBuilder.getDocItem("acopy"));
        use.addItem(diBuilder.getDocItem("amap"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
