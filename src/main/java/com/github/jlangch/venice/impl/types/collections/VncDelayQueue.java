/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MetaUtil;


/**
 * Provides a Java DelayQueue for Venice.
 *
 * <p>Delay queues can be used for building a simple rate limiter:
 *
 * <p> E.g.: Make sure that a method is executed no more than M
 * times in a sliding window of N seconds.
 *
 * <p>Initialize the delay queue with M delayed instances with their
 * delay initially set to zero. As requests to the method come in,
 * take a token from the queue, which causes the method to block until
 * the throttling requirement has been met. When a token has been taken,
 * add a new token to the queue with a delay of N.
 */
public class VncDelayQueue extends VncCollection implements VncMutable {

    public VncDelayQueue(final VncVal meta) {
        super(meta);
        this.queue = new DelayQueue<>();
    }

    private VncDelayQueue(final VncDelayQueue queue, final VncVal meta) {
        super(meta);
        this.queue = queue.queue;
    }


    @Override
    public VncCollection emptyWithMeta() {
        return new VncDelayQueue(getMeta());
    }

    @Override
    public VncDelayQueue withMeta(final VncVal meta) {
        return new VncDelayQueue(this, meta);
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
        return VncList.empty();
    }

    @Override
    public VncVector toVncVector() {
        return VncVector.empty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void put(
            final VncVal val,
            final long delay,
            final TimeUnit unit
    ) {
        queue.put(new DelayedVal(val, delay, unit));
    }

    public VncVal poll() {
        final DelayedVal val = queue.poll();
        return val == null ? Constants.Nil : val.deref();
    }

    public VncVal poll(final long timeoutMillis) {
        try {
            final DelayedVal val = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            return val == null ? Constants.Nil : val.deref();
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (poll! delay-queue)",
                        ex);
        }
    }

    public VncVal peek() {
        final DelayedVal val = queue.peek();
        return val == null ? Constants.Nil : val.deref();
    }

    public VncVal take() {
        try {
            final DelayedVal val = queue.take();
            return val == null ? Constants.Nil : val.deref();
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (take! delay-queue)",
                        ex);
        }
    }

    @Override
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
        return TYPE;
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return TYPE;
    }


    private static class DelayedVal implements IDeref, Delayed {
        public DelayedVal(
                final VncVal val,
                final long delay,
                final TimeUnit unit
        ) {
            this.val = val;
            this.delayMillis = unit.toMillis(delay);
            this.delayTo = System.currentTimeMillis() + this.delayMillis;
        }

        @Override
        public VncVal deref() {
            return val;
        }

        @Override
        public int compareTo(final Delayed o) {
            return saturatedCastToInt(delayTo - ((DelayedVal)o).delayTo);
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(
                        delayTo - System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS);
        }

        private final VncVal val;
        private final long delayTo;
        private final long delayMillis;
    }

    private static int saturatedCastToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        else if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        else {
            return (int)value;
        }
    }


    public static final String TYPE = ":core/delay-queue";

    private static final long serialVersionUID = -564531670922145260L;

    private final DelayQueue<DelayedVal> queue;
}
