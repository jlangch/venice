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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.ErrorMessage;

import io.vavr.collection.Stream;


public class VncLazySeq extends VncSequence {

	public VncLazySeq(final VncFunction fn, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = Stream.continually(() -> fn.apply(VncList.of()));
	}

	public VncLazySeq(final VncVal seed, final VncFunction fn, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = Stream.iterate(seed, (v) -> fn.apply(VncList.of(v)));
	}

	public VncLazySeq(final io.vavr.collection.Stream<VncVal> stream, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.value =stream;
	}
	
	@Override
	public VncSequence emptyWithMeta() {
		return new VncTinyList(getMeta());
	}
	
	@Override
	public VncLazySeq withVariadicValues(final VncVal... replaceVals) {
		throw new VncException("Not supported");
	}
	
	@Override
	public VncLazySeq withValues(final List<? extends VncVal> replaceVals) {
		throw new VncException("Not supported");
	}

	@Override
	public VncLazySeq withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		throw new VncException("Not supported");
	}

	@Override
	public VncLazySeq withMeta(final VncVal meta) {
		return new VncLazySeq(value, meta);
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
	public VncList filter(final Predicate<? super VncVal> predicate) {
		return new VncList(value.filter(predicate), getMeta());
	}

	@Override
	public VncList map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return new VncList(value.map(mapper), getMeta());
	}

	@Override
	public List<VncVal> getList() { 
		return value.asJava(); // return an immutable view on top of Vector<VncVal>
	}

    @Override
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : value.iterator();
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
	public VncLazySeq rest() {
		return new VncLazySeq(value.drop(1), getMeta()) ;
	}
	
	@Override
	public VncLazySeq butlast() {
		throw new VncException("Not supported");
	}

	@Override
	public VncLazySeq slice(final int start, final int end) {
		return new VncLazySeq(value.subSequence(start, end), getMeta());
	}
	
	@Override
	public VncLazySeq slice(final int start) {
		return new VncLazySeq(value.subSequence(start), getMeta());
	}
	
	@Override
	public VncList toVncList() {
		return new VncList(value, getMeta());
	}

	@Override
	public VncVector toVncVector() {
		return new VncVector(value, getMeta());
	}

	
	@Override
	public VncLazySeq addAtStart(final VncVal val) {
		return new VncLazySeq(value.prepend(val), getMeta());
	}
	
	@Override
	public VncLazySeq addAllAtStart(final VncSequence list) {
		final List<VncVal> items = list.getList();
		Collections.reverse(items);
		return new VncLazySeq(value.prependAll(items), getMeta());
	}
	
	@Override
	public VncLazySeq addAtEnd(final VncVal val) {
		return new VncLazySeq(value.append(val), getMeta());
	}
	
	@Override
	public VncLazySeq addAllAtEnd(final VncSequence list) {
		return new VncLazySeq(value.appendAll(list.getList()), getMeta());
	}
	
	@Override
	public VncLazySeq setAt(final int idx, final VncVal val) {
		return new VncLazySeq(value.update(idx, val), getMeta());
	}
	
	@Override
	public VncLazySeq removeAt(final int idx) {
		return new VncLazySeq(value.removeAt(idx), getMeta());
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.LAZYSEQ;
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
			final Integer sizeOther = ((VncLazySeq)o).size();
			int c = sizeThis.compareTo(sizeOther);
			if (c != 0) {
				return c;
			}
			else {
				for(int ii=0; ii<sizeThis; ii++) {
					c = nth(ii).compareTo(((VncLazySeq)o).nth(ii));
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
		VncLazySeq other = (VncLazySeq) obj;
		return value.equals(other.value);
	}

	@Override 
	public String toString() {
		return value.hasDefiniteSize()
				? "(" + Printer.join(value.toJavaList(), " ", true) + ")"
				: "(...)";
	}
	
	public String toString(final boolean print_readably) {
		return value.hasDefiniteSize()
				? "(" + Printer.join(value.toJavaList(), " ", print_readably) + ")"
				: "(...)";
	}

	
	public VncList realize() {
		return new VncList(value.toList(), getMeta());
	}
	
	public VncList realize(final int n) {
		return new VncList(value.slice(0, n).toList(), getMeta());
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/lazyseq");

    private static final long serialVersionUID = -1848883965231344442L;
 
	private final io.vavr.collection.Stream<VncVal> value;
}