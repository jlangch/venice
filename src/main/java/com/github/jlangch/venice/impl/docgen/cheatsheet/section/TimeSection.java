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


public class TimeSection implements ISectionBuilder {

    public TimeSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Time", "time");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection date = new DocSection("Date", "time.date");
        all.addSection(date);
        date.addItem(diBuilder.getDocItem("time/date"));
        date.addItem(diBuilder.getDocItem("time/date?"));

        final DocSection local_date = new DocSection("Local Date", "time.localdate");
        all.addSection(local_date);
        local_date.addItem(diBuilder.getDocItem("time/local-date"));
        local_date.addItem(diBuilder.getDocItem("time/local-date?"));
        local_date.addItem(diBuilder.getDocItem("time/local-date-parse"));

        final DocSection local_date_time = new DocSection("Local Date Time", "time.localdatetime");
        all.addSection(local_date_time);
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time"));
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time?"));
        local_date_time.addItem(diBuilder.getDocItem("time/local-date-time-parse"));

        final DocSection zoned_date_time = new DocSection("Zoned Date Time", "time.zoneddatetime");
        all.addSection(zoned_date_time);
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time"));
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time?"));
        zoned_date_time.addItem(diBuilder.getDocItem("time/zoned-date-time-parse"));

        final DocSection fields = new DocSection("Fields", "time.fields");
        all.addSection(fields);
        fields.addItem(diBuilder.getDocItem("time/year"));
        fields.addItem(diBuilder.getDocItem("time/month"));
        fields.addItem(diBuilder.getDocItem("time/day-of-week"));
        fields.addItem(diBuilder.getDocItem("time/day-of-month"));
        fields.addItem(diBuilder.getDocItem("time/day-of-year"));
        fields.addItem(diBuilder.getDocItem("time/hour"));
        fields.addItem(diBuilder.getDocItem("time/minute"));
        fields.addItem(diBuilder.getDocItem("time/second"));
        fields.addItem(diBuilder.getDocItem("time/milli"));

        final DocSection etc = new DocSection("Fields etc", "time.fieldsetc");
        all.addSection(etc);
        etc.addItem(diBuilder.getDocItem("time/length-of-year"));
        etc.addItem(diBuilder.getDocItem("time/length-of-month"));
        etc.addItem(diBuilder.getDocItem("time/first-day-of-month"));
        etc.addItem(diBuilder.getDocItem("time/last-day-of-month"));

        final DocSection zone = new DocSection("Zone", "time.zone");
        all.addSection(zone);
        zone.addItem(diBuilder.getDocItem("time/zone"));
        zone.addItem(diBuilder.getDocItem("time/zone-offset"));

        final DocSection format = new DocSection("Format", "time.format");
        all.addSection(format);
        format.addItem(diBuilder.getDocItem("time/formatter", false, false));
        format.addItem(diBuilder.getDocItem("time/format"));

        final DocSection compare = new DocSection("Test", "time.test");
        all.addSection(compare);
        compare.addItem(diBuilder.getDocItem("time/after?"));
        compare.addItem(diBuilder.getDocItem("time/not-after?"));
        compare.addItem(diBuilder.getDocItem("time/before?"));
        compare.addItem(diBuilder.getDocItem("time/not-before?"));
        compare.addItem(diBuilder.getDocItem("time/within?"));
        compare.addItem(diBuilder.getDocItem("time/leap-year?"));

        final DocSection misc = new DocSection("Miscellaneous", "time.misc");
        all.addSection(misc);
        misc.addItem(diBuilder.getDocItem("time/with-time"));
        misc.addItem(diBuilder.getDocItem("time/plus"));
        misc.addItem(diBuilder.getDocItem("time/minus"));
        misc.addItem(diBuilder.getDocItem("time/period"));
        misc.addItem(diBuilder.getDocItem("time/earliest"));
        misc.addItem(diBuilder.getDocItem("time/latest"));

        final DocSection util = new DocSection("Util", "time.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("time/zone-ids"));
        util.addItem(diBuilder.getDocItem("time/to-millis"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
