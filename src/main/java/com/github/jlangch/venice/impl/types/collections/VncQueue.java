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
package com.github.jlangch.venice.impl.types.collections;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncQueue extends VncCollection {

	public VncQueue() {
		this(null, Constants.Nil);
	}

	private VncQueue(final ConcurrentLinkedQueue<VncVal> stack, final VncVal meta) {
		super(meta);
		this.queue = stack != null ? stack : new ConcurrentLinkedQueue<>();
	}

	
	@Override
	public VncCollection empty() {
		return new VncQueue();
	}

	@Override
	public VncQueue withMeta(final VncVal meta) {
		return new VncQueue(queue, meta);
	}

	@Override
	public VncList toVncList() {
		return VncList.of(queue.toArray(new VncVal[0]));
	}

	@Override
	public VncVector toVncVector() {
		return VncVector.of(queue.toArray(new VncVal[0]));
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public VncQueue offer(final VncVal val) {
		queue.offer(val);
		return this;
	}

	public VncVal poll() {
		return isEmpty() ? Constants.Nil : queue.poll();
	}

	public VncVal peek() {
		return isEmpty() ? Constants.Nil : queue.peek();
	}
	
	@Override 
	public int typeRank() {
		return 203;
	}

	@Override
	public Object convertToJavaObject() {
		return Arrays
				.stream(queue.toArray(new VncVal[0]))
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toList());
	}

	@Override 
	public String toString() {
		return "(" + Printer.join(toVncList().getList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(toVncList().getList(), " ", print_readably) + ")";
	}


	private static final long serialVersionUID = -564531670922145260L;

	private final ConcurrentLinkedQueue<VncVal> queue;
}
