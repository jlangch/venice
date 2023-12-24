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


public class ModuleParsifalSection implements ISectionBuilder {

    public ModuleParsifalSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Parsifal",
                                            "A parser combinator",
                                            "modules.parsifal",
                                            "*Parsifal* is a port of Nate Young's Parsatron Clojure " +
                                            "[parser combinators](https://github.com/youngnh/parsatron) "+
                                            "project.",
                                            null);

        final DocSection all = new DocSection("(load-module :parsifal)", id());
        section.addSection(all);

        final DocSection run = new DocSection("Run", id());
        all.addSection(run);
        run.addItem(diBuilder.getDocItem("parsifal/run", false));

        final DocSection define = new DocSection("Define", id());
        all.addSection(define);
        define.addItem(diBuilder.getDocItem("parsifal/defparser", false));

        final DocSection parsers = new DocSection("Parsers", id());
        all.addSection(parsers);
        parsers.addItem(diBuilder.getDocItem("parsifal/any", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/many1", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/times", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/either", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/choice", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/between", false));
        parsers.addItem(diBuilder.getDocItem("parsifal/>>", false));

        final DocSection special = new DocSection("Special Parsers", id());
        all.addSection(special);
        special.addItem(diBuilder.getDocItem("parsifal/eof", false));
        special.addItem(diBuilder.getDocItem("parsifal/never", false));
        special.addItem(diBuilder.getDocItem("parsifal/always", false));
        special.addItem(diBuilder.getDocItem("parsifal/lookahead", false));
        special.addItem(diBuilder.getDocItem("parsifal/attempt", false));

        final DocSection binding = new DocSection("Binding", id());
        all.addSection(binding);
        binding.addItem(diBuilder.getDocItem("parsifal/let->>", false));

        final DocSection ch = new DocSection("Char Parsers", id());
        all.addSection(ch);
        ch.addItem(diBuilder.getDocItem("parsifal/char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/not-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char", false));
        ch.addItem(diBuilder.getDocItem("parsifal/digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/hexdigit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter", false));
        ch.addItem(diBuilder.getDocItem("parsifal/letter-or-digit", false));
        ch.addItem(diBuilder.getDocItem("parsifal/any-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/none-char-of", false));
        ch.addItem(diBuilder.getDocItem("parsifal/string", false));

        final DocSection tok = new DocSection("Token Parsers", id());
        all.addSection(tok);
        tok.addItem(diBuilder.getDocItem("parsifal/token", false));

        final DocSection proto = new DocSection("Protocols", id());
        all.addSection(proto);
        proto.addItem(diBuilder.getDocItem("parsifal/SourcePosition", false));

        final DocSection line = new DocSection("Line Info", id());
        all.addSection(line);
        line.addItem(diBuilder.getDocItem("parsifal/lineno", false));
        line.addItem(diBuilder.getDocItem("parsifal/pos", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
