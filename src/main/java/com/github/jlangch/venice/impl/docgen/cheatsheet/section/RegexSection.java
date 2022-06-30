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


public class RegexSection implements ISectionBuilder {

    public RegexSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Regex", "regex");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection general = new DocSection("General", "regex.general");
        all.addSection(general);
        general.addItem(diBuilder.getDocItem("regex/pattern"));
        general.addItem(diBuilder.getDocItem("regex/matcher"));
        general.addItem(diBuilder.getDocItem("regex/reset"));
        general.addItem(diBuilder.getDocItem("regex/matches?"));
        general.addItem(diBuilder.getDocItem("regex/matches"));
        general.addItem(diBuilder.getDocItem("regex/group"));
        general.addItem(diBuilder.getDocItem("regex/count"));
        general.addItem(diBuilder.getDocItem("regex/find?"));
        general.addItem(diBuilder.getDocItem("regex/find"));
        general.addItem(diBuilder.getDocItem("regex/find-all"));
        general.addItem(diBuilder.getDocItem("regex/find+"));
        general.addItem(diBuilder.getDocItem("regex/find-all+"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
