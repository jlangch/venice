/*
 *
 *  Copyright 2016 Robert Winkler and Bohdan Storozhuk
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

/*
 * io.github.resilience4j:resilience4j-circularbuffer:2.3.0
 *
 * Modified by Venice 22.11.2025
 *  - changed package to com.github.jlangch.venice.resilience4j.circularbuffer
 *  - backported to Java 8
 */
package com.github.jlangch.venice.resilience4j.circularbuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Thread safe implementation of {@link CircularFifoBuffer} on top of {@link
 * ConcurrentEvictingQueue}
 **/
public class ConcurrentCircularFifoBuffer<T> implements CircularFifoBuffer<T> {

    private final ConcurrentEvictingQueue<T> queue;
    private final int capacity;

    /**
     * Creates an {@code ConcurrentCircularFifoBuffer} with the given (fixed) capacity
     *
     * @param capacity the capacity of this {@code ConcurrentCircularFifoBuffer}
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ConcurrentCircularFifoBuffer(int capacity) {
        this.capacity = capacity;
        queue = new ConcurrentEvictingQueue<>(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> toList() {
        return new ArrayList<>(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<T> toStream() {
        return queue.stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(T element) {
        queue.offer(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<T> take() {
        return Optional.ofNullable(queue.poll());
    }
}