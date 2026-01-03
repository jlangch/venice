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


public class ModuleAvironQueueSection implements ISectionBuilder {

    public ModuleAvironQueueSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Aviron File Queue",
                                            "ClamAV client file watcher queue",
                                            "modules.aviron-queue");

        final DocSection all = new DocSection("(load-module :aviron-queue)", id());
        section.addSection(all);


        final DocSection watcher = new DocSection("File Queue", id());
        all.addSection(watcher);
        watcher.addItem(diBuilder.getDocItem("aviron-queue/create", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/capacity", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/empty?", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/size", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/clear", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/remove", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/push", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/pop", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/overflow-count", false));
        watcher.addItem(diBuilder.getDocItem("aviron-queue/overflow-reset", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
