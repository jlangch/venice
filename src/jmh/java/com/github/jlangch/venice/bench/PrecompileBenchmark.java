/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Venice;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class PrecompileBenchmark {
	@Benchmark
    public Object no_precompilation(State_ state) {
		return state.venice.eval("test", state.expr, state.parameters);
    }

	@Benchmark
    public Object precompilation_no_macroexpand(State_ state) {
		return state.venice.eval(state.precompiledNoMacroExpand, state.parameters);
    }
    
	@Benchmark
	public Object precompilation_macroexpand(State_ state) {
    	return state.venice.eval(state.precompiledMacroExpand, state.parameters);
    }
  
    @State(Scope.Benchmark)
    public static class State_ {
    	public String expr = "(+ (cond (< x 0) -1 (> x 0) 1 :else 0) " +
							 "   (cond (< y 0) -1 (> y 0) 1 :else 0) " +
							 "   (cond (< z 0) -1 (> z 0) 1 :else 0))";

    	public Venice venice = new Venice();
    	public PreCompiled precompiledNoMacroExpand = venice.precompile("example", expr, false);
    	public PreCompiled precompiledMacroExpand = venice.precompile("example", expr, true);
    	public Map<String,Object> parameters = Parameters.of("x", -10, "y", 0, "z", 10);
    }
}