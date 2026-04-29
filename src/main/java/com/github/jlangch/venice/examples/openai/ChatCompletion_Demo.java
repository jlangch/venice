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
package com.github.jlangch.venice.examples.openai;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputMessage.Status;
import com.openai.models.responses.ResponseUsage;


public class ChatCompletion_Demo {

    public static void main(String[] args) {

        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ResponseCreateParams params = ResponseCreateParams
                                        .builder()
                                        .input("Say this is a test")
                                        .model(ChatModel.GPT_5_4)
                                        .build();
        Response response = client.responses().create(params);

        final ResponseUsage usage = response.usage().orElse(null);
        if (usage != null) {
            System.out.println("Input Tokens:     " + usage.inputTokens());
            System.out.println("Ouput Tokens:     " + usage.outputTokens());
            System.out.println("Reasoning Tokens: " + usage.outputTokensDetails().reasoningTokens());
            System.out.println("Total Tokens:     " + usage.totalTokens());
        }

        System.out.println();

        response
            .output()
            .stream()
            .filter(outputItem -> outputItem.isMessage())
            .map(outputItem -> outputItem.asMessage())
            .filter(msg -> msg.status() == Status.COMPLETED)
            .flatMap(msg -> msg.content().stream())
            .filter(content -> content.isOutputText())
            .map(content -> content.asOutputText())
            .map(outText -> outText.text())
            .forEach(text -> System.out.println(text));
    }

}
