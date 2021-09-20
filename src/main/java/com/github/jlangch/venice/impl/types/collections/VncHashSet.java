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


public class VncHashSet extends VncSet {

	public VncHashSet() {
		this(null, null);
	}

	public VncHashSet(final VncVal meta) {
		this(null, meta);
	}

	public VncHashSet(final io.vavr.collection.Set<VncVal> val) {
		this(val, null);
	}

	public VncHashSet(final io.vavr.collection.Set<VncVal> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (val == null) {
			value = io.vavr.collection.HashSet.empty();
		}
		else if (val instanceof io.vavr.collection.HashSet) {
			value = (io.vavr.collection.HashSet<VncVal>)val;
		}
		else {
			value = io.vavr.collection.HashSet.ofAll(val);
		}
	}

	
	public static VncHashSet ofAll(final java.util.Collection<? extends VncVal> val) {
		return new VncHashSet(io.vavr.collection.HashSet.ofAll(val));
	}
	
	public static VncHashSet ofAll(final VncSequence val) {
		return new VncHashSet(io.vavr.collection.HashSet.ofAll(val));
	}
	
	public static VncHashSet of(final VncVal... mvs) {
		return new VncHashSet(io.vavr.collection.HashSet.of(mvs));
	}
	
	
	@Override
	public VncHashSet emptyWithMeta() {
		return new VncHashSet(getMeta());
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
	public VncHashSet withMeta(final VncVal meta) {
		return new VncHashSet(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}

	@Override
	public List<VncKeyword> getSupertypes() {
		return Arrays.asList(VncSet.TYPE, VncCollection.TYPE, VncVal.TYPE);
	}
	
	@Override
	public VncHashSet add(final VncVal val) {
		return new VncHashSet(value.add(val), getMeta());
	}
	
	@Override
	public VncHashSet addAll(final VncSet val) {
		return new VncHashSet(value.addAll(val), getMeta());
	}
	
	@Override
	public VncHashSet addAll(final VncSequence val) {
		return new VncHashSet(value.addAll(val), getMeta());
	}

	@Override
	public VncHashSet remove(final VncVal val) {
		return new VncHashSet(value.remove(val), getMeta());
	}

	@Override
	public VncHashSet removeAll(final VncSet val) {
		return new VncHashSet(value.removeAll(val), getMeta());
	}

	@Override
	public VncHashSet removeAll(final VncSequence val) {
		return new VncHashSet(value.removeAll(val), getMeta());
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
	public boolean contains(final VncVal val) {
		return value.contains(val);
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
		return TypeRank.HASHSET;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncHashSet(o)) {
			int c = Integer.compare(size(), ((VncHashSet)o).size());
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
		VncHashSet other = (VncHashSet) obj;
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

	
	public static final VncKeyword TYPE = new VncKeyword(":core/hash-set");

	private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.HashSet<VncVal> value;	
}