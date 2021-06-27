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
import java.util.Arrays;
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
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.StreamUtil;
import com.github.jlangch.venice.impl.util.ArityExceptions.FnType;

//	Benchmark                       Mode  Cnt  Score   Error  Units
//	VncTinyVectorBenchmark.append   avgt    3  9.900 ± 0.762  ns/op
//	VncTinyVectorBenchmark.butlast  avgt    3  9.036 ± 0.199  ns/op
//	VncTinyVectorBenchmark.first    avgt    3  3.007 ± 0.060  ns/op
//	VncTinyVectorBenchmark.last     avgt    3  3.609 ± 0.370  ns/op
//	VncTinyVectorBenchmark.prepend  avgt    3  9.061 ± 3.517  ns/op
//	VncTinyVectorBenchmark.rest     avgt    3  8.993 ± 0.503  ns/op
//	
//	Benchmark                    Mode  Cnt    Score   Error  Units
//	VavrVectorBenchmark.append   avgt    3   91.589 ± 2.012  ns/op
//	VavrVectorBenchmark.butlast  avgt    3   10.162 ± 0.534  ns/op
//	VavrVectorBenchmark.drop_1   avgt    3    9.519 ± 0.277  ns/op
//	VavrVectorBenchmark.first    avgt    3    5.040 ± 0.099  ns/op
//	VavrVectorBenchmark.last     avgt    3    5.978 ± 0.243  ns/op
//	VavrVectorBenchmark.prepend  avgt    3  130.839 ± 4.116  ns/op
//	VavrVectorBenchmark.rest     avgt    3    9.515 ± 0.106  ns/op

/**
 * An immutable vector optimized for keeping 1 to 4 values.
 * Returns a VncVector if the list grows beyond its max length.
 * 
 * <p>Most of the vectors in a typical Venice application have less than 5
 * items. This optimized implementation for an immutable tiny vector is 
 * much faster than a VAVR persistent vector that can hold an arbitrary 
 * number of items.
 */
public class VncTinyVector extends VncVector {

	public VncTinyVector() {
		this(null);
	}
	
	public VncTinyVector(final VncVal meta) {
		super(meta);
		this.len = 0;
		this.first = Constants.Nil;
		this.second = Constants.Nil;
		this.third = Constants.Nil;
		this.fourth = Constants.Nil;
	}
	

	private VncTinyVector(final int len, final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta);
		this.len = len;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	public static VncTinyVector of(final VncVal... mvs) {
		switch (mvs.length) {
			case 0:	return EMPTY;
			case 1:	return new VncTinyVector(1, mvs[0], Nil,    Nil,    Nil,    null);
			case 2:	return new VncTinyVector(2, mvs[0], mvs[1], Nil,    Nil,    null);
			case 3:	return new VncTinyVector(3, mvs[0], mvs[1], mvs[2], Nil,    null);
			case 4:	return new VncTinyVector(4, mvs[0], mvs[1], mvs[2], mvs[3], null);
			default: throw new IllegalStateException("Length out of range");
		}
	}

	public static VncTinyVector ofArr(final VncVal[] mvs, final VncVal meta) {
		switch (mvs.length) {
			case 0:	return new VncTinyVector(meta);
			case 1:	return new VncTinyVector(1, mvs[0], Nil,    Nil,    Nil,    meta);
			case 2:	return new VncTinyVector(2, mvs[0], mvs[1], Nil,    Nil,    meta);
			case 3:	return new VncTinyVector(3, mvs[0], mvs[1], mvs[2], Nil,    meta);
			case 4:	return new VncTinyVector(4, mvs[0], mvs[1], mvs[2], mvs[3], meta);
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncVal apply(final VncList args) {
		ArityExceptions.assertArity(this, FnType.Collection, args, 1, 2);

		if (args.size() == 1) {
			return nth(Coerce.toVncLong(args.first()).getValue().intValue());
		}
		else {
			return nthOrDefault(Coerce.toVncLong(args.first()).getValue().intValue(), args.second());
		}
	}
	
	@Override
	public VncVector emptyWithMeta() {
		return new VncTinyVector(getMeta());
	}
	
	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals) {
		return replaceVals.size() < MAX_ELEMENTS
				? VncTinyVector.ofArr(replaceVals.toArray(new VncVal[0]), getMeta())
				: VncVector.ofList(replaceVals, getMeta());
	}

	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return replaceVals.size() < MAX_ELEMENTS
				? VncTinyVector.ofArr(replaceVals.toArray(new VncVal[0]), meta)
				: VncVector.ofList(replaceVals, meta);
	}

