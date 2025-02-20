/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
import java.util.Collections;
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

@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class JavaSortingBenchmark {

    public JavaSortingBenchmark() {
    }

    @Benchmark
    public Object sort() {
        final ArrayList<Long> list = new ArrayList<>(2000);
        for(long ii=0; ii<2000; ii++) {
            list.add(Long.valueOf(ii));
        }
        Collections.shuffle(list);
        Collections.sort(list);
        return list;
    }

    @Benchmark
    public Object sort_effective(State_ state) {
        Collections.sort(state.list);
        return state.list;
    }

    @State(Scope.Benchmark)
    public static class State_ {
        public State_() {
            final ArrayList<Long> list = new ArrayList<>(2000);
            for(long ii=0; ii<2000; ii++) {
                list.add(Long.valueOf(ii));
            }
            Collections.shuffle(list);
            this.list = list;
        }

        public final ArrayList<Long> list;
    }
}
