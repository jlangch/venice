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
package com.github.jlangch.venice.impl.types.collections;

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.functions.MathFunctions;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;

import io.vavr.collection.Stream;


public class LazySeqTest {

    @Test
    public void testRecursiveStream() {
        VncLazySeq s = ones();
        VncVal f = null;

        for(int ii=0; ii<10000; ii++) {
            f = s.first();
            s = s.rest();
        }

        assertEquals(ONE, f);
    }

    @Test
    public void testRecursiveFib() {
        /* (defn fib
              ([]     (fib 1 1))
              ([a b]  (cons a (lazy-seq (fn [x] (fib b (+ a b)))))))

           (doall (take 20 (fib)))
         */

        // https://www.sitepoint.com/functional-fizzbuzz-with-vavr/
        VncLazySeq s = fib();

        s = s.take(90);
        VncList list = s.realize();

        assertEquals(2880067194370816120L, ((VncLong)list.last()).getValue());
    }

    @SuppressWarnings("serial")
    @Test
    public void testFinitStream() {
        VncLazySeq seq = VncLazySeq.iterate(
                            new VncFunction("test") {
                                @Override
                                public VncVal apply(VncList args) {
                                    return count > 0 ? new VncLong(count--) : Nil;
                                }
                                private long count = 5;
                            },
                            Nil);


        assertEquals(5L, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(4L, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(3L, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(2L, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(1L, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(null, seq.first().convertToJavaObject());
        seq = seq.rest();

        assertEquals(null, seq.first().convertToJavaObject());
        seq = seq.rest();
    }




    private VncLazySeq ones() {
        // Java evaluates method arguments before a method is called. In case of
        // an infinite stream this is tricked with a Supplier in order to prevent
        // a stack overflow.
        return new VncLazySeq(
                    Stream.cons(ONE, () -> ones().lazyStream()),
                    Nil);
    }


    private VncLazySeq fib() {
        return fib(ONE, ONE);
    }

    private VncLazySeq fib(final VncLong a, final VncLong b) {
        return new VncLazySeq(
                    Stream.cons(a, () -> fib(b, add(a,b)).lazyStream()),
                    Nil);
    }

    private VncLong add(final VncLong a, final VncLong b) {
        return (VncLong)MathFunctions.add.apply(VncList.of(a,b));
    }


    private final static VncLong ONE = new VncLong(1L);
}
