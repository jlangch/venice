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

import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.completions.CompletionUsage;


public class ChatCompletionTraditionalResponse {

    public ChatCompletionTraditionalResponse(
            final ChatCompletionTraditionalRequest request,
            final ChatCompletion completion
    ) {
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

    public ChatCompletionUsage getUsage() {
        final CompletionUsage usage = completion.usage().get();

        return new ChatCompletionUsage(
                        usage.promptTokens(),
                        usage.completionTokens(),
                        usage.completionTokensDetails().isPresent()
                            ? usage.completionTokensDetails().get().reasoningTokens().orElse(0L)
                            : 0,
                        usage.totalTokens());
    }


    private final ChatCompletionTraditionalRequest request;
    private final ChatCompletion completion;
}
