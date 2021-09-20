/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncMutableMap extends VncMap {

	public VncMutableMap() {
		this(null, null);
	}

	public VncMutableMap(final VncVal meta) {
		this(null, meta);
	}

	public VncMutableMap(final Map<VncVal,VncVal> vals) {
		this(vals, null);
	}

	public VncMutableMap(final Map<VncVal,VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = vals == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(vals);
	}
	
	
	public static VncMutableMap ofAll(final VncSequence lst) {
		if (lst != null && (lst.size() % 2 != 0)) {
			throw new VncException(String.format(
					"mutable-map: create requires an even number of list items. Got %d items.", 
					lst.size()));
		}
		
		return new VncMutableMap().assoc(lst);
	}

	public static VncMutableMap of(final VncVal... mvs) {
		if (mvs != null && (mvs.length % 2 != 0)) {
			throw new VncException(String.format(
					"mutable-map: create requires an even number of items. Got %d items.", 
					mvs.length));
		}
		
		return new VncMutableMap().assoc(mvs);
	}

	
	@Override
	public VncMutableMap emptyWithMeta() {
		return new VncMutableMap(getMeta());
	}

	@Override
	public VncMutableMap withValues(final Map<VncVal,VncVal> replaceVals) {
		return new VncMutableMap(replaceVals, getMeta());
	}
	
	@Override
	public VncMutableMap withValues(
			final Map<VncVal,VncVal> replaceVals, 
			final VncVal meta
	) {
		return new VncMutableMap(replaceVals, meta);
	}

	@Override
	public VncMutableMap withMeta(final VncVal meta) {
		// shallow copy
		return new VncMutableMap(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(VncMap.TYPE, VncCollection.TYPE, VncVal.TYPE);
	}

	@Override
	public Map<VncVal,VncVal> getJavaMap() {
		return Collections.unmodifiableMap(value);
	}
	
	@Override
	public VncVal get(final VncVal key) {
		final VncVal val = value.get(key);
		return val == null ? Constants.Nil : val;
	}

	@Override
	public VncVal containsKey(final VncVal key) {
		return VncBoolean.of(value.containsKey(key));
	}

	@Override
	public VncList keys() {
		return VncList.ofList(new ArrayList<>(value.keySet()));
	}

	@Override
	public List<VncMapEntry> entries() {
		return Collections.unmodifiableList(
					value
						.entrySet()
						.stream().map(e -> new VncMapEntry(e.getKey(), e.getValue()))
						.collect(Collectors.toList()));
	}

	@Override
	public VncMutableMap putAll(final VncMap map) {
		value.putAll(map.getJavaMap());
		return this;
	}

	@Override
	public VncMutableMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					"mutable-map: assoc requires an even number of items."));
		}
		
		for (int i=0; i<mvs.length-1; i+=2) {
			value.put(mvs[i], mvs[i+1]);
		}
		return this;
	}

	@Override
	public VncMutableMap assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					"mutable-map: assoc requires an even number of items."));
		}	

		VncSequence kv = mvs;
		while(!kv.isEmpty()) {
			value.put(kv.first(), kv.second());
			kv = kv.drop(2);
		}
		return this;
	}

	@Override
	public VncMutableMap dissoc(final VncVal... keys) {
		for (VncVal key : keys) {
			value.remove(key);
		}
		return this;
	}

	@Override
	public VncMutableMap dissoc(final VncSequence keys) {
		for (VncVal key : keys) {
			value.remove(key);
		}
		return this;
	}
	
	@Override
	public VncList toVncList() {
		return VncList.ofAll(
						value.entrySet()
							 .stream()
							 .map(e -> VncVector.of(e.getKey(), e.getValue())),
						getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return VncVector.ofAll(
						value.entrySet()
							 .stream()
							 .map(e -> VncVector.of(e.getKey(), e.getValue())),
						getMeta());
	}
	
	@Override
	public int size() {
		return value.size();
	}
	
	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	public void clear() {
		value.clear();
	}
	
	@Override public TypeRank typeRank() {
		return TypeRank.MUTABLEMAP;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncMutableMap(o)) {
			int c = Integer.compare(size(), ((VncMutableMap)o).size());
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
		VncMutableMap other = (VncMutableMap) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		final Stream<VncVal> stream = value
										.entrySet()
										.stream()
										.map(e -> Arrays.asList(e.getKey(), e.getValue()))
										.flatMap(l -> l.stream());

		return "{" + Printer.join(stream, " ", print_readably) + "}";
	}
		

	public static final VncKeyword TYPE = new VncKeyword(":core/mutable-map", MetaUtil.typeMeta());

    private static final long serialVersionUID = -1848883965231344442L;

	private final ConcurrentHashMap<VncVal,VncVal> value;	
}