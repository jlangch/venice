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


public class ShellCoreSection implements ISectionBuilder {

    public ShellCoreSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Shell System", "shell-system");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection shell = new DocSection("Shell", "shell-system.shell");
        all.addSection(shell);
        shell.addItem(diBuilder.getDocItem("sh", false));
        shell.addItem(diBuilder.getDocItem("with-sh-dir", false));
        shell.addItem(diBuilder.getDocItem("with-sh-env", false));
        shell.addItem(diBuilder.getDocItem("with-sh-throw", false));

        final DocSection tools = new DocSection("Shell Tools", "shell-system.shell.tools");
        all.addSection(tools);
        tools.addItem(diBuilder.getDocItem("sh/open", false));
        tools.addItem(diBuilder.getDocItem("sh/pwd", false));

        final DocSection process = new DocSection("Process", "shell-system.shell.process");
        all.addSection(process);
        process.addItem(diBuilder.getDocItem("sh/alive?", false));
        process.addItem(diBuilder.getDocItem("sh/kill", false));
        process.addItem(diBuilder.getDocItem("sh/killall", false));
        process.addItem(diBuilder.getDocItem("sh/pgrep", false));
        process.addItem(diBuilder.getDocItem("sh/pargs", false));
        process.addItem(diBuilder.getDocItem("sh/pkill", false));
        process.addItem(diBuilder.getDocItem("sh/load-pid", false));
        process.addItem(diBuilder.getDocItem("sh/which", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
