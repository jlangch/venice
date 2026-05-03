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
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.github.jlangch.venice.impl.util.io.FileUtil;
import com.openai.client.OpenAIClient;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileCreateParams.ExpiresAfter;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;


public class Files {


    public static FileObject fileObject(
            final OpenAIClient client,
            final Path path,
            final String mimetype,
            final FilePurpose purpose,
            final long expiresAfterSeconds
    ) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(path);

        if (!"application/pdf".equals(mimetype)) {
            throw new RuntimeException("OpenAI accepts only PDF files");
        }

        final FileCreateParams.Builder params = FileCreateParams.builder();

        params.file(path);

        params.purpose(purpose == null ? FilePurpose.USER_DATA : purpose);

        final ExpiresAfter expiresAfter = createExpiresAfter(expiresAfterSeconds);
        if (expiresAfter != null) {
            params.expiresAfter(expiresAfter);
        }

        return client.files().create(params.build());
    }

    public static FileObject fileObject(
            final OpenAIClient client,
            final File file,
            final FilePurpose purpose,
            final long expiresAfterSeconds
    ) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(file);

        final FileCreateParams.Builder params = FileCreateParams.builder();

        params.file(file.toPath());

        params.purpose(purpose == null ? FilePurpose.USER_DATA : purpose);

        final ExpiresAfter expiresAfter = createExpiresAfter(expiresAfterSeconds);
        if (expiresAfter != null) {
            params.expiresAfter(expiresAfter);
        }

        return client.files().create(params.build());
    }

    public static FileObject fileObject(
            final OpenAIClient client,
            final byte[] data,
            final String filename,
            final FilePurpose purpose,
            final long expiresAfterSeconds
    ) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(data);
        Objects.requireNonNull(filename);

        File tmp = null;
        try {
            final String fileExt = "." + FileUtil.getFileExt(filename);

            // Create a temporary file
            tmp = File.createTempFile("OpenAI-upload", fileExt);
            java.nio.file.Files.write(tmp.toPath(), data);

            final FileCreateParams.Builder params = FileCreateParams.builder();

            params.file(tmp.toPath());

            params.purpose(purpose == null ? FilePurpose.USER_DATA : purpose);

            final ExpiresAfter expiresAfter = createExpiresAfter(expiresAfterSeconds);
            if (expiresAfter != null) {
                params.expiresAfter(expiresAfter);
            }

            return client.files().create(params.build());
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to create temporary file for upload", ex);
        }
        finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    public static String id(final FileObject fileObject) {
        Objects.requireNonNull(fileObject);
        return fileObject.id();
    }


    private static ExpiresAfter createExpiresAfter(final long expiresAfterSeconds) {
        if (expiresAfterSeconds > 0) {
            ExpiresAfter.Builder expires = ExpiresAfter.builder();
            expires.seconds(expiresAfterSeconds);
            return expires.build();
        }
        else {
            return null;
        }
    }

}
