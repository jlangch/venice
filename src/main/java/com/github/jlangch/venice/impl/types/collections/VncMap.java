/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.types.collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;
import com.github.jlangch.venice.impl.util.MetaUtil;


public abstract class VncMap extends VncCollection implements IVncFunction {

    public VncMap(final VncVal meta) {
        super(meta);
    }

    public VncMap(
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
    }


    @Override
    public VncVal apply(final VncList args) {
        ArityExceptions.assertArity(this, FnType.Collection, args, 1, 2);

        final VncVal first = args.first();

        if (args.size() == 1) {
            return get(args.first());
        }
        else if (VncBoolean.isTrue(containsKey(first))) {
            return get(args.first());
        }
        else {
            return args.second(); // default
        }
    }

    @Override
    public VncList getArgLists() {
        return VncList.of(
                new VncString("(map key)"),
                new VncString("(map key default-val)"));
    }

    @Override
    public abstract VncMap emptyWithMeta();

    public abstract VncMap withValues(Map<VncVal,VncVal> replaceVals);

    public abstract VncMap withValues(Map<VncVal,VncVal> replaceVals, VncVal meta);

    @Override
    public abstract VncMap withMeta(VncVal meta);

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncCollection.TYPE),
                            new VncKeyword(VncVal.TYPE)));
    }

    public abstract Map<VncVal,VncVal> getJavaMap();

    public abstract VncVal get(VncVal key);

    public VncVal get(VncVal key, VncVal defaultValue) {
        final VncVal val = get(key);
        return val == Constants.Nil ? defaultValue : val;
    }

    public abstract VncVal containsKey(VncVal key);

    public abstract VncList keys();

    public abstract List<VncMapEntry> entries();

    public abstract VncMap putAll(VncMap map);

    public abstract VncMap assoc(VncVal... mvs);

    public abstract VncMap assoc(VncSequence mvs);

    public abstract VncMap dissoc(VncVal... keys);

    public abstract VncMap dissoc(VncSequence keys);

    @Override
    public Object convertToJavaObject() {
        final Map<Object,Object> map = new HashMap<>();
        for(VncMapEntry e : entries()) {
            map.put(
                e.getKey().convertToJavaObject(),
                e.getValue().convertToJavaObject());
        }
        return map;
    }


    public static final String TYPE = ":core/map";

    private static final long serialVersionUID = -1848883965231344442L;
}
