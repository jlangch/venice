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


public class ModuleOpenAiSection implements ISectionBuilder {

    public ModuleOpenAiSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
        								"OpenAI Client",
        								"modules.openai");

        final DocSection all = new DocSection("(load-module :openai)", id());
        section.addSection(all);

        final DocSection chat = new DocSection("OpenAI chat", id());
        all.addSection(chat);
        chat.addItem(diBuilder.getDocItem("openai/chat-completion", false));
        chat.addItem(diBuilder.getDocItem("openai/chat-completion-streaming", false));
        chat.addItem(diBuilder.getDocItem("openai/process-streaming-events", false));

        final DocSection func = new DocSection("OpenAI functions", id());
        all.addSection(func);
        func.addItem(diBuilder.getDocItem("openai/exec-functions-stop?", false));
        func.addItem(diBuilder.getDocItem("openai/exec-functions-tool-calls?", false));
        func.addItem(diBuilder.getDocItem("openai/exec-functions", false));

        final DocSection utils = new DocSection("Utils", id());
        all.addSection(utils);
        utils.addItem(diBuilder.getDocItem("openai/pretty-print-json", false));
        utils.addItem(diBuilder.getDocItem("openai/extract-response-message-content", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
