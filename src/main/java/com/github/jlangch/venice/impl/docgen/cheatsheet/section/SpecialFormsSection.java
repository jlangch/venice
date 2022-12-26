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


public class SpecialFormsSection implements ISectionBuilder {

    public SpecialFormsSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Special Forms", "specialforms");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection generic = new DocSection("Forms", "specialforms.forms");
        all.addSection(generic);

        generic.addItem(diBuilder.getDocItem("def"));
        generic.addItem(diBuilder.getDocItem("defonce"));
        generic.addItem(diBuilder.getDocItem("def-dynamic"));
        generic.addItem(diBuilder.getDocItem("if"));
        generic.addItem(diBuilder.getDocItem("do"));
        generic.addItem(diBuilder.getDocItem("let"));
        generic.addItem(diBuilder.getDocItem("binding"));
        generic.addItem(diBuilder.getDocItem("fn"));
        generic.addItem(diBuilder.getDocItem("set!"));

        final DocSection multi = new DocSection("Multi Methods", "specialforms.multimethod");
        all.addSection(multi);
        multi.addItem(diBuilder.getDocItem("defmulti"));
        multi.addItem(diBuilder.getDocItem("defmethod"));

        final DocSection proto = new DocSection("Protocols", "specialforms.protocol");
        all.addSection(proto);
        proto.addItem(diBuilder.getDocItem("defprotocol"));
        proto.addItem(diBuilder.getDocItem("extend"));
        proto.addItem(diBuilder.getDocItem("extends?"));

        final DocSection recur = new DocSection("Recursion", "specialforms.recursion");
        all.addSection(recur);
        recur.addItem(diBuilder.getDocItem("loop"));
        recur.addItem(diBuilder.getDocItem("recur"));
        recur.addItem(diBuilder.getDocItem("tail-pos", true, true));

        final DocSection ex = new DocSection("Exception", "specialforms.exception");
        all.addSection(ex);
        ex.addItem(diBuilder.getDocItem("throw", true, true));
        ex.addItem(diBuilder.getDocItem("try", true, true));
        ex.addItem(diBuilder.getDocItem("try-with", true, true));

        final DocSection profiling = new DocSection("Profiling", "specialforms.profiling");
        all.addSection(profiling);

        profiling.addItem(diBuilder.getDocItem("dobench"));
        profiling.addItem(diBuilder.getDocItem("dorun"));
        profiling.addItem(diBuilder.getDocItem("prof"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
