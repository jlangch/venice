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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;



public abstract class VncSequence extends VncCollection implements Iterable<VncVal> {

    public VncSequence(VncVal meta) {
        super(meta);
    }

    public VncSequence(
            final VncWrappingTypeDef wrappingTypeDef,
            final VncVal meta
    ) {
        super(wrappingTypeDef, meta);
    }


    @Override
    public abstract VncSequence emptyWithMeta();

    public abstract VncSequence withVariadicValues(VncVal... replaceVals);

    public abstract VncSequence withValues(List<? extends VncVal> replaceVals);

    public abstract VncSequence withValues(List<? extends VncVal> replaceVals, VncVal meta);

    @Override
    public abstract VncSequence withMeta(VncVal meta);

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncCollection.TYPE),
                            new VncKeyword(VncVal.TYPE)));
    }

    public abstract List<VncVal> getJavaList();

    public abstract VncVal nth(int idx);

    public abstract VncVal nthOrDefault(int idx, VncVal defaultVal);

    public VncVal first() {
        return nthOrDefault(0, Constants.Nil);
    }

    public VncVal second() {
        return nthOrDefault(1, Constants.Nil);
    }

    public VncVal third() {
        return nthOrDefault(2, Constants.Nil);
    }

    public VncVal fourth() {
        return nthOrDefault(3, Constants.Nil);
    }

    public abstract VncVal last();

    public abstract VncSequence rest();

    public abstract VncSequence butlast();

    public abstract VncSequence drop(int n);

    public abstract VncSequence dropWhile(Predicate<? super VncVal> predicate);

    public abstract VncSequence dropRight(int n);

    public abstract VncSequence take(int n);

    public abstract VncSequence takeWhile(Predicate<? super VncVal> predicate);

    public abstract VncSequence takeRight(int n);

    public abstract VncSequence slice(int start, int end);

    public abstract VncSequence slice(int start);

    public abstract VncSequence setAt(int idx, VncVal val);

    public abstract VncSequence addAtStart(VncVal val) ;

    public abstract VncSequence addAllAtStart(VncSequence seq, boolean reverseAdd);

    public abstract VncSequence addAtEnd(VncVal val);

    public abstract VncSequence addAllAtEnd(VncSequence seq);

    public abstract VncSequence removeAt(int idx);

    public abstract VncSequence reverse();

    public abstract VncSequence shuffle();

    public abstract VncSequence distinct();

    @Override
    public abstract Iterator<VncVal> iterator();

    public abstract Stream<VncVal> stream();

    @Override
    public abstract void forEach(Consumer<? super VncVal> action);

    public abstract VncSequence filter(Predicate<? super VncVal> predicate);

    public abstract VncSequence map(Function<? super VncVal, ? extends VncVal> mapper);


    public static VncSequence coerceToSequence(final VncVal val) {
        if (Types.isVncMap(val)) {
            return VncList.ofList(((VncMap)val).entries());
        }
        else if (Types.isVncSet(val)) {
            return ((VncSet)val).toVncList();
        }
        else if (Types.isVncString(val)) {
            return ((VncString)val).toVncList();
        }
        else {
            return Coerce.toVncSequence(val);
        }
    }


    public static final String TYPE = ":core/sequence";

    private static final long serialVersionUID = -1848883965231344442L;
}
