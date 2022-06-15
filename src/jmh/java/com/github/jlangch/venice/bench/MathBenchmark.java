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

import com.github.jlangch.venice.impl.functions.MathFunctions;
import com.github.jlangch.venice.impl.types.VncLong;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
// Venice 1.10.16, Java 8
//
// Benchmark                  Mode  Cnt   Score   Error  Units
// MathBenchmark.addLong      avgt    3   3.456 ± 0.001  ns/op
// MathBenchmark.addLong4     avgt    3   4.261 ± 0.237  ns/op
// MathBenchmark.addVncLong   avgt    3  13.900 ± 0.417  ns/op
// MathBenchmark.addVncLong4  avgt    3  32.879 ± 0.272  ns/op


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class MathBenchmark {

     public MathBenchmark() {
     }

     @Benchmark
     public Object addLong() {
         return d1 + d2;
     }

     @Benchmark
     public Object addLong4() {
         return d1 + d2 + d3 + d4;
     }

     @Benchmark
     public Object addVncLong() {
         return MathFunctions.add.applyOf(d5, d6);
     }

     @Benchmark
     public Object addVncLong4() {
         return MathFunctions.add.applyOf(d5, d6, d7, d8);
     }


     private Long d1 = new Long(1L);
     private Long d2 = new Long(2L);
     private Long d3 = new Long(3L);
     private Long d4 = new Long(4L);
     private VncLong d5 = new VncLong(1L);
     private VncLong d6 = new VncLong(2L);
     private VncLong d7 = new VncLong(3L);
     private VncLong d8 = new VncLong(4L);
 }
