/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class SwitchBenchmark {
	
	public SwitchBenchmark() {
	}
	
	
	@Benchmark
	public Object dispatchLargeHit() {
		return dispatchLarge("var-local?");
	}
	
	@Benchmark
	public Object dispatchLargeMiss() {
		return dispatchLarge("xxxxxxxxx");
	}
	
	@Benchmark
	public Object dispatchSmallHit() {
		return dispatchSmall("loop");
	}
	
	@Benchmark
	public Object dispatchSmallMiss() {
		return dispatchSmall("xxxxxxxxx");
	}

	private int dispatchLarge(final String s) {
		switch (s) {
			case "do": return 100;
			case "if": return 101;
			case "let": return 102;
			case "loop": return 103;
			case "recur":  return 104;
			case "quasiquote": return 105;
			case "quote": return 106;
			case "fn": return 107;
			case "eval": return 108;
			case "def":  return 109;
			case "defonce": return 110;
			case "def-dynamic": return 111;
			case "defmacro": return 112;
			case "defprotocol": return 113;
			case "extend": return 114;
			case "extends?": return 115;
			case "deftype": return 116;
			case "deftype?": return 117;
			case "deftype-of": return 118;
			case "deftype-or": return 119;
			case "deftype-describe": return 120;
			case ".:": return 121;
			case "defmulti": return 122;
			case "defmethod": return 123;
			case "ns": return 124;
			case "ns-remove": return 125;
			case "ns-unmap": return 126;
			case "ns-list": return 127;
			case "import": return 128;
			case "imports": return 129;
			case "namespace": return 130;
			case "resolve": return 131;
			case "var-get": return 132;
			case "var-ns": return 133;
			case "var-name": return 134;
			case "var-local?": return 135;
			case "var-thread-local?": return 136;
			case "var-global?": return 137;
			case "set!": return 138;		
			case "inspect": return 139;
			case "macroexpand": return 140;
			case "macroexpand-all*": return 141;
			case "doc": return 142;
			case "print-highlight": return 143;
			case "modules": return 144;
			case "binding": return 145;
			case "bound?": return 146;
			case "try": return 147;
			case "try-with": return 148;
			case "locking": return 149;
			case "dorun": return 150;
			case "dobench": return 151;
			case "prof": return 152;		
			case "tail-pos": return 153;
			default: return 0;
		}
	}

	private int dispatchSmall(final String s) {
		switch (s) {
			case "do": return 100;
			case "if": return 101;
			case "let": return 102;
			case "loop": return 103;
			case "recur":  return 104;
			case "quasiquote": return 105;
			case "quote": return 106;
			case "fn": return 107;
			default: return 0;
		}
	}
}
