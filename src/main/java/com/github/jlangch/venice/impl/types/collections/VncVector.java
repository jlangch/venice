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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.functions.FunctionsUtil;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncVector extends VncSequence implements IVncFunction {

	protected VncVector() {
		this((io.vavr.collection.Seq<VncVal>)null, null);
	}

	protected VncVector(final VncVal meta) {
		this((io.vavr.collection.Seq<VncVal>)null, meta);
	}

	protected VncVector(final Collection<? extends VncVal> vals, final VncVal meta) {
		this(vals == null ? null : io.vavr.collection.Vector.ofAll(vals), meta);
	}

	public VncVector(final io.vavr.collection.Seq<VncVal> vals, final VncVal meta) {
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
	
	
	public static VncVector of(final VncVal... mvs) {
		return new VncVector(io.vavr.collection.Vector.of(mvs), Constants.Nil);
	}

	public static VncVector ofColl(final Collection<? extends VncVal> vals) {
		return new VncVector(vals, Constants.Nil);
	}

	public static VncVector ofColl(final Collection<? extends VncVal> vals, final VncVal meta) {
		return new VncVector(vals, meta);
	}
	

	@Override
	public VncVal apply(final VncList args) {
		FunctionsUtil.assertArity("nth", args, 1);
		
		return nth(Coerce.toVncLong(args.first()).getValue().intValue());
	}
	
	@Override
	public VncVector emptyWithMeta() {
		return new VncVector(getMeta());
	}
	
	@Override
	public VncVector withVariadicValues(final VncVal... replaceVals) {
		return VncVector.of(replaceVals).withMeta(getMeta());
	}
	
	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals) {
		return new VncVector(replaceVals, getMeta());
	}

	@Override
	public VncVector withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return new VncVector(replaceVals, meta);
	}

	@Override
	public VncVector withMeta(final VncVal meta) {
		return new VncVector(value, meta);
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
						"nth: index %d out of range for a vector of size %d. %s", 
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
	public VncVector rest() {
		return isEmpty() ? new VncVector(getMeta()) : new VncVector(value.tail(), getMeta());
	}
	
	@Override
	public VncVector butlast() {
		return new VncVector(value.dropRight(1), getMeta());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncVector(value.subSequence(start, end), getMeta());
	}
	
	@Override
	public VncVector slice(final int start) {
		return new VncVector(value.subSequence(start), getMeta());
	}

	@Override
	public VncList toVncList() {
		return new VncList(value, getMeta());
	}
	
	@Override
	public VncVector toVncVector() {
		return this;
	}

	
	@Override
	public VncVector addAtStart(final VncVal val) {
		return new VncVector(value.prepend(val), getMeta());
	}
	
	@Override
	public VncVector addAllAtStart(final VncSequence list) {
		return new VncVector(value.prependAll(list.getList()), getMeta());
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		return new VncVector(value.append(val), getMeta());
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		return new VncVector(value.appendAll(list.getList()), getMeta());
	}
	
	@Override
	public VncVector setAt(final int idx, final VncVal val) {
		return new VncVector(value.update(idx, val), getMeta());
	}
	
	@Override
	public VncVector removeAt(final int idx) {
		return new VncVector(value.removeAt(idx), getMeta());
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.VECTOR;
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
		VncVector other = (VncVector) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(value.toJavaList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(value.toJavaList(), " ", print_readably) + "]";
	}

	public static VncVector empty() {
		return VncTinyVector.empty();
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/vector");

    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.Vector<VncVal> value;
}