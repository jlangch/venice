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


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.FunctionCallOutput;
import com.openai.models.responses.ResponseOutputItem;



public class FunctionCall_1_Demo {

    @JsonClassDescription("Gets the current weather for a city.")
    public static class GetWeather {

        @JsonPropertyDescription("City and country, for example: Zurich, Switzerland")
        public String location;

        @JsonPropertyDescription("Temperature unit: celsius or fahrenheit")
        public String unit;

        public WeatherResult execute() {
            // Replace this with your real service call.
            return new WeatherResult(location, unit, 21, "Partly cloudy");
        }
    }

    public static class WeatherResult {
        public String location;
        public String unit;
        public int temperature;
        public String conditions;

        public WeatherResult(
                final String location,
                final String unit,
                final int temperature,
                final String conditions
        ) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.conditions = conditions;
        }
    }

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ResponseCreateParams params = ResponseCreateParams.builder()
                                        .model(ChatModel.GPT_5_4)
                                        .input("What is the weather in Zurich in Celsius?")
                                        .addTool(GetWeather.class)
                                        .build();

        Response response = client.responses().create(params);

        List<ResponseInputItem> followUpInput = new ArrayList<>();

        for (ResponseOutputItem item : response.output()) {
            if (!item.isFunctionCall()) {
                continue;
            }

            ResponseFunctionToolCall functionCall = item.asFunctionCall();
            Object functionResult = callFunction(functionCall);

            followUpInput.add(ResponseInputItem.ofFunctionCall(functionCall));
            followUpInput.add(ResponseInputItem.ofFunctionCallOutput(
                    FunctionCallOutput.builder()
                        .callId(functionCall.callId())
                        .outputAsJson(functionResult)
                        .build()
            ));

        }

        if (followUpInput.isEmpty()) {
            System.out.println("No function call requested.");
            return;

        }

        ResponseCreateParams followUpParams = ResponseCreateParams.builder()
                                                .model(ChatModel.GPT_5_2)
                                                .inputOfResponse(followUpInput)
                                                .build();

        Response finalResponse = client.responses().create(followUpParams);

        // finalResponse.output().forEach(System.out::println);

        finalResponse
            .output()
            .stream()
            .filter(outputItem -> outputItem.isMessage())
            .map(outputItem -> outputItem.asMessage())
            //.filter(msg -> msg.status() == Status.COMPLETED)
            .flatMap(msg -> msg.content().stream())
            .filter(content -> content.isOutputText())
            .map(content -> content.asOutputText())
            .map(outText -> outText.text())
            .forEach(text -> System.out.println(text));
    }

    private static Object callFunction(ResponseFunctionToolCall functionCall) {
        switch (functionCall.name()) {
            case "GetWeather":
                GetWeather args = functionCall.arguments(GetWeather.class);
                return args.execute();

            default:
                throw new IllegalArgumentException("Unknown function: " + functionCall.name());
        }
    }
}
