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
package com.github.jlangch.venice.impl.types;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.collections.VncCollection;
import com.github.jlangch.venice.impl.types.collections.VncMutable;
import com.github.jlangch.venice.impl.util.MetaUtil;


/**
 * A synchronization point at which threads can pair and swap elements
 * within pairs. Each thread presents some object on entry to the
 * exchange function, matches with a partner thread,
 * and receives its partner's object on return.  An Exchanger may be
 * viewed as a bidirectional form of a synchronous queue.
 * Exchangers may be useful in applications such as genetic algorithms
 * and pipeline designs.
 */
public class VncExchanger extends VncVal implements VncMutable {

    public VncExchanger(final VncVal meta) {
        super(meta);
        this.exchanger = new Exchanger<>();
    }

    private VncExchanger(final Exchanger<VncVal> exchanger, final VncVal meta) {
        super(meta);
        this.exchanger = exchanger;
    }


    @Override
    public VncExchanger withMeta(final VncVal meta) {
        return new VncExchanger(exchanger, meta);
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
    public void clear() {
        // no-op
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted}),
     * and then transfers the given object to it, receiving its object
     * in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of two things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread interrupts the current thread.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is interrupted while waiting for the exchange,
     * </ul>
     * then {@link com.github.jlangch.venice.InterruptedException} is
     * thrown and the current thread's interrupted status is cleared.
     *
     * @param x the object to exchange
     * @return the object provided by the other thread
     * @throws com.github.jlangch.venice.InterruptedException if the
     *         current thread was interrupted while waiting
     */
    public VncVal exchange(final VncVal x)  {
        try {
            return exchanger.exchange(x);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (exchange! e val)",
                        ex);
        }
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@linkplain Thread#interrupt interrupted} or
     * the specified waiting time elapses), and then transfers the given
     * object to it, receiving its object in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread.  The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of three things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread interrupts the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is interrupted while waiting for the exchange,
     * </ul>
     * then {@link com.github.jlangch.venice.InterruptedException} is
     * thrown and the current thread's interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link
     * com.github.jlangch.venice.TimeoutException} is thrown.  If the
     * time is less than or equal to zero, the method will not wait at all.
     *
     * @param x the object to exchange
     * @param timeoutMillis the maximum time to wait
     * @return the object provided by the other thread
     * @throws com.github.jlangch.venice.InterruptedException if the
     *         current thread was interrupted while waiting
     * @throws com.github.jlangch.venice.TimeoutException if the specified
     *         waiting time elapses before another thread enters the exchange
     */
    public VncVal exchange(final VncVal x, final long timeoutMillis)  {
        try {
            return exchanger.exchange(x, timeoutMillis, TimeUnit.MILLISECONDS);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                        "interrupted while calling (exchange! e val timeout)",
                        ex);
        }
        catch(TimeoutException ex) {
            throw new com.github.jlangch.venice.TimeoutException();
        }
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.EXCHANGER;
    }

    @Override
    public Object convertToJavaObject() {
        throw new VncException("Cannot convert an exchanger to a Java object!");
    }

    @Override
    public String toString() {
        return TYPE;
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return TYPE;
    }




    public static final String TYPE = ":core/exchanger";

    private static final long serialVersionUID = -564531670922145260L;

    private final Exchanger<VncVal> exchanger;
}
