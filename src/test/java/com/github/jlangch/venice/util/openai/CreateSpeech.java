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


import java.io.InputStream;

import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.HttpResponse;
import com.openai.models.audio.speech.SpeechCreateParams;
import com.openai.models.audio.speech.SpeechCreateParams.ResponseFormat;
import com.openai.models.audio.speech.SpeechModel;


public final class CreateSpeech {
    private CreateSpeech() {}

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        SpeechCreateParams params = SpeechCreateParams.builder()
            .input("Today is a wonderful day to build something people love!")
            .instructions("Speak in a cheerful and positive tone.")
            .model(SpeechModel.GPT_4O_MINI_TTS)
            .responseFormat(ResponseFormat.WAV)
            .voice("cedar")  // marin
            .build();

        HttpResponse speech = client.audio().speech().create(params);
        try (final InputStream is = speech.body()) {
            final byte[] audio = IOStreamUtil.copyIStoByteArray(is);
        }
        catch(Exception ex) {

        }
    }
}