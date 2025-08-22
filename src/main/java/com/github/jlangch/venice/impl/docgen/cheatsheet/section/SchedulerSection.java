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


public class SchedulerSection implements ISectionBuilder {

    public SchedulerSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Scheduler", "scheduler");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection sched = new DocSection("JDK", "scheduler.standard");
        all.addSection(sched);
        sched.addItem(diBuilder.getDocItem("schedule-delay", false));
        sched.addItem(diBuilder.getDocItem("schedule-at-fixed-rate", false));


        final DocSection cron = new DocSection("Cron", "Schedulers not prone to clock shifts", "scheduler.cron");
        all.addSection(cron);
        cron.addItem(diBuilder.getDocItem("cron/schedule-at", false));
        cron.addItem(diBuilder.getDocItem("cron/schedule-at-fixed-rate", false));
        cron.addItem(diBuilder.getDocItem("cron/schedule-at-round-times-in-day", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
