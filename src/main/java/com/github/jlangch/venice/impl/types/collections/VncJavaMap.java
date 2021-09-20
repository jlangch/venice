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
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncJavaMap extends VncMap implements IVncJavaObject {

	public VncJavaMap() {
		this(null, null);
	}

	public VncJavaMap(final VncVal meta) {
		this(null, meta);
	}

	public VncJavaMap(final Map<Object,Object> val) {
		this(val, null);
	}

	public VncJavaMap(final Map<Object,Object> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = val;
	}

	
	
	@Override
	public Object getDelegate() {
		return value;
	}

	@Override
	public VncJavaMap emptyWithMeta() {
		return new VncJavaMap(getMeta());
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
	public VncJavaMap withMeta(final VncVal meta) {
		return new VncJavaMap(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(value.getClass().getName());
	}

	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(new VncKeyword(value.getClass().getSuperclass().getName()));
	}

	@Override
	public Map<VncVal,VncVal> getJavaMap() {
		return value
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						e -> JavaInteropUtil.convertToVncVal(e.getKey()),
						e -> JavaInteropUtil.convertToVncVal(e.getValue())));
	}

	@Override
	public VncVal get(final VncVal key) {
		return JavaInteropUtil.convertToVncVal(
					value.get(key.convertToJavaObject()));
	}

	@Override
	public VncVal containsKey(final VncVal key) {
		return VncBoolean.of(value.containsKey(key.convertToJavaObject()));
	}

	@Override
	public VncList keys() {
		return VncList.ofList(
					value
						.keySet()
						.stream()
						.map(k -> JavaInteropUtil.convertToVncVal(k))
						.collect(Collectors.toList()));
	}

	@Override
	public List<VncMapEntry> entries() {
		return Collections.unmodifiableList(
					getJavaMap()
						.entrySet()
						.stream().map(e -> new VncMapEntry(e.getKey(), e.getValue()))
						.collect(Collectors.toList()));
	}

	@Override
	public VncJavaMap putAll(final VncMap map) {
		getJavaMap().entrySet().forEach(
				e -> value.put(
					e.getKey().convertToJavaObject(), 
					e.getValue().convertToJavaObject()));
		return this;
	}
	
	@Override
	public VncJavaMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					"java-map: assoc requires an even number of items."));
		}
		
		for (int i=0; i<mvs.length-1; i+=2) {
			value.put(
				mvs[i].convertToJavaObject(), 
				mvs[i+1].convertToJavaObject());
		}
		return this;
	}

	@Override
	public VncJavaMap assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					"java-map: assoc requires an even number of items."));
		}	

		VncSequence kv = mvs;
		while(!kv.isEmpty()) {
			value.put(
					kv.first().convertToJavaObject(), 
					kv.second().convertToJavaObject());
			kv = kv.drop(2);
		}
		return this;
	}

	@Override
	public VncJavaMap dissoc(final VncVal... keys) {
		for (VncVal key : keys) {
			value.remove(key.convertToJavaObject());
		}
		return this;
	}

	@Override
	public VncJavaMap dissoc(final VncSequence keys) {
		for (VncVal key : keys) {
			value.remove(key.convertToJavaObject());
		}
		return this;
	}
	
	@Override
	public VncList toVncList() {
		return VncList.ofAll(
						value.entrySet()
							 .stream()
							 .map(e -> VncVector.of(
										JavaInteropUtil.convertToVncVal(e.getKey()), 
										JavaInteropUtil.convertToVncVal(e.getValue()))),
						getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return VncVector.ofAll(
						value.entrySet()
							 .stream()
							 .map(e -> VncVector.of(
										JavaInteropUtil.convertToVncVal(e.getKey()), 
										JavaInteropUtil.convertToVncVal(e.getValue()))),
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
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.JAVAMAP;
	}

	@Override
	public Object convertToJavaObject() {
		return value;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncJavaMap(o)) {
			int c = Integer.compare(size(), ((VncJavaMap)o).size());
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
		VncJavaMap other = (VncJavaMap) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		final List<VncVal> list = new ArrayList<>();
		value.entrySet().forEach(e -> {
			list.add(JavaInteropUtil.convertToVncVal(e.getKey()));
			list.add(JavaInteropUtil.convertToVncVal(e.getValue()));
		});
	
		return "{" + Printer.join(list.stream(), " ", print_readably) + "}";
	}
	


    private static final long serialVersionUID = -1848883965231344442L;

	private final Map<Object,Object> value;	
}