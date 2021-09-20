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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncMutableSet extends VncSet {

	public VncMutableSet() {
		this(null, null);
	}

	public VncMutableSet(final VncVal meta) {
		this(null, meta);
	}

	public VncMutableSet(final Collection<? extends VncVal> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (val != null) value.addAll(val);
	}
	
	public static VncMutableSet ofAll(final Iterable<? extends VncVal> iter) {
		final Set<VncVal> set = ConcurrentHashMap.newKeySet();
		for(VncVal o : iter) set.add(o);
		return new VncMutableSet(set, null);
	}

	public static VncMutableSet ofAll(final Iterable<? extends VncVal> iter, final VncVal meta) {
		final Set<VncVal> set = ConcurrentHashMap.newKeySet();
		for(VncVal o : iter) set.add(o);
		return new VncMutableSet(set, meta);
	}
	
	public static VncMutableSet of(final VncVal... mvs) {
		return new VncMutableSet(Arrays.asList(mvs), null);
	}

	
	@Override
	public VncMutableSet emptyWithMeta() {
		return new VncMutableSet(getMeta());
	}
	
	@Override
	public VncMutableSet withValues(final Collection<? extends VncVal> replaceVals) {
		return new VncMutableSet(replaceVals, getMeta());
	}

	@Override
	public VncMutableSet withValues(final Collection<? extends VncVal> replaceVals, final VncVal meta) {
		return new VncMutableSet(replaceVals, meta);
	}

	@Override
	public VncMutableSet withMeta(final VncVal meta) {
		return new VncMutableSet(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return new VncKeyword(
						TYPE, 
						MetaUtil.typeMeta(
							new VncKeyword(VncSet.TYPE), 
							new VncKeyword(VncCollection.TYPE), 
							new VncKeyword(VncVal.TYPE)));
	}
	
	@Override
	public VncMutableSet add(final VncVal val) {
		value.add(val);
		return this;
	}

	@Override
	public VncMutableSet addAll(final VncSet val) {
		if (Types.isVncMutableSet(val)) {
			value.addAll(((VncMutableSet)val).value);
		}
		else {
			val.forEach(v -> value.add(v));
		}
		return this;
	}
	
	@Override
	public VncMutableSet addAll(final VncSequence seq) {
		for(VncVal v : seq) value.add(v);
		return this;
	}

	@Override
	public VncMutableSet remove(final VncVal val) {
		value.remove(val);
		return this;
	}

	@Override
	public VncMutableSet removeAll(final VncSet val) {
		if (Types.isVncMutableSet(val)) {
			value.removeAll(((VncMutableSet)val).value);
		}
		else {
			val.forEach(v -> value.remove(v));
		}
		return this;
	}

	@Override
	public VncMutableSet removeAll(final VncSequence seq) {
		for(VncVal v : seq) value.remove(v);
		return this;
	}

	@Override
	public boolean contains(final VncVal val) {
		return value.contains(val);
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
	public Set<VncVal> getJavaSet() { 
		return Collections.unmodifiableSet(value); 
	}

	@Override
	public List<VncVal> getJavaList() { 
		return Collections.unmodifiableList(new ArrayList<>(value)); 
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

	public void clear() {
		value.clear();
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.MUTABLESET;
	}

	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncMutableSet(o)) {
			int c = Integer.compare(size(), ((VncMutableSet)o).size());
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
		VncMutableSet other = (VncMutableSet) obj;
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


	public static final String TYPE = ":core/mutable-map";
	
    private static final long serialVersionUID = -1848883965231344442L;

	private final Set<VncVal> value = ConcurrentHashMap.newKeySet();	
}