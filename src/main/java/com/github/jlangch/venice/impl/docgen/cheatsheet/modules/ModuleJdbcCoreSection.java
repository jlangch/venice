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

        final DocSection meta = new DocSection("Meta Data", id());
        all.addSection(meta);
        meta.addItem(diBuilder.getDocItem("jdbc-core/meta-data", false));
        meta.addItem(diBuilder.getDocItem("jdbc-core/features", false));
        meta.addItem(diBuilder.getDocItem("jdbc-core/schemas", false));
        meta.addItem(diBuilder.getDocItem("jdbc-core/tables", false));
        meta.addItem(diBuilder.getDocItem("jdbc-core/columns", false));

        final DocSection conn = new DocSection("Connection", id());
        all.addSection(conn);
        conn.addItem(diBuilder.getDocItem("jdbc-core/closed?", false));

        final DocSection tpl = new DocSection("Templates", id());
        all.addSection(tpl);
        tpl.addItem(diBuilder.getDocItem("jdbc-core/with-conn", false));
        tpl.addItem(diBuilder.getDocItem("jdbc-core/with-tx", false));

        final DocSection tx = new DocSection("TX", id());
        all.addSection(tx);
        tx.addItem(diBuilder.getDocItem("jdbc-core/auto-commit?", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/auto-commit!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/commit!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/rollback!", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/tx-isolation", false));
        tx.addItem(diBuilder.getDocItem("jdbc-core/tx-isolation!", false));

        final DocSection stmt = new DocSection("Statements", id());
        all.addSection(stmt);
        stmt.addItem(diBuilder.getDocItem("jdbc-core/create-statement", false));
        stmt.addItem(diBuilder.getDocItem("jdbc-core/prepare-statement", false));

        final DocSection exec = new DocSection("Execute", id());
        all.addSection(exec);
        exec.addItem(diBuilder.getDocItem("jdbc-core/execute", false));
        exec.addItem(diBuilder.getDocItem("jdbc-core/execute-query", false));
        exec.addItem(diBuilder.getDocItem("jdbc-core/execute-query*", false));
        exec.addItem(diBuilder.getDocItem("jdbc-core/execute-update", false));
        exec.addItem(diBuilder.getDocItem("jdbc-core/generated-keys", false));
        exec.addItem(diBuilder.getDocItem("jdbc-core/count-rows", false));

        final DocSection ps = new DocSection("Prepared Stmt", id());
        all.addSection(ps);
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-clear-parameters", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-string", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-boolean", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-int", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-long", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-float", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-double", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-decimal", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-date", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-timestamp", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-clob", false));
        ps.addItem(diBuilder.getDocItem("jdbc-core/ps-blob", false));

        final DocSection rs = new DocSection("Result Set", id());
        all.addSection(rs);
        rs.addItem(diBuilder.getDocItem("jdbc-core/rs-first!", false));
        rs.addItem(diBuilder.getDocItem("jdbc-core/rs-next!", false));
        rs.addItem(diBuilder.getDocItem("jdbc-core/rs-last!", false));
        rs.addItem(diBuilder.getDocItem("jdbc-core/collect-result-set", false));
        rs.addItem(diBuilder.getDocItem("jdbc-core/render-query-result", false));
        rs.addItem(diBuilder.getDocItem("jdbc-core/print-query-result", false));

        final DocSection rsv = new DocSection("Result Set Data", id());
        all.addSection(rsv);
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-string", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-boolean", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-int", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-long", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-float", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-double", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-decimal", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-date", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-timestamp", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-clob", false));
        rsv.addItem(diBuilder.getDocItem("jdbc-core/rs-blob", false));

        final DocSection clob = new DocSection("Clob", id());
        all.addSection(clob);
        clob.addItem(diBuilder.getDocItem("jdbc-core/clob?", false));
        clob.addItem(diBuilder.getDocItem("jdbc-core/clob-length", false));
        clob.addItem(diBuilder.getDocItem("jdbc-core/clob-reader", false));
        clob.addItem(diBuilder.getDocItem("jdbc-core/clob-free", false));

        final DocSection blob = new DocSection("Blob", id());
        all.addSection(blob);
        blob.addItem(diBuilder.getDocItem("jdbc-core/blob?", false));
        blob.addItem(diBuilder.getDocItem("jdbc-core/blob-length", false));
        blob.addItem(diBuilder.getDocItem("jdbc-core/blob-in-stream", false));
        blob.addItem(diBuilder.getDocItem("jdbc-core/blob-bytebuf", false));
        blob.addItem(diBuilder.getDocItem("jdbc-core/blob-free", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
