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
package com.github.jlangch.venice.impl.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;


public class TimeUtil {

    public static Date convertLocalDateToDate(final LocalDate date) {
        return new Date(date.atTime(0, 0, 0)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli());
    }

    public static Date convertLocalDateTimeToDate(final LocalDateTime date) {
        return new Date(date.atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli());
    }

    public static Date convertZonedDateTimeToDate(final ZonedDateTime date) {
        return new Date(date.toInstant()
                            .toEpochMilli());
    }

    public static LocalDate convertDateToLocalDate(final Date date) {
        final long millis = date.getTime();
        return Instant.ofEpochMilli(millis)
                      .atZone(ZoneId.systemDefault())
                      .toLocalDate();
    }

    public static LocalDate convertLocalDateTimeToLocalDate(final LocalDateTime date) {
        return date.toLocalDate();
    }

    public static LocalDate convertZonedDateTimeToLocalDate(final ZonedDateTime date) {
        return date.toLocalDate();
    }


    public static LocalDateTime convertDateToLocalDateTime(final Date date) {
        final long millis = date.getTime();
        return Instant.ofEpochMilli(millis)
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime();
    }

    public static LocalDateTime convertLocalDateTimeToLocalDateTime(final LocalDate date) {
        return date.atTime(0, 0, 0);
    }

    public static LocalDateTime convertZonedDateTimeToLocalDateTime(final ZonedDateTime date) {
        return date.toLocalDateTime();
    }

}
