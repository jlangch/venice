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


public class NamespaceSection implements ISectionBuilder {

    public NamespaceSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Namespace", "namespace");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection open = new DocSection("Open", "namespace.open");
        all.addSection(open);
        open.addItem(diBuilder.getDocItem("ns"));

        final DocSection curr = new DocSection("Current", "namespace.current");
        all.addSection(curr);
        curr.addItem(diBuilder.getDocItem("*ns*"));

        final DocSection remove = new DocSection("Remove", "namespace.remove");
        all.addSection(remove);
        remove.addItem(diBuilder.getDocItem("ns-unmap"));
        remove.addItem(diBuilder.getDocItem("ns-remove"));

        final DocSection test = new DocSection("Test", "namespace.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("ns?"));

        final DocSection util = new DocSection("Util", "namespace.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ns-list"));
        util.addItem(diBuilder.getDocItem("namespace"));

        final DocSection alias = new DocSection("Alias", "namespace.alias");
        all.addSection(alias);
        alias.addItem(diBuilder.getDocItem("ns-alias"));
        alias.addItem(diBuilder.getDocItem("ns-aliases"));
        alias.addItem(diBuilder.getDocItem("ns-unalias"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
