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


public class VncVector extends VncList {

	public VncVector() {
		this(null, null);
	}

	public VncVector(final VncVal meta) {
		this(null, meta);
	}

	public VncVector(final Collection<? extends VncVal> vals) {
		this(vals, null);
	}

	public VncVector(final Collection<? extends VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = vals == null ? new ArrayList<>() : new ArrayList<>(vals);
	}
	
	
	public static VncVector of(final VncVal... mvs) {
		return new VncVector(Arrays.asList(mvs));
	}

	
	@Override
	public VncVector empty() {
		return new VncVector(getMeta());
	}
	
	@Override
	public VncVector copy() {
		// shallow copy
		return new VncVector(value, getMeta());
	}

	@Override
	public VncVector withMeta(final VncVal meta) {
		// shallow copy
		return new VncVector(value, meta);
	}
	
	
	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}

	@Override
	public List<VncVal> getList() { 
		return Collections.unmodifiableList(value); 
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
						"nth: index %d out of range for a vector of size %d. %s", 
						idx, 
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
		return nthOrDefault(0, Constants.Nil);
	}

	@Override
	public VncVal second() {
		return nthOrDefault(1, Constants.Nil);
	}

	@Override
	public VncVal third() {
		return nthOrDefault(2, Constants.Nil);
	}

	@Override
	public VncVal last() {
		return nthOrDefault(value.size()-1, Constants.Nil);
	}
	
	@Override
	public VncVector rest() {
		return isEmpty() ? new VncVector() : new VncVector(value.subList(1, value.size()));
	}

	@Override
	public VncVector slice(final int start) {
		return slice(start, value.size());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncVector(getList().subList(start, end));
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(getList(), getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return this;
	}

	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		value.set(idx, val);
		return this;
	}
	
	@Override
	public VncVector addAtStart(final VncVal val) {
		value.add(0, val);
		return this;
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list) {
		final List<VncVal> items = list.getList();
		for(int ii=0; ii<items.size(); ii++) {
			value.add(0, items.get(ii));
		}
		return this;
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		value.add(val);
		return this;
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		value.addAll(list.getList());
		return this;
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		value.remove(idx);
		return this;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncVector(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncVector)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncVector)o).nth(ii));
				if (c != 0) {
					return c;
				}
			}
			
		}
		
		return 0;
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}


	private static final long serialVersionUID = -1848883965231344442L;
	
	private final ArrayList<VncVal> value;
}