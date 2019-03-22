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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.StopWatch;


public class PrecompiledTest {

	@Test
	public void test_simple() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (nil? 1) (+ 1 3))");
		
		assertEquals(4L, venice.eval(precomp));
	}

	@Test
	public void test_simple_with_params() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (+ x y))");
		
		assertEquals(300L, venice.eval(precomp, Parameters.of("x", 100, "y", 200)));
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
	public void test_with_fn() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (defn sum[x y] (+ x y)) (sum 1 3))");
		
		assertEquals(4L, venice.eval(precomp));
	}

	@Test
	public void test_with_fn_serialize() {
		final Venice venice = new Venice();
		
		final PreCompiled precomp = venice.precompile("test", "(do (defn sum[x y] (+ x y)) (sum 1 3))");
		
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

}
