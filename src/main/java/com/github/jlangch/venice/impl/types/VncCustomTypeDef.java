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
package com.github.jlangch.venice.impl.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class VncCustomTypeDef extends VncMap {

	public VncCustomTypeDef(
			final VncKeyword type,
			final VncMap fields, 
			final VncVal meta
	) {
		super(meta);

		this.type = type;
		this.fields = fields;
	}
	
	@Override
	public VncMap emptyWithMeta() {
		throw new VncException("not supported");
	}
	@Override
	public VncHashMap withValues(final Map<VncVal,VncVal> replaceVals) {
		throw new VncException("not supported");
	}
	
	@Override
	public VncHashMap withValues(
			final Map<VncVal,VncVal> replaceVals, 
			final VncVal meta
	) {
		throw new VncException("not supported");
	}
	
	@Override
	public VncMap withMeta(final VncVal meta) {
		return new VncCustomTypeDef(type, fields, meta);
	}

	public VncKeyword getType() {
		return type;
	}

	@Override
	public Map<VncVal,VncVal> getMap() {
		return fields.getMap();
	}

	@Override
	public VncVal containsKey(final VncVal key) {
		return fields.containsKey(key);
	}

	@Override
	public VncVal get(final VncVal key) {
		return fields.get(key);
	}

	@Override
	public VncList keys() {
		return new VncList(new ArrayList<>(getMap().keySet()));
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
		throw new VncException("not supported");
	}

	@Override
	public VncMap assoc(final VncVal... mvs) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap assoc(final VncSequence mvs) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap dissoc(final VncVal... keys) {
		throw new VncException("not supported");
	}

	@Override
	public VncMap dissoc(final VncSequence keys) {
		throw new VncException("not supported");
	}

	@Override
	public VncList toVncList() {
		return fields.toVncList();
	}

	@Override
	public VncVector toVncVector() {
		return fields.toVncVector();
	}

	public VncMap toVncMap() {
		return fields;
	}

	@Override
	public int size() {
		return fields.size();
	}

	@Override
	public boolean isEmpty() {
		return fields.isEmpty();
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.CUSTOMTYPE;
	}

	@Override
	public Object convertToJavaObject() {
		return fields.convertToJavaObject();
	}

	@Override 
	public int compareTo(final VncVal o) {
		return fields.compareTo(o);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		VncCustomTypeDef other = (VncCustomTypeDef) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return fields.toString();
	}

	@Override
	public String toString(final boolean print_readably) {
		return fields.toString(print_readably);
	}

	
		
    private static final long serialVersionUID = -1848883965231344442L;
    private final VncKeyword type;
    private final VncMap fields;
}
