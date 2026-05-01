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
package com.github.jlangch.venice.util.openai;

import java.util.List;
import java.util.Objects;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;


public class ChatCompletionTraditionalRequest {

    private ChatCompletionTraditionalRequest(
            final OpenAIClient client,
            final ChatModel model
    ) {
        this.client = client;
        this.model = model;
        this.paramsBuilder = ChatCompletionCreateParams
                                .builder()
                                .model(model);
    }

    public static ChatCompletionTraditionalRequest of (
            final OpenAIClient client,
            final ChatModel model
    ) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(model);

        return new ChatCompletionTraditionalRequest(client, model);
    }

    public ChatCompletionTraditionalRequest maxCompletionTokens(final long maxCompletionTokens) {
        this.paramsBuilder.maxCompletionTokens(maxCompletionTokens);
        return this;
    }

    public ChatCompletionTraditionalRequest addUserMessage(final String text) {
        Objects.requireNonNull(text);
        this.paramsBuilder.addUserMessage(text);
        return this;
    }

    public ChatCompletionTraditionalRequest addUserMessages(final List<String> texts) {
        Objects.requireNonNull(texts);
        texts.forEach(t -> this.paramsBuilder.addUserMessage(t));
        return this;
    }

    public ChatCompletionTraditionalRequest addAssistantMessage(final String text) {
        Objects.requireNonNull(text);
        this.paramsBuilder.addAssistantMessage(text);
        return this;
    }

    public ChatCompletionTraditionalRequest addAssistantMessages(final List<String> texts) {
        Objects.requireNonNull(texts);
        texts.forEach(t -> this.paramsBuilder.addAssistantMessage(t));
        return this;
    }

    public ChatCompletionTraditionalRequest addSystemMessage(final String text) {
        Objects.requireNonNull(text);
        this.paramsBuilder.addSystemMessage(text);
        return this;
    }

    public ChatCompletionTraditionalRequest addSystemMessages(final List<String> texts) {
        Objects.requireNonNull(texts);
        texts.forEach(t -> this.paramsBuilder.addSystemMessage(t));
        return this;
    }

    public ChatCompletionTraditionalRequest addDeveloperMessage(final String text) {
        Objects.requireNonNull(text);
        this.paramsBuilder.addDeveloperMessage(text);
        return this;
    }

    public ChatCompletionTraditionalRequest addDeveloperMessages(final List<String> texts) {
        Objects.requireNonNull(texts);
        texts.forEach(t -> this.paramsBuilder.addDeveloperMessage(t));
        return this;
    }

    public OpenAIClient getClient() {
        return client;
    }

    public ChatModel getModel() {
        return model;
    }

    public ChatCompletionCreateParams.Builder getParamsBuilder() {
        return paramsBuilder;
    }

    public ChatCompletionTraditionalResponse execute() {
       final ChatCompletion completion = client.chat()
                                                .completions()
                                                .create(paramsBuilder.build());

        return new ChatCompletionTraditionalResponse(this, completion);
    }


    public static enum MessageType {User, Assistant, Developer, System};

    private final OpenAIClient client;
    private final ChatModel model;
    private final ChatCompletionCreateParams.Builder paramsBuilder;
}
