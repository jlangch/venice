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


public class ModuleCryptographySection implements ISectionBuilder {

    public ModuleCryptographySection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Cryptography",
                                            "modules.cryptography");

        final DocSection all = new DocSection("(load-module :crypt)", id());
        section.addSection(all);

        final DocSection ciphers = new DocSection("Ciphers", id());
        all.addSection(ciphers);
        ciphers.addItem(diBuilder.getDocItem("crypt/ciphers", false));
        ciphers.addItem(diBuilder.getDocItem("crypt/max-key-size", false));
        ciphers.addItem(diBuilder.getDocItem("crypt/provider?", false));
        ciphers.addItem(diBuilder.getDocItem("crypt/add-bouncy-castle-provider", false));

        final DocSection hashes = new DocSection("Hashes", id());
        all.addSection(hashes);
        hashes.addItem(diBuilder.getDocItem("crypt/md5-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha1-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/sha512-hash"));
        hashes.addItem(diBuilder.getDocItem("crypt/pbkdf2-hash"));

        final DocSection encrypt = new DocSection("Encrypt", id());
        all.addSection(encrypt);
        encrypt.addItem(diBuilder.getDocItem("crypt/encryptor-aes-256-gcm", false));
        encrypt.addItem(diBuilder.getDocItem("crypt/encryptor-chacha20", false));
        encrypt.addItem(diBuilder.getDocItem("crypt/encryptor-chacha20-bouncycastle", false));

        final DocSection filehash = new DocSection("File hash", id());
        all.addSection(filehash);
        filehash.addItem(diBuilder.getDocItem("crypt/hash-file", false));
        filehash.addItem(diBuilder.getDocItem("crypt/verify-file-hash", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
