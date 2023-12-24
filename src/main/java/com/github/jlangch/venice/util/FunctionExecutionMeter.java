/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.Collection;

import com.github.jlangch.venice.impl.util.MeterRegistry;


/**
 * Provides access to Venice's collected runtime execution times for
 * functions.
 *
 * <p>Its mainly used for unit testing
 *
 * @author juerg
 */
public class FunctionExecutionMeter {

    public FunctionExecutionMeter(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    /**
     * Enables the collection of runtime execution time for functions
     */
    public void enable() {
        meterRegistry.enable();
    }

    /**
     * Disables the collection of runtime execution time for functions
     */
    public void disable() {
        meterRegistry.disable();
    }

    /**
     * Removes all collected runtime execution times for functions
     */
    public void reset() {
        meterRegistry.reset();
    }

    /**
     * @return the collected runtime execution times for functions
     */
    public Collection<ElapsedTime> getData() {
        return meterRegistry.getTimerData();
    }

    /**
     * Formats the execution times
     *
     * @param title a title
     * @return formatted collected runtime execution times for functions
     */
    public String getDataFormatted(final String title) {
        return meterRegistry.getTimerDataFormatted(title, false);
    }


    private final MeterRegistry meterRegistry;
}
