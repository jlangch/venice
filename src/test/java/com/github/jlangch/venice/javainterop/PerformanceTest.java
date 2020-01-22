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
package com.github.jlangch.venice.javainterop;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.util.Benchmark;


public class PerformanceTest {

	@Test
	public void test_native() {
		final BigInteger[] total = new BigInteger[] {BigInteger.valueOf(0L)};
		
     	new Benchmark("Native Java", 1_000_000, 10_000, 1).benchmark(ii -> {
    		final long start = System.nanoTime();
    		final BigInteger i1 = BigInteger.valueOf(ii);
    		final BigInteger i2 = BigInteger.valueOf(100L);
    		final BigInteger sum = i1.add(i2);      		
       		final long elapsed = System.nanoTime() - start;
       		
       		total[0] = total[0].add(sum); // prevent JIT from optimizing too much
       		
    		return elapsed;
    	});
	}

	@Test
	public void test_reflective() throws Exception {
		final BigInteger[] total = new BigInteger[] {BigInteger.valueOf(0L)};
		
		// cache methods
		final Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
		final Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);
		
     	new Benchmark("Reflective Java", 1_000_000, 10_000, 1).benchmark(ii -> {
     		try {
	    		final long start = System.nanoTime();
	    		final BigInteger i1 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {ii});
	    		final BigInteger i2 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {100L});
	    		final BigInteger sum = (BigInteger)mAdd.invoke(i1, new Object[] {i2});	       		
	       		final long elapsed = System.nanoTime() - start;
	       		
	       		total[0] = total[0].add(sum); // prevent JIT from optimizing too much
	       		
	    		return elapsed;
    		}
     		catch(Exception ex) {
     			throw new RuntimeException(ex);
     		}
    	});
	}

}
