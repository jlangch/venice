/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;



public class VncHashMap extends VncMap {

	public VncHashMap() {
		this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, null);
	}

	public VncHashMap(final VncVal meta) {
		this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, meta);
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
		super(meta == null ? Constants.Nil : meta);
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
					"hash-map: create requires an even number of list items. Got %d items. %s", 
					lst.size(),
					ErrorMessage.buildErrLocation(lst)));
		}

		return new VncHashMap().assoc(lst);
	}
	
	public static VncHashMap ofAll(final VncVector vec) {
		if (vec != null && (vec.size() % 2 != 0)) {
			throw new VncException(String.format(
					"hash-map: create requires an even number of vector items. Got %d items. %s", 
					vec.size(),
					ErrorMessage.buildErrLocation(vec)));
		}

		return new VncHashMap().assoc(vec);
	}
	
	public static VncHashMap of(final VncVal... mvs) {
		if (mvs != null && (mvs.length % 2 != 0)) {
			throw new VncException(String.format(
					"hash-map: create requires an even number of items. Got %d items. %s",
					mvs.length,
					ErrorMessage.buildErrLocation(mvs[0])));
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
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncMap.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncMap.TYPE, VncCollection.TYPE, VncVal.TYPE);
	}
	
	@Override
	public Map<VncVal,VncVal> getMap() {
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
		return new VncHashMap(
						value.merge(io.vavr.collection.HashMap.ofAll(map.getMap())),
						getMeta());
	}
	
	@Override
	public VncHashMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					"hash-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs[0])));
		}
		
		io.vavr.collection.HashMap<VncVal,VncVal> tmp = value;
		for (int i=0; i<mvs.length; i+=2) {
			tmp = tmp.put(mvs[i], mvs[i+1]);
		}
		return new VncHashMap(tmp, getMeta());
	}

	@Override
	public VncHashMap assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					"hash-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs)));
		}	

		io.vavr.collection.HashMap<VncVal,VncVal> tmp = value;
		for (int i=0; i<mvs.getList().size(); i+=2) {
			tmp = tmp.put(mvs.nth(i), mvs.nth(i+1));
		}
		return new VncHashMap(tmp, getMeta());
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
					value.removeAll(keys.getList()),
					getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(
						value.map(e -> VncVector.of(e._1, e._2))
							 .collect(Collectors.toList()),
						getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return new VncVector(
						value.map(e -> VncVector.of(e._1, e._2))
							 .collect(Collectors.toList()),
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
	
	@Override public TypeRank typeRank() {
		return TypeRank.HASHMAP;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncHashMap(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = size();
			int c = sizeThis.compareTo(sizeOther);
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
	public String toString(final boolean print_readably) {
		final List<VncVal> list = value
									.map(e -> VncList.of(e._1, e._2).getList())
									.collect(Collectors.toList())
									.stream()
									.flatMap(l -> l.stream())
									.collect(Collectors.toList());

		return "{" + Printer.join(list, " ", print_readably) + "}";
	}

	public static class Builder {
		public Builder() {
		}

		public Builder add(final VncFunction fn) {
			if (fn.isPrivate()) {
				map.put(new VncSymbol(
								fn.getQualifiedName(), 
								VncHashMap.of(MetaUtil.PRIVATE, VncBoolean.True)),
						fn);
			}
			else {
				map.put(new VncSymbol(fn.getQualifiedName()), fn);
			}
			return this;
		}

		public Builder put(final VncVal key, final VncVal val) {
			map.put(key, val);
			return this;
		}

		public VncHashMap build() {
			return new VncHashMap(map);
		}
		
		public Map<VncVal,VncVal> toMap() {
			return map;
		}
		
		private HashMap<VncVal,VncVal> map = new HashMap<>();
	}
	

	public static final VncKeyword TYPE = new VncKeyword(":core/hash-map");

	public static final VncHashMap EMPTY = new VncHashMap();

    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.HashMap<VncVal,VncVal> value;	
}