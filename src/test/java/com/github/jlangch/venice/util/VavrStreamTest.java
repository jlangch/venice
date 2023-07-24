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
package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.vavr.Iterators;

import io.vavr.collection.Stream;
import io.vavr.control.Option;


public class VavrStreamTest {

    @Test
    public void testRecursiveStream() {
        Stream<Integer> s = ones();
        long f = 0L;

        for(int ii=0; ii<10000; ii++) {
            f = s.head();
            s = s.tail();
        }

        assertEquals(1L, f);
    }

    @Test
    public void testRecursiveFib() {
        // https://www.sitepoint.com/functional-fizzbuzz-with-vavr/
        Stream<Long> s = fib();
        long f = 0L;

        for(int ii=0; ii<90; ii++) {
            f = s.head();
            s = s.tail();
        }

        assertEquals(2880067194370816120L, f);
    }

    @Test
    public void testLazyFiniteStream1() {
    	Stream<Long> s = Stream.ofAll(
			    			Iterators.iterate(
		    					1L,
		    					v -> v < 5
		    							? Option.of(v + 1)
		    							: Option.none()));

    	// returns: 1, 2, 3, 4, 5

    	long sum = 0L;
    	while (!s.isEmpty()) {
            sum += s.head();
            s = s.tail();
        }

        assertEquals(15, sum);
    }

    @Test
    public void testLazyFiniteStream2() {
    	final Long seed = 1L;
    	final Function<Long,Long> inc = v -> v + 1;
    	final Function<Long,Boolean> test = v -> v < 5 ? true : false;

    	Stream<Long> s = Stream.ofAll(
			    			Iterators.iterate(
		    					seed,
		    					v -> test.apply(v)
		    							? Option.of(inc.apply(v))
		    							: Option.none()));

    	// returns: 1, 2, 3, 4, 5

    	long sum = 0L;
    	while (!s.isEmpty()) {
            sum += s.head();
            s = s.tail();
        }

        assertEquals(15, sum);
    }

    private Stream<Integer> ones() {
        // Java evaluates method arguments before a method is called. In case of
        // an infinite stream this is tricked with a Supplier in order to prevent
        // a stack overflow.
        return Stream.cons(1, () -> ones());
    }

    private Stream<Long> fib() {
        return fib(1L, 1L);
    }

    private Stream<Long> fib(final long a, final long b) {
        return Stream.cons(a, () -> fib(b, a + b));
    }
}
