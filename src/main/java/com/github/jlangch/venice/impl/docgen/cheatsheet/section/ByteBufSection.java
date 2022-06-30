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


public class ByteBufSection implements ISectionBuilder {

    public ByteBufSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Byte Buffer", "bytebuf");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection bb_create = new DocSection("Create", "bytebuf.create");
        all.addSection(bb_create);
        bb_create.addItem(diBuilder.getDocItem("bytebuf"));
        bb_create.addItem(diBuilder.getDocItem("bytebuf-allocate"));
        bb_create.addItem(diBuilder.getDocItem("bytebuf-from-string"));

        final DocSection bb_test = new DocSection("Test", "bytebuf.test");
        all.addSection(bb_test);
        bb_test.addItem(diBuilder.getDocItem("empty?"));
        bb_test.addItem(diBuilder.getDocItem("not-empty?"));
        bb_test.addItem(diBuilder.getDocItem("bytebuf?"));

        final DocSection bb_use = new DocSection("Use", "bytebuf.use");
        all.addSection(bb_use);
        bb_use.addItem(diBuilder.getDocItem("count"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-capacity"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-limit"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-to-string"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-to-list"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-sub"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-pos"));
        bb_use.addItem(diBuilder.getDocItem("bytebuf-pos!"));

        final DocSection bb_read = new DocSection("Read", "bytebuf.read");
        all.addSection(bb_read);
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-byte"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-int"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-long"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-float"));
        bb_read.addItem(diBuilder.getDocItem("bytebuf-get-double"));

        final DocSection bb_write = new DocSection("Write", "bytebuf.write");
        all.addSection(bb_write);
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-byte!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-int!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-long!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-float!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-double!"));
        bb_write.addItem(diBuilder.getDocItem("bytebuf-put-buf!"));

        final DocSection encode = new DocSection("Base64", "bytebuf.base64");
        all.addSection(encode);
        encode.addItem(diBuilder.getDocItem("str/encode-base64"));
        encode.addItem(diBuilder.getDocItem("str/decode-base64"));

        final DocSection hex = new DocSection("Hex", "bytebuf.hex");
        all.addSection(hex);
        hex.addItem(diBuilder.getDocItem("str/hex-to-bytebuf"));
        hex.addItem(diBuilder.getDocItem("str/bytebuf-to-hex"));
        hex.addItem(diBuilder.getDocItem("str/format-bytebuf"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
