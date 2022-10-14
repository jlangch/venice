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

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.impl.util.callstack.CallStack;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
// Venice 1.10.16, Java 8
//
// Benchmark                    Mode  Cnt  Score    Error  Units
// CallStackBenchmark.nanoTime  avgt    3  35.148 ± 0.463  ns/op
// CallStackBenchmark.push_pop  avgt    3   6.729 ± 0.397  ns/op


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class CallStackBenchmark {

    public CallStackBenchmark() {
    }

    @Benchmark
    public Object push_pop() {
        final CallFrame cf = new CallFrame("test", Constants.Nil);

        cs.push(cf);
        return cs.pop();
    }

    @Benchmark
    public Object nanoTime() {
        return System.nanoTime();
    }


    private final CallStack cs = new CallStack();
}
