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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.HttpResponse;
import com.openai.models.audio.AudioModel;
import com.openai.models.audio.AudioResponseFormat;
import com.openai.models.audio.speech.SpeechCreateParams;
import com.openai.models.audio.speech.SpeechCreateParams.ResponseFormat;
import com.openai.models.audio.speech.SpeechModel;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import com.openai.models.audio.transcriptions.TranscriptionCreateResponse;


public  class CreateTranscription {
    private CreateTranscription() {}

    // See https://developers.openai.com/api/reference/java/resources/audio/subresources/transcriptions/methods/create

    public static void main(String[] args)  throws Exception {
        File veniceHomeDir = new File(System.getProperty("user.home"), "Desktop/venice");
        File audioFile = new File(veniceHomeDir, "audio.wav");

        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        SpeechCreateParams paramsSpeech = SpeechCreateParams.builder()
                .input("Today is a wonderful day to build something people love!")
                .instructions("Speak in a cheerful and positive tone.")
                .model(SpeechModel.GPT_4O_MINI_TTS)
                .responseFormat(ResponseFormat.WAV)
                .voice("cedar")  // marin
                .build();

        HttpResponse speech = client.audio().speech().create(paramsSpeech);
        byte[] audio;
        try (final InputStream is = speech.body()) {
            audio = IOStreamUtil.copyIStoByteArray(is);

            Files.write(audioFile.toPath(), audio);
            System.out.println("Created audio file " + audioFile);
        }

        System.out.println("Transcribing audio file " + audioFile);

        TranscriptionCreateParams paramsTranscribe = TranscriptionCreateParams.builder()
            .file(audioFile.toPath())
                .language("en")
                .temperature(0.2)
            .model(AudioModel.GPT_4O_TRANSCRIBE)
            .responseFormat(AudioResponseFormat.TEXT)
            .build();

        TranscriptionCreateResponse response = client.audio().transcriptions().create(paramsTranscribe);


        Transcription transcription = response.transcription().orElse(null);
        if (transcription == null) {
            System.out.println("<no transcription>");
        }
        else {
            Transcription.Usage usage = transcription.usage().orElse(null);
            if (usage != null) {
                Transcription.Usage.Duration duration = usage.duration().orElse(null);
                Transcription.Usage.Tokens tokens = usage.tokens().orElse(null);
            }
            System.out.println("\nTranscription:");
            System.out.println(transcription.text());
        }
    }
}