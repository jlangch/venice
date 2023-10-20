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
package com.github.jlangch.venice.impl.util.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class JsonDateTime {

    public static String formatISO(final LocalDate date) {
        return date == null ? null : date.format(FMT_LOCAL_DATE);
    }

    public static String formatISO(final LocalDateTime date) {
        return date == null ? null : date.format(FMT_LOCAL_DATE_TIME);
    }

    public static String formatISO(final ZonedDateTime date) {
        return date == null ? null : date.format(FMT_ZONED_DATE_TIME);
    }

    public static LocalDate parseISO_LocalDate(final String date) {
        return date == null ? null : LocalDate.parse(date, FMT_LOCAL_DATE);
    }

    public static LocalDateTime parseISO_LocalDateTime(final String date) {
        return date == null ? null : LocalDateTime.parse(date, FMT_LOCAL_DATE_TIME);
    }

    public static ZonedDateTime parseISO_ZonedDateTime(final String date) {
        return date == null ? null : ZonedDateTime.parse(date, FMT_ZONED_DATE_TIME);
    }


    private static final DateTimeFormatter FMT_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FMT_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter FMT_ZONED_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
}
