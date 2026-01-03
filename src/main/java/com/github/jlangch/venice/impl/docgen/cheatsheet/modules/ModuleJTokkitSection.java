/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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


public class ModuleJTokkitSection implements ISectionBuilder {

    public ModuleJTokkitSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final String footer = "Install JTokkit 3rd party libraries:\n\n" +
                "```                                             \n" +
                "(do                                             \n" +
                "  (load-module :jtokkit-install)                \n" +
                "  (jtokkit-install/install :dir (repl/libs-dir) \n" +
                "                           :silent false))      \n" +
                "```\n";

        final DocSection section = new DocSection("JTokkit", "A tokenizer designed for use with OpenAI models", "modules.jtokkit", null, footer);

        final DocSection all = new DocSection("(load-module :jtokkit)", id());
        section.addSection(all);

        final DocSection encoding = new DocSection("Encoding", id());
        all.addSection(encoding);
        encoding.addItem(diBuilder.getDocItem("jtokkit/encoding", false));
        encoding.addItem(diBuilder.getDocItem("jtokkit/encoding-types", false));
        encoding.addItem(diBuilder.getDocItem("jtokkit/model-types", false));
        encoding.addItem(diBuilder.getDocItem("jtokkit/encode", false));
        encoding.addItem(diBuilder.getDocItem("jtokkit/count-tokens", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
