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

import java.util.Map;
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

import com.github.jlangch.venice.IPreCompiled;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
// Venice 1.10.21, Java 8
//
// Benchmark                                                   Mode  Cnt     Score      Error  Units
// PrecompileBenchmark.no_precompilation_params                avgt    3  2452.398 ±  781.016  us/op
// PrecompileBenchmark.no_precompilation_noparams              avgt    3  2448.145 ± 1668.292  us/op
// PrecompileBenchmark.no_precompilation_ref                   avgt    3  2274.765 ±  536.594  us/op
// PrecompileBenchmark.precompilation_no_macroexpand_params    avgt    3    38.511 ±    4.331  us/op
// PrecompileBenchmark.precompilation_no_macroexpand_noparams  avgt    3    37.198 ±    1.744  us/op
// PrecompileBenchmark.precompilation_macroexpand_params       avgt    3     6.672 ±    0.173  us/op
// PrecompileBenchmark.precompilation_macroexpand_noparams     avgt    3     5.682 ±    1.664  us/op
// PrecompileBenchmark.precompilation_ref                      avgt    3     4.157 ±    0.339  us/op


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class PrecompileBenchmark {
    @Benchmark
    public Object no_precompilation_ref(State_ state) {
        return state.venice.eval("test", state.exprRef);
    }

    @Benchmark
    public Object precompilation_ref(State_ state) {
        return state.venice.eval(state.precompiled_ref);
    }

    @Benchmark
    public Object no_precompilation_params(State_ state) {
        return state.venice.eval("test", state.expr1, state.parameters);
    }

    @Benchmark
    public Object precompilation_no_macroexpand_params(State_ state) {
        return state.venice.eval(state.precompiledNoMacroExpand_params, state.parameters);
    }

    @Benchmark
    public Object precompilation_macroexpand_params(State_ state) {
        return state.venice.eval(state.precompiledMacroExpand_params, state.parameters);
    }

    @Benchmark
    public Object no_precompilation_noparams(State_ state) {
        return state.venice.eval("test", state.expr2);
    }

    @Benchmark
    public Object precompilation_no_macroexpand_noparams(State_ state) {
        return state.venice.eval(state.precompiledNoMacroExpand_noparams);
    }

    @Benchmark
    public Object precompilation_macroexpand_noparams(State_ state) {
        return state.venice.eval(state.precompiledMacroExpand_noparams);
    }

    @State(Scope.Benchmark)
    public static class State_ {
        public String expr1 = "(+ (cond (< x 0) -1 (> x 0) 1 :else 0) " +
                              "   (cond (< y 0) -1 (> y 0) 1 :else 0) " +
                              "   (cond (< z 0) -1 (> z 0) 1 :else 0))";

        public String expr2 = "(+ (cond (< -10 0) -1 (> -10 0) 1 :else 0) " +
                              "   (cond (< 0 0)   -1 (> 0 0)   1 :else 0) " +
                              "   (cond (< 10 0)  -1 (> 10 0)  1 :else 0))";

        public String exprRef = "nil";  // most simple expression, just return nil

        public Venice venice = new Venice();
        public Map<String,Object> parameters = Parameters.of("x", -10, "y", 0, "z", 10);

        public IPreCompiled precompiledNoMacroExpand_params = venice.precompile("example", expr1, false);
        public IPreCompiled precompiledMacroExpand_params = venice.precompile("example", expr1, true);
        public IPreCompiled precompiledNoMacroExpand_noparams = venice.precompile("example", expr2, false);
        public IPreCompiled precompiledMacroExpand_noparams = venice.precompile("example", expr2, true);
        public IPreCompiled precompiled_ref = venice.precompile("example", exprRef, true);
    }
}
