/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


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
	public void test_local_date_parse() {
		final Venice venice = new Venice();
		
		assertEquals("2018-12-01", venice.eval("(str (time/local-date-parse \"2018-12-01\" \"yyyy-MM-dd\"))"));		
		assertEquals("2018-12-01", venice.eval("(str (time/local-date-parse \"2018-Dec-01\" \"yyyy-MMM-dd\" :ENGLISH))"));		
		
		// TODO: why does this fail?
		//  see: https://medium.com/better-programming/localization-changes-in-java-9-c05ffde8cc2f	
		//assertEquals("2018-12-01", venice.eval("(str (time/local-date-parse \"2018-Dez-01\" \"yyyy-MMM-dd\" :GERMAN))"));	
		
		// works on JDK 11
		//assertEquals("2018-12-01", venice.eval("(str (time/local-date-parse \"2018 Dez. 01\" \"yyyy MMM dd\" :de))"));
		//assertEquals("2018-12-01", venice.eval("(str (time/local-date-parse \"2018 Dez. 01\" \"yyyy MMM dd\" :de_DE))"));
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
	public void test_local_date_time_parse() {
		final Venice venice = new Venice();
		
		assertEquals("2018-08-01T10:15:30", venice.eval("(str (time/local-date-time-parse \"2018-08-01 10:15:30\" \"yyyy-MM-dd HH:mm:ss\"))"));		
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
		assertTrue(venice.eval("(time/zoned-date-time :UTC 2018 8 1 10 15 30 980)") instanceof ZonedDateTime);		
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
	public void test_zoned_date_time_parse() {
		final Venice venice = new Venice();
		
		assertEquals("2018-08-01T10:15:30+01:00", venice.eval("(str (time/zoned-date-time-parse \"2018-08-01T10:15:30+01:00\" \"yyyy-MM-dd'T'HH:mm:ssz\"))"));		
	}

	@Test
	public void test_zone_ids() {
		final Venice venice = new Venice();
		
		assertTrue(((Map<?,?>)venice.eval("(time/zone-ids)")).size() > 0);
	}

	@Test
	public void test_equal() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval(
				"(== (time/local-date \"2018-08-01\")" +
				"    (time/local-date \"2018-08-01\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(== (time/local-date-time \"2018-08-01T10:15:30.980\")" +
				"    (time/local-date-time \"2018-08-01T10:15:30.980\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(== (time/zoned-date-time \"2018-08-01T10:15:30.980+01:00\")" +
				"    (time/zoned-date-time \"2018-08-01T10:15:30.980+01:00\"))"));		
	}

	@Test
	public void test_within() {
		final Venice venice = new Venice();
		
		assertFalse((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-09\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-10\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-15\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-20\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertFalse((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-21\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              (time/local-date \"2018-08-20\"))"));	

		
		// lower bound nil
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-10\")" +
				"              nil" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-20\")" +
				"              nil" +	
				"              (time/local-date \"2018-08-20\"))"));		
		
		assertFalse((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-21\")" +
				"              nil" +	
				"              (time/local-date \"2018-08-20\"))"));		

		
		// upper bound nil
		
		assertFalse((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-01\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              nil)"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-10\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              nil)"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-20\")" +
				"              (time/local-date \"2018-08-10\")" +	
				"              nil)"));		

		
		// lower & upper bound nil
		
		assertTrue((Boolean)venice.eval(
				"(time/within? (time/local-date \"2018-08-01\") nil nil)"));		
	}

	@Test
	public void test_before() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/local-date \"2018-08-09\")" +
				"              (time/local-date \"2018-08-10\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/local-date-time \"2018-08-09T10:00:00.000\")" +
				"              (time/local-date-time \"2018-08-10T10:00:00.000\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\")" +
				"              (time/zoned-date-time \"2018-08-10T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_not_before() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/local-date \"2018-08-10\")" +
				"                  (time/local-date \"2018-08-09\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/local-date-time \"2018-08-10T10:00:00.000\")" +
				"                  (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/zoned-date-time \"2018-08-10T10:00:00.000+01:00\")" +
				"                  (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_after() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/local-date \"2018-08-10\")" +
				"                  (time/local-date \"2018-08-09\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/local-date-time \"2018-08-10T10:00:00.000\")" +
				"                  (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/not-before? (time/zoned-date-time \"2018-08-10T10:00:00.000+01:00\")" +
				"                  (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_not_after() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/local-date \"2018-08-09\")" +
				"              (time/local-date \"2018-08-10\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/local-date-time \"2018-08-09T10:00:00.000\")" +
				"              (time/local-date-time \"2018-08-10T10:00:00.000\"))"));		
		
		assertTrue((Boolean)venice.eval(
				"(time/before? (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\")" +
				"              (time/zoned-date-time \"2018-08-10T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_year() {
		final Venice venice = new Venice();
		
		assertEquals(2018L, venice.eval("(time/year (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(2018L, venice.eval("(time/year (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(2018L, venice.eval("(time/year (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_month() {
		final Venice venice = new Venice();
		
		assertEquals(8L, venice.eval("(time/month (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(8L, venice.eval("(time/month (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(8L, venice.eval("(time/month (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_day_of_year() {
		final Venice venice = new Venice();
		
		assertEquals(221L, venice.eval("(time/day-of-year (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(221L, venice.eval("(time/day-of-year (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(221L, venice.eval("(time/day-of-year (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_day_of_month() {
		final Venice venice = new Venice();
		
		assertEquals(9L, venice.eval("(time/day-of-month (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(9L, venice.eval("(time/day-of-month (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(9L, venice.eval("(time/day-of-month (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_day_of_week() {
		final Venice venice = new Venice();
		
		assertEquals("THURSDAY", venice.eval("(time/day-of-week (time/local-date \"2018-08-09\"))"));		
		
		assertEquals("THURSDAY", venice.eval("(time/day-of-week (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals("THURSDAY", venice.eval("(time/day-of-week (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_hour() {
		final Venice venice = new Venice();
		
		assertEquals(0L, venice.eval("(time/hour (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(10L, venice.eval("(time/hour (time/local-date-time \"2018-08-09T10:20:30.400\"))"));		
		
		assertEquals(10L, venice.eval("(time/hour (time/zoned-date-time \"2018-08-09T10:20:30.400+00:00\"))"));		
	}

	@Test
	public void test_minute() {
		final Venice venice = new Venice();
		
		assertEquals(0L, venice.eval("(time/minute (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(20L, venice.eval("(time/minute (time/local-date-time \"2018-08-09T10:20:30.400\"))"));		
		
		assertEquals(20L, venice.eval("(time/minute (time/zoned-date-time \"2018-08-09T10:20:30.400+00:00\"))"));		
	}

	@Test
	public void test_second() {
		final Venice venice = new Venice();
		
		assertEquals(0L, venice.eval("(time/second (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(30L, venice.eval("(time/second (time/local-date-time \"2018-08-09T10:20:30.400\"))"));		
		
		assertEquals(30L, venice.eval("(time/second (time/zoned-date-time \"2018-08-09T10:20:30.400+00:00\"))"));		
	}

	@Test
	public void test_length_of_year() {
		final Venice venice = new Venice();
		
		assertEquals(365L, venice.eval("(time/length-of-year (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(365L, venice.eval("(time/length-of-year (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(365L, venice.eval("(time/length-of-year (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_length_of_month() {
		final Venice venice = new Venice();
		
		assertEquals(31L, venice.eval("(time/length-of-month (time/local-date \"2018-08-09\"))"));		
		
		assertEquals(31L, venice.eval("(time/length-of-month (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertEquals(31L, venice.eval("(time/length-of-month (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

	@Test
	public void test_leap_year() {
		final Venice venice = new Venice();
		
		assertTrue((Boolean)venice.eval("(time/leap-year? (time/local-date \"2016-08-09\"))"));		
		
		assertTrue((Boolean)venice.eval("(time/leap-year? (time/local-date-time \"2016-08-09T10:00:00.000\"))"));		
		
		assertTrue((Boolean)venice.eval("(time/leap-year? (time/zoned-date-time \"2016-08-09T10:00:00.000+01:00\"))"));		

		
		assertFalse((Boolean)venice.eval("(time/leap-year? (time/local-date \"2018-08-09\"))"));		
		
		assertFalse((Boolean)venice.eval("(time/leap-year? (time/local-date-time \"2018-08-09T10:00:00.000\"))"));		
		
		assertFalse((Boolean)venice.eval("(time/leap-year? (time/zoned-date-time \"2018-08-09T10:00:00.000+01:00\"))"));		
	}

}
