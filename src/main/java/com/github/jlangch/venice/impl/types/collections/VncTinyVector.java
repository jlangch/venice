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
import com.github.jlangch.venice.impl.functions.FunctionsUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;

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
	
	public VncTinyVector(final VncVal first, final VncVal meta) {
		super(meta);
		this.len = 1;
		this.first = first;
		this.second = Constants.Nil;
		this.third = Constants.Nil;
		this.fourth = Constants.Nil;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal meta) {
		super(meta);
		this.len = 2;
		this.first = first;
		this.second = second;
		this.third = Constants.Nil;
		this.fourth = Constants.Nil;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal third, final VncVal meta) {
		super(meta);
		this.len = 3;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = Constants.Nil;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta);
		this.len = 4;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	public VncTinyVector(final int len, final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta);
		this.len = len;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
		
	public static VncVector range(final int from, final int toExclusive) {
		final List<VncVal> list = new ArrayList<>();
		for(int ii=from; ii<toExclusive; ii++) list.add(new VncLong(ii));
		return VncVector.ofList(list, null);
	}

	
	@Override
	public VncVal apply(final VncList args) {
		FunctionsUtil.assertArity("nth", args, 1);
		
		return nth(Coerce.toVncLong(args.first()).getValue().intValue());
	}
	
	@Override
	public VncVector emptyWithMeta() {
		return new VncTinyVector(getMeta());
	}
	
	@Override
	public VncVector withVariadicValues(final VncVal... replaceVals) {
		return VncVector.of(replaceVals).withMeta(getMeta());
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
		if (len == 0) return Constants.Nil;
		if (len == 1) return first;
		if (len == 2) return second;
		if (len == 3) return third;
		if (len == 4) return fourth;
		else throw new IllegalStateException("Length out of range");
	}
	
	@Override
	public VncVector rest() {
		if (len == 0) return this;
		if (len == 1) return new VncTinyVector(getMeta());
		if (len == 2) return new VncTinyVector(second, getMeta());
		if (len == 3) return new VncTinyVector(second, third, getMeta());
		if (len == 4) return new VncTinyVector(second, third, fourth, getMeta());
		else throw new IllegalStateException("Length out of range");
	}
	
	@Override
	public VncVector butlast() {
		if (len == 0) return this;
		if (len == 1) return new VncTinyVector(getMeta());
		if (len == 2) return new VncTinyVector(first, getMeta());
		if (len == 3) return new VncTinyVector(first, second, getMeta());
		if (len == 4) return new VncTinyVector(first, second, third, getMeta());
		else throw new IllegalStateException("Length out of range");
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return start == 0 && end >= len
				? this
				: VncVector.ofList(getList().subList(start, end), getMeta());
	}
	
	@Override
	public VncVector slice(final int start) {
		if (start == 0) {
			return this;
		}
		else if (start == 1) {
			return rest();
		}
		else {
			return VncVector.ofList(getList().subList(start, len), getMeta());
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
		if (len == 0) return new VncTinyVector(val, getMeta()); 
		if (len == 1) return new VncTinyVector(val, first, getMeta()); 
		if (len == 2) return new VncTinyVector(val, first, second, getMeta()); 
		if (len == 3) return new VncTinyVector(val, first, second, third, getMeta());
		if (len == 4) return VncVector.of(val, first, second, third, fourth).withMeta(getMeta());
		else throw new IllegalStateException("Vector length out of range");
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list) {
		final List<VncVal> vals = new ArrayList<>(list.getList());
		Collections.reverse(vals);
		vals.addAll(getList());

		return VncVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		switch (len) {
			case 0:	return new VncTinyVector(val, getMeta()); 
			case 1: return new VncTinyVector(first, val, getMeta()); 
			case 2:	return new VncTinyVector(first, second, val, getMeta()); 
			case 3:	return new VncTinyVector(first, second, third, val, getMeta()).withMeta(getMeta());
			case 4:	return VncVector.of(first, second, third, fourth, val).withMeta(getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		final List<VncVal> vals = getList();
		vals.addAll(list.getList());
		
		return VncVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		final List<VncVal> vals = getList();
		vals.set(idx, val);
		
		return VncVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		if (idx == 0) {
			return rest();
		}
		else if (idx == (len-1)) {
			return butlast();
		}
		else {
			final List<VncVal> vals = getList();
			vals.remove(idx);
			
			return VncVector.ofList(vals, getMeta());
		}
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.VECTOR;
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
				throw new IllegalStateException("Vector length out of range");
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
			final Integer sizeOther = ((VncTinyVector)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
					c = nth(ii).compareTo(((VncTinyVector)o).nth(ii));
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
		result = prime * result + len;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
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
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}

	public static VncTinyVector empty() {
		return EMPTY;
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/vector");
	public static final int MAX_ELEMENTS = 4;

    private static final long serialVersionUID = -1848883965231344442L;
    private static final VncTinyVector EMPTY = new VncTinyVector();

    private final int len;
	private final VncVal first;
	private final VncVal second;
	private final VncVal third;
	private final VncVal fourth;
}