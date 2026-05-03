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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;


public class ChatCompletionTraditionalFileExample {

    private ChatCompletionTraditionalFileExample() {}


    public static void main(String[] args) throws Exception {

        final Path path = Paths.get("/Users/juerg/Desktop/Tour_Eiffel_Wikimedia_Commons.pdf");

        final byte[] data = java.nio.file.Files.readAllBytes(path);

        final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        // 3600s is the minimum expiry time
        final FileObject fileObj = Files.fileObject(
                                    client,
                                    data,
                                    "application/pdf",
                                    FilePurpose.USER_DATA,
                                    3600);

        System.out.println("Created FileObject with id=" + fileObj.id());

        final ChatCompletionTraditionalRequest request =
                ChatCompletionTraditionalRequest
                    .of(client, ChatModel.GPT_5_4)
                    .maxCompletionTokens(2048)
                    .addUserMessageWithFiles(
                        "Describe this Image",
                        toList(fileObj));

        final ChatCompletionTraditionalResponse response = request.execute();

        print(response);
    }


    private static void print(final ChatCompletionTraditionalResponse response) {
        System.out.println("USAGE: " + response.getUsage());
        System.out.println();

        final List<String> messages = response.getMessages();
        System.out.println(messages.get(0));
        for(int ii=1; ii<messages.size(); ii++) {
            System.out.println("\n--------------------------------------------------\n");
            System.out.println(messages.get(ii));
        }
    }

}
