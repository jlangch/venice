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

import java.util.LinkedHashMap;
import java.util.Objects;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.openai.models.completions.CompletionUsage;
import com.openai.models.completions.CompletionUsage.CompletionTokensDetails;
import com.openai.models.images.ImagesResponse.Usage.OutputTokensDetails;


public class TokenUsage {

    public TokenUsage() {
    }

    public TokenUsage(
            final long inputTokens,
            final long outputTokens,
            final long totalTokens,
            // input details
            final long inputDetails_ImageTokens,
            final long inputDetails_TextTokens,
            // output details
            final long outputDetails_ReasoningTokens,
            final long outputDetails_ImageTokens,
            final long outputDetails_TextTokens

    ) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;

        this.inputDetails_ImageTokens = inputDetails_ImageTokens;
        this.inputDetails_TextTokens = inputDetails_TextTokens;

        this.outputDetails_ReasoningTokens = outputDetails_ReasoningTokens;
        this.outputDetails_ImageTokens = outputDetails_ImageTokens;
        this.outputDetails_TextTokens = outputDetails_TextTokens;
}

    public static TokenUsage of(final CompletionUsage usage) {
        Objects.requireNonNull(usage);

        final CompletionTokensDetails details = usage.completionTokensDetails().get();

        final long reasoningTokens = details == null ? 0L : details.reasoningTokens().orElse(0L);

        return new TokenUsage(
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens(),
                0, 0, reasoningTokens, 0, 0);
    }

    public static TokenUsage of(final com.openai.models.images.ImagesResponse.Usage usage) {
        Objects.requireNonNull(usage);

        final OutputTokensDetails details = usage.outputTokensDetails().get();

        final long imageTokens = details == null ? 0 : details.imageTokens();
        final long textTokens = details == null ? 0 : details.textTokens();

        return new TokenUsage(
                usage.inputTokens(),
                usage.outputTokens(),
                usage.totalTokens(),
                0, 0, 0, imageTokens, textTokens);
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getInputDetails_ImageTokens() {
        return inputDetails_ImageTokens;
    }

    public long getInputDetails_TextTokens() {
        return inputDetails_TextTokens;
    }

    public long getOutputDetails_ReasoningTokens() {
        return outputDetails_ReasoningTokens;
    }

    public long getOutputDetails_ImageTokens() {
        return outputDetails_ImageTokens;
    }

    public long getOutputDetails_TextTokens() {
        return outputDetails_TextTokens;
    }

    public LinkedHashMap<String,Object> toMap() {
        final LinkedHashMap<String,Object> tokens = new LinkedHashMap<>();

        tokens.put("inputTokens", inputTokens);
        tokens.put("outputTokens", outputTokens);
        tokens.put("totalTokens", totalTokens);

        final LinkedHashMap<String,Object> inputDetails = new LinkedHashMap<>();
        inputDetails.put("imageTokens", inputDetails_ImageTokens);
        inputDetails.put("textTokens", inputDetails_TextTokens);
        tokens.put("inputDetails", inputDetails);

        final LinkedHashMap<String,Object> outputDetails = new LinkedHashMap<>();
        outputDetails.put("reasoningTokens", outputDetails_ReasoningTokens);
        outputDetails.put("mageTokens", outputDetails_ImageTokens);
        outputDetails.put("textTokens", outputDetails_TextTokens);
        tokens.put("outputDetails", outputDetails);

        return tokens;
    }

    public VncMap toVncMap() {
        VncOrderedMap tokens = new VncOrderedMap();

        tokens = tokens.assoc(
            new VncKeyword("input-tokens"), new VncLong(inputTokens),
            new VncKeyword("output-tokens"), new VncLong(outputTokens),
            new VncKeyword("total-tokens"), new VncLong(totalTokens));

        VncOrderedMap inputDetails = new VncOrderedMap();
        inputDetails = inputDetails.assoc(
                new VncKeyword("image-tokens"), new VncLong(inputDetails_ImageTokens),
                new VncKeyword("text-tokens"), new VncLong(inputDetails_TextTokens));

        VncOrderedMap outputDetails = new VncOrderedMap();
        outputDetails = outputDetails.assoc(
                new VncKeyword("reasoning-tokens"), new VncLong(outputDetails_ReasoningTokens),
                new VncKeyword("image-tokens"), new VncLong(outputDetails_ImageTokens),
                new VncKeyword("text-tokens"), new VncLong(outputDetails_TextTokens));

        tokens = tokens.assoc(new VncKeyword("input-details"), inputDetails);
        tokens = tokens.assoc(new VncKeyword("output-details"), outputDetails);

        return tokens;
    }

    @Override
    public String toString() {
        return "inputTokens: " + inputTokens + ", " +
               "outputTokens: " + outputTokens + ", " +
               "totalTokens: " + totalTokens + ", " +
               "inputDetails_ImageTokens: " + inputDetails_ImageTokens + ", " +
               "inputDetails_TextTokens: " + inputDetails_TextTokens + ", " +
               "outputDetails_ReasoningTokens: " + outputDetails_ReasoningTokens + ", " +
               "outputDetails_ImageTokens: " + outputDetails_ImageTokens + ", " +
               "outputDetails_TextTokens: " + outputDetails_TextTokens;
    }


    private long inputTokens;
    private long outputTokens;
    private long totalTokens;

    // input details
    private long inputDetails_ImageTokens;
    private long inputDetails_TextTokens;

    // output details
    private long outputDetails_ReasoningTokens;
    private long outputDetails_ImageTokens;
    private long outputDetails_TextTokens;
}
