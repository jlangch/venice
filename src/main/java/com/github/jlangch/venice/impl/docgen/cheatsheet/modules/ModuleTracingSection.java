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


public class ModuleTracingSection implements ISectionBuilder {

    public ModuleTracingSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Tracing",
                                            "Tracing functions",
                                            "modules.tracing");

        final DocSection all = new DocSection("(load-module :trace)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Tracing", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("trace/trace"));
        trace.addItem(diBuilder.getDocItem("trace/trace-var"));
        trace.addItem(diBuilder.getDocItem("trace/untrace-var"));

        final DocSection test = new DocSection("Test", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("trace/traced?"));
        test.addItem(diBuilder.getDocItem("trace/traceable?"));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("trace/trace-str-limit"));

        final DocSection tee = new DocSection("Tee", id());
        all.addSection(tee);
        tee.addItem(diBuilder.getDocItem("trace/tee->"));
        tee.addItem(diBuilder.getDocItem("trace/tee->>"));
        tee.addItem(diBuilder.getDocItem("trace/tee"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
