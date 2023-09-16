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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleDockerSection implements ISectionBuilder {

    public ModuleDockerSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Docker", "modules.docker");

        final DocSection all = new DocSection("(load-module :docker)", id());
        section.addSection(all);

        final DocSection docker = new DocSection("Docker", id());
        all.addSection(docker);
        docker.addItem(diBuilder.getDocItem("docker/version", false));
        docker.addItem(diBuilder.getDocItem("docker/cmd", false));
        docker.addItem(diBuilder.getDocItem("docker/debug", false));

        final DocSection images = new DocSection("Images", id());
        all.addSection(images);
        images.addItem(diBuilder.getDocItem("docker/images", false));
        images.addItem(diBuilder.getDocItem("docker/image-pull", false));
        images.addItem(diBuilder.getDocItem("docker/rmi", false));
        images.addItem(diBuilder.getDocItem("docker/image-rm", false));
        images.addItem(diBuilder.getDocItem("docker/image-prune", false));

        final DocSection containers = new DocSection("Containers", id());
        all.addSection(containers);
        containers.addItem(diBuilder.getDocItem("docker/run", false));
        containers.addItem(diBuilder.getDocItem("docker/ps", false));
        containers.addItem(diBuilder.getDocItem("docker/start", false));
        containers.addItem(diBuilder.getDocItem("docker/stop", false));
        containers.addItem(diBuilder.getDocItem("docker/exec", false));
        containers.addItem(diBuilder.getDocItem("docker/rm", false));
        containers.addItem(diBuilder.getDocItem("docker/prune", false));
        containers.addItem(diBuilder.getDocItem("docker/cp", false));
        containers.addItem(diBuilder.getDocItem("docker/diff", false));
        containers.addItem(diBuilder.getDocItem("docker/pause", false));
        containers.addItem(diBuilder.getDocItem("docker/unpause", false));
        containers.addItem(diBuilder.getDocItem("docker/wait", false));
        containers.addItem(diBuilder.getDocItem("docker/logs", false));

        final DocSection volume = new DocSection("Volumes", id());
        all.addSection(volume);
        volume.addItem(diBuilder.getDocItem("docker/volume-list", false));
        volume.addItem(diBuilder.getDocItem("docker/volume-create", false));
        volume.addItem(diBuilder.getDocItem("docker/volume-rm", false));
        volume.addItem(diBuilder.getDocItem("docker/volume-exists?", false));

        final DocSection utils = new DocSection("Utils", id());
        all.addSection(utils);
        utils.addItem(diBuilder.getDocItem("docker/images-query-by-repo", false));
        utils.addItem(diBuilder.getDocItem("docker/image-ready?", false));
        utils.addItem(diBuilder.getDocItem("docker/container-find-by-name", false));
        utils.addItem(diBuilder.getDocItem("docker/container-exists-with-name?", false));
        utils.addItem(diBuilder.getDocItem("docker/container-running-with-name?", false));
        utils.addItem(diBuilder.getDocItem("docker/container-start-by-name", false));
        utils.addItem(diBuilder.getDocItem("docker/container-stop-by-name", false));
        utils.addItem(diBuilder.getDocItem("docker/container-status-by-name", false));
        utils.addItem(diBuilder.getDocItem("docker/container-exec-by-name", false));
        utils.addItem(diBuilder.getDocItem("docker/container-has-log-msg", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
