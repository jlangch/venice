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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.jlangch.venice.Version;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


public class CoreSystemFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // System
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction version =
        new VncFunction(
                "version",
                VncFunction
                    .meta()
                    .arglists("(version)")
                    .doc("Returns the Venice version.")
                    .examples("(version)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncString(Version.VERSION);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction uuid =
        new VncFunction(
                "uuid",
                VncFunction
                    .meta()
                    .arglists("(uuid)")
                    .doc("Generates a UUID.")
                    .examples("(uuid)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);
                return new VncString(UUID.randomUUID().toString());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction objid =
        new VncFunction(
                "objid",
                VncFunction
                    .meta()
                    .arglists("(objid)")
                    .doc("Returns the original unique hash code for the given object.")
                    .examples("(objid x)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);
                return new VncLong(System.identityHashCode(args.first()));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    public static VncFunction current_time_millis =
        new VncFunction(
                "current-time-millis",
                VncFunction
                    .meta()
                    .arglists("(current-time-millis)")
                    .doc("Returns the current time in milliseconds.")
                    .examples("(current-time-millis)")
                    .seeAlso("nano-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncLong(System.currentTimeMillis());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction nano_time =
        new VncFunction(
                "nano-time",
                VncFunction
                    .meta()
                    .arglists("(nano-time)")
                    .doc(
                        "Returns the current value of the running Java Virtual Machine's " +
                        "high-resolution time source, in nanoseconds.")
                    .examples(
                        "(nano-time)",
                        "(let [t (nano-time)                        \n" +
                        "      _ (sleep 100)                        \n" +
                        "      e (nano-time)]                       \n" +
                        "  (format-nano-time (- e t) :precision 2))  ")
                    .seeAlso(
                        "current-time-millis", "format-nano-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncLong(System.nanoTime());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction format_milli_time =
        new VncFunction(
                "format-milli-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(format-milli-time time)",
                        "(format-milli-time time & options)")
                    .doc(
                        "Formats a time given in milliseconds as long or double. \n\n" +
                        "Options: \n\n" +
                        "| :precision p | e.g :precision 4 (defaults to 3)|\n")
                    .examples(
                        "(format-milli-time 203)",
                        "(format-milli-time 20389.0 :precision 2)",
                        "(format-milli-time 20389 :precision 2)",
                        "(format-milli-time 20389 :precision 0)")
                    .seeAlso("format-micro-time", "format-nano-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncVal val = args.first();

                if (Types.isVncLong(val) || Types.isVncInteger(val)) {
                    final long time = VncLong.of(val).getValue();

                    if (time < 1_000) {
                        return new VncString(String.format("%dms", time));
                    }
                }

                final VncHashMap options = VncHashMap.ofAll(args.rest());
                final int precision = Coerce.toVncLong(options.get(new VncKeyword("precision"), new VncLong(3)))
                                            .getIntValue();

                final double time = VncDouble.of(val).getValue();

                String unit = "s";
                double scale = 1_000.0D;

                if (time < 1_000.0D) {
                    unit = "ms";
                    scale = 1.0D;
                }

                return new VncString(String.format("%." + precision + "f" + unit, time / scale));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction format_micro_time =
        new VncFunction(
                "format-micro-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(format-micro-time time)",
                        "(format-micro-time time & options)")
                    .doc(
                        "Formats a time given in microseconds as long or double. \n\n" +
                        "Options: \n\\n" +
                        "| :precision p | e.g :precision 4 (defaults to 3)|")
                    .examples(
                        "(format-micro-time 203)",
                        "(format-micro-time 20389.0 :precision 2)",
                        "(format-micro-time 20389 :precision 2)",
                        "(format-micro-time 20389 :precision 0)",
                        "(format-micro-time 20386766)",
                        "(format-micro-time 20386766 :precision 2)",
                        "(format-micro-time 20386766 :precision 6)")
                    .seeAlso("format-milli-time", "format-nano-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncVal val = args.first();

                if (Types.isVncLong(val) || Types.isVncInteger(val)) {
                    final long time = VncLong.of(val).getValue();

                    if (time < 1_000) {
                        return new VncString(String.format("%dµs", time));
                    }
                }

                final VncHashMap options = VncHashMap.ofAll(args.rest());
                final int precision = Coerce.toVncLong(options.get(new VncKeyword("precision"), new VncLong(3)))
                                            .getIntValue();

                final double time = VncDouble.of(val).getValue();

                String unit = "s";
                double scale = 1_000_000.0D;

                if (time < 1_000.0D) {
                    unit = "µs";
                    scale = 1.0D;
                }
                else if (time < 1_000_000.0D) {
                    unit = "ms";
                    scale = 1_000_000.0D;
                }

                return new VncString(String.format("%." + precision + "f" + unit, time / scale));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction format_nano_time =
        new VncFunction(
                "format-nano-time",
                VncFunction
                    .meta()
                    .arglists(
                        "(format-nano-time time)",
                        "(format-nano-time time & options)")
                    .doc(
                        "Formats a time given in nanoseconds as long or double. \n\n" +
                        "Options: \n\n" +
                        "| :precision p | e.g :precision 4 (defaults to 3)|")
                    .examples(
                        "(format-nano-time 203)",
                        "(format-nano-time 20389.0 :precision 2)",
                        "(format-nano-time 20389 :precision 2)",
                        "(format-nano-time 20389 :precision 0)",
                        "(format-nano-time 203867669)",
                        "(format-nano-time 20386766988 :precision 2)",
                        "(format-nano-time 20386766988 :precision 6)")
                    .seeAlso("format-milli-time", "format-micro-time", "nano-time")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertMinArity(this, args, 1);

                final VncVal val = args.first();

                if (Types.isVncLong(val) || Types.isVncInteger(val)) {
                    final long time = VncLong.of(val).getValue();

                    if (time < 1_000) {
                        return new VncString(String.format("%dns", time));
                    }
                }

                final VncHashMap options = VncHashMap.ofAll(args.rest());
                final int precision = Coerce.toVncLong(options.get(new VncKeyword("precision"), new VncLong(3)))
                                            .getIntValue();

                final double time = VncDouble.of(val).getValue();

                String unit = "s";
                double scale = 1_000_000_000.0D;

                if (time < 1_000.0D) {
                    unit = "ns";
                    scale = 1.0D;
                }
                else if (time < 1_000_000.0D) {
                    unit = "µs";
                    scale = 1_000.0D;
                }
                else if (time < 1_000_000_000.0D) {
                    unit = "ms";
                    scale = 1_000_000.0D;
                }

                return new VncString(String.format("%." + precision + "f" + unit, time / scale));
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sleep =
        new VncFunction(
                "sleep",
                VncFunction
                    .meta()
                    .arglists(
                        "(sleep n)",
                        "(sleep n time-unit)")
                    .doc(
                        "Sleep for the time n. The default time unit is milliseconds.¶" +
                        "Time unit is one of :milliseconds, :seconds, :minutes, :hours, or :days or " +
                        "their abbreviations :msec, :ms, :sec, :s, :min, :hr, :h, :d.")
                    .examples(
                        "(sleep 30)",
                        "(sleep 30 :milliseconds)",
                        "(sleep 30 :msec)",
                        "(sleep 5 :seconds)",
                        "(sleep 5 :sec)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                try {
                    final long sleep = Coerce.toVncLong(args.first()).getValue();
                    final TimeUnit unit = args.size() == 1
                                            ? TimeUnit.MILLISECONDS
                                            : toTimeUnit(Coerce.toVncKeyword(args.second()));

                    Thread.sleep(unit.toMillis(Math.max(0,sleep)));
                }
                catch(InterruptedException ex) {
                    throw new com.github.jlangch.venice.InterruptedException("interrupted while calling (sleep n)", ex);
                }

                return Nil;
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction charset_default_encoding =
        new VncFunction(
                "charset-default-encoding",
                VncFunction
                    .meta()
                    .arglists("(charset-default-encoding)")
                    .doc("Returns the default charset of this Java virtual machine.")
                    .examples("(charset-default-encoding)")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return new VncKeyword(Charset.defaultCharset().name());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    private static TimeUnit toTimeUnit(final VncKeyword unit) {
        switch(unit.getValue()) {
            case "milliseconds":
            case "msec":
            case "ms":
                return TimeUnit.MILLISECONDS;

            case "seconds":
            case "sec":
            case "s":
                return TimeUnit.SECONDS;

            case "minutes":
            case "min":
                return TimeUnit.MINUTES;

            case "hours":
            case "hr":
            case "h":
                return TimeUnit.HOURS;

            case "days":
            case "d":
                return TimeUnit.DAYS;

            default:
                throw new VncException(
                        "Invalid scheduler time-unit " + unit.toString() + ". "
                            + "Use one of {:milliseconds, :seconds, :minutes, :hours, :days} "
                            + "or the abbreviations {:msec, :ms, :sec, :s, :min, :hr, :h, :d}");
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(uuid)
                    .add(objid)
                    .add(current_time_millis)
                    .add(nano_time)
                    .add(format_nano_time)
                    .add(format_micro_time)
                    .add(format_milli_time)
                    .add(sleep)
                    .add(version)
                    .add(charset_default_encoding)
                    .toMap();



    public static final VncKeyword CALLSTACK_KEY_FN_NAME = new VncKeyword(":fn-name");
    public static final VncKeyword CALLSTACK_KEY_FILE = new VncKeyword(":file");
    public static final VncKeyword CALLSTACK_KEY_LINE = new VncKeyword(":line");
    public static final VncKeyword CALLSTACK_KEY_COL = new VncKeyword(":col");

    public static final AtomicInteger SYSTEM_EXIT_CODE = new AtomicInteger(0);
}
