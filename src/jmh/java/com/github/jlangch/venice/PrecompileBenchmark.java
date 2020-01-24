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
package com.github.jlangch.venice.examples;

import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Venice;
import org.openjdk.jmh.annotations.*;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class PrecompileBenchmark {
 
	public PrecompileBenchmark() {
		init();
	}

	@Benchmark
    public Object bench_no_precompilation() {
		return venice.eval("test", expr, Parameters.of("x", -10, "y", 0, "z", 10));
    }

	@Benchmark
    public Object bench_precompilation_no_macroexpand() {
		return venice.eval(precompiledNoMacroExpand, Parameters.of("x", -10, "y", 0, "z", 10));
    }
    
	@Benchmark
	public Object bench_precompilation_macroexpand() {
    	return venice.eval(precompiledMacroExpand, Parameters.of("x", -10, "y", 0, "z", 10));
    }

    private void init() {
        this.venice = new Venice();
        this.precompiledNoMacroExpand = venice.precompile("example", expr, false);
        this.precompiledMacroExpand = venice.precompile("example", expr, true);
    }
    
    
	private String expr = "(do (cond (< x 0) -1 (> x 0) 1 :else 0) " +
						  "    (cond (< y 0) -1 (> y 0) 1 :else 0) " +
						  "    (cond (< z 0) -1 (> z 0) 1 :else 0))";
	
	private Venice venice;
	
	private PreCompiled precompiledNoMacroExpand;
	private PreCompiled precompiledMacroExpand;
}