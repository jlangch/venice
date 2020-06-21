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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;


/**
 * An immutable list optimized for keeping 1 to 4 values.
 * Returns a VncList if the list grows beyond its max length.
 * 
 * <p>Most of the lists in a typical Venice application have less than 5
 * items. This optimized implementation for an immutable tiny list is 
 * much faster than a VAVR persistent list that can hold an arbitrary 
 * number of items.
 */
public class VncTinyList extends VncList {

	public VncTinyList() {
		this(null);
	}
	
	public VncTinyList(final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 0;
		this.first = Constants.Nil;
		this.second = Constants.Nil;
		this.fourth = Constants.Nil;
		this.third = Constants.Nil;
	}
	
	public VncTinyList(final VncVal first, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 1;
		this.first = first;
		this.second = Constants.Nil;
		this.third = Constants.Nil;
		this.fourth = Constants.Nil;
	}

	public VncTinyList(final VncVal first, final VncVal second, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 2;
		this.first = first;
		this.second = second;
		this.third = Constants.Nil;
		this.fourth = Constants.Nil;
	}

	public VncTinyList(final VncVal first, final VncVal second, final VncVal third, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 3;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = Constants.Nil;
	}

	public VncTinyList(final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 4;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	private VncTinyList(final int len, final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = len;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
	
	public static VncList of(final VncVal... mvs) {
		switch (mvs.length) {
			case 0:	return VncTinyList.empty();
			case 1:	return new VncTinyList(mvs[0], null);
			case 2:	return new VncTinyList(mvs[0], mvs[1], null);
			case 3:	return new VncTinyList(mvs[0], mvs[1], mvs[2], null);
			default: return VncList.of(mvs);
		}
	}

	public static VncList range(final int from, final int toExclusive) {
		final List<VncVal> list = new ArrayList<>();
		for(int ii=from; ii<toExclusive; ii++) list.add(new VncLong(ii));
		return VncTinyList.ofList(list, null);
	}

	public static VncList ofList(final List<? extends VncVal> list, final VncVal meta) {
		switch (list.size()) {
			case 0:	return new VncTinyList(meta);
			case 1:	return new VncTinyList(list.get(0), meta);
			case 2:	return new VncTinyList(list.get(0), list.get(1), meta);
			case 3:	return new VncTinyList(list.get(0), list.get(1), list.get(2), meta);
			case 4:	return new VncTinyList(list.get(0), list.get(1), list.get(2), list.get(3), meta);
			default: return new VncList(list, meta);
		}
	}
	
	@Override
	public VncList emptyWithMeta() {
		return new VncTinyList(getMeta());
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
		return new VncTinyList(len, first, second, third, fourth, meta);
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		if (len > 0) {
			action.accept(first);
			if (len > 1) {
				action.accept(second);
				if (len > 2) {
					action.accept(third);
					if (len > 3) action.accept(fourth);
				}
			}
		}
	}

	@Override
	public List<VncVal> getList() { 
		final ArrayList<VncVal> list = new ArrayList<>(len);
		if (len > 0) {
			list.add(first);
			if (len > 1) {
				list.add(second);
				if (len > 2) {
					list.add(third);
					if (len > 3) list.add(fourth);
				}
			}
		}
		return list;
	}

	@Override
	public int size() {
		return len;
	}
	
	@Override
	public boolean isEmpty() {
		return len == 0;
	}

	@Override
	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= len) {
			throw new VncException(String.format(
						"nth: index %d out of range for a list of size %d. %s", 
						idx, 
						len,
						isEmpty() ? "" : ErrorMessage.buildErrLocation(first)));
		}

		switch(idx) {
			case 0:	return first;
			case 1:	return second;
			case 2:	return third;
			case 3:	return fourth;
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		if (idx < 0 || idx >= len) {
			return defaultVal;
		}
		else {
			switch(idx) {
				case 0:	return first;
				case 1:	return second;
				case 2:	return third;
				case 3:	return fourth;
				default: return defaultVal;
			}
		}
	}

	@Override
	public VncVal first() {
		return first;
	}

	@Override
	public VncVal second() {
		return second;
	}

	@Override
	public VncVal third() {
		return third;
	}

	@Override
	public VncVal fourth() {
		return fourth;
	}

	@Override
	public VncVal last() {
		if (len == 0) return Constants.Nil;
		if (len == 1) return first;
		if (len == 2) return second;
		if (len == 3) return third;
		if (len == 4) return fourth;
		else throw new IllegalStateException("Length out of range");
	}
	
	@Override
	public VncList rest() {
		if (len == 0) return this;
		if (len == 1) return new VncTinyList(getMeta());
		if (len == 2) return new VncTinyList(second, getMeta());
		if (len == 3) return new VncTinyList(second, third, getMeta());
		if (len == 4) return new VncTinyList(second, third, fourth, getMeta());
		else throw new IllegalStateException("Length out of range");
	}
	
	@Override
	public VncList butlast() {
		if (len == 0) return this;
		if (len == 1) return new VncTinyList(getMeta());
		if (len == 2) return new VncTinyList(first, getMeta());
		if (len == 3) return new VncTinyList(first, second, getMeta());
		if (len == 4) return new VncTinyList(first, second, third, getMeta());
		else throw new IllegalStateException("Length out of range");
	}

	@Override
	public VncList slice(final int start, final int end) {
		return start == 0 && end >= len
				? this
				: VncTinyList.ofList(getList().subList(start, end), getMeta());
	}
	
	@Override
	public VncList slice(final int start) {
		if (start == 0) {
			return this;
		}
		else if (start == 1) {
			return rest();
		}
		else {
			return VncTinyList.ofList(getList().subList(start, len), getMeta());
		}
	}
	
	@Override
	public VncList toVncList() {
		return this;
	}

	@Override
	public VncVector toVncVector() {
		switch (len) {
			case 0:	return new VncVector(getMeta());
			case 1: return VncVector.of(first).withMeta(getMeta()); 
			case 2:	return VncVector.of(first, second).withMeta(getMeta()); 
			case 3:	return VncVector.of(first, second, third).withMeta(getMeta());
			case 4:	return VncVector.of(first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}

	
	@Override
	public VncList addAtStart(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyList(val, getMeta()); 
			case 1: return new VncTinyList(val, first, getMeta()); 
			case 2:	return new VncTinyList(val, first, second, getMeta()); 
			case 3:	return new VncTinyList(val, first, second, third, getMeta()); 
			case 4:	return VncList.of(val, first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}
	
	@Override
	public VncList addAllAtStart(final VncSequence list) {
		final List<VncVal> vals = new ArrayList<>(list.getList());
		Collections.reverse(vals);
		vals.addAll(getList());

		return VncTinyList.ofList(vals, getMeta());
	}
	
	@Override
	public VncList addAtEnd(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyList(val, getMeta()); 
			case 1: return new VncTinyList(first, val, getMeta()); 
			case 2:	return new VncTinyList(first, second, val, getMeta()); 
			case 3:	return new VncTinyList(first, second, third, val, getMeta()); 
			case 4:	return VncList.of(first, second, third, fourth, val).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}
	
	@Override
	public VncList addAllAtEnd(final VncSequence list) {
		final List<VncVal> vals = getList();
		vals.addAll(list.getList());
		
		return VncTinyList.ofList(vals, getMeta());
	}
	
	@Override
	public VncList setAt(final int idx, final VncVal val) {
		final List<VncVal> vals = getList();
		vals.set(idx, val);
		
		return VncTinyList.ofList(vals, getMeta());
	}
	
	@Override
	public VncList removeAt(final int idx) {
		if (idx == 0) {
			return rest();
		}
		else if (idx == (len-1)) {
			return butlast();
		}
		else {
			final List<VncVal> vals = getList();
			vals.remove(idx);
			
			return VncTinyList.ofList(vals, getMeta());
		}
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
		final ArrayList<Object> list = new ArrayList<>(len);
		switch (len) {
			case 0:	
				break;
			case 1:	
				list.add(first.convertToJavaObject()); 
				break;
			case 2:	
				list.add(first.convertToJavaObject()); 
				list.add(second.convertToJavaObject());
				break;
			case 3:	
				list.add(first.convertToJavaObject()); 
				list.add(second.convertToJavaObject()); 
				list.add(third.convertToJavaObject()); 
				break;
			case 4:	
				list.add(first.convertToJavaObject()); 
				list.add(second.convertToJavaObject()); 
				list.add(third.convertToJavaObject()); 
				list.add(fourth.convertToJavaObject()); 
				break;
			default: 
				throw new IllegalStateException("List length out of range");
		}
		return list;
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncTinyList)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
					c = nth(ii).compareTo(((VncTinyList)o).nth(ii));
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
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
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
		VncTinyList other = (VncTinyList) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(getList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(getList(), " ", print_readably) + ")";
	}

	public static VncTinyList empty() {
		return EMPTY;
	}
	
	
	public static final VncKeyword TYPE = new VncKeyword(":core/list");
	
    private static final long serialVersionUID = -1848883965231344442L;
    private static final VncTinyList EMPTY = new VncTinyList();

    private final int len;
	private final VncVal first;
	private final VncVal second;
	private final VncVal third;
	private final VncVal fourth;
}