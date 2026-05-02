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
import java.util.Map;
import java.util.Objects;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.ResponseFormatJsonSchema.JsonSchema;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;


public class ChatCompletionTraditionalRequest {

    private ChatCompletionTraditionalRequest(
            final OpenAIClient client,
            final ChatModel model,
            final IFunctionDispatcher functionDispatcher
    ) {
        this.client = client;
        this.model = model;
        this.functionDispatcher = functionDispatcher;
        this.paramsBuilder = ChatCompletionCreateParams
                                .builder()
                                .model(model);
    }

    public static ChatCompletionTraditionalRequest of (
            final OpenAIClient client,
            final ChatModel model
    ) {
        return ChatCompletionTraditionalRequest.of(client, model, null);
    }

    public static ChatCompletionTraditionalRequest of (
            final OpenAIClient client,
            final ChatModel model,
            final IFunctionDispatcher functionDispatcher
    ) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(model);

        return new ChatCompletionTraditionalRequest(
                        client,
                        model,
                        functionDispatcher == null
                            ?  new DefaultFunctionDispatcher()
                            : functionDispatcher);
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

    public ChatCompletionTraditionalRequest addFunction(
            final String name,
            final String description,
            final String type,
            final Map<String,Map<String,Object>> properties,
            final List<String> requiredProperties
    ) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
        Objects.requireNonNull(properties);
        Objects.requireNonNull(requiredProperties);

        final FunctionParameters.Builder paramBuilder = FunctionParameters.builder();
        paramBuilder.putAdditionalProperty("type", JsonValue.from(type));
        paramBuilder.putAdditionalProperty("properties", JsonValue.from(properties));
        paramBuilder.putAdditionalProperty("required", JsonValue.from(requiredProperties));
        paramBuilder.putAdditionalProperty("additionalProperties", JsonValue.from(false));

        final FunctionDefinition.Builder fnBuilder = FunctionDefinition.builder();
        fnBuilder.name(name);
        fnBuilder.description(description);
        fnBuilder.parameters(paramBuilder.build());

        paramsBuilder.addTool(ChatCompletionFunctionTool
                                .builder()
                                .function(fnBuilder.build())
                                .build());

        return this;
    }

    public ChatCompletionTraditionalRequest jsonResponseFormat(
            final String name,
            final String description,
            final String type,
            final Map<String,Map<String,Object>> properties
    ) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
        Objects.requireNonNull(type);
        Objects.requireNonNull(properties);

        final JsonSchema.Schema.Builder schemaBuilder = JsonSchema.Schema.builder();
        schemaBuilder.putAdditionalProperty("type", JsonValue.from(type));
        schemaBuilder.putAdditionalProperty("properties", JsonValue.from(properties));

        paramsBuilder.responseFormat(ResponseFormatJsonSchema
                                        .builder()
                                        .jsonSchema(JsonSchema
                                                        .builder()
                                                        .name(name)
                                                        .description(description)
                                                        .schema(schemaBuilder.build())
                                                        .build())
                                        .build());

        return this;
    }

    public ChatCompletionTraditionalRequest debug(final boolean on) {
        this.debug = on;
        return this;
    }

    public OpenAIClient getClient() {
        return client;
    }

    public ChatModel getModel() {
        return model;
    }

    public IFunctionDispatcher getIFunctionDispatcher() {
        return functionDispatcher;
    }

    public ChatCompletionCreateParams.Builder getParamsBuilder() {
        return paramsBuilder;
    }

    public boolean isDebug() {
        return debug;
    }

    public long getStartTimeMillis() {
        return startMillis;
    }

    public long elapsed() {
        return System.currentTimeMillis() - startMillis;
    }

    public ChatCompletionTraditionalResponse execute() {
        final ChatCompletion completion = client.chat()
                                                .completions()
                                                .create(paramsBuilder.build());

        final ChatCompletionTraditionalResponse response =
                    new ChatCompletionTraditionalResponse(this, completion);

        if (response.hasToolCalls()) {
            response.processToolCalls();
        }

        return response;
    }

    private static class DefaultFunctionDispatcher implements IFunctionDispatcher {
        @Override
        public String call(String fnName, String fnArgsJson) {
            throw new RuntimeException("No function available for name '" + fnName + "'");
        }
    }


    public static enum MessageType {User, Assistant, Developer, System};

    private volatile boolean debug;

    private final long startMillis = System.currentTimeMillis();
    private final OpenAIClient client;
    private final ChatModel model;
    private final IFunctionDispatcher functionDispatcher;
    private final ChatCompletionCreateParams.Builder paramsBuilder;
}
