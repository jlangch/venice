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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleShellSection implements ISectionBuilder {

    public ModuleShellSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Shell",
                                            "Functions to deal with the operating system",
                                            "modules.shell");

        final DocSection all = new DocSection("(load-module :shell)", id());
        section.addSection(all);

        final DocSection trace = new DocSection("Open", id());
        all.addSection(trace);
        trace.addItem(diBuilder.getDocItem("shell/open", false));
        trace.addItem(diBuilder.getDocItem("shell/open-macos-app", false));

        final DocSection test = new DocSection("Process", id());
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("shell/kill", false));
        test.addItem(diBuilder.getDocItem("shell/kill-forcibly", false));
        test.addItem(diBuilder.getDocItem("shell/wait-for-process-exit", false));
        test.addItem(diBuilder.getDocItem("shell/alive?", false));
        test.addItem(diBuilder.getDocItem("shell/pid", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle", false));
        test.addItem(diBuilder.getDocItem("shell/process-handle?", false));
        test.addItem(diBuilder.getDocItem("shell/process-info", false));
        test.addItem(diBuilder.getDocItem("shell/processes", false));
        test.addItem(diBuilder.getDocItem("shell/processes-info", false));
        test.addItem(diBuilder.getDocItem("shell/descendant-processes", false));
        test.addItem(diBuilder.getDocItem("shell/parent-process", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("shell/diff", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
