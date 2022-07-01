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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleAnsiSection implements ISectionBuilder {

    public ModuleAnsiSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Ansi",
                                            "ANSI codes, styles, and colorization helper functions",
                                            "modules.ansi");

        final DocSection all = new DocSection("(load-module :ansi)", id());
        section.addSection(all);

        final DocSection colors = new DocSection("Colors", id());
        all.addSection(colors);
        colors.addItem(diBuilder.getDocItem("ansi/fg-color", false));
        colors.addItem(diBuilder.getDocItem("ansi/bg-color", false));

        final DocSection style = new DocSection("Styles", id());
        all.addSection(style);
        style.addItem(diBuilder.getDocItem("ansi/style", false));
        style.addItem(diBuilder.getDocItem("ansi/ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/with-ansi", false));
        style.addItem(diBuilder.getDocItem("ansi/without-ansi", false));

        final DocSection cursor = new DocSection("Cursor", id());
        all.addSection(cursor);
        cursor.addItem(diBuilder.getDocItem("ansi/without-cursor", false));

        final DocSection progress = new DocSection("Progress", id());
        all.addSection(progress);
        progress.addItem(diBuilder.getDocItem("ansi/progress", false));
        progress.addItem(diBuilder.getDocItem("ansi/progress-bar", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
