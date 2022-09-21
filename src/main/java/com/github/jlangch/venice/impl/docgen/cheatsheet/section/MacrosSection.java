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


public class MacrosSection implements ISectionBuilder {

    public MacrosSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Macros", "macros");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "macros.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("def-", false));
        create.addItem(diBuilder.getDocItem("defn"));
        create.addItem(diBuilder.getDocItem("defn-"));
        create.addItem(diBuilder.getDocItem("defmacro"));
        create.addItem(diBuilder.getDocItem("macroexpand"));
        create.addItem(diBuilder.getDocItem("macroexpand-all"));
        create.addItem(diBuilder.getDocItem("macro?"));

        final DocSection test = new DocSection("Test", "macros.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("macro?"));
        test.addItem(diBuilder.getDocItem("macroexpand-on-load?"));


        final DocSection quote = new DocSection("Quoting", "macros.quoting");
        all.addSection(quote);
        quote.addItem(diBuilder.getDocItem("quote"));
        quote.addItem(diBuilder.getDocItem("quasiquote"));

        final DocSection branch = new DocSection("Branch", "macros.branch");
        all.addSection(branch);
        branch.addItem(diBuilder.getDocItem("and"));
        branch.addItem(diBuilder.getDocItem("or"));
        branch.addItem(diBuilder.getDocItem("when"));
        branch.addItem(diBuilder.getDocItem("when-not"));
        branch.addItem(diBuilder.getDocItem("if-not"));
        branch.addItem(diBuilder.getDocItem("if-let"));
        branch.addItem(diBuilder.getDocItem("when-let"));
        branch.addItem(diBuilder.getDocItem("letfn"));

        final DocSection cond = new DocSection("Conditions", "macros.cond");
        all.addSection(cond);
        cond.addItem(diBuilder.getDocItem("cond"));
        cond.addItem(diBuilder.getDocItem("condp"));
        cond.addItem(diBuilder.getDocItem("case"));

        final DocSection loop = new DocSection("Loop", "macros.loop");
        all.addSection(loop);
        loop.addItem(diBuilder.getDocItem("while"));
        loop.addItem(diBuilder.getDocItem("dotimes"));
        loop.addItem(diBuilder.getDocItem("list-comp"));
        loop.addItem(diBuilder.getDocItem("doseq"));

        final DocSection call = new DocSection("Call", "macros.call");
        all.addSection(call);
        call.addItem(diBuilder.getDocItem("doto"));
        call.addItem(diBuilder.getDocItem("->"));
        call.addItem(diBuilder.getDocItem("->>"));
        call.addItem(diBuilder.getDocItem("-<>"));
        call.addItem(diBuilder.getDocItem("as->"));
        call.addItem(diBuilder.getDocItem("cond->"));
        call.addItem(diBuilder.getDocItem("cond->>"));
        call.addItem(diBuilder.getDocItem("some->"));
        call.addItem(diBuilder.getDocItem("some->>"));

        final DocSection loading = new DocSection("Load Code", "macros.loadcode");
        all.addSection(loading);
        loading.addItem(diBuilder.getDocItem("load-module"));
        loading.addItem(diBuilder.getDocItem("load-file", false));
        loading.addItem(diBuilder.getDocItem("load-classpath-file"));
        loading.addItem(diBuilder.getDocItem("load-string"));
        loading.addItem(diBuilder.getDocItem("loaded-modules"));

        final DocSection assert_ = new DocSection("Assert", "macros.assert");
        all.addSection(assert_);
        assert_.addItem(diBuilder.getDocItem("assert", true, true));
        assert_.addItem(diBuilder.getDocItem("assert-throws", true, true));

        final DocSection util = new DocSection("Util", "macros.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("comment"));
        util.addItem(diBuilder.getDocItem("gensym"));
        util.addItem(diBuilder.getDocItem("time"));
        util.addItem(diBuilder.getDocItem("with-out-str"));
        util.addItem(diBuilder.getDocItem("with-err-str"));

        final DocSection profil = new DocSection("Profiling", "macros.profiling");
        all.addSection(profil);
        profil.addItem(diBuilder.getDocItem("time"));
        profil.addItem(diBuilder.getDocItem("perf", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
