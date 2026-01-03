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


public class ModuleAvironLimiterSection implements ISectionBuilder {

    public ModuleAvironLimiterSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Aviron Limiter",
                                            "ClamAV client CPU Limiter",
                                            "modules.aviron-limiter");

        final DocSection all = new DocSection("(load-module :aviron-limiter)", id());
        section.addSection(all);

        final DocSection limiter = new DocSection("Clamd Limiter", id());
        all.addSection(limiter);
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/create-clamd-cpu-limiter", false));
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/clamd-activate-cpu-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/clamd-deactivate-cpu-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/clamd-last-seen-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/clamd-limit-for-timestamp", false));
        limiter.addItem(diBuilder.getDocItem("aviron-limiter/format-profiles-as-table-by-hour", false));

        final DocSection dynLimiter = new DocSection("Dynamic Limiter", id());
        all.addSection(dynLimiter);
        dynLimiter.addItem(diBuilder.getDocItem("aviron-limiter/create-dynamic-cpu-limiter", false));
        dynLimiter.addItem(diBuilder.getDocItem("aviron-limiter/compute-dynamic-cpu-limit", false));
        dynLimiter.addItem(diBuilder.getDocItem("aviron-limiter/format-profiles-as-table-by-hour", false));

        final DocSection profile = new DocSection("CPU Profiles", id());
        all.addSection(profile);
        profile.addItem(diBuilder.getDocItem("aviron-limiter/create-cpu-profile", false));
        profile.addItem(diBuilder.getDocItem("aviron-limiter/get-cpu-profile-entries", false));
        profile.addItem(diBuilder.getDocItem("aviron-limiter/get-cpu-profile-limit", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
