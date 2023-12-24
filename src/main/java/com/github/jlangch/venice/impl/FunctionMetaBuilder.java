/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class FunctionMetaBuilder {

    public FunctionMetaBuilder() {
    }

    public FunctionMetaBuilder arglists(final String... arglists) {
        meta.put(MetaUtil.ARGLIST, toVncList(arglists));
        return this;
    }

    public FunctionMetaBuilder doc(final String doc) {
        meta.put(MetaUtil.DOC, new VncString(doc));
        return this;
    }

    public FunctionMetaBuilder added(final String version) {
        meta.put(MetaUtil.ADDED, new VncString(version));
        return this;
    }

    public FunctionMetaBuilder examples(final String... examples) {
        meta.put(MetaUtil.EXAMPLES, toVncList(examples));
        return this;
    }

    public FunctionMetaBuilder seeAlso(final String... refs) {
        meta.put(MetaUtil.SEE_ALSO, toVncList(refs));
        return this;
    }

    public FunctionMetaBuilder functionRefs(final String... refs) {
        meta.put(MetaUtil.FUNCTION_REFS, toVncList(refs));
        return this;
    }

    public FunctionMetaBuilder privateFn() {
        meta.put(MetaUtil.PRIVATE, VncBoolean.True);
        return this;
    }

    public VncHashMap build() {
        return new VncHashMap(meta);
    }


    private VncList toVncList(final String... strings) {
        return VncList.ofList(Arrays.stream(strings)
                                    .map(r -> new VncString(r))
                                    .collect(Collectors.toList()));
    }


    private final HashMap<VncVal,VncVal> meta = new HashMap<>();
}
