/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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


public class ModuleGeoipSection implements ISectionBuilder {

    public ModuleGeoipSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Geo IP",
                                            "Geolocation mapping for IP adresses",
                                            "modules.geoip");

        final DocSection all = new DocSection("(load-module :geoip)", id());
        section.addSection(all);

        final DocSection geoip = new DocSection("Lookup", id());
        all.addSection(geoip);
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-country-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver", false));
        geoip.addItem(diBuilder.getDocItem("geoip/ip-to-city-loc-resolver-mem-optimized", false));

        final DocSection db = new DocSection("Databases", id());
        all.addSection(db);
        db.addItem(diBuilder.getDocItem("geoip/download-google-country-db-to-csvfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db-to-zipfile", false));
        db.addItem(diBuilder.getDocItem("geoip/download-maxmind-db", false));

        final DocSection dbBuild = new DocSection("DB Parser", id());
        all.addSection(dbBuild);
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-ip-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-country-db", false));
        dbBuild.addItem(diBuilder.getDocItem("geoip/parse-maxmind-city-db", false));

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-country-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/build-maxmind-city-db-url"));
        util.addItem(diBuilder.getDocItem("geoip/map-location-to-numerics"));
        util.addItem(diBuilder.getDocItem("geoip/country-to-location-resolver", false));
        util.addItem(diBuilder.getDocItem("geoip/addr-ranges->trie"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