	@Override
	public VncVector withMeta(final VncVal meta) {
		return new VncTinyVector(len, first, second, third, fourth, meta);
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
	public VncVector filter(final Predicate<? super VncVal> predicate) {
		final ArrayList<VncVal> list = new ArrayList<>(len);		
		if (len > 0) {
			if (predicate.test(first)) list.add(first);
			if (len > 1) {
				if (predicate.test(second)) list.add(second);
				if (len > 2) {
					if (predicate.test(third)) list.add(third);
					if (len > 3) {
						if (predicate.test(fourth)) list.add(fourth);
					}
				}
			}
		}
		
		return VncVector.ofList(list, getMeta()); 
	}

	@Override
	public VncVector map(final Function<? super VncVal, ? extends VncVal> mapper) {
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
		
		return VncTinyVector.ofArr(values, getMeta()); 
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
						"nth: index %d out of range for a vector of size %d. %s", 
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
		switch(len) {
			case 0:	return Constants.Nil;
			case 1:	return first;
			case 2:	return second;
			case 3:	return third;
			case 4:	return fourth;
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncVector rest() {
		switch(len) {
			case 0:	return this;
			case 1:	return new VncTinyVector(getMeta());
			case 2:	return new VncTinyVector(1, second, Nil,   Nil,    Nil, getMeta());
			case 3:	return new VncTinyVector(2, second, third, Nil,    Nil, getMeta());
			case 4:	return new VncTinyVector(3, second, third, fourth, Nil, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override
	public VncVector butlast() {
		switch(len) {
			case 0:	return this;
			case 1:	return new VncTinyVector(getMeta());
			case 2:	return new VncTinyVector(1, first, Nil,    Nil,   Nil, getMeta());
			case 3:	return new VncTinyVector(2, first, second, Nil,   Nil, getMeta());
			case 4:	return new VncTinyVector(3, first, second, third, Nil, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public VncVector drop(final int n) {
		if (n <= 0) {
			return this;
		}
		else if (n >= len) {
			return EMPTY;
		}
		else {
			return slice(n);
		}
	}
	
	@Override
	public VncVector dropWhile(final Predicate<? super VncVal> predicate) {
		final List<VncVal> list = getJavaList();
		for(int i=0; i<list.size(); i++) {
			final boolean drop = predicate.test(list.get(i));
			if (!drop) {
				return VncVector.ofList(list.subList(i, list.size()), getMeta());
			}
		}
		
		return new VncTinyVector(getMeta());
	}
	
	@Override
	public VncVector take(final int n) {
		return slice(0, n);
	}
	
	@Override
	public VncVector takeWhile(final Predicate<? super VncVal> predicate) {
		final List<VncVal> list = getJavaList();
		for(int i=0; i<list.size(); i++) {
			final boolean take = predicate.test(list.get(i));
			if (!take) {
				return VncVector.ofList(list.subList(0, i), getMeta());
			}
		}
		
		return this;
	}
	
	@Override 
	public VncVector reverse() {
		switch(len) {
			case 0:	return this;
			case 1:	return new VncTinyVector(1, first, Nil,    Nil,   Nil,    getMeta());
			case 2:	return new VncTinyVector(2, second, first, Nil,   Nil,    getMeta());
			case 3:	return new VncTinyVector(3, third, second, first, Nil,    getMeta());
			case 4:	return new VncTinyVector(4, fourth, third, second, first, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}
	
	@Override 
	public VncVector shuffle() {
		final List<VncVal> list = getJavaList();
		Collections.shuffle(list);
		return VncTinyVector.ofAll(list, getMeta());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		if (start < 0) {
			 throw new IllegalStateException("Vector index out of range");
		}
		else if (start >= len || end <= start) {
			return EMPTY;
		}
		else {
			final int len_ = Math.min(end, len) - start;
			final VncVal[] vals = new VncVal[len_];
			for(int ii=0; ii<len_; ii++) vals[ii] = nth(ii+start);
			return VncTinyVector.ofArr(vals, getMeta());
		}
	}
	
	@Override
	public VncVector slice(final int start) {
		if (start < 0) {
			 throw new IllegalStateException("Vector index out of range");
		}
		else if (start >= len) {
			return EMPTY;
		}
		else if (start == 0) {
			return this;
		}
		else {
			final int len_ = len - start;
			final VncVal[] vals = new VncVal[len_];
			for(int ii=0; ii<len_; ii++) vals[ii] = nth(ii+start);
			return VncTinyVector.ofArr(vals, getMeta());
		}
	}
	
	@Override
	public VncVector toVncVector() {
		return this;
	}

	@Override
	public VncList toVncList() {
		switch (len) {
			case 0:	return new VncTinyList(getMeta());
			case 1: return VncTinyList.of(first).withMeta(getMeta()); 
			case 2:	return VncTinyList.of(first, second).withMeta(getMeta()); 
			case 3:	return VncTinyList.of(first, second, third).withMeta(getMeta());
			case 4:	return VncTinyList.of(first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}

	
	@Override
	public VncVector addAtStart(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyVector(1, val, Nil,   Nil,    Nil,   getMeta()); 
			case 1: return new VncTinyVector(2, val, first, Nil,    Nil,   getMeta()); 
			case 2:	return new VncTinyVector(3, val, first, second, Nil,   getMeta()); 
			case 3:	return new VncTinyVector(4, val, first, second, third, getMeta());
			case 4:	return VncVector.of(val, first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list, final boolean reverseAdd) {
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
				return VncTinyVector.of(vals);
			}
		}

		final VncSequence seq = reverseAdd ? list.reverse() : list;
		return VncVector.ofAll(seq, getMeta()).addAllAtEnd(this);
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyVector(1, val,   Nil,    Nil,   Nil, getMeta()); 
			case 1:	return new VncTinyVector(2, first, val,    Nil,   Nil, getMeta()); 
			case 2:	return new VncTinyVector(3, first, second, val,   Nil, getMeta()); 
			case 3:	return new VncTinyVector(4, first, second, third, val, getMeta());
			case 4:	return VncVector.of(first, second, third, fourth, val).withMeta(getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		if (!(list instanceof VncLazySeq)) { // no size() support
			final int otherLen = list.size();
			if (otherLen == 0) {
				return this;
			}
			
			if (otherLen + len <= MAX_ELEMENTS) {
				final VncVal[] vals = new VncVal[otherLen + len];
				for(int ii=0; ii<len; ii++) vals[ii] = nth(ii);
				for(int ii=0; ii<otherLen; ii++) vals[ii+len] = list.nth(ii);
				return VncTinyVector.of(vals);
			}
		}
		
		return VncVector.ofAll(this, getMeta()).addAllAtEnd(list);
	}
	
	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		if (idx < 0 || idx >= len) {
			 throw new IllegalStateException("Vector index out of range");
		}

		switch (idx) {
			case 0:	return new VncTinyVector(len, val, second, third, fourth, getMeta());
			case 1:	return new VncTinyVector(len, first, val, third, fourth, getMeta());
			case 2:	return new VncTinyVector(len, first, second, val, fourth, getMeta());
			case 3:	return new VncTinyVector(len, first, second, third, val, getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		if (idx < 0 || idx >= len) {
			 throw new IllegalStateException("Vector index out of range");
		}
		
		if (len == 1) {
			return emptyWithMeta();
		}
		else {
			switch (idx) {
				case 0:	return new VncTinyVector(len-1, second, third, fourth, Nil, getMeta());
				case 1:	return new VncTinyVector(len-1, first, third, fourth, Nil, getMeta());
				case 2:	return new VncTinyVector(len-1, first, second, fourth, Nil, getMeta());
				case 3:	return new VncTinyVector(len-1, first, second, third, Nil, getMeta());
				default: throw new IllegalStateException("Vector length out of range");
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
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncVector(o)) {
			int c = Integer.compare(size(), ((VncVector)o).size());
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<size(); ii++) {
					c = nth(ii).compareTo(((VncVector)o).nth(ii));
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
		VncTinyVector other = (VncTinyVector) obj;
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
		return "[" + Printer.join(this, " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(this, " ", print_readably) + "]";
	}

	
	private static class MappingIterator implements Iterator<VncVal> {

		public MappingIterator(final VncTinyVector value) {
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
		
		private final VncTinyVector value;
	}

	
	public static final VncKeyword TYPE = new VncKeyword(":core/vector");
	public static final VncTinyVector EMPTY = new VncTinyVector();
	public static final int MAX_ELEMENTS = 4;

	private static final long serialVersionUID = -1848883965231344442L;

	private final int len;
	private final VncVal first;
	private final VncVal second;
	private final VncVal third;
	private final VncVal fourth;
}