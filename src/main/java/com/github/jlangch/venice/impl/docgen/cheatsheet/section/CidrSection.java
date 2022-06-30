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


public class CidrSection implements ISectionBuilder {

    public CidrSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "CIDR",
                                            "classless inter-domain routing",
                                            "cidr");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection cidr = new DocSection("CIDR", "cidr.cidr");
        all.addSection(cidr);
        cidr.addItem(diBuilder.getDocItem("cidr/parse"));
        cidr.addItem(diBuilder.getDocItem("cidr/in-range?"));
        cidr.addItem(diBuilder.getDocItem("cidr/start-inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/end-inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr-to-bytes"));
        cidr.addItem(diBuilder.getDocItem("cidr/inet-addr-from-bytes"));

        final DocSection cidr_trie = new DocSection("CIDR Trie", "cidr.cidrtrie");
        all.addSection(cidr_trie);
        cidr_trie.addItem(diBuilder.getDocItem("cidr/trie"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/size"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/insert"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/lookup"));
        cidr_trie.addItem(diBuilder.getDocItem("cidr/lookup-reverse"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
