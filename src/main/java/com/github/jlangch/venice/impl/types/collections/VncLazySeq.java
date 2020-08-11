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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.EmptyIterator;

import io.vavr.collection.Stream;


public class VncLazySeq extends VncSequence {

	public VncLazySeq(final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = io.vavr.collection.Stream.empty();
	}

	public VncLazySeq(final VncFunction fn, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = Stream.continually(() -> fn.apply(VncList.of()));
	}

	public VncLazySeq(final VncVal seed, final VncFunction fn, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = Stream.iterate(seed, v -> fn.apply(VncList.of(v)));
	}

	public VncLazySeq(final VncVal head, final VncLazySeq tail, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);

		this.value = Stream.cons(head, () -> tail.value);
	}

	public VncLazySeq(final io.vavr.collection.Stream<VncVal> stream, final VncVal meta) {
		super(meta == null ? Constants.Nil : meta);
		this.value = stream;
	}

	
	public static VncLazySeq fill(final int n, final VncFunction fn, final VncVal meta) {
		return new VncLazySeq(Stream.fill(n, () -> fn.apply(VncList.of())), meta);
	}

	// val fibs: Stream[Int] = 0 #:: fibs.scanLeft(1)(_ + _)
	public VncLazySeq scanLeft(final VncVal zero, final VncFunction fn, final VncVal meta) {
		return new VncLazySeq(value.scanLeft(zero, (u,v) -> fn.apply(VncList.of(u,v))), meta);
	}

	
	@Override
	public VncLazySeq emptyWithMeta() {
		return new VncLazySeq(io.vavr.collection.Stream.empty(), getMeta());
	}
	
	@Override
	public VncLazySeq withVariadicValues(final VncVal... replaceVals) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq withValues(final List<? extends VncVal> replaceVals) {
		throw new VncException("Not supported for lazy sequences");
	}

	@Override
	public VncLazySeq withValues(final List<? extends VncVal> replaceVals, final VncVal meta) {
		throw new VncException("Not supported for lazy sequences");
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
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : value.iterator();
    }

	@Override
	public void forEach(Consumer<? super VncVal> action) {
		value.forEach(v -> action.accept(v));
	}
	
	@Override
	public VncLazySeq filter(final Predicate<? super VncVal> predicate) {
		return new VncLazySeq(value.filter(predicate), getMeta());
	}

	@Override
	public VncLazySeq map(final Function<? super VncVal, ? extends VncVal> mapper) {
		return new VncLazySeq(value.map(mapper), getMeta());
	}

	@Override
	public List<VncVal> getList() { 
		return value.asJava(); // return an immutable view on top of Vector<VncVal>
	}

	@Override
	public int size() {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public VncVal nth(final int idx) {
		throw new VncException("Not supported for lazy sequences");
	}

	@Override
	public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
		throw new VncException("Not supported for lazy sequences");
	}

	@Override
	public VncVal first() {
		return isEmpty() ? Constants.Nil : value.head();
	}

	@Override
	public VncVal second() {
		return value.drop(1).head();
	}

	@Override
	public VncVal third() {
		return value.drop(2).head();
	}

	@Override
	public VncVal fourth() {
		return value.drop(3).head();
	}

	@Override
	public VncVal last() {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq rest() {
		return new VncLazySeq(value.drop(1), getMeta()) ;
	}
	
	@Override
	public VncLazySeq butlast() {
		throw new VncException("Not supported for lazy sequences");
	}

	@Override
	public VncLazySeq drop(final int n) {
		return value.isEmpty() ? this : new VncLazySeq(value.drop(n), getMeta());
	}
	
	@Override
	public VncLazySeq dropWhile(final Predicate<? super VncVal> predicate) {
		return new VncLazySeq(value.dropWhile(predicate), getMeta());
	}
	
	@Override
	public VncLazySeq take(final int n) {
		return value.isEmpty() ? this : new VncLazySeq(value.take(n), getMeta());
	}
	
	@Override
	public VncLazySeq takeWhile(final Predicate<? super VncVal> predicate) {
		return new VncLazySeq(value.takeWhile(predicate), getMeta());
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
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq addAllAtStart(final VncSequence list) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq addAtEnd(final VncVal val) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq addAllAtEnd(final VncSequence list) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq setAt(final int idx, final VncVal val) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public VncLazySeq removeAt(final int idx) {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.LAZYSEQ;
	}

	@Override 
	public boolean isVncList() {
		return false;
	}

	@Override
	public Object convertToJavaObject() {
		throw new VncException("Not supported for lazy sequences");
	}
	
	@Override
	public int compareTo(final VncVal o) {
		throw new VncException("Not supported for lazy sequences");
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
	
	public static VncLazySeq empty() {
		return new VncLazySeq(io.vavr.collection.Stream.empty(), Constants.Nil);
	}

	

	public static final VncKeyword TYPE = new VncKeyword(":core/lazyseq");

    private static final long serialVersionUID = -1848883965231344442L;
 
	private final io.vavr.collection.Stream<VncVal> value;
}