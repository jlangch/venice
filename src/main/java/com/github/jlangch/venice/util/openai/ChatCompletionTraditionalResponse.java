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
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;


public class ChatCompletionTraditionalResponse {

    public ChatCompletionTraditionalResponse(
            final ChatCompletionTraditionalRequest request,
            final ChatCompletion completion
    ) {
        this.functionDispatcher = request.getIFunctionDispatcher();
        this.request = request;
        this.completion = completion;
    }

    public ChatCompletionTraditionalRequest getRequest() {
        return request;
    }

    public ChatCompletion getChatCompletion() {
        return completion;
    }

    public List<String> getMessages() {
        return completion
                    .choices()
                    .stream()
                    .map(choice -> choice.message().content())
                    .filter(text -> text.isPresent())
                    .map(text -> text.get())
                    .collect(Collectors.toList());
    }

    public String firstMessage() {
        final List<String> messages = getMessages();
        return messages.isEmpty() ? null : messages.get(0);
    }

    public String lastMessage() {
        final List<String> messages = getMessages();
        return messages.isEmpty() ? null : messages.get(messages.size()-1);
    }

    public TokenUsage getUsage() {
        return TokenUsage.of(completion.usage().get());
    }

    public boolean hasToolCalls() {
       return !getToolCalls().isEmpty();
    }

    public void processToolCalls() {
        final ChatCompletionCreateParams.Builder paramsBuilder = request.getParamsBuilder();

        // Add each assistant message onto the builder so that we keep track of the conversation
        // for asking a follow-up question later.
        completion.choices()
                  .stream()
                  .map(ChatCompletion.Choice::message)
                  .forEach(m -> paramsBuilder.addMessage(m));

        final List<ChatCompletionMessageToolCall> toolCalls = getToolCalls();
        for(ChatCompletionMessageToolCall toolCall : toolCalls) {
            final String fnResult = callFunction(toolCall.asFunction().function());

            // Add the tool call result to the conversation.
            paramsBuilder
               .addMessage(ChatCompletionToolMessageParam
                    .builder()
                    .toolCallId(toolCall.asFunction().id())
                    .content(fnResult)
                    .build());
        }
    }


    private List<ChatCompletionMessageToolCall> getToolCalls() {
        return completion
                    .choices()
                    .stream()
                    .map(choice -> choice.message().toolCalls())
                    .filter(call -> call.isPresent())
                    .map(call -> call.get())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
    }

    private String callFunction(ChatCompletionMessageFunctionToolCall.Function function) {
        final String fnName = function.name();
        final String fnArgsJson = function.arguments();

        final String result = functionDispatcher.call(fnName, fnArgsJson);

        if (request.isDebug()) {
            System.out.println();
            System.out.println("FUNCTION CALL");
            System.out.println("  NAME:   " + fnName);
            System.out.println("  ARGS:   " + StringUtil.truncate(fnArgsJson, 100, "..."));
            System.out.println("  RESULT: " + StringUtil.truncate(result, 100, "..."));
            System.out.println();
        }

        return result;
    }


    private final ChatCompletionTraditionalRequest request;
    private final ChatCompletion completion;
    private final IFunctionDispatcher functionDispatcher;
}
