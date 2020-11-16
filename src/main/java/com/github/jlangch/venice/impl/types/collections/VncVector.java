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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncVector extends VncSequence implements IVncFunction {

	protected VncVector(final VncVal meta) {
		this((io.vavr.collection.Seq<VncVal>)null, meta);
	}

	protected VncVector(final java.util.Collection<? extends VncVal> vals, final VncVal meta) {
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
		return mvs.length <= VncTinyVector.MAX_ELEMENTS
				? VncTinyVector.of(mvs)
				: new VncVector(io.vavr.collection.Vector.of(mvs), null);
	}

	public static VncVector ofList(final List<? extends VncVal> list) {
		return list.size() <= VncTinyVector.MAX_ELEMENTS
				? VncTinyVector.of(list.toArray(new VncVal[0]))
				: new VncVector(list, null);
	}

	public static VncVector ofList(final List<? extends VncVal> list, final VncVal meta) {
		return list.size() <= VncTinyVector.MAX_ELEMENTS
				? VncTinyVector.of(list.toArray(new VncVal[0])).withMeta(meta)
				: new VncVector(list, meta);
	}

	public static VncVector ofColl(final Collection<? extends VncVal> vals) {
		return new VncVector(vals, Constants.Nil);
	}

	public static VncVector ofColl(final Collection<? extends VncVal> vals, final VncVal meta) {
		return new VncVector(vals, meta);
	}

	public static VncVector ofAll(final Iterable<? extends VncVal> iter, final VncVal meta) {
		return new VncVector(io.vavr.collection.Vector.ofAll(iter), meta);
	}

	public static VncVector ofAll(final Stream<? extends VncVal> stream, final VncVal meta) {
		return new VncVector(io.vavr.collection.Vector.ofAll(stream), meta);
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
		return replaceVals.length <= VncTinyList.MAX_ELEMENTS
				? VncTinyVector.of(replaceVals).withMeta(getMeta())
				: new VncVector(io.vavr.collection.Vector.of(replaceVals), getMeta());
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
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : value.iterator();
    }

    @Override
	public Stream<VncVal> stream() {
		return value.toJavaStream();
	}

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}
	
	@Override
	public VncVector filter(final Predicate<? super VncVal> predicate) {
		return new VncVector(value.filter(predicate), getMeta());
	}

	@Override
	public VncVector map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return new VncVector(value.map(mapper), getMeta());
	}

	@Override
	public List<VncVal> getJavaList() { 
		return value.asJava(); // return an immutable view on top of Vector<VncVal>
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
		if (value.isEmpty()) {
			return this;
		}
		else {
			final io.vavr.collection.Vector<VncVal> rest = value.tail();
			return rest.size() <= VncTinyVector.MAX_ELEMENTS
					? VncTinyVector.ofList(rest.asJava(), getMeta())
					: new VncVector(rest, getMeta());
		}
	}
	
	@Override
	public VncVector butlast() {
		if (value.isEmpty()) {
			return this;
		}
		else {
			final io.vavr.collection.Vector<VncVal> butlast = value.dropRight(1);
			return butlast.size() < VncTinyVector.MAX_ELEMENTS
					? VncTinyVector.ofList(butlast.asJava(), getMeta())
					: new VncVector(butlast, getMeta());
		}
	}

	@Override
	public VncVector drop(final int n) {
		if (n <= 0) {
			return this;
		}
		else if (n >= value.size()) {
			return VncTinyVector.EMPTY;
		}
		else {
			return value.isEmpty() ? this : new VncVector(value.drop(n), getMeta());
		}
	}
	
	@Override
	public VncVector dropWhile(final Predicate<? super VncVal> predicate) {
		return new VncVector(value.dropWhile(predicate), getMeta());
	}
	
	@Override
	public VncVector take(final int n) {
		return value.isEmpty() ? this : new VncVector(value.take(n), getMeta());
	}
	
	@Override
	public VncVector takeWhile(final Predicate<? super VncVal> predicate) {
		return new VncVector(value.takeWhile(predicate), getMeta());
	}

	@Override
	public VncVector reverse() {
		return new VncVector(value.reverse(), getMeta());
	}
	
	@Override 
	public VncVector shuffle() {
		return new VncVector(value.shuffle(), getMeta());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncVector(value.subSequence(start, Math.min(end, value.size())), getMeta());
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
	public VncVector addAllAtStart(final VncSequence list, final boolean reverseAdd) {
		final VncSequence seq = reverseAdd ? list.reverse() : list;
		return new VncVector(value.prependAll(seq), getMeta());
	}
	
	@Override
	public VncVector addAtEnd(final VncVal val) {
		return new VncVector(value.append(val), getMeta());
	}
	
	@Override
	public VncVector addAllAtEnd(final VncSequence list) {
		return new VncVector(value.appendAll(list), getMeta());
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
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		VncVector other = (VncVector)obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(this, " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(this, " ", print_readably) + "]";
	}

	public static VncVector empty() {
		return VncTinyVector.EMPTY;
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/vector");

    private static final long serialVersionUID = -1848883965231344442L;

	private final io.vavr.collection.Vector<VncVal> value;
}