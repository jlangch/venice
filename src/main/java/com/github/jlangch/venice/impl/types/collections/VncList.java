/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncList extends VncSequence {

	protected VncList() {
		this((io.vavr.collection.Seq<VncVal>)null, null);
	}

	protected VncList(final VncVal meta) {
		this((io.vavr.collection.Seq<VncVal>)null, meta);
	}

	protected VncList(final Collection<? extends VncVal> vals, final VncVal meta) {
		this(vals == null ? null : io.vavr.collection.Vector.ofAll(vals), meta);
	}

	public VncList(final io.vavr.collection.Seq<VncVal> vals, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		if (vals == null) {
			value = io.vavr.collection.Vector.empty();
		}
		else if (vals instanceof io.vavr.collection.Vector) {
			value = (io.vavr.collection.Vector<VncVal>)vals;
		}
		else {
			value = io.vavr.collection.Vector.ofAll(vals);
		}
	}
	
	
	public static VncList of(final VncVal... mvs) {
		return new VncList(io.vavr.collection.Vector.of(mvs), Constants.Nil);
	}

	public static VncList ofColl(final Collection<? extends VncVal> vals) {
		return new VncList(vals, Constants.Nil);
	}

	public static VncList ofColl(final Collection<? extends VncVal> vals, final VncVal meta) {
		return new VncList(vals, meta);
	}

	
	@Override
	public VncList emptyWithMeta() {
		return new VncList(getMeta());
	}
	
	@Override
	public VncList withVariadicValues(final VncVal... replaceVals) {
		return VncTinyList.of(replaceVals).withMeta(getMeta());
	}
	
	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals) {
		return VncTinyList.ofList(replaceVals, getMeta());
	}

	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return VncTinyList.ofList(replaceVals, meta);
	}

	@Override
	public VncList withMeta(final VncVal meta) {
		return new VncList(value, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncSequence.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncSequence.TYPE, VncVal.TYPE);
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}

	@Override
	public List<VncVal> getList() { 
		return value.toJavaList(); 
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
	public VncVal first() {
		return isEmpty() ? Constants.Nil : value.head();
	}

	@Override
	public VncVal last() {
		return isEmpty() ? Constants.Nil : value.last();
	}
	
	@Override
	public VncList rest() {
		return isEmpty() ? new VncList(getMeta()) : new VncList(value.tail(), getMeta());
	}
	
	@Override
	public VncList butlast() {
		return new VncList(value.dropRight(1), getMeta());
	}

	@Override
	public VncList slice(final int start, final int end) {
		return new VncList(value.subSequence(start, end), getMeta());
	}
	
	@Override
	public VncList slice(final int start) {
		return new VncList(value.subSequence(start), getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return this;
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(value, getMeta());
	}

	
	@Override
	public VncList addAtStart(final VncVal val) {
		return new VncList(value.prepend(val), getMeta());
	}
	
	@Override
	public VncList addAllAtStart(final VncSequence list) {
		final List<VncVal> items = list.getList();
		Collections.reverse(items);
		return new VncList(value.prependAll(items), getMeta());
	}
	
	@Override
	public VncList addAtEnd(final VncVal val) {
		return new VncList(value.append(val), getMeta());
	}
	
	@Override
	public VncList addAllAtEnd(final VncSequence list) {
		return new VncList(value.appendAll(list.getList()), getMeta());
	}
	
	@Override
	public VncList setAt(final int idx, final VncVal val) {
		return new VncList(value.update(idx, val), getMeta());
	}
	
	@Override
	public VncList removeAt(final int idx) {
		return new VncList(value.removeAt(idx), getMeta());
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.LIST;
	}

	@Override 
	public boolean isVncList() {
		return true;
	}

	@Override
	public Object convertToJavaObject() {
		return getList()
				.stream()
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toList());
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncList)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
					c = nth(ii).compareTo(((VncList)o).nth(ii));
					if (c != 0) {
						return c;
					}
				}
				return 0;
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
		return "(" + Printer.join(value.toJavaList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(value.toJavaList(), " ", print_readably) + ")";
	}

	public static VncList empty() {
		return VncTinyList.empty();
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/list");

    private static final long serialVersionUID = -1848883965231344442L;
 
	private final io.vavr.collection.Vector<VncVal> value;
}