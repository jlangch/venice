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
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;



public class VncOrderedMap extends VncMap {

	public VncOrderedMap() {
		this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, null);
	}

	public VncOrderedMap(final VncVal meta) {
		this((io.vavr.collection.LinkedHashMap<VncVal,VncVal>)null, meta);
	}

	public VncOrderedMap(final java.util. Map<? extends VncVal,? extends VncVal> vals) {
		this(vals, null);
	}

	public VncOrderedMap(final java.util.Map<? extends VncVal,? extends VncVal> vals, final VncVal meta) {
		this(vals == null ? null : io.vavr.collection.LinkedHashMap.ofAll(vals), meta);
	}

	public VncOrderedMap(final io.vavr.collection.Map<VncVal,VncVal> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (val == null) {
			value = io.vavr.collection.LinkedHashMap.empty();
		}
		else if (val instanceof io.vavr.collection.LinkedHashMap) {
			value = (io.vavr.collection.LinkedHashMap<VncVal,VncVal>)val;
		}
		else {
			value = io.vavr.collection.LinkedHashMap.ofEntries(val);
		}
	}
	
	
	public static VncOrderedMap ofAll(final VncSequence lst) {
		if (lst != null && (lst.size() % 2 != 0)) {
			throw new VncException(String.format(
					"ordered-map: create requires an even number of list items. Got %d items. %s", 
					lst.size(),
					ErrorMessage.buildErrLocation(lst)));
		}

		return new VncOrderedMap().assoc(lst);
	}
	
	public static VncOrderedMap ofAll(final VncVector vec) {
		if (vec != null && (vec.size() % 2 != 0)) {
			throw new VncException(String.format(
					"ordered-map: create requires an even number of vector items. Got %d items. %s", 
					vec.size(),
					ErrorMessage.buildErrLocation(vec)));
		}

		return new VncOrderedMap().assoc(vec);
	}
	
	public static VncOrderedMap of(final VncVal... mvs) {
		if (mvs != null && (mvs.length % 2 != 0)) {
			throw new VncException(String.format(
					"ordered-map: create requires an even number of items. Got %d items. %s", 
					mvs.length,
					ErrorMessage.buildErrLocation(mvs[0])));
		}
		
		return new VncOrderedMap().assoc(mvs);
	}
	

	@Override
	public VncOrderedMap emptyWithMeta() {
		return new VncOrderedMap(getMeta());
	}

	@Override
	public VncOrderedMap withValues(final Map<VncVal,VncVal> replaceVals) {
		return new VncOrderedMap(replaceVals, getMeta());
	}
	
	@Override
	public VncOrderedMap withValues(
			final Map<VncVal,VncVal> replaceVals, 
			final VncVal meta
	) {
		return new VncOrderedMap(replaceVals, meta);
	}


	@Override
	public VncOrderedMap withMeta(final VncVal meta) {
		return new VncOrderedMap(value, meta);
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
	public VncOrderedMap putAll(final VncMap map) {
		if (map instanceof VncOrderedMap) {
			return new VncOrderedMap(
					value.merge(((VncOrderedMap)map).value),
					getMeta());
		}
		else {
			return new VncOrderedMap(
				value.merge(io.vavr.collection.LinkedHashMap.ofAll(map.getJavaMap())),
				getMeta());
		}
	}
	
	@Override
	public VncOrderedMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					"ordered-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs[0])));
		}
		
		io.vavr.collection.LinkedHashMap<VncVal,VncVal> tmp = value;
		for (int i=0; i<mvs.length; i+=2) {
			tmp = tmp.put(mvs[i], mvs[i+1]);
		}
		return new VncOrderedMap(tmp, getMeta());
	}

	@Override
	public VncOrderedMap assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					"ordered-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs)));
		}	

		io.vavr.collection.LinkedHashMap<VncVal,VncVal> map = value;
		VncSequence kv = mvs;
		while(!kv.isEmpty()) {
			map = map.put(kv.first(), kv.second());
			kv = kv.drop(2);
		}
		return new VncOrderedMap(map, getMeta());
	}

	@Override
	public VncOrderedMap dissoc(final VncVal... keys) {
		return new VncOrderedMap(
					value.removeAll(Arrays.asList(keys)),
					getMeta());
	}

	@Override
	public VncOrderedMap dissoc(final VncSequence keys) {
		return new VncOrderedMap(
					value.removeAll(keys),
					getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return VncList.ofAll(value.map(e -> VncVector.of(e._1, e._2)), getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return VncVector.ofAll(value.map(e -> VncVector.of(e._1, e._2)), getMeta());
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
		return TypeRank.ORDEREDMAP;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncOrderedMap(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncOrderedMap)o).size();
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
		VncOrderedMap other = (VncOrderedMap) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		final Stream<VncVal> stream = value
										.map(e -> Arrays.asList(e._1, e._2))
										.collect(Collectors.toList())
										.stream()
										.flatMap(l -> l.stream());

		return "{" + Printer.join(stream, " ", print_readably) + "}";
	}
		

	public static final VncKeyword TYPE = new VncKeyword(":core/ordered-map");

    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.LinkedHashMap<VncVal,VncVal> value;	
}