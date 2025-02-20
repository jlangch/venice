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


public class ModuleRingSection implements ISectionBuilder {

    public ModuleRingSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Ring", "modules.ring");

        final DocSection all = new DocSection("(load-module :ring)", id());
        section.addSection(all);

        final DocSection servlet = new DocSection("Servlet", id());
        all.addSection(servlet);
        servlet.addItem(diBuilder.getDocItem("ring/create-servlet", false));

        final DocSection routing = new DocSection("Routing", id());
        all.addSection(routing);
        routing.addItem(diBuilder.getDocItem("ring/match-routes", false));

        final DocSection util = new DocSection("Utils", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ring-util/redirect", false));
        util.addItem(diBuilder.getDocItem("ring-util/not-found-response", false));
        util.addItem(diBuilder.getDocItem("ring-util/get-request-header", false));
        util.addItem(diBuilder.getDocItem("ring-util/get-request-header-accept-mimetypes", false));
        util.addItem(diBuilder.getDocItem("ring-util/get-request-parameters ", false));
        util.addItem(diBuilder.getDocItem("ring-util/get-request-parameter", false));
        util.addItem(diBuilder.getDocItem("ring-util/get-request-long-parameter", false));
        util.addItem(diBuilder.getDocItem("ring-util/html-request?", false));
        util.addItem(diBuilder.getDocItem("ring-util/json-request?", false));
        util.addItem(diBuilder.getDocItem("ring-util/parse-charset", false));
        util.addItem(diBuilder.getDocItem("ring-util/debug?", false));

        final DocSection middleware = new DocSection("Middleware", id());
        all.addSection(middleware);
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-identity", false));
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-debug", false));
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-print-uri", false));
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-request-counter", false));
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-dump-request", false));
        middleware.addItem(diBuilder.getDocItem("ring-mw/mw-dump-response", false));

        final DocSection session = new DocSection("Session", id());
        all.addSection(session);
        session.addItem(diBuilder.getDocItem("ring-session/session-invalidate", false));
        session.addItem(diBuilder.getDocItem("ring-session/session-clear", false));
        session.addItem(diBuilder.getDocItem("ring-session/session-id", false));
        session.addItem(diBuilder.getDocItem("ring-session/session-get-value", false));
        session.addItem(diBuilder.getDocItem("ring-session/session-remove-value", false));
        session.addItem(diBuilder.getDocItem("ring-session/session-creation-time", false));

        final DocSection multipart = new DocSection("Multipart", id());
        all.addSection(multipart);
        multipart.addItem(diBuilder.getDocItem("ring-multipart/multipart-request?", false));
        multipart.addItem(diBuilder.getDocItem("ring-multipart/parts", false));
        multipart.addItem(diBuilder.getDocItem("ring-multipart/parts-delete-all", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
