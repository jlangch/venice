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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class FunctionsSection implements ISectionBuilder {

    public FunctionsSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Functions", "functions");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "functions.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("fn"));
        create.addItem(diBuilder.getDocItem("defn"));
        create.addItem(diBuilder.getDocItem("defn-"));
        create.addItem(diBuilder.getDocItem("identity"));
        create.addItem(diBuilder.getDocItem("comp"));
        create.addItem(diBuilder.getDocItem("partial"));
        create.addItem(diBuilder.getDocItem("memoize"));
        create.addItem(diBuilder.getDocItem("juxt"));
        create.addItem(diBuilder.getDocItem("fnil"));
        create.addItem(diBuilder.getDocItem("trampoline"));
        create.addItem(diBuilder.getDocItem("complement"));
        create.addItem(diBuilder.getDocItem("constantly"));
        create.addItem(diBuilder.getDocItem("every-pred"));
        create.addItem(diBuilder.getDocItem("any-pred"));

        final DocSection call = new DocSection("Call", "functions.call");
        all.addSection(call);
        call.addItem(diBuilder.getDocItem("apply"));
        call.addItem(diBuilder.getDocItem("->"));
        call.addItem(diBuilder.getDocItem("->>"));

        final DocSection test = new DocSection("Test", "functions.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("fn?"));

        final DocSection misc = new DocSection("Misc", "functions.misc");
        all.addSection(misc);
        misc.addItem(diBuilder.getDocItem("nil?"));
        misc.addItem(diBuilder.getDocItem("some?"));
        misc.addItem(diBuilder.getDocItem("name"));
        misc.addItem(diBuilder.getDocItem("qualified-name"));
        misc.addItem(diBuilder.getDocItem("namespace"));
        misc.addItem(diBuilder.getDocItem("fn-name"));
        misc.addItem(diBuilder.getDocItem("callstack"));
        misc.addItem(diBuilder.getDocItem("coalesce"));

        final DocSection load = new DocSection("Load Source", "functions.load");
        all.addSection(load);
        load.addItem(diBuilder.getDocItem("load-module", false));
        load.addItem(diBuilder.getDocItem("load-file", false));
        load.addItem(diBuilder.getDocItem("load-classpath-file"));
        load.addItem(diBuilder.getDocItem("read-string"));
        load.addItem(diBuilder.getDocItem("eval"));

        final DocSection env = new DocSection("Environment", "functions.environment");
        all.addSection(env);
        env.addItem(diBuilder.getDocItem("set!"));
        env.addItem(diBuilder.getDocItem("resolve"));
        env.addItem(diBuilder.getDocItem("bound?"));
        env.addItem(diBuilder.getDocItem("var-get"));
        env.addItem(diBuilder.getDocItem("var-sym"));
        env.addItem(diBuilder.getDocItem("var-name"));
        env.addItem(diBuilder.getDocItem("var-ns"));
        env.addItem(diBuilder.getDocItem("var-sym-meta"));
        env.addItem(diBuilder.getDocItem("var-val-meta"));
        env.addItem(diBuilder.getDocItem("var-thread-local?"));
        env.addItem(diBuilder.getDocItem("var-local?"));
        env.addItem(diBuilder.getDocItem("var-global?"));
        env.addItem(diBuilder.getDocItem("name"));
        env.addItem(diBuilder.getDocItem("namespace"));

        final DocSection walk = new DocSection("Tree Walker", "functions.treewalker");
        all.addSection(walk);
        walk.addItem(diBuilder.getDocItem("prewalk"));
        walk.addItem(diBuilder.getDocItem("postwalk"));
        walk.addItem(diBuilder.getDocItem("prewalk-replace"));
        walk.addItem(diBuilder.getDocItem("postwalk-replace"));

        final DocSection meta = new DocSection("Meta", "functions.meta");
        all.addSection(meta);
        meta.addItem(diBuilder.getDocItem("meta"));
        meta.addItem(diBuilder.getDocItem("with-meta"));
        meta.addItem(diBuilder.getDocItem("vary-meta"));

        final DocSection doc = new DocSection("Documentation", "functions.doc");
        all.addSection(doc);
        doc.addItem(diBuilder.getDocItem("doc", false));
        doc.addItem(diBuilder.getDocItem("finder"));
        doc.addItem(diBuilder.getDocItem("modules"));

        final DocSection def = new DocSection("Definiton", "functions.def");
        all.addSection(def);
        def.addItem(diBuilder.getDocItem("fn-name"));
        def.addItem(diBuilder.getDocItem("fn-about"));
        def.addItem(diBuilder.getDocItem("fn-body"));
        def.addItem(diBuilder.getDocItem("fn-pre-conditions"));

        final DocSection syntax = new DocSection("Syntax", "functions.syntax");
        all.addSection(syntax);
        syntax.addItem(diBuilder.getDocItem("highlight"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
