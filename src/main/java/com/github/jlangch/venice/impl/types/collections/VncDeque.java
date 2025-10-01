/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MetaUtil;


public class VncDeque extends VncCollection implements VncMutable, Iterable<VncVal> {

    public VncDeque() {
        this(Integer.MAX_VALUE);
    }

    public VncDeque(final int capacity) {
        super(Constants.Nil);
        this.capacity = capacity;
        this.deque = new LinkedBlockingDeque<>(capacity);
    }

    private VncDeque(final VncDeque deque, final VncVal meta) {
        super(meta);
        this.capacity = deque.capacity;
        this.deque = deque.deque;
    }


    @Override
    public VncCollection emptyWithMeta() {
        return new VncDeque(capacity);
    }

    @Override
    public VncDeque withMeta(final VncVal meta) {
        return new VncDeque(this, meta);
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                        TYPE,
                        MetaUtil.typeMeta(
                            new VncKeyword(VncCollection.TYPE),
                            new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncList toVncList() {
        return VncList.of(deque.toArray(new VncVal[0]));
    }

    @Override
    public VncVector toVncVector() {
        return VncVector.of(deque.toArray(new VncVal[0]));
    }

    @Override
    public int size() {
        return deque.size();
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    /**
     * Inserts the specified element into the deque represented by this deque
     * (in other words, at the tail of this deque) if it is possible to do so
     * immediately without violating capacity restrictions, returning true
     * upon success and false if no space is currently available.
     *
     * <p>This method is equivalent to {@link #offerLast}.
     *
     * @param val the value to add
     * @return true if the value was added to this deque, else false
     */
    public VncBoolean offer(final VncVal val) {
        return VncBoolean.of(deque.offer(val));
    }

    public VncBoolean offerFirst(final VncVal val) {
        return VncBoolean.of(deque.offerFirst(val));
    }

    public VncBoolean offerLast(final VncVal val) {
        return VncBoolean.of(deque.offerLast(val));
    }

    /**
     * Inserts the specified element into the deque represented by this deque
     * (in other words, at the tail of this deque), waiting up to the specified
     * wait time if necessary for space to become available.
     *
     * <p>This method is equivalent to {@link #offerLast}.
     *
     * @param val the value to add
     * @param timeoutMillis timeout how long to wait before giving up, in units of milliseconds
     * @return if the value was added to this deque, else false
     */
    public VncBoolean offer(final VncVal val, final long timeoutMillis) {
        try {
            return VncBoolean.of(deque.offer(val, timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (offer! deque timeout val)",
                        ex);
        }
    }

    public VncBoolean offerFirst(final VncVal val, final long timeoutMillis) {
        try {
            return VncBoolean.of(deque.offerFirst(val, timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (offer! deque timeout val)",
                        ex);
        }
    }

    public VncBoolean offerLast(final VncVal val, final long timeoutMillis) {
        try {
            return VncBoolean.of(deque.offerLast(val, timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (offer! deque timeout val)",
                        ex);
        }
    }

    /**
     * Inserts the specified element into the deque represented by this deque
     * (in other words, at the tail of this deque), waiting if necessary for
     * space to become available.
     *
     * <p>This method is equivalent to {@link #putLast}.
     *
     * @param val the value to add
     */
    public void put(final VncVal val) {
        try {
            deque.put(val);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (put! deque val)",
                        ex);
        }
    }
    public void putFirst(final VncVal val) {
        try {
            deque.putFirst(val);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (put! deque val)",
                        ex);
        }
    }
    public void putLast(final VncVal val) {
        try {
            deque.putLast(val);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (put! deque val)",
                        ex);
        }
    }

    /**
     * Retrieves and removes the head of the deque represented by this deque
     * (in other words, the first element of this deque), or returns nil
     * if this deque is empty.
     *
     * <p>This method is equivalent to {@link #pollFirst}.
     *
     * @return the head of this deque, or nil if this deque is empty
     */
    public VncVal poll() {
        return toNil(deque.poll());
    }

    public VncVal pollFirst() {
        return toNil(deque.pollFirst());
    }

    public VncVal pollLast() {
        return toNil(deque.pollLast());
    }

    /**
     * Retrieves and removes the head of the deque represented by this deque
     * (in other words, the first element of this deque), waiting up to the
     * specified wait time if necessary for an element to become available.
     *
     * <p>This method is equivalent to {@link #pollFirst}.
     *
     * @param timeoutMillis timeout how long to wait before giving up, in units of milliseconds
     * @return the head of this deque, or nil if the specified waiting time elapses before an element is available
     */
    public VncVal poll(final long timeoutMillis) {
        try {
            return toNil(deque.poll(timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (poll! deque timeout)",
                        ex);
        }
    }

    public VncVal pollFirst(final long timeoutMillis) {
        try {
            return toNil(deque.pollFirst(timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (poll! deque timeout)",
                        ex);
        }
    }

    public VncVal pollLast(final long timeoutMillis) {
        try {
            return toNil(deque.pollLast(timeoutMillis, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (poll! deque timeout)",
                        ex);
        }
    }


    /**
     * Retrieves, but does not remove, the head of the deque represented by this
     * deque (in other words, the first element of this deque), or returns nil
     * if this deque is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst}.
     *
     * @return the head of this deque, or nil if this deque is empty
     */
    public VncVal peek() {
        return toNil(deque.peek());
    }

    public VncVal peekFirst() {
        return toNil(deque.peekFirst());
    }

    public VncVal peekLast() {
        return toNil(deque.peekLast());
    }

    /**
     * Retrieves and removes the head of the queue represented by this queue
     * (in other words, the first element of this queue), waiting if necessary
     * until an element becomes available.
     *
     * <p>This method is equivalent to {@link #takeFirst}.
     *
     * @return the head of this queue
     */
    public VncVal take() {
        try {
            return deque.take();
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (take! deque)",
                        ex);
        }
    }

    public VncVal takeFirst() {
        try {
            return deque.takeFirst();
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (take! deque)",
                        ex);
        }
    }

    public VncVal takeLast() {
        try {
            return deque.takeLast();
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (take! deque)",
                        ex);
        }
    }

    @Override
    public void clear() {
        deque.clear();
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.DEQUEUE;
    }

    @Override
    public Object convertToJavaObject() {
        return Arrays
                .stream(deque.toArray(new VncVal[0]))
                .map(v -> v.convertToJavaObject())
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<VncVal> iterator() {
        return deque.iterator();
    }

    @Override
    public String toString() {
        return "(" + Printer.join(toVncList(), " ", true) + ")";
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return "(" + Printer.join(toVncList(), " ", print_machine_readably) + ")";
    }


    private VncVal toNil(final VncVal val) {
        return val == null ? Constants.Nil : val;
    }


    public static final String TYPE = ":core/deque";

    private static final long serialVersionUID = -564531670922145260L;

    private final int capacity;
    private final LinkedBlockingDeque<VncVal> deque;
}
