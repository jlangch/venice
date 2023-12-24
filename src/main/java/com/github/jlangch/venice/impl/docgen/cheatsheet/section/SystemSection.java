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


public class SystemSection implements ISectionBuilder {

    public SystemSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("System", "system");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection venice = new DocSection("Venice", "system.venice");
        all.addSection(venice);
        venice.addItem(diBuilder.getDocItem("version"));

        final DocSection system = new DocSection("System", "system.system");
        all.addSection(system);
        system.addItem(diBuilder.getDocItem("system-prop"));
        system.addItem(diBuilder.getDocItem("system-env"));
        system.addItem(diBuilder.getDocItem("system-exit-code", false));
        system.addItem(diBuilder.getDocItem("shutdown-hook", false));
        system.addItem(diBuilder.getDocItem("charset-default-encoding"));

        final DocSection java = new DocSection("Java", "system.java");
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("java-version"));
        java.addItem(diBuilder.getDocItem("java-version-info"));
        java.addItem(diBuilder.getDocItem("java-major-version"));
        java.addItem(diBuilder.getDocItem("java-source-location", false));

        final DocSection javaVM = new DocSection("Java VM", "system.java-vm");
        all.addSection(javaVM);
        javaVM.addItem(diBuilder.getDocItem("pid"));
        javaVM.addItem(diBuilder.getDocItem("gc"));
        javaVM.addItem(diBuilder.getDocItem("total-memory"));
        javaVM.addItem(diBuilder.getDocItem("used-memory"));

        final DocSection os = new DocSection("OS", "system.os");
        all.addSection(os);
        os.addItem(diBuilder.getDocItem("os-type"));
        os.addItem(diBuilder.getDocItem("os-type?"));
        os.addItem(diBuilder.getDocItem("os-arch"));
        os.addItem(diBuilder.getDocItem("os-name"));
        os.addItem(diBuilder.getDocItem("os-version"));

        final DocSection jansi = new DocSection("Jansi", "system.jansi");
        all.addSection(jansi);
        jansi.addItem(diBuilder.getDocItem("jansi-version"));

        final DocSection time = new DocSection("Time", "system.time");
        all.addSection(time);
        time.addItem(diBuilder.getDocItem("current-time-millis"));
        time.addItem(diBuilder.getDocItem("nano-time"));
        time.addItem(diBuilder.getDocItem("format-nano-time"));
        time.addItem(diBuilder.getDocItem("format-micro-time"));
        time.addItem(diBuilder.getDocItem("format-milli-time"));

        final DocSection host = new DocSection("Host", "system.host");
        all.addSection(host);
        host.addItem(diBuilder.getDocItem("host-name"));
        host.addItem(diBuilder.getDocItem("host-address"));
        host.addItem(diBuilder.getDocItem("ip-private?"));
        host.addItem(diBuilder.getDocItem("cpus"));
        host.addItem(diBuilder.getDocItem("byte-order"));

        final DocSection user = new DocSection("User", "system.user");
        all.addSection(user);
        user.addItem(diBuilder.getDocItem("user-name"));
        user.addItem(diBuilder.getDocItem("io/user-home-dir"));

        final DocSection util = new DocSection("Util", "system.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("uuid"));
        util.addItem(diBuilder.getDocItem("sleep"));

        final DocSection services = new DocSection("Services", "service");
        all.addSection(services);
        services.addItem(diBuilder.getDocItem("service", false));
        services.addItem(diBuilder.getDocItem("service?", false));

        final DocSection shell = new DocSection("Shell", "system.shell");
        all.addSection(shell);
        shell.addItem(diBuilder.getDocItem("sh", false));
        shell.addItem(diBuilder.getDocItem("with-sh-dir", false));
        shell.addItem(diBuilder.getDocItem("with-sh-env", false));
        shell.addItem(diBuilder.getDocItem("with-sh-throw", false));

        final DocSection tools = new DocSection("Shell Tools", "system.shell.tools");
        all.addSection(tools);
        tools.addItem(diBuilder.getDocItem("sh/open", false));
        tools.addItem(diBuilder.getDocItem("sh/pwd", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
