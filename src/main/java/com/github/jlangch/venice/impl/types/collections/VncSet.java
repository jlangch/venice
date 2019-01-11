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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncSet extends VncCollection {

	public VncSet() {
		value = io.vavr.collection.HashSet.of();
	}
	
	public VncSet(final io.vavr.collection.HashSet<VncVal> val) {
		value = val;
	}

	public VncSet(final Set<VncVal> val) {
		value = io.vavr.collection.HashSet.ofAll(val);
	}
	
	public VncSet(final VncList val) {
		value = io.vavr.collection.HashSet.ofAll(val.getList());
	}
	
	public VncSet(final VncVal... mvs) {
		value = io.vavr.collection.HashSet.of(mvs);
	}
	
	public VncSet empty() {
		return new VncSet();
	}
	
	public VncSet add(final VncVal val) {
		return new VncSet(value.add(val));
	}
	
	public VncSet addAll(final VncSet val) {
		return new VncSet(value.addAll(val.value));
	}
	
	public VncSet addAll(final VncList val) {
		return new VncSet(value.addAll(val.getList()));
	}

	public VncSet remove(final VncVal val) {
		return new VncSet(value.remove(val));
	}

	public VncSet removeAll(final VncSet val) {
		return new VncSet(value.removeAll(val.value));
	}

	public VncSet removeAll(final VncList val) {
		return new VncSet(value.removeAll(val.getList()));
	}
	
	public boolean contains(final VncVal val) {
		return value.contains(val);
	}
	
	public VncSet copy() {
		final VncSet s = new VncSet(value);
		s.setMeta(getMeta());
		return s;
	}

	public Set<VncVal> getSet() { 
		return Collections.unmodifiableSet(value.toJavaSet()); 
	}

	public List<VncVal> getList() { 
		return Collections.unmodifiableList(value.toJavaList()); 
	}
		
	public VncVector toVncVector() {
		return new VncVector(getList());
	}
	
	public VncList toVncList() {
		return new VncList(getList());
	}
	
	public int size() {
		return value.size();
	}
	
	public boolean isEmpty() {
		return value.isEmpty();
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
		VncSet other = (VncSet) obj;
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

	private final io.vavr.collection.HashSet<VncVal> value;	
}