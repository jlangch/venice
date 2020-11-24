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

import java.util.concurrent.ConcurrentHashMap;
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

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
//
// Java Benchmark                Mode  Cnt  Score        Error        Units
// ------------------------------------------------------------------------
// create_mutable_map            avgt    3  126'334.710  ± 16018.747  ns/op
// create_persistent_map         avgt    3  129'435.875  ± 24465.158  ns/op
//
//
// Venice Benchmark              Mode  Cnt  Score        Error        Units
// -----------------------------------------------------------------------
// create-mutable-map                       1'560'000                 ns/op
// create-persistent-map                    1'640'000                 ns/op
//
// => Java is 12x faster than Venice


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class CreateMapBenchmark {
	
	public CreateMapBenchmark() {
	}
	
	/** 
	    [VENICE]
	     
	    (load-module :benchmark)
	       
	 	(defn create−mutable-map [size] 
		  (loop [m (mutable-map), i size]
		    (if (zero? i)
		      m
		      (recur (assoc! m i (* 2 i)) (dec i)))))

	 	(defn create−persistent-map [size] 
		  (loop [m (hash-map), i size]
		    (if (zero? i)
		      m
		      (recur (assoc m i (* 2 i)) (dec i)))))


        (bench/benchmark (create−mutable-map 2000) 1000 1000)
        
 		(do (time (dorun 1000 (create−mutable-map 2000))) nil)
		(reduce + (dobench 1000 (create−mutable-map 2000)))
		
		
		[CLOJURE]
		
		(require '[criterium.core :as criterium])
		
		(time (dotimes [_ 1e6] (reduce + (map #(/ % 100.0) (range 100)))))
		
		(criterium/quick-bench (reduce + (map #(/ % 100.0) (range 100))))
    */

	@Benchmark
	public Object create_mutable_map() {
		final ConcurrentHashMap<Long,Long> map = new ConcurrentHashMap<>();
		for(long ii=0; ii<2000; ii++) {
			map.put(Long.valueOf(ii), Long.valueOf(ii*2));
		}
		return map;
	}

	@Benchmark
	public Object create_persistent_map() {
		io.vavr.collection.HashMap<Long,Long> map = io.vavr.collection.HashMap.empty();
		for(long ii=0; ii<2000; ii++) {
			map = map.put(Long.valueOf(ii), Long.valueOf(ii*2));
		}
		return map;
	}
}
