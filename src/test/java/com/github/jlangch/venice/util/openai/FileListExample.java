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

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.files.FileListPage;
import com.openai.models.files.FileListParams;
import com.openai.models.files.FileObject;

public final class FileListExample {
    private FileListExample() {}

    public static void main(String[] args) {
        final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        FileListParams params = FileListParams
                                    .builder()
                                    .limit(1000)
                                    .order(FileListParams.Order.ASC)
                                    .build();

        FileListPage page = client.files().list(params);

        for(FileObject fileObj : page.items()) {
            System.out.println(fileObj.id());
            System.out.println(fileObj.filename());
            System.out.println();

            fileObj.filename();
            fileObj.bytes();
            fileObj.expiresAt();
            fileObj.createdAt();
            fileObj.id();
            fileObj.purpose();
        }

        /*
        FileDeleted fileDeleted = client.files().delete("file_id");
        final boolean deleted = fileDeleted.deleted();

        FileObject fileObject = client.files().retrieve("file_id");
        */
    }
}
