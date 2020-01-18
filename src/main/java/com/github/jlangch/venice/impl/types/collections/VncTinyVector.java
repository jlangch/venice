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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.functions.FunctionsUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;


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
		super(meta == null ? Constants.Nil : meta);
		this.len = 0;
		this.first = null;
		this.second = null;
		this.third = null;
		this.fourth = null;
	}
	
	public VncTinyVector(final VncVal first, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 1;
		this.first = first;
		this.second = null;
		this.third = null;
		this.fourth = null;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 2;
		this.first = first;
		this.second = second;
		this.third = null;
		this.fourth = null;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal third, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 3;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = null;
	}

	public VncTinyVector(final VncVal first, final VncVal second, final VncVal third, final VncVal fourth, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.len = 4;
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
	
	public static VncVector of(final VncVal... mvs) {
		switch (mvs.length) {
			case 0:	return VncTinyVector.EMPTY;
			case 1:	return new VncTinyVector(mvs[0], null);
			case 2:	return new VncTinyVector(mvs[0], mvs[1], null);
			case 3:	return new VncTinyVector(mvs[0], mvs[1], mvs[2], null);
			case 4:	return new VncTinyVector(mvs[0], mvs[1], mvs[2], mvs[3], null);
			default: return VncVector.of(mvs);
		}
	}
	
	private static VncVector ofList(final List<? extends VncVal> list, final VncVal meta) {
		switch (list.size()) {
			case 0:	return new VncTinyVector(meta);
			case 1:	return new VncTinyVector(list.get(0), meta);
			case 2:	return new VncTinyVector(list.get(0), list.get(1), meta);
			case 3:	return new VncTinyVector(list.get(0), list.get(1), list.get(2), meta);
			case 4:	return new VncTinyVector(list.get(0), list.get(1), list.get(2), list.get(3), meta);
			default: return new VncVector(list, meta);
		}
	}

	@Override
	public VncVal apply(final VncList args) {
		FunctionsUtil.assertArity("map", args, 1);
		
		return nth(Coerce.toVncLong(args.first()).getValue().intValue());
	}
	
	@Override
	public VncVector emptyWithMeta() {
		return new VncTinyVector(getMeta());
	}
	
	@Override
	public VncVector withVariadicValues(final VncVal... replaceVals) {
		return VncTinyVector.of(replaceVals).withMeta(getMeta());
	}
	
	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals) {
		return VncTinyVector.ofList(replaceVals, getMeta());
	}

	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return VncTinyVector.ofList(replaceVals, meta);
	}

	@Override
	public VncVector withMeta(final VncVal meta) {
		switch (len) {
			case 0:	return new VncTinyVector(meta);
			case 1:	return new VncTinyVector(first, meta);
			case 2:	return new VncTinyVector(first, second, meta);
			case 3:	return new VncTinyVector(first, second, third, meta);
			case 4:	return new VncTinyVector(first, second, third, fourth, meta);
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		getList().forEach(v -> action.accept(v));
	}

	@Override
	public List<VncVal> getList() { 
		final ArrayList<VncVal> list = new ArrayList<>(len);
		switch (len) {
			case 0:	
				break;
			case 1:	
				list.add(first); 
				break;
			case 2:	
				list.add(first); 
				list.add(second);
				break;
			case 3:	
				list.add(first); 
				list.add(second); 
				list.add(third); 
				break;
			case 4:	
				list.add(first); 
				list.add(second); 
				list.add(third); 
				list.add(fourth); 
				break;
			default: 
				throw new IllegalStateException("Vector length out of range");
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
		return len >= 1 ? first : Constants.Nil;
	}

	@Override
	public VncVal second() {
		return len >= 2 ? second : Constants.Nil;
	}

	@Override
	public VncVal third() {
		return len >= 3 ? third : Constants.Nil;
	}

	@Override
	public VncVal fourth() {
		return len >= 4 ? fourth : Constants.Nil;
	}

	@Override
	public VncVal last() {
		switch (len) {
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
		switch (len) {
			case 0:	return this;
			case 1:	return new VncTinyVector(getMeta());
			case 2:	return new VncTinyVector(second, getMeta());
			case 3:	return new VncTinyVector(second, third, getMeta());
			case 4:	return new VncTinyVector(second, third, fourth, getMeta());
			default: throw new IllegalStateException("Length out of range");
		}
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return start == 0 && end >= len
				? this
				: VncTinyVector.ofList(getList().subList(start, end), getMeta());
	}
	
	@Override
	public VncVector slice(final int start) {
		return start == 0
				? this
				: VncTinyVector.ofList(getList().subList(start, len), getMeta());
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
			case 0:	return new VncTinyVector(val, getMeta()); 
			case 1: return new VncTinyVector(val, first, getMeta()); 
			case 2:	return new VncTinyVector(val, first, second, getMeta()); 
			case 3:	return VncVector.of(val, first, second, third).withMeta(getMeta());
			case 4:	return VncVector.of(val, first, second, third, fourth).withMeta(getMeta());
			default: throw new IllegalStateException("Vector length out of range");
		}
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list) {
		final List<VncVal> vals = new ArrayList<>(list.getList());
		Collections.reverse(vals);
		vals.addAll(getList());

		return VncTinyVector.ofList(vals, getMeta());
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
		
		return VncTinyVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		final List<VncVal> vals = getList();
		vals.set(idx, val);
		
		return VncTinyVector.ofList(vals, getMeta());
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		final List<VncVal> vals = getList();
		vals.remove(idx);
		
		return VncTinyVector.ofList(vals, getMeta());
	}
	
	@Override 
	public int typeRank() {
		return 201;
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
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
		result = prime * result + len;
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
		VncTinyVector other = (VncTinyVector) obj;
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
		if (len != other.len)
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
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}


    public static final VncTinyVector EMPTY = new VncTinyVector();

    private static final long serialVersionUID = -1848883965231344442L;

    private final int len;
	private final VncVal first;
	private final VncVal second;
	private final VncVal third;
	private final VncVal fourth;
}