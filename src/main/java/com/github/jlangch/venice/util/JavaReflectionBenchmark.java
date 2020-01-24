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

import java.lang.reflect.Method;
import java.math.BigInteger;

import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function2;

// ${JAVA_11_HOME}/bin/java -cp "libs/*" com.github.jlangch.venice.util.JavaReflectionBenchmark
// ${JAVA_8_HOME}/bin/java -cp "libs/*" com.github.jlangch.venice.util.JavaReflectionBenchmark

// https://dev.to/o_a_e/jmh-with-gradle--from-easy-to-simple-52ec
// https://www.cuba-platform.com/blog/think-twice-before-using-reflection/
// https://github.com/cuba-rnd/entity-lambda-accessors-benchmark
public class JavaReflectionBenchmark {
	
	public static void main(final String[] args) {
		System.out.println("Java VM: " 
								+ System.getProperty("java.version")
								+ " / "
								+ System.getProperty("java.vendor"));
		System.out.println();

		test_native();
		
		System.out.println();
		
		test_reflective();
		
		System.out.println();
		
		test_LambdaMetafactory_1();
		
		System.out.println();
		
		test_LambdaMetafactory_2();
	}
		   
	private static void test_native() {
		final BigInteger[] total = new BigInteger[] { BigInteger.ZERO };

		Benchmark
			.builder()
			.title("Native Java")
			.warmupIterations(WARMUP_ITERATIONS)
			.iterations(ITERATIONS)
			.microIterations(MICRO_ITERATIONS)
			.build()
			.benchmark(ii -> {
				final long start = System.nanoTime();
	
				for(long kk=0; kk<MICRO_ITERATIONS; kk++) {
					final BigInteger i1 = BigInteger.valueOf(ii);
					final BigInteger i2 = BigInteger.valueOf(100L);
					final BigInteger sum = i1.add(i2);
					
					total[0] = total[0].add(sum); // prevent JIT from optimizing too much
				}
	
				return System.nanoTime() - start;
			});

		System.out.println("SUM: " + total[0]);
	}

	private static void test_reflective() {
		try {
			final BigInteger[] total = new BigInteger[] { BigInteger.ZERO };

			// cache methods
			final Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
			final Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);

			Benchmark
				.builder()
				.title("Reflective Java")
				.warmupIterations(WARMUP_ITERATIONS)
				.iterations(ITERATIONS)
				.microIterations(MICRO_ITERATIONS)
				.build()
				.benchmark(ii -> {
						try {
							final long start = System.nanoTime();
			
							for(long kk=0; kk<MICRO_ITERATIONS; kk++) {
								final BigInteger i1 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {ii});
								final BigInteger i2 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {100L});
								final BigInteger sum = (BigInteger)mAdd.invoke(i1, new Object[] {i2});	       		
			
								total[0] = total[0].add(sum); // prevent JIT from optimizing too much
							}
						
							return System.nanoTime() - start;
						}
						catch(Exception ex) {
							throw new RuntimeException(ex);
						}
					});
	
			System.out.println("SUM: " + total[0]);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void test_LambdaMetafactory_1() {
		try {
			final BigInteger[] total = new BigInteger[] { BigInteger.ZERO };

			// cache methods
			final Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
			final Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);

			final Function1<Object,Object> fnValueOf = LambdaMetafactoryUtil.staticMethod_1_args(mValueOf);
			final Function2<Object,Object,Object> fnAdd = LambdaMetafactoryUtil.instanceMethod_1_args(mAdd);

			Benchmark
				.builder()
				.title("LambdaMetafactory 1 Java")
				.warmupIterations(WARMUP_ITERATIONS)
				.iterations(ITERATIONS)
				.microIterations(MICRO_ITERATIONS)
				.build()
				.benchmark(ii -> {
						try {
							final long start = System.nanoTime();
			
							for(long kk=0; kk<MICRO_ITERATIONS; kk++) {
								final BigInteger i1 = (BigInteger)fnValueOf.apply(ii);
								final BigInteger i2 = (BigInteger)fnValueOf.apply(100L);
								final BigInteger sum = (BigInteger)fnAdd.apply(i1, i2);      		
			
								total[0] = total[0].add(sum); // prevent JIT from optimizing too much
							}
						
							return System.nanoTime() - start;
						}
						catch(Exception ex) {
							throw new RuntimeException(ex);
						}
					});
	
			System.out.println("SUM: " + total[0]);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void test_LambdaMetafactory_2() {
		try {
			final BigInteger[] total = new BigInteger[] { BigInteger.ZERO };

			// cache methods
			final Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
			final Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);

			final Function1<Object,Object> fnValueOf = LambdaMetafactoryUtil.staticMethod_1_args(mValueOf);
			final Function2<Object,Object,Object> fnAdd = LambdaMetafactoryUtil.instanceMethod_1_args(mAdd);

			Benchmark
				.builder()
				.title("LambdaMetafactory 2 Java")
				.warmupIterations(WARMUP_ITERATIONS)
				.iterations(ITERATIONS)
				.microIterations(MICRO_ITERATIONS)
				.build()
				.benchmark(ii -> {
						try {
							final long start = System.nanoTime();
			
							for(long kk=0; kk<MICRO_ITERATIONS; kk++) {
								final BigInteger i1 = (BigInteger)LambdaMetafactoryUtil.invoke_staticMethod(new Object[] {ii}, fnValueOf);
								final BigInteger i2 = (BigInteger)LambdaMetafactoryUtil.invoke_staticMethod(new Object[] {100L}, fnValueOf);
								final BigInteger sum = (BigInteger)LambdaMetafactoryUtil.invoke_instanceMethod(i1, new Object[] {i2}, fnAdd);      		
			
								total[0] = total[0].add(sum); // prevent JIT from optimizing too much
							}
						
							return System.nanoTime() - start;
						}
						catch(Exception ex) {
							throw new RuntimeException(ex);
						}
					});
	
			System.out.println("SUM: " + total[0]);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static final int WARMUP_ITERATIONS = 100000;
	private static final int ITERATIONS = 400;
	private static final int MICRO_ITERATIONS = 800;
}
