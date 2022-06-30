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


public class IoZipSection implements ISectionBuilder {

    public IoZipSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Zip/GZip", "io.zip");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection zip = new DocSection("zip", "io.zip_");
        all.addSection(zip);
        zip.addItem(diBuilder.getDocItem("io/zip", false));
        zip.addItem(diBuilder.getDocItem("io/zip-file", false));
        zip.addItem(diBuilder.getDocItem("io/zip-list", false));
        zip.addItem(diBuilder.getDocItem("io/zip-list-entry-names", false));
        zip.addItem(diBuilder.getDocItem("io/zip-append", false));
        zip.addItem(diBuilder.getDocItem("io/zip-remove", false));
        zip.addItem(diBuilder.getDocItem("io/zip?"));
        zip.addItem(diBuilder.getDocItem("io/unzip"));
        zip.addItem(diBuilder.getDocItem("io/unzip-first"));
        zip.addItem(diBuilder.getDocItem("io/unzip-nth"));
        zip.addItem(diBuilder.getDocItem("io/unzip-all"));
        zip.addItem(diBuilder.getDocItem("io/unzip-to-dir", false));

        final DocSection gzip = new DocSection("gzip", "io.gzip");
        all.addSection(gzip);
        gzip.addItem(diBuilder.getDocItem("io/gzip", false));
        gzip.addItem(diBuilder.getDocItem("io/gzip-to-stream"));
        gzip.addItem(diBuilder.getDocItem("io/gzip?"));
        gzip.addItem(diBuilder.getDocItem("io/ungzip"));
        gzip.addItem(diBuilder.getDocItem("io/ungzip-to-stream"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
