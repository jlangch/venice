/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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


public class ModuleTestSection implements ISectionBuilder {

    public ModuleTestSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Test",
                                            "modules.test");

        final DocSection all = new DocSection("(load-module :test)", id());
        section.addSection(all);

        final DocSection def = new DocSection("Define", id());
        all.addSection(def);
        def.addItem(diBuilder.getDocItem("test/deftest"));

        final DocSection fixtures = new DocSection("Fixture", id());
        all.addSection(fixtures);
        fixtures.addItem(diBuilder.getDocItem("test/use-fixtures"));

        final DocSection run = new DocSection("Run", id());
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("test/run-tests"));
        run.addItem(diBuilder.getDocItem("test/run-test-var"));
        run.addItem(diBuilder.getDocItem("test/successful?"));

        final DocSection asserts = new DocSection("Assert", "macros.assert");
        all.addSection(asserts);
        asserts.addItem(diBuilder.getDocItem("assert", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-false", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-eq", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-ne", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-throws", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-does-not-throw", true, true));
        asserts.addItem(diBuilder.getDocItem("assert-throws-with-msg", true, true));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
