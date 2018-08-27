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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.types.Constants.False;
import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.Constants.True;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncJavaObject;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;



public class TimeFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Date
	///////////////////////////////////////////////////////////////////////////
 	
	public static VncFunction date = new VncFunction("time/date") {
		{
			setArgLists("(time/date)", "(time/date x)");
			
			setDoc("Creates a new date. A date is represented by 'java.util.Date'");
			
			setExamples("(time/date)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/date", args, 0, 1);

			if (args.size() == 0) {
				return new VncJavaObject(new Date());
			}
			else {
				final VncVal val = args.first();
				if (Types.isVncLong(val)) {
					final long millis = ((VncLong)val).getValue();
					return new VncJavaObject(new Date(millis));
				}
				else if (Types.isVncJavaObject(val)) {
					final Object date = ((VncJavaObject)val).getDelegate();
					if (date instanceof Date) {
						return new VncJavaObject(new Date(((Date)date).getTime()));
					}
					else if (date instanceof LocalDate) {
						return new VncJavaObject(new Date(
													((LocalDate)date)
														.atTime(0, 0, 0)
														.atZone(ZoneId.systemDefault())
														.toInstant()
														.toEpochMilli()));
					}
					else if (date instanceof LocalDateTime) {
						return new VncJavaObject(new Date(
													((LocalDateTime)date)
														.atZone(ZoneId.systemDefault())
														.toInstant()
														.toEpochMilli()));
					}
					else if (date instanceof ZonedDateTime) {
						return new VncJavaObject(new Date(((ZonedDateTime)date).toInstant().toEpochMilli()));
					}
				}
				
				throw new VncException(String.format(
						"Function 'time/date' does not allow %s as parameter. %s", 
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	
	public static VncFunction date_Q = new VncFunction("time/date?") {
		{
			setArgLists("(time/date? date)");
			
			setDoc("Returns true if date is a date else false");
			
			setExamples("(time/date? (time/date))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/date?", args, 1);
	
			final VncVal val = args.first();
			return Types.isVncJavaObject(val) 
					&& ((VncJavaObject)val).getDelegate() instanceof Date ? True : False;
		}
	};

	

	///////////////////////////////////////////////////////////////////////////
	// LocalDate
	///////////////////////////////////////////////////////////////////////////
 	
	public static VncFunction local_date = new VncFunction("time/local-date") {
		{
			setArgLists("(time/local-date)", "(time/local-date year month day)", "(time/local-date date)");
			
			setDoc("Creates a new local-date. A local-date is represented by 'java.time.LocalDate'");
			
			setExamples(
					"(time/local-date)",
					"(time/local-date 2018 8 1)",
					"(time/local-date \"2018-08-01\")",
					"(time/local-date 1375315200000)",
					"(time/local-date (. :java.util.Date :new))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date", args, 0, 1, 3);
			
			if (args.size() == 0) {
				return new VncJavaObject(LocalDate.now());
			}
			else if (args.size() == 1) {
				final VncVal val = args.first();
				if (Types.isVncJavaObject(val)) {
					final Object obj = ((VncJavaObject)val).getDelegate();
					if (obj instanceof Date) {
						final long millis = ((Date)obj).getTime();
						return new VncJavaObject(
										Instant.ofEpochMilli(millis)
											   .atZone(ZoneId.systemDefault())
											   .toLocalDate());
					}
					else if (obj instanceof ZonedDateTime) {
						return new VncJavaObject(((ZonedDateTime)obj).toLocalDate());
					}
					else if (obj instanceof LocalDateTime) {
						return new VncJavaObject(((LocalDateTime)obj).toLocalDate());						
					}
					else if (obj instanceof LocalDate) {
						return val;
					}	
					else {
						throw new VncException(String.format(
								"Function 'time/local-date' does not allow %s as parameter. %s", 
								Types.getClassName(val),
								ErrorMessage.buildErrLocation(args)));
					}
				}
				else if (Types.isVncString(val)) {
					// ISO local date format "yyyy-mm-dd"
					final String s = ((VncString)val).getValue();
					return new VncJavaObject(LocalDate.parse(s));
				}
				else if (Types.isVncLong(val)) {
					final long millis = ((VncLong)val).getValue();
					return new VncJavaObject(
									Instant.ofEpochMilli(millis)
										   .atZone(ZoneId.systemDefault())
										   .toLocalDate());
				}
				else {
					throw new VncException(String.format(
							"Function 'time/local-date' does not allow %s as parameter. %s", 
							Types.getClassName(val),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else {
				return new VncJavaObject(
							LocalDate.of(
								Coerce.toVncLong(args.nth(0)).getValue().intValue(),
								Coerce.toVncLong(args.nth(1)).getValue().intValue(),
								Coerce.toVncLong(args.nth(2)).getValue().intValue()));
			}
		}
	};
	
	public static VncFunction local_date_Q = new VncFunction("time/local-date?") {
		{
			setArgLists("(time/local-date? date)");
			
			setDoc("Returns true if date is a locale date else false");
			
			setExamples("(time/local-date? (time/local-date))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date?", args, 1);
			
			final VncVal val = args.first();
			return Types.isVncJavaObject(val) 
					&& ((VncJavaObject)val).getDelegate() instanceof LocalDate ? True : False;
		}
	};

	public static VncFunction local_date_parse = new VncFunction("time/local-date-parse") {
		{
			setArgLists("(time/local-date-parse str format locale?");
			
			setDoc("Parses a local-date.");
			
			setExamples(
					"(time/local-date-parse \"2018-08-01\" \"yyyy-MM-dd\")");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date-parse", args, 2, 3);
			
			final VncString date = Coerce.toVncString(args.first());
			final DateTimeFormatter formatter = getDateTimeFormatter(args.second());
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
		
			return new VncJavaObject(LocalDate.parse(date.getValue(), localize(formatter, locale)));
		}
	};
	
	

	///////////////////////////////////////////////////////////////////////////
	// LocalDateTime
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction local_date_time = new VncFunction("time/local-date-time") {
		{
			setArgLists(
					"(time/local-date-time)", 
					"(time/local-date-time year month day)", 
					"(time/local-date-time year month day hour minute second)", 
					"(time/local-date-time year month day hour minute second millis)", 
					"(time/local-date-time date)");
			
			setDoc("Creates a new local-date-time. A local-date-time is represented by 'java.time.LocalDateTime'");
			
			setExamples(
					"(time/local-date-time)",
					"(time/local-date-time 2018 8 1)",
					"(time/local-date-time 2018 8 1 14 20 10)",
					"(time/local-date-time 2018 8 1 14 20 10 200)",
					"(time/local-date-time \"2018-08-01T14:20:10.200\")",
					"(time/local-date-time 1375315200000)",
					"(time/local-date-time (. :java.util.Date :new))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date-time", args, 0, 1, 3, 6, 7);
			
			if (args.size() == 0) {
				return new VncJavaObject(LocalDateTime.now());
			}
			else if (args.size() == 1) {
				final VncVal val = args.first();
				if (Types.isVncJavaObject(val)) {
					final Object obj = ((VncJavaObject)val).getDelegate();
					if (obj instanceof Date) {
						final long millis = ((Date)obj).getTime();
						return new VncJavaObject(
										Instant.ofEpochMilli(millis)
											   .atZone(ZoneId.systemDefault())
											   .toLocalDateTime());
					}
					else if (obj instanceof ZonedDateTime) {
						return new VncJavaObject(((ZonedDateTime)obj).toLocalDateTime());
					}
					else if (obj instanceof LocalDateTime) {
						return val;
					}
					else if (obj instanceof LocalDate) {
						return new VncJavaObject( ((LocalDate)obj).atTime(0, 0, 0));						
					}	
					else {
						throw new VncException(String.format(
								"Function 'time/local-date-time' does not allow %s as parameter. %s", 
								Types.getClassName(val),
								ErrorMessage.buildErrLocation(args)));
					}
				}
				else if (Types.isVncString(val)) {
					// ISO local date format "yyyy-mm-ddThh:MM:ss.SSS"
					final String s = ((VncString)val).getValue();
					return new VncJavaObject(LocalDateTime.parse(s));
				}
				else if (Types.isVncLong(val)) {
					final long millis = ((VncLong)val).getValue();
					return new VncJavaObject(
									Instant.ofEpochMilli(millis)
										   .atZone(ZoneId.systemDefault())
										   .toLocalDateTime());
				}
				else {
					throw new VncException(String.format(
							"Function 'time/local-date-time' does not allow %s as parameter. %s", 
							Types.getClassName(val),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else if (args.size() == 3) {
				return new VncJavaObject(
						LocalDateTime.of(
							Coerce.toVncLong(args.nth(0)).getValue().intValue(),
							Coerce.toVncLong(args.nth(1)).getValue().intValue(),
							Coerce.toVncLong(args.nth(2)).getValue().intValue(),
							0, 0, 0, 0));
			}
			else if (args.size() == 6) {
				return new VncJavaObject(
						LocalDateTime.of(
							Coerce.toVncLong(args.nth(0)).getValue().intValue(),
							Coerce.toVncLong(args.nth(1)).getValue().intValue(),
							Coerce.toVncLong(args.nth(2)).getValue().intValue(),
							Coerce.toVncLong(args.nth(3)).getValue().intValue(),
							Coerce.toVncLong(args.nth(4)).getValue().intValue(),
							Coerce.toVncLong(args.nth(5)).getValue().intValue(),
							0));
			}
			else {
				return new VncJavaObject(
						LocalDateTime.of(
							Coerce.toVncLong(args.nth(0)).getValue().intValue(),
							Coerce.toVncLong(args.nth(1)).getValue().intValue(),
							Coerce.toVncLong(args.nth(2)).getValue().intValue(),
							Coerce.toVncLong(args.nth(3)).getValue().intValue(),
							Coerce.toVncLong(args.nth(4)).getValue().intValue(),
							Coerce.toVncLong(args.nth(5)).getValue().intValue(),
							Coerce.toVncLong(args.nth(6)).getValue().intValue()));
			}
		}
	};
	
	public static VncFunction local_date_time_Q = new VncFunction("time/local-date-time?") {
		{
			setArgLists("(time/local-date-time? date)");
			
			setDoc("Returns true if date is a local-date-time else false");
			
			setExamples("(time/local-date-time? (time/local-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date-time?", args, 1);
			
			final VncVal val = args.first();
			return Types.isVncJavaObject(val) 
					&& ((VncJavaObject)val).getDelegate() instanceof LocalDateTime ? True : False;
		}
	};

	public static VncFunction local_date_time_parse = new VncFunction("time/local-date-time-parse") {
		{
			setArgLists("(time/local-date-time-parse str format locale?");
			
			setDoc("Parses a local-date-time.");
			
			setExamples(
					"(time/local-date-time-parse \"2018-08-01 14:20\" \"yyyy-MM-dd HH:mm\")",
					"(time/local-date-time-parse \"2018-08-01 14:20:01.000\" \"yyyy-MM-dd HH:mm:ss.SSS\")");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date-time-parse", args, 2, 3);
			
			final VncString date = Coerce.toVncString(args.first());
			final DateTimeFormatter formatter = getDateTimeFormatter(args.second());
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
		
			return new VncJavaObject(LocalDateTime.parse(date.getValue(), localize(formatter, locale)));
		}
	};

	

	///////////////////////////////////////////////////////////////////////////
	// ZonedDateTime
	///////////////////////////////////////////////////////////////////////////
	
	public static VncFunction zoned_date_time = new VncFunction("time/zoned-date-time") {
		{
			setArgLists(
					"(time/zoned-date-time )", 
					"(time/zoned-date-time year month day)", 
					"(time/zoned-date-time year month day hour minute second)", 
					"(time/zoned-date-time year month day hour minute second millis)", 
					"(time/zoned-date-time date)",
					"(time/zoned-date-time zone-id)", 
					"(time/zoned-date-time zone-id year month day)", 
					"(time/zoned-date-time zone-id year month day hour minute second)", 
					"(time/zoned-date-time zone-id year month day hour minute second millis)", 
					"(time/zoned-date-time zone-id date)");
			
			setDoc("Creates a new zoned-date-time. A zoned-date-time is represented by 'java.time.ZonedDateTime'");
			
			setExamples(
					"(time/zoned-date-time)",
					"(time/zoned-date-time 2018 8 1)",
					"(time/zoned-date-time 2018 8 1 14 20 10)",
					"(time/zoned-date-time 2018 8 1 14 20 10 200)",
					"(time/zoned-date-time \"2018-08-01T14:20:10.200+01:00\")",
					"(time/zoned-date-time 1375315200000)",
					"(time/zoned-date-time (. :java.util.Date :new))",

					"(time/zoned-date-time :UTC)",
					"(time/zoned-date-time :UTC 2018 8 1)",
					"(time/zoned-date-time :UTC 2018 8 1 14 20 10)",
					"(time/zoned-date-time :UTC 2018 8 1 14 20 10 200)",
					"(time/zoned-date-time :UTC \"2018-08-01T14:20:10.200+01:00\")",
					"(time/zoned-date-time :UTC 1375315200000)",
					"(time/zoned-date-time :UTC (. :java.util.Date :new))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zoned-date-time", args, 0, 1, 2, 3, 4, 6, 7, 8);
			
			ZoneId zoneId = null;
			VncList argList = args;
			if (args.size() > 0) {
				final VncVal val = args.first();
				if (Types.isVncKeyword(val)) {
					zoneId = ZoneId.of(((VncKeyword)val).getValue());
					argList = args.slice(1);
				}
			}
			if (argList.size() == 0) {
				return new VncJavaObject(ZonedDateTime.now(orDefaultZone(zoneId)));
			}
			else if (argList.size() == 1) {
				final VncVal val = argList.first();
				if (Types.isVncJavaObject(val)) {
					final Object obj = ((VncJavaObject)val).getDelegate();
					if (obj instanceof Date) {
						final long millis = ((Date)obj).getTime();
						return new VncJavaObject(
										Instant.ofEpochMilli(millis)
											   .atZone(orDefaultZone(zoneId)));
					}
					else if (obj instanceof ZonedDateTime) {
						return new VncJavaObject(((ZonedDateTime)obj).withZoneSameInstant(orDefaultZone(zoneId)));
					}
					else if (obj instanceof LocalDateTime) {
						return new VncJavaObject(((LocalDateTime)obj).atZone(orDefaultZone(zoneId)));
					}
					else if (obj instanceof LocalDate) {
						return new VncJavaObject( ((LocalDate)obj).atTime(0, 0, 0).atZone(orDefaultZone(zoneId)));						
					}	
					else {
						throw new VncException(String.format(
								"Function 'time/zoned-date-time' does not allow %s as parameter. %s", 
								Types.getClassName(val),
								ErrorMessage.buildErrLocation(args)));
					}
				}
				else if (Types.isVncString(val)) {
					// ISO local date format "yyyy-mm-ddThh:MM:ss.SSS"
					final String s = ((VncString)val).getValue();
					return new VncJavaObject(ZonedDateTime.parse(
												s, 
												zone(DateTimeFormatter.ISO_ZONED_DATE_TIME, zoneId)));
				}
				else if (Types.isVncLong(val)) {
					final long millis = ((VncLong)val).getValue();
					return new VncJavaObject(
									Instant.ofEpochMilli(millis)
										   .atZone(orDefaultZone(zoneId)));
				}
				else {
					throw new VncException(String.format(
							"Function 'time/zoned-date-time' does not allow %s as parameter. %s", 
							Types.getClassName(val),
							ErrorMessage.buildErrLocation(args)));
				}
			}
			else if (argList.size() == 3) {
				return new VncJavaObject(
						ZonedDateTime.of(
							Coerce.toVncLong(argList.nth(0)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(1)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(2)).getValue().intValue(),
							0, 0, 0, 0, 
							orDefaultZone(zoneId)));
			}
			else if (argList.size() == 6) {
				return new VncJavaObject(
						ZonedDateTime.of(
							Coerce.toVncLong(argList.nth(0)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(1)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(2)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(3)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(4)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(5)).getValue().intValue(),
							0, 
							orDefaultZone(zoneId)));
			}
			else {
				return new VncJavaObject(
						ZonedDateTime.of(
							Coerce.toVncLong(argList.nth(0)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(1)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(2)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(3)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(4)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(5)).getValue().intValue(),
							Coerce.toVncLong(argList.nth(6)).getValue().intValue(),
							orDefaultZone(zoneId)));
			}
		}
	};
	
	public static VncFunction zoned_date_time_Q = new VncFunction("time/zoned-date-time?") {
		{
			setArgLists("(time/zoned-date-time? date)");
			
			setDoc("Returns true if date is a zoned-date-time else false");
			
			setExamples("(time/zoned-date-time? (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zoned-date-time?", args, 1);
			
			final VncVal val = args.first();
			return Types.isVncJavaObject(val) 
					&& ((VncJavaObject)val).getDelegate() instanceof ZonedDateTime ? True : False;
		}
	};

	public static VncFunction zoned_date_time_parse = new VncFunction("time/zoned-date-time-parse") {
		{
			setArgLists("(time/zoned-date-time-parse str format locale?");
			
			setDoc("Parses a zoned-date-time.");
			
			setExamples(
					"(time/zoned-date-time-parse \"2018-08-01T14:20:01+01:00\" \"yyyy-MM-dd'T'HH:mm:ssz\")",
					"(time/zoned-date-time-parse \"2018-08-01T14:20:01.000+01:00\" \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
					"(time/zoned-date-time-parse \"2018-08-01T14:20:01.000+01:00\" :ISO_OFFSET_DATE_TIME)",
					"(time/zoned-date-time-parse \"2018-08-01 14:20:01.000 +01:00\" \"yyyy-MM-dd' 'HH:mm:ss.SSS' 'z\")"
					);
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zoned-date-time-parse", args, 2, 3);
			
			final VncString date = Coerce.toVncString(args.first());
			final DateTimeFormatter formatter = getDateTimeFormatter(args.second());
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
		
			return new VncJavaObject(ZonedDateTime.parse(date.getValue(), localize(formatter, locale)));
		}
	};
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// Compare
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction after = new VncFunction("time/after") {
		{
			setArgLists("(time/after date1 date2)");
			
			setDoc("Returns true if date1 is after date2 else false");
			
			setExamples(
					"(time/after (time/local-date) (time/minus (time/local-date) :days 2))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/after", args, 2);
				
			final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
			final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();
				
			if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
				return ((ZonedDateTime)date1).isAfter((ZonedDateTime)date2) ? True : False;
			}
			else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
				return ((LocalDateTime)date1).isAfter((LocalDateTime)date2) ? True : False;
			}
			else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
				return ((LocalDate)date1).isAfter((LocalDate)date2) ? True : False;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/after' does not allow %s %s as date1 / date2 parameter. %s", 
						Types.getClassName(args.first()),
						Types.getClassName(args.second()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction not_after = new VncFunction("time/not-after") {
		{
			setArgLists("(time/not-after date1 date2)");
			
			setDoc("Returns true if date1 is not-after date2 else false");
			
			setExamples(
					"(time/not-after (time/local-date) (time/minus (time/local-date) :days 2))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/not-after", args, 2);
				
			final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
			final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();
				
			if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
				return ((ZonedDateTime)date1).isAfter((ZonedDateTime)date2) ? False : True;
			}
			else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
				return ((LocalDateTime)date1).isAfter((LocalDateTime)date2) ? False : True;
			}
			else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
				return ((LocalDate)date1).isAfter((LocalDate)date2) ? False : True;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/not-after' does not allow %s %s as date1 / date2 parameter. %s", 
						Types.getClassName(args.first()),
						Types.getClassName(args.second()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction before = new VncFunction("time/before") {
		{
			setArgLists("(time/before date1 date2)");
			
			setDoc("Returns true if date1 is before date2 else false");
			
			setExamples(
					"(time/before (time/local-date) (time/minus (time/local-date) :days 2))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/before", args, 2);
				
			final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
			final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();
				
			if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
				return ((ZonedDateTime)date1).isBefore((ZonedDateTime)date2) ? True : False;
			}
			else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
				return ((LocalDateTime)date1).isBefore((LocalDateTime)date2) ? True : False;
			}
			else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
				return ((LocalDate)date1).isBefore((LocalDate)date2) ? True : False;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/before' does not allow %s %s as date1 / date2 parameter. %s", 
						Types.getClassName(args.first()),
						Types.getClassName(args.second()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction not_before = new VncFunction("time/not-before") {
		{
			setArgLists("(time/not-before date1 date2)");
			
			setDoc("Returns true if date1 is not-before date2 else false");
			
			setExamples(
					"(time/not-before (time/local-date) (time/minus (time/local-date) :days 2))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/not-before", args, 2);
				
			final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
			final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();
				
			if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
				return ((ZonedDateTime)date1).isBefore((ZonedDateTime)date2) ? False : True;
			}
			else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
				return ((LocalDateTime)date1).isBefore((LocalDateTime)date2) ? False : True;
			}
			else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
				return ((LocalDate)date1).isBefore((LocalDate)date2) ? False : True;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/not-before' does not allow %s %s as date1 / date2 parameter. %s", 
						Types.getClassName(args.first()),
						Types.getClassName(args.second()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	
	///////////////////////////////////////////////////////////////////////////
	// Plus/Minus
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction plus = new VncFunction("time/plus") {
		{
			setArgLists("(time/plus date unit n)");
			
			setDoc("Adds the n units to the date. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}");
			
			setExamples(
					"(time/plus (time/local-date) :days 2)",
					"(time/plus (time/local-date-time) :days 2)",
					"(time/plus (time/zoned-date-time) :days 2)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/plus", args, 3);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.second()).getValue());
			final long n = Coerce.toVncLong(args.nth(2)).getValue();
			
			if (unit == null) {
				throw new VncException(String.format(
						"Function 'time/plus' invalid time unit %s. %s", 
						Coerce.toVncKeyword(args.second()).getValue(),
						ErrorMessage.buildErrLocation(args)));
			}
			
			if (date instanceof ZonedDateTime) {
				return new VncJavaObject(((ZonedDateTime)date).plus(n, unit));
			}
			else if (date instanceof LocalDateTime) {
				return new VncJavaObject(((LocalDateTime)date).plus(n, unit));
			}
			else if (date instanceof LocalDate) {
				return new VncJavaObject(((LocalDate)date).plus(n, unit));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/plus' does not allow %s as date parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction minus = new VncFunction("time/minus") {
		{
			setArgLists("(time/minus date unit n)");
			
			setDoc("Subtracts the n units from the date. Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}");
			
			setExamples(
					"(time/minus (time/local-date) :days 2)",
					"(time/minus (time/local-date-time) :days 2)",
					"(time/minus (time/zoned-date-time) :days 2)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/minus", args, 3);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.second()).getValue());
			final long n = Coerce.toVncLong(args.nth(2)).getValue();
			
			if (unit == null) {
				throw new VncException(String.format(
						"Function 'time/minus' invalid time unit %s. %s", 
						Coerce.toVncKeyword(args.second()).getValue(),
						ErrorMessage.buildErrLocation(args)));
			}
			
			if (date instanceof ZonedDateTime) {
				return new VncJavaObject(((ZonedDateTime)date).minus(n, unit));
			}
			else if (date instanceof LocalDateTime) {
				return new VncJavaObject(((LocalDateTime)date).minus(n, unit));
			}
			else if (date instanceof LocalDate) {
				return new VncJavaObject(((LocalDate)date).minus(n, unit));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/minus' does not allow %s as date parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};


	public static VncFunction period = new VncFunction("time/period") {
		{
			setArgLists("(time/period from to unit)");
			
			setDoc( "Returns the period interval of two dates in the specified unit. " +
					"Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}");
			
			setExamples(
					"(time/period (time/local-date) (time/plus (time/local-date) :days 3) :days)",
					"(time/period (time/local-date-time) (time/plus (time/local-date-time) :days 3) :days)",
					"(time/period (time/zoned-date-time) (time/plus (time/zoned-date-time) :days 3) :days)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/period", args, 3);
			
			final Object from = Coerce.toVncJavaObject(args.first()).getDelegate();
			final Object to = Coerce.toVncJavaObject(args.second()).getDelegate();
			final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.nth(2)).getValue());
			
			if (unit == null) {
				throw new VncException(String.format(
						"Function 'time/period' invalid time unit %s. %s", 
						Coerce.toVncKeyword(args.second()).getValue(),
						ErrorMessage.buildErrLocation(args)));
			}
				
			if (from instanceof ZonedDateTime && to instanceof ZonedDateTime) {
				return new VncLong(unit.between((ZonedDateTime)from, (ZonedDateTime)to));
			}
			else if (from instanceof LocalDateTime && to instanceof LocalDateTime) {
				return new VncLong(unit.between((LocalDateTime)from, (LocalDateTime)to));
			}
			else if (from instanceof LocalDate && to instanceof LocalDate) {
				return new VncLong(unit.between((LocalDate)from, (LocalDate)to));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/period' does not allow %s %s as from / to parameter. %s", 
						Types.getClassName(args.first()),
						Types.getClassName(args.second()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// Fields
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction year = new VncFunction("time/year") {
		{
			setArgLists("(time/year date)");
			
			setDoc("Returns the year of the date");
			
			setExamples(
					"(time/year (time/local-date))",
					"(time/year (time/local-date-time))",
					"(time/year (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/year", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getYear());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getYear());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(((LocalDate)date).getYear());
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/year' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction month = new VncFunction("time/month") {
		{
			setArgLists("(time/month date)");
			
			setDoc("Returns the month of the date 1..12");
			
			setExamples(
					"(time/month (time/local-date))",
					"(time/month (time/local-date-time))",
					"(time/month (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/month", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getMonth().getValue());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getMonth().getValue());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(((LocalDate)date).getMonth().getValue());
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/month' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	public static VncFunction day_of_week = new VncFunction("time/day-of-week") {
		{
			setArgLists("(time/day-of-week date)");
			
			setDoc("Returns the day of the week (:MONDAY ... :SUNDAY)");
			
			setExamples(
					"(time/day-of-week (time/local-date))",
					"(time/day-of-week (time/local-date-time))",
					"(time/day-of-week (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/day-of-week", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncKeyword(((ZonedDateTime)date).getDayOfWeek().name());
			}
			else if (date instanceof LocalDateTime) {
				return new VncKeyword(((LocalDateTime)date).getDayOfWeek().name());
			}
			else if (date instanceof LocalDate) {
				return new VncKeyword(((LocalDate)date).getDayOfWeek().name());
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/day-of-week' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction day_of_month = new VncFunction("time/day-of-month") {
		{
			setArgLists("(time/day-of-month date)");
			
			setDoc("Returns the day of the month (1..31)");
			
			setExamples(
					"(time/day-of-month (time/local-date))",
					"(time/day-of-month (time/local-date-time))",
					"(time/day-of-month (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/day-of-month", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getDayOfMonth());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getDayOfMonth());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(((LocalDate)date).getDayOfMonth());
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/day-of-month' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction day_of_year = new VncFunction("time/day-of-year") {
		{
			setArgLists("(time/day-of-year date)");
			
			setDoc("Returns the day of the year (1..366)");
			
			setExamples(
					"(time/day-of-year (time/local-date))",
					"(time/day-of-year (time/local-date-time))",
					"(time/day-of-year (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/day-of-year", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getDayOfYear());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getDayOfYear());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(((LocalDate)date).getDayOfYear());
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/day-of-year' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction first_day_of_month = new VncFunction("time/first-day-of-month") {
		{
			setArgLists("(time/first-day-of-month date)");
			
			setDoc("Returns the first day of a month as a local-date.");
			
			setExamples(
					"(time/first-day-of-month (time/local-date))",
					"(time/first-day-of-month (time/local-date-time))",
					"(time/first-day-of-month (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/first-day-of-month", args, 1);
				
			final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (dt instanceof ZonedDateTime) {
				final LocalDate date = ((ZonedDateTime)dt).toLocalDateTime().toLocalDate();
				return new VncJavaObject(date.withDayOfMonth(1));
			}
			else if (dt instanceof LocalDateTime) {
				final LocalDate date = ((LocalDateTime)dt).toLocalDate();
				return new VncJavaObject(date.withDayOfMonth(1));
			}
			else if (dt instanceof LocalDate) {
				final LocalDate date = ((LocalDate)dt);
				return new VncJavaObject(date.withDayOfMonth(1));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/first-day-of-month' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction last_day_of_month = new VncFunction("time/last-day-of-month") {
		{
			setArgLists("(time/last-day-of-month date)");
			
			setDoc("Returns the last day of a month as a local-date.");
			
			setExamples(
					"(time/last-day-of-month (time/local-date))",
					"(time/last-day-of-month (time/local-date-time))",
					"(time/last-day-of-month (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/last-day-of-month", args, 1);
				
			final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (dt instanceof ZonedDateTime) {
				final LocalDate date = ((ZonedDateTime)dt).toLocalDateTime().toLocalDate();
				return new VncJavaObject(date.withDayOfMonth(date.lengthOfMonth()));
			}
			else if (dt instanceof LocalDateTime) {
				final LocalDate date = ((LocalDateTime)dt).toLocalDate();
				return new VncJavaObject(date.withDayOfMonth(date.lengthOfMonth()));
			}
			else if (dt instanceof LocalDate) {
				final LocalDate date = ((LocalDate)dt);
				return new VncJavaObject(date.withDayOfMonth(date.lengthOfMonth()));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/last-day-of-month' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	LocalDate initial = LocalDate.of(2014, 2, 13);
	LocalDate start = initial.withDayOfMonth(1);
	LocalDate end = initial.withDayOfMonth(initial.lengthOfMonth());
	
	public static VncFunction hour = new VncFunction("time/hour") {
		{
			setArgLists("(time/hour date)");
			
			setDoc("Returns the hour of the date 1..24");
			
			setExamples(
					"(time/hour (time/local-date))",
					"(time/hour (time/local-date-time))",
					"(time/hour (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/hour", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getHour());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getHour());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(0);
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/hour' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction minute = new VncFunction("time/minute") {
		{
			setArgLists("(time/minute date)");
			
			setDoc("Returns the minute of the date 0..59");
			
			setExamples(
					"(time/minute (time/local-date))",
					"(time/minute (time/local-date-time))",
					"(time/minute (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/minute", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getMinute());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getMinute());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(0);
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/minute' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction second = new VncFunction("time/second") {
		{
			setArgLists("(time/second date)");
			
			setDoc("Returns the second of the date 0..59");
			
			setExamples(
					"(time/second (time/local-date))",
					"(time/second (time/local-date-time))",
					"(time/second (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/second", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getSecond());
			}
			else if (date instanceof LocalDateTime) {
				return new VncLong(((LocalDateTime)date).getSecond());
			}
			else if (date instanceof LocalDate) {
				return new VncLong(0);
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/second' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction zone = new VncFunction("time/zone") {
		{
			setArgLists("(time/zone date)");
			
			setDoc("Returns the zone of the date");
			
			setExamples("(time/zone (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zone", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncKeyword(((ZonedDateTime)date).getZone().getId());
			}
			else if (date instanceof LocalDateTime) {
				return Nil;
			}
			else if (date instanceof LocalDate) {
				return Nil;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/zone' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
	
	public static VncFunction zone_offset = new VncFunction("time/zone-offset") {
		{
			setArgLists("(time/zone-offset date)");
			
			setDoc("Returns the zone-offset of the date in minutes");
			
			setExamples("(time/zone-offset (time/zoned-date-time))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zone-offset", args, 1);
				
			final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();
			
			if (date instanceof ZonedDateTime) {
				return new VncLong(((ZonedDateTime)date).getOffset().getTotalSeconds() / 60);
			}
			else if (date instanceof LocalDateTime) {
				return Nil;
			}
			else if (date instanceof LocalDate) {
				return Nil;
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/zone-offset' does not allow %s as parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// Formatter
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction formatter = new VncFunction("time/formatter") {
		{
			setArgLists("(time/formatter format locale?)");
			
			setDoc("Creates a formatter");
			
			setExamples(
					"(time/formatter \"dd-MM-yyyy\")",
					"(time/formatter \"dd-MM-yyyy\" :en_EN)",
					"(time/formatter \"dd-MM-yyyy\" \"en_EN\")",
					"(time/formatter \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
					"(time/formatter :ISO_OFFSET_DATE_TIME)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/formatter", args, 1, 2);
				
			// locale
			final Locale locale = args.size() == 2 ? getLocale(args.nth(1)) : null;
			
			// formatter
			return new VncJavaObject(localize(getDateTimeFormatter(args.first()), locale));
		}
	};

	public static VncFunction format = new VncFunction("time/format") {
		{
			setArgLists("(time/format date format locale?)");
			
			setDoc("Formats a date with a format");
			
			setExamples(
					"(time/format (time/local-date) \"dd-MM-yyyy\")",
					"(time/format (time/zoned-date-time) \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
					"(time/format (time/zoned-date-time) :ISO_OFFSET_DATE_TIME)",
					"(time/format (time/zoned-date-time) (time/formatter \"yyyy-MM-dd'T'HH:mm:ss.SSSz\"))",
					"(time/format (time/zoned-date-time) (time/formatter :ISO_OFFSET_DATE_TIME))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/format", args, 2, 3);
	
			if (!Types.isVncJavaObject(args.first())) {
				throw new VncException(String.format(
						"Function 'time/format' does not allow %s as date parameter. %s", 
						Types.getClassName(date),
						ErrorMessage.buildErrLocation(args.first())));
			}
		
			// locale
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
			
			// formatter
			final DateTimeFormatter formatter = localize(getDateTimeFormatter(args.second()), locale);
			
			// format
			final Object date = ((VncJavaObject)args.first()).getDelegate();
			if (date instanceof Date) {
				final ZonedDateTime dt = Instant.ofEpochMilli(((Date)date).getTime())
												.atZone(ZoneId.systemDefault());
				return new VncString(dt.format(formatter));
			}
			else if (date instanceof ZonedDateTime) {
				return new VncString(((ZonedDateTime)date).format(formatter));
			}
			else if (date instanceof LocalDateTime) {
				return new VncString(((LocalDateTime)date).format(formatter));
			}
			else if (date instanceof LocalDate) {
				return new VncString(((LocalDate)date).format(formatter));
			}	
			else {
				throw new VncException(String.format(
						"Function 'time/format' does not allow %s as date parameter. %s", 
						Types.getClassName(args.first()),
						ErrorMessage.buildErrLocation(args)));
			}
		}
	};

	
	
	///////////////////////////////////////////////////////////////////////////
	// Misc
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction zone_ids = new VncFunction("time/zone-ids") {
		{
			setArgLists("(time/zone-ids)");
			
			setDoc("Returns all available zone ids with time offset");
			
			setExamples("(nfirst (seq (time/zone-ids)) 10)");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zone-ids", args, 0);
			
			final VncOrderedMap map = new VncOrderedMap();

			final List<String> zoneList = new ArrayList<>(ZoneId.getAvailableZoneIds());

			//Get all ZoneIds
			final Map<String, String> allZoneIds = getAllZoneIds(zoneList);

			//sort map by key
			allZoneIds
				.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.forEachOrdered(e -> map.assoc(
										new VncKeyword(e.getKey()), 
										new VncString(e.getValue())));

			//sort by value, descending order
			/*
			allZoneIds
				.entrySet()
				.stream()
				.sorted(Map.Entry.<String,String>comparingByValue()
				.reversed())
				.forEachOrdered(e -> map.assoc(
										new VncKeyword(e.getKey()), 
										new VncString(e.getValue())));
			*/

			return map;
		}
	};

	public static VncFunction to_millis = new VncFunction("time/to-millis") {
		{
			setArgLists("(time/to-millis date)");
			
			setDoc("Converts the passed date to milliseconds since epoch");
			
			setExamples("(time/to-millis (time/local-date))");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/to-millis", args, 1);
			
			final VncVal val = args.first();
			if (Types.isVncJavaObject(val)) {
				final Object date = ((VncJavaObject)val).getDelegate();
				if (date instanceof Date) {
					return new VncLong(((Date)date).getTime());
				}
				else if (date instanceof LocalDate) {
					return new VncLong(((LocalDate)date)
											.atTime(0, 0, 0)
											.atZone(ZoneId.systemDefault())
											.toInstant()
											.toEpochMilli());
				}
				else if (date instanceof LocalDateTime) {
					return new VncLong(((LocalDateTime)date)
											.atZone(ZoneId.systemDefault())
											.toInstant()
											.toEpochMilli());
				}
				else if (date instanceof ZonedDateTime) {
					return new VncLong(((ZonedDateTime)date).toInstant().toEpochMilli());
				}
			}

			throw new VncException(String.format(
						"Function 'time/to-millis' does not allow %s as parameter. %s", 
						Types.getClassName(val),
						ErrorMessage.buildErrLocation(args)));
		}
	};

		
	
	///////////////////////////////////////////////////////////////////////////
	// Utils
	///////////////////////////////////////////////////////////////////////////
	
	private static Map<String, String> getAllZoneIds(final List<String> zoneList) {
		final Map<String, String> result = new HashMap<>();
		final LocalDateTime dt = LocalDateTime.now();

		for (String zoneId : zoneList) {
			final ZoneId zone = ZoneId.of(zoneId);
			final ZonedDateTime zdt = dt.atZone(zone);
			final ZoneOffset zos = zdt.getOffset();

			//replace Z to +00:00
			final String offset = zos.getId().replaceAll("Z", "+00:00");

			result.put(zone.toString(), offset);
		}

		return result;
	}

	private static Locale getLocale(final VncVal locale) {
		if (locale == Nil) {
			return Locale.getDefault();
		}
		if (Types.isVncKeyword(locale)) {
			final String[] e = ((VncKeyword)locale).getValue().split("_");
			switch(e.length) {
				case 0: return Locale.getDefault();
				case 1: return new Locale(e[0]);
				case 2: return new Locale(e[0], e[1]);
				default: return new Locale(e[0], e[1], e[2]);
			}
		}
		if (Types.isVncString(locale)) {
			final String[] e = ((VncString)locale).getValue().split("_");
			switch(e.length) {
				case 0: return Locale.getDefault();
				case 1: return new Locale(e[0]);
				case 2: return new Locale(e[0], e[1]);
				default: return new Locale(e[0], e[1], e[2]);
			}
		}
		else if (Types.isVncJavaObject(locale)) {
			final Object obj = ((VncJavaObject)locale).getDelegate();
			if (obj instanceof Locale) {
				return (Locale)obj;
			}
		}
		
		throw new VncException(String.format(
				"The type %s does not define a Locale. %s", 
				Types.getClassName(locale),
				ErrorMessage.buildErrLocation(locale)));
	}

	private static DateTimeFormatter getDateTimeFormatter(final VncVal fmt) {
		if (Types.isVncKeyword(fmt)) {
			return getPredefinedDateTimeFormatter((VncKeyword)fmt);
		}
		else if (Types.isVncString(fmt)) {
			return DateTimeFormatter.ofPattern(((VncString)fmt).getValue());
		}
		else if (Types.isVncJavaObject(fmt)) {
			final Object fmtObj = ((VncJavaObject)fmt).getDelegate();
			if (fmtObj instanceof DateTimeFormatter) {
				return (DateTimeFormatter)fmtObj;
			}
			else {
				throw new VncException(String.format(
						"Function 'time/format' does not allow %s as format parameter. %s", 
						Types.getClassName(fmt),
						ErrorMessage.buildErrLocation(fmt)));
			}				
		}
		else {
			throw new VncException(String.format(
					"Function 'time/format' does not allow %s as format parameter. %s", 
					Types.getClassName(fmt),
					ErrorMessage.buildErrLocation(fmt)));
		}
	}
	
	private static DateTimeFormatter getPredefinedDateTimeFormatter(final VncKeyword fmt) {
		final String fmtName = fmt.getValue();
		try {
			return (DateTimeFormatter)ReflectionAccessor.getStaticField(DateTimeFormatter.class, fmtName);
		}
		catch(Exception ex) {
			throw new VncException(String.format(
					"'%s' is not a predefined DateTimeFormatter. %s", 
					fmtName,
					ErrorMessage.buildErrLocation(fmt)));
		}
	}

	private static DateTimeFormatter localize(
			final DateTimeFormatter formatter, 
			final Locale locale
	) {
		return locale == null ? formatter : formatter.withLocale(locale);
	}

	private static DateTimeFormatter zone(
			final DateTimeFormatter formatter, 
			final ZoneId zoneId
	) {
		return zoneId == null ? formatter : formatter.withZone(zoneId);
	}

	private static ZoneId orDefaultZone(final ZoneId zoneId) {
		return zoneId == null ? ZoneId.systemDefault() : zoneId;
	}

	private static ChronoUnit toChronoUnit(final String unit) {
		switch(unit) {
			case "years": return ChronoUnit.YEARS;
			case "month": return ChronoUnit.MONTHS;
			case "weeks": return ChronoUnit.WEEKS;
			case "days": return ChronoUnit.DAYS;
			case "hours": return ChronoUnit.HOURS;
			case "minutes": return ChronoUnit.MINUTES;
			case "seconds": return ChronoUnit.SECONDS;
			case "millis": return ChronoUnit.MILLIS;
			default: return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
				.put("time/date",						date)
				.put("time/date?",						date_Q)
				.put("time/local-date",					local_date)
				.put("time/local-date?",				local_date_Q)
				.put("time/local-date-parse",			local_date_parse)
				.put("time/local-date-time",			local_date_time)
				.put("time/local-date-time?",			local_date_time_Q)
				.put("time/local-date-time-parse",		local_date_time_parse)
				.put("time/zoned-date-time",			zoned_date_time)
				.put("time/zoned-date-time?",			zoned_date_time_Q)
				.put("time/zoned-date-time-parse",		zoned_date_time_parse)
				.put("time/zone-ids",					zone_ids)
				.put("time/to-millis",					to_millis)
				.put("time/formatter",					formatter)
				.put("time/format",						format)
				.put("time/plus",						plus)
				.put("time/minus",						minus)
				.put("time/period",						period)
				.put("time/year",						year)
				.put("time/month",						month)
				.put("time/day-of-week",				day_of_week)
				.put("time/day-of-month",				day_of_month)
				.put("time/day-of-year",				day_of_year)
				.put("time/first-day-of-month",			first_day_of_month)
				.put("time/last-day-of-month",			last_day_of_month)
				.put("time/hour",						hour)
				.put("time/minute",						minute)
				.put("time/second",						second)
				.put("time/zone",						zone)
				.put("time/zone-offset",				zone_offset)
				.put("time/after",						after)
				.put("time/not-after",					not_after)
				.put("time/before",						before)
				.put("time/not-before",					not_before)
						
				.toMap();	
}
