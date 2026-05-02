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


public class ModuleSwissCitiesSection implements ISectionBuilder {

    public ModuleSwissCitiesSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Swiss Cities",
                                            "Swiss Cities Geo / ZIP Codes Data",
                                            "modules.swisscities");

        final DocSection all = new DocSection("(load-module :swiss-cities)", id());
        section.addSection(all);

        final DocSection geoip = new DocSection("Open", id());
        all.addSection(geoip);
        geoip.addItem(diBuilder.getDocItem("swiss-cities/download-city-data", false));
        geoip.addItem(diBuilder.getDocItem("swiss-cities/load-city-data-from-classpath", false));
        geoip.addItem(diBuilder.getDocItem("swiss-cities/slurp-city-data", false));
        geoip.addItem(diBuilder.getDocItem("swiss-cities/close", false));
        geoip.addItem(diBuilder.getDocItem("swiss-cities/open?", false));

        final DocSection query = new DocSection("Query", id());
        all.addSection(query);
        query.addItem(diBuilder.getDocItem("swiss-cities/find-location", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/find-by-ortschaft", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/find-by-gemeinde", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/find-by-ortschaft-or-gemeinde", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/find-by-ortschaft-and-gemeinde", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/ortschaften", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/gemeinden", false));
        query.addItem(diBuilder.getDocItem("swiss-cities/locations", false));

        final DocSection plz = new DocSection("PLZ", id());
        all.addSection(plz);
        plz.addItem(diBuilder.getDocItem("swiss-cities/find-by-plz", false));
        plz.addItem(diBuilder.getDocItem("swiss-cities/plz?", false));

        final DocSection dist = new DocSection("Distance", id());
        all.addSection(dist);
        dist.addItem(diBuilder.getDocItem("swiss-cities/distance", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
