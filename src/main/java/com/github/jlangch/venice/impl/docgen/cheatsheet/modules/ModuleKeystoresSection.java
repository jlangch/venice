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


public class ModuleKeystoresSection implements ISectionBuilder {

    public ModuleKeystoresSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Java Keystore",
                                            "modules.keystores");

        final DocSection all = new DocSection("(load-module :keystores)", id());
        section.addSection(all);

        final DocSection load = new DocSection("Load", id());
        all.addSection(load);
        load.addItem(diBuilder.getDocItem("keystores/load", false));

        final DocSection certs = new DocSection("Certificates", id());
        all.addSection(certs);
        certs.addItem(diBuilder.getDocItem("keystores/aliases", false));
        certs.addItem(diBuilder.getDocItem("keystores/certificate", false));
        certs.addItem(diBuilder.getDocItem("keystores/subject-dn", false));
        certs.addItem(diBuilder.getDocItem("keystores/issuer-dn", false));
        certs.addItem(diBuilder.getDocItem("keystores/expiry-date", false));
        certs.addItem(diBuilder.getDocItem("keystores/expired", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
