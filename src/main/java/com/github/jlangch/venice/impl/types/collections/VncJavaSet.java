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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncJavaObject;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.StreamUtil;


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

	private VncJavaSet(final Set<Object> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = val;
	}
	
	
	public static VncJavaSet of(final Object... vals) {
		final Set<Object> set = new HashSet<>();
		for(Object o : vals) set.add(o);
		return new VncJavaSet(set, Constants.Nil);
	}
	
	public static VncJavaSet ofAll(final Iterable<Object> iter) {
		final Set<Object> set = new HashSet<>();
		for(Object o : iter) set.add(o);
		return new VncJavaSet(set, null);
	}
	
	public static VncJavaSet ofAll(final Iterable<Object> iter, final VncVal meta) {
		final Set<Object> set = new HashSet<>();
		for(Object o : iter) set.add(o);
		return new VncJavaSet(set, meta);
	}
	
	
	@Override
	public Object getDelegate() {
		return value;
	}

	@Override
	public VncJavaSet emptyWithMeta() {
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
	public VncKeyword getType() {
		return new VncKeyword(value.getClass().getName());
	}

	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(new VncKeyword(value.getClass().getSuperclass().getName()));
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
			val.forEach(v -> add(v));
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
			val.forEach(v -> add(v));
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
			val.forEach(v -> remove(v));
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
			val.forEach(v -> remove(v));
		}
		return this;
	}

	@Override
	public boolean contains(final VncVal val) {
		return value.contains(val.convertToJavaObject());
	}

    @Override
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : new MappingIterator(value.iterator());
    }

    @Override
	public Stream<VncVal> stream() {
		return StreamUtil.stream(iterator());
    }

	@Override
	public Set<VncVal> getJavaSet() { 
		return Collections.unmodifiableSet(getVncValueSet()); 
	}

	@Override
	public List<VncVal> getJavaList() { 
		return Collections.unmodifiableList(getVncValueList()); 
	}

	@Override
	public VncList toVncList() {
		return VncList.ofAll(stream(), getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return VncVector.ofAll(stream(), getMeta());
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
		return TypeRank.JAVASET;
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
			int c = Integer.compare(size(), ((VncJavaSet)o).size());
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
		VncJavaSet other = (VncJavaSet) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		return "#{" + Printer.join(stream(), " ", print_readably) + "}";
	}

	private List<VncVal> getVncValueList() {
		return stream().collect(Collectors.toList());
	}

	private Set<VncVal> getVncValueSet() {
		return stream().collect(Collectors.toSet());
	}

	
	
	private static class MappingIterator implements Iterator<VncVal> {

		public MappingIterator(final Iterator<Object> iter) {
			this.iter = iter;
		}
		
	    @Override
	    public boolean hasNext() { return iter.hasNext(); }

	    @Override
	    public VncVal next() { 
	    	return JavaInteropUtil.convertToVncVal(iter.next());
	    }

	    @Override
	    public String toString() {
	        return "MappingIterator()";
	    }
	    
	    private final Iterator<Object> iter;
	}

	
	
	private static final long serialVersionUID = -1848883965231344442L;

	private final Set<Object> value;	
}