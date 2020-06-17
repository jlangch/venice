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
package com.github.jlangch.venice.impl.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class CallStack {

	public CallStack() {
	}
	
	
	public void push(final CallFrame frame) {
		queue.push(frame);
	}
	
	public CallFrame pop() {
		return isEmpty() ? null : queue.pop();
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
		return Arrays
				.stream(queue.toArray(new CallFrame[] {}))
				.map(f -> f.toString())
				.collect(Collectors.toList());
	}

	public List<CallFrame> callstack() {
		return Arrays.asList(queue.toArray(new CallFrame[] {}));
	}

	@Override
	public String toString() {
		return toList()
				.stream()
				.collect(Collectors.joining("\n"));
	}

	
	// A call stack is used only as a thread local variable. So it does
	// not face concurrent usage. ArrayDeque is the fastest Deque available.
	private final ArrayDeque<CallFrame> queue = new ArrayDeque<>(32);

	//private final ArrayListStack<CallFrame> queue = new ArrayListStack<>(20);
	//private final ConcurrentLinkedDeque<CallFrame> queue = new ConcurrentLinkedDeque<>();
}
