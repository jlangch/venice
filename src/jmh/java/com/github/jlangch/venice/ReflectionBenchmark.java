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
package com.github.jlangch.venice;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function1;
import com.github.jlangch.venice.impl.util.reflect.LambdaMetafactoryUtil.Function2;
import org.openjdk.jmh.annotations.*;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class ReflectionBenchmark {
	
	public ReflectionBenchmark() {
		init();
	}
	
	
	@Benchmark
	public void bench_native() {
		final BigInteger i1 = BigInteger.valueOf(10L);
		final BigInteger i2 = BigInteger.valueOf(100L);
		final BigInteger sum = i1.add(i2);
	}

	@Benchmark
	public void bench_reflective() {
		try {
			final BigInteger i1 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {10L});
			final BigInteger i2 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {100L});
			final BigInteger sum = (BigInteger)mAdd.invoke(i1, new Object[] {i2});	       		
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Benchmark
	public void bench_LambdaMetafactory_1() {
		try {
			final BigInteger i1 = (BigInteger)fnValueOf.apply(10L);
			final BigInteger i2 = (BigInteger)fnValueOf.apply(100L);
			final BigInteger sum = (BigInteger)fnAdd.apply(i1, i2);      		
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Benchmark
	public void bench_LambdaMetafactory_2() {
		try {
			final BigInteger i1 = (BigInteger)LambdaMetafactoryUtil.invoke_staticMethod(new Object[] {10L}, fnValueOf);
			final BigInteger i2 = (BigInteger)LambdaMetafactoryUtil.invoke_staticMethod(new Object[] {100L}, fnValueOf);
			final BigInteger sum = (BigInteger)LambdaMetafactoryUtil.invoke_instanceMethod(i1, new Object[] {i2}, fnAdd);      		
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void init() {
		try {
			mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
			mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);
	
			fnValueOf = LambdaMetafactoryUtil.staticMethod_1_args(mValueOf);
			fnAdd = LambdaMetafactoryUtil.instanceMethod_1_args(mAdd);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	private Method mValueOf;
	private Method mAdd;
	private Function1<Object,Object> fnValueOf;
	private Function2<Object,Object,Object> fnAdd;
}
