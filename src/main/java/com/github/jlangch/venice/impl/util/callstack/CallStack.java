/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util.callstack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Defines a call stack for function calls that represents the nested order
 * of function calls
 */
public class CallStack {

    public CallStack() {
        this.queue = new ArrayDeque<>(32);
    }

    private CallStack(final ArrayDeque<CallFrame> queue) {
        this.queue = queue;
    }

    /**
     * Pushes a frame onto the call stack.
     *
     * @param frame the frame to push
     * @throws NullPointerException if the specified frame is null
     */
    public void push(final CallFrame frame) {
        queue.push(frame);
    }

    /**
     * Pops an frame from the call stack.
     *
     * @return the head of this call stack, or {@code null} if this
     *         if this call stack is empty
     */
    public CallFrame pop() {
        return isEmpty() ? null : queue.pop();
    }

    /**
     * Retrieves, but does not remove, the head of the call stack
     * or returns {@code null} if this call stack is empty.
     *
     * @return the head of this call stack, or {@code null} if this
     *         if this call stack is empty
     */
    public CallFrame peek() {
        return queue.peek();
    }

    /**
     * Returns {@code true} if this call stack contains no elements.
     *
     * @return {@code true} if this call stack contains no elements
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns the call stack's size (depth)
     *
     * @return the call stack's size (depth)
     */
    public int size() {
        return queue.size();
    }

    /**
     * Removes all of the elements from this call stack.
     * The call stack will be empty after this call returns.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * @return a copy of this call stack
     */
    public CallStack copy() {
        return new CallStack(queue.clone());
    }

    /**
     * Returns a list of stringified call frames in this call stack.  The elements
     * will be ordered from first (head) to last (tail). This is the same
     * order that elements would be dequeued (via successive calls to {@link #pop}).
     * The head element will be stored at index 0 in the list.
     *
     * @return a list of stringified call frames in this call stack
     */
    public List<String> toList() {
        final List<String> list = new ArrayList<>(queue.size());
        for(CallFrame f : queue) {
            list.add(f.toString());
        }
        return list;
    }

    /**
     * Returns a list of call frames in this call stack.  The elements
     * will be ordered from first (head) to last (tail). This is the same
     * order that elements would be dequeued (via successive calls to {@link #pop}).
     * The head element will be stored at index 0 in the list.
     *
     * @return a list of call frames in this call stack
     */
    public List<CallFrame> callstack() {
        return Arrays.asList(queue.toArray(new CallFrame[queue.size()]));
    }

    /**
     * Checks if the any of call frames refers to the specified function name
     *
     * @param fnName A qualified function name
     * @param skipHead skip the head element
     * @return <code>true</code> if any of the call frames refers to the
     *         specified function name else <code>false</code>
     */
    public boolean hasAnyAncestor(final String fnName, final boolean skipHead) {
        final Iterator<CallFrame> iter = queue.iterator();

        if (skipHead && iter.hasNext()) {
            iter.next();
        }

        while (iter.hasNext()) {
            final CallFrame cf = iter.next();
            if (cf.hasFnName(fnName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the top call frame refers to the specified function name
     *
     * @param fnName A qualified function name
     * @param skipHead skip the head element
     * @return <code>true</code> if if the top call frame refers to the
     *         specified function name else <code>false</code>
     */
    public boolean hasNearestAncestor(final String fnName, final boolean skipHead) {
        final Iterator<CallFrame> iter = queue.iterator();

        if (skipHead && iter.hasNext()) {
            iter.next();
        }

        if (iter.hasNext()) {
            final CallFrame cf = iter.next();
            return cf.hasFnName(fnName);
        }

        return false;
    }


    @Override
    public String toString() {
        return toList()
                .stream()
                .collect(Collectors.joining("\n"));
    }


    // A call stack is used only as a thread local variable. So it does
    // not face concurrent usage. ArrayDeque is the fastest Deque available.
    private final ArrayDeque<CallFrame> queue;
}
