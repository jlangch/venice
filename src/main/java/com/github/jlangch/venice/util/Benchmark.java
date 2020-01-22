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
package com.github.jlangch.venice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.functions.SystemFunctions;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncList;


/**
 * A simple micro benchmark class
 */
public class Benchmark {

	public Benchmark(
			final String title, 
			final int iterations
	) {
		this(title, iterations, iterations, 0);
	}

	public Benchmark(
			final String title, 
			final int iterations, 
			final int microIterations
	) {
		this(title, iterations, iterations, microIterations);
	}

	public Benchmark(
			final String title, 
			final int warmupIterations, 
			final int iterations, 
			final int microIterations
	) {
		this.title = title;
		this.warmupIterations = warmupIterations;
		this.iterations = iterations;
		this.microIterations = microIterations;
	}
	
	public void benchmark(final Function<Integer,Long> task) {
		
        // warmup
        for(int ii=0; ii<warmupIterations; ii++) {
           task.apply(ii);
        }
        
        // run GC twice after warmup
        System.gc();
        System.gc();
        
        // benchmark
        final List<Long> raw = new ArrayList<>();       
        for(int ii=0; ii<iterations; ii++) {
            final long elapsed = task.apply(ii);         
            raw.add(elapsed);
        }
        
        // print results
        final List<Long> measures = stripOutliers(raw);
        final long elapsed = sum(measures);
        
        final String sElapsed = formatNanos(elapsed);
        final String sPerCall = formatNanos(elapsed 
												/ measures.size() 
												/ (microIterations > 1 ? microIterations : 1));

        System.out.println(String.format("%s Calls   : %12s", title, measures.size() * microIterations));

        System.out.println(String.format("%s Elapsed : %12s", title, sElapsed));
        
        System.out.println(String.format("%s Per call: %12s", title, sPerCall));
	}
	
	
	private String formatNanos(final long nanos) {
		final String s = ((VncString)SystemFunctions
										.format_nano_time
										.apply(VncList.of(new VncLong(nanos)))).getValue();
		
		// make the units always two chars width for printing alignment
		return s.endsWith(" s") ? s + " " : s;
	}
	
	private long sum(final List<Long> measures) {
		return measures.stream().mapToLong(p -> p).sum();
	}
	
	private List<Long> stripOutliers(final List<Long> measures) {
		// definition: the top 20% of the measures are outliers
		final int limit = (measures.size() * 8) / 10;
		return measures
					.stream()
					.sorted()
					.limit(limit) 
					.collect(Collectors.toList());
	}
	
	
	private final String title;
	private final int warmupIterations;
	private final int iterations;
	private final int microIterations;
}
