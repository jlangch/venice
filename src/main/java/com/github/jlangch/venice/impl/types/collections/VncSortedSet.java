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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncSortedSet extends VncSet {

	public VncSortedSet() {
		this(null, null);
	}

	public VncSortedSet(final VncVal meta) {
		this(null, meta);
	}

	public VncSortedSet(final io.vavr.collection.Set<VncVal> val) {
		this(val, null);
	}

	public VncSortedSet(final io.vavr.collection.Set<VncVal> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (val == null) {
			value = io.vavr.collection.TreeSet.empty();
		}
		else if (val instanceof io.vavr.collection.TreeSet) {
			value = (io.vavr.collection.TreeSet<VncVal>)val;
		}
		else {
			value = io.vavr.collection.TreeSet.ofAll(val);
		}
	}

	
	public static VncSortedSet ofAll(final java.util.Collection<? extends VncVal> val) {
		return new VncSortedSet(io.vavr.collection.TreeSet.ofAll(val));
	}
	
	public static VncSortedSet ofAll(final VncSequence val) {
		return new VncSortedSet(io.vavr.collection.TreeSet.ofAll(val));
	}
	
	public static VncSortedSet of(final VncVal... mvs) {
		return new VncSortedSet(io.vavr.collection.TreeSet.of(mvs));
	}
	
	
	@Override
	public VncSortedSet emptyWithMeta() {
		return new VncSortedSet(getMeta());
	}
	
	@Override
	public VncSortedSet withValues(final Collection<? extends VncVal> replaceVals) {
		return new VncSortedSet(io.vavr.collection.TreeSet.ofAll(replaceVals), getMeta());
	}

	@Override
	public VncSortedSet withValues(final Collection<? extends VncVal> replaceVals, final VncVal meta) {
		return new VncSortedSet(io.vavr.collection.TreeSet.ofAll(replaceVals), meta);
	}

	@Override
	public VncSortedSet withMeta(final VncVal meta) {
		return new VncSortedSet(value, meta);
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
	public VncSortedSet add(final VncVal val) {
		return new VncSortedSet(value.add(val), getMeta());
	}
	
	@Override
	public VncSortedSet addAll(final VncSet val) {
		return new VncSortedSet(value.addAll(val), getMeta());
	}
	
	@Override
	public VncSortedSet addAll(final VncSequence val) {
		return new VncSortedSet(value.addAll(val), getMeta());
	}

	@Override
	public VncSortedSet remove(final VncVal val) {
		return new VncSortedSet(value.remove(val), getMeta());
	}

	@Override
	public VncSortedSet removeAll(final VncSet val) {
		return new VncSortedSet(value.removeAll(val), getMeta());
	}

	@Override
	public VncSortedSet removeAll(final VncSequence val) {
		return new VncSortedSet(value.removeAll(val), getMeta());
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
		return value.toJavaStream();
	}

	@Override
	public Set<VncVal> getJavaSet() { 
		return Collections.unmodifiableSet(value.toJavaSet()); 
	}

	@Override
	public List<VncVal> getJavaList() { 
		return Collections.unmodifiableList(value.toJavaList()); 
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(value.toVector(), getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(value.toVector(), getMeta());
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
		return TypeRank.SORTEDSET;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncSortedSet(o)) {
			int c = Integer.compare(size(), ((VncSortedSet)o).size());
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
		VncSortedSet other = (VncSortedSet) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return toString(true);
	}
	
	@Override
	public String toString(final boolean print_readably) {
		return "#{" + Printer.join(toVncList(), " ", print_readably) + "}";
	}


	public static final String TYPE = ":core/sorted-set";

    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.TreeSet<VncVal> value;	
}