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
package com.github.jlangch.venice.impl.types.collections;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncQueue extends VncCollection {

	public VncQueue() {
		this(Integer.MAX_VALUE);
	}

	public VncQueue(final int capacity) {
		super(Constants.Nil);
		this.capacity = capacity;
		this.queue = new LinkedBlockingDeque<>(capacity);
	}

	private VncQueue(final VncQueue queue, final VncVal meta) {
		super(meta);
		this.capacity = queue.capacity;
		this.queue = queue.queue;
	}

	
	@Override
	public VncCollection emptyWithMeta() {
		return new VncQueue(capacity);
	}

	@Override
	public VncQueue withMeta(final VncVal meta) {
		return new VncQueue(this, meta);
	}
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncCollection.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncCollection.TYPE, VncVal.TYPE);
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

	public VncBoolean offer(final VncVal val) {
		return VncBoolean.of(queue.offer(val));
	}

	public VncBoolean offer(final VncVal val, final long timeoutMillis) {
		try {
			return VncBoolean.of(queue.offer(val, timeoutMillis, TimeUnit.MILLISECONDS));
		}
		catch(InterruptedException ex) {
			throw new com.github.jlangch.venice.InterruptedException("interrupted while calling (offer queue timeout val)", ex);
		}
	}
	
	public VncVal poll() {
		return toNil(queue.poll());
	}
	
	public VncVal poll(final long timeoutMillis) {
		try {
			return toNil(queue.poll(timeoutMillis, TimeUnit.MILLISECONDS));
		}
		catch(InterruptedException ex) {
			throw new com.github.jlangch.venice.InterruptedException("interrupted while calling (poll queue timeout)", ex);
		}
	}

	public VncVal peek() {
		return toNil(queue.peek());
	}

	public void clear() {
		queue.clear();
	}
	
	@Override 
	public TypeRank typeRank() {
		return TypeRank.QUEUE;
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
		return "(" + Printer.join(toVncList(), " ", true) + ")";
	}
	
	public String toString(final boolean print_readably) {
		return "(" + Printer.join(toVncList(), " ", print_readably) + ")";
	}

	
	private VncVal toNil(final VncVal val) {
		return val == null ? Constants.Nil : val;
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/queue");

	private static final long serialVersionUID = -564531670922145260L;

	private final int capacity;
	private final LinkedBlockingDeque<VncVal> queue;
}
