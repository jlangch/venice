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


public class JsonSection implements ISectionBuilder {

    public JsonSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("JSON", "json");


        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection read = new DocSection("read", "json.read");
        all.addSection(read);
        read.addItem(diBuilder.getDocItem("json/read-str"));
        read.addItem(diBuilder.getDocItem("json/slurp"));

        final DocSection write = new DocSection("write", "json.write");
        all.addSection(write);
        write.addItem(diBuilder.getDocItem("json/write-str"));
        write.addItem(diBuilder.getDocItem("json/spit"));

        final DocSection prettify = new DocSection("prettify", "json.prettify");
        all.addSection(prettify);
        prettify.addItem(diBuilder.getDocItem("json/pretty-print"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
