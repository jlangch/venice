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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleLoggerSection implements ISectionBuilder {

    public ModuleLoggerSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Logger",
                                            "modules.logger");

        final DocSection all = new DocSection("(load-module :logger)", id());
        section.addSection(all);

        final DocSection main = new DocSection("Logger", id());
        all.addSection(main);
        main.addItem(diBuilder.getDocItem("logger/logger", false));
        main.addItem(diBuilder.getDocItem("logger/console-logger", false));
        main.addItem(diBuilder.getDocItem("logger/file-logger", false));

        final DocSection rot = new DocSection("Rotation", id());
        all.addSection(rot);
        rot.addItem(diBuilder.getDocItem( "logger/rotation-scheduler-running?", false));
        rot.addItem(diBuilder.getDocItem("logger/enable-auto-start-rotation-scheduler", false));
        rot.addItem(diBuilder.getDocItem("logger/start-rotation-scheduler", false));
        rot.addItem(diBuilder.getDocItem("logger/rotate", false));
        rot.addItem(diBuilder.getDocItem("logger/rotate-all", false));
        rot.addItem(diBuilder.getDocItem("logger/requires-rotation?", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("logger/format-level", false));
        util.addItem(diBuilder.getDocItem("logger/format-level", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
