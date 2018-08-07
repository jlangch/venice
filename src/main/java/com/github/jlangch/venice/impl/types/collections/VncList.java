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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncList extends VncSequence {
	
	public VncList(final List<VncVal> val) {
		value = new ArrayList<VncVal>(val);
	}
	
	public VncList(final VncVal... mvs) {
		value = new ArrayList<VncVal>(Arrays.asList(mvs));
	}

    public void forEach(Consumer<? super VncVal> action) {
    	value.forEach(v -> action.accept(v));
    }

	@SuppressWarnings("unchecked")
	public VncList copy() {
		final VncList v = new VncList((ArrayList<VncVal>)value.clone());
		v.setMeta(getMeta());
		return v;
	}

	public List<VncVal> getList() { 
		return value; 
	}
	
	public boolean isList() { 
		return true; 
	}

	public int size() {
		return value.size();
	}
	
	public boolean isEmpty() {
		return value.isEmpty();
	}

	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= value.size()) {
			throw new VncException("nth: index out of range");
		}

		return value.get((int)idx);
	}

	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? nth(idx) : defaultVal;
	}

	public VncVal first() {
		return isEmpty() ? Constants.Nil : nth(0);
	}

	public VncVal second() {
		return size() < 2 ? Constants.Nil : nth(1);
	}

	public VncVal last() {
		return isEmpty() ? Constants.Nil : nth(value.size()-1);
	}
	
	public VncList rest() {
		if (isEmpty()) {
			return new VncList();
		} 
		else {
			return new VncList(value.subList(1, value.size()));
		}
	}

	public VncList slice(final int start, final int end) {
		return new VncList(value.subList(start, end));
	}
	
	public VncList slice(final int start) {
		return slice(start, value.size());
	}
	
	public VncList empty() {
		return new VncList();
	}
	
	public VncList toVncList() {
		return new VncList(value);
	}

	public VncVector toVncVector() {
		return new VncVector(value);
	}
	
	public VncSet toVncSet() {
		return new VncSet(this);
	}
	
	public VncList addAtStart(final VncVal val) {
		value.add(0, val);
		return this;
	}
	
	public VncList addAtStart(final VncList list) {
		final List<VncVal> items = list.getList();
		for(int ii=items.size()-1; ii>=0; ii++) {
			value.add(0, items.get(ii));
		}
		return this;
	}
	
	public VncList addAtEnd(final VncVal val) {
		value.add(val);
		return this;
	}
	
	public VncList addAtEnd(final VncList list) {
		value.addAll(list.getList());
		return this;
	}
	
	public VncList addList(final VncList list) {
		value.add(list);
		return this;
	}

	@Override
	public int compareTo(final VncVal o) {
		if (Types.isVncList(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncList)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncList)o).nth(ii));
				if (c != 0) {
					return c;
				}
			}			
		}
		
		return 0;
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
		VncList other = (VncList) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(value, " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(value, " ", print_readably) + ")";
	}


	private final ArrayList<VncVal> value;
}