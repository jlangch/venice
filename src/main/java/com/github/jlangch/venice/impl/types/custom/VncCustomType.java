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
package com.github.jlangch.venice.impl.types.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.specialforms.DefTypeForm;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class VncCustomType extends VncMap {

	public VncCustomType(
			final VncCustomTypeDef typeDef,
			final VncMap values, 
			final VncVal meta
	) {
		this(typeDef, values, null, meta);
	}

	public VncCustomType(
			final VncCustomTypeDef typeDef,
			final VncMap values, 
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) {
		super(wrappingTypeDef, meta);

		this.type = typeDef.getType();
		this.typeDef = typeDef;
		this.values = values;
	}

	@Override
	public VncCustomType emptyWithMeta() {
		throw new VncException("not supported for custom types!");
	}
	@Override
	public VncCustomType withValues(final Map<VncVal,VncVal> replaceVals) {
		throw new VncException("not supported for custom types!");
	}
	
	@Override
	public VncCustomType withValues(
			final Map<VncVal,VncVal> replaceVals, 
			final VncVal meta
	) {
		throw new VncException("not supported for custom types!");
	}
	
	@Override
	public VncCustomType wrap(final VncWrappingTypeDef wrappingTypeDef, final VncVal meta) {
		return new VncCustomType(typeDef, values, wrappingTypeDef, meta); 
	}
	
	@Override
	public VncCustomType withMeta(final VncVal meta) {
		return new VncCustomType(typeDef, values, meta);
	}

	@Override
	public VncKeyword getType() {
		return type;
	}

	@Override
	public VncKeyword getSupertype() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(
					TYPE, 
					VncMap.TYPE,
					VncCollection.TYPE,
					VncVal.TYPE);
	}

	public VncCustomTypeDef getTypeDef() {
		return typeDef;
	}

	@Override
	public Map<VncVal,VncVal> getJavaMap() {
		return values.getJavaMap();
	}

	@Override
	public VncVal containsKey(final VncVal key) {
		return values.containsKey(key);
	}

	@Override
	public VncVal get(final VncVal key) {
		return values.get(key);
	}

	@Override
	public VncList keys() {
		return VncList.ofList(new ArrayList<>(getJavaMap().keySet()));
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
	public VncMap putAll(final VncMap map) {
		// return a normal map because after putting fields the value
		// will not comply anymore to the custom type contract
		return values.putAll(map);
	}

	@Override
	public VncMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					":core/custom-type: assoc requires an even number of items."));
		}
		
		VncMap tmp = values;
		for (int i=0; i<mvs.length; i+=2) {
			tmp = tmp.assoc(mvs[i], mvs[i+1]);
		}
		
		return DefTypeForm.createCustomType(typeDef, tmp);
	}

	@Override
	public VncCustomType assoc(final VncSequence mvs) {
		if (mvs.size() %2 != 0) {
			throw new VncException(String.format(
					":core/custom-type: assoc requires an even number of items."));
		}	

		VncMap map = values;
		VncSequence kv = mvs;
		while(!kv.isEmpty()) {
			map = map.assoc(kv.first(), kv.second());
			kv = kv.drop(2);
		}
		
		return DefTypeForm.createCustomType(typeDef, map);
	}

	@Override
	public VncMap dissoc(final VncVal... keys) {
		// return a normal map because after removing a field the value
		// will not comply anymore to the custom type contract
		return values.dissoc(keys);
	}

	@Override
	public VncMap dissoc(final VncSequence keys) {
		// return a normal map because after removing a field the value
		// will not comply anymore to the custom type contract
		return values.dissoc(keys); 
	}

	@Override
	public VncList toVncList() {
		return values.toVncList();
	}

	@Override
	public VncVector toVncVector() {
		return values.toVncVector();
	}

	public VncMap toVncMap() {
		return values;
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.CUSTOMTYPE;
	}

	@Override
	public Object convertToJavaObject() {
		return values.convertToJavaObject();
	}

	@Override 
	public int compareTo(final VncVal o) {
		return values.compareTo(o);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		VncCustomType other = (VncCustomType) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return VncOrderedMap
				.of(new VncKeyword(":custom-type*"), type) 
				.putAll(values).toString();
	}

	@Override
	public String toString(final boolean print_readably) {
		return VncOrderedMap
				.of(new VncKeyword(":custom-type*"), type) 
				.putAll(values).toString(print_readably);
	}

	
	public static final VncKeyword TYPE = new VncKeyword(":core/custom-type");
		
	private static final long serialVersionUID = -1848883965231344442L;
	
	private final VncKeyword type;
	private final VncCustomTypeDef typeDef;
	private final VncMap values;
}
