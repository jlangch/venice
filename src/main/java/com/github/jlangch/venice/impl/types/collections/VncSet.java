/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncSet extends VncCollection {

	public VncSet(final Set<VncVal> val) {
		value = (val instanceof HashSet) 
					? (HashSet<VncVal>)val
					: new HashSet<>(val);
	}
	
	public VncSet(final VncList lst) {
		value = new HashSet<>();
		value.addAll(lst.getList());
	}
	
	public VncSet(final VncVal... mvs) {
		value = new HashSet<>();
		if (mvs != null) {
			for(VncVal v : mvs) {
				value.add(v);
			}
		}
	}
	
	public VncSet empty() {
		return new VncSet();
	}
	
	public void add(final VncVal val) {
		value.add(val);
	}
	
	public void remove(final VncVal val) {
		value.remove(val);
	}
	
	public boolean contains(final VncVal val) {
		return value.contains(val);
	}
	
	@SuppressWarnings("unchecked")
	public VncSet copy() {
		return new VncSet((HashSet<VncVal>)value.clone());

	}

	public Set<VncVal> getSet() { 
		return value; 
	}

	public List<VncVal> getList() { 
		return new ArrayList<VncVal>(value); 
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

	
	private final HashSet<VncVal> value;	
}