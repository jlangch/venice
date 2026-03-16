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


public class ModuleRSASection implements ISectionBuilder {

    public ModuleRSASection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "RSA",
                                            "modules.rsa");

        final DocSection all = new DocSection("(load-module :rsa)", id());
        section.addSection(all);

        final DocSection keys = new DocSection("Keys", id());
        all.addSection(keys);
        keys.addItem(diBuilder.getDocItem("rsa/generate-key-pair", false));
        keys.addItem(diBuilder.getDocItem("rsa/private-key", false));
        keys.addItem(diBuilder.getDocItem("rsa/public-key", false));
        keys.addItem(diBuilder.getDocItem("rsa/save-key-pair", false));
        keys.addItem(diBuilder.getDocItem("rsa/load-key", false));

        final DocSection crypt = new DocSection("Encryption", id());
        all.addSection(crypt);
        crypt.addItem(diBuilder.getDocItem("rsa/encrypt", false));
        crypt.addItem(diBuilder.getDocItem("rsa/decrypt", false));

        final DocSection sign = new DocSection("Signatures", id());
        all.addSection(sign);
        sign.addItem(diBuilder.getDocItem("rsa/sign", false));
        sign.addItem(diBuilder.getDocItem("rsa/verify", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
