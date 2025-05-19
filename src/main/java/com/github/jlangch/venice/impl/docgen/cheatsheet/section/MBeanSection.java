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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class MBeanSection implements ISectionBuilder {

    public MBeanSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "MBeans",
                                        "Managing static and dynamic Java MBeans",
                                        "mbean");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection server = new DocSection("Server", "mbean.server");
        all.addSection(server);
        server.addItem(diBuilder.getDocItem("mbean/platform-mbean-server", false));
        server.addItem(diBuilder.getDocItem("mbean/query-mbean-object-names", false));

        final DocSection access = new DocSection("Server", "mbean.access");
        all.addSection(access);
        server.addItem(diBuilder.getDocItem("mbean/object-name", false));
        access.addItem(diBuilder.getDocItem("mbean/info", false));
        access.addItem(diBuilder.getDocItem("mbean/attribute", false));
        access.addItem(diBuilder.getDocItem("mbean/attribute!", false));
        access.addItem(diBuilder.getDocItem("mbean/invoke", false));

        final DocSection register = new DocSection("Register", "mbean.register");
        all.addSection(register);
        register.addItem(diBuilder.getDocItem("mbean/register", false));
        register.addItem(diBuilder.getDocItem("mbean/register-dynamic", false));
        register.addItem(diBuilder.getDocItem("mbean/unregister", false));

        final DocSection mx = new DocSection("MX Beans", "mbean.mx");
        all.addSection(mx);
        mx.addItem(diBuilder.getDocItem("mbean/operating-system-mxbean", false));
        mx.addItem(diBuilder.getDocItem("mbean/runtime-mxbean", false));
        mx.addItem(diBuilder.getDocItem("mbean/memory-mxbean", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
