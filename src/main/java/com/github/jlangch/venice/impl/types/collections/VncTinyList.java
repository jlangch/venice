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

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.StreamUtil;


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
		super(meta);
		this.len = 0;
		this.first = Nil;
		this.second = Nil;
		this.third = Nil;
		this.fourth = Nil;
	}
	
	private VncTinyList(final int len, final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta);
		this.len = len;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	public static VncTinyList of(final VncVal... mvs) {
		switch (mvs.length) {
			case 0:	return EMPTY;
			case 1:	return new VncTinyList(1, mvs[0], Nil,    Nil,    Nil,    null);
			case 2:	return new VncTinyList(2, mvs[0], mvs[1], Nil,    Nil,    null);
			case 3:	return new VncTinyList(3, mvs[0], mvs[1], mvs[2], Nil,    null);
			case 4:	return new VncTinyList(4, mvs[0], mvs[1], mvs[2], mvs[3], null);
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	public static VncTinyList ofArr(final VncVal[] mvs, final VncVal meta) {
		switch (mvs.length) {
			case 0:	return new VncTinyList(meta);
			case 1:	return new VncTinyList(1, mvs[0], Nil,    Nil,    Nil,    meta);
			case 2:	return new VncTinyList(2, mvs[0], mvs[1], Nil,    Nil,    meta);
			case 3:	return new VncTinyList(3, mvs[0], mvs[1], mvs[2], Nil,    meta);
			case 4:	return new VncTinyList(4, mvs[0], mvs[1], mvs[2], mvs[3], meta);
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncList emptyWithMeta() {
		return new VncTinyList(getMeta());
	}
	
	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals) {
		return replaceVals.size() < MAX_ELEMENTS
				? VncTinyList.ofArr(replaceVals.toArray(new VncVal[0]), getMeta())
				: VncList.ofList(replaceVals, getMeta());
	}

	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return replaceVals.size() < MAX_ELEMENTS
				? VncTinyList.ofArr(replaceVals.toArray(new VncVal[0]),meta)
				: VncList.ofList(replaceVals, meta);
	}

	@Override
	public VncList withMeta(final VncVal meta) {
		return new VncTinyList(len, first, second, third, fourth, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return super.getType();
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return super.getAllSupertypes();
	}

    @Override
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : new MappingIterator(this);
    }

    @Override
	public Stream<VncVal> stream() {
		return StreamUtil.stream(iterator());
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		iterator().forEachRemaining(action);
	}
	
	@Override
	public VncList filter(final Predicate<? super VncVal> predicate) {
		final VncVal[] values = new VncVal[len];
		int idx = 0;
		
		if (len > 0) {
			if (predicate.test(first)) values[idx++] = first;
			if (len > 1) {
				if (predicate.test(second)) values[idx++] = second;
				if (len > 2) {
					if (predicate.test(third)) values[idx++] = third;
					if (len > 3) {
						if (predicate.test(fourth)) values[idx++] = fourth;
					}
				}
			}
		}

		switch (idx) {
			case 0:	return EMPTY;
			case 1:	return new VncTinyList(1, values[0], Nil,       Nil,       Nil,       getMeta());
			case 2:	return new VncTinyList(2, values[0], values[1], Nil,       Nil,       getMeta());
			case 3:	return new VncTinyList(3, values[0], values[1], values[2], Nil,       getMeta());
			case 4:	return new VncTinyList(4, values[0], values[1], values[2], values[3], getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public VncList map(final Function<? super VncVal, ? extends VncVal> mapper) {
		final VncVal[] values = new VncVal[len];
		int idx = 0;
		
		if (len > 0) {
			values[idx++] = mapper.apply(first);
			if (len > 1) {
				values[idx++] = mapper.apply(second);
				if (len > 2) {
					values[idx++] = mapper.apply(third);
					if (len > 3) {
						values[idx++] = mapper.apply(fourth);
					}
				}
			}
		}
		
		return VncTinyList.ofArr(values, getMeta()); 
	}

	@Override
	public List<VncVal> getJavaList() { 
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
						"nth: index %d out of range for a list of size %d.", 
						idx, 
						len));
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
		switch(len) {
			case 0:	return Nil;
			case 1:	return first;
			case 2:	return second;
			case 3:	return third;
			case 4:	return fourth;
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncList rest() {
		switch(len) {
			case 0:	return this;
			case 1:	return new VncTinyList(getMeta());
			case 2:	return new VncTinyList(1, second, Nil,   Nil,    Nil, getMeta());
			case 3:	return new VncTinyList(2, second, third, Nil,    Nil, getMeta());
			case 4:	return new VncTinyList(3, second, third, fourth, Nil, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncList butlast() {
		switch(len) {
			case 0:	return this;
			case 1:	return new VncTinyList(getMeta());
			case 2:	return new VncTinyList(1, first, Nil,    Nil,   Nil, getMeta());
			case 3:	return new VncTinyList(2, first, second, Nil,   Nil, getMeta());
			case 4:	return new VncTinyList(3, first, second, third, Nil, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public VncList drop(final int n) {
		if (n <= 0) {
			return this;
		}
		else if (n >= len) {
			return EMPTY;
		}
		
		switch(n) {
			case 1:	return new VncTinyList(len-1, second, third,  fourth, Nil, getMeta());
			case 2:	return new VncTinyList(len-2, third,  fourth, Nil,    Nil, getMeta());
			case 3:	return new VncTinyList(len-3, fourth, Nil,    Nil,    Nil, getMeta());
			default: return EMPTY;
		}
	}
	
	@Override
	public VncList dropWhile(final Predicate<? super VncVal> predicate) {
		final List<VncVal> list = getJavaList();
		for(int i=0; i<list.size(); i++) {
			final boolean drop = predicate.test(list.get(i));
			if (!drop) {
				return VncList.ofList(list.subList(i, list.size()), getMeta());
			}
		}
		
		return new VncTinyList(getMeta());
	}
	
	@Override
	public VncList take(final int n) {
		if (n <= 0) {
			return EMPTY;
		}
		else if (n >= len) {
			return this;
		}
		
		switch(n) {
			case 1:	return new VncTinyList(1, first, Nil,    Nil,   Nil, getMeta());
			case 2:	return new VncTinyList(2, first, second, Nil,   Nil, getMeta());
			case 3:	return new VncTinyList(3, first, second, third, Nil, getMeta());
			default: return this;
		}
	}
	
	@Override
	public VncList takeWhile(final Predicate<? super VncVal> predicate) {
		final List<VncVal> list = getJavaList();
		for(int i=0; i<list.size(); i++) {
			final boolean take = predicate.test(list.get(i));
			if (!take) {
				return VncList.ofList(list.subList(0, i), getMeta());
			}
		}
		
		return this;
	}
	
	@Override 
	public VncList reverse() {
		switch(len) {
			case 0:	return this;
			case 1:	return this;
			case 2:	return new VncTinyList(2, second, first, Nil,   Nil,    getMeta());
			case 3:	return new VncTinyList(3, third, second, first, Nil,    getMeta());
			case 4:	return new VncTinyList(4, fourth, third, second, first, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override 
	public VncList shuffle() {
		if (len <= 1) {
			return this;
		}
		else {
			final List<VncVal> list = getJavaList();
			Collections.shuffle(list);
			return VncList.ofList(list, getMeta());
		}
	}

	@Override
	public VncList slice(final int start, final int end) {
		if (start < 0) {
			 throw new IllegalStateException("List index out of range");
		}
		else if (start >= len || end <= start) {
			return EMPTY;
		}
		else {
			final int len_ = Math.min(end, len) - start;
			final VncVal[] vals = new VncVal[len_];
			for(int ii=0; ii<len_; ii++) vals[ii] = nth(ii+start);
			return VncTinyList.ofArr(vals, getMeta());
		}
	}
	
	@Override
	public VncList slice(final int start) {
		if (start <= 0) {
			return this;
		}
		else if (start >= len) {
			return EMPTY;
		}
		
		switch(start) {
			case 1:	return new VncTinyList(len-1, second, third,  fourth, Nil, getMeta());
			case 2:	return new VncTinyList(len-2, third,  fourth, Nil,    Nil, getMeta());
			case 3:	return new VncTinyList(len-3, fourth, Nil,    Nil,    Nil, getMeta());
			default: return EMPTY;
		}
	}
	
	@Override
	public VncList toVncList() {
		return this;
	}

	@Override
	public VncVector toVncVector() {
		switch (len) {
			case 0:	return new VncTinyVector(getMeta());
			case 1: return VncTinyVector.of(first).withMeta(getMeta()); 
			case 2:	return VncTinyVector.of(first, second).withMeta(getMeta()); 
			case 3:	return VncTinyVector.of(first, second, third).withMeta(getMeta());
			case 4:	return VncTinyVector.of(first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}

	@Override
	public VncList addAtStart(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyList(1, val, Nil,   Nil,    Nil,  getMeta()); 
			case 1: return new VncTinyList(2, val, first, Nil,    Nil,  getMeta()); 
			case 2:	return new VncTinyList(3, val, first, second, Nil,  getMeta()); 
			case 3:	return new VncTinyList(4, val, first, second, third, getMeta());
			case 4:	return VncList.of(val, first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}
	
	@Override
	public VncList addAllAtStart(final VncSequence list, final boolean reverseAdd) {
		if (!(list instanceof VncLazySeq)) { // no size() support
			final int otherLen = list.size();
			if (otherLen == 0) {
				return this;
			}
			
			if (otherLen + len <= MAX_ELEMENTS) {
				final VncVal[] vals = new VncVal[otherLen + len];
				if (reverseAdd) {
					for(int ii=0; ii<otherLen; ii++) vals[ii] = list.nth(otherLen-ii-1);
				}
				else {
					for(int ii=0; ii<otherLen; ii++) vals[ii] = list.nth(ii);
				}
				for(int ii=0; ii<len; ii++) vals[ii+otherLen] = nth(ii);
				return VncTinyList.of(vals);
			}
		}
		
		final VncSequence seq = reverseAdd ? list.reverse() : list;
		return VncList.ofAll(seq, getMeta()).addAllAtEnd(this);
	}
	
	@Override
	public VncList addAtEnd(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyList(1, val,   Nil,    Nil,   Nil, getMeta()); 
			case 1:	return new VncTinyList(2, first, val,    Nil,   Nil, getMeta()); 
			case 2:	return new VncTinyList(3, first, second, val,   Nil, getMeta()); 
			case 3:	return new VncTinyList(4, first, second, third, val,  getMeta());
			case 4:	return VncList.of(first, second, third, fourth, val).withMeta(getMeta());
			default: throw new IllegalStateException("List length out of range");
		}
	}
	
	@Override
	public VncList addAllAtEnd(final VncSequence list) {
		if (!(list instanceof VncLazySeq)) { // no size() support
			final int otherLen = list.size();
			if (otherLen == 0) {
				return this;
			}
			
			if (otherLen + len <= MAX_ELEMENTS) {
				final VncVal[] vals = new VncVal[otherLen + len];
				for(int ii=0; ii<len; ii++) vals[ii] = nth(ii);
				for(int ii=0; ii<otherLen; ii++) vals[ii+len] = list.nth(ii);
				return VncTinyList.of(vals);
			}
		}

		return VncList.ofAll(this, getMeta()).addAllAtEnd(list);
	}
	
	@Override
	public VncList setAt(final int idx, final VncVal val) {
		if (idx < 0 || idx >= len) {
			 throw new IllegalStateException("List index out of range");
		}

		final VncVal[] vals = new VncVal[len];
		for(int ii=0; ii<len; ii++) vals[ii] = nth(ii);
		vals[idx] = val;
		return VncTinyList.of(vals);
	}
	
	@Override
	public VncList removeAt(final int idx) {
		if (idx < 0 || idx >= len) {
			 throw new IllegalStateException("List index out of range");
		}
		
		if (len == 1) {
			return emptyWithMeta();
		}
		else {
			switch (idx) {
				case 0:	return new VncTinyList(len-1, second, third, fourth, Nil, getMeta());
				case 1:	return new VncTinyList(len-1, first, third, fourth, Nil, getMeta());
				case 2:	return new VncTinyList(len-1, first, second, fourth, Nil, getMeta());
				case 3:	return new VncTinyList(len-1, first, second, third, Nil, getMeta());
				default: throw new IllegalStateException("List length out of range");
			}
		}
	}

	@Override
	public Object convertToJavaObject() {
		return stream()
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toList());
	}
	
	@Override
	public int compareTo(final VncVal o) {
		if (o == Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			int c = Integer.compare(size(), ((VncList)o).size());
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<size(); ii++) {
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
		int result = 1;
		result = prime * result + len;
		result = prime * result + ((first == null)  ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null)  ? 0 : third.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		VncTinyList other = (VncTinyList) obj;
		if (len != other.len)
			return false;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
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
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(this, " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(this, " ", print_readably) + ")";
	}

	
	private static class MappingIterator implements Iterator<VncVal> {

		public MappingIterator(final VncTinyList value) {
			this.value = value;
		}
		
		@Override
		public boolean hasNext() { 
			return index < value.len; 
		}
		
		@Override
		public VncVal next() { 
			return value.nth(index++);
		}
		
		@Override
		public String toString() {
			return "MappingIterator()";
		}
		
		private int index;
		
		private final VncTinyList value;
	}

	
	public static final VncKeyword TYPE = VncList.TYPE;
	public static final VncTinyList EMPTY = new VncTinyList();
	public static final int MAX_ELEMENTS = 4;

	private static final long serialVersionUID = -1848883965231344442L;

	private final int len;
	private final VncVal first;
	private final VncVal second;
	private final VncVal third;
	private final VncVal fourth;
}