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
package com.github.jlangch.venice.impl.types.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;



public class VncHashMap extends VncMap {

    public VncHashMap() {
        this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, null);
    }

    public VncHashMap(final VncVal meta) {
        this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, meta);
    }

    public VncHashMap(
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        this(null, wrappingTypeDef, meta);
    }

    public VncHashMap(final io.vavr.collection.Map<VncVal,VncVal> val) {
        this(val, null);
    }

    public VncHashMap(final java.util.Map<? extends VncVal, ? extends VncVal> vals) {
        this(vals, null);
    }

    public VncHashMap(final Map<? extends VncVal,? extends VncVal> vals, final VncVal meta) {
        this(vals == null ? null : io.vavr.collection.HashMap.ofAll(vals), meta);
    }

    public VncHashMap(final io.vavr.collection.Map<VncVal,VncVal> val, final VncVal meta) {
        super(meta);
        if (val == null) {
            value = io.vavr.collection.HashMap.empty();
        }
        else if (val instanceof io.vavr.collection.HashMap) {
            value = (io.vavr.collection.HashMap<VncVal,VncVal>)val;
        }
        else {
            value = io.vavr.collection.HashMap.ofEntries(val);
        }
    }

    public VncHashMap(
            final io.vavr.collection.Map<VncVal,VncVal> val,
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
        if (val == null) {
            value = io.vavr.collection.HashMap.empty();
        }
        else if (val instanceof io.vavr.collection.HashMap) {
            value = (io.vavr.collection.HashMap<VncVal,VncVal>)val;
        }
        else {
            value = io.vavr.collection.HashMap.ofEntries(val);
        }
    }

    public static VncHashMap ofAll(final VncSequence lst) {
        if (lst != null && (lst.size() % 2 != 0)) {
            throw new VncException(String.format(
                    "hash-map: create requires an even number of list items. Got %d items.",
                    lst.size()));
        }

        return new VncHashMap().assoc(lst);
    }

    public static VncHashMap ofAll(final VncVector vec) {
        if (vec != null && (vec.size() % 2 != 0)) {
            throw new VncException(String.format(
                    "hash-map: create requires an even number of vector items. Got %d items.",
                    vec.size()));
        }

        return new VncHashMap().assoc(vec);
    }

    public static VncHashMap of(final VncVal... mvs) {
        if (mvs != null && (mvs.length % 2 != 0)) {
            throw new VncException(String.format(
                    "hash-map: create requires an even number of items. Got %d items.",
                    mvs.length));
        }

        return new VncHashMap().assoc(mvs);
    }


    @Override
    public VncHashMap emptyWithMeta() {
        return new VncHashMap(getMeta());
    }

    @Override
    public VncHashMap withValues(final Map<VncVal,VncVal> replaceVals) {
        return new VncHashMap(replaceVals, getMeta());
    }

    @Override
    public VncHashMap withValues(
            final Map<VncVal,VncVal> replaceVals,
            final VncVal meta
    ) {
        return new VncHashMap(replaceVals, meta);
    }


    @Override
    public VncHashMap withMeta(final VncVal meta) {
        return new VncHashMap(value, meta);
    }

    @Override
    public VncHashMap wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
        return new VncHashMap(value, wrappingTypeDef, meta);
    }

    @Override
    public VncKeyword getType() {
        return isWrapped() ? new VncKeyword(
                                    getWrappingTypeDef().getType().getQualifiedName(),
                                    MetaUtil.typeMeta(
                                        new VncKeyword(VncHashMap.TYPE),
                                        new VncKeyword(VncMap.TYPE),
                                        new VncKeyword(VncCollection.TYPE),
                                        new VncKeyword(VncVal.TYPE)))
                           : new VncKeyword(
                                   VncHashMap.TYPE,
                                    MetaUtil.typeMeta(
                                            new VncKeyword(VncMap.TYPE),
                                            new VncKeyword(VncCollection.TYPE),
                                            new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public Map<VncVal,VncVal> getJavaMap() {
        return Collections.unmodifiableMap(value.toJavaMap());
    }

    @Override
    public VncVal get(final VncVal key) {
        return value.get(key).getOrElse(Constants.Nil);
    }

    @Override
    public VncVal containsKey(final VncVal key) {
        return VncBoolean.of(value.containsKey(key));
    }

    @Override
    public VncList keys() {
        return VncList.ofList(new ArrayList<>(value.keySet().toJavaList()));
    }

    @Override
    public List<VncMapEntry> entries() {
        return Collections.unmodifiableList(
                    value
                        .map(e -> new VncMapEntry(e._1, e._2))
                        .collect(Collectors.toList()));
    }

    @Override
    public VncHashMap putAll(final VncMap map) {
        if (map instanceof VncHashMap) {
            return new VncHashMap(
                    value.merge(((VncHashMap)map).value, (u,v) -> v),
                    getMeta());
        }
        else {
            return new VncHashMap(
                            value.merge(
                                    io.vavr.collection.HashMap.ofAll(map.getJavaMap()),
                                    (u,v) -> v),
                            getMeta());
        }
    }

    @Override
    public VncHashMap assoc(final VncVal... mvs) {
        if (mvs.length %2 != 0) {
            throw new VncException(String.format(
                    "hash-map: assoc requires an even number of items."));
        }

        io.vavr.collection.HashMap<VncVal,VncVal> tmp = value;
        for (int i=0; i<mvs.length-1; i+=2) {
            tmp = tmp.put(mvs[i], mvs[i+1]);
        }
        return new VncHashMap(tmp, getMeta());
    }

    @Override
    public VncHashMap assoc(final VncSequence mvs) {
        if (mvs.size() %2 != 0) {
                throw new VncException(String.format(
                    "hash-map: assoc requires an even number of items."));
        }


        io.vavr.collection.HashMap<VncVal,VncVal> map = value;
        VncSequence kv = mvs;
        while(!kv.isEmpty()) {
            map = map.put(kv.first(), kv.second());
            kv = kv.drop(2);
        }
        return new VncHashMap(map, getMeta());
    }

    @Override
    public VncHashMap dissoc(final VncVal... keys) {
        return new VncHashMap(
                    value.removeAll(Arrays.asList(keys)),
                    getMeta());
    }

    @Override
    public VncHashMap dissoc(final VncSequence keys) {
        return new VncHashMap(
                    value.removeAll(keys),
                    getMeta());
    }

    @Override
    public VncList toVncList() {
        return VncList.ofAll(value.map(e -> VncVector.of(e._1, e._2)), getMeta());
    }

    @Override
    public VncVector toVncVector() {
        return new VncVector(value.map(e -> VncVector.of(e._1, e._2)), getMeta());
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override public TypeRank typeRank() {
        return TypeRank.HASHMAP;
    }

    public static VncMap empty() {
        return EMPTY;
    }

    @Override
    public int compareTo(final VncVal o) {
        if (o == Constants.Nil) {
            return 1;
        }
        else if (Types.isVncHashMap(o)) {
            int c = Integer.compare(size(), ((VncHashMap)o).size());
            if (c != 0) {
                return c;
            }
            else {
                return equals(o) ? 0 : -1;
            }
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        VncHashMap other = (VncHashMap) obj;
        return value.equals(other.value);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        final Stream<VncVal> stream = value
                                        .map(e -> Arrays.asList(e._1, e._2))
                                        .collect(Collectors.toList())
                                        .stream()
                                        .flatMap(l -> l.stream());

        return "{" + Printer.join(stream, " ", print_machine_readably) + "}";
    }


    public static final String TYPE = ":core/hash-map";

    public static final VncHashMap EMPTY = new VncHashMap();

    private static final long serialVersionUID = -1848883965231344442L;

    private final io.vavr.collection.HashMap<VncVal,VncVal> value;
}
