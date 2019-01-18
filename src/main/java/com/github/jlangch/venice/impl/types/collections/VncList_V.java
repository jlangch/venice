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


public class VncList_V extends VncSequence {

	public VncList_V() {
		this((io.vavr.collection.Vector<VncVal>)null, null);
	}

	public VncList_V(final VncVal meta) {
		this((io.vavr.collection.Vector<VncVal>)null, meta);
	}

	public VncList_V(final Collection<? extends VncVal> vals) {
		this(vals, null);
	}

	public VncList_V(final Collection<? extends VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = vals == null 
					? io.vavr.collection.Vector.of() 
					: io.vavr.collection.Vector.ofAll(vals);
	}

	public VncList_V(final io.vavr.collection.Vector<VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		value = vals == null ? io.vavr.collection.Vector.of() : vals;
	}
	
	
	public static VncList_V of(final VncVal... mvs) {
		return new VncList_V(Arrays.asList(mvs));
	}
	
	
	@Override
	public VncList_V empty() {
		return new VncList_V(getMeta());
	}
	
	@Override
	public VncList_V withValues(final Collection<? extends VncVal> replaceVals) {
		return new VncList_V(replaceVals, getMeta());
	}
	
	@Override
	public VncList_V copy() {
		return this;
	}

	@Override
	public VncList_V withMeta(final VncVal meta) {
		return new VncList_V(value, meta);
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}

	@Override
	public List<VncVal> getList() { 
		return Collections.unmodifiableList(value.toJavaList()); 
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
						"nth: index %d out of range for a list of size %d. %s", 
						idx, 
						size(),
						isEmpty() ? "" : ErrorMessage.buildErrLocation(value.get(0))));
		}

		return value.get(idx);
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx >= 0 && idx < value.size() ? value.get(idx) : defaultVal;
	}
	
	@Override
	public VncList_V rest() {
		return isEmpty() ? new VncList_V() : slice(1);
	}

	@Override
	public VncList_V slice(final int start, final int end) {
		return new VncList_V(value.subSequence(start, end), getMeta());
	}
	
	@Override
	public VncList_V slice(final int start) {
		return new VncList_V(value.subSequence(start), getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(getList(), getMeta());
		//return this;
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(getList(), getMeta());
		//return new VncVector(value, getMeta());
	}

	
	@Override
	public VncList_V addAtStart(final VncVal val) {
		return new VncList_V(value.prepend(val), getMeta());
	}
	
	@Override
	public VncList_V addAllAtStart(final VncSequence list) {
		final List<VncVal> items = new ArrayList<>(list.getList());
		Collections.reverse(items);
		return new VncList_V(value.prependAll(items), getMeta());
	}
	
	@Override
	public VncList_V addAtEnd(final VncVal val) {
		return new VncList_V(value.append(val), getMeta());
	}
	
	@Override
	public VncList_V addAllAtEnd(final VncSequence list) {
		return new VncList_V(value.appendAll(list.getList()), getMeta());
	}
	
	@Override
	public VncList_V setAt(final int idx, final VncVal val) {
		return new VncList_V(value.update(idx, val), getMeta());
	}
	
	@Override
	public VncList_V removeAt(final int idx) {
		return new VncList_V(value.removeAt(idx), getMeta());
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncList_V)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncList_V)o).nth(ii));
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
		VncList_V other = (VncList_V) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(value.toJavaList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(value.toJavaList(), " ", print_readably) + ")";
	}


    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.Vector<VncVal> value;
}