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


public class ModuleSwissGeoSection implements ISectionBuilder {

    public ModuleSwissGeoSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Swiss Geo",
                                            "Swiss Geo Data",
                                            "modules.swissgeo");

        final DocSection all = new DocSection("(load-module :swiss-geo)", id());
        section.addSection(all);

        final DocSection geoip = new DocSection("Open", id());
        all.addSection(geoip);
        geoip.addItem(diBuilder.getDocItem("swiss-geo/download-geo-data", false));
        geoip.addItem(diBuilder.getDocItem("swiss-geo/load-geo-data-from-classpath", false));
        geoip.addItem(diBuilder.getDocItem("swiss-geo/slurp-geo-data", false));
        geoip.addItem(diBuilder.getDocItem("swiss-geo/close", false));
        geoip.addItem(diBuilder.getDocItem("swiss-geo/open?", false));

        final DocSection db = new DocSection("Query", id());
        all.addSection(db);
        db.addItem(diBuilder.getDocItem("swiss-geo/find-location", false));
        db.addItem(diBuilder.getDocItem("swiss-geo/find-by-plz", false));
        db.addItem(diBuilder.getDocItem("swiss-geo/ortschaften", false));
        db.addItem(diBuilder.getDocItem("swiss-geo/gemeinden", false));
        db.addItem(diBuilder.getDocItem("swiss-geo/locations", false));

        final DocSection dist = new DocSection("Distance", id());
        all.addSection(dist);
        dist.addItem(diBuilder.getDocItem("swiss-geo/distance", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
