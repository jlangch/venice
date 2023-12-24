/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.EmptyIterator;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.vavr.Streams;

import io.vavr.collection.Stream;
import io.vavr.control.Option;


public class VncLazySeq extends VncSequence {

    public VncLazySeq(final VncVal meta) {
        this(Stream.empty(), meta);
    }

    public VncLazySeq(final Stream<VncVal> stream, final VncVal meta) {
        super(meta == null ? Nil : meta);
        this.value = stream;
    }


    public static VncLazySeq continually(final VncFunction fn, final VncVal meta) {
        return new VncLazySeq(Stream.continually(() -> fn.apply(VncList.empty())), meta);
    }

    public static VncLazySeq iterate(final VncFunction fn, final VncVal meta) {
        return new VncLazySeq(Streams.iterate(() -> toOptional(fn.apply(VncList.empty()))), meta);
    }

    public static VncLazySeq iterate(final VncVal seed, final VncFunction fn, final VncVal meta) {
        return new VncLazySeq(Streams.iterate(seed, v -> toOptional(fn.apply(VncList.of(v)))), meta);
    }

    public static VncLazySeq cons(final VncVal head, final VncFunction tailFn, final VncVal meta) {
        return new VncLazySeq(Stream.cons(
                                head,
                                () -> {
                                    final VncVal v = tailFn.apply(VncList.empty());
                                    return v == Nil ? Stream.empty()
                                                    : ((VncLazySeq)v).lazyStream();
                                }),
                              meta);
    }

    public static VncLazySeq cons(final VncVal head, final VncLazySeq tail, final VncVal meta) {
        return new VncLazySeq(Stream.cons(head, () -> tail.value), meta);
    }

    public static VncLazySeq ofAll(final VncSequence list, final VncVal meta) {
        return new VncLazySeq(Stream.ofAll(list.stream()), meta);
    }

    public static VncLazySeq ofAll(final Iterable<VncVal> items, final VncVal meta) {
        return new VncLazySeq(Stream.ofAll(items), meta);
    }

    public static VncLazySeq fill(final int n, final VncFunction fn, final VncVal meta) {
        return new VncLazySeq(Stream.fill(n, () -> fn.apply(VncList.empty())), meta);
    }

    public Stream<VncVal> lazyStream() {
        return value;
    }


    public VncLazySeq scanLeft(final VncVal zero, final VncFunction fn, final VncVal meta) {
        return new VncLazySeq(value.scanLeft(zero, (u,v) -> fn.apply(VncList.of(u,v))), meta);
    }


    @Override
    public VncLazySeq emptyWithMeta() {
        return new VncLazySeq(Stream.empty(), getMeta());
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
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncSequence.TYPE),
                            new VncKeyword(VncCollection.TYPE),
                            new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public Iterator<VncVal> iterator() {
        return isEmpty() ? EmptyIterator.empty() : value.iterator();
    }

    @Override
    public java.util.stream.Stream<VncVal> stream() {
        return value.toJavaStream();
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
    public List<VncVal> getJavaList() {
        return value.asJava(); // return an immutable view on top of Stream<VncVal>
    }

    @Override
    public int size() {
        throw new VncException("Getting the size of lazy sequences is not supported");
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public VncVal nth(final int idx) {
        return value.get(idx);
    }

    @Override
    public VncVal nthOrDefault(final int idx, final VncVal defaultVal) {
        try {
            return value.get(idx);
        }
        catch(IndexOutOfBoundsException ex) {
            return defaultVal;
        }
    }

    @Override
    public VncVal first() {
        return isEmpty() ? Nil : value.head();
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
        throw new VncException("Getting the last element of a lazy sequence is not supported");
    }

    @Override
    public VncLazySeq rest() {
        return new VncLazySeq(value.drop(1), getMeta()) ;
    }

    @Override
    public VncLazySeq butlast() {
        throw new VncException("Getting all but the last element of a lazy sequence is not supported");
    }

    @Override
    public VncLazySeq drop(final int n) {
        return new VncLazySeq(value.drop(n), getMeta());
    }

    @Override
    public VncLazySeq dropWhile(final Predicate<? super VncVal> predicate) {
        return new VncLazySeq(value.dropWhile(predicate), getMeta());
    }

    @Override
    public VncLazySeq dropRight(final int n) {
        return new VncLazySeq(value.dropRight(n), getMeta());
    }

    @Override
    public VncLazySeq take(final int n) {
        return new VncLazySeq(value.take(n), getMeta());
    }

    @Override
    public VncLazySeq takeWhile(final Predicate<? super VncVal> predicate) {
        return new VncLazySeq(value.takeWhile(predicate), getMeta());
    }

    @Override
    public VncLazySeq takeRight(final int n) {
        return new VncLazySeq(value.takeRight(n), getMeta());
    }

    @Override
    public VncLazySeq reverse() {
        return new VncLazySeq(value.reverse(), getMeta());
    }

    @Override
    public VncLazySeq shuffle() {
        return new VncLazySeq(value.shuffle(), getMeta());
    }

    @Override
    public VncLazySeq distinct() {
        return new VncLazySeq(value.distinct(), getMeta());
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
    public VncLazySeq addAllAtStart(final VncSequence list, final boolean reverseAdd) {
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
                ? "(" + Printer.join(this, " ", true) + ")"
                : "(...)";
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return value.hasDefiniteSize()
                ? "(" + Printer.join(this, " ", print_machine_readably) + ")"
                : "(...)";
    }


    public VncList realize() {
        return new VncList(value.toList(), getMeta());
    }

    public VncList realize(final int n) {
        return new VncList(value.slice(0, n).toList(), getMeta());
    }

    public static VncLazySeq empty() {
        return new VncLazySeq(Stream.empty(), Nil);
    }


    private static Option<VncVal> toOptional(final VncVal val) {
        return val == Nil ? Option.none() : Option.of(val);
    }




    public static final String TYPE = ":core/lazyseq";

    private static final long serialVersionUID = -1848883965231344442L;

    private final Stream<VncVal> value;
}
