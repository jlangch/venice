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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleJdbcCoreSection implements ISectionBuilder {

    public ModuleJdbcCoreSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "JDBC Core",
                                        "modules.jdbc-core");

        final DocSection all = new DocSection("(load-module :jdbc-core)", id());
        section.addSection(all);

        final DocSection db = new DocSection("Create/Drop", id());
        all.addSection(db);
        db.addItem(diBuilder.getDocItem("jdbc-core/create-database", false));
        db.addItem(diBuilder.getDocItem("jdbc-core/drop-database", false));

        final DocSection provider = new DocSection("Provider", id());
        all.addSection(provider);
        provider.addItem(diBuilder.getDocItem("jdbc-core/postgresql?", false));

        final DocSection conn = new DocSection("Connection", id());
        all.addSection(conn);
        conn.addItem(diBuilder.getDocItem("jdbc-core/closed?", false));

        final DocSection tx = new DocSection("TX", id());
        all.addSection(tx);
        tx.addItem(diBuilder.getDocItem("jdbc-core/auto-commit?", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/auto-commit!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/commit!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/rollback!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/tx-isolation", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/tx-isolation!", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
