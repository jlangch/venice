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
package com.github.jlangch.venice.impl.util;


/**
 * A stop watch
 */
public class NanoStopWatch {

    /**
     * Create a new nano seconds based stop watch. Start time is now
     */
    public NanoStopWatch() {
        this.startTime = currentTime();
    }

    /**
     * Restart the stop watch setting the start time to now
     *
     * @return this stop watch
     */
    public NanoStopWatch start() {
        startTime = currentTime();
        return this;
    }

    /**
     * <p>Stops the stop watch setting the elapsed time to now - start time.
     *
     * <p>A stop watch may be stopped multiple times each time setting the
     * elapsed time to now - start time.
     *
     * @return this stop watch
     */
    public NanoStopWatch stop() {
        elapsedTime = splitTime();
        return this;
    }

    /**
     * Returns the elapsed time of this stop watch.
     *
     * @return the elapsed time or 0 if the watch has not been stopped.
     */
    public long elapsedMillis() {
        return elapsedTime;
    }

    /**
     * Returns the elapsed time of this stop watch.
     *
     * @return the elapsed time or 0 if the watch has not been stopped.
     */
    public long elapsedNanos() {
        return elapsedTime;
    }

    /**
     * Returns the split time
     *
     * @return the split time
     */
    public long splitTime() {
        // Take care for system clock adjustments that the elapsed time does not get negative
        return Math.max(0L, currentTime() - startTime);
    }

    @Override
    public String toString() {
        return formatNanos(elapsedTime);
    }

    /**
     * Formats an elapsed time given in milliseconds
     *
     * <p>Formats chosen
     * <ul>
     *   <li>245ms - for elapsed times &lt; 1s</li>
     *   <li>45s 245ms - for elapsed times &lt; 1m</li>
     *   <li>10m 45s - for elapsed times &gt;= 1m</li>
     * </ul>
     *
     * @param millis a duration in milliseconds
     *
     * @return the formatted time
     */
    public static String formatMillis(final long millis) {
        if (millis < 1000L) {
            return String.format("%dms", millis);
        }
        else if (millis < 60_000L){
            return String.format("%ds %dms", millis / 1000L, millis % 1000L);
        }
        else {
            final long seconds = millis / 1000L;
            return String.format("%dm %ds", seconds / 60L, seconds % 60L);
        }
    }

    /**
     * Formats an elapsed time given in nanoseconds
     *
     * <p>Formats chosen
     * <ul>
     *   <li>245ns - for elapsed times &lt; 1us</li>
     *   <li>45.245us - for elapsed times &lt; 1ms</li>
     *   <li>45.245ms- for elapsed times &lt; 1s</li>
     *   <li>45.245s - for elapsed times &gt; 1s</li>
     * </ul>
     *
     * @param nanos a duration in nanoseconds
     *
     * @return the formatted time
     */
    public static String formatNanos(final long nanos) {
        if (nanos < 1000L) {
            return String.format("%dns", nanos);
        }
        else if (nanos < 1_000_000L){
            return String.format("%.3fus", nanos / 1000F);
        }
        else if (nanos < 1_000_000_000L){
            final long usecs = nanos / 1_000L;
            return String.format("%.3fms", usecs / 1000F);
        }
        else {
            final long millis = nanos / 1_000_000L;
            return String.format("%.3fs", millis / 1000F);
        }
    }


    private long currentTime() {
        return System.nanoTime();
    }


    private long startTime = 0L;
    private long elapsedTime = 0L;
}
