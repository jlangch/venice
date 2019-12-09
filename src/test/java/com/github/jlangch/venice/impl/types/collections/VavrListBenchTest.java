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
package com.github.jlangch.venice.impl.types.collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.Benchmark;

import io.vavr.collection.List;


@Disabled
public class VavrListBenchTest {
    
	private final int iterations = 10_000;
	private final int actions = 1_000;

	
	@Test
	public void test_vector_prepend() {
    	new Benchmark("List::prepend", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v = v.prepend(0L);
    		}

            return System.nanoTime() - start;
    	});
	}
	
	@Test
	public void test_vector_append() {
    	new Benchmark("List::append", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v = v.append(0L);
    		}

            return System.nanoTime() - start;
    	});
	}

	@Test
	public void test_vector_first() {
    	new Benchmark("List::first", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v.get(0);
    		}

            return System.nanoTime() - start;
    	});
 	}
	
	@Test
	public void test_vector_last() {
    	new Benchmark("List::last", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v.last();
    		}

            return System.nanoTime() - start;
    	});
	}

	@Test
	public void test_vector_rest() {
    	new Benchmark("List::rest", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v.tail();
    		}

            return System.nanoTime() - start;
    	});
	}
	
	@Test
	public void test_vector_butlast() {
    	new Benchmark("List::butlast", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v.slice(0, v.length()-1);
    		}

            return System.nanoTime() - start;
    	});
	}
	
	@Test
	public void test_vector_drop_1() {
    	new Benchmark("List::drop", iterations, actions).benchmark(ii -> {
    		final long start = System.nanoTime();
             
    		List<Long> v = create(1000);
    		for(int jj=0; jj<actions; jj++) {
    			v.drop(1);
    		}

            return System.nanoTime() - start;
    	});
	}

	
	private List<Long> create(final int len) {
		List<Long> v = List.empty();
		
		for(int ii=0; ii<len; ii++) {
			v = v.append(0L);
		}
		
		return v;
	}
}
