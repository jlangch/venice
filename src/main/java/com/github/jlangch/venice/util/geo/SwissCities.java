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
package com.github.jlangch.venice.util.geo;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.csv.CSVReader;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.impl.util.io.zip.Zipper;


public class SwissCities {

    private SwissCities(final List<City> cities) {
        this.cities.addAll(cities);

        // map by ortschaft
        this.mappedByOrtschaft = cities
                                  .stream()
                                  .filter(it -> !StringUtil.isBlank(it.ortschaft))
                                  .collect(Collectors.groupingBy(
                                               City::getOrtschaft,
                                               Collectors.mapping(si -> si, Collectors.toList())));


        // map by gemeinde
        this.mappedByGemeinde = cities
                                 .stream()
                                 .filter(it -> !StringUtil.isBlank(it.gemeinde))
                                 .collect(Collectors.groupingBy(
                                              City::getGemeinde,
                                              Collectors.mapping(si -> si, Collectors.toList())));

        // zip codes
        this.zipCodes = cities
                         .stream()
                         .filter(it -> !StringUtil.isBlank(it.plz))
                         .map(it -> it.plz)
                         .collect(Collectors.toSet());
    }


    public List<City> locations() {
        return Collections.unmodifiableList(cities);
    }

    public List<String> ortschaften() {
        return cities
                .stream()
                .map(it -> it.ortschaft)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> gemeinden() {
        return cities
                .stream()
                .map(it -> it.gemeinde)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<City> findByOrtschaft(final String ortschaft) {
        Objects.requireNonNull(ortschaft);

        return mappedByOrtschaft.getOrDefault(ortschaft, new ArrayList<>());
    }

    public List<City> findByOrtschaftAndKanton(final String ortschaft, final String kanton) {
        Objects.requireNonNull(ortschaft);
        Objects.requireNonNull(kanton);

        return findByOrtschaft(ortschaft)
                .stream()
                .filter(it -> kanton.equals(it.kanton))
                .collect(Collectors.toList());
    }


    public List<City> findByGemeinde(final String gemeinde) {
        Objects.requireNonNull(gemeinde);

        return mappedByGemeinde.getOrDefault(gemeinde, new ArrayList<>());
    }

    public List<City> findByGemeindeAndKanton(final String gemeinde, final String kanton) {
        Objects.requireNonNull(gemeinde);
        Objects.requireNonNull(kanton);

        return findByGemeinde(gemeinde)
                .stream()
                .filter(it -> kanton.equals(it.kanton))
                .collect(Collectors.toList());
    }


    public List<City> findByOrtschaftOrGemeinde(final String location) {
        Objects.requireNonNull(location);

        return cities
                .stream()
                .filter(it -> location.equals(it.ortschaft) || location.equals(it.gemeinde))
                .collect(Collectors.toList());
    }

    public List<City> findByOrtschaftOrGemeindeAndKanton(final String location, final String kanton) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(kanton);

        return cities
                .stream()
                .filter(it -> location.equals(it.ortschaft) || location.equals(it.gemeinde))
                .filter(it -> kanton.equals(it.kanton))
                .collect(Collectors.toList());
    }


    public List<City> findByOrtschaftAndGemeinde(final String location) {
        Objects.requireNonNull(location);

        return cities
                .stream()
                .filter(it -> location.equals(it.ortschaft) && location.equals(it.gemeinde))
                .collect(Collectors.toList());
    }

    public List<City> findByOrtschaftAndGemeindeAndKanton(final String location, final String kanton) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(kanton);

        return cities
                .stream()
                .filter(it -> location.equals(it.ortschaft) && location.equals(it.gemeinde))
                .filter(it -> kanton.equals(it.kanton))
                .collect(Collectors.toList());
    }


    public List<City> findByLocation(final String location) {
        Objects.requireNonNull(location);

        final List<City> loc1 = findByOrtschaftAndGemeinde(location);
        if (!loc1.isEmpty()) return loc1;

        final List<City> loc2 = findByOrtschaft(location);
        if (!loc2.isEmpty()) return loc1;

        return findByGemeinde(location);
    }


    public List<City> findByPlz(final String plz) {
        Objects.requireNonNull(plz);

        return cities
                .stream()
                .filter(it -> plz.equals(plz))
                .collect(Collectors.toList());
    }


    public boolean hasPlz(final String plz) {
        Objects.requireNonNull(plz);

        return zipCodes.contains(plz);
    }


