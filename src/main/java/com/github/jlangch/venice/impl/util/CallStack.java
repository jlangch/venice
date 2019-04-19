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
package com.github.jlangch.venice.impl.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


public class CallStack {

	public CallStack() {
	}
	
	
	public void push(final CallFrame frame) {
		if (frame.getModule() == null) {
			if (queue.isEmpty()) {
				// default to "user"
				queue.offer(frame.withModule("user"));
			}
			else {
				// inherit module
				queue.offer(frame.withModule(queue.peek().getModule()));
			}
		}
		else {
			queue.offer(frame);
		}
	}
	
	public CallFrame pop() {
		return queue.poll();
	}
	
	public CallFrame peek() {
		return queue.peek();
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

	public List<CallFrame> callstack() {
		final List<CallFrame> callstack = Arrays.asList(queue.toArray(new CallFrame[] {}));
		
		Collections.reverse(callstack); 

		return callstack;
	}
	
	public String peekModule() {
		return queue.isEmpty() ? "user" : queue.peek().getModule();
	}

	@Override
	public String toString() {
		return toList()
				.stream()
				.collect(Collectors.joining("\n"));
	}

	
	
	private final ConcurrentLinkedQueue<CallFrame> queue = new ConcurrentLinkedQueue<>();
}
