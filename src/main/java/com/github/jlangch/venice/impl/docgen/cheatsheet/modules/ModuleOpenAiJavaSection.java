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


public class ModuleOpenAiJavaSection implements ISectionBuilder {

    public ModuleOpenAiJavaSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "OpenAI Client",
                                        "modules.openai-java");

        final DocSection all = new DocSection("(load-module :openai-java)", id());
        section.addSection(all);

        final DocSection client = new DocSection("Client", id());
        all.addSection(client);
        client.addItem(diBuilder.getDocItem("openai-java/client", false));
        client.addItem(diBuilder.getDocItem("openai-java/close", false));
        client.addItem(diBuilder.getDocItem("openai-java/version", false));

        final DocSection chat = new DocSection("Chat", id());
        all.addSection(chat);
        chat.addItem(diBuilder.getDocItem("openai-java/chat-completion", false));
        chat.addItem(diBuilder.getDocItem("openai-java/max-completion-tokens", false));
        chat.addItem(diBuilder.getDocItem("openai-java/add-user-message", false));
        chat.addItem(diBuilder.getDocItem("openai-java/add-user-message-with-files", false));
        chat.addItem(diBuilder.getDocItem("openai-java/add-assistant-message", false));
        chat.addItem(diBuilder.getDocItem("openai-java/json-response-format", false));
        chat.addItem(diBuilder.getDocItem("openai-java/add-function", false));
        chat.addItem(diBuilder.getDocItem("openai-java/execute", false));

        final DocSection response = new DocSection("Response", id());
        all.addSection(response);
        response.addItem(diBuilder.getDocItem("openai-java/messages", false));
        response.addItem(diBuilder.getDocItem("openai-java/usage", false));

        final DocSection files = new DocSection("Files", id());
        all.addSection(files);
        files.addItem(diBuilder.getDocItem("openai-java/create-file-object", false));
        files.addItem(diBuilder.getDocItem("openai-java/create-file-object-binary", false));

        final DocSection utils = new DocSection("Utils", id());
        all.addSection(utils);
        utils.addItem(diBuilder.getDocItem("openai-java/models", false));
        utils.addItem(diBuilder.getDocItem("openai-java/create-function-registry", false));
        utils.addItem(diBuilder.getDocItem("openai-java/register-function", false));
        utils.addItem(diBuilder.getDocItem("openai-java/format-usage", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
