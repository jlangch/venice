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

import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.env.Var;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncSymbol;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
// Venice 1.10.16, Java 8
//
// Benchmark                            Mode  Cnt   Score   Error  Units
// EnvGlobalQualifiedBenchmark.env_001  avgt    3  13.232 ± 1.852  ns/op
// EnvGlobalQualifiedBenchmark.env_002  avgt    3  14.403 ± 1.127  ns/op
// EnvGlobalQualifiedBenchmark.env_010  avgt    3  13.270 ± 0.282  ns/op
// EnvGlobalQualifiedBenchmark.env_020  avgt    3  13.215 ± 2.566  ns/op
// EnvGlobalQualifiedBenchmark.env_100  avgt    3  14.119 ± 2.534  ns/op


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class EnvGlobalQualifiedBenchmark {

    public EnvGlobalQualifiedBenchmark() {
        env_level_1 = createEnv(1);
        env_level_2 = createEnv(2);
        env_level_10 = createEnv(10);
        env_level_20 = createEnv(20);
        env_level_100 = createEnv(100);
    }

    @Benchmark
    public Object env_001() {
        return env_level_1.get(GLOBAL_SYMBOL);
    }

    @Benchmark
    public Object env_002() {
        return env_level_2.get(GLOBAL_SYMBOL);
    }

    @Benchmark
    public Object env_010() {
        return env_level_10.get(GLOBAL_SYMBOL);
    }

    @Benchmark
    public Object env_020() {
        return env_level_20.get(GLOBAL_SYMBOL);
    }

    @Benchmark
    public Object env_100() {
        return env_level_100.get(GLOBAL_SYMBOL);
    }


    private Env createEnv(final int levels) {
        Env env = createEnv(null);
        for(int ii=1; ii<levels; ii++) {
            env = createEnv(env);
        }
        env.setGlobal(new Var(GLOBAL_SYMBOL, new VncLong(900)));
        return env;
    }

    private Env createEnv(final Env parent) {
        final Env env = new Env(parent);
        env.setLocal(new Var(new VncSymbol("a"), new VncLong(100)));
        env.setLocal(new Var(new VncSymbol("b"), new VncLong(200)));
        env.setLocal(new Var(new VncSymbol("c"), new VncLong(300)));
        return env;
    }


    private static VncSymbol GLOBAL_SYMBOL = new VncSymbol("test/global");

    private final Env env_level_1;
    private final Env env_level_2;
    private final Env env_level_10;
    private final Env env_level_20;
    private final Env env_level_100;
}
