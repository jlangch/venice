/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vavr.collection.Stream;
import io.vavr.control.Option;

public class Streams {

    /**
     * Generates a (theoretically) infinitely long Stream using a repeatedly invoked supplier
     * that provides a {@code Some} for each next value and a {@code None} for the end.
     * The {@code Supplier} will be invoked only that many times until it returns {@code None},
     * and repeated iteration over the stream will produce the same values in the same order,
     * without any further invocations to the {@code Supplier}.
     *
     * @param supplier A Supplier of iterator values
     * @param <T> value type
     * @return A new Stream
     */
    public static <T> Stream<T> iterate(
            final Supplier<? extends Option<? extends T>> supplier
    ) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Stream.ofAll(Iterators.iterate(supplier));
    }

    /**
     * Generates a (theoretically) infinitely long Stream using a function to calculate the next value
     * based on the previous.
     *
     * @param seed The first value in the Stream
     * @param fn   A function to calculate the next value based on the previous
     * @param <T>  value type
     * @return A new Stream
     */
    public static <T> Stream<T> iterate(
            final T seed,
            final Function<? super T, ? extends Option<? extends T>> fn
    ) {
        Objects.requireNonNull(fn, "function is null");
        return Stream.ofAll(Iterators.iterate(seed, fn));
    }

}
