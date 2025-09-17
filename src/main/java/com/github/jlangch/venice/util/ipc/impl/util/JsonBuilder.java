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

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class JsonBuilder {

    public JsonBuilder add(final String key, final String value) {
        elements.add(new VncString(key));
        elements.add(new VncString(value));
        return this;
    }

    public JsonBuilder add(final String key, final int value) {
        elements.add(new VncString(key));
        elements.add(new VncInteger(value));
        return this;
    }

    public JsonBuilder add(final String key, final long value) {
        elements.add(new VncString(key));
        elements.add(new VncLong(value));
        return this;
    }

    public JsonBuilder add(final String key, final boolean value) {
        elements.add(new VncString(key));
        elements.add(VncBoolean.of(value));
        return this;
    }

    public String toJson(final boolean pretty) {
        final VncMap map = VncHashMap.of(elements.toArray(new VncVal[]{}));
        return Json.writeJson(map, pretty);
    }


    final List<VncVal> elements = new ArrayList<>();
}
