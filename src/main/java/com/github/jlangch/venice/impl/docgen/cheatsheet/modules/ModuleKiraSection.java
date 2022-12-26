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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleKiraSection implements ISectionBuilder {

    public ModuleKiraSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Kira",
                                            "Templating system",
                                            "modules.kira");

        final DocSection all = new DocSection("(load-module :kira)", id());
        section.addSection(all);

        final DocSection kira = new DocSection("Kira", id());
        all.addSection(kira);
        kira.addItem(diBuilder.getDocItem("kira/eval"));
        kira.addItem(diBuilder.getDocItem("kira/fn"));

        final DocSection escape = new DocSection("Escape", id());
        all.addSection(escape);
        escape.addItem(diBuilder.getDocItem("kira/escape-xml"));
        escape.addItem(diBuilder.getDocItem("kira/escape-html"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
