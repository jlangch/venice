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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncMutableList extends VncSequence {

	public VncMutableList() {
		this(null, null);
	}

	public VncMutableList(final VncVal meta) {
		this(null, meta);
	}

	@SuppressWarnings("unchecked")
	public VncMutableList(final Collection<? extends VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (vals == null) {
			value = new ArrayList<>();
		}
		else if (vals instanceof ArrayList){
			value = (ArrayList<VncVal>)vals;
		}
		else {
			value = new ArrayList<>(vals);
		}
	}

	
	public static VncMutableList ofAll(final Iterable<? extends VncVal> iter) {
		final List<VncVal> list = new CopyOnWriteArrayList<>();
		for(VncVal o : iter) list.add(o);
		return new VncMutableList(list, null);
	}	
	
	public static VncMutableList ofAll(final Iterable<? extends VncVal> iter, final VncVal meta) {
		final List<VncVal> list = new CopyOnWriteArrayList<>();
		for(VncVal o : iter) list.add(o);
		return new VncMutableList(list, meta);
	}	
	
	public static VncMutableList of(final VncVal... mvs) {
		return new VncMutableList(Arrays.asList(mvs), Constants.Nil);
	}
	
	
	@Override
	public VncMutableList emptyWithMeta() {
		return new VncMutableList(getMeta());
	}
	
	@Override
	public VncMutableList withVariadicValues(final VncVal... replaceVals) {
		return VncMutableList.of(replaceVals).withMeta(getMeta());
	}
	
	@Override
	public VncMutableList withValues(final List<? extends VncVal> replaceVals) {
		return new VncMutableList(replaceVals, getMeta());
	}

	@Override
	public VncMutableList withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return new VncMutableList(replaceVals, meta);
	}

	@Override
	public VncMutableList withMeta(final VncVal meta) {
		return new VncMutableList(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(
						TYPE, 
						MetaUtil.typeMeta(
							new VncKeyword(VncSequence.TYPE), 
							new VncKeyword(VncCollection.TYPE), 
							new VncKeyword(VncVal.TYPE)));
	}

    @Override
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : value.iterator();
    }

    @Override
	public Stream<VncVal> stream() {
		return value.stream();
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}
	
	@Override
	public VncList filter(final Predicate<? super VncVal> predicate) {
		return VncList.ofAll(value.stream().filter(predicate), getMeta());
	}

	@Override
	public VncList map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return VncList.ofAll(value.stream().map(mapper), getMeta());
	}

	@Override
	public List<VncVal> getJavaList() { 
		return value; 
	}

	@Override
	public int size() {
		return value.size();
	}
	
	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	public void clear() {
		value.clear();
	}

	@Override
	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= value.size()) {
			throw new VncException(String.format(
						"nth: index %d out of range for a mutable list of size %d.", 
						idx, 
						size()));
		}

		return value.get(idx);
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? value.get(idx) : defaultVal;
	}

	@Override
	public VncVal first() {
		return isEmpty() ? Constants.Nil : value.get(0);
	}

	@Override
	public VncVal last() {
		return isEmpty() ? Constants.Nil : value.get(value.size()-1);
	}
	
	@Override
	public VncMutableList rest() {
		return value.size() <= 1 ? new VncMutableList(getMeta()) : slice(1);
	}
	
	@Override
	public VncMutableList butlast() {
		return value.size() <= 1 ? new VncMutableList(getMeta()) : slice(0, value.size()-1);
	}
	
	@Override
	public VncMutableList drop(final int n) {
		return slice(n);
	}

	@Override
	public VncMutableList dropWhile(final Predicate<? super VncVal> predicate) {
		for(int i=0; i<value.size(); i++) {
			final boolean drop = predicate.test(VncList.of(value.get(i)));
			if (!drop) {
				return slice(i);
			}
		}

		return new VncMutableList(getMeta());
	}

	@Override
	public VncMutableList dropRight(final int n) {
		if (value.isEmpty()) {
			return this;
		}
		else {
			return n >= value.size() ? emptyWithMeta() : slice(0, value.size() - n);
		}
	}

	@Override
	public VncMutableList take(final int n) {
		return slice(0, n);
	}

	@Override
	public VncMutableList takeWhile(final Predicate<? super VncVal> predicate) {
		for(int i=0; i<value.size(); i++) {
			final boolean take = predicate.test(VncList.of(value.get(i)));
			if (!take) {
				return slice(0, i);
			}
		}

		return this;
	}

	@Override
	public VncMutableList reverse() {
		final ArrayList<VncVal> seq = new ArrayList<>(value);
		Collections.reverse(seq);
		return new VncMutableList(seq, getMeta());
	}
	
	@Override 
	public VncMutableList shuffle() {
		final ArrayList<VncVal> seq = new ArrayList<>(value);
		Collections.shuffle(seq);
		return new VncMutableList(seq, getMeta());
	}
	
	@Override 
	public VncMutableList distinct() {
		return new VncMutableList(
				stream().distinct().collect(Collectors.toList()),
				getMeta());
	}

	@Override
	public VncMutableList slice(final int start, final int end) {
		if (start >= value.size()) {
			return new VncMutableList(getMeta());
		}
		else {
			return new VncMutableList(value.subList(start, Math.min(end, value.size())), getMeta());
		}
	}
	
	@Override
	public VncMutableList slice(final int start) {
		return slice(start, value.size());
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(value, getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(value, getMeta());
	}

	
	@Override
	public VncMutableList addAtStart(final VncVal val) {
		value.add(0, val);
		return this;
	}
	
	@Override
	public VncMutableList addAllAtStart(final VncSequence list, final boolean reverseAdd) {
		final List<VncVal> items = list.getJavaList();
		if (reverseAdd) Collections.reverse(items);		
		value.addAll(0, items);
		return this;
	}
	
	@Override
	public VncMutableList addAtEnd(final VncVal val) {
		value.add(val);
		return this;
	}
	
	@Override
	public VncMutableList addAllAtEnd(final VncSequence list) {
		for(VncVal v : list) value.add(v);
		return this;
	}
	
	@Override
	public VncMutableList setAt(final int idx, final VncVal val) {
		value.set(idx, val);
		return this;
	}
	
	@Override
	public VncMutableList removeAt(final int idx) {
		value.remove(idx);
		return this;
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.MUTABLELIST;
	}

	@Override 
	public boolean isVncList() {
		return true;
	}

	@Override
	public Object convertToJavaObject() {
		return stream()
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toList());
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncMutableList(o)) {
			int c = Integer.compare(size(), ((VncMutableList)o).size());
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<size(); ii++) {
					c = nth(ii).compareTo(((VncMutableList)o).nth(ii));
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
		VncMutableList other = (VncMutableList) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(this, " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(this, " ", print_readably) + ")";
	}

   
	public static final String TYPE = ":core/mutable-list";

	private static final long serialVersionUID = -1848883965231344442L;
 
	private final ArrayList<VncVal> value;
}