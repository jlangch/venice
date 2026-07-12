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

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.ResponseInputFile;
import com.openai.models.responses.ResponseInputText;


public class ChatCompletionResponsesApiFileExample {

    private ChatCompletionResponsesApiFileExample() {}


    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        FileObject uploadedFile = client.files().create(
                FileCreateParams.builder()
                        .purpose(FilePurpose.USER_DATA)
                        .file(new File("document.pdf").toPath())
                        .build()

        );
        ResponseInputFile fileInput = ResponseInputFile.builder()
                .fileId(uploadedFile.id())
                .build();

        ResponseInputText textInput = ResponseInputText.builder()
                .text("Summarize this document.")
                .build();

//        ResponseCreateParams params = ResponseCreateParams.builder()
//                .model("gpt-5.6")
//                 .addInputItem(
//                        ResponseInputItem.ofMessage(
//                                ResponseInputItem.Message.builder()
//                                        .role(ResponseInputItem.Message.Role.USER)
//                                        .addContent(ResponseInputContent.ofInputFile(fileInput))
//                                        .addContent(ResponseInputContent.ofInputText(textInput))
//                                        .build()
//                        )
//                )
//                .build();
//
//        Response response = client.responses().create(params);

    }
}
