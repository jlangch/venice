/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Defines a named total elapsed time for profiling functions
 */
public class ElapsedTime implements Serializable {
    public ElapsedTime(final String name, final long elapsedNanos) {
        this.name = name;
        this.count.set(1);
        this.elapsedNanos.set(elapsedNanos);
    }

    public ElapsedTime(final String name, final int count, final long elapsedNanos) {
        this.name = name;
        this.count.set(count);
        this.elapsedNanos.set(elapsedNanos);
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count.get();
    }

    public long getElapsedNanos() {
        return elapsedNanos.get();
    }

    public ElapsedTime add(final long elapsedNanos) {
        this.count.incrementAndGet();
        this.elapsedNanos.addAndGet(elapsedNanos);
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "%s [%d]: %s",
                name,
                count.get(),
                formatNanos(elapsedNanos.get()));
    }

    public static String formatNanos(final long nanos) {
        if (nanos < 1_000L) {
            return Long.valueOf(nanos).toString() + " ns";
        }
        else if (nanos < 1_000_000L) {
            return String.format("%.2f us", nanos / 1_000.0D);
        }
        else if (nanos < 9_000_000_000L) {
            return String.format("%.2f ms", nanos / 1_000_000.0D);
        }
        else {
            return String.format("%.2f s ", nanos / 1_000_000_000.0D);
        }
    }


    private final String name;
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicLong elapsedNanos = new AtomicLong(0L);

    private static final long serialVersionUID = -1;
}
