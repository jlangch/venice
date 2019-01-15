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
import java.util.List;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncList extends VncSequence {
	
	public VncList(final Collection<? extends VncVal> vals) {
		value = new ArrayList<VncVal>(vals);
	}
	
	public VncList(final VncVal... mvs) {
		value = new ArrayList<VncVal>(Arrays.asList(mvs));
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}

	@Override
	@SuppressWarnings("unchecked")
	public VncList copy() {
		return copyMetaTo(new VncList((ArrayList<VncVal>)value.clone()));
	}

	@Override
	public List<VncVal> getList() { 
		return Collections.unmodifiableList(value); 
	}
	
	@Override
	public boolean isList() { 
		return true; 
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
			throw new VncException(String.format(
						"nth: index %d out of range for a %s of size %d. %s", 
						idx, 
						Types.isVncVector(this) ? "vector" : "list",
						size(),
						isEmpty() ? "" : ErrorMessage.buildErrLocation(value.get(0))));
		}

		return value.get((int)idx);
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? nth(idx) : defaultVal;
	}

	@Override
	public VncVal first() {
		return isEmpty() ? Constants.Nil : nth(0);
	}

	@Override
	public VncVal second() {
		return size() < 2 ? Constants.Nil : nth(1);
	}

	@Override
	public VncVal third() {
		return size() < 3 ? Constants.Nil : nth(2);
	}

	@Override
	public VncVal last() {
		return isEmpty() ? Constants.Nil : nth(value.size()-1);
	}
	
	@Override
	public VncList rest() {
		return isEmpty() ? new VncList() : new VncList(value.subList(1, value.size()));
	}

	@Override
	public VncList slice(final int start, final int end) {
		return new VncList(value.subList(start, end));
	}
	
	@Override
	public VncList slice(final int start) {
		return slice(start, value.size());
	}
	
	@Override
	public VncList empty() {
		return copyMetaTo(new VncList());
	}
	
	@Override
	public VncList toVncList() {
		return copyMetaTo(new VncList(value));
	}

	@Override
	public VncVector toVncVector() {
		return copyMetaTo(new VncVector(value));
	}
	
	@Override
	public VncList setAt(final int idx, final VncVal val) {
		value.set(idx, val);
		return this;
	}
	
	@Override
	public VncList addAtStart(final VncVal val) {
		value.add(0, val);
		return this;
	}
	
	@Override
	public VncList addAllAtStart(final VncSequence list) {
		final List<VncVal> items = list.getList();
		for(int ii=0; ii<items.size(); ii++) {
			value.add(0, items.get(ii));
		}
		return this;
	}
	
	@Override
	public VncList addAtEnd(final VncVal val) {
		value.add(val);
		return this;
	}
	
	@Override
	public VncList addAllAtEnd(final VncSequence list) {
		value.addAll(list.getList());
		return this;
	}
	
	@Override
	public VncList removeAt(final int idx) {
		value.remove(idx);
		return this;
	}

	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
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


    private static final long serialVersionUID = -1848883965231344442L;

	private final ArrayList<VncVal> value;
}