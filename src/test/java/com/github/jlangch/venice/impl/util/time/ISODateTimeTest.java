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
package com.github.jlangch.venice.impl.util.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.util.time.ISODateTime;


public class ISODateTimeTest {

    @Test
    public void test_format_ISO() {
        final LocalDate ld = LocalDate.of(2023, 10, 25);
        final LocalTime lt = LocalTime.of(10, 45, 30, 851000000);
        final LocalTime lt0 = LocalTime.of(10, 45, 30, 0);
        final ZoneId zoneId = ZoneId.of("Europe/Paris");

        assertEquals("2023-10-25", ISODateTime.formatISO(ld));

        assertEquals("2023-10-25T10:45:30.851", ISODateTime.formatISO(LocalDateTime.of(ld, lt)));
        assertEquals("2023-10-25T10:45:30", ISODateTime.formatISO(LocalDateTime.of(ld, lt0)));

        assertEquals("2023-10-25T10:45:30.851+02:00", ISODateTime.formatISO(ZonedDateTime.of(ld, lt, zoneId)));
    }

    @Test
    public void test_parse_ISO() {
        assertEquals("2023-10-25", ISODateTime.parseISO_LocalDate("2023-10-25").toString());

        assertEquals("2023-10-25T10:45:30.851", ISODateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.851").toString());
        assertEquals("2023-10-25T10:45:30.850", ISODateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.85").toString());
        assertEquals("2023-10-25T10:45:30.800", ISODateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.8").toString());
        assertEquals("2023-10-25T10:45:30", ISODateTime.parseISO_LocalDateTime("2023-10-25T10:45:30").toString());

        assertEquals("2023-10-25T10:45:30.851+02:00", ISODateTime.parseISO_ZonedDateTime("2023-10-25T10:45:30.851+02:00").toString());
    }
}
