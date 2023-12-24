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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ConcurrencySection implements ISectionBuilder {

    public ConcurrencySection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Concurrency", "concurrency");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection atoms = new DocSection("Atoms", "concurrency.atoms");
        all.addSection(atoms);
        atoms.addItem(diBuilder.getDocItem("atom"));
        atoms.addItem(diBuilder.getDocItem("atom?"));
        atoms.addItem(diBuilder.getDocItem("deref"));
        atoms.addItem(diBuilder.getDocItem("deref?"));
        atoms.addItem(diBuilder.getDocItem("reset!"));
        atoms.addItem(diBuilder.getDocItem("swap!"));
        atoms.addItem(diBuilder.getDocItem("swap-vals!"));
        atoms.addItem(diBuilder.getDocItem("compare-and-set!"));
        atoms.addItem(diBuilder.getDocItem("add-watch"));
        atoms.addItem(diBuilder.getDocItem("remove-watch"));

        final DocSection locks = new DocSection("Locks", "concurrency.locks");
        all.addSection(locks);
        locks.addItem(diBuilder.getDocItem("lock"));
        locks.addItem(diBuilder.getDocItem("lock?"));
        locks.addItem(diBuilder.getDocItem("acquire"));
        locks.addItem(diBuilder.getDocItem("try-acquire"));
        locks.addItem(diBuilder.getDocItem("release"));
        locks.addItem(diBuilder.getDocItem("locked?"));

        final DocSection locking = new DocSection("Locking", "concurrency.locking");
        all.addSection(locking);
        locking.addItem(diBuilder.getDocItem("locking"));

        final DocSection futures = new DocSection("Futures", "concurrency.futures");
        all.addSection(futures);
        futures.addItem(diBuilder.getDocItem("future"));
        futures.addItem(diBuilder.getDocItem("future-task"));
        futures.addItem(diBuilder.getDocItem("future?"));
        futures.addItem(diBuilder.getDocItem("futures-fork"));
        futures.addItem(diBuilder.getDocItem("futures-wait"));
        futures.addItem(diBuilder.getDocItem("futures-thread-pool-info"));
        futures.addItem(diBuilder.getDocItem("done?"));
        futures.addItem(diBuilder.getDocItem("cancel"));
        futures.addItem(diBuilder.getDocItem("cancelled?"));
        futures.addItem(diBuilder.getDocItem("deref"));
        futures.addItem(diBuilder.getDocItem("deref?"));
        futures.addItem(diBuilder.getDocItem("realized?"));

        final DocSection promises = new DocSection("Promises", "concurrency.promises");
        all.addSection(promises);
        promises.addItem(diBuilder.getDocItem("promise"));
        promises.addItem(diBuilder.getDocItem("promise?"));
        promises.addItem(diBuilder.getDocItem("deliver"));
        promises.addItem(diBuilder.getDocItem("deliver-ex"));
        promises.addItem(diBuilder.getDocItem("realized?"));
        promises.addItem(diBuilder.getDocItem("then-accept"));
        promises.addItem(diBuilder.getDocItem("then-accept-both"));
        promises.addItem(diBuilder.getDocItem("then-apply"));
        promises.addItem(diBuilder.getDocItem("then-combine"));
        promises.addItem(diBuilder.getDocItem("then-compose"));
        promises.addItem(diBuilder.getDocItem("when-complete"));
        promises.addItem(diBuilder.getDocItem("accept-either"));
        promises.addItem(diBuilder.getDocItem("apply-to-either"));
        promises.addItem(diBuilder.getDocItem("all-of"));
        promises.addItem(diBuilder.getDocItem("any-of"));
        promises.addItem(diBuilder.getDocItem("or-timeout", true, true));
        promises.addItem(diBuilder.getDocItem("complete-on-timeout", true, true));
        promises.addItem(diBuilder.getDocItem("timeout-after", true, true));
        promises.addItem(diBuilder.getDocItem("done?"));
        promises.addItem(diBuilder.getDocItem("cancel"));
        promises.addItem(diBuilder.getDocItem("cancelled?"));

        final DocSection delay = new DocSection("Delay", "concurrency.delay");
        all.addSection(delay);
        delay.addItem(diBuilder.getDocItem("delay"));
        delay.addItem(diBuilder.getDocItem("delay?"));
        delay.addItem(diBuilder.getDocItem("deref"));
        delay.addItem(diBuilder.getDocItem("deref?"));
        delay.addItem(diBuilder.getDocItem("force"));
        delay.addItem(diBuilder.getDocItem("realized?"));

        final DocSection agents = new DocSection("Agents", "concurrency.agents");
        all.addSection(agents);
        agents.addItem(diBuilder.getDocItem("agent"));
        agents.addItem(diBuilder.getDocItem("send"));
        agents.addItem(diBuilder.getDocItem("send-off"));
        agents.addItem(diBuilder.getDocItem("restart-agent"));
        agents.addItem(diBuilder.getDocItem("set-error-handler!"));
        agents.addItem(diBuilder.getDocItem("agent-error"));
        agents.addItem(diBuilder.getDocItem("await"));
        agents.addItem(diBuilder.getDocItem("await-for"));
        agents.addItem(diBuilder.getDocItem("shutdown-agents", false));
        agents.addItem(diBuilder.getDocItem("shutdown-agents?", false));
        agents.addItem(diBuilder.getDocItem("await-termination-agents", false));
        agents.addItem(diBuilder.getDocItem("await-termination-agents?", false));
        agents.addItem(diBuilder.getDocItem("agent-send-thread-pool-info"));
        agents.addItem(diBuilder.getDocItem("agent-send-off-thread-pool-info"));


        final DocSection sched = new DocSection("Scheduler", "concurrency.scheduler");
        all.addSection(sched);
        sched.addItem(diBuilder.getDocItem("schedule-delay", false));
        sched.addItem(diBuilder.getDocItem("schedule-at-fixed-rate", false));

        final DocSection volatiles = new DocSection("Volatiles", "concurrency.volatiles");
        all.addSection(volatiles);
        volatiles.addItem(diBuilder.getDocItem("volatile"));
        volatiles.addItem(diBuilder.getDocItem("volatile?"));
        volatiles.addItem(diBuilder.getDocItem("deref"));
        volatiles.addItem(diBuilder.getDocItem("deref?"));
        volatiles.addItem(diBuilder.getDocItem("reset!"));
        volatiles.addItem(diBuilder.getDocItem("swap!"));

        final DocSection thlocal = new DocSection("ThreadLocal", "concurrency.threadlocal");
        all.addSection(thlocal);
        thlocal.addItem(diBuilder.getDocItem("thread-local"));
        thlocal.addItem(diBuilder.getDocItem("thread-local?"));
        thlocal.addItem(diBuilder.getDocItem("thread-local-clear"));
        thlocal.addItem(diBuilder.getDocItem("thread-local-map"));
        thlocal.addItem(diBuilder.getDocItem("assoc"));
        thlocal.addItem(diBuilder.getDocItem("dissoc"));
        thlocal.addItem(diBuilder.getDocItem("get"));
        thlocal.addItem(diBuilder.getDocItem("binding"));
        thlocal.addItem(diBuilder.getDocItem("def-dynamic"));

        final DocSection threads = new DocSection("Threads", "concurrency.threads");
        all.addSection(threads);
        threads.addItem(diBuilder.getDocItem("thread"));
        threads.addItem(diBuilder.getDocItem("thread-id"));
        threads.addItem(diBuilder.getDocItem("thread-name"));
        threads.addItem(diBuilder.getDocItem("thread-daemon?"));
        threads.addItem(diBuilder.getDocItem("thread-interrupted?"));
        threads.addItem(diBuilder.getDocItem("thread-interrupted"));

        final DocSection parallel = new DocSection("Parallel", "concurrency.parallel");
        all.addSection(parallel);
        parallel.addItem(diBuilder.getDocItem("pcalls"));
        parallel.addItem(diBuilder.getDocItem("pmap"));
        parallel.addItem(diBuilder.getDocItem("preduce"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
