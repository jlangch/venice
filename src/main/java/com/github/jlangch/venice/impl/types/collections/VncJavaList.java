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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.VncException;
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


public class VncJavaList extends VncSequence implements IVncJavaObject {

	public VncJavaList() {
		this(null, null);
	}

	public VncJavaList(final VncVal meta) {
		this(null, meta);
	}

	public VncJavaList(final List<Object> val) {
		this(val, null);
	}

	public VncJavaList(final List<Object> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.value = val;
	}
	
	
	@Override
	public Object getDelegate() {
		return value;
	}
	
	@Override
	public VncList emptyWithMeta() {
		return new VncList(getMeta());
	}
	
	@Override
	public VncList withVariadicValues(final VncVal... replaceVals) {
		return VncList.of(replaceVals).withMeta(getMeta());
	}

	@Override
	public VncList withValues(final List<? extends VncVal> vals) {
		return new VncList(vals, getMeta());
	}

	@Override
	public VncList withValues(final List<? extends VncVal> vals, final VncVal meta) {
		return new VncList(vals, meta);
	}

	@Override
	public VncJavaList withMeta(final VncVal meta) {
		return new VncJavaList(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(value.getClass().getName());
	}
	
	@Override
	public VncKeyword getSupertype() {
		return new VncKeyword(value.getClass().getSuperclass().getName());
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(getSupertype());
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
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(JavaInteropUtil.convertToVncVal(v)));
	}
	
	@Override
	public VncJavaList filter(final Predicate<? super VncVal> predicate) {
		return new VncJavaList(
					stream()
						.filter((v) -> predicate.test(v))
						.collect(Collectors.toList()), 
					getMeta());
	}

	@Override
	public VncJavaList map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return new VncJavaList(
					stream()
						.map((v) -> mapper.apply(v).convertToJavaObject())
						.collect(Collectors.toList()), 
					getMeta());
	}

	@Override
	public List<VncVal> getJavaList() { 
		return stream().collect(Collectors.toList());
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
	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= value.size()) {
			throw new VncException("nth: index out of range");
		}

		return JavaInteropUtil.convertToVncVal(value.get((int)idx));
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? nth(idx) : defaultVal;
	}
	
	@Override
	public VncVal last() {
		return nthOrDefault(value.size()-1, Constants.Nil);
	}

	@Override
	public VncList rest() {
		return value.size() <= 1 ? VncList.empty() : slice(1);
	}

	@Override
	public VncList butlast() {
		return value.size() <= 1 ? VncList.empty() : slice(0, value.size()-1);
	}

	@Override
	public VncList drop(final int n) {
		return slice(n);
	}

	@Override
	public VncList dropWhile(final Predicate<? super VncVal> predicate) {
		for(int i=0; i<value.size(); i++) {
			final boolean drop = predicate.test(JavaInteropUtil.convertToVncVal(value.get(i)));
			if (!drop) {
				return slice(i);
			}
		}

		return VncList.empty();
	}
	
	@Override
	public VncList take(final int n) {
		return slice(0, n);
	}

	@Override
	public VncList takeWhile(final Predicate<? super VncVal> predicate) {
		for(int i=0; i<value.size(); i++) {
			final boolean take = predicate.test(JavaInteropUtil.convertToVncVal(value.get(i)));
			if (!take) {
				return slice(0, i);
			}
		}

		return toVncList();
	}

	@Override
	public VncList slice(final int start, final int end) {
		if (start >= value.size()) {
			return VncList.empty();
		}
		else {
			return VncList.ofList(
						value
							.subList(start, Math.min(end, value.size()))
							.stream()
							.map(v -> JavaInteropUtil.convertToVncVal(v))
							.collect(Collectors.toList()));
		}
	}
	
	@Override
	public VncList slice(final int start) {
		return slice(start, value.size());
	}

	@Override
	public VncJavaList setAt(final int idx, final VncVal val) {
		value.set(idx, val.convertToJavaObject());
		return this;
	}

	@Override
	public VncJavaList removeAt(final int idx) {
		value.remove(idx);
		return this;
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(getJavaList(), getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return new VncVector(getJavaList(), getMeta());
	}
	
	@Override
	public VncJavaList addAtStart(final VncVal val) {
		value.add(0, val.convertToJavaObject());
		return this;
	}

	@Override
	public VncJavaList addAllAtStart(final VncSequence list, final boolean reverseAdd) {
		final List<Object> items = list.getJavaList()
									   .stream()
									   .map(v -> v.convertToJavaObject())
									   .collect(Collectors.toList());
		
		if (reverseAdd) {
			Collections.reverse(items);
		}
		
		value.addAll(0, items);
		return this;
	}
	
	@Override
	public VncJavaList addAtEnd(final VncVal val) {
		value.add(val.convertToJavaObject());
		return this;
	}

	@Override
	public VncJavaList addAllAtEnd(final VncSequence list) {
		final List<VncVal> items = list.getJavaList();
		for(int ii=0; ii<items.size(); ii++) {
			value.add(items.get(ii).convertToJavaObject());
		}
		return this;
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.JAVALIST;
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
		else if (Types.isVncJavaList(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncJavaList)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
					c = nth(ii).compareTo(((VncJavaList)o).nth(ii));
					if (c != 0) {
						return c;
					}
				}
				return 0;
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
		VncJavaList other = (VncJavaList) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(stream(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(stream(), " ", print_readably) + ")";
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

	private final List<Object> value;
}