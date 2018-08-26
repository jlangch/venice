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
import static com.github.jlangch.venice.impl.types.Constants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
		
			return new VncJavaObject(LocalDate.parse(date.getValue(), formatter.withLocale(locale)));
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
					"(time/local-date-time-parse \"2018-08-01\" \"yyyy-MM-dd\")");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/local-date-time-parse", args, 2, 3);
			
			final VncString date = Coerce.toVncString(args.first());
			final DateTimeFormatter formatter = getDateTimeFormatter(args.second());
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
		
			return new VncJavaObject(LocalDateTime.parse(date.getValue(), formatter.withLocale(locale)));
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
			
			ZoneId zoneId = ZoneId.systemDefault();
			VncList argList = args;
			if (args.size() > 0) {
				final VncVal val = args.first();
				if (Types.isVncKeyword(val)) {
					zoneId = ZoneId.of(((VncKeyword)val).getValue());
					argList = args.slice(1);
				}
			}
			if (argList.size() == 0) {
				return new VncJavaObject(ZonedDateTime.now(zoneId));
			}
			else if (argList.size() == 1) {
				final VncVal val = argList.first();
				if (Types.isVncJavaObject(val)) {
					final Object obj = ((VncJavaObject)val).getDelegate();
					if (obj instanceof Date) {
						final long millis = ((Date)obj).getTime();
						return new VncJavaObject(
										Instant.ofEpochMilli(millis)
											   .atZone(zoneId));
					}
					else if (obj instanceof ZonedDateTime) {
						return new VncJavaObject(((ZonedDateTime)obj).withZoneSameInstant(zoneId));
					}
					else if (obj instanceof LocalDateTime) {
						return new VncJavaObject(((LocalDateTime)obj).atZone(zoneId));
					}
					else if (obj instanceof LocalDate) {
						return new VncJavaObject( ((LocalDate)obj).atTime(0, 0, 0).atZone(zoneId));						
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
					return new VncJavaObject(ZonedDateTime.parse(s));
				}
				else if (Types.isVncLong(val)) {
					final long millis = ((VncLong)val).getValue();
					return new VncJavaObject(
									Instant.ofEpochMilli(millis)
										   .atZone(ZoneId.systemDefault()));
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
							zoneId));
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
							zoneId));
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
							zoneId));
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
					"(time/zoned-date-time-parse \"2018-08-01\" \"yyyy-MM-dd\")");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/zoned-date-time-parse", args, 2, 3);
			
			final VncString date = Coerce.toVncString(args.first());
			final DateTimeFormatter formatter = getDateTimeFormatter(args.second());
			final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;
		
			return new VncJavaObject(ZonedDateTime.parse(date.getValue(), formatter.withLocale(locale)));
		}
	};
	

		
	///////////////////////////////////////////////////////////////////////////
	// Formatter
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction formatter = new VncFunction("time/formatter") {
		{
			setArgLists("(time/formatter format locale?)");
			
			setDoc("Creates a formatter");
			
			setExamples("(time/formatter \"dd-MM-yyyy\")");
		}
		public VncVal apply(final VncList args) {
			assertArity("time/formatter", args, 1, 2);
				
			// locale
			final Locale locale = args.size() == 2 ? getLocale(args.nth(1)) : null;
			
			// formatter
			return new VncJavaObject(
					locale == null
						? getDateTimeFormatter(args.first())
						: getDateTimeFormatter(args.first()).withLocale(locale));
		}
	};

	public static VncFunction format = new VncFunction("time/format") {
		{
			setArgLists("(time/format date format locale?)");
			
			setDoc("Formats a date with a format");
			
			setExamples("(time/format (time/local-date) \"dd-MM-yyyy\")");
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
			final DateTimeFormatter formatter = locale == null
													? getDateTimeFormatter(args.second())
													: getDateTimeFormatter(args.second()).withLocale(locale);
			
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
			
			setExamples("(time/zone-ids)");
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
		if (Types.isVncString(fmt)) {
			return DateTimeFormatter.ofPattern(((VncString)fmt).getValue());
		}
		else if (Types.isVncKeyword(fmt)) {
			return getPredefinedDateTimeFormatter((VncKeyword)fmt);
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
				.put("time/format",					format)
							
				.toMap();	
}
