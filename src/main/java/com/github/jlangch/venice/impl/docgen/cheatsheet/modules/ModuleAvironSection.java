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


public class ModuleAvironSection implements ISectionBuilder {

    public ModuleAvironSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                            "Aviron",
                                            "ClamAV client",
                                            "modules.aviron");

        final DocSection all = new DocSection("(load-module :aviron)", id());
        section.addSection(all);

        final DocSection main = new DocSection("Client", id());
        all.addSection(main);
        main.addItem(diBuilder.getDocItem("aviron/create-client", false));
        main.addItem(diBuilder.getDocItem("aviron/print-config", false));
        main.addItem(diBuilder.getDocItem("aviron/version", false));

        final DocSection scan = new DocSection("Scan", id());
        all.addSection(scan);
        scan.addItem(diBuilder.getDocItem("aviron/scan-stream", false));
        scan.addItem(diBuilder.getDocItem("aviron/scan-path", false));
        scan.addItem(diBuilder.getDocItem("aviron/scan-parallel", false));

        final DocSection result = new DocSection("Scan Result", id());
        all.addSection(result);
        result.addItem(diBuilder.getDocItem("aviron/ok?", false));
        result.addItem(diBuilder.getDocItem("aviron/virus?", false));
        result.addItem(diBuilder.getDocItem("aviron/viruses", false));

        final DocSection admin = new DocSection("Admin", id());
        all.addSection(admin);
        admin.addItem(diBuilder.getDocItem("aviron/ping", false));
        admin.addItem(diBuilder.getDocItem("aviron/reachable?", false));
        admin.addItem(diBuilder.getDocItem("aviron/shutdown-server", false));
        admin.addItem(diBuilder.getDocItem("aviron/clamav-version", false));
        admin.addItem(diBuilder.getDocItem("aviron/stats", false));
        admin.addItem(diBuilder.getDocItem("aviron/reload-virus-databases", false));

        final DocSection debug = new DocSection("Debug", id());
        all.addSection(debug);
        debug.addItem(diBuilder.getDocItem("aviron/last-command-run-details", false));

        final DocSection quarantine = new DocSection("Quarantine", id());
        all.addSection(quarantine);
        quarantine.addItem(diBuilder.getDocItem("aviron/quarantine-active?", false));
        quarantine.addItem(diBuilder.getDocItem("aviron/list-quarantine-files", false));
        quarantine.addItem(diBuilder.getDocItem("aviron/remove-quarantine-file", false));
        quarantine.addItem(diBuilder.getDocItem("aviron/remove-all-quarantine-files", false));

        final DocSection clamd = new DocSection("Clamd Admin", id());
        all.addSection(clamd);
        clamd.addItem(diBuilder.getDocItem("aviron/clamd-pid", false));
        clamd.addItem(diBuilder.getDocItem("aviron/clamd-cpu-limit", false));
        clamd.addItem(diBuilder.getDocItem("aviron/clamd-cpu-limit-off", false));
        clamd.addItem(diBuilder.getDocItem("aviron/cpus", false));

        final DocSection limiter = new DocSection("Clamd CPU Limiter", id());
        all.addSection(limiter);
        limiter.addItem(diBuilder.getDocItem("aviron/create-clamd-cpu-limiter", false));
        limiter.addItem(diBuilder.getDocItem("aviron/clamd-activate-cpu-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron/clamd-deactivate-cpu-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron/clamd-last-seen-limit", false));
        limiter.addItem(diBuilder.getDocItem("aviron/clamd-limit-for-timestamp", false));
        limiter.addItem(diBuilder.getDocItem("aviron/format-profiles-as-table-by-hour", false));

        final DocSection dynLimiter = new DocSection("Dynamic CPU Limiter", id());
        all.addSection(dynLimiter);
        dynLimiter.addItem(diBuilder.getDocItem("aviron/create-dynamic-cpu-limiter", false));
        dynLimiter.addItem(diBuilder.getDocItem("aviron/compute-dynamic-cpu-limit", false));
        dynLimiter.addItem(diBuilder.getDocItem("aviron/format-profiles-as-table-by-hour", false));

        final DocSection profile = new DocSection("CPU Profiles", id());
        all.addSection(profile);
        profile.addItem(diBuilder.getDocItem("aviron/create-cpu-profile", false));
        profile.addItem(diBuilder.getDocItem("aviron/get-cpu-profile-entries", false));
        profile.addItem(diBuilder.getDocItem("aviron/get-cpu-profile-limit", false));

        final DocSection watcher = new DocSection("File Queue", id());
        all.addSection(watcher);
        watcher.addItem(diBuilder.getDocItem("aviron/queue-create", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-capacity", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-empty?", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-size", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-clear", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-remove", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-push", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-pop", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-overflow-count", false));
        watcher.addItem(diBuilder.getDocItem("aviron/queue-overflow-reset", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }


    private final DocItemBuilder diBuilder;
}
