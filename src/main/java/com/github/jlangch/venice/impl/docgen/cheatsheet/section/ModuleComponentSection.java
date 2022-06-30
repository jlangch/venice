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


public class ModuleComponentSection implements ISectionBuilder {

    public ModuleComponentSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Component",
                                            "Managing lifecycle and dependencies of components",
                                            "modules.component");

        final DocSection all = new DocSection("(load-module :component)", id());
        section.addSection(all);

        final DocSection system = new DocSection("Build", id());
        all.addSection(system);
        system.addItem(diBuilder.getDocItem("component/system-map", false));
        system.addItem(diBuilder.getDocItem("component/system-using"));

        final DocSection protocol = new DocSection("Protocol", id());
        all.addSection(protocol);
        protocol.addItem(diBuilder.getDocItem("component/Component", false));


        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("component/deps"));
        util.addItem(diBuilder.getDocItem("component/dep"));
        util.addItem(diBuilder.getDocItem("component/id"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
