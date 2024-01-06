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


public class ModuleMavenSection implements ISectionBuilder {

    public ModuleMavenSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Maven",
                                            "modules.maven");

        final DocSection all = new DocSection("(load-module :maven)", id());
        section.addSection(all);

        final DocSection maven = new DocSection("Artifacts", id());
        all.addSection(maven);
        maven.addItem(diBuilder.getDocItem("maven/download", false));
        maven.addItem(diBuilder.getDocItem("maven/get", false));
        maven.addItem(diBuilder.getDocItem("maven/uri", false));
        maven.addItem(diBuilder.getDocItem("maven/parse-artifact", false));

        final DocSection cmds = new DocSection("Commands", id());
        all.addSection(cmds);
        cmds.addItem(diBuilder.getDocItem("maven/home-dir", false));
        cmds.addItem(diBuilder.getDocItem("maven/mvn", false));
        cmds.addItem(diBuilder.getDocItem("maven/version", false));
        cmds.addItem(diBuilder.getDocItem("maven/dependencies", false));

        final DocSection install = new DocSection("Install", id());
        all.addSection(install);
        install.addItem(diBuilder.getDocItem("maven/install", false));
        install.addItem(diBuilder.getDocItem("maven/uninstall", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
