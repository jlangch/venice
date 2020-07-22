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
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.ErrorMessage;


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
	public Map<VncVal,VncVal> getMap() {
		return values.getMap();
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
		return VncList.ofList(new ArrayList<>(getMap().keySet()));
	}

	@Override
	public List<VncMapEntry> entries() {
		return Collections.unmodifiableList(
					getMap()
						.entrySet()
						.stream().map(e -> new VncMapEntry(e.getKey(), e.getValue()))
						.collect(Collectors.toList()));
	}

	@Override
	public VncMap putAll(final VncMap map) {
		throw new VncException("not supported for custom types!");
	}

	@Override
	public VncMap assoc(final VncVal... mvs) {
		if (mvs.length %2 != 0) {
			throw new VncException(String.format(
					":core/custom-type: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs[0])));
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
					":core/custom-type: assoc requires an even number of items. %s", 
					ErrorMessage.buildErrLocation(mvs)));
		}	

		VncMap tmp = values;
		for (int i=0; i<mvs.getList().size(); i+=2) {
			tmp = tmp.assoc(mvs.nth(i), mvs.nth(i+1));
		}
		
		return DefTypeForm.createCustomType(typeDef, tmp);
	}

	@Override
	public VncCustomType dissoc(final VncVal... keys) {
		throw new VncException("not supported for custom types!");
	}

	@Override
	public VncMap dissoc(final VncSequence keys) {
		throw new VncException("not supported for custom types!");
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
		return "#:" + type.getValue() + values.toString();
	}

	@Override
	public String toString(final boolean print_readably) {
		return "#:" + type.getValue() + values.toString(print_readably);
	}

	
	public static final VncKeyword TYPE = new VncKeyword(":core/custom-type");
		
	private static final long serialVersionUID = -1848883965231344442L;
	
	private final VncKeyword type;
	private final VncCustomTypeDef typeDef;
	private final VncMap values;
}
