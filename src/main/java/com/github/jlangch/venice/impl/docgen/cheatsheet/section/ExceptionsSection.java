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


public class ExceptionsSection implements ISectionBuilder {

    public ExceptionsSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Exceptions", "exceptions");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection block = new DocSection("Throw/Catch", "exceptions.block");
        all.addSection(block);
        block.addItem(diBuilder.getDocItem("try", true, true));
        block.addItem(diBuilder.getDocItem("try-with", true, true));
        block.addItem(diBuilder.getDocItem("throw", true, true));

        final DocSection create = new DocSection("Create", "exceptions.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("ex"));

        final DocSection test = new DocSection("Test", "exceptions.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("ex?"));
        test.addItem(diBuilder.getDocItem("ex-venice?"));

        final DocSection util = new DocSection("Util", "exceptions.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ex-message"));
        util.addItem(diBuilder.getDocItem("ex-cause"));
        util.addItem(diBuilder.getDocItem("ex-value"));

        final DocSection stacktrace = new DocSection("Stacktrace", "exceptions.stacktrace");
        all.addSection(stacktrace);
        stacktrace.addItem(diBuilder.getDocItem("ex-venice-stacktrace"));
        stacktrace.addItem(diBuilder.getDocItem("ex-java-stacktrace", false, true));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
