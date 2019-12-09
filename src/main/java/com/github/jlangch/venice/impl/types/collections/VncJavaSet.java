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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncJavaSet extends VncSet implements IVncJavaObject {

	public VncJavaSet() {
		this(null, null);
	}

	public VncJavaSet(final VncVal meta) {
		this(null, meta);
	}

	public VncJavaSet(final Set<Object> val) {
		this(val, null);
	}

	public VncJavaSet(final Set<Object> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = val;
	}
	
	
	@Override
	public Object getDelegate() {
		return value;
	}

	@Override
	public VncJavaSet empty() {
		return new VncJavaSet(getMeta());
	}
	
	@Override
	public VncHashSet withValues(final Collection<? extends VncVal> replaceVals) {
		return new VncHashSet(io.vavr.collection.HashSet.ofAll(replaceVals), getMeta());
	}

	@Override
	public VncHashSet withValues(final Collection<? extends VncVal> replaceVals, final VncVal meta) {
		return new VncHashSet(io.vavr.collection.HashSet.ofAll(replaceVals), meta);
	}

	@Override
	public VncJavaSet withMeta(final VncVal meta) {
		return new VncJavaSet(value, meta);
	}
	
	@Override
	public VncJavaSet add(final VncVal val) {
		value.add(val.convertToJavaObject());
		return this;
	}

	@Override
	public VncJavaSet addAll(final VncSet val) {
		if (Types.isVncJavaSet(val)) {
			value.addAll(((VncJavaSet)val).value);
		}
		else {
			val.getList().forEach(v -> add(v));
		}
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public VncJavaSet addAll(final VncSequence val) {
		if (Types.isVncJavaList(val)) {
			value.addAll((List<Object>)((VncJavaList)val).getDelegate());
		}
		else {
			val.getList().forEach(v -> add(v));
		}
		return this;
	}

	@Override
	public VncJavaSet remove(final VncVal val) {
		value.remove(val.convertToJavaObject());
		return this;
	}

	@Override
	public VncJavaSet removeAll(final VncSet val) {
		if (Types.isVncJavaSet(val)) {
			value.removeAll(((VncJavaSet)val).value);
		}
		else {
			val.getList().forEach(v -> remove(v));
		}
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public VncJavaSet removeAll(final VncSequence val) {
		if (Types.isVncJavaList(val)) {
			value.removeAll((List<Object>)((VncJavaList)val).getDelegate());
		}
		else {
			val.getList().forEach(v -> remove(v));
		}
		return this;
	}

	@Override
	public boolean contains(final VncVal val) {
		return value.contains(val.convertToJavaObject());
	}

	@Override
	public Set<VncVal> getSet() { 
		return Collections.unmodifiableSet(getVncValueSet()); 
	}

	@Override
	public List<VncVal> getList() { 
		return Collections.unmodifiableList(getVncValueList()); 
	}

	@Override
	public VncList toVncList() {
		return new VncList(getVncValueList(), getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(getVncValueList(), getMeta());
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
	public int typeRank() {
		return 206;
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
		else if (Types.isVncJavaSet(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncJavaSet)o).size();
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
		VncJavaSet other = (VncJavaSet) obj;
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
		return "#{" + Printer.join(getVncValueList(), " ", print_readably) + "}";
	}

	private List<VncVal> getVncValueList() {
		return value
				.stream()
				.map(v -> JavaInteropUtil.convertToVncVal(v))
				.collect(Collectors.toList());
	}

	private Set<VncVal> getVncValueSet() {
		return value
				.stream()
				.map(v -> JavaInteropUtil.convertToVncVal(v))
				.collect(Collectors.toSet());
	}

	
    private static final long serialVersionUID = -1848883965231344442L;

	private final Set<Object> value;	
}