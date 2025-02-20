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

import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
// Venice 1.10.16, Java 8
//
// Benchmark                                  Mode  Cnt  Score   Error  Units
// ThreadLocalBenchmark.interceptor           avgt    3  5.370 ± 0.436  ns/op
// ThreadLocalBenchmark.lookup                avgt    3  4.414 ± 0.583  ns/op
// ThreadLocalBenchmark.thread_id             avgt    3  2.685 ± 0.106  ns/op
// ThreadLocalBenchmark.thread_interceptor_1  avgt    3  9.749 ± 0.161  ns/op
// ThreadLocalBenchmark.thread_interceptor_2  avgt    3  9.909 ± 0.856  ns/op


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class ThreadLocalBenchmark {

    @Benchmark
    public Object lookup(State_ state) {
    	return ThreadContext.getInterceptor();
    }

    @Benchmark
    public Object thread_interceptor_1(State_ state) {
    	return ThreadContext.getInterceptor().validateVeniceFunction("+");
    }

    @Benchmark
    public Object thread_interceptor_2(State_ state) {
    	return ThreadContext.get().getInterceptor_().validateVeniceFunction("+");
    }

    @Benchmark
    public Object interceptor(State_ state) {
    	return state.interceptor.validateVeniceFunction("+");
    }

    @Benchmark
    public Object thread_id(State_ state) {
    	if (Thread.currentThread().getId() != state.threadID) {
    		throw new RuntimeException("Runs on another thread than the setup!");
    	}

    	return null;
    }


    @State(Scope.Benchmark)
    public static class State_ {
    	public IInterceptor interceptor = setup();
    	public long threadID = Thread.currentThread().getId();

    }

    private static IInterceptor setup() {
        final SandboxRules rules = new SandboxRules()
										.rejectVeniceFunctions("*io*")
										.rejectVeniceFunctions("*system*");

        final IInterceptor interceptor = new SandboxInterceptor(rules);

        ThreadContext.setInterceptor(interceptor);

        return interceptor;
    }
}
