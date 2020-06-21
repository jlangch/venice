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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;


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
					"mutable-map: create requires an even number of list items. %s", 
					ErrorMessage.buildErrLocation(lst)));
		}
		
		return new VncMutableMap().assoc(lst);
	}

	public static VncMutableMap of(final VncVal... mvs) {
		if (mvs != null && (mvs.length % 2 != 0)) {
			throw new VncException(String.format(
					"mutable-map: create requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs[0])));
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
	public VncKeyword getSupertype() {
		return VncMap.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncMap.TYPE, VncCollection.TYPE, VncVal.TYPE);
	}

	@Override
	public Map<VncVal,VncVal> getMap() {
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
		return VncList.ofColl(new ArrayList<>(value.keySet()));
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
		value.putAll(map.getMap());
		return this;
	}

	@Override
	public VncMutableMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					"mutable-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs[0])));
		}
		
		for (int i=0; i<mvs.length; i+=2) {
			value.put(mvs[i], mvs[i+1]);
		}
		return this;
	}

	@Override
	public VncMutableMap assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					"mutable-map: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs)));
		}	

		for (int i=0; i<mvs.getList().size(); i+=2) {
			value.put(mvs.nth(i), mvs.nth(i+1));
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
		for (int i=0; i<keys.getList().size(); i++) {
			value.remove(keys.nth(i));
		}
		return this;
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(
						value
							.entrySet()
							.stream()
							.map(e -> VncVector.of(e.getKey(), e.getValue()))
							.collect(Collectors.toList()),
						getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return new VncVector(
						value
							.entrySet()
							.stream()
							.map(e -> VncVector.of(e.getKey(), e.getValue()))
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
		return TypeRank.MUTABLEMAP;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncMutableMap(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncMutableMap)o).size();
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VncMutableMap other = (VncMutableMap) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		final List<VncVal> list = value
									.entrySet()
									.stream()
									.map(e -> VncList.of(e.getKey(), e.getValue()).getList())
									.flatMap(l -> l.stream())
									.collect(Collectors.toList());

		return "{" + Printer.join(list, " ", print_readably) + "}";
	}
	
	public static class Builder {
		public Builder() {
		}
		
		public Builder put(final String key, final VncVal val) {
			map.put(new VncSymbol(key), val);
			return this;
		}

		public Builder put(final VncVal key, final VncVal val) {
			map.put(key, val);
			return this;
		}

		public VncMutableMap build() {
			return new VncMutableMap(map);
		}
		
		public Map<VncVal,VncVal> toMap() {
			return map;
		}
		
		private final HashMap<VncVal,VncVal> map = new HashMap<>();
	}
	

	public static final VncKeyword TYPE = new VncKeyword(":core/mutable-map");

    private static final long serialVersionUID = -1848883965231344442L;

	private final ConcurrentHashMap<VncVal,VncVal> value;	
}