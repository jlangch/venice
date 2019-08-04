/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StopWatch;
import com.github.jlangch.venice.util.CapturingPrintStream;


public class PrecompiledTest {

	@Test
	public void test_simple() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");
		
		assertEquals(4L, venice.eval(precomp));
	}

	@Test
	public void test_simple2() throws Exception {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile(
										"test", 
										"(do (defn sum [a b] (+ a b z)) (sum x y))");
		
		assertEquals(103L, venice.eval(precomp, Parameters.of("x", 100L, "y", 1L, "z", 2L)));
	}

	@Test
	public void test_ns() throws Exception {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (defn x [] *ns*) (x))");
		
		assertEquals("user", venice.eval(precomp));
	}

	@Test
	public void test_simple_with_params() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (+ x y))");
		
		assertEquals(300, venice.eval(precomp, Parameters.of("x", 100, "y", 200)));
		assertEquals(300L, venice.eval(precomp, Parameters.of("x", 100L, "y", 200L)));
	}

	@Test
	public void test_simple_serialize() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");
		
		final byte[] data = precomp.serialize();
		System.out.println("PreCompiled (simple) size: " + data.length);
		assertEquals(4L, venice.eval(PreCompiled.deserialize(data)));
	}
	
	@Test
	public void test_stdout() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(print 23)");
		
		final CapturingPrintStream ps = CapturingPrintStream.create();

		venice.eval(precomp, Parameters.of("*out*", ps));
		
		assertEquals("23", ps.getOutput());
	}
	
	@Test
	public void test_with_stdout_str() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(with-out-str (print 23))");
		
		final CapturingPrintStream ps = CapturingPrintStream.create();

		assertEquals("23", venice.eval(precomp, Parameters.of("*out*", ps)));
	}

	@Test
	public void test_with_fn() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (defn sum [x y] (+ x y)) (sum 1 3))");
		
		assertEquals(4L, venice.eval(precomp));
	}

	@Test
	public void test_with_fn_serialize() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (defn sum [x y] (+ x y)) (sum 1 3))");
		
		final byte[] data = precomp.serialize();
		System.out.println("PreCompiled (defn) size: " + data.length);
		assertEquals(4L, venice.eval(PreCompiled.deserialize(data)));
	}

	@Test
	public void test_elapsed() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");
				
		// warmup
		for(int ii=0; ii<40_000; ii++) {
			venice.eval(precomp);
		}
		
		System.gc();
		final StopWatch	sw = StopWatch.nanos();
		for(int ii=0; ii<10_000; ii++) {
			venice.eval(precomp);
		}
		sw.stop();	
		
		System.out.println("Elapsed (pre-compiled, 10'000 calls): " + sw.toString()); 
	}

	@Test
	public void test_elapsed_with_params() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ x y))");
				
		// warmup
		for(int ii=0; ii<40_000; ii++) {
			venice.eval(precomp, Parameters.of("x", 100, "y", 200));
		}
		
		System.gc();
		final StopWatch	sw = StopWatch.nanos();
		for(int ii=0; ii<10_000; ii++) {
			venice.eval(precomp, Parameters.of("x", 100, "y", 200));
		}
		sw.stop();
		
		System.out.println("Elapsed (pre-compiled, params, 10'000 calls): " + sw.toString()); 
	}

	@Test
	public void test_multi_threaded() throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(10);

		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile(
										"test", 
										"(do (defn sum [a b] (+ a b z)) (sleep (rand-long 50)) (sum x y))");
		
		final List<Callable<Object>> tasks = new ArrayList<>();
		for(long ii=0; ii<2000; ii++) {
			final long count = ii;
			tasks.add(new Callable<Object>() {
				public Object call() throws Exception {
					return venice.eval(precomp, Parameters.of("x", 100L, "y", 0L, "z", count));
				}
			});
		}
		
		final List<Future<Object>> results = es.invokeAll(tasks);

		assertEquals(2000, results.size());
		
		long resVal = 100L;
		for(Future<Object> result : results) {
			assertEquals(resVal++, result.get());		
		}
		
		es.shutdown();
	}

	@Test
	public void test_multi_threaded_2() throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(10);

		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile(
										"test", 
										"(do (defn sum [a b] (+ a b z)) (long (with-out-str (do (sleep (rand-long 50)) (print (sum x y))))))");
		
		final List<Callable<Object>> tasks = new ArrayList<>();
		for(long ii=0; ii<2000; ii++) {
			final long count = ii;
			tasks.add(new Callable<Object>() {
				public Object call() throws Exception {
					return venice.eval(precomp, Parameters.of("x", 100L, "y", 0L, "z", count));
				}
			});
		}
		
		final List<Future<Object>> results = es.invokeAll(tasks);

		assertEquals(2000, results.size());
		
		long resVal = 100L;
		for(Future<Object> result : results) {
			assertEquals(resVal++, result.get());		
		}
		
		es.shutdown();
	}

}
