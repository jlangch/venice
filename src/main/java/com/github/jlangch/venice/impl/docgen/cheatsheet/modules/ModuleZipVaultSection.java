/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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


public class ModuleZipVaultSection implements ISectionBuilder {

    public ModuleZipVaultSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "Zip Vault",
                                        "AES 256 encrypted und password protected zip file",
                                        "modules.zipvault");

        final DocSection all = new DocSection("(load-module :zipvault)", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", id());
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("zipvault/zip", false));
        create.addItem(diBuilder.getDocItem("zipvault/entropy", true));

        final DocSection add = new DocSection("Add", id());
        all.addSection(add);
        add.addItem(diBuilder.getDocItem("zipvault/add-files", false));
        add.addItem(diBuilder.getDocItem("zipvault/add-folder", false));
        add.addItem(diBuilder.getDocItem("zipvault/add-stream", false));

        final DocSection rem = new DocSection("Remove", id());
        all.addSection(rem);
        rem.addItem(diBuilder.getDocItem("zipvault/remove-files", false));

        final DocSection extract = new DocSection("Extract", id());
        all.addSection(extract);
        extract.addItem(diBuilder.getDocItem("zipvault/extract-file", false));
        extract.addItem(diBuilder.getDocItem("zipvault/extract-all", false));
        extract.addItem(diBuilder.getDocItem("zipvault/extract-file-data", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("zipvault/encrypted?", false));
        util.addItem(diBuilder.getDocItem("zipvault/valid-zip-file?", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
