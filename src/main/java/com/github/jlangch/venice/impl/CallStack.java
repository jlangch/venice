/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class CallStack {

	public CallStack() {
	}
	
	
	public void push(final CallFrame frame) {
		queue.offer(frame);
	}
	
	public CallFrame pop() {
		return queue.poll();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public void clear() {
		queue.clear();
	}
	
	public CallStack copy() {
		final CallStack stack = new CallStack();
		queue.forEach(f -> stack.queue.add(f));
		return stack;
	}

	public List<String> toList() {
		final List<String> callstack =
				Arrays
					.stream(queue.toArray(new CallFrame[] {}))
					.map(f -> f.toString())
					.collect(Collectors.toList());
		
		Collections.reverse(callstack); 

		return callstack;
	}

	public VncVector toVncVector() {
		final List<VncVal> callstack =
				Arrays
					.stream(queue.toArray(new CallFrame[] {}))
					.map(f -> new VncOrderedMap(
									new VncKeyword(":fn-name"),
									new VncString(f.getFnName()),
									new VncKeyword(":file"),
									new VncString(f.getFile()),
									new VncKeyword(":line"),
									new VncLong(f.getLine()),
									new VncKeyword(":col"),
									new VncLong(f.getCol())))
					.collect(Collectors.toList());
		
		Collections.reverse(callstack); 		
		
		return new VncVector(callstack);
	}

	@Override
	public String toString() {
		return toList()
				.stream()
				.collect(Collectors.joining("\n"));
	}

	
	final ConcurrentLinkedQueue<CallFrame> queue = new ConcurrentLinkedQueue<>();
}
