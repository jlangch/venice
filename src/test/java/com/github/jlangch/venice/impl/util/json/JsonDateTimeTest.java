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
package com.github.jlangch.venice.impl.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;


public class JsonDateTimeTest {

    @Test
    public void test_format_ISO() {
        final LocalDate ld = LocalDate.of(2023, 10, 25);
        final LocalTime lt = LocalTime.of(10, 45, 30, 851000000);
        final LocalTime lt0 = LocalTime.of(10, 45, 30, 0);
        final ZoneId zoneId = ZoneId.of("Europe/Paris");

        assertEquals("2023-10-25", JsonDateTime.formatISO(ld));

        assertEquals("2023-10-25T10:45:30.851", JsonDateTime.formatISO(LocalDateTime.of(ld, lt)));
        assertEquals("2023-10-25T10:45:30", JsonDateTime.formatISO(LocalDateTime.of(ld, lt0)));

        assertEquals("2023-10-25T10:45:30.851+02:00", JsonDateTime.formatISO(ZonedDateTime.of(ld, lt, zoneId)));
    }

    @Test
    public void test_parse_ISO() {
        assertEquals("2023-10-25", JsonDateTime.parseISO_LocalDate("2023-10-25").toString());

        assertEquals("2023-10-25T10:45:30.851", JsonDateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.851").toString());
        assertEquals("2023-10-25T10:45:30.850", JsonDateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.85").toString());
        assertEquals("2023-10-25T10:45:30.800", JsonDateTime.parseISO_LocalDateTime("2023-10-25T10:45:30.8").toString());
        assertEquals("2023-10-25T10:45:30", JsonDateTime.parseISO_LocalDateTime("2023-10-25T10:45:30").toString());

        assertEquals("2023-10-25T10:45:30.851+02:00", JsonDateTime.parseISO_ZonedDateTime("2023-10-25T10:45:30.851+02:00").toString());
    }
}
