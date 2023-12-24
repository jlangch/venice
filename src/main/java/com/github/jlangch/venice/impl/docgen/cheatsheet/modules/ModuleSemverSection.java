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


public class ModuleSemverSection implements ISectionBuilder {

    public ModuleSemverSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Semver",
                                            "Semantic versioning",
                                            "modules.semver");

        final DocSection all = new DocSection("(load-module :semver)", id());
        section.addSection(all);

        final DocSection semver = new DocSection("Semver", id());
        all.addSection(semver);
        semver.addItem(diBuilder.getDocItem("semver/parse"));
        semver.addItem(diBuilder.getDocItem("semver/version"));

        final DocSection valid = new DocSection("Validation", id());
        all.addSection(valid);
        valid.addItem(diBuilder.getDocItem("semver/valid?"));
        valid.addItem(diBuilder.getDocItem("semver/valid-format?"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("semver/newer?"));
        test.addItem(diBuilder.getDocItem("semver/older?"));
        test.addItem(diBuilder.getDocItem("semver/equal?"));
        test.addItem(diBuilder.getDocItem("semver/cmp"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
