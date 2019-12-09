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

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.util.Benchmark;

// Without precompilation
public class Embed_05_PrecompiledShootout_1 {
    
    public static void main(final String[] args) {
    	final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
   	
    	new Benchmark("No precompilation", 10_000).benchmark(ii -> {
    		final long start = System.nanoTime();
    		venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
    		return System.nanoTime() - start;
    	});
    }

}