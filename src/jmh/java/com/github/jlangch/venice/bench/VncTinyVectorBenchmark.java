/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncTinyVector;
import com.github.jlangch.venice.impl.types.collections.VncVector;

import io.vavr.collection.Vector;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class VncTinyVectorBenchmark {

    public VncTinyVectorBenchmark() {
    }


    @Benchmark
    public Object prepend() {
        return vector.addAtStart(val);
    }

    @Benchmark
    public Object append() {
        return vector.addAtEnd(val);
    }

    @Benchmark
    public Object first() {
        return vector.first();
     }

    @Benchmark
    public Object last() {
        return vector.last();
    }

    @Benchmark
    public Object rest() {
        return vector.rest();
    }

    @Benchmark
    public Object butlast() {
        return vector.butlast();
    }

    @Benchmark
    public Object drop_1() {
        return vector.removeAt(0);
    }

    @Benchmark
    public Object map() {
        return vector.map(v -> new VncLong(((VncLong)v).getValue() + 1));
    }

    @Benchmark
    public Object map_for_1() {
        Vector<VncLong> tmp = Vector.empty();
        for(VncVal v : vector) {
            tmp = tmp.append(new VncLong(((VncLong)v).getValue() + 1));
        }
        return tmp;
    }

    @Benchmark
    public Object map_for_2() {
        List<VncLong> tmp = new ArrayList<>(vector.size());
        for(VncVal v : vector) {
            tmp.add(new VncLong(((VncLong)v).getValue() + 1));
        }
        return Vector.ofAll(tmp);
    }



    private final VncVal val = new VncLong(0L);

                                     // range 1..4
    private final VncVector vector = VncTinyVector.of(
                                        new VncLong(1),
                                        new VncLong(2),
                                        new VncLong(3));
}
