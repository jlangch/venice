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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.github.jlangch.venice.VncException;
import com.openai.core.MultipartField;


public class Multiparts {

    // ------------------------------------------------------------------------
    // Edit Image
    // ------------------------------------------------------------------------

    public static MultipartField<com.openai.models.images.ImageEditParams.Image> multipartField_EditImage(
            final File file,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) throws IOException, FileNotFoundException{
        Objects.requireNonNull(file);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        try (InputStream is = new FileInputStream(file)) {
            return MultipartField
                    .<com.openai.models.images.ImageEditParams.Image>builder()
                    .value(com.openai.models.images.ImageEditParams.Image.ofInputStream(is))
                    .contentType(contentType)
                    .filename(fileName)
                    .build();
        }
    }

    public static MultipartField<com.openai.models.images.ImageEditParams.Image> multipartField_EditImage(
            final InputStream is,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) {
        Objects.requireNonNull(is);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        return MultipartField
                .<com.openai.models.images.ImageEditParams.Image>builder()
                .value(com.openai.models.images.ImageEditParams.Image.ofInputStream(is))
                .contentType(contentType)
                .filename(fileName)
                .build();
    }

    public static MultipartField<com.openai.models.images.ImageEditParams.Image> multipartField_EditImage(
            final byte[] data,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) throws IOException {
        Objects.requireNonNull(data);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        try (InputStream is = new ByteArrayInputStream(data)) {
            return MultipartField
                    .<com.openai.models.images.ImageEditParams.Image>builder()
                    .value(com.openai.models.images.ImageEditParams.Image.ofInputStream(is))
                    .contentType(contentType)
                    .filename(fileName)
                    .build();
        }
    }


    // ------------------------------------------------------------------------
    // Generic InputStrem
    // ------------------------------------------------------------------------

    public static MultipartField<InputStream> multipartField_InputStream(
            final File file,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        try (InputStream is = new FileInputStream(file)) {
            return MultipartField
                    .<InputStream>builder()
                    .value(is)
                    .contentType(contentType)
                    .filename(fileName)
                    .build();
        }
        catch (FileNotFoundException ex) {
            throw new VncException("The file '" + file + "' does not exist!");
        }
        catch (Exception ex) {
            throw new VncException("Failed to read data from file " + file);
        }
    }

    public static MultipartField<InputStream> multipartField_InputStream(
            final InputStream is,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) {
        Objects.requireNonNull(is);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        return MultipartField
                .<InputStream>builder()
                .value(is)
                .contentType(contentType)
                .filename(fileName)
                .build();
    }

    public static MultipartField<InputStream> multipartField_InputStream(
            final byte[] data,
            final String contentType,   // "image/png"
            final String fileName       // "sea-otter.png"
    ) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(fileName);

        try (InputStream is = new ByteArrayInputStream(data)) {
            return MultipartField
                    .<InputStream>builder()
                    .value(is)
                    .contentType(contentType)
                    .filename(fileName)
                    .build();
        }
        catch (Exception ex) {
            throw new VncException("Failed to read data from byte buffer");
        }
    }

}
