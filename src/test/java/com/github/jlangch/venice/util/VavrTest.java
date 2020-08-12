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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


public class VavrTest {

	@Test @Disabled
	public void testPerformance() {
		Vector<Long> vec = Vector.of(1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L);
		
		// WARMUP ------------------------------------------------------------
		int count = 0;
		for(int ii=0; ii<20000; ii++) {
			final List<Long> result = new ArrayList<>();
			vec.forEach(s -> result.add(s));
			count += result.size();
		}

		for(int ii=0; ii<20000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.asJava()) {
				result.add(i + 1L);
			}
			count += result.size();
		}

		for(int ii=0; ii<20000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.toJavaList()) {
				result.add(i + 1L);
			}
			count += result.size();
		}

		for(int ii=0; ii<20000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.asJavaMutable()) {
				result.add(i + 1L);
			}
			count += result.size();
		}

		
		// TEST ------------------------------------------------------------

		count = 0;
		
		System.gc();
		long nanos = System.nanoTime();
		for(int ii=0; ii<1000; ii++) {
			final List<Long> result = new ArrayList<>();
			vec.forEach(s -> result.add(s + 1L));
			count += result.size();
		}
		long elapsed = System.nanoTime() - nanos;
		System.out.println("Vavr forEach(): " + elapsed / 1000);

		System.gc();
		nanos = System.nanoTime();
		for(int ii=0; ii<1000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.asJava()) {
				result.add(i + 1L);
			}
			count += result.size();
		}
		elapsed = System.nanoTime() - nanos;
		System.out.println("Vavr vec.asJava(): " + elapsed / 1000);

		System.gc();
		nanos = System.nanoTime();
		for(int ii=0; ii<1000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.toJavaList()) {
				result.add(i + 1L);
			}
			count += result.size();
		}
		elapsed = System.nanoTime() - nanos;
		System.out.println("Vavr vec.toJavaList(): " + elapsed / 1000);

		System.gc();
		nanos = System.nanoTime();
		for(int ii=0; ii<1000; ii++) {
			final List<Long> result = new ArrayList<>();
			for(Long i : vec.asJavaMutable()) {
				result.add(i + 1L);
			}
			count += result.size();
		}
		elapsed = System.nanoTime() - nanos;
		System.out.println("Vavr vec.asJavaMutable(): " + elapsed / 1000);

		assertEquals(72000, count);
	}
	
	@Test 
	public void testRecursiveStream() {
		Stream<Integer> s = ones();
		long f = 0L;
		
		for(int ii=0; ii<10000; ii++) {
			f = s.head();
			s = s.tail();
		}
		
		assertEquals(1L, f);
	}
	
	@Test
	public void testRecursiveFib() {
		// https://www.sitepoint.com/functional-fizzbuzz-with-vavr/
		Stream<Long> s = fib();
		long f = 0L;
		
		for(int ii=0; ii<90; ii++) {
			f = s.head();
			s = s.tail();
		}
		
		assertEquals(2880067194370816120L, f);
	}
	
	private Stream<Integer> ones() {
		// Java evaluates method arguments before a method is called. In case of
		// an infinite stream this is tricked with a Supplier in order to prevent 
		// a stack overflow.
	    return Stream.cons(1, () -> ones());
	}
	
	private Stream<Long> fib() {
	    return fib(1L, 1L);
	}
	
	private Stream<Long> fib(final long a, final long b) {
	    return Stream.cons(a, () -> fib(b, a + b));
	}
}
