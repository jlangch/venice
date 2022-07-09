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
package com.github.jlangch.venice.impl.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.github.jlangch.venice.VncException;


public abstract class ManagedExecutor {

    public ManagedExecutor(final Supplier<ExecutorService> supplier) {
        this.supplier = supplier;
    }


    public ExecutorService getExecutor() {
        synchronized(this) {
            if (executor == null) {
                executor = supplier.get();
            }
            return executor;
        }
    }

    public final boolean exists() {
        synchronized(this) {
            return executor != null;
        }
    }

    public final boolean isShutdown() {
        synchronized(this) {
            return executor == null ? true : executor.isShutdown();
        }
    }

    public final void awaitTermination(final long timeoutMillis) {
        synchronized(this) {
            if (executor != null) {
                try {
                    executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
                }
                catch(Exception ex) {
                    throw new VncException(
                            "Failed awaiting for executor termination",
                            ex);
                }
            }
        }
    }

    public final boolean isTerminated() {
        synchronized(this) {
            return executor == null ? true : executor.isTerminated();
        }
    }

    public final void shutdown() {
        synchronized(this) {
            if (executor != null) {
                try {
                    executor.shutdown();
                }
                catch(Exception ex) {
                    // silently
                }
                finally {
                    executor = null;
                }
            }
        }
    }

    public final void shutdownNow() {
        synchronized(this) {
            if (executor != null) {
                try {
                    executor.shutdownNow();
                }
                catch(Exception ex) {
                    // silently
                }
                finally {
                    executor = null;
                }
            }
        }
    }


    private final Supplier<ExecutorService> supplier;

    private ExecutorService executor;
}
