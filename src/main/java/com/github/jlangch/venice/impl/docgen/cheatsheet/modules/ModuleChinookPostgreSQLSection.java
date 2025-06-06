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


public class ModuleChinookPostgreSQLSection implements ISectionBuilder {

    public ModuleChinookPostgreSQLSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "Chinook Dataset",
                                        "Chinook dataset for PostgreSQL",
                                        "modules.chinook-postgresql");

        final DocSection all = new DocSection("(load-module :chinook-postgresql)", id());
        section.addSection(all);

        final DocSection model = new DocSection("Data Model", id());
        all.addSection(model);
        model.addItem(diBuilder.getDocItem("chinook-postgresql/show-data-model", false));

        final DocSection data = new DocSection("Data", id());
        all.addSection(data);
        data.addItem(diBuilder.getDocItem("chinook-postgresql/show-data", false));

        final DocSection load = new DocSection("Load Data", id());
        all.addSection(load);
        load.addItem(diBuilder.getDocItem("chinook-postgresql/load-data", false));

        final DocSection download = new DocSection("Download Data", id());
        all.addSection(download);
        download.addItem(diBuilder.getDocItem("chinook-postgresql/download-data", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
