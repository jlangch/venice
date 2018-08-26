/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import org.junit.Test;


public class TimeFunctionsTest {

	@Test
	public void test_date() {
		final Venice venice = new Venice();
		
		assertTrue(venice.eval("(time/date)") instanceof Date);		
		assertTrue(venice.eval("(time/date (time/date))") instanceof Date);		
		assertTrue(venice.eval("(time/date 798797979)") instanceof Date);		
		assertTrue(venice.eval("(time/date (time/local-date))") instanceof Date);		
		assertTrue(venice.eval("(time/date (time/local-date-time))") instanceof Date);		
		assertTrue(venice.eval("(time/date (time/zoned-date-time))") instanceof Date);
	}

	@Test
	public void test_date_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(time/date? (time/date))"));
	}

	@Test
	public void test_local_date() {
		final Venice venice = new Venice();
		
		assertTrue(venice.eval("(time/local-date)") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date 798797979)") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date \"2018-08-01\")") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date 2018 8 1)") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date (time/local-date))") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date (time/local-date-time))") instanceof LocalDate);		
		assertTrue(venice.eval("(time/local-date (time/zoned-date-time))") instanceof LocalDate);
	}

	@Test
	public void test_local_date_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(time/local-date? (time/local-date))"));
	}

	@Test
	public void test_local_date_time() {
		final Venice venice = new Venice();
		
		assertTrue(venice.eval("(time/local-date-time)") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time 798797979)") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time \"2018-08-01T10:15:30\")") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time 2018 8 1 10 15 30)") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time (time/local-date))") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time (time/local-date-time))") instanceof LocalDateTime);		
		assertTrue(venice.eval("(time/local-date-time (time/zoned-date-time))") instanceof LocalDateTime);
	}

	@Test
	public void test_local_date_time_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(time/local-date-time? (time/local-date-time))"));
	}

	@Test
	public void test_zoned_date_time() {
		final Venice venice = new Venice();
		
		assertTrue(venice.eval("(time/zoned-date-time)") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time 798797979)") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time \"2018-08-01T10:15:30.980+01:00\")") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time 2018 8 1 10 15 30 980)") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time (time/local-date))") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time (time/local-date-time))") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time (time/zoned-date-time))") instanceof ZonedDateTime);

		assertTrue(venice.eval("(time/zoned-date-time :UTC)") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC 798797979)") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC \"2018-08-01T10:15:30.980+01:00\")") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC 2018 8 1 10 15 30 980\")") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC (time/local-date))") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC (time/local-date-time))") instanceof ZonedDateTime);		
		assertTrue(venice.eval("(time/zoned-date-time :UTC (time/zoned-date-time))") instanceof ZonedDateTime);
	}

	@Test
	public void test_zoned_date_time_Q() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(time/zoned-date-time? (time/zoned-date-time))"));
	}

	@Test
	public void test_zone_ids() {
		final Venice venice = new Venice();
		
		assertTrue(((Map<?,?>)venice.eval("(time/zone-ids)")).size() > 0);
	}

}
