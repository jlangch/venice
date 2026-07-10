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
package com.github.jlangch.venice.util.openai.bug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JacksonNumberParser {

    private JacksonNumberParser() {}

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = "{ \"value\" : 0E-6176 }";
            JsonNode jsonNode = objectMapper.readTree(json);
            double value = jsonNode.get("value").asDouble();
            System.out.println(value);  // 0.0 => OK
        }
        catch(Exception ex) {
            ex.printStackTrace();;
        }
    }

}
