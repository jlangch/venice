/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
package com.github.jlangch.venice.util.ipc.impl.util;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.json.VncJsonReader;
import com.github.jlangch.venice.impl.util.json.VncJsonWriter;
import com.github.jlangch.venice.nanojson.JsonAppendableWriter;
import com.github.jlangch.venice.nanojson.JsonReader;
import com.github.jlangch.venice.nanojson.JsonWriter;


public class Json {

    public static VncVal readJson(
            final String json,
            final boolean mapKeysToKeywords
    ) {
        try {
            return new VncJsonReader(
                        JsonReader.from(json),
                        mapKeysToKeywords
                            ? t -> CoreFunctions.keyword.applyOf(t)
                            : null,
                        null,
                        false).read();
        }
        catch(Exception ex) {
            throw new VncException("Failed to parse JSON data to Venice data!", ex);
        }
    }

    public static String writeJson(final VncVal val, final boolean pretty) {
        final StringBuilder sb = new StringBuilder();
        final JsonAppendableWriter writer = pretty ? JsonWriter.indent("  ").on(sb) : JsonWriter.on(sb);
        new VncJsonWriter(writer, false).write(val).done();
        return sb.toString();
    }
}
