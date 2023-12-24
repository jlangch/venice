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


public class ModuleQrRefSection implements ISectionBuilder {

    public ModuleQrRefSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "QR-Reference",
                                            "Create, parse, and format QR references  "
                                            + "according to the Swiss payment standards.",
                                            "modules.qrref");

        final DocSection all = new DocSection("(load-module :qrref)", id());
        section.addSection(all);

        final DocSection main = new DocSection("QR Ref", id());
        all.addSection(main);
        main.addItem(diBuilder.getDocItem("qrref/qr-ref", true));
        main.addItem(diBuilder.getDocItem("qrref/parse", true));
        main.addItem(diBuilder.getDocItem("qrref/format", true));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
