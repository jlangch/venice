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


public class ModuleAvironSection implements ISectionBuilder {

    public ModuleAvironSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "ClamAV",
                                            "ClamAV client.",
                                            "modules.aviron");

        final DocSection all = new DocSection("(load-module :aviron)", id());
        section.addSection(all);

        final DocSection main = new DocSection("Client", id());
        all.addSection(main);
        main.addItem(diBuilder.getDocItem("aviron/create-client", false));

        final DocSection scan = new DocSection("Scan", id());
        all.addSection(scan);
        scan.addItem(diBuilder.getDocItem("aviron/scan-stream", false));
        scan.addItem(diBuilder.getDocItem("aviron/scan-path", false));
        scan.addItem(diBuilder.getDocItem("aviron/scan-parallel", false));

        final DocSection result = new DocSection("Scan Result", id());
        all.addSection(result);
        result.addItem(diBuilder.getDocItem("aviron/ok?", false));
        result.addItem(diBuilder.getDocItem("aviron/virus?", false));
        result.addItem(diBuilder.getDocItem("aviron/viruses", false));

        final DocSection admin = new DocSection("Admin", id());
        all.addSection(admin);
        admin.addItem(diBuilder.getDocItem("aviron/ping", false));
        admin.addItem(diBuilder.getDocItem("aviron/reachable?", false));
        admin.addItem(diBuilder.getDocItem("aviron/shutdown-server", false));
        admin.addItem(diBuilder.getDocItem("aviron/version", false));
        admin.addItem(diBuilder.getDocItem("aviron/stats", false));
        admin.addItem(diBuilder.getDocItem("aviron/reload-virus-databases", false));

        final DocSection debug = new DocSection("Admin", id());
        all.addSection(debug);
        debug.addItem(diBuilder.getDocItem("aviron/last-command-run-details", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
