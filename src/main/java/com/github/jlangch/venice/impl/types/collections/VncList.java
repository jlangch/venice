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
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.ErrorMessage;


public class VncList extends VncSequence {

	protected VncList(final VncVal meta) {
		this((io.vavr.collection.Seq<VncVal>)null, meta);
	}

	protected VncList(final java.util.Collection<? extends VncVal> vals, final VncVal meta) {
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
		return mvs.length <= VncTinyList.MAX_ELEMENTS
				? VncTinyList.of(mvs)
				: new VncList(io.vavr.collection.Vector.of(mvs), null);
	}
	
	public static VncList ofList(final List<? extends VncVal> list) {
		return list.size() <= VncTinyList.MAX_ELEMENTS
				? VncTinyList.ofArr(list.toArray(new VncVal[0]), null)
				: new VncList(list, null);
	}

	public static VncList ofList(final List<? extends VncVal> list, final VncVal meta) {
		return list.size() <= VncTinyList.MAX_ELEMENTS
				? VncTinyList.ofArr(list.toArray(new VncVal[0]), meta)
				: new VncList(list, meta);
	}

	public static VncList ofColl(final Collection<? extends VncVal> vals) {
		return new VncList(vals, Constants.Nil);
	}

	public static VncList ofColl(final Collection<? extends VncVal> vals, final VncVal meta) {
		return new VncList(vals, meta);
	}

	public static VncList ofAll(final Iterable<? extends VncVal> iter, final VncVal meta) {
		return new VncList(io.vavr.collection.Vector.ofAll(iter), meta);
	}

	public static VncList ofAll(final Stream<? extends VncVal> stream, final VncVal meta) {
		return new VncList(io.vavr.collection.Vector.ofAll(stream), meta);
	}

	
	@Override
	public VncList emptyWithMeta() {
		return new VncTinyList(getMeta());
	}
	
	@Override
	public VncList withVariadicValues(final VncVal... replaceVals) {
		return replaceVals.length <= VncTinyList.MAX_ELEMENTS
				? VncTinyList.ofArr(replaceVals, getMeta())
				: new VncList(io.vavr.collection.Vector.of(replaceVals), getMeta());
	}
	
	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals) {
		return VncList.ofList(replaceVals, getMeta());
	}

	@Override
	public VncList withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		return VncList.ofList(replaceVals, meta);
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
	public VncList filter(final Predicate<? super VncVal> predicate) {
		return new VncList(value.filter(predicate), getMeta());
	}

	@Override
	public VncList map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return new VncList(value.map(mapper), getMeta());
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
		if (value.isEmpty()) {
			return this;
		}
		else {
			final io.vavr.collection.Vector<VncVal> rest = value.tail();
			return rest.size() <= VncTinyList.MAX_ELEMENTS
					? VncTinyList.ofList(rest.asJava(), getMeta())
					: new VncList(rest, getMeta());
		}
	}
	
	@Override
	public VncList butlast() {
		if (value.isEmpty()) {
			return this;
		}
		else {
			final io.vavr.collection.Vector<VncVal> butlast = value.dropRight(1);
			return butlast.size() < VncTinyList.MAX_ELEMENTS
					? VncTinyList.ofList(butlast.asJava(), getMeta())
					: new VncList(butlast, getMeta());
		}
	}

	@Override
	public VncList drop(final int n) {
		if (n <= 0) {
			return this;
		}
		else if (n >= value.size()) {
			return VncTinyList.EMPTY;
		}
		else {
			return value.isEmpty() ? this : new VncList(value.drop(n), getMeta());
		}
	}
	
	@Override
	public VncList dropWhile(final Predicate<? super VncVal> predicate) {
		return new VncList(value.dropWhile(predicate), getMeta());
	}

	@Override
	public VncList take(final int n) {
		return value.isEmpty() ? this : new VncList(value.take(n), getMeta());
	}
	
	@Override
	public VncList takeWhile(final Predicate<? super VncVal> predicate) {
		return new VncList(value.takeWhile(predicate), getMeta());
	}

	@Override
	public VncList reverse() {
		return new VncList(value.reverse(), getMeta());
	}
	
	@Override 
	public VncList shuffle() {
		return new VncList(value.shuffle(), getMeta());
	}

	@Override
	public VncList slice(final int start, final int end) {
		return new VncList(value.subSequence(start, Math.min(end, value.size())), getMeta());
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
	public VncList addAllAtStart(final VncSequence list, final boolean reverseAdd) {
		final VncSequence seq = reverseAdd ? list.reverse() : list;
		return new VncList(value.prependAll(seq), getMeta());
	}
	
	@Override
	public VncList addAtEnd(final VncVal val) {
		return new VncList(value.append(val), getMeta());
	}
	
	@Override
	public VncList addAllAtEnd(final VncSequence list) {
		return new VncList(value.appendAll(list), getMeta());
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
		return stream()
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
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		VncList other = (VncList) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(this, " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(this, " ", print_readably) + ")";
	}

	public static VncList empty() {
		return VncTinyList.EMPTY;
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/list");

    private static final long serialVersionUID = -1848883965231344442L;
 
	private final io.vavr.collection.Vector<VncVal> value;
}