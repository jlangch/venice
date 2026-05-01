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

import static com.github.jlangch.venice.impl.util.CollectionUtil.toList;
import static com.github.jlangch.venice.impl.util.CollectionUtil.toMap;
import static com.openai.core.ObjectMappers.jsonMapper;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;


public class ChatCompletionTraditionalFunctionExample {

    private ChatCompletionTraditionalFunctionExample() {}

    public static void main(String[] args) {
        final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        final ChatCompletionTraditionalRequest request =
                ChatCompletionTraditionalRequest
                    .of(client, ChatModel.GPT_5_4, new FunctionDispatcher())
                    .maxCompletionTokens(2048)
                    .debug(true)
                    .addFunction(
                        "get-sdk-quality",
                        "Gets the quality of the given SDK.",
                        toMap("name", toMap("type", "string")),
                        toList("name"))
                    .addUserMessage("How good are the following SDKs: OpenAI Java SDK, Unknown Company SDK");

        ChatCompletionTraditionalResponse response = request.execute();

        print(response);

        System.out.println("\n\n==================================================\n\n");

        request.addAssistantMessages(response.getMessages());
        request.addUserMessage("Please list the SDK valuation and explain the results!");
        response = request.execute();

//        request.addAssistantMessages(response.getMessages());
//        request.addUserMessage("Why do you say that?");
//        response = request.execute();

        print(response);
    }

    private static void print(final ChatCompletionTraditionalResponse response) {
        System.out.println("USAGE: " + response.getUsage());
        System.out.println();

        final List<String> messages = response.getMessages();
        if (!messages.isEmpty()) {
            System.out.println(messages.get(0));
            for(int ii=1; ii<messages.size(); ii++) {
                System.out.println("\n--------------------------------------------------\n");
                System.out.println(messages.get(ii));
            }
        }
    }


    private static class FunctionDispatcher implements IFunctionDispatcher {
        @Override
        public String call(String fnName, String fnArgsJson) {
            if ("get-sdk-quality".equals(fnName)) {
                JsonValue arguments;
                try {
                    arguments = JsonValue.from(jsonMapper().readTree(fnArgsJson));
                }
                catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Bad function arguments", e);
                }

                String sdkName = ((JsonObject)arguments).values().get("name").asStringOrThrow();
                if (sdkName.contains("OpenAI")) {
                    return sdkName + ": It's robust and polished!";
                }

                return sdkName + ": *shrug*";
            }

            throw new RuntimeException("No function available for name '" + fnName + "'");
        }
    }

}
