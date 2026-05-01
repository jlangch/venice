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

import java.util.Objects;


public class ChatCompletionUsage {

    public ChatCompletionUsage() {
        this.inputTokens = 0;
        this.outputTokens = 0;
        this.outputReasoningTokens = 0;
        this.totalTokens = 0;
    }

    public ChatCompletionUsage(
            final long inputTokens,
            final long outputTokens,
            final long outputReasoningTokens,
            final long totalTokens
    ) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.outputReasoningTokens = outputReasoningTokens;
        this.totalTokens = totalTokens;
    }


    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public long getOutputReasoningTokens() {
        return outputReasoningTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public ChatCompletionUsage addUsage(final ChatCompletionUsage usage) {
        Objects.requireNonNull(usage);

        return new ChatCompletionUsage(
                    inputTokens + usage.inputTokens,
                    outputTokens + usage.outputTokens,
                    outputReasoningTokens + usage.outputReasoningTokens,
                    totalTokens + usage.totalTokens);
    }

    @Override
    public String toString() {
        return "inputTokens: " + inputTokens + ", " +
               "outputTokens: " + outputTokens + ", " +
               "outputReasoningTokens: " + outputReasoningTokens + ", " +
               "totalTokens: " + totalTokens;
    }


    private final long inputTokens;
    private final long outputTokens;
    private final long outputReasoningTokens;
    private final long totalTokens;
}
