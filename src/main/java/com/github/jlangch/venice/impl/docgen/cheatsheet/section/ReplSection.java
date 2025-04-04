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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ReplSection implements ISectionBuilder {

    public ReplSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("REPL", "repl");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection repl = new DocSection("Info", "repl.info");
        all.addSection(repl);
        repl.addItem(diBuilder.getDocItem("repl?", false));
        repl.addItem(diBuilder.getDocItem("repl/info", false));

        final DocSection term = new DocSection("Terminal", "repl.terminal");
        all.addSection(term);
        term.addItem(diBuilder.getDocItem("repl/term-rows", false));
        term.addItem(diBuilder.getDocItem("repl/term-cols", false));

        final DocSection dirs = new DocSection("Dirs", "repl.dirs");
        all.addSection(dirs);
        dirs.addItem(diBuilder.getDocItem("repl/home-dir", false));
        dirs.addItem(diBuilder.getDocItem("repl/libs-dir", false));

        final DocSection config = new DocSection("Config", "repl.config");
        all.addSection(config);
        config.addItem(diBuilder.getDocItem("repl/prompt!", false));
        config.addItem(diBuilder.getDocItem("repl/handler!", false));
        config.addItem(diBuilder.getDocItem("repl/color-theme", false));
        config.addItem(diBuilder.getDocItem("repl/color-theme!", false));

        final DocSection env = new DocSection("Env Vars", "repl.env");
        all.addSection(env);
        env.addItem(diBuilder.getDocItem("repl/cat-env", false));
        env.addItem(diBuilder.getDocItem("repl/get-env", false));
        env.addItem(diBuilder.getDocItem("repl/add-env", false));
        env.addItem(diBuilder.getDocItem("repl/remove-env", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
