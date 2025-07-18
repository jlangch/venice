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

        final DocSection util = new DocSection("Util", id());
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("openai/me", false));
        util.addItem(diBuilder.getDocItem("openai/openapi-yaml", false));

        final DocSection chat = new DocSection("Chat", id());
        all.addSection(chat);
        chat.addItem(diBuilder.getDocItem("openai/chat-completion", false));
        chat.addItem(diBuilder.getDocItem("openai/chat-completion-streaming", false));
        chat.addItem(diBuilder.getDocItem("openai/chat-process-streaming-events", false));

        final DocSection func = new DocSection("Chat Functions", id());
        all.addSection(func);
        func.addItem(diBuilder.getDocItem("openai/exec-fn", false));

        final DocSection response_chat = new DocSection("Chat Response", id());
        all.addSection(response_chat);
        response_chat.addItem(diBuilder.getDocItem("openai/chat-finish-reason", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-finish-reason-stop?", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-finish-reason-tool-calls?", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-extract-response-message", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-extract-response-message-role", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-extract-response-message-content", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-extract-response-tool-calls-id", false));
        response_chat.addItem(diBuilder.getDocItem("openai/chat-extract-function-name", false));

        final DocSection image = new DocSection("Image", id());
        all.addSection(image);
        image.addItem(diBuilder.getDocItem("openai/image-create", false));
        image.addItem(diBuilder.getDocItem("openai/image-variants", false));
        image.addItem(diBuilder.getDocItem("openai/image-edits", false));
        image.addItem(diBuilder.getDocItem("openai/image-download", false));

        final DocSection audio = new DocSection("Audio", id());
        all.addSection(audio);
        audio.addItem(diBuilder.getDocItem("openai/audio-speech-generate", false));
        audio.addItem(diBuilder.getDocItem("openai/audio-speech-transcribe", false));
        audio.addItem(diBuilder.getDocItem("openai/audio-speech-translate", false));
        audio.addItem(diBuilder.getDocItem("openai/audio-file-ext", false));

        final DocSection files = new DocSection("Files", id());
        all.addSection(files);
        files.addItem(diBuilder.getDocItem("openai/file-upload", false));
        files.addItem(diBuilder.getDocItem("openai/file-list", false));
        files.addItem(diBuilder.getDocItem("openai/file-retrieve", false));
        files.addItem(diBuilder.getDocItem("openai/file-delete", false));
        files.addItem(diBuilder.getDocItem("openai/file-retrieve-content", false));

        final DocSection models = new DocSection("Models", id());
        all.addSection(models);
        models.addItem(diBuilder.getDocItem("openai/model-list", false));
        models.addItem(diBuilder.getDocItem("openai/model-retrieve", false));
        models.addItem(diBuilder.getDocItem("openai/model-delete", false));

        final DocSection embedding = new DocSection("Embeddings", id());
        all.addSection(embedding);
        embedding.addItem(diBuilder.getDocItem("openai/embedding-create", false));

        final DocSection assistants = new DocSection("Assistants", id());
        all.addSection(assistants);
        assistants.addItem(diBuilder.getDocItem("openai/assistant-create", false));
        assistants.addItem(diBuilder.getDocItem("openai/assistant-list", false));
        assistants.addItem(diBuilder.getDocItem("openai/assistant-retrieve", false));
        assistants.addItem(diBuilder.getDocItem("openai/assistant-modify", false));
        assistants.addItem(diBuilder.getDocItem("openai/assistant-delete", false));

        final DocSection threads = new DocSection("Threads", id());
        all.addSection(threads);
        threads.addItem(diBuilder.getDocItem("openai/thread-create", false));
        threads.addItem(diBuilder.getDocItem("openai/thread-retrieve", false));

        final DocSection utils = new DocSection("Utils", id());
        all.addSection(utils);
        utils.addItem(diBuilder.getDocItem("openai/assert-response-http-ok", false));
        utils.addItem(diBuilder.getDocItem("openai/pretty-print-json", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
