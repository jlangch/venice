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
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.ErrorMessage;
import com.github.jlangch.venice.impl.util.StreamUtil;

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
		values = new VncVal[0];
	}
	
	private VncTinyVector(final VncVal[] vals, final VncVal meta) {
		super(meta);
		if (vals.length <= MAX_ELEMENTS) {
			values = vals;
		}
		else {
			throw new VncException(String.format(
						"A VncTinyVector is limited to %d elements", 
						MAX_ELEMENTS));
		}
	}

	
	public static VncVector of(final VncVal... mvs) {
		return mvs.length <= MAX_ELEMENTS ? new VncTinyVector(copy(mvs), Nil) : VncVector.of(mvs);
	}
	
	@Override
	public VncVector emptyWithMeta() {
		return new VncTinyVector(getMeta());
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
	public VncVector withValues(final List<? extends VncVal> replaceVals) {
		return VncVector.ofList(replaceVals, getMeta());
	}

	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return VncVector.ofList(replaceVals, meta);
	}

	@Override
	public VncVector withMeta(final VncVal meta) {
		return new VncTinyVector(copy(values), meta);
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
		final VncVal[] filtered = new VncVal[values.length];
		int len = 0;
		
		for(int ii=0; ii<values.length; ii++) {
			if (predicate.test(values[ii])) {
				filtered[len++] = values[ii];
			}
		}
		
		return len == 0
				? emptyWithMeta()
				: new VncTinyVector(slice(filtered, 0, len), getMeta());
	}

	@Override
	public VncVector map(final Function<? super VncVal, ? extends VncVal> mapper) {
		final VncVal[] mapped = new VncVal[values.length];
		for(int ii=0; ii<values.length; ii++) {
			mapped[ii] = mapper.apply(values[ii]);
		}
		return new VncTinyVector(mapped, getMeta()); 
	}

	@Override
	public List<VncVal> getList() {
		return Arrays.asList(values);
	}

	@Override
	public int size() {
		return values.length;
	}
	
	@Override
	public boolean isEmpty() {
		return values.length == 0;
	}

	@Override
	public VncVal nth(final int idx) {
		if (idx < 0 || idx >= values.length) {
			throw new VncException(String.format(
						"nth: index %d out of range for a list of size %d. %s", 
						idx, 
						values.length,
						isEmpty() ? "" : ErrorMessage.buildErrLocation(values[0])));
		}

		return values[idx];
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		return idx < 0 || idx >= values.length ?  defaultVal : values[idx];
	}

	@Override
	public VncVal first() {
		return values.length > 0 ? values[0] : Nil;
	}

	@Override
	public VncVal second() {
		return values.length > 1 ? values[1] : Nil;
	}

	@Override
	public VncVal third() {
		return values.length > 2 ? values[2] : Nil;
	}

	@Override
	public VncVal fourth() {
		return values.length > 3 ? values[3] : Nil;
	}

	@Override
	public VncVal last() {
		return values.length == 0 ?  Nil : values[values.length-1];
	}
	
	@Override
	public VncVector rest() {
		return slice(1, values.length);
	}
	
	@Override
	public VncVector butlast() {
		return slice(0, values.length-1);
	}

	@Override
	public VncVector drop(final int n) {
		return slice(n);
	}
	
	@Override
	public VncVector dropWhile(final Predicate<? super VncVal> predicate) {
		int drop = 0;
		for(int ii=0; ii<values.length; ii++) {
			if (predicate.test(values[ii])) {
				drop++;
			}
			else {
				break;
			}
		}
		
		return slice(drop);
	}
	
	@Override
	public VncVector take(final int n) {
		return slice(0, n);
	}
	
	@Override
	public VncVector takeWhile(final Predicate<? super VncVal> predicate) {
		int take = 0;
		for(int ii=0; ii<values.length; ii++) {
			if (predicate.test(values[ii])) {
				take++;
			}
			else {
				break;
			}
		}
		
		return slice(0, take);
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncTinyVector(
				slice(values, start, end), 
				getMeta());
	}
	
	@Override
	public VncVector slice(final int start) {
		return new VncTinyVector(
				slice(values, start, values.length), 
				getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return VncTinyList.of(values).withMeta(getMeta()); 
	}

	@Override
	public VncVector toVncVector() {
		return this;
	}

	
	@Override
	public VncVector addAtStart(final VncVal val) {
		if (values.length < MAX_ELEMENTS) {
			final VncVal[] copy = new VncVal[values.length + 1];
			System.arraycopy(values, 0, copy, 1, values.length);
			copy[0] = val;
			return new VncTinyVector(copy, getMeta());
		}
		else {
			return VncVector.of(val, values[0], values[1], values[2], values[3]).withMeta(getMeta());
		}
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list) {
		if (!(list instanceof VncLazySeq)) {  // lazy-seq do not have a size
			final int otherLen = list.size();
			final int thisLen = size();
			if (otherLen + thisLen <= MAX_ELEMENTS) {
				final VncVal[] copy = new VncVal[otherLen + thisLen];
				for(int ii=0; ii<otherLen; ii++) {
					copy[otherLen-1-ii] = list.nth(ii); // reverse order
				}
				System.arraycopy(values, 0, copy, otherLen, thisLen);
				return new VncTinyVector(copy, getMeta());
			}
		}
		
		final List<VncVal> vals = new ArrayList<>(list.getList());
		Collections.reverse(vals);
		vals.addAll(getList());	
		return VncVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		if (values.length < MAX_ELEMENTS) {
			final VncVal[] copy = new VncVal[values.length + 1];
			System.arraycopy(values, 0, copy, 0, values.length);
			copy[values.length] = val;
			return new VncTinyVector(copy, getMeta());
		}
		else {
			return VncVector.of(values[0], values[1], values[2], values[3], val).withMeta(getMeta());
		}
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		if (!(list instanceof VncLazySeq)) {  // lazy-seq do not have a size
			final int otherLen = list.size();
			final int thisLen = size();
			if (otherLen + thisLen <= MAX_ELEMENTS) {
				final VncVal[] copy = new VncVal[otherLen + thisLen];
				System.arraycopy(values, 0, copy, 0, thisLen);
				for(int ii=0; ii<otherLen; ii++) {
					copy[thisLen+ii] = list.nth(ii);
				}
				return new VncTinyVector(copy, getMeta());
			}
		}

		final List<VncVal> vals = new ArrayList<>(getList());
		vals.addAll(list.getList());		
		return VncVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		if (idx<0 || idx >= values.length) {
			throw new VncException(String.format(
					"VncTinyVector index out of bounds at %d", idx)); 
		}
		
		final VncVal[] copy = new VncVal[values.length];
		System.arraycopy(values, 0, copy, 0, values.length);
		copy[idx] = val;
		return new VncTinyVector(copy, getMeta());
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		if (idx<0 || idx >= values.length) {
			throw new VncException(String.format(
					"VncTinyVector index out of bounds at %d", idx)); 
		}
		
		if (idx == 0) {
			return values.length == 1 ? VncVector.empty() : rest();
		}
		else if (idx == values.length-1) {
			return butlast();
		}
		else {
			final VncVal[] copy = new VncVal[values.length-1];
			System.arraycopy(values, 0, copy, 0, idx);
			System.arraycopy(values, idx+1, copy, idx, values.length-idx-1);
			return new VncTinyVector(copy, getMeta());
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
		else if (Types.isVncVector(o)) {
			final Integer sizeThis = size();
			final Integer sizeOther = ((VncVector)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
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
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(values);
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
		VncTinyVector other = (VncTinyVector) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}

	
	private static VncVal[] copy(final VncVal[] arr) {
		if (arr.length == 0) {
			return new VncVal[0];
		}
		else {
			final VncVal[] copy = new VncVal[arr.length];
			System.arraycopy(arr, 0, copy, 0, arr.length);
			return copy;
		}
	}
	
	private static VncVal[] slice(final VncVal[] arr, final int start, final int end) {
		if (start < 0 || start >= arr.length || end <= start) {
			return new VncVal[0];
		}
		else if (start == 0 && end >= arr.length) {
			return arr;
		}
		else {
			final int len = (end > arr.length ? arr.length : end) - start;
			final VncVal[] copy = new VncVal[len];
			System.arraycopy(arr, start, copy, 0, len);
			return copy;
		}
	}
	
	
	
	private static class MappingIterator implements Iterator<VncVal> {

		public MappingIterator(final VncTinyVector value) {
			this.value = value;
		}
		
	    @Override
	    public boolean hasNext() { 
	    	return index < value.size(); 
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

  	private final VncVal[] values;
}