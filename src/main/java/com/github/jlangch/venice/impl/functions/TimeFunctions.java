/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import static com.github.jlangch.venice.impl.types.Constants.Nil;
import static com.github.jlangch.venice.impl.types.VncBoolean.True;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.impl.util.time.TimeUtil;



public class TimeFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // Date
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction date =
        new VncFunction(
                "time/date",
                VncFunction
                    .meta()
                    .arglists("(time/date)", "(time/date x)")
                    .doc(
                        "Creates a new date of type 'java.util.Date'. \n" +
                        "x can be a long representing milliseconds since the epoch, " +
                        "a 'java.time.LocalDate', a 'java.time.LocalDateTime', " +
                        "or a 'java.time.ZonedDateTime'")
                    .examples("(time/date)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1);

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
                            return new VncJavaObject(TimeUtil.convertLocalDateToDate((LocalDate)date));
                        }
                        else if (date instanceof LocalDateTime) {
                            return new VncJavaObject(TimeUtil.convertLocalDateTimeToDate((LocalDateTime)date));
                        }
                        else if (date instanceof ZonedDateTime) {
                            return new VncJavaObject(TimeUtil.convertZonedDateTimeToDate((ZonedDateTime)date));
                        }
                    }

                    throw new VncException(String.format(
                            "Function 'time/date' does not allow %s as parameter",
                            Types.getType(val)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction date_Q =
        new VncFunction(
                "time/date?",
                VncFunction
                    .meta()
                    .arglists("(time/date? date)")
                    .doc("Returns true if date is a 'java.util.Date' else false")
                    .examples("(time/date? (time/date))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), Date.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // LocalDate
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction local_date =
        new VncFunction(
                "time/local-date",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/local-date)",
                        "(time/local-date year month day)",
                        "(time/local-date date)")
                    .doc("Creates a new local-date. A local-date is represented by 'java.time.LocalDate'")
                    .examples(
                        "(time/local-date)",
                        "(time/local-date 2018 8 1)",
                        "(time/local-date \"2018-08-01\")",
                        "(time/local-date (time/local-date-time 2018 8 1 14 20 10))",
                        "(time/local-date 1375315200000)",
                        "(time/local-date (. :java.util.Date :new))")
                    .seeAlso(
                        "time/local-date-time", "time/zoned-date-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 3);

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
                                    "Function 'time/local-date' does not allow %s as parameters",
                                    Types.getType(val)));
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
                                "Function 'time/local-date' does not allow %s as parameter",
                                Types.getType(val)));
                    }
                }
                else {
                    return new VncJavaObject(
                                LocalDate.of(
                                    Coerce.toVncLong(args.first()).getValue().intValue(),
                                    Coerce.toVncLong(args.second()).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(2)).getValue().intValue()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction local_date_Q =
        new VncFunction(
                "time/local-date?",
                VncFunction
                    .meta()
                    .arglists("(time/local-date? date)")
                    .doc("Returns true if date is a locale date ('java.time.LocalDate') else false")
                    .examples("(time/local-date? (time/local-date))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), LocalDate.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction local_date_parse =
        new VncFunction(
                "time/local-date-parse",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/local-date-parse str format",
                        "(time/local-date-parse str format locale")
                    .doc(
                        "Parses a local-date.                                            \n\n" +
                        "To parse a large number of dates a pre instantiated formatter   " +
                        "delivers best performance:                                      \n\n" +
                        "```                                                             \n" +
                        "(let [fmt (time/formatter \"yyyy-MM-dd\")]                      \n" +
                        "  (dotimes [n 100] (time/local-date-parse \"2018-12-01\" fmt))) \n" +
                        "```")
                    .examples(
                        "(time/local-date-parse \"2018-12-01\" \"yyyy-MM-dd\")",
                        "(time/local-date-parse \"2018-Dec-01\" \"yyyy-MMM-dd\" :ENGLISH)",
                        "(time/local-date-parse \"2018-12-01\" :iso)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncString date = Coerce.toVncString(args.first());
                final VncVal format = args.second();
                final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;

                final DateTimeFormatter formatter = isIsoFormat(format)
                                                      ? DateTimeFormatter.ISO_LOCAL_DATE
                                                      : getDateTimeFormatter(format);

                return new VncJavaObject(LocalDate.parse(date.getValue(), localize(formatter, locale)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // LocalDateTime
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction local_date_time =
        new VncFunction(
                "time/local-date-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/local-date-time)",
                        "(time/local-date-time year month day)",
                        "(time/local-date-time year month day hour minute second)",
                        "(time/local-date-time year month day hour minute second millis)",
                        "(time/local-date-time date)")
                    .doc("Creates a new local-date-time. A local-date-time is represented by 'java.time.LocalDateTime'")
                    .examples(
                        "(time/local-date-time)",
                        "(time/local-date-time 2018 8 1)",
                        "(time/local-date-time 2018 8 1 14 20 10)",
                        "(time/local-date-time 2018 8 1 14 20 10 200)",
                        "(time/local-date-time \"2018-08-01T14:20:10.200\")",
                        "(time/local-date-time (time/local-date 2018 8 1))",
                        "(time/local-date-time 1375315200000)",
                        "(time/local-date-time (. :java.util.Date :new))")
                    .seeAlso(
                        "time/local-date", "time/zoned-date-time")
                     .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 3, 6, 7);

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
                            return new VncJavaObject(((LocalDate)obj).atTime(0, 0, 0));
                        }
                        else if (obj instanceof Instant) {
                            return new VncJavaObject(
                                    LocalDateTime.ofInstant((Instant)obj, ZoneOffset.systemDefault()));
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'time/local-date-time' does not allow %s as parameters",
                                    Types.getType(val)));
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
                                "Function 'time/local-date-time' does not allow %s as parameter",
                                Types.getType(val)));
                    }
                }
                else if (args.size() == 3) {
                    return new VncJavaObject(
                            LocalDateTime.of(
                                Coerce.toVncLong(args.first()).getValue().intValue(),
                                Coerce.toVncLong(args.second()).getValue().intValue(),
                                Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                0, 0, 0, 0));
                }
                else if (args.size() == 6) {
                    return new VncJavaObject(
                            LocalDateTime.of(
                                Coerce.toVncLong(args.first()).getValue().intValue(),
                                Coerce.toVncLong(args.second()).getValue().intValue(),
                                Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(4)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(5)).getValue().intValue(),
                                0));
                }
                else {
                    return new VncJavaObject(
                            LocalDateTime.of(
                                Coerce.toVncLong(args.first()).getValue().intValue(),
                                Coerce.toVncLong(args.second()).getValue().intValue(),
                                Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(4)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(5)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(6)).getValue().intValue() * 1_000_000));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction local_date_time_Q =
        new VncFunction(
                "time/local-date-time?",
                VncFunction
                    .meta()
                    .arglists("(time/local-date-time? date)")
                    .doc("Returns true if date is a local-date-time  ('java.time.LocalDateTime') else false")
                    .examples("(time/local-date-time? (time/local-date-time))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                return VncBoolean.of(Types.isVncJavaObject(args.first(), LocalDateTime.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction local_date_time_parse =
        new VncFunction(
                "time/local-date-time-parse",
                VncFunction
                    .meta()
                    .arglists(
                            "(time/local-date-time-parse str format",
                            "(time/local-date-time-parse str format locale")
                    .doc(
                        "Parses a local-date-time.                                                     \n\n" +
                        "To parse a large number of dates a pre instantiated formatter                 " +
                        "delivers best performance:                                                    \n\n" +
                        "```                                                                           \n" +
                        "(let [fmt (time/formatter \"yyyy-MM-dd HH:mm:ss\")]                           \n" +
                        "  (dotimes [n 100] (time/local-date-time-parse \"2018-12-01 14:20:01\" fmt))) \n" +
                        "```")
                    .examples(
                        "(time/local-date-time-parse \"2018-08-01 14:20\" \"yyyy-MM-dd HH:mm\")",
                        "(time/local-date-time-parse \"2018-08-01 14:20:01.231\" \"yyyy-MM-dd HH:mm:ss.SSS\")",
                        "(time/local-date-time-parse \"2018-08-01T14:20:01.231\" :iso)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncString date = Coerce.toVncString(args.first());
                final VncVal format = args.second();
                final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;

                final DateTimeFormatter formatter = isIsoFormat(format)
                                                        ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                                        : getDateTimeFormatter(format);

                return new VncJavaObject(LocalDateTime.parse(date.getValue(), localize(formatter, locale)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // ZonedDateTime
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction zoned_date_time =
        new VncFunction(
                "time/zoned-date-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/zoned-date-time)",
                        "(time/zoned-date-time year month day)",
                        "(time/zoned-date-time year month day hour minute second)",
                        "(time/zoned-date-time year month day hour minute second millis)",
                        "(time/zoned-date-time date)",
                        "(time/zoned-date-time zone-id)",
                        "(time/zoned-date-time zone-id year month day)",
                        "(time/zoned-date-time zone-id year month day hour minute second)",
                        "(time/zoned-date-time zone-id year month day hour minute second millis)",
                        "(time/zoned-date-time zone-id date)")
                    .doc("Creates a new zoned-date-time. A zoned-date-time is represented by 'java.time.ZonedDateTime'")
                    .examples(
                        "(time/zoned-date-time)",
                        "(time/zoned-date-time 2018 8 1)",
                        "(time/zoned-date-time 2018 8 1 14 20 10)",
                        "(time/zoned-date-time 2018 8 1 14 20 10 200)",
                        "(time/zoned-date-time \"2018-08-01T14:20:10.200+01:00\")",
                        "(time/zoned-date-time (time/local-date 2018 8 1))",
                        "(time/zoned-date-time (time/local-date-time 2018 8 1 14 20 10))",
                        "(time/zoned-date-time 1375315200000)",
                        "(time/zoned-date-time (. :java.util.Date :new))",

                        "(time/zoned-date-time \"UTC\")",
                        "(time/zoned-date-time \"UTC\" 2018 8 1)",
                        "(time/zoned-date-time \"UTC\" 2018 8 1 14 20 10)",
                        "(time/zoned-date-time \"UTC\" 2018 8 1 14 20 10 200)",
                        "(time/zoned-date-time \"UTC\" \"2018-08-01T14:20:10.200+01:00\")",
                        "(time/zoned-date-time \"UTC\" (time/local-date 2018 8 1))",
                        "(time/zoned-date-time \"UTC\" (time/local-date-time 2018 8 1 14 20 10))",
                        "(time/zoned-date-time \"UTC\" 1375315200000)",
                        "(time/zoned-date-time \"UTC\" (. :java.util.Date :new))")
                    .seeAlso(
                        "time/local-date", "time/local-date-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArities(this, args, 0, 1, 2, 3, 4, 6, 7, 8);

                ZoneId zoneId = null;
                VncList argList = args;
                if (args.size() > 0) {
                    final VncVal val = args.first();
                    if (Types.isVncKeyword(val)) {
                        zoneId = ZoneId.of(((VncKeyword)val).getValue());
                        argList = args.rest();
                    }
                    else if (Types.isVncString(val)) {
                        final String s = ((VncString)val).getValue();
                        if (!s.isEmpty() && !Character.isDigit(s.charAt(0))) {
                            zoneId = ZoneId.of(s);
                            argList = args.rest();
                        }
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
                                    "Function 'time/zoned-date-time' does not allow %s as parameter",
                                    Types.getType(val)));
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
                                "Function 'time/zoned-date-time' does not allow %s as parameter",
                                Types.getType(val)));
                    }
                }
                else if (argList.size() == 3) {
                    return new VncJavaObject(
                            ZonedDateTime.of(
                                Coerce.toVncLong(argList.first()).getValue().intValue(),
                                Coerce.toVncLong(argList.second()).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(2)).getValue().intValue(),
                                0, 0, 0, 0,
                                orDefaultZone(zoneId)));
                }
                else if (argList.size() == 6) {
                    return new VncJavaObject(
                            ZonedDateTime.of(
                                Coerce.toVncLong(argList.first()).getValue().intValue(),
                                Coerce.toVncLong(argList.second()).getValue().intValue(),
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
                                Coerce.toVncLong(argList.first()).getValue().intValue(),
                                Coerce.toVncLong(argList.second()).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(2)).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(3)).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(4)).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(5)).getValue().intValue(),
                                Coerce.toVncLong(argList.nth(6)).getValue().intValue() * 1_000_000,
                                orDefaultZone(zoneId)));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction zoned_date_time_Q =
        new VncFunction(
                "time/zoned-date-time?",
                VncFunction
                    .meta()
                    .arglists("(time/zoned-date-time? date)")
                    .doc("Returns true if date is a zoned-date-time ('java.time.ZonedDateTime') else false")
                    .examples("(time/zoned-date-time? (time/zoned-date-time))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncVal val = args.first();
                return VncBoolean.of(Types.isVncJavaObject(val, ZonedDateTime.class));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction zoned_date_time_parse =
        new VncFunction(
                "time/zoned-date-time-parse",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/zoned-date-time-parse str format",
                        "(time/zoned-date-time-parse str format locale")
                    .doc(
                        "Parses a zoned-date-time.                                                           \n\n" +
                        "To parse a large number of dates a pre instantiated formatter                       " +
                        "delivers best performance:                                                          \n\n" +
                        "```                                                                                 \n" +
                        "(let [fmt (time/formatter \"yyyy-MM-dd'T'HH:mm:ssz\")]                              \n" +
                        "  (dotimes [n 100] (time/zoned-date-time-parse \"2018-12-01T14:20:01+01:00\" fmt))) \n" +
                        "```")
                    .examples(
                        "(time/zoned-date-time-parse \"2018-08-01T14:20:01+01:00\" \"yyyy-MM-dd'T'HH:mm:ssz\")",
                        "(time/zoned-date-time-parse \"2018-08-01T14:20:01.000+01:00\" \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
                        "(time/zoned-date-time-parse \"2018-08-01T14:20:01.000+01:00\" :iso)",
                        "(time/zoned-date-time-parse \"2018-08-01 14:20:01.000 +01:00\" \"yyyy-MM-dd' 'HH:mm:ss.SSS' 'z\")")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                final VncString date = Coerce.toVncString(args.first());
                final VncVal format = args.second();
                final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;

                final DateTimeFormatter formatter = isIsoFormat(format)
                                                        ? DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                                        : getDateTimeFormatter(format);

                return new VncJavaObject(ZonedDateTime.parse(date.getValue(), localize(formatter, locale)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Unix timestamp. Seconds since Jan 01 1970 (UTC).
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction unix_timestamp =
        new VncFunction(
                "time/unix-timestamp",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/unix-timestamp)",
                        "(time/unix-timestamp year month day)",
                        "(time/unix-timestamp year month day hour minute second)",
                        "(time/unix-timestamp year month day hour minute second millis)",
                        "(time/unix-timestamp date)")
                    .doc(
                        "Returns a unix timestamp. Seconds since Jan 01 1970 (UTC).\n\n" +
                        "See: [Unix Timestamp](https://www.unixtimestamp.com/)")
                    .examples(
                        "(time/unix-timestamp)",
                        "(time/unix-timestamp 2018 8 1)",
                        "(time/unix-timestamp 2018 8 1 14 20 10)",
                        "(time/unix-timestamp 2018 8 1 14 20 10 200)",
                        "(time/unix-timestamp \"2018-08-01T14:20:10.200\")",
                        "(time/unix-timestamp (time/local-date-time))",
                        "(time/unix-timestamp (time/local-date 2018 8 1))",
                        "(time/unix-timestamp (. :java.util.Date :new))")
                    .seeAlso(
                        "time/unix-timestamp-to-local-date-time",
                        "time/local-date-time",
                        "time/local-date",
                        "time/zoned-date-time")
                     .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0, 1, 3, 6, 7);

                if (args.size() == 0) {
                    return new VncLong(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                }
                else if (args.size() == 1) {
                    final VncVal val = args.first();
                    if (Types.isVncJavaObject(val)) {
                        final Object obj = ((VncJavaObject)val).getDelegate();
                        if (obj instanceof Date) {
                            final long millis = ((Date)obj).getTime();
                            return new VncLong(
                                            Instant.ofEpochMilli(millis)
                                                   .atZone(ZoneId.systemDefault())
                                                   .toLocalDateTime()
                                                   .toEpochSecond(ZoneOffset.UTC));
                        }
                        else if (obj instanceof ZonedDateTime) {
                            return new VncLong(((ZonedDateTime)obj).toLocalDateTime().toEpochSecond(ZoneOffset.UTC));
                        }
                        else if (obj instanceof LocalDateTime) {
                            return new VncLong(((LocalDateTime)obj).toEpochSecond(ZoneOffset.UTC));
                        }
                        else if (obj instanceof LocalDate) {
                            return new VncLong(((LocalDate)obj).atTime(0,0,0).toEpochSecond(ZoneOffset.UTC));
                        }
                        else if (obj instanceof Instant) {
                            return new VncLong(
                                    ((Instant)obj).getEpochSecond());
                        }
                        else {
                            throw new VncException(String.format(
                                    "Function 'time/unix-timestamp' does not allow %s as parameters",
                                    Types.getType(val)));
                        }
                    }
                    else if (Types.isVncString(val)) {
                        // ISO local date format "yyyy-mm-ddThh:MM:ss.SSS"
                        final String s = ((VncString)val).getValue();
                        return new VncJavaObject(LocalDateTime.parse(s));
                    }
                    else if (Types.isVncLong(val)) {
                        return val;
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'time/unix-timestamp' does not allow %s as parameter",
                                Types.getType(val)));
                    }
                }
                else if (args.size() == 3) {
                    return new VncLong(
                            LocalDateTime
                                .of(
                                    Coerce.toVncLong(args.first()).getValue().intValue(),
                                    Coerce.toVncLong(args.second()).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                    0, 0, 0, 0)
                                .toEpochSecond(ZoneOffset.UTC));
                }
                else if (args.size() == 6) {
                    return new VncLong(
                            LocalDateTime
                                .of(
                                    Coerce.toVncLong(args.first()).getValue().intValue(),
                                    Coerce.toVncLong(args.second()).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(4)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(5)).getValue().intValue(),
                                    0)
                                .toEpochSecond(ZoneOffset.UTC));
                }
                else {
                    return new VncLong(
                            LocalDateTime
                                .of(
                                    Coerce.toVncLong(args.first()).getValue().intValue(),
                                    Coerce.toVncLong(args.second()).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(4)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(5)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(6)).getValue().intValue() * 1_000_000)
                                .toEpochSecond(ZoneOffset.UTC));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction unix_timestamp_to_local_date_time =
        new VncFunction(
                "time/unix-timestamp-to-local-date-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/unix-timestamp-to-local-date-time seconds-since-epoch)")
                    .doc(
                        "Converts a unix timestamp (seconds since Jan 01 1970 (UTC)) to a " +
                        "java :LocalDateTime.\n\n" +
                        "See: [Unix Timestamp](https://www.unixtimestamp.com/)")
                    .examples(
                        "(time/unix-timestamp-to-local-date-time (time/unix-timestamp))")
                    .seeAlso(
                        "time/unix-timestamp")
                     .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final long seconds = Coerce.toVncLong(args.first()).toJavaLong();

                final LocalDateTime ts = Instant.ofEpochSecond(seconds)
                                                .atOffset(ZoneOffset.UTC)
                                                .toLocalDateTime();

                return new VncJavaObject(ts);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    ///////////////////////////////////////////////////////////////////////////
    // Between
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction between =
        new VncFunction(
                "time/between",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/between date1 date2 unit)")
                    .doc(
                        "Calculates the amount of time between two date/time values. Unit is " +
                        "one of :millis, :seconds, :minutes, :hours, :days, :weeks, :months, :years.\n\n" +
                        "Note: the units :millis, :seconds, :minutes, and :hours are not supported for local-date type.")
                    .examples(
                        "(time/between (time/local-date 2018 1 1) \n" +
                        "              (time/local-date 2019 1 1) \n" +
                        "              :days)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :seconds)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :minutes)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :hours)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :days)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :weeks)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :months)",
                        "(time/between (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-03-01T10:00:00.000\") \n" +
                        "              :years)",
                        "(time/between (time/zoned-date-time \"2018-01-01T10:00:00.000+01:00\") \n" +
                        "              (time/zoned-date-time \"2019-03-01T10:00:00.000+01:00\") \n" +
                        "              :months)")
                    .seeAlso(
                        "time/after?", "time/before?", "time/not-after?", "time/not-before?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 3);

                final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
                final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();
                final VncKeyword unit = Coerce.toVncKeyword(args.third());

                if (!(date1 instanceof Temporal)) {
                    throw new VncException(String.format(
                            "Function 'time/between' invalid date1 parameter %s. Must be a date/time",
                            Types.getType(args.first())));
                }
                if (!(date2 instanceof Temporal)) {
                    throw new VncException(String.format(
                            "Function 'time/between' invalid date2 parameter %s. Must be a date/time",
                            Types.getType(args.second())));
                }

                switch(unit.getSimpleName()) {
                    case "millis":
                        return new VncLong(MILLIS.between((Temporal)date1, (Temporal)date2));
                    case "seconds":
                        return new VncLong(SECONDS.between((Temporal)date1, (Temporal)date2));
                    case "minutes":
                        return new VncLong(MINUTES.between((Temporal)date1, (Temporal)date2));
                    case "hours":
                        return new VncLong(HOURS.between((Temporal)date1, (Temporal)date2));
                    case "days":
                        return new VncLong(DAYS.between((Temporal)date1, (Temporal)date2));
                    case "weeks":
                        return new VncLong(WEEKS.between((Temporal)date1, (Temporal)date2));
                    case "months":
                        return new VncLong(MONTHS.between((Temporal)date1, (Temporal)date2));
                    case "years":
                        return new VncLong(YEARS.between((Temporal)date1, (Temporal)date2));
                    default:
                        throw new VncException(String.format(
                                "Function 'time/between' invalid unit parameter ':%s'. Use one of" +
                                ":millis, :seconds, :minutes, :hours, :days, :weeks, :months, :years",
                                unit.getSimpleName()));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Compare
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction after_Q =
        new VncFunction(
                "time/after?",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/after? date1 date2)",
                        "(time/after? date1 date2 & more)")
                    .doc(
                        "Returns true if all dates are ordered from the latest to the earliest " +
                        "(same semantics as `>`)")
                    .examples(
                        "(time/after? (time/local-date 2019 1 1) \n" +
                        "             (time/local-date 2018 1 1))",
                        "(time/after? (time/local-date-time \"2019-01-01T10:00:00.000\") \n" +
                        "             (time/local-date-time \"2018-01-01T10:00:00.000\"))",
                        "(time/after? (time/zoned-date-time \"2019-01-01T10:00:00.000+01:00\") \n" +
                        "             (time/zoned-date-time \"2018-01-01T10:00:00.000+01:00\"))")
                    .seeAlso(
                        "time/before?", "time/not-after?", "time/not-before?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                boolean after = true;

                VncVal d1 = args.first();
                for(VncVal d2 : args.rest()) {
                    final Object date1 = Coerce.toVncJavaObject(d1).getDelegate();
                    final Object date2 = Coerce.toVncJavaObject(d2).getDelegate();

                    if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
                        after = after && ((ZonedDateTime)date1).isAfter((ZonedDateTime)date2);
                    }
                    else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
                        after = after && ((LocalDateTime)date1).isAfter((LocalDateTime)date2);
                    }
                    else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
                        after = after && ((LocalDate)date1).isAfter((LocalDate)date2);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'time/after?' does not allow %s %s as date1 / date2 parameter",
                                Types.getType(args.first()),
                                Types.getType(args.second())));
                    }

                    if (!after) break;
                    d1 = d2;
                }

                return VncBoolean.of(after);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_after_Q =
        new VncFunction(
                "time/not-after?",
                VncFunction
                    .meta()
                    .arglists("(time/not-after? date1 date2)")
                    .doc(
                        "Returns true if date1 is not-after date2 else false (same semantics as `<=`)")
                    .examples(
                        "(time/not-after? (time/local-date 2018 1 1) \n" +
                        "                 (time/local-date 2019 1 1))",
                        "(time/not-after? (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "                 (time/local-date-time \"2019-01-01T10:00:00.000\"))",
                        "(time/not-after? (time/zoned-date-time \"2018-01-01T10:00:00.000+01:00\") \n" +
                        "                 (time/zoned-date-time \"2019-01-01T10:00:00.000+01:00\"))")
                    .seeAlso(
                        "time/after?", "time/before?", "time/not-before?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
                final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();

                if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
                    return VncBoolean.of(!((ZonedDateTime)date1).isAfter((ZonedDateTime)date2));
                }
                else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
                    return VncBoolean.of(!((LocalDateTime)date1).isAfter((LocalDateTime)date2));
                }
                else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
                    return VncBoolean.of(!((LocalDate)date1).isAfter((LocalDate)date2));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/not-after?' does not allow %s %s as date1 / date2 parameter",
                            Types.getType(args.first()),
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction before_Q =
        new VncFunction(
                "time/before?",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/before? date1 date2)",
                        "(time/before? date1 date2 & more)")
                    .doc(
                        "Returns true if all dates are ordered from the earliest to the latest " +
                        "(same semantics as `<`)")
                    .examples(
                        "(time/before? (time/local-date 2018 1 1) \n" +
                        "              (time/local-date 2019 1 1))",
                        "(time/before? (time/local-date-time \"2018-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2019-01-01T10:00:00.000\"))",
                        "(time/before? (time/zoned-date-time \"2018-01-01T10:00:00.000+01:00\") \n" +
                        "              (time/zoned-date-time \"2019-01-01T10:00:00.000+01:00\"))")
                    .seeAlso(
                        "time/after?", "time/not-after?", "time/not-before?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 2);

                boolean before = true;

                VncVal d1 = args.first();
                for(VncVal d2 : args.rest()) {
                    final Object date1 = Coerce.toVncJavaObject(d1).getDelegate();
                    final Object date2 = Coerce.toVncJavaObject(d2).getDelegate();

                    if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
                        before = before && ((ZonedDateTime)date1).isBefore((ZonedDateTime)date2);
                    }
                    else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
                        before = before && ((LocalDateTime)date1).isBefore((LocalDateTime)date2);
                    }
                    else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
                        before = before && ((LocalDate)date1).isBefore((LocalDate)date2);
                    }
                    else {
                        throw new VncException(String.format(
                                "Function 'time/before?' does not allow %s, %s as date parameter",
                                Types.getType(d1),
                                Types.getType(d2)));
                    }

                    if (!before) break;
                    d1 = d2;
                }

                return VncBoolean.of(before);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction not_before_Q =
        new VncFunction(
                "time/not-before?",
                VncFunction
                    .meta()
                    .arglists("(time/not-before? date1 date2)")
                    .doc("Returns true if date1 is not-before date2 else false (same semantics as `>=`)")
                    .examples(
                        "(time/not-before? (time/local-date 2019 1 1) \n" +
                        "                  (time/local-date 2019 1 1))",
                        "(time/not-before? (time/local-date-time \"2019-01-01T10:00:00.000\") \n" +
                        "                  (time/local-date-time \"2018-01-01T10:00:00.000\"))",
                        "(time/not-before? (time/zoned-date-time \"2019-01-01T10:00:00.000+01:00\") \n" +
                        "                  (time/zoned-date-time \"2018-01-01T10:00:00.000+01:00\"))")
                    .seeAlso(
                        "time/after?", "time/before?", "time/not-after?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Object date1 = Coerce.toVncJavaObject(args.first()).getDelegate();
                final Object date2 = Coerce.toVncJavaObject(args.second()).getDelegate();

                if (date1 instanceof ZonedDateTime && date2 instanceof ZonedDateTime) {
                    return VncBoolean.of(!((ZonedDateTime)date1).isBefore((ZonedDateTime)date2));
                }
                else if (date1 instanceof LocalDateTime && date2 instanceof LocalDateTime) {
                    return VncBoolean.of(!((LocalDateTime)date1).isBefore((LocalDateTime)date2));
                }
                else if (date1 instanceof LocalDate && date2 instanceof LocalDate) {
                    return VncBoolean.of(!((LocalDate)date1).isBefore((LocalDate)date2));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/not-before?' does not allow %s %s as date1 / date2 parameter",
                            Types.getType(args.first()),
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Plus/Minus
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction plus =
        new VncFunction(
                "time/plus",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/plus date unit n)",
                        "(time/minus plus temporal)")
                    .doc(
                        "Adds the n units to the date. Units: {:years :months :weeks " +
                        ":days :hours :minutes :seconds :milliseconds}\n\n" +
                        "In the two argument version add a :java.time.Temporal (Period, Duration) " +
                        "to the date.")
                    .examples(
                        "(time/plus (time/local-date) :days 2)",
                        "(time/plus (time/local-date-time) :days 2)",
                        "(time/plus (time/zoned-date-time) :days 2)",
                        "(time/plus (time/local-date) (. :java.time.Period :ofDays 2))",
                        "(time/plus (time/local-date-time) (. :java.time.Period :ofDays 2))",
                        "(time/plus (time/zoned-date-time) (. :java.time.Period :ofDays 2))")
                    .seeAlso(
                        "time/minus")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (args.size() == 2) {
                    final Temporal temporal = Coerce.toVncJavaObject(args.first(), Temporal.class);
                    final TemporalAmount amount = Coerce.toVncJavaObject(args.second(), TemporalAmount.class);
                    return new VncJavaObject(temporal.plus(amount));
                }
                else {
                    final Temporal temporal = Coerce.toVncJavaObject(args.first(), Temporal.class);
                    final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.second()).getValue());
                    final long n = Coerce.toVncLong(args.nth(2)).getValue();

                    if (unit == null) {
                        throw new VncException(String.format(
                                "Function 'time/plus' invalid time unit %s",
                                Coerce.toVncKeyword(args.second()).getValue()));
                    }

                    return new VncJavaObject(temporal.plus(n, unit));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction minus =
        new VncFunction(
                "time/minus",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/minus date unit n)",
                        "(time/minus date temporal)")
                    .doc(
                        "Subtracts the n units from the date. Units: {:years :months :weeks " +
                        ":days :hours :minutes :seconds :milliseconds}\n\n" +
                        "In the two argument version subtracts a :java.time.Temporal (Period, Duration) " +
                        "from the date.")
                    .examples(
                        "(time/minus (time/local-date) :days 2)",
                        "(time/minus (time/local-date-time) :days 2)",
                        "(time/minus (time/zoned-date-time) :days 2)",
                        "(time/minus (time/local-date) (. :java.time.Period :ofDays 2))",
                        "(time/minus (time/local-date-time) (. :java.time.Period :ofDays 2))",
                        "(time/minus (time/zoned-date-time) (. :java.time.Period :ofDays 2))")
                    .seeAlso(
                        "time/plus")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (args.size() == 2) {
                    final Temporal temporal = Coerce.toVncJavaObject(args.first(), Temporal.class);
                    final TemporalAmount amount = Coerce.toVncJavaObject(args.second(), TemporalAmount.class);
                    return new VncJavaObject(temporal.minus(amount));
                }
                else {
                       final Temporal temporal = Coerce.toVncJavaObject(args.first(), Temporal.class);
                    final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.second()).getValue());
                    final long n = Coerce.toVncLong(args.nth(2)).getValue();

                    if (unit == null) {
                        throw new VncException(String.format(
                                "Function 'time/minus' invalid time unit %ss",
                                Coerce.toVncKeyword(args.second()).getValue()));
                    }

                    return new VncJavaObject(temporal.minus(n, unit));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction period =
        new VncFunction(
                "time/period",
                VncFunction
                    .meta()
                    .arglists("(time/period from to unit)")
                    .doc(
                        "Returns the period interval of two dates in the specified unit.¶" +
                        "Units: {:years :months :weeks :days :hours :minutes :seconds :milliseconds}")
                    .examples(
                        "(time/period (time/local-date) (time/plus (time/local-date) :days 3) :days)",
                        "(time/period (time/local-date-time) (time/plus (time/local-date-time) :days 3) :days)",
                        "(time/period (time/zoned-date-time) (time/plus (time/zoned-date-time) :days 3) :days)")
                    .seeAlso(
                        "time/local-date", "time/local-date-time", "time/zoned-date-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final Object from = Coerce.toVncJavaObject(args.first()).getDelegate();
                final Object to = Coerce.toVncJavaObject(args.second()).getDelegate();
                final ChronoUnit unit = toChronoUnit(Coerce.toVncKeyword(args.nth(2)).getValue());

                if (unit == null) {
                    throw new VncException(String.format(
                            "Function 'time/period' invalid time unit %s",
                            Coerce.toVncKeyword(args.second()).getValue()));
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
                            "Function 'time/period' does not allow %s %s as from / to parameter",
                            Types.getType(args.first()),
                            Types.getType(args.second())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction year =
        new VncFunction(
                "time/year",
                VncFunction
                    .meta()
                    .arglists("(time/year date)")
                    .doc("Returns the year of the date")
                    .examples(
                        "(time/year (time/local-date))",
                        "(time/year (time/local-date-time))",
                        "(time/year (time/zoned-date-time))")
                    .seeAlso(
                         "time/month",
                         "time/day-of-year",
                         "time/day-of-month", "time/first-day-of-month", "time/last-day-of-month",
                         "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/year' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction month =
        new VncFunction(
                "time/month",
                VncFunction
                    .meta()
                    .arglists("(time/month date)")
                    .doc("Returns the month of the date 1..12")
                    .examples(
                        "(time/month (time/local-date))",
                        "(time/month (time/local-date-time))",
                        "(time/month (time/zoned-date-time))")
                    .seeAlso(
                        "time/year",
                        "time/day-of-year",
                        "time/day-of-month", "time/first-day-of-month", "time/last-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/month' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction day_of_week =
        new VncFunction(
                "time/day-of-week",
                VncFunction
                    .meta()
                    .arglists("(time/day-of-week date)")
                    .doc("Returns the day of the week (:MONDAY ... :SUNDAY)")
                    .examples(
                        "(time/day-of-week (time/local-date))",
                        "(time/day-of-week (time/local-date-time))",
                        "(time/day-of-week (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/day-of-month", "time/first-day-of-month", "time/last-day-of-month")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/day-of-week' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction day_of_month =
        new VncFunction(
                "time/day-of-month",
                VncFunction
                    .meta()
                    .arglists("(time/day-of-month date)")
                    .doc("Returns the day of the month (1..31)")
                    .examples(
                        "(time/day-of-month (time/local-date))",
                        "(time/day-of-month (time/local-date-time))",
                        "(time/day-of-month (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/first-day-of-month", "time/last-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/day-of-month' does not allow %s as parameters",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction day_of_year =
        new VncFunction(
                "time/day-of-year",
                VncFunction
                    .meta()
                    .arglists("(time/day-of-year date)")
                    .doc("Returns the day of the year (1..366)")
                    .examples(
                        "(time/day-of-year (time/local-date))",
                        "(time/day-of-year (time/local-date-time))",
                        "(time/day-of-year (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-month", "time/first-day-of-month", "time/last-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/day-of-year' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction first_day_of_month =
        new VncFunction(
                "time/first-day-of-month",
                VncFunction
                    .meta()
                    .arglists("(time/first-day-of-month date)")
                    .doc("Returns the first day of a month as a local-date.")
                    .examples(
                        "(time/first-day-of-month (time/local-date))",
                        "(time/first-day-of-month (time/local-date-time))",
                        "(time/first-day-of-month (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/day-of-month", "time/last-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/first-day-of-month' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction last_day_of_month =
        new VncFunction(
                "time/last-day-of-month",
                VncFunction
                    .meta()
                    .arglists("(time/last-day-of-month date)")
                    .doc("Returns the last day of a month as a local-date.")
                    .examples(
                        "(time/last-day-of-month (time/local-date))",
                        "(time/last-day-of-month (time/local-date-time))",
                        "(time/last-day-of-month (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/day-of-month", "time/first-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/last-day-of-month' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction first_day_of_month_Q =
        new VncFunction(
                "time/first-day-of-month?",
                VncFunction
                    .meta()
                    .arglists("(time/first-day-of-month? date)")
                    .doc("Returns `true` if the date is the first day of a month otherwise `false`.")
                    .examples(
                        "(time/first-day-of-month? (time/local-date))",
                        "(time/first-day-of-month? (time/local-date-time))",
                        "(time/first-day-of-month? (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/day-of-month", "time/first-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (dt instanceof ZonedDateTime) {
                    final LocalDate date = ((ZonedDateTime)dt).toLocalDateTime().toLocalDate();
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == 1);
                }
                else if (dt instanceof LocalDateTime) {
                    final LocalDate date = ((LocalDateTime)dt).toLocalDate();
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == 1);
                }
                else if (dt instanceof LocalDate) {
                    final LocalDate date = ((LocalDate)dt);
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == 1);
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/last-day-of-month?' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction last_day_of_month_Q =
        new VncFunction(
                "time/last-day-of-month?",
                VncFunction
                    .meta()
                    .arglists("(time/last-day-of-month? date)")
                    .doc("Returns `true` if the date is the last day of a month otherwise `false`.")
                    .examples(
                        "(time/last-day-of-month? (time/local-date))",
                        "(time/last-day-of-month? (time/local-date-time))",
                        "(time/last-day-of-month? (time/zoned-date-time))")
                    .seeAlso(
                        "time/year", "time/month",
                        "time/day-of-year",
                        "time/day-of-month", "time/first-day-of-month",
                        "time/day-of-week")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (dt instanceof ZonedDateTime) {
                    final LocalDate date = ((ZonedDateTime)dt).toLocalDateTime().toLocalDate();
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == date.lengthOfMonth());
                }
                else if (dt instanceof LocalDateTime) {
                    final LocalDate date = ((LocalDateTime)dt).toLocalDate();
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == date.lengthOfMonth());
                }
                else if (dt instanceof LocalDate) {
                    final LocalDate date = ((LocalDate)dt);
                    final int dayOfMonth = date.getDayOfMonth();
                    return VncBoolean.of(dayOfMonth == date.lengthOfMonth());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/last-day-of-month?' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction hour =
        new VncFunction(
                "time/hour",
                VncFunction
                    .meta()
                    .arglists("(time/hour date)")
                    .doc("Returns the hour of the date 0..23")
                    .examples(
                        "(time/hour (time/local-date))",
                        "(time/hour (time/local-date-time))",
                        "(time/hour (time/zoned-date-time))")
                    .seeAlso(
                         "time/minute", "time/second", "time/milli")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/hour' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction minute =
        new VncFunction(
                "time/minute",
                VncFunction
                    .meta()
                    .arglists("(time/minute date)")
                    .doc("Returns the minute of the date 0..59")
                    .examples(
                        "(time/minute (time/local-date))",
                        "(time/minute (time/local-date-time))",
                        "(time/minute (time/zoned-date-time))")
                    .seeAlso(
                        "time/hour", "time/second", "time/milli")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/minute' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction second =
        new VncFunction(
                "time/second",
                VncFunction
                    .meta()
                    .arglists("(time/second date)")
                    .doc("Returns the second of the date 0..59")
                    .examples(
                        "(time/second (time/local-date))",
                        "(time/second (time/local-date-time))",
                        "(time/second (time/zoned-date-time))")
                    .seeAlso(
                        "time/hour", "time/minute", "time/milli")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/second' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction milli =
        new VncFunction(
                "time/milli",
                VncFunction
                    .meta()
                    .arglists("(time/milli date)")
                    .doc("Returns the millis of the date 0..999")
                    .examples(
                        "(time/milli (time/local-date))",
                        "(time/milli (time/local-date-time))",
                        "(time/milli (time/zoned-date-time))")
                    .seeAlso(
                        "time/hour", "time/minute", "time/second")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (date instanceof ZonedDateTime) {
                    return new VncLong(((ZonedDateTime)date).getNano() / 1_000_000);
                }
                else if (date instanceof LocalDateTime) {
                    return new VncLong(((LocalDateTime)date).getNano() / 1_000_000);
                }
                else if (date instanceof LocalDate) {
                    return new VncLong(0);
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/milli' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction length_of_year =
        new VncFunction(
                "time/length-of-year",
                VncFunction
                    .meta()
                    .arglists("(time/length-of-year date)")
                    .doc(
                        "Returns the length of the year represented by this date. \n\n" +
                        "This returns the length of the year in days, either 365 or 366.")
                    .examples(
                        "(time/length-of-year (time/local-date 2000 1 1))",
                        "(time/length-of-year (time/local-date 2001 1 1))",
                        "(time/length-of-year (time/local-date-time))",
                        "(time/length-of-year (time/zoned-date-time))")
                    .seeAlso(
                        "time/length-of-month", "time/leap-year?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (dt instanceof ZonedDateTime) {
                    return new VncLong(((ZonedDateTime)dt).toLocalDateTime().toLocalDate().lengthOfYear());
                }
                else if (dt instanceof LocalDateTime) {
                    return new VncLong(((LocalDateTime)dt).toLocalDate().lengthOfYear());
                }
                else if (dt instanceof LocalDate) {
                    return new VncLong(((LocalDate)dt).lengthOfYear());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/length-of-year' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction length_of_month =
        new VncFunction(
                "time/length-of-month",
                VncFunction
                    .meta()
                    .arglists("(time/length-of-month date)")
                    .doc(
                        "Returns the length of the month represented by this date.\n\n" +
                        "This returns the length of the month in days. " +
                        "For example, a date in January would return 31.")
                    .examples(
                        "(time/length-of-month (time/local-date 2000 2 1))",
                        "(time/length-of-month (time/local-date 2001 2 1))",
                        "(time/length-of-month (time/local-date-time))",
                        "(time/length-of-month (time/zoned-date-time))")
                    .seeAlso(
                        "time/length-of-year", "time/leap-year?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (dt instanceof ZonedDateTime) {
                    return new VncLong(((ZonedDateTime)dt).toLocalDateTime().toLocalDate().lengthOfMonth());
                }
                else if (dt instanceof LocalDateTime) {
                    return new VncLong(((LocalDateTime)dt).toLocalDate().lengthOfMonth());
                }
                else if (dt instanceof LocalDate) {
                    return new VncLong(((LocalDate)dt).lengthOfMonth());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/length-of-month' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction leap_yearQ =
        new VncFunction(
                "time/leap-year?",
                VncFunction
                    .meta()
                    .arglists("(time/leap-year? date)")
                    .doc("Checks if the year is a leap year.")
                    .examples(
                        "(time/leap-year? 2000)",
                        "(time/leap-year? (time/local-date 2000 1 1))",
                        "(time/leap-year? (time/local-date-time))",
                        "(time/leap-year? (time/zoned-date-time))")
                    .seeAlso(
                        "time/length-of-year", "time/length-of-month")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);


                if (args.first() instanceof VncLong) {
                    return VncBoolean.of(
                            LocalDate.of(Coerce.toVncLong(args.first()).getValue().intValue(), 1, 1)
                                     .isLeapYear());
                }

                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (dt instanceof ZonedDateTime) {
                    return VncBoolean.of(((ZonedDateTime)dt).toLocalDateTime().toLocalDate().isLeapYear());
                }
                else if (dt instanceof LocalDateTime) {
                    return VncBoolean.of(((LocalDateTime)dt).toLocalDate().isLeapYear());
                }
                else if (dt instanceof LocalDate) {
                    return VncBoolean.of(((LocalDate)dt).isLeapYear());
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/leap-year?' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    ///////////////////////////////////////////////////////////////////////////
    // Miscallenous
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction with_time =
        new VncFunction(
                "time/with-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/with-time date hour minute second)",
                        "(time/with-time date hour minute second millis)")
                    .doc("Sets the time of a date. Returns a new date")
                    .examples(
                        "(time/with-time (time/local-date) 22 00 15 333)",
                        "(time/with-time (time/local-date-time) 22 00 15 333)",
                        "(time/with-time (time/zoned-date-time) 22 00 15 333)")
                    .seeAlso(
                        "time/local-date", "time/local-date-time", "time/zoned-date-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 4, 5);

                final Object dt = Coerce.toVncJavaObject(args.first()).getDelegate();

                final int nanos = args.size() == 5 ? Coerce.toVncLong(args.nth(4)).getValue().intValue() * 1_000_000 : 0;

                if (dt instanceof ZonedDateTime) {
                    final ZonedDateTime date = ((ZonedDateTime)dt);
                    return new VncJavaObject(
                                date.withHour(Coerce.toVncLong(args.second()).getValue().intValue())
                                    .withMinute(Coerce.toVncLong(args.nth(2)).getValue().intValue())
                                    .withSecond(Coerce.toVncLong(args.nth(3)).getValue().intValue())
                                    .withNano(nanos));
                }
                else if (dt instanceof LocalDateTime) {
                    return new VncJavaObject(
                                ((LocalDateTime)dt).toLocalDate().atTime(
                                    Coerce.toVncLong(args.second()).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                    Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                    nanos));
                }
                else if (dt instanceof LocalDate) {
                    return new VncJavaObject(
                            ((LocalDate)dt).atTime(
                                Coerce.toVncLong(args.second()).getValue().intValue(),
                                Coerce.toVncLong(args.nth(2)).getValue().intValue(),
                                Coerce.toVncLong(args.nth(3)).getValue().intValue(),
                                nanos));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/with-time' does not allow %s as parameters",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction latest =
        new VncFunction(
                "time/latest",
                VncFunction
                    .meta()
                    .arglists("(time/latest coll)")
                    .doc(
                        "Returns the latest date from a collection of dates. " +
                        "All dates must be of equal type. The coll may be empty or nil.")
                    .examples(
                        "(time/latest [(time/local-date 2018 8 1) (time/local-date 2018 8 3)])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSequence seq = Coerce.toVncSequence(args.first());

                if (seq.isEmpty()) {
                    return Nil;
                }
                else if (seq.size() == 1) {
                    return seq.first();
                }
                else {
                    VncVal latest = seq.first();
                    for(VncVal date : seq.rest()) {
                        if (VncBoolean.isTrue(after_Q.apply(VncList.of(date, latest)))) {
                            latest = date;
                        }
                    }

                    return latest;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction earliest =
        new VncFunction(
                "time/earliest",
                VncFunction
                    .meta()
                    .arglists("(time/earliest coll)")
                    .doc(
                        "Returns the earliest date from a collection of dates. " +
                        "All dates must be of equal type. The coll may be empty or nil.")
                    .examples(
                        "(time/earliest [(time/local-date 2018 8 4) (time/local-date 2018 8 3)])")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final VncSequence seq = Coerce.toVncSequence(args.first());

                if (seq.isEmpty()) {
                    return Nil;
                }
                else if (seq.size() == 1) {
                    return seq.first();
                }
                else {
                    VncVal latest = seq.first();
                    for(VncVal date : seq.rest()) {
                        if (VncBoolean.isTrue(before_Q.apply(VncList.of(date, latest)))) {
                            latest = date;
                        }
                    }

                    return latest;
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction within_Q =
        new VncFunction(
                "time/within?",
                VncFunction
                    .meta()
                    .arglists("(time/within? date start end)")
                    .doc(
                        "Returns true if the date is after or equal to the start and is before or equal to the end. " +
                        "All three dates must be of the same type. The start and end date may each be nil meaning " +
                        "start is -infinity and end is +infinity. (same semantics as `start <= date <= end`)")
                    .examples(
                        "(time/within? (time/local-date 2018 8 15) \n" +
                        "              (time/local-date 2018 8 10) \n" +
                        "              (time/local-date 2018 8 20))",
                        "(time/within? (time/local-date 2018 8 25) \n" +
                        "              (time/local-date 2018 8 10) \n" +
                        "              (time/local-date 2018 8 20))",
                        "(time/within? (time/local-date 2018 8 20) \n" +
                        "              (time/local-date 2018 8 10) \n" +
                        "              nil)",
                        "(time/within? (time/local-date-time \"2019-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2010-01-01T10:00:00.000\") \n" +
                        "              (time/local-date-time \"2020-01-01T10:00:00.000\"))",
                        "(time/within? (time/zoned-date-time \"2010-01-01T10:00:00.000+01:00\") \n" +
                        "              (time/zoned-date-time \"2019-01-01T10:00:00.000+01:00\") \n" +
                        "              (time/zoned-date-time \"2020-01-01T10:00:00.000+01:00\"))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final VncVal date = args.first();
                final VncVal start = args.second();
                final VncVal end = args.third();

                if (start == Nil && end == Nil) {
                    return True;
                }
                else if (start != Nil && end != Nil) {
                    return VncBoolean.of(
                            ((VncBoolean.isTrue(not_before_Q.apply(VncList.of(date, start))))
                              && (VncBoolean.isTrue(not_after_Q.apply(VncList.of(date, end))))));
                }
                else if (start != Nil) {
                    return not_before_Q.apply(VncList.of(date, start));
                }
                else {
                    return not_after_Q.apply(VncList.of(date, end));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction zone =
        new VncFunction(
                "time/zone",
                VncFunction
                    .meta()
                    .arglists("(time/zone date)")
                    .doc("Returns the zone of the date")
                    .examples("(time/zone (time/zoned-date-time))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final Object date = Coerce.toVncJavaObject(args.first()).getDelegate();

                if (date instanceof ZonedDateTime) {
                    return new VncString(((ZonedDateTime)date).getZone().getId());
                }
                else if (date instanceof LocalDateTime) {
                    return Nil;
                }
                else if (date instanceof LocalDate) {
                    return Nil;
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/zone' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction zone_offset =
        new VncFunction(
                "time/zone-offset",
                VncFunction
                    .meta()
                    .arglists("(time/zone-offset date)")
                    .doc("Returns the zone-offset of the date in minutes")
                    .examples("(time/zone-offset (time/zoned-date-time))")
                    .seeAlso("time/zoned-date-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/zone-offset' does not allow %s as parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Formatter
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction formatter =
        new VncFunction(
                "time/formatter",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/formatter format)",
                         "(time/formatter format locale)")
                    .doc("Creates a formatter")
                    .examples(
                        "(time/formatter \"dd-MM-yyyy\")",
                        "(time/formatter \"dd-MM-yyyy\" :en_EN)",
                        "(time/formatter \"dd-MM-yyyy\" \"en_EN\")",
                        "(time/formatter \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
                        "(time/formatter :ISO_OFFSET_DATE_TIME)")
                    .seeAlso("time/format")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                // locale
                final Locale locale = args.size() == 2 ? getLocale(args.second()) : null;

                // formatter
                return new VncJavaObject(localize(getDateTimeFormatter(args.first()), locale));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction format =
        new VncFunction(
                "time/format",
                VncFunction
                    .meta()
                    .arglists(
                        "(time/format date format)",
                        "(time/format date format locale)",
                        "(time/format date formatter)",
                        "(time/format date formatter locale)")
                    .doc(
                        "Formats a date with a format.                                  \n\n" +
                        "To format a large number of dates a pre instantiated formatter " +
                        "delivers best performance:                                     \n\n" +
                        "```                                                            \n" +
                        "(let [fmt (time/formatter \"yyyy-MM-dd'T'HH:mm:ss\")]          \n" +
                        "  (dotimes [n 100] (time/format (time/local-date-time) fmt)))  \n" +
                        "```")
                    .examples(
                        "(time/format (time/local-date) \"dd-MM-yyyy\")",
                        "(time/format (time/local-date) (time/formatter \"dd-MM-yyyy\"))",
                        "(time/format (time/local-date) :iso)",
                        "(time/format (time/local-date-time) \"yyyy-MM-dd'T'HH:mm:ss\")",
                        "(time/format (time/local-date-time) (time/formatter \"yyyy-MM-dd'T'HH:mm:ss\"))",
                        "(time/format (time/local-date-time) :iso)",
                        "(time/format (time/zoned-date-time) \"yyyy-MM-dd'T'HH:mm:ss.SSSz\")",
                        "(time/format (time/zoned-date-time) :iso)",
                        "(time/format (time/zoned-date-time) (time/formatter \"yyyy-MM-dd'T'HH:mm:ss.SSSz\"))")
                    .seeAlso("time/formatter")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2, 3);

                if (!Types.isVncJavaObject(args.first())) {
                    throw new VncException(String.format(
                            "Function 'time/format' does not allow %s as date parameter",
                            Types.getType(date)));
                }

                // locale
                final Locale locale = args.size() == 3 ? getLocale(args.nth(2)) : null;

                // formatter
                final VncVal format = args.second();

                // format
                final Object date = ((VncJavaObject)args.first()).getDelegate();
                if (date instanceof Date) {
                    final ZonedDateTime dt = Instant.ofEpochMilli(((Date)date).getTime())
                                                    .atZone(ZoneId.systemDefault());

                    final DateTimeFormatter formatter =
                            isIsoFormat(format)
                                ? DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                : localize(getDateTimeFormatter(format), locale);

                    return new VncString(dt.format(formatter));
                }
                else if (date instanceof ZonedDateTime) {
                    final DateTimeFormatter formatter =
                            isIsoFormat(format)
                                ? DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                : localize(getDateTimeFormatter(format), locale);

                    return new VncString(((ZonedDateTime)date).format(formatter));
                }
                else if (date instanceof LocalDateTime) {
                    final DateTimeFormatter formatter =
                            isIsoFormat(format)
                                ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                : localize(getDateTimeFormatter(format), locale);

                    return new VncString(((LocalDateTime)date).format(formatter));
                }
                else if (date instanceof LocalDate) {
                    final DateTimeFormatter formatter =
                            isIsoFormat(format)
                                ? DateTimeFormatter.ISO_LOCAL_DATE
                                : localize(getDateTimeFormatter(format), locale);

                    return new VncString(((LocalDate)date).format(formatter));
                }
                else {
                    throw new VncException(String.format(
                            "Function 'time/format' does not allow %s as date parameter",
                            Types.getType(args.first())));
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    ///////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction zone_ids =
        new VncFunction(
                "time/zone-ids",
                VncFunction
                    .meta()
                    .arglists("(time/zone-ids)")
                    .doc("Returns all available zone ids with time offset")
                    .examples("(nfirst (seq (time/zone-ids)) 10)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);


                final List<String> zoneList = new ArrayList<>(ZoneId.getAvailableZoneIds());

                //Get all ZoneIds
                final Map<String, String> allZoneIds = getAllZoneIds(zoneList);


                //sort map by key
                final LinkedHashMap<VncVal,VncVal> map = new LinkedHashMap<>();
                allZoneIds
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> map.put(
                                            new VncString(e.getKey()),
                                            new VncString(e.getValue())));

                return new VncOrderedMap(map);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction to_millis =
        new VncFunction(
                "time/to-millis",
                VncFunction
                    .meta()
                    .arglists("(time/to-millis date)")
                    .doc("Converts the passed date to milliseconds since epoch")
                    .examples(
                        "(time/to-millis (time/date))",
                        "(time/to-millis (time/local-date))",
                        "(time/to-millis (time/local-date-time))",
                        "(time/to-millis (time/zoned-date-time))")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

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
                            "Function 'time/to-millis' does not allow %s as parameter",
                            Types.getType(val)));
            }

            private static final long serialVersionUID = -1848883965231344442L;
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
        else if (Types.isVncJavaObject(locale, Locale.class)) {
            return (Locale)((VncJavaObject)locale).getDelegate();
        }

        throw new VncException(String.format(
                "The type %s does not define a Locale.",
                Types.getType(locale)));
    }

    private static DateTimeFormatter getDateTimeFormatter(final VncVal fmt) {
        if (Types.isVncKeyword(fmt)) {
            return getPredefinedDateTimeFormatter((VncKeyword)fmt);
        }
        else if (Types.isVncString(fmt)) {
            return DateTimeFormatter.ofPattern(((VncString)fmt).getValue());
        }
        else if (Types.isVncJavaObject(fmt, DateTimeFormatter.class)) {
            return (DateTimeFormatter)((VncJavaObject)fmt).getDelegate();
        }
        else {
            throw new VncException(String.format(
                "Function 'time/format' does not allow %s as format parameter.",
                Types.getType(fmt)));
        }
    }

    private static DateTimeFormatter getPredefinedDateTimeFormatter(final VncKeyword fmt) {
        final String fmtName = fmt.getValue();
        try {
            return (DateTimeFormatter)ReflectionAccessor.getStaticField(DateTimeFormatter.class, fmtName).getValue();
        }
        catch(Exception ex) {
            throw new VncException(String.format(
                    "'%s' is not a predefined DateTimeFormatter.",
                    fmtName));
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

    private static boolean isIsoFormat(final VncVal format) {
        return Types.isVncKeyword(format)
                ? "iso".equalsIgnoreCase(((VncKeyword)format).getValue())
                : false;
    }

    private static ZoneId orDefaultZone(final ZoneId zoneId) {
        return zoneId == null ? ZoneId.systemDefault() : zoneId;
    }

    private static ChronoUnit toChronoUnit(final String unit) {
        switch(unit) {
            case "years":   return ChronoUnit.YEARS;
            case "year":    return ChronoUnit.YEARS;

            case "months":  return ChronoUnit.MONTHS;
            case "month":   return ChronoUnit.MONTHS;

            case "weeks":   return ChronoUnit.WEEKS;
            case "week":    return ChronoUnit.WEEKS;

            case "days":    return ChronoUnit.DAYS;
            case "day":     return ChronoUnit.DAYS;

            case "hours":   return ChronoUnit.HOURS;
            case "hour":    return ChronoUnit.HOURS;

            case "minutes": return ChronoUnit.MINUTES;
            case "minute":  return ChronoUnit.MINUTES;

            case "seconds": return ChronoUnit.SECONDS;
            case "second":  return ChronoUnit.SECONDS;

            case "millis":  return ChronoUnit.MILLIS;
            case "milli":   return ChronoUnit.MILLIS;

            default:        return null;
        }
    }



    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(date)
                    .add(date_Q)
                    .add(local_date)
                    .add(local_date_Q)
                    .add(local_date_parse)
                    .add(local_date_time)
                    .add(local_date_time_Q)
                    .add(local_date_time_parse)
                    .add(zoned_date_time)
                    .add(zoned_date_time_Q)
                    .add(zoned_date_time_parse)
                    .add(unix_timestamp)
                    .add(unix_timestamp_to_local_date_time)
                    .add(with_time)
                    .add(zone_ids)
                    .add(to_millis)
                    .add(formatter)
                    .add(format)
                    .add(plus)
                    .add(minus)
                    .add(period)
                    .add(year)
                    .add(month)
                    .add(day_of_week)
                    .add(day_of_month)
                    .add(day_of_year)
                    .add(first_day_of_month)
                    .add(last_day_of_month)
                    .add(first_day_of_month_Q)
                    .add(last_day_of_month_Q)
                    .add(hour)
                    .add(minute)
                    .add(second)
                    .add(milli)
                    .add(leap_yearQ)
                    .add(length_of_year)
                    .add(length_of_month)
                    .add(zone)
                    .add(zone_offset)
                    .add(between)
                    .add(after_Q)
                    .add(not_after_Q)
                    .add(before_Q)
                    .add(not_before_Q)
                    .add(earliest)
                    .add(latest)
                    .add(within_Q)
                    .add(within_Q)

                    .toMap();
}
