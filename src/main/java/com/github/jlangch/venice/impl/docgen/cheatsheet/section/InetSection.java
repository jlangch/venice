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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class InetSection implements ISectionBuilder {

    public InetSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("INET", "inet");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection create = new DocSection("Create", "inet.create");
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("inet/inet-addr"));

        final DocSection util = new DocSection("Util", "inet.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("inet/inet-addr-to-bytes"));
        util.addItem(diBuilder.getDocItem("inet/inet-addr-from-bytes"));

        final DocSection test = new DocSection("Test", "inet.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("inet/ip4?"));
        test.addItem(diBuilder.getDocItem("inet/ip6?"));
        test.addItem(diBuilder.getDocItem("inet/linklocal-addr?"));
        test.addItem(diBuilder.getDocItem("inet/sitelocal-addr?"));
        test.addItem(diBuilder.getDocItem("inet/multicast-addr?"));
        test.addItem(diBuilder.getDocItem("inet/reachable?", true, true));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
