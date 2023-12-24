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


public class SystemVarSection implements ISectionBuilder {

    public SystemVarSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("System Vars", "sysvars");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection other = new DocSection("System Vars", "sysvars.var");
        all.addSection(other);
        other.addItem(diBuilder.getDocItem("*version*"));
        other.addItem(diBuilder.getDocItem("*newline*"));
        other.addItem(diBuilder.getDocItem("*loaded-modules*"));
        other.addItem(diBuilder.getDocItem("*loaded-files*"));
        other.addItem(diBuilder.getDocItem("*ns*"));
        other.addItem(diBuilder.getDocItem("*run-mode*"));
        other.addItem(diBuilder.getDocItem("*ansi-term*"));
        other.addItem(diBuilder.getDocItem("*ARGV*"));
        other.addItem(diBuilder.getDocItem("*out*"));
        other.addItem(diBuilder.getDocItem("*err*"));
        other.addItem(diBuilder.getDocItem("*in*"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
