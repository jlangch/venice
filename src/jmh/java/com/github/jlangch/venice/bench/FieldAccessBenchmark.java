/*   __	__		 _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *	\ \/ / _ \ '_ \| |/ __/ _ \
 *	 \  /  __/ | | | | (_|  __/
 *	  \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.bench;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

// Run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).
//
// Benchmark                                           Mode  Cnt  Score   Error  Units
// -----------------------------------------------------------------------------------
// FieldAccessBenchmark.dynamic_mh_invoke              avgt    5  3.953 ± 0.058  ns/op
// FieldAccessBenchmark.dynamic_mh_invokeExact         avgt    5  3.958 ± 0.034  ns/op
// FieldAccessBenchmark.dynamic_reflect                avgt    5  5.070 ± 0.055  ns/op
// FieldAccessBenchmark.dynamic_unreflect_invoke       avgt    5  4.147 ± 0.343  ns/op
// FieldAccessBenchmark.dynamic_unreflect_invokeExact  avgt    5  4.024 ± 0.239  ns/op
// FieldAccessBenchmark.plain                          avgt    5  1.916 ± 0.126  ns/op
// FieldAccessBenchmark.static_mh_invoke               avgt    5  1.901 ± 0.058  ns/op
// FieldAccessBenchmark.static_mh_invokeExact          avgt    5  1.897 ± 0.022  ns/op
// FieldAccessBenchmark.static_reflect                 avgt    5  4.378 ± 0.058  ns/op
// FieldAccessBenchmark.static_unreflect_invoke        avgt    5  1.901 ± 0.037  ns/op
// FieldAccessBenchmark.static_unreflect_invokeExact   avgt    5  1.904 ± 0.055  ns/op
//
// static_* cases are faster than dynamic_* due to aggressive inlining


@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class FieldAccessBenchmark {

	public FieldAccessBenchmark() {
		init();
	}
	
	@Benchmark
	public int plain() {
		return value;
	}

	@Benchmark
	public int dynamic_reflect() throws InvocationTargetException, IllegalAccessException {
		return (int)reflective.get(this);
	}

	@Benchmark
	public int dynamic_unreflect_invoke() throws Throwable {
		return (int)unreflect.invoke(this);
	}

	@Benchmark
	public int dynamic_unreflect_invokeExact() throws Throwable {
		return (int)unreflect.invokeExact(this);
	}

	@Benchmark
	public int dynamic_mh_invoke() throws Throwable {
		return (int)mh.invoke(this);
	}

	@Benchmark
	public int dynamic_mh_invokeExact() throws Throwable {
		return (int)mh.invokeExact(this);
	}

	@Benchmark
	public int static_reflect() throws InvocationTargetException, IllegalAccessException {
		return (int)static_reflective.get(this);
	}

	@Benchmark
	public int static_unreflect_invoke() throws Throwable {
		return (int)static_unreflect.invoke(this);
	}

	@Benchmark
	public int static_unreflect_invokeExact() throws Throwable {
		return (int)static_unreflect.invokeExact(this);
	}

	@Benchmark
	public int static_mh_invoke() throws Throwable {
		return (int)static_mh.invoke(this);
	}

	@Benchmark
	public int static_mh_invokeExact() throws Throwable {
		return (int)static_mh.invokeExact(this);
	}


	private void init() {
		try {
			reflective = FieldAccessBenchmark.class.getDeclaredField("value");
			unreflect = MethodHandles.lookup().unreflectGetter(reflective);
			mh = MethodHandles.lookup().findGetter(FieldAccessBenchmark.class, "value", int.class);
		} 
		catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private int value = 42;

	private static final Field static_reflective;
	private static final MethodHandle static_unreflect;
	private static final MethodHandle static_mh;

	private Field reflective;
	private MethodHandle unreflect;
	private MethodHandle mh;

	static {
		try {
			static_reflective = FieldAccessBenchmark.class.getDeclaredField("value");
			static_unreflect = MethodHandles.lookup().unreflectGetter(static_reflective);
			static_mh = MethodHandles.lookup().findGetter(FieldAccessBenchmark.class, "value", int.class);
		} 
		catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}
}
