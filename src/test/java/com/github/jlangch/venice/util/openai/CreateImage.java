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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.images.Image;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageGenerateParams.Background;
import com.openai.models.images.ImageGenerateParams.OutputFormat;
import com.openai.models.images.ImageGenerateParams.Quality;
import com.openai.models.images.ImageGenerateParams.Size;
import com.openai.models.images.ImageModel;
import com.openai.models.images.ImagesResponse;
import com.openai.models.images.ImagesResponse.Usage;


public class CreateImage {
    private CreateImage() {}

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ImageGenerateParams params = ImageGenerateParams.builder()
                .prompt("A cute baby sea otter")
                .background(Background.AUTO)
                .model(ImageModel.GPT_IMAGE_1_MINI)
                .n(1)  // 1..10
//                .outputCompression(20)  // 0..100%
                .outputFormat(OutputFormat.PNG)
                .quality(Quality.AUTO)
//                .responseFormat(ResponseFormat.B64_JSON) // or URL
                .size(Size.of("1024x1024"))
//                .style(Style.NATURAL)
                .build();

            final ImagesResponse imagesResponse = client.images().generate(params);
            final Optional<Usage> usage = imagesResponse.usage();
            final List<Image> images = imagesResponse.data().orElse(new ArrayList<>());

            if (images.isEmpty()) {
                System.out.println("<no image>");
            }
            else {
                final Image image = images.get(0);
                final String b64Json = image.b64Json().orElse(null);
                final String url = image.url().orElse(null);

                System.out.println(String.format("Images: %d", images.size()));
                System.out.println(String.format("Base64: %d", b64Json == null ? 0 : b64Json.length()));
                System.out.println(String.format("URL:    %s", url));
            }
    }
}