    public double distance(final City city1, final City city2) {
        Objects.requireNonNull(city1);
        Objects.requireNonNull(city2);

        final double east1 = city1.getEast();
        final double east2 = city2.getEast();
        final double north1 = city1.getNorth();
        final double north2 = city2.getNorth();

        final double deltaEast = east1 - east2;
        final double deltaNorth= north1 - north2;

        final double distanceInMeters = Math.sqrt(
                                         Math.abs(
                                         deltaEast * deltaEast - deltaNorth * deltaNorth));

        return distanceInMeters / 1000.0; // convert to km
    }


    public static SwissCities loadFromClasspath(final String lv95Resource) {
        Objects.requireNonNull(lv95Resource);

        try (InputStream is = new ClassPathResource(lv95Resource).getInputStream()) {
            return parse(is);
        }
        catch(Exception ex) {
            throw new VncException("Failed to load Swiss cities dataset from classpath", ex);
        }
    }

    public static SwissCities load(final File lv95Zip) {
        Objects.requireNonNull(lv95Zip);

        try (InputStream is = new FileInputStream(lv95Zip)) {
            return parse(is);
        }
        catch(Exception ex) {
            throw new VncException("Failed to load Swiss cities dataset from file", ex);
        }
    }

    public static SwissCities downloadFromSwissTopo() {
        try {
            final URL url = new URL(DOWNLOAD_URL);

            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.connect();

            try {
                final int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedInputStream is = new BufferedInputStream(conn.getInputStream())) {
                       return parse(is);
                    }
                }
                else {
                    throw new VncException(
                            "Failed to load Swiss cities dataset from 'https://data.geo.admin.ch/'");
                }
            }
            finally {
                conn.disconnect();
            }
        }
        catch(Exception ex) {
            throw new VncException(
                    "Failed to load Swiss cities dataset from 'https://data.geo.admin.ch/'", ex);
        }
    }


    public static class City {

        // LV95 CSV Format "ortschaftenverzeichnis_plz_2056.csv.zip":
        // Ortschaftsname;PLZ4;Zusatzziffer;ZIP_ID;Gemeindename;BFS-Nr;Kantonskürzel;Adressenanteil;E;N;Sprache;Validity

        public City(final List<String> entry) {
            ortschaft = entry.get(0);
            plz = entry.get(1);
            gemeinde = StringUtil.nullToEmpty(entry.get(4));
            kanton = entry.get(6);
            east = Double.parseDouble(entry.get(8));
            north = Double.parseDouble(entry.get(9));
            language = entry.get(10);
            validity = entry.get(11);
        }

        public String getOrtschaft() { return ortschaft; }
        public String getPlz() { return plz; }
        public String getGemeinde() { return gemeinde; }
        public String getKanton() { return kanton; }
        public double getEast() { return east; }
        public double getNorth() { return north; }
        public String getLanguage() { return language; }
        public String getValidity() { return validity; }

        private final String ortschaft;
        private final String plz;
        private final String gemeinde;
        private final String kanton;
        private final double east;
        private final double north;
        private final String language;
        private final String validity;
    }


    private static byte[] unpackCsvData(final InputStream is) throws Exception {
        final byte[] zip = IOStreamUtil.copyIStoByteArray(is);
        return Zipper.unzip(zip, LV95_ZIP_ENTRYNAME);
    }

    private static SwissCities parse(final InputStream is) throws Exception {
        final byte[] csv = unpackCsvData(is);

        // skip first entry: header line
        try (InputStream iscd = new ByteArrayInputStream(csv)) {
            return new SwissCities(
                    CollectionUtil
                        .drop(new CSVReader(';', '"').parse(iscd, StandardCharsets.UTF_8), 1)
                        .stream()
                        .map(it -> new City(it))
                        .collect(Collectors.toList()));
        }
    }


    private static final String LV95_ZIP_FILENAME = "ortschaftenverzeichnis_plz_2056.csv.zip";

    private static final String LV95_ZIP_ENTRYNAME = "AMTOVZ_CSV_LV95/AMTOVZ_CSV_LV95.csv";

    private static final String DOWNLOAD_URL = "https://data.geo.admin.ch/ch.swisstopo-vd.ortschaftenverzeichnis_plz/ortschaftenverzeichnis_plz/ortschaftenverzeichnis_plz_2056.csv.zip";


    private final List<City> cities = new ArrayList<>();
    private final Map<String, List<City>> mappedByOrtschaft;
    private final Map<String, List<City>> mappedByGemeinde;
    private final Set<String> zipCodes;
}
