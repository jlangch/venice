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
package com.github.jlangch.venice.impl.types.concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.impl.types.TypeRank;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncLock extends VncVal implements AutoCloseable {

    public VncLock() {
        this.semaphore = new Semaphore(1);
    }


    public VncLock lock() {
        semaphore.acquireUninterruptibly();
        return this;
    }

    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public boolean tryAcquire(final long timeout, final TimeUnit unit) {
        try {
            return semaphore.tryAcquire(timeout, unit);
        }
        catch(InterruptedException ex) {
            throw new com.github.jlangch.venice.InterruptedException(
                    "Interrupted while acquiring lock");
        }
    }

    public boolean isLocked() {
        return this.semaphore.availablePermits() < 1;
    }

    @Override
    public void close() {
        unlock();
    }

    public void unlock() {
        semaphore.release();
    }


    @Override
    public VncVal withMeta(VncVal meta) {
        return this;
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(TYPE);
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.LOCK;
    }

    @Override
    public Object convertToJavaObject() {
        return semaphore;
    }



    private static final long serialVersionUID = 1L;

    public static final String TYPE = ":core/lock";

    private final Semaphore semaphore;
}