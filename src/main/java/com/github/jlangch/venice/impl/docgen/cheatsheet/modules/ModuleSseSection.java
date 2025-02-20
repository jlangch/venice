/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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


public class ModuleSseSection implements ISectionBuilder {

    public ModuleSseSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("SSE", "Server Side Events", "modules.sse");

        final DocSection all = new DocSection("(load-module :server-side-events)", id());
        section.addSection(all);

        final DocSection servlet = new DocSection("Render/Parse", id());
        all.addSection(servlet);
        servlet.addItem(diBuilder.getDocItem("server-side-events/render"));
        servlet.addItem(diBuilder.getDocItem("server-side-events/parse"));

        final DocSection read = new DocSection("Read", id());
        all.addSection(read);
        read.addItem(diBuilder.getDocItem("server-side-events/read-event"));
        read.addItem(diBuilder.getDocItem("server-side-events/read-events"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
