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
package com.github.jlangch.venice.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * A thread safe stop watch
 *
 * <pre>
 * final StopWatch sw = new StopWatch();
 * // some processing
 * final long elapsedMillis = sw.stop().elapsedMillis()
 * </pre>
 *
 * @author juerg
 */
public class StopWatch {

    /**
     * Create a new stop watch. Start time is now
     */
    public StopWatch() {
        this(System.currentTimeMillis(), 0L, 0L);
    }

    /**
     * Create a new stop watch with a time limit. Start time is now.
     *
     * <p>
     * The stop watch has the same functionality as a standard stop watch.
     * But in addition the time limit can be checked with
     * {@link #hasExpired() hasExpired}.
     *
     * @param unit The unit of the time limit
     * @param n The time limit value
     */
    public StopWatch(final TimeUnit unit, final long n) {
        this(
            System.currentTimeMillis(),
            0L,
            unit == null ? n
                         : MILLISECONDS.convert(n, unit));
    }


    private StopWatch(
            final long startTime,
            final long elapsedTime,
            final long limitMilliseconds
    ) {
        this.startTime = new AtomicLong(startTime);
        this.elapsedTime = new AtomicLong(elapsedTime);
        this.limitTime = new AtomicLong(startTime + limitMilliseconds);
    }


    /**
     * Returns a copy this stop watch
     *
     * @return this stop watch
     */
    public StopWatch copy() {
        return new StopWatch(startTime.get(), elapsedTime.get(), limitTime.get());
    }

    /**
     * Restart the stop watch setting the start time to now, setting the
     * elapsed time to 0.
     *
     * @return this stop watch
     */
    public StopWatch start() {
        startTime = new AtomicLong(System.currentTimeMillis());
        elapsedTime = new AtomicLong(0L);
        return this;
    }

    /**
     * Restart the stop watch setting the start time to now, keeping the
     * elapsed time.
     *
     * @return this stop watch
     */
    public StopWatch resume() {
        startTime = new AtomicLong(System.currentTimeMillis());
        return this;
    }

    /**
     * <p>Stops the stop watch adding to the elapsed time <code>now - start time</code>.
     *
     * <p>A stop watch may be resumed and stopped multiple times.
     *
     * @return this stop watch
     */
    public StopWatch stop() {
        elapsedTime.addAndGet(splitTime());
        startTime = new AtomicLong(System.currentTimeMillis());
        return this;
    }

    /**
     * Returns the elapsed time of this stop watch.
     *
     * @param unit The unit of the elapsed time if <code>null</code> milliseconds are used
     * @return the elapsed time or 0 if the watch has not been stopped.
     */
    public long elapsed(final TimeUnit unit) {
        return unit == null
                ? elapsedTime.get()
                : unit.convert(elapsedTime.get(), MILLISECONDS);
    }

    /**
     * Returns the split time regarding a time unit
     *
     * @param unit The unit of the split time if <code>null</code> milliseconds are used
     * @return the split time
     */
    public long splitTime(final TimeUnit unit) {
        return unit == null
                    ? splitTime()
                    : unit.convert(splitTime(), MILLISECONDS);
    }

    /**
     * Returns the split time in milliseconds
     *
     * @return the split time
     */
    public long splitTime() {
        // Take care for system clock adjustments that the elapsed time does not get negative
        return Math.max(0, System.currentTimeMillis() - startTime.get());
    }

    /**
     * Returns the formatted elapsed time of this stop watch.
     *
     * @return the formatted elapsed time or 0 if the watch has not been stopped.
     */
    public String elapsedMillisFormatted() {
        return formatMillis(elapsedTime.get());
    }


    /**
     * @return true if the stop has expired, meaning it exceeded the time limit
     */
    public boolean hasExpired() {
        final long limit = limitTime.get();
        return limit == 0L ? false : System.currentTimeMillis() > limit;
    }


    @Override
    public String toString() {
        return formatMillis(elapsedTime.get());
    }

    /**
     * Formats an elapsed time given in milliseconds
     *
     * <p>Formats chosen
     * <ul>
     *   <li>245ms - for elapsed times &lt; 1s</li>
     *   <li>45s 245ms - for elapsed times &lt; 1h</li>
     *   <li>10m 45s - for elapsed times &gt;= 1h</li>
     * </ul>
     *
     * @param millis a duration in milliseconds
     *
     * @return the formatted time
     */
    public static String formatMillis(final long millis) {
        if (millis < 1000) {
            return String.format("%dms", millis);
        }
        else if (millis < 60000){
            return String.format("%ds %dms", millis / 1000, millis % 1000);
        }
        else {
            final long seconds = millis / 1000;
            return String.format("%dm %ds", seconds / 60, seconds % 60);
        }
    }


    private AtomicLong startTime = new AtomicLong(0L);
    private AtomicLong elapsedTime = new AtomicLong(0L);
    private AtomicLong limitTime = new AtomicLong(0L);
}
