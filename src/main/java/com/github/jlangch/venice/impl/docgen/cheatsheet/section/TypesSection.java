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


public class TypesSection implements ISectionBuilder {

    public TypesSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Types", "types");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection util = new DocSection("Util", "types.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("type"));
        util.addItem(diBuilder.getDocItem("supertype"));
        util.addItem(diBuilder.getDocItem("supertypes"));

        final DocSection test = new DocSection("Test", "types.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("instance-of?"));
        test.addItem(diBuilder.getDocItem("deftype?"));

        final DocSection define = new DocSection("Define", "types.define");
        all.addSection(define);
        define.addItem(diBuilder.getDocItem("deftype"));
        define.addItem(diBuilder.getDocItem("deftype-of"));
        define.addItem(diBuilder.getDocItem("deftype-or"));

        final DocSection create = new DocSection("Create", "types.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem(".:"));

        final DocSection describe = new DocSection("Describe", "types.describe");
        all.addSection(describe);
        describe.addItem(diBuilder.getDocItem("deftype-describe"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
