/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;


public class VncMutableSet extends VncSet {

	public VncMutableSet() {
		this(null, null);
	}

	public VncMutableSet(final VncVal meta) {
		this(null, meta);
	}

	public VncMutableSet(final Collection<? extends VncVal> val) {
		this(val, null);
	}

	public VncMutableSet(final Collection<? extends VncVal> val, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (val != null) value.addAll(val);
	}
	

	
	public static VncMutableSet ofAll(final Collection<? extends VncVal> val) {
		return new VncMutableSet(val);
	}
	
	public static VncMutableSet ofAll(final VncSequence val) {
		return new VncMutableSet(val.getList());
	}
	
	public static VncMutableSet of(final VncVal... mvs) {
		return new VncMutableSet(Arrays.asList(mvs));
	}

	
	@Override
	public VncMutableSet empty() {
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
	public VncMutableSet add(final VncVal val) {
		value.add(val);
		return this;
	}

	@Override
	public VncMutableSet addAll(final VncSet val) {
		value.addAll(val.getSet());
		return this;
	}
	
	@Override
	public VncMutableSet addAll(final VncSequence val) {
		value.addAll(val.getList());
		return this;
	}

	@Override
	public VncMutableSet remove(final VncVal val) {
		value.remove(val);
		return this;
	}

	@Override
	public VncMutableSet removeAll(final VncSet val) {
		value.removeAll(val.getSet());
		return this;
	}

	@Override
	public VncMutableSet removeAll(final VncSequence val) {
		value.removeAll(val.getList());
		return this;
	}

	@Override
	public boolean contains(final VncVal val) {
		return value.contains(val);
	}

	@Override
	public Set<VncVal> getSet() { 
		return Collections.unmodifiableSet(value); 
	}

	@Override
	public List<VncVal> getList() { 
		return Collections.unmodifiableList(new ArrayList<>(value)); 
	}

	@Override
	public VncList toVncList() {
		return new VncList(getList(), getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(getList(), getMeta());
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
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncMutableSet(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncMutableSet)o).size();
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
		VncMutableSet other = (VncMutableSet) obj;
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
		return "#{" + Printer.join(getList(), " ", print_readably) + "}";
	}


	
    private static final long serialVersionUID = -1848883965231344442L;

	private final Set<VncVal> value = new HashSet<VncVal>();	
}