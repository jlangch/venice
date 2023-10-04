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
package com.github.jlangch.venice.impl.util;

import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncKeyword;


public class TimeUnitUtil {

    public static TimeUnit toTimeUnit(final VncKeyword unit) {
        switch(unit.getValue()) {
            case "milli":        return TimeUnit.MILLISECONDS;
            case "millis":       return TimeUnit.MILLISECONDS;
            case "milliseconds": return TimeUnit.MILLISECONDS;

            case "second":       return TimeUnit.SECONDS;
            case "seconds":      return TimeUnit.SECONDS;

            case "minute":       return TimeUnit.MINUTES;
            case "minutes":      return TimeUnit.MINUTES;

            case "hour":         return TimeUnit.HOURS;
            case "hours":        return TimeUnit.HOURS;

            case "day":          return TimeUnit.DAYS;
            case "days":         return TimeUnit.DAYS;

            default:
                throw new VncException("Invalid scheduler time-unit " + unit.getValue());
        }
    }

}
