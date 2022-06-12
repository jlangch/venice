/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.vavr;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vavr.collection.Iterator;
import io.vavr.control.Option;


public class Iterators {

    /**
     * Creates an iterator that repeatedly invokes the supplier
     * while it's a {@code Some} and end on the first {@code None}
     *
     * @param supplier A Supplier of iterator values
     * @param <T> value type
     * @return A new {@code Iterator}
     * @throws NullPointerException if supplier produces null value
     */
    public static <T> io.vavr.collection.Iterator<T> iterate(
            final Supplier<? extends Option<? extends T>> supplier
    ) {
        Objects.requireNonNull(supplier, "supplier is null");

        return new io.vavr.collection.Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (nextOption == null) {
                    nextOption = supplier.get();
                }
                return nextOption.isDefined();
            }

            @Override
            public String toString() {
                return stringPrefix() + "(" + (isEmpty() ? "" : "?") + ")";
            }

            @Override
            public final T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("next() on empty iterator");
                }

                final T next = nextOption.get();
                nextOption = null;
                return next;
            }

            private Option<? extends T> nextOption = null;
        };
    }

    /**
     * Generates an infinite iterator using a function to calculate the next value
     * based on the previous. The functions is invoked as long as it returns a {@code Some}
     * and stops on the first {@code None}
     *
     * @param seed The first value in the iterator
     * @param fn   A function to calculate the next value based on the previous
     * @param <T>  value type
     * @return A new {@code Iterator}
     */
    public static <T> Iterator<T> iterate(
            final T seed,
            final Function<? super T, ? extends Option<? extends T>> fn
    ) {
        Objects.requireNonNull(fn, "function is null");

        return new io.vavr.collection.Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (nextOption == null) {
                    nextOption = fn.apply(last);
                }

                return nextOption.isDefined();
            }

            @Override
            public String toString() {
                return stringPrefix() + "(" + (isEmpty() ? "" : "?") + ")";
            }

            @Override
            public final T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("next() on empty iterator");
                }

                final T next = nextOption.get();
                nextOption = null;
                last = next;
                return next;
            }

            private T last = null;
            private Option<? extends T> nextOption = Option.of(seed);
        };
    }

}
