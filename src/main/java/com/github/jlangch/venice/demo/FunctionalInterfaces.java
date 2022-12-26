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
package com.github.jlangch.venice.demo;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


/**
 * Util class to show the usage of the Venice's functional interface proxies.
 */
public class FunctionalInterfaces {

	public static void testRunnable(final Runnable r) {
		r.run();
	}

	public static Long testCallable(final Callable<Long> c) throws Exception {
		return c.call();
	}

	public static boolean testPredicate(final Predicate<Long> p, final Long t) {
		return p.test(t);
	}

	public static Long testFunction(final Function<Long,Long> f, final Long t) {
		return f.apply(t);
	}

	public static void testConsumer(final Consumer<Long> f, final Long t) {
		f.accept(t);
	}

	public static Long testSupplier(final Supplier<Long> f) {
		return f.get();
	}

	public static boolean testBiPredicate(final BiPredicate<Long,Long> f, final Long t, final Long u) {
		return f.test(t,u);
	}

	public static Long testBiFunction(final BiFunction<Long,Long,Long> f, final Long t, final Long u) {
		return f.apply(t,u);
	}

	public static void testBiConsumer(final BiConsumer<Long,Long> f, final Long t, final Long u) {
		f.accept(t,u);
	}

	public static Long testUnaryOperator(final UnaryOperator<Long> f, final Long t) {
		return f.apply(t);
	}

	public static Long testBinaryOperator(final BinaryOperator<Long> f, final Long t, final Long u) {
		return f.apply(t,u);
	}

}
